package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;

public class MoveGroupWindow extends StyledIWAdminWindow { // GroupPermissionWindow
															// {//implements
															// StatefullPresentation{

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	public static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID; // todo
																						// remove
																						// when
																						// using
																						// event
																						// system
	private static final String PARAM_SAVING = "gpw_save";
	private static final String SELECTED_TARGET_GROUP_KEY = "grp_ch_grp_id";

	private static final String HELP_TEXT_KEY = "move_group_window";

	private GroupBusiness groupBiz = null;

	private boolean saveChanges = false;

	protected int width = 640;
	protected int height = 480;

	private String selectedGroupId = null;

	private IWResourceBundle iwrb = null;

	private String mainStyleClass = "main";
	private Group selectedGroup;

	/**
	 * Constructor for GroupOwnersWindow.
	 */
	public MoveGroupWindow() {
		super();

		setWidth(this.width);
		setHeight(this.height);
		setScrollbar(true);
		setResizable(true);

	}

	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param name
	 */
	public MoveGroupWindow(String name) {
		super(name);
	}

	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param width
	 * @param heigth
	 */
	public MoveGroupWindow(int width, int heigth) {
		super(width, heigth);
	}

	/**
	 * Constructor for GroupOwnersWindow.
	 * 
	 * @param name
	 * @param width
	 * @param height
	 */
	public MoveGroupWindow(String name, int width, int height) {
		super(name, width, height);
	}

	public void main(IWContext iwc) throws Exception {
		this.iwrb = this.getResourceBundle(iwc);
		addTitle(this.iwrb.getLocalizedString("gmov.move_group_window",
				"Move Group Window"), TITLE_STYLECLASS);

		parseAction(iwc);

		if (this.saveChanges) {
			String targetGroupNodeString = iwc
					.getParameter(SELECTED_TARGET_GROUP_KEY);
			// cut it down because it is in the form "domain_id"_"group_id"
			targetGroupNodeString = targetGroupNodeString.substring(Math.max(
					targetGroupNodeString.indexOf("_") + 1, 0),
					targetGroupNodeString.length());
			int targetGroupId = Integer.parseInt(targetGroupNodeString);

			// move to the real group not the alias!
			Group target = getGroupBusiness(iwc).getGroupByGroupID(
					targetGroupId);
			if (target.isAlias()) {
				target = target.getAlias();
			}

			Group parent = (Group) this.selectedGroup.getParentGroups()
					.iterator().next();
			/*if (this.selectedGroup.isAlias()) {
				parent = (Group) this.selectedGroup.getAlias()
						.getParentGroups().iterator().next();
			}*/

			parent.removeGroup(this.selectedGroup, iwc.getCurrentUser());
			target.addGroup(this.selectedGroup);

			//TODO fix this what is it doing? some caching stuff?
			iwc.getApplicationContext().removeApplicationAttribute("domain_group_tree");
			iwc.getApplicationContext().removeApplicationAttribute("group_tree");
		}

		Form form = getMoveGroupForm();
		form
				.add(new HiddenInput(PARAM_SELECTED_GROUP_ID,
						this.selectedGroupId));
		form.add(new HiddenInput(PARAM_SAVING, "TRUE"));// cannot use this if we
														// put in a navigator in
														// the entitybrowser,
														// change submit button
														// to same value
		add(form, iwc);

	}

	private Form getMoveGroupForm() {

		Help help = getHelp(HELP_TEXT_KEY);

		SubmitButton saveButton = new SubmitButton(this.iwrb
				.getLocalizedString("save", "Save"));
		saveButton
				.setSubmitConfirm(this.iwrb.getLocalizedString(
						"gmov.move_group?",
						"Are you sure you want to move the group?"));

		StyledButton save = new StyledButton(saveButton);
		CloseButton closeButton = new CloseButton(this.iwrb.getLocalizedString(
				"close", "Close"));
		StyledButton close = new StyledButton(closeButton);

		Table buttonTable = new Table(3, 1);
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setWidth(2, 5);
		buttonTable.add(close, 1, 1);
		buttonTable.add(save, 3, 1);

		Table table = new Table(2, 3);
		table.setRowHeight(1, "20");
		table.setStyleClass(this.mainStyleClass);
		table.mergeCells(1, 2, 2, 2);

		table.add(new Text(this.iwrb.getLocalizedString("gmov.table_heading",
				"Move group: ")
				+ this.selectedGroup.getName(), true, false, false), 1, 1);

		GroupChooser targetGroupChooser = new GroupChooser(
				SELECTED_TARGET_GROUP_KEY);
		targetGroupChooser
				.setInputStyle(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
		if (this.selectedGroup != null) {
			targetGroupChooser.setSelectedNode(new GroupTreeNode(
					this.selectedGroup));
		}

		table.add(targetGroupChooser, 1, 2);
		table.setVerticalAlignment(1, 3, "bottom");
		table.setVerticalAlignment(2, 3, "bottom");
		table.add(help, 1, 3);
		table.add(buttonTable, 2, 3);
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
		this.selectedGroupId = iwc
				.getParameter(GroupOwnersWindow.PARAM_SELECTED_GROUP_ID);
		this.saveChanges = iwc.isParameterSet(PARAM_SAVING);

		try {
			this.selectedGroup = getGroupBusiness(iwc).getGroupByGroupID(
					Integer.parseInt(this.selectedGroupId));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}

	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public String getName(IWContext iwc) {
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("gmov.move_group", "Move group");
	}

	public GroupBusiness getGroupBusiness(IWContext iwc) {
		if (this.groupBiz == null) {

			try {
				this.groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(
						iwc, GroupBusiness.class);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		return this.groupBiz;
	}

	/**
	 * @see com.idega.presentation.PresentationObject#getName()
	 */
	public String getName() {
		return "Move group";
	}
}