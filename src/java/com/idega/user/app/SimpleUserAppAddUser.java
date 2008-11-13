package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.faces.component.UIComponent;

import com.idega.content.business.ContentConstants;
import com.idega.core.contact.data.Email;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class SimpleUserAppAddUser extends Block {
	
	private List<Integer> parentGroups = null;
	private List<Integer> childGroups = null;
	
	private SimpleUserPropertiesBean properties = null;
	
	private Integer userId = null;
	
	private GroupHelper groupsHelper = null;
	private SimpleUserAppHelper helper = new SimpleUserAppHelper();
	
	private IWResourceBundle iwrb = null;
	
	private String requiredFieldLocalizationKey = "this_field_is_required";
	private String requiredFieldLocalizationValue = "This field is required!";
	
	public SimpleUserAppAddUser(SimpleUserPropertiesBean properties) {
		this.properties = properties;
		
		String parentComponentInstanceId = properties.getInstanceId();
		String parentContainerId = properties.getContainerId();
		if (StringUtil.isEmpty(parentComponentInstanceId) || StringUtil.isEmpty(parentContainerId)) {
			throw new NullPointerException("Provide valid parameters for " + SimpleUserAppAddUser.class.getName());
		}
	}

	@Override
	public void main(IWContext iwc) {
		groupsHelper = ELUtil.getInstance().getBean(GroupHelper.class);
		
		Layer container = new Layer();
		add(container);
		
		iwrb = getResourceBundle(iwc);
		
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
		if (!properties.isAllFieldsEditable()) {
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
		action.append(helper.getJavaScriptParameter(properties.getGroupTypes())).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(helper.getJavaScriptParameter(properties.getRoleTypes())).append("], ")
		.append(helper.getJavaScriptParameter(properties.getParentGroupId() == -1 ? null : String.valueOf(properties.getParentGroupId()))).append(");");
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
		countriesDropdown.setFirstSelectOption(new SelectOption(iwrb.getLocalizedString("simple_user_application.select_country", "Select country"), -1));
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
		
		//	***************************** Data for inputs *****************************
		UserDataBean userInfo = new UserDataBean();
		UserApplicationEngine userEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		if (user != null) {
			userInfo = userEngine.getUserInfo(user);
		}
		
		//	Personal ID
		if (!StringUtil.isEmpty(personalId)) {
			idValueInput.setContent(personalId);
			idValueInput.setDisabled(true);
		}
		
		//	Name
		if (!properties.isAllFieldsEditable()) {
			nameValueInput.setDisabled(true);
		}
		nameValueInput.setContent(name == null ? CoreConstants.EMPTY : name);
		
		//	Phone
		phoneInput.setContent(userInfo.getPhone());
		
		//	Email
		emailInpnut.setContent(email == null ? CoreConstants.EMPTY : email);
		if (!properties.isAllFieldsEditable()) {
			emailInpnut.setDisabled(true);
		}
		
		//	Street and number
		streetNameAndNumberInput.setContent(userInfo.getStreetNameAndNumber());
		
		//	Postal code
		postalCodeIdInput.setContent(userInfo.getPostalCodeId());
		
		//	Postal box
		postalBoxInput.setContent(userInfo.getPostalBox());
		
		//	City
		cityInput.setContent(userInfo.getCity());
		
		//	Province
		provinceInput.setContent(userInfo.getProvince());
		
		//	Country
		Country country = userEngine.getCountry(userInfo.getCountryName());
		if (country == null) {
			CountryHome countryHome = null;
			try {
				countryHome = (CountryHome) IDOLookup.getHome(Country.class);
			} catch (IDOLookupException e) {
				e.printStackTrace();
			}
			if (countryHome != null) {
				Locale locale = iwc.getCurrentLocale();
				try {
					country = countryHome.findByIsoAbbreviation(locale.getCountry());
				} catch (Exception e) {
					log(Level.INFO, SimpleUserAppAddUser.class.getName() + ": country was not found by locale: " + locale);
				}
			}
		}
		countriesDropdown.setSelectedCountry(country);
		
		//	Login
		loginValueInput.setContent(userInfo.getLogin());
		
		//	Password
		if (CoreConstants.EMPTY.equals(userInfo.getPassword())) {
			passwordInput.setDisabled(false);
		}
		else {
			if (!properties.isAllFieldsEditable()) {
				passwordInput.setDisabled(true);
			}
		}
		passwordInput.setContent(userInfo.getPassword());
		
		//	Account enabled/disabled
		CheckBox manageAccountAvailability = getCheckBox((userInfo.getAccountEnabled() == null || !userInfo.getAccountEnabled()) ? 
				iwrb.getLocalizedString("account_is_disabled_check_to_enable", "Account is disabled, check to enable it") :
				iwrb.getLocalizedString("account_is_enabled_un_check_to_disable_it", "Account is enabled, uncheck to disable it"),
				userInfo.getAccountEnabled() != null && userInfo.getAccountEnabled());
		String accountManagerId = manageAccountAvailability.getId();
		
		//	Change password next time
		CheckBox changePasswordNextTime = getCheckBox(iwrb.getLocalizedString("user_will_have_to_change_password_next_time",
				"User will have to change password on next login"), userInfo.getChangePasswordNextTime() != null && userInfo.getChangePasswordNextTime());
		String changePasswordNextTimeId = changePasswordNextTime.getId();
	
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
		idsForFields.add(accountManagerId);									//	13	Account enabled
		idsForFields.add(changePasswordNextTimeId);							//	14	Change password
		StringBuffer idAction = new StringBuffer("getUserByPersonalId(event, ").append(helper.getJavaScriptFunctionParameter(idsForFields))
								.append(SimpleUserApp.COMMA_SEPARATOR).append(properties.isAllFieldsEditable()).append(");");
		idValueInput.setOnKeyUp(idAction.toString());
		
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
		inputs.add(manageAccountAvailability);		//	10	Enable/disable account
		inputs.add(changePasswordNextTime);			//	11	Change password next time
		addUserFields(iwc, userFieldsContainer, inputs);
		
		//	Login information
		Layer userLoginLabelContainer = new Layer();
		userLoginLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		userLoginLabelContainer.add(new Text(iwrb.getLocalizedString("login_information", "Login information")));
		container.add(userLoginLabelContainer);
		
		//	Login fields
		Layer userLoginContainer = new Layer();
		container.add(userLoginContainer);
		List<GenericInput> loginInputs = new ArrayList<GenericInput>();
		loginInputs.add(loginValueInput);
		loginInputs.add(passwordInput);
		loginInputs.add(manageAccountAvailability);
		loginInputs.add(changePasswordNextTime);
		addLoginFields(iwc, userLoginContainer, loginInputs);
		
		//	Selected groups
		Layer selectGroupsLabelContainer = new Layer();
		selectGroupsLabelContainer.setStyleClass("addUserlabelContainerStyleClass");
		container.add(selectGroupsLabelContainer);
		selectGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		addRequiredFieldMark(selectGroupsLabelContainer);
		Layer selectedGroupsContainer = new Layer();
		container.add(selectedGroupsContainer);
		List<String> childGroups = addSelectedGroups(iwc, user, selectedGroupsContainer, availableGroupsOfUserContaianer);
		
		//	Explanation text
		Layer explanationContainer = getLabelContainer(null, true);
		container.add(explanationContainer);
		explanationContainer.add(new Text("&nbsp;"));
		explanationContainer.add(new Text(iwrb.getLocalizedString(requiredFieldLocalizationKey, requiredFieldLocalizationValue)));
		
		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		List<String> ids = new ArrayList<String>();
		ids.add(parentGroupChooserId);			//	0
		ids.add(nameValueInputId);				//	1	Name
		ids.add(loginInputId);					//	2	Login
		ids.add(passwordInputId);				//	3	Password
		ids.add(properties.getDefaultGroupId() == null ? "null" : properties.getDefaultGroupId());				//	4
		ids.add(emailInputId);					//	5	Email
		ids.add(phoneInputId);					//	6	Phone
		ids.add(streetNameAndNumberInputId);	//	7	Street name and number
		ids.add(idValueInput.getId());			//	8	Personal ID
		ids.add(postalCodeIdInputId);			//	9	Postal code
		ids.add(countriesDropdownId);			//	10	Country
		ids.add(cityInputId);					//	11	City
		ids.add(provinceInputId);				//	12	Province
		ids.add(postalBoxInputId);				//	13	Postal box
		ids.add(properties.isAllowEnableDisableAccount() ? accountManagerId : "-1");							//	14 Enable/disable account
		ids.add(properties.isChangePasswordNextTime() ? changePasswordNextTimeId : "-1");						//	15 Change password next time
		ids.add(properties.getParentGroupId() == -1 ? "-1" : String.valueOf(properties.getParentGroupId()));	//	16 Selected group ID
		addButtons(iwc, buttons, ids, childGroups, userInfo.isAccountExists());
	}
	
	private CheckBox getCheckBox(String toolTip, boolean checked) {
		CheckBox checkBox = new CheckBox();
		checkBox.setChecked(checked, true);
		checkBox.setToolTip(toolTip);
		checkBox.setOnClick(new StringBuilder("if (!window.confirm('").append(iwrb.getLocalizedString("are_you_sure", "Are you sure?"))
						.append("')) { var checkBox = document.getElementById('").append(checkBox.getId()).append("'); checkBox.checked = !checkBox.checked; }")
						.toString());
		return checkBox;
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
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("add_user_checkbox_description",
				"Select the groups the user should have access to by checking the groups checkbox.")));
		container.add(getSpacer());
		
		List<String> ids = new ArrayList<String>();
		String selectedGroupId = String.valueOf(properties.getGroupId());
		if (selectedGroupId == null || ContentConstants.MINUS_ONE.equals(selectedGroupId)) {
			selectedGroupId = String.valueOf(properties.getParentGroupId());
		}
		Layer selectedGroupsContainer = helper.getSelectedGroupsByIds(iwc, user, groupsHelper, childGroups, ids, selectedGroupId);
		fieldsContainer.add(selectedGroupsContainer);
		
		return ids;
	}
	
	private void addLoginFields(IWContext iwc, Layer container, List<GenericInput> inputs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("user_login_description",
				"The user's name is always the user's personal ID and it cannot be changed.")));
		container.add(getSpacer());
		
		//	Login
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("login", "Username"), true));
		fieldsContainer.add(getComponentContainer(inputs.get(0)));
		fieldsContainer.add(getSpacer());
		
		//	Password
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("password", "Password"), true));
		fieldsContainer.add(getComponentContainer(inputs.get(1)));
		
		//	Enable/disable account
		if (properties.isAllowEnableDisableAccount()) {
			fieldsContainer.add(getSpacer());
			fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("account_enabled", "Account enabled")));
			fieldsContainer.add(getComponentContainer(inputs.get(2)));
		}
		
		//	Change password next time
		if (properties.isChangePasswordNextTime()) {
			fieldsContainer.add(getSpacer());
			fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("set_to_change_password_next_time", "Change password next time")));
			fieldsContainer.add(getComponentContainer(inputs.get(3)));
		}
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
		return getLabelContainer(localizedText, false);
	}
	
	private Layer getLabelContainer(String localizedText, boolean required) {
		Layer labelContainer = new Layer();
		labelContainer.setStyleClass("userFieldLabelContainerStyleClass");
		
		if (!StringUtil.isEmpty(localizedText)) {
			labelContainer.add(new Text(localizedText));
		}
		
		if (required) {
			addRequiredFieldMark(labelContainer);
		}
		
		return labelContainer;
	}
	
	private void addRequiredFieldMark(Layer container) {
		Span requiredText = new Span(new Text("*"));
		requiredText.setStyleClass("requiredFieldUserApp");
		requiredText.setToolTip(iwrb.getLocalizedString(requiredFieldLocalizationKey, requiredFieldLocalizationValue));
		container.add(requiredText);
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
		
		container.add(getDescriptionContainer(iwrb.getLocalizedString("enter_personal_id_desc",
				"Please enter the user's personal ID. The system finds the user's name from the national registry.")));
		container.add(getSpacer());
		
		//	Personal ID
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("personal_id", "Personal ID")));
		fieldsContainer.add(getComponentContainer(inputs.get(0)));
		fieldsContainer.add(getSpacer());
		
		//	Name
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("user.user_name", "Name"), true));
		fieldsContainer.add(getComponentContainer(inputs.get(1)));
		fieldsContainer.add(getSpacer());
		
		//	Phone
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("phone", "Phone")));
		fieldsContainer.add(getComponentContainer(inputs.get(2)));
		fieldsContainer.add(getSpacer());
		
		//	Email
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("email", "Email"), true));
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
		
		fieldsContainer.add(getLabelContainer(iwrb.getLocalizedString("select_parent_group", "Select parent group"), true));
		
		Layer parentGroupValueContainer = getComponentContainer(null);
		fieldsContainer.add(parentGroupValueContainer);
		String parentGroupId = properties.getParentGroupId() < 0 ? null : String.valueOf(properties.getParentGroupId());
		if (ListUtil.isEmpty(parentGroups)) {									//	Normally shouldn't be empty
			Group group = groupsHelper.getGroup(iwc, parentGroupId);
			if (group == null) {
				Collection<Group> topGroups = groupsHelper.getTopGroupsFromDomain(iwc);
				if (!properties.isGetParentGroupsFromTopNodes()) {
					topGroups = groupsHelper.getTopAndParentGroups(topGroups);	//	Will get top nodes and parent groups for them
				}
				if (topGroups == null) {
					addLabelForNoGroups(iwrb, parentGroupValueContainer);		//	No group available
				}
				else {
					parentGroupsChooser.addMenuElements(groupsHelper.getFilteredGroups(iwc, topGroups, properties.getGroupTypesForParentGroups(),
							CoreConstants.COMMA, properties.isUseChildrenOfTopNodesAsParentGroups()));
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
	
	private void addButtons(IWContext iwc, Layer container, List<String> ids, List<String> childGroups, boolean accountExists) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		GenericButton back = new GenericButton(iwrb.getLocalizedString("back", "Back"));
		StringBuffer backAction = new StringBuffer("goBackToSimpleUserApp('").append(properties.getInstanceId());
		backAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(properties.getContainerId()).append(SimpleUserApp.PARAMS_SEPARATOR)
					.append(iwrb.getLocalizedString("loading", "Loading...")).append(SimpleUserApp.PARAMS_SEPARATOR).append(ids.get(0)).append("', ")
					.append(helper.getJavaScriptParameter(properties.getParentGroupId() == -1 ? null : String.valueOf(properties.getParentGroupId())))
					.append(");");
		back.setOnClick(backAction.toString());
		container.add(back);
		
		GenericButton save = new GenericButton(iwrb.getLocalizedString("save", "Save"));
		List<String> messages = new ArrayList<String>();
		messages.add(iwrb.getLocalizedString("saving", "Saving..."));								//	0
		messages.add(iwrb.getLocalizedString("please_enter_password", "Please, enter password!"));	//	1	Password
		messages.add(iwrb.getLocalizedString("please_enter_name", "Please, enter name!"));			//	2	Name
		messages.add(iwrb.getLocalizedString("please_enter_login", "Please, enter login!"));		//	3	Login
		StringBuffer saveAction = new StringBuffer("saveUserInSimpleUserApplication(");
		saveAction.append(helper.getJavaScriptFunctionParameter(ids)).append(SimpleUserApp.COMMA_SEPARATOR);
		saveAction.append(helper.getJavaScriptFunctionParameter(childGroups)).append(SimpleUserApp.COMMA_SEPARATOR);
		saveAction.append(helper.getJavaScriptFunctionParameter(messages)).append(SimpleUserApp.COMMA_SEPARATOR).append(properties.isAllFieldsEditable());
		saveAction.append(SimpleUserApp.COMMA_SEPARATOR).append(helper.getJavaScriptParameter(userId == null ? null : String.valueOf(userId)));
		saveAction.append(SimpleUserApp.COMMA_SEPARATOR).append(accountExists ? false : properties.isSendMailToUser()).append(SimpleUserApp.COMMA_SEPARATOR);
		saveAction.append(properties.isJuridicalPerson()).append(");");
		save.setOnClick(saveAction.toString());
		container.add(save);
	}
	
	@Override
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

	private Layer getSpacer() {
		return new CSSSpacer();
	}

}
