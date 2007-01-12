package com.idega.user.presentation;

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
import com.idega.util.IWTimestamp;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
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
	private Text generatePasswordText;
	private Text mustChangePasswordText;
	private Text cannotChangePasswordText;
	private Text passwordNeverExpiresText;
	private Text disableAccountText;
	private CheckBox mustChangePasswordField;
	private CheckBox cannotChangePasswordField;
	private CheckBox passwordNeverExpiresField;
	private CheckBox disableAccountField;
	public static String _PARAM_USER_LOGIN = "login";
	public static String _PARAM_PASSWORD = "password";
	public static String _PARAM_CONFIRM_PASSWORD = "confirmPassword";
	public static String _PARAM_MUST_CHANGE_PASSWORD = "mustChange";
	public static String _PARAM_CANNOT_CHANGE_PASSWORD = "cannotChange";
	public static String _PARAM_PASSWORD_NEVER_EXPIRES = "neverExpires";
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
				this.fieldValues.put(_PARAM_CANNOT_CHANGE_PASSWORD, new Boolean(!lInfo.getAllowedToChange()));
				this.fieldValues.put(_PARAM_PASSWORD_NEVER_EXPIRES, new Boolean(lInfo.getPasswordExpires()));
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
		this.cannotChangePasswordField.setChecked(((Boolean) this.fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue());
		this.passwordNeverExpiresField.setChecked(((Boolean) this.fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES)).booleanValue());
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
		this.cannotChangePasswordField = new CheckBox(_PARAM_CANNOT_CHANGE_PASSWORD);
		this.cannotChangePasswordField.setHeight("10");
		this.cannotChangePasswordField.setWidth("10");
		this.passwordNeverExpiresField = new CheckBox(_PARAM_PASSWORD_NEVER_EXPIRES);
		this.passwordNeverExpiresField.setHeight("10");
		this.passwordNeverExpiresField.setWidth("10");
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
		this.cannotChangePasswordText = new Text(iwrb.getLocalizedString(_PARAM_CANNOT_CHANGE_PASSWORD,
				"User cannot change password"));
		this.cannotChangePasswordText.setBold();
		this.passwordNeverExpiresText = new Text(iwrb.getLocalizedString(_PARAM_PASSWORD_NEVER_EXPIRES,
				"Password never expires"));
		this.passwordNeverExpiresText.setBold();
		this.disableAccountText = new Text(iwrb.getLocalizedString(_PARAM_DISABLE_ACCOUNT, "Account is disabled"));
		this.disableAccountText.setBold();
	}

	public boolean store(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		boolean updateLoginTable = true;
		String login = (String) this.fieldValues.get(_PARAM_USER_LOGIN);
		String passw = ((String) this.fieldValues.get(_PARAM_PASSWORD));
		String confirmedpassw = ((String) this.fieldValues.get(_PARAM_PASSWORD));
		Boolean mustChangePassw = ((Boolean) this.fieldValues.get(_PARAM_MUST_CHANGE_PASSWORD));
		//.booleanValue();
		Boolean canChangePassw = ((Boolean) this.fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue() ? Boolean.FALSE
				: Boolean.TRUE;
		Boolean passwExpires = ((Boolean) this.fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES));
		//.booleanValue();
		Boolean accountEnabled = ((Boolean) this.fieldValues.get(_PARAM_DISABLE_ACCOUNT)).booleanValue() ? Boolean.FALSE
				: Boolean.TRUE;
		try {
			if (((passw != null && !passw.equals("")) && ((confirmedpassw != null && !confirmedpassw.equals(""))))) {
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
			try {
				LoginTable loginTable = LoginDBHandler.getUserLogin(this.getUserId());
				if (loginTable != null) {
					if (updateLoginTable) {
						LoginDBHandler.updateLogin(this.getUserId(), login, passw);
					}
					LoginDBHandler.updateLoginInfo(loginTable.getID(), accountEnabled, IWTimestamp.RightNow(), 5000,
							passwExpires, canChangePassw, mustChangePassw, null);
				}
				else if (updateLoginTable) {
					LoginDBHandler.createLogin(this.getUserId(), login, passw, accountEnabled, IWTimestamp.RightNow(),
							5000, passwExpires, canChangePassw, mustChangePassw, null);
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
		table.add(this.cannotChangePasswordField, 1, row);
		table.add(this.cannotChangePasswordText, 1, row++);
		table.mergeCells(1, row, 2, row);
		table.add(this.passwordNeverExpiresField, 1, row);
		table.add(this.passwordNeverExpiresText, 1, row++);
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
			String cannotChangePassw = iwc.getParameter(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD);
			String passwExpires = iwc.getParameter(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES);
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
			if (cannotChangePassw != null && mustChangePassw != null) {
				this.addErrorMessage(iwrb.getLocalizedString("usr_log_pwdNotTwoCheck",
						"'User must change password at next login' and 'User cannot change password' cannot both be checked"));
				this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
				this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
			}
			else {
				if (mustChangePassw != null) {
					this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
				}
				else {
					this.fieldValues.put(UserLoginTab._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
				}
				if (cannotChangePassw != null) {
					this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.TRUE);
				}
				else {
					this.fieldValues.put(UserLoginTab._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
				}
			}
			if (passwExpires != null) {
				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.TRUE);
			}
			else {
				this.fieldValues.put(UserLoginTab._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
			}
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
		this.fieldValues.put(this._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
		this.fieldValues.put(this._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
		this.fieldValues.put(this._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
		this.fieldValues.put(this._PARAM_DISABLE_ACCOUNT, Boolean.FALSE);
		initFieldContents();
		this.updateFieldsDisplayStatus();
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}