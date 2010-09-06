package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;
import javax.swing.event.ChangeListener;

import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.data.Group;
import com.idega.user.event.MoveGroupEvent;
import com.idega.user.event.SelectGroupEvent;

public class MoveGroupWindow extends StyledIWAdminWindow implements
		StatefullPresentation {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	public static final String PARAM_SELECTED_GROUP_ID = SelectGroupEvent.PRM_GROUP_ID;

	private IWPresentationState presentationState = null;

	public static final String GROUP_ID_KEY = "group_id_key";
	public static final String OLD_PARENT_GROUP_ID_KEY = "old_parent_group_id";
	public static final String NEW_PARENT_GROUP_ID_KEY = "new_parent_group_id";

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
	private Group currentGroup = null;

	/**
	 * Constructor for GroupOwnersWindow.
	 */
	public MoveGroupWindow() {
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
		getPresentationState(iwc);
		// get groupid
		Integer groupId = new Integer(-1);
		if (iwc.isParameterSet(GROUP_ID_KEY)) {
			String groupIdString = iwc.getParameter(GROUP_ID_KEY);
			groupId = new Integer(groupIdString);

			this.currentGroup = getGroupBusiness(iwc)
					.getGroupByGroupID(groupId);
		}

		Integer oldParentGroupId = new Integer(-1);
		if (iwc.isParameterSet(OLD_PARENT_GROUP_ID_KEY)) {
			String groupIdString = iwc.getParameter(OLD_PARENT_GROUP_ID_KEY);
			oldParentGroupId = new Integer(groupIdString);
		}

		this.iwrb = this.getResourceBundle(iwc);
		addTitle(this.iwrb.getLocalizedString("gmov.move_group_window",
				"Move Group Window"), TITLE_STYLECLASS);

		parseAction(iwc);

		MoveGroupEvent moveEvent = new MoveGroupEvent();
		moveEvent.setSource(this);
		moveEvent.setOldParentGroupId(oldParentGroupId);
		// moveEvent.setNewParentGroupId(primaryKey)
		moveEvent.setGroupId(groupId);
		moveEvent.setPerformer((Integer) iwc.getCurrentUser().getPrimaryKey());

		Form form = getMoveGroupForm();
		form.addEventModel(moveEvent, iwc);
		form.addParameter(MoveGroupEvent.OKAY_KEY, "w");

		add(form, iwc);

	}

	private Form getMoveGroupForm() {

		Help help = getHelp(HELP_TEXT_KEY);

		SubmitButton saveButton = new SubmitButton(
				this.iwrb.getLocalizedString("save", "Save"));
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

		table.add(
				new Text(this.iwrb.getLocalizedString("gmov.table_heading",
						"Move group: ") + this.currentGroup.getName(), true,
						false, false), 1, 1);

		GroupChooser targetGroupChooser = new GroupChooser(
				MoveGroupEvent.NEW_PARENT_GROUP_ID);
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
		} catch (FinderException e) {
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

	public void initializeInMain(IWContext iwc) {
		IWPresentationState state = this.getPresentationState(iwc);
		// add action listener
		addActionListener((IWActionListener) state);
		IWStateMachine stateMachine;
		// IWPresentationState changeListenerState = null;
		// add all changelisteners
		Collection changeListeners;
		try {
			stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc,
					IWStateMachine.class);
			changeListeners = stateMachine.getAllChangeListeners();
		} catch (RemoteException e) {
			changeListeners = new ArrayList();
		}
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			state.addChangeListener((ChangeListener) iterator.next());
		}
	}

	/**
	 * @see com.idega.presentation.StatefullPresentation#getPresentationStateClass()
	 */
	public Class getPresentationStateClass() {
		return MoveGroupConfirmWindowPS.class;
	}

	/**
	 * @see com.idega.presentation.StatefullPresentation#getPresentationState(com.idega.idegaweb.IWUserContext)
	 */
	public IWPresentationState getPresentationState(IWUserContext iwuc) {
		if (this.presentationState == null) {
			try {
				IWStateMachine stateMachine = (IWStateMachine) IBOLookup
						.getSessionInstance(iwuc, IWStateMachine.class);
				this.presentationState = stateMachine.getStateFor(
						getCompoundId(), MoveGroupConfirmWindowPS.class);
			} catch (RemoteException re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		return this.presentationState;
	}

}