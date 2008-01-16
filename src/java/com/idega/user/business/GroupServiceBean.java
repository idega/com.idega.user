package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOSessionBean;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.cache.IWCacheManager2;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;

public class GroupServiceBean extends IBOSessionBean implements GroupService {
	
	private static final long serialVersionUID = 1649699626972508631L;
	
	private final GroupHelperBusinessBean helper = new GroupHelperBusinessBean();
	
	private LoginTableHome loginHome = null;
	private LoginBusinessBean loginBean = null;
	
	private GroupBusiness groupBusiness = null;
	private UserBusiness userBusiness = null;
	
	private String groupsCacheName = "groupsInfoViewersUniqueIdsCache";
	private String usersCacheName = "groupsUsersInfoViewersUniqueIdsCache";
	private String treeCacheName = "groupsChooserTreeSelectedGroupsUniqueIdsCache";

	/**
	 * Returns tree of Groups
	 */
	public List getTopGroupsAndDirectChildren(List uniqueIds) {
		List topGroupsAndDirectChildren = helper.getTopGroupsAndDirectChildren();
		if (uniqueIds == null) {
			return topGroupsAndDirectChildren;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return topGroupsAndDirectChildren;
		}
		
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return topGroupsAndDirectChildren;
		}
		
		List uniqueIdsOfTopGroups = new ArrayList();
		uniqueIdsOfTopGroups = getCurrentTreeUniqueIds(uniqueIdsOfTopGroups, topGroupsAndDirectChildren);
		
		String image = helper.getGroupImageBaseUri(iwc);
		
