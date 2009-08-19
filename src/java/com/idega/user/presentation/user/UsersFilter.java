package com.idega.user.presentation.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.GroupNode;
import com.idega.user.business.UserConstants;
import com.idega.user.business.UsersFilterHelper;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class UsersFilter extends InterfaceObject {

	@Autowired
	private GroupHelper groupHelper;
	
	private List<String> roles;
	
	private String groupId;
	
	private String selectedUserInputName;
	private List<String> selectedUsers;
	
	private boolean addLabel = true;
	private boolean showGroupChooser = true;
	
	private List<String> visibleGroups;
	
	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);
		
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/user.css"));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				CoreConstants.DWR_ENGINE_SCRIPT,
				CoreConstants.DWR_UTIL_SCRIPT,
				"/dwr/interface/" + UsersFilterHelper.DWR_OBJECT + ".js",
				getBundle(iwc).getVirtualPathWithFileNameString("javascript/UsersFilterHelper.js")
		));
		String action = new StringBuilder("UsersFilterHelper.assignActionToForm('").append(getSelectedUserInputName()).append("', [")
			.append(ListUtil.convertListOfStringsToCommaseparatedString(selectedUsers)).append("]);").toString();
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = new StringBuilder("registerEvent(window, 'load', function() {").append(action).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, action);
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("usersFilterStyle");
		
		Layer userListContainer = new Layer();
		if (ListUtil.isEmpty(roles) || isShowGroupChooser()) {
			container.add(getGroupChooser(iwc, userListContainer.getId()));
		}
		container.add(new CSSSpacer());
		container.add(userListContainer);
		userListContainer.add(getUsersList(iwc));
	}
	
	private Layer getGroupChooser(IWContext iwc, String usersListId) {
		Layer container = new Layer();
		
		DropdownMenu groupChooser = new DropdownMenu();
		if (addLabel) {
			Label chooserLabel = new Label(getResourceBundle(iwc).getLocalizedString("users_filter.select_group", "Select group"), groupChooser);
			container.add(chooserLabel);
		}
		container.add(groupChooser);
		
		fillGroupChooser(iwc, groupChooser, usersListId);
		
		resolveGroupId(iwc);
		
		if (groupId != null) {
			groupChooser.setSelectedElement(groupId);
		}
		
		return container;
	}
	
	private void resolveGroupId(IWContext iwc) {
		if (groupId != null) {
			return;
		}
		
		if (ListUtil.isEmpty(visibleGroups) || ListUtil.isEmpty(selectedUsers)) {
			return;
		}

		User user = null;
		try {
			user = groupHelper.getUserBusiness(getIWApplicationContext()).getUser(Integer.valueOf(selectedUsers.get(0)));
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (user == null) {
			return;
		}
		
		List<String> userGroups = groupHelper.getUserGroupsIds(iwc, user);
		if (ListUtil.isEmpty(userGroups)) {
			return;
		}
		
		for (String userGroupId: userGroups) {
			if (visibleGroups.contains(userGroupId)) {
				groupId = userGroupId;
				return;
			}
		}
	}
	
	private void fillGroupChooser(IWContext iwc, DropdownMenu groupChooser, String usersListId) {
		User user = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		List<GroupNode> userGroups = groupHelper.getTopGroupsAndDirectChildren(user, iwc, true);
		if (ListUtil.isEmpty(userGroups)) {
			groupChooser.addFirstOption(new SelectOption(
					getResourceBundle(iwc).getLocalizedString("users_filter.no_group_available", "Sorry, there are no groups available"), -1));
			groupChooser.setDisabled(true);
			return;
		}
		
		String groupId = null;
		for (GroupNode groupNode: userGroups) {
			groupId = groupNode.getId();
			if (visibleGroups == null) {
				visibleGroups = new ArrayList<String>();
			}
			visibleGroups.add(groupId);
			
			if (ListUtil.isEmpty(selectedUsers) && this.groupId == null) {
				this.groupId = groupId;
			}
			
			SelectOption option = new SelectOption(groupNode.getName(), groupId);
			groupChooser.addOption(option);
			if (groupNode.isHasChildren()) {
				String childGroupId = null;
				for (GroupNode childGroup: groupNode.getChildren()) {
					childGroupId = childGroup.getId();
					visibleGroups.add(childGroupId);
					
					groupChooser.addOption(new SelectOption(
							new StringBuilder(CoreConstants.MINUS).append(CoreConstants.SPACE).append(childGroup.getName()).toString(), childGroupId));
				}
			}
		}
		
		groupChooser.setOnChange(new StringBuilder("UsersFilterHelper.getUsers('").append(groupChooser.getId()).append("', ")
				.append(groupHelper.getJavaScriptFunctionParameter(selectedUsers)).append(", '")
				.append(getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("', '").append(usersListId)
				.append("', '").append(getSelectedUserInputName()).append("');")
		.toString());
	}
	
	private UsersFilterList getUsersList(IWContext iwc) {
		UsersFilterList list = new UsersFilterList();
		list.setSelectedUsers(selectedUsers);
		list.setGroupId(groupId);
		list.setRoles(roles);
		list.setSelectedUserInputName(getSelectedUserInputName());
		return list;
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void handleKeepStatus(IWContext iwc) {
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	public List<String> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<String> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public boolean isAddLabel() {
		return addLabel;
	}

	public void setAddLabel(boolean addLabel) {
		this.addLabel = addLabel;
	}

	public String getSelectedUserInputName() {
		if (selectedUserInputName == null) {
			selectedUserInputName = UsersFilterList.USERS_FILTER_SELECTED_USERS;
		}
		return selectedUserInputName;
	}

	public void setSelectedUserInputName(String selectedUserInputName) {
		this.selectedUserInputName = selectedUserInputName;
	}

	public boolean isShowGroupChooser() {
		return showGroupChooser;
	}

	public void setShowGroupChooser(boolean showGroupChooser) {
		this.showGroupChooser = showGroupChooser;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
}