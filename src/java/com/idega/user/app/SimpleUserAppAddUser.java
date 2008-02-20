package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.business.SpringBeanLookup;
import com.idega.core.contact.data.Email;
import com.idega.core.location.data.Country;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class SimpleUserAppAddUser extends Block {
	
	private List<Integer> parentGroups = null;
	private List<Integer> childGroups = null;
	
	private String groupTypesForParentGroups = null;
	private String parentGroupId = null;
	private String groupId = null;
	private String groupForUsersWithoutLoginId = null;
	private String parentComponentInstanceId = null;
	private String parentContainerId = null;
	private String groupTypes = null;
	private String roleTypes = null;
	
	private boolean getParentGroupsFromTopNodes = true;
	private boolean useChildrenOfTopNodesAsParentGroups = false;
	private boolean allFieldsEditable = false;
	
	private Integer userId = null;
	
	private GroupHelper groupsHelper = null;
	private SimpleUserAppHelper helper = new SimpleUserAppHelper();
	
	public SimpleUserAppAddUser(String parentComponentInstanceId, String parentContainerId, boolean allFieldsEditable) {
		if (parentComponentInstanceId == null || parentContainerId == null) {
			throw new NullPointerException("Provide valid parameters for " + SimpleUserAppAddUser.class.getName());
		}
		this.parentComponentInstanceId = parentComponentInstanceId;
		this.parentContainerId = parentContainerId;
		
		this.allFieldsEditable = allFieldsEditable;
	}

	public void main(IWContext iwc) {
		groupsHelper = SpringBeanLookup.getInstance().getSpringBean(iwc, GroupHelper.class);
		
		Layer container = new Layer();
		add(container);
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		//	User
		String id = null;
		String name = null;
		String personalId = null;
		String email = null;
		User user = null;
		if (userId != null) {
			user = groupsHelper.getUser(iwc, userId.intValue());
			if (user != null) {
				id = user.getId();
				name = user.getName();
				personalId = user.getPersonalID();
				email = getEmail(iwc, user);
			}
		}
		
		//	User name
		TextInput nameValueInput = new TextInput();
		String nameValueInputId = nameValueInput.getId();
		
		//	User password
		PasswordInput passwordInput = new PasswordInput();
		String passwordInputId = passwordInput.getId();
		
		//	Login name
		TextInput loginValueInput = new TextInput();
		if (!allFieldsEditable) {
			loginValueInput.setDisabled(true);
		}
		String loginInputId = loginValueInput.getId();
		
		//	Add user
		Layer addUserlabelContainer = new Layer();
		addUserlabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		addUserlabelContainer.add(new Text(iwrb.getLocalizedString("add_user", "Add user")));
		container.add(addUserlabelContainer);
		
		//	Container of available groups for user
		Layer availableGroupsOfUserContaianer = getFieldsContainer();
		
		//	Parent groups dropdown
		Layer parentGroupsContainer = new Layer();
		container.add(parentGroupsContainer);
		DropdownMenu parentGroupsChooser = new DropdownMenu();
		String parentGroupChooserId = parentGroupsChooser.getId();
		StringBuffer action = new StringBuffer("reloadAvailableGroupsForUser(");
		action.append(helper.getJavaScriptParameter(parentGroupChooserId)).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(helper.getJavaScriptParameter(id)).append(SimpleUserApp.COMMA_SEPARATOR).append("['");
		action.append(availableGroupsOfUserContaianer.getId()).append(SimpleUserApp.PARAMS_SEPARATOR);
		action.append(iwrb.getLocalizedString("loading", "Loading...")).append("', ");
		action.append(helper.getJavaScriptParameter(groupTypes)).append(SimpleUserApp.COMMA_SEPARATOR);
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
		
		//	Email
		TextInput emailInpnut = new TextInput();
		String emailInputId = emailInpnut.getId();
		if (email != null) {
			emailInpnut.setContent(email);
			if (!allFieldsEditable) {
				emailInpnut.setDisabled(true);
			}
		}
		
		//	Personal ID
		TextInput idValueInput = new TextInput();
		idValueInput.setMaxlength(12);
		
		//	Phone
		TextInput phoneInput = new TextInput();
		String phoneInputId = phoneInput.getId();
		
		//	Street name and number
		TextInput streetNameAndNumberInput = new TextInput();
		String streetNameAndNumberInputId = streetNameAndNumberInput.getId();
		
		//	Postal code id
		TextInput postalCodeIdInput = new TextInput();
		String postalCodeIdInputId = postalCodeIdInput.getId();
		
		//	Countries
		CountryDropdownMenu countriesDropdown = new CountryDropdownMenu();
		String countriesDropdownId = countriesDropdown.getId();
		
		//	City
		TextInput cityInput = new TextInput();
		String cityInputId = cityInput.getId();
		
		//	Province
		TextInput provinceInput = new TextInput();
		String provinceInputId = provinceInput.getId();
		
		//	Postal box
		TextInput postalBoxInput = new TextInput();
		String postalBoxInputId = postalBoxInput.getId();
		
		//	Data for inputs
		UserDataBean userInfo = null;
		UserApplicationEngine userEngine = null;
		if (user != null) {
			userEngine = SpringBeanLookup.getInstance().getSpringBean(iwc, UserApplicationEngine.class);
			userInfo = userEngine.getUserByPersonalId(user.getPersonalID());
		}
		if (userInfo != null && userInfo.getErrorMessage() == null) {
			streetNameAndNumberInput.setContent(userInfo.getStreetNameAndNumber());
			postalCodeIdInput.setContent(userInfo.getPostalCodeId());
			
			Country country = userEngine.getCountry(userInfo.getCountryName());
			if (country != null) {
				countriesDropdown.setSelectedCountry(country);
			}
			
			cityInput.setContent(userInfo.getCity());
			provinceInput.setContent(userInfo.getProvince());
			postalBoxInput.setContent(userInfo.getPostalBox());
			phoneInput.setContent(userInfo.getPhone());
			
			loginValueInput.setContent(userInfo.getLogin());
			if (CoreConstants.EMPTY.equals(userInfo.getPassword())) {
				passwordInput.setDisabled(false);
			}
			else {
				if (!allFieldsEditable) {
					passwordInput.setDisabled(true);
				}
			}
			passwordInput.setContent(userInfo.getPassword());
		}
		
		List<String> idsForFields = new ArrayList<String>();
		idsForFields.add(idValueInput.getId());								//	0	Personal ID
		idsForFields.add(nameValueInputId);									//	1	Name
		idsForFields.add(loginInputId);										//	2	Login
		idsForFields.add(passwordInputId);									//	3	Password
		idsForFields.add(iwrb.getLocalizedString("loading", "Loading..."));	//	4	Message
		idsForFields.add(emailInputId);										//	5 	Email
		idsForFields.add(streetNameAndNumberInputId);						//	6	Street name and number
		idsForFields.add(postalCodeIdInputId);								//	7	Postal code id
		idsForFields.add(countriesDropdownId);								//	8	Countries
		idsForFields.add(cityInputId);										//	9	City
		idsForFields.add(provinceInputId);									//	10	Province
		idsForFields.add(postalBoxInputId);									//	11	Postal box
		idsForFields.add(phoneInputId);										//	12	Phone
		StringBuffer idAction = new StringBuffer("getUserByPersonalId(");
		idAction.append(helper.getJavaScriptFunctionParameter(idsForFields)).append(SimpleUserApp.COMMA_SEPARATOR).append(allFieldsEditable).append(");");
		idValueInput.setOnKeyUp(idAction.toString());
		idValueInput.setContent(personalId == null ? CoreConstants.EMPTY : personalId);
		
		if (!allFieldsEditable) {
			nameValueInput.setDisabled(true);
		}
		nameValueInput.setContent(name == null ? CoreConstants.EMPTY : name);
		
		List<UIComponent> inputs = new ArrayList<UIComponent>();
		inputs.add(idValueInput);					//	0	Personal ID
		inputs.add(nameValueInput);					//	1	Name
		inputs.add(phoneInput);						//	2	Phone
		inputs.add(emailInpnut);					//	3	Email
		inputs.add(streetNameAndNumberInput);		//	4	Street name and number
		inputs.add(postalCodeIdInput);				//	5	Postal code id
		inputs.add(countriesDropdown);				//	6	Countries
		inputs.add(cityInput);						//	7	City
		inputs.add(provinceInput);					//	8	Province
		inputs.add(postalBoxInput);					//	9	Postal box
		addUserFields(iwc, userFieldsContainer, inputs);
		
		//	Login information
		Layer userLoginLabelContainer = new Layer();
		userLoginLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		userLoginLabelContainer.add(new Text(iwrb.getLocalizedString("login_information", "Login information")));
		container.add(userLoginLabelContainer);
		
		//	Login fields
		Layer userLoginContainer = new Layer();
		container.add(userLoginContainer);
		addLoginFields(iwc, userLoginContainer, loginValueInput, passwordInput);
		
		//	Selected groups
		Layer selectGroupsLabelContainer = new Layer();
		selectGroupsLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		container.add(selectGroupsLabelContainer);
		selectGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		Layer selectedGroupsContainer = new Layer();
		container.add(selectedGroupsContainer);
		List<String> childGroups = addSelectedGroups(iwc, user, selectedGroupsContainer, availableGroupsOfUserContaianer);
		
		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		List<String> ids = new ArrayList<String>();
		ids.add(parentGroupChooserId);			//	0
		ids.add(nameValueInputId);				//	1	Name
		ids.add(loginInputId);					//	2	Login
		ids.add(passwordInputId);				//	3	Password
		ids.add(groupForUsersWithoutLoginId);	//	4
		ids.add(emailInputId);					//	5	Email
		ids.add(phoneInputId);					//	6	Phone
		ids.add(streetNameAndNumberInputId);	//	7	Street name and number
		ids.add(idValueInput.getId());			//	8	Personal ID
		ids.add(postalCodeIdInputId);			//	9	Postal code
		ids.add(countriesDropdownId);			//	10	Country
		ids.add(cityInputId);					//	11	City
		ids.add(provinceInputId);				//	12	Province
		ids.add(postalBoxInputId);				//	13	Postal box
		addButtons(iwc, buttons, ids, childGroups);
	}
	
	private String getEmail(IWContext iwc, User user) {
		if (user == null) {
			return null;
		}
		
		UserBusiness userBusiness = groupsHelper.getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}
		Email email = null;
		try {
			email = userBusiness.getUserMail(user);
		} catch (RemoteException e) {}
		
		return email == null ? null : email.getEmailAddress();
	}
	
	private List<String> addSelectedGroups(IWContext iwc, User user, Layer container, Layer fieldsContainer) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		container.add(fieldsContainer);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("add_user_checkbox_description", "Select the groups the user should have access to by checking the groups checkbox.")));
		container.add(getSpacer());
		
		List<String> ids = new ArrayList<String>();
		Layer selectedGroupsContainer = helper.getSelectedGroupsByIds(iwc, user, groupsHelper, childGroups, ids, groupId);
		fieldsContainer.add(selectedGroupsContainer);
		
		return ids;
	}
	
	private void addLoginFields(IWContext iwc, Layer container, TextInput loginValueInput, PasswordInput passwordInput) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("user_login_description", "The user's name is always the user's personal ID and it cannot be changed.")));
		container.add(getSpacer());
		
		//	Login
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("login", "Username")));
		fieldsContainer.add(getComponentContainer(loginValueInput));
		fieldsContainer.add(getSpacer());
		
		//	Password
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("password", "Password")));
		fieldsContainer.add(getComponentContainer(passwordInput));
	}
	
	private Layer getFieldsContainer() {
		Layer fieldsContainer = new Layer();
		fieldsContainer.setStyleClass("userFieldsContainerStyleClass");
		return fieldsContainer;
	}
	
	private Layer getDescriptionContainer(String description) {
		Layer descriptionContainer = new Layer();
		descriptionContainer.setStyleClass("descriptionContainerStyleClass");
		descriptionContainer.add(new Text(description));
		return descriptionContainer;
	}
	
	private Layer getLabelContainer(String localizedText) {
		Layer labelContainer = new Layer();
		labelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		labelContainer.add(new Text(localizedText));
		return labelContainer;
	}
	
	private Layer getComponentContainer(UIComponent component) {
		Layer componetContainer = new Layer();
		componetContainer.setStyleClass("userFieldValueContainerStyleClass");
		if (component != null) {
			componetContainer.add(component);
		}
		return componetContainer;
	}
	
	private void addUserFields(IWContext iwc, Layer container, List<UIComponent> inputs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("enter_personal_id_desc", "Please enter the user's personal ID. The system finds the user's name from the national registry.")));
		container.add(getSpacer());
		
		//	Personal ID
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("personal_id", "Personal ID")));
		fieldsContainer.add(getComponentContainer(inputs.get(0)));
		fieldsContainer.add(getSpacer());
		
		//	Name
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("user.user_name", "Name")));
		fieldsContainer.add(getComponentContainer(inputs.get(1)));
		fieldsContainer.add(getSpacer());
		
		//	Phone
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("phone", "Phone")));
		fieldsContainer.add(getComponentContainer(inputs.get(2)));
		fieldsContainer.add(getSpacer());
		
		//	Email
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("email", "Email")));
		fieldsContainer.add(getComponentContainer(inputs.get(3)));
		fieldsContainer.add(getSpacer());
		
		//	Address fields
		Layer addressFields = getFieldsContainer();
		container.add(addressFields);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("user_address_info", "User's address information")));
		container.add(getSpacer());
		
		//	Street name and number
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.STREET_NAME_AND_NUMBER", "Street name and number")));
		addressFields.add(inputs.get(4));
		addressFields.add(getSpacer());
		
		//	Postal code
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.POSTAL_CODE", "Postal code")));
		addressFields.add(inputs.get(5));
		addressFields.add(getSpacer());
		
		//	Postal box
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.POSTAL_BOX", "Postal box")));
		addressFields.add(inputs.get(9));
		addressFields.add(getSpacer());
		
		//	City
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.CITY", "City")));
		addressFields.add(inputs.get(7));
		addressFields.add(getSpacer());
		
		//	Province
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.PROVINCE", "Province")));
		addressFields.add(inputs.get(8));
		addressFields.add(getSpacer());
		
		//	Country
		addressFields.add(getLabelContainer(iwrb.getLocalizedString("Address.COUNTRY", "Country")));
		addressFields.add(inputs.get(6));
		addressFields.add(getSpacer());
	}
	
	private void addParentGroups(IWContext iwc, Layer container, DropdownMenu parentGroupsChooser) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("add_user_parent_group_description", "Select parent group")));
		container.add(getSpacer());
		
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		
		Layer parentGroupValueContainer = getComponentContainer(null);
		fieldsContainer.add(parentGroupValueContainer);
		if (parentGroups == null) {	//	Normally shouldn't be null
			Group group = groupsHelper.getGroup(iwc, parentGroupId);
			if (group == null) {
				Collection<Group> topGroups = groupsHelper.getTopGroupsFromDomain(iwc);
				if (!getParentGroupsFromTopNodes) {
					topGroups = groupsHelper.getTopAndParentGroups(topGroups);	//	Will get top nodes and parent groups for them
				}
				if (topGroups == null) {
					addLabelForNoGroups(iwrb, parentGroupValueContainer);	//	No group available
				}
				else {
					parentGroupsChooser.addMenuElements(groupsHelper.getFilteredGroups(topGroups, groupTypesForParentGroups, ",", useChildrenOfTopNodesAsParentGroups));
					parentGroupValueContainer.add(parentGroupsChooser);
				}
			}
			else {
				String groupName = group.getName() == null ? iwrb.getLocalizedString("unknown_group", "Unknown group") : group.getName();
				parentGroupValueContainer.add(new Text(groupName));
			}
		}
		else {
			List<Group> groups = groupsHelper.getGroups(iwc, parentGroups);
			if (groups == null) {
				addLabelForNoGroups(iwrb, parentGroupValueContainer);	//	No group available
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
	
	private void addButtons(IWContext iwc, Layer container, List<String> ids, List<String> childGroups) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		GenericButton back = new GenericButton(iwrb.getLocalizedString("back", "Back"));
		StringBuffer backAction = new StringBuffer("goBackToSimpleUserApp('").append(parentComponentInstanceId);
		backAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(parentContainerId).append(SimpleUserApp.PARAMS_SEPARATOR);
		backAction.append(iwrb.getLocalizedString("loading", "Loading...")).append("')");
		back.setOnClick(backAction.toString());
		container.add(back);
		
		GenericButton save = new GenericButton(iwrb.getLocalizedString("save", "Save"));
		StringBuffer saveAction = new StringBuffer("saveUserInSimpleUserApplication(");
		saveAction.append(helper.getJavaScriptFunctionParameter(ids));
		saveAction.append(", ");
		saveAction.append(helper.getJavaScriptFunctionParameter(childGroups));
		saveAction.append(", '").append(iwrb.getLocalizedString("saving", "Saving..."));
		saveAction.append(SimpleUserApp.PARAMS_SEPARATOR);
		saveAction.append(iwrb.getLocalizedString("please_enter_password", "Please, enter password!")).append("', ").append(allFieldsEditable).append(");");
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

	public void setParentGroups(List<Integer> parentGroups) {
		this.parentGroups = parentGroups;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setChildGroups(List<Integer> childGroups) {
		this.childGroups = childGroups;
	}
	
	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}

	public void setRoleTypes(String roleTypes) {
		this.roleTypes = roleTypes;
	}

	public void setGetParentGroupsFromTopNodes(boolean getParentGroupsFromTopNodes) {
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	}

	private Layer getSpacer() {
		Layer spacer = new Layer();
		spacer.setStyleClass("spacer");
		return spacer;
	}

	public void setGroupTypesForParentGroups(String groupTypesForParentGroups) {
		this.groupTypesForParentGroups = groupTypesForParentGroups;
	}

	public void setUseChildrenOfTopNodesAsParentGroups(
			boolean useChildrenOfTopNodesAsParentGroups) {
		this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
	}

}
