var SimpleUserApplication = {}
SimpleUserApplication.userPicture = null;

var USERS_TO_REMOVE = new Array();
var DESELECTED_GROUPS = new Array();

var USER_ID = null;
var SAVE_GROUP_RESULT_IN_HIDDEN_INPUT_ID = 'saveGroupResultInHiddenInputIdForSimpleUserApplication';

var SimpleAppInfo = new Object();	//this is for /is.idega.block.saga/resources/javascript/GroupEditorHelper.js
SimpleAppInfo.isGroupNameOk = true;	
SimpleAppInfo.currentGroupId = -1; //not existing group
SimpleAppInfo.actionsAfterSave = function(savedGroupId){
	SimpleAppInfo.currentGroupId = savedGroupId;
}

function setErrorHandlerForSimpleUserApplication(errorExplanations) {
	var errorHandler = function(e) {
		closeAllLoadingMessages();
		
		var errorFromDwr = null;
		var errorExplanation = errorExplanations[0];
		if (e) {
			if (typeof(e) == 'string') {
				errorFromDwr = e;
			}
			else if (e.message) {
				errorFromDwr = e.message;
			}
		}
		
		if (errorFromDwr != null && errorFromDwr != '') {
			errorExplanation += ' ' + errorExplanations[1] + ': ' + errorFromDwr
		}
		showHumanizedMessage(errorExplanation, null);
	}
	
	dwr.engine.setErrorHandler(errorHandler);
}

function getParentGroupIdInSUA(chooserId, selectedId) {
	var parentGroupId = getSelectObjectValue(chooserId);
	if (parentGroupId == -1 && selectedId != null && selectedId != '') {
		parentGroupId = selectedId;
	}
	return parentGroupId;
}

function reloadComponents(message, childGroupsChooserId, orderByChooserId, containerId, chooserId, groupTypes, groupRoles, params, subGroups, subGroupsToExclude) {
	showLoadingMessage(message);
	var chooser = document.getElementById(childGroupsChooserId);
	if (chooser != null) {
		dwr.util.removeAllOptions(childGroupsChooserId);
	}
	
	var parentGroupId = getParentGroupIdInSUA(chooserId, params[16]);
	
	UserApplicationEngine.getChildGroups(parentGroupId, groupTypes, groupRoles, subGroups, subGroupsToExclude, {
		callback: function(childGroups) {
			getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params);
		}
	});
}

function getChildGroupsCallback(childGroups, childGroupsChooserId, orderByChooserId, containerId, chooserId, message, params) {
	closeAllLoadingMessages();
	
	var chooser = $(childGroupsChooserId);
	if (chooser == null) {
		return false;
	}
	chooser.removeProperty('disabled');
	if (childGroups == null) {
		chooser.setProperty('disabled', true);
	}
	else {
		dwr.util.addOptions(childGroupsChooserId, childGroups, 'id', 'value');
	}
	
	var groupId = getSelectObjectValue(childGroupsChooserId);
	selectChildGroup(childGroupsChooserId, containerId, chooserId, orderByChooserId, message, params);
}

function getSelectObjectValue(id) {
	if (id == null) {
		return -1;
	}
	var select = document.getElementById(id);
	if (select == null) {
		return -1;
	}
	if (select.value) {
		return select.value;
	}
	return -1;
}

function reOrderGroupUsers(parentGroupChooserId, childGroupChooserId, orderByChooserId, containerId, message, params) {
	var groupId = getSelectObjectValue(childGroupChooserId);
	selectChildGroup(childGroupChooserId, containerId, parentGroupChooserId, orderByChooserId, message, params);
}

function selectChildGroup(groupChooserId, containerId, parentGroupChooserId, orderByChooserId, message, parameters) {
	showLoadingMessage(message);
	var parentGroupId = getParentGroupIdInSUA(parentGroupChooserId, parameters[16]);
	var groupId = getSelectObjectValue(groupChooserId);
	var orderBy = getSelectObjectValue(orderByChooserId);
	
	var bean = new SimpleUserPropertiesBeanWithParameters(parentGroupId, groupId, orderBy, parameters);
	
	UserApplicationEngine.getMembersList(bean, containerId, {
		callback: function(component) {
			getMembersListCallback(component, containerId);
		}
	});
}

function getMembersListCallback(component, containerId) {
	closeAllLoadingMessages();
	USERS_TO_REMOVE = new Array();
	var container = document.getElementById(containerId);
	if (container == null) {
		return false;
	}
	
	removeChildren(container);
	insertNodesToContainer(component, container);
	return true;
}

function AdvancedProperty(id, value) {
	this.id = id;
	this.value = value;
}

function MarkedUsers(containerId, userId, groupId) {
	this.containerId = containerId;
	this.userId = userId;
	this.groupId = groupId;
}

function removeSelectedUsers(message, areYouSure, nothingSelected) {
	if (USERS_TO_REMOVE.length == 0) {
		showHumanizedMessage(nothingSelected, null);
		return false;
	}
	
	var confirmDelete = false;
	confirmDelete = window.confirm(areYouSure);
	if (!confirmDelete) {
		return false;
	}
	
	showLoadingMessage(message);
	var users = new Array();
	var groupId = -1;
	for (var i = 0; i < USERS_TO_REMOVE.length; i++) {
		groupId = USERS_TO_REMOVE[i].groupId;
		users.push(USERS_TO_REMOVE[i].userId);
	}
	UserApplicationEngine.removeUsers(users, groupId, removeUsersCallback);
}

