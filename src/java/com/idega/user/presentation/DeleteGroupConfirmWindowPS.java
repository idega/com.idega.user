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
import com.idega.idegaweb.IWException;
import com.idega.user.data.Group;
import com.idega.user.data.GroupDomainRelation;
import com.idega.user.data.GroupDomainRelationHome;
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
        try { 
          if (group.getGroupType().equals("alias") || group.getChildCount() <= 0)  {
            if (parentGroup != null)
              parentGroup.removeGroup(group);
            else if (parentDomain != null)  {
              removeRelation( parentDomain, group);
            }
          }
        }
         catch (RemoteException ex)  {
        }
        this.fireStateChanged();
      }
      
    }
	}
  
  private void removeRelation(IBDomain domain, Group group)  {
    try {
      GroupDomainRelationHome home = (GroupDomainRelationHome) 
        IDOLookup.getHome(GroupDomainRelation.class);
      Collection coll = home.findDomainsRelationshipsContaining(domain, group);
      Iterator iterator = coll.iterator();
      while (iterator.hasNext())  {
        GroupDomainRelation relation = (GroupDomainRelation) iterator.next();
        relation.remove();
      }
    }
    catch (Exception ex)  {
    }
    
  }
    
      
    
  
  
}
