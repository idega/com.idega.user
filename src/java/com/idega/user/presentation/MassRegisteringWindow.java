package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
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
    private String ACTION_NEXT ="mrw_act_nx";
    private String ACTION_SAVE ="mrw_act_sv";
    private List failedInserts;
    private Group group;
    private IWResourceBundle iwrb;
    private String mainTableStyle = "main";
    private int numberOfRows = 18;
    private String PARAMETER_PID = "mrw_pid";
    private String PARAMETER_SAVE = "mrw_sv";
    private String PARAMETER_STATUS = "mrw_sta";
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
        iwrb = getResourceBundle(iwc);
        super.main(iwc);
        setTitle("Mass Registering Window");
        addTitle(iwrb.getLocalizedString("mass_registering_window", "Mass Registering Window"), IWConstants.BUILDER_FONT_STYLE_TITLE);
        
        init(iwc);
        if (group != null) {
            
            String action = iwc.getParameter(ACTION);
            
            if (action == null) {
                addForm(iwc, false);	
            } else if (action.equals(ACTION_CANCEL))  {
                close();
            }      
            else if (action.equals(ACTION_NEXT)) {
                addForm(iwc, true);
            }else if (action.equals(ACTION_SAVE)) {
                if (handleInsert(iwc)) {
                    addForm(iwc, false);
                }else {
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
        table.setStyleClass(mainTableStyle);
        table.setWidth(Table.HUNDRED_PERCENT);
        //table.setHeight(560);
        table.setBorder(0);
        
        boolean foundUser = false;
        
        int row = 1;
        if (verifyForm) {
            table.add(formatText(iwrb.getLocalizedString("save", "Save")), 1, row);
            table.add(formatText(iwrb.getLocalizedString("user.user_name", "User name")), 3, row);
        }else {
            table.add(formatText(iwrb.getLocalizedString("row", "Row")), 1, row);
            table.add(formatText(iwrb.getLocalizedString("personal.id.number", "Personal ID number")), 3, row);
        }
        table.setWidth(2, "10");
        table.setWidth(4, "10");
        table.add(formatText(iwrb.getLocalizedString("user.status","Status")), 5, row);
        
        TextInput pid = new TextInput();
        UserStatusDropdown status = new UserStatusDropdown("noname");
        CheckBox check;
        String sPid;
        String sStat;
        User user;
        Status stat;
        for (int i = 1; i <= numberOfRows; i++) {
            /** Listing valid PersonalIDs */
            if (verifyForm) { 
                sPid = iwc.getParameter(PARAMETER_PID+"_"+i);
                sStat = iwc.getParameter(PARAMETER_STATUS+"_"+i);
                if (sPid != null && !sPid.equals("")) {
                    try {
                        ++row;
                        user = uHome.findByPersonalID(sPid);
                        if (UserStatusDropdown.NO_STATUS_KEY.equals(sStat))  {
                            stat = null;
                        }
                        else {
                            stat = sHome.findByPrimaryKey(new Integer(sStat));
                        }
                        check = new CheckBox(PARAMETER_SAVE+"_"+i);
                        check.setStyleAttribute(STYLE_2);
                        check.setChecked(true);
                        
                        table.add(check, 1, row);
                        table.add(formatText(user.getName()), 3, row);
                        if (stat != null)   {
                            table.add(formatText(iwrb.getLocalizedString(stat.getStatusKey(), stat.getStatusKey())), 5, row);
                        }
                        form.maintainParameter(PARAMETER_PID+"_"+i);
                        form.maintainParameter(PARAMETER_STATUS+"_"+i);
                        foundUser = true;
                    } catch (FinderException e) {
                        //e.printStackTrace(System.err);
                        table.add(formatText(iwrb.getLocalizedString("user.user_not_found","User not found")+" ("+sPid+")"), 3, row);
                    }
                }
            } 
            /** Creating and adding inputs to form */
            else {
                ++row;
                status  = new UserStatusDropdown(PARAMETER_STATUS+"_"+i);
                status.setStyleAttribute(STYLE_2);
                pid = new TextInput(PARAMETER_PID+"_"+i);
                pid.setAsIcelandicSSNumber(iwrb.getLocalizedString("user.pid_incorrect_in_row","Personal ID not correct for user in row")+" "+i);
                pid.setStyleAttribute(STYLE_2);
                pid.setMaxlength(10);
                table.add(formatText(Integer.toString(i)), 1, row);
                table.add(pid, 3, row);	
                table.add(status, 5, row);	
            }
        }
        
        ++row;
        ++row;
        
    		Table bottomTable = new Table();
    		bottomTable.setCellpadding(0);
    		bottomTable.setCellspacing(5);
    		bottomTable.setWidth(Table.HUNDRED_PERCENT);
    		bottomTable.setHeight(39);
    		bottomTable.setStyleClass(mainTableStyle);
    		bottomTable.add(help,1,1);
    		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
    		
    		bottomTable.add(new SubmitButton(iwrb.getLocalizedImageButton("cancel", "Cancel"), ACTION, ACTION_CANCEL), 2, 1 );
    		bottomTable.add(Text.getNonBrakingSpace(),2,1);
    		  
        table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
        table.setRowVerticalAlignment(row,Table.VERTICAL_ALIGN_TOP);
        if (verifyForm) {
            table.mergeCells(1, row, 2, row);
            bottomTable.add(new BackButton(iwrb.getLocalizedImageButton("back", "Back")), 1 , 1);
            if (foundUser) {
            	bottomTable.add(new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"), ACTION, ACTION_SAVE), 2, 1);
            }
        }else {		
        	bottomTable.add(new SubmitButton(iwrb.getLocalizedImageButton("next", "Next"), ACTION, ACTION_NEXT), 2, 1);
        }
        // add close button
        
	    		mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
	    		mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
        mainTable.add(table,1,1);
        mainTable.add(bottomTable,1,3);
        form.add(mainTable);
        add(form,iwc);
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
        
        table.add(formatText(iwrb.getLocalizedString("save_failed_for_users", "Save failed for the following user/s:")), 1, row);
        Iterator iter = failedInserts.iterator();
        User user;
        while (iter.hasNext()) {
            ++row;
            user = (User) iter.next();
            table.add(user.getName()+" ("+user.getPersonalID()+")", 1, row);	
        }
        
        ++row;
        table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
        table.add(new SubmitButton(iwrb.getLocalizedImageButton("back", "Back")), 1, row);
        
        form.add(table);
        add(form,iwc);
    }
    
    private boolean handleInsert(IWContext iwc) throws RemoteException{
        String sPid;
        String sStat;
        User user;
        Status stat;
        UserStatusBusiness usb = (UserStatusBusiness) IBOLookup.getServiceInstance(iwc, UserStatusBusiness.class);
        failedInserts = new Vector();
        boolean errorFree = true;
        
        for (int i = 1; i <= numberOfRows; i++) {
            if (iwc.isParameterSet(PARAMETER_SAVE+"_"+i)) {
                try {
                    sPid = iwc.getParameter(PARAMETER_PID+"_"+i);
                    sStat = iwc.getParameter(PARAMETER_STATUS+"_"+i);
                    user = uHome.findByPersonalID(sPid);
                    if (UserStatusDropdown.NO_STATUS_KEY.equals(sStat))  {
                        stat = null;
                    }
                    else {
                        stat = sHome.findByPrimaryKey(new Integer(sStat));
                    }					
                    group.addGroup(user);
                    if ( stat != null && (! usb.setUserGroupStatus(user.getID(), ((Integer)group.getPrimaryKey()).intValue(), ((Integer)stat.getPrimaryKey()).intValue()) )) {
                        failedInserts.add(user);
                        errorFree = false;
                    }
                    
                    if (user.getPrimaryGroup() == null) {
                        user.setPrimaryGroup(group);
                        user.store();
                    }
                } catch (FinderException e) {
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
                uHome = (UserHome) IDOLookup.getHome(User.class);
                sHome = (StatusHome) IDOLookup.getHome(Status.class);
                GroupHome gHome = (GroupHome) IDOLookup.getHome(Group.class);
                group = gHome.findByPrimaryKey(new Integer(sGroupId));
            } catch (IDOLookupException e) {
                e.printStackTrace(System.err);
            } catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            } catch (FinderException e) {
                e.printStackTrace(System.err);
            }
        }
        iwrb = getResourceBundle(iwc);
    }
    
    
}