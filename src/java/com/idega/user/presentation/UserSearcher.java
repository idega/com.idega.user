/*
 * Created on Aug 1, 2003
 *
 */
package com.idega.user.presentation;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.FinderException;

import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.TextSoap;
/**
 * UserSearcher small adjustable search block, used to search for users in database.
 * 
 * @author aron 
 * @version 1.0
 */
public class UserSearcher extends Block {
	private static final String SEARCH_PERSONAL_ID = "usrch_search_pid";
	private static final String SEARCH_LAST_NAME = "usrch_search_lname";
	private static final String SEARCH_MIDDLE_NAME = "usrch_search_mname";
	private static final String SEARCH_FIRST_NAME = "usrch_search_fname";
	public static final String SEARCH_COMMITTED = "mbe_act_search";
	public static final String SEARCH_CLEARED = "mbe_act_clear";
	public final static String STYLENAME_TEXT = "Text";
	public final static String STYLENAME_HEADER = "Header";
	public final static String STYLENAME_BUTTON = "Button";
	public final static String STYLENAME_WARNING = "Warning";
	public final static String STYLENAME_INTERFACE = "Interface";
	private String textFontStyleName = null;
	private String headerFontStyleName = null;
	private String buttonStyleName = null;
	private String warningStyleName = null;
	private String interfaceStyleName = null;
	private String textFontStyle = "font-weight:plain;";
	private String headerFontStyle = "font-weight:bold;";
	private String warningFontStyle = "font-weight:bold;font-color:#FF0000";
	private String buttonStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	private String interfaceStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	/** Parameter for user id */
	private static final String PRM_USER_ID = "usrch_user_id_";
	/** The userID is the handled users ID. */
	private Integer userID = null;
	/** The user currently handled */
	private User user = null;
	/** A Collection of users complying to search */
	private Collection usersFound = null;
	/** flag telling if we have more than one user */
	private boolean hasManyUsers = false;
	/** Determines if we should allow search by users first name*/
	private boolean showFirstNameInSearch = true;
	/** Determines if we should allow search by users middle name*/
	private boolean showMiddleNameInSearch = true;
	/** Determines if we should allow search by users last name*/
	private boolean showLastNameInSearch = true;
	/** Determines if we should allow search by users personal ID*/
	private boolean showPersonalIDInSearch = true;
	/** Maximum search result rows */
	private int maxFoundUserRows = 20;
	/** Maximum search result columns */
	private int maxFoundUserCols = 3;
	/** The dynamic bundle identifier*/
	private String bundleIdentifer = null;
	/** The  static bundle identifier used in this package */
	private static String BUNDLE_IDENTIFIER = "is.idega.idegaweb.member";
	/** The Bundle */
	private IWBundle iwb;
	/** The resource bundle */
	private IWResourceBundle iwrb;
	/** flag for process method */
	private boolean processed = false;
	/** list of maintainparameters */
	private List maintainedParameters = new Vector();
	/** personalID input length */
	private int personalIDLength = 10;
	/** firstname input length */
	private int firstNameLength = 10;
	/** middlename input length */
	private int middleNameLength = 10;
	/**lastname input length */
	private int lastNameLength = 10;
	/** stacked view flag : if stacked heading appears above inputs*/
	private boolean stacked = true;
	/** First letter in names case insensitive*/
	private boolean firstLetterCaseInsensitive = true;
	/** Skip results if only one found */
	private boolean skipResultsForOneFound = true;
	/** Contained in own form */
	private boolean OwnFormContainer = true;
	/** Flag for showing reset button */
	private boolean showResetButton = true;
	private boolean showMultipleResetButton = false;
	/** Flag for hiding buttons */
	private boolean showButtons = true;
	
	/** Flag for forgiving ssn search */
	private boolean useFlexiblePersonalID = true;
	/** unique identifier */
	private String uniqueIdentifier = "unique";
	/** flag for showing result overflow */
	private boolean showOverFlowMessage = true;
	/**Collection of objects for the button area */
	private Collection addedButtons = null;
	private Collection otherClearIdentifiers = null;
	private boolean constrainToUniqueSearch = true;
	
	private Collection monitoredSearchIdentifiers =  null;
	private Map monitorMap = null;
	
	/** flag for making links do form submit */
	private boolean setToFormSubmit = false;
	
