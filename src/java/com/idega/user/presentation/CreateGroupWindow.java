package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.ChangeListener;

import com.idega.builder.presentation.StyledIBPageChooser;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.*;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.app.ToolbarElement;
import com.idega.user.app.UserApplication;
import com.idega.user.app.UserApplicationMenuAreaPS;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.event.CreateGroupEvent;

/**
 *
 * <p>Title: idegaWeb User</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0 
 */
public class CreateGroupWindow extends StyledIWAdminWindow implements StatefullPresentation, ToolbarElement { //changed from extends IWAdminWindow
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	
	public static final String SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY = "selected_group_pp_id_key";
	public static final String NO_GROUP_SELECTED = "no_group_selected";
	
	private static final String HELP_TEXT_KEY = "create_group_window";
	
	private StatefullPresentationImplHandler _stateHandler = null;
	private CreateGroupEvent _createEvent;
	private String selectedGroupProviderStateId = null; 
	private Group selectedGroup = null;
	private Collection groupTypes = null;
	
	private String mainTableStyle = "main";
	
	public CreateGroupWindow() {
		_stateHandler = new StatefullPresentationImplHandler();
		_stateHandler.setPresentationStateClass(CreateGroupWindowPS.class);
		setWidth(445);//380
		setHeight(380);//320
		setResizable(true);
		setScrollbar(false);
		getLocation().setApplicationClass(CreateGroupWindow.class);
		getLocation().isInPopUpWindow(true);
	}
	
