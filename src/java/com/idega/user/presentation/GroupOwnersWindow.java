package com.idega.user.presentation;

import java.rmi.RemoteException;
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
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.*;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;
import com.idega.util.ListUtil;

/**
 * Description: An editor window for the selected groups owner permissions. <br>Company: Idega Software <br>Copyright: Idega Software 2003 <br>
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 *  
 */
public class GroupOwnersWindow extends StyledIWAdminWindow { //GroupPermissionWindow {//implements StatefullPresentation{

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID; //todo remove when using event system
	private static final String PARAM_SAVING = "gpw_save";
	private static final String PARAM_USER_CHOOSER_USER_ID = "us_ch_us_id";
	private static final String RECURSE_PERMISSIONS_TO_CHILDREN_KEY = "gpw_recurse_ch_of_gr";
	private static final String DELETE_PERMISSIONS_KEY = "gpw_delete_owner";

	private static final String HELP_TEXT_KEY = "group_owners_window";

	private GroupBusiness groupBiz = null;

	private boolean saveChanges = false;

	protected int width = 640;
	protected int height = 480;

	private String selectedGroupId = null;

	private List permissionType;
	private IWResourceBundle iwrb = null;
	private UserBusiness userBiz = null;

	private final String permissionTypeOwner = "owner"; //HARD CODED TEMPORARY

	private String mainStyleClass = "main";
	private Group selectedGroup;

