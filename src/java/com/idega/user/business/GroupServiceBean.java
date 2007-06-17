package com.idega.user.business;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.FinderException;

import org.jdom.Document;

import com.idega.bean.GroupDataBean;
import com.idega.bean.GroupMembersDataBean;
import com.idega.bean.GroupPropertiesBean;
import com.idega.bean.PropertiesBean;
import com.idega.bean.UserPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.business.chooser.helper.GroupsChooserHelper;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupsManagerBean;
import com.idega.user.data.User;
import com.idega.user.presentation.group.GroupInfoViewerBlock;
import com.idega.user.presentation.group.GroupUsersViewerBlock;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.webface.WFUtil;

public class GroupServiceBean extends IBOServiceBean implements GroupService {
	
	private static final long serialVersionUID = 1649699626972508631L;
	
	private final GroupHelperBusinessBean helper = new GroupHelperBusinessBean();
	
	private LoginTableHome loginHome = null;
	private LoginBusinessBean loginBean = null;
	
	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;

	/**
	 * Returns tree of Groups
	 */
	public List<GroupNode> getTopGroupNodes() {
		return helper.getTopGroupNodes();
	}
	
	/**
	 * Returns tree of Groups
	 */
	public List<GroupNode> getGroupsTree(String login, String password) {
		if (login == null || password == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (isLoggedUser(iwc, login)) {
			return getTopGroupNodes();
		}
		
		if (logInUser(iwc, login, password)) {
			return getTopGroupNodes();
		}
		
		return null;
	}
	
	/**
	 * Checks if can use DWR on remote server
	 */
	public boolean canUseRemoteServer(String server) {
		if (server == null) {
			return false;
		}
		
		if (server.endsWith("/")) {
			server = server.substring(0, server.lastIndexOf("/"));
		}
		
		String engineScript = new StringBuffer(server).append("/dwr/engine.js").toString();
		String interfaceScript = new StringBuffer(server).append(CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT).toString();
		
		return (existsFileOnRemoteServer(engineScript) && existsFileOnRemoteServer(interfaceScript));
	}
	
	/**
	 * Returns user's parameters for getting info about Groups
	 */
	public GroupPropertiesBean getGroupPropertiesBean(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		GroupsManagerBean bean = getBean();
		if (bean == null) {
			return null;
		}
		
		GroupPropertiesBean properties = bean.getGroupProperties(instanceId);
		if (properties == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		IWResourceBundle iwrb = getBundle().getResourceBundle(iwc);
		List<String> localizedText = new ArrayList<String>();
		localizedText.add(iwrb.getLocalizedString("group_name", "Name: "));					//	0
		localizedText.add(iwrb.getLocalizedString("short_name", "Short name: "));			//	1
		localizedText.add(iwrb.getLocalizedString("group_address", "Address: "));			//	2
		localizedText.add(iwrb.getLocalizedString("group_phone", "Phone: "));				//	3
		localizedText.add(iwrb.getLocalizedString("group_fax", "Fax: "));					//	4
		localizedText.add(iwrb.getLocalizedString("group_homepage", "Homepage: "));			//	5
		localizedText.add(iwrb.getLocalizedString("group_email", "Email: "));				//	6
		localizedText.add(iwrb.getLocalizedString("group_description", "Description: "));	//	7
		localizedText.add(iwrb.getLocalizedString("goup_extra_info", "Info: "));			//	8
		properties.setLocalizedText(localizedText);
		return properties;
	}
	
	/**
	 * Returns user's parameters for getting info about Groups members
	 * @param instanceId
	 * @return
	 */
	public UserPropertiesBean getUserPropertiesBean(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		GroupsManagerBean bean = getBean();
		if (bean == null) {
			return null;
		}
		
		UserPropertiesBean properties = bean.getUserProperties(instanceId);
		if (properties == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		IWResourceBundle iwrb = getBundle().getResourceBundle(iwc);
		List<String> localizedText = new ArrayList<String>();
		
		localizedText.add(iwrb.getLocalizedString("user_name", "Name: "));					//	0
		localizedText.add(iwrb.getLocalizedString("user_title", "Title: "));				//	1
		localizedText.add(iwrb.getLocalizedString("user_age", "Age: "));					//	2
		localizedText.add(iwrb.getLocalizedString("user_workphone", "Workphone: "));		//	3
		localizedText.add(iwrb.getLocalizedString("user_homephone", "Homephone: "));		//	4
		localizedText.add(iwrb.getLocalizedString("user_mobilephone", "Mobilephone: "));	//	5
		localizedText.add(iwrb.getLocalizedString("group_email", "Email: "));				//	6
		localizedText.add(iwrb.getLocalizedString("user_education", "Education: "));		//	7
		localizedText.add(iwrb.getLocalizedString("user_school", "School: "));				//	8
		localizedText.add(iwrb.getLocalizedString("user_area", "Area: "));					//	9
		localizedText.add(iwrb.getLocalizedString("user_began_work", "Began work: "));		//	10
		localizedText.add(iwrb.getLocalizedString("user_status", "Status: "));				//	11
		localizedText.add(iwrb.getLocalizedString("user_address", "Address: "));			//	12
		
		properties.setLocalizedText(localizedText);
		return properties;
	}
	
	/**
	 * Returns info about selected groups
	 */
	public List<GroupDataBean> getGroupsInfo(GroupPropertiesBean bean) {
		//	Checking if valid parameters
		if (bean == null) {
			return null;
		}
		if (bean.getUniqueIds() == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (bean.isRemoteMode()) {
			//	Checking if user is allowed to get info
			if (!isLoggedUser(iwc, bean.getLogin())) {
				if (!logInUser(iwc, bean.getLogin(), bean.getPassword())) {
					return null;
				}
			}
		}
		
		return getGroupBusiness(iwc).getGroupsData(bean);
	}
	
	public boolean reloadProperties(String instanceId) {
		if (instanceId == null) {
			return false;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}
		
		BuilderLogic builder = BuilderLogic.getInstance();
		String pageKey = builder.getCurrentIBPage(iwc);
		String propertyName = ":method:1:implied:void:setGroups:com.idega.bean.PropertiesBean:";
		String[] values = builder.getPropertyValues(iwc.getIWMainApplication(), pageKey, instanceId, propertyName, null, true);
		if (values == null) {
			return false;
		}
		if (values.length == 0) {
			return false;
		}
		GroupsChooserHelper helper = new GroupsChooserHelper();
		PropertiesBean bean = helper.getExtractedPropertiesFromString(values[0]);
		if (bean == null) {
			return false;
		}
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = bean;
		
		Class[] classes = new Class[2];
		classes[0] = String.class;
		classes[1] = PropertiesBean.class;
		
		//	Setting parameters to bean, these parameters will be taken by DWR and sent to selected server to get required info
		WFUtil.invoke(UserConstants.GROUPS_MANAGER_BEAN_ID, "addAbstractProperties", parameters, classes);
		return true;
	}
	
	public Document getGroupInfoPresentationObject(List<GroupDataBean> groupsData, GroupPropertiesBean bean) {
		if (groupsData == null || bean == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		GroupInfoViewerBlock groupViewer = new GroupInfoViewerBlock();
		groupViewer.setShowName(bean.isShowName());
		groupViewer.setShowHomePage(bean.isShowHomePage());
		groupViewer.setShowDescription(bean.isShowDescription());
		groupViewer.setShowExtraInfo(bean.isShowExtraInfo());
		groupViewer.setShowShortName(bean.isShowShortName());
		groupViewer.setShowPhone(bean.isShowPhone());
		groupViewer.setShowFax(bean.isShowFax());
		groupViewer.setShowEmails(bean.isShowEmails());
		groupViewer.setShowAddress(bean.isShowAddress());
		groupViewer.setShowEmptyFields(bean.isShowEmptyFields());
		groupViewer.setShowLabels(bean.isShowLabels());
		
		groupViewer.setGroupsData(groupsData);
		
		return BuilderLogic.getInstance().getRenderedComponent(iwc, groupViewer, false);
	}
	
	public List<GroupMembersDataBean> getUsersInfo(UserPropertiesBean bean) {
		//	Checking if valid parameters
		if (bean == null) {
			return null;
		}
		if (bean.getUniqueIds() == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (bean.isRemoteMode()) {
			//	Checking if user is allowed to get info
			if (!isLoggedUser(iwc, bean.getLogin())) {
				if (!logInUser(iwc, bean.getLogin(), bean.getPassword())) {
					return null;
				}
			}
		}
		
		return getUserBusiness(iwc).getGroupsMembersData(bean);
	}
	
	public Document getGroupsMembersPresentationObject(List<GroupMembersDataBean> membersData, UserPropertiesBean bean) {
		if (membersData == null || bean == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		GroupUsersViewerBlock usersViewer = new GroupUsersViewerBlock();
		usersViewer.setShowGroupName(bean.isShowGroupName());
		usersViewer.setShowTitle(bean.isShowTitle());
		usersViewer.setShowAge(bean.isShowAge());
		usersViewer.setShowWorkPhone(bean.isShowWorkPhone());
		usersViewer.setShowHomePhone(bean.isShowHomePhone());
		usersViewer.setShowMobilePhone(bean.isShowMobilePhone());
		usersViewer.setShowEmails(bean.isShowEmails());
		usersViewer.setShowEducation(bean.isShowEducation());
		usersViewer.setShowSchool(bean.isShowSchool());
		usersViewer.setShowArea(bean.isShowArea());
		usersViewer.setShowBeganWork(bean.isShowBeganWork());
		usersViewer.setShowLabels(bean.isShowLabels());
		
		usersViewer.setMembersData(membersData);
		
		usersViewer.setImageHeight(bean.getImageHeight());
		usersViewer.setImageWidth(bean.getImageWidth());
		
		usersViewer.setServer(bean.getServer());
		
		return BuilderLogic.getInstance().getRenderedComponent(iwc, usersViewer, false);
	}
	
	/** Private methods starts **/
	/**
	 * Logs in user
	 * @param iwc
	 * @param login
	 * @param password
	 * @return
	 */
	private boolean logInUser(IWContext iwc, String login, String password) {
		if (iwc == null || login == null || password == null) {
			return false;
		}

		return getLoginBean(iwc).logInUser(iwc.getRequest(), login, password);
	}
	
	/**
	 * Checks if file exists on server
	 * @param urlToFile
	 * @return
	 */
	private boolean existsFileOnRemoteServer(String urlToFile) {
		InputStream streamToFile = null;
		
		try {
			URL dwr = new URL(urlToFile);
			streamToFile = dwr.openStream();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if (streamToFile == null) {
			return false;
		}
		try {
			streamToFile.close();
		} catch (Exception e) {}
		
		return true;
	}
	
	/**
	 * Checks if current (logged) user is the same user that is making request
	 * @param iwc
	 * @param userName
	 * @return
	 */
	private boolean isLoggedUser(IWContext iwc, String userName) {
		if (iwc == null || userName == null) {
			return false;
		}
		
		//	Geting current user
		User current = null;
		try {
			current = iwc.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (current == null) {	//	Not logged
			return false;
		}
		
		LoginTableHome loginHome = getLoginHome();
		if (loginHome == null) {
			return false;
		}
		
		int userId = 0;
		try {
			userId = Integer.valueOf(current.getId());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		
		//	Checking if current user is making request
		LoginTable login = null;
		try {
			login = loginHome.findLoginForUser(userId);
		} catch (FinderException e) {
			e.printStackTrace();
			return false;
		}
		if (userName.equals(login.getUserLogin())) {
			return true;
		}
		
		return false;
	}
	
	private GroupsManagerBean getBean() {
		Object o = WFUtil.getBeanInstance(UserConstants.GROUPS_MANAGER_BEAN_ID);
		if (!(o instanceof GroupsManagerBean)) {
			return null;
		}
		return (GroupsManagerBean) o;
	}
	
	private synchronized GroupBusiness getGroupBusiness(IWContext iwc) {
		if (groupBusiness == null) {
			try {
				groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}
	
	private synchronized LoginBusinessBean getLoginBean(IWContext iwc) {
		if (loginBean == null) {
			try {
				loginBean = LoginBusinessBean.getLoginBusinessBean(iwc.getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return loginBean;
	}
	
	private synchronized LoginTableHome getLoginHome() {
		if (loginHome == null) {
			try {
				loginHome = (LoginTableHome) IDOLookup.getHome(LoginTable.class);
			} catch (IDOLookupException e) {
				e.printStackTrace();
			}
		}
		return loginHome;
	}
	
	private synchronized UserBusiness getUserBusiness(IWContext iwc) {
		if (userBusiness == null) {
			try {
				userBusiness = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}
	
	protected String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}
}
