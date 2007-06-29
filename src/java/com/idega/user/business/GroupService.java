package com.idega.user.business;


import com.idega.business.IBOService;
import java.util.List;
import java.rmi.RemoteException;

import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.bean.UserPropertiesBean;

public interface GroupService extends IBOService {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List getTopGroupNodes() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsTree
	 */
	public List getGroupsTree(String login, String password) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#canUseRemoteServer
	 */
	public boolean canUseRemoteServer(String server) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsInfo
	 */
	public List getGroupsInfo(GroupPropertiesBean bean) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUsersInfo
	 */
	public List getUsersInfo(UserPropertiesBean bean) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#clearGroupInfoCache
	 */
	public boolean clearGroupInfoCache(GroupPropertiesBean bean);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#clearUsersInfoCache
	 */
	public boolean clearUsersInfoCache(UserPropertiesBean bean);
}