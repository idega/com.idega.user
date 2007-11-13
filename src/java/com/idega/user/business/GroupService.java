package com.idega.user.business;


import java.rmi.RemoteException;
import java.util.List;

import com.idega.business.IBOSession;

public interface GroupService extends IBOSession {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List getTopGroupNodes() throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsTree
	 */
	public List getGroupsTree(String login, String password) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsInfo
	 */
	public List getGroupsInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getUsersInfo
	 */
	public List getUsersInfo(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#clearGroupInfoCache
	 */
	public boolean clearGroupInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#clearUsersInfoCache
	 */
	public boolean clearUsersInfoCache(String login, String password, String instanceId, Integer cacheTime, boolean remoteMode) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#addGroupsIds
	 */
	public boolean addGroupsIds(String instanceId, List ids) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#addUsersIds
	 */
	public boolean addUsersIds(String instanceId, List ids) throws RemoteException;
}