

package com.idega.user.app;

import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.user.business.GroupBusiness;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.event.*;
import com.idega.user.data.Group;
import com.idega.user.data.UserGroupPlugIn;
import com.idega.user.event.ChangeClassEvent;
import com.idega.user.event.SelectGroupEvent;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur Saemundsson</a>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */

public class UserApplicationMainAreaPS extends IWPresentationStateImpl implements IWActionListener {

  private EventListenerList _listenerList = new EventListenerList();
  private String _class = null;
  private Group _selectedGroup = null;
  private Collection _plugins = null;
  

  public UserApplicationMainAreaPS() {

  }

  public void addIWActionListener(IWActionListener l){
    listenerList.add(IWActionListener.class, l);

    Object[] list = _listenerList.getListenerList();

    boolean hasBeenAdded = false;
    // Is l on the list?
    for (int i = list.length-2; i>=0; i-=2) {
        if ((list[i]==IWActionListener.class) && (list[i+1].equals(l) == true)) {
            hasBeenAdded = true;
            break;
        }
    }
    if(!hasBeenAdded){
      _listenerList.add(IWActionListener.class,l);
    }
  }

  public void actionPerformed(IWPresentationEvent e) throws IWException{
    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }
    
    if(e instanceof SelectGroupEvent){
    	try{
    		System.out.println("Select group event! getting plugins");
	    	_selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
	    	String groupType = _selectedGroup.getGroupType();
	    	_plugins = getGroupBusiness( e.getIWContext()).getUserGroupPluginsForGroupTypeString(groupType);
	    	if( _plugins==null ) System.out.println("Plugins are null fro group_type ="+groupType);
	    	
	    	Iterator iter = _plugins.iterator();
    	
	    	while (iter.hasNext()) {
	    		
				UserGroupPlugIn plugin = (UserGroupPlugIn) iter.next();
				String className = plugin.getBusinessICObject().getClassName();
				System.out.println("Plugin business class : "+className);

			}
		
      		this.fireStateChanged();
    	}
    	catch( Exception ex ){
    		ex.printStackTrace();	
    	}
    }
    
    
    if(e instanceof SimpleSearchEvent){
      System.out.println("[UserAppMainArea]: search for "+((SimpleSearchEvent)e).getSearchString());
      System.out.println("[UserAppMainArea]: searchType =  "+((SimpleSearchEvent)e).getSearchType());
      this.fireStateChanged();
    }
    
    if(e instanceof ChangeClassEvent){
      _class = ((ChangeClassEvent)e).getChangeClassName();
      System.out.println(this+"Class to change to is "+((ChangeClassEvent)e).getChangeClassName() );
      this.fireStateChanged();
    }

    IWActionListener[] listners =  (IWActionListener[])_listenerList.getListeners(IWActionListener.class);
    for (int i = 0; i < listners.length; i++) {
      listners[i].actionPerformed(e);
    }

  }

  public String getClassNameToShow(){
    return _class;
  }
 

  public void setClassNameToShow(String className){
    _class = className;
  }

  public Group getSelectedGroup(){
    return _selectedGroup;
  }
  
  public Collection getUserGroupPlugins(){
    return _plugins;
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

/*
  public Object clone() {
    UserApplicationMainAreaPS obj = null;
    obj = (UserApplicationMainAreaPS)super.clone();
    obj._class = this._class;
    obj._listenerList = this._listenerList;
    return obj;
  }*/

}