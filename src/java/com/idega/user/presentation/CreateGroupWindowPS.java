package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupDomainRelationType;
import com.idega.user.data.GroupDomainRelationTypeHome;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;
import com.idega.user.event.CreateGroupEvent;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class CreateGroupWindowPS extends IWPresentationStateImpl implements IWActionListener{

  private boolean _close = false ;

  private String _groupName = null;
  private String _groupDescription = null;
  private String _groupType = null;
  private IWResourceBundle iwrb;
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
//  private String _groupParentID = null;


  public CreateGroupWindowPS() {
  }


  public void reset(){
    _groupName = null;
    _groupDescription = null;
    _groupType = null;
    _close = false;
  }


  public String getGroupName(){
    return _groupName;
  }

  public String getGroupDescription(){
    return _groupDescription;
  }

  public String getGroupType(){
    return _groupType;
  }

  public boolean doClose(){
    return _close;
  }


  public void doneClosing(){
    _close = false;
  }




  public void actionPerformed(IWPresentationEvent e) throws IWException {
//    System.out.println("[CreateGroupWindowPS]: ps = "+this);
//    System.out.println("[CreateGroupWindowPS] : event = " + e);
		iwrb = e.getIWContext().getApplication().getBundle(IW_BUNDLE_IDENTIFIER).getResourceBundle(e.getIWContext());

    if(e instanceof CreateGroupEvent ){
//      System.out.println("[CreateGroupWindowPS] : (e instanceof CreateGroupEvent) = true");
      CreateGroupEvent event = (CreateGroupEvent)e;

//      System.out.println("[CreateGroupWindowPS] : event.doCommit() = "+event.doCommit());
//      System.out.println("[CreateGroupWindowPS] : event.doCancel() = "+event.doCancel());

      if(event.doCommit()){
		try
		{
			GroupBusiness business = (GroupBusiness)IBOLookup.getServiceInstance(e.getIWContext(),GroupBusiness.class);
			Group group = business.createGroup(event.getName(),event.getDescription(),event.getGroupType(),event.getHomePageID(),event.getAliasID());

			//set current user a owner of group
			setCurrentUserAsOwnerOfGroup(e.getIWContext(), group);
			//get groupType tree and iterate through it and create default sub groups.
			createDefaultSubGroups(group,business,e.getIWContext());
					
			
			// Create under
			if(event.getParentType() == CreateGroupEvent.TYPE_DOMAIN){  // under Domain
			  GroupDomainRelationTypeHome gdrHome = (GroupDomainRelationTypeHome)IDOLookup.getHome(GroupDomainRelationType.class);
			  IBDomain domain = (IBDomain)IDOLookup.findByPrimaryKey(IBDomain.class,event.getParentID());
			  business.addGroupUnderDomain(domain,group,gdrHome.getTopNodeRelationType());
			} else if(event.getParentType() == CreateGroupEvent.TYPE_GROUP){ // under other group
			  Group parentGroup = (Group)IDOLookup.findByPrimaryKey(Group.class,event.getParentID());
			  parentGroup.addGroup(group);
			} else {
			  System.err.println("[CreateGroupWindow]: parentGroupType "+event.getParentType()+"not found");
			}
			
			//TODO fix this
			e.getIWContext().getApplicationContext().setApplicationAttribute("domain_group_tree",null);
			e.getIWContext().getApplicationContext().setApplicationAttribute("group_tree",null);			
		}
		catch (CreateException ce)
		{
			throw new EJBException(ce);
		}
		catch (RemoteException ex)
		{
			throw new EJBException(ex);
		}
		catch (FinderException fe)
		{
			throw new EJBException(fe);
		}
    this.fireStateChanged();


      } else if(event.doCancel()){
        this.reset();
        _close = true;
        this.fireStateChanged();
      } else {
        _groupName = event.getName();
        _groupDescription = event.getDescription();
        _groupType = event.getGroupType();
      }

    }
  }
	/**
	 * Method createDefaultSubGroups.
	 * @param group
	 * @param business
	 * @param iWContext
	 */
	private void createDefaultSubGroups(Group group, GroupBusiness business, IWContext iwc) throws RemoteException{
		GroupType type;
		try {
			type = business.getGroupTypeHome().findByPrimaryKey(group.getGroupType());
		}
		catch (FinderException e) {
			e.printStackTrace();
			return;
		}
		
		
		Iterator iterator = type.getChildren();
			
		while (iterator!=null && iterator.hasNext()) {
			GroupType gType = (GroupType) iterator.next();
			String name = gType.getDefaultGroupName();
			
			String typeString = gType.getType();
			System.out.println("TYPE :" +typeString );
			//to avoid circular reference with beginning type
			//if( this.getGroupType().equals(typeString) ) continue; rather add all types to a map to check
			
			if(name==null){
				name =  iwrb.getLocalizedString(typeString,typeString);
			}
			//create group then call recursive
			try {
				Group newGroup = business.createGroup(name,gType.getDescription(),typeString);
				setCurrentUserAsOwnerOfGroup(iwc,newGroup);
				group.addGroup(newGroup);
				if(!type.isLeaf()){
					createDefaultSubGroups(newGroup,business,iwc);
				}
							
				
			}
			catch (CreateException e) {
				e.printStackTrace();
				return;
			}
			
			
		}
	}
  
  private void setCurrentUserAsOwnerOfGroup(IWUserContext iwc, Group group){
		User user = iwc.getCurrentUser();
		AccessController access = iwc.getAccessController();
		try {
			access.setAsOwner(group,((Integer)user.getPrimaryKey()).intValue(), iwc);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		
		}
  }
}