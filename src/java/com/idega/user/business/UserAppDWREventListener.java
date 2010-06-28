package com.idega.user.business;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.dwr.event.DWREvent;
import com.idega.dwr.event.DWREventListener;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.user.presentation.BasicUserOverviewPS;

public class UserAppDWREventListener implements DWREventListener{
	UserBusiness business = null;
	
	public boolean processEvent(DWREvent event) {
		System.out.println(event);
		
		
		 try {
			 if(event.getEventType().equalsIgnoreCase("selectGroup")){
	             IWStateMachine stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(IWContext.getInstance(), IWStateMachine.class);
	             BasicUserOverviewPS business = (BasicUserOverviewPS) stateMachine.getStateFor("basicuseroverview", BasicUserOverviewPS.class);
	             UserBusiness buz = getUserBusiness(IWMainApplication.getDefaultIWApplicationContext());
	             business.setSelectedGroup(buz.getGroupBusiness().getGroupByUniqueId(event.getEventData().get("selectedGroup")));
	             
	             //piggybackevent
//	             DWREvent gimmiEvent = new DWREvent(this.getClass().toString(),"gimmiAwesome",null);
//	             event.addNestedEvent(gimmiEvent);	
			 }
             
         }
         catch (RemoteException re) {
             throw new RuntimeException(re.getMessage());
         } catch (FinderException e) {
			e.printStackTrace();
		}
         
         
		return true;
	}

	 public UserBusiness getUserBusiness(IWApplicationContext iwc) {
	       if (business == null) {
	            try {
	                business = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	            }
	            catch (java.rmi.RemoteException rme) {
	                throw new RuntimeException(rme.getMessage());
	            }
	        }
	        return business;
	  }
}