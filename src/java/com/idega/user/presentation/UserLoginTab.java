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
		errorMessageTable = new Table();
		errorText = new Text();
		errorText.setFontColor("red");
		super.init();
	}

	public void initFieldContents() {
		try {
			LoginTable lTable = LoginDBHandler.getUserLogin(getUserId());
			LoginInfo lInfo = null;

			if (lTable != null) {
				lInfo = LoginDBHandler.getLoginInfo(lTable.getID());
				fieldValues.put(_PARAM_USER_LOGIN, lTable.getUserLogin());
			}
			if (lInfo != null) {
				fieldValues.put(
					_PARAM_MUST_CHANGE_PASSWORD,
					new Boolean(lInfo.getChangeNextTime()));
				fieldValues.put(
					_PARAM_CANNOT_CHANGE_PASSWORD,
					new Boolean(!lInfo.getAllowedToChange()));
				fieldValues.put(
					_PARAM_PASSWORD_NEVER_EXPIRES,
					new Boolean(lInfo.getPasswordExpires()));
				fieldValues.put(
					_PARAM_DISABLE_ACCOUNT,
					new Boolean(!lInfo.getAccountEnabled()));
			}
			this.updateFieldsDisplayStatus();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(
				"UserLoginTab: error in initFieldContents() for user: "
					+ this.getUserId());
		}
	}

	public void updateFieldsDisplayStatus() {
		userLoginField.setContent((String)fieldValues.get(_PARAM_USER_LOGIN));

		passwordField.setContent((String)fieldValues.get(_PARAM_PASSWORD));
		confirmPasswordField.setContent((String)fieldValues.get(_PARAM_PASSWORD));

		mustChangePasswordField.setChecked(
			((Boolean)fieldValues.get(_PARAM_MUST_CHANGE_PASSWORD)).booleanValue());
		cannotChangePasswordField.setChecked(
			((Boolean)fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue());
		passwordNeverExpiresField.setChecked(
			((Boolean)fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES)).booleanValue());
		disableAccountField.setChecked(
			((Boolean)fieldValues.get(_PARAM_DISABLE_ACCOUNT)).booleanValue());
	}

	public void initializeFields() {
		userLoginField = new TextInput(_PARAM_USER_LOGIN);
		userLoginField.setLength(32);

		passwordField = new PasswordInput(_PARAM_PASSWORD);
		passwordField.setLength(32);

		confirmPasswordField = new PasswordInput(_PARAM_CONFIRM_PASSWORD);
		confirmPasswordField.setLength(32);

		mustChangePasswordField = new CheckBox(_PARAM_MUST_CHANGE_PASSWORD);
		mustChangePasswordField.setHeight("10");
		mustChangePasswordField.setWidth("10");

		cannotChangePasswordField = new CheckBox(_PARAM_CANNOT_CHANGE_PASSWORD);
		cannotChangePasswordField.setHeight("10");
		cannotChangePasswordField.setWidth("10");
		
		passwordNeverExpiresField = new CheckBox(_PARAM_PASSWORD_NEVER_EXPIRES);
		passwordNeverExpiresField.setHeight("10");
		passwordNeverExpiresField.setWidth("10");
		
		disableAccountField = new CheckBox(_PARAM_DISABLE_ACCOUNT);
		disableAccountField.setHeight("10");
		disableAccountField.setWidth("10");
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		userLoginText = new Text(iwrb.getLocalizedString(_PARAM_USER_LOGIN,"User login"));
		userLoginText.setBold();
		
		passwordText = new Text(iwrb.getLocalizedString(_PARAM_PASSWORD,"New password"));
		passwordText.setBold();
		
		confirmPasswordText = new Text(iwrb.getLocalizedString(_PARAM_CONFIRM_PASSWORD,"Confirm password"));
		confirmPasswordText.setBold();

		mustChangePasswordText = new Text(iwrb.getLocalizedString(_PARAM_MUST_CHANGE_PASSWORD,"User must change password at next login"));
		mustChangePasswordText.setBold();
		
		cannotChangePasswordText = new Text(iwrb.getLocalizedString(_PARAM_CANNOT_CHANGE_PASSWORD,"User cannot change password"));
		cannotChangePasswordText.setBold();
		
		passwordNeverExpiresText = new Text(iwrb.getLocalizedString(_PARAM_PASSWORD_NEVER_EXPIRES,"Password never expires"));
		passwordNeverExpiresText.setBold();
		
		disableAccountText = new Text(iwrb.getLocalizedString(_PARAM_DISABLE_ACCOUNT,"Account is disabled"));
		disableAccountText.setBold();
	}

	public boolean store(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		boolean updateLoginTable = true;
		String login = (String)fieldValues.get(_PARAM_USER_LOGIN);

		String passw = ((String)fieldValues.get(_PARAM_PASSWORD));
		String confirmedpassw = ((String)fieldValues.get(_PARAM_PASSWORD));

		Boolean mustChangePassw =
			((Boolean)fieldValues.get(_PARAM_MUST_CHANGE_PASSWORD));
		//.booleanValue();
		Boolean canChangePassw =
			((Boolean)fieldValues.get(_PARAM_CANNOT_CHANGE_PASSWORD)).booleanValue()
				? Boolean.FALSE
				: Boolean.TRUE;
		Boolean passwExpires =
			((Boolean)fieldValues.get(_PARAM_PASSWORD_NEVER_EXPIRES));
		//.booleanValue();
		Boolean accountEnabled =
			((Boolean)fieldValues.get(_PARAM_DISABLE_ACCOUNT)).booleanValue()
				? Boolean.FALSE
				: Boolean.TRUE;

		try {

			if (((passw != null && !passw.equals(""))
				&& ((confirmedpassw != null && !confirmedpassw.equals(""))))) {
				if (login != null && !login.equals("")) {
					LoginTable userLoginTable =
						LoginDBHandler.getUserLogin(this.getUserId());
					String oldLogin = null;
					if (userLoginTable != null) {
						oldLogin = userLoginTable.getUserLogin();
					}
					boolean inUse = LoginDBHandler.isLoginInUse(login);
					if (oldLogin != null) {
						if (inUse && !oldLogin.equals(login)) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse","login in use"));
						}
						else {
							fieldValues.put(this._PARAM_USER_LOGIN, login);
						}
					}
					else {
						if (inUse) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse","login in use"));
						}
						else {
							fieldValues.put(this._PARAM_USER_LOGIN, login);
						}
					}
				}
				else {
					this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginNotValid","login not valid"));
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
			errorMessageTable.empty();
			try {
				LoginTable loginTable = LoginDBHandler.getUserLogin(this.getUserId());
				if (loginTable != null) {
					if (updateLoginTable) {
						LoginDBHandler.updateLogin(this.getUserId(), login, passw);
					}
					LoginDBHandler.updateLoginInfo(
						loginTable.getID(),
						accountEnabled,
						IWTimestamp.RightNow(),
						5000,
						passwExpires,
						canChangePassw,
						mustChangePassw,
						null);
				}
				else if (updateLoginTable) {
					LoginDBHandler.createLogin(
						this.getUserId(),
						login,
						passw,
						accountEnabled,
						IWTimestamp.RightNow(),
						5000,
						passwExpires,
						canChangePassw,
						mustChangePassw,
						null);
				} else {
						LoginDBHandler.createLogin(this.getUserId(), login, passw);
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

		errorMessageTable.setHeight(1);
		errorMessageTable.setCellpadding(0);
		errorMessageTable.setCellspacing(0);
		table.mergeCells(1, row, 2, row);
		table.add(errorMessageTable, 1, row);

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

			String login = iwc.getParameter(this._PARAM_USER_LOGIN);
			String passw = iwc.getParameter(this._PARAM_PASSWORD);
			String confirmedpassw = iwc.getParameter(this._PARAM_CONFIRM_PASSWORD);

			String mustChangePassw =
				iwc.getParameter(this._PARAM_MUST_CHANGE_PASSWORD);
			String cannotChangePassw =
				iwc.getParameter(this._PARAM_CANNOT_CHANGE_PASSWORD);
			String passwExpires =
				iwc.getParameter(this._PARAM_PASSWORD_NEVER_EXPIRES);
			String accountDisabled = iwc.getParameter(this._PARAM_DISABLE_ACCOUNT);

			if (((passw != null && !passw.equals(""))
				|| ((confirmedpassw != null && !confirmedpassw.equals(""))))) {
				if (login != null && !login.equals("")) {
					LoginTable userLoginTable =
						LoginDBHandler.getUserLogin(this.getUserId());
					String oldLogin = null;
					if (userLoginTable != null) {
						oldLogin = userLoginTable.getUserLogin();
					}
					boolean inUse = LoginDBHandler.isLoginInUse(login);
					if (oldLogin != null) {
						if (inUse && !oldLogin.equals(login)) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse","login in use"));
						}
						else {
							fieldValues.put(this._PARAM_USER_LOGIN, login);
						}
					}
					else {
						if (inUse) {
							this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginInUse","login in use"));
						}
						else {
							fieldValues.put(this._PARAM_USER_LOGIN, login);
						}
					}
				}
				else {
					this.addErrorMessage(iwrb.getLocalizedString("usr_log_loginNotValid","login not valid"));
				}

				if (passw != null
					&& confirmedpassw != null
					&& passw.equals(confirmedpassw)) {
					fieldValues.put(this._PARAM_PASSWORD, passw);
					fieldValues.put(this._PARAM_CONFIRM_PASSWORD, confirmedpassw);
				}
				else {
					this.addErrorMessage(
					iwrb.getLocalizedString("usr_log_pwdNotSame","password and confirmed password not valid or not the same"));
					fieldValues.put(this._PARAM_PASSWORD, "");
					fieldValues.put(this._PARAM_CONFIRM_PASSWORD, "");
				}
			}
			else {
				fieldValues.put(this._PARAM_PASSWORD, "");
				fieldValues.put(this._PARAM_CONFIRM_PASSWORD, "");
			}

			if (cannotChangePassw != null && mustChangePassw != null) {
				this.addErrorMessage(
				iwrb.getLocalizedString("usr_log_pwdNotTwoCheck","'User must change password at next login' and 'User cannot change password' cannot both be checked"));
				fieldValues.put(this._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
				fieldValues.put(this._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
			}
			else {
				if (mustChangePassw != null) {
					fieldValues.put(this._PARAM_MUST_CHANGE_PASSWORD, Boolean.TRUE);
				}
				else {
					fieldValues.put(this._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
				}

				if (cannotChangePassw != null) {
					fieldValues.put(this._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.TRUE);
				}
				else {
					fieldValues.put(this._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
				}
			}

			if (passwExpires != null) {
				fieldValues.put(this._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.TRUE);
			}
			else {
				fieldValues.put(this._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
			}

			if (accountDisabled != null) {
				fieldValues.put(this._PARAM_DISABLE_ACCOUNT, Boolean.TRUE);
			}
			else {
				fieldValues.put(this._PARAM_DISABLE_ACCOUNT, Boolean.FALSE);
			}

			this.updateFieldsDisplayStatus();

			if (someErrors()) {
				fieldValues.put(this._PARAM_PASSWORD, "");
				fieldValues.put(this._PARAM_CONFIRM_PASSWORD, "");
				presentErrorMessage(this.clearErrorMessages());
				return false;
			}
			else {
				errorMessageTable.empty();
				return true;
			}
		}
		this.addErrorMessage("IWContext is null");
		if (someErrors()) {
			fieldValues.put(this._PARAM_PASSWORD, "");
			fieldValues.put(this._PARAM_CONFIRM_PASSWORD, "");
			presentErrorMessage(this.clearErrorMessages());
			return false;
		}
		else {
			errorMessageTable.empty();
			return true;
		}
	}

	public void presentErrorMessage(String[] messages) {
		errorMessageTable.empty();
		if (messages != null) {
			for (int i = 0; i < messages.length; i++) {
				Text message = (Text)errorText.clone();
				message.setText("* " + messages[i] + Text.BREAK);

				errorMessageTable.add(message);
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
		fieldValues.put(this._PARAM_USER_LOGIN, "");
		fieldValues.put(this._PARAM_PASSWORD, "");
		fieldValues.put(this._PARAM_CONFIRM_PASSWORD, "");
		fieldValues.put(this._PARAM_MUST_CHANGE_PASSWORD, Boolean.FALSE);
		fieldValues.put(this._PARAM_CANNOT_CHANGE_PASSWORD, Boolean.FALSE);
		fieldValues.put(this._PARAM_PASSWORD_NEVER_EXPIRES, Boolean.FALSE);
		fieldValues.put(this._PARAM_DISABLE_ACCOUNT, Boolean.FALSE);
		
		initFieldContents();
		
		this.updateFieldsDisplayStatus();
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}