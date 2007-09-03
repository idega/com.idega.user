var USERS_TO_REMOVE = new Array();
var DESELECTED_GROUPS = new Array();

function reloadComponents(message, childGroupsChooserId, orderByChooserId, containerId, chooserId, groupTypes, groupRoles,
							instanceId, mainContainerId, defaultGroupId, parentGroupChooserId) {
	showLoadingMessage(message);
	var chooser = document.getElementById(childGroupsChooserId);
	if (chooser != null) {
		DWRUtil.removeAllOptions(childGroupsChooserId);
	}
	
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	
	var params = new Array();
	params.push(instanceId);
	params.push(mainContainerId);
	params.push(childGroupsChooserId);
	params.push(defaultGroupId);
	params.push(groupTypes);
	params.push(groupRoles);
	params.push(message);
	params.push(parentGroupChooserId);
	
	UserApplicationEngine.getChildGroups(parentGroupId, groupTypes, groupRoles, {
		callback: function(childGroups) {
			getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params);
		}
	});
}

function getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params) {
	closeAllLoadingMessages();
	if (childGroups == null) {
		return false;
	}
	
	var chooser = document.getElementById(childGroupsChooserId);
	if (chooser == null) {
		return false;
	}
	chooser.removeAttribute('disabled');
	if (childGroups == null) {
		chooser.setAttribute('disabled', true);
	}
	else {
		DWRUtil.addOptions(childGroupsChooserId, childGroups, 'id', 'value');
	}
	
	var groupId = getSelectObjectValue(childGroupsChooserId);
	selectChildGroup(childGroupsChooserId, containerId, chooserId, orderByChooserId, message, params);
}

function getSelectObjectValue(id) {
	if (id == null) {
		return -1;
	}
	var select = document.getElementById(id);
	if (select == null) {
		return -1;
	}
	if (select.value) {
		return select.value;
	}
	return -1;
}

function reOrderGroupUsers(parentGroupChooserId, childGroupChooserId, orderByChooserId, containerId, message, params) {
	var groupId = getSelectObjectValue(childGroupChooserId);
	selectChildGroup(childGroupChooserId, containerId, parentGroupChooserId, orderByChooserId, message, params);
}

function selectChildGroup(groupChooserId, containerId, parentGroupChooserId, orderByChooserId, message, parameters) {
	showLoadingMessage(message);
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	var groupId = getSelectObjectValue(groupChooserId);
	var orderBy = getSelectObjectValue(orderByChooserId);
	
	var bean = new SimpleUserPropertiesBeanWithParameters(parentGroupId, groupId, orderBy, parameters);
	
	showLoadingMessage(message);
	UserApplicationEngine.getMembersList(bean, {
		callback: function(component) {
			getMembersListCallback(component, containerId);
		}
	})
}

function getMembersListCallback(component, containerId) {
	closeAllLoadingMessages();
	USERS_TO_REMOVE = new Array();
	var container = document.getElementById(containerId);
	if (container == null) {
		return false;
	}
	
	removeChildren(container);
	insertNodesToContainer(component, container);
	return true;
}

function AdvancedProperty(id, value) {
	this.id = id;
	this.value = value;
}

function MarkedUsers(containerId, userId, groupId) {
	this.containerId = containerId;
	this.userId = userId;
	this.groupId = groupId;
}

function removeSelectedUsers(message, areYouSure, nothingSelected) {
	if (USERS_TO_REMOVE.length == 0) {
		alert(nothingSelected);
		return false;
	}
	
	var confirmDelete = false;
	confirmDelete = window.confirm(areYouSure);
	if (!confirmDelete) {
		return false;
	}
	
	showLoadingMessage(message);
	var users = new Array();
	var groupId = -1;
	for (var i = 0; i < USERS_TO_REMOVE.length; i++) {
		groupId = USERS_TO_REMOVE[i].groupId;
		users.push(USERS_TO_REMOVE[i].userId);
	}
	UserApplicationEngine.removeUsers(users, groupId, removeUsersCallback);
}

