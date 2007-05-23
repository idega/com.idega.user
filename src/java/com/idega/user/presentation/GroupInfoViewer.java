package com.idega.user.presentation;

import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserConstants;

public class GroupInfoViewer extends Block {
	
	public GroupInfoViewer() {
		setCacheable(getCacheKey());
	}

	public String getCacheKey() {
		return UserConstants.GROUP_INFO_VIEWER_CACHE_KEY;
	}
	
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		return cacheStatePrefix;
	}

}
