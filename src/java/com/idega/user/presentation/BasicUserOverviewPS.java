package com.idega.user.presentation;

import java.util.Arrays;

import javax.swing.event.ChangeEvent;

import com.idega.block.entity.event.EntityBrowserEvent;
import com.idega.builder.data.IBDomain;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.presentation.IWContext;
import com.idega.presentation.event.ResetPresentationEvent;
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

 

  public BasicUserOverviewPS() {

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

    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }

    if(e instanceof SelectGroupEvent){
      _selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
      _selectedDomain = null;
      parentGroupOfSelection = ((SelectGroupEvent)e).getParentGroupOfSelection();
      parentDomainOfSelection = ((SelectGroupEvent)e).getParentDomainOfSelection();
      this.fireStateChanged();
    }

    if(e instanceof SelectDomainEvent){
      _selectedDomain = ((SelectDomainEvent)e).getSelectedDomain();
      _selectedGroup = null;
      this.fireStateChanged();
    }


    if (e instanceof EntityBrowserEvent)  {
      IWContext mainIwc = e.getIWContext();
      String[] userIds;
      if (mainIwc.isParameterSet(BasicUserOverview.DELETE_USERS_KEY) &&
          mainIwc.isParameterSet(BasicUserOverview.PARAMETER_DELETE_USERS)) {
        userIds = mainIwc.getParameterValues(BasicUserOverview.PARAMETER_DELETE_USERS);
        // delete users (if something has been chosen)
        BasicUserOverview.removeUsers(Arrays.asList(userIds), _selectedGroup, mainIwc); 
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