dwr.engine.XMLHttpRequest = 1;
dwr.engine.ScriptTag = 3;

var SERVER_START = 'http://';

var UNIQUE_IDS_ID = 'uniqueids';
var GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS = 'groupsTreeListElement';
var GROUPS_CHOOSER_TREE_SELECTED_GROUPS_CACHE_NAME = 'groupsChooserTreeSelectedGroupsUniqueIdsCache';
var NO_GROUPS_MESSAGE = 'Sorry, no groups found on selected server.';

var SERVER = null;
var LOGIN = null;
var PASSWORD = null;
var NODE_ON_CLICK_ACTION = null;

if (!groups_chooser_helper) var groups_chooser_helper = new ChooserHelper();

function getGroupTreeListElementStyleClass(){
	return GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS;
}

function registerGroupInfoChooserActions(nodeOnClickAction, noGroupsMessage, selectedGroups, styleClass) {
	if (noGroupsMessage != null) {
		NO_GROUPS_MESSAGE = noGroupsMessage;
	}
	if (nodeOnClickAction != null){
		NODE_ON_CLICK_ACTION = nodeOnClickAction;
	}
	
	$$('input.groupInfoChooserRadioStyle').each(
		function(element) {
			element.removeEvents('click');
			
			element.addEvent('click', function() {
				if (element.value) {
					var inputs = $$('input.groupInfoChooserRadioStyle');
					for (var i = 0; i < inputs.length; i++){
						if (element != inputs[i]) {
							inputs[i].checked = false;
						}
					}
					element.checked = true;
					
					var values = element.value.split('@');
					if (values.length = 2) {
						manageConnectionType('local' == values[0], values[1], noGroupsMessage, selectedGroups, styleClass, element.getProperty('specialmark'));
						groups_chooser_helper.removeAllAdvancedProperties();	//	Because changing connection type
						groups_chooser_helper.addAdvancedProperty('connection', values[0]);
					}
				}
			});
    	}
    );
    
	registerActionsForGroupTreeSpan();
    
    if (styleClass != null && styleClass != GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS) {	//	We don't want to override default actions
	    $$('span.' + styleClass).each(	//	These are custom actions
			function(element) {
				//element.addEvent('click', customFunction);	<- example
	    	}
	    );
    }
}

function registerActionsForGroupTreeSpan() {
	$$('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(	//	These actions needed for Builder, define your own if need
		function(element) {
			element.removeEvents('click');
			
			element.addEvent('click', function() {
				selectGroup(element);
				checkOtherProperties(element);
			});
			if (NODE_ON_CLICK_ACTION != null){
				element.addEvent('click', NODE_ON_CLICK_ACTION);
			}
    	}
    );
}

function checkOtherGroupsChooserProperties() {
	//	Inputs' values (connection parameters)
	$$('input.groupConnectionChooserInputStyle').each(
		function(element) {
			groups_chooser_helper.addAdvancedProperty(element.name, element.value);
		}
	);
	
	//	Connection type
	var radio = null;
	$$('input.groupInfoChooserRadioStyle').each(
		function(element) {
			if (element.checked) {
				radio = element;
			}
    	}
    );
    if (radio != null) {
    	if (radio.value) {
			var values = radio.value.split('@');
			if (values.length = 2) {
				groups_chooser_helper.addAdvancedProperty('connection', values[0]);
			}
		}
    }
}

function checkOtherProperties(clickedElement) {
	checkOtherGroupsChooserProperties();
	
    //	Seaching for selected nodes
    var otherGroupsNodes = new Array();
    $$('span.' + GROUPS_TREE_LIST_ELEMENT_STYLE_CLASS).each(
		function(element) {
			if (element != clickedElement) {
				otherGroupsNodes.push(element);
			}
    	}
    );
    
    for (var i = 0; i < otherGroupsNodes.length; i++) {
    	if ('bold' == otherGroupsNodes[i].getStyle('font-weight')) {
    		var advancedProperty = groups_chooser_helper.getAdvancedProperty(UNIQUE_IDS_ID);
			if (advancedProperty == null) {
				groups_chooser_helper.addAdvancedProperty(UNIQUE_IDS_ID, otherGroupsNodes[i].id);
			}
			else {
				var allIds = advancedProperty.value.split(',');
				if (!existsElementInArray(allIds, otherGroupsNodes[i].id)) {				//	This node must be selected
					var newValues = advancedProperty.value + ',' + otherGroupsNodes[i].id;	//	Adding new id
					groups_chooser_helper.addAdvancedProperty(UNIQUE_IDS_ID, newValues);
				}
    		}
    	}
    }
}

