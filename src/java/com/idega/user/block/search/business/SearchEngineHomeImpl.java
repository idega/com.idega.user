package com.idega.user.block.search.business;


public class SearchEngineHomeImpl extends com.idega.business.IBOHomeImpl implements SearchEngineHome
{
 protected Class getBeanInterfaceClass(){
  return SearchEngine.class;
 }


 public SearchEngine create() throws javax.ejb.CreateException{
  return (SearchEngine) super.createIBO();
 }


}