package com.idega.user.presentation;

import java.util.Arrays;
import java.util.Map;

import javax.swing.event.ChangeEvent;

import com.idega.block.entity.event.EntityBrowserEvent;
import com.idega.builder.data.IBDomain;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.presentation.IWContext;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.user.block.search.event.UserSearchEvent;
import com.idega.user.data.Group;
import com.idega.user.event.SelectDomainEvent;
import com.idega.user.event.SelectGroupEvent;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class BasicUserOverviewPS extends IWControlFramePresentationState implements IWActionListener {


//  String color1 = "00FF00";
//  String color2 = "FF0000";
//  String color = color1;

  protected Group parentGroupOfSelection = null;
	protected IBDomain parentDomainOfSelection = null;

	protected Group _selectedGroup = null;
	protected IBDomain _selectedDomain = null;
  
  private Map resultOfMovingUsers = null;
  private int targetGroupId;

  public BasicUserOverviewPS() {
  }
  
  public Map getResultOfMovingUsers() {
    return resultOfMovingUsers;
  }
  
  public int getTargetGroupId() {
    return targetGroupId;
  }

  public Group getSelectedGroup(){
    return _selectedGroup;
  }

  public IBDomain getSelectedDomain(){
    return _selectedDomain;
  }

  public void reset(){
    super.reset();
    _selectedGroup = null;
    _selectedDomain = null;
  }


//  public String getColor(){
//    return color;
//  }



  public void actionPerformed(IWPresentationEvent e)throws IWException{
    
    if (e instanceof UserSearchEvent) {
      _selectedGroup = null;
    }

    if(e instanceof ResetPresentationEvent){
      resultOfMovingUsers = null;
      this.reset();
      this.fireStateChanged();
    }

    if(e instanceof SelectGroupEvent){
      _selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
      _selectedDomain = null;
      parentGroupOfSelection = ((SelectGroupEvent)e).getParentGroupOfSelection();
      parentDomainOfSelection = ((SelectGroupEvent)e).getParentDomainOfSelection();
      resultOfMovingUsers = null;
      this.fireStateChanged();
    }

    if(e instanceof SelectDomainEvent){
      _selectedDomain = ((SelectDomainEvent)e).getSelectedDomain();
      _selectedGroup = null;
      resultOfMovingUsers = null;
      this.fireStateChanged();
    }


    if (e instanceof EntityBrowserEvent)  {
      IWContext mainIwc = e.getIWContext();
      String[] userIds;
      if (mainIwc.isParameterSet(BasicUserOverview.DELETE_USERS_KEY) &&
          mainIwc.isParameterSet(BasicUserOverview.SELECTED_USERS_KEY)) {
        userIds = mainIwc.getParameterValues(BasicUserOverview.SELECTED_USERS_KEY);
        // delete users (if something has been chosen)
        BasicUserOverview.removeUsers(Arrays.asList(userIds), _selectedGroup, mainIwc); 
      }
    }
    if (e instanceof EntityBrowserEvent)  {
      IWContext mainIwc = e.getIWContext();
      String[] userIds;
      if (mainIwc.isParameterSet(BasicUserOverview.MOVE_USERS_KEY) &&
        mainIwc.isParameterSet(BasicUserOverview.SELECTED_USERS_KEY) &&
        mainIwc.isParameterSet(BasicUserOverview.SELECTED_TARGET_GROUP_KEY)) {
        userIds = mainIwc.getParameterValues(BasicUserOverview.SELECTED_USERS_KEY);
        int targetGroupId = Integer.parseInt(mainIwc.getParameter(BasicUserOverview.SELECTED_TARGET_GROUP_KEY));
        // move users to a group
        resultOfMovingUsers = BasicUserOverview.moveUsers(Arrays.asList(userIds), _selectedGroup, targetGroupId, mainIwc);
        this.targetGroupId = targetGroupId; 
      }
    }  
    
    if (e instanceof EntityBrowserEvent && (MassMovingWindow.EVENT_NAME.equals( ((EntityBrowserEvent)e).getEventName() )))  {
      IWContext mainIwc = e.getIWContext();
      String[] groupIds;
      if (mainIwc.isParameterSet(MassMovingWindow.SELECTED_CHECKED_GROUPS_KEY) && 
          mainIwc.isParameterSet(MassMovingWindow.MOVE_SELECTED_GROUPS) ) {
        groupIds = mainIwc.getParameterValues(MassMovingWindow.SELECTED_CHECKED_GROUPS_KEY);
        // move users 
        resultOfMovingUsers = BasicUserOverview.moveContentOfGroups(Arrays.asList(groupIds), MassMovingWindow.GROUP_TYPE_CLUB_DIVISION, mainIwc);
        targetGroupId = -1;
        fireStateChanged();
      }
    }     
  }


	/**
	 * Returns the parentDomainOfSelection.
	 * @return IBDomain
	 */
	public IBDomain getParentDomainOfSelection() {
		return parentDomainOfSelection;
	}

  /**
  * Returns the parentGroupOfSelection. 
  * @return Group
  */
  public Group getParentGroupOfSelection() {
	  return parentGroupOfSelection;
  }

  public void stateChanged(ChangeEvent e) {
    Object object = e.getSource();
    if (object instanceof DeleteGroupConfirmWindowPS) {
      // selected group was successfully(!) removed 
      // set selected group to null
      _selectedGroup = null;
    }
  }

}