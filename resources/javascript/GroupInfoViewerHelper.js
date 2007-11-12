function reloadGroupProperties(instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	var strings = new Array();
	strings.push(instanceId);
	strings.push(containerId);
	strings.push(message);
	
	//	Getting properties bean
	GroupService.getBasicGroupPropertiesBean(instanceId, {
		callback: function(bean) {
			getBasicGroupPropertiesBeanCallback(bean, strings);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function getBasicGroupPropertiesBeanCallback(bean, strings) {
	var dwrCallType = dwr.engine.XMLHttpRequest;
	if (bean.remoteMode) {
		prepareDwr(GroupService, bean.server + getDefaultDwrPath());
		dwrCallType = dwr.engine.ScriptTag;
	}
	else {
		prepareDwr(GroupService, getDefaultDwrPath());
	}
	
	//	Clearing cache
	GroupService.clearGroupInfoCache(bean, {
		callback: function(result) {
			clearGroupInfoCacheCallback(strings);
		},
		rpcType:dwrCallType
	});
}

function clearGroupInfoCacheCallback(strings) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	//	Reloading properties
	GroupService.reloadProperties(strings[0], {
		callback: function(result) {
			reloadGroupPropertiesCallback(result, strings[0], strings[1], strings[2]);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function reloadGroupPropertiesCallback(result, instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());
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
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function getGroupPropertiesCallback(properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());
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
		
		prepareDwr(GroupService, getDefaultDwrPath());
		GroupService.canUseRemoteServer(properties.server, {
			callback: function(result) {
				getGroupsData(result, properties, containerId);
			},
			rpcType:dwr.engine.XMLHttpRequest
		});
	}
	else {
		//	Local mode
		getGroupsData(true, properties, containerId);
	}
}

function getGroupsData(result, properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (!result) {
		closeAllLoadingMessages();
		return false;
	}
	
	var dwrCallType = dwr.engine.XMLHttpRequest;
	var dwrPath = getDefaultDwrPath();
	if (properties.remoteMode) {
		//	Preparing DWR for remote call
		dwrPath = properties.server + getDefaultDwrPath();	
		dwrCallType = dwr.engine.ScriptTag;
	}
	prepareDwr(GroupService, dwrPath);
	
	GroupService.addGroupIds(properties.instanceId, properties.uniqueIds, {
		callback: function(result) {
			//alert('added ids: ' + result);
			if (!result) {
				closeAllLoadingMessages();
				return false;
			}
			
			prepareDwr(GroupService, dwrPath);
			//	Calling method to get info about groups on selected ('local' or 'remote') server
			GroupService.getGroupsInfo(new GroupPropertiesBean(properties), {
				callback: function(groupsInfo) {
				 	getGroupsInfoCallback(groupsInfo, properties, containerId);
				},
				rpcType:dwrCallType
			});
		}
	});
}

function GroupPropertiesBean(properties) {
	this.server = properties.server;
	this.login = properties.login;
	this.password = properties.password;
	this.instanceId = properties.instanceId;
	
	this.remoteMode = properties.remoteMode;
	this.showLabels = properties.showLabels;
	this.showAddress = properties.showAddress;
	this.showDescription = properties.showDescription;
	this.showExtraInfo = properties.showExtraInfo;
	this.showEmails = properties.showEmails;
	
	this.cacheTime = properties.cacheTime;
	
	this.showName = properties.showName;
	this.showHomePage = properties.showHomePage;
	this.showShortName = properties.showShortName;
	this.showPhone = properties.showPhone;
	this.showFax = properties.showFax;
	this.showEmptyFields = properties.showEmptyFields;
}

function getGroupsInfoCallback(groupsInfo, properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (groupsInfo == null || containerId == null) {
		closeAllLoadingMessages();
		return false;
	}
	
	GroupService.getLocalizationForGroupInfo({
		callback: function(localizedText) {
			renderGroupsInfoViewerWithAllData(groupsInfo, properties, containerId, localizedText);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function renderGroupsInfoViewerWithAllData(groupsInfo, properties, containerId, localizedText) {
	var main = $(containerId);
	if (main == null) {
		closeAllLoadingMessages();
		return false;
	}
	main.empty();
	
	var container = new Element('div');
	container.addClass('groupsInfoList');
	
	for (var i = 0; i < groupsInfo.length; i++) {
		var group = new Element('div');
		group.addClass('groupInfoEntryContainerStyleClass');
		
		//	Name
		if (properties.showName) {
			getGroupInfoEntryPO(localizedText[0], groupsInfo[i].name, properties.showEmptyFields, properties.showLabels, 'groupInfoNameContainerStyleClass').injectInside(group);
		}
		//	Short name
		if (properties.showShortName) {
			getGroupInfoEntryPO(localizedText[1], groupsInfo[i].showShortName, properties.showEmptyFields, properties.showLabels, 'groupInfoShortNameContainerStyleClass').injectInside(group);
		}
		//	Address
		if (properties.showAddress) {
			getAddressContainer(groupsInfo[i].address, 'groupAddressContainer', properties.showEmptyFields, properties.showLabels, localizedText[2]).injectInside(group);
		}
		//	Phone
		if (properties.showPhone) {
			getGroupInfoEntryPO(localizedText[3], groupsInfo[i].phoneNumber, properties.showEmptyFields, true, 'groupInfoPhoneContainerStyleClass').injectInside(group);
		}
		//	Fax
		if (properties.showPhone) {
			getGroupInfoEntryPO(localizedText[4], groupsInfo[i].faxNumber, properties.showEmptyFields, true, 'groupInfoFaxContainerStyleClass').injectInside(group);
		}
		//	HomePage
		if (properties.showHomePage) {
			var homePage = getEmptyValueIfNull(groupsInfo[i].homePageUrl);
			if (properties.showEmptyFields || homePage.length > 0) {
				var homePageContainer = new Element('div');
				homePageContainer.addClass('groupInfoHomepageContainerStyleClass');
				if (properties.showLabels) {
					homePageContainer.appendText(localizedText[5]);
				}
				var link = new Element('a');
				link.appendText(homePage);
				link.setProperty('href', 'http://' + homePage);
				link.setProperty('target', 'newWindow');
				link.injectInside(homePageContainer);
				homePageContainer.injectInside(group);
			}
		}
		//	Emails
		if (properties.showEmails) {
			var text = null;
			if (properties.showLabels) {
				text = localizedText[6];
			}
			var emailsContainer = getEmailsContainer(text, groupsInfo[i].emailsAddresses, 'groupInfoEmailsContainerStyleClass');
			emailsContainer.injectInside(group);
		}
		//	Description
		if (properties.showDescription) {
			getGroupInfoEntryPO(localizedText[7], groupsInfo[i].description, properties.showEmptyFields, properties.showLabels, 'groupInfoDescriptionContainerStyleClass').injectInside(group);
		}
		//	Extra info
		if (properties.showExtraInfo) {
			getGroupInfoEntryPO(localizedText[8], groupsInfo[i].extraInfo, properties.showEmptyFields, properties.showLabels, 'groupInfoExtrainfoContainerStyleClass').injectInside(group);
		}
		
		getDivsSpacer().injectInside(group);
		group.injectInside(container);
	}
	
	getDivsSpacer().injectInside(container);
	container.injectInside(main);
	closeAllLoadingMessages();
}