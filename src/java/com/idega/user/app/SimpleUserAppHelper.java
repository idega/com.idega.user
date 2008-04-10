package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.SpringBeanLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class SimpleUserAppHelper {

	public Layer getMembersList(IWContext iwc, SimpleUserPropertiesBean bean, GroupHelper helper, String image) {
		Layer valuesContainer = new Layer();
		if (iwc == null || bean == null) {
			return valuesContainer;
		}
		if (helper == null) {
			helper = SpringBeanLookup.getInstance().getSpringBean(iwc, GroupHelper.class);
		}
		
		List<User> users = helper.getSortedUsers(iwc, bean);
		if (users == null) {
			return valuesContainer;
		}
		
		User user = null;
		String userValuesLineContainerStyleClass = "userValuesLineContainerStyleClass";
		String nameContainerStyleClass = "userNameValueContainerStyleClass";
		String personalIdContainerStyleClass = "userPersonalIdValueContainerStyleClass";
		String changeUserContainerStyleClass = "changeUserImageContainerStyleClass";
		String removeUserContainerStyleClass = "removeUserCheckboxContainerStyleClass";
		String changeUserImageStyleClass = "changeUserImageStyleClass";
		Layer lineContainer = null;
		Layer nameContainer = null;
		Layer personalIdContainer = null;
		Layer changeUserContainer = null;
		Layer removeUserContainer = null;
		Image changeUserImage = null;
		CheckBox removeUserCheckbox = null;
		StringBuffer checkBoxAction = null;
		
		String unknown = getResourceBundle(iwc).getLocalizedString("unknown", "Unknown");
		String name = null;
		String personalId = null;
		String userId = null;
		int groupId = -1;
		for (int i = 0; i < users.size(); i++) {
			user = users.get(i);
			
			userId = user.getId();
			name = user.getName();
			if (CoreConstants.EMPTY.equals(name)) {
				name = null;
			}
			personalId = user.getPersonalID();
			if (CoreConstants.EMPTY.equals(personalId)) {
				personalId = null;
			}
			groupId = bean.getGroupId();
			if (groupId < 0) {
				groupId = bean.getParentGroupId();
			}
			
			lineContainer = new Layer();
			lineContainer.setStyleClass(userValuesLineContainerStyleClass);
			valuesContainer.add(lineContainer);
			
			nameContainer = new Layer();
			nameContainer.setStyleClass(nameContainerStyleClass);
			nameContainer.add(new Text(name == null ? unknown : name));
			lineContainer.add(nameContainer);
			
			personalIdContainer = new Layer();
			personalIdContainer.setStyleClass(personalIdContainerStyleClass);
			personalIdContainer.add(new Text(personalId == null ? unknown : personalId));
			lineContainer.add(personalIdContainer);
			
			changeUserContainer = new Layer();
			changeUserContainer.setStyleClass(changeUserContainerStyleClass);
			changeUserImage = new Image(image);
			changeUserImage.setStyleClass(changeUserImageStyleClass);
			
			changeUserImage.setOnClick(getActionForAddUserView(bean, userId));
			changeUserContainer.add(changeUserImage);
			lineContainer.add(changeUserContainer);
			
			removeUserContainer = new Layer();
			removeUserContainer.setStyleClass(removeUserContainerStyleClass);
			removeUserCheckbox = new CheckBox();
			checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(userId);
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(groupId).append("', ");
			checkBoxAction.append(getJavaScriptParameter(removeUserCheckbox.getId())).append(");");
			removeUserCheckbox.setOnClick(checkBoxAction.toString());
			removeUserContainer.add(removeUserCheckbox);
			lineContainer.add(removeUserContainer);
		}
		
		return valuesContainer;
	}
	
	public Layer getSelectedGroupsByIds(IWContext iwc, User user, GroupHelper helper, List<Integer> groupsIds, List<String> ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		List<Group> groups = null;
		if (groupsIds == null || groupsIds.size() == 0) {
			boolean changedToCurrentUser = false;
			if (user == null) {
				user = iwc.getCurrentUser();
				changedToCurrentUser = true;
			}
			Collection<Group> topGroups = helper.getTopGroupsFromDomain(iwc);
			if (changedToCurrentUser) {
				user = null;
			}
			
			if (topGroups == null || topGroups.size() == 0) {
				addLabelForNoGroups(iwc, selectedGroups);
				return selectedGroups;
			}
			
			groups = new ArrayList<Group>(topGroups);
		}
		else {
			groups = helper.getGroups(iwc, groupsIds);
		}
		
		return getSelectedGroups(iwc, user, helper, groups, ids, selectedGroupId);
	}
	
	public Layer getSelectedGroups(IWContext iwc, User user, GroupHelper helper, List<Group> groups, List<String> ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		if (groups == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		if (groups.size() == 0) {
			boolean changedToCurrentUser = false;
			if (user == null) {
				user = iwc.getCurrentUser();
				changedToCurrentUser = true;
			}
			Collection<Group> topGroups = helper.getTopGroupsFromDomain(iwc);
			if (changedToCurrentUser) {
				user = null;
			}
			
			if (topGroups == null || topGroups.size() == 0) {
				addLabelForNoGroups(iwc, selectedGroups);
				return selectedGroups;
			}
			
			groups = new ArrayList<Group>(topGroups);
		}
		
		if (ids == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		
		List<String> userGroups = helper.getUserGroupsIds(iwc, user);
		
		Group group = null;
		String groupId = null;
		StringBuffer action = null;
		boolean checkGroup = false;
		String minusOne = "-1";
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			
			checkGroup = false;
			//	Layer
			Layer selectedGroup = new Layer();
			selectedGroups.add(selectedGroup);
				
			//	Checkbox
			groupId = group.getId() == null ? CoreConstants.EMPTY : group.getId();
			CheckBox selectGroup = new CheckBox(group.getName(), groupId);
			if (minusOne.equals(selectedGroupId) && userGroups.size() == 0 && i == 0) {
				checkGroup = true;
			}
			else {
				if (groupId.equals(selectedGroupId) || userGroups.contains(groupId)) {
					checkGroup = true;
				}
			}
			if (checkGroup) {
				selectGroup.setChecked(true, true);
			}
			action = new StringBuffer("deselectUserFromGroup(").append(getJavaScriptParameter(groupId)).append(");");
			selectGroup.setOnClick(action.toString());
			ids.add(selectGroup.getId());
			selectedGroup.add(selectGroup);
			
			//	Label
			selectedGroup.add(new Text(group.getName() == null ? CoreConstants.EMPTY : group.getName()));
		}
		
		return selectedGroups;
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		return iwc.getApplicationContext().getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
	}
	
	private void addLabelForNoGroups(IWContext iwc, Layer container) {
		IWResourceBundle iwrb = null;
		String text = "There are no groups available";
		try {
			iwrb = getResourceBundle(iwc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (iwrb == null) {
			container.add(new Text(text));
		}
		else {
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", text)));
		}
	}
	
	protected String getJavaScriptParameter(String parameter) {
		if (parameter == null) {
			return "null";
		}
		return new StringBuffer("'").append(parameter).append("'").toString();
	}
	
	protected String getActionForAddUserView(SimpleUserPropertiesBean bean, String userId) {
		StringBuffer action = new StringBuffer("addUserPresentationObject('").append(bean.getInstanceId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getContainerId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getParentGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getMessage()).append("', ");
		action.append(getJavaScriptParameter(bean.getDefaultGroupId())).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(userId)).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(bean.getGroupTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getRoleTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isGetParentGroupsFromTopNodes());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getGroupTypesForParentGroups()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isUseChildrenOfTopNodesAsParentGroups());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isAllFieldsEditable());
		action.append(");");
		return action.toString();
	}
	
	protected String getJavaScriptFunctionParameter(List<String> parameters) {
		if (parameters == null || parameters.size() == 0) {
			return "null";
		}
		
		StringBuffer params = new StringBuffer("[");
	
		for (int i = 0; i < parameters.size(); i++) {
			params.append(getJavaScriptParameter(parameters.get(i)));
			if (i + 1 < parameters.size()) {
				params.append(SimpleUserApp.COMMA_SEPARATOR);
			}
		}
	
		params.append("]");
		return params.toString();
	}
	
	@SuppressWarnings("unchecked")
	public Layer getRolesEditor(IWContext iwc, int groupId, boolean addInput) {
		if (iwc == null) {
			return null;
		}
		Group group = null;
		if (groupId != -1) {
			try {
				group = ((GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class)).getGroupByGroupID(groupId);
			} catch (IBOLookupException e) {
			} catch (RemoteException e) {
			} catch (FinderException e) {}
		}
		
		Layer container = new Layer();
		container.setStyleClass("groupRolesStyleClass");
		Layer rolesContainer = new Layer();
		container.add(rolesContainer);
		rolesContainer.setStyleClass("checkboxesForGroupRoleEditorStyleClass");
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		String message = iwrb.getLocalizedString("saving", "Saving...");
		
		AccessController accessControler = iwc.getAccessController();
		List<ICRole> allRoles = getAllRolesWithoutMasterRole(accessControler);
		boolean addTable = true;
		if (allRoles == null || allRoles.isEmpty()) {
			rolesContainer.add(new Heading1(iwrb.getLocalizedString("no_roles", "There are no roles...")));
			addTable = false;
		}
		
		if (group == null) {
			rolesContainer.add(new Heading3(iwrb.getLocalizedString("create_new_group", "Create new group")));
		}
		else {
			rolesContainer.add(new Heading3(iwrb.getLocalizedString("groupownerswindow.setting_roles_for_group", "Setting roles for ") + group.getName()));
			
			Collection<ICPermission> permissionsForCurrentGroup = accessControler.getAllRolesWithRolePermissionsForGroup(group);
			List<String> permissions = Arrays.asList(new String[] {/*AccessController.PERMISSION_KEY_VIEW, AccessController.PERMISSION_KEY_EDIT,
					AccessController.PERMISSION_KEY_CREATE, AccessController.PERMISSION_KEY_DELETE, */AccessController.PERMISSION_KEY_ROLE});
			List<String> roles = getRolesNotIncludedOriginaly(permissionsForCurrentGroup, allRoles);
			
			Table2 rolesTable = new Table2();
			if (addTable) {
				rolesContainer.add(rolesTable);
			}
			
			TableRowGroup headerGroup = rolesTable.createHeaderRowGroup();
			TableRow headerRow = headerGroup.createRow();
			TableCell2 cell = headerRow.createHeaderCell();
			cell.add(new Text(iwrb.getLocalizedString("role", "Role")));
			for (String permission: permissions) {
				cell = headerRow.createHeaderCell();
				cell.add(new Text(iwrb.getLocalizedString(permission, permission)));
			}
			
			TableRowGroup bodyRows = rolesTable.createBodyRowGroup();
			for (ICRole role: allRoles) {
				addRowAndCellsForRole(groupId, role.getNodeName(), role.getRoleKey(), bodyRows, iwrb, permissions, permissionsForCurrentGroup, message);
			}
			for (String role: roles) {
				addRowAndCellsForRole(groupId, role, role, bodyRows, iwrb, permissions, permissionsForCurrentGroup, message);
			}
		}
		
		if (addInput) {
			Layer newRoleContainer = new Layer();
			container.add(newRoleContainer);
			TextInput newRoleInput = new TextInput();
			newRoleInput.setStyleClass("addNewRoleInputStyleClass");
			newRoleInput.setMarkupAttribute("groupid", groupId);
			StringBuilder action = new StringBuilder("addNewRoleKey(event, '").append(newRoleInput.getId()).append(SimpleUserApp.PARAMS_SEPARATOR);
			action.append(rolesContainer.getId()).append(SimpleUserApp.PARAMS_SEPARATOR).append(message).append("');");
			newRoleInput.setOnKeyUp(action.toString());
			Label newRoleLabel = new Label(iwrb.getLocalizedString("groupownerswindow.new_role", "New role key:"), newRoleInput);
			newRoleContainer.add(newRoleLabel);
			newRoleContainer.add(newRoleInput);
		}
		
		return container;
	}
	
	private void addRowAndCellsForRole(int groupId, String roleName, String roleKey, TableRowGroup bodyRows, IWResourceBundle iwrb, List<String> permissions,
			Collection<ICPermission> permissionsForCurrentGroup, String message) {
		TableRow bodyRow = bodyRows.createRow();
		
		TableCell2 cell = bodyRow.createCell();
		cell.add(new Text(iwrb.getLocalizedString(roleKey, roleName)));
		
		StringBuilder action = null;
		String attribute = "groupid";
		String checkBoxStyle = "changePermissionForRoleCheckboxStyle";
		for (String permissionKey: permissions) {
			cell = bodyRow.createHeaderCell();
			CheckBox checkBox = new CheckBox(permissionKey, roleKey);
			checkBox.setChecked(isRoleChecked(permissionKey, roleKey, permissionsForCurrentGroup));
			checkBox.setStyleClass(checkBoxStyle);
			checkBox.setMarkupAttribute(attribute, groupId);
			action = new StringBuilder("changePermissionValueForRole('").append(checkBox.getId()).append(SimpleUserApp.PARAMS_SEPARATOR).append(message).append("');");
			checkBox.setOnClick(action.toString());
			cell.add(checkBox);
		}
	}
	
	private List<String> getRolesNotIncludedOriginaly(Collection<ICPermission> permissionsForGroup, List<ICRole> originalRoles) {
		List<String> roles = new ArrayList<String>();
		
		if (permissionsForGroup == null || permissionsForGroup.isEmpty()) {
			return roles;
		}
		
		List<String> originalRolesKeys = new ArrayList<String>();
		for (ICRole role: originalRoles) {
			originalRolesKeys.add(role.getRoleKey());
		}
		
		String roleKey = null;
		for (ICPermission permission: permissionsForGroup) {
			roleKey = permission.getPermissionString();
			if (roleKey != null && !originalRolesKeys.contains(roleKey)) {
				roles.add(roleKey);
			}
		}
		
		return roles;
	}
	
	private boolean isRoleChecked(String permissionKey, String roleKey, Collection<ICPermission> allPermissions) {
		if (permissionKey == null || roleKey == null ||allPermissions == null || allPermissions.isEmpty()) {
			return false;
		}
		
		ICPermission permission = null;
		String permissionType = null;
		String roleType = null;
		for (Iterator<ICPermission> it = allPermissions.iterator(); it.hasNext();) {
			permission = it.next();
			
			permissionType = permission.getContextValue();
			roleType = permission.getPermissionString();
			if (permissionKey.equals(permissionType) && roleKey.equals(roleType)) {
				return permission.getPermissionValue();
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private List<ICRole> getAllRolesWithoutMasterRole(AccessController accessControler) {
		Collection<ICRole> allRoles = accessControler.getAllRoles();
        if (allRoles == null || allRoles.isEmpty()) {
        	return null;
        }

        List<ICRole> roles = new ArrayList<ICRole>();
        ICRole role = null;
        for (Iterator<ICRole> it = allRoles.iterator(); it.hasNext();) {
        	role = it.next();
        	if (!role.getRoleKey().equals(AccessController.PERMISSION_KEY_ROLE_MASTER)) {
        		roles.add(role);
        	}
        }
        return roles;
	}
	
}
