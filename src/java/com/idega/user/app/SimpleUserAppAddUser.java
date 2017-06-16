package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.ejb.FinderException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.content.business.ContentConstants;
import com.idega.content.upload.presentation.FileUploadViewer;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.contact.data.Email;
import com.idega.core.localisation.data.ICLanguage;
import com.idega.core.localisation.data.ICLanguageHome;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Gender;
import com.idega.user.data.GenderHome;
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

	@Autowired
	private GroupHelper groupsHelper;
	@Autowired
	private SimpleUserAppHelper helper;

	private IWResourceBundle iwrb = null;

	private String requiredFieldLocalizationKey = "this_field_is_required";
	private String requiredFieldLocalizationValue = "This field is required!";

	private ICLanguageHome icLanguageHome;

	private ICLanguageHome getICLanguageHome() {
		if (this.icLanguageHome  == null) {
			try {
				this.icLanguageHome = (ICLanguageHome) IDOLookup.getHome(ICLanguage.class);
			} catch (RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return this.icLanguageHome;
	}

	public SimpleUserAppAddUser(SimpleUserPropertiesBean properties) {
		this.properties = properties;

		String parentComponentInstanceId = properties.getInstanceId();
		String parentContainerId = properties.getContainerId();
		if (StringUtil.isEmpty(parentComponentInstanceId) || StringUtil.isEmpty(parentContainerId)) {
			throw new NullPointerException("Provide valid parameters for " + SimpleUserAppAddUser.class.getName());
		}
	}

	private Boolean ableToSetLoginAndPassword = null;
	private boolean isAbleToSetLoginAndPassword() {
		if (ableToSetLoginAndPassword == null)
			ableToSetLoginAndPassword = IWMainApplication.getDefaultIWMainApplication().getSettings().getBoolean("sua_able_set_login_psw", Boolean.TRUE);
		return ableToSetLoginAndPassword;
	}

	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);

		Layer container = new Layer();
		container.setStyleClass("add-user");
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
		PasswordInput passwordInput = isAbleToSetLoginAndPassword() ? new PasswordInput() : null;
		String passwordInputId = passwordInput == null ? null : passwordInput.getId();

		//	Login name
		TextInput loginValueInput = isAbleToSetLoginAndPassword() || user != null ? new TextInput() : null;
		String loginInputId = null;
		if (loginValueInput != null) {
			if (!properties.isAllFieldsEditable())
				loginValueInput.setDisabled(true);
			loginInputId = loginValueInput.getId();
		}

		//	Add user
		Layer addUserlabelContainer = new Layer();
		addUserlabelContainer.setStyleClass("addUserlabelContainerStyleClass add-user-label");
		addUserlabelContainer.add(new Text(iwrb.getLocalizedString("add_user", "Add user")));
		container.add(addUserlabelContainer);

		//	Container of available groups for user
		Layer availableGroupsOfUserContaianer = getFieldsContainer();

		//	Parent groups dropdown
		Layer parentGroupsContainer = new Layer();
		container.add(parentGroupsContainer);
		UIComponentBase parentGroupsChooser;
		String parentGroupChooserId;
		if (user == null){
			parentGroupsChooser = new DropdownMenu();
			parentGroupChooserId = parentGroupsChooser.getId();
			StringBuffer action = new StringBuffer("reloadAvailableGroupsForUser(");
			action.append(helper.getJavaScriptParameter(parentGroupChooserId)).append(SimpleUserApp.COMMA_SEPARATOR);
			action.append(helper.getJavaScriptParameter(id)).append(SimpleUserApp.COMMA_SEPARATOR).append("['");
			action.append(availableGroupsOfUserContaianer.getId()).append(SimpleUserApp.PARAMS_SEPARATOR);
			action.append(iwrb.getLocalizedString("loading", "Loading...")).append("', ");
			action.append(helper.getJavaScriptParameter(properties.getGroupTypes())).append(SimpleUserApp.COMMA_SEPARATOR);
			action.append(helper.getJavaScriptParameter(properties.getRoleTypes())).append("], ")
			.append(helper.getJavaScriptParameter(properties.getParentGroupId() == -1 ? null : String.valueOf(properties.getParentGroupId()))).append(SimpleUserApp.COMMA_SEPARATOR);
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(properties.getSubGroups()).append(CoreConstants.QOUTE_SINGLE_MARK).append(SimpleUserApp.COMMA_SEPARATOR);
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(properties.getSubGroupsToExclude()).append(CoreConstants.QOUTE_SINGLE_MARK);
			action.append(");");
			((DropdownMenu)parentGroupsChooser).setOnChange(action.toString());
			addParentGroups(iwc, parentGroupsContainer, (DropdownMenu)parentGroupsChooser);
		}
		else {
			Group group = user.getPrimaryGroup();
			String groupName = "";
			if (group != null) groupName = group.getName();
			parentGroupsChooser = new Text(groupName);
			parentGroupChooserId = parentGroupsChooser.getId();
		}


		//	Choose user
		Layer chooseUserLabelContainer = new Layer();
		chooseUserLabelContainer.setStyleClass("addUserlabelContainerStyleClass personal-information");
		chooseUserLabelContainer.add(new Text(iwrb.getLocalizedString("sua.personal_information", "Personal information")));
		container.add(chooseUserLabelContainer);

		//	User fields
		Layer userFieldsContainer = new Layer();
		container.add(userFieldsContainer);

		//	Email
		TextInput emailInpnut = new TextInput();
		String emailInputId = emailInpnut.getId();

		//	Personal ID
		TextInput idValueInput = new TextInput();
		idValueInput.setTitle(iwrb.getLocalizedString("sua.enter_personal_id_to_search_for_user", "Enter personal ID to search for a person"));
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

		List<String> langs = Arrays.asList("de", "fr", "es", "en", "it", "ru", "zh");

		List<ICLanguage> languages = new ArrayList<ICLanguage>();
		try {
			languages.addAll(getICLanguageHome().findManyByISOAbbreviation(langs));
		} catch (FinderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		// Language
		UserBusiness userBusiness = null;
		try {
			userBusiness = IBOLookup.getServiceInstance(getIWApplicationContext(), UserBusiness.class);
		} catch (IBOLookupException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		ICLanguage l1 = null;
		ICLanguage l2 = null;
		ICLanguage l3 = null;
		String l4 = null;
		String sl = null;
		if (user != null) {
			Map<String, String> meta = user.getMetaDataAttributes();
			if (!StringUtil.isEmpty(meta.get("language1"))){
				try {
					l1 = getICLanguageHome().findByPrimaryKey(Integer.parseInt(meta.get("language1")));
				} catch (NumberFormatException | FinderException e) {
				}
			}
			if (!StringUtil.isEmpty(meta.get("language2"))){
				try{
					l2 = getICLanguageHome().findByPrimaryKey(Integer.parseInt(meta.get("language2")));
				} catch (NumberFormatException | FinderException e) {
				}
			}
			if (!StringUtil.isEmpty(meta.get("language3"))){
				try {
					l3 = getICLanguageHome().findByPrimaryKey(Integer.parseInt(meta.get("language3")));
				} catch (NumberFormatException | FinderException e) {
				}
			}
			l4 =meta.get("language4");
			sl =meta.get("skillLevel");
		}


		DropdownMenu language1 = new DropdownMenu();
		language1.setEntities(languages);
		language1.setMenuElement(0, iwrb.getLocalizedString("simple_user_application.select_language", "Select language"));
		language1.setSelectedElement(0);
		if (l1 != null) language1.setSelectedElement(Integer.parseInt(l1.getPrimaryKey().toString()));


		DropdownMenu language2 = new DropdownMenu();
		language2.setEntities(languages);
		language2.setMenuElement(0, iwrb.getLocalizedString("simple_user_application.select_language", "Select language"));
		language2.setSelectedElement(0);
		if (l2 != null) language2.setSelectedElement(Integer.parseInt(l2.getPrimaryKey().toString()));

		DropdownMenu language3 = new DropdownMenu();
		language3.setEntities(languages);
		language3.setMenuElement(0, iwrb.getLocalizedString("simple_user_application.select_language", "Select language"));
		language3.setSelectedElement(0);
		if (l3 != null) language3.setSelectedElement(Integer.parseInt(l3.getPrimaryKey().toString()));

		TextInput languageOther = new TextInput();
		languageOther.setValue(l4);

		String l1id = language1.getId();
		String l2id = language2.getId();
		String l3id = language3.getId();
		String l4id = languageOther.getId();

		DropdownMenu gender = new DropdownMenu();
		GenderHome genderHome = null;
		try {
			genderHome = (GenderHome) IDOLookup.getHome(Gender.class);
			List <Gender> genders = new ArrayList<Gender>(genderHome.findAllGenders());
			gender.setEntities(genders);
			gender.setMenuElement(-1, iwrb.getLocalizedString("simple_user_application.select_gender", "Select gender"));
			if (user != null) gender.setSelectedElement(user.getGenderID());
		} catch (IDOLookupException e1) {
		} catch (FinderException e) {

		}
		String genderId = gender.getId();

		IWDatePicker birthDay = new IWDatePicker();
		if (user != null) birthDay.setDate(user.getDateOfBirth());
		String birthDayId = birthDay.getId();
		birthDay.setVersion("1.8.17");

		DropdownMenu skillLevel = new DropdownMenu();
		skillLevel.setMenuElement(0, iwrb.getLocalizedString("simple_user_application.select_skillLevel", "Select skill level"));
		skillLevel.setMenuElement(1, iwrb.getLocalizedString("simple_user_application.expert_skillLevel", "Expert"));
		skillLevel.setMenuElement(2, iwrb.getLocalizedString("simple_user_application.advanced_skillLevel", "Advanced"));
		skillLevel.setMenuElement(3, iwrb.getLocalizedString("simple_user_application.basic_skillLevel", "Basic"));
		skillLevel.setSelectedElement(sl == null ? 0 : Integer.parseInt(sl));
		String skillLevelId = skillLevel.getId();

		Text dateCreated = new Text(user == null ? "" : user.getCreated().toGMTString());
		String dateCreatedId = dateCreated.getId();

		//	***************************** Data for inputs *****************************
		UserDataBean userInfo = new UserDataBean();
		UserApplicationEngine userEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		if (user != null)
			userInfo = userEngine.getUserInfo(user);

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

		//	Picture
		Image picture = new Image();
		picture.setStyleClass("simpleUserApplicationUserPicture");
		picture.setTitle(userInfo.isImageSet() ? iwrb.getLocalizedString("sua.click_to_delete_or_change_picture", "Click to change or delete picture") :
			iwrb.getLocalizedString("sua.click_to_add_picture", "Click to add picture"));
		String pictureId = picture.getId();
		String pictureChangerId = getPictureChanger(iwc, container, pictureId, userInfo.isImageSet());
		picture.setOnClick(getPictureChangerAction(pictureId, pictureChangerId));
		picture.setURL(StringUtil.isEmpty(userInfo.getPictureUri()) ? getBundle(iwc).getVirtualPathWithFileNameString("images/user_default.png") :
			userInfo.getPictureUri());

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
		if (user != null) {
			LoginTable loginTable = LoginDBHandler.getUserLogin(user);
			if (loginTable == null) {
				loginValueInput.setDisabled(false);
			} else {
				loginValueInput.setDisabled(true);
				loginValueInput.setContent(loginTable.getUserLogin());
			}
		}

		//	Password
		if (passwordInput != null) {
			String password = null;
			if (user != null) {
				try {
					userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
					password = userBusiness.getUserPassword(user);
				} catch (Exception e) {
					getLogger().log(Level.WARNING, "Error getting password for user " + user, e);
				}
			}

			if (StringUtil.isEmpty(password)) {
				passwordInput.setDisabled(false);
			} else {
				if (!properties.isAllFieldsEditable()) {
					passwordInput.setDisabled(true);
				}
			}
			passwordInput.setContent(password);
		}

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
		idsForFields.add(StringUtil.isEmpty(loginInputId) ? CoreConstants.MINUS : loginInputId);		//	2	Login
		idsForFields.add(StringUtil.isEmpty(passwordInputId) ? CoreConstants.MINUS : passwordInputId);	//	3	Password
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
		idsForFields.add(pictureId);										//	15	Picture
		idsForFields.add(l1id);
		idsForFields.add(l2id);
		idsForFields.add(l3id);
		idsForFields.add(l4id);
		idsForFields.add(genderId);
		idsForFields.add(birthDayId);
		idsForFields.add(skillLevelId);
		idsForFields.add(dateCreatedId);
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
		inputs.add(picture);						//	12	Picture
		inputs.add(language1);
		inputs.add(language2);
		inputs.add(language3);
		inputs.add(languageOther);
		inputs.add(gender);
		inputs.add(birthDay);
		inputs.add(skillLevel);
		inputs.add(dateCreated);
		addUserFields(iwc, userFieldsContainer, inputs, userInfo, pictureChangerId);

		//	Login information
		Layer userLoginLabelContainer = new Layer();
		userLoginLabelContainer.setStyleClass("addUserlabelContainerStyleClass login-information");
		userLoginLabelContainer.add(new Text(iwrb.getLocalizedString("login_information", "Login information")));
		container.add(userLoginLabelContainer);

		//	Login fields
		Layer userLoginContainer = new Layer();
		container.add(userLoginContainer);
		List<GenericInput> loginInputs = new ArrayList<GenericInput>();
		loginInputs.add(manageAccountAvailability);
		loginInputs.add(changePasswordNextTime);
		if (loginValueInput != null)
			loginInputs.add(loginValueInput);
		if (passwordInput != null)
			loginInputs.add(passwordInput);
		addLoginFields(iwc, userLoginContainer, loginInputs);

		//	Selected groups
		List<String> childGroups = null;
		if (properties.isShowSubGroup()) {
			Layer selectGroupsLabelContainer = new Layer();
			selectGroupsLabelContainer.setStyleClass("addUserlabelContainerStyleClass select-sub-group-label");
			container.add(selectGroupsLabelContainer);
			selectGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
			addRequiredFieldMark(selectGroupsLabelContainer);
			Layer selectedGroupsContainer = new Layer();
			container.add(selectedGroupsContainer);
			addSelectedGroups(iwc, user, selectedGroupsContainer, availableGroupsOfUserContaianer);
		}

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
		ids.add(StringUtil.isEmpty(loginInputId) ? CoreConstants.MINUS : loginInputId);		//	2	Login
		ids.add(StringUtil.isEmpty(passwordInputId) ? CoreConstants.MINUS : passwordInputId);	//	3	Password
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
		ids.add(pictureId);						//	17	Picture
		ids.add(l1id);
		ids.add(l2id);
		ids.add(l3id);
		ids.add(l4id);
		ids.add(genderId);
		ids.add(birthDayId);
		ids.add(skillLevelId);
		ids.add(dateCreatedId);
		addButtons(iwc, buttons, ids, childGroups, userInfo.isAccountExists());
	}

	private String getPictureChangerAction(String id, String boxId) {
		return new StringBuilder("SimpleUserApplication.togglePictureChanger('").append(id).append("', '").append(boxId).append("');").toString();
	}

	private String getPictureChanger(IWContext iwc, Layer container, String pictureId, boolean hasImage) {
		Layer pictureChangerContainer = new Layer();
		container.add(pictureChangerContainer);
		pictureChangerContainer.setStyleClass("simpleUserApplicationPictureChangerBox");
		pictureChangerContainer.setStyleAttribute("display: none");

		Layer explantationContainer = new Layer();
		explantationContainer.setStyleClass("simpleUserApplicationPictureChangerBoxExplanationText");
		Text explanation = new Text(new StringBuilder(
				iwrb.getLocalizedString("sua.select_picture_and_upload_note_about_alowed_files", "Select profile picture and upload it. Available types:"))
				.append(" PNG, JPEG, GIF").toString());
		explantationContainer.add(explanation);
		pictureChangerContainer.add(explantationContainer);

		FileUploadViewer pictureUploader = new FileUploadViewer();
		pictureUploader.setAllowMultipleFiles(false);
		pictureUploader.setAutoAddFileInput(false);
		String uploadPath = CoreConstants.CONTENT_PATH + "/users/temp/";
		pictureUploader.setUploadPath(uploadPath);
		pictureUploader.setActionAfterUpload(new StringBuilder("SimpleUserApplication.toggleUserPicture('").append(pictureId).append("', '")
			.append(CoreConstants.WEBDAV_SERVLET_URI).append(uploadPath).append("', '").append(pictureChangerContainer.getId()).append("');").toString());
		pictureChangerContainer.add(pictureUploader);

		Layer buttons = new Layer();
		pictureChangerContainer.add(buttons);

		if (hasImage) {
			GenericButton delete = new GenericButton(iwrb.getLocalizedString("delete", "Delete"));
			delete.setOnClick(new StringBuilder("SimpleUserApplication.toggleUserPicture('").append(pictureId).append("', '")
					.append(getBundle(iwc).getVirtualPathWithFileNameString("images/user_default.png")).append("', '").append(pictureChangerContainer.getId())
					.append("');").toString());
			delete.setStyleClass("simpleUserApplicationPictureDeleter");
			buttons.add(delete);
		}

		GenericButton close = new GenericButton(iwrb.getLocalizedString("close", "Close"));
		close.setTitle(iwrb.getLocalizedString("sua.close_picture_changer", "Close"));
		close.setOnClick(getPictureChangerAction(pictureId, pictureChangerContainer.getId()));
		close.setStyleClass("simpleUserApplicationPictureChangerBoxCloser");
		buttons.add(close);

		return pictureChangerContainer.getId();
	}

	private CheckBox getCheckBox(String toolTip, boolean checked) {
		CheckBox checkBox = new CheckBox();
		checkBox.setChecked(checked, true);
		checkBox.setTitle(toolTip);
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

		Layer subGroupsDescription = getDescriptionContainer(iwrb.getLocalizedString("add_user_checkbox_description",
				"Select the groups the user should have access to by checking the groups checkbox."));
		container.add(subGroupsDescription);
		subGroupsDescription.setStyleClass("sub-groups field-layer");
		container.add(getSpacer());

		List<String> ids = new ArrayList<String>();
		String selectedGroupId = String.valueOf(properties.getGroupId());
		if (selectedGroupId == null || ContentConstants.MINUS_ONE.equals(selectedGroupId)) {
			selectedGroupId = String.valueOf(properties.getParentGroupId());
		}
		Layer fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("sub-groups field-layer");
		Layer selectedGroupsContainer = helper.getSelectedGroupsByIds(iwc, user, groupsHelper, childGroups, ids, selectedGroupId);
		fieldContainer.add(selectedGroupsContainer);

		return ids;
	}

	private void addLoginFields(IWContext iwc, Layer container, List<GenericInput> inputs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);

		Layer loginDescription = getDescriptionContainer(iwrb.getLocalizedString("user_login_password_description",
				"The user's name is always the user's personal ID and it cannot be changed. Password will be auto generated by the system and sent via email to teh user"));
		container.add(loginDescription);
		loginDescription.setStyleClass("login");
		container.add(getSpacer());

		//	Login
		if (inputs.size() > 2) {
			Layer fieldContainer = new Layer("span");
			fieldsContainer.add(fieldContainer);
			fieldContainer.setStyleClass("login field-layer");
			fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("login", "Username"), true));
			fieldContainer.add(getComponentContainer(inputs.get(2)));
			fieldContainer.add(getSpacer());
		}

		//	Password
		if (inputs.size() > 3) {
			Layer fieldContainer = new Layer("span");
			fieldsContainer.add(fieldContainer);
			fieldContainer.setStyleClass("password field-layer");
			fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("password", "Password"), true));
			fieldContainer.add(getComponentContainer(inputs.get(3)));
		}

		//	Enable/disable account
		if (properties.isAllowEnableDisableAccount()) {
			Layer fieldContainer = new Layer("span");
			fieldsContainer.add(fieldContainer);
			fieldContainer.setStyleClass("enabled field-layer");
			fieldContainer.add(getSpacer());
			fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("account_enabled", "Account enabled")));
			fieldContainer.add(getComponentContainer(inputs.get(0)));
		}

		//	Change password next time
		if (properties.isChangePasswordNextTime()) {
			Layer fieldContainer = new Layer("span");
			fieldsContainer.add(fieldContainer);
			fieldContainer.setStyleClass("change-password field-layer");
			fieldContainer.add(getSpacer());
			fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("set_to_change_password_next_time", "Change password next time")));
			fieldContainer.add(getComponentContainer(inputs.get(1)));
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
		requiredText.setTitle(iwrb.getLocalizedString(requiredFieldLocalizationKey, requiredFieldLocalizationValue));
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

	private void addUserFields(IWContext iwc, Layer container, List<UIComponent> inputs, UserDataBean userInfo, String pictureChangerBoxId) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);

		Layer personalIdDescription = getDescriptionContainer(iwrb.getLocalizedString("enter_personal_id_desc",
				"Please enter the user's personal ID. The system finds the user's name from the national registry."));
		container.add(personalIdDescription);
		personalIdDescription.setStyleClass("personal-id field-layer");
		container.add(getSpacer());

		//	Personal ID
		Layer fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("personal-id field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("personal_id", "Personal ID")));
		fieldContainer.add(getComponentContainer(inputs.get(0)));
		fieldContainer.add(getSpacer());

		//	Name
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("name field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("user.user_name", "Name"), true));
		fieldContainer.add(getComponentContainer(inputs.get(1)));
		fieldContainer.add(getSpacer());

		//	Phone
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("phone field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("phone", "Phone")));
		fieldContainer.add(getComponentContainer(inputs.get(2)));
		fieldContainer.add(getSpacer());

		//	Email
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("email field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("email", "Email"), true));
		fieldContainer.add(getComponentContainer(inputs.get(3)));
		fieldContainer.add(getSpacer());

		//	Picture
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("picture field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("picture", "Picture")));
		Layer imageContainer = getComponentContainer(inputs.get(12));
		imageContainer.add(new Break());
		Link pictureEditor = new Link(userInfo.isImageSet() ? iwrb.getLocalizedString("sua.edit_user_picture", "Edit picture") :
			iwrb.getLocalizedString("sua.add_user_picture", "Add picture"), "javascript:void(0)");
		pictureEditor.setOnClick(getPictureChangerAction(inputs.get(12).getId(), pictureChangerBoxId));
		pictureEditor.setStyleClass("simpleUserApplicationLinkToPictureEditor");
		imageContainer.add(pictureEditor);
		fieldContainer.add(imageContainer);
		fieldContainer.add(getSpacer());

		//	Address fields
		Layer addressFields = getFieldsContainer();
		container.add(addressFields);

		Layer addressDescription = getDescriptionContainer(iwrb.getLocalizedString("user_address_info", "User's address information"));
		addressDescription.setStyleClass("address-description");
		container.add(addressDescription);
		container.add(getSpacer());

		//	Street name and number
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("street-and-number field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("Address.STREET_NAME_AND_NUMBER", "Street name and number")));
		fieldContainer.add(inputs.get(4));
		fieldContainer.add(getSpacer());

		//	Postal code
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("postal-code field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("Address.POSTAL_CODE", "Postal code")));
		fieldContainer.add(inputs.get(5));
		fieldContainer.add(getSpacer());

		//	Postal box
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("postal-box field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("Address.POSTAL_BOX", "Postal box")));
		fieldContainer.add(inputs.get(9));
		fieldContainer.add(getSpacer());

		//	City
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("city field-layer");
		Layer city = new Layer();
		city.setStyleClass("cityLayer");
		city.add(getLabelContainer(iwrb.getLocalizedString("Address.CITY", "City")));
		city.add(inputs.get(7));
		city.add(getSpacer());
		fieldContainer.add(city);

		//	Province
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("province field-layer");
		Layer province = new Layer();
		province.setStyleClass("provinceLayer");
		province.add(getLabelContainer(iwrb.getLocalizedString("Address.PROVINCE", "Province")));
		province.add(inputs.get(8));
		province.add(getSpacer());
		fieldContainer.add(province);

		//	Country
		fieldContainer = new Layer("span");
		addressFields.add(fieldContainer);
		fieldContainer.setStyleClass("country field-layer");
		Layer country = new Layer();
		country.setStyleClass("countryLayer");
		country.add(getLabelContainer(iwrb.getLocalizedString("Address.COUNTRY", "Country")));
		country.add(inputs.get(6));
		country.add(getSpacer());
		fieldContainer.add(country);

		// Language 1
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("language field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("language", "Language")));
		fieldContainer.add(getComponentContainer(inputs.get(13)));
		fieldContainer.add(getSpacer());

		// Language 2
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("language field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("language", "Language")));
		fieldContainer.add(getComponentContainer(inputs.get(14)));
		fieldContainer.add(getSpacer());

		// Language 3
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("language field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("language", "Language")));
		fieldContainer.add(getComponentContainer(inputs.get(15)));
		fieldContainer.add(getSpacer());

		// Language 4
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("language field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("language", "Language")));
		fieldContainer.add(getComponentContainer(inputs.get(16)));
		fieldContainer.add(getSpacer());

		// Gender
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("gender field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("gender", "Gender")));
		fieldContainer.add(getComponentContainer(inputs.get(17)));
		fieldContainer.add(getSpacer());

		// Birth day
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("birthday field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("birthday", "Birth day")));
		fieldContainer.add(getComponentContainer(inputs.get(18)));
		fieldContainer.add(getSpacer());

		// Skill level
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("skilllevel field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("skilllevel", "Skill level")));
		fieldContainer.add(getComponentContainer(inputs.get(19)));
		fieldContainer.add(getSpacer());

		// Date created
		fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("datecreated field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("datecreated", "Date created")));
		fieldContainer.add(getComponentContainer(inputs.get(20)));
		fieldContainer.add(getSpacer());

	}

	private void addParentGroups(IWContext iwc, Layer container, DropdownMenu parentGroupsChooser) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		Layer fieldsContainer = getFieldsContainer();
		container.add(fieldsContainer);

		Layer parentDescription = getDescriptionContainer(iwrb.getLocalizedString("add_user_parent_group_description", "Select parent group"));
		container.add(parentDescription);
		container.add(getSpacer());
		parentDescription.setStyleClass("parent-group field-layer");

		Layer fieldContainer = new Layer("span");
		fieldsContainer.add(fieldContainer);
		fieldContainer.setStyleClass("parent-group field-layer");
		fieldContainer.add(getLabelContainer(iwrb.getLocalizedString("select_parent_group", "Select parent group"), true));

		Layer parentGroupValueContainer = getComponentContainer(null);
		fieldContainer.add(parentGroupValueContainer);
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
