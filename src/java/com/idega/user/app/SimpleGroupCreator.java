package com.idega.user.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ejb.FinderException;

import com.idega.builder.data.IBPageName;
import com.idega.builder.data.IBPageNameHome;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.GroupTypeBMPBean;
import com.idega.user.data.GroupTypeHome;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class SimpleGroupCreator extends Block {

	private IWBundle bundle = null;
	private IWResourceBundle iwrb = null;
	
	private String name = null;
	private String homePageName = null;
	private String homePageId = null;
	private String type = null;
	private String description = null;
	private String parentGroupId = null;
	private String editedGroupId = null;
	private List<String> groupTypes = null;
	private List<String> roleTypes = null;
	
	private String sep = "', '";
	private String groupTab = "groupTab";
	private String rolesTab = "rolesTab";
	private String mootabsPanel = "mootabs_panel";
	
	private boolean editingMode = false;
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	@SuppressWarnings("unchecked")
	private void initializeLocalVariables(IWContext iwc) {
		bundle = getBundle(iwc);
		iwrb = bundle.getResourceBundle(iwc);
		
		parentGroupId = iwc.getParameter(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION);
		editedGroupId = iwc.getParameter(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION);
		String selectedGroupTypes = iwc.getParameter(UserConstants.AVAILABLE_GROUP_TYPES_IN_SIMPLE_USER_APPLICATION);
		if (!StringUtil.isEmpty(selectedGroupTypes)) {
			selectedGroupTypes = StringHandler.replace(selectedGroupTypes, CoreConstants.SPACE, CoreConstants.EMPTY);
			groupTypes = Arrays.asList(selectedGroupTypes.split(CoreConstants.COMMA));
		}
		String selectedRoleTypes = iwc.getParameter(UserConstants.AVAILABLE_ROLE_TYPES_IN_SIMPLE_USER_APPLICATION);
		if (!StringUtil.isEmpty(selectedRoleTypes)) {
			selectedRoleTypes = StringHandler.replace(selectedRoleTypes, CoreConstants.SPACE, CoreConstants.EMPTY);
			roleTypes = Arrays.asList(selectedRoleTypes.split(CoreConstants.COMMA));
		}
		
		Group group = null;
		if (editedGroupId != null) {
			try {
				group = ((GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class)).getGroupByGroupID(Integer.valueOf(editedGroupId));
			} catch(Exception e) {}
		}
		
		if (group != null) {
			editingMode = true;
			name = group.getName();
			
			try {
				ICPage page = group.getHomePage();
				if (page != null) {
					homePageId = page.getId();
					
					Locale locale = iwc.getCurrentLocale();
					IBPageNameHome pageNameHome = (IBPageNameHome) IDOLookup.getHome(IBPageName.class);
					int localeId = ICLocaleBusiness.getLocaleId(locale);
					Collection<IBPageName> pageNames = pageNameHome.findAllByPageIdAndLocaleId(Integer.valueOf(homePageId), localeId);
					if (pageNames != null && !pageNames.isEmpty()) {
						List<IBPageName> names = new ArrayList<IBPageName>(pageNames);
						homePageName = names.get(0).getPageName();
					}
					
					if (homePageName == null) {
						homePageName = page.getNodeName(locale);
					}
				}
			} catch(Exception e) {}
			
			type = group.getGroupType();
			
			description = group.getDescription();
		}
	}
	
	@Override
	public void main(IWContext iwc) throws IOException {
		initializeLocalVariables(iwc);
		
		Layer container = new Layer();
		add(container);
		String mainId = container.getId();
		
		Lists titlesForTabs = new Lists();
		container.add(titlesForTabs);
		titlesForTabs.setStyleClass("mootabs_title");
		ListItem groupTab = new ListItem();
		titlesForTabs.add(groupTab);
		groupTab.addText(iwrb.getLocalizedString("group", "Group"));
		groupTab.setMarkupAttribute("title", this.groupTab);
		
		Layer groupTabContent = new Layer();
		groupTabContent.setStyleClass(mootabsPanel);
		container.add(groupTabContent);
		groupTabContent.setId(this.groupTab);
		
		String styleName = "webfaceFormItem";
		
		//	Name
		Layer nameContainer = new Layer();
		groupTabContent.add(nameContainer);
		nameContainer.setStyleClass(styleName);
		TextInput nameInput = new TextInput("name", StringUtil.isEmpty(name) ? CoreConstants.EMPTY : name);
		if (editingMode) {
			nameInput.setDisabled(editingMode);
		}
		Label nameLabel = new Label(iwrb.getLocalizedString("group_name", "Group name"), nameInput);
		nameContainer.add(nameLabel);
		nameContainer.add(nameInput);
		
		//	Home page
		Layer homePageContainer = new Layer();
		groupTabContent.add(homePageContainer);
		homePageContainer.setStyleClass(styleName);
		TextInput homePageInput = new TextInput("homePage", homePageName == null ? CoreConstants.EMPTY : homePageName);
		HiddenInput homePageSelection = new HiddenInput("homePage", homePageId == null ? "-1" : homePageId);
		homePageContainer.add(homePageSelection);
		String homePageInputId = homePageSelection.getId();
		StringBuilder action = new StringBuilder("findAvailablePages('").append(homePageInput.getId()).append(sep).append(homePageInputId).append(sep);
		action.append(iwrb.getLocalizedString("no_pages_found_by_name", "No pages found by name")).append("');");
		homePageInput.setOnKeyUp(action.toString());
		homePageInput.setToolTip(iwrb.getLocalizedString("enter_page_name", "Enter page name"));
		Label homePageLabel = new Label(iwrb.getLocalizedString("homepage", "Home page"), homePageInput);
		homePageContainer.add(homePageLabel);
		homePageContainer.add(homePageInput);
		
		//	Group types
		Layer typesContainer = new Layer();
		groupTabContent.add(typesContainer);
		typesContainer.setStyleClass(styleName);
		DropdownMenu groupTypes = new DropdownMenu();	
		fillWithGroupTypes(groupTypes, iwc.getCurrentLocale());
		groupTypes.setSelectedElement(type == null ? GroupTypeBMPBean.TYPE_PERMISSION_GROUP : type);
		Label typesLabel = new Label(iwrb.getLocalizedString("group_type", "Group type"), groupTypes);
		typesContainer.add(typesLabel);
		typesContainer.add(groupTypes);
		
		//	Description
		Layer descriptionContainer = new Layer();
		groupTabContent.add(descriptionContainer);
		descriptionContainer.setStyleClass(styleName);
		TextArea descriptionArea = new TextArea("description", description == null ? CoreConstants.EMPTY : description);
		Label descriptionLabel = new Label(iwrb.getLocalizedString("group_description", "Description"), descriptionArea);
		descriptionContainer.add(descriptionLabel);
		descriptionContainer.add(descriptionArea);
		
		//	Hidden inputs
		HiddenInput groupInput = new HiddenInput("groupId", editedGroupId == null ? "-1" : editedGroupId);
		groupTabContent.add(groupInput);
		HiddenInput parentGroupInput = new HiddenInput("parentGroupId", parentGroupId == null ? "-1" : parentGroupId);
		groupTabContent.add(parentGroupInput);
		
		//	Roles
		ListItem rolesTab = new ListItem();
		titlesForTabs.add(rolesTab);
		rolesTab.setMarkupAttribute("title", this.rolesTab);
		rolesTab.addText(iwrb.getLocalizedString("roles", "Roles"));
		
		Layer rolesTabContent = new Layer();
		container.add(rolesTabContent);
		rolesTabContent.setId(this.rolesTab);
		rolesTabContent.setStyleClass(mootabsPanel);
		rolesTabContent.add(ELUtil.getInstance().getBean(UserApplicationEngine.class).getRolesEditor(iwc, editedGroupId == null ? -1 : 
			Integer.valueOf(editedGroupId), true, roleTypes));
		
		//	Save button
		Layer buttonsContainer = new Layer();
		groupTabContent.add(buttonsContainer);
		buttonsContainer.setStyleClass("webfaceButtonLayer");
		GenericButton saveButton = new GenericButton(iwrb.getLocalizedString("save", "Save"));
		StringBuilder idsExpression = new StringBuilder("'").append(nameInput.getId()).append(sep).append(homePageInputId).append(sep).append(groupTypes.getId());
		idsExpression.append(sep).append(descriptionArea.getId()).append(sep).append(groupInput.getId()).append(sep).append(parentGroupInput.getId()).append(sep);
		idsExpression.append(mainId).append(sep).append(iwrb.getLocalizedString("saving", "Saving...")).append("'");
		GroupHelper groupHelper = ELUtil.getInstance().getBean(GroupHelper.class);
		String selectedRolesParam = groupHelper.getJavaScriptFunctionParameter(roleTypes);
		action = new StringBuilder("saveGroupInSimpleUserApplication([").append(idsExpression.toString()).append("], ").append(selectedRolesParam).append(");");
		saveButton.setOnClick(action.toString());
		buttonsContainer.add(saveButton);
		
		//	JS
		Layer script = new Layer();
		script.add(PresentationUtil.getJavaScriptAction(new StringBuffer("createTabsWithMootabs('").append(mainId).append("');").toString()));
		container.add(script);
	}
	
	@SuppressWarnings("unchecked")
	private void fillWithGroupTypes(DropdownMenu menu, Locale locale) {
		Collection<GroupType> allTypes = null;
		try {
			allTypes = ((GroupTypeHome) IDOLookup.getHome(GroupType.class)).findVisibleGroupTypes();
		} catch (IDOLookupException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(allTypes)) {
			return;
		}
		if (ListUtil.isEmpty(groupTypes)) {
			menu.addMenuElements(allTypes);
			return;
		}
		
		String typeKey = null;
		for (GroupType type: allTypes) {
			typeKey = type.getType();
			if (groupTypes.contains(typeKey)) {
				menu.addOption(new SelectOption(type.getNodeName(locale), typeKey));
			}
		}
	}
	
}
