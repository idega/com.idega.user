package com.idega.user.business;


import javax.ejb.CreateException;

import com.idega.business.IBOHomeImpl;

public class GroupServiceHomeImpl extends IBOHomeImpl implements GroupServiceHome {

	private static final long serialVersionUID = -7275929807687783008L;

	@Override
	public Class<GroupService> getBeanInterfaceClass() {
		return GroupService.class;
	}

	@Override
	public GroupService create() throws CreateException {
		return (GroupService) super.createIBO();
	}
}