var LOCALIZATIONS = null;

function reloadGroupMemberProperties(instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	var strings = new Array();
	strings.push(instanceId);
	strings.push(containerId);
	strings.push(message);
	
	//	Getting proeprties bean
	GroupService.getBasicUserPropertiesBean(instanceId, {
		callback: function(bean) {
			getBasicUserPropertiesBeanCallback(bean, strings);
		}
	});
}

function getBasicUserPropertiesBeanCallback(bean, strings) {
	if (bean.remoteMode) {
		prepareDwr(GroupService, bean.server + getDefaultDwrPath());
	}
	else {
		prepareDwr(GroupService, getDefaultDwrPath());
	}
	
	//	Clearing cache
	GroupService.clearUsersInfoCache(bean, {
		callback: function(result) {
			clearUsersInfoCacheCallback(strings);
		}
	});
}

function clearUsersInfoCacheCallback(strings) {
	prepareDwr(GroupService, getDefaultDwrPath());
	
	//	Reloading properties
	GroupService.reloadProperties(strings[0], {
		callback: function(result) {
			reloadGroupMemberPropertiesCallback(result, strings[0], strings[1], strings[2]);
		}
	});
}

function reloadGroupMemberPropertiesCallback(result, instanceId, containerId, message) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
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

	//	To be sure we'll call to 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getUserStatusLocalization({
		callback: function(list) {
			getUserStatusLocalizationCallback(list, instanceId, containerId);
		}
	});
}

function getUserStatusLocalizationCallback(list, instanceId, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR

	LOCALIZATIONS = list;
	
	//	To be sure we'll call to 'local' server
	prepareDwr(GroupService, getDefaultDwrPath());
	
	GroupService.getUserPropertiesBean(instanceId, {
		callback: function(properties) {
			getUserPropertiesCallback(properties, containerId);
		}
	});
}

function getUserPropertiesCallback(properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
	if (properties == null) {
		closeAllLoadingMessages();
		return false;
	}
	
	if (properties.remoteMode) {
		//	To be sure we'll call to 'local' server
		prepareDwr(GroupService, getDefaultDwrPath());
		
		//	Remote mode
		if (properties.server == null || properties.login == null || properties.password == null) {
			closeAllLoadingMessages();
			return false;
		}
	
		GroupService.canUseRemoteServer(properties.server, {
			callback: function(result) {
				getGroupsUsersData(result, properties, containerId);
			}
		});
	}
	else {
		//	Local mode
		getGroupsUsersData(true, properties, containerId)
	}
}

function getGroupsUsersData(result, properties, containerId) {
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
	GroupService.getUsersInfo(properties, {
		callback: function(usersInfo) {
		 	getUsersInfoCallback(usersInfo, properties, containerId);
		}
	});
}

