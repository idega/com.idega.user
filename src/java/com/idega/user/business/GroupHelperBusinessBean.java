package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;

public class GroupHelperBusinessBean{
//extends IBOServiceBean  {
	private UserBusiness userBusiness = null;
	private GroupBusiness groupBusiness = null;
	
	public Collection getTopGroupNodes(){
		IWContext iwc = IWContext.getInstance();
		userBusiness = getUserBusiness(iwc);
		groupBusiness = getGroupBusiness(iwc);
		
		Collection allGroups = null;
		try {
			allGroups = userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Collection groupNodes = convertGroupCollectionToGroupNodeCollection(allGroups,iwc.getApplicationContext());
		
		return groupNodes;
	}
	
	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (this.userBusiness == null) {
			try {
				this.userBusiness = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return this.userBusiness;
	}	
	
	public GroupBusiness getGroupBusiness(IWApplicationContext iwc) {
		if (this.groupBusiness == null) {
			try {
				groupBusiness =(GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			// Remote and FinderException
			catch (Exception ex)  {
				throw new RuntimeException(ex.getMessage());
			}		
		}
		return this.groupBusiness;
	}
	public Collection convertGroupCollectionToGroupNodeCollection(Collection col, IWApplicationContext iwac){
		List <GroupNode>list = new Vector<GroupNode>();
		
		Iterator iter = col.iterator();
		while (iter.hasNext()) {
			Group group = (Group) iter.next();
			GroupNode groupNode = new GroupNode(); 
			groupNode.setUniqueId(group.getId());
			groupNode.setName(group.getName());
			if (group.getChildCount() != 0){
				groupNode.setChildren(convertGroupCollectionToGroupNodeCollection(group.getChildren(), iwac));
				groupNode.setHasChildren(true);
			}
			list.add(groupNode);
		}

		return list;
	}	
}
