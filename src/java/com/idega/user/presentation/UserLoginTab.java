package com.idega.user.presentation;

import java.text.MessageFormat;

import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginInfo;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * Title:        UserLoginTab
 * Description:	A tab for creating or modifying a users login information
 * Copyright:    Copyright (c) 2001 
 * Company:      Idega Software
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.5
 */
public class UserLoginTab extends UserTab {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private static final String TAB_NAME = "usr_log_tab_name";
	private static final String DEFAULT_TAB_NAME = "Login";
	private static final String HELP_TEXT_KEY = "user_login_tab";
	private Text userLoginText;
	private TextInput userLoginField;
	private Table errorMessageTable;
	private Text errorText;
	private Text passwordText;
	private Text confirmPasswordText;
	private PasswordInput passwordField;
	private PasswordInput confirmPasswordField;
	private Text mustChangePasswordText;
//	private Text cannotChangePasswordText;
//	private Text passwordNeverExpiresText;
	private Text disableAccountText;
	private CheckBox mustChangePasswordField;
//	private CheckBox cannotChangePasswordField;
//	private CheckBox passwordNeverExpiresField;
	private CheckBox disableAccountField;
	public static String _PARAM_USER_LOGIN = "login";
	public static String _PARAM_PASSWORD = "password";
	public static String _PARAM_CONFIRM_PASSWORD = "confirmPassword";
	public static String _PARAM_MUST_CHANGE_PASSWORD = "mustChange";
//	public static String _PARAM_CANNOT_CHANGE_PASSWORD = "cannotChange";
//	public static String _PARAM_PASSWORD_NEVER_EXPIRES = "neverExpires";
	public static String _PARAM_DISABLE_ACCOUNT = "disableAccount";

