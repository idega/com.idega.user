package com.idega.user.block.homepage.business;


public interface HomePageBusiness extends com.idega.business.IBOService
{
 public void createHomePage(com.idega.presentation.IWContext p0,com.idega.user.data.Group p1,com.idega.builder.dynamicpagetrigger.data.PageTriggerInfo p2)throws java.rmi.RemoteException,Exception, java.rmi.RemoteException;
 public int getCurrentGroupID(com.idega.presentation.IWContext p0)throws java.lang.Exception, java.rmi.RemoteException;
 public java.util.List getGroupDPTPageLinks(int[] p0)throws java.sql.SQLException, java.rmi.RemoteException;
 public boolean invalidateGroup(com.idega.presentation.IWContext p0,com.idega.user.data.Group p1)throws com.idega.data.IDOLookupException, java.rmi.RemoteException;
}
