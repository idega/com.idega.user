package com.idega.user.business;


import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface UserApplicationEngineHome extends IBOHome {
	public UserApplicationEngine create() throws CreateException, RemoteException;
}