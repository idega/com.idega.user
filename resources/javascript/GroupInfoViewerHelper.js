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
	//getGroupPropertiesCallback(new GroupPropertiesBean(), containerId);	//	For testing
}

function GroupPropertiesBean() {
	this.server = 'http://formbuilder.idega.is';
	this.login = 'Administrator';
	this.password = 'idega';
	this.uniqueIds = new Array();
	this.uniqueIds.push('987654321');
	this.uniqueIds.push('dfjikdjgiofdgjnmxcsuandyusndfvyn');
	this.uniqueIds.push('123456789');
	
	this.showName = true;
	this.showHomePage = true;
	this.showDescription = true;
	this.showExtraInfo = true;
	this.showShortName = true;
	this.showPhone = true;
	this.showFax = true;
	this.showEmails = true;
	this.showAddress = true;
	this.showEmptyFields = false;
}

function getGroupPropertiesCallback(properties, containerId) {
	if (properties == null) {
		closeLoadingMessage();
		return false;
	}
	
	if (properties.server == null || properties.login == null || properties.password == null) {
		closeLoadingMessage();
		return false;
	}
	
	GroupService.canUseRemoteServer(properties.server, {
		callback: function(result) {
			canUseRemoteServerForGroupCallback(result, properties, containerId);
		}
	});
}

function canUseRemoteServerForGroupCallback(result, properties, containerId) {
	if (!result) {
		closeLoadingMessage();
		return false;
	}
	
	//	Preparing DWR for remote call
	prepareDwr(GroupService, properties.server + getDefaultDwrPath());
	
	//	Calling method to get info about groups on remote server
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