	public void initializeInMain(IWContext iwc) {
		if (iwc.isParameterSet(SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY)) {
			selectedGroupProviderStateId = iwc.getParameter(SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY);
		}      
		IWPresentationState state = this.getPresentationState(iwc);
		// add action listener
		addActionListener((IWActionListener) state);
		IWStateMachine stateMachine;
		// add all change listeners
		Collection changeListeners;
		try {
			stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
			changeListeners = stateMachine.getAllChangeListeners();
			// try to get the selected group  
			if (selectedGroupProviderStateId != null) {
				UserApplicationMenuAreaPS groupProviderState = (UserApplicationMenuAreaPS) stateMachine.getStateFor(selectedGroupProviderStateId, UserApplicationMenuAreaPS.class);
				Integer selectedGroupId = (Integer) groupProviderState.getSelectedGroupId();
				selectedGroup = getGroup(selectedGroupId); 
			}
		}
		catch (RemoteException e) {
			changeListeners = new ArrayList();
		}
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())  {
			state.addChangeListener((ChangeListener) iterator.next());
		}
		// fill collection of grouptypes stored as strings
		// used for drop down menu group type
		// used for alias group 
		groupTypes = getGroupTypes(iwc);
	}
	
	public void main(IWContext iwc) throws Exception {
		//this.debugParameters(iwc);
		//IWBundle iwb = getBundle(iwc);
		CreateGroupWindowPS _ps = (CreateGroupWindowPS) this.getPresentationState(iwc);
		
		
		if (_ps.doClose()) {
			close();
			_ps.doneClosing();
		}
		else {
			_createEvent = new CreateGroupEvent();
			//_createEvent.setSource(this.getLocation());
			_createEvent.setSource(this);
			// set controller (added by Thomas)
			String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
			id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
			_createEvent.setController(id);
			
			IWResourceBundle iwrb = getResourceBundle(iwc);
			Form form = new Form();
			form.addEventModel(_createEvent, iwc);
			
			setTitle(iwrb.getLocalizedString("create_new_group", "Create a new Group"));
			addTitle(iwrb.getLocalizedString("create_new_group", "Create a new Group"), IWConstants.BUILDER_FONT_STYLE_TITLE);
			
			add(form,iwc);
			Table mainTable = new Table();
			mainTable.setWidth(380);
			mainTable.setHeight(290);
			mainTable.setCellpadding(0);
			mainTable.setCellspacing(0);
			Table tab = new Table(2, 11); //changed from Table(2,8) - birna
			tab.setStyleClass(mainTableStyle);
			tab.setWidth(Table.HUNDRED_PERCENT);
			tab.setHeight(250);
			
			//setting alignment for all the cells in the main table:
			tab.setColumnAlignment(1, "left"); //changed from (1,"right") - birna

			tab.setCellspacing(5);
			tab.setCellpadding(0);
			
			TextInput inputName = new TextInput(_createEvent.getIONameForName());
			inputName.setAsNotEmpty(iwrb.getLocalizedString("new_group.group_name_required","Group name must be selected"));
			inputName.setStyleClass("text");
			inputName.setLength(17);
			inputName.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			
			Text inputText = new Text();
			inputText.setText(iwrb.getLocalizedString("group_name", "Group name") + ":");
			
			tab.add(inputText, 1, 1);
			tab.add(inputName, 1, 2); //changed from (inputName, 2,1) - birna
			
			TextArea descriptionTextArea = new TextArea(_createEvent.getIONameForDescription());
			descriptionTextArea.setHeight(10); //changed from (4)
			descriptionTextArea.setWidth(30);
			descriptionTextArea.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			
			Text descText = new Text(iwrb.getLocalizedString("group_description", "Description") + ":");
			tab.add(descText, 2, 1); // changed from (descText,1,2); - birna
			tab.mergeCells(2,2,2,8); //added - birna
			tab.add(descriptionTextArea, 2, 2); 
			
			GroupChooser groupChooser = getGroupChooser(_createEvent.getIONameForParentID(), true, iwc);
			groupChooser.setStyleClassName("text");
			Text createUnderText = new Text(iwrb.getLocalizedString("parent_group", "Create group under") + ":");
			
			Layer layer = new Layer();
			layer.add(createUnderText);
			layer.setNoWrap();
			tab.add(layer, 1, 3);
			tab.add(groupChooser, 1, 4); //changed from (groupChooser, 2,3) - birna
			
			StyledIBPageChooser pageChooser = new StyledIBPageChooser(_createEvent.getIONameForHomePage(), IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			pageChooser.setStyleClassName("text");
			pageChooser.setInputLength(20);
			Text pageText = new Text(iwrb.getLocalizedString("home_page", "Select homepage") + ":");

			tab.add(pageText, 1, 5); //changed from (pageText,1,4) - birna
			tab.add(pageChooser, 1, 6); //changed from (pageChooser, 2,4) - birna
			
			DropdownMenu mnu = getGroupTypeMenu(iwrb, iwc);
			/* 
			 new DropdownMenu(_createEvent.getIONameForGroupType());
			 try {
			 GroupTypeHome gtHome = (GroupTypeHome) IDOLookup.getHome(GroupType.class);
			 Collection types = gtHome.findVisibleGroupTypes();
			 Iterator iter = types.iterator();
			 while (iter.hasNext()) {
			 GroupType item = (GroupType) iter.next();
			 String value = item.getType();
			 String name = item.getType(); //item.getName();
			 mnu.addMenuElement(value, iwrb.getLocalizedString(name, name));
			 }
			 }
			 catch (RemoteException ex) {
			 throw new EJBException(ex);
			 }*/
			//    mnu.setSelectedElement(type);
			mnu.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			
			Text typeText = new Text(iwrb.getLocalizedString("select_type", "Select type") + ":");
			tab.add(typeText, 1, 7); //changed from (typeText,1,5) - birna
			tab.add(mnu, 1, 8); //changed from (mnu,2,5) - birna
			
			GroupChooser aliasGroupChooser = getGroupChooser(_createEvent.getIONameForAliasID(), false, iwc);
			aliasGroupChooser.setStyleClassName("text");
			String filter = NO_GROUP_SELECTED;
			if (selectedGroup != null)  {
				filter = selectedGroup.getPrimaryKey().toString();
			}
			aliasGroupChooser.setFilter(filter);
			Text aliasText = new Text(iwrb.getLocalizedString("alias_group", "Alias for group") + ":");
//			aliasText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			
			Layer layer2 = new Layer();
			layer2.add(aliasText);
			layer2.setNoWrap();
			tab.add(layer2, 1, 9); //changed from (layer2,1,6) - birna
			tab.add(aliasGroupChooser, 1, 10); //changed from (aliasGroupcChooser,2,3) - birna
			tab.add(Text.BREAK + Text.BREAK,1,10);
			SubmitButton button = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"), _createEvent.getIONameForCommit());
//			String message = iwrb.getLocalizedString("group_please_set_name_choose_group", "Please set name and choose a group as parent");
//			getAssociatedScript().addFunction("mandatoryCheck", "function mandatoryCheck(form) { " +
//			"\n\t if ((form."+
//			_createEvent.getIONameForParentID() +
//			".value == \"\") || (form." +
//			_createEvent.getIONameForName() +
//			".value == \"\")) { \n\t alert(\""
//			+ message +
//			"\") \n\t return false \n\t } \n\t } "); //else  \n\t { \n\t window.close() \n\t }  \n\t }" );
//			form.setOnSubmit("mandatoryCheck(this)");
			SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"), _createEvent.getIONameForCancel());
			//button.setOnClick("mandatoryCheck(this)")
			close.setOnClick("window.close();return false;");

			Help help = getHelp(HELP_TEXT_KEY);
			Table bottomTable = new Table();
			bottomTable.setCellpadding(0);
			bottomTable.setCellspacing(5);
			bottomTable.setWidth(Table.HUNDRED_PERCENT);
			bottomTable.setHeight(39);
			bottomTable.setStyleClass(mainTableStyle);
			bottomTable.add(help,1,1);
			bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
			bottomTable.add(button,2,1);
			bottomTable.add(Text.getNonBrakingSpace(),2,1);
			bottomTable.add(close,2,1);
			
			mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
			mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
			mainTable.add(tab,1,1);
			mainTable.add(bottomTable,1,3);
			form.add(mainTable);
			
		}
	}
	
	private DropdownMenu getGroupTypeMenu(IWResourceBundle iwrb, IWContext iwc)  {
		DropdownMenu menu = new DropdownMenu(_createEvent.getIONameForGroupType());
		Iterator iterator = groupTypes.iterator();
		while (iterator.hasNext())  {
			String value = (String) iterator.next();
			menu.addMenuElement(value, iwrb.getLocalizedString(value, value));
		}
		return menu;
	}
	
	
	
	/*
	 *
	 */
	private GroupChooser getGroupChooser(String name, boolean preselectSelectedGroup, IWContext iwc) {
		IWBundle iwb = getBundle(iwc);
		Image chooserImage = iwb.getImage("magnify.gif");
		GroupChooser chooser = new GroupChooser(name);
		chooser.setInputStyle(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
		chooser.setChooseButtonImage(chooserImage);
		
		try {
			//IBDomain domain = iwc.getDomain();
			if (selectedGroup != null && preselectSelectedGroup)  {
				chooser.setSelectedNode(new GroupTreeNode(selectedGroup));
			}
			else  {
				//chooser.setSelectedNode(new GroupTreeNode(domain,iwc.getApplicationContext()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return (chooser);
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	public Class getPresentationStateClass() {
		return _stateHandler.getPresentationStateClass();
	}
	
	public IWPresentationState getPresentationState(IWUserContext iwuc) {
		return _stateHandler.getPresentationState(this, iwuc);
	}
	
	public StatefullPresentationImplHandler getStateHandler() {
		return _stateHandler;
	}
	
	public Image getButtonImage(IWContext iwc) {
		IWBundle bundle = this.getBundle(iwc);
		return bundle.getImage("create_group.gif", "Create group");
	}
	
	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("create_group", "Create group");
	}
	
	public PresentationObject getPresentationObject(IWContext iwc) {
		return this;
	}
	
	private Group getGroup(Integer groupId){
		if(groupId != null){
			try {
				return (Group)IDOLookup.findByPrimaryKey(Group.class, groupId);
			}
			catch (Exception ex) {
				// FinderException and RemoteException
				throw new RuntimeException(ex.getMessage());
			}
		}
		return null;
	}   
	
	private Collection getGroupTypes(IWContext iwc)  {
		Collection groupTypes = new ArrayList();
		//TODO make sure no duplications and order alphabetically by localizedname
		// get group types
		GroupBusiness groupBusiness;
		try {
			groupBusiness =(GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
		}
		catch (RemoteException ex)  {
			throw new RuntimeException(ex.getMessage());
		}
		
		Iterator iterator = groupBusiness.getAllAllowedGroupTypesForChildren(selectedGroup, iwc).iterator();
		while (iterator.hasNext())  {
			GroupType item = (GroupType) iterator.next();
			String value = item.getType();
			if(!groupTypes.contains(value)){
				groupTypes.add(value);
			}
		}
		return groupTypes;
	}   
	
	
	
	
	
}