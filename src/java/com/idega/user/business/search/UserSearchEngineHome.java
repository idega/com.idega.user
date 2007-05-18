package com.idega.user.business.search;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface UserSearchEngineHome extends IBOHome {
	public UserSearchEngine create() throws CreateException, RemoteException;
}