package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.FinderException;
import javax.transaction.TransactionManager;

import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.transaction.IdegaTransactionManager;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserStatusBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.Status;
import com.idega.user.data.StatusHome;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;

/**
 * @author gimmi
 * 
 * Hint: This class does not use the event system at all.
 * 
 */
public class MassRegisteringWindow extends StyledIWAdminWindow {

	private static final String HELP_TEXT_KEY = "mass_registering_window";
	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	public static final String PARAMETER_GROUP_ID = GroupPropertyWindow.PARAMETERSTRING_GROUP_ID;
	public final static String STYLE_2 = "font-family:arial; font-size:8pt; color:#000000; text-align: justify;";
	private String ACTION = "mrw_act";
	private String ACTION_CANCEL = "mrw_act_cc";
	private String ACTION_NEXT = "mrw_act_nx";
	private String ACTION_SAVE = "mrw_act_sv";
	private List failedInserts;
	private Group group;
	private IWResourceBundle iwrb;
	private String mainTableStyle = "main";
	private int numberOfRows = 18;
	private String PARAMETER_PID = "mrw_pid";
	private String PARAMETER_SAVE = "mrw_sv";
	private String PARAMETER_STATUS = "mrw_sta";
	private String PARAMETER_EMAIL = "mrw_email";
	private StatusHome sHome;
	private UserHome uHome;

