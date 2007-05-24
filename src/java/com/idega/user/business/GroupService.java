package com.idega.user.business;

import java.util.List;

import com.idega.business.IBOService;
import com.idega.user.bean.GroupPropertiesBean;

public interface GroupService extends IBOService {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List<GroupNode> getTopGroupNodes();
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getRemoteGroups
	 */
	public List<GroupNode> getRemoteGroups(String login, String password);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#canUseRemoteServer
	 */
	public boolean canUseRemoteServer(String server);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getPropertiesBean
	 */
	public GroupPropertiesBean getPropertiesBean(String instanceId);
}