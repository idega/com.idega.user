package com.idega.user.app;

import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.presentation.IWContext;
import com.idega.presentation.IWTabbedPane;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.presentation.text.Link;
import com.idega.user.block.search.event.UserSearchEvent;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.event.ChangeClassEvent;
import com.idega.user.event.SelectGroupEvent;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateGroupWindowPS;
import com.idega.user.presentation.DeleteGroupConfirmWindowPS;
import com.idega.user.presentation.GroupPropertyWindow;
import com.idega.user.presentation.UserPropertyWindow;

/**
 * <p>Description: The main actionlistener for the main area of the user application</p>
 * <p>Copyright: Idega Software Copyright (c) 2002</p>
 * <p>Company: Idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur Saemundsson</a>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */

public class UserApplicationMainAreaPS extends IWControlFramePresentationState implements IWActionListener {

  private EventListenerList _listenerList = new EventListenerList();
  private String _class = null;
  private Group _selectedGroup = null;
  private Collection _plugins = null;
  public boolean search = false;
  
  private String styledLinkClass = "styledLinkGeneral";

  

  public UserApplicationMainAreaPS() {

  }

  public void addIWActionListener(IWActionListener l){
    this.listenerList.add(IWActionListener.class, l);

    Object[] list = this._listenerList.getListenerList();

    boolean hasBeenAdded = false;
    // Is l on the list?
    for (int i = list.length-2; i>=0; i-=2) {
        if ((list[i]==IWActionListener.class) && (list[i+1].equals(l) == true)) {
            hasBeenAdded = true;
            break;
        }
    }
    if(!hasBeenAdded){
      this._listenerList.add(IWActionListener.class,l);
    }
  }

  public void actionPerformed(IWPresentationEvent e) throws IWException{
  	  	
    if(e instanceof ResetPresentationEvent){
			this.search = false;
      this.reset();
      this.fireStateChanged();
    }
    
    
    if(e instanceof SelectGroupEvent){
    	try{
    		//System.out.println("Select group event! getting plugins");
	    	this._selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
	    	String groupType = this._selectedGroup.getGroupType();
	    	//System.out.println("Selected group type = "+groupType);
	    	
	    	this._plugins = getGroupBusiness( e.getIWContext()).getUserGroupPluginsForGroupType(groupType);
	    		    	
		this.search = false;
      	this.fireStateChanged();
    	}
    	catch( Exception ex ){
    		ex.printStackTrace();	
    	}
    }
    
    
    if(e instanceof UserSearchEvent){
      System.out.println("[UserAppMainArea]: search for "+((UserSearchEvent)e).getSearchString());
      System.out.println("[UserAppMainArea]: searchType =  "+((UserSearchEvent)e).getSearchType());
      this.search = true;  
      this.fireStateChanged();
    }
    
    if(e instanceof ChangeClassEvent){
      this._class = ((ChangeClassEvent)e).getChangeClassName();
      System.out.println(this+"Class to change to is "+((ChangeClassEvent)e).getChangeClassName() );
			this.search = false;
      this.fireStateChanged();
    }

    IWActionListener[] listners =  (IWActionListener[])this._listenerList.getListeners(IWActionListener.class);
    for (int i = 0; i < listners.length; i++) {
      listners[i].actionPerformed(e);
    }
    
    
		if( this.search ){
			this._class= null;
			this._selectedGroup = null;
			this._plugins = null;
		}

  }

  public String getClassNameToShow(){
    return this._class;
  }
 

  public void setClassNameToShow(String className){
    this._class = className;
  }

  public Group getSelectedGroup(){
    return this._selectedGroup;
  }
  
  public boolean isSearch(){
  	return this.search;
  }
  
    
  public Collection getUserGroupPlugins(){
    return this._plugins;
  }
  
  public GroupBusiness getGroupBusiness(IWApplicationContext iwc){
    GroupBusiness business = null;
    if(business == null){
      try{
        business = (GroupBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
    }
    return business;
  }

  public void stateChanged(ChangeEvent e) {
    Object object = e.getSource();
    if (object instanceof DeleteGroupConfirmWindowPS) { 
      // refresh
      setOnLoad("parent.frames['iwb_main_left'].location.reload()");
      setOnLoad("parent.frames['iwb_main'].location.reload()");
    }
    
    
    // do not use "else if" !
    if (object instanceof CreateGroupWindowPS) {
      CreateGroupWindowPS state = (CreateGroupWindowPS) e.getSource();
      IWContext eventContext = state.getEventContext();
      Integer groupId = state.getGroupId();
      if(groupId!=null){
	      Link gotoLink = new Link();  
	      gotoLink.setWindowToOpen(GroupPropertyWindow.class);
	      gotoLink.addParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, groupId.toString());
	      setOnLoad("parent.frames['iwb_main_left'].location.reload()");
	      setOnLoad("parent.frames['iwb_main'].location.reload()");
	      setOnLoad(gotoLink.getWindowToOpenCallingScript(eventContext));
      }else{
      	//group creation must have failed or the groupt type changed open the window again
      	//eiki
	      Link gotoLink = new Link();  
	      gotoLink.setWindowToOpen(CreateGroupWindow.class);
	      setOnLoad(gotoLink.getWindowToOpenCallingScript(eventContext));
      }
      
    }
    if (object instanceof IWTabbedPane) {
      IWTabbedPane pane = (IWTabbedPane) object;
      String attribute = pane.getMarkupAttributesString();
      String userPropertyString = UserPropertyWindow.SESSION_ADDRESS;
      String groupPropertyString = GroupPropertyWindow.SESSION_ADDRESS;
      boolean groupProperty = (attribute.indexOf(groupPropertyString) > -1);
      boolean userProperty = (attribute.indexOf(userPropertyString) > -1);
      if (groupProperty || userProperty) {
		setOnLoad("parent.frames['iwb_main'].location.reload()");
	}
      if (groupProperty) {
		setOnLoad("parent.frames['iwb_main_left'].location.reload()");
	} 
    }
  }

/*
  public Object clone() {
    UserApplicationMainAreaPS obj = null;
    obj = (UserApplicationMainAreaPS)super.clone();
    obj._class = this._class;
    obj._listenerList = this._listenerList;
    return obj;
  }*/

}