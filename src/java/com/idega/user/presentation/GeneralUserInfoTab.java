package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import com.idega.block.media.presentation.ImageInserter;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Gender;
import com.idega.user.data.GenderHome;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */
public class GeneralUserInfoTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_info_tab_name";
	private static final String DEFAULT_TAB_NAME = "General";
	
	private static final String HELP_TEXT_KEY = "tabbed_property_panel";

	private TextInput idField;
	private TextInput fullNameField;
	private TextInput displayNameField;
	private TextArea descriptionField;
	private DateInput dateOfBirthField;
	private DropdownMenu genderField;
	private TextInput personalIDField;
	private DateInput createdField;
	private ImageInserter imageField;
	private CheckBox removeImageField;

	private String idFieldName;
	private String fullNameFieldName;
	private String displayNameFieldName;
	private String descriptionFieldName;
	private String dateOfBirthFieldName;
	private String genderFieldName;
	private String personalIDFieldName;
	private String createdFieldName;
	private String imageFieldName;
	private String removeImageFieldName;

	private Text idText;
	private Text fullNameText;
	private Text displayNameText;
	private Text descriptionText;
	private Text dateOfBirthText;
	private Text genderText;
	private Text personalIDText;
	private Text createdText;
	private Text imageText;
	private Text removeImageText;
	
	private User user = null;
	private int systemImageId = -1;

	public GeneralUserInfoTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
	}

	public GeneralUserInfoTab(int userId) {
		this();
		setUserID(userId);
	}

	public void initializeFieldNames() {
		idFieldName = "usr_info_UMid";
		fullNameFieldName = "usr_info_UMflname";
		displayNameFieldName = "usr_info_UMdname";
		descriptionFieldName = "usr_info_UMdesc";
		dateOfBirthFieldName = "usr_info_UMdateofbirth";
		genderFieldName = "usr_info_UMgender";
		personalIDFieldName = "usr_info_UMpersonalID";
		createdFieldName = "usr_info_UMcreated";
		imageFieldName = "usr_imag_userSystemImageId";
		removeImageFieldName = "image_removeImageFieldName";
	}

	public void initializeFieldValues() {
		fieldValues.put(idFieldName, "");
		fieldValues.put(fullNameFieldName, "");
		fieldValues.put(displayNameFieldName, "");
		fieldValues.put(descriptionFieldName, "");
		fieldValues.put(dateOfBirthFieldName, "");
		fieldValues.put(genderFieldName, "");
		fieldValues.put(personalIDFieldName, "");
		fieldValues.put(createdFieldName, "");
		systemImageId = -1;
		fieldValues.put(removeImageFieldName, new Boolean(false));

		updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
		
		idField.setContent((String) fieldValues.get(idFieldName));		
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

		IWContext iwc = IWContext.getInstance();
		boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff")!=null;
		if (showISStuff)
			personalIDField.setDisabled(true);

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
		
		imageField.setImageId(systemImageId);
		removeImageField.setChecked(((Boolean)fieldValues.get(removeImageFieldName)).booleanValue());
	}

	public void initializeFields() {
		idField = new TextInput(idFieldName);
		idField.setLength(20);//changed from 12 - birna
		
		fullNameField = new TextInput(fullNameFieldName);
		fullNameField.setLength(20);

		displayNameField = new TextInput(displayNameFieldName);
		displayNameField.setLength(20);//changed from 12 - birna
		displayNameField.setMaxlength(20);

		descriptionField = new TextArea(descriptionFieldName);
		descriptionField.setHeight(7);//changed from (5) - birna
		descriptionField.setWidth(42); //changed from (42)
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
				genderField.addMenuElement(((Integer) item.getPrimaryKey()).intValue(), iwrb.getLocalizedString(item.getName(),item.getName()));
			}
			catch (Exception ex) {
			}
		}

		personalIDField = new TextInput(personalIDFieldName);
		personalIDField.setLength(20); //changed from 12 - birna
		
		createdField = new DateInput(createdFieldName);
		createdField.setYearRange(time.getYear(), time.getYear() - 50);
		
		imageField = new ImageInserter(imageFieldName + getUserId());
		imageField.setWidth(String.valueOf(90));
		imageField.setHeight(String.valueOf(138));
		imageField.setHasUseBox(false);
		removeImageField = new CheckBox(removeImageFieldName);

	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		idText = new Text();//getTextObject();
		idText.setText(iwrb.getLocalizedString(idFieldName,"ID") + ":");
