package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.ChangeListener;
import com.idega.builder.presentation.StyledIBPageChooser;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.app.ToolbarElement;
import com.idega.user.app.UserApplicationMenuAreaPS;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.event.CreateGroupEvent;
import com.idega.user.util.ICUserConstants;

/**
 *
 * <p>Title: CreateGroupWindow</p>
 * <p>Description: This window is used to create a group under a certain parent group. It has numerous info fields.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur Agust Saemundsson</a>,<a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
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
	private String mainTableStyle = "main";

	private CreateGroupWindowPS _ps;

	public CreateGroupWindow() {
		_stateHandler = new StatefullPresentationImplHandler();
		_stateHandler.setPresentationStateClass(CreateGroupWindowPS.class);
		setWidth(400);
		setHeight(370);
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

	}
	
	public void main(IWContext iwc) throws Exception {
		_ps = (CreateGroupWindowPS) this.getPresentationState(iwc);
		
		if (_ps.doClose()) {
			close();
			_ps.doneClosing();
		}
		else {
			_createEvent = new CreateGroupEvent();
			//_createEvent.setSource(this.getLocation());
			_createEvent.setSource(this);
			Group parentFromPS = _ps.getParentGroup();
			if( (selectedGroup==null) || (parentFromPS!=null && !selectedGroup.equals(parentFromPS)) ){
				selectedGroup = parentFromPS;
			}
			
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			Form form = new Form();
			form.addEventModel(_createEvent, iwc);
			
			setTitle(iwrb.getLocalizedString("create_new_group", "Create a new Group"));
			addTitle(iwrb.getLocalizedString("create_new_group", "Create a new Group"), TITLE_STYLECLASS);
			
			add(form,iwc);
			Table mainTable = new Table();
			mainTable.setWidth(Table.HUNDRED_PERCENT);
			mainTable.setCellpadding(0);
			mainTable.setCellspacing(0);
			mainTable.setHeight(2, 5);
			Table tab = new Table(2, 5); //changed from Table(2,8) - birna
			tab.setStyleClass(mainTableStyle);
			tab.setWidth(Table.HUNDRED_PERCENT);
			tab.setWidth(1, "50%");
			tab.setWidth(2, "50%");
			tab.setColumnAlignment(1, "left"); //changed from (1,"right") - birna
			tab.setCellspacing(12);
			tab.setCellpadding(0);
			
			TextInput inputName = new TextInput(_createEvent.getIONameForName());
			String name = _ps.getGroupName();
			if(name!=null){
				inputName.setValue(name);
			}
			
			inputName.setAsNotEmpty(iwrb.getLocalizedString("new_group.group_name_required","A group name is required"));
			inputName.setStyleClass("text");
			//inputName.setLength(20);
			inputName.setWidth(Table.HUNDRED_PERCENT);
			
			Text inputText = new Text();
			inputText.setText(iwrb.getLocalizedString("group_name", "Group name") + ":");
			
			tab.add(inputText, 1, 1);
			tab.add(Text.getBreak(), 1, 1);
			tab.add(inputName, 1, 1); //changed from (inputName, 2,1) - birna
			
			TextArea descriptionTextArea = new TextArea(_createEvent.getIONameForDescription());
			descriptionTextArea.setHeight("200"); //changed from (4)
			descriptionTextArea.setWidth(Table.HUNDRED_PERCENT);
			descriptionTextArea.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			String desc = _ps.getGroupDescription();
			if(desc!=null){
				descriptionTextArea.setValue(desc);
			}
			
			Text descText = new Text(iwrb.getLocalizedString("group_description", "Description") + ":");
			tab.add(descText, 2, 1); // changed from (descText,1,2); - birna
			tab.add(Text.getBreak(), 2, 1);
			tab.mergeCells(2, 1, 2, 5); //added - birna
			tab.setVerticalAlignment(2,1,Table.VERTICAL_ALIGN_TOP);
			tab.add(descriptionTextArea, 2, 1); 
			
			GroupChooser groupChooser = getGroupChooser(_createEvent.getIONameForParentID(), true, iwc);
			groupChooser.setStyleClassName("text");
			groupChooser.setInputLength(17);
			groupChooser.setToSubmitParentFormOnChange();
			
			Text createUnderText = new Text(iwrb.getLocalizedString("parent_group", "Create group under") + ":");
			
			tab.add(createUnderText, 1, 2);
			tab.add(Text.getBreak(), 1, 2);
			tab.add(groupChooser, 1, 2); //changed from (groupChooser, 2,3) - birna
			
			StyledIBPageChooser pageChooser = new StyledIBPageChooser(_createEvent.getIONameForHomePage(), IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			pageChooser.setStyleClassName("text");
			pageChooser.setInputLength(17);
			if(_ps.getHomePageID()>0){
				pageChooser.setSelectedPage(_ps.getHomePage());
			}
			Text pageText = new Text(iwrb.getLocalizedString("home_page", "Select homepage") + ":");

			tab.add(pageText, 1, 3); //changed from (pageText,1,4) - birna
			tab.add(Text.getBreak(), 1, 3);
			tab.add(pageChooser, 1, 3); //changed from (pageChooser, 2,4) - birna
			
			DropdownMenu mnu = getGroupTypeMenu(iwrb, iwc);
			mnu.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			
			Text typeText = new Text(iwrb.getLocalizedString("select_type", "Select type") + ":");
			tab.add(typeText, 1, 4); //changed from (typeText,1,5) - birna
			tab.add(Text.getBreak(), 1, 4);
			tab.add(mnu, 1, 4); //changed from (mnu,2,5) - birna
			
			GroupChooser aliasGroupChooser = getGroupChooser(_createEvent.getIONameForAliasID(), false, iwc);
			aliasGroupChooser.setStyleClassName("text");
			aliasGroupChooser.setInputLength(17);
			if(_ps.getAliasID()>0){
				aliasGroupChooser.setSelectedNode(new GroupTreeNode(getGroup(new Integer(_ps.getAliasID()))));
			}
			
			String filter = NO_GROUP_SELECTED;
			if (selectedGroup != null)  {
				filter = selectedGroup.getPrimaryKey().toString();
			}
			aliasGroupChooser.setFilter(filter);
			Text aliasText = new Text(iwrb.getLocalizedString("alias_group", "Alias for group") + ":");
			
			tab.add(aliasText, 1, 5); //changed from (layer2,1,6) - birna
			tab.add(Text.getBreak(), 1, 5);
			tab.add(aliasGroupChooser, 1, 5); //changed from (aliasGroupcChooser,2,3) - birna

			StyledButton button = new StyledButton(new SubmitButton(_createEvent.getIONameForCommit(), iwrb.getLocalizedString("save", "Save")));
			SubmitButton closeButton = new SubmitButton(_createEvent.getIONameForCancel(), iwrb.getLocalizedString("close", "Close"));
			closeButton.setOnClick("window.close();return false;");
			StyledButton close = new StyledButton(closeButton);

			Table buttonTable = new Table(3, 1);
			buttonTable.setCellpadding(0);
			buttonTable.setCellspacing(0);
			buttonTable.setWidth(2, 5);
			buttonTable.add(button, 1, 1);
			buttonTable.add(close, 3, 1);
			
			Help help = getHelp(HELP_TEXT_KEY);
			Table bottomTable = new Table();
			bottomTable.setCellpadding(0);
			bottomTable.setCellspacing(5);
			bottomTable.setWidth(Table.HUNDRED_PERCENT);
			bottomTable.setStyleClass(mainTableStyle);
			bottomTable.add(help, 1, 1);
			bottomTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
			bottomTable.add(buttonTable, 2, 1);
			
			mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
			mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
			mainTable.add(tab, 1, 1);
			mainTable.add(bottomTable, 1, 3);
			form.add(mainTable);
			
			//if the last creation failed
			if(_ps.hasFailedToCreateGroup()){
				this.setAlertOnLoad(iwrb.getLocalizedString("cannot.create.group.no.edit.permission","You cannot create a group under that group, you do not have edit permission for it."));
				
			}
			
			_ps.reset();
			
		}
	}
	
	private DropdownMenu getGroupTypeMenu(IWResourceBundle iwrb, IWContext iwc)  {
		DropdownMenu menu = new DropdownMenu(_createEvent.getIONameForGroupType());
		// fill collection of grouptypes stored as strings
		// used for drop down menu group type
		// used for alias group 
		Collection groupTypes = getGroupTypes(iwc);
		Iterator iterator = groupTypes.iterator();
		while (iterator.hasNext())  {
			String value = (String) iterator.next();
			menu.addMenuElement(value, iwrb.getLocalizedString(value, value));
		}
		
		String typeBefore = _ps.getGroupType();
		if(groupTypes.contains(typeBefore)){
			menu.setSelectedElement(typeBefore);
		}
		else if(groupTypes.contains(ICUserConstants.GROUP_TYPE_GENERAL)){
			menu.setSelectedElement(ICUserConstants.GROUP_TYPE_GENERAL);
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
			if ( selectedGroup != null && preselectSelectedGroup )  {
				chooser.setSelectedNode(new GroupTreeNode(selectedGroup));
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
	
	public Class getPresentationObjectClass(IWContext iwc) {
		return this.getClass();
	}
	
	public Map getParameterMap(IWContext iwc) {
		return null;
	}
	
	public boolean isValid(IWContext iwc) {
		return true;
	}
	
	public int getPriority(IWContext iwc) {
		return -1;
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
		
		// get group types
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness =(GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			
			Iterator iterator = groupBusiness.getAllAllowedGroupTypesForChildren(selectedGroup, iwc).iterator();
			while (iterator.hasNext())  {
				GroupType item = (GroupType) iterator.next();
				String value = item.getType();
				if(!groupTypes.contains(value)){
					groupTypes.add(value);
				}
			}
			
		}
		catch (RemoteException ex)  {
			throw new RuntimeException(ex.getMessage());
		}
		
		return groupTypes;
	}

	/* (non-Javadoc)
	 * @see com.idega.user.app.ToolbarElement#isButton(com.idega.presentation.IWContext)
	 */
	public boolean isButton(IWContext iwc) {
		return false;
	}   
	
	
	
	
	
}