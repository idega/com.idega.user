package com.idega.user.business;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.jdom2.Document;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.business.SpringBeanName;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Country;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.app.SimpleUserAppViewUsers;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.data.User;

@SpringBeanName(UserApplicationEngine.SPRING_BEAN_IDENTIFIER)
public interface UserApplicationEngine {

	public static final String SPRING_BEAN_IDENTIFIER = "userApplicationEngine";

	public List<AdvancedProperty> getChildGroups(String groupId, String groupTypes, String groupRoles, String subGroups, String subGroupsToExclude);

	public List<Integer> removeUsers(List<Integer> usersIds, Integer groupId);

	public Document getMembersList(SimpleUserPropertiesBean bean, String containerId);

	public Document getAddUserPresentationObject(SimpleUserPropertiesBean bean, List<Integer> parentGroups, List<Integer> childGroups, Integer userId);

	public Document getSimpleUserApplication(String instanceId, Integer parentGroupId);

	public Document getAvailableGroupsForUserPresentationObject(Integer parentGroupId, Integer userId, String groupTypes, String groupRoles, String subGroups, String subGroupsToExclude);

	public void addViewUsersCase(String instanceId, SimpleUserAppViewUsers viewUsers);

	public UserDataBean getUserByPersonalId(String personalId);

	public UserDataBean getUserById(Integer id);

	public AdvancedProperty createUser(UserDataBean userData, Integer primaryGroupId, List<Integer> childGroups, List<Integer> deselectedGroups,
			boolean allFieldsEditable, boolean sendEmailWithLoginInfo, String login, String password);

	public AdvancedProperty createUserWithEmailProps(UserDataBean userData, Integer primaryGroupId, List<Integer> childGroups, List<Integer> deselectedGroups,
			boolean allFieldsEditable, boolean sendEmailWithLoginInfo, String login, String password, Map<String, String> emailProps);

	public String isValidEmail(String email);
	
	public void sendMailWithLoginInfo(
			IWContext iwc,
			IWResourceBundle iwrb,
			boolean newLogin,
			String name,
			String login,
			String password,
			String email,
			Map<String, String> emailProps
	) throws MessagingException;

	public String getSimpleUserApplicationClassName();

	public Country getCountry(String countryName);

	public String getCountryIdByCountryName(String countryName);

	public UserDataBean getUserInfo(User user);

	public String saveGroup(String name, String homePageId, String type, String description, String parentGroupId, String groupId);

	public List<AdvancedProperty> findAvailablePages(String phrase);

	public List<AdvancedProperty> getAvailableGroups(String groupTypes, String groupTypesForChildrenGroups, String roleTypes, int groupId, int groupsType,
			boolean getTopAndParentGroups, boolean useChildrenOfTopNodesAsParentGroups, String subGroups, String subGroupsToExclude);

	public Layer getRolesEditor(IWContext iwc, int groupId, boolean addInput, List<String> selectedRoles);

	public Document getRenderedRolesEditor(int groupId, List<String> selectedRoles);

	public boolean changePermissionValueForRole(int groupId, String permissionKey, String roleKey, boolean value);

	public Document addNewRole(String roleKey, int groupId, List<String> selectedRoles);

	public String getGroupSaveStatus(boolean needErrorMessage);

	public void setPagerProperties(String id, List<Integer> properties);

	public List<Integer> getPagerProperties(String id);

	public String getIdForPagerProperties(SimpleUserPropertiesBean bean);

	public void fillUserInfo(UserDataBean info, Phone phone, Email email, Address address);

	public AdvancedProperty isValidUserName(String userName);

	public String getUserLogin(String personalId);

	public String getUserIdByLogin(String login);
}