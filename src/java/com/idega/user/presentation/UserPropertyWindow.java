package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.*;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.TabbedPropertyWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.*;
import com.idega.user.data.*;
import com.idega.util.IWColor;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserPropertyWindow extends TabbedPropertyWindow{

  public static final String PARAMETERSTRING_USER_ID = "ic_user_id";

  public UserPropertyWindow(){
    super();
    this.setBackgroundColor(new IWColor(207,208,210));
  }

  public String getSessionAddressString(){
    return "ic_user_property_window";
  }

  public void initializePanel( IWContext iwc, TabbedPropertyPanel panel){
  	int count = 0;
    GeneralUserInfoTab genTab = new GeneralUserInfoTab();

    panel.addTab(genTab, count, iwc);
    panel.addTab(new AddressInfoTab(), ++count, iwc);
    panel.addTab(new UserPhoneTab(), ++count, iwc);
    panel.addTab(new UserGroupList(),++count,iwc);
    
    try {//temporary before plugins work
		
    panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserFamilyTab").newInstance() ,++count,iwc);
    panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserFinanceTab").newInstance() ,++count,iwc);
    panel.addTab((PresentationObject)Class.forName("is.idega.idegaweb.member.presentation.UserHistoryTab").newInstance() ,++count,iwc);
	
	
	//temp shit
	String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
      int userId = Integer.parseInt(id);
      User user = getUserBusiness(iwc).getUser(userId);
   
	  Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForUser(user);
	  Iterator iter = plugins.iterator();
	  
	  while (iter.hasNext()) {
		UserGroupPlugIn element = (UserGroupPlugIn) iter.next();
		
		
		UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness)
				 com.idega.business.IBOLookup.getServiceInstance(iwc,Class.forName(element.getBusinessICObject().getClassName()));
			
		
		
		
		List tabs = pluginBiz.getUserPropertiesTabs(user);
		Iterator tab = tabs.iterator();
		while (tab.hasNext()) {
			UserTab el = (UserTab) tab.next();
			panel.addTab(el,++count,iwc);
		}
		
	  }
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	
    UserLoginTab ult = new UserLoginTab();
    ult.displayLoginInfoSettings();
    panel.addTab(ult,++count,iwc);


  }

  public void main(IWContext iwc) throws Exception {
    String id = iwc.getParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID);
    if(id != null){
      int newId = Integer.parseInt(id);
      PresentationObject[] obj = this.getAddedTabs();
      for (int i = 0; i < obj.length; i++) {
        PresentationObject mo = obj[i];
        if( mo instanceof UserTab && ((UserTab)mo).getUserId() != newId){
          mo.setIWContext(iwc);
          ((UserTab)mo).setUserID(newId);
        }
      }
    }
  }
  
  public GroupBusiness getGroupBusiness(IWApplicationContext iwac) throws RemoteException{
  	return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac,GroupBusiness.class);
  }
  
  public UserBusiness getUserBusiness(IWApplicationContext iwac) throws RemoteException{
  	return (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac,UserBusiness.class);
  }

}
