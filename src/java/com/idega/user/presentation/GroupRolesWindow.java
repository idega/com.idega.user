package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.CheckBoxConverter;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
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
    
    protected int width = 750;
    protected int height = 550;
    
    private String selectedGroupId = null;
    private Integer integerSelectedGroupId = null;
    
    private List permissionType;
    private IWResourceBundle iwrb = null;
    private UserBusiness userBiz = null;
    
    private String mainStyleClass = "main";
    private Group selectedGroup;
    
    private List permissionTypes;
    private AccessController access;
    
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
        access = iwc.getAccessController();
        addTitle(iwrb.getLocalizedString("grouproleswindow.title", "Group Roles Window"), TITLE_STYLECLASS);

        parseAction(iwc);
        
        if (saveChanges) {
            saveChanges(iwc, access);
        }
        
        //get the data
        Collection rolesForTheSelectedGroup = access.getAllRolesWithRolePermissionsForGroup(selectedGroup);
        //setCurrentGroupsRolesInSession(iwc, rolesForTheSelectedGroup);
        
        Collection permissionsForBrowser = orderAndGroupPermissionsByPermissionString(rolesForTheSelectedGroup);
        
        
        //setup the data viewer
        EntityBrowser browser = EntityBrowser.getInstanceUsingEventSystemAndExternalForm();
        browser.setEntities("grw_" + selectedGroupId, permissionsForBrowser);
        browser.setDefaultNumberOfRows(permissionsForBrowser.size());
        browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
        browser.setWidth(browser.HUNDRED_PERCENT);
        
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
            
            public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {
                
                //TODO add localized stuff like description
                //also this does not need a converted just the right entity path
                String roleName = null;
                
                Iterator iter = ((Collection)permissions).iterator();
                while (iter.hasNext()) {
                    
                    ICPermission perm = (ICPermission)iter.next();
                    roleName = perm.getPermissionString();
                    
                    break;
                }
                
                
                return new Text(iwrb.getLocalizedString(roleName,roleName));
                
            }
        };
        browser.setMandatoryColumnWithConverter(column++, nameKey, converterLink);
        
        