function removeUsersCallback(removedUsers) {
	closeAllLoadingMessages();
	if (removedUsers == null) {
		return false;
	}
	
	var userId = -1;
	var markedUser = null;
	for (var i = 0; i < removedUsers.length; i++) {
		markedUser = findMarkedUserById(removedUsers[i]);
		if (markedUser != null) {
			var lineContainer = document.getElementById(markedUser.containerId);
			if (lineContainer != null) {
				var parentContainer = lineContainer.parentNode;
				parentContainer.removeChild(lineContainer);
			}
		}
	}
	
	USERS_TO_REMOVE = new Array();
	return true;
}

function findMarkedUserById(userId) {
	if (USERS_TO_REMOVE.length == 0) {
		return null;
	}
	
	var found = false;
	var markedUser = null;
	for (var i = 0; (i < USERS_TO_REMOVE.length && !found); i++) {
		markedUser = USERS_TO_REMOVE[i];
		if (markedUser.userId == userId) {
			found = true;
		}
	}
	
	if (found) {
		return markedUser;
	}
	return null;
}

function findMarkedUser(containerId, userId, groupId) {
	var index = -1;
	var markedUser = null;
	var found = false;
	for (var i = 0; (i < USERS_TO_REMOVE.length && !found); i++) {
		markedUser = USERS_TO_REMOVE[i];
		if (markedUser.containerId == containerId && markedUser.userId == userId && markedUser.groupId == groupId) {
			found = true;
			index = i;
		}
	}
	return index;
}

function removeUser(containerId, userId, groupId, checkBoxId) {
	var index = findMarkedUser(containerId, userId, groupId);
	if (index != -1) {
		USERS_TO_REMOVE.splice(index, 1);
	}
	
	var isChecked = false;
	var checkbox = document.getElementById(checkBoxId);
	if (checkbox != null) {
		isChecked = checkbox.checked;
	}
	
	if (isChecked) {
		USERS_TO_REMOVE.push(new MarkedUsers(containerId, userId, groupId));
	}
}

function addUserPresentationObject(instanceId, containerId, parentGroupChooserId, groupChooserId, message, defaultGroupId, userId,
										groupTypes, roleTypes, getParentGroupsFromTopNodes, groupTypesForParentGroups,
										useChildrenOfTopNodesAsParentGroups) {
	refreshDeselectedGroups();
	showLoadingMessage(message);
	
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	var groupId = getSelectObjectValue(groupChooserId);
	
	//	Properties bean
	var bean = new SimpleUserPropertiesBean(instanceId, parentGroupId, groupId, defaultGroupId, containerId, groupTypes, roleTypes, getParentGroupsFromTopNodes, groupTypesForParentGroups, useChildrenOfTopNodesAsParentGroups);
	
	//	Parent groups
	var parentGroups = getSelectObjectValues(parentGroupChooserId);
	
	//	Groups
	var groups = getSelectObjectValues(groupChooserId);
	
	UserApplicationEngine.getAddUserPresentationObject(bean, parentGroups, groups, userId, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		}
	});
}

function getSelectObjectValues(id) {
	if (id == null) {
		return null;
	}
	
	var chooser = document.getElementById(id);
	if (chooser == null) {
		return null;
	}
	var options = chooser.options;
	if (options == null) {
		return null;
	}
	var values = new Array();
	for (var i = 0; i < options.length; i++) {
		values.push(options[i].value);
	}
	
	return values;
}

function getAddUserPresentationObjectCallback(component, containerId) {
	closeAllLoadingMessages();
	
	if (component == null) {
		return false;
	}
	var container = document.getElementById(containerId);
	if (container == null) {
		return false;
	}
	
	removeChildren(container);
	insertNodesToContainer(component, container);
	return true;
}