	private String legalNonDigitPIDLetters = null;
	
	private void initStyleNames() {
		if (textFontStyleName == null)
			textFontStyleName = getStyleName(STYLENAME_TEXT);
		if (headerFontStyleName == null)
			headerFontStyleName = getStyleName(STYLENAME_HEADER);
		if (buttonStyleName == null)
			buttonStyleName = getStyleName(STYLENAME_BUTTON);
		if (warningStyleName == null)
			warningStyleName = getStyleName(STYLENAME_WARNING);
		if (interfaceStyleName == null)
			interfaceStyleName = getStyleName(STYLENAME_INTERFACE);
	}
	public void main(IWContext iwc) throws Exception {
		//debugParameters(iwc);
		initStyleNames();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		String message = null;
		try {
			process(iwc);
		}
		catch (RemoteException e) {
			e.printStackTrace();
			message = iwrb.getLocalizedString("usrch.service_available", "Search service not available");
		}
		catch (FinderException e) {
			//e.printStackTrace();
			message = iwrb.getLocalizedString("usrch.no_user_found", "No user found");
		}
		Table T = new Table();		
		T.add(presentateCurrentUserSearch(iwc), 1, 2);
		if (!skipResultsForOneFound || hasManyUsers) {
			T.add(presentateFoundUsers(iwc), 1, 3);
		}
		if (message != null) {
			Text tMessage = new Text(message);
			tMessage.setStyleAttribute("color:red");
			T.add(tMessage, 1, 1);
		}
		if (OwnFormContainer) {
			Form form = new Form();
			form.add(T);
			add(form);
		}
		else {
			add(T);
		}
	}
	/**
	 * Main processing method, searches if search has ben committed, or looks up the user chosen
	 * is called by main(),
	 * @param iwc
	 */
	public void process(IWContext iwc) throws FinderException, RemoteException {
		if (processed)
			return;
		String searchIdentifier = constrainToUniqueSearch ? uniqueIdentifier : "";
		
		if (iwc.isParameterSet(PRM_USER_ID + uniqueIdentifier)) {
			userID = Integer.valueOf((iwc.getParameter(PRM_USER_ID + uniqueIdentifier)).trim());
		}
		if (iwc.isParameterSet(SEARCH_COMMITTED + searchIdentifier)) {
			processSearch(iwc);
		}
		
		if (userID != null && userID.intValue()>0) {
			try {
				UserHome home = (UserHome) IDOLookup.getHome(User.class);
				user = home.findByPrimaryKey(userID);
			}
			catch (IDOLookupException e) {
				throw new FinderException(e.getMessage());
			}
		}
		digMonitors(iwc);
		processed = true;
	}
	
