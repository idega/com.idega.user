package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.EJBException;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.builder.data.ICDomain;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.presentation.IWContext;
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
				ICDomain parentDomain = event.getParentDomain();
				IWApplicationContext iwac = e.getIWContext().getApplicationContext();
				GroupBusiness groupBusiness = getGroupBusiness(iwac);
				try {
					if (groupBusiness.isGroupRemovable(group))  {
						if (parentGroup != null){
							parentGroup.removeGroup(group, e.getIWContext().getCurrentUser());
						}
						else if (parentDomain != null)  {
							removeRelation( parentDomain, group, e.getIWContext().getCurrentUser());
						}
						
						//disable permissions for group if it has no other parents.
						removePermissions(group,e.getIWContext());
						
						
						
						//TODO fix this
						e.getIWContext().getApplicationContext().removeApplicationAttribute("domain_group_tree");
						e.getIWContext().getApplicationContext().removeApplicationAttribute("group_tree");			
						this.fireStateChanged();
					}
				}
				catch (RemoteException e1) {
					e1.printStackTrace();
				}
				catch (EJBException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method removes the groups permissions if it no longer has any valid parent relations
	 * @param group
	 * @param context
	 */
	private void removePermissions(Group group, IWContext iwc) {
		Collection parents = null;
		
		try {
			parents = getGroupBusiness(iwc).getParentGroups(group);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//if it has no parents it will disapeer from view and therefor we can disable its permissions.
		if( parents==null || parents.isEmpty()){
			Collection permissions = getAllPermissionForGroup(group);
			
			Iterator entries = permissions.iterator();
			while (entries.hasNext()) {
				ICPermission permission = (ICPermission) entries.next();
				permission.removeBy(iwc.getCurrentUser());
			}
			
			
			//TODO fix this better: refresh permissions PermissionCacher.updatePermissions()
			iwc.getApplicationContext().removeApplicationAttribute("ic_permission_map_"+AccessController.CATEGORY_GROUP_ID);
			
			
		}
		
		
		
	}
	
	private Collection getAllPermissionForGroup(Group group)  {
		Collection allPermissions = null;
		Collection permissionSetOnGroup = null;
		
		try {
			allPermissions = AccessControl.getAllGroupPermissionsForGroup(group);
			permissionSetOnGroup = AccessControl.getAllGroupPermissionsReverseForGroup(group);
			
			allPermissions.addAll(permissionSetOnGroup);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("GroupPermission selected group ("+group.getPrimaryKey()+") not found or remote error!");
		} 
		return allPermissions;
	}
	
	private void removeRelation(ICDomain domain, Group group, User currentUser)  {
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
