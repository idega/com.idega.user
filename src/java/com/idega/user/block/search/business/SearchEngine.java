package com.idega.user.block.search.business;


public interface SearchEngine extends com.idega.business.IBOService
{
 public java.lang.Class getResultType(com.idega.user.block.search.event.SimpleSearchEvent p0) throws java.rmi.RemoteException;
 public java.util.Collection getResult(com.idega.user.block.search.event.SimpleSearchEvent p0) throws java.rmi.RemoteException;
}
