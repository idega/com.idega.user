/*
 * Created on 6.7.2003
 *
 */
package com.idega.user.presentation;
import is.idega.idegaweb.member.presentation.FamilyRelationConnector;
import is.idega.idegaweb.member.presentation.UserRelationConnector;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneType;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.contact.data.PhoneTypeHome;
import com.idega.core.location.business.CommuneBusiness;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.core.location.data.PostalCode;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.data.IDORemoveRelationshipException;
import com.idega.data.IDOStoreException;
import com.idega.event.IWPageEventListener;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Script;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.Window;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserStatusBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupRelation;
import com.idega.user.data.GroupRelationHome;
import com.idega.user.data.User;
import com.idega.user.data.UserStatus;
import com.idega.util.IWTimestamp;
import com.idega.util.URLUtil;

/**
 * The <code>UserEditor</code> handles user relations and addresses.
 * It contains a configurable unstrict user search. If more than one
 * user comply to the search criteria, a window with the compliant users
 * will popup where the right user can be chosen.
 * The chosen users relations can be handled, both family and other kind
 * of relations. If the deceased date is registered the listeners listening
 * to that event should be notified.
 * 
 * @author <a href="mailto:aron@idega.is"> Aron Birkir 
 * @version 1.0
 */
public class UserEditor extends Block {
	private static final String prm_coaddress_country_id = "co_country_id";
	private static final String prm_coaddress_postal_id = "co_postal_id";
	private static final String prm_primaddress_country_id = "prim_country_id";
	private static final String prm_primaddress_postal_id = "prim_postal_id";
	private static final String prm_deceased_date = "deceased_date";
	private static final String prm_email_address = "email_address";
	private static final String prm_main_phone = "phone_number";
	private static final String prm_mainaddress_street = "addr_prim_str";
	private static final String prm_mainaddress_postal_code = "addr_prim_pst_code";
	private static final String prm_mainaddress_postal_name = "addr_prim_pst_name";
	private static final String prm_mainaddress_country = "addr_prim_country";
	private static final String prm_coaddress_street = "addr_co_str";
	private static final String prm_coaddress_postal_code = "addr_co_pst_code";
	private static final String prm_coaddress_postal_name = "addr_co_pst_name";
	private static final String prm_coaddress_country = "addr_co_country";
	private static final String prm_old_value_suffix = "_old";
	private static final String prm_personal_id = "mbe_personal_id";
	private static final String prm_first_name = "mbe_first_name";
	private static final String prm_middle_name = "mbe_middle_name";
	private static final String prm_last_name = "mbe_last_name";
	private static final String prm_maincommune_id = "commune_prim_id";
	private static final String prm_cocommune_id = "commune_co_id";
	
	protected static final String prm_primary_group_id = "mbe_primary_group_id";
	/** Parameter for user id */
	//public static final String PRM_USER_ID = UserSearcher.PRM_USER_ID; //"ic_user_id";
	protected static final String PRM_SAVE = "mbe_save";
	protected static final String PRM_NEW_USER = "mbe_newuser";
	protected static final String PRM_USER_ID ="mbe_userid";
	/** The userID is the handled users ID. */
	protected Integer userID = null;
	/** The user currently handled */
	protected User user = null;
	/** The dynamic bundle identifier*/
	private String bundleIdentifer = null;
	/** The  static bundle identifier used in this package */
	private static String BUNDLE_IDENTIFIER = "is.idega.idegaweb.member";
	/** The Bundle */
	protected IWBundle iwb;
	/** The resource bundle */
	protected IWResourceBundle iwrb;
	/** The list of relationstyped handle by the editor */
	private List relationTypes = new Vector();
	private CountryDropdownMenu countryMenu = null;
	/** Determines if we show the users relations */
	protected boolean showUserRelations = true;
	/** The main layout table */
	private Table mainTable = null;
	/** the current layout table row */
	private int mainRow = 1;
	/** the button table */
	private Table buttonTable = null,actionButtonTable=null;
	/** flag for family relation types */
	protected boolean showAllRelationTypes = true;
	/** Class of relation connector window */
	protected Class connectorWindowClass = FamilyRelationConnector.class;
	/** flag for showing close button */
	protected boolean showCloseButton = false;
	/** flag for allowing registration of new users */
	protected boolean allowNewUserRegistration = true;
	/** flag for new user view */
	private boolean newUserView = false, reqNewUser = false;;
	public final static String STYLENAME_TEXT = "Text";
	public final static String STYLENAME_HEADER = "Header";
	public final static String STYLENAME_DECEASED = "Deceased";
	public final static String STYLENAME_BUTTON = "Button";
	public final static String STYLENAME_INTERFACE = "Interface";
	protected String textFontStyle = "font-weight:plain;";
	protected String headerFontStyle = "font-weight:bold;";
	protected String deceasedFontStyle = "font-weight:bold;font-color:red";
	protected String buttonStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	protected String interfaceStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	protected String textFontStyleName = null;
	protected String headerFontStyleName = null;
	protected String deceasedFontStyleName = null;
	protected String buttonStyleName = null;
	protected String interfaceStyleName = null;
    protected String seperatorBackgroundColor = "#6da7fd";
	private UserSearcher searcher = null;
	private boolean showMiddleNameInput = true;
	private int nameInputLength = 25;
	private int personalIdInputLength = 15;
	private int streetInputLength = 30;
	private int emailInputLength = 30;
	private int postalcodeInputLength = 10;
	private int postalnameInputLength = 30;
	private int phoneInputLength = 30;
	private boolean allowPersonalIdEdit = true;
	private boolean warnIfPostalExists = false;
	private boolean warnIfPersonalIDIsIllegal =true;
	private boolean showSeperators = true;
	protected boolean showDefaultCommuneOption = false;
	
