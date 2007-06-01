function getSelectedGroups(instanceId, containerId, message) {
	if (instanceId == null) {
		return;
	}

	showLoadingMessage(message);

	//	To be sure we'll call to 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getGroupPropertiesBean(instanceId, {
		callback: function(properties) {
			getGroupPropertiesCallback(properties, containerId);
		}
	});
}

function getGroupPropertiesCallback(properties, containerId) {
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
				getGroupsData(result, properties, containerId);
			}
		});
	}
	else {
		//	Local mode
		getGroupsData(true, properties, containerId);
	}
}

function getGroupsData(result, properties, containerId) {
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
	//	Calling method to get info about groups on selected ('local' or 'remote') server
	GroupService.getGroupsInfo(properties, {
		callback: function(groupsInfo) {
		 	getGroupsInfoCallback(groupsInfo, properties, containerId);
		}
	});
}

function getGroupsInfoCallback(groupsInfo, properties, containerId) {
	if (groupsInfo == null || containerId == null) {
		closeLoadingMessage();
		return false;
	}
	//Received info about Groups from 'remote' server
	//Now rendering object in 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getGroupInfoPresentationObject(groupsInfo, properties, {
		callback: function(presentationObject) {
			getGroupInfoPresentationObjectCallback(presentationObject, containerId);
		}
	});
}

function getGroupInfoPresentationObjectCallback(presentationObject, containerId) {
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