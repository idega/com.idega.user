package com.idega.user.business;


import java.util.List;

import org.jdom.Document;

import com.idega.business.IBOSession;
import com.idega.user.app.SimpleUserAppViewUsers;

public interface UserApplicationEngine extends IBOSession {
	
	public List getChildGroups(String groupId, String groupTypes, String groupRoles);
	
	public String getChildGroupsInString(String groupId, String groupTypes, String groupRoles);
	
	public String getSomeData(String groupId, String groupTypes);
	
	public List removeUsers(List usersIds, Integer groupId);
	
	public Document getMembersList(int parentGroupId, int groupId, int orderBy, String[] parameters);
	
	public Document getAddUserPresentationObject(String[] ids, List parentGroups, List childGroups, Integer userId, String groupTypes, String roleTypes);
	
	public Document getSimpleUserApplication(String instanceId);
	
	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, String groupTypes, String groupRoles);
	
	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers);
	
	public List getUserByPersonalId(String personalId);
	
	public String createUser(String name, String personalId, String password, Integer primaryGroupId, List childGroups);
	
}