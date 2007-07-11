 package com.idega.user.app;

import java.util.ArrayList;
import java.util.List;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.GroupHelperBusinessBean;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class SimpleUserAppAddUser extends SimpleUserApp {
	
	private List parentGroups = null;
	private List childGroups = null;
	
	private String parentGroupId = null;
	private String groupId = null;
	private String groupForUsersWithoutLoginId = null;
	private String parentComponentInstanceId = null;
	private String parentContainerId = null;
	private String groupTypes = null;
	private String roleTypes = null;
	
	private Integer userId = null;
	
	private GroupHelperBusinessBean groupsHelper = new GroupHelperBusinessBean();
	private SimpleUserAppHelper helper = new SimpleUserAppHelper();
	
	public SimpleUserAppAddUser(String parentComponentInstanceId, String parentContainerId) {
		this.parentComponentInstanceId = parentComponentInstanceId;
		this.parentContainerId = parentContainerId;
	}

	public void main(IWContext iwc) {
		Layer container = new Layer();
		add(container);
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		//	User
		User user = null;
		if (userId != null) {
			user = groupsHelper.getUser(iwc, userId.intValue());
		}
		
		//	User name input
		TextInput nameValueInput = new TextInput();
		String nameValueInputId = nameValueInput.getId();
		
		//	User password input
		PasswordInput passwordInput = new PasswordInput();
		String passwordInputId = passwordInput.getId();
		
		//	Login name
		TextInput loginValueInput = new TextInput();
		loginValueInput.setDisabled(true);
		String loginInputId = loginValueInput.getId();
		
		//	Add user
		Layer addUserlabelContainer = new Layer();
		addUserlabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		addUserlabelContainer.add(new Text(iwrb.getLocalizedString("add_user", "Add user")));
		container.add(addUserlabelContainer);
		
		//	Container of available groups for user
		Layer availableGroupsOfUserContaianer = new Layer();
		
		//	Parent groups dropdown
		Layer parentGroupsContainer = new Layer();
		container.add(parentGroupsContainer);
		DropdownMenu parentGroupsChooser = new DropdownMenu();
		String parentGroupChooserId = parentGroupsChooser.getId();
		StringBuffer action = new StringBuffer("reloadAvailableGroupsForUser(");
		action.append(helper.getJavaScriptParameter(parentGroupChooserId)).append(", ");
		action.append(helper.getJavaScriptParameter(user == null ? null : user.getId())).append(COMMA_SEPARATOR).append("['");
		action.append(availableGroupsOfUserContaianer.getId()).append(PARAMS_SEPARATOR);
		action.append(iwrb.getLocalizedString("loading", "Loading...")).append("', ");
		action.append(helper.getJavaScriptParameter(groupTypes)).append(COMMA_SEPARATOR);
		action.append(helper.getJavaScriptParameter(roleTypes)).append("]);");
		parentGroupsChooser.setOnChange(action.toString());
		addParentGroups(iwc, parentGroupsContainer, parentGroupsChooser);
		
		//	Choose user
		Layer chooseUserLabelContainer = new Layer();
		chooseUserLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		chooseUserLabelContainer.add(new Text(iwrb.getLocalizedString("choose_user", "Choose user")));
		container.add(chooseUserLabelContainer);
		
		//	User fields
		Layer userFieldsContainer = new Layer();
		container.add(userFieldsContainer);
		TextInput idValueInput = new TextInput();
		idValueInput.setMaxlength(12);
		StringBuffer idAction = new StringBuffer("getUserByPersonalId(");
		idAction.append(helper.getJavaScriptParameter(idValueInput.getId())).append(", '").append(nameValueInputId);
		idAction.append(PARAMS_SEPARATOR).append(loginInputId);
		idAction.append(PARAMS_SEPARATOR).append(passwordInputId);
		idAction.append(PARAMS_SEPARATOR).append(iwrb.getLocalizedString("loading", "Loading..."));
		idAction.append("');");
		idValueInput.setOnKeyUp(idAction.toString());
		nameValueInput.setDisabled(true);
		if (user != null) {
			idValueInput.setContent(user.getPersonalID() == null ? CoreConstants.EMPTY : user.getPersonalID());
			nameValueInput.setContent(user.getName() == null ? CoreConstants.EMPTY : user.getName());
		}
		addUserFields(iwc, userFieldsContainer, idValueInput, nameValueInput, loginInputId);
		
		//	Login information
		Layer userLoginLabelContainer = new Layer();
		userLoginLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		userLoginLabelContainer.add(new Text(iwrb.getLocalizedString("login_information", "Login information")));
		container.add(userLoginLabelContainer);
		
		//	Login fields
		Layer userLoginContainer = new Layer();
		container.add(userLoginContainer);
		if (user != null) {
			UserBusiness userBusiness = groupsHelper.getUserBusiness(iwc);
			if (userBusiness != null) {
				String login = userBusiness.getUserLogin(user);
				if (login == null) {
					loginValueInput.setContent(user.getPersonalID());
				}
				else {
					loginValueInput.setContent(login);
				}
				String password = userBusiness.getUserPassword(user);
				if (password != null) {
					passwordInput.setContent(password);
					passwordInput.setDisabled(true);
				}
			}
		}
		addLoginFields(iwc, userLoginContainer, loginValueInput, passwordInput);
		
		//	Selected groups
		Layer selectGroupsLabelContainer = new Layer();
		selectGroupsLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		container.add(selectGroupsLabelContainer);
		selectGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		Layer selectedGroupsContainer = new Layer();
		container.add(selectedGroupsContainer);
		List childGroups = addSelectedGroups(iwc, user, selectedGroupsContainer, availableGroupsOfUserContaianer);
		
		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		String[] ids = new String[5];
		ids[0] = parentGroupChooserId;
		ids[1] = nameValueInputId;
		ids[2] = loginInputId;
		ids[3] = passwordInputId;
		ids[4] = groupForUsersWithoutLoginId;
		addButtons(iwc, buttons, ids, childGroups);
	}
	
	private List addSelectedGroups(IWContext iwc, User user, Layer container, Layer fieldsContainer) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		fieldsContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(fieldsContainer);
		
		Layer descriptionContainer = new Layer();
		descriptionContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(descriptionContainer);
		descriptionContainer.add(new Text(iwrb.getLocalizedString("add_user_checkbox_description", "Select the groups the user should have access to by checking the groups checkbox.")));
		
		container.add(getSpacer());
		
		List ids = new ArrayList();
		
		Layer selectedGroupsContainer = helper.getSelectedGroupsByIds(iwc, user, groupsHelper, childGroups, ids, groupId);
		fieldsContainer.add(selectedGroupsContainer);
		
		return ids;
	}
	
	private void addLoginFields(IWContext iwc, Layer container, TextInput loginValueInput, PasswordInput passwordInput) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = new Layer();
		fieldsContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(fieldsContainer);
		
		Layer descriptionContainer = new Layer();
		descriptionContainer.setStyleClass("userFieldsContainerStyleClass");
		descriptionContainer.add(new Text(iwrb.getLocalizedString("user_login_description", "The user's name is always the user's personal ID and it cannot be changed.")));
		container.add(descriptionContainer);
		
		container.add(getSpacer());
		
		//	Login
		Layer loginLabelContainer = new Layer();
		loginLabelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		loginLabelContainer.add(new Text(iwrb.getLocalizedString("login", "Username")));
		fieldsContainer.add(loginLabelContainer);
		Layer loginValueContainer = new Layer();
		loginValueContainer.add(loginValueInput);
		fieldsContainer.add(loginValueContainer);
		
		fieldsContainer.add(getSpacer());
		
		//	Password
		Layer passwordLabelContainer = new Layer();
		passwordLabelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		passwordLabelContainer.add(new Text(iwrb.getLocalizedString("password", "Password")));
		fieldsContainer.add(passwordLabelContainer);
		Layer passwordValueContainer = new Layer();
		passwordValueContainer.add(passwordInput);
		fieldsContainer.add(passwordValueContainer);
	}
	
	private void addUserFields(IWContext iwc, Layer container, TextInput idValueInput, TextInput nameValueInput, String loginId) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = new Layer();
		fieldsContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(fieldsContainer);
		
		Layer descriptionContainer = new Layer();
		descriptionContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(descriptionContainer);
		descriptionContainer.add(new Text(iwrb.getLocalizedString("enter_personal_id_desc", "Please enter the user's personal ID. The system finds the user's name from the national registry.")));
	
		container.add(getSpacer());
		
		//	Personal ID
		Layer idLabelContainer = new Layer();
		idLabelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		idLabelContainer.add(new Text(iwrb.getLocalizedString("personal_id", "Personal ID")));
		fieldsContainer.add(idLabelContainer);
		Layer idValueContainer = new Layer();
		idValueContainer.add(idValueInput);
		fieldsContainer.add(idValueContainer);
		
		fieldsContainer.add(getSpacer());
		
		//	Name
		Layer nameLabelContainer = new Layer();
		nameLabelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		nameLabelContainer.add(new Text(iwrb.getLocalizedString("user.user_name", "Name")));
		fieldsContainer.add(nameLabelContainer);
		Layer nameValueContainer = new Layer();
		nameValueContainer.add(nameValueInput);
		fieldsContainer.add(nameValueContainer);
	}
	
	private void addParentGroups(IWContext iwc, Layer container, DropdownMenu parentGroupsChooser) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = new Layer();
		fieldsContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(fieldsContainer);
		
		Layer descriptionContainer = new Layer();
		descriptionContainer.add(new Text(iwrb.getLocalizedString("add_user_parent_group_description", "Some description text here.")));
		descriptionContainer.setStyleClass("userFieldsContainerStyleClass");
		container.add(descriptionContainer);
		
		container.add(getSpacer());
		
		Layer parentGroupLabelContainer = new Layer();
		parentGroupLabelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		parentGroupLabelContainer.add(new Text(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		fieldsContainer.add(parentGroupLabelContainer);
		
		Layer parentGroupValueContainer = new Layer();
		fieldsContainer.add(parentGroupValueContainer);
		if (parentGroups == null) {
			Group group = groupsHelper.getGroup(iwc, parentGroupId);
			if (group == null) {
				addLabelForNoGroups(iwrb, parentGroupValueContainer);	//	No group availabe
			}
			else {
				String groupName = group.getName() == null ? iwrb.getLocalizedString("unknown_group", "Unknown group") : group.getName();
				parentGroupValueContainer.add(new Text(groupName));
			}
		}
		else {
			List groups = groupsHelper.getGroups(iwc, parentGroups);
			if (groups == null) {
				addLabelForNoGroups(iwrb, parentGroupValueContainer);	//	No group availabe
			}
			else {
				parentGroupsChooser.addMenuElements(groups);
				parentGroupValueContainer.add(parentGroupsChooser);
				if (parentGroupId != null) {
					parentGroupsChooser.setSelectedElement(parentGroupId);
				}
			}
		}
	}
	
	private void addLabelForNoGroups(IWResourceBundle iwrb, Layer container) {
		container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
	}
	
	private void addButtons(IWContext iwc, Layer container, String[] ids, List childGroups) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		GenericButton back = new GenericButton(iwrb.getLocalizedString("back", "Back"));
		StringBuffer backAction = new StringBuffer("goBackToSimpleUserApp('").append(parentComponentInstanceId);
		backAction.append(PARAMS_SEPARATOR).append(parentContainerId).append(PARAMS_SEPARATOR);
		backAction.append(iwrb.getLocalizedString("loading", "Loading...")).append("')");
		back.setOnClick(backAction.toString());
		container.add(back);
		
		GenericButton save = new GenericButton(iwrb.getLocalizedString("save", "Save"));
		StringBuffer saveAction = new StringBuffer("saveUserInSimpleUserApplication([");
		for (int i = 0; i < ids.length; i++) {
			saveAction.append(helper.getJavaScriptParameter(ids[i]));
			if ((i + 1) < ids.length) {
				saveAction.append(COMMA_SEPARATOR);
			}
		}
		saveAction.append("], ['");
		for (int i = 0; i < childGroups.size(); i++) {
			saveAction.append(childGroups.get(i).toString());
			if ((i + 1) < childGroups.size()) {
				saveAction.append(PARAMS_SEPARATOR);
			}
		}
		saveAction.append("'], '").append(iwrb.getLocalizedString("saving", "Saving..."));
		saveAction.append(PARAMS_SEPARATOR);
		saveAction.append(iwrb.getLocalizedString("please_enter_password", "Please, enter password!")).append("');");
		save.setOnClick(saveAction.toString());
		container.add(save);
	}

	public void setGroupForUsersWthouLoginId(String groupForUsersWithoutLoginId) {
		this.groupForUsersWithoutLoginId = groupForUsersWithoutLoginId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setParentGroupId(String parentGroupId) {
		this.parentGroupId = parentGroupId;
	}
	
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public void setParentGroups(List parentGroups) {
		this.parentGroups = parentGroups;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setChildGroups(List childGroups) {
		this.childGroups = childGroups;
	}
	
	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}

	public void setRoleTypes(String roleTypes) {
		this.roleTypes = roleTypes;
	}

	private Layer getSpacer() {
		Layer spacer = new Layer();
		spacer.setStyleClass("spacer");
		return spacer;
	}

}
