function getSelectedGroups(instanceId, containerId, message) {
	if (instanceId == null) {
		return;
	}

	//showLoadingMessage(message);

	//	To be sure we'll call to 'local' server
	dwr.engine._defaultPath = '/dwr';
	GroupService._path = '/dwr';
	
	//GroupService.getTopGroupNodes(callbackNodes);
	
	/*GroupService.getPropertiesBean(instanceId, {
		callback: function(properties) {
			getPropertiesCallback(properties, containerId);
		}
	});*/
}

function callbackNodes(result) {
	if (result == null) {
		alert('returned null');
		return;
	}
	setNodes(result, 'selected_group_info_container');
}

function getPropertiesCallback(properties, containerId) {
	closeLoadingMessage();
	if (properties == null) {
		return;
	}
	
}