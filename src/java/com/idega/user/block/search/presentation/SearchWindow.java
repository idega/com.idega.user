package com.idega.user.block.search.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.app.ToolbarElement;
import com.idega.user.app.UserApplicationMainArea;
import com.idega.user.app.UserApplicationMainAreaPS;
import com.idega.user.block.search.event.UserSearchEvent;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupComparator;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.CachedGroup;
import com.idega.user.data.Group;
import com.idega.user.presentation.UserStatusDropdown;


/**
 * <p>Title: idegaWeb User</p>
 * <p>Description: The standard advances search window of the IW User system</p>
 * <p>Copyright: Idega Software Copyright (c) 2002</p>
 * <p>Company: Idega Software</p>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0 
 */
public class SearchWindow extends StyledIWAdminWindow implements ToolbarElement { //changed from extends IWAdminWindow - birna
	
	private UserBusiness userBiz;
	private GroupBusiness groupBiz;

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private static final String HELP_TEXT_KEY = "search_window";
  
	private UserSearchEvent searchEvent;
  private String userApplicationMainAreaPSId = null; 
  private Group selectedGroup = null;
  
  private String mainTableStyle = "main";


	public SearchWindow() {
		setWidth(500);
		setHeight(370);
		setScrollbar(false);
		setResizable(true);
	}

	public void initializeInMain(IWContext iwc) {    
		userApplicationMainAreaPSId = iwc.getParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY);
		
		// add action listener
		IWStateMachine stateMachine;
   
