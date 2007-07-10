var USERS_TO_REMOVE = new Array();

function reloadComponents(message, childGroupsChooserId, orderByChooserId, containerId, chooserId, groupTypes, groupRoles, groupId,
							instanceId, mainContainerId, defaultGroupId, parentGroupChooserId) {
	showLoadingMessage(message);
	DWRUtil.removeAllOptions(childGroupsChooserId);
	
	var params = new Array();
	params.push(instanceId);
	params.push(mainContainerId);
	params.push(childGroupsChooserId);
	params.push(defaultGroupId);
	params.push(groupTypes);
	params.push(groupRoles);
	params.push(message);
	params.push(parentGroupChooserId);
	
	UserApplicationEngine.getChildGroups(groupId, groupTypes, groupRoles, {
		callback: function(childGroups) {
			getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params);
		}
	});
}

function myCallback(data) {
	alert(data);
	closeAllLoadingMessages();
}

function getChildGroupsInStringCallback(childGroups, childGroupsChooserId) {
	//alert('info: ' + childGroups);
	if (childGroups == null) {
		closeAllLoadingMessages();
		return false;
	}
	if (childGroups == '') {
		closeAllLoadingMessages();
		return false;
	}
	
	var properties = childGroups.split('@prop_separator@');
	if (properties == null) {
		closeAllLoadingMessages();
		return false;
	}
	var advancedProperties = new Array();
	for (var i = 0; i < properties.length; i++) {
		var property = properties[i].split(',');
		if (property != null) {
			advancedProperties.push(new AdvancedProperty(property[0], property[1]));
		}
	}
	
	getChildGroupsCallback(advancedProperties, childGroupsChooserId)
}

function getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params) {
	closeAllLoadingMessages();
	if (childGroups == null) {
		return false;
	}
	
	var chooser = document.getElementById(childGroupsChooserId);
	chooser.removeAttribute('disabled');
	if (childGroups == null) {
		chooser.setAttribute('disabled', true);
	}
	else {
		DWRUtil.addOptions(childGroupsChooserId, childGroups, 'id', 'value');
	}
	
	var groupId = getSelectObjectValue(childGroupsChooserId);
	selectChildGroup(groupId, containerId, chooserId, orderByChooserId, message, params);
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
	selectChildGroup(groupId, containerId, parentGroupChooserId, orderByChooserId, message, params);
}

function selectChildGroup(groupId, containerId, parentGroupChooserId, orderByChooserId, message, parameters) {
	showLoadingMessage(message);
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	var orderBy = getSelectObjectValue(orderByChooserId);
	
	showLoadingMessage(message);
	UserApplicationEngine.getMembersList(parentGroupId, groupId, orderBy, parameters, {
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

function removeUser(containerId, userId, groupId, isChecked) {
	var index = findMarkedUser(containerId, userId, groupId);
	if (index != -1) {
		USERS_TO_REMOVE.splice(index, 1);
	}
	if (isChecked) {
		USERS_TO_REMOVE.push(new MarkedUsers(containerId, userId, groupId));
	}
}

function addUserPresentationObject(instanceId, containerId, parentGroupChooserId, groupChooserId, message, defaultGroupId, userId, groupTypes, roleTypes) {
	showLoadingMessage(message);
	
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	var groupId = getSelectObjectValue(groupChooserId);
	
	//	IDs
	var ids = new Array();
	ids.push(instanceId);
	ids.push(parentGroupId);
	ids.push(groupId);
	ids.push(defaultGroupId);
	ids.push(containerId);
	
	//	Parent groups
	var parentGroups = getSelectObjectValues(parentGroupChooserId);
	
	//	Groups
	var groups = getSelectObjectValues(groupChooserId);
	
	UserApplicationEngine.getAddUserPresentationObject(ids, parentGroups, groups, userId, groupTypes, roleTypes, {
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
	showLoadingMessage(message);
	UserApplicationEngine.getSimpleUserApplication(instanceId, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		} 
	});
}

function reloadAvailableGroupsForUser(groupId, parameters) {
	var containerId = parameters[0];
	var message = parameters[1];
	var groupTypes = parameters[2];
	var roleTypes = parameters[3];
	showLoadingMessage(message);
	UserApplicationEngine.getAvailableGroupsForUserPresentationObject(groupId, groupTypes, roleTypes, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		}
	});
}

function getUserByPersonalId(personalId, nameInputId, loginNameInputId, passwordInputId, message) {
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
			getUserByPersonalIdCallback(info, nameInputId, loginNameInputId, passwordInputId);
		}
	});
}

function userApplicationRemoveSpaces(value) {
	if (value == null) {
		return null;
	}
	return value.replace(/^\s+|\s+$/g, '');
}

function getUserByPersonalIdCallback(info, nameInputId, loginNameInputId, passwordInputId) {
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
	nameInput.value = '';
	loginInput.value = '';
	
	if (info == null) {
		return false;
	}
	if (info.length == 1) {
		alert(info[0]);
		return false;
	}
	
	nameInput.value = info[0];
	loginInput.value = info[1];
	if (passwordInput != null) {
		if (info[2] == null || info[2] == '') {
			passwordInput.removeAttribute('disabled');
		}
		else {
			passwordInput.setAttribute('disabled', 'true');
			passwordInput.value = info[2];	
		}
	}
	return true;
}

function saveUserInSimpleUserApplication(ids, childGroups, message, passwordErrorMessage) {
	var parentGroupChooserId = ids[0];
	var nameValueInputId = ids[1];
	var loginInputId = ids[2];
	var passwordInputId = ids[3];
	var groupForUsersWithoutLoginId = ids[4];
	
	var selectedGroups = new Array();
	if (childGroups == null) {
		selectedGroups.push(groupForUsersWithoutLoginId);
	}
	else {
		var checkbox = null;
		for (var i = 0; i < childGroups.length; i++) {
			checkbox = document.getElementById(childGroups[i]);
			if (checkbox != null) {
				if (checkbox.checked) {
					selectedGroups.push(checkbox.value);
				}
			}
		}
	}
	
	var userName = document.getElementById(nameValueInputId).value;
	var personalId = document.getElementById(loginInputId).value;	// Personal ID = Login name
	var password = document.getElementById(passwordInputId).value;
	if (password == null || password == '') {
		alert(passwordErrorMessage);
		return false;
	}
	var primaryGroupId = getSelectObjectValue(parentGroupChooserId);
	
	showLoadingMessage(message);
	UserApplicationEngine.createUser(userName, personalId, password, primaryGroupId, selectedGroups, {
		callback: function(result) {
			closeAllLoadingMessages();
			if (result != null) {
				alert(result);
			}
		}
	});
}