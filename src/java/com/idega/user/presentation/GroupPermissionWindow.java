package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.CheckBoxConverter;
import com.idega.block.help.presentation.Help;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupComparator;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;

/**
 * Description: An editor window for the selected groups permissions. <br>The diplayed list of groups contains the groups the selected group has
 * <br>permissions to and then the currentUsers owned groups. <br>The selected groups permission groups will be disabled if the currentUser <br>
 * does not own them. <br>Company: Idega Software <br>Copyright: Idega Software 2003 <br>
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 *  
 */
public class GroupPermissionWindow extends StyledIWAdminWindow { //implements StatefullPresentation{

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID; //todo remove when using event system
	private static final String PARAM_SAVING = "gpw_save";
	private static final String SESSION_PARAM_PERMISSIONS_BEFORE_SAVE = "gpw_permissions_b_s";
	private static final String RECURSE_PERMISSIONS_TO_CHILDREN_KEY = "gpw_recurse_ch_of_gr";
	private static final String PARAM_OVERRIDE_INHERITANCE = "gpw_over";
	
	private static final String HELP_TEXT_KEY = "group_permission_window";

	//private static final String PARA = "com.idega.user";

	private String mainStyleClass = "main";
	
	List groupIdsToRecurseChangesOn = null;

	private StatefullPresentationImplHandler stateHandler = null;
	private GroupBusiness groupBiz = null;

	private boolean saveChanges = false;

	protected int width = 670;
	protected int height = 545;

	private String selectedGroupId = null;

	private List permissionType;
	private IWResourceBundle iwrb = null;
	private Group selectedGroup;
	private boolean hasInheritedPermissions;
	private static final String PARAM_PERMISSIONS_SET_TO_CHILDREN = "gpw_inherit_perm";
	
	/**
	 * Constructor for GroupPermissionWindow.
	 */
	public GroupPermissionWindow() {
		super();

		/*
		 * stateHandler = new StatefullPresentationImplHandler(); stateHandler.setPresentationStateClass(GroupPermissionWindowPS.class);
		 * this.getLocation().setApplicationClass(GroupPermissionWindow.class);
		 */

		setWidth(width);
		setHeight(height);
		setScrollbar(true);
		setResizable(true);

	}
	/**
	 * Constructor for GroupPermissionWindow.
	 * 
	 * @param name
	 */
	public GroupPermissionWindow(String name) {
		super(name);
	}
	/**
	 * Constructor for GroupPermissionWindow.
	 * 
	 * @param width
	 * @param heigth
	 */
	public GroupPermissionWindow(int width, int heigth) {
		super(width, heigth);
	}
	/**
	 * Constructor for GroupPermissionWindow.
	 * 
	 * @param name
	 * @param width
	 * @param height
	 */
	public GroupPermissionWindow(String name, int width, int height) {
		super(name, width, height);
	}

	public void main(IWContext iwc) throws Exception {
		iwrb = this.getResourceBundle(iwc);
		addTitle(iwrb.getLocalizedString("group_permission_window", "Group Permission Window"), IWConstants.BUILDER_FONT_STYLE_TITLE);

		parseAction(iwc);


		//if this group has inherited permissions it cannot change them
		if (hasInheritedPermissions) {
			addInheritedPermissionsView(iwc);
		}
		else {

			//get permission, order and use entitybrowser
			Collection allPermissions = getAllPermissionForSelectedGroupAndCurrentUser(iwc);
			List permissionTypes = getAllPermissionTypes(allPermissions);

			if (saveChanges) {
				saveChanges(iwc, permissionTypes);
				//refetch
				allPermissions = getAllPermissionForSelectedGroupAndCurrentUser(iwc);
				permissionTypes = getAllPermissionTypes(allPermissions);
			}
			
			
			addPermissionsForm(iwc, allPermissions, permissionTypes);
			
			
		}

	}

	private void addInheritedPermissionsView(IWContext iwc) {
		
		Form form = new Form();
		Text cannotEdit = new Text(iwrb.getLocalizedString("group_permission_window.cannot_edit","You cannot edit this groups permissions."),true,false,false);
		
		Text cannotEdit2 = new Text(iwrb.getLocalizedString("group_permission_window.group_has_inherited_permissions","This group has inherited permissions from the group : "));
		
		Text permissionControlGroupName = new Text(selectedGroup.getPermissionControllingGroup().getName(),true,false,false);
		
		Table table = new Table(1, 3);
		table.setRowHeight(1,"20");
		table.setStyleClass(mainStyleClass);
		table.setWidth(620);
		table.setHeight(480);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_BOTTOM);
		table.setAlignment(1, 3, Table.HORIZONTAL_ALIGN_RIGHT);
		
		
		table.add(cannotEdit,1,1);
		table.add(cannotEdit2,1,2);
		table.add(permissionControlGroupName,1,2);
		
