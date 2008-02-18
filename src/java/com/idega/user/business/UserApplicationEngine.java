package com.idega.user.business;

import java.util.List;

import org.jdom.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.SpringBeanName;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;

@SpringBeanName("userApplicationEngine")
public interface UserApplicationEngine {

	public List<AdvancedProperty> getChildGroups(String groupId, String groupTypes, String groupRoles);
	
	public List<Integer> removeUsers(List<Integer> usersIds, Integer groupId);
	
	public Document getMembersList(SimpleUserPropertiesBean bean);
	
	public Document getAddUserPresentationObject(SimpleUserPropertiesBean bean, List<Integer> parentGroups, List<Integer> childGroups, Integer userId);
	
	public Document getSimpleUserApplication(String instanceId);
	
	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, Integer userId, String groupTypes, String groupRoles);
	
	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers);
	
	public UserDataBean getUserByPersonalId(String personalId);
	
	public String createUser(UserDataBean userData, Integer primaryGroupId, List<Integer> childGroups, List<Integer> deselectedGroups, boolean allFieldsEditable);
	
	public String isValidEmail(String email);
	
	public String getSimpleUserApplicationClassName();
	
}