package com.idega.user.business;

import java.util.Collection;

import com.idega.business.IBOServiceBean;

public class GroupServiceBean extends IBOServiceBean implements GroupService{
	
	public Collection getTopGroupNodes(){
		GroupHelperBusinessBean groupHelper = new GroupHelperBusinessBean();
		Collection nodes = groupHelper.getTopGroupNodes();
		return nodes;
	}

	public String getDivId(){
		return GroupConstants.groupsDivId;
	}
}