	public UserLoginTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		//		super.setName("Login");
	}

	public void init() {
		this.errorMessageTable = new Table();
		this.errorText = new Text();
		this.errorText.setFontColor("red");
		super.init();
	}

	public void initFieldContents() {
		try {
			LoginTable lTable = LoginDBHandler.getUserLogin(getUserId());
			LoginInfo lInfo = null;
			if (lTable != null) {
				lInfo = LoginDBHandler.getLoginInfo(lTable.getID());
				this.fieldValues.put(_PARAM_USER_LOGIN, lTable.getUserLogin());
			}
			if (lInfo != null) {
				this.fieldValues.put(_PARAM_MUST_CHANGE_PASSWORD, new Boolean(lInfo.getChangeNextTime()));
//				this.fieldValues.put(_PARAM_CANNOT_CHANGE_PASSWORD, new Boolean(!lInfo.getAllowedToChange()));
				//this.fieldValues.put(_PARAM_PASSWORD_NEVER_EXPIRES, new Boolean(lInfo.getPasswordExpires()));
				this.fieldValues.put(_PARAM_DISABLE_ACCOUNT, new Boolean(!lInfo.getAccountEnabled()));
			}
			this.updateFieldsDisplayStatus();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("UserLoginTab: error in initFieldContents() for user: " + this.getUserId());
		}
	}

	public void updateFieldsDisplayStatus() {
		this.userLoginField.setContent((String) this.fieldValues.get(_PARAM_USER_LOGIN));
		this.passwordField.setContent((String) this.fieldValues.get(_PARAM_PASSWORD));
		this.confirmPasswordField.setContent((String) this.fieldValues.get(_PARAM_PASSWORD));
		this.mustChangePasswordField.setChecked(((Boolean) this.fieldValues.get(_PARAM_MUST_CHANGE_PASSWORD)).booleanValue());
//		this.cannotChangePasswordField.setChecked(((Boolean) this.fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue());
//		this.passwordNeverExpiresField.setChecked(((Boolean) this.fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES)).booleanValue());
		this.disableAccountField.setChecked(((Boolean) this.fieldValues.get(_PARAM_DISABLE_ACCOUNT)).booleanValue());
	}

	public void initializeFields() {
		this.userLoginField = new TextInput(_PARAM_USER_LOGIN);
		this.userLoginField.setLength(32);
		this.passwordField = new PasswordInput(_PARAM_PASSWORD);
		this.passwordField.setLength(32);
		this.confirmPasswordField = new PasswordInput(_PARAM_CONFIRM_PASSWORD);
		this.confirmPasswordField.setLength(32);
		this.mustChangePasswordField = new CheckBox(_PARAM_MUST_CHANGE_PASSWORD);
		this.mustChangePasswordField.setHeight("10");
		this.mustChangePasswordField.setWidth("10");
//		this.cannotChangePasswordField = new CheckBox(_PARAM_CANNOT_CHANGE_PASSWORD);
//		this.cannotChangePasswordField.setHeight("10");
//		this.cannotChangePasswordField.setWidth("10");
//		this.passwordNeverExpiresField = new CheckBox(_PARAM_PASSWORD_NEVER_EXPIRES);
//		this.passwordNeverExpiresField.setHeight("10");
//		this.passwordNeverExpiresField.setWidth("10");
		this.disableAccountField = new CheckBox(_PARAM_DISABLE_ACCOUNT);
		this.disableAccountField.setHeight("10");
		this.disableAccountField.setWidth("10");
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.userLoginText = new Text(iwrb.getLocalizedString(_PARAM_USER_LOGIN, "User login"));
		this.userLoginText.setBold();
		this.passwordText = new Text(iwrb.getLocalizedString(_PARAM_PASSWORD, "New password"));
		this.passwordText.setBold();
		this.confirmPasswordText = new Text(iwrb.getLocalizedString(_PARAM_CONFIRM_PASSWORD, "Confirm password"));
		this.confirmPasswordText.setBold();
		this.mustChangePasswordText = new Text(iwrb.getLocalizedString(_PARAM_MUST_CHANGE_PASSWORD,
				"User must change password at next login"));
		this.mustChangePasswordText.setBold();
//		this.cannotChangePasswordText = new Text(iwrb.getLocalizedString(_PARAM_CANNOT_CHANGE_PASSWORD,"User cannot change password"));
//		this.cannotChangePasswordText.setBold();
//		this.passwordNeverExpiresText = new Text(iwrb.getLocalizedString(_PARAM_PASSWORD_NEVER_EXPIRES,"Password never expires"));
//		this.passwordNeverExpiresText.setBold();
		this.disableAccountText = new Text(iwrb.getLocalizedString(_PARAM_DISABLE_ACCOUNT, "Account is disabled"));
		this.disableAccountText.setBold();
	}

	public boolean store(IWContext iwc) {
		//get all the params from the fields Map
		IWResourceBundle iwrb = getResourceBundle(iwc);
		boolean updateLoginTable = true;
		String newLoginName = (String) this.fieldValues.get(_PARAM_USER_LOGIN);
		String passw = ((String) this.fieldValues.get(_PARAM_PASSWORD));
		//String confirmedpassw = ((String) this.fieldValues.get(_PARAM_PASSWORD));
		Boolean mustChangePassw = ((Boolean) this.fieldValues.get(_PARAM_MUST_CHANGE_PASSWORD));
		Boolean accountEnabled = ((Boolean) this.fieldValues.get(_PARAM_DISABLE_ACCOUNT)).booleanValue() ? Boolean.FALSE: Boolean.TRUE;
//		Boolean canChangePassw = ((Boolean) this.fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue() ? Boolean.FALSE: Boolean.TRUE;
//		Boolean passwExpires = ((Boolean) this.fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES));.booleanValue();

		//Check if the current user is allowed to change the password/username and if the new username (if changed) is not already taken
		try {
			//collect method already checked if the password and the confirmed one matched
			if (stringIsNotNullOrEmpty(passw)) {
				if (stringIsNotNullOrEmpty(newLoginName)) {
					LoginTable userLoginTable = LoginDBHandler.getUserLogin(this.getUserId());
					String oldLogin = null;
					if (userLoginTable != null) {
						oldLogin = userLoginTable.getUserLogin();
//						Check if the current user is allowed to change the password/username, only Admin,the user himself and the changedByUser or a member of changedByGroup can.
						checkToSeeIfCurrentUserChangeTheLogin(iwc, iwrb, userLoginTable);
					}
					//if nobody has created a username + password for the user, we don't care who's doing it. 
					//The current user and his primary group will be saved in the logintable record if no errors occur.
					
					//only adds an error message if the new login name is taken!
					checkIfLoginIsTaken(iwrb, newLoginName, oldLogin);
				}
				else {
					this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginNotValid", "login not valid"));
				}
			}
			else {
				updateLoginTable = false;
			}
		}
		catch (Exception ex) {
			this.addErrorMessage(ex.getMessage());
		}
		
		
		if (someErrors()) {
			presentErrorMessage(this.clearErrorMessages());
			return false;
		}
		else {
			this.errorMessageTable.empty();
			return saveLoginChanges(updateLoginTable, newLoginName, passw, mustChangePassw, accountEnabled);
		}
	}

	/**
	 * @param iwc
	 * @param iwrb
	 * @param userLoginTable
	 */
	protected void checkToSeeIfCurrentUserChangeTheLogin(IWContext iwc, IWResourceBundle iwrb, LoginTable userLoginTable) {
		//the admin can do whatever he wants!
		if(!iwc.isSuperAdmin()){
			//the user can change his own username and password of course
			int currentUserId = iwc.getCurrentUserId();
			if(! (currentUserId==this.getUserId()) ){
				//hmmm it's not the admin and not the user himself
				//THEN we only let the person change the users username and/or password
				//if he/she is the same person or is in the same group as the person's primary group that last modified the username+password
				int lastChangerUserId = userLoginTable.getChangedByUserId();
				if( (lastChangerUserId!=-1) && !(lastChangerUserId==currentUserId) ){
					//he's not the last changer, perhaps he is in the same group though!
					Group lastChangedByGroup = userLoginTable.getChangedByGroup();
					User chUser = userLoginTable.getChangedByUser();
					User user = iwc.getCurrentUser();
					String changerName = chUser.getName();
					String pin = chUser.getPersonalID();
					//todo fix if the group is null?
					if(lastChangedByGroup!=null){
						if(!lastChangedByGroup.hasRelationTo(user)){
							//this user CANNOT change the username and password, he's not the admin, the user himself nor the last changer or even in the last changers primary group!
							//show error!
							String changerGroupName = userLoginTable.getChangedByGroup().getName();
							Object[] arguments = {changerName,pin,changerGroupName};
							
							String formatted = MessageFormat.format(iwrb.getLocalizedString("usr_log_changing_login_not_allowed_with_groupname", "You cannot change this users login! Only the administrator, the user himself, {0} (personal id : {1}) or someone from the group {2} can."), arguments);
							this.addErrorMessage(formatted);
						}
						//else he's in the group, he can change stuff
					}
					else{
						//show error!
						
						Object[] arguments = {changerName,pin};
						String formatted = MessageFormat.format(iwrb.getLocalizedString("usr_log_changing_login_not_allowed", "You cannot change this users login! Only the administrator, the user himself or the user {0} (personal id : {1}) can."), arguments);
						this.addErrorMessage(formatted);
					}
				
				}
				//else we don't care, its the first time of change or the user is the last changer, the LoginDBHandler will save the current user as the lastChangedBy user...	
			}	
		}
	}

	/**
	 * @param stringToCheck
	 * @return
	 */
	protected boolean stringIsNotNullOrEmpty(String stringToCheck) {
		return stringToCheck != null && !stringToCheck.equals("");
	}

	/**
	 * @param iwrb
	 * @param login
	 * @param oldLogin
	 */
	protected void checkIfLoginIsTaken(IWResourceBundle iwrb, String login, String oldLogin) {
		boolean inUse = LoginDBHandler.isLoginInUse(login);
		if (oldLogin != null) {
			if (inUse && !oldLogin.equals(login)) {
				this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse", "login in use"));
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_USER_LOGIN, login);
			}
		}
		else {
			if (inUse) {
				this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse", "login in use"));
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_USER_LOGIN, login);
			}
		}
	}

	/**
	 * @param updateLoginTable
	 * @param login
	 * @param passw
	 * @param mustChangePassw
	 * @param accountEnabled
	 * @return
	 */
	protected boolean saveLoginChanges(boolean updateLoginTable, String login, String passw, Boolean mustChangePassw, Boolean accountEnabled) {
		try {
			LoginTable loginTable = LoginDBHandler.getUserLogin(this.getUserId());
			if (loginTable != null) {
				if (updateLoginTable) {
					LoginDBHandler.updateLogin(this.getUserId(), login, passw);
				}
				//removed password expires
				//LoginDBHandler.updateLoginInfo(loginTable, accountEnabled, IWTimestamp.RightNow(), 5000,passwExpires, canChangePassw, mustChangePassw, null);
				LoginDBHandler.updateLoginInfo(loginTable.getID(), accountEnabled, IWTimestamp.RightNow(), 5000,Boolean.FALSE, Boolean.TRUE, mustChangePassw, null);
			}
			else if (updateLoginTable) {
				LoginDBHandler.createLogin(this.getUserId(), login, passw, accountEnabled, IWTimestamp.RightNow(),5000, Boolean.FALSE, Boolean.TRUE, mustChangePassw, null);
			}
			else {
				if(login!=null && !"".equals(login) && passw!=null && !"".equals(passw)){
					LoginDBHandler.createLogin(this.getUserId(), login, passw);
				}
			}
			return true;
		}
		catch (Exception ex) {
			this.addErrorMessage(ex.getMessage());
			presentErrorMessage(this.clearErrorMessages());
			return false;
		}
	}

	public void lineUpFields() {
		Table table = new Table();
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setBorder(0);
		int row = 1;
		table.add(this.userLoginText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.userLoginField, 1, row++);
		table.add(this.passwordText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.passwordField, 1, row++);
		table.add(this.confirmPasswordText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.confirmPasswordField, 1, row++);
		row++;
		table.mergeCells(1, row, 2, row);
		table.add(this.mustChangePasswordField, 1, row);
		table.add(this.mustChangePasswordText, 1, row++);
		table.mergeCells(1, row, 2, row);
//		table.add(this.cannotChangePasswordField, 1, row);
//		table.add(this.cannotChangePasswordText, 1, row++);
//		table.mergeCells(1, row, 2, row);
//		table.add(this.passwordNeverExpiresField, 1, row);
//		table.add(this.passwordNeverExpiresText, 1, row++);
		table.mergeCells(1, row, 2, row);
		table.add(this.disableAccountField, 1, row);
		table.add(this.disableAccountText, 1, row++);
		this.errorMessageTable.setHeight(1);
		this.errorMessageTable.setCellpadding(0);
		this.errorMessageTable.setCellspacing(0);
		table.mergeCells(1, row, 2, row);
		table.add(this.errorMessageTable, 1, row);
		this.add(table);
	}

	public void main(IWContext iwc) {
		if (getPanel() != null) {
			getPanel().addHelpButton(getHelpButton());
		}
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			String login = iwc.getParameter(UserLoginTab._PARAM_USER_LOGIN);
			String passw = iwc.getParameter(UserLoginTab._PARAM_PASSWORD);
			String confirmedpassw = iwc.getParameter(UserLoginTab._PARAM_CONFIRM_PASSWORD);
			String mustChangePassw = iwc.getParameter(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD);
//			String cannotChangePassw = iwc.getParameter(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD);
//			String passwExpires = iwc.getParameter(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES);
			String accountDisabled = iwc.getParameter(UserLoginTab._PARAM_DISABLE_ACCOUNT);
			if (((passw != null && !passw.equals("")) || ((confirmedpassw != null && !confirmedpassw.equals(""))))) {
				if (login != null && !login.equals("")) {
					LoginTable userLoginTable = LoginDBHandler.getUserLogin(this.getUserId());
					String oldLogin = null;
					if (userLoginTable != null) {
						oldLogin = userLoginTable.getUserLogin();
					}
					boolean inUse = LoginDBHandler.isLoginInUse(login);
					if (oldLogin != null) {
						if (inUse && !oldLogin.equals(login)) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse", "login in use"));
						}
						else {
							this.fieldValues.put(UserLoginTab._PARAM_USER_LOGIN, login);
						}
					}
					else {
						if (inUse) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse", "login in use"));
						}
						else {
							this.fieldValues.put(UserLoginTab._PARAM_USER_LOGIN, login);
						}
					}
				}
				else {
					this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginNotValid", "login not valid"));
				}
				if (passw != null && confirmedpassw != null && passw.equals(confirmedpassw)) {
					this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, passw);
					this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, confirmedpassw);
				}
				else {
					this.addErrorMessage(iwrb.getLocalizedString("usr_log_pwdNotSame",
							"password and confirmed password not valid or not the same"));
					this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, "");
					this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, "");
				}
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, "");
				this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, "");
			}
