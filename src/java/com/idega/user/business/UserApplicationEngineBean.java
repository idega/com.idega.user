package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jdom.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOSessionBean;
import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.contact.data.Email;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.app.SimpleUserAppAddUser;
import com.idega.user.app.SimpleUserAppHelper;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.EmailValidator;

public class UserApplicationEngineBean extends IBOSessionBean implements UserApplicationEngine {

	private static final long serialVersionUID = -7472052374016555081L;
	
	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;
	private GroupHelperBusinessBean groupHelper = new GroupHelperBusinessBean();
	private SimpleUserAppHelper presentationHelper = new SimpleUserAppHelper();
	
	private Map simpleUserApps = new HashMap();
	
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
	
	public Document getMembersList(SimpleUserPropertiesBean bean) {
		if (bean == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		String image = null;
		IWBundle bundle = getBundle();
		if (bundle != null) {
			image = bundle.getVirtualPathWithFileNameString(SimpleUserApp.EDIT_IMAGE);
		}
		
		Layer membersList = presentationHelper.getMembersList(iwc, bean, groupHelper, image);
		
		BuilderLogic builder = BuilderLogic.getInstance();
		return builder.getRenderedComponent(iwc, membersList, true);
	}
	
	public Document getAddUserPresentationObject(SimpleUserPropertiesBean bean, List parentGroups, List childGroups, Integer userId) {
		if (bean == null) {
			return null;
		}
		
		String instanceId = bean.getInstanceId();
		if (instanceId == null) {
			return null;
		}
		String parentGroupId = String.valueOf(bean.getParentGroupId());
		String groupId = String.valueOf(bean.getGroupId());
		String groupForUsersWithoutLoginId = bean.getDefaultGroupId();
		String containerId = bean.getContainerId();
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		BuilderLogic builder = BuilderLogic.getInstance();
		
		SimpleUserAppAddUser addUser = new SimpleUserAppAddUser(instanceId, containerId);
		addUser.setParentGroupId(parentGroupId);
		addUser.setGroupId(groupId);
		addUser.setGroupForUsersWthouLoginId(groupForUsersWithoutLoginId);
		addUser.setParentGroups(parentGroups);
		addUser.setChildGroups(childGroups);
		addUser.setUserId(userId);
		addUser.setGroupTypes(bean.getGroupTypes());
		addUser.setRoleTypes(bean.getRoleTypes());
		addUser.setGetParentGroupsFromTopNodes(bean.isGetParentGroupsFromTopNodes());
		
		return builder.getRenderedComponent(iwc, addUser, true);
	}
	
	public Document getSimpleUserApplication(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		
		Object simpleUserApp = simpleUserApps.get(instanceId);
		if (simpleUserApp instanceof SimpleUserAppViewUsers) {
			IWContext iwc = CoreUtil.getIWContext();
			if (iwc == null) {
				return null;
			}
			
			SimpleUserAppViewUsers viewUsers = (SimpleUserAppViewUsers) simpleUserApp;
			Collection children = viewUsers.getChildren();
			if (children != null) {
				viewUsers.removeAll(children);
			}
			
			return BuilderLogic.getInstance().getRenderedComponent(iwc, viewUsers, true);
		}
		
		return null;
	}
	
	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers) {
		if (instanceId == null || viewUsers == null) {
			return;
		}
		
		simpleUserApps.put(instanceId, viewUsers);
	}
	
	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, Integer userId, String groupTypes, String groupRoles) {
		if (parentGroupId == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		User user = null;
		if (userId != null) {
			UserBusiness userBusiness = getUserBusiness(iwc);
			if (userBusiness != null) {
				try {
					user = userBusiness.getUser(userId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		List groups = groupHelper.getFilteredChildGroups(iwc, parentGroupId.intValue(), groupTypes, groupRoles, ",");
		List ids = new ArrayList();
		Layer availableGroupsContainer = presentationHelper.getSelectedGroups(iwc, user, groupHelper, groups, ids, null);
		
		return BuilderLogic.getInstance().getRenderedComponent(iwc, availableGroupsContainer, true);
	}
	
	public UserDataBean getUserByPersonalId(String personalId) {
		if (personalId == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}
		
		UserDataBean bean = new UserDataBean();
		
		User user = null;
		try {
			user = userBusiness.getUser(personalId);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		
		if (user == null) {
			IWResourceBundle iwrb = null;
			String errorMessage = "Unable to find user by provided personal ID!";
			try {
				iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (iwrb != null) {
				errorMessage = iwrb.getLocalizedString("unable_to_find_user_by_personal_id", errorMessage);
			}
			bean.setErrorMessage(errorMessage);
		}
		else {
			String password = userBusiness.getUserPassword(user);
			String personalID = user.getPersonalID();
			Email email = null;
			try {
				email = userBusiness.getUserMail(user);
			} catch (RemoteException e) {}
			String mail = email == null ? CoreConstants.EMPTY : email.getEmailAddress();
			bean.setName(user.getName());
			bean.setPersonalId(personalID == null ? personalId : personalID);
			bean.setPassword(password == null ? CoreConstants.EMPTY : password);
			bean.setEmail(mail == null ? CoreConstants.EMPTY : mail);
		}
		
		return bean;
	}
	
	public String createUser(String name, String personalId, String password, String email, Integer primaryGroupId, List childGroups) {
		if (name == null || personalId == null || password == null || primaryGroupId == null || childGroups == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		IWResourceBundle iwrb = getBundle().getResourceBundle(iwc);
		String sucessText = iwrb.getLocalizedString("success_saving_user", "Your changes were successfully saved.");
		String errorText = iwrb.getLocalizedString("error_saving_user", "Error occurred while saving Your changes.");
		
		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return errorText;
		}
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return errorText;
		}
		
		User user = null;
		try {
			user = userBusiness.getUser(personalId);
		} catch (RemoteException e) {
		} catch (FinderException e) {
		}
		
		
		if (user == null) {	//	Creating user
			try {
				user = userBusiness.createUserByPersonalIDIfDoesNotExist(name, personalId, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (CreateException e) {
				e.printStackTrace();
			}
			if (user == null) {
				return errorText;
			}
		}
		
		//	Email
		Email mail = null;
		try {
			mail = userBusiness.getUserMail(user);
		} catch (RemoteException e) {}
		if (mail == null) {
			try {
				mail = userBusiness.updateUserMail(user, email);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (CreateException e) {
				e.printStackTrace();
			}
			if (mail == null) {
				return errorText;
			}
		}
		
		LoginTable loginTable = null;
		loginTable = LoginDBHandler.getUserLogin(user);
		if (loginTable == null) {	//	Creating login
			try {
				loginTable = LoginDBHandler.createLogin(user, personalId, password);
			} catch (LoginCreateException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (loginTable == null) {
				return errorText;
			}
			loginTable.store();
		}
		
		//	Setting new available groups for user
		Object o = null;
		for (int i = 0; i < childGroups.size(); i++) {
			o = childGroups.get(i);
			if (o instanceof Integer) {
				try {
					groupBusiness.addUser(((Integer) o).intValue(), user);
				} catch (EJBException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		//	Setting primary group
		user.setPrimaryGroupID(primaryGroupId);
		user.store();
		
		return sucessText;
	}
	
	public String isValidEmail(String email) {
		String error = "Please provide valid email!";
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return error;
		}
		
		IWResourceBundle iwrb = getBundle().getResourceBundle(iwc);
		String errorText = iwrb.getLocalizedString("invalid_email", error);
		
		if (email == null) {
			return errorText;
		}
		
		if (EmailValidator.getInstance().validateEmail(email)) {
			return null;	// Email is valid
		}
		
		return errorText;	//	Email is invalid
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
