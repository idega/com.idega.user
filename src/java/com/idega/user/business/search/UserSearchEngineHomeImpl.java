package com.idega.user.business.search;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class UserSearchEngineHomeImpl extends IBOHomeImpl implements UserSearchEngineHome {

	private static final long serialVersionUID = 6014950840925710030L;

	public Class getBeanInterfaceClass() {
		return UserSearchEngine.class;
	}

	public UserSearchEngine create() throws CreateException {
		return (UserSearchEngine) super.createIBO();
	}
}