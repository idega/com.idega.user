package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.Window;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.user.data.UserGroupRepresentative;
import com.idega.util.IWColor;
import com.idega.util.IWTimestamp;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class CreateUser extends Window {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_create_tab_name";
	private static final String DEFAULT_TAB_NAME = "Create member";

	private Text fullNameText;
	private Text userLoginText;
	private Text passwordText;
	private Text confirmPasswordText;
	private Text ssnText;

	private Text generateLoginText;
	private Text generatePasswordText;
	private Text mustChangePasswordText;
	private Text cannotChangePasswordText;
	private Text passwordNeverExpiresText;
	private Text disableAccountText;
	private Text goToPropertiesText;
	private Text primaryGroupText;

	private TextInput fullNameField;
	private TextInput userLoginField;
	private PasswordInput passwordField;
	private PasswordInput confirmPasswordField;
	private TextInput ssnField;

	private CheckBox generateLoginField;
	private CheckBox generatePasswordField;
	private CheckBox mustChangePasswordField;
	private CheckBox cannotChangePasswordField;
	private CheckBox passwordNeverExpiresField;
	private CheckBox disableAccountField;
	private CheckBox goToPropertiesField;

	private DropdownMenu primaryGroupField;

	private SubmitButton okButton;
	private CloseButton cancelButton;

	private Form myForm;

	private String selectedGroupId = null;

	public static String PARAMETERSTRING_GROUP_ID = "default_group";

	public static String okButtonParameterValue = "ok";
	public static String cancelButtonParameterValue = "cancel";
	public static String submitButtonParameterName = "submit";

	public static String fullNameFieldParameterName = "fullName";
	public static String userLoginFieldParameterName = "login";
	public static String passwordFieldParameterName = "password";
	public static String confirmPasswordFieldParameterName = "confirmPassword";
	public static String ssnFieldParameterName = "ssn";

	public static String generateLoginFieldParameterName = "generateLogin";
	public static String generatePasswordFieldParameterName = "generatePassword";
	public static String mustChangePasswordFieldParameterName = "mustChange";
	public static String cannotChangePasswordFieldParameterName = "cannotChange";
	public static String passwordNeverExpiresFieldParameterName = "neverExpires";
	public static String disableAccountFieldParameterName = "disableAccount";
	public static String goToPropertiesFieldParameterName = "gotoProperties";
	public static String primaryGroupFieldParameterName = "primarygroup";

	private String rowHeight = "37";

	public CreateUser() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		setHeight(490);
		setWidth(390);
		setBackgroundColor(new IWColor(207, 208, 210));
		setScrollbar(false);
		myForm = new Form();
		add(myForm);
		initializeTexts();
		initializeFields();
		lineUpElements();
	}

	protected void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

  	fullNameText = new Text(iwrb.getLocalizedString(fullNameFieldParameterName,"Name"));
		userLoginText = new Text(iwrb.getLocalizedString(userLoginFieldParameterName,"User login"));
		passwordText = new Text(iwrb.getLocalizedString(passwordFieldParameterName,"Password"));
		confirmPasswordText = new Text(iwrb.getLocalizedString(confirmPasswordFieldParameterName,"Confirm password"));
		ssnText = new Text(iwrb.getLocalizedString(ssnFieldParameterName,"Personal ID (SSN)"));

		generateLoginText = new Text(iwrb.getLocalizedString(generateLoginFieldParameterName,"generate"));
		generatePasswordText = new Text(iwrb.getLocalizedString(generatePasswordFieldParameterName,"generate"));
		mustChangePasswordText = new Text(iwrb.getLocalizedString(mustChangePasswordFieldParameterName,"User must change password at next login"));
		cannotChangePasswordText = new Text(iwrb.getLocalizedString(cannotChangePasswordFieldParameterName,"User cannot change password"));
		passwordNeverExpiresText = new Text(iwrb.getLocalizedString(passwordNeverExpiresFieldParameterName,"Password never expires"));
		disableAccountText = new Text(iwrb.getLocalizedString(disableAccountFieldParameterName,"Account is disabled"));
		goToPropertiesText = new Text(iwrb.getLocalizedString(goToPropertiesFieldParameterName,"go to properties"));

		primaryGroupText = new Text(iwrb.getLocalizedString(primaryGroupFieldParameterName,"Primarygroup"));
	}

	protected void initializeFields() {
		fullNameField = new TextInput(fullNameFieldParameterName);
		fullNameField.setLength(20);
		userLoginField = new TextInput(userLoginFieldParameterName);
		userLoginField.setLength(12);
		passwordField = new PasswordInput(passwordFieldParameterName);
		passwordField.setLength(12);
		confirmPasswordField = new PasswordInput(confirmPasswordFieldParameterName);
		confirmPasswordField.setLength(12);
		ssnField = new TextInput(ssnFieldParameterName);
		ssnField.setLength(12);
		ssnField.setMaxlength(10);
		ssnField.setAsIcelandicSSNumber();

		generateLoginField = new CheckBox(generateLoginFieldParameterName);
		generatePasswordField = new CheckBox(generatePasswordFieldParameterName);
		mustChangePasswordField = new CheckBox(mustChangePasswordFieldParameterName);
		cannotChangePasswordField = new CheckBox(cannotChangePasswordFieldParameterName);
		passwordNeverExpiresField = new CheckBox(passwordNeverExpiresFieldParameterName);
		passwordNeverExpiresField.setChecked(true);
		disableAccountField = new CheckBox(disableAccountFieldParameterName);
		goToPropertiesField = new CheckBox(goToPropertiesFieldParameterName);
		goToPropertiesField.setChecked(true);

		primaryGroupField = new DropdownMenu(primaryGroupFieldParameterName);
		primaryGroupField.addSeparator();

		try {
			String[] gr = new String[1];
			gr[0] = ((UserGroupRepresentative) com.idega.user.data.UserGroupRepresentativeBMPBean.getStaticInstance(UserGroupRepresentative.class)).getGroupTypeValue();
			GroupHome home = (GroupHome) IDOLookup.getHome(Group.class);
			Collection groups = home.findAllGroups(gr, false);
			if (groups != null) {
				/**
				 * @todo filter standardGroups
				 */
				Iterator iter = groups.iterator();
				while (iter.hasNext()) {
					Group item = (Group) iter.next();
					primaryGroupField.addMenuElement(item.getPrimaryKey().toString(), item.getName());
				}
			}
		}
		catch (RemoteException ex) {
			throw new RuntimeException(ex.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		okButton = new SubmitButton("     OK     ", submitButtonParameterName, okButtonParameterValue);
		//cancelButton = new SubmitButton(" Cancel ", submitButtonParameterName, cancelButtonParameterValue);
		cancelButton = new CloseButton();

	}

	public void lineUpElements() {

		Table frameTable = new Table(1, 6);
		frameTable.setAlignment("center");
		frameTable.setVerticalAlignment("middle");
		frameTable.setCellpadding(0);
		frameTable.setCellspacing(0);

		// nameTable begin
		Table nameTable = new Table(2, 2);
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setHeight(1, rowHeight);
		nameTable.setHeight(2, rowHeight);

		nameTable.add(ssnText, 1, 1);
		nameTable.add(ssnField, 2, 1);
		nameTable.add(fullNameText, 1, 2);
		nameTable.add(fullNameField, 2, 2);
		// nameTable end

		// loginTable begin
		Table loginTable = new Table(4, 3);
		loginTable.setCellpadding(0);
		loginTable.setCellspacing(0);
		loginTable.setHeight(1, rowHeight);
		loginTable.setHeight(2, rowHeight);
		loginTable.setHeight(3, rowHeight);
		loginTable.setWidth(1, "110");

		loginTable.add(userLoginText, 1, 1);
		loginTable.add(userLoginField, 2, 1);
		loginTable.add(generateLoginField, 3, 1);
		loginTable.add(generateLoginText, 4, 1);
		loginTable.add(passwordText, 1, 2);
		loginTable.add(passwordField, 2, 2);
		loginTable.add(generatePasswordField, 3, 2);
		loginTable.add(generatePasswordText, 4, 2);
		loginTable.add(confirmPasswordText, 1, 3);
		loginTable.add(confirmPasswordField, 2, 3);
		// loginTable end

		// groupTable begin
		Table groupTable = new Table(2, 1);
		groupTable.setCellpadding(0);
		groupTable.setCellspacing(0);
		groupTable.setHeight(1, rowHeight);
		groupTable.setWidth(1, "110");

		groupTable.add(primaryGroupText, 1, 1);
		groupTable.add(primaryGroupField, 2, 1);
		// groupTable end

		// AccountPropertyTable begin
		Table AccountPropertyTable = new Table(2, 4);
		AccountPropertyTable.setCellpadding(0);
		AccountPropertyTable.setCellspacing(0);
		AccountPropertyTable.setHeight(1, rowHeight);
		AccountPropertyTable.setHeight(2, rowHeight);
		AccountPropertyTable.setHeight(3, rowHeight);
		AccountPropertyTable.setHeight(4, rowHeight);

		AccountPropertyTable.add(mustChangePasswordField, 1, 1);
		AccountPropertyTable.add(mustChangePasswordText, 2, 1);
		AccountPropertyTable.add(cannotChangePasswordField, 1, 2);
		AccountPropertyTable.add(cannotChangePasswordText, 2, 2);
		AccountPropertyTable.add(passwordNeverExpiresField, 1, 3);
		AccountPropertyTable.add(passwordNeverExpiresText, 2, 3);
		AccountPropertyTable.add(disableAccountField, 1, 4);
		AccountPropertyTable.add(disableAccountText, 2, 4);
		// AccountPropertyTable end

		// propertyTable begin
		Table propertyTable = new Table(2, 1);
		propertyTable.setCellpadding(0);
		propertyTable.setCellspacing(0);
		propertyTable.setHeight(1, rowHeight);

		propertyTable.add(goToPropertiesText, 1, 1);
		propertyTable.add(goToPropertiesField, 2, 1);
		// propertyTable end

		// buttonTable begin
		Table buttonTable = new Table(3, 1);
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setHeight(1, rowHeight);
		buttonTable.setWidth(2, "5");

		buttonTable.add(okButton, 1, 1);
		buttonTable.add(cancelButton, 3, 1);
		// buttonTable end

		frameTable.add(nameTable, 1, 1);
		frameTable.add(loginTable, 1, 2);
		frameTable.add(groupTable, 1, 3);
		frameTable.add(AccountPropertyTable, 1, 4);
		frameTable.add(propertyTable, 1, 5);
		frameTable.setAlignment(1, 5, "right");
		frameTable.add(buttonTable, 1, 6);
		frameTable.setAlignment(1, 6, "right");

		myForm.add(frameTable);

	}

	public void commitCreation(IWContext iwc) throws Exception {

		User newUser = null;

		String login = iwc.getParameter(userLoginFieldParameterName);
		String passw = iwc.getParameter(passwordFieldParameterName);
		String cfPassw = iwc.getParameter(confirmPasswordFieldParameterName);
		String password = null;
		String ssn = iwc.getParameter(ssnFieldParameterName);

		String mustChage = iwc.getParameter(mustChangePasswordFieldParameterName);
		String cannotchangePassw = iwc.getParameter(cannotChangePasswordFieldParameterName);
		String passwNeverExpires = iwc.getParameter(passwordNeverExpiresFieldParameterName);
		String disabledAccount = iwc.getParameter(disableAccountFieldParameterName);
		String primaryGroup = iwc.getParameter(primaryGroupFieldParameterName);

		Boolean bMustChange;
		Boolean bAllowedToChangePassw;
		Boolean bPasswNeverExpires;
		Boolean bEnabledAccount;

		Integer primaryGroupId = null;

		if (primaryGroup != null && !primaryGroup.equals("")) {
			primaryGroupId = new Integer(primaryGroup);
		}

		if (mustChage != null && !"".equals(mustChage)) {
			bMustChange = Boolean.TRUE;
		}
		else {
			bMustChange = Boolean.FALSE;
		}

		if (cannotchangePassw != null && !"".equals(cannotchangePassw)) {
			bAllowedToChangePassw = Boolean.FALSE;
		}
		else {
			bAllowedToChangePassw = Boolean.TRUE;
		}

		if (passwNeverExpires != null && !"".equals(passwNeverExpires)) {
			bPasswNeverExpires = Boolean.TRUE;
		}
		else {
			bPasswNeverExpires = Boolean.FALSE;
		}

		if (disabledAccount != null && !"".equals(disabledAccount)) {
			bEnabledAccount = Boolean.FALSE;
		}
		else {
			bEnabledAccount = Boolean.TRUE;
		}

		if (passw != null && cfPassw != null && passw.equals(cfPassw)) {
			password = passw;
		}
		else if (passw != null && cfPassw != null && !passw.equals(cfPassw)) {
			throw new Exception("password and confirmed password not the same");
		}

		try {
			String fullName = iwc.getParameter(fullNameFieldParameterName);
			IWTimestamp t = null;
			if (ssn != null) {
				t = new IWTimestamp();
			
				String day = ssn.substring(0,2);
				String month = ssn.substring(2,4);
				String year = ssn.substring(4,6);
				
				int iDay = Integer.parseInt(day);
				int iMonth = Integer.parseInt(month);
				int iYear = Integer.parseInt(year);
				if (ssn.substring(9).equals("9"))
					iYear += 1900;
				else if (ssn.substring(9).equals("0"))
					iYear += 2000;
				else if (ssn.substring(9).equals("8"))
					iYear += 1800;
				t.setHour(0);
				t.setMinute(0);
				t.setSecond(0);
				t.setMilliSecond(0);
				t.setDay(iDay);
				t.setMonth(iMonth);
				t.setYear(iYear);
				
				
			}
			newUser =
				getUserBusiness(iwc).createUserWithLogin(
					null,
					null,
					null,
					ssn,
					null,
					null,
					null,
					t,
					primaryGroupId,
					login,
					password,
					bEnabledAccount,
					IWTimestamp.RightNow(),
					5000,
					bPasswNeverExpires,
					bAllowedToChangePassw,
					bMustChange,
					null,
					fullName);
		}
		catch (Exception e) {
			add("Error: " + e.getMessage());
			e.printStackTrace();
		}

		if (iwc.getParameter(goToPropertiesFieldParameterName) != null) {
			Link gotoLink = new Link();
			gotoLink.setWindowToOpen(UserPropertyWindow.class);
			gotoLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, newUser.getPrimaryKey().toString());
			String script = "window.opener." + gotoLink.getWindowToOpenCallingScript(iwc);
			setOnLoad(script);
		}

	}

	public void main(IWContext iwc) throws Exception {
		String submit = iwc.getParameter("submit");
		selectedGroupId = iwc.getParameter(PARAMETERSTRING_GROUP_ID);
		if (selectedGroupId != null) {
			primaryGroupField.setSelectedElement(selectedGroupId);
			myForm.add(new HiddenInput(PARAMETERSTRING_GROUP_ID, selectedGroupId));
		}
		if (submit != null) {
			if (submit.equals("ok")) {
				commitCreation(iwc);
				close();
				setParentToReload();
			}
			else if (submit.equals("cancel")) {
				close();
			}
		}
	}

	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		UserBusiness business = null;
		if (business == null) {
			try {
				business = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return business;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}