package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.CheckBoxConverter;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWColor;

/**
 * Description: An editor window for the selecting role masters. <br>
 * Company: Idega Software <br>
 * Copyright: Idega Software 2003 <br>
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson </a>
 *  
 */
public class RoleMastersWindow extends StyledIWAdminWindow {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String PARAM_SAVING = "role_master_save";

	private static final String PARAM_USER_CHOOSER_USER_ID = "us_ch_us_id";

	private static final String DELETE_PERMISSIONS_KEY = "role_master_delete";

	private static final String HELP_TEXT_KEY = "role_masters";

	private GroupBusiness groupBiz = null;

	private boolean saveChanges = false;

	protected int width = 640;

	protected int height = 520;

	private List permissionType;

	private IWResourceBundle iwrb = null;

	private UserBusiness userBiz = null;

	private String mainTableStyle = "main";

	/**
	 * Constructor for RoleMastersWindow.
	 */
	public RoleMastersWindow() {
		super();

		setWidth(width);
		setHeight(height);
		setScrollbar(true);
		setResizable(true);

	}

	/**
	 * Constructor for RoleMastersWindow.
	 * 
	 * @param name
	 */
	public RoleMastersWindow(String name) {
		super(name);
	}

	/**
	 * Constructor for RoleMastersWindow.
	 * 
	 * @param width
	 * @param heigth
	 */
	public RoleMastersWindow(int width, int heigth) {
		super(width, heigth);
	}

	/**
	 * Constructor for RoleMastersWindow.
	 * 
	 * @param name
	 * @param width
	 * @param height
	 */
	public RoleMastersWindow(String name, int width, int height) {
		super(name, width, height);
	}

	public void main(IWContext iwc) throws Exception {
		iwrb = this.getResourceBundle(iwc);

		parseAction(iwc);

		if (saveChanges) {

			AccessController access = iwc.getAccessController();

			try {

				//delete roleMasters
				List deleteRoleMastersIds = null;
				if (iwc.isParameterSet(DELETE_PERMISSIONS_KEY)) {
					deleteRoleMastersIds = CheckBoxConverter.getResultByParsing(iwc, DELETE_PERMISSIONS_KEY);

					if (deleteRoleMastersIds != null && !deleteRoleMastersIds.isEmpty()) {
						Iterator roleMastersToDeleteIter = deleteRoleMastersIds.iterator();
						while (roleMastersToDeleteIter.hasNext()) {
							Integer userGroupId = (Integer) roleMastersToDeleteIter.next();
							access.removeGroupFromRoleMastersList(getUserBusiness(iwc).getUser(userGroupId), iwc);
						}
					}
				}

				//add roleMaster
				String chosenUserId = iwc.getParameter(PARAM_USER_CHOOSER_USER_ID);

				if (chosenUserId != null && !chosenUserId.equals("")) {
					access.addGroupAsRoleMaster(getUserBusiness(iwc).getUser(new Integer(chosenUserId)), iwc);
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}

		//get the data
		Collection groups = iwc.getAccessController().getAllGroupsThatAreRoleMasters(iwc);//Users
																						  // really

		EntityBrowser browser = EntityBrowser.getInstanceUsingEventSystemAndExternalForm();
		browser.setEntities("roles_masters", groups);
		browser.setDefaultNumberOfRows(groups.size());
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
		String nameKey = "UserName";

		//	define link converter class
		EntityToPresentationObjectConverter converterLink = new EntityToPresentationObjectConverter() {

			private com.idega.core.user.data.User administrator = null;

			private boolean loggedInUserIsAdmin;

			public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
				return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
			}

			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {

				Group group = (Group) entity;

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

				Link aLink = null;
				User user;
				try {
					user = getUserBusiness(iwc).getUser((Integer) group.getPrimaryKey());

					aLink = new Link(user.getName());
					if (!group.equals(administrator)) {
						aLink.setWindowToOpen(UserPropertyWindow.class);
						aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, group.getPrimaryKey().toString());
					}
					else if (loggedInUserIsAdmin) {
						aLink.setWindowToOpen(AdministratorPropertyWindow.class);
						aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, group.getPrimaryKey().toString());
					}

				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return aLink;

			}
		};

		browser.setMandatoryColumnWithConverter(column++, iwrb.getLocalizedString(nameKey,"User Name"), converterLink);

		//converter ends

		CheckBoxConverter deleteCheckBoxConverter = new CheckBoxConverter(DELETE_PERMISSIONS_KEY) {

			private com.idega.core.user.data.User administrator = null;

			public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
				Group group = (Group) entity;

				String checkBoxKey = path.getShortKey();
				CheckBox checkBox = new CheckBox(checkBoxKey, group.getPrimaryKey().toString());

				return checkBox;

			}
		};

		deleteCheckBoxConverter.setShowTitle(true);
		browser.setMandatoryColumnWithConverter(column++, DELETE_PERMISSIONS_KEY, deleteCheckBoxConverter);

		//converter ends
		setName(iwrb.getLocalizedString("role_master_title", "Role Master"));
		addTitle(iwrb.getLocalizedString("role_master_title", "Role Master"), TITLE_STYLECLASS);

		Form form = getRoleMastersForm(browser);
		form.add(new HiddenInput(PARAM_SAVING, "TRUE"));//cannot use this if we
														// put in a navigator in
														// the entitybrowser,
														// change submit button
														// to same value
		add(form, iwc);

	}

	private Form getRoleMastersForm(EntityBrowser browser) throws Exception {

		Help help = getHelp(HELP_TEXT_KEY);

		SubmitButton saveButton = new SubmitButton(iwrb.getLocalizedString("save", "Save"));
		saveButton.setSubmitConfirm(iwrb.getLocalizedString("change.selected.permissions?", "Change selected permissions?"));
		StyledButton save = new StyledButton(saveButton);
		
		CloseButton closeButton = new CloseButton(iwrb.getLocalizedString("close", "Close"));
		StyledButton close = new StyledButton(closeButton);

		Table mainTable = new Table(1, 3);
		mainTable.setWidth(Table.HUNDRED_PERCENT);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);

		Table table = new Table();
		table.setRowHeight(1, "20");
		table.setCellpadding(12);
		table.setCellspacing(0);
		table.setStyleClass(mainTableStyle);
		table.mergeCells(1, 2, 2, 2);

		table.add(new Text(iwrb.getLocalizedString("roleMastersWindow.active_role_masters", "Active role masters"), true, false, false), 1, 1);

		table.add(browser, 1, 2);
		UserChooserBrowser userChooser = new UserChooserBrowser(PARAM_USER_CHOOSER_USER_ID);
		userChooser.setUserBundle(true);
		userChooser.setImageName("magnifyingglass.gif");
		table.add(userChooser, 1, 2);

		Table buttonTable = new Table();
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setWidth(2, "5");
		buttonTable.add(save, 1, 1);
		buttonTable.add(close, 3, 1);

		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setStyleClass(mainTableStyle);
		bottomTable.add(help, 1, 1);
		bottomTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(buttonTable, 2, 1);

		mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
		mainTable.add(table, 1, 1);
		mainTable.setHeight(2, 5);
		mainTable.add(bottomTable, 1, 3);

		table.setWidth(Table.HUNDRED_PERCENT);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);

		Form form = new Form();
		form.add(mainTable);

		return form;
	}

	private void parseAction(IWContext iwc) throws RemoteException {
		saveChanges = iwc.isParameterSet(PARAM_SAVING);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("RoleMastersWindow.title", "Role masters");
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
		return "Roles";
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