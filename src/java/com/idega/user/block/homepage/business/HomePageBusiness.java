package com.idega.user.block.homepage.business;

import com.idega.builder.dynamicpagetrigger.business.DPTTriggerBusiness;


public interface HomePageBusiness extends DPTTriggerBusiness
{
 public void createHomePage(com.idega.presentation.IWContext p0,com.idega.user.data.Group p1,com.idega.builder.dynamicpagetrigger.data.PageTriggerInfo p2)throws java.rmi.RemoteException,java.sql.SQLException, java.rmi.RemoteException;
 public int getCurrentGroupID(com.idega.presentation.IWContext p0)throws java.lang.Exception, java.rmi.RemoteException;
 public java.util.List getGroupDPTPageLinks(int[] p0)throws java.sql.SQLException, java.rmi.RemoteException;
}
