package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.FinderException;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.CheckBoxConverter;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.*;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;
import com.idega.util.ListUtil;

/**
 * Description: An editor window for the selected groups roles <br>Company: Idega Software <br>Copyright: Idega Software 2003 <br>
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 */
public class GroupRolesWindow extends StyledIWAdminWindow {
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID;
	private static final String PARAM_SAVING = "grw_save";
	private static final String PARAM_NEW_ROLE = "grw_new_role_key";
	private static final String RECURSE_PERMISSIONS_TO_CHILDREN_KEY = "grw_recurse_ch_of_gr";
	private static final String CHANGE_ROLE_KEY = "grw_ch_gr_role_status";
	private static final String SESSION_PARAM_ROLES_BEFORE_SAVE = "grw_roles_b_s";
	
	private static final String HELP_TEXT_KEY = "group_roles_window";
	
	private GroupBusiness groupBiz = null;
	
	private boolean saveChanges = false;
	
	protected int width = 640;
	protected int height = 480;
	
	private String selectedGroupId = null;
	
	private List permissionType;
	private IWResourceBundle iwrb = null;
	private UserBusiness userBiz = null;
	
	private String mainStyleClass = "main";
	private Group selectedGroup;
	
	/**
	 * Constructor for GroupRolesWindow.
	 */
	public GroupRolesWindow() {
		super();
		
		setWidth(width);
		setHeight(height);
		setScrollbar(true);
		setResizable(true);
		
	}
	/**
	 * Constructor for GroupRolesWindow.
	 * 
	 * @param name
	 */
	public GroupRolesWindow(String name) {
		super(name);
	}
	/**
	 * Constructor for GroupRolesWindow.
	 * 
	 * @param width
	 * @param heigth
	 */
	public GroupRolesWindow(int width, int heigth) {
		super(width, heigth);
	}
	/**
	 * Constructor for GroupRolesWindow.
	 * 
	 * @param name
	 * @param width
	 * @param height
	 */
	public GroupRolesWindow(String name, int width, int height) {
		super(name, width, height);
	}
	
