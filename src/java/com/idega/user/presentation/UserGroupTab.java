package com.idega.user.presentation;

import com.idega.presentation.TabbedPropertyPanel;
import com.idega.presentation.Table;
import com.idega.presentation.IWContext;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.GroupBusiness;
import com.idega.util.datastructures.Collectable;
import java.util.Hashtable;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public abstract class UserGroupTab extends Table implements Collectable {

 private int groupId = -1;
 private int selectedParentGroupId = -1;

  protected Text proxyText;

  //protected UserBusiness business;

  protected Hashtable fieldValues;
	private TabbedPropertyPanel panel;


  public UserGroupTab() {
    super();
    //business = new UserBusiness();
    init();
    this.setCellpadding(0);
    this.setCellspacing(0);
		this.setWidth(Table.HUNDRED_PERCENT); //changed from 370
    this.setStyleClass("main");
    fieldValues = new Hashtable();
    initializeFieldNames();
    initializeFields();
    initializeTexts();
    initializeFieldValues();
    lineUpFields();
  }

  public void init(){}
  public abstract void initializeFieldNames();
  public abstract void initializeFieldValues();
  public abstract void updateFieldsDisplayStatus();
  public abstract void initializeFields();
  public abstract void initializeTexts();
  public abstract void lineUpFields();

  public abstract boolean collect(IWContext iwc);
  public abstract boolean store(IWContext iwc);
  public abstract void initFieldContents();
  
//  public abstract String getLocalizedLabel(IWContext iwc);

	public void setPanel(TabbedPropertyPanel panel) {
		this.panel = panel;
	}
	
	public TabbedPropertyPanel getPanel() {
		return panel;
	}

  /** Sets group id and id of the selected parent group.
   * 
   * @param groupId
   * @param selectedParentGroupId
   */
  public void setGroupIds(int groupId, int selectedParentGroupId) {
    this.selectedParentGroupId = selectedParentGroupId;
    setGroupId(groupId);
  }

  public void setGroupId(int id){
    groupId = id;
    initFieldContents();
  }

  public int getGroupId(){
    return groupId;
  }
  
  public int getSelectedParentGroupId() {
    return selectedParentGroupId;
  }

  public UserBusiness getUserBusiness(IWApplicationContext iwc){
    UserBusiness business = null;
    if(business == null){
      try{
        business = (UserBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,UserBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
    }
    return business;
  }

  public GroupBusiness getGroupBusiness(IWApplicationContext iwc){
    GroupBusiness business = null;
    if(business == null){
      try{
        business = (GroupBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
    }
    return business;
  }


}
