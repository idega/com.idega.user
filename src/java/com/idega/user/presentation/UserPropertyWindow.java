package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.block.cal.presentation.CalPropertyWindow;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.TabbedPropertyWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.User;
import com.idega.user.data.UserGroupPlugIn;
import com.idega.util.IWColor;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class UserPropertyWindow extends TabbedPropertyWindow implements CalPropertyWindow {

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
		

		try { //temporary before plugins work
			panel.addTab(new GeneralUserInfoTab(), count, iwc);
//			panel.addTab(new UserImageTab(), ++count, iwc); //not needed because image added to the general tab - birna
			panel.addTab(new AddressInfoTab(), ++count, iwc);
			panel.addTab(new UserPhoneTab(), ++count, iwc);
			panel.addTab(new UserGroupList(), ++count, iwc);

			panel.addTab((PresentationObject) Class.forName("is.idega.idegaweb.member.presentation.UserFamilyTab").newInstance(), ++count, iwc);
			//panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserFinanceTab").newInstance() ,++count,iwc);
			//panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserHistoryTab").newInstance() ,++count,iwc);

			//temp 
			String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
			int userId = Integer.parseInt(id);
			
			//get the user
			User user = getUserBusiness(iwc).getUser(userId);
			
			
			//get plugins
			Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForUser(user);
			Iterator iter = plugins.iterator();

			while (iter.hasNext()) {
				UserGroupPlugIn element = (UserGroupPlugIn) iter.next();

				UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, Class.forName(element.getBusinessICObject().getClassName()));

				List tabs = pluginBiz.getUserPropertiesTabs(user);
				if (tabs != null) {
					Iterator tab = tabs.iterator();
					while (tab.hasNext()) {
						UserTab el = (UserTab) tab.next();						
						panel.addTab(el, ++count, iwc);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		UserLoginTab ult = new UserLoginTab();
		panel.addTab(ult, ++count, iwc);

	}

	public void main(IWContext iwc) throws Exception {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
		String grpid = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		
		

		PresentationObject[] obj = this.getAddedTabs();

		int iGrpId = -1;
		if (grpid != null) iGrpId = Integer.parseInt(grpid);
			
		if (id != null) {
			userId = Integer.parseInt(id);
			int newId = Integer.parseInt(id);		
			for (int i = 0; i < obj.length; i++) {
				PresentationObject mo = obj[i];
				if (mo instanceof UserTab && ((UserTab) mo).getUserId() != newId) {
					mo.setIWContext(iwc);
					((UserTab) mo).setUserID(newId);
					((UserTab) mo).setGroupID(iGrpId);					
				}
			}
		}
		// ask one of the tab for the user id because the user id parameter is not set when navigating within the user property window 
		// that is switching from one tab to another
		if (obj.length > 0) {
			userId = ((UserTab) obj[0]).getUserId();
			User user = getUserBusiness(iwc).getUser(userId);
			String userName = user.getName();
			if(userName!=null) {
			    addTitle(userName);
			}
		}
		addTitle(iwrb.getLocalizedString("user_property_window", "User Property Window"), IWConstants.BUILDER_FONT_STYLE_TITLE);

	}

	/**
	 * overrides the default behaviour to check for edit permissions
	 */
	protected TabbedPropertyPanel getPanelInstance(IWContext iwc) {
        boolean useOkButton = false;
        boolean useApplyButton = false;
        boolean useCancelButton = true;
        
//		check if we have edit permissions, otherwise disable saving
		String groupId = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		
		boolean isAdmin = iwc.isSuperAdmin();
		if(groupId!=null && !"-1".equals(groupId) && !isAdmin) {
		    try {
                useOkButton = iwc.getAccessController().hasEditPermissionFor(getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(groupId)), iwc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
		    useApplyButton = useOkButton;
		}
		else {
		    if(isAdmin) {//only super admin can edit without permission
		        useOkButton = true;
		        useApplyButton = true;
		    }
		    
		}
		
		
		return new TabbedPropertyPanel(getSessionAddressString(),iwc,useOkButton,useCancelButton,useApplyButton);
    }

	/* (non-Javadoc)
	 * @see com.idega.block.cal.presentation.CalPropertyWindow#getIdParameter()
	 */
	public String getIdParameter() {
		return PARAMETERSTRING_USER_ID;
	}



}