	public void main(IWContext iwc) throws Exception {
		iwrb = this.getResourceBundle(iwc);
		AccessController access = iwc.getAccessController();
		
		parseAction(iwc);
		
		if (saveChanges) {
			saveChanges(iwc, access);
		}
		
		//get the data
		Collection rolesForTheSelectedGroup = access.getAllRolesForGroup(selectedGroup);
		setCurrentGroupsRolesInSession(iwc, rolesForTheSelectedGroup);
		Collection allRoles = getAllRolesWithoutRoleMasterRole(access);
		
		
		
		
		EntityBrowser browser = new EntityBrowser();
		browser.setEntities("grw_" + selectedGroupId, allRoles);
		browser.setDefaultNumberOfRows(allRoles.size());
		browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
		browser.setWidth(browser.HUNDRED_PERCENT);
		browser.setUseExternalForm(true);
		
		//	fonts
		Text columnText = new Text();
		columnText.setBold();
		browser.setColumnTextProxy(columnText);
		
		//		set color of rows
		browser.setColorForEvenRows("#FFFFFF");
		browser.setColorForOddRows(IWColor.getHexColorString(246, 246, 247));
		
		int column = 1;
		String nameKey = "Role";
		
		EntityToPresentationObjectConverter converterLink = new EntityToPresentationObjectConverter() {
			private com.idega.core.user.data.User administrator = null;
			private boolean loggedInUserIsAdmin;
			
			public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
				return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			}
			
			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
				
				//TODO add localized stuff like description
				//also this does not need a converted just the right entity path
				ICRole role = (ICRole) entity;
				
				return new Text(role.getRoleKey());
				
			}
		};
		browser.setMandatoryColumnWithConverter(column++, nameKey, converterLink);
		
		//
		CheckBoxConverter recurseCheckBoxConverter = new CheckBoxConverter(RECURSE_PERMISSIONS_TO_CHILDREN_KEY) {
			
			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
				ICRole role = (ICRole) entity;
				
				String roleKey = role.getRoleKey();
				String checkBoxKey = path.getShortKey();
				CheckBox checkBox = new CheckBox(checkBoxKey, roleKey);
				
				return checkBox;
				
			}
			
		};
		
		recurseCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, RECURSE_PERMISSIONS_TO_CHILDREN_KEY, recurseCheckBoxConverter);
		
		//converter ends
		
		//
		CheckBoxConverter isActiveCheckBoxConverter = new CheckBoxConverter(CHANGE_ROLE_KEY) {
			
			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
				ICRole role = (ICRole) entity;
				
				String roleKey = role.getRoleKey();
				List groupsCurrentRolesKeys = (List) iwc.getSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + selectedGroupId);
				
				String checkBoxKey = path.getShortKey();
				CheckBox checkBox = new CheckBox(checkBoxKey, roleKey);
				if (groupsCurrentRolesKeys != null && !groupsCurrentRolesKeys.isEmpty() && groupsCurrentRolesKeys.contains(roleKey)) {
					checkBox.setChecked(true);
				}
				
				return checkBox;
				
			}
			
		};
		
		isActiveCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, CHANGE_ROLE_KEY, isActiveCheckBoxConverter);
		
		//converter ends
		
		Form form = getGroupPermissionForm(browser);
		form.add(new HiddenInput(PARAM_SELECTED_GROUP_ID, selectedGroupId));
		form.add(new HiddenInput(PARAM_SAVING, "TRUE"));
		//cannot use this if we put in a navigator in the entitybrowser, change submit button to same value
		add(form, iwc);
		
	}
	
	protected Collection getAllRolesWithoutRoleMasterRole(AccessController access) {
		Collection allRoles = access.getAllRoles();
		if(allRoles!=null && !allRoles.isEmpty()){
			List roles = new Vector();
	
			Iterator allIter = allRoles.iterator();
			while (allIter.hasNext()) {
				ICRole role = (ICRole) allIter.next();
				if(!role.getRoleKey().equals(access.PERMISSION_KEY_ROLE_MASTER)){
					roles.add(role);
				}
			}
			
			return roles;
		}
		
		
		return allRoles;
	}
	protected void saveChanges(IWContext iwc, AccessController access) {
		List groupsCurrentRoleKeys = (List) iwc.getSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + selectedGroupId);
		Collection allRoles = getAllRolesWithoutRoleMasterRole(access);
		List rolesToAddOrKeepForGroup = CheckBoxConverter.getResultByParsing(iwc, CHANGE_ROLE_KEY);
		List rolesToRecurseToChildren = CheckBoxConverter.getResultByParsing(iwc, RECURSE_PERMISSIONS_TO_CHILDREN_KEY);
				
		
		try {
			
			//set or remove roles from selected group
			if (iwc.isParameterSet(CHANGE_ROLE_KEY)) {
				
				if (rolesToAddOrKeepForGroup != null && !rolesToAddOrKeepForGroup.isEmpty()) {
					Iterator rolesToAdd = rolesToAddOrKeepForGroup.iterator();
					while (rolesToAdd.hasNext()) {
						String roleKey = (String) rolesToAdd.next();
						
						if (!groupsCurrentRoleKeys.contains(roleKey)) { //otherwise no need to add
							access.addRoleToGroup(roleKey, selectedGroup, iwc);
						}
						
						//do we add the same to this groups children
						if (rolesToRecurseToChildren != null && rolesToRecurseToChildren.contains(roleKey)) { //recurse to children
							Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
							if (children != null && !children.isEmpty()) {
								Iterator childIter = children.iterator();
								while (childIter.hasNext()) {
									Group childGroup = (Group) childIter.next();
									access.addRoleToGroup(roleKey, childGroup, iwc);
								}
							}
							
						}
					}
				}
			}
			
			//find all roles that need to be removed by removing the ones that where just added or kept
			if (rolesToAddOrKeepForGroup != null) {
				groupsCurrentRoleKeys.removeAll(rolesToAddOrKeepForGroup);
			}
			
			//			roles to remove from this group
			Iterator rolesToRemove = groupsCurrentRoleKeys.iterator();
			while (rolesToRemove.hasNext()) {
				String roleKey = (String) rolesToRemove.next();
				access.removeRoleFromGroup(roleKey, selectedGroup, iwc);
			}
			
			//a special case when this group does not have a certain role but we want to remove
			//a role from all its children anyway needed this implementation
			//do we remove the same role from this groups children
			
			if(allRoles!=null && !allRoles.isEmpty() && rolesToRecurseToChildren != null){
				if(rolesToAddOrKeepForGroup==null) rolesToAddOrKeepForGroup = ListUtil.getEmptyList();
				Iterator allIter = allRoles.iterator();
				while (allIter.hasNext()) {
					ICRole role = (ICRole) allIter.next();
					String rKey = role.getRoleKey();
					if ( rolesToRecurseToChildren.contains(rKey)  && !rolesToAddOrKeepForGroup.contains(rKey) ) { //recurse to children
						Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
						if (children != null && !children.isEmpty()) {
							Iterator childIter = children.iterator();
							while (childIter.hasNext()) {
								Group childGroup = (Group) childIter.next();
								access.removeRoleFromGroup(rKey, childGroup, iwc);
							}
						}
						
					}
					
				}
				
			}
			
			//add a new role
			String newRoleKey = iwc.getParameter(PARAM_NEW_ROLE);
			
			if (newRoleKey != null && !newRoleKey.equals("")) {
				access.createRoleWithRoleKey(newRoleKey);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setCurrentGroupsRolesInSession(IWContext iwc, Collection rolesForTheSelectedGroup) {
		List roleKeysForSelectedGroup = new ArrayList();
		
		if (rolesForTheSelectedGroup != null && !rolesForTheSelectedGroup.isEmpty()) {
			
			Iterator iter = rolesForTheSelectedGroup.iterator();
			while (iter.hasNext()) {
				ICPermission perm = (ICPermission) iter.next();
				roleKeysForSelectedGroup.add(perm.getPermissionString());
			}
			
			iwc.setSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + selectedGroupId, roleKeysForSelectedGroup);
			
		}
		else {
			iwc.setSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + selectedGroupId, roleKeysForSelectedGroup);
		}
		
	}
	/**
	 * Method addGroupPermissionForm.
	 * 
	 * @param iwc
	 */
	private Form getGroupPermissionForm(EntityBrowser browser) throws Exception {
		
		Help help = getHelp(HELP_TEXT_KEY);
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"));
		save.setSubmitConfirm(iwrb.getLocalizedString("change.selected.permissions?", "Change selected permissions?"));
		
		SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"));
		close.setOnClick("window.close()");
		
		Table table = new Table(2, 3);
		table.setRowHeight(1, "20");
		table.setStyleClass(mainStyleClass);
		table.mergeCells(1, 2, 2, 2);
		
		table.add(
				new Text(
						iwrb.getLocalizedString("groupownerswindow.setting_roles_for_group", "Setting roles for ") + selectedGroup.getName(),
						true,
						false,
						false),
				1,
				1);
		
		table.add(browser, 1, 2);
		table.addBreak(1, 2);
		table.add(new Text(iwrb.getLocalizedString("groupownerswindow.new_role", "New role key : "), true, false, false), 1, 2);
		table.add(new TextInput(PARAM_NEW_ROLE), 1, 2);
		
		table.setVerticalAlignment(1, 3, "bottom");
		table.setVerticalAlignment(2, 3, "bottom");
		table.add(help, 1, 3);
		table.add(save, 2, 3);
		table.add(Text.NON_BREAKING_SPACE, 2, 3);
		table.add(close, 2, 3);
		table.setWidth(600);
		table.setHeight(410);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setAlignment(2, 3, Table.HORIZONTAL_ALIGN_RIGHT);
		
		Form form = new Form();
		form.add(table);
		
		return form;
	}
	
	private void parseAction(IWContext iwc) throws RemoteException {
		selectedGroupId = iwc.getParameter(GroupRolesWindow.PARAM_SELECTED_GROUP_ID);
		saveChanges = iwc.isParameterSet(PARAM_SAVING);
		
		try {
			selectedGroup = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(selectedGroupId));
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("grouproleswindow.title", "Group roles");
	}
	
	public GroupBusiness getGroupBusiness(IWContext iwc) {
		if (groupBiz == null) {
			
			try {
				groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			
		}
		
		return groupBiz;
	}
	
	/**
	 * @see com.idega.presentation.PresentationObject#getName()
	 */
	public String getName() {
		return "Group roles";
	}
	
	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBiz == null) {
			try {
				userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return userBiz;
	}
	
}
