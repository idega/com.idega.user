package com.idega.user.business;

import com.idega.repository.data.ConstantsPlaceholder;
import com.idega.util.CoreConstants;

public class UserConstants implements ConstantsPlaceholder {

	public static final String IW_BUNDLE_IDENTIFIER = CoreConstants.IW_USER_BUNDLE_IDENTIFIER;

	//	Cache keys
	public static final String GROUP_INFO_VIEWER_CACHE_KEY = "group_info_viewer_block";
	public static final String GROUP_USERS_VIEWER_CACHE_KEY = "group_users_viewer_block";
	public static final String GROUP_INFO_CHOOSER_CACHE_KEY = "group_info_chooser_block";
	public static final String GROUP_INFO_VIEWER_DATA_CACHE_KEY = "group_info_viewer_data_cache_key";
	public static final String GROUP_USERS_VIEWER_DATA_CACHE_KEY = "group_users_viewer_data_cache_key";

	//	Bean ID
	public static final String GROUPS_MANAGER_BEAN_ID = "GroupsManagerBean";

	public static final String GROUP_VIEWER_CONTAINER_ID_ENDING = "_realChildComponent";

	public static final String GROUPS_TO_RELOAD_IN_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION = "groupsToReloadInMenuDropdownIdInSimpleUserApplication";
	public static final String EDITED_GROUP_MENU_DROPDOWN_ID_IN_SIMPLE_USER_APPLICATION = "eidtedGroupMenuDropdownIdInSimpleUserApplication";
	public static final String AVAILABLE_GROUP_TYPES_IN_SIMPLE_USER_APPLICATION = "availableGroupTypesInSimpleUserApplication";
	public static final String AVAILABLE_ROLE_TYPES_IN_SIMPLE_USER_APPLICATION = "availableRoleTypesInSimpleUserApplication";

	public static final String EMAIL_PLACEHOLDER_ADDED_ACCESS = "EMAIL_PLACEHOLDER_ADDED_ACCESS";
	public static final String EMAIL_PLACEHOLDER_CLUB = "EMAIL_PLACEHOLDER_CLUB";
}