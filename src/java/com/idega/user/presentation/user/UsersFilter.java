package com.idega.user.presentation.user;

import com.idega.presentation.IWContext;
import com.idega.user.presentation.group.GroupsFilter;

public class UsersFilter extends GroupsFilter {

	@Override
	public void main(IWContext iwc) {
		add("Here will be users chooser");
		return;
	}
}
