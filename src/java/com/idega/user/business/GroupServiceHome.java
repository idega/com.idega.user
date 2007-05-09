package com.idega.user.business;

import javax.ejb.CreateException;
import com.idega.business.IBOHome;
import java.rmi.RemoteException;

public interface GroupServiceHome extends IBOHome {
	public GroupService create() throws CreateException, RemoteException;
}