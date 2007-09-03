package com.idega.user.bean;

public class SimpleUserPropertiesBean {

	private int parentGroupId = -1;
	private int groupId = -1;
	private int orderBy = -1;
	
	private String instanceId = null;
	private String containerId = null;
	private String parentGroupChooserId = null;
	private String groupChooserId = null;
	private String defaultGroupId = null;
	private String groupTypes = null;
	private String roleTypes = null;
	private String message = null;
	private String groupTypesForParentGroups = null;
	
	private boolean getParentGroupsFromTopNodes = true;
	private boolean useChildrenOfTopNodesAsParentGroups = false;
	
	public SimpleUserPropertiesBean() {}
	
	public SimpleUserPropertiesBean(int parentGroupId, int groupId, int orderBy) {
		this();
		this.parentGroupId = parentGroupId;
		this.groupId = groupId;
		this.orderBy = orderBy;
	}
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}
	public int getParentGroupId() {
		return parentGroupId;
	}
	public void setParentGroupId(int parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getDefaultGroupId() {
		return defaultGroupId;
	}

	public void setDefaultGroupId(String defaultGroupId) {
		this.defaultGroupId = defaultGroupId;
	}

	public String getGroupChooserId() {
		return groupChooserId;
	}

	public void setGroupChooserId(String groupChooserId) {
		this.groupChooserId = groupChooserId;
	}

	public String getGroupTypes() {
		return groupTypes;
	}

	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getRoleTypes() {
		return roleTypes;
	}

	public void setRoleTypes(String roleType) {
		this.roleTypes = roleType;
	}

	public String getParentGroupChooserId() {
		return parentGroupChooserId;
	}

	public void setParentGroupChooserId(String parentGroupChooserId) {
		this.parentGroupChooserId = parentGroupChooserId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isGetParentGroupsFromTopNodes() {
		return getParentGroupsFromTopNodes;
	}

	public void setGetParentGroupsFromTopNodes(boolean getParentGroupsFromTopNodes) {
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	}

	public String getGroupTypesForParentGroups() {
		return groupTypesForParentGroups;
	}

	public void setGroupTypesForParentGroups(String groupTypesForParentGroups) {
		this.groupTypesForParentGroups = groupTypesForParentGroups;
	}

	public boolean isUseChildrenOfTopNodesAsParentGroups() {
		return useChildrenOfTopNodesAsParentGroups;
	}

	public void setUseChildrenOfTopNodesAsParentGroups(
			boolean useChildrenOfTopNodesAsParentGroups) {
		this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
	}
	
}
