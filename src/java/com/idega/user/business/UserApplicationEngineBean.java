package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jdom.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOSessionBean;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.app.SimpleUserAppHelper;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;

public class UserApplicationEngineBean extends IBOSessionBean implements UserApplicationEngine {

	private static final long serialVersionUID = -7472052374016555081L;
	
	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;
	private GroupHelperBusinessBean groupHelper = new GroupHelperBusinessBean();
	private SimpleUserAppHelper presentationHelper = new SimpleUserAppHelper();
	
	public List getChildGroups(String groupId, String groupTypes, String groupRoles) {
		if (groupId == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}
		
		int id = -1;
		try {
			id = Integer.valueOf(groupId).intValue();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		
		Group selected = null;
		try {
			selected = groupBusiness.getGroupByGroupID(id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} catch (FinderException e) {
			e.printStackTrace();
			return null;
		}
		
		Collection childGroups = groupHelper.getFilteredChildGroups(iwc, selected, groupTypes, groupRoles, ",");
		if (childGroups == null) {
			return null;
		}
		
		Object o = null;
		Group group = null;
		List childGroupsProperties = new ArrayList();
		for (Iterator it = childGroups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				childGroupsProperties.add(new AdvancedProperty(group.getId(), group.getName()));
			}
		}
		
		return childGroupsProperties;
	}
	
	public String getChildGroupsInString(String groupId, String groupTypes, String groupRoles) {
		List properties = getChildGroups(groupId, groupTypes, groupRoles);
		if (properties == null) {
			return null;
		}
		if (properties.size() == 0) {
			return null;
		}
		
		String comma = ",";
		String separator = "@prop_separator@";
		StringBuffer childGroups = new StringBuffer();
		AdvancedProperty property = null;
		for (int i = 0; i < properties.size(); i++) {
			property = (AdvancedProperty) properties.get(i);
			childGroups.append(property.getId()).append(comma).append(property.getValue());
			if (i + 1 < properties.size()) {
				childGroups.append(separator);
			}
		}
		
		return childGroups.toString();
	}
	
	public String getSomeData(String groupId, String types) {
		return new StringBuffer(groupId).append(" ").append(types).toString();
	}
	
	public List removeUsers(List usersIds, Integer groupId) {
		if (usersIds == null || groupId == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}
		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}
		
		Group group = null;
		try {
			group = groupBusiness.getGroupByGroupID(groupId.intValue());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		if (group == null) {
			return null;
		}
		User currentUser = iwc.getCurrentUser();
		if (currentUser == null) {
			return null;
		}
		
		List removedUsers = new ArrayList();
		Integer id = null;
		for (int i = 0; i < usersIds.size(); i++) {
			id = (Integer) usersIds.get(i);
			try {
				userBusiness.removeUserFromGroup(id.intValue(), group, currentUser);
				removedUsers.add(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (RemoveException e) {
				e.printStackTrace();
			}
		}
		
		return removedUsers;
	}
	
	public Document getMembersList(int parentGroupId, int groupId, int orderBy) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		SimpleUserPropertiesBean bean = new SimpleUserPropertiesBean(parentGroupId, groupId, orderBy);

		String image = null;
		IWBundle bundle = getBundle();
		if (bundle != null) {
			image = bundle.getVirtualPathWithFileNameString(SimpleUserApp.EDIT_IMAGE);
		}
		
		Layer membersList = presentationHelper.getMembersList(iwc, bean, groupHelper, image);
		
		BuilderLogic builder = BuilderLogic.getInstance();
		return builder.getRenderedComponent(iwc, membersList);
	}
	
	private UserBusiness getUserBusiness(IWContext iwc) {
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
	
	private GroupBusiness getGroupBusiness(IWContext iwc) {
		if (groupBusiness == null) {
			try {
				groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}
	
	protected String getBundleIdentifier(){
	  	return UserConstants.IW_BUNDLE_IDENTIFIER;
	  }

}
