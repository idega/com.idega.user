package com.idega.user.business;

import java.util.Collection;
import java.util.List;

import com.idega.business.IBOService;
import java.rmi.RemoteException;

public interface GroupService extends IBOService {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public Collection getTopGroupNodes() throws RemoteException;
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getRemoteGroups
	 */
	public List<GroupNode> getRemoteGroups(String login, String password);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#canUseRemoteServer
	 */
	public boolean canUseRemoteServer(String server);
}