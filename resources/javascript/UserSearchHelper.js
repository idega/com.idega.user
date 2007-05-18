function executeSearch(containerId, searchKey, message) {
	showLoadingMessage(message);
	removeChildren(document.getElementById(containerId));
	UserSearchEngine.getUserBrowser(searchKey, {
		callback: function(userBrowser) {
			executeSearchCallback(containerId, userBrowser);
		}
	});
}

function executeSearchCallback(containerId, userBrowser) {
	closeLoadingMessage();
	var container = document.getElementById(containerId);
	if (container == null) {
		return;
	}
	insertNodesToContainer(userBrowser, container);
}