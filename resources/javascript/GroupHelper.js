var SERVER_START = 'http://';
var DEFAULT_DWR_PATH = '/dwr';

var UNIQUE_IDS_ID = 'uniqueids';
var GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS = 'groups_tree_list_element';
var NO_GROUPS_MESSAGE = 'Sorry, no groups found on selected server.';

var SERVER = null;
var LOGIN = null;
var PASSWORD = null;

function registerGroupInfoChooserActions(nodeOnClickAction, noGroupsMessage, selectedGroups){
	if (noGroupsMessage != null) {
		NO_GROUPS_MESSAGE = noGroupsMessage;
	}
	$$('input.groupInfoChooserRadioStyle').each(
		function(element) {
			element.onclick = function() {
				if (element.value) {
					var values = element.value.split('@');
					if (values.length = 2) {
						manageConnectionType('local' == values[0], values[1], noGroupsMessage, selectedGroups);
						removeAllAdvancedProperties();	//	Because changing connection type
						addAdvancedProperty('connection', values[0]);
					}
				}
			}
    	}
    );
    $$('li.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(
		function(element) {
			element.onclick = function() {
				selectGroup(element);
				checkOtherProperties(element);
			}
    	}
    );
}

function checkOtherProperties(clickedElement) {
	//	Inputs' values (connection parameters)
	$$('input.groupConnectionChooserInputStyle').each(
		function(element) {
			addAdvancedProperty(element.name, element.value);
		}
	);
	
	//	Connection type
	var radio = null;
	$$('input.groupInfoChooserRadioStyle').each(
		function(element) {
			if (element.checked) {
				radio = element;
			}
    	}
    );
    if (radio != null) {
    	if (radio.value) {
			var values = radio.value.split('@');
			if (values.length = 2) {
				addAdvancedProperty('connection', values[0]);
			}
		}
    }
    
    //	Seaching for selected nodes
    var otherGroupsNodes = new Array();
    $$('li.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(
		function(element) {
			if (element != clickedElement) {
				otherGroupsNodes.push(element);
			}
    	}
    );
    for (var i = 0; i < otherGroupsNodes.length; i++) {
    	if ('bold' == otherGroupsNodes[i].style.fontWeight) {
    		var advancedProperty = getAdvancedProperty(UNIQUE_IDS_ID);
			if (advancedProperty == null) {
				addAdvancedProperty(UNIQUE_IDS_ID, otherGroupsNodes[i].id);
			}
			else {
				var allIds = advancedProperty.value.split(',');
				if (!existsElementInArray(allIds, otherGroupsNodes[i].id)) {	//	This node must be selected
					var newValues = advancedProperty.value + ',' + otherGroupsNodes[i].id;	//	Adding new id
					addAdvancedProperty(UNIQUE_IDS_ID, newValues);
				}
    		}
    	}
    }
}

function selectGroup(element) {
	if (element == null) {
		return;
	}
	if (element.id == null) {
		return;
	}
	
	var addId = false;
	if (element.style.fontWeight == null) {
		addId = true;
	}
	else {
		if (element.style.fontWeight == '') {
			addId = true;
		}
	}
	if (addId) {
		element.style.fontWeight = 'bold';
	}
	else {
		element.style.fontWeight = '';
	}
	
	var advancedProperty = getAdvancedProperty(UNIQUE_IDS_ID);
	if (advancedProperty == null) {
		if (addId) {
			addAdvancedProperty(UNIQUE_IDS_ID, element.id);
		}
	}
	else {
		var newValues = '';
		if (addId) {
			 newValues = advancedProperty.value + ',' + element.id;	//	Adding new id
		}
		else {
			var allIds = advancedProperty.value.split(',');
			removeElementFromArray(allIds, element.id);	//	Removing id
			for (var i = 0; i < allIds.length; i++) {	//	Building new value
				newValues = allIds[i];
				if (i + 1 < allIds.length) {
					newValues += ',';
				} 
			}
		}
		addAdvancedProperty(UNIQUE_IDS_ID, newValues);
	}
}

