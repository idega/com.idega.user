

package com.idega.user.app;

import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.event.*;
import com.idega.user.event.ChangeClassEvent;
import javax.swing.event.EventListenerList;
import com.idega.idegaweb.IWException;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationMainAreaPS extends IWPresentationStateImpl implements IWActionListener {

  private EventListenerList _listenerList = new EventListenerList();
  private String _class = null;

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

/*
  public Object clone() {
    UserApplicationMainAreaPS obj = null;
    obj = (UserApplicationMainAreaPS)super.clone();
    obj._class = this._class;
    obj._listenerList = this._listenerList;
    return obj;
  }*/

}