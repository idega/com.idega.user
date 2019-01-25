package com.idega.user.business;


import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.IBOSession;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.presentation.IWContext;
import com.idega.user.bean.GroupDataBean;
import com.idega.user.bean.GroupMemberDataBean;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.UserPropertiesBean;

public interface GroupService extends IBOSession {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List<GroupNode> getTopGroupsAndDirectChildren(List<String> uniqueIds) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsTree
	 */
	public List<GroupNode> getGroupsTree(String login, String password, String instanceId, List<String> uniqueIds) throws RemoteException;

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
	public List<GroupDataBean> getGroupsInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#reloadProperties
	 */
	public boolean reloadProperties(String instanceId) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUsersInfo
	 */
	public List<GroupMemberDataBean> getUsersInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUserStatusLocalization
	 */
	public List<AdvancedProperty> getUserStatusLocalization() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getBasicGroupPropertiesBean
	 */
	public GroupPropertiesBean getBasicGroupPropertiesBean(String instanceId) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getBasicUserPropertiesBean
	 */
	public UserPropertiesBean getBasicUserPropertiesBean(String instanceId) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#clearGroupInfoCache
	 */
	public boolean clearGroupInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#clearUsersInfoCache
	 */
	public boolean clearUsersInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getLocalizationForGroupInfo
	 */
	public List<String> getLocalizationForGroupInfo() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getLocalizationForGroupUsersInfo
	 */
	public List<String> getLocalizationForGroupUsersInfo() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#addGroupsIds
	 */
	public boolean addGroupsIds(String instanceId, List<String> ids) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#addUsersIds
	 */
	public boolean addUsersIds(String instanceId, List<String> ids) throws RemoteException;

	/** Adds user to group
	 * @param userId
	 * @param groupId
	 * @return true if added user successfully, false if failed
	 */
	public boolean addUser(String userId, String groupId);

	/** Removes user from group
	 * @param userId
	 * @param groupId
	 * @return true if removed user successfully, false if failed
	 */
	public boolean removeUser(String userId, String groupId);

	public List<GroupNode> getChildrenOfGroup(String uniqueId);

	public List<GroupNode> getChildrenOfGroupWithLogin(String login, String password, String uniqueId);

	public boolean streamUniqueIds(String instanceId, List<String> uniqueIds, String cacheName);

	public boolean logInUser(IWContext iwc, String login, String password);

	public boolean isLoggedUser(IWContext iwc, String userName);

	public boolean canMakeCallToServerAndScript(String server, List<String> scripts);

	public boolean addUniqueIds(String cacheName, String instanceId, List<String> ids);

	public Map<String, List<String>> getUniqueIds(String cacheName) throws NullPointerException;

	public boolean isUserLoggedOn(IWContext iwc, String login, String password);

	public String getUserStatusLocalizationByKey(String key);

	public RenderedComponent getRenderedGroup(String uniqueId, String containerId, String groupName);

	public AdvancedProperty getFelixLogin(IWContext iwc);

	public List<AdvancedProperty> getChildGroupsRecursive(Integer groupId);

	public boolean createGroup(String name, String type, Integer parentGroupId);

}