function selectGroup(element) {
	if (element == null) {
		return;
	}
	if (element.id == null) {
		return;
	}
	
	var addId = false;
	var fontVariant = element.getStyle('font-weight');
	if (fontVariant == null) {
		addId = true;
	}
	else {
		if (fontVariant == '' || fontVariant == 'normal') {
			addId = true;
		}
	}
	
	if (addId) {
		fontVariant = 'bold';
	}
	else {
		fontVariant = 'normal';
	}
	element.setStyle('font-weight', fontVariant);
	
	var advancedProperty = groups_chooser_helper.getAdvancedProperty(UNIQUE_IDS_ID);
	if (advancedProperty == null) {
		if (addId) {
			groups_chooser_helper.addAdvancedProperty(UNIQUE_IDS_ID, element.id);
		}
	}
	else {
		var newValues = '';
		if (addId) {
			 newValues = advancedProperty.value + ',' + element.id;	//	Adding new id
		}
		else {
			var allIds = advancedProperty.value.split(',');
			removeElementFromArray(allIds, element.id);	//	Removing id
			for (var i = 0; i < allIds.length; i++) {	//	Building new value
				newValues = allIds[i];
				if (i + 1 < allIds.length) {
					newValues += ',';
				}
			}
		}
		groups_chooser_helper.addAdvancedProperty(UNIQUE_IDS_ID, newValues);
	}
}

function manageConnectionType(useLocal, id, noGroupsMessage, selectedGroups, styleClass, specialMark) {
	var connection = $('connectionData');
	if (connection == null) {
		return false;
	}
	var displayValue = 'block';
	if (useLocal) {
		displayValue = 'none';
		loadLocalTree(id, noGroupsMessage, selectedGroups, styleClass);
		if (specialMark != null) {
			if (specialMark == 'calendar') {
				getSimpleCalendarTypes(null, null, null, false, CALENDAR_EVENTS_CONTAINER_ID, null);
				getSimpleCalendarLedgers(null, null, null, false, CALENDAR_LEDGERS_CONTAINER_ID, null);
			}
		}
	}
	connection.setStyle('display', displayValue);
}

function getGroupsTree(serverId, loginId, passwordId, id, messages, selectedGroups, styleClass, specialMark) {
	var serverInput = $(serverId);
	var loginInput = $(loginId);
	var passwordInput = $(passwordId);
	if (serverInput == null || loginInput == null || passwordInput == null) {
		humanMsg.displayMsg(messages[0]);
		return false;
	}
	
	var server = serverInput.value;
	if (server == '') {
		humanMsg.displayMsg(messages[1]);
		return false;
	}
	
	var login = loginInput.value;
	if (login == '') {
		humanMsg.displayMsg(messages[2]);
		return false;
	}
	
	var password = passwordInput.value;
	if (password == '') {
		humanMsg.displayMsg(messages[3]);
		return false;
	}
	
	if (server.indexOf(SERVER_START) != 0) {
		server = SERVER_START + server;
	}
	
	groups_chooser_helper.addAdvancedProperty(serverInput.name, server);
	groups_chooser_helper.addAdvancedProperty(loginInput.name, login);
	groups_chooser_helper.addAdvancedProperty(passwordInput.name, password);
	
	getGroupsWithValues(messages[4], server, login, password, id, messages[5], messages[6], messages[7], false, selectedGroups, styleClass);
	
	if (specialMark != null) {
		if (specialMark == 'calendar') {
			getSimpleCalendarTypes(server, login, password, true, CALENDAR_EVENTS_CONTAINER_ID, null);
			getSimpleCalendarLedgers(server, login, password, true, CALENDAR_LEDGERS_CONTAINER_ID, null);
		}
	}
}

function getGroupsWithValues(loadingMsg, server, login, password, id, canNotConnectMsg, failedLoginMsg, noGroupsMsg, needsDecode, selectedGroups, styleClass) {
	showLoadingMessage(loadingMsg);
	if (needsDecode) {
		password = decode64(password);
	}
	prepareDwr(GroupService, getDefaultDwrPath());
	GroupService.canUseRemoteServer(server, {
		callback: function(result) {
			canUseRemoteCallback(result, server, login, password, id, canNotConnectMsg, failedLoginMsg, noGroupsMsg, selectedGroups, styleClass);
		},
		errorHandler: function(message, exeption) {
			humanMsg.displayMsg(canNotConnectMsg);
			closeAllLoadingMessages();
			return false;
		},
		timeout: 10000,
		rpcType: dwr.engine.XMLHttpRequest,
		transport: dwr.engine.transport.xhr
	});
}

