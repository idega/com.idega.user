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
  
  private Integer groupId = null;
  private IWContext eventContext = null;

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
    groupId = null;
    eventContext = null;
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
  
  public Integer getGroupId() {
    return groupId;
  }
  
  public IWContext getEventContext()  {
    return eventContext;
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
      // store group id and context, so change listners are able to open windows (e.g. the group property window)
      groupId = (Integer) group.getPrimaryKey();
      eventContext = e.getIWContext();
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
			e.getIWContext().getApplicationContext().removeApplicationAttribute("domain_group_tree");
			e.getIWContext().getApplicationContext().removeApplicationAttribute("group_tree");			
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
    // forget everything
    reset();


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
			
			if(gType.getAutoCreate()){
				Integer numberOfInstances = gType.getNumberOfInstancesToAutoCreate();
				int nrOfGroupsToCreate = 1;
				
				if( (numberOfInstances != null) && (numberOfInstances.intValue()>1) ){
					nrOfGroupsToCreate = numberOfInstances.intValue();
				}
				
				for(int i = 1 ; i<=nrOfGroupsToCreate ; i++){
					String typeString = gType.getType();
					String typeLocalizingKey = "auto.create.name."+typeString;
					String defaultValue = gType.getDescription();
					if( (defaultValue == null ) || ("".equals(defaultValue)) ) defaultValue = typeString;
		
					//to avoid circular reference with beginning type
					//if( this.getGroupType().equals(typeString) ) continue; rather add all types to a map to check
					
					if(name==null){
						if(nrOfGroupsToCreate>1){
							typeLocalizingKey=typeLocalizingKey+" "+i;
							defaultValue = defaultValue +" "+i;
						}
						name =  iwrb.getLocalizedString(typeLocalizingKey,defaultValue);
					}
					else{
						if(nrOfGroupsToCreate>1){
							name=name+" "+i;
						}
						
					}
					//create group then call recursive
					try {
						Group newGroup = business.createGroup(name,"",typeString);
						setCurrentUserAsOwnerOfGroup(iwc,newGroup);
						setCurrentUsersPrimaryGroupPermissionsForGroup(iwc,newGroup);
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
		}
	}
  
  private void setCurrentUserAsOwnerOfGroup(IWUserContext iwc, Group group){
		User user = iwc.getCurrentUser();
		AccessController access = iwc.getAccessController();
		//get users and add them as owners also, user.getPrimaryGroup();
		
		try {
			access.setAsOwner(group,((Integer)user.getPrimaryKey()).intValue(), iwc);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		
		}
  }
  
	private void setCurrentUsersPrimaryGroupPermissionsForGroup(IWUserContext iwc, Group group){
		User user = iwc.getCurrentUser();
		AccessController access = iwc.getAccessController();
		try {
			Group primary = user.getPrimaryGroup();
			String primaryGroupId = primary.getPrimaryKey().toString();
			String newGroupId = group.getPrimaryKey().toString();
			//TDOD create methods for this in accesscontrol
			//create permission
			access.setPermission(AccessController.CATEGORY_GROUP_ID,iwc,primaryGroupId,newGroupId,access.PERMISSION_KEY_CREATE,Boolean.TRUE);
			//edit permission
			access.setPermission(AccessController.CATEGORY_GROUP_ID,iwc,primaryGroupId,newGroupId,access.PERMISSION_KEY_EDIT,Boolean.TRUE);
			//delete permission
			access.setPermission(AccessController.CATEGORY_GROUP_ID,iwc,primaryGroupId,newGroupId,access.PERMISSION_KEY_DELETE,Boolean.TRUE);
			//view permission
			access.setPermission(AccessController.CATEGORY_GROUP_ID,iwc,primaryGroupId,newGroupId,access.PERMISSION_KEY_VIEW,Boolean.TRUE);
					
		}
		catch (Exception ex) {
			ex.printStackTrace();
		
		}
	}
}