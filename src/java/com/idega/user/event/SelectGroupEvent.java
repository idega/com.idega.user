package com.idega.user.event;

import com.idega.user.data.Group;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class SelectGroupEvent extends IWPresentationEvent {

  private Integer _selectedGroup = null;
  public static final String PRM_GROUP_ID = "usgr_id";

  public SelectGroupEvent() {
  }

  public void setGroupToSelect(Integer primaryKey){
    this.addParameter(PRM_GROUP_ID, primaryKey.toString());
  }

  public void setGroupToSelect(int nodeId){
    this.addParameter(PRM_GROUP_ID, nodeId);
  }


  public Group getSelectedGroup(){
    if(_selectedGroup != null){
      try {
        return (Group)IDOLookup.findByPrimaryKey(Group.class,_selectedGroup);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  public boolean initializeEvent(IWContext iwc) {

    try {
      _selectedGroup = new Integer(iwc.getParameter(PRM_GROUP_ID));
      return true;
    }
    catch (NullPointerException ex) {
      return false;
    } catch (NumberFormatException e){
      e.printStackTrace();
      return false;
    }

  }
}