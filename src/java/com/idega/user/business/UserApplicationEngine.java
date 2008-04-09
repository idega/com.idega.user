package com.idega.user.business;

import java.util.List;

import org.jdom.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.SpringBeanName;
import com.idega.core.location.data.Country;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.User;

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
	
	public Country getCountry(String countryName);
	
	public String getCountryIdByCountryName(String countryName);
	
	public GroupHelper getGroupHelperBean();
	
	public void setGroupHelperBean(GroupHelper groupHelper);
	
	public UserDataBean getUserInfo(User user);
	
	public String saveGroup(String name, String homePageId, String type, String description, String parentGroupId, String groupId);
	
	public List<AdvancedProperty> findAvailablePages(String phrase);
	
	public List<AdvancedProperty> getAvailableGroups(String groupTypes, String groupTypesForChildrenGroups, String roleTypes, int groupId, int groupsType,
			boolean getTopAndParentGroups, boolean useChildrenOfTopNodesAsParentGroups);
	
	public Layer getRolesEditor(IWContext iwc, int groupId, boolean addInput);
	
	public Document getRenderedRolesEditor(int groupId);
	
	public boolean changePermissionValueForRole(int groupId, String permissionKey, String roleKey, boolean value);
	
	public Document addNewRole(String roleKey, int groupId);
	
	public String getGroupSaveStatus(boolean needErrorMessage);
}