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
import com.idega.user.util.ICUserConstants;
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
		this.idFieldName = "usr_info_UMid";
		this.fullNameFieldName = "usr_info_UMflname";
		this.displayNameFieldName = "usr_info_UMdname";
		this.descriptionFieldName = "usr_info_UMdesc";
		this.dateOfBirthFieldName = "usr_info_UMdateofbirth";
		this.genderFieldName = "usr_info_UMgender";
		this.personalIDFieldName = "usr_info_UMpersonalID";
		this.createdFieldName = "usr_info_UMcreated";
		this.imageFieldName = "usr_imag_userSystemImageId";
		this.removeImageFieldName = "image_removeImageFieldName";
	}

	public void initializeFieldValues() {
		this.fieldValues.put(this.idFieldName, "");
		this.fieldValues.put(this.fullNameFieldName, "");
		this.fieldValues.put(this.displayNameFieldName, "");
		this.fieldValues.put(this.descriptionFieldName, "");
		this.fieldValues.put(this.dateOfBirthFieldName, "");
		this.fieldValues.put(this.genderFieldName, "");
		this.fieldValues.put(this.personalIDFieldName, "");
		this.fieldValues.put(this.createdFieldName, "");
		this.systemImageId = -1;
		this.fieldValues.put(this.removeImageFieldName, new Boolean(false));

		updateFieldsDisplayStatus();
	}

	public void updateFieldsDisplayStatus() {
		
		this.idField.setContent((String) this.fieldValues.get(this.idFieldName));		
		this.fullNameField.setContent((String) this.fieldValues.get(this.fullNameFieldName));
		this.displayNameField.setContent((String) this.fieldValues.get(this.displayNameFieldName));
		this.descriptionField.setContent((String) this.fieldValues.get(this.descriptionFieldName));
		StringTokenizer date = new StringTokenizer((String) this.fieldValues.get(this.dateOfBirthFieldName), " -");

		if (date.hasMoreTokens()) {
			this.dateOfBirthField.setYear(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			this.dateOfBirthField.setMonth(date.nextToken());
		}
		if (date.hasMoreTokens()) {
			this.dateOfBirthField.setDay(date.nextToken());
		}

		this.genderField.setSelectedElement((String) this.fieldValues.get(this.genderFieldName));
		this.personalIDField.setContent((String) this.fieldValues.get(this.personalIDFieldName));

		IWContext iwc = IWContext.getInstance();
		boolean unlockPersonalIDField = iwc.getAccessController().hasRole(ICUserConstants.ROLE_KEY_EDIT_PERSONAL_ID,iwc);
		
		if (!unlockPersonalIDField){
			this.personalIDField.setDisabled(true);
		}
		
		StringTokenizer created = new StringTokenizer((String) this.fieldValues.get(this.createdFieldName), " -");
		if (created.hasMoreTokens()) {
			this.createdField.setYear(created.nextToken());
		}
		if (created.hasMoreTokens()) {
			this.createdField.setMonth(created.nextToken());
		}
		if (created.hasMoreTokens()) {
			this.createdField.setDay(created.nextToken());
		}
		
		this.imageField.setImageId(this.systemImageId);
		this.removeImageField.setChecked(((Boolean)this.fieldValues.get(this.removeImageFieldName)).booleanValue());
	}

	public void initializeFields() {
		this.idField = new TextInput(this.idFieldName);
		this.idField.setLength(20);//changed from 12 - birna
		
		this.fullNameField = new TextInput(this.fullNameFieldName);
		this.fullNameField.setLength(20);

		this.displayNameField = new TextInput(this.displayNameFieldName);
		this.displayNameField.setLength(20);//changed from 12 - birna
		this.displayNameField.setMaxlength(20);

		this.descriptionField = new TextArea(this.descriptionFieldName);
		this.descriptionField.setHeight(7);//changed from (5) - birna
		this.descriptionField.setWidth(42); //changed from (42)
		this.descriptionField.setWrap(true);

		this.dateOfBirthField = new DateInput(this.dateOfBirthFieldName);
		IWTimestamp time = IWTimestamp.RightNow();
		this.dateOfBirthField.setYearRange(time.getYear(), time.getYear() - 100);

		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		this.genderField = new DropdownMenu(this.genderFieldName);
		this.genderField.addMenuElement("", iwrb.getLocalizedString(this.genderFieldName,"Gender"));

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
				this.genderField.addMenuElement(((Integer) item.getPrimaryKey()).intValue(), iwrb.getLocalizedString(item.getName(),item.getName()));
			}
			catch (Exception ex) {
			}
		}

		this.personalIDField = new TextInput(this.personalIDFieldName);
		this.personalIDField.setLength(20); //changed from 12 - birna
		
		this.createdField = new DateInput(this.createdFieldName);
		this.createdField.setYearRange(time.getYear(), time.getYear() - 50);
		
		this.imageField = new ImageInserter(this.imageFieldName + getUserId());
		this.imageField.setWidth(String.valueOf(107));
		this.imageField.setMaxImageWidth(107);
		this.imageField.setHasUseBox(false);
		
		this.removeImageField = new CheckBox(this.removeImageFieldName);
		this.removeImageField.setWidth("10");
		this.removeImageField.setHeight("10");
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		this.idText = new Text(iwrb.getLocalizedString(this.idFieldName,"ID") + ":");
		this.idText.setBold();
		this.fullNameText = new Text(iwrb.getLocalizedString(this.fullNameFieldName,"Name") + ":");
		this.fullNameText.setBold();
		this.displayNameText = new Text(iwrb.getLocalizedString(this.displayNameFieldName,"Display name") + ":");
		this.displayNameText.setBold();
		this.descriptionText = new Text(iwrb.getLocalizedString(this.descriptionFieldName,"Description") + ":");
		this.descriptionText.setBold();
		this.dateOfBirthText = new Text(iwrb.getLocalizedString(this.dateOfBirthFieldName,"Date of birth") + ":");
		this.dateOfBirthText.setBold();
		this.genderText = new Text(iwrb.getLocalizedString(this.genderFieldName,"Gender") + ":");
		this.genderText.setBold();
		this.personalIDText = new Text(iwrb.getLocalizedString(this.personalIDFieldName,"Personal ID") + ":");
		this.personalIDText.setBold();
		this.createdText = new Text(iwrb.getLocalizedString(this.createdFieldName,"Created") + ":");
		this.createdText.setBold();
		this.imageText = new Text(iwrb.getLocalizedString(this.imageFieldName, "Image") + ":");
		this.imageText.setBold();
		this.removeImageText = new Text(iwrb.getLocalizedString(this.removeImageFieldName, "do not show an image"));
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
		resize(1, 1);
		
		Table table = new Table(); //changed from (2,5) - birna
		table.setWidth("100%");
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setBorder(0);

		table.add(this.fullNameText,1,1);//(idText, 1, 1);
		table.add(Text.getBreak(), 1, 1);
		table.add(this.fullNameField,1,1);//(idField, 2, 1);
		
		table.add(this.personalIDText,2,1);//(personalIDText, 1, 2);
		table.add(Text.getBreak(), 2, 1);
		table.add(this.personalIDField,2,1);//(personalIDField, 2, 2);
		
		table.add(this.idText,1,2);//(fullNameText, 1, 3);
		table.add(Text.getBreak(), 1, 2);
		table.add(this.idField,1,2);//(fullNameField, 2, 3);
		
		table.add(this.displayNameText,2,2);//(displayNameText, 1, 4);
		table.add(Text.getBreak(), 2, 2);
		table.add(this.displayNameField, 2, 2);
		
		table.add(this.genderText, 1,3);
		table.add(Text.getBreak(), 1, 3);
		table.add(this.genderField,1,3);//(genderField, 2, 5);
		
		table.mergeCells(1, 4, 2, 4);
		table.add(this.dateOfBirthText, 1, 4);
		table.add(Text.getBreak(), 1, 4);
		table.add(this.dateOfBirthField, 1, 4);//changed from ...,2,1) - birna

		table.mergeCells(1, 5, 2, 5);
		table.add(this.createdText, 1, 5);
		table.add(Text.getBreak(), 1, 5);
		table.add(this.createdField, 1, 5);//changed from ...,2,2) - birna

		IWBundle iwb = getBundle(IWContext.getInstance());
		String displayDescription = iwb.getProperty("display_description_column_in_grouppropertywindow","true");
		if (IWContext.getInstance().isSuperAdmin() || displayDescription.equalsIgnoreCase("true")) {
			table.mergeCells(1, 6, 2, 6);
			table.add(this.descriptionText, 1, 6);
			table.add(Text.getBreak(), 1, 6);
			table.add(this.descriptionField, 1, 6);//changed from ...,1,2)
		}

		table.mergeCells(3,1,3,6);
		table.setVerticalAlignment(3, 1, Table.VERTICAL_ALIGN_TOP);
		table.add(this.imageText,3,1);
		table.add(Text.getBreak(), 3, 1);
		table.add(this.imageField,3,1);
		
		table.add(this.removeImageField,3,1);
		//table.add(Text.getNonBrakingSpace(),3,1);
		table.add(this.removeImageText,3,1);
		add(table, 1, 1);
	}
	
	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());		
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {
			String name = iwc.getParameter(this.fullNameFieldName);
			String ID = iwc.getParameter(this.idFieldName);
			String dname = iwc.getParameter(this.displayNameFieldName);
			String desc = iwc.getParameter(this.descriptionFieldName);
			String dateofbirth = iwc.getParameter(this.dateOfBirthFieldName);
			String gender = iwc.getParameter(this.genderFieldName);
			String personalID = iwc.getParameter(this.personalIDFieldName);
			String created = iwc.getParameter(this.createdFieldName);
			String imageID = iwc.getParameter(this.imageFieldName + this.getUserId());

			if(ID!=null){
				this.fieldValues.put(this.idFieldName, ID);
			}
			if (name != null) {
				this.fieldValues.put(this.fullNameFieldName, name);
			}	
			if (dname != null) {
				this.fieldValues.put(this.displayNameFieldName, dname);
			}
			if (desc != null) {
				this.fieldValues.put(this.descriptionFieldName, desc);
			}
			if (dateofbirth != null) {
				this.fieldValues.put(this.dateOfBirthFieldName, dateofbirth);
			}
			if (gender != null) {
				this.fieldValues.put(this.genderFieldName, gender);
			}
			if (personalID != null) {
				this.fieldValues.put(this.personalIDFieldName, personalID);
			}
			if (created != null) {
				this.fieldValues.put(this.createdFieldName, created);
			}
			if (imageID != null) {
				this.fieldValues.put(this.imageFieldName, imageID);
			}
			this.fieldValues.put(this.removeImageFieldName, new Boolean(iwc.isParameterSet(this.removeImageFieldName)));

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
						boolean success = setClubMemberNumberForUser((String)this.fieldValues.get(this.idFieldName),getUser(),club);
						if(!success){//number already taken
							this.idField.setStyleAttribute("color:#FF0000"); 
						}
						else {
							this.idField.setStyleAttribute("color:#000000");
						} 
					}
				}
				
				
				IWTimestamp dateOfBirthTS = null;
				String st = (String) this.fieldValues.get(this.dateOfBirthFieldName);
				Integer gen = (this.fieldValues.get(this.genderFieldName).equals("")) ? null : new Integer((String) this.fieldValues.get(this.genderFieldName));
				if (st != null && !st.equals("")) {
					try {
						dateOfBirthTS = new IWTimestamp(st);
					}
					catch (IllegalArgumentException iae) {
						dateOfBirthTS = null;
					}
				}

				IWTimestamp createdTS = null;
				String createdString = (String) this.fieldValues.get(this.createdFieldName);
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
					(String) this.fieldValues.get(this.displayNameFieldName),
					(String) this.fieldValues.get(this.descriptionFieldName),
					gen,
					(String) this.fieldValues.get(this.personalIDFieldName),
					dateOfBirthTS,
					null,
				    (String) this.fieldValues.get(this.fullNameFieldName)
					);
				if (createdTS != null) {
					getUser().setCreated(createdTS.getTimestamp());
				}
			}
			if (getUserId() > -1) {
	
				String image = (String)this.fieldValues.get(this.imageFieldName);
	
				if ((image != null) && (!image.equals("-1")) && (!image.equals(""))) {
					if (this.user == null) {
						this.user = getUser();
					}
					int tempId;
					if (((Boolean) this.fieldValues.get(this.removeImageFieldName)).booleanValue())  {
						this.user.setSystemImageID(null);
						// set variables to default values
						this.systemImageId = -1;
						this.fieldValues.put(this.imageFieldName, "-1");
						this.user.store();
						updateFieldsDisplayStatus();
					}
					else if ((tempId = Integer.parseInt(image)) != this.systemImageId) {
						this.systemImageId = tempId;
						this.user.setSystemImageID(this.systemImageId);
						this.user.store();
						updateFieldsDisplayStatus();
					}
	
					iwc.removeSessionAttribute(this.imageFieldName + getUserId());
	
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
				this.imageField.setImSessionImageName(this.imageFieldName + getUserId());
				this.systemImageId = getSelectedImageId(user);
				
				if (this.systemImageId != -1) {
					this.fieldValues.put(this.imageFieldName, Integer.toString(this.systemImageId));
				}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}

			this.fieldValues.put(this.idFieldName, (memberNumber != null) ? memberNumber : "");
			this.fieldValues.put(this.fullNameFieldName, (user.getName() != null) ? user.getName() : "");
			this.fieldValues.put(this.displayNameFieldName, (user.getDisplayName() != null) ? user.getDisplayName() : "");
			this.fieldValues.put(this.descriptionFieldName, (user.getDescription() != null) ? user.getDescription() : "");
			this.fieldValues.put(this.dateOfBirthFieldName, (user.getDateOfBirth() != null) ? new IWTimestamp(user.getDateOfBirth()).toSQLDateString() : "");
			this.fieldValues.put(this.genderFieldName, (user.getGenderID() != -1) ? Integer.toString(user.getGenderID()) : "");
			this.fieldValues.put(this.personalIDFieldName, (user.getPersonalID() != null) ? user.getPersonalID() : "");
			this.fieldValues.put(this.createdFieldName, (user.getCreated() != null) ? new IWTimestamp(user.getCreated()).toSQLDateString() : "");
			this.fieldValues.put(this.removeImageFieldName, new Boolean(false));
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
			//else System.out.print("CLUB GROUP IS NULL");
		}
		//else System.out.print("SELECTED GROUP IS NULL");
		
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
			else {
				setNumber = true;
			}
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
			String image = (String)this.fieldValues.get(this.imageFieldName);
			if ((image != null)
				&& (!image.equals("-1"))
				&& (!image.equals(""))
				&& (!image.equals("0"))) {
				this.systemImageId = Integer.parseInt(image);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}
	private int getSelectedImageId(User user) {
			try {
				int tempImageId = user.getSystemImageID();
				if ((this.systemImageId == -1) && (tempImageId != -1)) {
					this.systemImageId = tempImageId;
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
			}

			return this.systemImageId;
		}

} // Class GeneralUserInfoTab