function removeUsersCallback(removedUsers) {
	closeAllLoadingMessages();
	if (removedUsers == null) {
		return false;
	}
	
	var userId = -1;
	var markedUser = null;
	for (var i = 0; i < removedUsers.length; i++) {
		markedUser = findMarkedUserById(removedUsers[i]);
		if (markedUser != null) {
			var lineContainer = document.getElementById(markedUser.containerId);
			if (lineContainer != null) {
				var parentContainer = lineContainer.parentNode;
				parentContainer.removeChild(lineContainer);
			}
		}
	}
	
	USERS_TO_REMOVE = new Array();
	return true;
}

function findMarkedUserById(userId) {
	if (USERS_TO_REMOVE.length == 0) {
		return null;
	}
	
	var found = false;
	var markedUser = null;
	for (var i = 0; (i < USERS_TO_REMOVE.length && !found); i++) {
		markedUser = USERS_TO_REMOVE[i];
		if (markedUser.userId == userId) {
			found = true;
		}
	}
	
	if (found) {
		return markedUser;
	}
	return null;
}

function findMarkedUser(containerId, userId, groupId) {
	var index = -1;
	var markedUser = null;
	var found = false;
	for (var i = 0; (i < USERS_TO_REMOVE.length && !found); i++) {
		markedUser = USERS_TO_REMOVE[i];
		if (markedUser.containerId == containerId && markedUser.userId == userId && markedUser.groupId == groupId) {
			found = true;
			index = i;
		}
	}
	return index;
}

function removeUser(containerId, userId, groupId, checkBoxId) {
	var index = findMarkedUser(containerId, userId, groupId);
	if (index != -1) {
		USERS_TO_REMOVE.splice(index, 1);
	}
	
	var isChecked = false;
	var checkbox = document.getElementById(checkBoxId);
	if (checkbox != null) {
		isChecked = checkbox.checked;
	}
	
	if (isChecked) {
		USERS_TO_REMOVE.push(new MarkedUsers(containerId, userId, groupId));
	}
}

function addUserPresentationObject(instanceId, containerId, parentGroupChooserId, groupChooserId, message, defaultGroupId, userId,
										groupTypes, roleTypes, getParentGroupsFromTopNodes, groupTypesForParentGroups,
										useChildrenOfTopNodesAsParentGroups, allFieldsEditable, juridicalPerson, changePasswordNextTime, sendMailToUser,
										allowEnableDisableAccount, selectedParentGroupId, parentGroups, parentGroupsToExclude, subGroups, subGroupsToExclude) {
	USER_ID = userId;
	
	refreshDeselectedGroups();
	showLoadingMessage(message);
	
	var parentGroupId = getParentGroupIdInSUA(parentGroupChooserId, selectedParentGroupId);
	var groupId = getSelectObjectValue(groupChooserId);
	
	//	Properties bean
	var bean = new SimpleUserPropertiesBean(instanceId, parentGroupId, groupId, defaultGroupId, containerId, groupTypes, roleTypes, getParentGroupsFromTopNodes,
											groupTypesForParentGroups, useChildrenOfTopNodesAsParentGroups, allFieldsEditable, juridicalPerson,
											changePasswordNextTime, sendMailToUser, allowEnableDisableAccount, parentGroups, parentGroupsToExclude, subGroups, subGroupsToExclude);
	
	//	Parent groups
	var parentGroups = getSelectObjectValues(parentGroupChooserId);
	
	//	Groups
	var groups = getSelectObjectValues(groupChooserId);
	
	UserApplicationEngine.getAddUserPresentationObject(bean, parentGroups, groups, userId, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		}
	});
}

function getSelectObjectValues(id) {
	if (id == null) {
		return null;
	}
	
	var chooser = document.getElementById(id);
	if (chooser == null) {
		return null;
	}
	var options = chooser.options;
	if (options == null) {
		return null;
	}
	var values = new Array();
	for (var i = 0; i < options.length; i++) {
		values.push(options[i].value);
	}
	
	return values;
}

function getAddUserPresentationObjectCallback(component, containerId) {
	closeAllLoadingMessages();
	
	if (component == null) {
		return false;
	}
	var container = document.getElementById(containerId);
	if (container == null) {
		return false;
	}
	
	removeChildren(container);
	insertNodesToContainer(component, container);
	jQuery('body').trigger('simple-user-app-loaded');
	return true;
}
jQuery(document).ready(function(){
	jQuery('body').trigger('simple-user-app-loaded');
});

function goBackToSimpleUserApp(instanceId, containerId, message, parentGroupChooserId, selectedParentGroupId) {
	refreshDeselectedGroups();
	
	var parentGroupId = getParentGroupIdInSUA(parentGroupChooserId, selectedParentGroupId);
	
	showLoadingMessage(message);
	UserApplicationEngine.getSimpleUserApplication(instanceId, parentGroupId, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		} 
	});
}

function reloadAvailableGroupsForUser(parentGroupChooserId, userId, parameters, selectedGroupId, subGroups, subGroupsToExclude, parentGroups) {
	refreshDeselectedGroups();
	var groupId = getParentGroupIdInSUA(parentGroupChooserId, selectedGroupId);
	var containerId = parameters[0];
	var message = parameters[1];
	var groupTypes = parameters[2];
	var roleTypes = parameters[3];
	showLoadingMessage(message);
	UserApplicationEngine.getAvailableGroupsForUserPresentationObject(groupId, userId, groupTypes, roleTypes, subGroups, subGroupsToExclude, parentGroups, {
		callback: function(component) {
			getAddUserPresentationObjectCallback(component, containerId);
		}
	});
}

