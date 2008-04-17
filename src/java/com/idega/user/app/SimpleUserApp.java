package com.idega.user.app;

import java.util.ArrayList;
import java.util.List;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.SpringBeanLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;

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
	/** Properties end **/
	
	/**
	 * Provide instance id of the parent (container, wrapper etc.) module inserted in IBXMLPage or null if this object is inserted
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
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
		
		SimpleUserAppViewUsers viewUsers = new SimpleUserAppViewUsers(instanceId, container.getId(), parentGroup, groupForUsersWithoutLogin, groupTypes,
				groupTypesForChildGroups, roleTypesForChildGroups, getParentGroupsFromTopNodes, useChildrenOfTopNodesAsParentGroups, allFieldsEditable,
				addGroupCreateButton, addGroupEditButton);
		container.add(viewUsers);
		
		UserApplicationEngine userEngine = null;
		try {
			userEngine = SpringBeanLookup.getInstance().getSpringBean(iwc, UserApplicationEngine.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		List<String> cssFiles = new ArrayList<String>();
		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/user.css"));
		cssFiles.add(bundle.getVirtualPathWithFileNameString("style/screen.css"));
		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			container.add(PresentationUtil.getJavaScriptSourceLines(files));
			container.add(PresentationUtil.getStyleSheetsSourceLines(cssFiles));
		}
		else {
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, files);
			PresentationUtil.addStyleSheetsToHeader(iwc, cssFiles);
		}
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

	/** Methods for properties end **/

	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

}
