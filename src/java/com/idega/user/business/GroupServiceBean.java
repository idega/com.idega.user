package com.idega.user.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	/**
	 * Returns tree of Groups
	 */
	public List getTopGroupNodes() {
		return helper.getTopGroupNodes();
	}
	
	/**
	 * Returns tree of Groups
	 */
	public List getGroupsTree(String login, String password) {
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
			uniqueIds = (List) getUniqueIds(true).get(instanceId);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
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
			uniqueIds = (List) getUniqueIds(false).get(instanceId);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
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
			getUniqueIds(true).put(instanceId, ids);
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
			getUniqueIds(false).put(instanceId, ids);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private Map getUniqueIds(boolean group) throws NullPointerException {
		IWCacheManager2 cache = IWCacheManager2.getInstance(IWMainApplication.getDefaultIWMainApplication());
		
		int caheSize = 1000;
		boolean overFlowDisk = true;
		boolean eternal = false;
		long cacheTime = 5 * 60;	// Seconds
		String cacheName = "groupsUsersInfoViewersUniqueIdsCache";
		if (group) {
			cacheName = "groupsInfoViewersUniqueIdsCache";
		}
		
		return cache.getCache(cacheName, caheSize, overFlowDisk, eternal, cacheTime, cacheTime);
	}
}
