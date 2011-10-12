if (UserGroups == null) var UserGroups = {};
var $j = jQuery.noConflict();

UserGroups.getRenderedGroup = function(loadingMessage, uniqueId, containerId, groupName) {
	showLoadingMessage(loadingMessage);
	
	jQuery('span.groupsTreeListElement').each(function() {
		var node = jQuery(this);
		if (uniqueId == node.attr('id')) {
			node.attr('style', 'font-weight: bold;');
		} else
			node.removeAttr('style');
	});
	
	prepareDwr(GroupService, getDefaultDwrPath());
	GroupService.getRenderedGroup(uniqueId, containerId, groupName, {
		callback: function(rendered) {
			if (rendered == null) {
				closeAllLoadingMessages();
				return;
			}
			
			IWCORE.insertRenderedComponent(rendered, {container: containerId, rewrite: true});
		},
		rpcType: dwr.engine.XMLHttpRequest,
		transport: dwr.engine.transport.xhr
	});
}

UserGroups.scrollToUsers = function(containerId, message) {
	if (UserInfoViewerHelper.GROUP_USERS == 0) {
		jQuery('#' + containerId).html('<h4>' + message + '</h4>');
	}
	$j.scrollTo('#' + containerId, 300);
}