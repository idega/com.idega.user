package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.TabbedPropertyWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.User;
import com.idega.util.IWColor;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class UserPropertyWindow extends TabbedPropertyWindow {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	public static final String PARAMETERSTRING_SELECTED_GROUP_ID = "selected_ic_group_id";
	public static final String PARAMETERSTRING_USER_ID = "ic_user_id";
	public static final String SESSION_ADDRESS = "ic_user_property_window";
	private int userId = -1;

	public UserPropertyWindow() {
		super(500, 600); //changed from super(410,550); - birna
		super.setResizable(true);
		super.setScrollbar(true);
		this.setBackgroundColor(new IWColor(207, 208, 210));
		
	}
	
	public void main(IWContext iwc) throws Exception {
	    IWResourceBundle iwrb = getResourceBundle(iwc);
	    String userIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
	    if(userIdString != null) {
			int userId = Integer.parseInt(userIdString);
		    User user = getUserBusiness(iwc).getUser(userId);
		    addTitle(user.getName(), TITLE_STYLECLASS);
		}
		setTitle(iwrb.getLocalizedString("user_property_window", "User Property Window"));
	}

//	public UserBusiness getUserBusiness(IWApplicationContext iwac) throws RemoteException {
//		return (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, UserBusiness.class);
//	}

	/**
	 * @see com.idega.presentation.TabbedPropertyWindow#disposeOfPanel(com.idega.presentation.IWContext)
	 */
	public boolean disposeOfPanel(IWContext iwc) {
		return iwc.isParameterSet(PARAMETERSTRING_USER_ID);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException {
		return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}

    public String getSessionAddressString() {
		return SESSION_ADDRESS;
	}

	public void initializePanel(IWContext iwc, TabbedPropertyPanel panel) {
		int count = 0;
		//IWResourceBundle iwrb = getResourceBundle(iwc);
		String userIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
		int userId = Integer.parseInt(userIdString);
		String groupIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		int groupId = -1;
		if (groupIdString != null){
			groupId = Integer.parseInt(groupIdString);
		}
		
//add the standard tabs and then from plugins
		UserTab userInfo = new GeneralUserInfoTab();
		userInfo.setPanel(panel);
		userInfo.setUserID(userId);
		userInfo.setGroupID(groupId);
		
		UserTab addressInfo = new AddressInfoTab();
		addressInfo.setPanel(panel);
		addressInfo.setUserID(userId);
		addressInfo.setGroupID(groupId);
		
		UserTab phone = new UserPhoneTab();
		phone.setPanel(panel);
		phone.setUserID(userId);
		phone.setGroupID(groupId);
		
		UserTab group = new UserGroupList();
		group.setPanel(panel);
		group.setUserID(userId);
		group.setGroupID(groupId);
		
		panel.addTab(userInfo, count, iwc);
		panel.addTab(addressInfo, ++count, iwc);
		panel.addTab(phone, ++count, iwc);
		panel.addTab(group, ++count, iwc);
		

		
		
		//get the user
		try {
			User user = getUserBusiness(iwc).getUser(userId);
			//METADATA TAB, only show if admin
			if(iwc.isSuperAdmin()){
				GenericMetaDataTab metaDataTab = new GenericMetaDataTab(user);
				metaDataTab.setPanel(panel);
				panel.addTab(metaDataTab,++count,iwc);
			}
			
			
			//get plugins
			Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForUser(user);
			Iterator iter = plugins.iterator();
			
			while (iter.hasNext()) {
				UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness) iter.next();
				
				List tabs = pluginBiz.getUserPropertiesTabs(user);
				if (tabs != null) {
					Iterator tab = tabs.iterator();
					while (tab.hasNext()) {
						UserTab el = (UserTab) tab.next();						
						el.setPanel(panel);
						el.setUserID(userId);
						el.setGroupID(groupId);
						panel.addTab(el, ++count, iwc);
					}
				}
			}
			
			//don't forget the login tab
			UserLoginTab ult = new UserLoginTab();
			ult.setPanel(panel);
			ult.setUserID(userId);
			ult.setGroupID(groupId);
			panel.addTab(ult, ++count, iwc);
			
		}
		catch (RemoteException remoteEx) {
			logError("[UserPropertyWindow] Could not look up services bean");
			throw new RuntimeException("[UserPropertyWindow] Could not look up services beans", remoteEx);
		}


	}


	/**
	 * overrides the default behaviour to check for edit permissions
	 */
	protected TabbedPropertyPanel getPanelInstance(IWContext iwc) {
        boolean useOkButton = false;
        boolean useApplyButton = false;
        boolean useCancelButton = true;
        boolean isAdmin = iwc.isSuperAdmin();
        
        TabbedPropertyPanel panelInSession = (TabbedPropertyPanel)iwc.getSessionAttribute(getSessionAddressString());
		if(panelInSession!=null){
			useOkButton = panelInSession.isOkButtonDisabled();
			useApplyButton = panelInSession.isApplyButtonDisabled();
			useCancelButton = panelInSession.isCancelButtonDisabled();
		}
		
		if(isAdmin) {//only super admin can edit without permission
	        useOkButton = true;
	        useApplyButton = true;
	    }
		
//		check if we have edit permissions, otherwise disable saving
		String groupId = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		if(groupId!=null && !"-1".equals(groupId) && !isAdmin) {
		    try {
                useOkButton = iwc.getAccessController().hasEditPermissionFor(getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(groupId)), iwc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
		    useApplyButton = useOkButton;
		}
		
		if(panelInSession!=null){
			panelInSession.disableApplyButton(!useApplyButton);
			panelInSession.disableOkButton(!useOkButton);
			panelInSession.disableCancelButton(!useCancelButton);
			return panelInSession;
		}
		else{
			return new TabbedPropertyPanel(getSessionAddressString(),iwc,useOkButton,useCancelButton,useApplyButton);
		}
    }

	/* (non-Javadoc)
	 * @see com.idega.block.cal.presentation.CalPropertyWindow#getIdParameter()
	 */
	public String getIdParameter() {
		return PARAMETERSTRING_USER_ID;
	}



}
