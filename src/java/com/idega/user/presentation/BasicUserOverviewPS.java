package com.idega.user.presentation;

import com.idega.user.event.*;
import com.idega.block.entity.event.EntityBrowserEvent;
import com.idega.builder.data.IBDomain;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.user.data.Group;
import com.idega.presentation.event.TreeViewerEvent;
import com.idega.idegaweb.browser.event.IWBrowseEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import javax.swing.event.ChangeListener;
import com.idega.idegaweb.IWException;
import com.idega.event.*;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class BasicUserOverviewPS extends IWPresentationStateImpl implements IWActionListener {


//  String color1 = "00FF00";
//  String color2 = "FF0000";
//  String color = color1;

  private Group parentGroupOfSelection = null;
  private IBDomain parentDomainOfSelection = null;

  Group _selectedGroup = null;
  IBDomain _selectedDomain = null;

  private int _selectedPartitionDefaultValue = 0;
  private int _partitionSizeDefaultValue = 30;
  private int _firstPartitionIndexDefaultValue = 0;

  private int _selectedPartition = _selectedPartitionDefaultValue;
  private int _partitionSize = _partitionSizeDefaultValue;
  private int _firstPartitionIndex = _firstPartitionIndexDefaultValue;

  public BasicUserOverviewPS() {

  }
  


  public int getSelectedPartition(){
    return _selectedPartition;
  }

  public int getPartitionSize(){
    return _partitionSize;
  }

  public int getFirstPartitionIndex(){
    return _firstPartitionIndex;
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
    _selectedPartition = _selectedPartitionDefaultValue;
    _partitionSize = _partitionSizeDefaultValue;
    _firstPartitionIndex = _firstPartitionIndexDefaultValue;
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
      _selectedPartition = _selectedPartitionDefaultValue;
      _partitionSize = _partitionSizeDefaultValue;
      _firstPartitionIndex = _firstPartitionIndexDefaultValue;
      parentGroupOfSelection = ((SelectGroupEvent)e).getParentGroupOfSelection();
      parentDomainOfSelection = ((SelectGroupEvent)e).getParentDomainOfSelection();
      this.fireStateChanged();
    }

    if(e instanceof SelectDomainEvent){
      _selectedDomain = ((SelectDomainEvent)e).getSelectedDomain();
      _selectedGroup = null;
      _selectedPartition = _selectedPartitionDefaultValue;
      _partitionSize = _partitionSizeDefaultValue;
      _firstPartitionIndex = _firstPartitionIndexDefaultValue;
      this.fireStateChanged();
    }

    if(e instanceof PartitionSelectEvent){
      _selectedPartition = ((PartitionSelectEvent)e).getSelectedPartition();
      _partitionSize = ((PartitionSelectEvent)e).getPartitionSize();
      _firstPartitionIndex = ((PartitionSelectEvent)e).getFirstPartitionIndex();
      this.fireStateChanged();
    }
    
    if (e instanceof EntityBrowserEvent)  {
      IWContext mainIwc = e.getIWContext();
      String[] userIds;
      if (mainIwc.isParameterSet(BasicUserOverview.DELETE_USERS_KEY) &&
          mainIwc.isParameterSet(BasicUserOverview.PARAMETER_DELETE_USERS)) {
        userIds = mainIwc.getParameterValues(BasicUserOverview.PARAMETER_DELETE_USERS);
        // delete users (if something has been chosen)
        List notDeletedUsers = BasicUserOverview.deleteUsers(Arrays.asList(userIds),mainIwc); 
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

}