function getUserByPersonalId(event, parameters, allFieldsEditable, customCallback) {
	if (parameters == null) {
		return false;
	}

	var valueInputId = parameters[0];
	if (valueInputId == null) {
		return false;
	}
	var input = document.getElementById(valueInputId);
	if (input == null) {
		return false;
	}
	var personalId = input.value;
	if (personalId == null) {
		return false;
	}
	if (!isEnterEvent(event) && personalId.length < 10) {
		return false;
	}
	
	var lastIndex = personalId.length;
	var lastChar = personalId.charAt(lastIndex-1);
	if (lastChar == '-') {
		personalId = personalId.substring(0, lastIndex-1);
	}
	
	var id = userApplicationRemoveSpaces(personalId);
	showLoadingMessage(parameters.length >= 5 ? parameters[4] : '');
	UserApplicationEngine.getUserByPersonalId(id, {
		callback: function(info) {
			if (customCallback) {
				customCallback(info);
				return;
			}
			
			getUserByPersonalIdCallback(info, parameters, allFieldsEditable, personalId);
		}
	});
}

function userApplicationRemoveSpaces(value) {
	if (value == null) {
		return null;
	}
	return value.replace(/^\s+|\s+$/g, '');
}

function getUserByPersonalIdCallback(bean, parameters, allFieldsEditable, personalId) {
	closeAllLoadingMessages();
	if (bean == null) {
		return false;
	}
	if (bean.errorMessage != null) {
		showHumanizedMessage(bean.errorMessage, null);
		return false;
	}
	
	USER_ID = bean.userId;
	
	var nameInput = document.getElementById(parameters[1]);
	if (nameInput == null) {
		return false;
	}
	var loginInput = document.getElementById(parameters[2]);
	/*var passwordInput = document.getElementById(parameters[3]);
	if (passwordInput != null) {
		passwordInput.value = '';
	}*/
	var emailInput = document.getElementById(parameters[5]);
	if (emailInput != null) {
		emailInput.value = '';
	}
	nameInput.value = '';
	document.getElementById(parameters[12]).value = '';
	document.getElementById(parameters[6]).value = '';
	document.getElementById(parameters[7]).value = '';
	document.getElementById(parameters[11]).value = '';
	document.getElementById(parameters[9]).value = '';
	document.getElementById(parameters[10]).value = '';
	dwr.util.setValue(document.getElementById(parameters[8]), '-1');
	if (loginInput != null) {
		UserApplicationEngine.getUserLogin(personalId, {
			callback: function(userLogin) {
				if (userLogin == null) {
					loginInput.disabled = false;
				} else {
					loginInput.value = userLogin;
				}
			}
		});
	}
	
	refreshDeselectedGroups();
	
	setValueForUserInput(nameInput, bean.name, allFieldsEditable);												//	Name
	setValueForUserInput(document.getElementById(parameters[12]), bean.phone, allFieldsEditable);				//	Phone
	setValueForUserInput(emailInput, bean.email, allFieldsEditable);											//	Email
	setValueForUserInput(document.getElementById(parameters[6]), bean.streetNameAndNumber, allFieldsEditable);	//	Street name and number
	setValueForUserInput(document.getElementById(parameters[7]), bean.postalCodeId, allFieldsEditable);			//	Postal code
	setValueForUserInput(document.getElementById(parameters[11]), bean.postalBox, allFieldsEditable);			//	Postal box
	setValueForUserInput(document.getElementById(parameters[9]), bean.city, allFieldsEditable);					//	City
	setValueForUserInput(document.getElementById(parameters[10]), bean.province, allFieldsEditable);			//	Province

	//setValueForUserInput(loginInput, bean.login, allFieldsEditable);											//	Login
	//setValueForUserInput(passwordInput, bean.password, allFieldsEditable);									//	Password
	
	if (allFieldsEditable) {
		UserApplicationEngine.getCountryIdByCountryName(bean.countryName, {
			callback: function(id) {
				if (id == null) {
					return;
				}
				
				dwr.util.setValue(document.getElementById(parameters[8]), id);									//	Country
			}
		});
	}
	
	var enableAccount = document.getElementById(parameters[13]);												//	Account enabled
	if (enableAccount != null) {
		enableAccount.checked = bean.accountEnabled ? true : false;
	}
	var changePassword = document.getElementById(parameters[14]);												//	Change password
	if (changePassword != null) {
		changePassword.checked = bean.changePasswordNextTime ? true : false;
	}
	
	return true;
}

function setValueForUserInput(input, value, allFieldsEditable) {
	if (input == null) {
		return false;
	}
	
	input.value = '';
	if (value == null || value == '' || value == 'null') {
		input.removeAttribute('disabled');
		return false;
	}
	
	input.value = value;
	if (allFieldsEditable) {
		input.removeAttribute('disabled');
	}
	else {
		input.setAttribute('disabled', 'true');
	}
}

function saveUserInSimpleUserApplication(ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson) {
	showLoadingMessage(messages[0]);
	var emailInputId = ids[5];
	var email = document.getElementById(emailInputId).value;
	UserApplicationEngine.isValidEmail(email, {
		callback: function(result) {
			isValidUserEmailCallback(result, ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson);
		}
	});
}

function isValidUserEmailCallback(result, ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson) {
	closeAllLoadingMessages();
	if (result != null) {
		showHumanizedMessage(result, ids[5]);
		return false;
	}
	
	/*if (USER_ID == null) {
		var loginInputId = ids[2];
		var userName = document.getElementById(loginInputId).value;
		UserApplicationEngine.isValidUserName(userName, {
			callback: function(userNameCheckResult) {
				isValidUserNameCallback(userNameCheckResult, ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson);
			}
		});
	} else {*/
		isValidUserNameCallback({id: 'true', value: null}, ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson);
	//}
}