		try {
			stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
			if (userApplicationMainAreaPSId != null) {
				addActionListener( (IWActionListener)stateMachine.getStateFor(userApplicationMainAreaPSId, UserApplicationMainAreaPS.class));
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
  
	}
	

	public void main(IWContext iwc) throws Exception {
		//this.debugParameters(iwc);
		IWResourceBundle iwrb = getResourceBundle(iwc);
		searchEvent = new UserSearchEvent();
		searchEvent.setSource(this);
					
		setTitle(iwrb.getLocalizedString("advanced_searchwindow.title", "Advanced search"));
		addTitle(iwrb.getLocalizedString("advanced_searchwindow.title", "Advanced search"), TITLE_STYLECLASS);
		setName(iwrb.getLocalizedString("advanced_searchwindow.title", "Advanced search"));
		
		Form form = new Form();
		form.addEventModel(searchEvent, iwc);
		add(form,iwc);

		Table mainTable = new Table(1, 3);
		mainTable.setWidth(Table.HUNDRED_PERCENT);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		mainTable.setHeight(2, 5);
		form.add(mainTable);
		
		Table tab = new Table(3, 6);
		tab.setColumns(3);
		tab.setStyleClass(mainTableStyle);
		tab.setWidth(Table.HUNDRED_PERCENT);
		tab.setBorder(0);
		tab.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);
		tab.setColumnVerticalAlignment(2, Table.VERTICAL_ALIGN_TOP);
		tab.setCellspacing(5);
		int column = 1;
		int row = 1;
		
		//names params
		//first name
		TextInput firstName = new TextInput(searchEvent.SEARCH_FIELD_FIRST_NAME);
		Text firstNameText = new Text(iwrb.getLocalizedString("user.search.window.user_first_name", "First name"));
		tab.add(firstNameText, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(firstName, column++, row);

		//middle name
		TextInput middleName = new TextInput(searchEvent.SEARCH_FIELD_MIDDLE_NAME);
		Text middleNameText = new Text(iwrb.getLocalizedString("user.search.window.user_middle_name", "Middle name"));
		tab.add(middleNameText, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(middleName, column++, row);
		
		//middle name
		TextInput lastName = new TextInput(searchEvent.SEARCH_FIELD_LAST_NAME);
		Text lastNameText = new Text(iwrb.getLocalizedString("user.search.window.user_last_name", "Last name"));
		tab.add(lastNameText, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(lastName, column++, row++);
		
		//group selectionbox
		
		SelectionBox groupSel = new SelectionBox(UserSearchEvent.SEARCH_FIELD_GROUPS);
		groupSel.setHeight(15); 
		groupSel.setWidth(Table.HUNDRED_PERCENT);

		List groupsCol = (List)getUserBusiness(iwc).getAllGroupsWithViewPermission(iwc.getCurrentUser(),iwc);
		GroupComparator groupComparator = new GroupComparator(iwc);
		groupComparator.setSortByParents(true);
		groupComparator.setGroupBusiness(this.getGroupBusiness(iwc));
		Collections.sort(groupsCol, groupComparator);
		Iterator nodes = groupsCol.iterator();
//		Map cachedParents = new HashMap();  // No dublicates so this doesn't do anything
//		Map cachedGroups = new HashMap();
		for(int i = 0;nodes.hasNext();i++) {
			Group group = (Group) nodes.next();
			CachedGroup cachedGroup = new CachedGroup(group);
			try {
				groupSel.addMenuElement( ((Integer)group.getPrimaryKey()).intValue(), groupComparator.getIndentedGroupName(cachedGroup));//getGroupBusiness(iwc).getNameOfGroupWithParentName(group));//,cachedParents,cachedGroups) );
				//getchildren
			} catch (NullPointerException e) {
				System.out.println("[SearchWindow]: null in group list index "+ i);
				e.printStackTrace();
			}
		}
		
		column = 1;
		Text groups = new Text(iwrb.getLocalizedString("user.search.window.groups", "Groups"));
		tab.mergeCells(column, row, column+1, tab.getRows());
		tab.add(groups, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(groupSel, column, row); 
		
		
//	personal id
		column = 3;
		TextInput ssn = new TextInput(searchEvent.SEARCH_FIELD_PERSONAL_ID);
		Text ssnText = new Text(iwrb.getLocalizedString("user.search.window.personal_id", "SSN"));
		tab.add(ssnText, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(ssn, column, row++);
			
			
//	streetname search
		TextInput address = new TextInput(searchEvent.SEARCH_FIELD_ADDRESS);
		Text addressText = new Text(iwrb.getLocalizedString("user.search.window.address", "Address"));
		tab.add(addressText, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(address, column, row++);
		
//	user status dropdown
		DropdownMenu statusMenu = new UserStatusDropdown(UserSearchEvent.SEARCH_FIELD_STATUS_ID);
		statusMenu.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
		statusMenu.addMenuElement(-1,iwrb.getLocalizedString("user.search.window.all_statuses", "All statuses"));
		statusMenu.setSelectedElement(-1);
	
	
		Text status = new Text(iwrb.getLocalizedString("user.search.window.status", "Status"));
		tab.add(status, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(statusMenu, column, row++);
	
	
		//age
		TextInput ageFloor = new TextInput(searchEvent.SEARCH_FIELD_AGE_FLOOR,"0");
		ageFloor.setLength(3);
		ageFloor.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
	
		TextInput ageCeil = new TextInput(searchEvent.SEARCH_FIELD_AGE_CEILING,"120");
		ageCeil.setLength(3);
		ageCeil.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

		Text ages = new Text(iwrb.getLocalizedString("user.search.window.ages", "Age"));
		tab.add(ages, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(ageFloor, column, row);
		tab.add(" - ", column, row);
		tab.add(ageCeil, column, row++);
	
	
		//gender
		Integer maleId = getUserBusiness(iwc).getGenderId("male");
		Integer femaleId = getUserBusiness(iwc).getGenderId("female");	
		DropdownMenu genders = new DropdownMenu(UserSearchEvent.SEARCH_FIELD_GENDER_ID);
		genders.addMenuElement(femaleId.intValue(),iwrb.getLocalizedString("user.search.window.females", "Women"));
		genders.addMenuElement(maleId.intValue(),iwrb.getLocalizedString("user.search.window.males", "Men"));
		genders.addMenuElement(-1,iwrb.getLocalizedString("user.search.window.both.genders", "Both genders"));
		genders.setSelectedElement(-1);
	
		Text gender = new Text(iwrb.getLocalizedString("user.search.window.gender", "Gender"));
		tab.add(gender, column, row);
		tab.add(Text.getBreak(), column, row);
		tab.add(genders, column, row++); 		

		//buttons
		Help help = getHelp(HELP_TEXT_KEY);
		StyledButton save = new StyledButton(new SubmitButton(iwrb.getLocalizedString("user.search.window.search", "Search")));
   	StyledButton close = new StyledButton(new CloseButton(iwrb.getLocalizedString("user.search.window.close", "Close")));
    
		HiddenInput type = new HiddenInput(UserSearchEvent.SEARCH_FIELD_SEARCH_TYPE, Integer.toString(UserSearchEvent.SEARCHTYPE_ADVANCED));
		
		
		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setStyleClass(mainTableStyle);
		bottomTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(help,1,1);

		Table buttonTable = new Table();
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setWidth(2, "5");
		buttonTable.add(save, 1, 1);
		buttonTable.add(type, 2, 1);
		buttonTable.add(close, 3, 1);			
		bottomTable.add(buttonTable,2,1);

		mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
		mainTable.add(tab,1,1);
		mainTable.add(bottomTable,1,3);		
	}


	public Image getButtonImage(IWContext iwc) {
		IWBundle bundle = this.getBundle(iwc);
		return bundle.getImage("create_group.gif", "Create group");
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}


	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("searchwindow.name", "Search");
	}

	public Class  getPresentationObjectClass(IWContext iwc) {
		return this.getClass();
	}
	
	public boolean isValid(IWContext iwc) {
		return true;
	}
	
	public Map  getParameterMap(IWContext iwc) {
		return null;
	}
	
	public int getPriority(IWContext iwc) {
		return -1;
	}
	
	public GroupBusiness getGroupBusiness(IWContext iwc) {
		if(groupBiz==null){	
			try {
				groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}	
		}	
		return groupBiz;
	}
	
	public UserBusiness getUserBusiness(IWContext iwc) {
		if(userBiz==null){	
			try {
				userBiz = (UserBusiness) IBOLookup.getServiceInstance(iwc,UserBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}	
		}	
		return userBiz;
	}

	/* (non-Javadoc)
	 * @see com.idega.user.app.ToolbarElement#isButton(com.idega.presentation.IWContext)
	 */
	public boolean isButton(IWContext iwc) {
		return false;
	}
  
}