	public MassRegisteringWindow() {
		setHeight(650);
		setWidth(420);
		setResizable(true);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public void main(IWContext iwc) throws Exception {
		this.iwrb = getResourceBundle(iwc);
		super.main(iwc);
		setTitle("Mass Registering Window");
		addTitle(this.iwrb.getLocalizedString("mass_registering_window", "Mass Registering Window"), TITLE_STYLECLASS);
		init(iwc);
		if (this.group != null) {
			String action = iwc.getParameter(this.ACTION);
			if (action == null) {
				addForm(iwc, false);
			}
			else if (action.equals(this.ACTION_CANCEL)) {
				close();
			}
			else if (action.equals(this.ACTION_NEXT)) {
				addForm(iwc, true);
			}
			else if (action.equals(this.ACTION_SAVE)) {
				if (handleInsert(iwc)) {
					addForm(iwc, false);
				}
				else {
					errorList();
				}
				setOnLoad("window.opener.parent.frames['iwb_main'].location.reload()");
			}
		}
	}

	private void addForm(IWContext iwc, boolean verifyForm) {
		Help help = getHelp(HELP_TEXT_KEY);
		Form form = new Form();
		form.maintainParameter(PARAMETER_GROUP_ID);
		Table mainTable = new Table();
		mainTable.setWidth(380);
		mainTable.setHeight(290);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		Table table = new Table();
		table.setCellpadding(2);
		table.setCellspacing(0);
		table.setStyleClass(this.mainTableStyle);
		table.setWidth(Table.HUNDRED_PERCENT);
		// table.setHeight(560);
		table.setBorder(0);
		boolean foundUser = false;
		int row = 1;
		if (verifyForm) {
			table.add(formatText(this.iwrb.getLocalizedString("save", "Save")), 1, row);
			table.add(formatText(this.iwrb.getLocalizedString("user.user_name", "User name")), 3, row);
		}
		else {
			table.add(formatText(this.iwrb.getLocalizedString("row", "Row")), 1, row);
			table.add(formatText(this.iwrb.getLocalizedString("personal.id.number", "Personal ID number")), 3, row);
		}
		table.setWidth(2, "10");
		table.setWidth(4, "10");
		table.setWidth(6, "10");
		table.add(formatText(this.iwrb.getLocalizedString("user.status", "Status")), 5, row);
		table.add(formatText(this.iwrb.getLocalizedString("user.email", "E-mail")), 7, row);
		TextInput pid = new TextInput();
		TextInput email = new TextInput();
		UserStatusDropdown status = new UserStatusDropdown("noname");
		CheckBox check;
		String sPid;
		String sStat;
		String sEmail;
		User user;
		Status stat;
		for (int i = 1; i <= this.numberOfRows; i++) {
			/** Listing valid PersonalIDs */
			if (verifyForm) {
				sPid = iwc.getParameter(this.PARAMETER_PID + "_" + i);
				sStat = iwc.getParameter(this.PARAMETER_STATUS + "_" + i);
				sEmail = iwc.getParameter(this.PARAMETER_EMAIL + "_" + i);
				if (sPid != null && !sPid.equals("")) {
					try {
						++row;
						user = this.uHome.findByPersonalID(sPid);
						if (UserStatusDropdown.NO_STATUS_KEY.equals(sStat)) {
							stat = null;
						}
						else {
							stat = this.sHome.findByPrimaryKey(new Integer(sStat));
						}
						check = new CheckBox(this.PARAMETER_SAVE + "_" + i);
						check.setStyleAttribute(STYLE_2);
						check.setChecked(true);
						table.add(check, 1, row);
						table.add(formatText(user.getName()), 3, row);
						if (stat != null) {
							table.add(formatText(this.iwrb.getLocalizedString(stat.getStatusKey(), stat.getStatusKey())), 5,
									row);
						}
						if (sEmail != null) {
							table.add(formatText(sEmail), 7, row);
						}
						form.maintainParameter(this.PARAMETER_PID + "_" + i);
						form.maintainParameter(this.PARAMETER_STATUS + "_" + i);
						form.maintainParameter(this.PARAMETER_EMAIL + "_" + i);
						foundUser = true;
					}
					catch (FinderException e) {
						// e.printStackTrace(System.err);
						table.add(formatText(this.iwrb.getLocalizedString("user.user_not_found", "User not found") + " ("
								+ sPid + ")"), 3, row);
					}
				}
			}
			/** Creating and adding inputs to form */
			else {
				++row;
				status = new UserStatusDropdown(this.PARAMETER_STATUS + "_" + i);
				status.setStyleAttribute(STYLE_2);
				pid = new TextInput(this.PARAMETER_PID + "_" + i);
				pid.setAsIcelandicSSNumber(this.iwrb.getLocalizedString("user.pid_incorrect_in_row",
						"Personal ID not correct for user in row")
						+ " " + i);
				pid.setStyleAttribute(STYLE_2);
				pid.setMaxlength(10);
				
				email = new TextInput(this.PARAMETER_EMAIL + "_" + i);
				email.setAsEmail(this.iwrb.getLocalizedString("user.email_incorrect_in_row",
						"E-mail not correct for user in row")
						+ " " + i);
				email.setStyleAttribute(STYLE_2);
				
				table.add(formatText(Integer.toString(i)), 1, row);
				table.add(pid, 3, row);
				table.add(status, 5, row);
				table.add(email, 7, row);
			}
		}
		++row;
		++row;
		
		Table buttonTable = new Table(3, 1);
		buttonTable.setCellpadding(0);
		buttonTable.setCellspacing(0);
		buttonTable.setWidth(2, 5);
				
		Table bottomTable = new Table(2,1);
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setHeight(30);
		bottomTable.setStyleClass(this.mainTableStyle);
		bottomTable.add(help, 1, 1);
		bottomTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(buttonTable,2,1);
		
		
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_TOP);
		
		if (verifyForm) {
			table.mergeCells(1, row, 2, row);
			StyledButton backButton = new StyledButton(new BackButton(this.iwrb.getLocalizedString("back", "Back")));
			buttonTable.add(backButton, 1, 1);
			if (foundUser) {
				StyledButton saveButton = new StyledButton(new SubmitButton(this.iwrb.getLocalizedString("save", "Save"), this.ACTION, this.ACTION_SAVE));
				buttonTable.add(saveButton, 3,1);
			}
		}
		else {
			StyledButton cancelButton = new StyledButton(new SubmitButton(this.iwrb.getLocalizedString("cancel", "Cancel"), this.ACTION, this.ACTION_CANCEL));
			buttonTable.add(cancelButton, 1, 1);
			StyledButton nextButton = new StyledButton(new SubmitButton(this.iwrb.getLocalizedString("next", "Next"), this.ACTION, this.ACTION_NEXT));
			buttonTable.add(nextButton, 3, 1);
		}
		// add close button
		mainTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
		mainTable.add(table, 1, 1);
		mainTable.add(bottomTable, 1, 3);
		form.add(mainTable);
		add(form, iwc);
	}