function isValidUserNameCallback(userNameCheckResult, ids, childGroups, messages, allFieldsEditable, userId, sendEmailWithLoginInfo, juridicalPerson) {
	closeAllLoadingMessages();
	if (userNameCheckResult != null && userNameCheckResult.id == 'false') {
		showHumanizedMessage(userNameCheckResult.value, ids[2]);
		return false;
	}
	
	var parentGroupChooserId = ids[0];
	var nameValueInputId = ids[1];
	var loginInputId = ids[2];
	var passwordInputId = ids[3];
	var groupForUsersWithoutLoginId = ids[4];
	var emailInputId = ids[5];
	var phoneInputId = ids[6];
	var streetNameAndNumberInputId = ids[7];
	var personalIdInputId = ids[8];
	var postalCodeIdInputId = ids[9];
	var countriesDropdownId = ids[10];
	var cityInputId = ids[11];
	var provinceInputId = ids[12];
	var postalBoxInputId = ids[13];
	
	var changePasswordNextTime = isCheckboxChecked(ids[15]);
	var accountEnabled = isCheckboxChecked(ids[14]);
	
	var language1id =ids[18];
	var language2id =ids[19];
	var language3id =ids[20];
	var language4id =ids[21];
	
	var genderId =ids[22];
	var birthDayId =ids[23];
	var skillLevelId =ids[24];
	
	var selectedGroups = new Array();
	if (childGroups == null) {
		if (groupForUsersWithoutLoginId != null && groupForUsersWithoutLoginId != 'null') {
			selectedGroups.push(groupForUsersWithoutLoginId);
		} else {
			jQuery('input.selectSubGroupInSimpleUserAppCheckBoxStyle').each(function() {
				var checkbox = this;
				if (checkbox.checked) {
					selectedGroups.push(this.value);
				}
			});
		}
	} else {
		var checkboxValue = null;
		for (var i = 0; i < childGroups.length; i++) {
			checkboxValue = getCheckboxValue(childGroups[i], true);
			if (checkboxValue != null) {
				selectedGroups.push(checkboxValue);
			}
		}
		
		if (selectedGroups.length == 0 && DESELECTED_GROUPS.length == 0) {
			//	Mostly ids changed
			var checkBoxesInSUA = getElementsByClassName(document.body, 'input', 'selectSubGroupInSimpleUserAppCheckBoxStyle');
			if (checkBoxesInSUA != null) {
				for (var i = 0; i < checkBoxesInSUA.length; i++) {
					checkboxValue = getCheckboxValueFromCheckBox(checkBoxesInSUA[i], true);
					if (checkboxValue != null) {
						selectedGroups.push(checkboxValue);
					}
				}
			}
		}
		if (selectedGroups.length == 0 && DESELECTED_GROUPS.length == 0) {
			selectedGroups.push(getCheckboxValue(childGroups[0], false));	// Nothing selected, adding the first group
		}
	}
	
	//	Name
	var userName = getElementValue(nameValueInputId);
	if (userName == null) {
		showHumanizedMessage(messages[2], nameValueInputId);
		return false;
	}
	
	//	Login
	var login = loginInputId == '-' ? null : getElementValue(loginInputId);
	/*if (login == null) {
		showHumanizedMessage(messages[3], loginInputId);
		return false;
	}*/
	
	var personalId = getElementValue(personalIdInputId);
	
	//	Password
	var password = passwordInputId == '-' ? null : getElementValue(passwordInputId);
	/*if (password == null && !juridicalPerson) {
		showHumanizedMessage(messages[1], passwordInputId);
		return false;
	}*/
	
	var email = document.getElementById(emailInputId).value;
	var primaryGroupId = getParentGroupIdInSUA(parentGroupChooserId, ids[16]);
	var phone = document.getElementById(phoneInputId).value;
	var streetNameAndNumber = document.getElementById(streetNameAndNumberInputId).value;
	var postalCodeId = document.getElementById(postalCodeIdInputId).value;
	var countryName = dwr.util.getValue(document.getElementById(countriesDropdownId));
	var city = document.getElementById(cityInputId).value;
	var province = document.getElementById(provinceInputId).value;
	var postalBox = document.getElementById(postalBoxInputId).value;
	var language1 = document.getElementById(language1id).value;
	var language2 = document.getElementById(language2id).value;
	var language3 = document.getElementById(language3id).value;
	var language4 = document.getElementById(language4id).value;
	
	var gender = document.getElementById(genderId).value;
	var birthDay = document.getElementById(birthDayId).value;
	var skillLevel = document.getElementById(skillLevelId).value;
	
	if (USER_ID != null) {
		if (USER_ID != userId) {
			userId = USER_ID;
		}
	}
	
	showLoadingMessage(messages[0]);
	var userInfo = new UserDataBean(userName, /*login, password,*/ personalId, email, null, phone, streetNameAndNumber, postalCodeId, countryName, city, province,
									postalBox, userId, juridicalPerson, changePasswordNextTime, accountEnabled, language1, language2, language3, language4, gender, birthDay, skillLevel);
	UserApplicationEngine.createUser(userInfo, primaryGroupId, selectedGroups, DESELECTED_GROUPS, allFieldsEditable, sendEmailWithLoginInfo, login,
		password, {
		callback: function(result) {
			closeAllLoadingMessages();
			if (result != null) {
				USER_ID = result.id;
				
				if (result.value != null) {
					showHumanizedMessage(result.value, null);
				}
			}
		}
	});
}

function showHumanizedMessage(message, inputId) {
	if (inputId != null) {
		var element = jQuery('#' + inputId);
		if (element != null) {
			var disabledAttr = element.attr('disabled');
			if (disabledAttr == null || disabledAttr == false || disabledAttr == 'false') {
				element.focus();
			}
		}
	}
	humanMsg.displayMsg(message);
}

