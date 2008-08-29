package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.FinderException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
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
import com.idega.user.presentation.GroupMembersListViewer;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class SimpleUserAppHelper {

	public GroupMembersListViewer getMembersList(SimpleUserPropertiesBean bean, String image, String containerId, boolean checkIds) {
		GroupMembersListViewer list = new GroupMembersListViewer();
		list.setContainerId(containerId);
		list.setBean(bean);
		list.setImage(image);
		list.setCheckIds(checkIds);
		return list;
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
		String styleClass = "selectSubGroupInSimpleUserAppCheckBoxStyle";
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			
			checkGroup = false;
			//	Layer
			Layer selectedGroup = new Layer();
			selectedGroups.add(selectedGroup);
				
			//	Checkbox
			groupId = group.getId() == null ? CoreConstants.EMPTY : group.getId();
			CheckBox selectGroup = new CheckBox(group.getName(), groupId);
			selectGroup.setStyleClass(styleClass);
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
		return ELUtil.getInstance().getBean(GroupHelper.class).getJavaScriptParameter(parameter);
	}
	
	protected String getActionForAddUserView(SimpleUserPropertiesBean bean, String userId) {
		return ELUtil.getInstance().getBean(GroupHelper.class).getActionForAddUserView(bean, userId);
	}
	
	protected String getJavaScriptFunctionParameter(List<String> parameters) {
		return ELUtil.getInstance().getBean(GroupHelper.class).getJavaScriptFunctionParameter(parameters);
	}
	
	@SuppressWarnings("unchecked")
	public Layer getRolesEditor(IWContext iwc, int groupId, boolean addInput, List<String> selectedRoles) {
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
		List<ICRole> allRoles = getFilteredRoles(getFilteredRolesByUser(iwc, accessControler), selectedRoles);
		boolean addTable = true;
		if (ListUtil.isEmpty(allRoles)) {
			rolesContainer.add(new Heading3(iwrb.getLocalizedString("no_roles", "There are no roles...")));
			return container;
		}
		
		if (group == null) {
			rolesContainer.add(new Heading3(iwrb.getLocalizedString("create_new_group", "Create new group")));
		}
		else {
			rolesContainer.add(new Heading3(iwrb.getLocalizedString("groupownerswindow.setting_roles_for_group", "Setting roles for ") + group.getName()));
			
			Collection<ICPermission> permissionsForCurrentGroup = accessControler.getAllRolesWithRolePermissionsForGroup(group);
			List<String> permissions = Arrays.asList(new String[] {/*AccessController.PERMISSION_KEY_VIEW, AccessController.PERMISSION_KEY_EDIT,
					AccessController.PERMISSION_KEY_CREATE, AccessController.PERMISSION_KEY_DELETE, */AccessController.PERMISSION_KEY_ROLE});
			List<String> roles = getRolesNotIncludedOriginaly(permissionsForCurrentGroup, allRoles, selectedRoles);
			
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
			action.append(rolesContainer.getId()).append(SimpleUserApp.PARAMS_SEPARATOR).append(message).append("', ");
			action.append(getJavaScriptFunctionParameter(selectedRoles)).append(");");
			newRoleInput.setOnKeyUp(action.toString());
			Label newRoleLabel = new Label(iwrb.getLocalizedString("groupownerswindow.new_role", "New role key:"), newRoleInput);
			newRoleContainer.add(newRoleLabel);
			newRoleContainer.add(newRoleInput);
		}
		
		return container;
	}
	
	public String getBeanAsParameters(SimpleUserPropertiesBean bean, String parentGroupChooserId, String childGroupChooserId, String message) {
		List<String> parameters = new ArrayList<String>();
		
		addParamaterToList(parameters, bean.getInstanceId());							//	0
		addParamaterToList(parameters, bean.getContainerId());							//	1
		addParamaterToList(parameters, StringUtil.isEmpty(childGroupChooserId) ? bean.getGroupChooserId() : childGroupChooserId);													//	2
		addParamaterToList(parameters, bean.getDefaultGroupId());						//	3
		addParamaterToList(parameters, bean.getGroupTypes());							//	4
		addParamaterToList(parameters, bean.getRoleTypes());							//	5
		addParamaterToList(parameters, StringUtil.isEmpty(message) ? bean.getMessage() : message);																						//	6
		addParamaterToList(parameters, StringUtil.isEmpty(parentGroupChooserId) ? bean.getParentGroupChooserId() : parentGroupChooserId);					//	7
		addParamaterToList(parameters, bean.getGroupTypesForParentGroups());			//	8
		parameters.add(String.valueOf(bean.isUseChildrenOfTopNodesAsParentGroups()));	//	9
		parameters.add(String.valueOf(bean.isAllFieldsEditable()));						//	10
		addParamaterToList(parameters, String.valueOf(bean.getParentGroupId()));		//	11
		parameters.add(String.valueOf(bean.isJuridicalPerson()));						//	12
		parameters.add(String.valueOf(bean.isSendMailToUser()));						//	13
		parameters.add(String.valueOf(bean.isChangePasswordNextTime()));				//	14
		parameters.add(String.valueOf(bean.isAllowEnableDisableAccount()));				//	15
		addParamaterToList(parameters, bean.getParentGroupId() == -1 ? null : String.valueOf(bean.getParentGroupId()));																	//	16
		
		return getJavaScriptFunctionParameter(parameters);
	}
	
	private void addParamaterToList(List<String> parameters, String parameter) {
		parameters.add(StringUtil.isEmpty(parameter) ? "null" : parameter);
	}
	
	private List<ICRole> getFilteredRoles(List<ICRole> allRoles, List<String> selectedRoles) {
		if (ListUtil.isEmpty(allRoles)) {
			return null;
		}
		if (ListUtil.isEmpty(selectedRoles)) {
			return allRoles;
		}
		
		List<ICRole> roles = new ArrayList<ICRole>();
		for (ICRole role: allRoles) {
			if (selectedRoles.contains(role.getRoleKey())) {
				roles.add(role);
			}
		}
		
		return roles;
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
			action = new StringBuilder("changePermissionValueForRole('").append(checkBox.getId()).append(SimpleUserApp.PARAMS_SEPARATOR).append(message)
			.append("');");
			checkBox.setOnClick(action.toString());
			cell.add(checkBox);
		}
	}
	
	private List<String> getRolesNotIncludedOriginaly(Collection<ICPermission> permissionsForGroup, List<ICRole> originalRoles, List<String> selectedRoles) {
		List<String> roles = new ArrayList<String>();
		
		List<String> originalRolesKeys = new ArrayList<String>();
		for (ICRole role: originalRoles) {
			originalRolesKeys.add(role.getRoleKey());
		}
		
		if (!ListUtil.isEmpty(permissionsForGroup)) {
			String roleKey = null;
			for (ICPermission permission: permissionsForGroup) {
				roleKey = permission.getPermissionString();
				if (roleKey != null && !originalRolesKeys.contains(roleKey)) {
					roles.add(roleKey);
				}
			}
		}
		
		if (!ListUtil.isEmpty(selectedRoles)) {
			for (String selectedRole: selectedRoles) {
				if (!roles.contains(selectedRole) && !originalRolesKeys.contains(selectedRole)) {
					roles.add(selectedRole);
				}
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
	private List<ICRole> getFilteredRolesByUser(IWContext iwc, AccessController accessControler) {
		Collection<ICRole> allRoles = accessControler.getAllRoles();
		if (ListUtil.isEmpty(allRoles)) {
        	return null;
        }
		
		if (iwc.isSuperAdmin()) {
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
        
        User user = null;
        try {
        	user = iwc.getCurrentUser();
        } catch(NotLoggedOnException e) {}
        if (user == null) {
        	return null;
        }
        
        Set userRoles = accessControler.getAllRolesForUser(user);
        if (ListUtil.isEmpty(userRoles)) {
        	return null;
        }
        
		Collection<String> rolesKeys = new ArrayList<String>();
        for (Iterator it = userRoles.iterator(); it.hasNext();) {
        	rolesKeys.add(it.next().toString());
        }
        List<ICRole> filteredRoles = new ArrayList<ICRole>();
        for (String roleKey: rolesKeys) {
        	for (ICRole generalRole: allRoles) {
        		if (roleKey.equals(generalRole.getRoleKey()) && !filteredRoles.contains(generalRole)) {        			
        			filteredRoles.add(generalRole);
        		}
        	}
        }
        
        return filteredRoles;
	}
	
}
