package com.idega.user.presentation;

import com.idega.user.event.*;
import com.idega.builder.data.IBDomain;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.user.data.Group;
import com.idega.presentation.event.TreeViewerEvent;
import com.idega.idegaweb.browser.event.IWBrowseEvent;
import java.util.Iterator;
import java.util.List;
import com.idega.presentation.Page;
import javax.swing.event.ChangeListener;
import com.idega.idegaweb.IWException;
import com.idega.event.*;

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