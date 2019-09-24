package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.TabbedPropertyWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserGroupPlugInBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.util.UserGroupPluginFormCollector;
import com.idega.util.IWColor;

/**
 * Title: User Copyright: Copyright (c) 2001 Company: idega.is
 *
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gudmundur Saemundsson</a>,<a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.5
 */
public class UserPropertyWindow extends TabbedPropertyWindow {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	public static final String PARAMETERSTRING_SELECTED_GROUP_ID = "selected_ic_group_id";
	public static final String PARAMETERSTRING_USER_ID = "ic_user_id";
	public static final String SESSION_ADDRESS = "ic_user_property_window";
	private int userId = -1;
	private int groupId = -1;

	public UserPropertyWindow() {
		super(500, 600); // changed from super(410,550); - birna
		super.setResizable(true);
		super.setScrollbar(true);
		this.setBackgroundColor(new IWColor(207, 208, 210));
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		try {
			IWResourceBundle iwrb = getResourceBundle(iwc);
			String userIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
			if (userIdString != null) {
				int userId = Integer.parseInt(userIdString);
				User user = getUserBusiness(iwc).getUser(userId);
				addTitle(user.getName(), TITLE_STYLECLASS);
			}
			setTitle(iwrb.getLocalizedString("user_property_window", "User Property Window"));

			if (this.panel.clickedApply() || this.panel.clickedOk()) {
	        	setOnLoad("window.opener.parent.frames['iwb_main'].location.reload()");
	        }
		} catch (Throwable e) {
			getLogger().log(Level.WARNING, "Error rendering " + getClass().getSimpleName(), e);
			throw e;
		}
	}

	/**
	 * @see com.idega.presentation.TabbedPropertyWindow#disposeOfPanel(com.idega.presentation.IWContext)
	 */
	@Override
	public boolean disposeOfPanel(IWContext iwc) {
		return iwc.isParameterSet(PARAMETERSTRING_USER_ID);
	}

	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException {
		return com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
	}

	@Override
	public String getSessionAddressString() {
		return SESSION_ADDRESS;
	}

	/**
	 * <p>
	 * Initializes the userId and groupId variables.
	 * This is by default called from initializePanel()
	 * </p>
	 * @param iwc
	 */
	protected void initializeUserAndGroup(IWContext iwc){
		String userIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
		int userId = Integer.parseInt(userIdString);
		String groupIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
		int groupId = -1;
		if (groupIdString != null) {
			groupId = Integer.parseInt(groupIdString);
		}
		setUserId(userId);
		setGroupId(groupId);
	}

	@Override
	public void initializePanel(IWContext iwc, TabbedPropertyPanel panel) {
		try {
			int count = 0;
			// IWResourceBundle iwrb = getResourceBundle(iwc);
			initializeUserAndGroup(iwc);
			int userId = getUserId();
			int groupId = getGroupId();

			// Collects the tab info and calls usergroupplugin methods
			User user = getUserBusiness(iwc).getUser(userId);
			panel.setCollector(new UserGroupPluginFormCollector(user));
			//we could also add the group if we want that to be notified of changes panel.setGroup(group);

			// add the standard tabs and then from plugins
			UserTab userInfo = new GeneralUserInfoTab(userId);
			userInfo.setPanel(panel);
			userInfo.setUserIDAndGroupID(userId, groupId);

			UserTab addressInfo = new AddressInfoTab();
			addressInfo.setPanel(panel);
			addressInfo.setUserIDAndGroupID(userId, groupId);

			UserTab phone = new UserPhoneTab();
			phone.setPanel(panel);
			phone.setUserIDAndGroupID(userId, groupId);

			UserTab group = new UserGroupList();
			group.setPanel(panel);
			group.setUserIDAndGroupID(userId, groupId);
			panel.addTab(userInfo, count, iwc);
			panel.addTab(addressInfo, ++count, iwc);
			panel.addTab(phone, ++count, iwc);
			panel.addTab(group, ++count, iwc);

			// METADATA TAB, only show if admin
			if (iwc.isSuperAdmin()) {
				GenericMetaDataTab metaDataTab = new GenericMetaDataTab(user);
				metaDataTab.setPanel(panel);
				panel.addTab(metaDataTab, ++count, iwc);
			}

			// get plugins for extra tabs
			Collection<UserGroupPlugInBusiness> plugins = getGroupBusiness(iwc).getUserGroupPluginsForUser(user);
			Iterator<UserGroupPlugInBusiness> iter = plugins.iterator();
			while (iter.hasNext()) {
				UserGroupPlugInBusiness pluginBiz = iter.next();
				List<?> tabs = pluginBiz.getUserPropertiesTabs(user);
				if (tabs != null) {
					Iterator<?> tab = tabs.iterator();
					while (tab.hasNext()) {
						UserTab el = (UserTab) tab.next();
						el.setPanel(panel);
						el.setUserIDAndGroupID(userId, groupId);
						panel.addTab(el, ++count, iwc);
					}
				}
			}

			// don't forget the login tab
			UserLoginTab ult = new UserLoginTab();
			ult.setPanel(panel);
			ult.setUserIDAndGroupID(userId, groupId);
			panel.addTab(ult, ++count, iwc);
		}
		catch (Throwable e) {
			getLogger().log(Level.WARNING, "Error initializing tab " + getClass().getSimpleName() + ". User ID: " + getUserId() + ", selected group ID: " + getGroupId(), e);
			throw new RuntimeException("[UserPropertyWindow] Could not look up services beans", e);
		}
	}

