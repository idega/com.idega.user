package com.idega.user.presentation;

import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;

/**
 * <p>Title: The presentation state (IWActionListener) for
 * GroupPermissionWindow</p>
 * <p>Description: </p>
 * <p>Copyright: Idega Software (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */

public class GroupPermissionWindowPS extends IWPresentationStateImpl implements IWActionListener {

  Group _selectedGroup = null;

  public GroupPermissionWindowPS() {

  }

  public Group getSelectedGroup(){
    return _selectedGroup;
  }


  public void reset(){
    super.reset();
    _selectedGroup = null;
  }



  public void actionPerformed(IWPresentationEvent e)throws IWException{

System.out.println("ACTION IN LISTENER");
    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }

    if(e instanceof SelectGroupEvent){
      _selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
      this.fireStateChanged();
    }
    
    
    

  }


}