package com.idega.user.presentation;

import java.sql.SQLException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneType;
import com.idega.core.contact.data.PhoneTypeBMPBean;
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
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
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

	public void initializeFieldNames() {
	}

	public void initializeFieldValues() {
		fieldValues.put(this.homePhoneFieldName, "");
		fieldValues.put(this.workPhoneFieldName, "");
		fieldValues.put(this.mobilePhoneFieldName, "");
		fieldValues.put(this.faxPhoneFieldName, "");
		fieldValues.put(this.homePhoneMenuName, "");
		fieldValues.put(this.workPhoneMenuName, "");
		fieldValues.put(this.mobilePhoneMenuName, "");
		fieldValues.put(this.faxPhoneMenuName, "");
		fieldValues.put(this.emailFieldName, "");
    fieldValues.put(jobFieldName,"");
		fieldValues.put(workPlaceFieldName,"");

		this.updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
		homePhoneField.setContent((String)fieldValues.get(this.homePhoneFieldName));
		workPhoneField.setContent((String)fieldValues.get(this.workPhoneFieldName));
		mobilePhoneField.setContent(
			(String)fieldValues.get(this.mobilePhoneFieldName));
		faxPhoneField.setContent((String)fieldValues.get(this.faxPhoneFieldName));
		emailField.setContent((String)fieldValues.get(this.emailFieldName));
    jobField.setContent((String)fieldValues.get(jobFieldName));
		workPlaceField.setContent((String)fieldValues.get(workPlaceFieldName));
	}

	public void initializeFields() {
		PhoneType[] phoneTypes = null;
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		try {
			phoneTypes =
				(PhoneType[])PhoneTypeBMPBean.getStaticInstance(PhoneType.class).findAll();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		if (phoneTypes != null) {
			if (phoneTypes.length > 0) {
				
				
				for (int i = 0; i < phoneTypes.length; i++) {
					String n = phoneTypes[i].getName();
					if (n != null) {
						String l = iwrb.getLocalizedString("usr_phone_" + n,n);
						phoneTypes[i].setName(l);
					} 					
				}
			}
		}
		
		homePhoneField = new TextInput(homePhoneFieldName);
		homePhoneField.setLength(24);

		workPhoneField = new TextInput(workPhoneFieldName);
		workPhoneField.setLength(24);

		mobilePhoneField = new TextInput(mobilePhoneFieldName);
		mobilePhoneField.setLength(24);

		faxPhoneField = new TextInput(faxPhoneFieldName);
		faxPhoneField.setLength(24);

		emailField = new TextInput(emailFieldName);
		emailField.setLength(24);
    
    jobField = new TextInput(jobFieldName);
    jobField.setLength(24);

		workPlaceField = new TextInput(workPlaceFieldName);
		workPlaceField.setLength(24);
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		firstPhoneText = new Text(iwrb.getLocalizedString(homePhoneFieldName,"Phone 1") + ":");
		firstPhoneText.setBold();

		secondPhoneText = new Text(iwrb.getLocalizedString(mobilePhoneFieldName,"Phone 2") + ":");
		secondPhoneText.setBold();

		thirdPhoneText = new Text(iwrb.getLocalizedString(workPhoneFieldName,"Phone 3") + ":");
		thirdPhoneText.setBold();

		fourthPhoneText = new Text(iwrb.getLocalizedString(faxPhoneFieldName,"Phone 4") + ":");
		fourthPhoneText.setBold();

		homePhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.home_phone", "Home phone"));
		homePhoneTypeText.setFontStyle("font-size:8px");

		workPhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.work_phone", "Work phone"));
		workPhoneTypeText.setFontStyle("font-size:8px");

		mobilePhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.mobile_phone", "Mobile phone"));
		mobilePhoneTypeText.setFontStyle("font-size:8px");

		faxPhoneTypeText = new Text(iwrb.getLocalizedString("usr_phone.fax_phone", "Fax"));
		faxPhoneTypeText.setFontStyle("font-size:8px");

		emailText = new Text(iwrb.getLocalizedString(emailFieldName,"E-mail") + ":");
		emailText.setBold();
    
    jobText = new Text(iwrb.getLocalizedString(jobFieldName, "Job") + ":");
    jobText.setBold();

		workPlaceText = new Text(iwrb.getLocalizedString(workPlaceFieldName, "Workplace") + ":");
		workPlaceText.setBold();
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

		table.add(firstPhoneText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(homePhoneField, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(homePhoneTypeText, 1, row);
		
		table.add(secondPhoneText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(mobilePhoneField, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(mobilePhoneTypeText, 2, row++);
		
		table.add(thirdPhoneText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(workPhoneField, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(workPhoneTypeText, 1, row);
		
		table.add(fourthPhoneText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(faxPhoneField, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(faxPhoneTypeText, 2, row++);
		
		row++;

		table.add(emailText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(emailField, 1, row);

		table.add(jobText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(jobField, 2, row++);
		
		table.add(workPlaceText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(workPlaceField, 1, row);
    
		this.add(table, 1, 1);
	}

	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());		
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

			String homePhone = iwc.getParameter(this.homePhoneFieldName);
			String workPhone = iwc.getParameter(this.workPhoneFieldName);
			String mobilePhone = iwc.getParameter(this.mobilePhoneFieldName);
			String faxPhone = iwc.getParameter(this.faxPhoneFieldName);
			String homePhoneType = iwc.getParameter(this.homePhoneMenuName);
			String workPhoneType = iwc.getParameter(this.workPhoneMenuName);
			String mobilePhoneType = iwc.getParameter(this.mobilePhoneMenuName);
			String faxPhoneType = iwc.getParameter(this.faxPhoneMenuName);
			String email = iwc.getParameter(this.emailFieldName);
      String job = iwc.getParameter(jobFieldName);
			String workPlace = iwc.getParameter(workPlaceFieldName);

			if (homePhone != null) {
				fieldValues.put(this.homePhoneFieldName, homePhone);
			}
			if (workPhone != null) {
				fieldValues.put(this.workPhoneFieldName, workPhone);
			}
			if (mobilePhone != null) {
				fieldValues.put(this.mobilePhoneFieldName, mobilePhone);
			}
			if (faxPhone != null) {
				fieldValues.put(this.faxPhoneFieldName, faxPhone);
			}
			if (homePhoneType != null) {
				fieldValues.put(this.homePhoneMenuName, homePhoneType);
			}
			if (workPhoneType != null) {
				fieldValues.put(this.workPhoneMenuName, workPhoneType);
			}
			if (mobilePhoneType != null) {
				fieldValues.put(this.mobilePhoneMenuName, mobilePhoneType);
			}
			if (faxPhoneType != null) {
				fieldValues.put(this.faxPhoneMenuName, faxPhoneType);
			}
			if (email != null) {
				fieldValues.put(this.emailFieldName, email);
			}
      if (job != null)  
        fieldValues.put(jobFieldName, job);
			if (workPlace != null)  
				fieldValues.put(workPlaceFieldName, workPlace);

			this.updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {
				String[] phoneString = 
					{
						(String)fieldValues.get(this.homePhoneFieldName),
						(String)fieldValues.get(this.workPhoneFieldName),
						(String)fieldValues.get(this.mobilePhoneFieldName),
						(String)fieldValues.get(this.faxPhoneFieldName)};
				int[] phoneTypes = { PhoneType.HOME_PHONE_ID, PhoneType.WORK_PHONE_ID, PhoneType.MOBILE_PHONE_ID, PhoneType.FAX_NUMBER_ID };
				for (int a = 0; a < phoneString.length; a++) {
					if (phoneString[a] != null && phoneString[a].trim().length() > 0) {
						//business.updateUserPhone(getUserId(),Integer.parseInt(phoneTypeString[a]),phoneString[a]);
						super.getUserBusiness(iwc).updateUserPhone(
							getUserId(),
							phoneTypes[a],
							phoneString[a].trim());
					}
				}
				if ((String)fieldValues.get(this.emailFieldName) != null
					&& ((String)fieldValues.get(this.emailFieldName)).length() > 0)
					//business.updateUserMail(getUserId(),(String)fieldValues.get(this.emailFieldName));
					super.getUserBusiness(iwc).updateUserMail(
						getUserId(),
						(String)fieldValues.get(this.emailFieldName));
        String job = (String)fieldValues.get(jobFieldName);
        if ( job != null)
            getUserBusiness(iwc).updateUserJob(getUserId(), job);
            
				String workPlace = (String)fieldValues.get(workPlaceFieldName);
				if ( workPlace != null)
						getUserBusiness(iwc).updateUserWorkPlace(getUserId(), workPlace);

			}
		}
		catch (Exception e) {
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
			Phone[] phones =
				userBusiness.getUserPhones(user);
			Email mail =
				userBusiness.getUserMail(user);
      String job =
        userBusiness.getUserJob(user);
      String workPlace = userBusiness.getUserWorkPlace(user);      

			for (int a = 0; a < phones.length; a++) {
				if (a == 0) {
					fieldValues.put(
						this.homePhoneMenuName,
						(phones[a].getPhoneTypeId() != -1)
							? Integer.toString(phones[a].getPhoneTypeId())
							: "");
					fieldValues.put(
						this.homePhoneFieldName,
						(phones[a].getNumber() != null) ? phones[a].getNumber() : "");
				}
				else if (a == 1) {
					fieldValues.put(
						this.workPhoneMenuName,
						(phones[a].getPhoneTypeId() != -1)
							? Integer.toString(phones[a].getPhoneTypeId())
							: "");
					fieldValues.put(
						this.workPhoneFieldName,
						(phones[a].getNumber() != null) ? phones[a].getNumber() : "");
				}
				else if (a == 2) {
					fieldValues.put(
						this.mobilePhoneMenuName,
						(phones[a].getPhoneTypeId() != -1)
							? Integer.toString(phones[a].getPhoneTypeId())
							: "");
					fieldValues.put(
						this.mobilePhoneFieldName,
						(phones[a].getNumber() != null) ? phones[a].getNumber() : "");
				}
				else if (a == 3) {
					fieldValues.put(
						this.faxPhoneMenuName,
						(phones[a].getPhoneTypeId() != -1)
							? Integer.toString(phones[a].getPhoneTypeId())
							: "");
					fieldValues.put(
						this.faxPhoneFieldName,
						(phones[a].getNumber() != null) ? phones[a].getNumber() : "");
				}
			}
			if (mail != null)
				fieldValues.put(
					this.emailFieldName,
					(mail.getEmailAddress() != null) ? mail.getEmailAddress() : "");
      
      fieldValues.put(jobFieldName, (job == null) ? "" : job);
			fieldValues.put(workPlaceFieldName, (workPlace == null) ? "" : workPlace);
        
      this.updateFieldsDisplayStatus();  

		}
		catch (Exception e) {
			System.err.println(
				"UserPhoneTab error initFieldContents, userId : " + getUserId());
		}

	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

} // Class UserPhoneTab
