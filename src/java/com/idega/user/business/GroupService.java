package com.idega.user.business;


import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.idega.business.IBOSession;
import com.idega.presentation.IWContext;

public interface GroupService extends IBOSession {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List getTopGroupsAndDirectChildren(List uniqueIds) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsTree
	 */
	public List getGroupsTree(String login, String password, String instanceId, List uniqueIds) throws RemoteException;

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
	public Boolean addGroupsIds(String instanceId, List ids) throws RemoteException;

	/**
	 * @see com.idega.user.business.GroupServiceBean#addUsersIds
	 */
	public Boolean addUsersIds(String instanceId, List ids) throws RemoteException;
	
	public List getChildrenOfGroup(String uniqueId);
	
	public List getChildrenOfGroupWithLogin(String login, String password, String uniqueId);
	
	public Boolean streamUniqueIds(String instanceId, List uniqueIds, String cacheName);
	
	public boolean logInUser(IWContext iwc, String login, String password);
	
	public boolean isLoggedUser(IWContext iwc, String userName);
	
	public Boolean addUniqueIds(String cacheName, String instanceId, List ids);
	
	public boolean isUserLoggedOn(IWContext iwc, String login, String password);
	
	public Map getUniqueIds(String cacheName) throws NullPointerException;
}