function getElementValue(id) {
	var element = jQuery('#' + id);
	var value = element == null ? null : element.attr('value');
	return value == '' ? null : value;
}

function UserDataBean(name, /*login, password,*/ personalId, email, errorMessage, phone, streetNameAndNumber, postalCodeId, countryName, city, province, postalBox,
					 userId, juridicalPerson, changePasswordNextTime, accountEnabled, language1, language2, language3, language4, gender, birthDay, skillLevel) {
	this.name = name;
	//this.login = login;
	//this.password = password;
	this.personalId = personalId;
	this.email = email;
	this.errorMessage = errorMessage;
	this.phone = phone == null ? null : phone.trim();
	this.streetNameAndNumber = streetNameAndNumber;
	this.postalCodeId = postalCodeId;
	this.countryName = countryName;
	this.city = city;
	this.province = province;
	this.postalBox = postalBox;
	this.userId = userId;
	this.juridicalPerson = juridicalPerson;
	this.changePasswordNextTime = changePasswordNextTime;
	this.accountEnabled = accountEnabled;
	this.pictureUri = SimpleUserApplication.userPicture;
	this.metadata = {};
	this.metadata.language1 = language1;
	this.metadata.language2 = language2;
	this.metadata.language3 = language3;
	this.metadata.language4 = language4;
	this.metadata.gender = gender;
	this.metadata.birthDay = birthDay;
	this.metadata.skillLevel = skillLevel;
}

function isCheckboxChecked(id) {
	if (id == null) {
		return null;
	}
	
	var checkBox = document.getElementById(id);
	if (checkBox == null) {
		return null;
	}
	
	return checkBox.checked;
}

function getCheckboxValue(id, checkIfChecked) {
	if (id == null) {
		return null;
	}

	return getCheckboxValueFromCheckBox(document.getElementById(id), checkIfChecked);
}

function getCheckboxValueFromCheckBox(checkbox, checkIfChecked) {
	if (checkbox == null) {
		return null;
	}
	if (checkIfChecked) {
		if (checkbox.checked) {
			return checkbox.value;
		}
		return null;
	}
	return checkbox.value;
}

function SimpleUserPropertiesBean(instanceId, parentGroupId, groupId, defaultGroupId, containerId, groupTypes, roleTypes, getParentGroupsFromTopNodes,
									groupTypesForParentGroups, useChildrenOfTopNodesAsParentGroups, allFieldsEditable, juridicalPerson, changePasswordNextTime,
									sendMailToUser, allowEnableDisableAccount, parentGroups, parentGroupsToExclude, subGroups, subGroupsToExclude) {
	
	this.parentGroupId = parentGroupId;
	this.groupId = groupId;
	
	this.instanceId = instanceId;
	this.containerId = containerId;

	this.defaultGroupId = defaultGroupId;
	this.groupTypes = groupTypes;
	this.roleTypes = roleTypes;
	
	this.groupTypesForParentGroups = groupTypesForParentGroups;
	
	this.getParentGroupsFromTopNodes = getParentGroupsFromTopNodes;
	this.useChildrenOfTopNodesAsParentGroups = useChildrenOfTopNodesAsParentGroups;
	this.allFieldsEditable = allFieldsEditable;
	this.juridicalPerson = juridicalPerson;
	this.sendMailToUser = sendMailToUser;
	this.changePasswordNextTime = changePasswordNextTime;
	this.allowEnableDisableAccount = allowEnableDisableAccount;
	
	this.parentGroups = parentGroups;
	this.parentGroupsToExclude = parentGroupsToExclude;
	this.subGroups = subGroups;
	this.subGroupsToExclude = subGroupsToExclude;
}

function SimpleUserPropertiesBeanWithParameters(parentGroupId, groupId, orderBy, parameters) {
	this.instanceId = null;
	this.containerId = null;
	this.parentGroupChooserId = null;
	this.groupChooserId = null;
	this.message = null;
	this.defaultGroupId = null;
	this.groupTypes = null;
	this.roleTypes = null;
	
	this.count = 20;
	this.orderBy = orderBy;
	
	this.groupTypesForParentGroups = null;
	this.useChildrenOfTopNodesAsParentGroups = false;
	this.allFieldsEditable = false;
	
	this.parentGroupId = parentGroupId;
	this.groupId = groupId;
	
	if (parameters == null) {
		return;
	}
	if (parameters.length < 16) {
		return;
	}
	this.instanceId = parameters[0];
	this.containerId = parameters[1];
	this.parentGroupChooserId = parameters[7];
	this.groupChooserId = parameters[2];
	this.message = parameters[6];
	this.defaultGroupId = parameters[3];
	this.groupTypes = parameters[4];
	this.roleTypes = parameters[5];
	this.groupTypesForParentGroups = parameters[8];
	this.useChildrenOfTopNodesAsParentGroups = parameters[9];
	this.allFieldsEditable = parameters[10];
	this.juridicalPerson = parameters[12];
	this.sendMailToUser = parameters[13];
	this.changePasswordNextTime = parameters[14];
	this.allowEnableDisableAccount = parameters[15];
}

function refreshDeselectedGroups() {
	DESELECTED_GROUPS = new Array();
}