	private void digMonitors(IWContext iwc){
		
		if(monitoredSearchIdentifiers!=null && !monitoredSearchIdentifiers.isEmpty()){
			monitorMap = new Hashtable();
			for (Iterator iter = monitoredSearchIdentifiers.iterator(); iter.hasNext();) {
				String identifier = (String) iter.next();
				boolean addSearchPrm = false;
				if(iwc.isParameterSet(SEARCH_FIRST_NAME+identifier)){
					monitorMap.put(SEARCH_FIRST_NAME+identifier,iwc.getParameter(SEARCH_FIRST_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_MIDDLE_NAME+identifier)){
					monitorMap.put(SEARCH_MIDDLE_NAME+identifier,iwc.getParameter(SEARCH_MIDDLE_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_LAST_NAME+identifier)){
					monitorMap.put(SEARCH_LAST_NAME+identifier,iwc.getParameter(SEARCH_LAST_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_PERSONAL_ID+identifier)){
					monitorMap.put(SEARCH_PERSONAL_ID+identifier,iwc.getParameter(SEARCH_PERSONAL_ID+identifier));
					addSearchPrm = true;
				}
				if(addSearchPrm){
					monitorMap.put(SEARCH_COMMITTED + (constrainToUniqueSearch ? identifier : ""),"true");	
				
				}
				
			}
		}
	}

	
	public boolean isClearedButtonPushed(IWContext iwc){
		return iwc.getParameter(SEARCH_CLEARED) != null;
	}
	
	private void processSearch(IWContext iwc) throws IDOLookupException, FinderException, RemoteException {
		UserHome home = (UserHome) IDOLookup.getHome(User.class);
		String first = iwc.getParameter(SEARCH_FIRST_NAME + uniqueIdentifier);
		String middle = iwc.getParameter(SEARCH_MIDDLE_NAME + uniqueIdentifier);
		String last = iwc.getParameter(SEARCH_LAST_NAME + uniqueIdentifier);
		String pid = iwc.getParameter(SEARCH_PERSONAL_ID + uniqueIdentifier);
		
		if (firstLetterCaseInsensitive) {
			if (first != null)
				first = TextSoap.capitalize(first);
			if (middle != null)
				middle = TextSoap.capitalize(middle);
			if (last != null)
				last = TextSoap.capitalize(last);
		}
		// dont allow empty search
		if ((pid != null && pid.length() > 0)
			|| (first != null && first.length() > 0)
			|| (middle != null && middle.length() > 0)
			|| (last != null && last.length() > 0)){
			// forgiving search criteria
			if(useFlexiblePersonalID){
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<pid.length(); i++) {
					char ch = pid.charAt(i);
					if (Character.isDigit(ch)){
						sb.append(ch);
					}
					else if(legalNonDigitPIDLetters!=null){
						if(legalNonDigitPIDLetters.indexOf(ch)>=0){
							// non digit letters turned to uppercase
							sb.append(Character.toUpperCase(ch));
						}
					}
				}
				//sb.insert(0,"%");
				pid = sb.toString();
			}
			usersFound =home.findUsersByConditions(first, middle, last, pid, null, null, -1, -1, -1, -1, null, null, true, false);
		}
		else{
			user = null;
		}
		//System.out.println("users found " + usersFound.size());
		if (user == null && usersFound != null) {
			// if some users found
			if (!usersFound.isEmpty()) {
				hasManyUsers = usersFound.size() > 1;
				if (!hasManyUsers)
					user = (User) usersFound.iterator().next();
			}
			// if no user found
			else {
				throw new FinderException("No user was found");
			}
		}
	}
	/**
		 * Presentates the users personal info
		 * @param iwc the current context
		 */
	private Table presentateCurrentUserSearch(IWContext iwc) {
		Table searchTable = new Table();
		int row = 1;
		int col = 1;
		Vector clearFields = new Vector();
				
		if (showPersonalIDInSearch) {
			Text tPersonalID = new Text(iwrb.getLocalizedString(SEARCH_PERSONAL_ID, "Personal ID"));
			tPersonalID.setStyleClass(headerFontStyleName);
			tPersonalID.setStyleAttribute(headerFontStyle);			
			searchTable.add(tPersonalID, col, row);
			TextInput input = new TextInput(SEARCH_PERSONAL_ID + uniqueIdentifier);
			input.setStyleClass(interfaceStyleName);
			input.setLength(personalIDLength);
			if (user != null && user.getPersonalID() != null) {
				input.setContent(user.getPersonalID());
			}
			if (stacked)
				searchTable.add(input, col++, row + 1);
			else
				searchTable.add(input, ++col, row);
			clearFields.add(SEARCH_PERSONAL_ID);
		}
		if (showLastNameInSearch) {
			Text tLastName = new Text(iwrb.getLocalizedString(SEARCH_LAST_NAME, "Last name"));
			tLastName.setStyleClass(headerFontStyleName);
			tLastName.setStyleAttribute(headerFontStyle);			
			searchTable.add(tLastName, col, row);
			TextInput input = new TextInput(SEARCH_LAST_NAME + uniqueIdentifier);
			input.setStyleClass(interfaceStyleName);
			input.setLength(lastNameLength);
			if (user != null && user.getLastName() != null) {
				input.setContent(user.getLastName());
			}
			if (stacked)
				searchTable.add(input, col++, row + 1);
			else
				searchTable.add(input, ++col, row);
			clearFields.add(SEARCH_LAST_NAME);
		}
		if (showMiddleNameInSearch) {
			Text tMiddleName = new Text(iwrb.getLocalizedString(SEARCH_MIDDLE_NAME, "Middle name"));
			tMiddleName.setStyleClass(headerFontStyleName);
			tMiddleName.setStyleAttribute(headerFontStyle);
			
			searchTable.add(tMiddleName, col, row);
			TextInput input = new TextInput(SEARCH_MIDDLE_NAME + uniqueIdentifier);
			input.setStyleClass(interfaceStyleName);
			input.setLength(middleNameLength);
			if (user != null && user.getMiddleName() != null) {
				input.setContent(user.getMiddleName());
			}
			if (stacked)
				searchTable.add(input, col++, row + 1);
			else
				searchTable.add(input, ++col, row);
			clearFields.add(SEARCH_MIDDLE_NAME);
		}
		if (showFirstNameInSearch) {
			Text tFirstName = new Text(iwrb.getLocalizedString(SEARCH_FIRST_NAME, "First name"));
			tFirstName.setStyleClass(headerFontStyleName);
			tFirstName.setStyleAttribute(headerFontStyle);
			searchTable.add(tFirstName, col, row);
			TextInput input = new TextInput(SEARCH_FIRST_NAME + uniqueIdentifier);
			input.setStyleClass(interfaceStyleName);
			input.setLength(firstNameLength);
			if (user != null) {
				input.setContent(user.getFirstName());
			}
			if (stacked)
				searchTable.add(input, col++, row + 1);
			else
				searchTable.add(input, ++col, row);
			clearFields.add(SEARCH_FIRST_NAME);
		}
		if (showButtons) {
			SubmitButton search =
				new SubmitButton(
					iwrb.getLocalizedString(SEARCH_COMMITTED, "Search"),
					SEARCH_COMMITTED + (constrainToUniqueSearch ? uniqueIdentifier : ""),
					"true");
			search.setStyleClass(buttonStyleName);
			if (stacked) {
				searchTable.add(search, col++, row + 1);
			}
			else
				searchTable.add(search, 1, row + 1);
			if (addedButtons != null && !addedButtons.isEmpty()) {
				for (Iterator iter = addedButtons.iterator(); iter.hasNext();) {
					PresentationObject element = (PresentationObject) iter.next();
					if (stacked)
						searchTable.add(element, col++, row + 1);
					else
						searchTable.add(element, 1, row + 1);
				}
			}
			if (showResetButton) {
				String clearAction = "";
				for (Iterator iter = clearFields.iterator(); iter.hasNext();) {
					String field = (String) iter.next();
					clearAction += getClearActionPart(field, uniqueIdentifier,"''");
				}
				clearAction +=getClearActionObjectTest(PRM_USER_ID,uniqueIdentifier);
				clearAction += getClearActionPart(PRM_USER_ID,uniqueIdentifier,"-1");
				SubmitButton reset = new SubmitButton(SEARCH_CLEARED, iwrb.getLocalizedString("clear", "Clear"));
				reset.setStyleClass(buttonStyleName);
				reset.setOnClick(clearAction + "return false;");
				searchTable.add(reset, col++, row + 1);
			}
			if (showMultipleResetButton) {
				addClearButtonIdentifiers(uniqueIdentifier);
				String otherClearActions = "";
				for (Iterator iter = otherClearIdentifiers.iterator(); iter.hasNext();) {
					String identifier = (String) iter.next();
					for (Iterator iter2 = clearFields.iterator(); iter2.hasNext();) {
						String field = (String) iter2.next();
						otherClearActions += getClearActionPart(field, identifier,"''");
					}
					otherClearActions +=getClearActionObjectTest(PRM_USER_ID,identifier);
					otherClearActions +=getClearActionPart(PRM_USER_ID,identifier,"-1");
				}
			
			SubmitButton resetmultiple = new SubmitButton(SEARCH_CLEARED, iwrb.getLocalizedString("clear_all", "Clear All"));
			resetmultiple.setStyleClass(buttonStyleName);
			resetmultiple.setOnClick(otherClearActions + "return false;");
			searchTable.add(resetmultiple, col++, row + 1);
		}
	}
	return searchTable;
}
private String getClearActionPart(String field, String identifier,String value) {
	return "this.form." + field + identifier + ".value ="+value+" ;";
}

private String getClearActionObjectTest(String field,String identifier){
	return "if(this.form." + field + identifier + ")";
}
/**
	 * Presentates the users found by search
	 * @param iwc the context
	*/
private Table presentateFoundUsers(IWContext iwc) {
	Table T = new Table();
	if (usersFound != null && !usersFound.isEmpty()) {
		Iterator iter = usersFound.iterator();
		T.setCellspacing(4);
		Link userLink;
		int row = 1;
		int col = 1;
		int colAdd = 1;


		HiddenInput userPk = new HiddenInput(getUniqueUserParameterName(uniqueIdentifier));
		if (setToFormSubmit) {
			getParentForm().add(userPk);
			addParameters(getParentForm());				
		}
						
		while (iter.hasNext()) {
			User u = (User) iter.next();
			T.add(PersonalIDFormatter.format(u.getPersonalID(),iwc.getCurrentLocale()), colAdd, row);
			userLink = new Link(u.getName());
			
			//Added by Roar 29.10.03
			if (setToFormSubmit){
				userLink.setToFormSubmit(getParentForm());	
				userLink.setOnClick("findObj('"+ userPk.getID() +"').value='"+ u.getPrimaryKey() +"';");
			}
			
			userLink.addParameter(getUniqueUserParameter((Integer) u.getPrimaryKey()));
			addParameters(userLink);
			T.add(userLink, colAdd + 1, row);
			row++;
			if (row == maxFoundUserRows) {
				col++;
				colAdd += 2;
				row = 1;
			}
			if (col == maxFoundUserCols) {
				break;
			}
		}
		if (showOverFlowMessage && iter.hasNext()) {
			int lastRow = T.getRows() + 1;
			T.mergeCells(1, lastRow, maxFoundUserCols, lastRow);
			Text tOverflowMessage =
				new Text(
					iwrb.getLocalizedString(
						"usrch_overflow_message",
						"There are more hits in your search than shown, you have to narrow down your searchcriteria"));
			tOverflowMessage.setStyleClass(warningStyleName);
			T.add(tOverflowMessage, 1, lastRow);
		}
	}
	return T;
}
public void addClearButtonIdentifiers(String identifier) {
	if (otherClearIdentifiers == null)
		otherClearIdentifiers = new Vector();
	otherClearIdentifiers.add(identifier);
}
private void addParameters(Link link) {
	for (Iterator iter = maintainedParameters.iterator(); iter.hasNext();) {
		Parameter element = (Parameter) iter.next();
		link.addParameter(element);
	}
	if(monitorMap!=null){
		for (Iterator iter = monitorMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			link.addParameter((String)entry.getKey(),(String)entry.getValue());
		}
	}
}

private void addParameters(Form form) {
	for (Iterator iter = maintainedParameters.iterator(); iter.hasNext();) {
		Parameter element = (Parameter) iter.next();
		form.addParameter(element.getName(), element.getValueAsString());
	}
	if(monitorMap!=null){
		for (Iterator iter = monitorMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			form.addParameter((String)entry.getKey(),(String)entry.getValue());
		}
	}
}


/**
 * Flags the first name field in the user search
 * @param b
 */
public void setShowFirstNameInSearch(boolean b) {
	showFirstNameInSearch = b;
}
/**
 * Flags the last name in the user search
 * @param b
 */
public void setShowLastNameInSearch(boolean b) {
	showLastNameInSearch = b;
}
/**
 * Flags the middle name in the user search
 * @param b
 */
public void setShowMiddleNameInSearch(boolean b) {
	showMiddleNameInSearch = b;
}
/**
 * Flags the personal id in the user search
 * @param b
 */
public void setShowPersonalIDInSearch(boolean b) {
	showPersonalIDInSearch = b;
}
/**
 * Flag telling if  the search found more than one user
 * @return
 */
public boolean isHasManyUsers() {
	return hasManyUsers;
}
/**
 * Gets the number of maximum allowed result columns 
 * @return
 */
public int getMaxFoundUserCols() {
	return maxFoundUserCols;
}
/**
 * Gets the number of maximum allowed result rows
 * @return
 */
public int getMaxFoundUserRows() {
	return maxFoundUserRows;
}
/**
 * Gets the selected user
 * @return User
 */
public User getUser() {
	return user;
}
/**
 * Gets the collection of users found by searc
 * @return
 */
public Collection getUsersFound() {
	return usersFound;
}
/**
 * Set the maximum number of columns showing search results
 * @param cols
 */
public void setMaxFoundUserCols(int cols) {
	maxFoundUserCols = cols;
}
/**
 * Sets the maximum number of rows showing search results
 * @param i
 */
public void setMaxFoundUserRows(int rows) {
	maxFoundUserRows = rows;
}
/**
 * Manually set the found user
 * @param user
 */
public void setUser(User user) {
	this.user = user;
}
/**
 * Manually set the found user collection
 * @param collection
 */
public void setUsersFound(Collection collection) {
	usersFound = collection;
}
/**
 * Add maintainedparameters
 * @param parameter
 */
public void maintainParameter(Parameter parameter) {
	maintainedParameters.add(parameter);
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
 * Gets the unique user id parameter  to be used for chosen user
 * @param userID
 * @return Parameter
 */
public Parameter getUniqueUserParameter(Integer userID) {
	return new Parameter(getUniqueUserParameterName(uniqueIdentifier), userID.toString());
}
/**
 * Gets the unique user id parameter name to be used for chosen user
 * @param uniqueIdentifier
 * @return parameter name
 */
public static String getUniqueUserParameterName(String uniqueIdentifier) {
	return PRM_USER_ID + uniqueIdentifier;
}
/* (non-Javadoc)
 * @see com.idega.presentation.Block#getStyleNames()
 */
public Map getStyleNames() {
	HashMap map = new HashMap();
	map.put(STYLENAME_HEADER, headerFontStyle);
	map.put(STYLENAME_TEXT, textFontStyle);
	map.put(STYLENAME_BUTTON, buttonStyle);
	map.put(STYLENAME_WARNING, warningFontStyle);
	map.put(STYLENAME_INTERFACE, interfaceStyle);
	return map;
}
/**
 * Gets the input length of the first name input
 * @return
 */
public int getFirstNameLength() {
	return firstNameLength;
}
/**
 * Gets the heading font style
 * @return font style
 */
public String getHeaderFontStyle() {
	return headerFontStyle;
}
/**
 * Gets the inputlength of the last name input
 * @return length
 */
public int getLastNameLength() {
	return lastNameLength;
}
/**
 * Gets the input length of the middle name input
 * @return length
 */
public int getMiddleNameLength() {
	return middleNameLength;
}
/**
 * Gets the inputlength of the personal id input
 * @return length
 */
public int getPersonalIDLength() {
	return personalIDLength;
}
/**
 * Gets flag for first name input appearance
 * @return flag
 */
public boolean isShowFirstNameInSearch() {
	return showFirstNameInSearch;
}
/**
 * Gets flag for last name input appearance
 * @return flag
 */
public boolean isShowLastNameInSearch() {
	return showLastNameInSearch;
}
/**
 * Gets flag for middle name input appearance
 * @return flag
 */
public boolean isShowMiddleNameInSearch() {
	return showMiddleNameInSearch;
}
/**
 * Gets flag for personal ID appearance
 * @return flag
 */
public boolean isShowPersonalIDInSearch() {
	return showPersonalIDInSearch;
}
/**
 * Gets status of stacked flag
 * @return flag <code>boolean</code>
 */
public boolean isStacked() {
	return stacked;
}
/**
 * Gets the normal text font style
 * @return font style
 */
public String getTextFontStyle() {
	return textFontStyle;
}
/**
 * Sets the length of the first name input
 * @param length
 */
public void setFirstNameLength(int length) {
	firstNameLength = length;
}
/**
 * Sets the heading font style
 * @param style
 */
public void setHeaderFontStyle(String style) {
	headerFontStyle = style;
}
/**
 * Sets the length of the last name input
 * @param length
 */
public void setLastNameLength(int length) {
	lastNameLength = length;
}
/**
 * Sets the length of the middle name input
 * @param length
 */
public void setMiddleNameLength(int length) {
	middleNameLength = length;
}
/**
 * Sets the  length of the personalID input
 * @param length
 */
public void setPersonalIDLength(int length) {
	personalIDLength = length;
}
/**
 * Flags if searcher should be presented with stacked headers and inputs
 * @param flag
 */
public void setStacked(boolean flag) {
	stacked = flag;
}
/**
 * Set normal text font style
 * @param style
 */
public void setTextFontStyle(String style) {
	textFontStyle = style;
}
/**
 * Returns the status of the flag , concerning the searcher own form
 * @return flag status
 */
public boolean isOwnFormContainer() {
	return OwnFormContainer;
}
/**
 * Flags if the searcher should provide its own form
 * @param flag
 */
public void setOwnFormContainer(boolean flag) {
	OwnFormContainer = flag;
}
/**
 * Gets the unique identifier for this searcher instance
 * @return unique identifier
 */
public String getUniqueIdentifier() {
	return uniqueIdentifier;
}
/**
 * Sets a unique identifier, convenient when using many instances on same page
 * @param identifier
 */
public void setUniqueIdentifier(String identifier) {
	uniqueIdentifier = identifier;
}
/**
 * @return
 */
public String getButtonStyle() {
	return buttonStyle;
}
/**
 * @return
 */
public String getButtonStyleName() {
	return buttonStyleName;
}
/**
 * @return
 */
public String getHeaderFontStyleName() {
	return headerFontStyleName;
}
/**
 * Gets the button style
 * @return style
 */
public String getTextFontStyleName() {
	return textFontStyleName;
}
/**
 * Sets the button style
 * @param string
 */
public void setButtonStyle(String string) {
	buttonStyle = string;
}
/**
 * Sets the button style name to use
 * @param string
 */
public void setButtonStyleName(String string) {
	buttonStyleName = string;
}
/**
 * Sets the header font style name to use
 * @param string
 */
public void setHeaderFontStyleName(String string) {
	headerFontStyleName = string;
}
/**
 * Sets the normal font style name to use
 * @param string
 */
public void setTextFontStyleName(String string) {
	textFontStyleName = string;
}
/**
 * Flag status, skips result list if only one user found
 * @return
 */
public boolean isSkipResultsForOneFound() {
	return skipResultsForOneFound;
}
/**
 * Sets flag for skipping result list if only one user found
 * @param 
 */
public void setSkipResultsForOneFound(boolean flag) {
	skipResultsForOneFound = flag;
}
/**
 * @return
 */
public boolean isShowResetButton() {
	return showResetButton;
}
/**
 * @param b
 */
public void setShowResetButton(boolean b) {
	showResetButton = b;
}
/**
 * @return
 */
public boolean isShowOverFlowMessage() {
	return showOverFlowMessage;
}
/**
 * @param b
 */
public void setShowOverFlowMessage(boolean b) {
	showOverFlowMessage = b;
}
public void addButtonObject(PresentationObject obj) {
	if (addedButtons == null)
		addedButtons = new Vector();
	addedButtons.add(obj);
}
/**
 * @return
 */
public boolean isShowButtons() {
	return showButtons;
}
/**
 * @param b
 */
public void setShowButtons(boolean b) {
	showButtons = b;
}
/**
 * @return
 */
public boolean isConstrainToUniqueSearch() {
	return constrainToUniqueSearch;
}
/**
 * @param constrainToUniqueSearch
 */
public void setConstrainToUniqueSearch(boolean constrainToUniqueSearch) {
	this.constrainToUniqueSearch = constrainToUniqueSearch;
}
/**
 * @return
 */
public boolean isShowMultipleResetButton() {
	return showMultipleResetButton;
}
/**
 * @param showMultipleResetButton
 */
public void setShowMultipleResetButton(boolean showMultipleResetButton) {
	this.showMultipleResetButton = showMultipleResetButton;
}

public void addMonitoredSearchIdentifier(String identifier){
	if(monitoredSearchIdentifiers==null)
		monitoredSearchIdentifiers = new Vector();
	monitoredSearchIdentifiers.add(identifier);
}

public void setToFormSubmit(boolean b){
	setToFormSubmit = b;
}

public boolean getToFormSubmit(){
	return setToFormSubmit;
}

public void setUseFlexiblePersonalID(boolean flag){
	useFlexiblePersonalID = flag;
}

public boolean isUseFlexiblePersonalID(){
	return useFlexiblePersonalID;
}
	/**
	 * @return Returns the legalNonDigitPIDLetters.
	 */
	public String getLegalNonDigitPIDLetters() {
		return legalNonDigitPIDLetters;
	}

	/**
	 * @param legalNonDigitPIDLetters The legalNonDigitPIDLetters to set.
	 */
	public void setLegalNonDigitPIDLetters(String legalNonDigitPIDLetters) {
		this.legalNonDigitPIDLetters = legalNonDigitPIDLetters;
	}

}