		Group selectedGroup = null;
		String uniqueId = null;
		for (int i = 0; i < uniqueIds.size(); i++) {
			uniqueId = null;
			try {
				selectedGroup = groupBusiness.getGroupByUniqueId(uniqueIds.get(i).toString());
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}
			if (selectedGroup != null) {
				uniqueId = selectedGroup.getUniqueId();
				if (uniqueId != null && !uniqueIdsOfTopGroups.contains(uniqueId)) {
					try {
						topGroupsAndDirectChildren = appendParentGroupsToList(groupBusiness.getParentGroups(selectedGroup), selectedGroup, topGroupsAndDirectChildren, groupBusiness, image);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return topGroupsAndDirectChildren;
	}
	
	private List getCurrentTreeUniqueIds(List ids, List nodes) {
		if (nodes == null) {
			return ids;
		}
		
		GroupNode node = null;
		for (int i = 0; i < nodes.size(); i++) {
			node = (GroupNode) nodes.get(i);
			ids.add(node.getUniqueId());
			
			if (node.getChildren() != null) {
				ids = getCurrentTreeUniqueIds(ids, node.getChildren());
			}
		}
		
		return ids;
	}
	
	private List appendParentGroupsToList(Collection parentGroups, Group selectedGroup, List groupNodes, GroupBusiness groupBusiness, String image) {
		if (parentGroups == null) {
			return null;
		}
		
		Object o = null;
		Group parentGroup = null;
		for (Iterator it = parentGroups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				parentGroup = (Group) o;
				GroupNode parentNode = findParentNode(parentGroup, groupNodes);
				if (parentNode != null) {
					try {
						helper.addChildGroupsToNode(parentNode, groupBusiness.getChildGroups(parentGroup), image);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return groupNodes;
	}
	
	private GroupNode findParentNode(Group group, List groupNodes) {
		if (group == null || groupNodes == null) {
			return null;
		}
		
		String uniqueId = group.getUniqueId();
		if (uniqueId == null) {
			return null;
		}
		
		GroupNode groupNode = null;
		for (int i = 0; i < groupNodes.size(); i++) {
			groupNode = (GroupNode) groupNodes.get(i);
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
	
	public List getChildrenOfGroup(String uniqueId) {
		if (uniqueId == null) {
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
		
		Group group = null;
		try {
			group = groupBusiness.getGroupByUniqueId(uniqueId);
		} catch (Exception e) {
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
	
	public List getChildrenOfGroupWithLogin(String login, String password, String uniqueId) {
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
	public List getGroupsTree(String login, String password, String instanceId, List uniqueIds) {
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
			try {
				uniqueIds = (List) getUniqueIds(treeCacheName).get(instanceId);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return getTopGroupsAndDirectChildren(uniqueIds);
	}
	
	private Map getCache(IWContext iwc, String cacheKey, int minutes) {
		IWCacheManager2 cache = IWCacheManager2.getInstance(iwc.getIWMainApplication());
		if (cache == null) {
			return null;
		}
		
		long time = new Long(minutes * 60).longValue();
		return cache.getCache(cacheKey, 1000, true, false, time, time);
	}
	
	private List getGroupInfoFromCache(IWContext iwc, String id, int minutes) {
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
			List extractedData = new ArrayList();
			Object o = null;
			for (int i = 0; i < abstractList.size(); i++) {
				o = abstractList.get(i);
				if (o instanceof GroupDataBean) {
					extractedData.add(o);
				}
				else {
					return null;
				}
			}
			return extractedData;
		}
		
		return null;
	}
	
	private void addGroupInfoToCache(IWContext iwc, String id, int minutes, List info) {
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
	public List getGroupsInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		//	Checking if valid parameters
		if (instanceId == null) {
			return null;
		}
		
		if (remoteMode && (login == null || password == null)) {
			return null;
		}
		
		List uniqueIds = null;
		try {
			uniqueIds = (List) getUniqueIds(groupsCacheName).get(instanceId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (uniqueIds == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (remoteMode && !canUseServer(iwc, login, password)) {
			return null;
		}

		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List cachedInfo = getGroupInfoFromCache(iwc, instanceId, cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List info = getGroupBusiness(iwc).getGroupsData(uniqueIds);
		if (useCache && info != null) {
			addGroupInfoToCache(iwc, instanceId, cacheTime.intValue(), info);
		}
		return info;
	}
	
	private List getUsersInfoFromCache(IWContext iwc, String id, int minutes) {
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
			List extractedData = new ArrayList();
			Object o = null;
			for (int i = 0; i < abstractList.size(); i++) {
				o = abstractList.get(i);
				if (o instanceof GroupMemberDataBean) {
					extractedData.add(o);
				}
				else {
					return null;
				}
			}
			return extractedData;
		}
		
		return null;
	}

	private void addUsersInfoToCache(IWContext iwc, String id, int minutes, List info) {
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
	
	public List getUsersInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		//	Checking if valid parameters
		if (instanceId == null) {
			return null;
		}
		
		if (remoteMode && (login == null || password == null)) {
			return null;
		}
		
		List uniqueIds = null;
		try {
			uniqueIds = (List) getUniqueIds(usersCacheName).get(instanceId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (uniqueIds == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (remoteMode && !canUseServer(iwc, login, password)) {
			return null;
		}
		
		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List cachedInfo = getUsersInfoFromCache(iwc, instanceId, cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List info = getUserBusiness(iwc).getGroupsMembersData(uniqueIds);
		if (useCache && info != null) {
			addUsersInfoToCache(iwc, instanceId, cacheTime.intValue(), info);
		}
		return info;
	}
	
	/**
	 * Logs in user
	 * @param iwc
	 * @param login
	 * @param password
	 * @return
	 */
	public boolean logInUser(IWContext iwc, String login, String password) {
		if (iwc == null || login == null || password == null) {
			return false;
		}

		return getLoginBean(iwc).logInUser(iwc.getRequest(), login, password);
	}
	
	/**
	 * Checks if current (logged) user is the same user that is making request
	 * @param iwc
	 * @param userName
	 * @return
	 */
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
			userId = Integer.valueOf(current.getId()).intValue();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		
		//	Checking if current user is making request
		LoginTable login = null;
		try {
			login = loginHome.findLoginForUser(userId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (userName.equals(login.getUserLogin())) {
			return true;
		}
		
		return false;
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
	
	private boolean canUseServer(IWContext iwc, String login, String password) {
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
	
	private boolean clearCache(String cacheKey, String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		if (cacheKey == null || instanceId == null) {
			return false;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}
		
		if (remoteMode && !canUseServer(iwc, login, password)) {
			return false;
		}
		
		int minutes = 0;
		if (cacheTime != null) {
			minutes = cacheTime.intValue();
		}
		
		Map cache = getCache(iwc, cacheKey, minutes);
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
	
	public boolean clearGroupInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		return clearCache(UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, login, password, instanceId, cacheTime, remoteMode);
	}
	
	public boolean clearUsersInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) {
		return clearCache(UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, login, password, instanceId, cacheTime, remoteMode);
	}
	
	public boolean addGroupsIds(String instanceId, List ids) {
		if (instanceId == null || ids == null) {
			return false;
		}
		
		try {
			getUniqueIds(groupsCacheName).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean addUsersIds(String instanceId, List ids) {
		if (instanceId == null || ids == null) {
			return false;
		}
		
		try {
			getUniqueIds(usersCacheName).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public Map getUniqueIds(String cacheName) throws NullPointerException {
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
	
	public boolean streamUniqueIds(String instanceId, List uniqueIds, boolean isGroupIds, boolean isTree) {
		if (instanceId == null || uniqueIds == null) {
			return false;
		}
		
		String cacheName = usersCacheName;
		if (isGroupIds) {
			cacheName = groupsCacheName;
		}
		if (isTree) {
			cacheName = treeCacheName;
		}
		
		List ids = null;
		try {
			ids = (List) getUniqueIds(cacheName).get(instanceId);
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
		
		if (isGroupIds) {
			return addGroupsIds(instanceId, ids);
		}
		
		if (isTree) {
			try {
				getUniqueIds(treeCacheName).put(instanceId, ids);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		return addUsersIds(instanceId, ids);
	}
	
	public boolean addUniqueIds(String cacheName, String instanceId, List ids) {
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

}
