package com.idega.user.business;

import javax.ejb.CreateException;

import com.idega.business.IBOHomeImpl;

public class GroupServiceHomeImpl extends IBOHomeImpl implements GroupServiceHome {

	private static final long serialVersionUID = 7141978995132493723L;

	public Class getBeanInterfaceClass() {
		return GroupService.class;
	}

	public GroupService create() throws CreateException {
		return (GroupService) super.createIBO();
	}

}
