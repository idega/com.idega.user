package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Gender;
import com.idega.user.data.GenderHome;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */
public class GeneralUserInfoTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_info_tab_name";
	private static final String DEFAULT_TAB_NAME = "General";
	
//	private TextInput firstNameField;
//	private TextInput middleNameField;
//	private TextInput lastNameField;
	private TextInput fullNameField;
	private TextInput displayNameField;
	private TextArea descriptionField;
	private DateInput dateOfBirthField;
	private DropdownMenu genderField;
	private TextInput personalIDField;
	private DateInput createdField;

//	private String firstNameFieldName;
//	private String middleNameFieldName;
//	private String lastNameFieldName;
	private String fullNameFieldName;
	private String displayNameFieldName;
	private String descriptionFieldName;
	private String dateOfBirthFieldName;
	private String genderFieldName;
	private String personalIDFieldName;
	private String createdFieldName;

//	private Text firstNameText;
//	private Text middleNameText;
//	private Text lastNameText;
	private Text fullNameText;
	private Text displayNameText;
	private Text descriptionText;
	private Text dateOfBirthText;
	private Text genderText;
	private Text personalIDText;
	private Text createdText;

	public GeneralUserInfoTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		
//		setName("General");
	}

	public GeneralUserInfoTab(int userId) {
		this();
		setUserID(userId);
	}

	public void initializeFieldNames() {
//		firstNameFieldName = "usr_info_UMfname";
//		middleNameFieldName = "usr_info_UMmname";
//		lastNameFieldName = "usr_info_UMlname";
		fullNameFieldName = "usr_info_UMflname";
		displayNameFieldName = "usr_info_UMdname";
		descriptionFieldName = "usr_info_UMdesc";
		dateOfBirthFieldName = "usr_info_UMdateofbirth";
		genderFieldName = "usr_info_UMgender";
		personalIDFieldName = "usr_info_UMpersonalID";
		createdFieldName = "usr_info_UMcreated";
	}

	public void initializeFieldValues() {
//		fieldValues.put(firstNameFieldName, "");
//		fieldValues.put(middleNameFieldName, "");
//		fieldValues.put(lastNameFieldName, "");
		fieldValues.put(fullNameFieldName, "");
		fieldValues.put(displayNameFieldName, "");
		fieldValues.put(descriptionFieldName, "");
		fieldValues.put(dateOfBirthFieldName, "");
		fieldValues.put(genderFieldName, "");
		fieldValues.put(personalIDFieldName, "");
		fieldValues.put(createdFieldName, "");

		updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
//		firstNameField.setContent((String) fieldValues.get(firstNameFieldName));
//		middleNameField.setContent((String) fieldValues.get(middleNameFieldName));
//		lastNameField.setContent((String) fieldValues.get(lastNameFieldName));
		fullNameField.setContent((String) fieldValues.get(fullNameFieldName));
		displayNameField.setContent((String) fieldValues.get(displayNameFieldName));
		descriptionField.setContent((String) fieldValues.get(descriptionFieldName));
		StringTokenizer date = new StringTokenizer((String) fieldValues.get(dateOfBirthFieldName), " -");

		if (date.hasMoreTokens()) {
			dateOfBirthField.setYear(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			dateOfBirthField.setMonth(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			dateOfBirthField.setDay(date.nextToken());
		}

		genderField.setSelectedElement((String) fieldValues.get(genderFieldName));
		personalIDField.setContent((String) fieldValues.get(personalIDFieldName));

		StringTokenizer created = new StringTokenizer((String) fieldValues.get(createdFieldName), " -");
		if (created.hasMoreTokens()) {
			createdField.setYear(created.nextToken());
		}
		if (created.hasMoreTokens()) {
			createdField.setMonth(created.nextToken());
		}
		if (created.hasMoreTokens()) {
			createdField.setDay(created.nextToken());
		}
	}

	public void initializeFields() {
//		firstNameField = new TextInput(firstNameFieldName);
//		firstNameField.setLength(12);
//
//		middleNameField = new TextInput(middleNameFieldName);
//		middleNameField.setLength(5);
//
//		lastNameField = new TextInput(lastNameFieldName);
//		lastNameField.setLength(12);

		fullNameField = new TextInput(fullNameFieldName);
		fullNameField.setLength(20);

		displayNameField = new TextInput(displayNameFieldName);
		displayNameField.setLength(12);
		displayNameField.setMaxlength(20);

		descriptionField = new TextArea(descriptionFieldName);
		descriptionField.setHeight(5);
		descriptionField.setWidth(42);
		descriptionField.setWrap(true);

		dateOfBirthField = new DateInput(dateOfBirthFieldName);
		IWTimestamp time = IWTimestamp.RightNow();
		dateOfBirthField.setYearRange(time.getYear(), time.getYear() - 100);

		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		genderField = new DropdownMenu(genderFieldName);
		genderField.addMenuElement("", iwrb.getLocalizedString(genderFieldName,"Gender"));

		Collection genders = null;
		try {
			GenderHome g = (GenderHome) IDOLookup.getHome(Gender.class);
			genders = g.findAllGenders();
		}
		catch (Exception ex) {
			// do nothing
		}

		Iterator iter = genders.iterator();
		while (iter.hasNext()) {
			Gender item = (Gender) iter.next();
			try {
				genderField.addMenuElement(((Integer) item.getPrimaryKey()).intValue(), item.getName());
			}
			catch (Exception ex) {
			}
		}

		personalIDField = new TextInput(personalIDFieldName);
		personalIDField.setLength(12);

		createdField = new DateInput(createdFieldName);
		createdField.setYearRange(time.getYear(), time.getYear() - 50);

	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

//		firstNameText = getTextObject();		
//		firstNameText.setText(iwrb.getLocalizedString(firstNameFieldName,"First name"));
//
//		middleNameText = getTextObject();
//		middleNameText.setText(iwrb.getLocalizedString(middleNameFieldName,"Middle name"));
//
//		lastNameText = getTextObject();
//		lastNameText.setText(iwrb.getLocalizedString(lastNameFieldName,"Last name"));

		fullNameText = getTextObject();		
		fullNameText.setText(iwrb.getLocalizedString(fullNameFieldName,"Name"));

		displayNameText = getTextObject();
		displayNameText.setText(iwrb.getLocalizedString(displayNameFieldName,"Display name"));

		descriptionText = getTextObject();
		descriptionText.setText(iwrb.getLocalizedString(descriptionFieldName,"Description"));

		dateOfBirthText = getTextObject();
		dateOfBirthText.setText(iwrb.getLocalizedString(dateOfBirthFieldName,"Date of birth"));

		genderText = getTextObject();
		genderText.setText(iwrb.getLocalizedString(genderFieldName,"Gender"));

		personalIDText = getTextObject();
		personalIDText.setText(iwrb.getLocalizedString(personalIDFieldName,"Personal ID"));

		createdText = getTextObject();
		createdText.setText(iwrb.getLocalizedString(createdFieldName,"Created"));

	}

	public void lineUpFields() {
		resize(1, 3);

		//First Part (names)
		Table nameTable = new Table(3, 2);
		nameTable.setWidth("100%");
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setHeight(1, columnHeight);
		nameTable.setHeight(2, columnHeight);
		nameTable.setHeight(3, columnHeight);

		nameTable.add(personalIDText, 1, 1);
		nameTable.add(personalIDField, 2, 1);
		nameTable.add(displayNameText, 1, 2);
		nameTable.add(displayNameField, 2, 2);
		nameTable.add(genderText, 1, 3);
		nameTable.add(genderField, 2, 3);
		add(nameTable, 1, 1);
		//First Part ends

		//Second Part (Date of birth)
		Table dateofbirthTable = new Table(2, 2);
		dateofbirthTable.setCellpadding(0);
		dateofbirthTable.setCellspacing(0);
		dateofbirthTable.setHeight(1, columnHeight);
		dateofbirthTable.setHeight(2, columnHeight);
//		dateofbirthTable.setHeight(3, columnHeight);
//		dateofbirthTable.add(personalIDText, 1, 1);
//		dateofbirthTable.add(personalIDField, 2, 1);
		dateofbirthTable.add(dateOfBirthText, 1, 1);
		dateofbirthTable.add(dateOfBirthField, 2, 1);
		dateofbirthTable.add(createdText, 1, 2);
		dateofbirthTable.add(createdField, 2, 2);
		add(dateofbirthTable, 1, 2);
		//Second Part Ends

		//Third Part (description)
		Table descriptionTable = new Table(1, 2);
		descriptionTable.setCellpadding(0);
		descriptionTable.setCellspacing(0);
		descriptionTable.setHeight(1, columnHeight);
		descriptionTable.add(descriptionText, 1, 1);
		descriptionTable.add(descriptionField, 1, 2);
		add(descriptionTable, 1, 3);
		//Third Part ends
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

//			String fname = iwc.getParameter(firstNameFieldName);
//			String mname = iwc.getParameter(middleNameFieldName);
//			String lname = iwc.getParameter(lastNameFieldName);
			String name = iwc.getParameter(fullNameFieldName);

			String dname = iwc.getParameter(displayNameFieldName);
			String desc = iwc.getParameter(descriptionFieldName);
			String dateofbirth = iwc.getParameter(dateOfBirthFieldName);
			String gender = iwc.getParameter(genderFieldName);
			String personalID = iwc.getParameter(personalIDFieldName);
			String created = iwc.getParameter(createdFieldName);

//			if (fname != null) {
//				fieldValues.put(firstNameFieldName, fname);
//			}
//			if (mname != null) {
//				fieldValues.put(middleNameFieldName, mname);
//			}
//			if (lname != null) {
//				fieldValues.put(lastNameFieldName, lname);
//			}
			if (name != null) {
				fieldValues.put(fullNameFieldName, name);
			}	
			if (dname != null) {
				fieldValues.put(displayNameFieldName, dname);
			}
			if (desc != null) {
				fieldValues.put(descriptionFieldName, desc);
			}
			if (dateofbirth != null) {
				fieldValues.put(dateOfBirthFieldName, dateofbirth);
			}
			if (gender != null) {
				fieldValues.put(genderFieldName, gender);
			}
			if (personalID != null) {
				fieldValues.put(personalIDFieldName, personalID);
			}
			if (created != null) {
				fieldValues.put(createdFieldName, created);
			}

			updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {
				IWTimestamp dateOfBirthTS = null;
				String st = (String) fieldValues.get(dateOfBirthFieldName);
				Integer gen = (fieldValues.get(genderFieldName).equals("")) ? null : new Integer((String) fieldValues.get(genderFieldName));
				if (st != null && !st.equals("")) {
					try {
						dateOfBirthTS = new IWTimestamp(st);
					}
					catch (IllegalArgumentException iae) {
						dateOfBirthTS = null;
					}
				}

				IWTimestamp createdTS = null;
				String createdString = (String) fieldValues.get(createdFieldName);
				if (createdString != null & !createdString.equals("")) {
					try {
						createdTS = new IWTimestamp(createdString);
					}
					catch (IllegalArgumentException iae) {
						createdTS = null;
					}
				}

				super.getUserBusiness(iwc).updateUser(
					getUserId(),
					null,
					null,
					null,
					(String) fieldValues.get(displayNameFieldName),
					(String) fieldValues.get(descriptionFieldName),
					gen,
					(String) fieldValues.get(personalIDFieldName),
					dateOfBirthTS,
					null,
				    (String) fieldValues.get(fullNameFieldName)
					);
				if (createdTS != null)
					getUser().setCreated(createdTS.getTimestamp());
			}
		}
		catch (Exception e) {
			//return false;
			e.printStackTrace(System.err);
			throw new RuntimeException("update user exception");
		}
		return true;
	}

	public void initFieldContents() {

		try {
			User user = getUser();

//			fieldValues.put(firstNameFieldName, (user.getFirstName() != null) ? user.getFirstName() : "");
//			fieldValues.put(middleNameFieldName, (user.getMiddleName() != null) ? user.getMiddleName() : "");
//			fieldValues.put(lastNameFieldName, (user.getLastName() != null) ? user.getLastName() : "");
			fieldValues.put(fullNameFieldName, (user.getName() != null) ? user.getLastName() : "");
			fieldValues.put(displayNameFieldName, (user.getDisplayName() != null) ? user.getDisplayName() : "");
			fieldValues.put(descriptionFieldName, (user.getDescription() != null) ? user.getDescription() : "");
			fieldValues.put(dateOfBirthFieldName, (user.getDateOfBirth() != null) ? new IWTimestamp(user.getDateOfBirth()).toSQLDateString() : "");
			fieldValues.put(genderFieldName, (user.getGenderID() != -1) ? Integer.toString(user.getGenderID()) : "");
			fieldValues.put(personalIDFieldName, (user.getPersonalID() != null) ? user.getPersonalID() : "");
			fieldValues.put(createdFieldName, (user.getCreated() != null) ? new IWTimestamp(user.getCreated()).toSQLDateString() : "");
			updateFieldsDisplayStatus();

		}
		catch (Exception e) {
			System.err.println("GeneralUserInfoTab error initFieldContents, userId : " + getUserId());
		}

	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

} // Class GeneralUserInfoTab