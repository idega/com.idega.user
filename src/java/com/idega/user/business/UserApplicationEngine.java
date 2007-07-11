package com.idega.user.business;


import java.util.List;

import org.jdom.Document;

import com.idega.business.IBOSession;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;

public interface UserApplicationEngine extends IBOSession {
	
	public List getChildGroups(String groupId, String groupTypes, String groupRoles);
	
	public List removeUsers(List usersIds, Integer groupId);
	
	public Document getMembersList(SimpleUserPropertiesBean bean);
	
	public Document getAddUserPresentationObject(SimpleUserPropertiesBean bean, List parentGroups, List childGroups, Integer userId);
	
	public Document getSimpleUserApplication(String instanceId);
	
	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, Integer userId, String groupTypes, String groupRoles);
	
	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers);
	
	public List getUserByPersonalId(String personalId);
	
	public String createUser(String name, String personalId, String password, Integer primaryGroupId, List childGroups);
	
}