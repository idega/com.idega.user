package com.idega.user.business;


import java.util.List;

import org.jdom.Document;

import com.idega.business.IBOSession;

public interface UserApplicationEngine extends IBOSession {
	
	public List getChildGroups(String groupId, String groupTypes, String groupRoles);
	
	public String getChildGroupsInString(String groupId, String groupTypes, String groupRoles);
	
	public String getSomeData(String groupId, String groupTypes);
	
	public List removeUsers(List usersIds, Integer groupId);
	
	public Document getMembersList(int parentGroupId, int groupId, int orderBy);
	
}