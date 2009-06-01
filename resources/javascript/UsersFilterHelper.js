var UsersFilterHelper = {};

UsersFilterHelper.getUsers = function(dropdownId, selectedUsers, loadingMessage, containerId) {
	showLoadingMessage(loadingMessage);
	UsersFilter.getUsersInGroup(dwr.util.getValue(dropdownId), selectedUsers, {
		callback: function(component) {
			closeAllLoadingMessages();
			
			IWCORE.insertRenderedComponent(component, {
				container: containerId,
				rewrite: true
			});
		}
	});
}