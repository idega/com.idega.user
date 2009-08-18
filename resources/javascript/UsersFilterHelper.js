var UsersFilterHelper = {};

UsersFilterHelper.values = {};

UsersFilterHelper.getUsers = function(dropdownId, selectedUsers, loadingMessage, containerId, selectedUserInputName) {
	showLoadingMessage(loadingMessage);
	
	var selectedByUser = UsersFilterHelper.values[selectedUserInputName];
	if (typeof(selectedByUser) == 'undefined') {
		selectedByUser = selectedUsers;
	}
	
	UsersFilter.getUsersInGroup(dwr.util.getValue(dropdownId), selectedByUser, selectedUserInputName, {
		callback: function(component) {
			closeAllLoadingMessages();
			
			IWCORE.insertRenderedComponent(component, {
				container: containerId,
				rewrite: true
			});
		}
	});
}

UsersFilterHelper.assignActionToForm = function(hiddenInputName, selectedValues) {
	UsersFilterHelper.values[hiddenInputName] = selectedValues;
	
	var allForms = document.forms;
	if (allForms == null || allForms.length == 0) {
		return;
	}
	
	var form = allForms[allForms.length - 1];
	if (form == null) {
		return;
	}
	
	registerEvent(form, 'submit', function() {
		UsersFilterHelper.addHiddenInputs(form, hiddenInputName);
	});
}

UsersFilterHelper.addHiddenInputs = function(form, hiddenInputName) {
	var valuesToSubmit = UsersFilterHelper.values[hiddenInputName];
	if (valuesToSubmit == null || valuesToSubmit.length == 0) {
		return;
	}
	
	for (var i = 0; i < valuesToSubmit.length; i++) {
		var hiddenInput = document.createElement('input');
		hiddenInput.type = 'hidden';
		hiddenInput.name = hiddenInputName;
		hiddenInput.value = valuesToSubmit[i];
		form.appendChild(hiddenInput);
	}
}

UsersFilterHelper.markUserInForm = function(checkboxId, hiddenInputName, userId) {
	var selected = document.getElementById(checkboxId).checked;
	
	var oldValues = UsersFilterHelper.values[hiddenInputName];
	if (oldValues == null) {
		oldValues = new Array();
	}
	
	if (selected) {
		oldValues.push(userId);
	} else {
		removeElementFromArray(oldValues, userId);
	}
	
	UsersFilterHelper.values[hiddenInputName] = oldValues;
}