function getUsersInfoCallback(members, properties, containerId) {
	prepareDwr(GroupService, getDefaultDwrPath());	//	Restoring DWR
	if (members == null || containerId == null) {
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
			getGroupInfoEntryPO(properties.localizedText[11], getLocalizationForUserStatus(members[j].status), true, properties.showLabels, 'groupMemberStatusContainerStyleClass').injectInside(infoContainer);
		
			if (members[j].status != null && members[j].status != '') {
				//	Empty line
				var emptyLine = new Element('div');
				emptyLine.addClass('emptyLineContainerStyleClass');
				new Element('br').injectInside(emptyLine);
				emptyLine.injectInside(infoContainer);
			}
		}
		
		//	Name and age
		getUserNameAndAgeContainer(members[j].name, members[j].age, properties).injectInside(infoContainer);
		
		// Address
		if (properties.showAddress) {
			getAddressContainer(members[j].address, 'groupMemberAddressContainerStyleClass', false, properties.showLabels, properties.localizedText[12]).injectInside(infoContainer);
		}
				
		//	Phones and mails
		getPhonesAndEmailsContainer(members[j].homePhone, members[j].workPhone, members[j].mobilePhone, members[j].emailsAddresses, 'groupMemberPhonesAndMailsContainerStyleClass', properties).injectInside(infoContainer);
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
			getAddressContainer(members[j].companyAddress, 'groupMemberCompanyAddressContainerStyleClass', false, properties.showLabels, properties.localizedText[18]).injectInside(infoContainer);
		}
				
		//	Group name
		if (properties.showGroupName) {
			getGroupInfoEntryPO(properties.localizedText[22], members[j].groupName, false, properties.showLabels, 'groupMemberGroupContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Date of birth
		if (properties.showDateOfBirth) {
			getGroupInfoEntryPO(properties.localizedText[19], members[j].dateOfBirth, false, properties.showLabels, 'groupMemberDateOfBirthContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Job
		if (properties.showJob) {
			getGroupInfoEntryPO(properties.localizedText[20], members[j].job, false, properties.showLabels, 'groupMemberJobContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Workplace
		if (properties.showWorkplace) {
			getGroupInfoEntryPO(properties.localizedText[21], members[j].workPlace, false, properties.showLabels, 'groupMemberWorkplaceContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Extra info
		if (properties.showExtraInfo) {
			getGroupInfoEntryPO(properties.localizedText[16], members[j].extraInfo, false, properties.showLabels, 'groupMemberExtraInfoContainerStyleClass').injectInside(infoContainer);
		}
				
		//	Description
		if (properties.showDescription) {
			getGroupInfoEntryPO(properties.localizedText[17], members[j].description, false, properties.showLabels, 'groupMemberDescriptionContainerStyleClass').injectInside(infoContainer);
		}
				
		/*//	Title
		if (properties.showTitle) {
			getGroupInfoEntryPO(properties.localizedText[1], members[j].title, false, properties.showLabels, 'groupMemberTitleContainerStyleClass').injectInside(infoContainer);
		}
		//	Education
		if (properties.showEducation) {
			getGroupInfoEntryPO(properties.localizedText[7], members[j].education, false, properties.showLabels, 'groupMemberEducationContainerStyleClass').injectInside(infoContainer);
		}
		//	School
		if (properties.showSchool) {
			getGroupInfoEntryPO(properties.localizedText[8], members[j].school, false, properties.showLabels, 'groupMemberSchoolContainerStyleClass').injectInside(infoContainer);
		}
		//	Area
		if (properties.showArea) {
			getGroupInfoEntryPO(properties.localizedText[9], members[j].area, false, properties.showLabels, 'groupMemberAreaContainerStyleClass').injectInside(infoContainer);
		}
		//	Began work
		if (properties.showBeganWork) {
			getGroupInfoEntryPO(properties.localizedText[10], members[j].beganWork, false, properties.showLabels, 'groupMemberBeganworkContainerStyleClass').injectInside(infoContainer);
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

function getUserNameAndAgeContainer(name, age, properties) {
	var container = new Element('div');
	container.addClass('groupMemberNameAndAgeContainerStyleClass');
	
	//	Name
	if (properties.showLabels) {
		container.appendText(properties.localizedText[0]);
	}
	container.appendText(name);
	
	//	Age
	if (age != null && age != '') {
		if (properties.showAge) {
			if (properties.showLabels) {
				container.appendText(' ' + properties.localizedText[2] + age);
			}
			else {
				container.appendText(' (' + age + ')');
			}
		}
	}
	
	return container;
}

function getPhonesAndEmailsContainer(homePhone, workPhone, mobilePhone, emails, styleClass, properties) {
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
			homePhoneContainer.appendText(properties.localizedText[13] + '. ' + homePhone);
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
			workPhoneContainer.appendText(properties.localizedText[14] + '. ' + workPhone);
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
			mobilePhoneContainer.appendText(properties.localizedText[15] + '. ' +  mobilePhone);
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