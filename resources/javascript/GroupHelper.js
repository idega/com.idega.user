var SERVER_START = 'http://';
var DEFAULT_DWR_PATH = '/dwr';

var SERVER = null;
var LOGIN = null;
var PASSWORD = null;

function registerGroupInfoChooserActions(){
	$$('input.groupInfoChooserRadioStyle').each(
		function(element) {
			element.onclick = function() {
				if (element.value) {
					var values = element.value.split('@');
					if (values.length = 2) {
						manageConnectionType('local' == values[0], values[1]);
					}
				}
			}
    	}
    );
}

function manageConnectionType(useLocal, id) {
	var connection = $('connectionData');
	if (connection == null) {
		return;
	}
	var displayValue = 'inline';
	if (useLocal) {
		displayValue = 'none';
		loadLocalTree(id);
	}
	connection.style.display = displayValue;
}

function getGroupsTree(serverId, loginId, passwordId, id, messages) {
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
	
	showLoadingMessage(messages[4]);
	GroupService.canUseRemoteServer(server, {
		callback: function(result) {
			canUseRemoteCallback(result, server, login, password, id, messages[5], messages[6]);
		}
	});
}

function canUseRemoteCallback(result, server, login, password, id, severErrorMessage, logInErrorMessage) {
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
				setNodes(groups, id);
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

function loadLocalTree(id) {
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
			setNodes(groups, id);
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

function getServer() {
	return SERVER;
}

function getLogin() {
	return LOGIN;
}

function getPassword() {
	return PASSWORD;
}

function empty(result){}
