var USERS_TO_REMOVE = new Array();

function reloadComponents(message, childGroupsChooserId, orderByChooserId, containerId, chooserId, groupTypes, groupRoles, groupId) {
	showLoadingMessage(message);
	DWRUtil.removeAllOptions(childGroupsChooserId);
	/*if (IE) {
		//alert('IE');
		UserApplicationEngine.getChildGroupsInString(groupId, groupTypes, groupRoles, myCallback);
		/*UserApplicationEngine.getChildGroupsInString(groupId, groupTypes, {
			callback: function(childGroups) {
				getChildGroupsInStringCallback(childGroups, childGroupsChooserId);
			}
		});
	}
	else {*/
		UserApplicationEngine.getChildGroups(groupId, groupTypes, groupRoles, {
			callback: function(childGroups) {
				getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message);
			}
		});
	//}
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

function getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message) {
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
	selectChildGroup(groupId, containerId, chooserId, orderByChooserId, message);
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

function reOrderGroupUsers(parentGroupChooserId, childGroupChooserId, orderByChooserId, containerId, message) {
	var groupId = getSelectObjectValue(childGroupChooserId);
	selectChildGroup(groupId, containerId, parentGroupChooserId, orderByChooserId, message);
}

function selectChildGroup(groupId, containerId, parentGroupChooserId, orderByChooserId, message) {
	showLoadingMessage(message);
	var parentGroupId = getSelectObjectValue(parentGroupChooserId);
	var orderBy = getSelectObjectValue(orderByChooserId);
	
	showLoadingMessage(message);
	UserApplicationEngine.getMembersList(parentGroupId, groupId, orderBy, {
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