	/**
	 * Constructor for GroupOwnersWindow.
	 */
	public GroupOwnersWindow() {
		super();

		setWidth(width);
		setHeight(height);
		setScrollbar(true);
		setResizable(true);

	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param name
	 */
	public GroupOwnersWindow(String name) {
		super(name);
	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param width
	 * @param heigth
	 */
	public GroupOwnersWindow(int width, int heigth) {
		super(width, heigth);
	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param name
	 * @param width
	 * @param height
	 */
	public GroupOwnersWindow(String name, int width, int height) {
		super(name, width, height);
	}

	public void main(IWContext iwc) throws Exception {
		iwrb = this.getResourceBundle(iwc);
		addTitle(iwrb.getLocalizedString("gpow.group_owners_window", "Group Owners Window"), TITLE_STYLECLASS);



		parseAction(iwc);

		if (saveChanges) {

			AccessController access = iwc.getAccessController();

			try {

				//delete owners
				List deleteOwnersIds = null;
				if (iwc.isParameterSet(DELETE_PERMISSIONS_KEY)) {
					deleteOwnersIds = CheckBoxConverter.getResultByParsing(iwc, DELETE_PERMISSIONS_KEY);

					if (deleteOwnersIds != null && !deleteOwnersIds.isEmpty()) {
						Iterator ownersToDeleteIter = deleteOwnersIds.iterator();
						while (ownersToDeleteIter.hasNext()) {
							Integer userGroupId = (Integer) ownersToDeleteIter.next();
							access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, userGroupId.toString(), selectedGroupId, permissionTypeOwner, Boolean.FALSE);
						}
					}
				}

				//recursive set owners as owners of children or remove as owners of children
				if (iwc.isParameterSet(RECURSE_PERMISSIONS_TO_CHILDREN_KEY)) {
					List groupIdsToRecurseChangesOn = CheckBoxConverter.getResultByParsing(iwc, RECURSE_PERMISSIONS_TO_CHILDREN_KEY);
					if (deleteOwnersIds == null) { //just easier
						deleteOwnersIds = ListUtil.getEmptyList();
					}

					if (groupIdsToRecurseChangesOn != null && !groupIdsToRecurseChangesOn.isEmpty()) {
						Iterator ownersRecurseToChildren = groupIdsToRecurseChangesOn.iterator();
						while (ownersRecurseToChildren.hasNext()) {
							Integer userGroupId = (Integer) ownersRecurseToChildren.next();

							//add
							if (deleteOwnersIds.contains(userGroupId)) {
								//delete recursively
								Group parent = selectedGroup;
								Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
								if (children != null && !children.isEmpty()) {
									Iterator childIter = children.iterator();
									while (childIter.hasNext()) {
										Group childGroup = (Group) childIter.next();
										//only if current user owns the group
										if (iwc.isSuperAdmin() || access.isOwner(childGroup, iwc)) {
											access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, userGroupId.toString(), childGroup.getPrimaryKey().toString(), permissionTypeOwner, Boolean.FALSE);
										}
									}
								}
							}
							else {
								//add owner recursively
								Group parent = selectedGroup;
								Collection children = getGroupBusiness(iwc).getChildGroupsRecursive(parent);
								if (children != null && !children.isEmpty()) {
									Iterator childIter = children.iterator();
									while (childIter.hasNext()) {
										Group childGroup = (Group) childIter.next();
										//only if current user owns the group
										if (iwc.isSuperAdmin() || access.isOwner(childGroup, iwc)) {
											access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, userGroupId.toString(), childGroup.getPrimaryKey().toString(), permissionTypeOwner, Boolean.TRUE);
										}
									}
								}
							}

						}
					}

				}

				//add owner
				String chosenUserId = iwc.getParameter(PARAM_USER_CHOOSER_USER_ID);

				if (chosenUserId != null && !chosenUserId.equals("")) {
					access.setPermission(AccessController.CATEGORY_GROUP_ID, iwc, chosenUserId, selectedGroupId, permissionTypeOwner, Boolean.TRUE);
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

//get the data
		Collection activePermission = new Vector();
		Collection permissions = AccessControl.getAllOwnerGroupPermissionsReverseForGroup(selectedGroup);
		
		//we only want active ones
		Iterator permissionsIter = permissions.iterator();
		while (permissionsIter.hasNext()) {
			ICPermission perm = (ICPermission) permissionsIter.next();
			if(perm.getPermissionValue()){
				activePermission.add(perm);
			}
			
		}
		
		

		EntityBrowser browser = EntityBrowser.getInstanceUsingEventSystemAndExternalForm();
		browser.setEntities("gpow_" + selectedGroupId, activePermission);
		browser.setDefaultNumberOfRows(activePermission.size());
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
		String nameKey = iwrb.getLocalizedString("gpow.user_name", "User name");

		//	define link converter class
		EntityToPresentationObjectConverter converterLink = new EntityToPresentationObjectConverter() {
			private com.idega.core.user.data.User administrator = null;
			private boolean loggedInUserIsAdmin;

			public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
				return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			}

			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {

				ICPermission permission = (ICPermission) entity;
				User user;

				try {
					user = getUserBusiness(iwc).getUser(permission.getGroupID());

					if (administrator == null) {
						try {
							administrator = iwc.getAccessController().getAdministratorUser();
						}
						catch (Exception ex) {
							System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
							ex.printStackTrace(System.err);
							administrator = null;
						}
						loggedInUserIsAdmin = iwc.isSuperAdmin();
					}
					
					Link aLink = new Link(user.getName());
					if (!user.getPrimaryKey().equals(administrator.getPrimaryKey())) {
						aLink.setWindowToOpen(UserPropertyWindow.class);
						aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					}
					else if (loggedInUserIsAdmin) {
						aLink.setWindowToOpen(AdministratorPropertyWindow.class);
						aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					}

					return aLink;
				}
				catch (RemoteException e) {
					e.printStackTrace();
					return new Text("ERROR NO USER FOR ID" + permission.getGroupID());
				}

			}
		};

		browser.setMandatoryColumnWithConverter(column++, nameKey, converterLink);

		//
		CheckBoxConverter recurseCheckBoxConverter = new CheckBoxConverter(RECURSE_PERMISSIONS_TO_CHILDREN_KEY) {

			private com.idega.core.user.data.User administrator = null;

			public PresentationObject getPresentationObject(Object permission, EntityPath path, EntityBrowser browser, IWContext iwc) {
				ICPermission perm = (ICPermission) permission;

				String checkBoxKey = path.getShortKey();
				CheckBox checkBox = new CheckBox(checkBoxKey, Integer.toString(perm.getGroupID()));

				return checkBox;

			}

		};

		recurseCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, RECURSE_PERMISSIONS_TO_CHILDREN_KEY, recurseCheckBoxConverter);

		//converter ends

		//
		CheckBoxConverter deleteCheckBoxConverter = new CheckBoxConverter(DELETE_PERMISSIONS_KEY) {

			private com.idega.core.user.data.User administrator = null;

			public PresentationObject getPresentationObject(Object permission, EntityPath path, EntityBrowser browser, IWContext iwc) {
				ICPermission perm = (ICPermission) permission;

				String checkBoxKey = path.getShortKey();
				CheckBox checkBox = new CheckBox(checkBoxKey, Integer.toString(perm.getGroupID()));

				return checkBox;

			}
		};

		deleteCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, DELETE_PERMISSIONS_KEY, deleteCheckBoxConverter);

		//converter ends

		Form form = getGroupPermissionForm(browser);
		form.add(new HiddenInput(PARAM_SELECTED_GROUP_ID, selectedGroupId));
		form.add(new HiddenInput(PARAM_SAVING, "TRUE"));//cannot use this if we put in a navigator in the entitybrowser, change submit button to same value
		add(form, iwc);

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

		CloseButton close = new CloseButton(iwrb.getLocalizedImageButton("close", "Close"));

		Table table = new Table(2, 3);
		table.setRowHeight(1,"20");
		table.setStyleClass(mainStyleClass);
		table.mergeCells(1, 2, 2, 2);

		table.add(
			new Text(
				iwrb.getLocalizedString("groupownerswindow.setting_permission_for_group", "Setting owners for ") + selectedGroup.getName(),
				true,
				false,
				false),
			1,
			1);
		
		UserChooserBrowser ucb = new UserChooserBrowser(PARAM_USER_CHOOSER_USER_ID);
		ucb.setImageName("magnifyingglass.gif");

		table.add(browser, 1, 2);
		table.add(ucb, 1, 2);
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
		selectedGroupId = iwc.getParameter(GroupOwnersWindow.PARAM_SELECTED_GROUP_ID);
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
		return rBundle.getLocalizedString("group.owners", "Group owners");
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
		return "Group owners";
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