function deselectUserFromGroup(groupId) {
	var existsId = false;
	for (var i = 0; (i < DESELECTED_GROUPS.length && !existsId); i++) {
		if (DESELECTED_GROUPS[i] == groupId) {
			existsId = true;
		}
	}
	if (existsId)
		return;
	
	var checkbox = jQuery('input.selectSubGroupInSimpleUserAppCheckBoxStyle[value=\'' + groupId + '\']');
	if (checkbox != null && (checkbox.attr('checked') == 'checked' || checkbox.attr('checked') == 'true')) {
		removeElementFromArray(DESELECTED_GROUPS, groupId);
		return;
	}
	
	DESELECTED_GROUPS.push(groupId);
}

function createTabsWithMootabs(id) {
	var dialogWidth = getEditOrCreateDialogWidth() - 20;
	var dialogHeight = getEditOrCreateDialogHeight() - 70;
	var tabs = new mootabs(id, {width: dialogWidth + 'px',
		height: dialogHeight + 'px', changeTransition: 'none'});
}

function createOrModifyGroup(parameters, getTopAndParentGroups, useChildrenOfTopNodesAsParentGroups, isEditAction) {
	var uri = parameters[0];
	var chooserId = parameters[1];
	var groupsType = parameters[2];
	
	var groupTypes = parameters[3];
	var groupTypesForChildrenGroups = parameters[4];
	var roleTypes = parameters[5];
	var message = parameters[6];
	
	var groupId = -1;
	if (isEditAction) {
		groupId = getParentGroupIdInSUA(chooserId, parameters[12]);
	}
	if (isEditAction && groupId == -1) {
		return false;
	}
	if (groupId != -1) {
		uri += '&' + parameters[7] + '=' + groupId;
	}
	
	var availableGroupTypes = groupTypes;
	var parentGroupId = -1;
	var parentGroupChooserId = parameters[9];
	if (parentGroupChooserId != null) {
		parentGroupId = getParentGroupIdInSUA(parentGroupChooserId, parameters[12]);
	}
	if (parentGroupId != -1) {
		//	Working with child group!
		uri += '&' + parameters[8] + '=' + parentGroupId;
		availableGroupTypes = groupTypesForChildrenGroups;
	}
	if (availableGroupTypes != null && availableGroupTypes != '') {
		uri += '&' + parameters[10] + '=' + availableGroupTypes;
	}
	if (roleTypes != null && roleTypes != '') {
		uri += '&' + parameters[11] + '=' + roleTypes;
	}
	
	//	To avoid stupid caching in IE
	var date = new Date();
	uri += '&openTime=' + date.getTime();
	
	var dialogWidth = getEditOrCreateDialogWidth();
	var dialogHeight = getEditOrCreateDialogHeight();
	MOOdalBox.init({resizeDuration: 0, evalScripts: true, animateCaption: false,
		defContentsWidth: dialogWidth, defContentsHeight: dialogHeight});
	var actionOnCLose = function() {
		var hiddenInput = $(SAVE_GROUP_RESULT_IN_HIDDEN_INPUT_ID);
		var needToReloadGroups = false;
		if (hiddenInput != null) {
			needToReloadGroups = hiddenInput.getProperty('value') == '1';
			hiddenInput.setProperty('value', '0');
		}
		if (needToReloadGroups) {
			showLoadingMessage(message);
			UserApplicationEngine.getAvailableGroups(groupTypes, groupTypesForChildrenGroups, roleTypes, parentGroupId, groupsType, getTopAndParentGroups,
			 useChildrenOfTopNodesAsParentGroups, {
				callback: function(groups) {
					closeAllLoadingMessages();
					
					var menu = $(chooserId);
					if (menu == null) {
						return false;
					}
					menu.removeProperty('disabled');
					var selectedIndex = menu.selectedIndex;
					menu.empty();
					
					if (groups == null || groups.length == 0) {
						menu.setProperty('disabled', true);
						return false;
					}
					
					for (var i = 0; i < groups.length; i++) {
						var option = new Element('option');
						option.setProperty('value', groups[i].id);
						option.setText(groups[i].value);
						// TODO: generate ids?
						option.injectInside(menu);
					}
					if (selectedIndex != null && selectedIndex >= 0) {
						menu.selectedIndex = selectedIndex;
					}
					else {
						menu.selectedIndex = 0;
					}
				}
			});
		}
	}
	MOOdalBox.addEventToCloseAction(actionOnCLose);
	MOOdalBox.open(uri, '', '');
}


function areAllFieldsOk(){
	return SimpleAppInfo.isGroupNameOk;
}