//      define checkbox button converter class
        EntityToPresentationObjectConverter permissionTypeConverter = new EntityToPresentationObjectConverter() {
            
            private com.idega.core.user.data.User administrator = null;
            private boolean loggedInUserIsAdmin;
            
            //called when going between subsets
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                getPermissionMapFromSession(iwc, entityPath.getShortKey(), true); //zero the map
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {
                boolean isSet = false;
                String permissionKey = path.getShortKey();
                //final int selectedId = Integer.parseInt(selectedGroupId);
                //String groupId = null;
                String permissionType = null;
                String roleKey = null;
                
                
                Collection col = (Collection) permissions;
                Iterator iterator = col.iterator();
                
                //here we add to the permission map in session for saving purposes
                Map permissionMap = getPermissionMapFromSession(iwc, permissionKey, false);
                
                while (iterator.hasNext() && !isSet) {
                    
                    ICPermission perm = (ICPermission)iterator.next();
                    roleKey = perm.getPermissionString();
                    permissionType = perm.getContextValue();//stored in contextvalue instead of permissionstring
                    roleKey = perm.getPermissionString();
                    
                    if(permissionKey.equals(permissionType) && perm.getPermissionValue()) {
                        isSet = true;
                        permissionMap.put(roleKey, perm);
                    }
                    
                    
                    
                }
                
                
                CheckBox checkBox = new CheckBox(permissionKey, roleKey);
                checkBox.setChecked(isSet);
                
                //todo add check to see if the current user has permissionKey permission to this group
                //if(iwc.getAccessController().hasPermitPermissionFor())
                
                return checkBox;
                
            }
        };
        
        Iterator iterator = permissionTypes.iterator();
        
        //add the view,edit,delete,create
        while (iterator.hasNext()) {
            String type = (String) iterator.next();
            browser.setMandatoryColumn(column++, type);
            browser.setEntityToPresentationConverter(type, permissionTypeConverter);
        }
        
        //
        CheckBoxConverter recurseCheckBoxConverter = new CheckBoxConverter(RECURSE_PERMISSIONS_TO_CHILDREN_KEY) {
            
            public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {
                String roleKey = null;
                
                
                Iterator iter = ((Collection)permissions).iterator();
                while (iter.hasNext()) {
                    
                    ICPermission perm = (ICPermission)iter.next();
                    roleKey = perm.getPermissionString();
                    
                    break;
                }
                
                
                String checkBoxKey = path.getShortKey();
                CheckBox checkBox = new CheckBox(checkBoxKey, roleKey);
                
                return checkBox;
                
            }
            
        };
        
        recurseCheckBoxConverter.setShowTitle(true);
        browser.setMandatoryColumnWithConverter(column++, RECURSE_PERMISSIONS_TO_CHILDREN_KEY, recurseCheckBoxConverter);
        
        //converter ends
        
        
        
        //
        /*
         CheckBoxConverter isActiveCheckBoxConverter = new CheckBoxConverter(CHANGE_ROLE_KEY) {
         
         public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {
         String roleKey = null;
         
         Iterator iter = ((Collection)permissions).iterator();
         while (iter.hasNext()) {
         ICPermission perm = (ICPermission)iter.next();
         roleKey = perm.getPermissionString();
         break;
         }
         
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
         */
        
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
        //List groupsCurrentRoleKeys = (List) iwc.getSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + selectedGroupId);
        List rolesToRecurseToChildren = CheckBoxConverter.getResultByParsing(iwc, RECURSE_PERMISSIONS_TO_CHILDREN_KEY);
        
        
        try {
            
//          iterate for each permission key, view, edit etc.
            Iterator iterator = permissionTypes.iterator();
            
            while (iterator.hasNext()) {
                //permission key, view, edit etc.
                String permissionKey = (String) iterator.next();
                //group ids for this key
                String[] roles = iwc.getParameterValues(permissionKey);
                //get a map of permissions by groups and key that the selected group has BEFORE THE SAVE
                //then we remove the ones we add from the list and the rest are those we need to remove! smart eh?
                Map permissions = this.getPermissionMapFromSession(iwc, permissionKey, false);
                
                if (roles != null && roles.length > 0) {
                    
                    //add stuff
                    for (int i = 0; i < roles.length; i++) {
                        String roleKey = roles[i];
                        //                  
                        //adding
                        access.addRoleToGroup(roleKey, permissionKey, integerSelectedGroupId , iwc);
                        //todo add for children
//                      do we add the same to this groups children
                        if (rolesToRecurseToChildren != null && rolesToRecurseToChildren.contains(roleKey)) { //recurse to children
                            Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
                            if (children != null && !children.isEmpty()) {
                                Iterator childIter = children.iterator();
                                while (childIter.hasNext()) {
                                    Group childGroup = (Group) childIter.next();
                                    access.addRoleToGroup(roleKey,permissionKey, (Integer)childGroup.getPrimaryKey(), iwc);
                                }
                            }
                            
                        }
                        
                        permissions.remove(roleKey);
                        
                    }
                }
                    
                    //remove
                    //                  removing (setting to false) permissions
                    Iterator entries = permissions.values().iterator();
                    while (entries.hasNext()) {	
                        ICPermission permission = (ICPermission) entries.next();
                        String roleKey = permission.getPermissionString();
                        
                        access.removeRoleFromGroup(roleKey, permissionKey, integerSelectedGroupId , iwc);
                        
                        //todo remove for children
                        //removeInheritedPermissionFromChildGroups(iwc, key, permission);
                        if (rolesToRecurseToChildren != null && rolesToRecurseToChildren.contains(roleKey)) { //recurse to children
                            Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
                            if (children != null && !children.isEmpty()) {
                                Iterator childIter = children.iterator();
                                while (childIter.hasNext()) {
                                    Group childGroup = (Group) childIter.next();
                                    access.removeRoleFromGroup(roleKey,permissionKey, (Integer)childGroup.getPrimaryKey(), iwc);
                                }
                            }
                            
                        }
                        
                    }
                    
                    
                    
                
                
            }
            
            /*
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
                    
                    */
            
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
    /*
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
        
    }*/
    
    
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
        
        Table mainTable = new Table();
	    		mainTable.setWidth(600);
	    		mainTable.setHeight(410);
	    		mainTable.setCellpadding(0);
	    		mainTable.setCellspacing(0);
	        
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
        
        Table bottomTable = new Table();
	    		bottomTable.setCellpadding(0);
	    		bottomTable.setCellspacing(5);
	    		bottomTable.setWidth(Table.HUNDRED_PERCENT);
	    		bottomTable.setHeight(39);
	    		bottomTable.setStyleClass(mainStyleClass);
	    		bottomTable.add(help,1,1);
	    		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
	    		bottomTable.add(save, 2, 1);
	    		bottomTable.add(Text.NON_BREAKING_SPACE, 2, 1);
	    		bottomTable.add(close, 2, 1);

	    		table.setWidth(Table.HUNDRED_PERCENT);
        table.setHeight(370);
        table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
        table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
        
        mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
        mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
        mainTable.add(table,1,1);
        mainTable.add(bottomTable,1,3);
        Form form = new Form();
        form.add(mainTable);
        
        return form;
    }
    
    private void parseAction(IWContext iwc) throws RemoteException {
        selectedGroupId = iwc.getParameter(GroupRolesWindow.PARAM_SELECTED_GROUP_ID);
        saveChanges = iwc.isParameterSet(PARAM_SAVING);
        integerSelectedGroupId = new Integer(selectedGroupId);
        permissionTypes = getAllPermissionTypes();
        try {
            selectedGroup = getGroupBusiness(iwc).getGroupByGroupID(integerSelectedGroupId.intValue());
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
    
    ////////////////////
    //added
    
    /**
     * Gets all the permissiontypes (e.g. read/write) from the collection of ICPermissions from the permissionString column.
     * 
     * @param permissions
     * @return List
     */
    protected List getAllPermissionTypes() {
        
        if(permissionTypes == null ) {
            permissionTypes = new ArrayList();
            
            permissionTypes.add(0, "view");
            permissionTypes.add(1, "edit");
            permissionTypes.add(2, "create");
            permissionTypes.add(3, "delete");
            //	permissionTypes.add(4, "permit");//the permission to give others permissions
            permissionTypes.add(4,"role_permission");//is active flag
        }
        
        return permissionTypes;
        
    }
    
    protected Map getPermissionMapFromSession(IWContext iwc, String permissionKey, boolean emptyMap) {
        Map map = (Map) iwc.getSessionAttribute(this.SESSION_PARAM_ROLES_BEFORE_SAVE + permissionKey);
        
        if (map == null || emptyMap) {
            map = new HashMap();
            iwc.setSessionAttribute(SESSION_PARAM_ROLES_BEFORE_SAVE + permissionKey, map);
        }
        return map;
        
    }
    
    
    /**
     * Method orderAndGroupPermissionsByPermissionString orders by groupId and returns the permissions as a collection of collections.
     * 
     * @param iwc
     * @return Collection
     */
    private List orderAndGroupPermissionsByPermissionString(Collection allPermissions) {
        
        Iterator iter = allPermissions.iterator();
        
        
        //order the permissions by the groupId and create a List for each one.
        Map map = new HashMap();
        List finalCollection = new ArrayList();
        Collection allRoles = getAllRolesWithoutRoleMasterRole(access);
        
        
        //this is needed to get the roles the group does not have to display
        //role key placeholder
        //hack
        if(allRoles!=null && !allRoles.isEmpty()) {
            Iterator iterator = allRoles.iterator();
            while (iterator.hasNext()) {
                ICRole role = (ICRole) iterator.next();
                List rolesList = new ArrayList();
                try {
                    ICPermission perm = AccessControl.getPermissionHome().create();
                    perm.setPermissionString(role.getRoleKey());
                    //could not do this because of entitybrowser bug,rolesList.add(role.getRoleKey());
                    rolesList.add(perm);
                    map.put(role.getRoleKey(),rolesList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (CreateException ex) {
                    ex.printStackTrace();
                }
                
            }
        }
        
        
        String roleKey;
        
        while (iter.hasNext()) {
            ICPermission perm = (ICPermission) iter.next();
            roleKey = perm.getPermissionString();
            List list= (List) map.get(roleKey);
            
            if (list == null) {
                list = new ArrayList();
            }
            
            list.add(perm);
            
            map.put(roleKey, list);
            
        }
        
        finalCollection = com.idega.util.ListUtil.convertCollectionToList(map.values());
        
        return finalCollection;
        
    }
    
}
