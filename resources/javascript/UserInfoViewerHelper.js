var LOCALIZATIONS = null;

function reloadGroupMemberProperties(instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	var strings = new Array();
	strings.push(instanceId);
	strings.push(containerId);
	strings.push(message);
	
	//	Getting properties bean
	GroupService.getBasicUserPropertiesBean(instanceId, {
		callback: function(bean) {
			getBasicUserPropertiesBeanCallback(bean, strings);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function getBasicUserPropertiesBeanCallback(bean, strings) {
	var dwrCallType = dwr.engine.XMLHttpRequest;
	if (bean.remoteMode) {
		prepareDwr(GroupService, bean.server + getDefaultDwrPath());
		dwrCallType = dwr.engine.ScriptTag;
	}
	else {
		prepareDwr(GroupService, getDefaultDwrPath());
	}
	
	//	Clearing cache
	GroupService.clearUsersInfoCache(bean.login, bean.password, bean.instanceId, bean.cacheTime, bean.remoteMode, {
		callback: function(result) {
			clearUsersInfoCacheCallback(strings);
		},
		rpcType:dwrCallType
	});
}

function clearUsersInfoCacheCallback(strings) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	//	Reloading properties
	GroupService.reloadProperties(strings[0], {
		callback: function(result) {
			reloadGroupMemberPropertiesCallback(result, strings[0], strings[1], strings[2]);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function reloadGroupMemberPropertiesCallback(result, instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (!result) {
		return false;
	}
	
	getSelectedUsers(instanceId, containerId, message);
}

function getSelectedUsers(instanceId, containerId, message) {
	if (instanceId == null) {
		return;
	}

	showLoadingMessage(message);

	prepareDwr(GroupService, getDefaultDwrPath());
	GroupService.getUserStatusLocalization({
		callback: function(list) {
			getUserStatusLocalizationCallback(list, instanceId, containerId);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function getUserStatusLocalizationCallback(list, instanceId, containerId) {
	LOCALIZATIONS = list;

	prepareDwr(GroupService, getDefaultDwrPath());
	GroupService.getUserPropertiesBean(instanceId, {
		callback: function(properties) {
			getUserPropertiesCallback(properties, containerId);
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}

function getUserPropertiesCallback(properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (properties == null) {
		closeAllLoadingMessages();
		return false;
	}

	if (properties.remoteMode) {
		prepareDwr(GroupService, getDefaultDwrPath());
		
		//	Remote mode
		if (properties.server == null || properties.login == null || properties.password == null) {
			closeAllLoadingMessages();
			return false;
		}
	
		GroupService.canUseRemoteServer(properties.server, {
			callback: function(result) {
				getGroupsUsersData(result, properties, containerId);
			},
			rpcType:dwr.engine.XMLHttpRequest
		});
	}
	else {
		//	Local mode
		getGroupsUsersData(true, properties, containerId)
	}
}

function getGroupsUsersData(result, properties, containerId) {
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
	
	if (IE && properties.uniqueIds != null) { 
		if (properties.uniqueIds.length > 20) {
			if (streamUniqueIdsToServer(properties.instanceId, properties.uniqueIds, properties.server, properties.remoteMode, false, false)) {
				getUsersInfoAfterIdsAreAdded(true, properties, containerId);
			}
			
			return false;
		}
	}
		
	GroupService.addUsersIds(properties.instanceId, properties.uniqueIds, {
		callback:function(result) {
			getUsersInfoAfterIdsAreAdded(result, properties, containerId)
		},
		rpcType:dwrCallType
	});
}

function getUsersInfoAfterIdsAreAdded(result, properties, containerId) {
	if (!result) {
		closeAllLoadingMessages();
		return false;
	}
	
	var dwrCallType = dwr.engine.XMLHttpRequest;
	var dwrPath = getDefaultDwrPath();
	if (properties.remoteMode) {
		dwrPath = properties.server + getDefaultDwrPath();	
		dwrCallType = dwr.engine.ScriptTag;
	}
	prepareDwr(GroupService, dwrPath);
	
	GroupService.getUsersInfo(properties.login, properties.password, properties.instanceId, properties.cacheTime, properties.remoteMode, {
		callback: function(usersInfo) {
			getUsersInfoCallback(usersInfo, properties, containerId);
		},
		rpcType:dwrCallType
	});
}

function getUsersInfoCallback(members, properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());
	if (members == null || containerId == null) {
		closeAllLoadingMessages();
		return false;
	}

	GroupService.getLocalizationForGroupUsersInfo({
		callback: function(localizedText) {
			renderGroupUserInfoViewerWithAllData(members, properties, containerId, localizedText)
		},
		rpcType:dwr.engine.XMLHttpRequest
	});
}
	
function renderGroupUserInfoViewerWithAllData(members, properties, containerId, localizedText) {
	var main = $(containerId);
	if (main == null) {
		closeAllLoadingMessages();
		return false;
	}
	main.empty();
	
	var container = new Element('div');
	container.addClass('groupsMembersInfoList');
				
	var maxElements = members.length;	//	TODO: make paging
	if (members.length > 40) {
		maxElements = 40;
	}
	for (var j = 0; j < maxElements; j++) {
		var user = new Element('div');
		
		//	Image
		var imageContainer = new Element('div');
		imageContainer.addClass('groupMemberPhotoContainerStyleClass');
		imageContainer.injectInside(user);
		if (properties.showImage) {
			var imageSrc = members[j].imageUrl;
			if (imageSrc == null) {
				imageSrc = properties.defaultPhoto;	//	Default 'photo'
			}
			else {
				if (properties.remoteMode) {
					imageSrc = properties.server + imageSrc;	//	Setting full path
				}
			}
			if (imageSrc != null) {
				var image = new Element('img');
				image.setAttribute('src', imageSrc);
				if (properties.imageWidth != null) {
					image.setAttribute('width', properties.imageWidth);
				}
				if (properties.imageHeight != null) {
					image.setAttribute('height', properties.imageHeight);
				}
				image.injectInside(imageContainer);
				
				//	Add reflecion?
				if (properties.addReflection) {
					image.addReflection({height: '0.16', opacity: '0.55'});
				}
			}
		}
		
		//	Info
		var infoContainer = new Element('div');
		infoContainer.addClass('groupMemberInfoContainerStyleClass');
				
		//	Status
		if (properties.showStatus) {
			getGroupInfoEntryPO(localizedText[11], getLocalizationForUserStatus(members[j].status), true, properties.showLabels, 'groupMemberStatusContainerStyleClass').injectInside(infoContainer);
		
			if (members[j].status != null && members[j].status != '') {
				//	Empty line
				var emptyLine = new Element('div');
				emptyLine.addClass('emptyLineContainerStyleClass');
				new Element('br').injectInside(emptyLine);
				emptyLine.injectInside(infoContainer);
			}
		}
		
		//	Name and age
		getUserNameAndAgeContainer(members[j].name, members[j].age, properties, localizedText).injectInside(infoContainer);
		
		// Address
		if (properties.showAddress) {
			getAddressContainer(members[j].address, 'groupMemberAddressContainerStyleClass', false, properties.showLabels, localizedText[12]).injectInside(infoContainer);
		}
				
		//	Phones and mails
		getPhonesAndEmailsContainer(members[j].homePhone, members[j].workPhone, members[j].mobilePhone, members[j].emailsAddresses, 'groupMemberPhonesAndMailsContainerStyleClass', properties, localizedText).injectInside(infoContainer);
		getDivsSpacer().injectInside(infoContainer);
				
		// User info 1
		if (properties.showUserInfoOne) {
			getGroupInfoEntryPO(null,  members[j].infoOne, false, properties.showLabels, 'groupMemberInfoOneContainerStyleClass').injectInside(infoContainer);
		}
				
		// User info 2
		if (properties.showUserInfoTwo) {
			getGroupInfoEntryPO(null,  members[j].infoTwo, false, properties.showLabels, 'groupMemberInfoTwoContainerStyleClass').injectInside(infoContainer);
		}
		
		// User info 3
		if (properties.showUserInfoThree) {
			getGroupInfoEntryPO(null,  members[j].infoThree, false, properties.showLabels, 'groupMemberInfoThreeContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Company address
		if (properties.showCompanyAddress) {
			getAddressContainer(members[j].companyAddress, 'groupMemberCompanyAddressContainerStyleClass', false, properties.showLabels, localizedText[18]).injectInside(infoContainer);
		}
				
		//	Group name
		if (properties.showGroupName) {
			getGroupInfoEntryPO(localizedText[22], members[j].groupName, false, properties.showLabels, 'groupMemberGroupContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Date of birth
		if (properties.showDateOfBirth) {
			getGroupInfoEntryPO(localizedText[19], members[j].dateOfBirth, false, properties.showLabels, 'groupMemberDateOfBirthContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Job
		if (properties.showJob) {
			getGroupInfoEntryPO(localizedText[20], members[j].job, false, properties.showLabels, 'groupMemberJobContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Workplace
		if (properties.showWorkplace) {
			getGroupInfoEntryPO(localizedText[21], members[j].workPlace, false, properties.showLabels, 'groupMemberWorkplaceContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Extra info
		if (properties.showExtraInfo) {
			getGroupInfoEntryPO(localizedText[16], members[j].extraInfo, false, properties.showLabels, 'groupMemberExtraInfoContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Description
		if (properties.showDescription) {
			getGroupInfoEntryPO(localizedText[17], members[j].description, false, properties.showLabels, 'groupMemberDescriptionContainerStyleClass').injectInside(infoContainer);
		}
				
		/*//	Title
		if (properties.showTitle) {
			getGroupInfoEntryPO(localizedText[1], members[j].title, false, properties.showLabels, 'groupMemberTitleContainerStyleClass').injectInside(infoContainer);
		}
		//	Education
		if (properties.showEducation) {
			getGroupInfoEntryPO(localizedText[7], members[j].education, false, properties.showLabels, 'groupMemberEducationContainerStyleClass').injectInside(infoContainer);
		}
		//	School
		if (properties.showSchool) {
			getGroupInfoEntryPO(localizedText[8], members[j].school, false, properties.showLabels, 'groupMemberSchoolContainerStyleClass').injectInside(infoContainer);
		}
		//	Area
		if (properties.showArea) {
			getGroupInfoEntryPO(localizedText[9], members[j].area, false, properties.showLabels, 'groupMemberAreaContainerStyleClass').injectInside(infoContainer);
		}
		//	Began work
		if (properties.showBeganWork) {
			getGroupInfoEntryPO(localizedText[10], members[j].beganWork, false, properties.showLabels, 'groupMemberBeganworkContainerStyleClass').injectInside(infoContainer);
		}*/
		
		infoContainer.injectInside(user);
		user.addClass('groupMemberStyleClass');
		getDivsSpacer().injectInside(user);
		user.injectInside(container);		
	}
	
	container.injectInside(main);
	getDivsSpacer().injectInside(main);
	closeAllLoadingMessages();	
}

function getUserNameAndAgeContainer(name, age, properties, localizedText) {
	var container = new Element('div');
	container.addClass('groupMemberNameAndAgeContainerStyleClass');
	
	//	Name
	if (properties.showLabels) {
		container.appendText(localizedText[0]);
	}
	container.appendText(name);
	
	//	Age
	if (age != null && age != '') {
		if (properties.showAge) {
			if (properties.showLabels) {
				container.appendText(' ' + localizedText[2] + age);
			}
			else {
				container.appendText(' (' + age + ')');
			}
		}
	}
	
	return container;
}

function getPhonesAndEmailsContainer(homePhone, workPhone, mobilePhone, emails, styleClass, properties, localizedText) {
	var container = new Element('div');
	if (styleClass != null) {
		container.addClass(styleClass);
	}
	
	var addedAnything = false;
	
	//	Home phone
	if (properties.showHomePhone) {
		if (homePhone != null && homePhone != '') {
			var homePhoneContainer = new Element('div');
			homePhoneContainer.setStyle('float', 'left');
			homePhoneContainer.appendText(localizedText[13] + '. ' + homePhone);
			homePhoneContainer.injectInside(container);
			addedAnything = true;
		}
	}
	
	//	Work phone
	if (properties.showWorkPhone) {
		if (workPhone != null && workPhone != '') {
			var workPhoneContainer = new Element('div');
			workPhoneContainer.setStyle('float', 'left');
			if (addedAnything) {
				workPhoneContainer.appendText('\u00a0/\u00a0');
			}
			workPhoneContainer.appendText(localizedText[14] + '. ' + workPhone);
			workPhoneContainer.injectInside(container);
			addedAnything = true;
		}
	}
	
	//	Mobile phone
	if (properties.showMobilePhone) {
		if (mobilePhone != null && mobilePhone != '') {
			var mobilePhoneContainer = new Element('div');
			mobilePhoneContainer.setStyle('float', 'left');
			if (addedAnything) {
				mobilePhoneContainer.appendText('\u00a0/\u00a0');
			}
			mobilePhoneContainer.appendText(localizedText[15] + '. ' +  mobilePhone);
			mobilePhoneContainer.injectInside(container);
			addedAnything = true;
		}
	}
	
	//	Emails
	if (properties.showEmails) {
		if (emails != null) {
			if (emails.length > 0) {
				var text = null;
				if (addedAnything) {
					text = '\u00a0/\u00a0';
				}
				var emailsContainer = getEmailsContainer(text, emails, 'groupMemberEmailsContainerStyleClass');
				emailsContainer.injectInside(container);
			}
		}
	}
	
	getDivsSpacer().injectInside(container);
	
	return container;
}

function getLocalizationForUserStatus(key) {
	if (LOCALIZATIONS == null || key == null) {
		return null;
	}
	
	var localization = null;
	var found = false;
	for (var i = 0; i < LOCALIZATIONS.length; i++) {
		if (LOCALIZATIONS[i].id == key) {
			localization = LOCALIZATIONS[i].value;
			found = true;
		}
	}
	if (found) {
		return localization;
	}
	return 'Unknown';
}