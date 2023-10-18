package com.idega.user.business;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.faces.component.UIComponent;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.jdom2.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.AdvancedPropertyComparator;
import com.idega.builder.business.BuilderLogic;
import com.idega.builder.data.IBPageName;
import com.idega.builder.data.IBPageNameHome;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.LoginCreateException;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginInfo;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.builder.data.ICPage;
import com.idega.core.builder.data.ICPageHome;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.CommuneHome;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.core.location.data.PostalCode;
import com.idega.core.location.data.PostalCodeHome;
import com.idega.data.IDOEntity;
import com.idega.data.IDOHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.repository.bean.RepositoryItem;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.app.SimpleUserAppAddUser;
import com.idega.user.app.SimpleUserAppHelper;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.helpers.UserHelper;
import com.idega.user.presentation.GroupMembersListViewer;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.EmailValidator;
import com.idega.util.ListUtil;
import com.idega.util.SendMail;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

public class UserApplicationEngineBean extends DefaultSpringBean implements UserApplicationEngine, Serializable {

	private static final long serialVersionUID = -7472052374016555081L;
	protected static final Logger logger = Logger.getLogger(UserApplicationEngineBean.class.getName());

	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired(required = false)
	private CompanyHelper companyHelper;

	@Autowired
	private SimpleUserAppHelper presentationHelper;

	@Autowired
	private UserHelper userHelper;

	private final Map<String, SimpleUserAppViewUsers> simpleUserApps = new HashMap<>();
	private final Map<String, List<Integer>> pagerProperties = new HashMap<>();

	public GroupHelper getGroupHelper() {
		return groupHelper;
	}

	public void setGroupHelper(GroupHelper groupHelper) {
		this.groupHelper = groupHelper;
	}

	public CompanyHelper getCompanyHelper() {
		return companyHelper;
	}

	public void setCompanyHelper(CompanyHelper companyHelper) {
		this.companyHelper = companyHelper;
	}

