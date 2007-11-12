package com.idega.user.business;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;

import org.jdom.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOSessionBean;
import com.idega.business.chooser.helper.GroupsChooserHelper;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.cache.IWCacheManager2;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.bean.GroupMembersDataBean;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.GroupsManagerBean;
import com.idega.user.bean.PropertiesBean;
import com.idega.user.bean.UserPropertiesBean;
import com.idega.user.data.User;
import com.idega.user.presentation.group.GroupInfoViewerBlock;
import com.idega.user.presentation.group.GroupUsersViewerBlock;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.webface.WFUtil;

public class GroupServiceBean extends IBOSessionBean implements GroupService {
	
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
		return getBasicGroupPropertiesBean(instanceId);
	}
	
	/**
	 * Returns user's parameters for getting info about Groups members
	 * @param instanceId
	 * @return
	 */
	public UserPropertiesBean getUserPropertiesBean(String instanceId) {
		return getBasicUserPropertiesBean(instanceId);
	}
	
	@SuppressWarnings("unchecked")
	private Map getCache(IWContext iwc, String cacheKey, int minutes) {
		IWCacheManager2 cache = IWCacheManager2.getInstance(iwc.getIWMainApplication());
		if (cache == null) {
			return null;
		}
		
		long time = new Long(minutes * 60).longValue();
		return cache.getCache(cacheKey, 1000, true, false, time, time);
	}
	
	@SuppressWarnings("unchecked")
	private List<GroupDataBean> getGroupInfoFromCache(IWContext iwc, String id, int minutes) {
		Map cache = getCache(iwc, UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return null;
		}
		
		Object cachedList = null;
		try {
			cachedList = cache.get(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cachedList == null) {
			return null;
		}
		
		if (cachedList instanceof List) {
			List abstractList = (List) cachedList;
			List<GroupDataBean> extractedData = new ArrayList<GroupDataBean>();
			Object o = null;
			for (int i = 0; i < abstractList.size(); i++) {
				o = abstractList.get(i);
				if (o instanceof GroupDataBean) {
					extractedData.add((GroupDataBean) o);
				}
				else {
					return null;
				}
			}
			return extractedData;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void addGroupInfoToCache(IWContext iwc, String id, int minutes, List<GroupDataBean> info) {
		Map cache = getCache(iwc, UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return;
		}
		
		try {
			cache.put(id, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns info about selected groups
	 */
	public List<GroupDataBean> getGroupsInfo(GroupPropertiesBean bean) {
		//	Checking if valid parameters
		if (bean == null) {
			return null;
		}
		
		if (bean.getInstanceId() == null) {
			return null;
		}
		
		List<String> uniqueIds = getUniqueIds(true).get(bean.getInstanceId());
		if (uniqueIds == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (!canUseServer(iwc, bean)) {
			return null;
		}
		
		Integer cacheTime = bean.getCacheTime();
		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List<GroupDataBean> cachedInfo = getGroupInfoFromCache(iwc, bean.getInstanceId(), cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List<GroupDataBean> info = getGroupBusiness(iwc).getGroupsData(bean, uniqueIds);
		if (useCache && info != null) {
			addGroupInfoToCache(iwc, bean.getInstanceId(), cacheTime.intValue(), info);
		}
		return info;
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
		String name = new StringBuffer(":method:1:implied:void:setGroups:").append(PropertiesBean.class.getName()).append(":").toString();
		String[] values = builder.getPropertyValues(iwc.getIWMainApplication(), pageKey, instanceId, name, null, true);
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
		
		Class<?>[] classes = new Class[2];
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
	
	@SuppressWarnings("unchecked")
	private List<GroupMemberDataBean> getUsersInfoFromCache(IWContext iwc, String id, int minutes) {
		Map cache = getCache(iwc, UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return null;
		}
		
		Object cachedList = null;
		try {
			cachedList = cache.get(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cachedList == null) {
			return null;
		}
		
		if (cachedList instanceof List) {
			List abstractList = (List) cachedList;
			List<GroupMemberDataBean> extractedData = new ArrayList<GroupMemberDataBean>();
			Object o = null;
			for (int i = 0; i < abstractList.size(); i++) {
				o = abstractList.get(i);
				if (o instanceof GroupMemberDataBean) {
					extractedData.add((GroupMemberDataBean) o);
				}
				else {
					return null;
				}
			}
			return extractedData;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void addUsersInfoToCache(IWContext iwc, String id, int minutes, List<GroupMemberDataBean> info) {
		Map cache = getCache(iwc, UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return;
		}
		
		try {
			cache.put(id, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<GroupMemberDataBean> getUsersInfo(UserPropertiesBean bean) {
		//	Checking if valid parameters
		if (bean == null) {
			return null;
		}
		
		List<String> uniqueIds = getUniqueIds(false).get(bean.getInstanceId());
		if (uniqueIds == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (!canUseServer(iwc, bean)) {
			return null;
		}
		
		Integer cacheTime = bean.getCacheTime();
		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List<GroupMemberDataBean> cachedInfo = getUsersInfoFromCache(iwc, bean.getInstanceId(), cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List<GroupMemberDataBean> info = getUserBusiness(iwc).getGroupsMembersData(bean, uniqueIds);
		if (useCache && info != null) {
			addUsersInfoToCache(iwc, bean.getInstanceId(), cacheTime.intValue(), info);
		}
		return info;
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
	
	public List<AdvancedProperty> getUserStatusLocalization() {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		IWResourceBundle iwrb = null;
		try {
			iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		List<AdvancedProperty> statusLocalization = new ArrayList<AdvancedProperty>();
		addStatusLocalization(statusLocalization, "STAT_ASSCOACH", iwrb.getLocalizedString("STAT_ASSCOACH", "Assistant Coach"));
		addStatusLocalization(statusLocalization, "STAT_B_CASH", iwrb.getLocalizedString("STAT_B_CASH", "Cashier"));
		addStatusLocalization(statusLocalization, "STAT_B_CEO", iwrb.getLocalizedString("STAT_B_CEO", "CEO"));
		addStatusLocalization(statusLocalization, "STAT_B_CHAIR", iwrb.getLocalizedString("STAT_B_CHAIR", "Chairman"));
		addStatusLocalization(statusLocalization, "STAT_B_COCHIEF", iwrb.getLocalizedString("STAT_B_COCHIEF", "Co-Executive"));
		addStatusLocalization(statusLocalization, "STAT_B_EXTRA", iwrb.getLocalizedString("STAT_B_EXTRA", "Stand in"));
		addStatusLocalization(statusLocalization, "STAT_B_EXTRABOARD", iwrb.getLocalizedString("STAT_B_EXTRABOARD", "Stand in board member"));
		addStatusLocalization(statusLocalization, "STAT_B_MAINBOARD", iwrb.getLocalizedString("STAT_B_MAINBOARD", "Executive board"));
		addStatusLocalization(statusLocalization, "STAT_B_PRES", iwrb.getLocalizedString("STAT_B_PRES", "President"));
		addStatusLocalization(statusLocalization, "STAT_B_SECR", iwrb.getLocalizedString("STAT_B_SECR", "Secretery"));
		addStatusLocalization(statusLocalization, "STAT_B_VICECHAIR", iwrb.getLocalizedString("STAT_B_VICECHAIR", "Vice Chairman"));
		addStatusLocalization(statusLocalization, "STAT_B_VICEPRES", iwrb.getLocalizedString("STAT_B_VICEPRES", "Vice President"));
		addStatusLocalization(statusLocalization, "STAT_CHIEF_TRAINER", iwrb.getLocalizedString("STAT_CHIEF_TRAINER", "Head Coach"));
		addStatusLocalization(statusLocalization, "STAT_COACH", iwrb.getLocalizedString("STAT_COACH", "Coach"));
		addStatusLocalization(statusLocalization, "STAT_COMP", iwrb.getLocalizedString("STAT_COMP", "Competitor"));
		addStatusLocalization(statusLocalization, "STAT_COMPYEAR", iwrb.getLocalizedString("STAT_COMPYEAR", "Competing this year"));
		addStatusLocalization(statusLocalization, "STAT_EMPL", iwrb.getLocalizedString("STAT_EMPL", "Employee"));
		addStatusLocalization(statusLocalization, "STAT_FIELD_MANAGER", iwrb.getLocalizedString("STAT_FIELD_MANAGER", "Field Manager"));
		addStatusLocalization(statusLocalization, "STAT_MEMBER", iwrb.getLocalizedString("STAT_MEMBER", "Member"));
		addStatusLocalization(statusLocalization, "STAT_MEMBYEAR", iwrb.getLocalizedString("STAT_MEMBYEAR", "Member this year"));
		addStatusLocalization(statusLocalization, "STAT_PRACT", iwrb.getLocalizedString("STAT_PRACT", "Practicioner"));
		addStatusLocalization(statusLocalization, "STAT_PRACTYEAR", iwrb.getLocalizedString("STAT_PRACTYEAR", "Practicioner this year"));
		addStatusLocalization(statusLocalization, "STAT_REF", iwrb.getLocalizedString("STAT_REF", "Referee"));
		addStatusLocalization(statusLocalization, "STAT_SPONS", iwrb.getLocalizedString("STAT_SPONS", "Sponsor"));
		addStatusLocalization(statusLocalization, "STAT_PHYSIOTHERAPIST", iwrb.getLocalizedString("STAT_PHYSIOTHERAPIST", "Physio Therapist"));
		addStatusLocalization(statusLocalization, "STAT_GUIDE", iwrb.getLocalizedString("STAT_GUIDE", "Tour Guide"));
		addStatusLocalization(statusLocalization, "STAT_REGION_MANAGER", iwrb.getLocalizedString("STAT_REGION_MANAGER", "Regional Manager"));
		
		return statusLocalization;
	}
	
	protected String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
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
	
	private void addStatusLocalization(List<AdvancedProperty> list, String key, String defaultValue) {
		AdvancedProperty localizedStatus = new AdvancedProperty();
		localizedStatus.setId(key);
		localizedStatus.setValue(defaultValue);
		list.add(localizedStatus);
	}
	
	private boolean canUseServer(IWContext iwc, PropertiesBean bean) {
		if (bean.isRemoteMode()) {
			//	Checking if user is allowed to use server
			if (!isLoggedUser(iwc, bean.getLogin())) {
				if (!logInUser(iwc, bean.getLogin(), bean.getPassword())) {
					return false;
				}
				else {
					return true;
				}
			}
			else {
				return true;
			}
		}
		return true;
	}
	
	public GroupPropertiesBean getBasicGroupPropertiesBean(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		GroupsManagerBean bean = getBean();
		if (bean == null) {
			return null;
		}
		
		return bean.getGroupProperties(instanceId);
	}
	
	public UserPropertiesBean getBasicUserPropertiesBean(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		GroupsManagerBean bean = getBean();
		if (bean == null) {
			return null;
		}
		
		return bean.getUserProperties(instanceId);
	}
	
	@SuppressWarnings("unchecked")
	private boolean clearCache(String cacheKey, PropertiesBean bean) {
		if (cacheKey == null || bean == null) {
			return false;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}
		
		if (!canUseServer(iwc, bean)) {
			return false;
		}
		
		int minutes = 0;
		Integer cacheTime = bean.getCacheTime();
		if (cacheTime != null) {
			minutes = cacheTime.intValue();
		}
		
		Map cache = getCache(iwc, cacheKey, minutes);
		if (cache == null) {
			return false;
		}
		
		try {
			cache.remove(bean.getInstanceId());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean clearGroupInfoCache(GroupPropertiesBean bean) {
		return clearCache(UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, bean);
	}
	
	public boolean clearUsersInfoCache(UserPropertiesBean bean) {
		return clearCache(UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, bean);
	}
	
	public List<String> getLocalizationForGroupInfo() {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		if (iwrb == null) {
			return null;
		}
		
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
		
		return localizedText;
	}
	
	public List<String> getLocalizationForGroupUsersInfo() {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		if (iwrb == null) {
			return null;
		}
		
		List<String> localizedText = new ArrayList<String>();
		localizedText.add(iwrb.getLocalizedString("user_name", "Name: "));							//	0
		localizedText.add(iwrb.getLocalizedString("user_title", "Title: "));						//	1
		localizedText.add(iwrb.getLocalizedString("user_age", "Age: "));							//	2
		localizedText.add(iwrb.getLocalizedString("user_workphone", "Workphone: "));				//	3
		localizedText.add(iwrb.getLocalizedString("user_homephone", "Homephone: "));				//	4
		localizedText.add(iwrb.getLocalizedString("user_mobilephone", "Mobilephone: "));			//	5
		localizedText.add(iwrb.getLocalizedString("group_email", "Email: "));						//	6
		localizedText.add(iwrb.getLocalizedString("user_education", "Education: "));				//	7
		localizedText.add(iwrb.getLocalizedString("user_school", "School: "));						//	8
		localizedText.add(iwrb.getLocalizedString("user_area", "Area: "));							//	9
		localizedText.add(iwrb.getLocalizedString("user_began_work", "Began work: "));				//	10
		localizedText.add(iwrb.getLocalizedString("user_status", "Status: "));						//	11
		localizedText.add(iwrb.getLocalizedString("user_address", "Address: "));					//	12
		localizedText.add(iwrb.getLocalizedString("home", "hm"));									//	13
		localizedText.add(iwrb.getLocalizedString("work", "wrk"));									//	14
		localizedText.add(iwrb.getLocalizedString("mobile", "mbl"));								//	15
		localizedText.add(iwrb.getLocalizedString("user_extra_info", "Extra info: "));				//	16
		localizedText.add(iwrb.getLocalizedString("user_description", "Description: "));			//	17
		localizedText.add(iwrb.getLocalizedString("user_company_address", "Company's address: "));	//	18
		localizedText.add(iwrb.getLocalizedString("user_date_of_birth", "Date of birth: "));		//	19
		localizedText.add(iwrb.getLocalizedString("user_job", "Job: "));							//	20
		localizedText.add(iwrb.getLocalizedString("user_workplace", "Workplace: "));				//	21
		localizedText.add(iwrb.getLocalizedString("user_group", "Group: "));						//	22
		
		return localizedText;
	}
	
	public boolean addGroupsIds(String instanceId, List<String> ids) {
		if (instanceId == null || ids == null) {
			return false;
		}
		
		try {
			getUniqueIds(true).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean addUsersIds(String instanceId, List<String> ids) {
		if (instanceId == null || ids == null) {
			return false;
		}
		
		try {
			getUniqueIds(false).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getUniqueIds(boolean group) throws NullPointerException {
		IWCacheManager2 cache = IWCacheManager2.getInstance(IWMainApplication.getDefaultIWMainApplication());
		
		int caheSize = 1000;
		boolean overFlowDisk = true;
		boolean eternal = false;
		long cacheTime = 20 * 60;
		String cacheName = "groupsUsersInfoViewersUniqueIdsCache";
		if (group) {
			cacheName = "groupsInfoViewersUniqueIdsCache";
		}
		
		return cache.getCache(cacheName, caheSize, overFlowDisk, eternal, cacheTime, cacheTime);
	}
}
