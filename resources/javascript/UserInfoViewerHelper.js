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
		
		//	Group name
		/*if (properties.showGroupName) {
			group.appendChild(getGroupInfoEntryPO(null, usersInfo[i].groupName, false));
		}*/
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
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[11], members[j].status, true, properties.showLabels, 'groupMemberStatusContainerStyleClass'));
				}
				
				//	Empty line
				var emptyLine = document.createElement('div');
				emptyLine.setAttribute('class', 'emptyLineContainerStyleClass');
				emptyLine.appendChild(document.createElement('br'));
				infoContainer.appendChild(emptyLine);
				
				//	Name
				infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[0], members[j].name, true, properties.showLabels, 'groupMemberNameContainerStyleClass'));
				
				// Address
				if (properties.showAddress) {
					infoContainer.appendChild(getAddressContainer(members[j].address, 'groupMemberAddressContainerStyleClass', false, properties.showLabels, properties.localizedText[12]));
				}
				
				//	Title
				if (properties.showTitle) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[1], members[j].title, true, properties.showLabels, 'groupMemberTitleContainerStyleClass'));
				}
				//	Age
				if (properties.showAge) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[2], members[j].age, true, properties.showLabels, 'groupMemberAgeContainerStyleClass'));
				}
				//	Work phone
				if (properties.showWorkPhone) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[3], members[j].workPhone, true, properties.showLabels, 'groupMemberWorkphoneContainerStyleClass'));
				}
				//	Home phone
				if (properties.showHomePhone) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[4], members[j].homePhone, true, properties.showLabels, 'groupMemberHomephoneContainerStyleClass'));
				}
				//	Mobile phone
				if (properties.showMobilePhone) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[5], members[j].mobilePhone, true, properties.showLabels, 'groupMemberMobilephoneContainerStyleClass'));
				}
				//	Emails
				if (properties.showEmails) {
					var mainEmailsContainer = document.createElement('div');
					if (properties.showLabels) {
						mainEmailsContainer.appendChild(document.createTextNode(properties.localizedText[6]));
					}
					var emailsContainer = getEmailsContainer(members[j].emailsAddresses);
					if (emailsContainer != null) {
						mainEmailsContainer.appendChild(emailsContainer);
					}
					infoContainer.appendChild(mainEmailsContainer);
				}
				//	Education
				if (properties.showEducation) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[7], members[j].education, true, properties.showLabels, 'groupMemberEducationContainerStyleClass'));
				}
				//	School
				if (properties.showSchool) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[8], members[j].school, true, properties.showLabels, 'groupMemberSchoolContainerStyleClass'));
				}
				//	Area
				if (properties.showArea) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[9], members[j].area, true, properties.showLabels, 'groupMemberAreaContainerStyleClass'));
				}
				//	Began work
				if (properties.showBeganWork) {
					infoContainer.appendChild(getGroupInfoEntryPO(properties.localizedText[10], members[j].beganWork, true, properties.showLabels, 'groupMemberBeganworkContainerStyleClass'));
				}
			}
		}
	}
	
	main.appendChild(container);
	main.appendChild(getDivsSpacer());
	closeAllLoadingMessages();	
}