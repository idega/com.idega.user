package com.idega.user.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHomeImpl;

public class UserApplicationEngineHomeImpl extends IBOHomeImpl implements UserApplicationEngineHome {
	public Class getBeanInterfaceClass() {
		return UserApplicationEngine.class;
	}

	public UserApplicationEngine create() throws CreateException {
		return (UserApplicationEngine) super.createIBO();
	}
}