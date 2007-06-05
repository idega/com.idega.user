package com.idega.user.business;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ejb.FinderException;

import org.jdom.Document;

import com.idega.bean.GroupDataBean;
import com.idega.bean.GroupMembersDataBean;
import com.idega.bean.GroupPropertiesBean;
import com.idega.bean.UserPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.accesscontrol.data.LoginTableHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupsManagerBean;
import com.idega.user.data.User;
import com.idega.user.presentation.group.GroupInfoViewerBlock;
import com.idega.user.presentation.group.GroupUsersViewerBlock;
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
	public List<GroupNode> getTopGroupNodes(){
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
		String interfaceScript = new StringBuffer(server).append(UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT).toString();
		
		return (existsFileOnRemoteServer(engineScript) && existsFileOnRemoteServer(interfaceScript));
	}
	
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
		
		return bean.getGroupProperties(instanceId);
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
		
		return bean.getUserProperties(instanceId);
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
	
	public Document getGroupInfoPresentationObject(List<GroupDataBean> groupsData, GroupPropertiesBean bean) {
		if (groupsData == null) {
			return null;
		}
		if (groupsData.size() == 0) {
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
		
		usersViewer.setMembersData(membersData);
		
		usersViewer.setImageHeight(bean.getImageHeight());
		usersViewer.setImageWidth(bean.getImageWidth());
		
		usersViewer.setServer(bean.getServer());
		
		return BuilderLogic.getInstance().getRenderedComponent(iwc, usersViewer, false);
	}
	
	/** Private methods starts **/
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
}
