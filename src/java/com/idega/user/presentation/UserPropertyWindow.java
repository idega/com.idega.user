package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.TabbedPropertyWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.User;
import com.idega.user.data.UserGroupPlugIn;
import com.idega.util.IWColor;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserPropertyWindow extends TabbedPropertyWindow {

	public static final String PARAMETERSTRING_SELECTED_GROUP_ID = "selected_ic_group_id";

	public static final String PARAMETERSTRING_USER_ID = "ic_user_id";

	public static final String SESSION_ADDRESS = "ic_user_property_window";

	public UserPropertyWindow() {
		super(410, 550);
		super.setResizable(true);
		this.setBackgroundColor(new IWColor(207, 208, 210));
	}

	public String getSessionAddressString() {
		return SESSION_ADDRESS;
	}

	public void initializePanel(IWContext iwc, TabbedPropertyPanel panel) {
		int count = 0;
		GeneralUserInfoTab genTab = new GeneralUserInfoTab();

		try { //temporary before plugins work
			panel.addTab(genTab, count, iwc);
			panel.addTab(new UserImageTab(), ++count, iwc);
			panel.addTab(new AddressInfoTab(), ++count, iwc);
			panel.addTab(new UserPhoneTab(), ++count, iwc);
			panel.addTab(new UserGroupList(), ++count, iwc);

			panel.addTab((PresentationObject) Class.forName("is.idega.idegaweb.member.presentation.UserFamilyTab").newInstance(), ++count, iwc);
			//panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserFinanceTab").newInstance() ,++count,iwc);
			//panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserHistoryTab").newInstance() ,++count,iwc);

			//temp 
			String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
			int userId = Integer.parseInt(id);
			User user = getUserBusiness(iwc).getUser(userId);

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
		ult.displayLoginInfoSettings();
		panel.addTab(ult, ++count, iwc);

	}

	public void main(IWContext iwc) throws Exception {
		String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
		String grpid = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		int iGrpId = -1;
		if (grpid != null) iGrpId = Integer.parseInt(grpid);
	
		if (id != null) {
			int newId = Integer.parseInt(id);
			PresentationObject[] obj = this.getAddedTabs();
			for (int i = 0; i < obj.length; i++) {
				PresentationObject mo = obj[i];
				if (mo instanceof UserTab && ((UserTab) mo).getUserId() != newId) {
					mo.setIWContext(iwc);
					((UserTab) mo).setUserID(newId);
					((UserTab) mo).setGroupID(iGrpId);
				}
			}
		}
	}

	public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException {
		return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}

	public UserBusiness getUserBusiness(IWApplicationContext iwac) throws RemoteException {
		return (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, UserBusiness.class);
	}

	/**
	 * @see com.idega.presentation.TabbedPropertyWindow#disposeOfPanel(com.idega.presentation.IWContext)
	 */
	public boolean disposeOfPanel(IWContext iwc) {
		return iwc.isParameterSet(PARAMETERSTRING_USER_ID);
	}

}
