var LOCALIZATIONS = null;

function reloadGroupMemberProperties(instanceId, containerId, message) {
	GroupService.reloadProperties(instanceId, {
		callback: function(result) {
			reloadGroupMemberPropertiesCallback(result, instanceId, containerId, message);
		}
	})
}

function reloadGroupMemberPropertiesCallback(result, instanceId, containerId, message) {
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
	if (list == null) {
		closeAllLoadingMessages();
		return false;
	}
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

function getUsersInfoCallback(usersInfo, properties, containerId) {
	if (usersInfo == null || containerId == null) {
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
	container.setAttribute('class', 'groupsMembersInfoList');
	
	for (var i = 0; i < usersInfo.length; i++) {
		var group = document.createElement('div');
		container.appendChild(group);
		
		var members = usersInfo[i].membersInfo;
		if (members != null) {
			var users = document.createElement('div');
			group.appendChild(users);
			for (var j = 0; j < members.length; j++) {
				var user = document.createElement('div');
				users.appendChild(user);
				user.setAttribute('class', 'groupMemberStyleClass');
				users.appendChild(getDivsSpacer());
				
				//	Image
				var imageContainer = document.createElement('div');
				imageContainer.setAttribute('class', 'groupMemberPhotoContainerStyleClass');
				user.appendChild(imageContainer);
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
						var image = document.createElement('img');
						image.setAttribute('src', imageSrc);
						if (properties.imageWidth != null) {
							image.setAttribute('width', properties.imageWidth);
						}
						if (properties.imageHeight != null) {
							image.setAttribute('height', properties.imageHeight);
						}
						imageContainer.appendChild(image);
						
						//	Add reflecion?
						if (properties.addReflection) {
							image.addReflection({height: '0.16', opacity: '0.55'});
						}
					}
				}
				
				//	Info
				var infoContainer = document.createElement('div');
				infoContainer.setAttribute('class', 'groupMemberInfoContainerStyleClass');
				user.appendChild(infoContainer);
				
				//	Status
				if (properties.showStatus) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[11], getLocalizationForUserStatus(members[j].status), true, properties.showLabels, 'groupMemberStatusContainerStyleClass'));
				
					//	Empty line
					var emptyLine = document.createElement('div');
					emptyLine.setAttribute('class', 'emptyLineContainerStyleClass');
					emptyLine.appendChild(document.createElement('br'));
					infoContainer.appendChild(emptyLine);
				}
				
				//	Name and age
				infoContainer.appendChild(getUserNameAndAgeContainer(members[j].name, members[j].age, properties));
				
				// Address
				if (properties.showAddress) {
					infoContainer.appendChild(getAddressContainer(members[j].address, 'groupMemberAddressContainerStyleClass', false, properties.showLabels, properties.localizedText[12]));
				}
				
				//	Phones and mails
				infoContainer.appendChild(getPhonesAndEmailsContainer(members[j].homePhone, members[j].workPhone, members[j].mobilePhone, members[j].emailsAddresses, 'groupMemberPhonesAndMailsContainerStyleClass', properties));
				
				//	Company address
				if (properties.showCompanyAddress) {
					var companyAddressContainer = getAddressContainer(members[j].companyAddress, 'groupMemberCompanyAddressContainerStyleClass', false, properties.showLabels, properties.localizedText[18]);
					if (companyAddressContainer != null) {
						infoContainer.appendChild(companyAddressContainer);
					}
				}
				
				//	Group name
				if (properties.showGroupName) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[22], usersInfo[i].groupName, false, properties.showLabels, 'groupMemberGroupContainerStyleClass'));
				}
				
				//	Date of birth
				if (properties.showDateOfBirth) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[19], members[j].dateOfBirth, false, properties.showLabels, 'groupMemberDateOfBirthContainerStyleClass'));
				}
				
				//	Job
				if (properties.showJob) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[20], members[j].job, false, properties.showLabels, 'groupMemberJobContainerStyleClass'));
				}
				
				//	Workplace
				if (properties.showWorkplace) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[21], members[j].workPlace, false, properties.showLabels, 'groupMemberWorkplaceContainerStyleClass'));
				}
				
				//	Extra info
				if (properties.showExtraInfo) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[16], members[j].extraInfo, false, properties.showLabels, 'groupMemberExtraInfoContainerStyleClass'));
				}
				
				//	Description
				if (properties.showDescription) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[17], members[j].description, false, properties.showLabels, 'groupMemberDescriptionContainerStyleClass'));
				}
				
				//	Title
				if (properties.showTitle) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[1], members[j].title, false, properties.showLabels, 'groupMemberTitleContainerStyleClass'));
				}
				//	Education
				if (properties.showEducation) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[7], members[j].education, false, properties.showLabels, 'groupMemberEducationContainerStyleClass'));
				}
				//	School
				if (properties.showSchool) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[8], members[j].school, false, properties.showLabels, 'groupMemberSchoolContainerStyleClass'));
				}
				//	Area
				if (properties.showArea) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[9], members[j].area, false, properties.showLabels, 'groupMemberAreaContainerStyleClass'));
				}
				//	Began work
				if (properties.showBeganWork) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[10], members[j].beganWork, false, properties.showLabels, 'groupMemberBeganworkContainerStyleClass'));
				}
			}
		}
	}
	
	main.appendChild(container);
	main.appendChild(getDivsSpacer());
	closeAllLoadingMessages();	
}

