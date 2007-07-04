package com.idega.user.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelperBusinessBean;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;

public class SimpleUserApp extends Block {
	
	public static final int USER_ORDER_BY_NAME = 0;
	public static final int USER_ORDER_BY_ID = 1;
	
	public static final String EDIT_IMAGE = "images/edit.png";
	protected static final String PARAMS_SEPARATOR = "', '";
	
	private GroupHelperBusinessBean groupsHelper = new GroupHelperBusinessBean();
	
	private Group parentGroup = null;
	private Group groupForUsersWithoutLogin = null;
	private String groupTypes = null;
	private String groupTypesForChildGroups = null;
	private String roleTypesForChildGroups = null;
	private boolean getParentGroupsFromTopNodes = true;
	
	public void main(IWContext iwc) {
		addFiles(iwc);
		
		Layer container = new Layer();
		add(container);
		
		//	Container for group users
		Layer valuesContainer = new Layer();
		valuesContainer.setStyleClass("allUsersValuesLinesStyleClass");

		//	Upper part - dropdowns and description
		Layer choosersAndDescription = new Layer();
		choosersAndDescription.setStyleClass("choosersAndDescriptionStyleClass");
		container.add(choosersAndDescription);
		
		//	Dropdowns
		Layer choosersContainer = new Layer();
		choosersAndDescription.add(choosersContainer);
		choosersContainer.setStyleClass("userApplicationChoosersContainer");
		SimpleUserPropertiesBean bean = addChooserContainer(iwc, choosersContainer, valuesContainer.getId());
		
		//	Description
		Layer descriptionContainer = new Layer();
		choosersAndDescription.add(descriptionContainer);
		descriptionContainer.setStyleClass("userApplicationDescriptionContainerStyleClass");
		descriptionContainer.add(new Text(getResourceBundle(iwc).getLocalizedString("user_application_view_users_descripton", "To view users in the groups first select the parent group and then the desired sub group. You can remove a user from a group by checking the checkboxes here down below and by clicking the \"Remove\" button. To add new users to a group click the \"Add Users\" button.")));
		
		//	Spacer
		choosersAndDescription.add(getSpacer());
		
		//	Lower part
		Layer lowerPart = new Layer();
		lowerPart.setStyleClass("userAppLowerPartStyleClass");
		container.add(lowerPart);
		
		//	Members list
		Layer membersList = new Layer();
		membersList.setStyleClass("membersListContainerStyleClass");
		lowerPart.add(membersList);
		addMembersList(iwc, bean, membersList, valuesContainer);
		
		lowerPart.add(getSpacer());
		
		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		addButtons(iwc, buttons);
		
		container.add(getSpacer());
	}
	
	private void addButtons(IWContext iwc, Layer container) {
		BackButton back = new BackButton();
		container.add(back);
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		GenericButton removeFromGroup = new GenericButton(iwrb.getLocalizedString("remove_from_group", "Remove from group"));
		StringBuffer removeAction = new StringBuffer("removeSelectedUsers('");
		removeAction.append(iwrb.getLocalizedString("removing", "Removing...")).append(PARAMS_SEPARATOR);
		removeAction.append(iwrb.getLocalizedString("are_you_sure", "Are You sure?")).append(PARAMS_SEPARATOR);
		removeAction.append(iwrb.getLocalizedString("select_users_to_remove", "Please, select user(s) to remove firstly!"));
		removeAction.append("');");
		removeFromGroup.setOnClick(removeAction.toString());
		container.add(removeFromGroup);
	
		GenericButton addUsers = new GenericButton(iwrb.getLocalizedString("add_user", "Add user"));
		container.add(addUsers);
	}
	
