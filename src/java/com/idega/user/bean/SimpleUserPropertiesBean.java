package com.idega.user.bean;

public class SimpleUserPropertiesBean {

	private int parentGroupId = -1;
	private int groupId = -1;
	private int orderBy = -1;
	private int from = 0;
	private int count = 20;

	private String instanceId = null;
	private String containerId = null;
	private String parentGroupChooserId = null;
	private String groupChooserId = null;
	private String defaultGroupId = null;
	private String groupTypes = null;
	private String roleTypes = null;
	private String message = null;
	private String groupTypesForParentGroups = null;
	private String parentGroups;

	private boolean getParentGroupsFromTopNodes = true;
	private boolean useChildrenOfTopNodesAsParentGroups = false;
	private boolean allFieldsEditable = false;
	private boolean juridicalPerson = false;
	private boolean addGroupCreateButton = false;
	private boolean addGroupEditButton = false;
	private boolean sendMailToUser = false;
	private boolean changePasswordNextTime = false;
	private boolean allowEnableDisableAccount = false;
	private boolean addChildGroupCreateButton = true;
	private boolean addChildGroupEditButton = true,
					showSubGroup = true;

	public SimpleUserPropertiesBean() {}

	public SimpleUserPropertiesBean(String instanceId, String containerId, String groupTypesForParentGroups, String groupTypes,String roleTypes,
			boolean getParentGroupsFromTopNodes, boolean useChildrenOfTopNodesAsParentGroups, boolean allFieldsEditable, boolean juridicalPerson,
			boolean addGroupCreateButton, boolean addGroupEditButton, boolean sendMailToUser, boolean changePasswordNextTime, boolean allowEnableDisableAccount,
			boolean addChildGroupCreateButton, boolean addChildGroupEditButton, String parentGroups) {
		this();

		this.instanceId = instanceId;
		this.containerId = containerId;
		this.groupTypesForParentGroups = groupTypesForParentGroups;
		this.groupTypes = groupTypes;
		this.roleTypes = roleTypes;
		this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
		this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
		this.allFieldsEditable = allFieldsEditable;
		this.juridicalPerson = juridicalPerson;
		this.addGroupCreateButton = addGroupCreateButton;
		this.addGroupEditButton = addGroupEditButton;
		this.sendMailToUser = sendMailToUser;
		this.changePasswordNextTime = changePasswordNextTime;
		this.allowEnableDisableAccount = allowEnableDisableAccount;
		this.addChildGroupCreateButton = addChildGroupCreateButton;
		this.addChildGroupEditButton = addChildGroupEditButton;
		this.parentGroups = parentGroups;
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

	public boolean isAllFieldsEditable() {
		return allFieldsEditable;
	}

	public void setAllFieldsEditable(boolean allFieldsEditable) {
		this.allFieldsEditable = allFieldsEditable;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isJuridicalPerson() {
		return juridicalPerson;
	}

	public void setJuridicalPerson(boolean juridicalPerson) {
		this.juridicalPerson = juridicalPerson;
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

	public boolean isShowSubGroup() {
		return showSubGroup;
	}

	public void setShowSubGroup(boolean showSubGroup) {
		this.showSubGroup = showSubGroup;
	}

	public String getParentGroups() {
		return parentGroups;
	}

	public void setParentGroups(String parentGroups) {
		this.parentGroups = parentGroups;
	}

}