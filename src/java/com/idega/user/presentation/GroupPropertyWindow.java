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
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.data.UserGroupPlugIn;
import com.idega.util.IWColor;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GroupPropertyWindow extends TabbedPropertyWindow {

	public static final String PARAMETERSTRING_GROUP_ID = "ic_group_id";
  public static final String SESSION_ADDRESS = "ic_group_property_window";
  
	public GroupPropertyWindow() {
		super();
		setBackgroundColor(new IWColor(207, 208, 210));
	}
  
  public String getSessionAddressString() {
    return SESSION_ADDRESS;
  }

	public void initializePanel(IWContext iwc, TabbedPropertyPanel panel) {
		try {
			int count = 0;
			panel.addTab(new GeneralGroupInfoTab(), count++, iwc);

			//	temp shit
			String id = iwc.getParameter(PARAMETERSTRING_GROUP_ID);
			int groupId = Integer.parseInt(id);
			Group group = getGroupBusiness(iwc).getGroupByGroupID(groupId);

			Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForGroupTypeString(group.getGroupType());
			Iterator iter = plugins.iterator();

			while (iter.hasNext()) {
				UserGroupPlugIn element = (UserGroupPlugIn) iter.next();

				UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, Class.forName(element.getBusinessICObject().getClassName()));

				List tabs = pluginBiz.getGroupPropertiesTabs(group);
				Iterator tab = tabs.iterator();
				while (tab.hasNext()) {
					UserGroupTab el = (UserGroupTab) tab.next();
					panel.addTab(el, count++, iwc);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void main(IWContext iwc) throws Exception {
		String id = iwc.getParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID);
		if (id != null) {
			int newId = Integer.parseInt(id);
			PresentationObject[] obj = this.getAddedTabs();
			for (int i = 0; i < obj.length; i++) {
				PresentationObject mo = obj[i];
				if (mo instanceof UserGroupTab && ((UserGroupTab) mo).getGroupId() != newId) {
					((UserGroupTab) mo).setGroupId(newId);
				}
			}
		}
	}

	/**
	 * @see com.idega.presentation.TabbedPropertyWindow#disposeOfPanel(com.idega.presentation.IWContext)
	 */
	public boolean disposeOfPanel(IWContext iwc) {
		return iwc.isParameterSet(PARAMETERSTRING_GROUP_ID);
	}

	public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException {
		return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}
}