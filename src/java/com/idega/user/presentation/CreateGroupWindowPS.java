package com.idega.user.presentation;

import com.idega.idegaweb.IWException;
import com.idega.user.event.CreateGroupEvent;
import com.idega.event.*;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class CreateGroupWindowPS extends IWPresentationStateImpl implements IWActionListener{

  private boolean _close = false ;
  private boolean _commit = false ;

  private String _groupName = null;
  private String _groupDescription = null;
  private String _groupType = null;
//  private String _groupParentID = null;

  private CreateGroupEvent _cgEvent = null;

  public CreateGroupWindowPS() {
  }


  public void reset(){
    _groupName = null;
    _groupDescription = null;
    _groupType = null;
    _commit = false;
    _cgEvent = null;
    _close = false;
  }

  public CreateGroupEvent getCreateGroupEvent(){
    return _cgEvent;
  }

  public String getGroupName(){
    return _groupName;
  }

  public String getGroupDescription(){
    return _groupDescription;
  }

  public String getGroupType(){
    return _groupType;
  }

  public boolean doClose(){
    return _close;
  }

  public boolean doCommit(){
    return _commit;
  }

  public void doneClosing(){
    _close = false;
  }

  public void doneCommiting(){
    _commit = false;
    _cgEvent = null;
  }



  public void actionPerformed(IWPresentationEvent e) throws IWException {
    System.out.println("[CreateGroupWindowPS]: ps = "+this);
    System.out.println("[CreateGroupWindowPS] : event = " + e);
    if(e instanceof CreateGroupEvent ){
      System.out.println("[CreateGroupWindowPS] : (e instanceof CreateGroupEvent) = true");
      CreateGroupEvent event = (CreateGroupEvent)e;

      System.out.println("[CreateGroupWindowPS] : event.doCommit() = "+event.doCommit());
      System.out.println("[CreateGroupWindowPS] : event.doCancel() = "+event.doCancel());

      if(event.doCommit()){
        _cgEvent = event;
        _commit = true;

      } else if(event.doCancel()){
        this.reset();
        _close = true;
      } else {
        _groupName = event.getName();
        _groupDescription = event.getDescription();
        _groupType = event.getGroupType();
      }


    }
  }
}