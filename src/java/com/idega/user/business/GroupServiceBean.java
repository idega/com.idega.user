package com.idega.user.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.core.cache.IWCacheManager2;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.PropertiesBean;
import com.idega.user.bean.UserPropertiesBean;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;

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
	public List getGroupsInfo(GroupPropertiesBean bean) {
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
		
		if (!canUseServer(iwc, bean)) {
			return null;
		}
		
		Integer cacheTime = bean.getCacheTime();
		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List cachedInfo = getGroupInfoFromCache(iwc, bean.getInstanceId(), cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List info = getGroupBusiness(iwc).getGroupsData(bean);
		if (useCache && info != null) {
			addGroupInfoToCache(iwc, bean.getInstanceId(), cacheTime.intValue(), info);
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
	
	public List getUsersInfo(UserPropertiesBean bean) {
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
		
		if (!canUseServer(iwc, bean)) {
			return null;
		}
		
		Integer cacheTime = bean.getCacheTime();
		boolean useCache = cacheTime == null ? false : true;
		if (useCache) {
			List cachedInfo = getUsersInfoFromCache(iwc, bean.getInstanceId(), cacheTime.intValue());
			if (cachedInfo != null) {
				return cachedInfo;
			}
		}
		
		List info = getUserBusiness(iwc).getGroupsMembersData(bean);
		if (useCache && info != null) {
			addUsersInfoToCache(iwc, bean.getInstanceId(), cacheTime.intValue(), info);
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
	
	private Boolean clearCache(String cacheKey, PropertiesBean bean) {
		if (cacheKey == null || bean == null) {
			return Boolean.FALSE;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return Boolean.FALSE;
		}
		
		if (!canUseServer(iwc, bean)) {
			return Boolean.FALSE;
		}
		
		int minutes = 0;
		Integer cacheTime = bean.getCacheTime();
		if (cacheTime != null) {
			minutes = cacheTime.intValue();
		}
		
		Map cache = getCache(iwc, cacheKey, minutes);
		if (cache == null) {
			return Boolean.FALSE;
		}
		
		try {
			cache.remove(bean.getInstanceId());
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	public Boolean clearGroupInfoCache(GroupPropertiesBean bean) {
		return clearCache(UserConstants.GROUP_INFO_VIEWER_DATA_CACHE_KEY, bean);
	}
	
	public Boolean clearUsersInfoCache(UserPropertiesBean bean) {
		return clearCache(UserConstants.GROUP_USERS_VIEWER_DATA_CACHE_KEY, bean);
	}
}
