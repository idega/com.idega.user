package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class CreateUser extends StyledIWAdminWindow { 
	private GroupBusiness groupBiz;

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

/*	private CheckBox generateLoginField;
	private CheckBox generatePasswordField;
	private CheckBox mustChangePasswordField;
	private CheckBox cannotChangePasswordField;
	private CheckBox passwordNeverExpiresField;
	private CheckBox disableAccountField;
	*/
	private HiddenInput goToPropertiesField;
	
	private GroupChooser primaryGroupField;

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
/*
	public static String generateLoginFieldParameterName = "generateLogin";
	public static String generatePasswordFieldParameterName = "generatePassword";
	public static String mustChangePasswordFieldParameterName = "mustChange";
	public static String cannotChangePasswordFieldParameterName = "cannotChange";
	public static String passwordNeverExpiresFieldParameterName = "neverExpires";
	public static String disableAccountFieldParameterName = "disableAccount";*/
	public static String goToPropertiesFieldParameterName = "gotoProperties";
	public static String primaryGroupFieldParameterName = "primarygroup";

	private String rowHeight = "37";

	private UserBusiness userBiz;
	
	private String inputTextStyle = "text";
	private String backgroundTableStyle = "back";
	private String mainTableStyle = "main";
	private String bannerTableStyle = "banner";

	public CreateUser() {
		super();
		setHeight(250);
		setWidth(350);
	//	setBackgroundColor(new IWColor(207, 208, 210));
		setScrollbar(false);
		setResizable(true);
	}

	protected void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

  	fullNameText = new Text(iwrb.getLocalizedString(fullNameFieldParameterName,"Name"));
		userLoginText = new Text(iwrb.getLocalizedString(userLoginFieldParameterName,"User login"));
		passwordText = new Text(iwrb.getLocalizedString(passwordFieldParameterName,"Password"));
		confirmPasswordText = new Text(iwrb.getLocalizedString(confirmPasswordFieldParameterName,"Confirm password"));
		ssnText = new Text(iwrb.getLocalizedString(ssnFieldParameterName,"Personal ID (SSN)"));

	/*	generateLoginText = new Text(iwrb.getLocalizedString(generateLoginFieldParameterName,"generate"));
		generatePasswordText = new Text(iwrb.getLocalizedString(generatePasswordFieldParameterName,"generate"));
		mustChangePasswordText = new Text(iwrb.getLocalizedString(mustChangePasswordFieldParameterName,"User must change password at next login"));
		cannotChangePasswordText = new Text(iwrb.getLocalizedString(cannotChangePasswordFieldParameterName,"User cannot change password"));
		passwordNeverExpiresText = new Text(iwrb.getLocalizedString(passwordNeverExpiresFieldParameterName,"Password never expires"));
		disableAccountText = new Text(iwrb.getLocalizedString(disableAccountFieldParameterName,"Account is disabled"));
		goToPropertiesText = new Text(iwrb.getLocalizedString(goToPropertiesFieldParameterName,"go to properties"));*/

		primaryGroupText = new Text(iwrb.getLocalizedString(primaryGroupFieldParameterName,"Primarygroup"));
	}

	protected void initializeFields(IWContext iwc) {
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		fullNameField = new TextInput(fullNameFieldParameterName);
		fullNameField.setLength(20);
		fullNameField.setStyleClass("text");
		fullNameField.setAsNotEmpty(iwrb.getLocalizedString(fullNameFieldParameterName,"Full name must be selected"));
		userLoginField = new TextInput(userLoginFieldParameterName);
		userLoginField.setLength(12);
		passwordField = new PasswordInput(passwordFieldParameterName);
		passwordField.setLength(12);
		confirmPasswordField = new PasswordInput(confirmPasswordFieldParameterName);
		confirmPasswordField.setLength(12);
		ssnField = new TextInput(ssnFieldParameterName);
		ssnField.setLength(20);
		ssnField.setMaxlength(12);
		ssnField.setAsNotEmpty(iwrb.getLocalizedString(ssnFieldParameterName,"Personal ID must be selected"));
		//ssnField.setAsIcelandicSSNumber();

		/*generateLoginField = new CheckBox(generateLoginFieldParameterName);
		generatePasswordField = new CheckBox(generatePasswordFieldParameterName);
		mustChangePasswordField = new CheckBox(mustChangePasswordFieldParameterName);
		cannotChangePasswordField = new CheckBox(cannotChangePasswordFieldParameterName);
		passwordNeverExpiresField = new CheckBox(passwordNeverExpiresFieldParameterName);
		passwordNeverExpiresField.setChecked(true);
		disableAccountField = new CheckBox(disableAccountFieldParameterName);*/
		goToPropertiesField = new HiddenInput(goToPropertiesFieldParameterName,"TRUE");
		//goToPropertiesField.setChecked(true);
		
		primaryGroupField = new GroupChooser(primaryGroupFieldParameterName);
//		primaryGroupField.setAsNotEmpty(iwrb.getLocalizedString(primaryGroupFieldParameterName,"A group must be selected"));
		
		okButton = new SubmitButton(iwrb.getLocalizedString("save", "Save"), submitButtonParameterName, okButtonParameterValue);
    okButton.setAsImageButton(true);
		//cancelButton = new SubmitButton(" Cancel ", submitButtonParameterName, cancelButtonParameterValue);
		cancelButton = new CloseButton(iwrb.getLocalizedString("close", "Close"));
		cancelButton.setAsImageButton(true);

	}

	public void lineUpElements(IWContext iwc) {
	
		
		Table backTable = new Table(1,3);
		backTable.setStyleClass(backgroundTableStyle);
		backTable.setCellspacing(0);
		backTable.setCellpadding(0);
		backTable.setWidth("100%");
		backTable.setHeight("100%");
		backTable.setAlignment("left");
		backTable.setVerticalAlignment("middle");

		Table mainTable = new Table(2,2);
		mainTable.setStyleClass(mainTableStyle);
		mainTable.setCellspacing(10);
		mainTable.setCellpadding(0);
		mainTable.setWidth("98%");
		mainTable.setHeight("98%");
		mainTable.setAlignment("center");
		mainTable.setVerticalAlignment("middle");
		
		/*
		 * commented out 7/10/03 
		Table frameTable = new Table(1, 4);
		frameTable.setAlignment("center");
		frameTable.setVerticalAlignment("middle");
		frameTable.setCellpadding(0);
		frameTable.setCellspacing(0);*/
		
		Table inputTable = new Table(1, 6);
		inputTable.setCellpadding(0);
		inputTable.setCellspacing(0);
		
		inputTable.add(fullNameText,1,1);
		inputTable.add(fullNameField,1,2);
		inputTable.add(ssnText,1,3);
		inputTable.add(ssnField,1,4);
		inputTable.add(primaryGroupText, 1, 5);
		inputTable.add(primaryGroupField, 1, 6);
	//	inputTable.add(goToPropertiesField,1,5);
		
		// nameTable begin
		/* commented out 7/10/03
		Table nameTable = new Table(2, 2);
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setHeight(1, rowHeight);
		nameTable.setHeight(2, rowHeight);

		nameTable.add(ssnText, 1, 1);
		nameTable.add(ssnField, 2, 1);
		nameTable.add(fullNameText, 1, 2);
		nameTable.add(fullNameField, 2, 2);
		*/
		// nameTable end

		// loginTable begin
		/*Table loginTable = new Table(4, 3);
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
		loginTable.add(confirmPasswordField, 2, 3);*/
		// loginTable end

		// groupTable begin
		/*commented out 8/10/03
		Table groupTable = new Table(1, 2);
		groupTable.setCellpadding(0);
		groupTable.setCellspacing(0);
		groupTable.setHeight(1, rowHeight);
		groupTable.setWidth(1, "110");

		groupTable.add(primaryGroupText, 1, 1);
		groupTable.add(primaryGroupField, 1, 2);
		*/
		// groupTable end

		// AccountPropertyTable begin
		/*Table AccountPropertyTable = new Table(2, 4);
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
		AccountPropertyTable.add(disableAccountText, 2, 4);*/
		// AccountPropertyTable end

		// propertyTable begin
		Table propertyTable = new Table(2, 1);
		propertyTable.setCellpadding(0);
		propertyTable.setCellspacing(0);
		propertyTable.setHeight(1, rowHeight);

	//	propertyTable.add(goToPropertiesText, 1, 1);
		propertyTable.add(goToPropertiesField, 2, 1);
		
		// propertyTable end

		// buttonTable begin
		Table buttonTable = new Table(3, 1);
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setHeight(1, rowHeight);
		buttonTable.setWidth(2, "5");
		buttonTable.setAlignment("right");
		buttonTable.setVerticalAlignment("bottom");

		buttonTable.add(okButton, 1, 1); 
		buttonTable.add(cancelButton, 3, 1);
		// buttonTable end
		
		mainTable.add(inputTable, 1,1);
		mainTable.add(buttonTable, 2,2);
		
//		backTable.add(bannerTable,1,1);
		backTable.add(mainTable,1,2);

	/*commented out 7/10/03
		frameTable.add(nameTable, 1, 1);
		//frameTable.add(loginTable, 1, 2);
		frameTable.add(groupTable, 1, 2);
		//frameTable.add(AccountPropertyTable, 1, 4);
		frameTable.add(propertyTable, 1, 3);
		frameTable.add(buttonTable, 1, 4);
		frameTable.setAlignment(1, 4, "right");

		myForm.add(frameTable);*/
		
		myForm.add(backTable);

	}

	public void commitCreation(IWContext iwc) {

		User newUser = null;


		String ssn = iwc.getParameter(ssnFieldParameterName);
		String primaryGroup = iwc.getParameter(primaryGroupFieldParameterName);
		primaryGroup = primaryGroup.substring(primaryGroup.lastIndexOf("_")+1);
		Integer primaryGroupId = null;
		if (primaryGroup != null && !primaryGroup.equals("")) {
			primaryGroupId = new Integer(primaryGroup);
		}



		
			String fullName = iwc.getParameter(fullNameFieldParameterName);
			try {
				newUser = getUserBusiness(iwc).createUserByPersonalIDIfDoesNotExist(fullName,ssn,null,null);
			Group group = getGroupBusiness(iwc).getGroupByGroupID(primaryGroupId.intValue());
			group.addGroup(newUser);
			newUser.setPrimaryGroupID(primaryGroupId);
			newUser.store();

		

		if (iwc.getParameter(goToPropertiesFieldParameterName) != null) {
			Link gotoLink = new Link();
			gotoLink.setWindowToOpen(UserPropertyWindow.class);
			gotoLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, newUser.getPrimaryKey().toString());
			String script = "window.opener." + gotoLink.getWindowToOpenCallingScript(iwc);
			setOnLoad(script);
		}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			catch (CreateException e) {
				e.printStackTrace();
			}
			catch (FinderException e) {
				e.printStackTrace();
			}
	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		IWBundle iwb = getBundle(iwc);
	  	
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		addTitle(iwrb.getLocalizedString("create_new_user", "Create a new User"), IWConstants.BUILDER_FONT_STYLE_TITLE);

		myForm = new Form();
		add(myForm,iwc);
		initializeTexts();
		initializeFields(iwc);
		lineUpElements(iwc);
		
		String submit = iwc.getParameter("submit");
		
		//added to set a new image for the groupChooser
		Image groupChooseImage = iwb.getImage("magnify.gif");
		primaryGroupField.setChooseButtonImage(groupChooseImage);

		
		selectedGroupId = iwc.getParameter(PARAMETERSTRING_GROUP_ID);
		if (selectedGroupId != null) {
			primaryGroupField.setSelectedNode(new GroupTreeNode(this.getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(selectedGroupId))));
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
		if (userBiz == null) {
			try {
				userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return userBiz;
	}
	
	public GroupBusiness getGroupBusiness(IWApplicationContext iwc) {
		if (groupBiz == null) {
			try {
				groupBiz = (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return groupBiz;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}