function canUseRemoteCallback(result, server, login, password, id, severErrorMessage, logInErrorMessage, noGroupsMessage, selectedGroups, styleClass) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	if (result) {
		if (IE && selectedGroups != null) {
			if (selectedGroups.length > 20) {
				if (streamUniqueIdsToServer(id, selectedGroups, server, true, GROUPS_CHOOSER_TREE_SELECTED_GROUPS_CACHE_NAME)) {
					addGroupsTreeAfterIdsAreStreamed(result, server, login, password, id, severErrorMessage, logInErrorMessage, noGroupsMessage, selectedGroups,
						styleClass, true);
				}
				
				return false;
			}
		}
		
		addGroupsTreeAfterIdsAreStreamed(result, server, login, password, id, severErrorMessage, logInErrorMessage, noGroupsMessage, selectedGroups, styleClass,
			false);
	}
	else {
		//	Cannot use remote server
		closeAllLoadingMessages();
		humanMsg.displayMsg(severErrorMessage + ' ' + server);
		return false;
	}
}

function addGroupsTreeAfterIdsAreStreamed(result, server, login, password, id, severErrorMessage, logInErrorMessage, noGroupsMessage, selectedGroups, styleClass,
	fake) {
	if (!result) {
		closeAllLoadingMessages();
		return false;
	}
	
	var groupsToSend = selectedGroups;
	if (fake) {
		groupsToSend = null;
	}
	
	prepareDwr(GroupService, server + getDefaultDwrPath());
	GroupService.getGroupsTree(login, password, id, groupsToSend, {
		callback: function(groups) {
			closeAllLoadingMessages();

			prepareDwr(GroupService, getDefaultDwrPath());
			if (groups == null) {
				//	Login failed
				humanMsg.displayMsg(logInErrorMessage + ' ' + server);
				return false;
			}
			SERVER = server;
			LOGIN = login;
			PASSWORD = password;
			addGroupsTree(groups, id, noGroupsMessage, selectedGroups, styleClass);
		},
		errorHandler: function(message, exeption) {
			closeAllLoadingMessages();
			humanMsg.displayMsg(severErrorMessage + ' ' + server);
			return false;
		},
		rpcType: dwr.engine.ScriptTag,
		transport: dwr.engine.transport.scriptTag
	});
}

/**
 * This function is a bridge between DWR 2.* and DWR 3.*
 */
dwr.engine._remoteHandleCallback = function(batchId, callId, reply) {
	dwr.engine.remote.handleCallback(batchId, callId, reply);
}

function loadLocalTree(id, noGroupsMessage, selectedGroups, styleClass) {
	SERVER = null;
	LOGIN = null;
	PASSWORD = null;
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getTopGroupsAndDirectChildren(selectedGroups, {
		callback: function(groups) {
			if (groups == null) {
				closeAllLoadingMessages();
				return false;
			}
			addGroupsTree(groups, id, noGroupsMessage, selectedGroups, styleClass);
		},
		rpcType: dwr.engine.XMLHttpRequest,
		transport: dwr.engine.transport.xhr
	});
}

function addGroupsTree(groups, id, noGroupsMessage, selectedGroups, styleClass) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (groups.length == 0) {
		var container = $(id);
		if (container != null) {
			container.empty();
			var textContainer = new Element('div');
			textContainer.appendText(noGroupsMessage);
			textContainer.injectInside(container);
		}
	}
	else {
		setGroupsNodes(groups, id, styleClass, selectedGroups);
		registerGroupInfoChooserActions(null, noGroupsMessage, selectedGroups, styleClass);
	}
}

function getGroupInfoEntryPO(text, value, showEmptyFields, showLabel, styleClass) {
	var container = new Element('div');
	if (styleClass != null) {
		container.addClass(styleClass);
	}
	value = getEmptyValueIfNull(value);
	if (showEmptyFields || value.length > 0) {
		if (showLabel) {
			if (text != null) {
				container.appendText(text);
			}
		}
		container.appendText(value);
	}
	return container;
}

function getEmptyValueIfNull(value) {
	if (value == null) {
		return '';
	} else if (typeof value == 'object') {
		try {
			return value.toLocaleDateString();
		} catch (e) {}
	}
	return value + '';
}

function getEmailsContainer(text, emailAddresses, styleClass) {	
	var emails = new Element('div');
	if (styleClass != null) {
		emails.addClass(styleClass);
	}
	
	if (emailAddresses == null) {
		return emails;
	}
	
	if (text != null) {
		emails.appendText(text);
	}
	
	for (var i = 0; i < emailAddresses.length; i++) {
		var link = new Element('a');
		link.appendText(emailAddresses[i]);
		link.setProperty('href', 'mailto:' + emailAddresses[i]);
		link.injectInside(emails);
		if (i + 1 < emailAddresses.length) {
			emails.appendText(', ');
		}
	}
	return emails;
}