	private void initStyleNames() {
		if (textFontStyleName == null)
			textFontStyleName = getStyleName(STYLENAME_TEXT);
		if (headerFontStyleName == null)
			headerFontStyleName = getStyleName(STYLENAME_HEADER);
		if (buttonStyleName == null)
			buttonStyleName = getStyleName(STYLENAME_BUTTON);
		if (interfaceStyleName == null)
			interfaceStyleName = getStyleName(STYLENAME_INTERFACE);
		if (deceasedFontStyleName == null)
			deceasedFontStyleName = getStyleName(STYLENAME_DECEASED);
	}
	/**
	 * Constructs a new UserEditor with an empty list of relationtypes
	 */
	public UserEditor() {
		this(new ArrayList());
	}
	public UserEditor(List relationTypes) {
		this.relationTypes = relationTypes;
		searcher = new UserSearcher();
		searcher.setUniqueIdentifier("edt");
	}
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void main(IWContext iwc) {
		//debugParameters(iwc);
		// get bundles
		initStyleNames();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		//iwc.getApplication().getLog().info("Who is your daddy ?");
		if(iwc.isLoggedOn()){
		try {
			process(iwc);
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}
		try {
			presentate(iwc);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}}
		else{
			add(iwrb.getLocalizedString("no_user_logged_on","No user logged on"));
		}
	}
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#getBundleIdentifier()
	 */
	public String getBundleIdentifier() {
		if (bundleIdentifer != null)
			return bundleIdentifer;
		return BUNDLE_IDENTIFIER;
	}
	/**
	 * Sets the dynamic bundle identifier
	 * @param string
	 */
	public void setBundleIdentifer(String string) {
		bundleIdentifer = string;
	}
	/**
	 * Appends a new relation type to be handled at the specified index
	 * @param index of which the specified element is to be inserted
	 * @param element to be inserted
	 */
	public void addRelationType(int index, String type) {
		relationTypes.add(index, type);
	}
	/**
	 * Appends a new relation type to be handled
	 * @param type to be inserted 
	 */
	public void addRelationType(String type) {
		relationTypes.add(type);
	}
	public void setToShowAllRelationTypes(boolean bool) {
		showAllRelationTypes = bool;
	}
	/**
	 * Presentates the whole UserEditor
	 * will call other presentate* methods
	 * @param iwc the current context
	 */
	public void presentate(IWContext iwc) throws RemoteException {
		mainTable = new Table();
		if (!isNewUserView()) {
			addSeperator(iwrb.getLocalizedString("mbe.search","Search"));
			addToMainPart(searcher);
			addToMainPart(Text.getBreak());
			searcher.setOwnFormContainer(false);
			if (user != null) {
				searcher.setUser(user);
			}
			else if (user == null) {
				try {
					searcher.process(iwc);
					user = searcher.getUser();
				}
				catch (RemoteException e1) {
					log(e1);
				}
				catch (FinderException e1) {
					log(e1);
				}
			}
		}
		if (isAllowNewUserRegistration()) {
			SubmitButton newUser =
				new SubmitButton(iwrb.getLocalizedString("mbe.new_user", "New"), PRM_NEW_USER, "true");
			newUser.setStyleClass(buttonStyleName);
			addSearchButtonObject(newUser);
		}
		if (user != null || isNewUserView()) {
			
			presentateUserName(iwc);
			
//			presentateCommuneSelection(iwc);
			presentateUserGroup(iwc);
			addToMainPart(Text.getBreak());
			presentateUserInfo(iwc);
			
			if (showUserRelations) {
				try {
					presentateUserRelations(iwc);
				}
				catch (Exception e) {
					log(e);
				}
			}
			addToMainPart(Text.getBreak());
			presentateButtons(iwc);
		}
		
		/*
		Table blackColumn = new Table(1,1);
		blackColumn.setCellpadding(0);
		blackColumn.setColor(1,1,"#000000");
		blackColumn.setHeight(1,1,"100%");
		
	
		//blackColumn.setColor("#6da7fd");
		
		mainTable.add(blackColumn,1,1);
		mainTable.add(blackColumn,3,1);
		mainTable.mergeCells(1,1,1,mainTable.getRows());
		
		mainTable.mergeCells(3,1,3,mainTable.getRows());
		mainTable.setColumnAlignment(2,Table.VERTICAL_ALIGN_TOP);
		*/
		//mainTable.setBorder(1);
		Form form = new Form();
		form.setOnSubmit("return checkInfoForm()");
		form.add(mainTable);
		add(form);
	}
	/**
	 * Presentates the users relations
	 * @param iwc the context
	 */
	protected void presentateUserRelations(IWContext iwc) throws RemoteException {
		addSeperator(iwrb.getLocalizedString("mbe.user_relation","User relations"));
		Table relationsTable = new Table();
		relationsTable.setCellspacing(4);
		int row = 1;
		try {
			Map relations = getRelations(user);
			UserBusiness userService = getUserService(iwc);
			User relatedUser;
			if (relationTypes != null && !relationTypes.isEmpty()) {
				for (Iterator iter = relationTypes.iterator(); iter.hasNext();) {
					String type = (String) iter.next();
					Text tTypeName =
						new Text(iwrb.getLocalizedString("is_" + type + "_of", "Is " + type.toLowerCase() + " of"));
					relationsTable.add(tTypeName, 1, row);
					if (relations.containsKey(type)) {
						List list = (List) relations.get(type);
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							GroupRelation relation = (GroupRelation) iterator.next();
							relatedUser = userService.getUser(relation.getRelatedGroupPK());
							relationsTable.add(relatedUser.getPersonalID(), 2, row);
							relationsTable.add(getRelatedUserLink(relatedUser), 3, row);
							Link disconnectLink =
								getDisConnectorLink(
									type,
									null,
									(Integer) user.getPrimaryKey(),
									(Integer) relatedUser.getPrimaryKey(),
									iwb.getImageButton(
										iwrb.getLocalizedString("mbe.remove_" + type, "Remove " + type)));
							relationsTable.add(disconnectLink, 4, row);
							row++;
						}
					}
					row++;
				}
			}
		}
		catch (EJBException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}
		addToMainPart(relationsTable);
	}
	
	protected void addSeperator(String header){
		if(showSeperators){
			Table tSep = new Table(3,1);
			tSep.setCellpadding(0);	
			tSep.setWidth(1,30);
			tSep.setNoWrap(2,1);
			Text theader = getHeader(header);
			
			theader.setFontSize(Text.FONT_SIZE_7_HTML_1);
			tSep.add(theader,2,1);
			tSep.setWidth(3,Table.HUNDRED_PERCENT);
			Table tcolor = new Table(1,1);
			 tcolor.setCellpadding(0);
			 tcolor.setWidth(Table.HUNDRED_PERCENT);
			 tcolor.setColor(1,1,"#000000");
			 //tcolor.setColor(seperatorBackgroundColor);
			
			 Table tcolor1 = new Table(1,1);
			 tcolor1.setCellpadding(0);
			 tcolor1.setWidth(30);
			 tcolor1.setColor(1,1,"#000000");
			 //tcolor1.setColor(seperatorBackgroundColor);
			
			tSep.add(tcolor1,1,1);
			tSep.add(tcolor,3,1);
			addToMainPart(tSep);
		}
	}
	
	protected Link getRelatedUserLink(User relatedUser) {
		Link relatedLink = new Link(relatedUser.getName());
		relatedLink.addParameter(searcher.getUniqueUserParameter((Integer) relatedUser.getPrimaryKey()));
		return relatedLink;
	}
	/**
	 * Handles presentation of buttons
	 * calls all presentateButton* methods when appropiate
	 * @param iwc
	 */
	protected void presentateButtons(IWContext iwc) {
		addSeperator(iwrb.getLocalizedString("mbe.actions","Actions"));
		buttonTable = new Table();
		actionButtonTable = new Table();
		presentateButtonSave(iwc);
		if (showUserRelations && user != null) {
			presentateButtonRegister(iwc);
		}
		if (showCloseButton) {
			presentateButtonClose(iwc);
		}
		if(isNewUserView()){
			presentateButtonCancel(iwc);
		}
		addToMainPart(buttonTable);
		addToMainPart(actionButtonTable);
	}
	/**
	 * Presentates the save button
	 * @param iwc
	 */
	protected void presentateButtonSave(IWContext iwc) {
		String ID = user != null ? user.getPrimaryKey().toString() : "-1";
		SubmitButton save = new SubmitButton(iwrb.getLocalizedString("mbe.save", "Save"), PRM_SAVE, ID);
		save.setStyleClass(buttonStyleName);
		addButton(save,true);
	}
	/**
	 * Presentates the close button
	 * @param iwc
	 */
	protected void presentateButtonClose(IWContext iwc) {
		CloseButton close = new CloseButton(iwrb.getLocalizedString("mbe.close", "Close"));
		close.setStyleClass(buttonStyleName);
		 close.setOnClick("window.opener.location.reload()");
		//getParentPage().setParentToReload();
		addButton(close,true);
	}
	
	/**
	 * Presentates the cancel button
	 * @param iwc
	 */
		protected void presentateButtonCancel(IWContext iwc) {
			SubmitButton cancel = new SubmitButton(iwrb.getLocalizedString("mbe.cancel", "Cancel"));
			cancel.setStyleClass(buttonStyleName);
			addButton(cancel,true);
		}
	/**
	 * Presentates the user relation register buttons
	 * @param iwc
	 */
	protected void presentateButtonRegister(IWContext iwc) {
		int pageID = getParentPageID();
		Integer thisPageID = pageID>0 ?new Integer(pageID):null;
		for (Iterator iter = relationTypes.iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			SubmitButton registerButton =
				getConnectorButton(
					iwc,null,
					(iwrb.getLocalizedString("mbe.register_as_" + type, "Register as " + type)),
					(Integer) user.getPrimaryKey(),
					type,
					null,thisPageID);
			addButton(registerButton);
		}
	}
	/**
	 * Adds a object to the button area 
	 * @param button
	 */
	protected void addButton(PresentationObject button) {
		addButton(button,false);
	}
	
	/**
	 * Adds a object to the button area, in the specified row 
	 * @param button
     */
    protected void addButton(PresentationObject button,boolean actionrow) {
	   if(actionrow){
		   int cols = actionButtonTable.getColumns();
		   actionButtonTable.add(button,cols+1,1);
	   }
	   else{
			int cols = buttonTable.getColumns();
			buttonTable.add(button,cols+1,1);
	   }
	   
	   
	  
    }
	/**
	 * Gets a relation connector link
	 * @param roleUserID
	 * @param type
	 * @param reverseType
	 * @param object
	 * @return
	 */
	protected Link getConnectorLink(Integer roleUserID, String type, String reverseType, PresentationObject object,Integer editorPageID) {
		Link registerLink = new Link(object);
		registerLink.setWindowToOpen(connectorWindowClass);
		registerLink.addParameter(UserRelationConnector.PARAM_USER_ID, roleUserID.toString());
		if (type != null)
			registerLink.addParameter(UserRelationConnector.PARAM_TYPE, type);
		if (reverseType != null)
			registerLink.addParameter(UserRelationConnector.PARAM_REVERSE_TYPE, reverseType);
		if(editorPageID!=null)
			registerLink.addParameter(UserRelationConnector.PARAM_RELOAD_PAGE_ID,editorPageID.toString());
		registerLink.addParameter(UserRelationConnector.PARAM_RELOAD_USER_PRM_NAME,PRM_USER_ID);
		return registerLink;
	}
	/**
	 * Gets a relation connector button
	 * @param iwc
	 * @param display
	 * @param roleUserID
	 * @param type
	 * @param reverseType
	 * @return
	 */
	protected SubmitButton getConnectorButton(
		IWContext iwc,
		Image displayImage,String displayString,
		Integer roleUserID,
		String type,
		String reverseType,Integer editorPageID) {
		SubmitButton button = null;
		if(displayImage!=null){
			if(displayString!=null) displayImage.setToolTip(displayString);
			button = new SubmitButton(displayImage);
		}
		else
			button = new SubmitButton(displayString);
		URLUtil URL = new URLUtil(Window.getWindowURL(connectorWindowClass, iwc));
		//String URL = Window.getWindowURL(connectorWindowClass, iwc);
		URL.addParameter(UserRelationConnector.PARAM_USER_ID ,roleUserID.toString());
		//URL += "&" + UserRelationConnector.PARAM_USER_ID + "=" + roleUserID.toString();
		if (type != null)
			URL.addParameter(UserRelationConnector.PARAM_TYPE,type);
			//URL += "&" + UserRelationConnector.PARAM_TYPE + "=" + type;
		if (reverseType != null)
			URL.addParameter(UserRelationConnector.PARAM_REVERSE_TYPE,reverseType);
			//URL += "&" + UserRelationConnector.PARAM_REVERSE_TYPE + "=" + reverseType;
		if(editorPageID!=null)
			URL.addParameter(UserRelationConnector.PARAM_RELOAD_PAGE_ID,editorPageID.toString());
		URL.addParameter(UserRelationConnector.PARAM_RELOAD_USER_PRM_NAME,PRM_USER_ID);
		URL.addParameter(UserRelationConnector.PARAM_ACTION,UserRelationConnector.PARAM_ATTACH);
		button.setOnClick(
			"javascript:" + Window.getCallingScriptString(connectorWindowClass, URL.toString(), true, iwc) + ";return false;");
		button.setStyleClass(buttonStyleName);
		return button;
	}
	/**
	 * Gets a relation disconnector link
	 * @param type
	 * @param reverseType
	 * @param roleUserID
	 * @param victimUserID
	 * @param object
	 * @return
	 */
	protected Link getDisConnectorLink(
		String type,
		String reverseType,
		Integer roleUserID,
		Integer victimUserID,
		PresentationObject object) {
		Link registerLink = new Link(object);
		//new Link(	iwb.getImageButton(	iwrb.getLocalizedString("mbe.remove_"+type, "Remove "+type) ));
		registerLink.setWindowToOpen(connectorWindowClass);
		registerLink.addParameter(UserRelationConnector.PARAM_USER_ID, roleUserID.toString());
		registerLink.addParameter(UserRelationConnector.getRelatedUserParameterName(), victimUserID.toString());
		if (type != null)
			registerLink.addParameter(UserRelationConnector.PARAM_TYPE, type);
		if (reverseType != null)
			registerLink.addParameter(UserRelationConnector.PARAM_REVERSE_TYPE, reverseType);
		registerLink.addParameter(UserRelationConnector.PARAM_ACTION, UserRelationConnector.PARAM_DETACH);
		//registerLink.addParameter(GroupRelationConnector.PARAM_ACTION,GroupRelationConnector.ACTION_DETACH);
		return registerLink;
	}
	
	protected void presentateUserName(IWContext iwc) {
		
		Table nameTable = new Table();
		Text tPersonalID = new Text(iwrb.getLocalizedString("mbe.personal_id", "Personal ID"));
		tPersonalID.setStyleClass(headerFontStyleName);
		Text tFirstName = new Text(iwrb.getLocalizedString("mbe.first_name", "First name"));
		tFirstName.setStyleClass(headerFontStyleName);
		Text tMiddleName = new Text(iwrb.getLocalizedString("mbe.middle_name", "Middle name"));
		tMiddleName.setStyleClass(headerFontStyleName);
		Text tLastName = new Text(iwrb.getLocalizedString("mbe.last_name", "Last name"));
		tLastName.setStyleClass(headerFontStyleName);
		
		TextInput personalIdInput = new TextInput(prm_personal_id);
		personalIdInput.setLength(personalIdInputLength);
		personalIdInput.setStyleClass(interfaceStyleName);
		personalIdInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput firstNameInput = new TextInput(prm_first_name);
		firstNameInput.setLength(nameInputLength);
		firstNameInput.setStyleClass(interfaceStyleName);
		firstNameInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput middleNameInput = new TextInput(prm_middle_name);
		middleNameInput.setLength(nameInputLength);
		middleNameInput.setStyleClass(interfaceStyleName);
		middleNameInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput lastNameInput = new TextInput(prm_last_name);
		lastNameInput.setLength(nameInputLength);
		lastNameInput.setStyleClass(interfaceStyleName);
		lastNameInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		if (user != null) {
			if (user.getPersonalID() != null) {
				personalIdInput.setContent(user.getPersonalID());
				nameTable.add(getOldParameter(prm_personal_id, user.getPersonalID()));
			}
			if (user.getFirstName() != null) {
				firstNameInput.setContent(user.getFirstName());
				nameTable.add(getOldParameter(prm_first_name, user.getFirstName()));
			}
			if (user.getMiddleName() != null) {
				middleNameInput.setContent(user.getMiddleName());
				nameTable.add(getOldParameter(prm_middle_name, user.getMiddleName()));
			}
			if (user.getLastName() != null) {
				lastNameInput.setContent(user.getLastName());
				nameTable.add(getOldParameter(prm_last_name, user.getLastName()));
			}
		}
		int col = 1;
		nameTable.add(tPersonalID, col, 1);
		if (isAllowPersonalIdEdit(user) || user == null){
			nameTable.add(personalIdInput, col++, 2);
			addSeperator(iwrb.getLocalizedString("mbe.create_user","Create User"));
		}
		else if (user != null) {
			Text tPid = new Text(user.getPersonalID());
			tPid.setStyleClass(textFontStyleName);
			nameTable.add(tPid, col++, 2);
			addSeperator(iwrb.getLocalizedString("mbe.chosen_user","Chosen User"));
		}
		nameTable.add(tLastName, col, 1);
		nameTable.add(lastNameInput, col++, 2);
		if (showMiddleNameInput) {
			nameTable.add(tMiddleName, col, 1);
			nameTable.add(middleNameInput, col++, 2);
		}
		nameTable.add(tFirstName, col, 1);
		nameTable.add(firstNameInput, col++, 2);
		addToMainPart(nameTable);
	}
	
	protected void presentateUserGroup(IWContext iwc) {
		addSeperator(iwrb.getLocalizedString("mbe.chosen_group","Chosen group"));
		Table groupTable = new Table();
		Text tGroupTitle = new Text(iwrb.getLocalizedString("mbe.primary_group", "Primary group"));
		tGroupTitle.setStyleClass(headerFontStyleName);
		groupTable.add(tGroupTitle, 1, 1);
		DropdownMenu groupMenu = new DropdownMenu(prm_primary_group_id);
		groupMenu.setStyleClass(interfaceStyleName);
		groupTable.add(groupMenu, 2, 1);
		Collection groups = null;
		try {
			UserBusiness userService = getUserService(iwc);
			// copied from com.idega.user.presentation.CreateUser 
			// TODO should be a business method !!! 
			if (!iwc.isSuperAdmin()) {
				User currentUser = iwc.getCurrentUser();
				if (currentUser != null)
					groups = userService.getAllGroupsWithEditPermission(iwc.getCurrentUser(), iwc);
			}
			else {
				User currentUser = iwc.getCurrentUser();
				groups = userService.getUsersTopGroupNodesByViewAndOwnerPermissions(currentUser, iwc);
			}
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		if (groups != null) {
			Iterator iter = groups.iterator();
			try {
				GroupBusiness groupService = getGroupService(iwc);
				while (iter.hasNext()) {
					Group item = (Group) iter.next();
					groupMenu.addMenuElement(
						item.getPrimaryKey().toString(),
						groupService.getNameOfGroupWithParentName(item));
				}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			catch (EJBException e) {
				e.printStackTrace();
			}
		}
		addToMainPart(groupTable);
		addToMainPart(Text.getBreak());
	}
	/**
	 * Presentates the users found by search
	 * @param iwc the context
	*/
	protected void presentateUserInfo(IWContext iwc) throws RemoteException {
		addSeperator(iwrb.getLocalizedString("mbe.user_info","User info"));
		UserBusiness userService = getUserService(iwc);
		Page p = this.getParentPage();
		if (p != null) {
			Script S = p.getAssociatedScript();
			S.addFunction("checkInfoForm", getInfoCheckScript());
		}
		Table infoTable = new Table();
		Table addressTable = new Table();
		int row = 1;
		addressTable.setCellspacing(4);
		Address primaryAddress = null;
		Address coAddress = null;
		UserStatus deceasedStatus = null;
		Email email = null;
		if (user != null) {
			primaryAddress = userService.getUsersMainAddress(user);
			coAddress = userService.getUsersCoAddress(user);
			deceasedStatus = getUserStatusService(iwc).getDeceasedUserStatus((Integer) user.getPrimaryKey());
			email = userService.getUserMail(user);
		}
		if (!isNewUserView()) {
			//deceased layout section
			// TODO check for deceased date
			Text tDeceased = new Text(iwrb.getLocalizedString("mbe.deceased", "Deceased"));
			tDeceased.setStyleClass(deceasedFontStyleName);
			addressTable.add(tDeceased, 1, row);
			if (deceasedStatus != null) {
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, iwc.getCurrentLocale());
				Text tDeceasedDate = new Text(df.format(deceasedStatus.getDateFrom()));
				setStyle(tDeceasedDate, STYLENAME_DECEASED);
				addressTable.add(tDeceasedDate, 2, row);
			}
			else {
				DateInput deceasedInput = new DateInput(prm_deceased_date);
				deceasedInput.setToDisplayDayLast(true);
				IWTimestamp today = IWTimestamp.RightNow();
				deceasedInput.setLatestPossibleDate(
					today.getDate(),
					iwrb.getLocalizedString(
						"mbe.deceased_date_warning",
						"Please do not register deceased date in the future"));
				deceasedInput.setYearRange(today.getYear() - 5, today.getYear());
				deceasedInput.setStyleClass(interfaceStyleName);
				addressTable.add(deceasedInput, 2, row);
			}
			row++;
		}
		addToMainPart(addressTable);
		addToMainPart(Text.getBreak());
		// address layout section

		
		Text tAddress = new Text(iwrb.getLocalizedString("mbe.address", "Address"));
		Text tPrimary = new Text(iwrb.getLocalizedString("mbe.address.main", "Main"));
		Text tCO = new Text(iwrb.getLocalizedString("mbe.address.co", "C/O"));
		Text tStreetAddress = new Text(iwrb.getLocalizedString("mbe.address.street", "Street"));
		Text tPostalName = new Text(iwrb.getLocalizedString("mbe.address.postal.name", "Postal name"));
		Text tPostalCode = new Text(iwrb.getLocalizedString("mbe.address.postal.code", "Postal code"));
		Text tCommune = new Text(iwrb.getLocalizedString("mbe.commune", "Commune"));
		Text tCountry = new Text(iwrb.getLocalizedString("mbe.address.country", "Country"));
		tAddress.setStyleClass(headerFontStyleName);
		tPrimary.setStyleClass(headerFontStyleName);
		tCO.setStyleClass(headerFontStyleName);
		tStreetAddress.setStyleClass(headerFontStyleName);
		tPostalName.setStyleClass(headerFontStyleName);
		tPostalCode.setStyleClass(headerFontStyleName);
		tCountry.setStyleClass(headerFontStyleName);
		tCommune.setStyleClass(headerFontStyleName);

		addressTable.add(tAddress, 2, row);
		addressTable.add(tCO, 3, row);
		row++;
		int startRow = row;
		addressTable.add(tStreetAddress, 1, row++);
		addressTable.add(tPostalCode, 1, row++);
		addressTable.add(tPostalName, 1, row++);
		addressTable.add(tCommune, 1, row++);
		addressTable.add(tCountry, 1, row++);
		Country defaultCountry = null;
		Commune defaultCommune = null;
		Commune primaryCommune = null;
		Commune coCommune = null;
		if(showDefaultCommuneOption){
			try {
				defaultCommune = getCommuneBusiness(iwc).getCommuneHome().findDefaultCommune();
			} catch (IDOLookupException e3) {
				log(e3);
			} catch (RemoteException e3) {
				log(e3);
			} catch (FinderException e3) {
				logError("[UserEditor] Default Commune not found");
				//e3.printStackTrace();
			}			
		}
		try {
			defaultCountry =
				getCountryHome().findByIsoAbbreviation(iwc.getApplicationSettings().getDefaultLocale().getCountry());
		}
		catch (RemoteException e1) {
			log(e1);
		}
		catch (MissingResourceException e1) {
			log(e1);
		}
		catch (FinderException e1) {
			logError("[UserEditor] Default Country not found");
			//e1.printStackTrace();
		}
		TextInput primaryStreetAddressInput = new TextInput(prm_mainaddress_street);
		primaryStreetAddressInput.setStyleClass(interfaceStyleName);
		primaryStreetAddressInput.setLength(streetInputLength);
		primaryStreetAddressInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
			
		TextInput primaryPostalCodeInput = new TextInput(prm_mainaddress_postal_code);
		primaryPostalCodeInput.setStyleClass(interfaceStyleName);
		primaryPostalCodeInput.setLength(postalcodeInputLength);
		primaryPostalCodeInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput primaryPostalNameInput = new TextInput(prm_mainaddress_postal_name);
		primaryPostalNameInput.setStyleClass(interfaceStyleName);
		primaryPostalNameInput.setLength(postalnameInputLength);
		primaryPostalNameInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		DropdownMenu primaryCommunes = new DropdownMenu(prm_maincommune_id);
		primaryCommunes.addMenuElement("-1",iwrb.getLocalizedString("none-selected","None-selected"));
		primaryCommunes.setStyleClass(interfaceStyleName);
		primaryCommunes.keepStatusOnAction(isNewUserView() && !reqNewUser);
		SelectorUtility su = new SelectorUtility();
		su.getSelectorFromIDOEntities(primaryCommunes, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
		
		CountryDropdownMenu primaryCountryInput = new CountryDropdownMenu(prm_mainaddress_country);
		primaryCountryInput.setStyleClass(interfaceStyleName);
		primaryCountryInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput coStreetAddressInput = new TextInput(prm_coaddress_street);
		coStreetAddressInput.setStyleClass(interfaceStyleName);
		coStreetAddressInput.setLength(streetInputLength);
		coStreetAddressInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput coPostalCodeInput = new TextInput(prm_coaddress_postal_code);
		coPostalCodeInput.setStyleClass(interfaceStyleName);
		coPostalCodeInput.setLength(postalcodeInputLength);
		coPostalCodeInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		TextInput coPostalNameInput = new TextInput(prm_coaddress_postal_name);
		coPostalNameInput.setStyleClass(interfaceStyleName);
		coPostalNameInput.setLength(postalnameInputLength);
		coPostalNameInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		DropdownMenu coCommunes = new DropdownMenu(prm_cocommune_id);
		coCommunes.addMenuElement("-1",iwrb.getLocalizedString("none-selected","None-selected"));
		su.getSelectorFromIDOEntities(coCommunes, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
		coCommunes.setStyleClass(interfaceStyleName);
		coCommunes.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		CountryDropdownMenu coCountryInput = (CountryDropdownMenu) primaryCountryInput.clone();
		coCountryInput.setName(prm_coaddress_country);
		coCountryInput.setStyleClass(interfaceStyleName);
		coCountryInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		/*
		PostalCodeDropdownMenu coPostalAddressInput = new PostalCodeDropdownMenu();
		coPostalAddressInput.setName(prm_coaddress_postal);
		coPostalAddressInput.setStyleClass(interfaceStyleName);
		coPostalAddressInput.setShowCountry(true);
		*/
		if (defaultCountry != null) {
			primaryCountryInput.setSelectedCountry(defaultCountry);
			coCountryInput.setSelectedCountry(defaultCountry);
			//primaryPostalAddressInput.setCountry(defaultCountry);
			//coPostalAddressInput.setCountry(defaultCountry);
		}

		if (defaultCommune != null && user == null) {
				primaryCommunes.setSelectedElement((defaultCommune).getPrimaryKey().toString());
				coCommunes.setSelectedElement((defaultCommune).getPrimaryKey().toString());
		}		//addressTable.add(tPrimary, 2, 2);
		row = startRow;
		addressTable.add(primaryStreetAddressInput, 2, row++);
		addressTable.add(primaryPostalCodeInput, 2, row++);
		addressTable.add(primaryPostalNameInput, 2, row++);
		addressTable.add(primaryCommunes, 2, row++);
		addressTable.add(primaryCountryInput, 2, row++);
		//row++;
		row = startRow;
		addressTable.add(coStreetAddressInput, 3, row++);
		addressTable.add(coPostalCodeInput, 3, row++);
		addressTable.add(coPostalNameInput, 3, row++);
		addressTable.add(coCommunes, 3, row++);
		addressTable.add(coCountryInput, 3, row++);
		//row++;
		if (primaryAddress != null) {
			primaryStreetAddressInput.setContent(primaryAddress.getStreetAddress());
			addressTable.add(getOldParameter(prm_mainaddress_street, primaryAddress.getStreetAddress()));
			try {
				PostalCode postalCode = primaryAddress.getPostalCode();
				//System.err.println("postal ID: "+postalCode.getPrimaryKey().toString());
				if (postalCode != null) {
					if (postalCode.getPostalCode() != null)
						primaryPostalCodeInput.setContent(postalCode.getPostalCode());
					if (postalCode.getName() != null)
						primaryPostalNameInput.setContent(postalCode.getName());
					addressTable.add(new Parameter(prm_primaddress_postal_id, postalCode.getPrimaryKey().toString()));
					addressTable.add(getOldParameter(prm_mainaddress_postal_code, postalCode.getPostalCode()));
					addressTable.add(getOldParameter(prm_mainaddress_postal_name, postalCode.getName()));
					Country country = primaryAddress.getCountry();
					//postalCode.getCountry();
					if (country != null) {
						//primaryCountryInput.setSelectedElement(country.getPrimaryKey().toString());
						primaryCountryInput.setSelectedCountry(country);
						addressTable.add(getOldParameter(prm_mainaddress_country, country.getPrimaryKey().toString()));
					}
					
					/*else if(defaultCountry != null){
						//primaryCountryInput.setSelectedElement(defaultCountry.getPrimaryKey().toString());
						primaryCountryInput.setSelectedCountry(defaultCountry);
					}*/
				}
				if (primaryAddress.getCommuneID() > 0) {
					addressTable.add(getOldParameter(prm_maincommune_id, Integer.toString(primaryAddress.getCommuneID())));
					primaryCommunes.setSelectedElement(primaryAddress.getCommuneID());
				} else {
					primaryCommune = getCommune(iwc, user);
					if (primaryCommune != null) {
						primaryCommunes.setSelectedElement(primaryCommune.getPrimaryKey().toString());
					}
				}

			}
			catch (Exception e2) {
			}
			//primaryPostalAddressInput.setSelectedElement(primaryAddress.getPostalCodeID());
		}
		if (coAddress != null) {
			coStreetAddressInput.setContent(coAddress.getStreetAddress());
			addressTable.add(getOldParameter(prm_coaddress_street, coAddress.getStreetAddress()));
			try {
				PostalCode postalCode = coAddress.getPostalCode();
				if (postalCode != null) {
					if (postalCode.getPostalCode() != null)
						coPostalCodeInput.setContent(postalCode.getPostalCode());
					if (postalCode.getName() != null)
						coPostalNameInput.setContent(postalCode.getName());
					addressTable.add(new Parameter(prm_coaddress_postal_id, postalCode.getPrimaryKey().toString()));
					addressTable.add(getOldParameter(prm_coaddress_postal_code, postalCode.getPostalCode()));
					addressTable.add(getOldParameter(prm_coaddress_postal_name, postalCode.getName()));
					Country country = coAddress.getCountry();//postalCode.getCountry();
					if (country != null) {
						coCountryInput.setSelectedCountry(country);
						addressTable.add(getOldParameter(prm_coaddress_country_id, country.getPrimaryKey().toString()));
					}
					/*else if(defaultCountry != null){
							coCountryInput.setSelectedCountry(defaultCountry);
					}*/
					if (coAddress.getCommuneID() > 0) {
						addressTable.add(getOldParameter(prm_cocommune_id, Integer.toString(coAddress.getCommuneID())));
						coCommunes.setSelectedElement(coAddress.getCommuneID());
					} else {
						coCommune = getCommune(iwc, user);
						if (coCommune != null) {
							coCommunes.setSelectedElement(coCommune.getPrimaryKey().toString());
						}
					} 
				}
			}
			catch (Exception e2) {
			}
		}
		row++;
		// phone layout section
		Text tPhone = new Text(iwrb.getLocalizedString("mbe.phone", "Phone"));
		tPhone.setStyleClass(headerFontStyleName);
		
		TextInput phoneInput = new TextInput(prm_main_phone);
		phoneInput.setLength(phoneInputLength);
		phoneInput.setStyleClass(interfaceStyleName);
		phoneInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		addressTable.add(tPhone, 1, row);
		addressTable.add(phoneInput, 2, row++);
		try {
			Phone phone = userService.getUsersHomePhone(user);
			if (phone != null && phone.getNumber()!=null) {
				phoneInput.setContent(phone.getNumber());
				addressTable.add(getOldParameter(prm_main_phone, phone.getNumber()));
			}
		}
		catch (NoPhoneFoundException e) {
		}
		row++;
		// email layout section
		Text tEmail = new Text(iwrb.getLocalizedString("mbe.email", "Email"));
		tEmail.setStyleClass(headerFontStyleName);
		TextInput emailInput = new TextInput(prm_email_address);
		emailInput.setStyleClass(interfaceStyleName);
		emailInput.setLength(emailInputLength);
		emailInput.keepStatusOnAction(isNewUserView() && !reqNewUser);
		
		emailInput.setAsEmail(iwrb.getLocalizedString("mbe.error.email_input","Please enter a legal email address"));
		addressTable.add(tEmail, 1, row);
		addressTable.add(emailInput, 2, row++);
		if (email != null && email.getEmailAddress()!=null) {
			emailInput.setContent(email.getEmailAddress());
			addressTable.add(getOldParameter(prm_email_address, email.getEmailAddress()));
		}
	}
	/**
	 * Process parameters in the request
	 * @param iwc the context
	 */
	public void process(IWContext iwc) throws IDOLookupException, FinderException, RemoteException {
		initUser(iwc);
		initRelationTypes(iwc);
		setNewUserView(isNewUserView(iwc));
		if (iwc.isParameterSet(PRM_SAVE))
			saveUser(iwc);
	}
	
	protected Commune getCommune(IWContext iwc, User user) {
		return null;
	}
	
	public User findPersonalIDUser(IWContext iwc,String pid)throws FinderException,RemoteException{
		return getUserService(iwc).getUserHome().findByPersonalID(pid);
	}
	
	public User createUser(
		IWContext iwc,
		String personalID,
		String firstName,
		String middleName,
		String lastName,
		Integer primaryGroupID) {
		try {
			//User u = getUserService(iwc).createUser(firstName,middleName,lastName,null,personalID,null,null,null,primaryGroupID);
			User u = getUserService(iwc).createUser(firstName, middleName, lastName, personalID, null, null);
			if (primaryGroupID != null && primaryGroupID.intValue() > 0) {
				u.setPrimaryGroupID(primaryGroupID);
				u.store();
			}
			return u;
		}
		catch (IDOStoreException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (CreateException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void saveUser(IWContext iwc) throws RemoteException {
		UserBusiness userService = getUserService(iwc);
		Integer userID = Integer.valueOf(iwc.getParameter(PRM_SAVE));
		if (userID.intValue() <= 0) {
			// create new user
			String pid = iwc.getParameter(prm_personal_id);
			String fname = iwc.getParameter(prm_first_name);
			String mname = iwc.getParameter(prm_middle_name);
			String lname = iwc.getParameter(prm_last_name);
			String group = iwc.getParameter(prm_primary_group_id);
			Integer groupID = null;
			try {
				groupID = Integer.valueOf(group);
			}
			catch (NumberFormatException e1) {
				log(e1);
				//e1.printStackTrace();
			}
			if(isValidPersonalID(pid) && !"".equals(fname) && !"".equals(lname)){
				try {
					user = findPersonalIDUser(iwc,pid);
				}
				catch (FinderException e) {
					user=null;
				}
				if(user==null)
					user = createUser(iwc, pid, fname, mname, lname, groupID);
				else{
					String mainPostalExists =
						iwrb.getLocalizedString(
								"mbe.warning.user_with_pid_exists",
						"There is already a user registered with this personal id");
					this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
				}
			}
			else{
				String mainPostalExists =
				iwrb.getLocalizedString(
						"mbe.warning.fields_missing",
						"Please provide a legal personalID and nonempty names");
				this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
			   setNewUserView(true);
			}
		}
		else {
			user = userService.getUser(userID);
			try {
				// user part
				if (isNewValue(iwc, prm_personal_id)
					|| isNewValue(iwc, prm_first_name)
					|| isNewValue(iwc, prm_middle_name)
					|| isNewValue(iwc, prm_last_name)
					|| isNewValue(iwc, prm_primary_group_id)
					|| isRemovedValue(iwc,prm_middle_name)) {
					String pid = user.getPersonalID(), first = user.getFirstName(), middle = user.getMiddleName(), last = user.getLastName();
					Integer groupID = null;
					boolean legalState = false;
					if (isNewValue(iwc, prm_personal_id) ){
						if(isValidPersonalID(iwc.getParameter(prm_personal_id))) {
							pid = iwc.getParameter(prm_personal_id);
							try {
								User pidUser  =findPersonalIDUser(iwc,pid);
								if(pidUser!=null && !pidUser.getPrimaryKey().toString().equals(userID.toString()) ){
									String mainPostalExists =
										iwrb.getLocalizedString(
												"mbe.warning.personal_id_in_use",
										"Personal ID is already in use");
									this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
									pid= user.getPersonalID();
									legalState |=false;
								}
								else{
									legalState |= true;
								}
								
							}
							catch (FinderException e) {
								legalState |= true;
							}
							
						}
						else 
							if(warnIfPersonalIDIsIllegal){
							legalState = false;
							String mainPostalExists =
							iwrb.getLocalizedString(
									"mbe.warning.personal_id_illegal",
									"Personal ID is illegally formatted");
							this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
							pid= user.getPersonalID();
							legalState |=false;
						}
						
					}
					if (isNewValue(iwc, prm_first_name)) {
						first = iwc.getParameter(prm_first_name);
						legalState |= true;
					}
					if (isNewValue(iwc, prm_middle_name)) {
						middle = iwc.getParameter(prm_middle_name);
						legalState |= true;
					}
					else if (isRemovedValue(iwc, prm_middle_name)) {
						middle = "";
						legalState |= true;
					}
					if (isNewValue(iwc, prm_last_name)) {
						last = iwc.getParameter(prm_last_name);
						legalState |= true;
					}
					if (isNewValue(iwc, prm_primary_group_id)) {
						groupID = Integer.valueOf(iwc.getParameter(prm_primary_group_id));
						if (groupID.intValue() <= 0)
							groupID = null;
						legalState |= true;
					}
					if(legalState )
						userService.updateUser(user, first, middle, last, null, null, null, pid, null, groupID);
				}
			}
			catch (EJBException e3) {
				e3.printStackTrace();
			}
			catch (RemoteException e3) {
				e3.printStackTrace();
			}
		}
		if (user != null)
			userID = (Integer) user.getPrimaryKey();
		else
			userID = null;
		
		if(userID!=null){
		try {
			// main address part
			if (isRemovedValue(iwc, prm_mainaddress_street)) {
				Address address = null;
				try {
					address = userService.getUsersMainAddress(user);
					if (address != null) {
						user.removeAddress(address);
					}
				}
				catch (IDORemoveRelationshipException e1) {
					e1.printStackTrace();
				}
				catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
			else if (
				isNewValue(iwc, prm_mainaddress_street)
					|| isNewValue(iwc, prm_mainaddress_postal_code)
					|| isNewValue(iwc, prm_mainaddress_postal_name)
					|| isNewValue(iwc, prm_mainaddress_country)
					|| isNewValue(iwc, prm_maincommune_id)) {
				String street = iwc.getParameter(prm_mainaddress_street);
				if (!"".equals(street)) {
					
					Country country = null;
					PostalCode postalCode = null;
					Integer communeID = null;
					if (iwc.isParameterSet(prm_mainaddress_country)) {
						Integer countryID = Integer.valueOf(iwc.getParameter(prm_mainaddress_country));
						try {
							country = userService.getAddressBusiness().getCountryHome().findByPrimaryKey(countryID);
						}
						catch (RemoteException e1) {
							e1.printStackTrace();
						}
						catch (FinderException e1) {
							e1.printStackTrace();
						}
					}
					if (country != null
						&& ( isNewValue(iwc,prm_mainaddress_street)
							|| isNewValue(iwc, prm_mainaddress_postal_code)
							|| isNewValue(iwc, prm_mainaddress_postal_name)
							|| isNewValue(iwc, prm_maincommune_id)
							|| isNewValue(iwc, prm_mainaddress_country))) {
						String code = iwc.getParameter(prm_mainaddress_postal_code);
						String name = iwc.getParameter(prm_mainaddress_postal_name);
						if (!"".equals(code) && !"".equals(name)) {
							boolean postalExists = false;
							try {
								postalCode =	 userService.getAddressBusiness().getPostalCodeHome().findByPostalCodeAndCountryId(	code,	((Integer) country.getPrimaryKey()).intValue());
								postalExists = postalCode != null;
							}
							catch (RemoteException e2) {
							}
							catch (FinderException e2) {
							}
							if (warnIfPostalExists && postalExists && (isNewValue(iwc, prm_mainaddress_postal_code) || isNewValue(iwc, prm_mainaddress_postal_name))) {
								
								String mainPostalExists =	iwrb.getLocalizedString("mbe.warning.zip_code_and_city_already_exist","Zip code and city already exist in database");
								this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
							}
							if(postalExists && isNewValue(iwc,prm_mainaddress_postal_name) && !isNewValue(iwc,prm_mainaddress_postal_code)){
								userService.getAddressBusiness().changePostalCodeNameWhenOnlyOneAddressRelated(postalCode,name);
							}
							if(!postalExists){
								try {
									postalCode =userService.getAddressBusiness().getPostalCodeAndCreateIfDoesNotExist(code,	name,country);
								
								}
								catch (RemoteException e1) {
									e1.printStackTrace();
								}
							}
							if (iwc.isParameterSet(prm_maincommune_id)) {
								communeID = Integer.valueOf(iwc.getParameter(prm_maincommune_id));
								if(communeID.intValue()<=0)
									communeID = null;
							}
							
						}
						else if (iwc.isParameterSet(prm_primaddress_postal_id)) {
							Integer postalID = Integer.valueOf(iwc.getParameter(prm_primaddress_postal_id));
							try {
								postalCode = userService.getAddressBusiness().getPostalCodeHome().findByPrimaryKey(postalID);
							} catch (RemoteException e1) {
								e1.printStackTrace();
							} catch (FinderException e1) {
								e1.printStackTrace();
							}
						}
						
						userService.updateUsersMainAddressOrCreateIfDoesNotExist(user,  street,  postalCode, country, null,null,null, communeID);
					
					}
				}
			}
			// co address part
			if (isRemovedValue(iwc, prm_coaddress_street)) {
				Address address = null;
				try {
					address = userService.getUsersCoAddress(user);
					if (address != null) {
						user.removeAddress(address);
					}
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			else if (
				isNewValue(iwc, prm_coaddress_street)
					|| isNewValue(iwc, prm_coaddress_postal_code)
					|| isNewValue(iwc, prm_coaddress_postal_name)
					|| isNewValue(iwc, prm_cocommune_id)
					|| isNewValue(iwc, prm_coaddress_country)) {
				String street = iwc.getParameter(prm_coaddress_street);
				if (!"".equals(street)) {
					PostalCode postalCode =null;
					Country country = null;
					Integer communeID = null;
					if (iwc.isParameterSet(prm_coaddress_country)) {
						Integer countryID = Integer.valueOf(iwc.getParameter(prm_coaddress_country));
						try {
							country = userService.getAddressBusiness().getCountryHome().findByPrimaryKey(countryID);
						}
						catch (RemoteException e1) {
							e1.printStackTrace();
						}
						catch (FinderException e1) {
							e1.printStackTrace();
						}
					}
					if (country != null
						&& (isNewValue(iwc, prm_coaddress_street)
							||isNewValue(iwc, prm_coaddress_postal_code)
							|| isNewValue(iwc, prm_coaddress_postal_name)
							|| isNewValue(iwc, prm_cocommune_id)
							|| isNewValue(iwc, prm_coaddress_country))) {
						String code = iwc.getParameter(prm_coaddress_postal_code);
						String name = iwc.getParameter(prm_coaddress_postal_name);
						if (!"".equals(code) && !"".equals(name)) {
							boolean postalExists = false;
							try {
								postalCode = userService.getAddressBusiness().getPostalCodeHome().findByPostalCodeAndCountryId(code,((Integer) country.getPrimaryKey()).intValue());
								postalExists = postalCode != null;
							}
							catch (RemoteException e2) {
							}
							catch (FinderException e2) {
							}
							if (warnIfPostalExists && postalExists 	&& (isNewValue(iwc, prm_coaddress_postal_code)	|| isNewValue(iwc, prm_coaddress_postal_name))) {
								String mainPostalExists =	iwrb.getLocalizedString(	"mbe.warning.zip_code_and_city_already_exist",	"Zipl code and city already exist in database");
								this.getParentPage().setOnLoad("alert('" + mainPostalExists + "');");
							}
							if(postalExists && isNewValue(iwc,prm_coaddress_postal_name) && !isNewValue(iwc,prm_coaddress_postal_code)){
								userService.getAddressBusiness().changePostalCodeNameWhenOnlyOneAddressRelated(postalCode,name);
							}
							if(!postalExists){
								try {
									postalCode =	userService.getAddressBusiness().getPostalCodeAndCreateIfDoesNotExist(code,	name,country);
								}
								catch (RemoteException e1) {
									e1.printStackTrace();
								}
							}
							if (iwc.isParameterSet(prm_maincommune_id)) {
								communeID = Integer.valueOf(iwc.getParameter(prm_cocommune_id));
								if(communeID.intValue()<=0)
									communeID = null;
							}
						}
						else if (iwc.isParameterSet(prm_coaddress_postal_id)) {
							Integer postalID = Integer.valueOf(iwc.getParameter(prm_coaddress_postal_id));
							try {
								postalCode = userService.getAddressBusiness().getPostalCodeHome().findByPrimaryKey(postalCode);
							} catch (RemoteException e1) {
								
							} catch (FinderException e1) {
								
							}
						}
						
						userService.updateUsersCoAddressOrCreateIfDoesNotExist(user,  street,  postalCode, country, null,null,null, communeID);
					}
				}
			}
			// phone part
			if (isNewValue(iwc, prm_main_phone)) {
				String number = iwc.getParameter(prm_main_phone);
				userService.updateUserPhone(userID.intValue(), PhoneTypeBMPBean.HOME_PHONE_ID, number);
			}
			else if (isRemovedValue(iwc, prm_main_phone)) {
				Phone phone = null;
				try {
					phone = userService.getUsersHomePhone(user);
					if (phone != null) {
						user.removePhone(phone);
					}
				}
				catch (Exception e1) {
				}
			}
			// email part
			if (isNewValue(iwc, prm_email_address)) {
				String email = iwc.getParameter(prm_email_address);
				userService.updateUserMail(userID.intValue(), email);
			}
			else if (isRemovedValue(iwc, prm_email_address)) {
				Email email = null;
				try {
					email = userService.getUserMail(user);
					if (email != null) {
						user.removeEmail(email);
					}
				}
				catch (Exception e1) {
				}
			}
			// deceased part
			if (iwc.isParameterSet(prm_deceased_date)) {
				IWTimestamp deceased = new IWTimestamp(iwc.getParameter(prm_deceased_date));
				storeUserAsDeceased(iwc, userID, deceased.getDate());
				//TODO use some userbusiness to inform any services that want to know about a deceased user
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
		catch (EJBException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
		catch (CreateException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
		}
	}
	/**
	 * @param string
	 * @return
	 */
	protected boolean isValidPersonalID(String string) {
		return true;
	}
	
	public void initUser(IWContext iwc) {
		if(iwc.isParameterSet(PRM_USER_ID)){
			Integer uid = Integer.valueOf(iwc.getParameter(PRM_USER_ID));
			try {
				user = getUserService(iwc).getUser(uid);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
			
	}
	private void initRelationTypes(IWContext iwc) throws RemoteException {
	}
	protected void storeUserAsDeceased(IWContext iwc, Integer userID, Date deceasedDate) {
		try {
			getUserStatusService(iwc).setUserAsDeceased(userID, deceasedDate);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sets the current users primarykey
	 * @param integer
	 */
	public void setUserID(Integer integer) {
		userID = integer;
	}
	public void addDeceasedListener(IWPageEventListener listener) {
	}
	protected Map getRelations(User user) throws FinderException, RemoteException {
		Map map = new Hashtable();
		if (relationTypes == null)
			relationTypes = new Vector();
		Collection relations = getRelationHome().findGroupsRelationshipsUnder(user);
		String type;
		for (Iterator iter = relations.iterator(); iter.hasNext();) {
			GroupRelation relation = (GroupRelation) iter.next();
			type = relation.getRelationshipType();
			//only show nonpassive relations
			if (!relation.isPassive()) {
				if (showAllRelationTypes && !relationTypes.contains(type)) {
					relationTypes.add(type);
				}
				if (map.containsKey(type)) {
					((List) map.get(type)).add(relation);
				}
				else {
					List list = new Vector();
					list.add(relation);
					map.put(type, list);
				}
			}
		}
		return map;
	}
	public GroupRelationHome getRelationHome() throws RemoteException {
		return (GroupRelationHome) IDOLookup.getHome(GroupRelation.class);
	}
	/* (non-Javadoc)
		 * @see com.idega.presentation.Block#getStyleNames()
		 */
	public Map getStyleNames() {
		HashMap map = new HashMap();
		map.put(STYLENAME_HEADER, headerFontStyle);
		map.put(STYLENAME_TEXT, textFontStyle);
		map.put(STYLENAME_DECEASED, deceasedFontStyle);
		map.put(STYLENAME_BUTTON, buttonStyle);
		map.put(STYLENAME_INTERFACE, interfaceStyle);
		return map;
	}
	private Parameter getOldParameter(String pName, String pValue) {
		return new Parameter(pName + prm_old_value_suffix, pValue);
	}
	private boolean isNewValue(IWContext iwc, String pName) {
		if (iwc.isParameterSet(pName + prm_old_value_suffix) && iwc.isParameterSet(pName)) {
			return !iwc.getParameter(pName + prm_old_value_suffix).equals(iwc.getParameter(pName));
		}
		return iwc.isParameterSet(pName);
	}
	private boolean isRemovedValue(IWContext iwc, String pName) {
		String value = iwc.getParameter(pName);
		if (iwc.isParameterSet(pName + prm_old_value_suffix) && value != null && value.length() == 0)
			return true;
		return false;
	}
	private String getOldValue(IWContext iwc, String pName) {
		return iwc.getParameter(pName + prm_old_value_suffix);
	}
	/**
	 * Sets the relations connector window class, that must be a subclass of GroupRelationConnector
	 * @param windowClass
	 */
	public void setGroupRelationConnectorWindow(Class windowClass) {
		connectorWindowClass = windowClass;
	}
	public UserBusiness getUserService(IWApplicationContext iwac) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
	}
	public GroupBusiness getGroupService(IWApplicationContext iwac) throws RemoteException {
		return (GroupBusiness) IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}
	public UserStatusBusiness getUserStatusService(IWApplicationContext iwac) throws RemoteException {
		return (UserStatusBusiness) IBOLookup.getServiceInstance(iwac, UserStatusBusiness.class);
	}
	public PhoneTypeHome getPhoneHome() throws RemoteException {
		return (PhoneTypeHome) IDOLookup.getHome(PhoneType.class);
	}
	public CountryHome getCountryHome() throws RemoteException {
		return (CountryHome) IDOLookup.getHome(Country.class);
	}
	
	public CommuneBusiness getCommuneBusiness (IWApplicationContext iwac) throws RemoteException {
		return (CommuneBusiness) IBOLookup.getServiceInstance(iwac, CommuneBusiness.class);
	}
	/**
	 * Gets the relation connector window class
	 * @return
	 */
	public Class getConnectorWindowClass() {
		return connectorWindowClass;
	}
	/**
	 * Gets the current heading style
	 * @return
	 */
	public String getHeaderFontStyle() {
		return headerFontStyle;
	}
	/**
	 * Gets the user search block
	 * @return
	 */
	public UserSearcher getSearcher() {
		return searcher;
	}
	/**
	 * Tests flag for showing all relations
	 * @return
	 */
	public boolean isShowAllRelationTypes() {
		return showAllRelationTypes;
	}
	/**
	 * Tests flag for showing user relations
	 * @return
	 */
	public boolean isShowUserRelations() {
		return showUserRelations;
	}
	/**
	 * Gets the normal text style
	 * @return
	 */
	public String getTextFontStyle() {
		return textFontStyle;
	}
	/**
	 * Sets the relation connector window (Subclass of UserRelationConnector )
	 * @param class1
	 */
	public void setConnectorWindowClass(Class class1) {
		connectorWindowClass = class1;
	}
	/**
	 *  Sets the style for headings
	 * @param string
	 */
	public void setHeaderFontStyle(String style) {
		headerFontStyle = style;
	}
	/**
	 * Sets the user searcher block
	 * @param searcher
	 */
	public void setSearcher(UserSearcher searcher) {
		this.searcher = searcher;
	}
	/**
	 * Set flag for showing all found user relations
	 * @param flag
	 */
	public void setShowAllRelationTypes(boolean flag) {
		showAllRelationTypes = flag;
	}
	/**
	 * Sets flag for showing user relations
	 * @param flag
	 */
	public void setShowUserRelations(boolean flag) {
		showUserRelations = flag;
	}
	/**
	 * Sets the style for the normal text
	 * @param string
	 */
	public void setTextFontStyle(String string) {
		textFontStyle = string;
	}
	public synchronized Object clone() {
		UserEditor obj = (UserEditor) super.clone();
		obj.searcher = (UserSearcher) searcher.clone();
		return obj;
	}
	/**
	 * Gets the style used to present the deceased date if set
	 * @return
	 */
	public String getDeceasedFontStyle() {
		return deceasedFontStyle;
	}
	/**
	 * Set the style for the deceased date font style
	 * @param style
	 */
	public void setDeceasedFontStyle(String style) {
		deceasedFontStyle = style;
	}
	/**
	 * Gets unique static parameter name for the user id
	 * @return
	 */
	public static String getUserIDParameterName() {
		return UserSearcher.getUniqueUserParameterName("edt");
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setFirstNameLength(int ) 
	 */
	public void setFirstNameLength(int length) {
		searcher.setFirstNameLength(length);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setLastNameLength(int ) 
	 */
	public void setLastNameLength(int length) {
		searcher.setLastNameLength(length);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setMaxFoundUserCols(int ) 
	 */
	public void setMaxFoundUserCols(int cols) {
		searcher.setMaxFoundUserCols(cols);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setMaxFoundUserRows(int ) 
	 */
	public void setMaxFoundUserRows(int rows) {
		searcher.setMaxFoundUserRows(rows);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setMiddleNameLength(int ) 
	 */
	public void setMiddleNameLength(int length) {
		searcher.setMiddleNameLength(length);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setPersonalIDLength(int ) 
	 */
	public void setPersonalIDLength(int length) {
		searcher.setPersonalIDLength(length);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setShowFirstNameInSearch(boolean)
	 */
	public void setShowFirstNameInSearch(boolean b) {
		searcher.setShowFirstNameInSearch(b);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setShowLastNameInSearch(boolean)
	 */
	public void setShowLastNameInSearch(boolean b) {
		searcher.setShowLastNameInSearch(b);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setShowMiddleNameInSearch(boolean)
	 */
	public void setShowMiddleNameInSearch(boolean b) {
		searcher.setShowMiddleNameInSearch(b);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setShowPersonalIDInSearch(boolean)
	 */
	public void setShowPersonalIDInSearch(boolean b) {
		searcher.setShowPersonalIDInSearch(b);
	}
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setSkipResultsForOneFound(boolean)
	 */
	public void setSkipResultsForOneFound(boolean flag) {
		searcher.setSkipResultsForOneFound(flag);
	}
	
	/* (non-Javadoc)
	 * @see is.idega.idegaweb.member.presentation.UserSearcher#setSkipResultsForOneFound(boolean)
	 */
	public void setLegalNonDigitPIDLetters(String letters) {
		searcher.setLegalNonDigitPIDLetters(letters);
	}
	/**
	* @param obj
	*/
	public void addSearchButtonObject(PresentationObject obj) {
		searcher.addButtonObject(obj);
	}
	/**
	 * Adds object to the next row in the main table
	 * @param object
	 */
	public void addToMainPart(PresentationObject object) {
		mainTable.add(object, 2, mainRow++);
	}
	/**
	 * Gets a styled Text object using the header text style
	 * @param  text to be displayed
	 * @return
	 */
	public Text getHeader(String text) {
		Text t = new Text(text);
		setStyle(t, STYLENAME_HEADER);
		return t;
	}
	/**
	 * Gets a styled Text object using the normal text style
	 * @param  text to be displayed
	 * @return
	 */
	public Text getText(String text) {
		Text t = new Text(text);
		setStyle(t, STYLENAME_TEXT);
		return t;
	}
	/**
	 * Gets the current style used on buttons
	 * @return style
	 */
	public String getButtonStyle() {
		return buttonStyle;
	}
	/**
	 * Gets the current style used on input interfaces
	 * @return style
	 */
	public String getInterfaceStyle() {
		return interfaceStyle;
	}
	/**
	 * Sets the style for buttons
	 * @param style
	 */
	public void setButtonStyle(String style) {
		buttonStyle = style;
	}
	/**
	 * Sets the style for input interfaces
	 * @param style
	 */
	public void setInterfaceStyle(String style) {
		interfaceStyle = style;
	}
	/**
	 * Testing flag for showing close button in the button area
	 * @return
	 */
	public boolean isShowCloseButton() {
		return showCloseButton;
	}
	/**
	 * Set flag for showing close button in the button area
	 * @param flag
	 */
	public void setShowCloseButton(boolean flag) {
		showCloseButton = flag;
	}
	public String getInfoCheckScript() {
		StringBuffer s = new StringBuffer();
		s.append("\nfunction checkInfoForm(){\n\t");
		s.append("\n\t var mainStreetAddress = ").append("findObj('").append(prm_mainaddress_street).append("'); ");
		s.append("\n\t var mainPostalCode = ").append("findObj('").append(prm_mainaddress_postal_code).append("');");
		s.append("\n\t var mainPostalName = ").append("findObj('").append(prm_mainaddress_postal_name).append("');");
		s.append(
			"\n\t if( mainStreetAddress.value != '' || mainPostalCode.value != '' || mainPostalName.value !='' ){ ");
		s
			.append("\n\t\t var msg = '")
			.append(
				iwrb.getLocalizedString(
					"mbe.warning.main_address_item_missing",
					"Please provide the following items for the address"))
			.append("' ");
		s.append("\n\t\t var isAlert = false;");
		s.append("\n\t\t if(mainStreetAddress.value == '' ) {");
		s
			.append("\n\t\t msg += ' ")
			.append(iwrb.getLocalizedString("mbe.warning.missing_main_streetaddress", "Street address"))
			.append("' ");
		s.append("\n\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(mainPostalCode.value == '' ) {");
		s
			.append("\n\t\t\t msg += ' ")
			.append(iwrb.getLocalizedString("mbe.warning.missing_main_postalcode", "Zip code"))
			.append(",' ");
		s.append("\n\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(mainPostalName.value == '' ) {");
		s.append("\n\t\t\t msg += ' ").append(
			iwrb.getLocalizedString("mbe.warning.missing_main_postalname", "City")).append(
			",' \n\t");
		s.append("\n\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(isAlert){");
		s.append("\n\t\t\t alert(msg);");
		s.append("\n\t\t\t  return false;");
		s.append("\n\t\t }");
		s.append("\n\t }");
		s.append("\n\t var coStreetAddress = ").append("findObj('").append(prm_coaddress_street).append("');");
		s.append("\n\t var coPostalCode = ").append("findObj('").append(prm_coaddress_postal_code).append("');");
		s.append("\n\t var coPostalName = ").append("findObj('").append(prm_coaddress_postal_name).append("');");
		s.append("\n\t if( coStreetAddress.value !='' || coPostalCode.value !='' || coPostalName.value !='' ){ \n\t");
		s
			.append("\n\t\t var msg = '")
			.append(
				iwrb.getLocalizedString(
					"mbe.warning.co_address_item_missing",
					"Please provide the following items for the c/o address"))
			.append("' ");
		s.append("\n\t\t if(coStreetAddress.value == '' ) {");
		s
			.append("\n\t\t\t msg += ' ")
			.append(iwrb.getLocalizedString("mbe.warning.missing_co_streetaddress", "Street address"))
			.append(",' ");
		s.append("\n\t\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(coPostalCode.value == '' ) {");
		s.append("\n\t\t\t msg += ' ").append(
			iwrb.getLocalizedString("mbe.warning.missing_co_postalcode", "Zip code")).append(
			",' ");
		s.append("\n\t\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(coPostalName.value == '' ) {");
		s.append("\n\t\t\t msg += ' ").append(
			iwrb.getLocalizedString("mbe.warning.missing_co_postalname", "City")).append(
			",' ");
		s.append("\n\t\t\t isAlert = true");
		s.append("\n\t\t }");
		s.append("\n\t\t if(isAlert) {");
		s.append("\n\t\t\t alert(msg+' co');");
		s.append("\n\t\t\t return false;");
		s.append("\n\t\t }");
		s.append("\n\t }");
		s.append("\n\t return true ").append("\n }");
		return s.toString();
	}
	/**
	 * @return
	 */
	public boolean isAllowNewUserRegistration() {
		return allowNewUserRegistration;
	}
	/**
	 * @param b
	 */
	public void setAllowNewUserRegistration(boolean b) {
		allowNewUserRegistration = b;
	}
	/**
	 * @return
	 */
	public boolean isNewUserView() {
		return newUserView;
	}
	
	public boolean isNewUserView(IWContext iwc) {
		if( iwc.isParameterSet(PRM_NEW_USER)){
			reqNewUser = true;
			return true;
		}
		return false;
	
	}
	/**
	 * @param b
	 */
	public void setNewUserView(boolean b) {
		newUserView = b;
	}
	/**
	 * @return
	 */
	public boolean isAllowPersonalIdEdit(User user) {
		return allowPersonalIdEdit;
	}
	/**
	 * @return
	 */
	public int getNameInputLength() {
		return nameInputLength;
	}
	/**
	 * @return
	 */
	public int getPersonalIdInputLength() {
		return personalIdInputLength;
	}
	/**
	 * @return
	 */
	public boolean isShowMiddleNameInput() {
		return showMiddleNameInput;
	}
	/**
	 * @param b
	 */
	public void setAllowPersonalIdEdit(boolean b) {
		allowPersonalIdEdit = b;
	}
	/**
	 * @param i
	 */
	public void setNameInputLength(int i) {
		nameInputLength = i;
	}
	/**
	 * @param i
	 */
	public void setPersonalIdInputLength(int i) {
		personalIdInputLength = i;
	}
	/**
	 * @param b
	 */
	public void setShowMiddleNameInput(boolean b) {
		showMiddleNameInput = b;
	}
	/**
	 * @return
	 */
	public int getEmailInputLength() {
		return emailInputLength;
	}

	/**
	 * @param emailInputLength
	 */
	public void setEmailInputLength(int emailInputLength) {
		this.emailInputLength = emailInputLength;
	}

	/**
	 * @return
	 */
	public int getStreetInputLength() {
		return streetInputLength;
	}

	/**
	 * @param streetInputLength
	 */
	public void setStreetInputLength(int streetInputLength) {
		this.streetInputLength = streetInputLength;
	}

	/**
	 * @return
	 */
	public int getPostalcodeInputLength() {
		return postalcodeInputLength;
	}

	/**
	 * @param postalcodeInputLength
	 */
	public void setPostalcodeInputLength(int postalcodeInputLength) {
		this.postalcodeInputLength = postalcodeInputLength;
	}

	/**
	 * @return
	 */
	public int getPostalnameInputLength() {
		return postalnameInputLength;
	}

	/**
	 * @param postalnameInputLength
	 */
	public void setPostalnameInputLength(int postalnameInputLength) {
		this.postalnameInputLength = postalnameInputLength;
	}

	/**
	 * @return
	 */
	public int getPhoneInputLength() {
		return phoneInputLength;
	}

	/**
	 * @param phoneInputLength
	 */
	public void setPhoneInputLength(int phoneInputLength) {
		this.phoneInputLength = phoneInputLength;
	}
	
	public void setWarnIfPostalExists(boolean flag){
		warnIfPostalExists = flag;
	}
	
	public boolean isWarnIfPostalExists(){
		return warnIfPostalExists;
	}
	
	public void setShowSeperators(boolean flag){
		showSeperators = flag;
	}
	
	public boolean isShowSeperators(){
		return showSeperators;
	}
	
	public void setWarnIfPersonalIDIsIllegal(boolean flag){
		this.warnIfPersonalIDIsIllegal = flag;
	}
	
	public void setShowDefaultCommuneOption(boolean flag){
		this.showDefaultCommuneOption = flag;
	}
	
	

}