	/**
	 * overrides the default behaviour to check for edit permissions
	 */
	@Override
	protected TabbedPropertyPanel getPanelInstance(IWContext iwc) {
		try {
			boolean useOkButton = false;
			boolean useApplyButton = false;
			boolean useCancelButton = true;
			boolean isAdmin = iwc.isSuperAdmin();
			TabbedPropertyPanel panelInSession = (TabbedPropertyPanel) iwc.getSessionAttribute(getSessionAddressString());
			if (panelInSession != null) {
				useOkButton = panelInSession.isOkButtonDisabled();
				useApplyButton = panelInSession.isApplyButtonDisabled();
				useCancelButton = panelInSession.isCancelButtonDisabled();
			}
			if (isAdmin) {// only super admin can edit without permission
				useOkButton = true;
				useApplyButton = true;
			}
			if (!useOkButton && iwc.getApplicationSettings().getBoolean("ua.not_admin_can_save_users", false) && iwc.hasRole(StandardRoles.ROLE_KEY_USERADMIN)) {
				useOkButton = true;
			}
			// check if we have edit permissions, otherwise disable saving
			String groupId = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID);
			if (groupId != null && !"-1".equals(groupId) && !isAdmin) {
				try {
					useOkButton = iwc.getAccessController().hasEditPermissionFor(
							getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(groupId)), iwc);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				useApplyButton = useOkButton;
			}
			if (!useOkButton) {
				String userIdString = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
				if (userIdString != null && !userIdString.equals("")) {
					Integer selectedUserId = null;
					try {
					    selectedUserId = new Integer(userIdString);
					} catch (NumberFormatException e){
					    e.printStackTrace();
					}
					if (selectedUserId != null) {
						try {
						    User selectedUser = getUserBusiness(iwc).getUser(selectedUserId);
						    Collection<Group> parentGroupsOfSelectedUser = selectedUser.getParentGroups();
						    Iterator<Group> parentIter = parentGroupsOfSelectedUser.iterator();
						    while (parentIter.hasNext()) {
						        if (iwc.getAccessController().hasEditPermissionFor(parentIter.next(), iwc)) {
						            useOkButton = true;
						            break;
						        }
						    }
						} catch (RemoteException e) {
						    e.printStackTrace();
						}
					}
				}
			}
			if (panelInSession != null) {
				panelInSession.disableApplyButton(!useApplyButton);
				panelInSession.disableOkButton(!useOkButton);
				panelInSession.disableCancelButton(!useCancelButton);
				return panelInSession;
			}
			else {
				return new TabbedPropertyPanel(getSessionAddressString(), iwc, useOkButton, useCancelButton, useApplyButton);
			}
		} catch (Throwable t) {
			getLogger().log(Level.WARNING, "Error getting instance of panel", t);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.block.cal.presentation.CalPropertyWindow#getIdParameter()
	 */
	public String getIdParameter() {
		return PARAMETERSTRING_USER_ID;
	}


	/**
	 * @return Returns the groupId.
	 */
	public int getGroupId() {
		return this.groupId;
	}


	/**
	 * @param groupId The groupId to set.
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}


	/**
	 * @return Returns the userId.
	 */
	public int getUserId() {
		return this.userId;
	}


	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
}
