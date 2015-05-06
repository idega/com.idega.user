package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupComparator;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupMembersListViewer;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class SimpleUserAppViewUsers extends Block {

	private Group parentGroup = null;
	private Group groupForUsersWithoutLogin = null;

	private Integer selectedParentGroupId = null;

	private boolean checkPagerProperties = false;

	private SimpleUserPropertiesBean properties = null;

	@Autowired
	private GroupHelper groupsHelper;
	@Autowired
	private SimpleUserAppHelper helper;

	public SimpleUserAppViewUsers(SimpleUserPropertiesBean properties, Group parentGroup, Group groupForUsersWithoutLogin) {
		this.properties = properties;

		this.parentGroup = parentGroup;
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
	}

	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);

		Layer container = new Layer();
		add(container);

		//	Container for group users
		Layer valuesContainer = new Layer();
		valuesContainer.setStyleClass("allUsersValuesLinesStyleClass");

		//	Dropdowns
		DropdownMenu groupsDropdown = new DropdownMenu();
		String parentGroupsChooserId = groupsDropdown.getId();

		DropdownMenu childGroupsChooser = new DropdownMenu();
		String childGroupsChooserId = childGroupsChooser.getId();

		DropdownMenu orderByChooser = new DropdownMenu();
		String orderByChooserId = orderByChooser.getId();

		DropdownMenu[] dropDowns = new DropdownMenu[3];
		dropDowns[0] = groupsDropdown;
		dropDowns[1] = childGroupsChooser;
		dropDowns[2] = orderByChooser;

		String[] ids = new String[5];
		ids[0] = valuesContainer.getId();
		ids[1] = parentGroupsChooserId;
		ids[2] = childGroupsChooserId;
		ids[3] = orderByChooserId;
		ids[4] = properties.getContainerId();

		//	Upper part - dropdowns and description
		Layer choosersAndDescription = new Layer();
		choosersAndDescription.setStyleClass("choosersAndDescriptionStyleClass");
		container.add(choosersAndDescription);

		//	Dropdowns
		Layer choosersContainer = new Layer();
		choosersAndDescription.add(choosersContainer);
		choosersContainer.setStyleClass("userApplicationChoosersContainer");
		addChooserContainer(iwc, choosersContainer, dropDowns, ids);

		//	Description
		Layer descriptionContainer = new Layer();
		choosersAndDescription.add(descriptionContainer);
		descriptionContainer.setStyleClass("userApplicationDescriptionContainerStyleClass");
		descriptionContainer.add(new Text(getResourceBundle(iwc).getLocalizedString("user_application_view_users_descripton", "To view users in the groups first select the parent group and then the desired sub group. You can remove a user from a group by checking the checkboxes here down below and by clicking the \"Remove\" button. To add new users to a group click the \"Add User\" button.")));

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
		addMembersList(iwc, membersList, valuesContainer, properties.getContainerId());

		lowerPart.add(getSpacer());

		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		addButtons(iwc, buttons, ids);

		container.add(getSpacer());
	}

	private void addButtons(IWContext iwc, Layer container, String[] ids) {
		BackButton back = new BackButton();
		container.add(back);

		IWResourceBundle iwrb = getResourceBundle(iwc);

		GenericButton removeFromGroup = new GenericButton(iwrb.getLocalizedString("remove_from_group", "Remove from group"));
		StringBuffer removeAction = new StringBuffer("removeSelectedUsers('");
		removeAction.append(iwrb.getLocalizedString("removing", "Removing...")).append(SimpleUserApp.PARAMS_SEPARATOR);
		removeAction.append(iwrb.getLocalizedString("are_you_sure", "Are you sure?")).append(SimpleUserApp.PARAMS_SEPARATOR);
		removeAction.append(iwrb.getLocalizedString("select_users_to_remove", "Please, select user(s) to remove firstly!"));
		removeAction.append("');");
		removeFromGroup.setOnClick(removeAction.toString());
		container.add(removeFromGroup);

		GenericButton addUser = new GenericButton(iwrb.getLocalizedString("add_user", "Add user"));
		String id = getGroupForUsersWithoutLogin() == null ? null : getGroupForUsersWithoutLogin().getId();
		properties.setParentGroupChooserId(ids[1]);
		properties.setGroupChooserId(ids[2]);
		properties.setMessage(iwrb.getLocalizedString("loading", "Loading..."));
		properties.setDefaultGroupId(id);

		addUser.setOnClick(helper.getActionForAddUserView(properties, null));
		container.add(addUser);
	}

	private void addMembersList(IWContext iwc, Layer container, Layer valuesContainer, String mainContainerId) {
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

		Layer userEmailLabelContainer = new Layer();
		userEmailLabelContainer.setStyleClass("userEmailLabelContainerStyleClass");
		userEmailLabelContainer.add(new Text(iwrb.getLocalizedString("email", "E-mail")));
		labels.add(userEmailLabelContainer);

		Layer userPhoneLabelContainer = new Layer();
		userPhoneLabelContainer.setStyleClass("userPhoneLabelContainerStyleClass");
		userPhoneLabelContainer.add(new Text(iwrb.getLocalizedString("phone_number", "Phone number")));
		labels.add(userPhoneLabelContainer);

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

		String image = bundle.getVirtualPathWithFileNameString(SimpleUserApp.EDIT_IMAGE);
		GroupMembersListViewer list = new GroupMembersListViewer();
		if (isCheckPagerProperties()) {
			List<Integer> pagerProperties = null;
			UserApplicationEngine userAppEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
			pagerProperties = userAppEngine.getPagerProperties(userAppEngine.getIdForPagerProperties(properties));
			if (!ListUtil.isEmpty(pagerProperties)) {
				list.setLeftIndex(pagerProperties.get(0));
				list.setRightIndex(pagerProperties.get(1) + 1);
				list.setCount(pagerProperties.get(2));
			}
		}
		list.setContainerId(valuesContainer.getId());
		list.setBean(properties);
		list.setImage(image);
		valuesContainer.add(list);
	}

	private void addGroupButtons(IWContext iwc, String chooserId, String parentGroupChooserId, Layer container, int groupsType, boolean addGroupCreateButton,
			boolean addGroupEditButton) {
		if (!addGroupCreateButton && !addGroupEditButton) {
			return;
		}

		if (addGroupCreateButton || addGroupEditButton) {
			Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
			List<String> jsSources = new ArrayList<String>();
			List<String> css = new ArrayList<String>();
			try {
				jsSources.add(web2.getBundleURIToMootoolsLib());
				jsSources.add(web2.getMoodalboxScriptFilePath(false));
				jsSources.add(web2.getBundleUriToMootabsScript());
				css.add(web2.getMoodalboxStyleFilePath());
				css.add(web2.getBundleUriToMootabsStyle());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
				container.add(PresentationUtil.getJavaScriptSourceLines(jsSources));
				container.add(PresentationUtil.getStyleSheetsSourceLines(css));
			}
			else {
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, jsSources);
				PresentationUtil.addStyleSheetsToHeader(iwc, css);
			}
		}

		StringBuffer parameters = new StringBuffer("['").append(BuilderLogic.getInstance().getUriToObject(SimpleGroupCreator.class));
		parameters.append(SimpleUserApp.PARAMS_SEPARATOR).append(chooserId).append(SimpleUserApp.PARAMS_SEPARATOR).append(groupsType).append("', ");
		parameters.append(helper.getJavaScriptParameter(properties.getGroupTypesForParentGroups())).append(SimpleUserApp.COMMA_SEPARATOR);
		parameters.append(helper.getJavaScriptParameter(properties.getGroupTypes())).append(SimpleUserApp.COMMA_SEPARATOR);
		parameters.append(helper.getJavaScriptParameter(properties.getRoleTypes())).append(SimpleUserApp.COMMA_SEPARATOR);
		parameters.append(helper.getJavaScriptParameter(getResourceBundle(iwc).getLocalizedString("creating", "Creating..."))).append(", '");
		parameters.append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append(SimpleUserApp.PARAMS_SEPARATOR);
		parameters.append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append("', ");
		parameters.append(helper.getJavaScriptParameter(parentGroupChooserId)).append(SimpleUserApp.COMMA_SEPARATOR);
		parameters.append(groupsHelper.getJavaScriptParameter(UserConstants.AVAILABLE_GROUP_TYPES_IN_SIMPLE_USER_APPLICATION))
		.append(SimpleUserApp.COMMA_SEPARATOR).append(groupsHelper.getJavaScriptParameter(UserConstants.AVAILABLE_ROLE_TYPES_IN_SIMPLE_USER_APPLICATION))
		.append(SimpleUserApp.COMMA_SEPARATOR)
		.append(helper.getJavaScriptParameter(properties.getParentGroupId() == -1 ? null : String.valueOf(properties.getParentGroupId()))).append("]");
		StringBuffer action = new StringBuffer("createOrModifyGroup(").append(parameters.toString()).append(SimpleUserApp.COMMA_SEPARATOR)
							.append(properties.isGetParentGroupsFromTopNodes()).append(SimpleUserApp.COMMA_SEPARATOR)
							.append(properties.isUseChildrenOfTopNodesAsParentGroups())
							.append(SimpleUserApp.COMMA_SEPARATOR);

		Layer buttonsContainer = new Layer();
		container.add(buttonsContainer);
		if (addGroupCreateButton) {
			StringBuffer createAction = new StringBuffer(action.toString()).append(Boolean.FALSE.toString()).append(");");
			GenericButton createGroup = getSimpleButton(getResourceBundle(iwc).getLocalizedString("create_new_group", "Create group"), createAction.toString());
			buttonsContainer.add(createGroup);
		}
		if (addGroupEditButton) {
			StringBuffer editAction = new StringBuffer(action.toString()).append(Boolean.TRUE.toString()).append(");");
			GenericButton editGroup = getSimpleButton(getResourceBundle(iwc).getLocalizedString("edit.group", "Edit group"), editAction.toString());
			buttonsContainer.add(editGroup);
		}
	}

	private GenericButton getSimpleButton(String name, String action) {
		GenericButton button = new GenericButton(name);
		button.setOnClick(action);
		return button;
	}

	private void addChooserContainer(IWContext iwc, Layer choosers, DropdownMenu[] dropDowns, String[] ids) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		String loadingMessage = iwrb.getLocalizedString("loading", "Loading...");

		DropdownMenu groupsDropdown = dropDowns[0];
		DropdownMenu childGroupsChooser = dropDowns[1];
		DropdownMenu orderByChooser = dropDowns[2];

		String groupUsersContainerId = ids[0];
		String parentGroupsChooserId = ids[1];
		String childGroupsChooserId = ids[2];
		String orderByChooserId = ids[3];

		properties.setParentGroupChooserId(parentGroupsChooserId);
		properties.setGroupChooserId(childGroupsChooserId);
		properties.setDefaultGroupId(getGroupForUsersWithoutLogin() == null ? null : getGroupForUsersWithoutLogin().getId());
		properties.setMessage(loadingMessage);
		properties.setOrderBy(SimpleUserApp.USER_ORDER_BY_NAME);

		//	Parent group
		Layer parentGroupLabelContainer = new Layer();
		parentGroupLabelContainer.setStyleClass("parentGroupLabelContainerStyleClass");
		choosers.add(parentGroupLabelContainer);
		parentGroupLabelContainer.add(new Text(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		Layer parentGroupChooserContainer = new Layer();
		parentGroupChooserContainer.setStyleClass("parentGroupContainerStyleClass");
		choosers.add(parentGroupChooserContainer);
		Group parentGroup = fillParentGroupChooser(iwc, groupsDropdown, parentGroupChooserContainer, ids, properties.getParentGroups(), properties.getParentGroupsToExclude());
		addGroupButtons(iwc, parentGroupsChooserId, null, parentGroupChooserContainer, 0, properties.isAddGroupCreateButton(), properties.isAddGroupEditButton());
		choosers.add(getSpacer());

		//	Child groups
		Group childGroup = null;
		if (properties.isShowSubGroup()) {
			Layer childGroupsLabelContainer = new Layer();
			choosers.add(childGroupsLabelContainer);
			childGroupsLabelContainer.setStyleClass("childGroupsLabelContainerStyleClass");
			childGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
			Layer childGroupChooserContainer = new Layer();
			childGroupChooserContainer.setStyleClass("childGroupChooserContainerSyleClass");
			choosers.add(childGroupChooserContainer);
			childGroup = fillChildGroupsChooser(iwc, childGroupChooserContainer, parentGroup, childGroupsChooser, ids, properties.getSubGroups(), properties.getSubGroupsToExclude());
			addGroupButtons(iwc, childGroupsChooserId, parentGroupsChooserId, childGroupChooserContainer, 1, properties.isAddChildGroupCreateButton(), properties.isAddChildGroupEditButton());
			choosers.add(getSpacer());
		}

		//	Order
		Layer orderByLabelContainer = new Layer();
		choosers.add(orderByLabelContainer);
		orderByLabelContainer.setStyleClass("orderByLabelContainerStyleClass");
		orderByLabelContainer.add(new Text(iwrb.getLocalizedString("order_by", "Order by")));
		Layer orderByChooserContainer = new Layer();
		choosers.add(orderByChooserContainer);
		orderByChooserContainer.setStyleClass("orderByChooserStyleClass");

		SelectOption byName = new SelectOption(iwrb.getLocalizedString("name", "Name"), SimpleUserApp.USER_ORDER_BY_NAME);
		orderByChooser.addOption(byName);
		StringBuffer orderByAction = new StringBuffer("reOrderGroupUsers('").append(parentGroupsChooserId);
		orderByAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(childGroupsChooserId).append(SimpleUserApp.PARAMS_SEPARATOR).append(orderByChooserId);
		orderByAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(groupUsersContainerId).append(SimpleUserApp.PARAMS_SEPARATOR);
		orderByAction.append(loadingMessage).append("', ").append(helper.getBeanAsParameters(properties, ids[1], ids[2], loadingMessage)).append(");");
		orderByChooser.setOnChange(orderByAction.toString());
		SelectOption byId = new SelectOption(iwrb.getLocalizedString("personal_id", "Personal ID"), SimpleUserApp.USER_ORDER_BY_ID);
		orderByChooser.addOption(byId);
		orderByChooserContainer.add(orderByChooser);

		if (childGroup != null)
			properties.setGroupId(getParsedId(childGroup.getId()));
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

	private Group fillChildGroupsChooser(IWContext iwc, Layer container, Group parent, DropdownMenu childGroups, String[] ids, String subGroups, String subGroupsToExclude) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		String loadingMessage = iwrb.getLocalizedString("loading", "Loading...");

		List<Group> filteredChildGroups = groupsHelper.getFilteredChildGroups(iwc, parent, properties.getGroupTypes(), properties.getRoleTypes(), CoreConstants.COMMA, subGroups, subGroupsToExclude);

		String parentGroupChooserId = ids[1];
		String groupUsersContainerId = ids[0];
		String orderByChooserId = ids[3];

		if (!filteredChildGroups.isEmpty()) {
			childGroups.addMenuElement("-1", CoreConstants.MINUS);
		}
		childGroups.addMenuElements(filteredChildGroups);
		if (!filteredChildGroups.isEmpty()) {
			childGroups.setSelectedElement(filteredChildGroups.get(0).getId());
		}
		StringBuffer onChangeChildGroupsChooserAction = new StringBuffer("selectChildGroup(");
		onChangeChildGroupsChooserAction.append(helper.getJavaScriptParameter(ids[2])).append(", '");
		onChangeChildGroupsChooserAction.append(groupUsersContainerId).append(SimpleUserApp.PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(parentGroupChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(orderByChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);
		onChangeChildGroupsChooserAction.append(loadingMessage).append("', ");
		onChangeChildGroupsChooserAction.append(helper.getBeanAsParameters(properties, ids[1], ids[2], loadingMessage)).append(");");
		childGroups.setOnChange(onChangeChildGroupsChooserAction.toString());
		container.add(childGroups);
		if (filteredChildGroups.size() == 0) {
			return null;
		}
		return filteredChildGroups.get(0);
	}

	private Group fillParentGroupChooser(IWContext iwc, DropdownMenu groupsDropdown, Layer container, String[] ids, String parentGroups, String groupsToExclude) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		List<String> idsToExclude = StringUtil.getValuesFromString(groupsToExclude, CoreConstants.COMMA);

		Collection<Group> topGroups = null;
		if (!StringUtil.isEmpty(parentGroups)) {
			properties.setGetParentGroupsFromTopNodes(false);
			List<String> groupsIds = StringUtil.getValuesFromString(parentGroups, CoreConstants.COMMA);
			groupsIds.removeAll(idsToExclude);
			getLogger().info("Groups to select: " + groupsIds);
			try {
				GroupBusiness groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
				topGroups = groupBusiness.getGroups(groupsIds);
			} catch (Exception e) {
				e.printStackTrace();
			}
			getLogger().info("Got top groups: " + topGroups);
		} else if (getParentGroup() == null) {
			//	Group is not set as property
			topGroups = groupsHelper.getTopGroupsFromDomain(iwc);
		} else {
			//	Group is set as property
			if (properties.isUseChildrenOfTopNodesAsParentGroups()) {
				topGroups = getParentGroup().getChildren();
			} else {
				addGroupNameLabel(iwrb, container, getParentGroup());
				return getParentGroup();
			}
		}

		if (StringUtil.isEmpty(parentGroups) && !properties.isGetParentGroupsFromTopNodes()) {
			topGroups = groupsHelper.getTopAndParentGroups(topGroups);	//	Will get top nodes and parent groups for them
			getLogger().info("Got top and parent groups: " + topGroups);
		}

		if (ListUtil.isEmpty(topGroups)) {
			//	No groups found for current user
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return null;
		}

		getLogger().info("Top groups: " + topGroups);

		List<Group> filteredTopGroups = null;
		if (StringUtil.isEmpty(parentGroups)) {
			filteredTopGroups = new ArrayList<Group>(groupsHelper.getFilteredGroups(
						iwc,
						topGroups,
						properties.getGroupTypesForParentGroups(),
						CoreConstants.COMMA,
						(getParentGroup() == null && properties.isUseChildrenOfTopNodesAsParentGroups())
					)
				);
		} else {
			filteredTopGroups = new ArrayList<Group>(topGroups);
			Collections.sort(filteredTopGroups, new GroupComparator(iwc));
		}
		if (filteredTopGroups.size() > 1) {
			Group groupToReturn = null;

			String groupUsersContainerId = ids[0];
			String childGroupsChooserId = ids[2];
			String orderByChooserId = ids[3];

			groupsDropdown.addMenuElements(filteredTopGroups);
			if (selectedParentGroupId == null) {
				groupToReturn = filteredTopGroups.get(0);
			}
			else {
				String groupID = String.valueOf(selectedParentGroupId);
				for (int i = 0; (i < filteredTopGroups.size() && groupToReturn == null); i++) {
					groupToReturn = filteredTopGroups.get(i);
					if (!groupToReturn.getId().equals(groupID)) {
						groupToReturn = null;
					}
				}
			}
			if (groupToReturn != null) {
				properties.setParentGroupId(Integer.valueOf(groupToReturn.getId()));
			}

			StringBuffer action = new StringBuffer("reloadComponents('");
			action.append(iwrb.getLocalizedString("loading", "Loading...")).append(SimpleUserApp.PARAMS_SEPARATOR);			//	0
			action.append(childGroupsChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);										//	1
			action.append(orderByChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);											//	2
			action.append(groupUsersContainerId).append(SimpleUserApp.PARAMS_SEPARATOR);									//	3
			action.append(groupsDropdown.getId()).append("', ");															//	4
			action.append(helper.getJavaScriptParameter(properties.getGroupTypes())).append(SimpleUserApp.COMMA_SEPARATOR);	//	5
			action.append(helper.getJavaScriptParameter(properties.getRoleTypes())).append(SimpleUserApp.COMMA_SEPARATOR);	//	6
			action.append(helper.getBeanAsParameters(properties, null, childGroupsChooserId, null)).append(SimpleUserApp.COMMA_SEPARATOR);	//	7
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(properties.getSubGroups()).append(CoreConstants.QOUTE_SINGLE_MARK).append(SimpleUserApp.COMMA_SEPARATOR);	//	8
			action.append(CoreConstants.QOUTE_SINGLE_MARK).append(properties.getSubGroupsToExclude()).append(CoreConstants.QOUTE_SINGLE_MARK);	//	9
			action.append(");");
			groupsDropdown.setOnChange(action.toString());
			container.add(groupsDropdown);

			if (selectedParentGroupId == null) {
				return groupToReturn;
			}

			groupsDropdown.setSelectedElement(selectedParentGroupId);
			return groupToReturn;
		}
		else if (filteredTopGroups.size() == 1) {
			//	Only one group available
			Group group = filteredTopGroups.get(0);
			addGroupNameLabel(iwrb, container, group);
			return group;
		}

		//	No groups found for current user
		container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
		return null;
	}

	private void addGroupNameLabel(IWResourceBundle iwrb, Layer container, Group group) {
		String groupName = group.getName() == null ? iwrb.getLocalizedString("unknown_group", "Unknown group") : group.getName();
		container.add(new Text(groupName));
	}

	private Layer getSpacer() {
		return new CSSSpacer();
	}

	public void setGroupForUsersWithoutLogin(Group groupForUsersWithoutLogin) {
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
	}

	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}

	private Group getGroupForUsersWithoutLogin() {
		return groupForUsersWithoutLogin;
	}

	private Group getParentGroup() {
		return parentGroup;
	}

	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public void setSelectedParentGroupId(Integer selectedParentGroupId) {
		this.selectedParentGroupId = selectedParentGroupId;
	}

	public boolean isCheckPagerProperties() {
		return checkPagerProperties;
	}

	public void setCheckPagerProperties(boolean checkPagerProperties) {
		this.checkPagerProperties = checkPagerProperties;
	}

}
