package com.idega.user.event;

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

public class CreateGroupEvent extends IWPresentationEvent {

  public static final int TYPE_DOMAIN = 0;
  public static final int TYPE_GROUP = 1;

  private static String _PRM_NAME = "group_name";
  private static String _PRM_DESCRIPTION = "group_description";
  private static String _PRM_TYPE = "group_type";
  private static String _PRM_PARENT_ID = "group_parent_id";
  private static String _PRM_HOME_PAGE = "group_home_page_id";

  private static String _PRM_COMMIT = "group_commit";
  private static String _PRM_CANCEL = "group_cancel";




  private String _groupName = null;
  private String _groupDescription = null;
  private String _groupType = null;
  private String _groupHomePage = null;
  private int _groupParentID = 0;
  private int _groupParentType = 0;

  private String _groupCommit = null;
  private String _groupCancel = null;




  public String getName(){
    return _groupName;
  }

  public String getDescription(){
    return _groupDescription;
  }

  public String getGroupType(){
    return _groupType;
  }

  public int getParentID(){
    return _groupParentID;
  }

  public int getParentType(){
    return _groupParentType;
  }

  public boolean doCommit(){
    return _groupCommit != null;
  }

  public boolean doCancel(){
    return _groupCancel != null;
  }
  public int getHomePageID(){
  	if ( _groupHomePage != null && _groupHomePage.length() > 0 ) {
  		return Integer.parseInt(_groupHomePage);
  	}
  	return -1;
  }



  /**
   * @return Name of the InterfaceObject handling the group's name
   */
  public String getIONameForName(){
    return _PRM_NAME;
  }

  /**
   * @return Name of the InterfaceObject handling the group's description
   */
  public String getIONameForDescription(){
    return _PRM_DESCRIPTION;
  }
  public String getIONameForGroupType(){
    return _PRM_TYPE;
  }
  public String getIONameForParentID(){
    return _PRM_PARENT_ID;
  }
  public String getIONameForCommit(){
    return _PRM_COMMIT;
  }
  public String getIONameForCancel(){
    return _PRM_CANCEL;
  }
  public String getIONameForHomePage(){
    return _PRM_HOME_PAGE;
  }


  public boolean initializeEvent(IWContext iwc) {

    _groupName = iwc.getParameter(getIONameForName());
    _groupDescription = iwc.getParameter(getIONameForDescription());
    _groupType = iwc.getParameter(getIONameForGroupType());
    _groupHomePage = iwc.getParameter(getIONameForHomePage());
    String groupParentTypeAndID = iwc.getParameter(getIONameForParentID());

    try {
      int indexOf = groupParentTypeAndID.indexOf("_");
      _groupParentType = Integer.parseInt(groupParentTypeAndID.substring(0,indexOf));
      _groupParentID = Integer.parseInt(groupParentTypeAndID.substring(indexOf+1,groupParentTypeAndID.length()));
    }
    catch (Exception ex) {
      System.err.println("["+this+"]: > ");
      System.err.println(ex+": "+ex.getMessage());
      return false;
    }

    _groupCommit = iwc.getParameter(getIONameForCommit());
    _groupCancel = iwc.getParameter(getIONameForCancel());

    if(_groupCommit == null){
      _groupCommit = iwc.getParameter(getIONameForCommit()+".x");
    }

    if(_groupCancel == null){
      _groupCancel = iwc.getParameter(getIONameForCancel()+".x");
    }

    return true;
  }
}