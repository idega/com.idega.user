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
	container.className = 'groupsMembersInfoList';
	
	var groups = document.createElement('ul');
	container.appendChild(groups);
	
	for (var i = 0; i < usersInfo.length; i++) {
		var group = document.createElement('li');
		groups.appendChild(group);
		
		//	Group name
		if (properties.showGroupName) {
			group.appendChild(getGroupInfoEntryPO(null, usersInfo[i].groupName, false));
		}
		var members = usersInfo[i].membersInfo;
		if (members != null) {
			var users = document.createElement('ul');
			group.appendChild(users);
			for (var j = 0; j < members.length; j++) {
				var user = document.createElement('li');
				users.appendChild(user);
				
				//	Image
				if (properties.showImage) {
					if (members[j].imageUrl != null) {
						var imageContainer = document.createElement('div');
						var image = document.createElement('img');
						image.setAttribute('src', properties.server + members[j].imageUrl);
						if (properties.imageWidth != null) {
							image.setAttribute('width', properties.imageWidth);
						}
						if (properties.imageHeight != null) {
							image.setAttribute('height', properties.imageHeight);
						}
						imageContainer.appendChild(image);
						user.appendChild(imageContainer);
					}
				}
				//	Name
				user.appendChild(getGroupInfoEntryPO(properties.localizedText[0], members[j].name, true, properties.showLabels));
				//	Title
				if (properties.showTitle) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[1], members[j].title, true, properties.showLabels));
				}
				//	Age
				if (properties.showAge) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[2], members[j].age, true, properties.showLabels));
				}
				//	Work phone
				if (properties.showWorkPhone) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[3], members[j].workPhone, true, properties.showLabels));
				}
				//	Home phone
				if (properties.showHomePhone) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[4], members[j].homePhone, true, properties.showLabels));
				}
				//	Mobile phone
				if (properties.showMobilePhone) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[5], members[j].mobilePhone, true, properties.showLabels));
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
					user.appendChild(mainEmailsContainer);
				}
				//	Education
				if (properties.showEducation) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[7], members[j].education, true, properties.showLabels));
				}
				//	School
				if (properties.showSchool) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[8], members[j].school, true, properties.showLabels));
				}
				//	Area
				if (properties.showArea) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[9], members[j].area, true, properties.showLabels));
				}
				//	Began work
				if (properties.showBeganWork) {
					user.appendChild(getGroupInfoEntryPO(properties.localizedText[10], members[j].beganWork, true, properties.showLabels));
				}
			}
		}
	}
	
	main.appendChild(container);
	closeAllLoadingMessages();	
}