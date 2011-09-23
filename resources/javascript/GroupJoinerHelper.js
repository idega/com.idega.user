// Copyright 2011 Idega. All Rights Reserved.


var GroupJoinerHelper = {
	joinGroup : function(userId,groupId,joinSelector,leaveSelector) {
		showLoadingMessage(GroupJoinerHelper.ADDING_TO_GROUP_MSG);
		GroupService.addUser(userId,groupId,{
			callback : function(success){
				closeAllLoadingMessages();
				if(success){
					jQuery(joinSelector).fadeOut(GroupJoinerHelper._FADE_DURATION);
					setTimeout(function(){jQuery(leaveSelector).fadeIn(GroupJoinerHelper._FADE_DURATION);},GroupJoinerHelper._FADE_DURATION);
				}else{
					humanMsg.displayMsg(GroupJoinerHelper.FAILURE_MSG);
				}
			}
		});
	},
	_FADE_DURATION : 500,
	leaveGroup : function(userId,groupId,joinSelector,leaveSelector) {
		showLoadingMessage(GroupJoinerHelper.REMOVING_FROM_GROUP_MSG);
		GroupService.removeUser(userId,groupId,{
			callback : function(success){
				closeAllLoadingMessages();
				if(success){
					jQuery(leaveSelector).fadeOut(GroupJoinerHelper._FADE_DURATION);
					setTimeout(function(){jQuery(joinSelector).fadeIn(GroupJoinerHelper._FADE_DURATION);},GroupJoinerHelper._FADE_DURATION);
				}else{
					humanMsg.displayMsg(GroupJoinerHelper.FAILURE_MSG);
				}
			}
		});
	}
};