//		idText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		fullNameText = new Text();//getTextObject();		
		fullNameText.setText(iwrb.getLocalizedString(fullNameFieldName,"Name") + ":");
//		fullNameText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		displayNameText = new Text();//getTextObject();
		displayNameText.setText(iwrb.getLocalizedString(displayNameFieldName,"Display name") + ":");
//		displayNameText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		descriptionText = new Text();//getTextObject();
		descriptionText.setText(iwrb.getLocalizedString(descriptionFieldName,"Description") + ":");
//		descriptionText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		dateOfBirthText = new Text();// getTextObject();
		dateOfBirthText.setText(iwrb.getLocalizedString(dateOfBirthFieldName,"Date of birth") + ":");
//		dateOfBirthText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		genderText = new Text(); //getTextObject();
		genderText.setText(iwrb.getLocalizedString(genderFieldName,"Gender") + ":");
//		genderText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		personalIDText = new Text();//getTextObject();
		personalIDText.setText(iwrb.getLocalizedString(personalIDFieldName,"Personal ID") + ":");
//		personalIDText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		createdText = new Text();//getTextObject();
		createdText.setText(iwrb.getLocalizedString(createdFieldName,"Created") + ":");
//		createdText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		
		imageText = new Text();//getTextObject();
		imageText.setText(iwrb.getLocalizedString(imageFieldName, "Image") + ":");
//		imageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		
		removeImageText = new Text();//getTextObject();
		removeImageText.setText(iwrb.getLocalizedString(removeImageFieldName, "do not show an image"));
