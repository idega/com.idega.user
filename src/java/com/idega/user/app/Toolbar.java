package com.idega.user.app;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectOption;
import com.idega.repository.data.ImplementorRepository;
import com.idega.user.block.search.presentation.SearchForm;
import com.idega.user.block.search.presentation.SearchWindow;
import com.idega.user.event.ChangeClassEvent;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateUser;
import com.idega.user.presentation.RoleMastersWindow;

/**
 * <p>
 * Title: idegaWeb
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: idega Software
 * </p>
 * 
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson </a>
 * 
 * @version 1.0
 */

public class Toolbar extends Page implements IWBrowserView {
	
	public static final String SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY = "selected_group_mm_id_key";

	protected String title;

	protected IWBundle iwb;

	protected IWResourceBundle iwrb;

	protected String _controlTarget = null;

	protected IWPresentationEvent _controlEvent = null;

	private SearchForm searchForm = new SearchForm();

	private String selectedGroupProviderStateId = null;

	private String userApplicationMainAreaStateId = null;

	private String menuTableStyle = "menu";

	private String styledLink = "styledLink";

	public Toolbar() {
		// default constructor
	}

	public void setSelectedGroupProviderStateId(String selectedGroupProviderStateId) {
		this.selectedGroupProviderStateId = selectedGroupProviderStateId;
	}

	public String getBundleIdentifier() {
		return "com.idega.user";
	}

	public void setControlEventModel(IWPresentationEvent model) {
		_controlEvent = model;
		searchForm.setControlEventModel(model);
	}

	public void setControlTarget(String controlTarget) {
		_controlTarget = controlTarget;
		searchForm.setControlTarget(controlTarget);

	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);


		//	added for stylesheet writout:
		//			parentPage = this.getParentPage();
		//			styleSrc = iwb.getVirtualPathWithFileNameString(styleScript);
		//			parentPage.addStyleSheetURL(styleSrc);

		Table toolbarTable = new Table(3, 3);
		boolean useDropdown = iwb.getBooleanProperty("use_dropdown_in_toolbar", false);

		//	added for isi style
		//		toolbarTable.setStyleClass(menuTableStyle); --- changed

		toolbarTable.setCellpadding(0);
		toolbarTable.setCellspacing(0);
		toolbarTable.setStyleClass(menuTableStyle);
		toolbarTable.setWidth(Table.HUNDRED_PERCENT);
		toolbarTable.setHeight(2, Table.HUNDRED_PERCENT);

		toolbarTable.setAlignment(2, 2, Table.HORIZONTAL_ALIGN_RIGHT);

		add(toolbarTable);

