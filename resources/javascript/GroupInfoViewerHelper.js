function reloadGroupProperties(instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
	GroupService.reloadProperties(instanceId, {
		callback: function(result) {
			reloadGroupPropertiesCallback(result, instanceId, containerId, message);
		}
	})
}

function reloadGroupPropertiesCallback(result, instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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
		
		prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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
	
	var container = new Element('div');
	container.addClass('groupsInfoList');
	
	for (var i = 0; i < groupsInfo.length; i++) {
		var group = new Element('div');
		group.addClass('groupInfoEntryContainerStyleClass');
		
		//	Name
		if (properties.showName) {
			getGroupInfoEntryPO(properties.localizedText[0], groupsInfo[i].name, properties.showEmptyFields, properties.showLabels, 'groupInfoNameContainerStyleClass').injectInside(group);
		}
		//	Short name
		if (properties.showShortName) {
			getGroupInfoEntryPO(properties.localizedText[1], groupsInfo[i].showShortName, properties.showEmptyFields, properties.showLabels, 'groupInfoShortNameContainerStyleClass').injectInside(group);
		}
		//	Address
		if (properties.showAddress) {
			getAddressContainer(groupsInfo[i].address, 'groupAddressContainer', properties.showEmptyFields, properties.showLabels, properties.localizedText[2]).injectInside(group);
		}
		//	Phone
		if (properties.showPhone) {
			getGroupInfoEntryPO(properties.localizedText[3], groupsInfo[i].phoneNumber, properties.showEmptyFields, true, 'groupInfoPhoneContainerStyleClass').injectInside(group);
		}
		//	Fax
		if (properties.showPhone) {
			getGroupInfoEntryPO(properties.localizedText[4], groupsInfo[i].faxNumber, properties.showEmptyFields, true, 'groupInfoFaxContainerStyleClass').injectInside(group);
		}
		//	HomePage
		if (properties.showHomePage) {
			var homePage = getEmptyValueIfNull(groupsInfo[i].homePageUrl);
			if (properties.showEmptyFields || homePage.length > 0) {
				var homePageContainer = new Element('div');
				homePageContainer.addClass('groupInfoHomepageContainerStyleClass');
				if (properties.showLabels) {
					homePageContainer.appendText(properties.localizedText[5]);
				}
				var link = new Element('a');
				link.appendText(homePage);
				link.setProperty('href', homePage);
				link.setProperty('target', 'newWindow');
				link.injectInside(homePageContainer);
				homePageContainer.injectInside(group);
			}
		}
		//	Emails
		if (properties.showEmails) {
			var text = null;
			if (properties.showEmptyFields) {
				text = properties.localizedText[6];
			}
			var emailsContainer = getEmailsContainer(text, groupsInfo[i].emailsAddresses, 'groupInfoEmailsContainerStyleClass');
			emailsContainer.injectInside(group);
		}
		//	Description
		if (properties.showDescription) {
			getGroupInfoEntryPO(properties.localizedText[7], groupsInfo[i].description, properties.showEmptyFields, properties.showLabels, 'groupInfoDescriptionContainerStyleClass').injectInside(group);
		}
		//	Extra info
		if (properties.showExtraInfo) {
			getGroupInfoEntryPO(properties.localizedText[8], groupsInfo[i].extraInfo, properties.showEmptyFields, properties.showLabels, 'groupInfoExtrainfoContainerStyleClass').injectInside(group);
		}
		
		getDivsSpacer().injectInside(group);
		group.injectInside(container);
	}
	
	getDivsSpacer().injectInside(container);
	container.injectInside(main);
	closeAllLoadingMessages();
}