//		removeImageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
	}
	
	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle( UserConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
		
	}

	public void lineUpFields() {
		resize(1, 4);
		
		//First Part (names)
		Table nameTable = new Table(3, 6); //changed from (2,5) - birna
		nameTable.setWidth("100%");
		nameTable.setCellpadding(3);
		nameTable.setCellspacing(3);
//		nameTable.setHeight(1, rowHeight);
//		nameTable.setHeight(2, rowHeight);
//		nameTable.setHeight(3, rowHeight);
//		nameTable.setHeight(4, rowHeight);

		nameTable.add(fullNameText,1,1);//(idText, 1, 1); 
		nameTable.add(fullNameField,1,2);//(idField, 2, 1);
		nameTable.add(personalIDText,2,1);//(personalIDText, 1, 2);
		nameTable.add(personalIDField,2,2);//(personalIDField, 2, 2);
		nameTable.add(idText,1,3);//(fullNameText, 1, 3);
		nameTable.add(idField,1,4);//(fullNameField, 2, 3);
		nameTable.add(displayNameText,2,3);//(displayNameText, 1, 4);
		nameTable.add(displayNameField, 2, 4);
		nameTable.add(genderText, 1,5);
		nameTable.add(genderField,1,6);//(genderField, 2, 5);
		nameTable.mergeCells(3,2,3,5);
		nameTable.add(imageText,3,1);
		nameTable.add(imageField,3,2);
		nameTable.add(removeImageField,3,6);
		nameTable.add(Text.getNonBrakingSpace(),3,6);
		nameTable.add(removeImageText,3,6);
		add(nameTable, 1, 1);
		//First Part ends

		//Second Part (Date of birth)
		Table dateofbirthTable = new Table(2, 4);
		dateofbirthTable.setCellpadding(3);
		dateofbirthTable.setCellspacing(3);//changed from (0)
//		dateofbirthTable.setHeight(1, rowHeight);
//		dateofbirthTable.setHeight(2, rowHeight);
		dateofbirthTable.add(dateOfBirthText, 1, 1);
		dateofbirthTable.add(dateOfBirthField, 1, 2);//changed from ...,2,1) - birna
		dateofbirthTable.add(createdText, 1, 3);
		dateofbirthTable.add(createdField, 1, 4);//changed from ...,2,2) - birna
		add(dateofbirthTable, 1, 2);
		//Second Part Ends

		//Third Part (description)
		Table descriptionTable = new Table(1, 2);
		descriptionTable.setCellpadding(3);
		descriptionTable.setCellspacing(3);
		descriptionTable.setHeight(1, rowHeight);
		descriptionTable.add(descriptionText, 1, 1);
		descriptionTable.addBreak();
		descriptionTable.add(descriptionField, 1, 1);//changed from ...,1,2)
		add(descriptionTable, 1, 3);
		//Third Part ends
		
		Table helpTable = new Table(1,1);
		helpTable.setCellpadding(3);
		helpTable.setCellspacing(3);
		helpTable.add(getHelpButton());
		add(helpTable,1,4);
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {
			String name = iwc.getParameter(fullNameFieldName);
			String ID = iwc.getParameter(idFieldName);
			String dname = iwc.getParameter(displayNameFieldName);
			String desc = iwc.getParameter(descriptionFieldName);
			String dateofbirth = iwc.getParameter(dateOfBirthFieldName);
			String gender = iwc.getParameter(genderFieldName);
			String personalID = iwc.getParameter(personalIDFieldName);
			String created = iwc.getParameter(createdFieldName);
			String imageID = iwc.getParameter(imageFieldName + this.getUserId());

			if(ID!=null){
				fieldValues.put(idFieldName, ID);
			}
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
			if (imageID != null) {
				fieldValues.put(imageFieldName, imageID);
			}
			fieldValues.put(removeImageFieldName, new Boolean(iwc.isParameterSet(removeImageFieldName)));

			updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > 0) {
				
				if(getGroupID()>0){//temp remove with other IWMember stuff
					Group club = getClubForGroup(getGroup());
					if(club!=null){				
						boolean success = setClubMemberNumberForUser((String)fieldValues.get(idFieldName),getUser(),club);
						if(!success){//number already taken
							idField.setStyleAttribute("color:#FF0000"); 
						}
						else idField.setStyleAttribute("color:#000000"); 
					}
				}
				
				
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
			if (getUserId() > -1) {
	
				String image = (String)fieldValues.get(imageFieldName);
	
				if ((image != null) && (!image.equals("-1")) && (!image.equals(""))) {
					if (user == null)
						user = getUser();
					int tempId;
					if (((Boolean) fieldValues.get(removeImageFieldName)).booleanValue())  {
						user.setSystemImageID(null);
						// set variables to default values
						systemImageId = -1;
						fieldValues.put(imageFieldName, "-1");
						user.store();
						updateFieldsDisplayStatus();
					}
					else if ((tempId = Integer.parseInt(image)) != systemImageId) {
						systemImageId = tempId;
						user.setSystemImageID(systemImageId);
						user.store();
						updateFieldsDisplayStatus();
					}
	
					iwc.removeSessionAttribute(imageFieldName + getUserId());
	
				}
	
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("update user exception");
		}

		return true;
	}//end store

	public void initFieldContents() {

		try {
			User user = getUser();
			String memberNumber =null;
			try {
				memberNumber = getMemberNumber(getUser());
				imageField.setImSessionImageName(imageFieldName + getUserId());
				systemImageId = getSelectedImageId(user);
				
				if (systemImageId != -1) {
					fieldValues.put(this.imageFieldName, Integer.toString(systemImageId));
				}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}

			fieldValues.put(idFieldName, (memberNumber != null) ? memberNumber : "");
			fieldValues.put(fullNameFieldName, (user.getName() != null) ? user.getName() : "");
			fieldValues.put(displayNameFieldName, (user.getDisplayName() != null) ? user.getDisplayName() : "");
			fieldValues.put(descriptionFieldName, (user.getDescription() != null) ? user.getDescription() : "");
			fieldValues.put(dateOfBirthFieldName, (user.getDateOfBirth() != null) ? new IWTimestamp(user.getDateOfBirth()).toSQLDateString() : "");
			fieldValues.put(genderFieldName, (user.getGenderID() != -1) ? Integer.toString(user.getGenderID()) : "");
			fieldValues.put(personalIDFieldName, (user.getPersonalID() != null) ? user.getPersonalID() : "");
			fieldValues.put(createdFieldName, (user.getCreated() != null) ? new IWTimestamp(user.getCreated()).toSQLDateString() : "");
			fieldValues.put(removeImageFieldName, new Boolean(false));
			updateFieldsDisplayStatus();

		}
		catch (Exception e) {
			System.err.println("GeneralUserInfoTab error initFieldContents, userId : " + getUserId());
		}

	}



	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	
	
	//TODO Eiki inherit from this class and use plugin stuff
	//START REMOVE
	
	private String getMemberNumber(User user) throws RemoteException {
		String memberNumber = null;
		
		Group selectedGroup = getGroup();
		if(selectedGroup!=null){	
			Group club = getClubForGroup(selectedGroup);
			if(club!=null){
				memberNumber = getClubMemberNumberForUser(user,club);
			}
			else System.out.print("CLUB GROUP IS NULL");
		}
		else System.out.print("SELECTED GROUP IS NULL");
		
		return memberNumber;
	}
	
	/*
		* Returns the club that is a parent for this group.
	 */
	public Group getClubForGroup(Group group) throws EJBException, RemoteException{
		Collection parents = getGroupBusiness(this.getIWApplicationContext()).getParentGroupsRecursive(group);

		if(parents!=null && !parents.isEmpty()){
			Iterator iter = parents.iterator();
			while (iter.hasNext()) {
				Group parentGroup = (Group) iter.next();
				//if(IWMemberConstants.GROUP_TYPE_CLUB.equals(parentGroup.getGroupType())){
				if("iwme_club".equals(parentGroup.getGroupType())){
					return parentGroup;//there should only be one
				}
			}
		} 
		return null;
	}
	
	public String getClubMemberNumberForUser(User user, Group club){
		String id = user.getMetaData("CLUB_MEMB_NR_"+club.getPrimaryKey().toString());
		if(id!=null){
			return id;
		}else{
			return null;
		}	
	}
	
	/**
	 * @return false if number is already taken, else true
	 */
	public synchronized boolean setClubMemberNumberForUser(String number, User user, Group club){
		
		boolean setNumber = false;
		String clubId = club.getPrimaryKey().toString();
		
		if(number.equals("")){
			user.removeMetaData("CLUB_MEMB_NR_"+clubId);
			user.store();
			return true;
		}
		
		
		try {
			Collection users = getUserBusiness(getIWApplicationContext()).getUserHome().findUsersByMetaData("CLUB_MEMB_NR_"+clubId,number);
			
			if( users!=null && !users.isEmpty()){
				Iterator iter = users.iterator();
				
				while (iter.hasNext()) {
					User thingy = (User) iter.next();
					if(thingy.getPrimaryKey().equals(user.getPrimaryKey())){
						setNumber = true;//updating
					}
					break;//only one user should have this number
				}
			}
			else setNumber = true;
		}
		catch (EJBException e) {
			e.printStackTrace();
			return false;
		}
		catch (FinderException e) {
			setNumber = true;
		} 
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if(setNumber){
			user.setMetaData("CLUB_MEMB_NR_"+clubId,number);
			user.store();
			return true;
		}
		
		return false;
	}
	
	//END REMOVE
	private void setSelectedImageId() {
		try {
			String image = (String)fieldValues.get(this.imageFieldName);
			if ((image != null)
				&& (!image.equals("-1"))
				&& (!image.equals(""))
				&& (!image.equals("0"))) {
				systemImageId = Integer.parseInt(image);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}
	private int getSelectedImageId(User user) {
			try {
				int tempImageId = user.getSystemImageID();
				if ((systemImageId == -1) && (tempImageId != -1))
					systemImageId = tempImageId;
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
			}

			return systemImageId;
		}

} // Class GeneralUserInfoTab