function saveGroupInSimpleUserApplication(ids, selectedRoles) {
	if(!areAllFieldsOk()){
		showHumanizedMessage(ids[8], null);
		return;
	}
	var nameId = ids[0];
	var homePageId = ids[1];
	var groupTypeId = ids[2];
	var descriptionId = ids[3];
	var groupId = ids[4];
	var parentGroupId = ids[5];
	var containerId = ids[6];
	
	var message = ids[7];
	
	var name = dwr.util.getValue(nameId);
	var homePage = dwr.util.getValue(homePageId);
	homePage = homePage == '-1' ? null : homePage;
	var groupType = dwr.util.getValue(groupTypeId);
	var description = dwr.util.getValue(descriptionId);
	var group = dwr.util.getValue(groupId);
	group = group == '-1' ? null : group;
	var parentGroup = dwr.util.getValue(parentGroupId);
	parentGroup = parentGroup == '-1' ? null : parentGroup;
	
	showLoadingMessage(message);
	UserApplicationEngine.saveGroup(name, homePage, groupType, description, parentGroup, group, {
		callback: function(savedGroupId) {
			SimpleAppInfo.actionsAfterSave(savedGroupId);
			var container = $(containerId);
			if (container == null) {
				closeAllLoadingMessages();
				return false;
			}
			
			var hiddenInput = $(SAVE_GROUP_RESULT_IN_HIDDEN_INPUT_ID);
			if (hiddenInput == null) {
				hiddenInput = new Element('input');
				hiddenInput.setProperty('type', 'hidden');
				hiddenInput.setProperty('id', SAVE_GROUP_RESULT_IN_HIDDEN_INPUT_ID);
				hiddenInput.injectInside(container);
			}
			hiddenInput.setProperty('value', savedGroupId != null ? '1' : '0');
			
			UserApplicationEngine.getGroupSaveStatus(savedGroupId == null, {
				callback: function(message) {
					closeAllLoadingMessages();
					showHumanizedMessage(message, null);
					
					if (savedGroupId != null) {
						UserApplicationEngine.getRenderedRolesEditor(savedGroupId, selectedRoles, {
							callback: function(component) {
								if (component == null) {
									return false;
								}
								
								var inputs = getElementsByClassName(document.body, 'input', 'addNewRoleInputStyleClass');
								if (inputs == null || inputs.length == 0) {
									return false;
								}
								var input = $(inputs[0]);
								input.setProperty('groupid', savedGroupId);
								
								var containers = getElementsByClassName(document.body, 'div', 'checkboxesForGroupRoleEditorStyleClass');
								if (containers == null || containers.length == 0) {
									return false;
								}
								var rolesEditorContainer = $(containers[0]);
								rolesEditorContainer.empty();
								insertNodesToContainer(component, rolesEditorContainer);
							}
						});
					}
				}
			});
		}
	});
}

var LAST_PHRASE_FOR_PAGE_SEARCH = null;
var SEARCH_IN_PROGRESS = false;
function findAvailablePages(inputId, parameterName, message) {
	if (inputId == null) {
		return false;
	}
	var input = $(inputId);
	var phrase = input == null ? null : input.getProperty('value');
	if (phrase == null) {
		$(parameterName).setProperty('value', '-1');
		return false;
	}
	if (phrase == '' && LAST_PHRASE_FOR_PAGE_SEARCH == null) {
		return false;
	}
	if (LAST_PHRASE_FOR_PAGE_SEARCH == phrase) {
		return false;
	}
	else {
		LAST_PHRASE_FOR_PAGE_SEARCH = phrase;
	}
	if (SEARCH_IN_PROGRESS) {
		return false;
	}
	
	if (phrase == '') {
		$(parameterName).setProperty('value', '-1');
	}
	
	SEARCH_IN_PROGRESS = true;
	UserApplicationEngine.findAvailablePages(phrase, {
		callback: function(pages) {
			SEARCH_IN_PROGRESS = false;
			
			var id = 'dynamicPopUpBoxStyleInSimpleUserApplicationForCreateGroup';
			var pagesContainer = $(id);
			var useShowEfect = false;
			if (pagesContainer == null) {
				useShowEfect = true;
				var fromTop = getAbsoluteTop(inputId);
				var fromLeft = getAbsoluteLeft(inputId);
				var pagesContainer = new Element('div');
				pagesContainer.setProperty('id', id);
				pagesContainer.setStyle('top', (fromTop + 23) + 'px');
				pagesContainer.setStyle('left', fromLeft + 'px');
				pagesContainer.setStyle('position', 'absolute');
				pagesContainer.setStyle('z-index', 999999);
				pagesContainer.setStyle('background-color', '#fff');
				pagesContainer.setStyle('opacity', 0);
				pagesContainer.addClass('dynamicPopUpBoxStyleInSimpleUserApplicationStyle');
				pagesContainer.addEvent('click', function() {
					pagesContainer.remove();
				});
			}
			else {
				pagesContainer.empty();
			}
			
			$(document).addEvent('click', function() {
				var boxToHide = $(id);
				if (boxToHide != null) {
					boxToHide.remove();
				}
				return;
			});
			pagesContainer.injectInside($(document.body));
			
			if (pages == null || pages.length == 0) {
				pagesContainer.setText(message + ' \"'+phrase + '\"');
			}
			else {
				for (var i = 0; i < pages.length; i++) {
					var linkContainer = new Element('div');
					linkContainer.addEvent('click', function() {
						pagesContainer.remove();
					});
					
					var link = new Element('a');
					link.addEvent('click', function(event) {
						var event = new Event(event);
						
						input.setProperty('value', $(event.target).getProperty('pagevalue'));
						$(parameterName).setProperty('value', ($(event.target).getProperty('pageid')));
						
						pagesContainer.remove();
						event.stop();
					});
					link.setText(pages[i].value);
					link.setProperty('href', 'javascript:void(0)');
					link.setProperty('pageid', pages[i].id);
					link.setProperty('pagevalue', pages[i].value);
					link.injectInside(linkContainer);
					linkContainer.injectInside(pagesContainer);
				}
			}
			
			if (useShowEfect) {
				var showBoxEffect = new Fx.Style(pagesContainer, 'opacity', {duration: 150});
				showBoxEffect.start(0, 1);
			}
		}
	});
}

function changePermissionValueForRole(id, message) {
	var checkbox = $(id);
	if (checkbox == null) {
		return false;
	}
	
	var groupId = checkbox.getProperty('groupid');
	var permissionKey = checkbox.getProperty('name');
	var roleKey = checkbox.getProperty('value');
	showLoadingMessage(message);
	UserApplicationEngine.changePermissionValueForRole(groupId, permissionKey, roleKey, checkbox.checked, {
		callback: function(result) {
			closeAllLoadingMessages();
		}
	});
}

