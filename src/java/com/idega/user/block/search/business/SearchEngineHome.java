package com.idega.user.block.search.business;


public interface SearchEngineHome extends com.idega.business.IBOHome
{
 public SearchEngine create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}