//			if (cannotChangePassw != null && mustChangePassw != null) {
//				this.addErrorMessage(iwrb.getLocalizedString("usr_log_pwdNotTwoCheck",
//						"'User must change password at next login' and 'User cannot change password' cannot both be checked"));
//				this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
//				this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
//			}
//			else {
			if (mustChangePassw != null) {
				this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
			}
//				if (cannotChangePassw != null) {
//					this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.TRUE);
//				}
//				else {
//					this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
//				}
//			}
//			if (passwExpires != null) {
//				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.TRUE);
//			}
//			else {
//				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
//			}
			if (accountDisabled != null) {
				this.fieldValues.put(UserLoginTab._PARAM_DISABLE_ACCOUNT, Boolean.TRUE);
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_DISABLE_ACCOUNT, Boolean.FALSE);
			}
			this.updateFieldsDisplayStatus();
			if (someErrors()) {
				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, "");
				this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, "");
				presentErrorMessage(this.clearErrorMessages());
				return false;
			}
			else {
				this.errorMessageTable.empty();
				return true;
			}
		}
		this.addErrorMessage("IWContext is null");
		if (someErrors()) {
			this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, "");
			this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, "");
			presentErrorMessage(this.clearErrorMessages());
			return false;
		}
		else {
			this.errorMessageTable.empty();
			return true;
		}
	}

	public void presentErrorMessage(String[] messages) {
		this.errorMessageTable.empty();
		if (messages != null) {
			for (int i = 0; i < messages.length; i++) {
				Text message = (Text) this.errorText.clone();
				message.setText("* " + messages[i] + Text.BREAK);
				this.errorMessageTable.add(message);
			}
		}
	}

	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle(UserConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
	}

	public void initializeFieldNames() {
		/**@todo: implement this com.idega.user.presentation.UserTab abstract method*/
	}

	public void initializeFieldValues() {
		this.fieldValues.put(UserLoginTab._PARAM_USER_LOGIN, "");
		this.fieldValues.put(UserLoginTab._PARAM_PASSWORD, "");
		this.fieldValues.put(UserLoginTab._PARAM_CONFIRM_PASSWORD, "");
		this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
//		this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
//		this.fieldValues.put(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
		this.fieldValues.put(UserLoginTab._PARAM_DISABLE_ACCOUNT, Boolean.FALSE);
		initFieldContents();
		this.updateFieldsDisplayStatus();
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}