function goBackToSimpleUserApp(instanceId, containerId, message) {
	refreshDeselectedGroups();
	showLoadingMessage(message);
	UserApplicationEngine.getSimpleUserApplication(instanceId, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		} 
	});
}

function reloadAvailableGroupsForUser(parentGroupChooserId, userId, parameters) {
	refreshDeselectedGroups();
	var groupId = getSelectObjectValue(parentGroupChooserId);
	var containerId = parameters[0];
	var message = parameters[1];
	var groupTypes = parameters[2];
	var roleTypes = parameters[3];
	showLoadingMessage(message);
	UserApplicationEngine.getAvailableGroupsForUserPresentationObject(groupId, userId, groupTypes, roleTypes, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		}
	});
}

function getUserByPersonalId(valueInputId, nameInputId, loginNameInputId, passwordInputId, message, emailInputId) {
	if (valueInputId == null) {
		return false;
	}
	var input = document.getElementById(valueInputId);
	if (input == null) {
		return false;
	}
	var personalId = input.value;
	if (personalId == null) {
		return false;
	}
	if (personalId.length < 10) {
		return false;
	}
	
	var lastIndex = personalId.length;
	var lastChar = personalId.charAt(lastIndex-1);
	if (lastChar == '-') {
		personalId = personalId.substring(0, lastIndex-1);
	}
	
	var id = userApplicationRemoveSpaces(personalId);
	showLoadingMessage(message);
	UserApplicationEngine.getUserByPersonalId(id, {
		callback: function(info) {
			getUserByPersonalIdCallback(info, nameInputId, loginNameInputId, passwordInputId, emailInputId);
		}
	});
}

function userApplicationRemoveSpaces(value) {
	if (value == null) {
		return null;
	}
	return value.replace(/^\s+|\s+$/g, '');
}

function getUserByPersonalIdCallback(bean, nameInputId, loginNameInputId, passwordInputId, emailInputId) {
	closeAllLoadingMessages();
	
	var nameInput = document.getElementById(nameInputId);
	if (nameInput == null) {
		return false;
	}
	var loginInput = document.getElementById(loginNameInputId);
	if (loginInput == null) {
		return false;
	}
	var passwordInput = document.getElementById(passwordInputId);
	if (passwordInput != null) {
		passwordInput.value = '';
	}
	var emailInput = document.getElementById(emailInputId);
	if (emailInput != null) {
		emailInput.value = '';
	}
	nameInput.value = '';
	loginInput.value = '';
	
	if (bean == null) {
		return false;
	}
	if (bean.errorMessage != null) {
		alert(bean.errorMessage);
		return false;
	}
	
	refreshDeselectedGroups();
	
	nameInput.value = bean.name;
	loginInput.value = bean.personalId;
	setValueForUserInput(passwordInput, bean.password);
	setValueForUserInput(emailInput, bean.email);
	
	return true;
}

function setValueForUserInput(input, value) {
	if (input == null) {
		return false;
	}
	
	if (value == null || value == '') {
		input.removeAttribute('disabled');
	}
	else {
		input.setAttribute('disabled', 'true');
		input.value = value;	
	}
}

function saveUserInSimpleUserApplication(ids, childGroups, message, passwordErrorMessage) {
	showLoadingMessage(message);
	var emailInputId = ids[5];
	var email = document.getElementById(emailInputId).value;
	UserApplicationEngine.isValidEmail(email, {
		callback: function(result) {
			isValidUserEmailCallback(result, ids, childGroups, message, passwordErrorMessage);
		}
	});
}

