package com.idega.user.app;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;

public class SimpleUserApp extends Block {
	
	public static final int USER_ORDER_BY_NAME = 0;
	public static final int USER_ORDER_BY_ID = 1;
	
	protected static final String PARAMS_SEPARATOR = "', '";
	protected static final String COMMA_SEPARATOR = ", ";
	
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
	/** Properties end **/
	
	/**
	 * Provide instance id of the parent (container, wrapper etc.) module insterted in IBXMLPage or null if this object is inserted
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
		
		addFiles(iwc);
		
		Layer container = new Layer();
		add(container);
		
		SimpleUserAppViewUsers viewUsers = new SimpleUserAppViewUsers(instanceId, container.getId(), parentGroup,
				groupForUsersWithoutLogin, groupTypes, groupTypesForChildGroups, roleTypesForChildGroups,
				getParentGroupsFromTopNodes, useChildrenOfTopNodesAsParentGroups);
		container.add(viewUsers);
		
		UserApplicationEngine userEngine = null;
		try {
			userEngine = (UserApplicationEngine) IBOLookup.getSessionInstance(iwc, UserApplicationEngine.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (userEngine != null) {
			userEngine.addViewUsersCase(instanceId, viewUsers);
		}
	}
	
	private void addFiles(IWContext iwc) {
		IWBundle bundle = getBundle(iwc);
		AddResource adder = AddResourceFactory.getInstance(iwc);
	
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/UserApplicationEngine.js");
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/util.js");
		
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, bundle.getVirtualPathWithFileNameString("javascript/SimpleUserAppHelper.js"));
		
		adder.addStyleSheet(iwc, AddResource.HEADER_BEGIN, bundle.getVirtualPathWithFileNameString("style/user.css"));
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
	/** Methods for properties end **/

	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
}
