package com.idega.user.block.search.presentation;

import java.rmi.RemoteException;

import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.app.ToolbarElement;
import com.idega.user.app.UserApplicationMainArea;
import com.idega.user.app.UserApplicationMainAreaPS;
import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.user.data.Group;

/**
 * <p>Title: idegaWeb User</p>
 * <p>Description: The standard advances search window of the IW User system</p>
 * <p>Copyright: Idega Software Copyright (c) 2002</p>
 * <p>Company: Idega Software</p>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0 
 */
public class SearchWindow extends IWAdminWindow implements ToolbarElement {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
	private SimpleSearchEvent searchEvent;
  private String userApplicationMainAreaPSId = null; 
  private Group selectedGroup = null;


	public SearchWindow() {
		setWidth(320);
		setHeight(260);
		setScrollbar(false);
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
		searchEvent = new SimpleSearchEvent();
		searchEvent.setSource(this);
		
		// set controller (added by Thomas)
	/*	String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
		id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
		searchEvent.setController(id);*/


		Form form = new Form();
		form.addEventModel(searchEvent, iwc);

		setTitle(iwrb.getLocalizedString("searchwindow.title", "Search"));
		addTitle(iwrb.getLocalizedString("searchwindow.title", "Search"), IWConstants.BUILDER_FONT_STYLE_TITLE);

		add(form);
		Table tab = new Table(2, 8);
		tab.setColumnAlignment(1, "right");
		tab.setColumnVerticalAlignment(1, "top");
		tab.setWidth(1, "130");
		tab.setCellspacing(3);
		tab.setAlignment(2, 8, "right");
		form.add(tab);
		TextInput inputName = new TextInput(searchEvent.FIELDNAME_TEXTINPUT);
		inputName.setLength(28);
		inputName.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

      
			Text inputText = new Text();
			inputText.setText(iwrb.getLocalizedString("group_name", "Group name") + ":");

			inputText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(inputText, 1, 1);
			tab.add(inputName, 2, 1);
			
/*
			TextArea descriptionTextArea = new TextArea(searchEvent.getIONameForDescription());
			descriptionTextArea.setHeight(4);
			descriptionTextArea.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

			Text descText = new Text(iwrb.getLocalizedString("group_description", "Description") + ":");
			descText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(descText, 1, 2);
			tab.add(descriptionTextArea, 2, 2); 

			GroupChooser groupChooser = getGroupChooser(searchEvent.getIONameForParentID(), iwc);
			Text createUnderText = new Text(iwrb.getLocalizedString("parent_group", "Create group under") + ":");
			createUnderText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

			Layer layer = new Layer();
			layer.add(createUnderText);
			layer.setNoWrap();
			tab.add(layer, 1, 3);
			tab.add(groupChooser, 2, 3);

			IBPageChooser pageChooser = new IBPageChooser(searchEvent.getIONameForHomePage(), IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			Text pageText = new Text(iwrb.getLocalizedString("home_page", "Select homepage") + ":");
			pageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
			tab.add(pageText, 1, 4);
			tab.add(pageChooser, 2, 4);

			DropdownMenu mnu = getGroupTypeMenu(iwrb, iwc);
			
			*/
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
		
		/*mnu.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);

		Text typeText = new Text(iwrb.getLocalizedString("select_type", "Select type") + ":");
		typeText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		tab.add(typeText, 1, 5);
		tab.add(mnu, 2, 5);

		GroupChooser aliasGroupChooser = getGroupChooser(searchEvent.getIONameForAliasID(), iwc);
		Text aliasText = new Text(iwrb.getLocalizedString("alias_group", "Alias for group") + ":");
		aliasText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);

		Layer layer2 = new Layer();
		layer2.add(aliasText);
		layer2.setNoWrap();
		tab.add(layer2, 1, 6);
		tab.add(aliasGroupChooser, 2, 6);
		*/
		SubmitButton button = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"));
		
    
   	SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close") );
    close.setOnClick("window.close();return false;");
    
		HiddenInput type = new HiddenInput(SimpleSearchEvent.FIELDNAME_SEARCHTYPE, Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER));
	
		tab.add(close, 2, 8);
		tab.add(type,2,8);
		tab.add(Text.getNonBrakingSpace(), 2, 8);
		tab.add(button, 2, 8);
		
	
		
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

	public PresentationObject getPresentationObject(IWContext iwc) {
		return this;
	}
  
}