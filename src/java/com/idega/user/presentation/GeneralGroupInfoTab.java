package com.idega.user.presentation;

import com.idega.presentation.ui.IFrame;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Window;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;

import java.util.Iterator;
import java.util.Enumeration;
import com.idega.util.Disposable;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GeneralGroupInfoTab extends UserGroupTab implements Disposable{


  private TextInput nameField;
  private TextArea descriptionField;

  private Text nameText;
  private Text descriptionText;

  private String nameFieldName;
  private String descriptionFieldName;

  private Link addLink;
  private IFrame memberofFrame;
  public static final String PARAMETER_GROUP_ID = "ic_group_id";
  public static final String SESSIONADDRESS_GROUPS_DIRECTLY_RELATED = "ic_group_ic_group_direct_GGIT";
  public static final String SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED = "ic_group_ic_group_not_direct_GGIT";

  protected Text memberof;

  public GeneralGroupInfoTab() {
    super();
    this.setName("General");
  }

  public void initFieldContents() {
    addLink.setWindowToOpen(GroupGroupSetter.class);
    addLink.addParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID,this.getGroupId());

     try{
       
      Group group = (Group)(((GroupHome)com.idega.data.IDOLookup.getHome(Group.class)).findByPrimaryKey(new Integer(getGroupId())));

      fieldValues.put(this.nameFieldName,(group.getName() != null) ? group.getName():"" );
      fieldValues.put(this.descriptionFieldName,(group.getDescription() != null) ? group.getDescription():"" );
      this.updateFieldsDisplayStatus();

    }catch(Exception e){
      System.err.println("GeneralGroupInfoTab error initFieldContents, GroupId : " + getGroupId());
    }


  }
  public void updateFieldsDisplayStatus() {
    nameField.setContent((String)fieldValues.get(this.nameFieldName));

    descriptionField.setContent((String)fieldValues.get(this.descriptionFieldName));
  }
  public void initializeFields() {


    nameField = new TextInput(nameFieldName);
    nameField.setLength(26);

    descriptionField = new TextArea(descriptionFieldName);
    descriptionField.setHeight(5);
    descriptionField.setWidth(43);
    descriptionField.setWrap(true);

    memberofFrame = new IFrame("ic_user_memberof_ic_group",GroupList.class);
    memberofFrame.setHeight(150);
    memberofFrame.setWidth(367);
    memberofFrame.setScrolling(IFrame.SCROLLING_YES);

    addLink = new Link("  Add/Remove  ");

  }
  public void initializeTexts() {

    nameText = this.getTextObject();
    nameText.setText("Name:");

    descriptionText = getTextObject();
    descriptionText.setText("Description:");

    memberof = this.getTextObject();
    memberof.setText("Member of:");


  }
  public boolean store(IWContext iwc) {
    try{
      if(getGroupId() > -1){

        Group group = this.getGroupBusiness(iwc).getGroupByGroupID(getGroupId());
        //Group group = ((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(getGroupId());
        group.setName((String)fieldValues.get(this.nameFieldName));
        group.setDescription((String)fieldValues.get(this.descriptionFieldName));
        group.store();

      }
    }catch(Exception e){
      //return false;
      e.printStackTrace(System.err);
      throw new RuntimeException("update group exception");
    }
    return true;
  }
  public void lineUpFields() {
    this.resize(1,5);
    this.setCellpadding(0);
    this.setCellspacing(0);

    Table nameTable = new Table(2,1);
    nameTable.setCellpadding(0);
    nameTable.setCellspacing(0);
    nameTable.setWidth(1,1,"50");
    nameTable.add(this.nameText,1,1);
    nameTable.add(this.nameField,2,1);
    this.add(nameTable,1,1);

    Table descriptionTable = new Table(1,2);
    descriptionTable.setCellpadding(0);
    descriptionTable.setCellspacing(0);
    descriptionTable.setHeight(1,rowHeight);
    descriptionTable.add(descriptionText,1,1);
    descriptionTable.add(this.descriptionField,1,2);
    this.add(descriptionTable,1,2);

    this.add(memberof,1,3);
    this.add(memberofFrame,1,4);

    this.setHeight(3,"30");
    this.setHeight(1,super.rowHeight);
    this.setHeight(5,super.rowHeight);

    this.add(addLink,1,5);
  }

  public boolean collect(IWContext iwc) {
    if(iwc != null){

      String gname = iwc.getParameter(this.nameFieldName);
      String desc = iwc.getParameter(this.descriptionFieldName);

      if(gname != null){
        fieldValues.put(this.nameFieldName,gname);
      }

      if(desc != null){
        fieldValues.put(this.descriptionFieldName,desc);
      }

      this.updateFieldsDisplayStatus();

      return true;
    }
    return false;

  }
  public void initializeFieldNames() {
    descriptionFieldName = "UM_group_desc";
    nameFieldName = "UM_group_name";
  }
  public void initializeFieldValues() {
    fieldValues.put(this.nameFieldName,"");
    fieldValues.put(this.descriptionFieldName,"");

    this.updateFieldsDisplayStatus();
  }

  public void dispose(IWContext iwc){
    iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
    iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
  }

  public void main(IWContext iwc) throws Exception {
    Object obj =  this.getGroupBusiness(iwc).getParentGroups(this.getGroupId());
    if(obj != null){
      iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED,obj);
    }else{
      iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
    }

    Object ob = this.getGroupBusiness(iwc).getParentGroupsInDirect(this.getGroupId());
    if(ob != null){
      iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED,ob);
    }else{
      iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
    }
  }


 


 



}
