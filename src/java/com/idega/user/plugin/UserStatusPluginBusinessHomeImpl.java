package com.idega.user.plugin;


public class UserStatusPluginBusinessHomeImpl extends com.idega.business.IBOHomeImpl implements UserStatusPluginBusinessHome
{
 protected Class getBeanInterfaceClass(){
  return UserStatusPluginBusiness.class;
 }


 public UserStatusPluginBusiness create() throws javax.ejb.CreateException{
  return (UserStatusPluginBusiness) super.createIBO();
 }



}