function getUserNameAndAgeContainer(name, age, properties) {
	var container = document.createElement('div');
	container.setAttribute('class', 'groupMemberNameAndAgeContainerStyleClass');
	
	//	Name
	if (properties.showLabels) {
		container.appendChild(document.createTextNode(properties.localizedText[0]));
	}
	container.appendChild(document.createTextNode(name));
	
	//	Age
	if (age != null && age != '') {
		if (properties.showAge) {
			if (properties.showLabels) {
				container.appendChild(document.createTextNode(' ' + properties.localizedText[2] + age));
			}
			else {
				container.appendChild(document.createTextNode(' (' + age + ')'));
			}
		}
	}
	
	return container;
}

function getPhonesAndEmailsContainer(homePhone, workPhone, mobilePhone, emails, styleClass, properties) {
	var container = document.createElement('div');
	if (styleClass != null) {
		container.setAttribute('class', styleClass);
	}
	
	var addedAnything = false;
	
	//	Home phone
	if (properties.showHomePhone) {
		if (homePhone != null && homePhone != '') {
			container.appendChild(getUserPhoneLine(properties.localizedText[13], homePhone));
			addedAnything = true;
		}
	}
	
	//	Work phone
	if (properties.showWorkPhone) {
		if (workPhone != null && workPhone != '') {
			if (addedAnything) {
				container.appendChild(document.createTextNode(' / '));
			}
			container.appendChild(getUserPhoneLine(properties.localizedText[14], workPhone));
			addedAnything = true;
		}
	}
	
	//	Mobile phone
	if (properties.showMobilePhone && mobilePhone != '') {
		if (mobilePhone != null) {
			if (addedAnything) {
				container.appendChild(document.createTextNode(' / '));
			}
			container.appendChild(getUserPhoneLine(properties.localizedText[15], mobilePhone));
			addedAnything = true;
		}
	}
	
	//	Emails
	if (properties.showEmails) {
		if (emails != null) {
			var emailsContainer = getEmailsContainer(emails);
			if (emailsContainer != null) {
				emailsContainer.setAttribute('class', 'groupMemberEmailsContainerStyleClass');
				if (addedAnything) {
					container.appendChild(document.createTextNode(' / '));
				}
				container.appendChild(emailsContainer);
			}
		}
	}
	
	return container;
}

function getUserPhoneLine(loacalization, number) {
	return document.createTextNode(loacalization + '. ' + number);
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