function addNewRoleKey(event, id, containerId, message, selectedRoles) {
	if (event == null) {
		return false;
	}
	
	var event = new Event(event);
	if (event.key != 'enter') {
		return false;
	}
	
	var input = $(id);
	if (input == null) {
		return false;
	}
	
	var newRoleKey = input.getProperty('value');
	if (newRoleKey == null || newRoleKey == '') {
		return false;
	}
	
	showLoadingMessage(message);
	input.setProperty('value', '');
	UserApplicationEngine.addNewRole(newRoleKey, input.getProperty('groupid'), selectedRoles, {
		callback: function(component) {
			closeAllLoadingMessages();
			event.stop();
			
			if (component == null) {
				return false;
			}
			
			var container = $(containerId);
			if (container == null) {
				return false;
			}
			container.empty();
			insertNodesToContainer(component, container);
		}
	});
}

function navigateInUsersList(params, beanParameters, orderBy, index, moveToLeft, stayOnTheSamePage) {
	if (!stayOnTheSamePage) {
		stayOnTheSamePage = false;
	}
	var containerId = params[0];
	var message = params[1];
	var pageSize = dwr.util.getValue(params[2]);
	if (pageSize == null || pageSize == '' || isNaN(pageSize) || pageSize <= 0) {
		showHumanizedMessage(params[3], params[2]);
		return false;
	}
	
	var parentGroupId = getParentGroupIdInSUA(params[4], beanParameters[11]);
	var bean = new SimpleUserPropertiesBeanWithParameters(parentGroupId, params[5], orderBy, beanParameters);
	bean.count = pageSize;
	bean.from = (stayOnTheSamePage || moveToLeft) ? index - pageSize : index;
	
	showLoadingMessage(message);
	UserApplicationEngine.getMembersList(bean, containerId, {
		callback: function(component) {
			getMembersListCallback(component, containerId);
		}
	});
}

SimpleUserApplication.togglePictureChanger = function(pictureId, pictureBoxId) {
	var mouseYCoordinate = getAbsoluteTop(pictureId) - 107;
	var mouseXCoordinate = getAbsoluteLeft(pictureId) + 120;
	
	jQuery('#' + pictureBoxId).css({
		top: (mouseYCoordinate == null ? jQuery('#' + pictureId).position().top : mouseYCoordinate) + 'px',
		left: (mouseXCoordinate == null ? (jQuery('#' + pictureId).position().left + jQuery('#' + pictureId).width() + 15) : mouseXCoordinate) + 'px'
	});
	jQuery('#' + pictureBoxId).toggle('fast');
}

SimpleUserApplication.toggleUserPicture = function(pictureId, pictureUri, pictureBoxId) {
	var deletingImage = false;
	if (pictureUri.indexOf('.') == -1) {
		if (FileUploadHelper.uploadedFiles == null || FileUploadHelper.uploadedFiles.length == 0) {
			SimpleUserApplication.userPicture = null;
			return;
		}
		
		var uploadedFileName = FileUploadHelper.uploadedFiles[0];
		var dotIndex = uploadedFileName.indexOf('.');
		if (dotIndex == -1) {
			SimpleUserApplication.userPicture = null;
			return;
		}
		var fileEnd = uploadedFileName.substring(dotIndex + 1);
		fileEnd = fileEnd.toLowerCase();
		if (!(fileEnd == 'jpeg' || fileEnd == 'jpg' || fileEnd == 'png' || fileEnd == 'gif')) {
			SimpleUserApplication.userPicture = null;
			return;
		}
		
		var separator = '/';
		if (IE) {
			separator = '\\';	//	TODO: test
		}
		var fileUriParts = uploadedFileName.split(separator);
		uploadedFileName = fileUriParts[fileUriParts.length -1];
		
		pictureUri += uploadedFileName;
	} else {
		deletingImage = true;
	}
	
	if (pictureUri == null || pictureUri == '' || pictureUri.indexOf('.') == -1) {
		SimpleUserApplication.userPicture = null;
		return;
	}
	
	jQuery('#' + pictureId).load(pictureUri, function() {
		jQuery(this).fadeOut('fast', function() {
			SimpleUserApplication.userPicture = deletingImage ? null : pictureUri;
			jQuery('#' + pictureId).attr('src', pictureUri);
			jQuery('#' + pictureId).fadeIn('fast');
			
			SimpleUserApplication.togglePictureChanger(pictureId, pictureBoxId);
		});
	});
}

SimpleUserApplication.navigateThruUsers = function(event, params, beanParameters, orderBy, index, moveToLeft) {
	if (!isEnterEvent(event)) {
		return false;
	}
	
	navigateInUsersList(params, beanParameters, orderBy, index, moveToLeft, true);
}

function getEditOrCreateDialogWidth(){
	return Math.round(window.getWidth() * 0.55);
}
function getEditOrCreateDialogHeight(){
	return Math.round(window.getHeight() * 0.70);
}

SimpleUserApplication.searchForUser = function(event, params, groupChooserId, containerId, parentGroupChooserId, orderByChooserId, message, parametersForMembersList) {
	var parameters = [];
	parameters.push(params.personalIdInputId);
	getUserByPersonalId(event, parameters, true, function(info) {
		if (info == null || info.personalId == null || info.personalId == '') {
			closeAllLoadingMessages();
			return;
		}
		
		var parentGroupId = getParentGroupIdInSUA(parentGroupChooserId, parameters[16]);
		var groupId = getSelectObjectValue(groupChooserId);
		var orderBy = getSelectObjectValue(orderByChooserId);
		
		var bean = new SimpleUserPropertiesBeanWithParameters(parentGroupId, groupId, orderBy, parametersForMembersList);
		bean.personalId = info.personalId;
		
		UserApplicationEngine.getMembersList(bean, containerId, {
			callback: function(component) {
				getMembersListCallback(component, containerId);
			}
		});
	});
}