	private void errorList() {
		IWContext iwc = IWContext.getInstance();
		Form form = new Form();
		form.maintainParameter(PARAMETER_GROUP_ID);
		Table table = new Table();
		table.setCellpadding(2);
		table.setCellspacing(0);
		table.setBorder(0);
		int row = 1;
		table.add(
				formatText(this.iwrb.getLocalizedString("save_failed_for_users", "Save failed for the following user/s:")),
				1, row);
		Iterator iter = this.failedInserts.iterator();
		FailedRegisterUser user;
		while (iter.hasNext()) {
			++row;
			user = (FailedRegisterUser) iter.next();
			table.add(user.user.getName() + " (" + user.user.getPersonalID() + ")", 1, row);
			table.add(user.msg, 2, row);
		}
		++row;
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(new SubmitButton(this.iwrb.getLocalizedImageButton("back", "Back")), 1, row);
		form.add(table);
		add(form, iwc);
	}

	private boolean handleInsert(IWContext iwc) throws RemoteException {
		String sPid;
		String sStat;
		String sEmail;
		User user;
		Status stat;
		UserStatusBusiness usb = (UserStatusBusiness) IBOLookup.getServiceInstance(iwc, UserStatusBusiness.class);
		this.failedInserts = new Vector();
		boolean errorFree = true;
		UserBusiness userBuis = getUserBusiness(iwc);
		for (int i = 1; i <= this.numberOfRows; i++) {
			if (iwc.isParameterSet(this.PARAMETER_SAVE + "_" + i)) {
				try {
					sPid = iwc.getParameter(this.PARAMETER_PID + "_" + i);
					sStat = iwc.getParameter(this.PARAMETER_STATUS + "_" + i);
					sEmail = iwc.getParameter(this.PARAMETER_EMAIL + "_" + i);
					user = this.uHome.findByPersonalID(sPid);
					if (UserStatusDropdown.NO_STATUS_KEY.equals(sStat)) {
						stat = null;
					}
					else {
						stat = this.sHome.findByPrimaryKey(new Integer(sStat));
					}
					String failedMsg = userBuis.isUserSuitedForGroup(user, this.group);
					if (failedMsg == null) {
						TransactionManager transaction = IdegaTransactionManager.getInstance();
						try {
							// START A TRANSACTION!
							transaction.begin();
							this.group.addGroup(user);
							if (stat != null
									&& (!usb.setUserGroupStatus(user.getID(),
											((Integer) this.group.getPrimaryKey()).intValue(),
											((Integer) stat.getPrimaryKey()).intValue(), iwc.getCurrentUserId()))) {
								this.failedInserts.add(new FailedRegisterUser(user, ""));
								errorFree = false;
							}
							if (user.getPrimaryGroup() == null) {
								user.setPrimaryGroup(this.group);
								user.store();
							}
							
							if (sEmail != null && !"".equals(sEmail)) {
								getUserBusiness(iwc).updateUserMail(user, sEmail);
							}
							
							getUserBusiness(iwc).callAllUserGroupPluginAfterUserCreateOrUpdateMethod(user,this.group);
							transaction.commit();
						}// try ends
						catch (Exception e) {
							e.printStackTrace();
							try {
								transaction.rollback();
							}
							catch (Exception e1) {
								e1.printStackTrace();
							}
							String msg = e.getMessage();
							String errorMessage = this.iwrb.getLocalizedString(
									"new_user.transaction_rollback",
									"User could not be created/added because of the error: ")
									+ msg
									+ this.iwrb.getLocalizedString("new_user.try_again"," Please try again or contact the system administrator if you think it is a server error.");
							this.failedInserts.add(new FailedRegisterUser(user, errorMessage));
							errorFree = false;
						}
					}
					else {
						this.failedInserts.add(new FailedRegisterUser(user, failedMsg));
						errorFree = false;
					}
				}
				catch (FinderException e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return errorFree;
	}

	private void init(IWContext iwc) {
		String sGroupId = iwc.getParameter(PARAMETER_GROUP_ID);
		if (sGroupId != null) {
			try {
				this.uHome = (UserHome) IDOLookup.getHome(User.class);
				this.sHome = (StatusHome) IDOLookup.getHome(Status.class);
				GroupHome gHome = (GroupHome) IDOLookup.getHome(Group.class);
				this.group = gHome.findByPrimaryKey(new Integer(sGroupId));
			}
			catch (IDOLookupException e) {
				e.printStackTrace(System.err);
			}
			catch (NumberFormatException e) {
				e.printStackTrace(System.err);
			}
			catch (FinderException e) {
				e.printStackTrace(System.err);
			}
		}
		this.iwrb = getResourceBundle(iwc);
	}

	private class FailedRegisterUser {

		String msg;
		User user;

		public FailedRegisterUser(User user, String msg) {
			this.msg = msg;
			this.user = user;
		}
	}
}