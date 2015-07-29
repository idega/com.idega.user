package com.idega.user.business;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.faces.component.UIComponent;

import org.hsqldb.lib.StringUtil;

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
import com.idega.core.component.bean.RenderedComponent;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.GroupsManagerBean;
import com.idega.user.bean.PropertiesBean;
import com.idega.user.bean.UserPropertiesBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.presentation.group.GroupInfoViewer;
import com.idega.user.presentation.group.GroupUsersViewer;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class GroupServiceBean extends IBOSessionBean implements GroupService {

	private static final long serialVersionUID = 1649699626972508631L;

	private GroupHelper helper = null;

	private LoginTableHome loginHome = null;
	private LoginBusinessBean loginBean = null;

	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;

	private final String groupsCacheName = "groupsInfoViewersUniqueIdsCache";
	private final String usersCacheName = "groupsUsersInfoViewersUniqueIdsCache";
	private final String treeCacheName = "groupsChooserTreeSelectedGroupsUniqueIdsCache";

	private GroupHelper getGroupHelper(IWContext iwc) {
		if (helper == null) {
			helper = ELUtil.getInstance().getBean(GroupHelper.class);
		}
		return helper;
	}

	/**
	 * Returns tree of Groups
	 */
	@Override
	public List<GroupNode> getTopGroupsAndDirectChildren(List<String> uniqueIds) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return new ArrayList<GroupNode>();
		}

		GroupHelper helper = getGroupHelper(iwc);
		if (helper == null) {
			return new ArrayList<GroupNode>();
		}

		List<GroupNode> topGroupsAndDirectChildren = helper.getTopGroupsAndDirectChildren();
		if (uniqueIds == null) {
			return topGroupsAndDirectChildren;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return topGroupsAndDirectChildren;
		}

		List<String> uniqueIdsOfTopGroups = new ArrayList<String>();
		uniqueIdsOfTopGroups = getCurrentTreeUniqueIds(uniqueIdsOfTopGroups, topGroupsAndDirectChildren);

		String image = helper.getGroupImageBaseUri(iwc);

		Group selectedGroup = null;
		String uniqueId = null;
		for (int i = 0; i < uniqueIds.size(); i++) {
			uniqueId = null;
			try {
				selectedGroup = groupBusiness.getGroupByUniqueId(uniqueIds.get(i));
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}
			if (selectedGroup != null) {
				uniqueId = selectedGroup.getUniqueId();
				if (uniqueId != null && !uniqueIdsOfTopGroups.contains(uniqueId)) {
					try {
						topGroupsAndDirectChildren = appendParentGroupsToList(groupBusiness.getParentGroups(selectedGroup), selectedGroup,
								topGroupsAndDirectChildren, groupBusiness, image, iwc);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return topGroupsAndDirectChildren;
	}

	private List<String> getCurrentTreeUniqueIds(List<String> ids, List<GroupNode> nodes) {
		if (nodes == null) {
			return ids;
		}

		GroupNode node = null;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			ids.add(node.getUniqueId());

			if (node.getChildren() != null) {
				ids = getCurrentTreeUniqueIds(ids, node.getChildren());
			}
		}

		return ids;
	}

	private List<GroupNode> appendParentGroupsToList(Collection<Group> parentGroups, Group selectedGroup, List<GroupNode> groupNodes, GroupBusiness groupBusiness,
														String image, IWContext iwc) {
		if (parentGroups == null) {
			return null;
		}

		GroupHelper helper = getGroupHelper(iwc);
		if (helper == null) {
			return null;
		}

		for (Group parentGroup: parentGroups) {
			GroupNode parentNode = findParentNode(parentGroup, groupNodes);
			if (parentNode != null) {
				try {
					helper.addChildGroupsToNode(parentNode, groupBusiness.getChildGroups(parentGroup), image);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		return groupNodes;
	}

	private GroupNode findParentNode(Group group, List<GroupNode> groupNodes) {
		if (group == null || groupNodes == null) {
			return null;
		}

		String uniqueId = group.getUniqueId();
		if (uniqueId == null) {
			return null;
		}

		GroupNode groupNode = null;
		for (int i = 0; i < groupNodes.size(); i++) {
			groupNode = groupNodes.get(i);
			if (uniqueId.equals(groupNode.getUniqueId())) {
				return groupNode;
			}
			else {
				if (groupNode.getChildren() != null) {
					GroupNode groupNodeRecursive = findParentNode(group, groupNode.getChildren());
					if (groupNodeRecursive != null) {
						return groupNodeRecursive;
					}
				}
			}

		}

		return null;
	}

	@Override
	public List<GroupNode> getChildrenOfGroup(String uniqueId) {
		if (uniqueId == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		GroupHelper helper = getGroupHelper(iwc);
		if (helper == null) {
			return null;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}

		Group group = null;
		try {
			group = groupBusiness.getGroupByUniqueId(uniqueId);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}

		if (group == null) {
			return null;
		}

		try {
			return helper.convertGroupsToGroupNodes(groupBusiness.getChildGroups(group), iwc, false, helper.getGroupImageBaseUri(iwc));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<GroupNode> getChildrenOfGroupWithLogin(String login, String password, String uniqueId) {
		if (login == null || password == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		if (!isLoggedUser(iwc, login)) {
			if (!logInUser(iwc, login, password)) {
				return null;
			}
		}

		return getChildrenOfGroup(uniqueId);
	}

	/**
	 * Returns tree of Groups
	 */
	@Override
	public List<GroupNode> getGroupsTree(String login, String password, String instanceId, List<String> uniqueIds) {
		if (login == null || password == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		if (uniqueIds == null && instanceId == null) {
			return null;
		}

		if (!isLoggedUser(iwc, login)) {
			if (!logInUser(iwc, login, password)) {
				return null;
			}
		}

		if (uniqueIds == null) {
			uniqueIds = null;
			try {
				uniqueIds = getUniqueIds(treeCacheName).get(instanceId);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return getTopGroupsAndDirectChildren(uniqueIds);
	}

	/**
	 * Checks if can use DWR on remote server
	 */
	@Override
	public boolean canUseRemoteServer(String server) {
		List<String> scripts = new ArrayList<String>();
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);

		return canMakeCallToServerAndScript(server, scripts);
	}

	@Override
	public boolean canMakeCallToServerAndScript(String server, List<String> scripts) {
		if (server == null) {
			return false;
		}

		if (server.endsWith("/")) {
			server = server.substring(0, server.lastIndexOf("/"));
		}

		if (scripts == null) {
			return true;
		}

		for (int i = 0; i < scripts.size(); i++) {
			if (!existsFileOnRemoteServer(new StringBuffer(server).append(scripts.get(i)).toString())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns user's parameters for getting info about Groups
	 */
	@Override
	public GroupPropertiesBean getGroupPropertiesBean(String instanceId) {
		return getBasicGroupPropertiesBean(instanceId);
	}

	/**
	 * Returns user's parameters for getting info about Groups members
	 * @param instanceId
	 * @return
	 */
	@Override
	public UserPropertiesBean getUserPropertiesBean(String instanceId) {
		return getBasicUserPropertiesBean(instanceId);
	}

	private <T> Map<String, List<T>> getCache(IWContext iwc, String cacheKey, int minutes) {
		IWCacheManager2 cache = IWCacheManager2.getInstance(iwc.getIWMainApplication());
		if (cache == null) {
			return null;
		}

		long time = new Long(minutes * 60).longValue();
		return cache.getCache(cacheKey, 1000, true, false, time, time);
	}

	private List<GroupDataBean> getGroupInfoFromCache(IWContext iwc, String id, int minutes) {
		Map<String, List<GroupDataBean>> cache = getCache(iwc, UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return null;
		}

		List<GroupDataBean> cachedList = null;
		try {
			cachedList = cache.get(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(cachedList)) {
			return null;
		}

		return cachedList;
	}

	private void addGroupInfoToCache(IWContext iwc, String id, int minutes, List<GroupDataBean> info) {
		Map<String, List<GroupDataBean>> cache = getCache(iwc, UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, minutes);
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
	@Override
	public List<GroupDataBean> getGroupsInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		//	Checking if valid parameters
		if (instanceId == null) {
			return null;
		}

		if (remoteMode && (login == null || password == null)) {
			return null;
		}

		List<String> uniqueIds = getUniqueIds(groupsCacheName).get(instanceId);
		if (uniqueIds == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		if (remoteMode && !isUserLoggedOn(iwc, login, password)) {
			return null;
		}

		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List<GroupDataBean> cachedInfo = getGroupInfoFromCache(iwc, instanceId, cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}

		List<GroupDataBean> info = getGroupBusiness(iwc).getGroupsData(uniqueIds);
		if (useCache && info != null) {
			addGroupInfoToCache(iwc, instanceId, cacheTime.intValue(), info);
		}
		return info;
	}

	@Override
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
		PropertiesBean properties = null;
		UIComponent groups = builder.findComponentInPage(iwc, pageKey, instanceId);
		if (groups == null) {
			String name = new StringBuffer(":method:1:implied:void:setGroups:").append(PropertiesBean.class.getName()).append(":").toString();
			String[] values = builder.getPropertyValues(iwc.getIWMainApplication(), pageKey, instanceId, name, null, true);
			if (values == null) {
				return false;
			}
			if (values.length == 0) {
				return false;
			}
			GroupsChooserHelper helper = new GroupsChooserHelper();
			properties = helper.getExtractedPropertiesFromString(values[0]);
		}
		else {
			builder.getRenderedComponent(groups, iwc, false);

			if (groups instanceof GroupInfoViewer) {
				properties = getGroupPropertiesBean(instanceId);
			}
			else {
				properties = getUserPropertiesBean(instanceId);
			}
		}

		if (properties == null) {
			return false;
		}
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = properties;

		Class<?>[] classes = new Class[2];
		classes[0] = String.class;
		classes[1] = PropertiesBean.class;

		//	Setting parameters to bean, these parameters will be taken by DWR and sent to selected server to get required info
		WFUtil.invoke(UserConstants.GROUPS_MANAGER_BEAN_ID, "addAbstractProperties", parameters, classes);
		return true;
	}

	private List<GroupMemberDataBean> getUsersInfoFromCache(IWContext iwc, String id, int minutes) {
		Map<String, List<GroupMemberDataBean>> cache = getCache(iwc, UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return null;
		}

		List<GroupMemberDataBean> cachedList = null;
		try {
			cachedList = cache.get(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(cachedList)) {
			return null;
		}

		return cachedList;
	}

	private void addUsersInfoToCache(IWContext iwc, String id, int minutes, List<GroupMemberDataBean> info) {
		Map<String, List<GroupMemberDataBean>> cache = getCache(iwc, UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, minutes);
		if (cache == null) {
			return;
		}

		try {
			cache.put(id, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<GroupMemberDataBean> getUsersInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		//	Checking if valid parameters
		if (instanceId == null) {
			return null;
		}

		if (remoteMode && (login == null || password == null)) {
			return null;
		}

		List<String> uniqueIds = getUniqueIds(usersCacheName).get(instanceId);
		if (uniqueIds == null) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		if (remoteMode && !isUserLoggedOn(iwc, login, password)) {
			return null;
		}

		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List<GroupMemberDataBean> cachedInfo = getUsersInfoFromCache(iwc, instanceId, cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}

		List<GroupMemberDataBean> info = getUserBusiness(iwc).getGroupsMembersData(uniqueIds);
		if (useCache && info != null) {
			addUsersInfoToCache(iwc, instanceId, cacheTime.intValue(), info);
		}
		return info;
	}

	@Override
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
		addStatusLocalization(statusLocalization, "SPORTS_REPRESENTATIVE", iwrb.getLocalizedString("SPORTS_REPRESENTATIVE", "Sports Representative"));
		addStatusLocalization(statusLocalization, "STAT_OFFICE_MANAGER", iwrb.getLocalizedString("STAT_OFFICE_MANAGER", "Office Manager"));
		addStatusLocalization(statusLocalization, "STAT_PROJECT_MANGAGER", iwrb.getLocalizedString("STAT_PROJECT_MANGAGER", "Project Manager"));

		return statusLocalization;
	}

	@Override
	public String getUserStatusLocalizationByKey(String key) {
		if (key == null) {
			return CoreConstants.EMPTY;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return CoreConstants.EMPTY;
		}

		IWResourceBundle iwrb = null;
		try {
			iwrb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		} catch (Exception e) {
			e.printStackTrace();
			return CoreConstants.EMPTY;
		}

		return iwrb.getLocalizedString(key, key);
	}

	@Override
	protected String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}

	/**
	 * Logs in user
	 * @param iwc
	 * @param login
	 * @param password
	 * @return
	 */
	@Override
	public boolean logInUser(IWContext iwc, String login, String password) {
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

		boolean opening = false;
		boolean gotAnyResponse = false;
		IWTimestamp timeToWaitForConnection = new IWTimestamp(System.currentTimeMillis() + 10000);	//	10 seconds to open connection
		IWTimestamp now = IWTimestamp.RightNow();
		while(!gotAnyResponse && timeToWaitForConnection.isLaterThan(now)) {
			if (opening) {
				//	Do nothing
			}
			else {
				opening = true;
				try {
					URL dwr = new URL(urlToFile);
					streamToFile = dwr.openStream();
					gotAnyResponse = true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			now = IWTimestamp.RightNow();
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
	@Override
	public boolean isLoggedUser(IWContext iwc, String userName) {
		if (iwc == null || userName == null) {
			return false;
		}
		//	Getting current user
		User current = null;
		try {
			current = iwc.getCurrentUser();
		} catch (Exception e) {
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

	private GroupBusiness getGroupBusiness(IWContext iwc) {
		if (groupBusiness == null) {
			try {
				groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}

	private LoginBusinessBean getLoginBean(IWContext iwc) {
		if (loginBean == null) {
			try {
				loginBean = LoginBusinessBean.getLoginBusinessBean(iwc.getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return loginBean;
	}

	private LoginTableHome getLoginHome() {
		if (loginHome == null) {
			try {
				loginHome = (LoginTableHome) IDOLookup.getHome(LoginTable.class);
			} catch (IDOLookupException e) {
				e.printStackTrace();
			}
		}
		return loginHome;
	}

	private UserBusiness getUserBusiness(IWContext iwc) {
		if (userBusiness == null) {
			try {
				userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}

	private void addStatusLocalization(List<AdvancedProperty> list, String key, String value) {
		AdvancedProperty localizedStatus = new AdvancedProperty();
		localizedStatus.setId(key);
		localizedStatus.setValue(value);
		list.add(localizedStatus);
	}

	@Override
	public boolean isUserLoggedOn(IWContext iwc, String login, String password) {
		if (iwc == null || login == null || password == null) {
			return false;
		}

		//	Checking if user is allowed to use server
		if (!isLoggedUser(iwc, login)) {
			if (!logInUser(iwc, login, password)) {
				return false;
			}
			else {
				return true;
			}
		}

		return true;
	}

	@Override
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

	@Override
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

	private boolean clearCache(String cacheKey, String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		if (cacheKey == null || instanceId == null) {
			return false;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}

		if (remoteMode && !isUserLoggedOn(iwc, login, password)) {
			return false;
		}

		int minutes = 0;
		if (cacheTime != null) {
			minutes = cacheTime.intValue();
		}

		Map<String, ?> cache = getCache(iwc, cacheKey, minutes);
		if (cache == null) {
			return false;
		}

		try {
			cache.remove(instanceId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean clearGroupInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		return clearCache(UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, login, password, instanceId, cacheTime, remoteMode);
	}

	@Override
	public boolean clearUsersInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		return clearCache(UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, login, password, instanceId, cacheTime, remoteMode);
	}

	@Override
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

	@Override
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

	@Override
	public boolean addUniqueIds(String cacheName, String instanceId, List<String> ids) {
		if (cacheName == null || instanceId == null || ids == null) {
			return false;
		}

		try {
			getUniqueIds(cacheName).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean addGroupsIds(String instanceId, List<String> ids) {
		return addUniqueIds(groupsCacheName, instanceId, ids);
	}

	@Override
	public boolean addUsersIds(String instanceId, List<String> ids) {
		return addUniqueIds(usersCacheName, instanceId, ids);
	}

	@Override
	public Map<String, List<String>> getUniqueIds(String cacheName) throws NullPointerException {
		IWCacheManager2 cache = IWCacheManager2.getInstance(IWMainApplication.getDefaultIWMainApplication());

		int caheSize = 1000;
		boolean overFlowDisk = true;
		boolean eternal = false;
		long cacheTime = 5 * 60;	// Seconds
		try {
			return cache.getCache(cacheName, caheSize, overFlowDisk, eternal, cacheTime, cacheTime);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean streamUniqueIds(String instanceId, List<String> uniqueIds, String cacheName) {
		if (instanceId == null || uniqueIds == null || cacheName == null) {
			return false;
		}

		List<String> ids = null;
		try {
			ids = getUniqueIds(cacheName).get(instanceId);
		} catch(Exception e) {
			e.printStackTrace();
		}

		if (ids == null) {
			ids = uniqueIds;
		}
		else {
			for (int i = 0; i < uniqueIds.size(); i++) {
				ids.add(uniqueIds.get(i));
			}
		}
		try {
			getUniqueIds(cacheName).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

    @Override
    public boolean addUser(String userId, String groupId) {

    	if(StringUtil.isEmpty(userId) || StringUtil.isEmpty(userId)){
    		return false;
    	}


        //IBOLookup pad4s ideti EJB bean
        //jei neprisijunges, nukreipti i pages langa.

    	GroupBusiness groupBusiness = getGroupBusiness(CoreUtil.getIWContext());
    	try{
    		User userToAdd = groupBusiness.getUserByID(Integer.valueOf(userId));
    		groupBusiness.addUser(Integer.valueOf(groupId), userToAdd);
    	}catch(Exception e){
    		this.getLogger().log(Level.WARNING, "Failed adding user" +userId+ " to group" + groupId, e);
    		return false;
    	}
        return true;
    }

    @Override
    public boolean removeUser(String userId, String groupId) {
    	if(StringUtil.isEmpty(userId) || StringUtil.isEmpty(userId)){
    		return false;
    	}
    	IWContext iwc = CoreUtil.getIWContext();
    	if(!iwc.isLoggedOn()){
    		return false;
    	}
    	GroupBusiness groupBusiness = getGroupBusiness(CoreUtil.getIWContext());
    	int groupIdInt = Integer.valueOf(groupId);
    	try{

    		Group group = groupBusiness.getGroupByGroupID(groupIdInt);
    		User user = groupBusiness.getUserByID(Integer.valueOf(userId));
    		group.removeUser(user, iwc.getCurrentUser());
    	}catch(Exception e){
    		this.getLogger().log(Level.WARNING, "Failed adding user" +userId+ " to group" + groupId, e);
    		return false;
    	}
        return true;
    }

    @Override
	public AdvancedProperty getFelixLogin(IWContext iwc) {
    	IWMainApplicationSettings settings = iwc.getApplicationSettings();
    	AdvancedProperty login = new AdvancedProperty(settings.getProperty("remote_felix_login", "martha"), settings.getProperty("remote_felix_pswd", "060455"));

//    	try {
//	    	if (!iwc.isLoggedOn())
//	    		return login;
//
//	    	LoginTable loginTable = LoginDBHandler.getUserLogin(iwc.getCurrentUser());
//	    	if (loginTable == null)
//	    		return login;
//
//	    	if ("laddi".equals(loginTable.getUserLogin())) {
//	    		login.setId("laddi");
//	    		login.setValue("laddi");
//	    	}
//    	} catch (Exception e) {
//    		getLogger().log(Level.WARNING, "Error getting login", e);
//    	}

    	return login;
    }

	@Override
	public RenderedComponent getRenderedGroup(String uniqueId, String containerId, String groupName) {
		if (StringUtil.isEmpty(uniqueId) || StringUtil.isEmpty(containerId) || StringUtil.isEmpty(groupName))
			return null;

		IWContext iwc = CoreUtil.getIWContext();
		IWBundle bundle = iwc.getIWMainApplication().getBundle(CoreConstants.IW_USER_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);

		Layer container = new Layer();
		container.add(new Heading3(iwrb.getLocalizedString("members_of_group", "Members of a group") + " " + groupName + ":"));
		GroupUsersViewer users = new GroupUsersViewer();
		users.setUniqueIds(Arrays.asList(uniqueId));
		users.setRemoteMode(true);

		String remoteServer = iwc.getApplicationSettings().getProperty("remote_felix_server", "http://felix.is");

		//	TODO
		users.setServer(remoteServer);
		AdvancedProperty login = getFelixLogin(iwc);
		users.setUser(login.getId());
		users.setPassword(login.getValue());

		users.setAddReflection(true);
		users.setAddJavaScriptForGroupsTree(false);
		users.setCallback("UserGroups.scrollToUsers('" + containerId + "', '" + iwrb.getLocalizedString("there_are_no_users", "There are no users in this group") + "');");
		container.add(users);

		return BuilderLogic.getInstance().getRenderedComponent(container, null);
	}
}