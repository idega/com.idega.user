package com.idega.user.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class UserApplicationEngineHomeImpl extends IBOHomeImpl implements UserApplicationEngineHome {
	
	private static final long serialVersionUID = 7234154543803528820L;

	public Class<UserApplicationEngine> getBeanInterfaceClass() {
		return UserApplicationEngine.class;
	}

	public UserApplicationEngine create() throws CreateException {
		return (UserApplicationEngine) super.createIBO();
	}
}