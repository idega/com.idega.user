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
	
	private TextInput firstNameField;
	private TextInput middleNameField;
	private TextInput lastNameField;
	private TextInput displayNameField;
	private TextArea descriptionField;
	private DateInput dateOfBirthField;
	private DropdownMenu genderField;
	private TextInput personalIDField;
	private DateInput createdField;

	private String firstNameFieldName;
	private String middleNameFieldName;
	private String lastNameFieldName;
	private String displayNameFieldName;
	private String descriptionFieldName;
	private String dateOfBirthFieldName;
	private String genderFieldName;
	private String personalIDFieldName;
	private String createdFieldName;

	private Text firstNameText;
	private Text middleNameText;
	private Text lastNameText;
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
		
//		this.setName("General");
	}

	public GeneralUserInfoTab(int userId) {
		this();
		this.setUserID(userId);
	}

	public void initializeFieldNames() {
		firstNameFieldName = "usr_info_UMfname";
		middleNameFieldName = "usr_info_UMmname";
		lastNameFieldName = "usr_info_UMlname";
		displayNameFieldName = "usr_info_UMdname";
		descriptionFieldName = "usr_info_UMdesc";
		dateOfBirthFieldName = "usr_info_UMdateofbirth";
		genderFieldName = "usr_info_UMgender";
		personalIDFieldName = "usr_info_UMpersonalID";
		createdFieldName = "usr_info_UMcreated";
	}

	public void initializeFieldValues() {
		fieldValues.put(this.firstNameFieldName, "");
		fieldValues.put(this.middleNameFieldName, "");
		fieldValues.put(this.lastNameFieldName, "");
		fieldValues.put(this.displayNameFieldName, "");
		fieldValues.put(this.descriptionFieldName, "");
		fieldValues.put(this.dateOfBirthFieldName, "");
		fieldValues.put(this.genderFieldName, "");
		fieldValues.put(this.personalIDFieldName, "");
		fieldValues.put(this.createdFieldName, "");

		this.updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
		firstNameField.setContent((String) fieldValues.get(this.firstNameFieldName));

		middleNameField.setContent((String) fieldValues.get(this.middleNameFieldName));

		lastNameField.setContent((String) fieldValues.get(this.lastNameFieldName));

		displayNameField.setContent((String) fieldValues.get(this.displayNameFieldName));

		descriptionField.setContent((String) fieldValues.get(this.descriptionFieldName));

		StringTokenizer date = new StringTokenizer((String) fieldValues.get(this.dateOfBirthFieldName), " -");

		if (date.hasMoreTokens()) {
			dateOfBirthField.setYear(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			dateOfBirthField.setMonth(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			dateOfBirthField.setDay(date.nextToken());
		}

		genderField.setSelectedElement((String) fieldValues.get(this.genderFieldName));

		personalIDField.setContent((String) fieldValues.get(this.personalIDFieldName));

		StringTokenizer created = new StringTokenizer((String) fieldValues.get(this.createdFieldName), " -");
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
		firstNameField = new TextInput(firstNameFieldName);
		firstNameField.setLength(12);

		middleNameField = new TextInput(middleNameFieldName);
		middleNameField.setLength(5);

		lastNameField = new TextInput(lastNameFieldName);
		lastNameField.setLength(12);

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

		firstNameText = getTextObject();		
		firstNameText.setText(iwrb.getLocalizedString(firstNameFieldName,"First name"));

		middleNameText = getTextObject();
		middleNameText.setText(iwrb.getLocalizedString(middleNameFieldName,"Middle name"));

		lastNameText = getTextObject();
		lastNameText.setText(iwrb.getLocalizedString(lastNameFieldName,"Last name"));

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
		this.resize(1, 3);

		//First Part (names)
		Table nameTable = new Table(4, 3);
		nameTable.setWidth("100%");
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setHeight(1, columnHeight);
		nameTable.setHeight(2, columnHeight);
		nameTable.setHeight(3, columnHeight);

		nameTable.add(firstNameText, 1, 1);
		nameTable.add(this.firstNameField, 2, 1);
		nameTable.add(middleNameText, 3, 1);
		nameTable.add(this.middleNameField, 4, 1);
		nameTable.add(lastNameText, 1, 2);
		nameTable.add(this.lastNameField, 2, 2);
		nameTable.add(displayNameText, 1, 3);
		nameTable.add(this.displayNameField, 2, 3);
		nameTable.add(genderText, 3, 3);
		nameTable.add(this.genderField, 4, 3);
		this.add(nameTable, 1, 1);
		//First Part ends

		//Second Part (Date of birth)
		Table dateofbirthTable = new Table(2, 3);
		dateofbirthTable.setCellpadding(0);
		dateofbirthTable.setCellspacing(0);
		dateofbirthTable.setHeight(1, columnHeight);
		dateofbirthTable.setHeight(2, columnHeight);
		dateofbirthTable.setHeight(3, columnHeight);
		dateofbirthTable.add(personalIDText, 1, 1);
		dateofbirthTable.add(personalIDField, 2, 1);
		dateofbirthTable.add(dateOfBirthText, 1, 2);
		dateofbirthTable.add(this.dateOfBirthField, 2, 2);
		dateofbirthTable.add(createdText, 1, 3);
		dateofbirthTable.add(this.createdField, 2, 3);
		this.add(dateofbirthTable, 1, 2);
		//Second Part Ends

		//Third Part (description)
		Table descriptionTable = new Table(1, 2);
		descriptionTable.setCellpadding(0);
		descriptionTable.setCellspacing(0);
		descriptionTable.setHeight(1, columnHeight);
		descriptionTable.add(descriptionText, 1, 1);
		descriptionTable.add(this.descriptionField, 1, 2);
		this.add(descriptionTable, 1, 3);
		//Third Part ends
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

			String fname = iwc.getParameter(this.firstNameFieldName);
			String mname = iwc.getParameter(this.middleNameFieldName);
			String lname = iwc.getParameter(this.lastNameFieldName);

			String dname = iwc.getParameter(this.displayNameFieldName);
			String desc = iwc.getParameter(this.descriptionFieldName);
			String dateofbirth = iwc.getParameter(this.dateOfBirthFieldName);
			String gender = iwc.getParameter(this.genderFieldName);
			String personalID = iwc.getParameter(this.personalIDFieldName);
			String created = iwc.getParameter(this.createdFieldName);

			if (fname != null) {
				fieldValues.put(this.firstNameFieldName, fname);
			}
			if (mname != null) {
				fieldValues.put(this.middleNameFieldName, mname);
			}
			if (lname != null) {
				fieldValues.put(this.lastNameFieldName, lname);
			}
			if (dname != null) {
				fieldValues.put(this.displayNameFieldName, dname);
			}
			if (desc != null) {
				fieldValues.put(this.descriptionFieldName, desc);
			}
			if (dateofbirth != null) {
				fieldValues.put(this.dateOfBirthFieldName, dateofbirth);
			}
			if (gender != null) {
				fieldValues.put(this.genderFieldName, gender);
			}
			if (personalID != null) {
				fieldValues.put(this.personalIDFieldName, personalID);
			}
			if (created != null) {
				fieldValues.put(this.createdFieldName, created);
			}

			this.updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {
				IWTimestamp dateOfBirthTS = null;
				String st = (String) fieldValues.get(this.dateOfBirthFieldName);
				Integer gen = (fieldValues.get(this.genderFieldName).equals("")) ? null : new Integer((String) fieldValues.get(this.genderFieldName));
				if (st != null && !st.equals("")) {
					try {
						dateOfBirthTS = new IWTimestamp(st);
					}
					catch (IllegalArgumentException iae) {
						dateOfBirthTS = null;
					}
				}

				IWTimestamp createdTS = null;
				String createdString = (String) fieldValues.get(this.createdFieldName);
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
					(String) fieldValues.get(this.firstNameFieldName),
					(String) fieldValues.get(this.middleNameFieldName),
					(String) fieldValues.get(this.lastNameFieldName),
					(String) fieldValues.get(this.displayNameFieldName),
					(String) fieldValues.get(this.descriptionFieldName),
					gen,
					(String) fieldValues.get(this.personalIDFieldName),
					dateOfBirthTS,
					null);
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

			fieldValues.put(this.firstNameFieldName, (user.getFirstName() != null) ? user.getFirstName() : "");
			fieldValues.put(this.middleNameFieldName, (user.getMiddleName() != null) ? user.getMiddleName() : "");
			fieldValues.put(this.lastNameFieldName, (user.getLastName() != null) ? user.getLastName() : "");
			fieldValues.put(this.displayNameFieldName, (user.getDisplayName() != null) ? user.getDisplayName() : "");
			fieldValues.put(this.descriptionFieldName, (user.getDescription() != null) ? user.getDescription() : "");
			fieldValues.put(this.dateOfBirthFieldName, (user.getDateOfBirth() != null) ? new IWTimestamp(user.getDateOfBirth()).toSQLDateString() : "");
			fieldValues.put(this.genderFieldName, (user.getGenderID() != -1) ? Integer.toString(user.getGenderID()) : "");
			fieldValues.put(this.personalIDFieldName, (user.getPersonalID() != null) ? user.getPersonalID() : "");
			fieldValues.put(this.createdFieldName, (user.getCreated() != null) ? new IWTimestamp(user.getCreated()).toSQLDateString() : "");
			this.updateFieldsDisplayStatus();

		}
		catch (Exception e) {
			System.err.println("GeneralUserInfoTab error initFieldContents, userId : " + getUserId());
		}

	}

} // Class GeneralUserInfoTab
