package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.ChangeListener;

//import com.idega.builder.data.IBDomain;
import com.idega.builder.presentation.IBPageChooser;
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
import com.idega.idegaweb.presentation.IWAdminWindow;
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
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0 
 */
public class CreateGroupWindow extends IWAdminWindow implements StatefullPresentation, ToolbarElement {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

  public static final String SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY = "selected_group_pp_id_key";
  
	private StatefullPresentationImplHandler _stateHandler = null;
	private CreateGroupEvent _createEvent;
  private String selectedGroupProviderStateId = null; 
  private Group selectedGroup = null;


	public CreateGroupWindow() {
		_stateHandler = new StatefullPresentationImplHandler();
		_stateHandler.setPresentationStateClass(CreateGroupWindowPS.class);
		setWidth(320);
		setHeight(260);
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
		//this.debugParameters(iwc);
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

			add(form);
			Table tab = new Table(2, 8);
			tab.setColumnAlignment(1, "right");
			tab.setColumnVerticalAlignment(1, "top");
			tab.setWidth(1, "130");
			tab.setCellspacing(3);
			tab.setAlignment(2, 8, "right");
			form.add(tab);
			TextInput inputName = new TextInput(_createEvent.getIONameForName());
			inputName.setLength(28);
			inputName.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
      
			Text inputText = new Text();
			inputText.setText(iwrb.getLocalizedString("group_name", "Group name") + ":");

			inputText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(inputText, 1, 1);
			tab.add(inputName, 2, 1);

			TextArea descriptionTextArea = new TextArea(_createEvent.getIONameForDescription());
			descriptionTextArea.setHeight(4);
			descriptionTextArea.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

			Text descText = new Text(iwrb.getLocalizedString("group_description", "Description") + ":");
			descText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(descText, 1, 2);
			tab.add(descriptionTextArea, 2, 2); 

			GroupChooser groupChooser = getGroupChooser(_createEvent.getIONameForParentID(), iwc);
			Text createUnderText = new Text(iwrb.getLocalizedString("parent_group", "Create group under") + ":");
			createUnderText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

			Layer layer = new Layer();
			layer.add(createUnderText);
			layer.setNoWrap();
			tab.add(layer, 1, 3);
			tab.add(groupChooser, 2, 3);

			IBPageChooser pageChooser = new IBPageChooser(_createEvent.getIONameForHomePage(), IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			Text pageText = new Text(iwrb.getLocalizedString("home_page", "Select homepage") + ":");
			pageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(pageText, 1, 4);
			tab.add(pageChooser, 2, 4);

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
			typeText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(typeText, 1, 5);
			tab.add(mnu, 2, 5);

			GroupChooser aliasGroupChooser = getGroupChooser(_createEvent.getIONameForAliasID(), iwc);
			Text aliasText = new Text(iwrb.getLocalizedString("alias_group", "Alias for group") + ":");
			aliasText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

			Layer layer2 = new Layer();
			layer2.add(aliasText);
			layer2.setNoWrap();
			tab.add(layer2, 1, 6);
			tab.add(aliasGroupChooser, 2, 6);
 			SubmitButton button = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"), _createEvent.getIONameForCommit());
      String message = iwrb.getLocalizedString("group_please_set_name_choose_group", "Please set name and choose a group as parent");
      getAssociatedScript().addFunction("mandatoryCheck", "function mandatoryCheck(form) { " +
          "\n\t if ((form."+
          _createEvent.getIONameForParentID() +
          ".value == \"\") || (form." +
          _createEvent.getIONameForName() +
          ".value == \"\")) { \n\t alert(\""
          + message +
          "\") \n\t return false \n\t } \n\t else \n\t { \n\t window.close() \n\t return true \n\t } \n\t }");
      form.setOnSubmit("return mandatoryCheck(this)");
      SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"), _createEvent.getIONameForCancel());
      close.setOnClick("window.close();return false;");
			tab.add(close, 2, 8);
			tab.add(Text.getNonBrakingSpace(), 2, 8);
			tab.add(button, 2, 8);
		}
	}
  
  private DropdownMenu getGroupTypeMenu(IWResourceBundle iwrb, IWContext iwc)  {
    DropdownMenu menu = new DropdownMenu(_createEvent.getIONameForGroupType());
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
      menu.addMenuElement(value, iwrb.getLocalizedString(value, value));
    }
    return menu;
  }
    


	/*
	 *
	 */
	private GroupChooser getGroupChooser(String name, IWContext iwc) {
		GroupChooser chooser = new GroupChooser(name);
		chooser.setInputStyle(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

		try {
			//IBDomain domain = iwc.getDomain();
      if (selectedGroup != null)  {
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
}