function getSelectedUsers(instanceId, containerId, message) {
	if (instanceId == null) {
		return;
	}

	showLoadingMessage(message);

	//	To be sure we'll call to 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getUserPropertiesBean(instanceId, {
		callback: function(properties) {
			getUserPropertiesCallback(properties, containerId);
		}
	});
}

function getUserPropertiesCallback(properties, containerId) {
	if (properties == null) {
		closeLoadingMessage();
		return false;
	}
	
	if (properties.remoteMode) {
		//	Remote mode
		if (properties.server == null || properties.login == null || properties.password == null) {
			closeLoadingMessage();
			return false;
		}
	
		GroupService.canUseRemoteServer(properties.server, {
			callback: function(result) {
				getGroupsUsersData(result, properties, containerId);
			}
		});
	}
	else {
		//	Local mode
		getGroupsUsersData(true, properties, containerId)
	}
}

function getGroupsUsersData(result, properties, containerId) {
	if (!result) {
		closeLoadingMessage();
		return false;
	}
	
	if (properties.remoteMode) {
		//	Preparing DWR for remote call
		prepareDwr(GroupService, properties.server + getDefaultDwrPath());
	}
	else {
		//	Preparing DWR for local call
		prepareDwr(GroupService, getDefaultDwrPath());
	}
	GroupService.getUsersInfo(properties, {
		callback: function(usersInfo) {
		 	getUsersInfoCallback(usersInfo, properties, containerId);
		}
	});
}

function getUsersInfoCallback(usersInfo, properties, containerId) {
	if (usersInfo == null) {
		closeLoadingMessage();
		return false;
	}
	
	//Received info about Groups users from 'remote' server
	//Now rendering object in 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getGroupsMembersPresentationObject(usersInfo, properties, {
		callback: function(presentationObject) {
			getGroupsMembersPresentationObjectCallback(presentationObject, containerId);
		}
	});
}

function getGroupsMembersPresentationObjectCallback(presentationObject, containerId) {
	closeLoadingMessage();
	if (presentationObject == null || containerId == null) {
		return false;
	}
	
	var container = document.getElementById(containerId);
	if (container == null) {
		return false;
	}
	removeChildren(container);
	
	insertNodesToContainer(presentationObject, container);
}