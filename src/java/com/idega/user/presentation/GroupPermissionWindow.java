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
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupComparator;
import com.idega.user.data.Group;
import com.idega.user.data.GroupTypeConstants;
import com.idega.user.data.User;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;
import com.idega.util.Timer;

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
	private static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID; 
	private static final String PARAM_SAVING = "gpw_save";
	private static final String SESSION_PARAM_PERMISSIONS_BEFORE_SAVE = "gpw_permissions_b_s";
	private static final String RECURSE_PERMISSIONS_TO_CHILDREN_KEY = "gpw_recurse_ch_of_gr";
	private static final String PARAM_OVERRIDE_INHERITANCE = "gpw_over";
	private static final String PARAM_IS_PERMISSION_CONTROLLER = "gpw_permission_ctrl";
	private static final String PARAM_FILTER_CLUBS = "gpw_filter_clubs";
	private static final String PARAM_FILTER_DIVISIONS = "gpw_filter_divisions";
	private static final String HELP_TEXT_KEY = "group_permission_window";

	private boolean filterClubs = false;
	private boolean filterDivisions = false;

	private String mainStyleClass = "main";
	
	List groupIdsToRecurseChangesOn = null;

	private StatefullPresentationImplHandler stateHandler = null;
	private GroupBusiness groupBiz = null;
	private GroupComparator groupComparator = null;

	private boolean saveChanges = false;

	protected int width = 750;
	protected int height = 600;

	private String selectedGroupId = null;

	private IWResourceBundle iwrb = null;
	private Group selectedGroup;
	private boolean hasInheritedPermissions;
    private List permissionTypes;
    private AccessController access;

	
	/**
	 * Constructor for GroupPermissionWindow.
	 */
	public GroupPermissionWindow() {
		super();

		setWidth(width);
		setHeight(height);
		setScrollbar(false);
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
		addTitle(iwrb.getLocalizedString("group_permission_window", "Group Permission Window"), TITLE_STYLECLASS);

		//set initial variables
		parseAction(iwc);
		////////////////////////////////////

		//if this group has inherited permissions it cannot change them
		if (hasInheritedPermissions) {
			addInheritedPermissionsView(iwc);
		}
		else {
		    //save if the user clicked save
			if (saveChanges) {
				saveChanges(iwc);
			}
			
			//add the permission form
			Timer total = new Timer();
			total.start();
			addPermissionsForm(iwc);
			total.stop();
			System.out.println("Total = "+total.getTimeString());
		
		}

	}

	private void addInheritedPermissionsView(IWContext iwc) {
		
		Form form = new Form();
		Text cannotEdit = new Text(iwrb.getLocalizedString("group_permission_window.cannot_edit","You cannot edit this groups permissions."),true,false,false);
		
		Text cannotEdit2 = new Text(iwrb.getLocalizedString("group_permission_window.group_has_inherited_permissions","This group has inherited permissions from the group : "));
		
		Text permissionControlGroupName = new Text(selectedGroup.getPermissionControllingGroup().getName(),true,false,false);
		
		Table mainTable = new Table();
		int mainTableWidth = (width > 60) ? width - 60: 10;
		int mainTableHeight = (height > 50) ? height - 50 : 10;
		mainTable.setWidth(mainTableWidth);
		mainTable.setHeight(mainTableHeight);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		
		Table table = new Table(1, 3);
		table.setRowHeight(1,"20");
		table.setStyleClass(mainStyleClass);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(440);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_BOTTOM);
		table.setAlignment(1, 3, Table.HORIZONTAL_ALIGN_RIGHT);
		
		
		table.add(cannotEdit,1,1);
		table.add(cannotEdit2,1,2);
		table.add(permissionControlGroupName,1,2);
		
		SubmitButton override = new SubmitButton(iwrb.getLocalizedImageButton("group_permission_window.override", "Override inherited permissions"),PARAM_OVERRIDE_INHERITANCE,"true");
		CloseButton close = new CloseButton(iwrb.getLocalizedImageButton("close", "Close"));
		
		Link owners = new Link(iwrb.getLocalizedString("owner.button", "Owners"));
				owners.setWindowToOpen(GroupOwnersWindow.class);
				owners.setAsImageButton(true);
				owners.addParameter(PARAM_SELECTED_GROUP_ID, selectedGroupId);
				
    		Table bottomTable = new Table();
    		bottomTable.setCellpadding(0);
    		bottomTable.setCellspacing(5);
    		bottomTable.setWidth(Table.HUNDRED_PERCENT);
    		bottomTable.setHeight(39);
    		bottomTable.setStyleClass(mainStyleClass);
    		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
    		
    		bottomTable.add(override,1,2);
    		bottomTable.add(Text.NON_BREAKING_SPACE,1,2);
    		bottomTable.add(owners,1,2);
    		bottomTable.add(Text.NON_BREAKING_SPACE,1,2);
    		bottomTable.add(close,1,2);
		
		mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
		mainTable.add(table,1,1);
		mainTable.add(bottomTable,1,3);
				
		form.add(mainTable);
		add(form,iwc);
	}
	
	private void addPermissionsForm(IWContext iwc) throws Exception {
	    //get permission, order, sort alphabetically and use entitybrowser
		Collection allPermissions = getAllPermissionForSelectedGroupAndCurrentUser(iwc);
	    List entityList = orderAndGroupPermissionsByContextValue(allPermissions, iwc);
		groupComparator = new GroupComparator(iwc);
		groupComparator.setObjectsAreICPermissions(true);
		groupComparator.setGroupBusiness(this.getGroupBusiness(iwc));		
		groupComparator.setSortByParents(true);
		Timer t = new Timer();
		t.start();
		Collections.sort(entityList, groupComparator); 
		t.stop();
		System.out.println(t.getTimeString());
		List browserList = null;
		List groupTypes = new ArrayList();
		if (filterClubs) {
		    groupTypes.add(GroupTypeConstants.GROUP_TYPE_CLUB);
		}
		if (filterDivisions) {
		    groupTypes.add("iwme_club_division");
		}
		if (!groupTypes.isEmpty()) {
		    browserList = getFilteredEntityListByGroupType(entityList, groupTypes);
		} else {
		    browserList = entityList;
		}
		EntityBrowser browser = getEntityBrowser(permissionTypes, browserList);
		//////////////////////////
		
		Form form = getGroupPermissionForm(browser);
		//needed for the entitybrowser...I think...can't remember...need more beer ;)
		form.add(new HiddenInput(PARAM_SELECTED_GROUP_ID, selectedGroupId));

		add(form, iwc);
	}
	
	private List getFilteredEntityListByGroupType(List entityList, List groupTypes) {
	    List filteredEntityList = new ArrayList();
	    Iterator it = entityList.iterator();
	    while (it.hasNext()) {
	        List permissionCollection = (List)it.next();
	        String groupID = ((ICPermission) permissionCollection.iterator().next()).getContextValue();
	        Group tempGroup = (Group) groupComparator.getCachedGroups().get(groupID);
	        //System.out.println(tempGroup.getGroupType());
	        if (groupTypes.contains(tempGroup.getGroupType())) {
	            filteredEntityList.add(permissionCollection);
	        }
	    }
	    return filteredEntityList;
	}

	private EntityBrowser getEntityBrowser(List permissionTypes, List entityList) {
		EntityBrowser browser = EntityBrowser.getInstanceUsingExternalForm();
		
		browser.setEntities("gpw_" + selectedGroupId, entityList);
		//browser.setDefaultNumberOfRows(entityCollection.size() );
		browser.setDefaultNumberOfRows(18);
		browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
		browser.setWidth(browser.HUNDRED_PERCENT);
		int scrollableHeight = (height > 250) ? height -250 : 20;
		int scrollableWidth =(width > 60) ? width -60 : 20;
		browser.setScrollableWithHeightAndWidth(scrollableHeight, scrollableWidth);
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
					Group group = null;
					try {
						Integer groupID = Integer.valueOf(perm.getContextValue());
						String key = groupID.toString();
					    if (getGroupComparator().getCachedGroups()!=null) {
							if (getGroupComparator().getCachedGroups().containsKey(key))
							    group = (Group)getGroupComparator().getCachedGroups().get(key);
							else
							{	
							    group = getGroupBusiness(iwc).getGroupByGroupID(groupID.intValue());
							    groupComparator.getCachedGroups().put(key, group);
							}
						}
						else {
						    group = getGroupBusiness(iwc).getGroupByGroupID(groupID.intValue());
						}
						
						String name = getGroupComparator().getIndentedGroupName(group);
//						String number = group.getMetaData(ICUserConstants.META_DATA_GROUP_NUMBER);
						return new Text(name);
					}
					catch (RemoteException e) {
						e.printStackTrace();
					}
					catch (FinderException ex) {
						ex.printStackTrace();
					}

				}

				return new Text("THE PERMISSION MAP WAS EMPTY!");

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
				//used for displaying check boxes even though permission is not set
				//perhaps the permit permission should only be allowed to permit other the same permissions?
				boolean isOwnerOrHasPermitPermission = false;
				 

				final String columnName = path.getShortKey();
				final String ownerType = "owner";
				final String permitType = "permit";
				final int selectedId = Integer.parseInt(selectedGroupId);
				
				//here we add to the permission map in session for saving purposes
				Map permissionMap = getPermissionMapFromSession(iwc, columnName, false);

				String groupId = null;
				String permissionType = null;

				
				while (iterator.hasNext() && !isSet) {
					ICPermission perm = (ICPermission) iterator.next();
					groupId = perm.getContextValue();
					permissionType = perm.getPermissionString();

					if(selectedId == perm.getGroupID()){
						isSet = columnName.equals(permissionType);
					}
					else{
						//don't add the permission to the displayed list because it does not belong to the selected group
						//but to the current user or one of his parent groups. (permit permission is a special case)
						//the group id is still added to the list so the current user can give the selected group permissions for that group
						if(!permissionType.equals(permitType)){
							isSet = columnName.equals(permissionType);
						}
					}
					
					if (!isOwnerOrHasPermitPermission) { //isOwner is not always set if the group also has other permissions??
						isOwnerOrHasPermitPermission = ownerType.equals(permissionType) || permitType.equals(permissionType);
					}

					if (isSet) {
						active = perm.getPermissionValue();
						if (active) {
							permissionMap.put(groupId, perm);
						}
					}

				}

				PresentationObject returnObj = null;

				if (isSet || isOwnerOrHasPermitPermission) {
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

			    String checkBoxKey = path.getShortKey(); 
			    String groupID = null;
			    boolean checked = false;
			    
				Collection col = (Collection) permissions;
				Iterator iterator = col.iterator();

				while (iterator.hasNext()) {
					ICPermission perm = (ICPermission) iterator.next();
						
					groupID = perm.getContextValue();

					//we only care for permission that have a positive and active value
						if(perm.getPermissionValue()) {
							checked = perm.doesInheritToChildren();
							break;//we can stop right here because all the permission in this collection should have the same value
						}
						else {
						    continue;
						}
				}
				
				
				CheckBox checkBox = new CheckBox(checkBoxKey, groupID);
				checkBox.setChecked(checked);
				
				return checkBox;

			}
		};
		
		
		recurseCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, RECURSE_PERMISSIONS_TO_CHILDREN_KEY, recurseCheckBoxConverter);
	
		

		//converter ends

		return browser;
	}
	private void saveChanges(IWContext iwc) {
		
		try {
		    
		    //Adds or removes this group as a permission controlling group to its child groups.
			checkForPermissionControllingGroupChanges(iwc);
			
			
			//iterate for each permission key, view, edit etc.
			Iterator iterator = permissionTypes.iterator();

			while (iterator.hasNext()) {
			    //permission key, view, edit etc.
				String key = (String) iterator.next();
				//group ids for this key
				String[] groupIDs = iwc.getParameterValues(key);
				//get a map of permissions by groups and key that the selected group has BEFORE THE SAVE
				//then we remove the ones we add from the list and the rest are those we need to remove! smart eh?
				Map permissions = this.getPermissionMapFromSession(iwc, key, false);

				//If inherit is checked and had not been done before we add those permissions to that group and to its
				//children. If inherit is checked but a single permission like "edit" is unchecked then it is removed from
				//that group and its children. If you only want to detactivate the inheritance but not change the children
				//just uncheck inheritance.
				
				//add permissions and inherit if needed
				addPermissions(iwc, key, groupIDs, permissions);
				
				//remove permissions and inherited if needed
				removePermissions(iwc, key, permissions);

			}

			//refresh permissions PermissionCacher.updatePermissions()
			//temporary way of updating the permission map for groups
			iwc.getApplicationContext().removeApplicationAttribute("ic_permission_map_" + AccessController.CATEGORY_GROUP_ID);
			

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
     * @param iwc
     * @param key
     * @param permissions
     * @throws Exception
     * @throws FinderException
     * @throws RemoteException
     */
    private void removePermissions(IWContext iwc, String key, Map permissions) throws Exception, FinderException, RemoteException {
       
        //removing (setting to false) permissions
        Iterator entries = permissions.values().iterator();
        while (entries.hasNext()) {	
        	ICPermission permission = (ICPermission) entries.next();
        	
        	removePermission(iwc, key, permission.getContextValue());
        	  
        	removeInheritedPermissionFromChildGroups(iwc, key, permission);
        	
        }
    }
    /**
     * @param iwc
     * @param key
     * @param permission
     * @throws FinderException
     * @throws RemoteException
     * @throws Exception
     */
    private void removeInheritedPermissionFromChildGroups(IWContext iwc, String key, ICPermission permission) throws FinderException, RemoteException, Exception {
       //if inherit is checked then we remove the permissions that we want to change from its children otherwise we set 
        //it to not inherit to children
        if(groupIdsToRecurseChangesOn!=null && groupIdsToRecurseChangesOn.contains(new Integer(permission.getContextValue()))){
       
            //recurse through children and remove same rights	
        	Group parent = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(permission.getContextValue()));
        	Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
        	if(children!=null && !children.isEmpty()){
        		Iterator childIter = children.iterator();
        		while (childIter.hasNext()) {
        			Group childGroup = (Group) childIter.next();
        			//only if the user is allowed
        			if(iwc.isSuperAdmin() || access.isOwner(childGroup,iwc) || access.hasPermitPermissionFor(childGroup,iwc)){
        			    removePermission(iwc, key, childGroup.getPrimaryKey().toString());
        			}
        		
        		}
        	}
        	
        }
      
    }
    /**
     * @param iwc
     * @param key
     * @param childGroup
     * @throws Exception
     */
    private void removePermission(IWContext iwc, String key, String groupId) throws Exception {
        access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, selectedGroupId, groupId, key, Boolean.FALSE);
    }
    /**
     * @param iwc
     * @param key
     * @param values
     * @param permissions
     * @throws Exception
     * @throws FinderException
     * @throws RemoteException
     */
    private void addPermissions(IWContext iwc, String key, String[] groupIds, Map permissions) throws Exception, FinderException, RemoteException {
        //
        //adding new permissions
        //
        if (groupIds != null && groupIds.length > 0) {
            
            //Add permissions and recurse to the child groups if needed
        	for (int i = 0; i < groupIds.length; i++) {
        	    addPermission(iwc, key, groupIds[i]);
        	
        		addInheritedPermissionToChildGroups(iwc, key, groupIds[i]);
        		 
        		//remove from the list so we know which ones to remove later
        		permissions.remove(groupIds[i]);
        	
        	}
        }
        
    }
    /**
	 * Inherits the permission to the children
     * @param iwc
     * @param key
     * @param values
     * @param i
     * @throws FinderException
     * @throws RemoteException
     * @throws Exception
     */
    private void addInheritedPermissionToChildGroups(IWContext iwc, String key, String groupId) throws FinderException, RemoteException, Exception {
        //get the permission to mark if it should inherit to children or not
        ICPermission perm = AccessControl.getGroupICPermissionForGroupAndPermissionKeyAndContextValue(selectedGroup,key,groupId);
       
        //check if we need to recurse (inherit) the permission to the children of the group we are setting the permission to
        if(groupIdsToRecurseChangesOn!=null && groupIdsToRecurseChangesOn.contains(new Integer(groupId))){
             
            perm.setToInheritToChildren();
            perm.store();
           
            
        	//recurse through children and give same rights
        	Group parent = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(groupId));
        	Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
        	if(children!=null && !children.isEmpty()){
        		Iterator childIter = children.iterator();
        		while (childIter.hasNext()) {
        			Group childGroup = (Group) childIter.next();
        			//only if current user owns the group or has permit permission to it
        			if(iwc.isSuperAdmin() || access.isOwner(childGroup,iwc) || access.hasPermitPermissionFor(childGroup,iwc)){
        				addPermission(iwc, key, childGroup.getPrimaryKey().toString());
        			}
        		}
        	}
        }
        else {
            perm.setToNOTInheritToChildren();
            perm.store();
        }
    }
    
    
    /**
	 * Adds the permission from the selected group to the group it gets permission to
     * @param iwc
     * @param key view,edit,create,delete,permit
     * @param groupId the id of the group to get permission to
     * @param i
     * @throws Exception
     */
    private void addPermission(IWContext iwc, String key, String groupId) throws Exception {
        //Add the permission
        access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, selectedGroupId, groupId, key, Boolean.TRUE);
    }
    /**
	 * Adds or removes this group as a permission controlling group to its child groups.
	 * @param iwc
	 * @throws RemoteException
	 */
	private void checkForPermissionControllingGroupChanges(IWContext iwc) throws Exception {
		//inheritance stuff
		String inheritToChildren = iwc.getParameter(PARAM_IS_PERMISSION_CONTROLLER);
		if(inheritToChildren!=null && !selectedGroup.isPermissionControllingGroup()){
		    //its true and we don't have to do it again
			selectedGroup.setIsPermissionControllingGroup(true);
			selectedGroup.store();
			
			//getChildren and add this group as permission controlling group
			Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(selectedGroup);
			if(children!=null && !children.isEmpty()){
				Iterator itering = children.iterator();
				while (itering.hasNext()) {
					Group childGroup = (Group) itering.next();
					if(iwc.isSuperAdmin() || access.isOwner(childGroup,iwc) || access.hasPermitPermissionFor(childGroup,iwc)){
					    childGroup.setPermissionControllingGroup(selectedGroup);
					    childGroup.store();
					}
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
					  //we control the selected group so we do not have to ask permission to remove it
					  if(id==child.getPermissionControllingGroupID()){
					  	child.setPermissionControllingGroup(null);
					  	
						child.store();
					  }
					  
				  }
			  }
			}
		}
	}
	
	
	/**
	 * Gets all the ICPermission entries for the selected group and the current user.
	 * @param iwc
	 * @return
	 */
	private Collection getAllPermissionForSelectedGroupAndCurrentUser(IWContext iwc) {
		Collection allPermissions = new ArrayList();
		try {
			User user = iwc.getCurrentUser();
			
			//for this group
			allPermissions.addAll(AccessControl.getAllGroupPermissionsForGroup(selectedGroup));
			//for the user
			Collection ownedPermissions = AccessControl.getAllGroupOwnerPermissionsByGroup(user);
			//add the permissions to one big list
			allPermissions.addAll(ownedPermissions);
			
//			get all permit permissions from parents or their permission controlling groups
			Collection parentOrPersionControllingGroups = getAllParentOrPermissionControllingGroupsForUser(iwc, user);
			if(!parentOrPersionControllingGroups.isEmpty()){
			    Collection permitPermissions = AccessControl.getAllGroupPermitPermissions(parentOrPersionControllingGroups);
			    allPermissions.addAll(permitPermissions);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("GroupPermission selected group (" + selectedGroupId + ") not found or remote error!");
		}
		return allPermissions;
	}

	/**
     * @param iwc
     * @param user
     * @return
     * @throws RemoteException
     */
    private ArrayList getAllParentOrPermissionControllingGroupsForUser(IWContext iwc, User user) throws RemoteException {
        //permissions from the users direct parent groups or their permission controlling groups 
        ArrayList parents = new ArrayList();
        Collection directlyRelatedParents = getGroupBusiness(iwc).getParentGroups(user);
        
        Iterator iterating = directlyRelatedParents.iterator();
        
        
        while (iterating.hasNext()) {
        	Group parent = (Group) iterating.next();
        	if(parent.getPermissionControllingGroupID()>0){
        		parents.add(parent.getPermissionControllingGroup());
        	}
        	else{
        		parents.add(parent);
        	}	
        }
        return parents;
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
	//public List getAllPermissionTypes(Collection permissions) {
	protected List getAllPermissionTypes() {

		if(permissionTypes == null ) {
		    permissionTypes = new ArrayList();
			
	        permissionTypes.add(0, "view");
			permissionTypes.add(1, "edit");
			permissionTypes.add(2, "create");
			permissionTypes.add(3, "delete");
			permissionTypes.add(4, "permit");//the permission to give others permissions
		}
		//we shall only support these group permission for now. Use roles for other stuff.
		/*
		Iterator iter = permissions.iterator();
		String permissionType;
		while (iter.hasNext()) {
			ICPermission perm = (ICPermission) iter.next();
			permissionType = perm.getPermissionString();
			if (!permissionTypes.contains(permissionType)) {
				permissionTypes.add(permissionType);
			}
		}
		permissionTypes.remove("owner");
*/
		return permissionTypes;

	}

	/**
	 * Method addGroupPermissionForm.
	 * 
	 * @param iwc
	 */
	private Form getGroupPermissionForm(EntityBrowser browser) throws Exception {
		Help help = getHelp(HELP_TEXT_KEY);
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"),PARAM_SAVING,"TRUE");
		save.setSubmitConfirm(iwrb.getLocalizedString("grouppermissionwindow.confirm_message", "Change selected permissions?"));

		CloseButton close = new CloseButton(iwrb.getLocalizedImageButton("close", "Close"));

		Link owners = new Link(iwrb.getLocalizedString("owner.button", "Owners"));
		owners.setWindowToOpen(GroupOwnersWindow.class);
		owners.setAsImageButton(true);
		owners.addParameter(PARAM_SELECTED_GROUP_ID, selectedGroupId);

		Table mainTable = new Table();
		mainTable.setWidth(620);
		mainTable.setHeight(480);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		
		Table filterTable = new Table(1, 2);
		filterTable.setWidth(Table.HUNDRED_PERCENT);
		filterTable.setCellpadding(0);
		filterTable.setCellspacing(0);
		filterTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		filterTable.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		filterTable.setAlignment(1, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		filterTable.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_RIGHT);
		
		Text filterClubsText = new Text(iwrb.getLocalizedString("grouppermissionwindow.filter_clubs","Filter clubs"));
		filterTable.add(filterClubsText, 1, 1);
		filterTable.add(Text.NON_BREAKING_SPACE, 1, 1);
		CheckBox filterClubsCheckBox = new CheckBox(PARAM_FILTER_CLUBS, "filter_clubs");
		filterClubsCheckBox.setChecked(filterClubs);
		filterClubsCheckBox.setToSubmit();
		Text filterDivisionText = new Text(iwrb.getLocalizedString("grouppermissionwindow.filter_divisions","Filter divisions"));
		filterTable.add(filterDivisionText, 1, 2);
		filterTable.add(Text.NON_BREAKING_SPACE, 1, 2);
		CheckBox filterDivisionsCheckBox = new CheckBox(PARAM_FILTER_DIVISIONS, "filter_divisions");
		filterDivisionsCheckBox.setChecked(filterDivisions);
		filterDivisionsCheckBox.setToSubmit();
		
		filterTable.add(filterClubsCheckBox, 1, 1);
		filterTable.add(filterDivisionsCheckBox, 1, 2);

		Table table = new Table(2, 3);
		table.setRowHeight(1,"20");
		table.setStyleClass(mainStyleClass);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(440);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(2, 2, Table.VERTICAL_ALIGN_TOP);
		//table.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
		//table.setVerticalAlignment(2, 3, Table.VERTICAL_ALIGN_TOP);
		table.setAlignment(2, 3, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.mergeCells(1, 2, 2, 2);
		
		table.add(new Text(iwrb.getLocalizedString("grouppermissionwindow.setting_permission_for_group","Setting permissions for ")+selectedGroup.getName(),true,false,false),1,1);
		
		Text inherit = new Text(iwrb.getLocalizedString("grouppermissionwindow.apply_recursively_to_children","Apply on children"));
		table.add(inherit, 2, 1);
		table.add(Text.NON_BREAKING_SPACE, 2, 1);
		CheckBox setSamePermissionsOnChildrenCheckBox = new CheckBox(PARAM_IS_PERMISSION_CONTROLLER,"inherit_to_children");
		setSamePermissionsOnChildrenCheckBox.setChecked(selectedGroup.isPermissionControllingGroup());
		table.add(setSamePermissionsOnChildrenCheckBox, 2, 1);
		
		
		table.add(browser, 1, 2);
		table.add(filterTable, 2, 3);
		
    Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setHeight(39);
		bottomTable.setStyleClass(mainStyleClass);
		bottomTable.add(help,1,1);
		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(owners,2,1);
		bottomTable.add(Text.NON_BREAKING_SPACE,2,1);
		bottomTable.add(save, 2, 1);
		bottomTable.add(Text.NON_BREAKING_SPACE, 2, 1);
		bottomTable.add(close, 2, 1);
		
		 mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
	   mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
	   mainTable.add(table,1,1);
	   mainTable.add(bottomTable,1,3);
	   Form form = new Form();
	   form.add(mainTable);
		
//		form.maintainParameter(PARAM_PERMISSIONS_SET_TO_CHILDREN);

		return form;
	}

	private void parseAction(IWContext iwc) throws RemoteException {
		
	   //get the id and the group with it
	    selectedGroupId = iwc.getParameter(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID);

		if (selectedGroupId == null) {
			selectedGroupId = (String) iwc.getSessionAttribute(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID);
		}
		else {
			iwc.setSessionAttribute(GroupPermissionWindow.PARAM_SELECTED_GROUP_ID, selectedGroupId);
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
		////////////////////////////////////
		
		

		//did the user ask to inherit permissions to any of the permissions
		if (iwc.isParameterSet(RECURSE_PERMISSIONS_TO_CHILDREN_KEY))	{
			groupIdsToRecurseChangesOn = CheckBoxConverter.getResultByParsing(iwc, RECURSE_PERMISSIONS_TO_CHILDREN_KEY);
		}
		////////////////////////////////////


		//does this group have inherited permissions? then the user cannot change them unless they detach it
		hasInheritedPermissions = selectedGroup.getPermissionControllingGroupID() > 0;
	
		//are we detaching from the inheritance?
		if(iwc.isParameterSet(PARAM_OVERRIDE_INHERITANCE)){
			selectedGroup.setPermissionControllingGroup(null);
			selectedGroup.store();
			hasInheritedPermissions = false;
		}
		////////////////////////////////////	
		
		//Are we saving the changes
		saveChanges = iwc.isParameterSet(PARAM_SAVING);
		
		
	    //get all types or keys. view, edit etc.
	    permissionTypes = getAllPermissionTypes();
	    access = iwc.getAccessController();
		
		filterClubs = iwc.isParameterSet(PARAM_FILTER_CLUBS);
		filterDivisions = iwc.isParameterSet(PARAM_FILTER_DIVISIONS);
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

    /**
     * @return Returns the groupComparator.
     */
    public GroupComparator getGroupComparator() {
        return groupComparator;
    }
    /**
     * @param groupComparator The groupComparator to set.
     */
    public void setGroupComparator(GroupComparator groupComparator) {
        this.groupComparator = groupComparator;
    }
}