function manageConnectionType(useLocal, id, noGroupsMessage, selectedGroups) {
	var connection = $('connectionData');
	if (connection == null) {
		return;
	}
	var displayValue = 'inline';
	if (useLocal) {
		displayValue = 'none';
		loadLocalTree(id, noGroupsMessage, selectedGroups);
	}
	connection.style.display = displayValue;
}

function getGroupsTree(serverId, loginId, passwordId, id, messages, selectedGroups) {
	var serverInput = $(serverId);
	var loginInput = $(loginId);
	var passwordInput = $(passwordId);
	if (serverInput == null || loginInput == null || passwordInput == null) {
		alert(messages[0]);
		return false;
	}
	
	var server = serverInput.value;
	if (server == '') {
		alert(messages[1]);
		return false;
	}
	
	var login = loginInput.value;
	if (login == '') {
		alert(messages[2]);
		return false;
	}
	
	var password = passwordInput.value;
	if (password == '') {
		alert(messages[3]);
		return false;
	}
	
	if (server.indexOf(SERVER_START) != 0) {
		server = SERVER_START + server;
	}
	
	addAdvancedProperty(serverInput.name, server);
	addAdvancedProperty(loginInput.name, login);
	addAdvancedProperty(passwordInput.name, password);
	
	getGroupsWithValues(messages[4], server, login, password, id, messages[5], messages[6], messages[7], false, selectedGroups);
}

function getGroupsWithValues(loadingMsg, server, login, password, id, canNotConnectMsg, failedLoginMsg, noGroupsMsg, needsDecode, selectedGroups) {
	showLoadingMessage(loadingMsg);
	if (needsDecode) {
		password = decode64(password);
	}
	GroupService.canUseRemoteServer(server, {
		callback: function(result) {
			canUseRemoteCallback(result, server, login, password, id, canNotConnectMsg, failedLoginMsg, noGroupsMsg, selectedGroups);
		}
	});
}

function canUseRemoteCallback(result, server, login, password, id, severErrorMessage, logInErrorMessage, noGroupsMessage, selectedGroups) {
	if (result) {
		//	Can use remote server, preparing DWR
		prepareDwr(GroupService, server + DEFAULT_DWR_PATH);
	
		//	Getting info from remote server
		GroupService.getGroupsTree(login, password, {
			callback: function(groups) {
				if (groups == null) {
					//	Login failed
					closeLoadingMessage();
					alert(logInErrorMessage + ' ' + server);
					return false;
				}
				SERVER = server;
				LOGIN = login;
				PASSWORD = password;
				addGroupsTree(groups, id, noGroupsMessage, selectedGroups);
			}
		});
	}
	else {
		//	Cannot use remote server
		closeLoadingMessage();
		alert(severErrorMessage + ' ' + server);
		return false;
	}
}

function loadLocalTree(id, noGroupsMessage, selectedGroups) {
	SERVER = null;
	LOGIN = null;
	PASSWORD = null;
	prepareDwr(GroupService, DEFAULT_DWR_PATH);
	
	GroupService.getTopGroupNodes({
		callback: function(groups) {
			if (groups == null) {
				closeLoadingMessage();
				return false;
			}
			addGroupsTree(groups, id, noGroupsMessage, selectedGroups);
		}
	});
}

function prepareDwr(interfaceClass, path) {
	//	Preparing DWR
	dwr.engine._defaultPath = path;
	interfaceClass._path = path;
	DWREngine.setMethod(DWREngine.ScriptTag);
}

function getDefaultDwrPath() {
	return DEFAULT_DWR_PATH;
}

function addGroupsTree(groups, id, noGroupsMessage, selectedGroups) {
	if (groups.length == 0) {
		var container = document.getElementById(id);
		if (container != null) {
			removeChildren(container);
			var textContainer = document.createElement('div');
			textContainer.appendChild(document.createTextNode(noGroupsMessage));
			container.appendChild(textContainer);
		}
	}
	else {
		setGroupsNodes(groups, id, GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS, selectedGroups);
		registerGroupInfoChooserActions(null, noGroupsMessage);
	}
}
