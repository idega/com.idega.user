package com.idega.user.presentation;

import java.sql.SQLException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneType;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.data.GenericEntity;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * Title: User Copyright: Copyright (c) 2001 Company: idega.is
 * 
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st
 *         S�mundsson </a>
 * @version 1.0
 */

public class UserPhoneTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_phone_tab_name";

	private static final String DEFAULT_TAB_NAME = "Phone/Mail";

	private static final String HELP_TEXT_KEY = "user_phone_tab";

	private TextInput homePhoneField;

	private TextInput workPhoneField;

	private TextInput mobilePhoneField;

	private TextInput faxPhoneField;

	private Text homePhoneTypeText;

	private Text workPhoneTypeText;

	private Text mobilePhoneTypeText;

	private Text faxPhoneTypeText;

	private TextInput emailField;

	private TextInput jobField;

	private TextInput workPlaceField;

	public static String homePhoneFieldName = "homePhone";

	public static String workPhoneFieldName = "workPhone";

	public static String mobilePhoneFieldName = "mobilePhone";

	public static String faxPhoneFieldName = "faxPhone";

	public static String homePhoneMenuName = "homeChoice";

	public static String workPhoneMenuName = "workChoice";

	public static String mobilePhoneMenuName = "mobileChoice";

	public static String faxPhoneMenuName = "faxChoice";

	public static String emailFieldName = "email";

	public static String jobFieldName = "job";

	public static String workPlaceFieldName = "workplace";

	private Text firstPhoneText;

	private Text secondPhoneText;

	private Text thirdPhoneText;

	private Text fourthPhoneText;

	private Text emailText;

	private Text jobText;

	private Text workPlaceText;

	public UserPhoneTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
	}

	public UserPhoneTab(int userId) {
		this();
		setUserID(userId);
	}

	public void initializeFieldNames() {}

	public void initializeFieldValues() {
		this.fieldValues.put(UserPhoneTab.homePhoneFieldName, "");
		this.fieldValues.put(UserPhoneTab.workPhoneFieldName, "");
		this.fieldValues.put(UserPhoneTab.mobilePhoneFieldName, "");
		this.fieldValues.put(UserPhoneTab.faxPhoneFieldName, "");
		this.fieldValues.put(UserPhoneTab.homePhoneMenuName, "");
		this.fieldValues.put(UserPhoneTab.workPhoneMenuName, "");
		this.fieldValues.put(UserPhoneTab.mobilePhoneMenuName, "");
		this.fieldValues.put(UserPhoneTab.faxPhoneMenuName, "");
		this.fieldValues.put(UserPhoneTab.emailFieldName, "");
		this.fieldValues.put(jobFieldName, "");
		this.fieldValues.put(workPlaceFieldName, "");

		this.updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
		this.homePhoneField.setContent((String) this.fieldValues.get(UserPhoneTab.homePhoneFieldName));
		this.workPhoneField.setContent((String) this.fieldValues.get(UserPhoneTab.workPhoneFieldName));
		this.mobilePhoneField.setContent((String) this.fieldValues.get(UserPhoneTab.mobilePhoneFieldName));
		this.faxPhoneField.setContent((String) this.fieldValues.get(UserPhoneTab.faxPhoneFieldName));
		this.emailField.setContent((String) this.fieldValues.get(UserPhoneTab.emailFieldName));
		this.jobField.setContent((String) this.fieldValues.get(jobFieldName));
		this.workPlaceField.setContent((String) this.fieldValues.get(workPlaceFieldName));
	}

	public void initializeFields() {
		PhoneType[] phoneTypes = null;
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		try {
			phoneTypes = (PhoneType[]) GenericEntity.getStaticInstance(PhoneType.class).findAll();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		if (phoneTypes != null) {
			if (phoneTypes.length > 0) {

				for (int i = 0; i < phoneTypes.length; i++) {
					String n = phoneTypes[i].getName();
					if (n != null) {
						String l = iwrb.getLocalizedString("usr_phone_" + n, n);
						phoneTypes[i].setName(l);
					}
				}
			}
		}

		this.homePhoneField = new TextInput(homePhoneFieldName);
		this.homePhoneField.setLength(24);

		this.workPhoneField = new TextInput(workPhoneFieldName);
		this.workPhoneField.setLength(24);

		this.mobilePhoneField = new TextInput(mobilePhoneFieldName);
		this.mobilePhoneField.setLength(24);

		this.faxPhoneField = new TextInput(faxPhoneFieldName);
		this.faxPhoneField.setLength(24);

		this.emailField = new TextInput(emailFieldName);
		this.emailField.setLength(24);

		this.jobField = new TextInput(jobFieldName);
		this.jobField.setLength(24);

		this.workPlaceField = new TextInput(workPlaceFieldName);
		this.workPlaceField.setLength(24);
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		this.firstPhoneText = new Text(iwrb.getLocalizedString(homePhoneFieldName, "Phone 1") + ":");
		this.firstPhoneText.setBold();

		this.secondPhoneText = new Text(iwrb.getLocalizedString(mobilePhoneFieldName, "Phone 2") + ":");
		this.secondPhoneText.setBold();

		this.thirdPhoneText = new Text(iwrb.getLocalizedString(workPhoneFieldName, "Phone 3") + ":");
		this.thirdPhoneText.setBold();

		this.fourthPhoneText = new Text(iwrb.getLocalizedString(faxPhoneFieldName, "Phone 4") + ":");
		this.fourthPhoneText.setBold();

		this.homePhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.home_phone", "Home phone"));
		this.homePhoneTypeText.setFontStyle("font-size:8px");

		this.workPhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.work_phone", "Work phone"));
		this.workPhoneTypeText.setFontStyle("font-size:8px");

		this.mobilePhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.mobile_phone", "Mobile phone"));
		this.mobilePhoneTypeText.setFontStyle("font-size:8px");

		this.faxPhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.fax_phone", "Fax"));
		this.faxPhoneTypeText.setFontStyle("font-size:8px");

		this.emailText = new Text(iwrb.getLocalizedString(emailFieldName, "E-mail") + ":");
		this.emailText.setBold();

		this.jobText = new Text(iwrb.getLocalizedString(jobFieldName, "Job") + ":");
		this.jobText.setBold();

		this.workPlaceText = new Text(iwrb.getLocalizedString(workPlaceFieldName, "Workplace") + ":");
		this.workPlaceText.setBold();
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

	public void lineUpFields() {
		this.resize(1, 1);

		Table table = new Table();
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setColumns(2);
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setBorder(0);
		int row = 1;

		table.add(this.firstPhoneText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.homePhoneField, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.homePhoneTypeText, 1, row);

		table.add(this.secondPhoneText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.mobilePhoneField, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.mobilePhoneTypeText, 2, row++);

		table.add(this.thirdPhoneText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.workPhoneField, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.workPhoneTypeText, 1, row);

		table.add(this.fourthPhoneText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.faxPhoneField, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.faxPhoneTypeText, 2, row++);

		row++;

		table.add(this.emailText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.emailField, 1, row);

		table.add(this.jobText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.jobField, 2, row++);

		table.add(this.workPlaceText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.workPlaceField, 1, row);

		this.add(table, 1, 1);
	}

	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

			String homePhone = iwc.getParameter(UserPhoneTab.homePhoneFieldName);
			String workPhone = iwc.getParameter(UserPhoneTab.workPhoneFieldName);
			String mobilePhone = iwc.getParameter(UserPhoneTab.mobilePhoneFieldName);
			String faxPhone = iwc.getParameter(UserPhoneTab.faxPhoneFieldName);
			String homePhoneType = iwc.getParameter(UserPhoneTab.homePhoneMenuName);
			String workPhoneType = iwc.getParameter(UserPhoneTab.workPhoneMenuName);
			String mobilePhoneType = iwc.getParameter(UserPhoneTab.mobilePhoneMenuName);
			String faxPhoneType = iwc.getParameter(UserPhoneTab.faxPhoneMenuName);
			String email = iwc.getParameter(UserPhoneTab.emailFieldName);
			String job = iwc.getParameter(jobFieldName);
			String workPlace = iwc.getParameter(workPlaceFieldName);

			if (homePhone != null) {
				this.fieldValues.put(UserPhoneTab.homePhoneFieldName, homePhone);
			}
			if (workPhone != null) {
				this.fieldValues.put(UserPhoneTab.workPhoneFieldName, workPhone);
			}
			if (mobilePhone != null) {
				this.fieldValues.put(UserPhoneTab.mobilePhoneFieldName, mobilePhone);
			}
			if (faxPhone != null) {
				this.fieldValues.put(UserPhoneTab.faxPhoneFieldName, faxPhone);
			}
			if (homePhoneType != null) {
				this.fieldValues.put(UserPhoneTab.homePhoneMenuName, homePhoneType);
			}
			if (workPhoneType != null) {
				this.fieldValues.put(UserPhoneTab.workPhoneMenuName, workPhoneType);
			}
			if (mobilePhoneType != null) {
				this.fieldValues.put(UserPhoneTab.mobilePhoneMenuName, mobilePhoneType);
			}
			if (faxPhoneType != null) {
				this.fieldValues.put(UserPhoneTab.faxPhoneMenuName, faxPhoneType);
			}
			if (email != null) {
				this.fieldValues.put(UserPhoneTab.emailFieldName, email);
			}
			if (job != null) {
				this.fieldValues.put(jobFieldName, job);
			}
			if (workPlace != null) {
				this.fieldValues.put(workPlaceFieldName, workPlace);
			}

			this.updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {
				String[] phoneString = { (String) this.fieldValues.get(UserPhoneTab.homePhoneFieldName), (String) this.fieldValues.get(UserPhoneTab.workPhoneFieldName),
						(String) this.fieldValues.get(UserPhoneTab.mobilePhoneFieldName), (String) this.fieldValues.get(UserPhoneTab.faxPhoneFieldName) };
				int[] phoneTypes = { PhoneType.HOME_PHONE_ID, PhoneType.WORK_PHONE_ID, PhoneType.MOBILE_PHONE_ID, PhoneType.FAX_NUMBER_ID };
				for (int a = 0; a < phoneString.length; a++) {
					if (phoneString[a] != null) {
						// NB: (jonas) removed check for empty value because users may want to remove phone numbers
						//business.updateUserPhone(getUserId(),Integer.parseInt(phoneTypeString[a]),phoneString[a]);
						super.getUserBusiness(iwc).updateUserPhone(getUserId(), phoneTypes[a], phoneString[a]);
					}
				}
				if ((String) this.fieldValues.get(this.emailFieldName) != null && ((String) this.fieldValues.get(this.emailFieldName)).length() > 0) {
					//business.updateUserMail(getUserId(),(String)fieldValues.get(this.emailFieldName));
					super.getUserBusiness(iwc).updateUserMail(getUserId(), (String) this.fieldValues.get(this.emailFieldName));
				}
				String job = (String) this.fieldValues.get(jobFieldName);
				if (job != null) {
					getUserBusiness(iwc).updateUserJob(getUserId(), job);
				}

				String workPlace = (String) this.fieldValues.get(workPlaceFieldName);
				if (workPlace != null) {
					getUserBusiness(iwc).updateUserWorkPlace(getUserId(), workPlace);
				}

			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("update user exception");
		}
		return true;
	}

	public void initFieldContents() {

		try {
			IWContext context = getEventIWContext();
			UserBusiness userBusiness = getUserBusiness(context);
			User user = getUser();
			Phone[] phones = userBusiness.getUserPhones(user);
			Email mail = userBusiness.getUserMail(user);
			String job = userBusiness.getUserJob(user);
			String workPlace = userBusiness.getUserWorkPlace(user);

			this.fieldValues.put(this.homePhoneMenuName, Integer.toString(PhoneType.HOME_PHONE_ID));
			this.fieldValues.put(this.workPhoneMenuName, Integer.toString(PhoneType.WORK_PHONE_ID));
			this.fieldValues.put(this.mobilePhoneMenuName, Integer.toString(PhoneType.MOBILE_PHONE_ID));
			this.fieldValues.put(this.faxPhoneMenuName, Integer.toString(PhoneType.FAX_NUMBER_ID));
			for (int a = 0; a < phones.length; a++) {
				Phone phone = phones[a];
				if (phone.getPhoneTypeId() == PhoneType.HOME_PHONE_ID) {
					this.fieldValues.put(this.homePhoneFieldName, (phone.getNumber() != null) ? phone.getNumber() : "");
				} else if (phone.getPhoneTypeId() == PhoneType.WORK_PHONE_ID) {
					this.fieldValues.put(this.workPhoneFieldName, (phone.getNumber() != null) ? phone.getNumber() : "");
				} else if (phone.getPhoneTypeId() == PhoneType.MOBILE_PHONE_ID) {
					this.fieldValues.put(this.mobilePhoneFieldName, (phone.getNumber() != null) ? phone.getNumber() : "");
				} else if (phone.getPhoneTypeId() == PhoneType.FAX_NUMBER_ID) {
					this.fieldValues.put(this.faxPhoneFieldName, (phone.getNumber() != null) ? phone.getNumber() : "");
				}
			}
			if (mail != null) {
				this.fieldValues.put(this.emailFieldName, (mail.getEmailAddress() != null) ? mail.getEmailAddress() : "");
			}

			this.fieldValues.put(jobFieldName, (job == null) ? "" : job);
			this.fieldValues.put(workPlaceFieldName, (workPlace == null) ? "" : workPlace);

			this.updateFieldsDisplayStatus();

		} catch (Exception e) {
			System.err.println("UserPhoneTab error initFieldContents, userId : " + getUserId());
		}
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

} // Class UserPhoneTab
