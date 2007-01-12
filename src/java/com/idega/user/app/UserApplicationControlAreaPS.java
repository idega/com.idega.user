package com.idega.user.app;

import javax.swing.event.EventListenerList;

import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.presentation.event.ResetPresentationEvent;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class UserApplicationControlAreaPS extends IWPresentationStateImpl implements IWActionListener {

//  private Hashtable _listenerlists = new Hashtable();

  private EventListenerList _listenerList = new EventListenerList();

  public UserApplicationControlAreaPS() {


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

    IWActionListener[] listners =  (IWActionListener[])this._listenerList.getListeners(IWActionListener.class);
    for (int i = 0; i < listners.length; i++) {
      listners[i].actionPerformed(e);
    }

  }


}