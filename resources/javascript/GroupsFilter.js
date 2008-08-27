if (GroupsFilter == null) var GroupsFilter = {};

GroupsFilter.constants = {
	SEARCH_RESULT_PARAM: 'searchresult'
}

GroupsFilter.filterGroupsByNewInfo = function(params, selectedGroups, onClickAction, useRadioBox) {
	var id = params[0];
	var message = params[1];
	var containerId = params[2];
	var selectedGroupName = params[3];
	
	showLoadingMessage(message);
	GroupsFilterEngine.getFilteredGroups(jQuery('#' + id).attr('value'), selectedGroupName, selectedGroups, onClickAction, useRadioBox, {
		callback: function(html) {
			closeAllLoadingMessages();
			
			if (html == null) {
				return null;
			}
			
			var groupsBoxes = jQuery('div.filteredGroupsBoxStyle', jQuery('#' + containerId));
			if (groupsBoxes != null) {
				for (var i = 0; i < groupsBoxes.length; i++) {
					jQuery(groupsBoxes[i]).hide();
				}
			}
			
			IWCORE.insertHtml(html, document.getElementById(containerId));
		}
	});
}

GroupsFilter.clearSearchResults = function(params) {
	var containerId = params[0];
	var inputId = params[1];
	
	jQuery('#' + inputId).attr('value', '');
	
	var groupsBoxes = jQuery('div.filteredGroupsBoxStyle', jQuery('#' + containerId));
	if (groupsBoxes == null) {
		return false;
	}
	
	var box = null;
	var originalBox = null;
	for (var i = 0; i < groupsBoxes.length; i++) {
		box = jQuery(groupsBoxes[i]);
		if (box.attr(GroupsFilter.constants.SEARCH_RESULT_PARAM) == 'true') {
			box.remove();
		}
		else {
			originalBox = box;
		}
	}
	if (originalBox != null) {
		originalBox.show('fast');
	}
}

GroupsFilter.openOrCloseNodes = function(params, onClickAction, useRadioBox) {
	var image = jQuery('#' + params[0]);
	if (image == null) {
		return false;
	}
	
	var newImageUri = null;
	if (image.attr('opened') == 'true') {
		//	Close
		image.removeAttr('opened');
		newImageUri = params[2];
		
		var childrenContainer = jQuery('#' + params[1]);
		if (childrenContainer != null) {
			childrenContainer.hide('fast', function() {
				image.attr('src', params[2]);
			});
		}
	}
	else {
		//	Open
		if (image.attr('dataloaded') == 'true') {
			GroupsFilter.openClosedNodes(image, params);
		}
		else {
			showLoadingMessage(params[5]);
			GroupsFilterEngine.getChildGroups(image.attr('groupid'), params[4], onClickAction, useRadioBox, {
				callback: function(component) {
					closeAllLoadingMessages();
					
					if (component == null) {
						return false;
					}
					
					image.attr('dataloaded', 'true');
					IWCORE.insertHtml(component, document.getElementById(params[1]));
					GroupsFilter.openClosedNodes(image, params);
				}
			});
		}
	}
}

GroupsFilter.openClosedNodes = function(image, params) {
	image.attr('opened', 'true');
	
	var childrenContainer = jQuery('#' + params[1]);
	if (childrenContainer != null) {
		childrenContainer.show('fast', function() {
			image.attr('src', params[3]);
		});
	}
}

GroupsFilter.manageCheckedGroupsInOtherContainers = function(params) {
	var lists = jQuery('div.filteredGroupsBoxStyle');
	if (lists == null || lists.length <= 1) {
		return false;
	}
	
	var list = null;
	var currentListId = params[1];
	var checkBox = jQuery('#' + params[0]);
	for (var i = 0; i < lists.length; i++) {
		list = jQuery(lists[i]);
		if (list.attr('id') != currentListId) {
			var otherCheckboxes = jQuery('input.' + params[2], list);
			for (var j = 0; j < otherCheckboxes.length; j++) {
				console.log(otherCheckboxes[j]);
				otherCheckboxes[j].checked = false;
			}
		}
	}
}