/*
 * $Id: UserStatusTab.java,v 1.2.2.1 2007/01/12 19:31:51 idegaweb Exp $
 *
 * Copyright (C) 2000-2003 Idega Software. All Rights Reserved.
 *
 * This software is the proprietary information of Idega Software.
 * Use is subject to license terms.
 */
package com.idega.user.presentation;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.ejb.FinderException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SelectDropdown;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.business.UserStatusBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.Status;
import com.idega.user.data.StatusHome;

/**
 * A simple tab to change a users status within a group.
 */
public class UserStatusTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_status_tab_name";
	private static final String DEFAULT_TAB_NAME = "Status";
	
	private static final String HELP_TEXT_KEY = "user_status_tab";


	private Text _groupField;
	private SelectDropdown _statusField;

	private Text _groupText;
	private Text _statusText;

	private String _groupFieldName;
	private String _statusFieldName;

	public UserStatusTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#initializeFieldNames()
	 */
	public void initializeFieldNames() {
		this._groupFieldName = "usr_grp_status";
		this._statusFieldName = "usr_stat_status";
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#initializeFieldValues()
	 */
	public void initializeFieldValues() {
		this.fieldValues = new Hashtable();
		this.fieldValues.put(this._statusFieldName, "");
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#updateFieldsDisplayStatus()
	 */
	public void updateFieldsDisplayStatus() {
		if (getGroupID() > 0) {
			Group selectedGroup = getGroup();
			if (selectedGroup != null) {
				this._groupField.setText(selectedGroup.getName());
			}
		}
		else {
			IWContext iwc = IWContext.getInstance();
			IWResourceBundle iwrb = getResourceBundle(iwc);
			this._groupField.setText(iwrb.getLocalizedString("user_status_bar.no_group_selected","No group selected"));
		}
		this._statusField.setSelectedOption((String) this.fieldValues.get(this._statusFieldName));
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#initializeFields()
	 */
	public void initializeFields() {
		this._groupField = new Text(); //see initFieldContents
		this._statusField = new SelectDropdown(this._statusFieldName);

		IWContext iwc = IWContext.getInstance();
		List status = null;
		try {
			status = (List) ((StatusHome)IDOLookup.getHome(Status.class)).findAll();
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		if (status != null) {
			if (status.size() > 0) {
				final IWResourceBundle iwrb = getResourceBundle(iwc);
				this._statusField.addOption(new SelectOption(" ",-1));
				
				
				final Collator collator = Collator.getInstance(iwc.getLocale());
				Collections.sort(status,new Comparator() {
					public int compare(Object arg0, Object arg1) {
						return collator.compare(iwrb.getLocalizedString("usr_stat_" + ((Status) arg0).getStatusKey(), ((Status) arg0).getStatusKey()), iwrb.getLocalizedString("usr_stat_" + ((Status) arg1).getStatusKey(), ((Status) arg1).getStatusKey()));
					}				
				});
				
				Iterator it = status.iterator();
				while (it.hasNext()) {
					Status s = (Status)it.next();
					String n = s.getStatusKey();
					if (n != null) {
						String l = iwrb.getLocalizedString("usr_stat_" + n, n);
						this._statusField.addOption(new SelectOption(l, ((Integer) s.getPrimaryKey()).intValue()));
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#initializeTexts()
	 */
	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		this._groupText = new Text(iwrb.getLocalizedString(this._groupFieldName, "Group"));
		this._groupText.setBold();
		
		this._statusText = new Text(iwrb.getLocalizedString(this._statusFieldName, "Status"));
		this._statusText.setBold();
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#lineUpFields()
	 */
	public void lineUpFields() {
		empty();

		Table t = new Table(2, 4);
		t.setCellpadding(5);
		t.setCellspacing(0);
		t.add(this._groupText, 1, 1);
		t.add(this._groupField, 2, 1);
		t.add(this._statusText, 1, 2);
		t.add(this._statusField, 2, 2);
		add(t);
	}

	public void main(IWContext iwc) {
		if (getPanel() != null) {
			getPanel().addHelpButton(getHelpButton());		
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.util.datastructures.Collectable#collect(com.idega.presentation.IWContext)
	 */
	public boolean collect(IWContext iwc) {
		if (iwc != null) {
			String status = iwc.getParameter(this._statusFieldName);

			if (status != null) {
				this.fieldValues.put(this._statusFieldName, status);
			}
			else {
				this.fieldValues.put(this._statusFieldName, " ");
			}

			updateFieldsDisplayStatus();
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.util.datastructures.Collectable#store(com.idega.presentation.IWContext)
	 */
	public boolean store(IWContext iwc) {
		try {
			String status = (String)this.fieldValues.get(this._statusFieldName);
	
			if (status != null) {
				int user_id = this.getUserId();
				int group_id = this.getGroupID();
				int status_id = Integer.parseInt(status);
				
				getUserStatusBusiness(iwc).setUserGroupStatus(user_id,group_id,status_id,iwc.getCurrentUserId()); 	
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.UserTab#initFieldContents()
	 */
	public void initFieldContents() {
		IWContext iwc = IWContext.getInstance();
		this.fieldValues = new Hashtable();
		
		int status_id = -1;
		try {
			int user_id = getUserId();
			int group_id = getGroupID();
			status_id = getUserStatusBusiness(iwc).getUserGroupStatus(user_id,group_id);
		}
		catch(Exception e) {
			status_id = -1;
		}
		
		if (status_id > 0) {
			this.fieldValues.put(this._statusFieldName, Integer.toString(status_id));
		}
		else {
			this.fieldValues.put(this._statusFieldName, "");
		}

		updateFieldsDisplayStatus();
	}
	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle(TAB_NAME);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
		
	}
	
	public UserStatusBusiness getUserStatusBusiness(IWApplicationContext iwc){
		UserStatusBusiness business = null;
		if(business == null){
			try{
				business = (UserStatusBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,UserStatusBusiness.class);
			}
			catch(java.rmi.RemoteException rme){
				throw new RuntimeException(rme.getMessage());
			}
		}
		return business;
	}
	
}