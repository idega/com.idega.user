package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.StyledButton;
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
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
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

	private StyledButton okButton;
	private StyledButton continueButton;
	private StyledButton cancelButton;
	private StyledButton backButton;
	
	private Help help; 
	private static final String HELP_TEXT_KEY = "create_user";

	private Form myForm;
	private Table mainTable; 
	private Table inputTable;
	private Table buttonTable;
	private Table helpTable;
	private Table warningTable;

	private String selectedGroupId = null;

	public static String PARAMETERSTRING_GROUP_ID = "default_group";

	public static String okButtonParameterValue = "ok";
	public static String submitButtonParameterValue = "submit";
	public static String cancelButtonParameterValue = "cancel";
	public static String submitButtonParameterName = "submit";

	public static String fullNameFieldParameterName = "fullName";
	public static String userLoginFieldParameterName = "login";
	public static String passwordFieldParameterName = "password";
	public static String confirmPasswordFieldParameterName = "confirmPassword";
	public static String ssnFieldParameterName = "ssn";
	
	private String ssn = null;
	private String fullName = null;
	private String primaryGroup = null;
	
	public static String goToPropertiesFieldParameterName = "gotoProperties";
	public static String primaryGroupFieldParameterName = "primarygroup";

	private UserBusiness userBiz;
	
	private boolean ssnWarningDisplay = false;
	private boolean fullNameWarningDisplay = false;
	private boolean formNotComplete = false;
	
	private String inputTextStyle = "text";
	private String backgroundTableStyle = "back";
	private String mainTableStyle = "main";
	private String bannerTableStyle = "banner";

	public CreateUser() {
		super();
		setHeight(300);
		setWidth(240);
		setScrollbar(false);
		setResizable(true);
	}

	protected void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

  		fullNameText = new Text(iwrb.getLocalizedString(fullNameFieldParameterName,"Name") + ":");
  		fullNameText.setBold();
		
  		ssnText = new Text(iwrb.getLocalizedString(ssnFieldParameterName,"Personal ID (SSN)") + ":");
		ssnText.setBold();
		
		primaryGroupText = new Text(iwrb.getLocalizedString(primaryGroupFieldParameterName,"Primarygroup") + ":");
		primaryGroupText.setBold();
	}

	protected void initializeFields(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		fullNameField = new TextInput(fullNameFieldParameterName);
		fullNameField.setLength(20);
		fullNameField.setStyleClass("text");

		ssnField = new TextInput(ssnFieldParameterName);
		ssnField.setLength(20);
		ssnField.setMaxlength(12);
		ssnField.setStyleClass(inputTextStyle);

		goToPropertiesField = new HiddenInput(goToPropertiesFieldParameterName,"TRUE");
		
		primaryGroupField = new GroupChooser(primaryGroupFieldParameterName);
		primaryGroupField.setStyleClassName(inputTextStyle);
		primaryGroupField.setInputLength(17);

		help = getHelp(HELP_TEXT_KEY);
		
		okButton = new StyledButton(new SubmitButton(iwrb.getLocalizedString("save", "Save"), submitButtonParameterName, okButtonParameterValue));
    continueButton = new StyledButton(new SubmitButton(iwrb.getLocalizedString("yes", "Yes"), submitButtonParameterName, submitButtonParameterValue));
		cancelButton = new StyledButton(new CloseButton(iwrb.getLocalizedString("close", "Close")));
		backButton = new StyledButton(new BackButton(iwrb.getLocalizedString("back", "Back")));
	}

	public void lineUpElements(IWContext iwc) {
		mainTable = new Table();
		mainTable.setCellspacing(0);
		mainTable.setCellpadding(0);
		mainTable.setWidth(Table.HUNDRED_PERCENT);
		mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
		mainTable.setHeight(2, 5);

		inputTable = new Table();
		inputTable.setStyleClass(mainTableStyle);
		inputTable.setWidth(Table.HUNDRED_PERCENT);
		inputTable.setCellpadding(0);
		inputTable.setCellspacing(12);		
		inputTable.add(fullNameText,1,1);
		inputTable.add(Text.getBreak(), 1, 1);
		inputTable.add(fullNameField,1,1);
		inputTable.add(ssnText,1,2);
		inputTable.add(Text.getBreak(), 1, 2);
		inputTable.add(ssnField,1,2);
		inputTable.add(primaryGroupText, 1, 3);
		inputTable.add(Text.getBreak(), 1, 3);
		inputTable.add(primaryGroupField, 1, 3);
		inputTable.setHeight(4, 5);
	
		buttonTable = new Table();
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setWidth(2, "5");
		buttonTable.add(okButton, 1, 1);
		buttonTable.add(cancelButton, 3, 1);			
		
		helpTable = new Table();
		helpTable.setCellpadding(0);
		helpTable.setCellspacing(0);
		helpTable.add(help,1,1);
		
		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setStyleClass(mainTableStyle);
		bottomTable.add(helpTable,1,1);
		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(buttonTable,2,1);

		mainTable.add(inputTable, 1,1);
		mainTable.add(bottomTable,1,3);
		
		myForm.add(mainTable);
	}

	public void commitCreation(IWContext iwc) {
		
		IWResourceBundle iwrb = getResourceBundle(iwc);

		User newUser = null;
		Group group = null;
								
		Integer primaryGroupId = null;
			
		try {			
			if (primaryGroup != null && !primaryGroup.equals("")) {
				
				primaryGroupId = new Integer(primaryGroup);
				
				if((ssn != null || !ssn.equals("")) && (fullName == null || fullName.equals(""))) {
					try { 
						newUser = getUserBusiness(iwc).getUser(ssn);
					}
					catch (Exception e) {
						newUser = null;
					}
					
					if(newUser != null) {
						fullName = newUser.getName();
					}
					else {
						fullName = ssn; 
					}
				}
				newUser = getUserBusiness(iwc).createUserByPersonalIDIfDoesNotExist(fullName,ssn,null,null);					
				group = getGroupBusiness(iwc).getGroupByGroupID(primaryGroupId.intValue());
				group.addGroup(newUser);
				newUser.setPrimaryGroupID(primaryGroupId);
				newUser.store();	
				Link gotoLink = new Link();
				gotoLink.setWindowToOpen(UserPropertyWindow.class);
				gotoLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, newUser.getPrimaryKey().toString());
				close();
				setOnLoad("window.opener.parent.frames['iwb_main'].location.reload()");
				String script = "window.opener." + gotoLink.getWindowToOpenCallingScript(iwc);
				setOnLoad(script);						
			}
			else {
				setAlertOnLoad(iwrb.getLocalizedString("new_user.group_required","Group must be selected"));
				ssnField.setContent(ssn);
				fullNameField.setContent(fullName);
			}
				
			if(ssn == null || ssn.equals("")) {
				newUser.setPersonalID(Integer.toString(((Integer)newUser.getPrimaryKey()).intValue()));				
				newUser.store();
			}
			
			}//try ends
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
		addTitle("." + iwrb.getLocalizedString("create_new_user", "Create a new User"), TITLE_STYLECLASS);
		super.setTitle(iwrb.getLocalizedString("create_new_user", "Create a new User"));

		myForm = new Form();
		add(myForm,iwc);
		initializeTexts();
		initializeFields(iwc);
		lineUpElements(iwc);
						
		//added to set a new image for the groupChooser
		Image groupChooseImage = iwb.getImage("magnify.gif");
		primaryGroupField.setChooseButtonImage(groupChooseImage);

		selectedGroupId = iwc.getParameter(PARAMETERSTRING_GROUP_ID);
		if (selectedGroupId != null) {
			primaryGroupField.setSelectedNode(new GroupTreeNode(this.getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(selectedGroupId))));
			myForm.add(new HiddenInput(PARAMETERSTRING_GROUP_ID, selectedGroupId));
		}
		
		String submit = iwc.getParameter("submit");
		ssn = iwc.getParameter(ssnFieldParameterName);
		fullName = iwc.getParameter(fullNameFieldParameterName);
		primaryGroup = iwc.getParameter(primaryGroupFieldParameterName);
		
		if(primaryGroup == null || primaryGroup.equals(""))
			primaryGroup = "";
		else
			primaryGroup = primaryGroup.substring(primaryGroup.lastIndexOf("_")+1);
	

		if(ssn == null || ssn.equals("") || fullName == null || fullName.equals("")) 
			formNotComplete = true;			
					
			if(submit != null) {
				//is addressed if the okButton is pressed and the user has:
				//1. not entered anything in the form,
				//2. entered only the name
				//3. entered only the social security number
				if (submit.equals("ok") && formNotComplete) {
					//is addressed if both name and social security number are empty
					if((ssn == null || ssn.equals("")) && (fullName == null || fullName.equals("")))
						setAlertOnLoad(iwrb.getLocalizedString("new_user.ssn_or_fullName_required","Personal ID or name is required"));
					//is addressed if only the name is entered
					else if(ssn == null || ssn.equals("") && (fullName != null || !fullName.equals(""))) {
						inputTable.add(iwrb.getLocalizedString("new_user.ssn_warning","You have selected to create a user with no Personal ID, do you want to continue?"),1,5);
						ssnWarningDisplay = true;
						fullNameField.setContent(fullName);
						formNotComplete = false;
						buttonTable.remove(okButton);
						buttonTable.add(continueButton,1,1);
						if(primaryGroup != null || !primaryGroup.equals("")) {
							Integer primaryGroupId = new Integer(primaryGroup);
							primaryGroupField.setSelectedGroup(primaryGroup,getGroupBusiness(iwc).getGroupByGroupID(primaryGroupId.intValue()).getName());
						}
													
					}
					//is addressed if the only the social security number is entered
					else if((ssn != null || !ssn.equals("")) && (fullName == null || fullName.equals(""))) {
						inputTable.add(iwrb.getLocalizedString("new_user.fullName_warning","You have selected to create a user with no name, do you want to continue?"),1,5);
						fullNameWarningDisplay = true;
						ssnField.setContent(ssn);
						formNotComplete = false;
						buttonTable.remove(okButton);
						buttonTable.add(continueButton,1,1);
						if(primaryGroup != null || !primaryGroup.equals("")) {
							Integer primaryGroupId = new Integer(primaryGroup);
							primaryGroupField.setSelectedGroup(primaryGroup,getGroupBusiness(iwc).getGroupByGroupID(primaryGroupId.intValue()).getName());	
								
						}				
					}		
				}
				//is addressed if both name and social security number are entered
				else if (submit.equals("ok") && !formNotComplete) {
					commitCreation(iwc);		
				}
				//is addressed if the user submits entering only ssn or name
				//then name is set = ssn or ssn set = the primary key of the user (see commitCreation(iwc))
				else if (submit.equals("submit")) {
					commitCreation(iwc);
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