package com.idega.user.presentation.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserConstants;
import com.idega.user.data.User;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class UsersFilterList extends Block {

	public static final String USERS_FILTER_SELECTED_USERS = "usersFilterSelectedUsers";
	
	private String groupId;
	
	private String selectedUserInputName;
	private List<String> selectedUsers;

	@Autowired
	private GroupHelper groupHelper;
	
	public List<String> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<String> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}
	
	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);
		
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("style/user.css"));
		
		Layer container = new Layer();
		container.setStyleClass("usersFilterUsersListStyle");
		add(container);
		
		if (StringUtil.isEmpty(groupId)) {
			return;
		}
		
		List<User> users = getUsersFromSelectedGroup(iwc);
		if (ListUtil.isEmpty(users)) {
			container.add(new Heading3(getResourceBundle(iwc).getLocalizedString("users_filter.no_users_found", "There are no users")));
			return;
		}
		
		selectedUsers = selectedUsers == null ? new ArrayList<String>(0) : selectedUsers;
		
		for (User user: users) {
			Layer userEntry = new Layer();
			container.add(userEntry);
			container.add(new CSSSpacer());
			
			String id = user.getId();
			
			CheckBox select = new CheckBox(getSelectedUserInputName(), id);
			select.setChecked(selectedUsers.contains(id), true);
			userEntry.add(select);
			
			Span name = new Span(new Text(user.getName()));
			userEntry.add(name);
		}
	}
	
	private List<User> getUsersFromSelectedGroup(IWContext iwc) {
		if (StringUtil.isEmpty(groupId)) {
			return null;
		}
		
		SimpleUserPropertiesBean properties = new SimpleUserPropertiesBean();
		properties.setGroupId(Integer.valueOf(groupId));
		return groupHelper.getUsersInGroup(iwc, properties, true);
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSelectedUserInputName() {
		if (selectedUserInputName == null) {
			selectedUserInputName = USERS_FILTER_SELECTED_USERS;
		}
		return selectedUserInputName;
	}

	public void setSelectedUserInputName(String selectedUserInputName) {
		this.selectedUserInputName = selectedUserInputName;
	}
}
