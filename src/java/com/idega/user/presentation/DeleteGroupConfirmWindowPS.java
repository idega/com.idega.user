package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.RemoveException;

import com.idega.builder.data.IBDomain;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupDomainRelation;
import com.idega.user.data.GroupDomainRelationHome;
import com.idega.user.data.User;
import com.idega.user.event.DeleteGroupEvent;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class DeleteGroupConfirmWindowPS extends IWPresentationStateImpl implements IWActionListener {
  
  
	/**
	 * @see com.idega.event.IWActionListener#actionPerformed(com.idega.event.IWPresentationEvent)
	 */
	public void actionPerformed(IWPresentationEvent e) throws IWException {
    if (e instanceof DeleteGroupEvent)  {
      DeleteGroupEvent event = (DeleteGroupEvent) e;
      if (event.isDeletingConfirmed())  {
        Group group = event.getGroup();
        Group parentGroup = event.getParentGroup();
        IBDomain parentDomain = event.getParentDomain();
        IWApplicationContext iwac = e.getIWContext().getApplicationContext();
        GroupBusiness groupBusiness = getGroupBusiness(iwac);
        if (groupBusiness.isGroupRemovable(group))  {
          try {
            // remove group
            if (parentGroup != null)
              parentGroup.removeGroup(group, e.getIWContext().getCurrentUser());
            else if (parentDomain != null)  {
              removeRelation( parentDomain, group, e.getIWContext().getCurrentUser());
            }
            //TODO fix this
				    e.getIWContext().getApplicationContext().removeApplicationAttribute("domain_group_tree");
				    e.getIWContext().getApplicationContext().removeApplicationAttribute("group_tree");			
            this.fireStateChanged();
          }
          catch (RemoteException ex)  {
          }
        }
      }
    }
	}
  
  private void removeRelation(IBDomain domain, Group group, User currentUser)  {
    try {
      GroupDomainRelationHome home = (GroupDomainRelationHome) 
        IDOLookup.getHome(GroupDomainRelation.class);
      Collection coll = home.findDomainsRelationshipsContaining(domain, group);
      Iterator iterator = coll.iterator();
      while (iterator.hasNext())  {
        GroupDomainRelation relation = (GroupDomainRelation) iterator.next();
        relation.removeBy(currentUser);
      }
    }
    catch (Exception ex)  {
    }
    
  }
    
  private GroupBusiness getGroupBusiness(IWApplicationContext iwac)    {
    try {
      return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
    }
    catch (java.rmi.RemoteException rme) {
      throw new RuntimeException(rme.getMessage());
    }
  }      
    
  
  
}
