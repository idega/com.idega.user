package com.idega.user.app;

import com.idega.event.*;
import java.util.*;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.presentation.event.TreeViewerEvent;
import com.idega.user.data.Group;
import com.idega.user.presentation.BasicUserOverviewPS;
import com.idega.user.presentation.DeleteGroupConfirmWindowPS;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationMenuAreaPS extends IWControlFramePresentationState implements IWActionListener {

  private EventListenerList _listenerList = new EventListenerList();
  
  private Integer selectedGroupId = null;

  public UserApplicationMenuAreaPS() {


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

//  public void addInnerListener(Class event, IWActionListener l){
//    EventListenerList list = (EventListenerList)_listenerlists.get(event);
//    if(list == null){
//      list = new EventListenerList();
//      _listenerlists.put(event,list);
//    }
//    list.add(IWActionListener.class,l);
//  }


  public void actionPerformed(IWPresentationEvent e) throws IWException{
    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }

    IWActionListener[] listners =  (IWActionListener[])_listenerList.getListeners(IWActionListener.class);
    for (int i = 0; i < listners.length; i++) {
      listners[i].actionPerformed(e);
    }

  }
  
  public Integer getSelectedGroupId() {
    return selectedGroupId;
  }

  public void stateChanged(ChangeEvent e) {
    Object object = e.getSource();
    if (object instanceof BasicUserOverviewPS) {
      BasicUserOverviewPS state = (BasicUserOverviewPS) object;
      Group group = state.getSelectedGroup();
      if (group != null)
        selectedGroupId = (Integer) group.getPrimaryKey();
    }
    else if (object instanceof DeleteGroupConfirmWindowPS)  {
      // the former selected group does not exist any longer!
      selectedGroupId = null;
    }
      
  }

}