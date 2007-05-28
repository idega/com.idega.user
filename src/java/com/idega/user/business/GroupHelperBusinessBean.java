package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;

public class GroupHelperBusinessBean {
	
	private UserBusiness userBusiness = null;
	
	public List<GroupNode> getTopGroupNodes(){
		List<GroupNode> fake = new ArrayList<GroupNode>();
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return fake;
		}
		
		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(Exception e) {
			return fake;
		}
		
		return getTopGroupNodes(currentUser, iwc);
	}
	
	public List<GroupNode> getTopGroupNodes(User user, IWContext iwc) {
		List<GroupNode> fake = new ArrayList<GroupNode>();
		
		if (user == null || iwc == null) {
			return fake;
		}
		
		userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return fake;
		}
		
		Collection allGroups = null;
		try {
			allGroups = userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return fake;
		}
		
		return convertGroupCollectionToGroupNodeCollection(allGroups, iwc.getApplicationContext());
	}
	
	private synchronized UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBusiness == null) {
			try {
				userBusiness = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}
	
	private List<GroupNode> convertGroupCollectionToGroupNodeCollection(Collection nodes, IWApplicationContext iwac){
		List <GroupNode> list = new ArrayList<GroupNode>();
		if (nodes == null || iwac == null) {
			return list;
		}
		
		Object o = null;
		Group group = null;
		GroupNode groupNode = null;
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				groupNode = new GroupNode(); 
				groupNode.setUniqueId(group.getId());
				groupNode.setName(group.getName());
				if (group.getChildCount() > 0) {
					groupNode.setChildren(convertGroupCollectionToGroupNodeCollection(group.getChildren(), iwac));
					groupNode.setHasChildren(true);
				}
				list.add(groupNode);
			}
		}

		return list;
	}	
}
