package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.FinderException;
import javax.swing.event.ChangeListener;
import javax.transaction.TransactionManager;
import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.StatefullPresentationImplHandler;
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
import com.idega.transaction.IdegaTransactionManager;
import com.idega.user.app.UserApplicationMenuAreaPS;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;

/**
 * Title: User Description: Copyright: Copyright (c) 2001 Company: idega.is
 * 
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur
 *         �g�st S�mundsson</a>
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
	/*
	 * private CheckBox generateLoginField; private CheckBox
	 * generatePasswordField; private CheckBox mustChangePasswordField; private
	 * CheckBox cannotChangePasswordField; private CheckBox
	 * passwordNeverExpiresField; private CheckBox disableAccountField;
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
	private StatefullPresentationImplHandler _stateHandler = null;
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
	private boolean formNotComplete = false;
	private String inputTextStyle = "text";
	private String backgroundTableStyle = "back";
	private String mainTableStyle = "main";
	private String bannerTableStyle = "banner";

	public CreateUser() {
		super();
		this._stateHandler = new StatefullPresentationImplHandler();
		this._stateHandler.setPresentationStateClass(CreateGroupWindowPS.class);
		setHeight(300);
		setWidth(330);
		setScrollbar(false);
		setResizable(true);
	}

	protected void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.fullNameText = new Text(iwrb.getLocalizedString(fullNameFieldParameterName, "Name") + ":");
		this.fullNameText.setBold();
		this.ssnText = new Text(iwrb.getLocalizedString(ssnFieldParameterName, "Personal ID (SSN)") + ":");
		this.ssnText.setBold();
		this.primaryGroupText = new Text(iwrb.getLocalizedString(primaryGroupFieldParameterName, "Primarygroup") + ":");
		this.primaryGroupText.setBold();
	}

	protected void initializeFields(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.fullNameField = new TextInput(fullNameFieldParameterName);
		this.fullNameField.setLength(20);
		// fullNameField.setWidth(Table.HUNDRED_PERCENT);
		this.fullNameField.setStyleClass("text");
		this.ssnField = new TextInput(ssnFieldParameterName);
		this.ssnField.setLength(20);
		this.ssnField.setMaxlength(12);
		this.ssnField.setStyleClass(this.inputTextStyle);
		this.ssnField.setAsPersonalID(iwc.getCurrentLocale(),iwrb.getLocalizedString("new_user.not_valid_ssn","The SSN that was entered is not valid"));
		this.goToPropertiesField = new HiddenInput(goToPropertiesFieldParameterName, "TRUE");
		this.primaryGroupField = new GroupChooser(primaryGroupFieldParameterName);
		this.primaryGroupField.setStyleClassName(this.inputTextStyle);
		this.primaryGroupField.setInputLength(17);
		this.help = getHelp(HELP_TEXT_KEY);
		this.okButton = new StyledButton(new SubmitButton(iwrb.getLocalizedString("save", "Save"),
				submitButtonParameterName, okButtonParameterValue));
		this.continueButton = new StyledButton(new SubmitButton(iwrb.getLocalizedString("yes", "Yes"),
				submitButtonParameterName, submitButtonParameterValue));
		this.cancelButton = new StyledButton(new CloseButton(iwrb.getLocalizedString("close", "Close")));
		this.backButton = new StyledButton(new BackButton(iwrb.getLocalizedString("back", "Back")));
	}

	public void lineUpElements(IWContext iwc) {
		this.mainTable = new Table();
		this.mainTable.setCellspacing(0);
		this.mainTable.setCellpadding(0);
		this.mainTable.setWidth(Table.HUNDRED_PERCENT);
		this.mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		this.mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
		this.mainTable.setHeight(2, 5);
		this.inputTable = new Table();
		this.inputTable.setStyleClass(this.mainTableStyle);
		this.inputTable.setWidth(Table.HUNDRED_PERCENT);
		this.inputTable.setCellpadding(0);
		this.inputTable.setCellspacing(12);
		this.inputTable.add(this.fullNameText, 1, 1);
		this.inputTable.add(Text.getBreak(), 1, 1);
		this.inputTable.add(this.fullNameField, 1, 1);
		this.inputTable.add(this.ssnText, 1, 2);
		this.inputTable.add(Text.getBreak(), 1, 2);
		this.inputTable.add(this.ssnField, 1, 2);
		this.inputTable.add(this.primaryGroupText, 1, 3);
		this.inputTable.add(Text.getBreak(), 1, 3);
		this.inputTable.add(this.primaryGroupField, 1, 3);
		this.inputTable.setHeight(4, 5);
		this.buttonTable = new Table();
		this.buttonTable.setCellpadding(0);
		this.buttonTable.setCellspacing(0);
		this.buttonTable.setWidth(2, "5");
		this.buttonTable.add(this.okButton, 1, 1);
		this.buttonTable.add(this.cancelButton, 3, 1);
		this.helpTable = new Table();
		this.helpTable.setCellpadding(0);
		this.helpTable.setCellspacing(0);
		this.helpTable.add(this.help, 1, 1);
		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setStyleClass(this.mainTableStyle);
		bottomTable.add(this.helpTable, 1, 1);
		bottomTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(this.buttonTable, 2, 1);
		this.mainTable.add(this.inputTable, 1, 1);
		this.mainTable.add(bottomTable, 1, 3);
		this.myForm.add(this.mainTable);
	}

	public void commitCreation(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		User newUser = null;
		Group group = null;
		Integer primaryGroupId = null;
		if (this.primaryGroup != null && !this.primaryGroup.equals("")) {
			TransactionManager transaction = IdegaTransactionManager.getInstance();
			try {
				// START A TRANSACTION!
				transaction.begin();
				primaryGroupId = new Integer(this.primaryGroup);
				if ((this.ssn != null || !this.ssn.equals("")) && (this.fullName == null || this.fullName.equals(""))) {
					try {
						newUser = getUserBusiness(iwc).getUser(this.ssn);
						this.fullName = newUser.getName();
					}
					catch (Exception e) {
						newUser = null;
					}
					if (newUser != null) {
						this.fullName = newUser.getName();
					}
					else {
						this.fullName = this.ssn;
					}
				}
				group = getGroupBusiness(iwc).getGroupByGroupID(primaryGroupId.intValue());
				if (iwc.getAccessController().hasEditPermissionFor(group, iwc)) {
					newUser = getUserBusiness(iwc).createUserByPersonalIDIfDoesNotExist(this.fullName, this.ssn, null, null);
					if (this.ssn == null || this.ssn.equals("")) {
						// added / so it won't clash with any real personal
						// id's
						newUser.setPersonalID("/"
								+ Integer.toString(((Integer) newUser.getPrimaryKey()).intValue()) + "/");
					}
										
					String error = getUserBusiness(iwc).isUserSuitedForGroup(newUser, group);
					if (error == null) {
						group.addGroup(newUser);
						if (newUser.getPrimaryGroupID() < 0) {
							newUser.setPrimaryGroupID(primaryGroupId);
						}
						newUser.store();
						
						getUserBusiness(iwc).callAllUserGroupPluginAfterUserCreateOrUpdateMethod(newUser, group);
						Link gotoLink = new Link();
						gotoLink.setWindowToOpen(UserPropertyWindow.class);
						gotoLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID,
								newUser.getPrimaryKey().toString());
						close();
						setOnLoad("window.opener.parent.frames['iwb_main'].location.reload()");
						String script = "window.opener." + gotoLink.getWindowToOpenCallingScript(iwc);
						setOnLoad(script);
					}
					else {
						setAlertOnLoad(error);
						this.ssnField.setContent(this.ssn);
						this.fullNameField.setContent(this.fullName);
					}
				}
				else {
					setAlertOnLoad(iwrb.getLocalizedString("new_user.no_edit_permission_for_parent_group",
							"You cannot add the user to this group because you do not have edit permission to it."));
					this.ssnField.setContent(this.ssn);
					this.fullNameField.setContent(this.fullName);
				}
				transaction.commit();
			}// try ends
			catch (Exception e) {
				e.printStackTrace();
				try {
					transaction.rollback();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
				String msg = e.getMessage();
				
				String errorMessage = iwrb.getLocalizedString(
						"new_user.transaction_rollback",
						"User could not be created/added because of the error: ")
						+ msg
						+ iwrb.getLocalizedString("new_user.try_again"," Please try again or contact the system administrator if you think it is a server error.");
				
				
				setAlertOnLoad(errorMessage);
				
				this.ssnField.setContent(this.ssn);
				this.fullNameField.setContent(this.fullName);
				// add the parent group also?
			}
		}
		else {
			setAlertOnLoad(iwrb.getLocalizedString("new_user.group_required", "Group must be selected"));
			this.ssnField.setContent(this.ssn);
			this.fullNameField.setContent(this.fullName);
		}
	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		IWBundle iwb = getBundle(iwc);
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		addTitle(iwrb.getLocalizedString("create_new_user", "Create a new User"), TITLE_STYLECLASS);
		super.setTitle(iwrb.getLocalizedString("create_new_user", "Create a new User"));
		this.myForm = new Form();
		add(this.myForm, iwc);
		initializeTexts();
		initializeFields(iwc);
		lineUpElements(iwc);
		// added to set a new image for the groupChooser
		Image groupChooseImage = iwb.getImage("magnify.gif");
		this.primaryGroupField.setChooseButtonImage(groupChooseImage);
		this.selectedGroupId = iwc.getParameter(PARAMETERSTRING_GROUP_ID);
		if (this.selectedGroupId == null) {
			this.selectedGroupId = getSelectedGroupID(iwc);
		}
		if (this.selectedGroupId != null) {
			this.primaryGroupField.setSelectedNode(new GroupTreeNode(this.getGroupBusiness(iwc).getGroupByGroupID(
					Integer.parseInt(this.selectedGroupId))));
			this.myForm.add(new HiddenInput(PARAMETERSTRING_GROUP_ID, this.selectedGroupId));
		}
		String submit = iwc.getParameter("submit");
		this.ssn = iwc.getParameter(ssnFieldParameterName);
		this.fullName = iwc.getParameter(fullNameFieldParameterName);
		this.primaryGroup = iwc.getParameter(primaryGroupFieldParameterName);
		if (this.primaryGroup == null || this.primaryGroup.equals("")) {
			this.primaryGroup = "";
		}
		else {
			this.primaryGroup = this.primaryGroup.substring(this.primaryGroup.lastIndexOf("_") + 1);
		}
		if (this.ssn == null || this.ssn.equals("") || this.fullName == null || this.fullName.equals("")) {
			this.formNotComplete = true;
		}
		if (submit != null) {
			// is addressed if the okButton is pressed and the user has:
			// 1. not entered anything in the form,
			// 2. entered only the name
			// 3. entered only the social security number
			if (submit.equals("ok") && this.formNotComplete) {
				// is addressed if both name and social security number are
				// empty
				if ((this.ssn == null || this.ssn.equals("")) && (this.fullName == null || this.fullName.equals(""))) {
					setAlertOnLoad(iwrb.getLocalizedString("new_user.ssn_or_fullName_required",
							"Personal ID or name is required"));
				}
				else if (this.ssn == null || this.ssn.equals("") && (this.fullName != null || !this.fullName.equals(""))) {
					// is addressed if only the name is entered
					this.inputTable.add(iwrb.getLocalizedString("new_user.ssn_warning",
							"You have selected to create a user with no Personal ID, do you want to continue?"), 1, 4);
					this.fullNameField.setContent(this.fullName);
					this.formNotComplete = false;
					this.buttonTable.remove(this.okButton);
					this.buttonTable.add(this.continueButton, 1, 1);
					if (this.primaryGroup != null || !this.primaryGroup.equals("")) {
						Integer primaryGroupId = new Integer(this.primaryGroup);
						this.primaryGroupField.setSelectedGroup(this.primaryGroup, getGroupBusiness(iwc).getGroupByGroupID(
								primaryGroupId.intValue()).getName());
					}
				}
				// is addressed if the only the social security number is
				// entered
				else if ((this.ssn != null || !this.ssn.equals("")) && (this.fullName == null || this.fullName.equals(""))) {
					try {
						// todo fill in the name field if found by ssn
						User user = getUserBusiness(iwc).getUser(this.ssn);
						this.fullName = user.getName();
						this.fullNameField.setContent(this.fullName);
						this.fullNameField.setDisabled(true);
						this.inputTable.add(
								iwrb.getLocalizedString("new_user.user_found",
										"You are adding a user that exists in the database to the selected group, do you want to continue?"),
								1, 4);
					}
					catch (FinderException e) {
						//
						this.inputTable.add(iwrb.getLocalizedString("new_user.fullName_warning",
								"You have selected to create a user with no name, do you want to continue?"), 1, 4);
					}
					this.ssnField.setContent(this.ssn);
					this.formNotComplete = false;
					this.buttonTable.remove(this.okButton);
					this.buttonTable.add(this.continueButton, 1, 1);
					if (this.primaryGroup != null || !this.primaryGroup.equals("")) {
						Integer primaryGroupId = new Integer(this.primaryGroup);
						this.primaryGroupField.setSelectedGroup(this.primaryGroup, getGroupBusiness(iwc).getGroupByGroupID(
								primaryGroupId.intValue()).getName());
					}
				}
			}
			// is addressed if both name and social security number are entered
			else if (submit.equals("ok") && !this.formNotComplete) {
				commitCreation(iwc);
			}
			// is addressed if the user submits entering only ssn or name
			// then name is set = ssn or ssn set = the primary key of the user
			// (see commitCreation(iwc))
			else if (submit.equals("submit")) {
				commitCreation(iwc);
			}
			else if (submit.equals("cancel")) {
				close();
			}
		}
	}

	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (this.userBiz == null) {
			try {
				this.userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return this.userBiz;
	}

	public GroupBusiness getGroupBusiness(IWApplicationContext iwc) {
		if (this.groupBiz == null) {
			try {
				this.groupBiz = (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return this.groupBiz;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	private String getSelectedGroupID(IWContext iwc) {
		String selectedGroupProviderStateId = "";
		String tempSelectedGroupID = null;
		if (iwc.isParameterSet(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY)) {
			selectedGroupProviderStateId = iwc.getParameter(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY);
		}
		IWPresentationState state = this.getPresentationState(iwc);
		// add action listener
		addActionListener((IWActionListener) state);
		IWStateMachine stateMachine;
		// add all change listeners
		Collection changeListeners;
		try {
			stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
			changeListeners = stateMachine.getAllChangeListeners();
			// try to get the selected group
			if (selectedGroupProviderStateId != null) {
				UserApplicationMenuAreaPS groupProviderState = (UserApplicationMenuAreaPS) stateMachine.getStateFor(
						selectedGroupProviderStateId, UserApplicationMenuAreaPS.class);
				Integer tempID = groupProviderState.getSelectedGroupId();
				if (tempID != null) {
					tempSelectedGroupID = tempID.toString();
				}
			}
		}
		catch (RemoteException e) {
			changeListeners = new ArrayList();
		}
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			state.addChangeListener((ChangeListener) iterator.next());
		}
		return tempSelectedGroupID;
	}

	public IWPresentationState getPresentationState(IWUserContext iwuc) {
		return this._stateHandler.getPresentationState(this, iwuc);
	}
}