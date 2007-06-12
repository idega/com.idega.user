function reloadGroupProperties(instanceId, containerId, message) {
	GroupService.reloadProperties(instanceId, {
		callback: function(result) {
			reloadGroupPropertiesCallback(result, instanceId, containerId, message);
		}
	})
}

function reloadGroupPropertiesCallback(result, instanceId, containerId, message) {
	if (!result) {
		return false;
	}
	getSelectedGroups(instanceId, containerId, message);
}

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
		closeAllLoadingMessages();
		return false;
	}
	
	if (properties.remoteMode) {
		//	Remote mode
		if (properties.server == null || properties.login == null || properties.password == null) {
			closeAllLoadingMessages();
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
		closeAllLoadingMessages();
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
		closeAllLoadingMessages();
		return false;
	}
	var main = document.getElementById(containerId);
	if (main == null) {
		closeAllLoadingMessages();
		return false;
	}
	removeChildren(main);
	
	var container = document.createElement('div');
	container.className = 'groupsInfoList';
	
	var groups = document.createElement('ul');
	container.appendChild(groups);
	for (var i = 0; i < groupsInfo.length; i++) {
		var group = document.createElement('li');
		groups.appendChild(group);
		
		//	Name
		if (properties.showName) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[0], groupsInfo[i].name, properties.showEmptyFields, properties.showLabels));
		}
		//	Short name
		if (properties.showShortName) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[1], groupsInfo[i].showShortName, properties.showEmptyFields, properties.showLabels));
		}
		//	Address
		if (properties.showAddress) {
			var addressContainer = document.createElement('div');
			var address = groupsInfo[i].address;
			if (address == null) {
				if (properties.showEmptyFields) {
					addressContainer.appendChild(document.createTextNode(properties.localizedText[2]));
				}
			}
			else {
				var allAddress = address.streetAddress + ', ' + address.postalCode + ' ' + address.city;
				addressContainer.appendChild(document.createTextNode(properties.localizedText[2]));
				addressContainer.appendChild(document.createTextNode(allAddress));
			}
			group.appendChild(addressContainer);
		}
		//	Phone
		if (properties.showPhone) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[3], groupsInfo[i].phoneNumber, properties.showEmptyFields, properties.showLabels));
		}
		//	Fax
		if (properties.showPhone) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[4], groupsInfo[i].faxNumber, properties.showEmptyFields, properties.showLabels));
		}
		//	HomePage
		if (properties.showHomePage) {
			var homePage = getEmptyValueIfNull(groupsInfo[i].homePageUrl);
			if (properties.showEmptyFields || homePage.length > 0) {
				var homePageContainer = document.createElement('div');
				homePageContainer.appendChild(document.createTextNode(properties.localizedText[5]));
				var link = document.createElement('a');
				link.appendChild(document.createTextNode(homePage));
				link.setAttribute('href', homePage);
				link.setAttribute('target', 'newWindow');
				group.appendChild(homePageContainer);
			}
		}
		//	Emails
		if (properties.showEmails) {
			var emailsContainer = getEmailsContainer(groupsInfo[i].emailAddresses);
			if (properties.showEmptyFields || emailsContainer != null) {
				var mainEmailsContainer = document.createElement('div');
				mainEmailsContainer.appendChild(document.createTextNode(properties.localizedText[6]));
				mainEmailsContainer.appendChild(emailsContainer);
				group.appendChild(mainEmailsContainer);
			}
		}
		//	Description
		if (properties.showDescription) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[7], groupsInfo[i].description, properties.showEmptyFields, properties.showLabels));
		}
		//	Extra info
		if (properties.showExtraInfo) {
			group.appendChild(getGroupInfoEntryPO(properties.localizedText[8], groupsInfo[i].extraInfo, properties.showEmptyFields, properties.showLabels));
		}
	}
	
	main.appendChild(container);
	closeAllLoadingMessages();
}