function getAddressContainer(address, styleClass, showEmptyFields, showLabels, localizedText) {	
	var addressContainer = new Element('div');
	if (styleClass != null) {
		addressContainer.addClass(styleClass);
	}
	if (address == null) {
		if (showEmptyFields) {
			addressContainer.appendText(localizedText);
		}
	}
	else {
		var addedAnything = false;
		var addedPostalName = false;
		var allAddress = '';
		if (address.streetAddress != null && address.streetAddress != '') {
			allAddress = address.streetAddress;
			addedAnything = true;
		}
		if (address.postalCode != null && address.postalCode != '') {
			if (addedAnything) {
				allAddress += ', ';
			}
			allAddress += address.postalCode;
			
			if (address.postalName != null && address.postalName != '') {
				allAddress += ' ' + address.postalName;
				addedPostalName = true;
			}
			
			addedAnything = true;
		}
		if (address.city != null && address.city != '') {
			var addCity = true;
			if (addedPostalName) {
				if (address.city == address.postalName) {
					addCity = false;
				}
			}
			
			if (addCity) {
				if (addedAnything) {
					if (addedPostalName) {
						allAddress += ',';
					}
					allAddress += ' ';
				}
				allAddress += address.city;
			}
		}
		if (allAddress != '') {
			if (showLabels) {
				addressContainer.appendText(localizedText);
			}
			addressContainer.appendText(allAddress);
		}
	}
	return addressContainer;
}

function getDivsSpacer() {
	var spacer = new Element('div');
	spacer.addClass('spacer');
	return spacer;
}

function streamUniqueIdsToServer(instanceId, uniqueIds, server, remoteMode, cacheName) {
	if (instanceId == null || uniqueIds == null || cacheName == null) {
		return false;
	}
	var copiedUniqueIds = null;
	
	if (uniqueIds != null) {
		copiedUniqueIds = new Array();
		for (var i = 0; i < uniqueIds.length; i++) {
			copiedUniqueIds.push(uniqueIds[i]);
		}
	}
	
	if (sendPackedUniqueIdsToServer(instanceId, copiedUniqueIds, server, remoteMode, cacheName)) {
		return true;
	}
	
	return false;
}

function sendPackedUniqueIdsToServer(instanceId, uniqueIds, server, remoteMode, cacheName) {
	while (uniqueIds.length > 20) {
		var pack = new Array();
		for (var i = 0; i < 20; i++) {
			pack.push(uniqueIds[i]);
		}
		
		sendPackUniqueIdsToServer(instanceId, pack, server, remoteMode, cacheName);
		
		for (var i = 0; i < pack.length; i++) {
			removeElementFromArray(uniqueIds, pack[i]);
		}
	}
	
	if (uniqueIds) {
		if (uniqueIds.length > 0) {
			sendPackUniqueIdsToServer(instanceId, uniqueIds, server, remoteMode, cacheName);
		}
	}
	
	return true;
}

function sendPackUniqueIdsToServer(instanceId, uniqueIds, server, remoteMode, cacheName) {
	var dwrCallType = getDwrCallType(remoteMode);
	var dwrPath = getDefaultDwrPath();
	if (remoteMode) {
		dwrPath = server + getDefaultDwrPath();	
	}
	prepareDwr(GroupService, dwrPath);
	
	GroupService.streamUniqueIds(instanceId, uniqueIds, cacheName, {
		callback: function(result) {
			return result;
		},
		rpcType: dwrCallType,
		transport: dwrCallType
	});
}

function addElementValueForAdvancedProperty(ids, key, value) {
	if (ids == null) {
		groups_chooser_helper.addAdvancedProperty(key, value);
	}
	else {
		var allIds = ids.value.split(',');
		if (!existsElementInArray(allIds, value)) {
			var newValues = ids.value + ',' + value;		//	Adding new id
			groups_chooser_helper.addAdvancedProperty(key, newValues);
		}
	}
}

function removeElementValueForAdvancedProperty(ids, key, value) {
	if (ids == null) {
		return false;
	}
	
	var newValues = '';
	var allIds = ids.value.split(',');
	removeElementFromArray(allIds, value);				//	Removing id
	if (allIds != null) {
		for (var i = 0; i < allIds.length; i++) {		//	Building new value
			newValues += allIds[i];
			if (i + 1 < allIds.length) {
				newValues += ',';
			}
		}
	}
	groups_chooser_helper.addAdvancedProperty(key, newValues);
}

function getDwrCallType(remote) {
	var dwrCallType = dwr.engine.XMLHttpRequest;
	if (!dwrCallType) {
		dwrCallType = dwr.engine.transport.xhr;
	}
	if (remote) {
		dwrCallType = dwr.engine.ScriptTag;
		if (!dwrCallType) {
			dwrCallType = dwr.engine.transport.scriptTag;
		}
	}
	
	return dwrCallType;
}