function isValidUserEmailCallback(result, ids, childGroups, message, passwordErrorMessage) {
	closeAllLoadingMessages();
	if (result != null) {
		alert(result);
		return false;
	}
	
	var parentGroupChooserId = ids[0];
	var nameValueInputId = ids[1];
	var loginInputId = ids[2];
	var passwordInputId = ids[3];
	var groupForUsersWithoutLoginId = ids[4];
	var emailInputId = ids[5];
	
	var selectedGroups = new Array();
	if (childGroups == null) {
		selectedGroups.push(groupForUsersWithoutLoginId);
	}
	else {
		var checkboxValue = null;
		for (var i = 0; i < childGroups.length; i++) {
			checkboxValue = getCheckboxValue(childGroups[i], true);
			if (checkboxValue != null) {
				selectedGroups.push(checkboxValue);
			}
		}
		if (selectedGroups.length == 0 && DESELECTED_GROUPS.length == 0) {
			selectedGroups.push(getCheckboxValue(childGroups[0], false));	// Nothing selected, adding the first group
		}
	}
	
	var userName = document.getElementById(nameValueInputId).value;
	var personalId = document.getElementById(loginInputId).value;	// Personal ID = Login name
	var password = document.getElementById(passwordInputId).value;
	if (password == null || password == '') {
		alert(passwordErrorMessage);
		return false;
	}
	var email = document.getElementById(emailInputId).value;
	var primaryGroupId = getSelectObjectValue(parentGroupChooserId);
	
	showLoadingMessage(message);
	UserApplicationEngine.createUser(userName, personalId, password, email, primaryGroupId, selectedGroups, DESELECTED_GROUPS, {
		callback: function(result) {
			closeAllLoadingMessages();
			if (result != null) {
				alert(result);
			}
		}
	});
}

function getCheckboxValue(id, checkIfChecked) {
	if (id == null) {
		return null;
	}
	var checkbox = document.getElementById(id);
	if (checkbox == null) {
		return null;
	}
	if (checkIfChecked) {
		if (checkbox.checked) {
			return checkbox.value;
		}
		return null;
	}
	return checkbox.value;
}

function SimpleUserPropertiesBean(instanceId, parentGroupId, groupId, defaultGroupId, containerId, groupTypes, roleTypes, getParentGroupsFromTopNodes, groupTypesForParentGroups, useChildrenOfTopNodesAsParentGroups) {
	this.instanceId = instanceId;
	this.parentGroupId = parentGroupId;
	this.groupId = groupId;
	this.defaultGroupId = defaultGroupId;
	this.containerId = containerId;
	this.groupTypes = groupTypes;
	this.roleTypes = roleTypes;
	this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	this.groupTypesForParentGroups = groupTypesForParentGroups;
	this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
}

function SimpleUserPropertiesBeanWithParameters(parentGroupId, groupId, orderBy, parameters) {
	this.instanceId = null;
	this.containerId = null;
	this.parentGroupChooserId = null;
	this.groupChooserId = null;
	this.message = null;
	this.defaultGroupId = null;
	this.groupTypes = null;
	this.roleTypes = null;
	
	this.parentGroupId = parentGroupId;
	this.groupId = groupId;
	this.orderBy = orderBy;
	
	if (parameters == null) {
		return;
	}
	if (parameters.length < 10) {
		return;
	}
	this.instanceId = parameters[0];
	this.containerId = parameters[1];
	this.parentGroupChooserId = parameters[7];
	this.groupChooserId = parameters[2];
	this.message = parameters[6];
	this.defaultGroupId = parameters[3];
	this.groupTypes = parameters[4];
	this.roleTypes = parameters[5];
	this.groupTypesForParentGroups = parameters[8];
	this.useChildrenOfTopNodesAsParentGroups = parameters[9];
}

function refreshDeselectedGroups() {
	DESELECTED_GROUPS = new Array();
}

function deselectUserFromGroup(groupId) {
	var existsId = false;
	for (var i = 0; (i < DESELECTED_GROUPS.length && !existsId); i++) {
		if (DESELECTED_GROUPS[i] == groupId) {
			existsId = true;
		}
	}
	if (existsId) {
		return;
	}
	DESELECTED_GROUPS.push(groupId);
}