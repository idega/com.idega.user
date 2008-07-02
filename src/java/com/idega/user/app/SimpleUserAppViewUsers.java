package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
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
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupMembersListViewer;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class SimpleUserAppViewUsers extends Block {

	private String containerId = null;
	private String instanceId = null;
	
	private Group parentGroup = null;
	private Group groupForUsersWithoutLogin = null;
	
	private String groupTypes = null;
	private String groupTypesForChildGroups = null;
	private String roleTypesForChildGroups = null;
	
	private Integer selectedParentGroupId = null;
	
	private boolean getParentGroupsFromTopNodes = true;
	private boolean useChildrenOfTopNodesAsParentGroups = false;
	private boolean allFieldsEditable = false;
	private boolean addGroupCreateButton = false;
	private boolean addGroupEditButton = false;
	
	private GroupHelper groupsHelper = null;
	private SimpleUserAppHelper helper = new SimpleUserAppHelper();

	public SimpleUserAppViewUsers(String instanceId, String containerId) {
		this.instanceId = instanceId;
		this.containerId = containerId;
	}
	
	public SimpleUserAppViewUsers(String instanceId, String containerId, Group parentGroup, Group groupForUsersWithoutLogin,
			String groupTypes, String groupTypesForChildGroups, String roleTypesForChildGroups, boolean getParentGroupsFromTopNodes,
			boolean useChildrenOfTopNodesAsParentGroups, boolean allFieldsEditable, boolean addGroupCreateButton, boolean addGroupEditButton) {
		this(instanceId, containerId);
		this.parentGroup = parentGroup;
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
		this.groupTypes = groupTypes;
		this.groupTypesForChildGroups = groupTypesForChildGroups;
		this.roleTypesForChildGroups = roleTypesForChildGroups;
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
		this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
		this.allFieldsEditable = allFieldsEditable;
		this.addGroupCreateButton = addGroupCreateButton;
		this.addGroupEditButton = addGroupEditButton;
	}

	@Override
	public void main(IWContext iwc) {
		groupsHelper = ELUtil.getInstance().getBean(GroupHelper.class);
		
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
		ids[4] = containerId;

		//	Upper part - dropdowns and description
		Layer choosersAndDescription = new Layer();
		choosersAndDescription.setStyleClass("choosersAndDescriptionStyleClass");
		container.add(choosersAndDescription);
		
		//	Dropdowns
		Layer choosersContainer = new Layer();
		choosersAndDescription.add(choosersContainer);
		choosersContainer.setStyleClass("userApplicationChoosersContainer");
		SimpleUserPropertiesBean bean = addChooserContainer(iwc, choosersContainer, dropDowns, ids);
		bean.setGroupTypesForParentGroups(groupTypes);
		bean.setUseChildrenOfTopNodesAsParentGroups(useChildrenOfTopNodesAsParentGroups);
		
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
		addMembersList(iwc, bean, membersList, valuesContainer, containerId);
		
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
		
		SimpleUserPropertiesBean bean = new SimpleUserPropertiesBean();
		bean.setInstanceId(instanceId);
		bean.setContainerId(containerId);
		bean.setParentGroupChooserId(ids[1]);
		bean.setGroupChooserId(ids[2]);
		bean.setMessage(iwrb.getLocalizedString("loading", "Loading..."));
		bean.setDefaultGroupId(id);
		bean.setGroupTypes(groupTypesForChildGroups);
		bean.setRoleTypes(roleTypesForChildGroups);
		bean.setGetParentGroupsFromTopNodes(getParentGroupsFromTopNodes);
		bean.setGroupTypesForParentGroups(groupTypes);
		bean.setUseChildrenOfTopNodesAsParentGroups(useChildrenOfTopNodesAsParentGroups);
		bean.setAllFieldsEditable(allFieldsEditable);
		
		addUser.setOnClick(helper.getActionForAddUserView(bean, null));
		container.add(addUser);
	}
	
	private void addMembersList(IWContext iwc, SimpleUserPropertiesBean bean, Layer container, Layer valuesContainer,
			String mainContainerId) {
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
		
		String image = bundle.getVirtualPathWithFileNameString(SimpleUserApp.EDIT_IMAGE);
		GroupMembersListViewer list = new GroupMembersListViewer();
		list.setBean(bean);
		list.setImage(image);
		valuesContainer.add(list);
	}
	
	private void addGroupButtons(IWContext iwc, String chooserId, String parentGroupChooserId, Layer container, int groupsType) {
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
		if (getGroupTypes() == null) {
			parameters.append("null, ");
		}
		else {
			parameters.append("'").append(getGroupTypes()).append("', ");
		}
		if (getGroupTypesForChildGroups() == null) {
			parameters.append("null, ");
		}
		else {
			parameters.append("'").append(getGroupTypesForChildGroups()).append("', ");
		}
		if (getRoleTypesForChildGroups() == null) {
			parameters.append("null, ");
		}
		else {
			parameters.append("'").append(getRoleTypesForChildGroups()).append("', ");
		}
		parameters.append("'").append(getResourceBundle(iwc).getLocalizedString("creating", "Creating...")).append(SimpleUserApp.PARAMS_SEPARATOR);
		parameters.append(UserConstants.EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append(SimpleUserApp.PARAMS_SEPARATOR);
		parameters.append(UserConstants.GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION).append("', ");
		if (parentGroupChooserId == null) {
			parameters.append("null");
		}
		else {
			parameters.append("'").append(parentGroupChooserId).append("'");
		}
		parameters.append("]");
		StringBuffer action = new StringBuffer("createOrModifyGroup(").append(parameters.toString()).append(", ").append(isGetParentGroupsFromTopNodes()).append(", ");
		action.append(useChildrenOfTopNodesAsParentGroups).append(", ");
		
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
	
	private SimpleUserPropertiesBean addChooserContainer(IWContext iwc, Layer choosers, DropdownMenu[] dropDowns, String[] ids) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		String loadingMessage = iwrb.getLocalizedString("loading", "Loading...");
		
		DropdownMenu groupsDropdown = dropDowns[0];
		DropdownMenu childGroupsChooser = dropDowns[1];
		DropdownMenu orderByChooser = dropDowns[2];
		
		String groupUsersContainerId = ids[0];
		String parentGroupsChooserId = ids[1];
		String childGroupsChooserId = ids[2];
		String orderByChooserId = ids[3];
		
		//	Parent group
		Layer parentGroupLabelContainer = new Layer();
		parentGroupLabelContainer.setStyleClass("parentGroupLabelContainerStyleClass");
		choosers.add(parentGroupLabelContainer);
		parentGroupLabelContainer.add(new Text(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		Layer parentGroupChooserContainer = new Layer();
		parentGroupChooserContainer.setStyleClass("parentGroupContainerStyleClass");
		choosers.add(parentGroupChooserContainer);
		Group parentGroup = fillParentGroupChooser(iwc, groupsDropdown, parentGroupChooserContainer, ids);
		addGroupButtons(iwc, parentGroupsChooserId, null, parentGroupChooserContainer, 0);
		choosers.add(getSpacer());
		
		//	Child groups
		Layer childGroupsLabelContainer = new Layer();
		choosers.add(childGroupsLabelContainer);
		childGroupsLabelContainer.setStyleClass("childGroupsLabelContainerStyleClass");
		childGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		Layer childGroupChooserContainer = new Layer();
		childGroupChooserContainer.setStyleClass("childGroupChooserContainerSyleClass");
		choosers.add(childGroupChooserContainer);
		Group childGroup = fillChildGroupsChooser(iwc, childGroupChooserContainer, parentGroup, childGroupsChooser, ids);
		addGroupButtons(iwc, childGroupsChooserId, parentGroupsChooserId, childGroupChooserContainer, 1);
		choosers.add(getSpacer());
		
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
		orderByAction.append(loadingMessage).append("', ").append(getDefaultParameters(ids[1], ids[2], loadingMessage)).append(");");
		orderByChooser.setOnChange(orderByAction.toString());
		SelectOption byId = new SelectOption(iwrb.getLocalizedString("personal_id", "Personal ID"), SimpleUserApp.USER_ORDER_BY_ID);
		orderByChooser.addOption(byId);
		orderByChooserContainer.add(orderByChooser);
		
		SimpleUserPropertiesBean bean = new SimpleUserPropertiesBean();
		bean.setInstanceId(instanceId);
		bean.setContainerId(containerId);
		bean.setParentGroupChooserId(parentGroupsChooserId);
		bean.setGroupChooserId(childGroupsChooserId);
		bean.setDefaultGroupId(getGroupForUsersWithoutLogin() == null ? null : getGroupForUsersWithoutLogin().getId());
		bean.setGroupTypes(getGroupTypesForChildGroups());
		bean.setRoleTypes(getRoleTypesForChildGroups());
		bean.setMessage(loadingMessage);
		if (parentGroup != null) {
			bean.setParentGroupId(getParsedId(parentGroup.getId()));
		}
		if (childGroup != null) {
			bean.setGroupId(getParsedId(childGroup.getId()));
		}
		bean.setOrderBy(SimpleUserApp.USER_ORDER_BY_NAME);
		bean.setAllFieldsEditable(allFieldsEditable);
		
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
	
	private Group fillChildGroupsChooser(IWContext iwc, Layer container, Group parent, DropdownMenu childGroups, String[] ids) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		String loadingMessage = iwrb.getLocalizedString("loading", "Loading...");
		
		List<Group> filteredChildGroups = groupsHelper.getFilteredChildGroups(iwc, parent, getGroupTypesForChildGroups(),
				getRoleTypesForChildGroups(), ",");
		
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
		onChangeChildGroupsChooserAction.append(getDefaultParameters(ids[1], ids[2], loadingMessage)).append(");");
		childGroups.setOnChange(onChangeChildGroupsChooserAction.toString());
		container.add(childGroups);
		if (filteredChildGroups.size() == 0) {
			return null;
		}
		return filteredChildGroups.get(0);
	}
	
	private String getDefaultParameters(String parentGroupChooserId, String childGroupChooserId, String message) {
		StringBuffer params = new StringBuffer("[").append(helper.getJavaScriptParameter(instanceId)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(containerId)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(childGroupChooserId)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(getGroupForUsersWithoutLogin() == null ? null : getGroupForUsersWithoutLogin().getId()));
		params.append(SimpleUserApp.COMMA_SEPARATOR).append(helper.getJavaScriptParameter(getGroupTypesForChildGroups())).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(getRoleTypesForChildGroups())).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(message)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(parentGroupChooserId)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(helper.getJavaScriptParameter(groupTypes)).append(SimpleUserApp.COMMA_SEPARATOR);
		params.append(isUseChildrenOfTopNodesAsParentGroups()).append(SimpleUserApp.COMMA_SEPARATOR).append(allFieldsEditable);
		params.append("]");
		
		return params.toString();
	}
	
	private Group fillParentGroupChooser(IWContext iwc, DropdownMenu groupsDropdown, Layer container, String[] ids) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		if (getParentGroup() == null) {
			//	Group is not set as property
			Collection<Group> topGroups = groupsHelper.getTopGroupsFromDomain(iwc);
			if (!isGetParentGroupsFromTopNodes()) {
				topGroups = groupsHelper.getTopAndParentGroups(topGroups);	//	Will get top nodes and parent groups for them
			}
			
			if (topGroups.size() > 0) {
				List<Group> filteredTopGroups = new ArrayList<Group>(groupsHelper.getFilteredGroups(topGroups, getGroupTypes(), ",", useChildrenOfTopNodesAsParentGroups));
				if (filteredTopGroups.size() > 1) {
					String groupUsersContainerId = ids[0];
					String childGroupsChooserId = ids[2];
					String orderByChooserId = ids[3];
					
					groupsDropdown.addMenuElements(filteredTopGroups);
					StringBuffer action = new StringBuffer("reloadComponents('");
					action.append(iwrb.getLocalizedString("loading", "Loading...")).append(SimpleUserApp.PARAMS_SEPARATOR);	//	0
					action.append(childGroupsChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);								//	1
					action.append(orderByChooserId).append(SimpleUserApp.PARAMS_SEPARATOR);									//	2
					action.append(groupUsersContainerId).append(SimpleUserApp.PARAMS_SEPARATOR);							//	3
					action.append(groupsDropdown.getId()).append("', ");													//	4
					action.append(helper.getJavaScriptParameter(getGroupTypesForChildGroups())).append(SimpleUserApp.COMMA_SEPARATOR);	//	5
					action.append(helper.getJavaScriptParameter(getRoleTypesForChildGroups())).append(SimpleUserApp.COMMA_SEPARATOR);		//	6
					action.append(helper.getJavaScriptParameter(instanceId)).append(SimpleUserApp.COMMA_SEPARATOR);											//	7
					action.append(helper.getJavaScriptParameter(containerId)).append(SimpleUserApp.COMMA_SEPARATOR);										//	8
					action.append(helper.getJavaScriptParameter(getGroupForUsersWithoutLogin() == null ? null : getGroupForUsersWithoutLogin().getId()));	//	9
					action.append(SimpleUserApp.COMMA_SEPARATOR).append(helper.getJavaScriptParameter(ids[1]));		//	10
					action.append(SimpleUserApp.COMMA_SEPARATOR).append(helper.getJavaScriptParameter(groupTypes));	//	11
					action.append(SimpleUserApp.COMMA_SEPARATOR).append(isUseChildrenOfTopNodesAsParentGroups());	//	12
					action.append(SimpleUserApp.COMMA_SEPARATOR).append(allFieldsEditable);							//	13
					action.append(");");
					groupsDropdown.setOnChange(action.toString());
					container.add(groupsDropdown);
					
					if (selectedParentGroupId == null) {
						return filteredTopGroups.get(0);
					}
					groupsDropdown.setSelectedElement(selectedParentGroupId);
					Group selectedGroup = null;
					String groupID = String.valueOf(selectedParentGroupId);
					for (int i = 0; (i < filteredTopGroups.size() && selectedGroup == null); i++) {
						selectedGroup = filteredTopGroups.get(i);
						if (!selectedGroup.getId().equals(groupID)) {
							selectedGroup = null;
						}
					}
					return selectedGroup;
				}
				else if (filteredTopGroups.size() == 1) {
					//	Only one group available
					Object o = filteredTopGroups.get(0);
					if (o instanceof Group) {
						Group group = (Group) o;
						addGroupNameLabel(iwrb, container, group);
						return group;
					}
					container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));

					return null;
				}
				else {
					container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));

					return null;
				}
			}
			//	No groups found for current user
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return null;
		}
		else {
			//	Group is set as property
			addGroupNameLabel(iwrb, container, getParentGroup());
			return getParentGroup();
		}
	}
	
	private void addGroupNameLabel(IWResourceBundle iwrb, Layer container, Group group) {
		String groupName = group.getName() == null ? iwrb.getLocalizedString("unknown_group", "Unknown group") : group.getName();
		container.add(new Text(groupName));
	}
	
	private Layer getSpacer() {
		Layer spacer = new Layer();
		spacer.setStyleClass("spacer");
		return spacer;
	}

	public void setGroupForUsersWithoutLogin(Group groupForUsersWithoutLogin) {
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
	}

	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}

	public void setGroupTypesForChildGroups(String groupTypesForChildGroups) {
		this.groupTypesForChildGroups = groupTypesForChildGroups;
	}

	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}

	public void setRoleTypesForChildGroups(String roleTypesForChildGroups) {
		this.roleTypesForChildGroups = roleTypesForChildGroups;
	}

	public void setGetParentGroupsFromTopNodes(boolean getParentGroupsFromTopNodes) {
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	}

	private boolean isGetParentGroupsFromTopNodes() {
		return getParentGroupsFromTopNodes;
	}

	private Group getGroupForUsersWithoutLogin() {
		return groupForUsersWithoutLogin;
	}

	private String getGroupTypes() {
		return groupTypes;
	}

	private String getGroupTypesForChildGroups() {
		return groupTypesForChildGroups;
	}

	private Group getParentGroup() {
		return parentGroup;
	}

	private String getRoleTypesForChildGroups() {
		return roleTypesForChildGroups;
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public boolean isUseChildrenOfTopNodesAsParentGroups() {
		return useChildrenOfTopNodesAsParentGroups;
	}

	public void setSelectedParentGroupId(Integer selectedParentGroupId) {
		this.selectedParentGroupId = selectedParentGroupId;
	}
}
