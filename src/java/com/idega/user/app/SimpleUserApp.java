package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.idega.block.web2.business.JQueryUIType;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class SimpleUserApp extends Block {
	
	public static final int USER_ORDER_BY_NAME = 0;
	public static final int USER_ORDER_BY_ID = 1;
	
	public static final String PARAMS_SEPARATOR = "', '";
	public static final String COMMA_SEPARATOR = ", ";
	
	private String instanceId = null;
	
	public static final String EDIT_IMAGE = "images/edit.png";
	
	/** Properties start **/
	private Group parentGroup = null;
	private Group groupForUsersWithoutLogin = null;
	
	private String groupTypes = null;
	private String groupTypesForChildGroups = null;
	private String roleTypesForChildGroups = null;
	
	private boolean getParentGroupsFromTopNodes = true;
	private boolean useChildrenOfTopNodesAsParentGroups = false;
	private boolean allFieldsEditable = false;
	private boolean addGroupCreateButton = false;
	private boolean addGroupEditButton = false;
	private boolean addChildGroupCreateButton = true;
	private boolean addChildGroupEditButton = true;
	private boolean juridicalPerson = false;
	private boolean sendMailToUser = false;
	private boolean changePasswordNextTime = true;
	private boolean allowEnableDisableAccount = false;
	/** Properties end **/
	
	/**
	 * Provide instance id of the parent (container, wrapper etc.) module inserted in IBXMLPage or null if this object is inserted
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	@Override
	public void main(IWContext iwc) {
		if (instanceId == null)	{
			instanceId = BuilderLogic.getInstance().getInstanceId(this);
		}
		if (instanceId == null) {
			throw new NullPointerException("Provide instanceId for " + SimpleUserApp.class.getName());
		}
		
		Layer container = new Layer();
		
		addFiles(iwc, container);
		
		add(container);
		
		SimpleUserPropertiesBean properties = new SimpleUserPropertiesBean(instanceId, container.getId(), groupTypes, groupTypesForChildGroups,
				roleTypesForChildGroups, getParentGroupsFromTopNodes, useChildrenOfTopNodesAsParentGroups, allFieldsEditable, juridicalPerson,
				addGroupCreateButton, addGroupEditButton, sendMailToUser, changePasswordNextTime, allowEnableDisableAccount, addChildGroupCreateButton,
				addChildGroupEditButton);
		if (parentGroup != null) {
			properties.setParentGroupId(Integer.valueOf(parentGroup.getId()));
		}
		if (groupForUsersWithoutLogin != null) {
			properties.setDefaultGroupId(groupForUsersWithoutLogin.getId());
		}
		
		SimpleUserAppViewUsers viewUsers = new SimpleUserAppViewUsers(properties, parentGroup, groupForUsersWithoutLogin);
		container.add(viewUsers);
		
		UserApplicationEngine userEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		
		if (userEngine != null) {
			userEngine.addViewUsersCase(instanceId, viewUsers);
		}
	}
	
	private void addFiles(IWContext iwc, Layer container) {
		IWBundle bundle = getBundle(iwc);
		
		List<String> files = new ArrayList<String>();
		files.add(CoreConstants.DWR_ENGINE_SCRIPT);
		files.add("/dwr/interface/UserApplicationEngine.js");
		files.add(CoreConstants.DWR_UTIL_SCRIPT);
		
		files.add(bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));
		
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		try {
			files.add(web2.getBundleURIToMootoolsLib());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		files.add(web2.getBundleURIToJQueryLib());
		files.add(web2.getBundleURIToJQueryUILib(JQueryUIType.UI_EASING));
		files.add(web2.getBundleUriToHumanizedMessagesScript());	
		
		List<String> cssFiles = new ArrayList<String>();
		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/user.css"));
		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));
		cssFiles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
		
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		String initAction = new StringBuilder("setErrorHandlerForSimpleUserApplication(['")
						.append(iwrb.getLocalizedString("error_in_simple_user_app", "Oopps, some error occured...")).append("', '")
						.append(iwrb.getLocalizedString("error_message_from_server", "Error message from server")).append("']);").toString();
		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(files));
			container.add(PresentationUtil.getStyleSheetsSourceLines(cssFiles));
		}
		else {
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, files);
			PresentationUtil.addStyleSheetsToHeader(iwc, cssFiles);
			initAction = new StringBuilder("jQuery(window).load(function() {").append(initAction).append("});").toString();
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, initAction);
	}
	
	/** Methods for properties start **/
	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}
	
	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}

	public void setGetParentGroupsFromTopNodes(boolean getParentGroupsFromTopNodes) {
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	}

	public void setGroupTypesForChildGroups(String groupTypesForChildGroups) {
		this.groupTypesForChildGroups = groupTypesForChildGroups;
	}

	public void setRoleTypesForChildGroups(String roleTypesForChildGroups) {
		this.roleTypesForChildGroups = roleTypesForChildGroups;
	}

	public void setGroupForUsersWithoutLogin(Group groupForUsersWithoutLogin) {
		this.groupForUsersWithoutLogin = groupForUsersWithoutLogin;
	}
	
	public void setUseChildrenOfTopNodesAsParentGroups(boolean useChildrenOfTopNodesAsParentGroups) {
		this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
	}
	
	public boolean isAllFieldsEditable() {
		return allFieldsEditable;
	}

	public void setAllFieldsEditable(boolean allFieldsEditable) {
		this.allFieldsEditable = allFieldsEditable;
	}

	public boolean isAddGroupCreateButton() {
		return addGroupCreateButton;
	}

	public void setAddGroupCreateButton(boolean addGroupCreateButton) {
		this.addGroupCreateButton = addGroupCreateButton;
	}

	public boolean isAddGroupEditButton() {
		return addGroupEditButton;
	}

	public void setAddGroupEditButton(boolean addGroupEditButton) {
		this.addGroupEditButton = addGroupEditButton;
	}

	public boolean isJuridicalPerson() {
		return juridicalPerson;
	}

	public void setJuridicalPerson(boolean juridicalPerson) {
		this.juridicalPerson = juridicalPerson;
	}

	public boolean isSendMailToUser() {
		return sendMailToUser;
	}

	public void setSendMailToUser(boolean sendMailToUser) {
		this.sendMailToUser = sendMailToUser;
	}

	public boolean isChangePasswordNextTime() {
		return changePasswordNextTime;
	}

	public void setChangePasswordNextTime(boolean changePasswordNextTime) {
		this.changePasswordNextTime = changePasswordNextTime;
	}

	public boolean isAllowEnableDisableAccount() {
		return allowEnableDisableAccount;
	}

	public void setAllowEnableDisableAccount(boolean allowEnableDisableAccount) {
		this.allowEnableDisableAccount = allowEnableDisableAccount;
	}

	public boolean isAddChildGroupCreateButton() {
		return addChildGroupCreateButton;
	}

	public void setAddChildGroupCreateButton(boolean addChildGroupCreateButton) {
		this.addChildGroupCreateButton = addChildGroupCreateButton;
	}

	public boolean isAddChildGroupEditButton() {
		return addChildGroupEditButton;
	}

	public void setAddChildGroupEditButton(boolean addChildGroupEditButton) {
		this.addChildGroupEditButton = addChildGroupEditButton;
	}

	/** Methods for properties end **/

	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

}
