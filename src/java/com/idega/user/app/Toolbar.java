package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.idega.business.IBOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.block.search.presentation.SearchForm;
import com.idega.user.block.search.presentation.SearchWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.User;
import com.idega.user.data.UserGroupPlugIn;
import com.idega.user.event.ChangeClassEvent;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateUser;
import com.idega.user.presentation.MassMovingWindowPlugin;
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
	private String styledText = "styledText";

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
		boolean useDropdown = iwb.getBooleanProperty("use_dropdown_in_toolbar", false);

		Table toolbarTable = new Table(4, 1);
		toolbarTable.setCellpadding(0);
		toolbarTable.setCellspacing(0);
		toolbarTable.setBorder(0);
		toolbarTable.setHeight(Table.HUNDRED_PERCENT);
		toolbarTable.setStyleClass(menuTableStyle);
		toolbarTable.setWidth(Table.HUNDRED_PERCENT);
		toolbarTable.setWidth(4, Table.HUNDRED_PERCENT);
		toolbarTable.setHeight(1, Table.HUNDRED_PERCENT);
		toolbarTable.setAlignment(4, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		add(toolbarTable);

		Table toolbar1 = new Table();
		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);
		toolbar1.setBorder(0);
		toolbarTable.add(toolbar1, 1, 1);
		int toolbarColumn = 1;
		
		//User
		Image iconCrUser = iwb.getImage("new_user.gif");
		Link tLink11 = getToolbarLink(new Text(iwrb.getLocalizedString("new.member", "New member")), CreateUser.class, null);
		tLink11.setStyleClass(styledLink);
		toolbar1.setCellpaddingLeft(toolbarColumn, 1, 7);
		toolbar1.setCellpaddingRight(toolbarColumn, 1, 3);
		toolbar1.add(getToolbarLink(iconCrUser, CreateUser.class, null), toolbarColumn++, 1);
		toolbar1.setVerticalAlignment(toolbarColumn, 1, Table.VERTICAL_ALIGN_TOP);
		toolbar1.setCellpaddingTop(toolbarColumn, 1, 3);
		toolbar1.add(tLink11, toolbarColumn++, 1);

		//Group
		Image iconCrGroup = iwb.getImage("new_group.gif");
		Link tLink12 = getToolbarLink(new Text(iwrb.getLocalizedString("new.group", "New group")), CreateGroupWindow.class, null);
		tLink12.setStyleClass(styledLink);
		if (selectedGroupProviderStateId != null)
			tLink12.addParameter(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId);
		toolbar1.setCellpaddingLeft(toolbarColumn, 1, 7);
		toolbar1.setCellpaddingRight(toolbarColumn, 1, 3);
		toolbar1.add(getToolbarLink(iconCrGroup, CreateGroupWindow.class, null), toolbarColumn++, 1);
		toolbar1.setVerticalAlignment(toolbarColumn, 1, Table.VERTICAL_ALIGN_TOP);
		toolbar1.setCellpaddingTop(toolbarColumn, 1, 3);
		toolbar1.add(tLink12, toolbarColumn++, 1);

		if (iwc.isSuperAdmin()) {
			Image iconRoleMasters = iwb.getImage("key_icon.gif");
			Link tLink14 = getToolbarLink(new Text(iwrb.getLocalizedString("button.role_masters", "Role Masters")), RoleMastersWindow.class, null);
			tLink14.setStyleClass(styledLink);
			toolbar1.setCellpaddingLeft(toolbarColumn, 1, 7);
			toolbar1.setCellpaddingRight(toolbarColumn, 1, 3);
			toolbar1.add(getToolbarLink(iconRoleMasters, RoleMastersWindow.class, null), toolbarColumn++, 1);
			toolbar1.setVerticalAlignment(toolbarColumn, 1, Table.VERTICAL_ALIGN_TOP);
			toolbar1.setCellpaddingTop(toolbarColumn, 1, 3);
			toolbar1.add(tLink14, toolbarColumn++, 1);
		}

		//Search temp
		Image iconSearch = iwb.getImage("search.gif");
		Link tLink13 = getToolbarLink(new Text(iwrb.getLocalizedString("button.search", "Search")), SearchWindow.class, null);
		tLink13.setStyleClass(styledLink);
		if (userApplicationMainAreaStateId != null)
			tLink13.addParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY, userApplicationMainAreaStateId);
		toolbar1.setCellpaddingLeft(toolbarColumn, 1, 7);
		toolbar1.setCellpaddingRight(toolbarColumn, 1, 3);
		toolbar1.add(getToolbarLink(iconSearch, SearchWindow.class, null), toolbarColumn++, 1);
		toolbar1.setVerticalAlignment(toolbarColumn, 1, Table.VERTICAL_ALIGN_TOP);
		toolbar1.setCellpaddingTop(toolbarColumn, 1, 3);
		toolbar1.add(tLink13, toolbarColumn++, 1);

		// adding all plugins that implement the interface ToolbarElement
		List  toolbarElements = new ArrayList();
		User user = iwc.getCurrentUser();
		Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForUser(user);
		Iterator iter = plugins.iterator();
		while (iter.hasNext()) {
			UserGroupPlugIn element = (UserGroupPlugIn) iter.next();
			UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness) IBOLookup.getServiceInstance(iwc, Class.forName(element.getBusinessICObject().getClassName()));
			List list = pluginBiz.getMainToolbarElements();
			if (list != null) {
				toolbarElements.addAll(list);
			}
		}
		// adding some toolbar elements that belong to this bundle
		toolbarElements.add(new MassMovingWindowPlugin());
		
		boolean addPlugins = false;
		if (!toolbarElements.isEmpty()) {
			Table toolbar2 = new Table();
			toolbar2.setCellpadding(0);
			toolbar2.setCellspacing(0);
			toolbar2.setBorder(0);
			toolbarTable.add(toolbar2, 3, 1);
			toolbarColumn = 1;

			DropdownMenu menu  = new DropdownMenu("other_choices");
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
					addPlugins = true;
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
						Image toolImage = toolbarElement.getButtonImage(finalIwc);
						toolbar2.setCellpaddingLeft(toolbarColumn, 1, 7);
						if (toolImage != null) {
							toolbar2.setCellpaddingRight(toolbarColumn, 1, 3);
							toolbar2.add(getToolbarLink(toolImage, toolPresentationClass, parameterMap), toolbarColumn++, 1);
						}
						Link toolLink = getToolbarLink(new Text(toolName), toolPresentationClass, parameterMap);
						toolLink.setStyleClass(styledLink);
						
						toolbar2.setVerticalAlignment(toolbarColumn, 1, Table.VERTICAL_ALIGN_TOP);
						toolbar2.setCellpaddingTop(toolbarColumn, 1, 3);
						toolbar2.add(toolLink, toolbarColumn++, 1);
					}
				}
			}		

			if (addPlugins) {
				Image dottedImage = iwb.getImage("dotted.gif");
				toolbarTable.setCellpaddingLeft(2, 1, 10);
				toolbarTable.setCellpaddingRight(2, 1, 3);
				toolbarTable.add(dottedImage, 2, 1);
				
				if (useDropdown) {
					Form form = new Form();
					menu.addMenuElementFirst("", "");
					form.add(menu);
	
					Image iconOtherChanges = iwb.getImage("other_choises.gif");
					Text menuText =  new Text(iwrb.getLocalizedString("button.other_choices", "Other choices"));
					menuText.setStyleClass(styledText);
					toolbar2.setCellpaddingLeft(toolbarColumn, 1, 7);
					toolbar2.setCellpaddingRight(toolbarColumn, 1, 3);
					toolbar2.add(iconOtherChanges, toolbarColumn++, 1);
					toolbar2.add(menuText, toolbarColumn++, 1);
					toolbar2.setCellpaddingLeft(toolbarColumn, 1, 7);
					toolbar2.add(form, toolbarColumn++, 1);
				}
			}
		}
		
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
		toolbarTable.setCellpaddingRight(4, 1, 6);
		toolbarTable.add(searchForm, 4, 1);
	}
	
	protected Link getToolbarLink(PresentationObject obj, Class windowClass, Map parameters) {
		Link link = new Link(obj);
		link.setWindowToOpen(windowClass);
		if (parameters != null) {
			link.setParameter(parameters);
		}
		
		return link;
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

	public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException {
		return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}
}