	private void addMembersList(IWContext iwc, SimpleUserPropertiesBean bean, Layer container, Layer valuesContainer) {
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer labels = new Layer();
		labels.setStyleClass("groupMembersListLabelsStyleClass");
		container.add(labels);
		
		Layer userNameLabelContainer = new Layer();
		userNameLabelContainer.setStyleClass("userNameLabelContainerStyleClass");
		userNameLabelContainer.add(new Text(iwrb.getLocalizedString("user.user_name", "Name")));
		labels.add(userNameLabelContainer);
		
		Layer userPersonalIdLabelContainer = new Layer();
		userPersonalIdLabelContainer.setStyleClass("userPersonalIdLabelContainerStyleClass");
		userPersonalIdLabelContainer.add(new Text(iwrb.getLocalizedString("personal_id", "Personlal ID")));
		labels.add(userPersonalIdLabelContainer);
		
		Layer changeUserLabelContainer = new Layer();
		changeUserLabelContainer.setStyleClass("changeUserLabelContainerStyleClass");
		changeUserLabelContainer.add(new Text(iwrb.getLocalizedString("change_user", "Change user")));
		labels.add(changeUserLabelContainer);
		
		Layer removeUserLabelContainer = new Layer();
		removeUserLabelContainer.setStyleClass("removeUserLabelContainerStyleClass");
		removeUserLabelContainer.add(new Text(iwrb.getLocalizedString("remove_user", "Remove user")));
		labels.add(removeUserLabelContainer);
		
		labels.add(getSpacer());
		
		container.add(valuesContainer);
		
		SimpleUserAppHelper presentationHelper = new SimpleUserAppHelper();
		String image = bundle.getVirtualPathWithFileNameString(EDIT_IMAGE);
		valuesContainer.add(presentationHelper.getMembersList(iwc, bean, groupsHelper, image));
	}
	