		Table toolbar1 = new Table(11, 1);

		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);

		//User
		Table button = new Table(2, 1);
		button.setCellpadding(0);
		Image iconCrUser = iwb.getImage("new_user.gif");
		button.setCellpaddingLeft(1, 1, 7);
		button.add(iconCrUser, 1, 1);
		Text text = new Text(iwrb.getLocalizedString("new.member", "New member"));
		Link tLink11 = new Link(text);
		tLink11.setStyleClass(styledLink);
		tLink11.setWindowToOpen(CreateUser.class);
		button.setWidth(2, 10);
		button.setCellpaddingTop(2, 1, 2);
		button.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button.add(tLink11, 2, 1);
		toolbar1.add(button, 2, 1);

		//Group
		Table button2 = new Table(2, 1);
		button2.setCellpadding(0);
		Image iconCrGroup = iwb.getImage("new_group.gif");
		button2.add(iconCrGroup, 1, 1);
		Text text2 = new Text(iwrb.getLocalizedString("new.group", "New group"));
		//    text2.setFontFace(Text.FONT_FACE_VERDANA);
		//    text2.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link tLink12 = new Link(text2);
		tLink12.setStyleClass(styledLink);
		tLink12.setWindowToOpen(CreateGroupWindow.class);
		if (selectedGroupProviderStateId != null)
			tLink12.addParameter(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId);
		button2.setWidth(2, 10);
		button2.setCellpaddingTop(2, 1, 2);
		button2.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button2.add(tLink12, 2, 1);
		toolbar1.add(button2, 3, 1);

		if (iwc.isSuperAdmin()) {
			Table button4 = new Table(2, 1);
			button4.setCellpadding(0);
			Image iconRoleMasters = iwb.getImage("other_choises.gif");
			button4.add(iconRoleMasters, 1, 1);
			Text text4 = new Text(iwrb.getLocalizedString("button.role_masters", "Role Masters"));
			Link tLink14 = new Link(text4);
			tLink14.setStyleClass(styledLink);
			tLink14.setWindowToOpen(RoleMastersWindow.class);
			button4.setWidth(2, 10);
			button4.setCellpaddingTop(2, 1, 2);
			button4.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
			button4.add(tLink14, 2, 1);
			toolbar1.add(button4, 4, 1);
		}

		//Search temp
		Table button3 = new Table(3, 1);
		button3.setCellpadding(0);
		Image iconSearch = iwb.getImage("search.gif");
		button3.add(iconSearch, 1, 1);
		Text text3 = new Text(iwrb.getLocalizedString("button.search", "Search"));
		//		text3.setFontFace(Text.FONT_FACE_VERDANA);
		//		text3.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link tLink13 = new Link(text3);
		tLink13.setStyleClass(styledLink);
		if (userApplicationMainAreaStateId != null)
			tLink13.addParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY, userApplicationMainAreaStateId);
		tLink13.setWindowToOpen(SearchWindow.class);
		button3.setCellpaddingTop(2, 1, 2);
		button3.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button3.add(tLink13, 2, 1);
		button3.setWidth(2, 10);
		button3.setWidth(3, 20);
		button3.add(Text.NON_BREAKING_SPACE, 3, 1);
		Image dottedImage = iwb.getImage("dotted.gif");
		button3.setAlignment(3, 1, "right");
		button3.add(dottedImage, 3, 1);
		toolbar1.add(button3, 5, 1);


		DropdownMenu menu  = null;
		if (useDropdown) {
			Form form = new Form();
			menu = new DropdownMenu("other_choices");
			menu.addMenuElement("", "");
			form.add(menu);
			Table menuButton = new Table(2, 1);
			menuButton.setCellpadding(0);
			Image iconOtherChanges = iwb.getImage("other_choises.gif");
			menuButton.add(iconOtherChanges, 1, 1);
			Text menuText =  new Text(iwrb.getLocalizedString("button.other_choices", "Other choices"));
			menuText.setStyleClass(styledLink);
			menuButton.setWidth(2, 10);
			menuButton.setCellpaddingTop(2, 1, 2);
			menuButton.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
			menuButton.add(menuText, 2, 1);
			toolbar1.add(menuButton, 6, 1);
			toolbar1.setCellpaddingLeft(7, 1, 10);
			toolbar1.add(form, 7, 1);
		}
		//finance
		// toolbar1.add(
		// this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("finance","Finance"),
		// iwb.getImage("finance.gif"),
		// com.idega.block.finance.presentation.AccountViewer.class),4,1);

		// adding all plugins that implement the interface ToolbarElement
		int column = 6;
		List  toolbarElements = ImplementorRepository.getInstance().newInstances(ToolbarElement.class, this.getClass());
		final IWContext finalIwc = iwc;
		Comparator priorityComparator = new Comparator() {
			
			public int compare(Object toolbarElementA, Object toolbarElementB) {
				int priorityA = ((ToolbarElement) toolbarElementA).getPriority(finalIwc);
				int priorityB = ((ToolbarElement) toolbarElementB).getPriority(finalIwc);
				if (priorityA == -1  && priorityB == -1) {
					return 0;
				}
				else if (priorityA == -1) {
					return 1;
				}
				else if (priorityB ==  -1) {
					return -1;
				}
				return priorityA - priorityB;
			}
		};
		Collections.sort(toolbarElements, priorityComparator);
		Iterator toolbarElementsIterator = toolbarElements.iterator();
		while (toolbarElementsIterator.hasNext()) {
			ToolbarElement toolbarElement = (ToolbarElement) toolbarElementsIterator.next();
			if (toolbarElement.isValid(iwc)) {
				Class toolPresentationClass = toolbarElement.getPresentationObjectClass(iwc);
				Map parameterMap = toolbarElement.getParameterMap(iwc);
				// a special parameter, very few plugins are using it
				if (selectedGroupProviderStateId != null) {
					if (parameterMap == null) {
						parameterMap = new HashMap();
					}
					parameterMap.put(SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId );
				}
				String toolName = toolbarElement.getName(iwc);
				if (useDropdown && (! toolbarElement.isButton(iwc))) { 
					SelectOption toolOption = new SelectOption(toolName, "1");
					toolOption.setWindowToOpenOnSelect(toolPresentationClass, parameterMap);
					menu.addOption(toolOption);
				}
				else {
					Text toolText = new Text(toolName);
					Link toolLink = new Link(toolText);
					toolLink.setStyleClass(styledLink);
					toolLink.setParameter(parameterMap);
					toolLink.setWindowToOpen(toolPresentationClass);
					Table toolButton = new Table(2,1);
					toolButton.setCellpadding(0);
					toolButton.add(toolLink,2,1);
					toolbar1.add(toolButton, column++, 1);
				}
			}
		}		
		//To do - stickies
		//    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("To do",
		// iwb.getImage("todo.gif"),
		// com.idega.block.news.presentation.News.class),7,1);

		//settings
		//  toolbar1.add(
		// this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("settings","Settings"),
		// iwb.getImage("settings.gif"),com.idega.block.news.presentation.News.class
		// ),4,1);

		//view
		//dropdownmenu
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Yfirlit",
		// iwb.getImage("views.gif"),
		// com.idega.block.news.presentation.News.class),7,1);

		//search
		Table button9 = new Table(2, 1);
		Text text9 = new Text(iwrb.getLocalizedString("fast_search", "Fast search"));
		button9.add(text9, 1, 1);
		button9.setHorizontalAlignment("right");
		IWLocation location = (IWLocation) this.getLocation().clone();
		location.setSubID(1);
		searchForm.setLocation(location, iwc);
		searchForm.setArtificialCompoundId(getCompoundId(), iwc);
		searchForm.setHorizontalAlignment("right");
		searchForm.setTextInputValue(iwrb.getLocalizedString("insert_search_string", "Insert a search string"));

		toolbarTable.setAlignment(2, 2, "right");
		toolbarTable.setVerticalAlignment(2, 2, "top");
		toolbarTable.add(searchForm, 3, 2);

		toolbarTable.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_LEFT);
		toolbarTable.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		toolbarTable.add(toolbar1, 1, 2);

	}

	protected Table getToolbarButtonWithChangeClassEvent(String textOnButton, Image icon, Class changeClass) {
		Table button = new Table(2, 1);
		button.setCellpadding(0);
		Text text = new Text(textOnButton);
		text.setFontFace(Text.FONT_FACE_VERDANA);
		text.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link eventLink = new Link(text);
		button.add(icon, 1, 1);
		button.add(eventLink, 2, 1);
		eventLink.addEventModel(new ChangeClassEvent(changeClass));
		if (_controlEvent != null) {
			eventLink.addEventModel(_controlEvent);
		}
		if (_controlTarget != null) {
			eventLink.setTarget(_controlTarget);
		}

		return button;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param string
	 */
	public void setUserApplicationMainAreaStateId(String string) {
		userApplicationMainAreaStateId = string;
	}

}