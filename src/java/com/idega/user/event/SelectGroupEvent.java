package com.idega.user.event;

import com.idega.user.data.Group;
import com.idega.builder.data.IBDomain;
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
  private Integer parentGroupOfSelection = null;
  private Integer parentDomainOfSelection = null;
  public static final String PRM_GROUP_ID = "usgr_id";
  public static final String PRM_PARENT_GROUP_ID = "parentgroup_id";
  public static final String PRM_PARENT_DOMAIN_ID = "parentdomain_id";

  public SelectGroupEvent() {
  }

  public void setGroupToSelect(Integer primaryKey){
    this.addParameter(PRM_GROUP_ID, primaryKey.toString());
  }

  public void setGroupToSelect(int nodeId){
    this.addParameter(PRM_GROUP_ID, nodeId);
  }

  public void setParentGroupOfSelection(int nodeId)  {
    this.addParameter(PRM_PARENT_GROUP_ID, nodeId);
  }
  
  public void setParentDomainOfSelection(int nodeId)  {
    this.addParameter(PRM_PARENT_DOMAIN_ID, nodeId);
  }
  
  public Group getParentGroupOfSelection(){
    if(parentGroupOfSelection != null){
      try {
        return (Group)IDOLookup.findByPrimaryKey(Group.class, parentGroupOfSelection);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }
  
  public IBDomain getParentDomainOfSelection(){
    if(parentDomainOfSelection != null){
      try {
        return (IBDomain)IDOLookup.findByPrimaryKey(IBDomain.class, parentDomainOfSelection);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
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
    
    if (iwc.isParameterSet(PRM_PARENT_GROUP_ID))
      parentGroupOfSelection = new Integer(iwc.getParameter(PRM_PARENT_GROUP_ID));
    if (iwc.isParameterSet(PRM_PARENT_DOMAIN_ID))
      parentDomainOfSelection = new Integer(iwc.getParameter(PRM_PARENT_DOMAIN_ID));

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