	private SimpleUserPropertiesBean addChooserContainer(IWContext iwc, Layer choosers, String groupUsersContainerId) {
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		DropdownMenu groupsDropdown = new DropdownMenu();
		String parentGroupsChooserId = groupsDropdown.getId();
		
		DropdownMenu childGroupsChooser = new DropdownMenu();
		String childGroupsChooserId = childGroupsChooser.getId();
		
		DropdownMenu orderByMenu = new DropdownMenu();
		String orderByChooserId = orderByMenu.getId();
		
		//	Parent group
		Layer parentGroupLabelContainer = new Layer();
		parentGroupLabelContainer.setStyleClass("parentGroupLabelContainerStyleClass");
		choosers.add(parentGroupLabelContainer);
		parentGroupLabelContainer.add(new Text(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		Layer parentGroupChooserContainer = new Layer();
		parentGroupChooserContainer.setStyleClass("parentGroupContainerStyleClass");
		choosers.add(parentGroupChooserContainer);
		Group parentGroup = fillParentGroupChooser(iwc, groupsDropdown, parentGroupChooserContainer, childGroupsChooserId, orderByChooserId, groupUsersContainerId);
		choosers.add(getSpacer());
		
		//	Child groups
		Layer childGroupsLabelContainer = new Layer();
		choosers.add(childGroupsLabelContainer);
		childGroupsLabelContainer.setStyleClass("childGroupsLabelContainerStyleClass");
		childGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		Layer childGroupChooserContainer = new Layer();
		childGroupChooserContainer.setStyleClass("childGroupChooserContainerSyleClass");
		choosers.add(childGroupChooserContainer);
		Group childGroup = fillChildGroupsChooser(iwc, childGroupChooserContainer, parentGroup, childGroupsChooser, groupUsersContainerId, parentGroupsChooserId, orderByChooserId);
		choosers.add(getSpacer());
		
		//	Order
		Layer orderByLabelContainer = new Layer();
		choosers.add(orderByLabelContainer);
		orderByLabelContainer.setStyleClass("orderByLabelContainerStyleClass");
		orderByLabelContainer.add(new Text(iwrb.getLocalizedString("order_by", "Order by")));
		Layer orderByChooser = new Layer();
		choosers.add(orderByChooser);
		orderByChooser.setStyleClass("orderByChooserStyleClass");
		
		SelectOption byName = new SelectOption(iwrb.getLocalizedString("name", "Name"), USER_ORDER_BY_NAME);
		orderByMenu.addOption(byName);
		SelectOption byId = new SelectOption(iwrb.getLocalizedString("personal_id", "Personal ID"), USER_ORDER_BY_ID);
		orderByMenu.addOption(byId);
		orderByChooser.add(orderByMenu);
		
		SimpleUserPropertiesBean bean = new SimpleUserPropertiesBean();
		if (parentGroup != null) {
			bean.setParentGroupId(getParsedId(parentGroup.getId()));
		}
		if (childGroup != null) {
			bean.setGroupId(getParsedId(childGroup.getId()));
		}
		bean.setOrderBy(USER_ORDER_BY_NAME);
		
		return bean;
	}
	
	private int getParsedId(String id) {
		if (id == null) {
			return -1;
		}
		
		try {
			return Integer.valueOf(id).intValue();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private Group fillChildGroupsChooser(IWContext iwc, Layer container, Group parent, DropdownMenu childGroups,
			String groupUsersContainerId, String parentGroupChooserId, String orderByChooserId) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		List filteredChildGroups = groupsHelper.getFilteredChildGroups(iwc, parent, groupTypesForChildGroups, roleTypesForChildGroups, ",");
		if (filteredChildGroups.size() == 0) {
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return null;
		}
		
		childGroups.addMenuElements(filteredChildGroups);
		StringBuffer onChangeChildGroupsChooserAction = new StringBuffer("selectChildGroup(this.value, '");
		onChangeChildGroupsChooserAction.append(groupUsersContainerId).append(PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(parentGroupChooserId).append(PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(orderByChooserId).append(PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(iwrb.getLocalizedString("loading", "Loading...")).append("');");
		childGroups.setOnChange(onChangeChildGroupsChooserAction.toString());
		container.add(childGroups);
		return (Group) filteredChildGroups.get(0);
	}
	
	private Group fillParentGroupChooser(IWContext iwc, DropdownMenu groupsDropdown, Layer container,
			String childGroupsChooserId, String orderByChooserId, String groupUsersContainerId) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		if (parentGroup == null) {	//	Group is not set as property
			Collection topGroups = groupsHelper.getTopGroups(iwc, iwc.getCurrentUser());
			if (!getParentGroupsFromTopNodes) {
				topGroups = groupsHelper.getTopAndParentGroups(topGroups);	//	Will get top nodes and parent groups for them
			}
			
			if (topGroups.size() > 0) {
				List filteredTopGroups = new ArrayList(groupsHelper.getFilteredGroups(topGroups, groupTypes, ","));
				if (filteredTopGroups.size() > 1) {
					groupsDropdown.addMenuElements(topGroups);
					StringBuffer action = new StringBuffer("reloadComponents('");
					action.append(iwrb.getLocalizedString("loading", "Loading...")).append(PARAMS_SEPARATOR);
					action.append(childGroupsChooserId).append(PARAMS_SEPARATOR);
					action.append(orderByChooserId).append(PARAMS_SEPARATOR);
					action.append(groupUsersContainerId).append(PARAMS_SEPARATOR);
					action.append(groupsDropdown.getId()).append("', ");
					if (groupTypesForChildGroups == null) {
						action.append("null");
					}
					else {
						action.append("'").append(groupTypesForChildGroups).append("'");
					}
					action.append(", ");
					if (roleTypesForChildGroups == null) {
						action.append("null");
					}
					else {
						action.append("'").append(roleTypesForChildGroups).append("'");
					}
					action.append(", this.value);");
					groupsDropdown.setOnChange(action.toString());
					container.add(groupsDropdown);
					return (Group) filteredTopGroups.get(0);
				}
				else {
					//	Only one group available
					Object o = filteredTopGroups.get(0);
					if (o instanceof Group) {
						Group group = (Group) o;
						addGroupNameLabel(iwrb, container, group);
						return group;
					}
					return null;
				}
			}
			//	No groups found for current user
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return null;
		}
		else {	//	Group is set as property
			addGroupNameLabel(iwrb, container, parentGroup);
			return parentGroup;
		}
	}
	
	private void addGroupNameLabel(IWResourceBundle iwrb, Layer container, Group group) {
		String groupName = group.getName() == null ? iwrb.getLocalizedString("unknown_group", "Unknown group") : group.getName();
		container.add(new Text(groupName));
	}
	
	private void addFiles(IWContext iwc) {
		IWBundle bundle = getBundle(iwc);
		AddResource adder = AddResourceFactory.getInstance(iwc);
	
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/UserApplicationEngine.js");
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/util.js");
		
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));
		
		adder.addStyleSheet(iwc, AddResource.HEADER_BEGIN, bundle.getVirtualPathWithFileNameString("style/user.css"));
	}
	
	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}
	
	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}
	
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	private Layer getSpacer() {
		Layer spacer = new Layer();
		spacer.setStyleClass("spacer");
		return spacer;
	}

	public void setGetParentGroupsFromTopNodes(boolean getParentGroupsFromTopNodes) {
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	}

	public void setGroupTypesForChildGroups(String groupTypesForChildGroups) {
		this.groupTypesForChildGroups = groupTypesForChildGroups;
	}

	public void setRoleTypesForChildGroups(String roleTypesForChildGroups) {
		this.roleTypesForChildGroups = roleTypesForChildGroups;
	}

	public void setGroupForUsersWithoutLogin(Group groupForUsersWithoutLogin) {
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
	}

	public Group getGroupForUsersWithoutLogin() {
		return groupForUsersWithoutLogin;
	}

}
