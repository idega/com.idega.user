package com.idega.user.block.homepage.business;


public class HomePageBusinessHomeImpl extends com.idega.business.IBOHomeImpl implements HomePageBusinessHome
{
 protected Class getBeanInterfaceClass(){
  return HomePageBusiness.class;
 }


 public HomePageBusiness create() throws javax.ejb.CreateException{
  return (HomePageBusiness) super.createIBO();
 }



}