	@Override
	public List<AdvancedProperty> getChildGroups(String groupId, String groupTypes, String groupRoles, String subGroups, String subGroupsToExclude) {
		if (groupId == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		GroupBusiness groupBusiness = getGroupBusiness();
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

		GroupHelper helper = getGroupHelper();
		if (helper == null) {
			return null;
		}
		Collection<Group> childGroups = helper.getFilteredChildGroups(iwc, selected, groupTypes, groupRoles, CoreConstants.COMMA, subGroups, subGroupsToExclude);
		if (childGroups == null || childGroups.isEmpty()) {
			return null;
		}

		Locale locale = iwc.getCurrentLocale();
		if (locale == null) {
			locale = Locale.ENGLISH;
		}

		Group group = null;
		List<AdvancedProperty> childGroupsProperties = new ArrayList<>();
		for (Iterator<Group> it = childGroups.iterator(); it.hasNext();) {
			group = it.next();

			childGroupsProperties.add(new AdvancedProperty(group.getId(), getGroupNameInTreeOrientedWay(group, locale)));
		}

		IWResourceBundle iwrb = getBundle(iwc).getResourceBundle(iwc);
		childGroupsProperties.add(0, new AdvancedProperty("-1", iwrb.getLocalizedString("select_group", "Select group")));

		return childGroupsProperties;
	}

	private String getGroupNameInTreeOrientedWay(Group group, Locale locale) {
		Group parentGroup = group.getParentNode();
		StringBuilder name = new StringBuilder();
		while (parentGroup != null) {
			name.append(CoreConstants.MINUS);
			parentGroup = parentGroup.getParentNode();
		}

		String levels = name.toString();
		levels = levels.replaceFirst(CoreConstants.MINUS, CoreConstants.EMPTY);

		String groupName = group.getNodeName(locale);

		return levels.equals(CoreConstants.EMPTY) ? groupName : new StringBuilder(levels).append(CoreConstants.SPACE).append(groupName).toString();
	}

	@Override
	public List<Integer> removeUsers(List<Integer> usersIds, Integer groupId) {
		if (usersIds == null || groupId == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		GroupBusiness groupBusiness = getGroupBusiness();
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

		List<Integer> removedUsers = new ArrayList<>();
		Integer id = null;
		for (int i = 0; i < usersIds.size(); i++) {
			id = usersIds.get(i);
			try {
				userBusiness.removeUserFromGroup(id, group, currentUser);
				removedUsers.add(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (RemoveException e) {
				e.printStackTrace();
			}
		}

		return removedUsers;
	}

	@Override
	public Document getMembersList(SimpleUserPropertiesBean bean, String containerId) {
		if (bean == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		String image = null;
		IWBundle bundle = getBundle(iwc);
		if (bundle != null) {
			image = bundle.getVirtualPathWithFileNameString(SimpleUserApp.EDIT_IMAGE);
		}

		GroupMembersListViewer membersList = getPresentationHelper().getMembersList(bean, image, containerId, false);

		BuilderLogic builder = BuilderLogic.getInstance();
		return builder.getRenderedComponent(iwc, membersList, true);
	}

	@Override
	public Document getAddUserPresentationObject(SimpleUserPropertiesBean bean, List<Integer> parentGroups, List<Integer> childGroups, Integer userId) {
		if (bean == null) {
			return null;
		}

		if (StringUtil.isEmpty(bean.getInstanceId()) || StringUtil.isEmpty(bean.getContainerId())) {
			logger.log(Level.WARNING, "Can not generate form for creating/editing form, missing properties: instance ID: " + bean.getInstanceId() +
					", container ID: " + bean.getContainerId());
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		BuilderLogic builder = BuilderLogic.getInstance();

		SimpleUserAppAddUser addUser = new SimpleUserAppAddUser(bean);
		addUser.setParentGroups(parentGroups);
		if (ListUtil.isEmpty(childGroups) && bean.getParentGroupId() > 0) {
			try {
				Group parentGroup = getGroupBusiness().getGroupByGroupID(bean.getParentGroupId());
				childGroups = CoreUtil.getIdsAsIntegers(parentGroup.getChildGroups());
				if (ListUtil.isEmpty(childGroups)) {
					childGroups = Arrays.asList(Integer.valueOf(parentGroup.getPrimaryKey().toString()));
				}
			} catch (Exception e) {}
		}
		addUser.setChildGroups(childGroups);
		addUser.setUserId(userId);

		return builder.getRenderedComponent(iwc, addUser, true);
	}

	@Override
	public Document getSimpleUserApplication(String instanceId, Integer parentGroupId) {
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
			List<UIComponent> children = viewUsers.getChildren();
			if (!ListUtil.isEmpty(children)) {
				viewUsers.removeAll(children);
			}
			viewUsers.setSelectedParentGroupId(parentGroupId);
			viewUsers.setCheckPagerProperties(true);

			return BuilderLogic.getInstance().getRenderedComponent(iwc, viewUsers, true);
		}

		return null;
	}

	@Override
	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers) {
		if (instanceId == null || viewUsers == null) {
			return;
		}

		simpleUserApps.put(instanceId, viewUsers);
	}

	@Override
	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, Integer userId, String groupTypes, String groupRoles, String subGroups, String subGroupsToExclude) {
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

		GroupHelper helper = getGroupHelper();
		if (helper == null) {
			return null;
		}
		List<Group> groups = helper.getFilteredChildGroups(iwc, parentGroupId.intValue(), groupTypes, groupRoles, CoreConstants.COMMA, subGroups, subGroupsToExclude);
		List<String> ids = new ArrayList<>();
		String selectedGroupId = null;
		if (groups == null || groups.isEmpty()) {
			selectedGroupId = String.valueOf(parentGroupId);
		}
		Layer availableGroupsContainer = getPresentationHelper().getSelectedGroups(iwc, user, helper, groups, ids, selectedGroupId);

		return BuilderLogic.getInstance().getRenderedComponent(iwc, availableGroupsContainer, true);
	}

	@Override
	public UserDataBean getUserInfo(User user) {
		return userHelper.getUserInfo(user);
	}

	public void fillUserInfo(UserDataBean info, Phone phone, Phone mobilePhone, Email email, Address address){
		userHelper.fillUserInfo(info, phone, mobilePhone, null, email, address);
	}
	@Override
	public void fillUserInfo(UserDataBean info, Phone phone, Email email, Address address) {
		fillUserInfo(info, phone, null, email, address);
	}

	@Override
	public String getUserLogin(String personalId) {
		return getUserLogin(personalId, null);
	}

	private String getUserLogin(String personalId, Integer id) {
		try {
			UserBusiness userBusiness = getUserBusiness(IWMainApplication.getDefaultIWApplicationContext());
			User user = null;
			if (!StringUtil.isEmpty(personalId)) {
				try {
					user = userBusiness.getUser(personalId);
				} catch (Exception e) {}
			}

			if (user == null && id != null) {
				try {
					user = userBusiness.getUser(id);
				} catch (Exception e) {}
			}

			if (user == null) {
				logger.warning("User by personal ID '" + personalId + "' nor ID '" + id + "' does not exist");
				return null;
			}

			LoginTable loginTable = LoginDBHandler.getUserLogin(user);
			return loginTable == null ? null : loginTable.getUserLogin();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error occured whil resolving user's login by personal ID: " + personalId, e);
		}
		return null;
	}

	@Override
	public UserDataBean getUserById(Integer id) {
		if ((id == null) || (id < 1)) {
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

		UserDataBean info = null;

		User user = null;
		try {
			user = userBusiness.getUser(id);
		} catch (Exception e) {}
		if (user == null) {
			logger.log(Level.WARNING, "User by was not found by provided personal ID ('" + id + "'), trying to find company");
		} else {
			info = getUserInfo(user);
		}

		return info;
	}

	@Override
	public UserDataBean getUserByPersonalId(String personalId) {
		if (StringUtil.isEmpty(personalId)) {
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

		UserDataBean info = null;

		User user = null;
		try {
			user = userBusiness.getUser(personalId);
		} catch (Exception e) {}
		if (user == null) {
			logger.log(Level.WARNING, "User by was not found by provided personal ID ('" + personalId + "'), trying to find company");
		} else {
			info = getUserInfo(user);
		}

		if (info == null && getCompanyHelper() != null) {
			try {
				info = getCompanyHelper().getCompanyInfo(personalId);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error getting company information by ID: " + personalId, e);
			}
		}

		if (info == null) {
			IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
			info = new UserDataBean();
			String errorMessage = new StringBuilder(iwrb.getLocalizedString("no_user_found_by_provided_personal_id", "No user found by provided ID"))
													.append(": ").append(personalId).toString();
			info.setErrorMessage(errorMessage);
		}

		return info;
	}

	@Override
	public AdvancedProperty createUser(UserDataBean userInfo, Integer primaryGroupId, List<Integer> childGroups, List<Integer> deselectedGroups,
			boolean allFieldsEditable, boolean sendEmailWithLoginInfo, String login, String password) {
		return createUserWithEmailProps(userInfo, primaryGroupId, childGroups, deselectedGroups, allFieldsEditable, sendEmailWithLoginInfo, login, password, null);
	}

	@Override
	public AdvancedProperty createUserWithEmailProps(UserDataBean userInfo, Integer primaryGroupId, List<Integer> childGroups, List<Integer> deselectedGroups,
			boolean allFieldsEditable, boolean sendEmailWithLoginInfo, String login, String password, Map<String, String> emailProps) {
		if (userInfo == null) {
			logger.warning("User info is not provided!");
			return null;
		}

		AdvancedProperty result = new AdvancedProperty(userInfo.getUserId() == null ? null : String.valueOf(userInfo.getUserId()));

		String name = userInfo.getName();
		String personalId = userInfo.getPersonalId();
		String email = userInfo.getEmail();

		if (StringUtil.isEmpty(name) || primaryGroupId == null || childGroups == null || StringUtil.isEmpty(email)) {
			logger.warning("Some of the parameters are invalid! Name: " + name + ", primary group ID: " + primaryGroupId + ", child groups: " +
					childGroups + ", email: " + email);
			return result;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			logger.warning(IWContext.class.getName() + " is unavailable");
			return result;
		}

		String phoneNumber = userInfo.getPhone();

		IWResourceBundle iwrb = getBundle(iwc).getResourceBundle(iwc);
		result.setValue(iwrb.getLocalizedString("error_saving_user", "Error occurred while saving your changes."));

		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			logger.warning(UserBusiness.class.getName() + " is unavailable");
			return result;
		}
		GroupBusiness groupBusiness = getGroupBusiness();
		if (groupBusiness == null) {
			logger.warning(GroupBusiness.class.getName() + " is unavailable");
			return result;
		}

		User user = null;
		if (personalId == null) {
			personalId = CoreConstants.EMPTY;
		}
		if (!StringUtil.isEmpty(personalId)) {
			try {
				user = userBusiness.getUser(personalId);
			} catch (RemoteException e) {
			} catch (FinderException e) {}
		}
		if (userInfo.getUserId() != null) {
			try {
				user = userBusiness.getUser(userInfo.getUserId());
			} catch (RemoteException e) {}
		}

		LoginInfo loginInfo = null;
		LoginTable loginTable = null;
		boolean newLogin = false;
		if (user == null) {
			logger.info("Creating new user: " + name + ", personal ID: " + personalId);
			//	Creating user
			try {
				user = userBusiness.createUserByPersonalIDIfDoesNotExist(name, personalId, null, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (CreateException e) {
				e.printStackTrace();
			}
			if (user == null) {
				logger.warning("Unable to create new user: " + name + ", personal ID: " + personalId);
				return result;
			}
		}

		//	Generating login for new user OR existing user without login
		Integer userId = user == null ? null : Integer.valueOf(user.getId());
		String userLogin = getUserLogin(personalId, userId);
		if (StringUtil.isEmpty(userLogin)) {
			logger.info("Creating login and password for user: " + name + ", personal ID: " + user.getPersonalID());

			if (!StringUtil.isEmpty(user.getPersonalID())) {
				login = user.getPersonalID();
			}
			if (StringUtil.isEmpty(login)) {
				List<String> logins = LoginDBHandler.getPossibleGeneratedUserLogins(user);
				if (ListUtil.isEmpty(logins)) {
					return result;
				}
				login = logins.get(0);
			}
			if (StringUtil.isEmpty(login)) {
				logger.warning("Failed to generate login for " + name + ", personal ID: " + personalId);
				return result;
			}

			if (StringUtil.isEmpty(password)) {
				password = LoginDBHandler.getGeneratedPasswordForUser(user);
			}
			try {
				loginTable = LoginDBHandler.createLogin(user, login, password);
			} catch (LoginCreateException e) {
				logger.log(Level.WARNING, "Error creating login '" + login + "' for user " + user, e);
				String message = iwrb.getLocalizedString("error_creating_account_with_username_{0}_for_{1}", "Error creating account for {1} with username {0}: username {0} is already in use");
				message = MessageFormat.format(message, new Object[] {login, user == null ? CoreConstants.EMPTY : user.getName()});
				result.setValue(message);
				return result;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (loginTable == null) {
				logger.warning("Login table does not exist for " + name + ", personal ID: " + personalId);
				return result;
			}

			loginInfo = LoginDBHandler.getLoginInfo(loginTable);
			loginInfo.setChangeNextTime(Boolean.TRUE);
			loginInfo.store();
			userInfo.setChangePasswordNextTime(true);
			newLogin = true;
			sendEmailWithLoginInfo = true;
		}

		result.setId(user.getId());

		//	Personal ID
		if (!StringUtil.isEmpty(personalId) && !personalId.equals(user.getPersonalID())) {
			user.setPersonalID(personalId);
		}

		removeUserFromOldGroups(iwc, deselectedGroups, user);

		//	Name
		if (allFieldsEditable) {
			user.setFullName(name);
		}

		//	Phone
		if (!StringUtil.isEmpty(phoneNumber)) {
			Phone phone = null;
			try {
				phone = userBusiness.getUserPhone(Integer.valueOf(user.getId()), PhoneTypeBMPBean.HOME_PHONE_ID);
			} catch (Exception e) {}
			if (phone == null || allFieldsEditable) {
				try {
					userBusiness.updateUserPhone(user, PhoneTypeBMPBean.HOME_PHONE_ID, phoneNumber);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Error setting phone for " + name + ", personal ID: " + personalId);
					return result;
				}
			}
		}

		//	Email
		if (!StringUtil.isEmpty(email)) {
			Email mail = null;
			try {
				mail = userBusiness.getUserMail(user);
			} catch (RemoteException e) {}
			if (mail == null || allFieldsEditable) {
				try {
					mail = userBusiness.updateUserMail(user, email);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (CreateException e) {
					e.printStackTrace();
				}
				if (mail == null) {
					logger.warning("Error setting email for " + name + ", personal ID: " + personalId);
					return result;
				}
			}
		}

		//	Picture
		if (StringUtil.isEmpty(userInfo.getPictureUri())) {
			user.setSystemImageID(null);	//	Deleting
		} else {
			try {
				ICFile picture = ((ICFileHome) IDOLookup.getHome(ICFile.class)).create();
				picture.setFileValue(getRepositoryService().getInputStreamAsRoot(userInfo.getPictureUri()
						.replace(CoreConstants.WEBDAV_SERVLET_URI, CoreConstants.EMPTY)));
				picture.setPublic(true);
				picture.setFileUri(userInfo.getPictureUri());
				picture.setName(userInfo.getPictureUri().substring(userInfo.getPictureUri().lastIndexOf(CoreConstants.SLASH) + 1));
				picture.store();

				user.setSystemImageID(Integer.valueOf(picture.getId()));

				RepositoryItem resource = getRepositoryService().getRepositoryItemAsRootUser(userInfo.getPictureUri().replace(CoreConstants.WEBDAV_SERVLET_URI,
						CoreConstants.EMPTY));
				resource.delete();
			} catch(Exception e) {
				logger.log(Level.WARNING, "Error setting image for user: " + userInfo.getPictureUri());
			}
		}

		//	Address
		Address userAddress = null;
		try {
			userAddress = userBusiness.getUsersMainAddress(user);
		} catch (RemoteException e) {}
		String streetNameAndNumber = null;
		if (userAddress == null || allFieldsEditable) {
			try {
				Country country = getCountryById(userInfo.getCountryName());
				Commune commune = getCommune(StringUtil.isEmpty(userInfo.getCommune()) ? userInfo.getCity() : userInfo.getCommune());
				PostalCode postalCode = getPostalCode(userInfo.getPostalCodeId(), country);
				if (postalCode == null) {
					postalCode = createPostalCode(userInfo.getPostalCodeId(), country, commune, userInfo.getCity());
				} else {
					postalCode.setPostalCode(userInfo.getPostalCodeId());
					postalCode.store();
				}
				streetNameAndNumber = userInfo.getStreetNameAndNumber();
				if (streetNameAndNumber != null) {
					userAddress = userBusiness.updateUsersMainAddressOrCreateIfDoesNotExist(
							user,
							userInfo.getStreetNameAndNumber(),
							postalCode,
							country,
							userInfo.getCity(),
							userInfo.getProvince(),
							userInfo.getPostalBox(),
							commune == null ? null : (Integer) commune.getPrimaryKey()
					);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (CreateException e) {
				e.printStackTrace();
			}
			if (streetNameAndNumber != null && userAddress == null) {
				logger.warning("Error setting address for " + name + ", personal ID: " + personalId);
				return result;
			}
		}

		//	Login
		loginTable = loginTable == null ? LoginDBHandler.getUserLogin(user) : loginTable;
		if (!newLogin && allFieldsEditable && loginTable != null) {
			boolean updatePassword = false;
			String oldPassword = loginTable.getUserPasswordInClearText();
			if (password != null && (oldPassword == null || !password.equals(oldPassword))) {
				updatePassword = true;
			}

			if (updatePassword) {
				try {
					LoginDBHandler.changePassword(Integer.valueOf(user.getId()), password);
				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.WARNING, "Failed to set password for " + name + ", personal ID: " + personalId, e);
					return result;
				}
			}
		}
		loginInfo = loginInfo == null ? loginTable == null ? null : LoginDBHandler.getLoginInfo(loginTable) : loginInfo;
		if (loginInfo == null) {
			logger.warning("Unknown login information for " + name + ", personal ID: " + personalId);
			return result;
		}
		if (userInfo.getChangePasswordNextTime() != null) {
			loginInfo.setChangeNextTime(userInfo.getChangePasswordNextTime());
		}
		if (userInfo.getAccountEnabled() != null) {
			loginInfo.setAccountEnabled(userInfo.getAccountEnabled());
		}
		loginInfo.store();

		//	Setting new available groups for user
		checkChildGroups(childGroups, primaryGroupId);
		for (int i = 0; i < childGroups.size(); i++) {
			try {
				groupBusiness.addUser(childGroups.get(i), user);
			} catch (EJBException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		user.setPrimaryGroupID(primaryGroupId);
		user.setJuridicalPerson(userInfo.isJuridicalPerson());

		user.store();

		//	Sending mail
		if (sendEmailWithLoginInfo) {
			try {
				sendMailWithLoginInfo(
						iwc,
						iwrb,
						newLogin,
						user.getName(),
						userLogin,
						password,
						email,
						emailProps
				);
			} catch (MessagingException e) {
				getLogger().log(
						Level.WARNING,
						"Failed sending email with login",
						e
				);
			}
		}

		CoreUtil.clearAllCaches();

		result.setValue(iwrb.getLocalizedString("success_saving_user", "Your changes were successfully saved."));
		return result;
	}

	@Override
	public void sendMailWithLoginInfo(
			IWContext iwc,
			IWResourceBundle iwrb,
			boolean newLogin,
			String name,
			String login,
			String password,
			String email,
			Map<String, String> emailProps
	) throws MessagingException {
		IWMainApplicationSettings settings = iwc.getIWMainApplication().getSettings();

		String portNumber = new StringBuilder(":").append(String.valueOf(iwc.getServerPort())).toString();
		String portal = settings.getProperty("sua.portal_address");
		String serverURL = CoreUtil.getServerURL(iwc.getRequest());
		String serverLink = StringUtil.isEmpty(portal) ? StringHandler.replace(serverURL, portNumber, CoreConstants.EMPTY) : portal;
		String subject = newLogin ? iwrb.getLocalizedString("account_was_created", "Account was created") :
									iwrb.getLocalizedString("account_information_was_changed", "Account was modified");
		StringBuilder text = new StringBuilder(iwrb.getLocalizedString("dear", "Dear").concat(CoreConstants.SPACE).concat(name)
				.concat(",\n\r"));
		if (newLogin) {
			text = text.append(iwrb.getLocalizedString("login_here", "Login here")).append(": ").append(serverLink).append("\n\r")
				.append(iwrb.getLocalizedString("your_user_name", "Your user name")).append(": ").append(login).append(", ")
				.append(iwrb.getLocalizedString("your_password", "your password")).append(": ").append(password).append(". ")
				.append(iwrb.getLocalizedString("we_recommend_to_change_password_after_login", "We recommend to change password after login!"));
		} else {
			StringBuilder mainText = new StringBuilder();
			String defaultServerURL = settings.getProperty("DEFAULT_SERVER_URL", "cloud4club.com");
			if (!StringUtil.isEmpty(defaultServerURL) && defaultServerURL.contains("cloud4club")) {
				mainText = mainText.append(iwrb.getLocalizedString("account_was_modified_explanation_cloud4club", "Your access to cloud4club.com has been modified.")).append("\n\r\n\r");
				if (!MapUtil.isEmpty(emailProps) && emailProps.containsKey(UserConstants.EMAIL_PLACEHOLDER_CLUB)) {
					mainText = mainText.append(MessageFormat.format(iwrb.getLocalizedString("account_was_modified_explanation_cloud4club.club", "Club: {0}"), emailProps.get(UserConstants.EMAIL_PLACEHOLDER_CLUB))).append("\n\r");
				}
				if (!MapUtil.isEmpty(emailProps) && emailProps.containsKey(UserConstants.EMAIL_PLACEHOLDER_ADDED_ACCESS)) {
					mainText = mainText.append(MessageFormat.format(iwrb.getLocalizedString("account_was_modified_explanation_cloud4club.added_access", "Change: {0}"), emailProps.get(UserConstants.EMAIL_PLACEHOLDER_ADDED_ACCESS))).append("\n\r");
				}
			} else {
				if (!MapUtil.isEmpty(emailProps) && emailProps.containsKey(UserConstants.EMAIL_PLACEHOLDER_ADDED_ACCESS)) {
					mainText = mainText.append(MessageFormat.format(iwrb.getLocalizedString("account_was_modified_explanation_felix", "Your account was modified {0}. Please, login in to review changes."), emailProps.get(UserConstants.EMAIL_PLACEHOLDER_ADDED_ACCESS)));
				} else {
					mainText = mainText.append(MessageFormat.format(iwrb.getLocalizedString("account_was_modified_explanation", "Your account was modified. Please, login in to review changes."), CoreConstants.EMPTY));
				}
			}
			text = text.append(mainText.toString()).append("\n\r").append(iwrb.getLocalizedString("login_here", "Login here")).append(": ").append(serverLink);
		}
		text.append("\n\r\n\r").append(iwrb.getLocalizedString("with_regards", "With regards,")).append("\n\r").append(settings.getProperty("with_regards_text", serverLink.concat(" team")));

		sendEmail(email, subject, text.toString());
	}

	private Commune getCommune(String name) {
		if (StringUtil.isEmpty(name)) {
			return null;
		}

		try {
			CommuneHome communeHome = (CommuneHome) IDOLookup.getHome(Commune.class);
			return communeHome.findByCommuneName(name);
		} catch (Exception e) {}

		return null;
	}

	private boolean sendEmail(String emailTo, String subject, String text) {
		IWMainApplication iwma = getApplication();
		IWMainApplicationSettings settings = iwma.getSettings();
		String from = settings.getProperty(CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS, "staff@idega.com");

		if (settings.getBoolean("sua.send_simple_mail", Boolean.FALSE)) {
			return SendMail.sendSimpleMail(from, emailTo, subject, text);
		}

		try {
			Message msg = SendMail.send(from, emailTo, null, null, null, null, subject, text);
			return msg != null;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error sending mail to '" + emailTo + "' with subject '" + subject + "' and message '" + text + "'", e);
		}

		return false;
	}

	private PostalCode createPostalCode(String postalCodeValue, Country country, Commune commune, String city) {
		PostalCode postalCode = null;
		try {
			PostalCodeHome postalCodeHome = (PostalCodeHome) getIDOHome(PostalCode.class);
			postalCode = postalCodeHome.create();
			postalCode.setName(commune == null ? city : commune.getCommuneName());
			postalCode.setPostalCode(postalCodeValue);
			postalCode.setCountry(country);
			postalCode.setCommune(commune);
			postalCode.store();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return postalCode;
	}

	private void checkChildGroups(List<Integer> childGroups, Integer primaryGroupId) {
		int existsAnyNull = 0;
		for (int i = 0; i < childGroups.size(); i++) {
			if (childGroups.get(i) == null) {
				existsAnyNull++;
			}
		}

		for (int i = 0; i < existsAnyNull; i++) {
			childGroups.remove(null);
		}

		if (childGroups.size() == 0) {
			childGroups.add(primaryGroupId);
		}
	}

	private Country getCountryById(String countryId) {
		if (StringUtil.isEmpty(countryId)) {
			return null;
		}

		try {
			return ((CountryHome) getIDOHome(Country.class)).findByPrimaryKey(countryId);
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public Country getCountry(String countryName) {
		if (countryName == null) {
			return null;
		}

		try {
			return ((CountryHome) getIDOHome(Country.class)).findByCountryName(countryName);
		} catch (Exception e) {}

		return null;
	}

	@Override
	public String getCountryIdByCountryName(String countryName) {
		Country country = getCountry(countryName);
		if (country == null) {
			return null;
		}

		return country.getPrimaryKey().toString();
	}

	private PostalCode getPostalCode(String postalCode, Country country) {
		if (StringUtil.isEmpty(postalCode)) {
			return null;
		}

		try {
			PostalCodeHome postalCodeHome = (PostalCodeHome) getIDOHome(PostalCode.class);
			return country == null ? postalCodeHome.findByPostalCode(postalCode) : postalCodeHome.findByPostalCodeAndCountryId(postalCode, (Integer) country.getPrimaryKey());
		} catch (Exception e) {}

		return null;
	}

	private <E extends IDOEntity> IDOHome getIDOHome(Class<E> beanClass) {
		try {
			return IDOLookup.getHome(beanClass);
		} catch (IDOLookupException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String isValidEmail(String email) {
		String error = "Please provide valid email!";
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return error;
		}

		IWResourceBundle iwrb = getBundle(iwc).getResourceBundle(iwc);
		String errorText = iwrb.getLocalizedString("invalid_email", error);

		if (email == null) {
			return errorText;
		}

		if (EmailValidator.getInstance().validateEmail(email)) {
			return null;	// Email is valid
		}

		return errorText;	//	Email is invalid
	}

	private void removeUserFromOldGroups(IWContext iwc, List<Integer> deselectedGroups, User user) {
		if (iwc == null || deselectedGroups == null || user == null) {
			return;
		}
		if (deselectedGroups.size() == 0) {
			return;
		}

		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return;
		}
		GroupBusiness groupBusiness = getGroupBusiness();
		if (groupBusiness == null) {
			return;
		}

		//	Getting deselected groups
		Integer groupId = null;
		Group group = null;
		List<Group> groups = new ArrayList<>();
		for (int i = 0; i < deselectedGroups.size(); i++) {
			group = null;
			groupId = deselectedGroups.get(i);

			try {
				group = groupBusiness.getGroupByGroupID(groupId.intValue());
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}
			if (group != null) {
				groups.add(group);
			}
		}

		//	Removing user from deselected groups
		User currentUser = iwc.getCurrentUser();
		group = null;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);

			try {
				userBusiness.removeUserFromGroup(user, group, currentUser);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (RemoveException e) {
				e.printStackTrace();
			}
		}
	}

	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		if (userBusiness == null) {
			try {
				userBusiness = IBOLookup.getServiceInstance(iwac, UserBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}

	private GroupBusiness getGroupBusiness() {
		if (groupBusiness == null) {
			try {
				groupBusiness = IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), GroupBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}

	private IWBundle getBundle(IWContext iwc) {
		return iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
	}

	@Override
	public String getSimpleUserApplicationClassName() {
		return SimpleUserApp.class.getName();
	}

	@Override
	public String getGroupSaveStatus(boolean needErrorMessage) {
		String sucessMessage = "Your changes were successfully saved.";
		String errorMessage = "Error occurred while saving your changes.";
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return errorMessage;
		}

		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		sucessMessage = iwrb.getLocalizedString("success_saving_group", sucessMessage);
		errorMessage = iwrb.getLocalizedString("error_saving_group", errorMessage);

		return needErrorMessage ? errorMessage : sucessMessage;
	}

	@Override
	public String saveGroup(String name, String homePageId, String type, String description, String parentGroupId, String groupId) {
		if (StringUtil.isEmpty(name)) {
			return null;
		}

		GroupBusiness groupBusiness = getGroupBusiness();
		if (groupBusiness == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		Group parentGroup = null;
		try {
			parentGroup = groupBusiness.getGroupByGroupID(Integer.valueOf(parentGroupId));
		} catch(Exception e) {
		}

		int homePageIdInt = -1;
		try {
			homePageIdInt = homePageId == null ? -1 : Integer.valueOf(homePageId);
		} catch(Exception e) {
			e.printStackTrace();
		}

		Group group = null;
		try {
			group = groupBusiness.getGroupByGroupID(Integer.valueOf(groupId));
		} catch(Exception e) {}

		if (group == null) {
			//	Create group
			if (parentGroup == null) {
				try {
					group = groupBusiness.createGroup(name, description, type, homePageIdInt);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					group = groupBusiness.createGroupUnder(name, description, type, homePageIdInt, -1, parentGroup);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (CreateException e) {
					e.printStackTrace();
				}
			}

			if (group == null) {
				return null;
			}
		}
		else {
			//	Modify group
			group.setName(name);
			group.setHomePageID(homePageIdInt);
			group.setGroupType(type);
			group.setDescription(description);

			group.store();

			try {
				List<Group> currentParentGroups = group.getParentGroups();
				if (currentParentGroups == null || currentParentGroups.isEmpty() && parentGroup != null) {
					//	There were no parent groups until now - setting the first one
					parentGroup.addGroup(group);
				}
				else if (currentParentGroups != null && !currentParentGroups.isEmpty()) {
					if (parentGroup == null) {
						//	Removing all parent groups
						User currentUser = iwc.getCurrentUser();
						for (Group oneOfParentGroup: currentParentGroups) {
							oneOfParentGroup.removeGroup(group, currentUser);
						}
					}
					else if (!currentParentGroups.contains(parentGroup)) {
						//	Adding parent group
						parentGroup.addGroup(group);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return BuilderLogic.getInstance().reloadGroupsInCachedDomain(iwc, iwc.getServerName()) ? group.getId() : null;
	}

	@Override
	public List<AdvancedProperty> findAvailablePages(String phrase) {
		if (phrase == null || CoreConstants.EMPTY.equals(phrase)) {
			return null;
		}

		ICPageHome pagesHome = null;
		try {
			pagesHome = (ICPageHome) IDOLookup.getHome(ICPage.class);
		} catch (IDOLookupException e) {
			e.printStackTrace();
		}
		if (pagesHome == null) {
			return null;
		}

		List<AdvancedProperty> pages = new ArrayList<>();

		//	Getting pages by localized name
		Collection<IBPageName> pagesWithName = null;
		try {
			pagesWithName = ((IBPageNameHome) IDOLookup.getHome(IBPageName.class)).findAllByPhrase(phrase, CoreUtil.getIWContext().getCurrentLocale());
		} catch(Exception e) {
			e.printStackTrace();
		}
		Map<String, String> pagesIdAndName = new HashMap<>();
		if (pagesWithName != null && !pagesWithName.isEmpty()) {
			IBPageName pageName = null;
			for (Iterator<IBPageName> it = pagesWithName.iterator(); it.hasNext();) {
				pageName = it.next();

				pagesIdAndName.put(String.valueOf(pageName.getPageId()), pageName.getPageName());
			}
		}
		List<String> ids = null;
		if (pagesIdAndName.isEmpty()) {
			ids = new ArrayList<>();
		}
		else {
			ids = new ArrayList<>(pagesIdAndName.keySet());

			Collection<ICPage> pagesByLocalizedName = null;
			try {
				pagesByLocalizedName = pagesHome.findAllByPrimaryKeys(ids);
			} catch (FinderException e) {
				e.printStackTrace();
			}
			pages = new ArrayList<>(getFilteredPages(pagesByLocalizedName, pagesIdAndName));
			ids = new ArrayList<>();
			for (AdvancedProperty page : pages) {
				ids.add(page.getId());
			}
		}

		//	Getting the rest of pages without localized name
		Collection<ICPage> pagesWithoutLocalizedName = null;
		try {
			pagesWithoutLocalizedName = pagesHome.findAllByPhrase(phrase, ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pages.addAll(getFilteredPages(pagesWithoutLocalizedName, null));

		if (!pages.isEmpty()) {
			Locale locale = null;
			IWContext iwc = CoreUtil.getIWContext();
			if (iwc != null) {
				locale = iwc.getCurrentLocale();
			}
			Collections.sort(pages, new AdvancedPropertyComparator(locale == null ? Locale.ENGLISH : locale));
		}
		return pages;
	}

	private List<AdvancedProperty> getFilteredPages(Collection<ICPage> pages, Map<String, String> pagesIdAndName) {
		List<AdvancedProperty> filteredPages = new ArrayList<>();
		if (pages == null || pages.isEmpty()) {
			return filteredPages;
		}

		ICPage page = null;
		String id = null;
		String name = null;
		for (Iterator<ICPage> it = pages.iterator(); it.hasNext();) {
			page = it.next();
			id = null;
			name = null;

			if (page != null && page.isPage() && !page.getDeleted()) {
				id = page.getId();

				if (pagesIdAndName != null) {
					name = pagesIdAndName.get(id);
				}
				if (name == null) {
					name = page.getName();
				}

				filteredPages.add(new AdvancedProperty(id, name));
			}
		}

		return filteredPages;
	}

	private List<Group> getTopGroups(IWContext iwc, String groupTypes, boolean getTopAndParentGroups, boolean useChildrenOfTopNodesAsParentGroups) {
		Collection<Group> topGroups = getGroupHelper().getTopGroupsFromDomain(iwc);
		if (!getTopAndParentGroups) {
			topGroups = getGroupHelper().getTopAndParentGroups(topGroups);
		}

		return new ArrayList<>(getGroupHelper().getFilteredGroups(iwc, topGroups, groupTypes, CoreConstants.COMMA, useChildrenOfTopNodesAsParentGroups));
	}

	@Override
	public List<AdvancedProperty> getAvailableGroups(
			String groupTypes,
			String groupTypesForChildrenGroups,
			String roleTypes,
			int groupId,
			int groupsType,
			boolean getTopAndParentGroups,
			boolean useChildrenOfTopNodesAsParentGroups,
			String subGroups,
			String subGroupsToExclude
	) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		List<Group> groups = null;
		switch (groupsType) {
			case 0:
				//	Top groups
				groups = getTopGroups(iwc, groupTypes, getTopAndParentGroups, useChildrenOfTopNodesAsParentGroups);
				break;
			case 1:
				//	Child groups
				return getChildGroups(String.valueOf(groupId), groupTypesForChildrenGroups, roleTypes, subGroups, subGroupsToExclude);
			default:
				break;
		}

		if (groups == null || groups.size() == 0) {
			return null;
		}
		List<AdvancedProperty> result = new ArrayList<>();
		for (Group group: groups) {
			if (group == null) {
				continue;
			}

			result.add(new AdvancedProperty(group.getId(), group.getName()));
		}

		Collections.sort(result, new AdvancedPropertyComparator(iwc.getCurrentLocale()));

		return result;
	}

	@Override
	public Layer getRolesEditor(IWContext iwc, int groupId, boolean addInput, List<String> selectedRoles) {
		return getPresentationHelper().getRolesEditor(iwc, groupId, addInput, selectedRoles);
	}

	@Override
	public boolean changePermissionValueForRole(int groupId, String permissionKey, String roleKey, boolean value) {
		if (permissionKey == null || roleKey == null) {
			return false;
		}

		GroupBusiness groupBusiness = getGroupBusiness();
		if (groupBusiness == null) {
			return false;
		}

		Group group = null;
		try {
			group = groupBusiness.getGroupByGroupID(groupId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (group == null) {
			return false;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}

		AccessController accessController = iwc.getAccessController();
		if (accessController == null) {
			return false;
		}

		boolean result = setRoleByPermissionForGroup(iwc, accessController, groupId, value, roleKey, permissionKey);
		return result;
	}

	private boolean setRoleByPermissionForGroup(IWContext iwc, AccessController accessController, int groupId, boolean value, String roleKey, String permissionKey) {
		if (value) {
			return accessController.addRoleToGroup(roleKey, permissionKey, groupId, iwc);
		}

		return accessController.removeRoleFromGroup(roleKey, permissionKey, groupId, iwc);
	}

	@Override
	public Document addNewRole(String roleKey, int groupId, List<String> selectedRoles) {
		if (roleKey == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		try {
			iwc.getAccessController().createRoleWithRoleKey(roleKey);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return BuilderLogic.getInstance().getRenderedComponent(iwc, getRolesEditor(iwc, groupId, false, selectedRoles), false);
	}

	@Override
	public Document getRenderedRolesEditor(int groupId, List<String> selectedRoles) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		return BuilderLogic.getInstance().getRenderedComponent(iwc, getRolesEditor(iwc, groupId, false, selectedRoles), false);
	}

	@Override
	public List<Integer> getPagerProperties(String id) {
		if (StringUtil.isEmpty(id) || pagerProperties == null) {
			return null;
		}

		try {
			return pagerProperties.get(id);
		} catch(ClassCastException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void setPagerProperties(String id, List<Integer> properties) {
		if (StringUtil.isEmpty(id) || ListUtil.isEmpty(properties)) {
			return;
		}

		pagerProperties.put(id, properties);
	}

	@Override
	public String getIdForPagerProperties(SimpleUserPropertiesBean bean) {
		if (bean == null) {
			return null;
		}

		return new StringBuilder(bean.getInstanceId()).append(bean.getParentGroupId()).append(bean.getGroupId()).append(bean.getOrderBy()).toString();
	}

	public SimpleUserAppHelper getPresentationHelper() {
		return presentationHelper;
	}

	public void setPresentationHelper(SimpleUserAppHelper presentationHelper) {
		this.presentationHelper = presentationHelper;
	}

	@Override
	public AdvancedProperty isValidUserName(String userName) {
		AdvancedProperty result = new AdvancedProperty(Boolean.FALSE.toString(), "Sorry, some error occurred...");

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return result;
		}

		IWResourceBundle iwrb = getBundle(iwc).getResourceBundle(iwc);
		if (iwrb == null) {
			return result;
		}

		if (StringUtil.isEmpty(userName)) {
			result.setValue(iwrb.getLocalizedString("empty_user_name", "User name can not be empty!"));
			return result;
		}

		try {
			LoginTableHome loginInfo = (LoginTableHome) IDOLookup.getHome(LoginTable.class);
			LoginTable login = loginInfo.findByLogin(userName);

			if (login != null && userName.equals(login.getUserLogin())) {
				result.setValue(iwrb.getLocalizedString("user_name_exists", "Such user name already exists!"));
			} else {
				result.setId(Boolean.TRUE.toString());
				result.setValue(null);
			}
		} catch (FinderException e) {
			result.setId(Boolean.TRUE.toString());
			result.setValue(null);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while checking if user name is valid: " + userName, e);
		}

		return result;
	}

	@Override
	public String getUserIdByLogin(String login) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		if (StringUtil.isEmpty(login)) {
			return null;
		}

		try {
			LoginTableHome loginInfo = (LoginTableHome) IDOLookup.getHome(LoginTable.class);
			LoginTable loginTable = loginInfo.findByLogin(login);

			return String.valueOf(loginTable.getUserId());
		} catch (FinderException e) {
			return null;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while getting user id by user login: " + login, e);
		}

		return null;
	}
}