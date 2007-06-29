package com.idega.user.business;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.IBOService;
import java.util.List;
import org.jdom.Document;

import java.rmi.RemoteException;

import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.bean.GroupMembersDataBean;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.UserPropertiesBean;

public interface GroupService extends IBOService {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List<GroupNode> getTopGroupNodes() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsTree
	 */
	public List<GroupNode> getGroupsTree(String login, String password) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#canUseRemoteServer
	 */
	public boolean canUseRemoteServer(String server) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupPropertiesBean
	 */
	public GroupPropertiesBean getGroupPropertiesBean(String instanceId) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUserPropertiesBean
	 */
	public UserPropertiesBean getUserPropertiesBean(String instanceId) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsInfo
	 */
	public List<GroupDataBean> getGroupsInfo(GroupPropertiesBean bean) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupInfoPresentationObject
	 */
	public Document getGroupInfoPresentationObject(List<GroupDataBean> groupsData, GroupPropertiesBean bean) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUsersInfo
	 */
	public List<GroupMemberDataBean> getUsersInfo(UserPropertiesBean bean) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsMembersPresentationObject
	 */
	public Document getGroupsMembersPresentationObject(List<GroupMembersDataBean> membersData, UserPropertiesBean bean) throws RemoteException;
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#reloadProperties
	 */
	public boolean reloadProperties(String instanceId);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getUserStatusLocalization
	 */
	public List<AdvancedProperty> getUserStatusLocalization();
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getBasicUserPropertiesBean
	 */
	public UserPropertiesBean getBasicUserPropertiesBean(String instanceId);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getBasicGroupPropertiesBean
	 */
	public GroupPropertiesBean getBasicGroupPropertiesBean(String instanceId);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#clearGroupInfoCache
	 */
	public boolean clearGroupInfoCache(GroupPropertiesBean bean);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#clearUsersInfoCache
	 */
	public boolean clearUsersInfoCache(UserPropertiesBean bean);
}