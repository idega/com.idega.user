package com.idega.user.business;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ejb.FinderException;

import com.idega.business.IBOServiceBean;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.GroupsManagerBean;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.webface.WFUtil;

public class GroupServiceBean extends IBOServiceBean implements GroupService {
	
	private static final long serialVersionUID = 1649699626972508631L;
	
	private GroupHelperBusinessBean helper = new GroupHelperBusinessBean();
	private LoginTableHome loginHome = null;

	public List<GroupNode> getTopGroupNodes(){
		return helper.getTopGroupNodes();
	}
	
	public List<GroupNode> getRemoteGroups(String login, String password) {
		if (login == null || password == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		if (isLoggedUser(iwc, login)) {
			System.out.println("User " + login + " is logged.");
			return getTopGroupNodes();
		}
		
		LoginBusinessBean loginBean = null;
		try {
			loginBean = LoginBusinessBean.getLoginBusinessBean(iwc.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Trying to log in: " + login + ", " + password);
		boolean logged = loginBean.logInUser(iwc.getRequest(), login, password);
		System.out.println("Logged: " + logged);
		
		if (logged) {
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
		String interfaceScript = new StringBuffer(server).append(UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT).toString();
		
		return (existsFileOnRemoteServer(engineScript) && existsFileOnRemoteServer(interfaceScript));
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
	 * Will check if current (logged) user is the same user that is making request
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
		LoginTable t = null;
		try {
			t = loginHome.findLoginForUser(userId);
		} catch (FinderException e) {
			e.printStackTrace();
			return false;
		}
		if (userName.equals(t.getUserLogin())) {
			return true;
		}
		
		return false;
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
	
	public GroupPropertiesBean getPropertiesBean(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		Object o = WFUtil.getBeanInstance(UserConstants.GROUPS_MANAGER_BEAN_ID);
		if (!(o instanceof GroupsManagerBean)) {
			return null;
		}
		GroupsManagerBean bean = (GroupsManagerBean) o;
		
		return bean.getProperties(instanceId);
	}

}
