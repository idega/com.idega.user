var UsersFilterHelper = {};

UsersFilterHelper.getUsers = function(dropdownId, selectedUsers, loadingMessage, containerId, selectedUserInputName) {
	showLoadingMessage(loadingMessage);
	UsersFilter.getUsersInGroup(dwr.util.getValue(dropdownId), selectedUsers, selectedUserInputName, {
		callback: function(component) {
			closeAllLoadingMessages();
			
			IWCORE.insertRenderedComponent(component, {
				container: containerId,
				rewrite: true
			});
		}
	});
}