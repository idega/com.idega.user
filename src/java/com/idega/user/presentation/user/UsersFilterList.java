package com.idega.user.presentation.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.FinderException;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class UsersFilterList extends Block {

	public static final String USERS_FILTER_SELECTED_USERS = "usersFilterSelectedUsers";

	private String groupId;

	private List<String> roles;

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

		if (StringUtil.isEmpty(groupId) && ListUtil.isEmpty(selectedUsers) && ListUtil.isEmpty(roles)) {
			return;
		}

		//	From a selected group
		List<User> users = getUsersFromSelectedGroup(iwc);
		if (ListUtil.isEmpty(users) && ListUtil.isEmpty(selectedUsers) && ListUtil.isEmpty(roles)) {
			container.add(new Heading3(getResourceBundle(iwc).getLocalizedString("users_filter.no_users_found", "There are no users")));
			return;
		}

		//	By roles
		users = users == null ? getUsersByRoles(iwc, roles) : users;

		//	From IDs
		selectedUsers = selectedUsers == null ? new ArrayList<String>(0) : selectedUsers;
		users = ListUtil.isEmpty(users) ? getUsersByIds(selectedUsers) : users;

		String inputName = getSelectedUserInputName() + "_checkbox";
		if (!ListUtil.isEmpty(users)) {
			for (User user: users) {
				Layer userEntry = new Layer();
				container.add(userEntry);
				container.add(new CSSSpacer());

				String id = user.getId();

				CheckBox select = new CheckBox(inputName, id);
				select.setChecked(selectedUsers.contains(id), true);
				select.setOnClick(new StringBuilder("UsersFilterHelper.markUserInForm('").append(select.getId()).append("', '").append(getSelectedUserInputName())
						.append("', '").append(id).append("');").toString());
				userEntry.add(select);

				Span name = new Span(new Text(user.getName()));
				userEntry.add(name);
			}
		}
	}

	private List<User> getUsersByRoles(IWContext iwc, List<String> roles) {
		if (ListUtil.isEmpty(roles)) {
			return null;
		}

		List<User> users = new ArrayList<User>();
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (groupBusiness == null) {
			return null;
		}
		UserBusiness userBusiness = null;
		try {
			userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (userBusiness == null) {
			return null;
		}

		AccessController accessController = iwc.getAccessController();
		for (String roleKey: roles) {
			Collection<Group> groupsByRole = accessController.getAllGroupsForRoleKeyLegacy(roleKey, iwc);
			if (ListUtil.isEmpty(groupsByRole)) {
				continue;
			}

			for (Group group: groupsByRole) {
				if (StringUtil.isEmpty(group.getName())) {
					try {
						User user = userBusiness.getUser(Integer.valueOf(group.getId()));
						if (accessController.hasRole(user, roleKey) && !users.contains(user)) {
							users.add(user);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
					}
				}

				Collection<User> usersInGroup = null;
				try {
					usersInGroup = groupBusiness.getUsers(group);
				} catch (FinderException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!ListUtil.isEmpty(usersInGroup)) {
					for (User user: usersInGroup) {
						if (!users.contains(user)) {
							users.add(user);
						}
					}
				}
			}
		}

		return users;
	}

	private List<User> getUsersByIds(List<String> ids) {
		if (ListUtil.isEmpty(ids)) {
			return null;
		}

		try {
			UserBusiness userBusiness = IBOLookup.getServiceInstance(getIWApplicationContext(), UserBusiness.class);
			Collection<User> users = userBusiness.getUsers(ArrayUtil.convertListToArray(ids));
			return ListUtil.isEmpty(users) ? null : new ArrayList<User>(users);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