		SubmitButton override = new SubmitButton(iwrb.getLocalizedImageButton("group_permission_window.override", "Override inherited permissions"),PARAM_OVERRIDE_INHERITANCE,"true");
		SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"));
		close.setOnClick("window.close()");
		
		Link owners = new Link(iwrb.getLocalizedString("owner.button", "Owners"));
				owners.setWindowToOpen(GroupOwnersWindow.class);
				owners.setAsImageButton(true);
				owners.addParameter(PARAM_SELECTED_GROUP_ID, selectedGroupId);
				
		table.add(override,1,3);
		table.add(Text.NON_BREAKING_SPACE,1,3);
		table.add(owners,1,3);
		table.add(Text.NON_BREAKING_SPACE,1,3);
		table.add(close,1,3);
		
		form.add(table);
		add(form,iwc);
	}
	
	private void addPermissionsForm(IWContext iwc, Collection allPermissions, List permissionTypes) throws Exception {
		List entityList = orderAndGroupPermissionsByContextValue(allPermissions, iwc);
		GroupComparator groupComparator = new GroupComparator(iwc.getCurrentLocale());
		groupComparator.setObjectsAreICPermissions(true);
		groupComparator.setGroupBusiness(this.getGroupBusiness(iwc));
		Collections.sort(entityList, groupComparator); //sort alphabetically

		EntityBrowser browser = getEntityBrowser(permissionTypes, entityList);
		Form form = getGroupPermissionForm(browser);
		form.add(new HiddenInput(PARAM_SELECTED_GROUP_ID, selectedGroupId));
		
		
		
		add(form, iwc);
	}
	
	private EntityBrowser getEntityBrowser(List permissionTypes, List entityList) {
		EntityBrowser browser = new EntityBrowser();
		
		browser.setEntities("gpw_" + selectedGroupId, entityList);
		//browser.setDefaultNumberOfRows(entityCollection.size() );
		browser.setDefaultNumberOfRows(16);
		browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
		browser.setWidth(browser.HUNDRED_PERCENT);
		browser.setUseExternalForm(true);
		browser.setUseEventSystem(false);
		//disable top set browser
		browser.setShowNavigation(false, true);
		//		set color of rows
		browser.setColorForEvenRows("#FFFFFF");
		browser.setColorForOddRows(IWColor.getHexColorString(246, 246, 247));
		//	fonts
		Text columnText = new Text();
		columnText.setBold();
		browser.setColumnTextProxy(columnText);
		
		int column = 1;
		String groupIdColumn = "ICPermission.PERMISSION_CONTEXT_VALUE";
		String applyRecursively = "Recursively";
		//browser.setLeadingEntity("com.idega.core.accesscontrol.data.ICPermission");
		//browser.setMandatoryColumn(column,"com.idega.core.accesscontrol.data.ICPermission.GROUP_ID");

		
		//CONVERTERS
		// define groupname converter
		EntityToPresentationObjectConverter contextValueConverter = new EntityToPresentationObjectConverter() {

			private com.idega.core.user.data.User administrator = null;
			private boolean loggedInUserIsAdmin;

			public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
				return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			}

			public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {

				Collection col = (Collection) permissions;

				Iterator iterator = col.iterator();

				while (iterator.hasNext()) {
					ICPermission perm = (ICPermission) iterator.next();
					Group group;
					try {
						group = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(perm.getContextValue()));

						return new Text(getGroupBusiness(iwc).getNameOfGroupWithParentName(group));

					}
					catch (RemoteException e) {
						e.printStackTrace();
					}
					catch (FinderException ex) {
						ex.printStackTrace();
					}

				}

				return new Text("NO GROUP NAME");

			}
		};
		browser.setMandatoryColumn(column++, groupIdColumn);
		browser.setEntityToPresentationConverter(groupIdColumn, contextValueConverter);
		//converter ends

		// define checkbox button converter class
		EntityToPresentationObjectConverter permissionTypeConverter = new EntityToPresentationObjectConverter() {

			private com.idega.core.user.data.User administrator = null;
			private boolean loggedInUserIsAdmin;

			//called when going between subsets
			public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
				getPermissionMapFromSession(iwc, entityPath.getShortKey(), true); //zero the map
				return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			}

			public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {

				Collection col = (Collection) permissions;

				Iterator iterator = col.iterator();

				boolean active = false;
				boolean isSet = false;
				boolean isOwner = false;

				final String columnName = path.getShortKey();
				final String ownerType = "owner";

				Map permissionMap = getPermissionMapFromSession(iwc, columnName, false);

				String groupId = null;
				String permissionType = null;

				while (iterator.hasNext() && !isSet) {
					ICPermission perm = (ICPermission) iterator.next();
					groupId = perm.getContextValue();
					permissionType = perm.getPermissionString();

					isSet = columnName.equals(permissionType);
					if (!isOwner) { //isOwner is not always set if the group also has other permissions??
						isOwner = ownerType.equals(permissionType);
					}

					if (isSet) {
						active = perm.getPermissionValue();
						if (active) {
							permissionMap.put(groupId, perm);
						}
					}

				}

				PresentationObject returnObj = null;

				if (isSet || isOwner) {
					returnObj = new CheckBox(columnName, groupId);
					((CheckBox) returnObj).setChecked(active);
				}
				else {
					returnObj = new Text("");
				}

				return returnObj;

			}
		};

		Iterator iter = permissionTypes.iterator();

		while (iter.hasNext()) {
			String type = (String) iter.next();
			browser.setMandatoryColumn(column++, type);
			browser.setEntityToPresentationConverter(type, permissionTypeConverter);
		}
		
		
		CheckBoxConverter recurseCheckBoxConverter = new CheckBoxConverter(RECURSE_PERMISSIONS_TO_CHILDREN_KEY) {

			private com.idega.core.user.data.User administrator = null;

		
			public PresentationObject getPresentationObject(Object permissions, EntityPath path, EntityBrowser browser, IWContext iwc) {

				Collection col = (Collection) permissions;

				Iterator iterator = col.iterator();

				while (iterator.hasNext()) {
					ICPermission perm = (ICPermission) iterator.next();
				

						
						String checkBoxKey = path.getShortKey(); 
						CheckBox checkBox = new CheckBox(checkBoxKey, perm.getContextValue());
						
						return checkBox;
				

				}

				return new Text("");

			}
		};
		
		
		recurseCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, RECURSE_PERMISSIONS_TO_CHILDREN_KEY, recurseCheckBoxConverter);
	
		

		//converter ends

		return browser;
	}
	private void saveChanges(IWContext iwc, List permissionTypes) {
		AccessController access = iwc.getAccessController();
		
		try {
			checkForInheritanceChanges(iwc);
			
			
			Iterator iterator = permissionTypes.iterator();

			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String[] values = iwc.getParameterValues(key);
				Map permissions = this.getPermissionMapFromSession(iwc, key, false);

				//adding new values
				if (values != null && values.length > 0) {

					for (int i = 0; i < values.length; i++) {
						access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, selectedGroupId, values[i], key, Boolean.TRUE);
					
						
						if(groupIdsToRecurseChangesOn!=null && groupIdsToRecurseChangesOn.contains(new Integer(values[i]))){
							//recurse through children and give same rights
							Group parent = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(values[i]));
							Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
							if(children!=null && !children.isEmpty()){
								Iterator childIter = children.iterator();
								while (childIter.hasNext()) {
									Group childGroup = (Group) childIter.next();
									//only if current user owns the group
									if(iwc.isSuperAdmin() || access.isOwner(childGroup,iwc)){
										access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, selectedGroupId, childGroup.getPrimaryKey().toString(), key, Boolean.TRUE);
									}
								}
							}
						}
						
						permissions.remove(values[i]);
					
					
					
					}

				}

				//does not remove record only set the permission to false
				//todo remove if I am owner (see todo on owner stuff in this class)
				//AccessControl.removePermissionRecords(AccessController.CATEGORY_GROUP_ID,iwc, instanceId,(String)item, groupsToRemove);

				Iterator entries = permissions.values().iterator();
				while (entries.hasNext()) {	
					ICPermission permission = (ICPermission) entries.next();
				
					permission.setPermissionValue(false);
					permission.store();
					
					  
					 if(groupIdsToRecurseChangesOn!=null && groupIdsToRecurseChangesOn.contains(new Integer(permission.getContextValue()))){
						//recurse through children and remove same rights	
						Group parent = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(permission.getContextValue()));
						Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
						if(children!=null && !children.isEmpty()){
							Iterator childIter = children.iterator();
							while (childIter.hasNext()) {
								Group childGroup = (Group) childIter.next();
								
								access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, selectedGroupId, childGroup.getPrimaryKey().toString(), key, Boolean.FALSE);
							
							}
						}
						
					}
					
				}

			}

			//refresh permissions PermissionCacher.updatePermissions()
			iwc.getApplicationContext().removeApplicationAttribute("ic_permission_map_" + AccessController.CATEGORY_GROUP_ID);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void checkForInheritanceChanges(IWContext iwc) throws RemoteException {
		//inheritance stuff
		String inheritToChildren = iwc.getParameter(PARAM_PERMISSIONS_SET_TO_CHILDREN);
		if(inheritToChildren!=null && !selectedGroup.isPermissionControllingGroup()){//its true and we don't have to do it again
			selectedGroup.setIsPermissionControllingGroup(true);
			selectedGroup.store();
			
			//getChildren and add this group as permission controlling group
			Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
			if(children!=null && !children.isEmpty()){
				Iterator itering = children.iterator();
				while (itering.hasNext()) {
					Group child = (Group) itering.next();
					child.setPermissionControllingGroup(selectedGroup);
					child.store();
				}
			}
		}
		else{//take it off if set
			boolean isControlling = selectedGroup.isPermissionControllingGroup();
			if(isControlling){
				selectedGroup.setIsPermissionControllingGroup(false);
				selectedGroup.store();
				
			int id = ((Integer)selectedGroup.getPrimaryKey()).intValue();
			
//				getChildren and remove this group as permission controlling group
			  Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
			  if(children!=null && !children.isEmpty()){
				  Iterator itering = children.iterator();
				  while (itering.hasNext()) {
					  Group child = (Group) itering.next();
					  if(id==child.getPermissionControllingGroupID()){
					  	child.setPermissionControllingGroup(null);
					  	
						child.store();
					  }
					  
				  }
			  }
			}
		}
	}
	private Collection getAllPermissionForSelectedGroupAndCurrentUser(IWContext iwc) {
		Collection allPermissions = null;

		try {
			allPermissions = AccessControl.getAllGroupPermissionsForGroup(selectedGroup);
			Collection ownedPermissions = AccessControl.getAllGroupPermissionsOwnedByGroup(iwc.getCurrentUser().getGroup());
			//ownedPermissions.removeAll(allPermissions);

			allPermissions.addAll(ownedPermissions);

		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("GroupPermission selected group (" + selectedGroupId + ") not found or remote error!");
		}
		return allPermissions;
	}

	/**
	 * Method getAndOrderAllPermissions orders by groupId and returns the permissions as a collection of collections.
	 * 
	 * @param iwc
	 * @return Collection
	 */
	private List orderAndGroupPermissionsByContextValue(Collection allPermissions, IWContext iwc) {

		Iterator iter = allPermissions.iterator();

		//order the permissions by the groupId and create a List for each one.
		Map map = new HashMap();
		List finalCollection = new ArrayList();

		String groupId;

		while (iter.hasNext()) {
			ICPermission perm = (ICPermission) iter.next();
			groupId = perm.getContextValue();

			List list = (List) map.get(groupId);
			if (list == null) {
				list = new ArrayList();
			}

			list.add(perm);
			map.put(groupId, list);

		}

		finalCollection = com.idega.util.ListUtil.convertCollectionToList(map.values());

		return finalCollection;

	}

	/**
	 * Gets all the permissiontypes (e.g. read/write) from the collection of ICPermissions from the permissionString column.
	 * 
	 * @param permissions
	 * @return List
	 */
	public List getAllPermissionTypes(Collection permissions) {

		Iterator iter = permissions.iterator();

		List permissionTypes = new ArrayList();

		permissionTypes.add(0, "view");
		permissionTypes.add(1, "edit");
		permissionTypes.add(2, "create");
		permissionTypes.add(3, "delete");

		String permissionType;
		while (iter.hasNext()) {
			ICPermission perm = (ICPermission) iter.next();

			permissionType = perm.getPermissionString();

			if (!permissionTypes.contains(permissionType)) {
				permissionTypes.add(permissionType);
			}

		}

		permissionTypes.remove("owner");

		return permissionTypes;

	}

	/**
	 * Method addGroupPermissionForm.
	 * 
	 * @param iwc
	 */
	private Form getGroupPermissionForm(EntityBrowser browser) throws Exception {

		IWContext iwc = IWContext.getInstance();
		Help help = getHelp(HELP_TEXT_KEY);
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"),PARAM_SAVING,"TRUE");
		save.setSubmitConfirm(iwrb.getLocalizedString("grouppermissionwindow.confirm_message", "Change selected permissions?"));

		SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"));
		close.setOnClick("window.close()");

		Link owners = new Link(iwrb.getLocalizedString("owner.button", "Owners"));
		owners.setWindowToOpen(GroupOwnersWindow.class);
		owners.setAsImageButton(true);
		owners.addParameter(PARAM_SELECTED_GROUP_ID, selectedGroupId);

		Table table = new Table(2, 3);
		table.setRowHeight(1,"20");
		table.setStyleClass(mainStyleClass);
		table.setWidth(620);
		table.setHeight(480);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(2, 2, Table.VERTICAL_ALIGN_TOP);
		//table.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
		//table.setVerticalAlignment(2, 3, Table.VERTICAL_ALIGN_TOP);
		table.setAlignment(2, 3, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.mergeCells(1, 2, 2, 2);
		table.setVerticalAlignment(Table.VERTICAL_ALIGN_TOP);
		
		table.add(new Text(iwrb.getLocalizedString("grouppermissionwindow.setting_permission_for_group","Setting permissions for ")+selectedGroup.getName(),true,false,false),1,1);
		
		Text inherit = new Text(iwrb.getLocalizedString("grouppermissionwindow.apply_recursively_to_children","Apply on children"));
		table.add(inherit, 2, 1);
		table.add(Text.NON_BREAKING_SPACE, 2, 1);
		CheckBox setSamePermissionsOnChildrenCheckBox = new CheckBox(PARAM_PERMISSIONS_SET_TO_CHILDREN,"inherit_to_children");
		setSamePermissionsOnChildrenCheckBox.setChecked(selectedGroup.isPermissionControllingGroup());
		table.add(setSamePermissionsOnChildrenCheckBox, 2, 1);
		
		
		table.add(browser, 1, 2);
		table.setVerticalAlignment(1, 3, "bottom");
		table.setVerticalAlignment(2, 3, "bottom");
		table.add(help, 1, 3);
		table.add(owners, 2, 3);
		table.add(Text.NON_BREAKING_SPACE, 2, 3);
		table.add(save, 2, 3);
		table.add(Text.NON_BREAKING_SPACE, 2, 3);
		table.add(close, 2, 3);
		

		Form form = new Form();
		form.add(table);
//		form.maintainParameter(PARAM_PERMISSIONS_SET_TO_CHILDREN);

		return form;
	}

	private void parseAction(IWContext iwc) throws RemoteException {
		selectedGroupId = iwc.getParameter(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID);

		if (selectedGroupId == null) {
			selectedGroupId = (String) iwc.getSessionAttribute(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID);
		}
		else {
			iwc.setSessionAttribute(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID, selectedGroupId);
		}

		saveChanges = iwc.isParameterSet(PARAM_SAVING);
		

		if (iwc.isParameterSet(RECURSE_PERMISSIONS_TO_CHILDREN_KEY))	{
			groupIdsToRecurseChangesOn = CheckBoxConverter.getResultByParsing(iwc, RECURSE_PERMISSIONS_TO_CHILDREN_KEY);
		}

		try {
			selectedGroup = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(selectedGroupId));
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		hasInheritedPermissions = selectedGroup.getPermissionControllingGroupID() > 0;
	
		if(iwc.isParameterSet(PARAM_OVERRIDE_INHERITANCE)){
			selectedGroup.setPermissionControllingGroup(null);
			selectedGroup.store();
			hasInheritedPermissions = false;
		}
	

	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/**
	 * @see com.idega.presentation.StatefullPresentation#getPresentationStateClass()
	 */
	public Class getPresentationStateClass() {
		return stateHandler.getPresentationStateClass();
	}

	/**
	 * @see com.idega.presentation.PresentationObject#initializeInMain(com.idega.presentation.IWContext)
	 */
	public void initializeInMain(IWContext iwc) throws Exception {

		//	this.addActionListener((IWActionListener)this.getPresentationState(iwc));

	}

	public IWPresentationState getPresentationState(IWUserContext iwuc) {
		return stateHandler.getPresentationState(this, iwuc);
	}

	public StatefullPresentationImplHandler getStateHandler() {
		return stateHandler;
	}

	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("group.permissions", "Group permissions");
	}

	public PresentationObject getPresentationObject(IWContext iwc) {
		return this;
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

	protected Map getPermissionMapFromSession(IWContext iwc, String permissionKey, boolean emptyMap) {
		Map map = (Map) iwc.getSessionAttribute(this.SESSION_PARAM_PERMISSIONS_BEFORE_SAVE + permissionKey);

		if (map == null || emptyMap) {
			map = new HashMap();
			iwc.setSessionAttribute(SESSION_PARAM_PERMISSIONS_BEFORE_SAVE + permissionKey, map);
		}
		return map;

	}
	/**
	 * @see com.idega.presentation.PresentationObject#getName()
	 */
	public String getName() {
		return "Group permissions";
	}

}
