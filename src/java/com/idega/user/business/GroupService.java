package com.idega.user.business;

import java.util.List;

import org.jdom.Document;

import com.idega.bean.GroupDataBean;
import com.idega.bean.GroupMembersDataBean;
import com.idega.bean.GroupPropertiesBean;
import com.idega.bean.UserPropertiesBean;
import com.idega.business.IBOService;

public interface GroupService extends IBOService {
	/**
	 * @see com.idega.user.business.GroupServiceBean#getTopGroupNodes
	 */
	public List<GroupNode> getTopGroupNodes();
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getRemoteGroups
	 */
	public List<GroupNode> getGroupsTree(String login, String password);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#canUseRemoteServer
	 */
	public boolean canUseRemoteServer(String server);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupPropertiesBean
	 */
	public GroupPropertiesBean getGroupPropertiesBean(String instanceId);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsInfo
	 */
	public List<GroupDataBean> getGroupsInfo(GroupPropertiesBean bean);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getUsersInfo
	 */
	public List<GroupMembersDataBean> getUsersInfo(UserPropertiesBean bean);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupInfoPresentationObject
	 */
	public Document getGroupInfoPresentationObject(List<GroupDataBean> groupsData, GroupPropertiesBean bean);
	
	/**
	 * @see com.idega.user.business.GroupServiceBean#getGroupsMembersPresentationObject
	 */
	public Document getGroupsMembersPresentationObject(List<GroupMembersDataBean> membersData, UserPropertiesBean bean);
}