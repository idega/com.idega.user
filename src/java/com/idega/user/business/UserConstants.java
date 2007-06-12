package com.idega.user.business;

import com.idega.repository.data.ConstantsPlaceholder;

public class UserConstants implements ConstantsPlaceholder {
	
	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	
	//	Cache keys
	public static final String GROUP_INFO_VIEWER_CACHE_KEY = "group_info_viewer_block";
	public static final String GROUP_USERS_VIEWER_CACHE_KEY = "group_users_viewer_block";
	public static final String GROUP_INFO_CHOOSER_CACHE_KEY = "group_info_chooser_block";
	
	//	Bean ids
	public static final String GROUPS_MANAGER_BEAN_ID = "GroupsManagerBean";

	public static final String GROUP_SERVICE_DWR_INTERFACE_SCRIPT = "/dwr/interface/GroupService.js";
	
	public static final String GROUP_VIEWER_CONTAINER_ID_ENDING = "_realChildComponent";
	
}