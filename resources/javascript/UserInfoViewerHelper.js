function getSelectedUsers(instanceId, containerId, message) {
	if (instanceId == null) {
		return;
	}

	showLoadingMessage(message);

	//	To be sure we'll call to 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	/*GroupService.getUserPropertiesBean(instanceId, {
		callback: function(properties) {
			getUserPropertiesCallback(properties, containerId);
		}
	});*/
	getUserPropertiesCallback(new UserPropertiesBean(), containerId);	//	For testing
}

function UserPropertiesBean() {
	this.server = 'http://172.16.0.138:8080';
	this.login = 'Administrator';
	this.password = 'idega';
	
	this.uniqueIds = new Array();
	this.uniqueIds.push('987654321');
	this.uniqueIds.push('dfjikdjgiofdgjnmxcsuandyusndfvyn');
	this.uniqueIds.push('123456789');
	
	this.showGroupName = true;
	this.showTitle = true;
	this.showAge = true;
	this.showWorkPhone = true;
	this.showHomePhone = true;
	this.showMobilePhone = true;
	this.showEmails = true;
	this.showEducation = true;
	this.showSchool = true;
	this.showArea = true;
	this.showBeganWork = true;
}

function getUserPropertiesCallback(properties, containerId) {
	if (properties == null) {
		closeLoadingMessage();
		return false;
	}
	
	GroupService.canUseRemoteServer(properties.server, {
		callback: function(result) {
			canUseRemoteServerForUserCallback(result, properties, containerId);
		}
	});
}

function canUseRemoteServerForUserCallback(result, properties, containerId) {
	if (!result) {
		closeLoadingMessage();
		return false;
	}
	
	prepareDwr(GroupService, properties.server + getDefaultDwrPath());
	
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
	
	//Received info about Groups from 'remote' server
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