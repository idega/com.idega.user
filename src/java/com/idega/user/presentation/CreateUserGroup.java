package com.idega.user.presentation;

import java.util.Vector;

import com.idega.core.accesscontrol.data.PermissionGroup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.FramePane;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.RadioGroup;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.Window;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class CreateUserGroup extends Window {

  private Text groupNameText;
  private Text descriptionText;
  private Text groupTypeText;

  private TextInput groupNameField;
  private TextArea descriptionField;
  private RadioGroup groupTypeField;

  private SubmitButton okButton;
  private SubmitButton cancelButton;

  private Vector groupType;

  private Form myForm;

  public static String okButtonParameterValue = "ok";
  public static String cancelButtonParameterValue = "cancel";
  public static String submitButtonParameterName = "submit";

  public static String groupNameFieldParameterName = "groupName";
  public static String descriptionFieldParameterName = "description";
  public static String groupTypeFieldParameterName = "group_type";

  //private UserBusiness business;

  private String rowHeight = "37";

  public CreateUserGroup() {
    super();
    groupType = new  Vector();
    this.setName("idegaWeb Builder - Stofna Hóp");
    this.setHeight(340);
    this.setWidth(390);
    this.setBackgroundColor("#d4d0c8");
    myForm = new Form();
    this.add(myForm);
    //business = new UserBusiness();
    initializeTexts();
    initializeFields();
    init();
    //lineUpElements();
  }

  protected void initializeTexts(){

    groupNameText = new Text("Group name:");
    descriptionText = new Text("Description : ");
    groupTypeText = new Text("Type: ");
  }

  protected void initializeFields(){
    groupNameField = new TextInput(groupNameFieldParameterName);
    groupNameField.setLength(20);

    descriptionField = new TextArea(descriptionFieldParameterName);
    descriptionField.setHeight(3);
    descriptionField.setWidth(20);

    groupTypeField = new RadioGroup(groupTypeFieldParameterName);
    groupTypeField.setWidth(1);

    okButton = new SubmitButton("     OK     ",submitButtonParameterName,okButtonParameterValue);
    cancelButton = new SubmitButton(" Cancel ",submitButtonParameterName,cancelButtonParameterValue);

  }

  public void init(){
    this.addGroupType(Group.class);
    this.addGroupType(PermissionGroup.class);
  }



  public void addGroupType(Class genricGroup){
    groupType.add(genricGroup);
  }


  //public void lineUpElements(){
  public void lineUpElements(IWContext iwc)throws Exception{

    Table frameTable = new Table(1,3);
    frameTable.setAlignment("center");
    frameTable.setVerticalAlignment("middle");
    frameTable.setCellpadding(0);
    frameTable.setCellspacing(0);

    // nameTable begin
    Table nameTable = new Table(1,4);
    nameTable.setCellpadding(0);
    nameTable.setCellspacing(0);
    nameTable.setHeight(1,rowHeight);
    nameTable.setHeight(2,rowHeight);

    nameTable.add(groupNameText,1,1);
    nameTable.add(groupNameField,1,2);
    nameTable.add(descriptionText,1,3);
    nameTable.add(descriptionField,1,4);
    // nameTable end

    // Property begin
    int size = groupType.size();
    if(size > 1){
      Table propertyTable = new Table(2,1);
      propertyTable.setCellpadding(0);
      propertyTable.setCellspacing(0);
      propertyTable.setHeight(1,rowHeight);

      FramePane frPane = new FramePane("Type");


      for (int i = 0; i < groupType.size(); i++){
        String value = getGroupBusiness(iwc).getGroupType((Class)groupType.get(i));
        //String value = ((Group)com.idega.user.data.GroupBMPBean.getStaticInstance((Class)groupType.get(i))).getGroupTypeValue();
        String text = value.substring(1);
        text = value.substring(0,1).toUpperCase() + text;

        if(i==0){
          groupTypeField.addRadioButton(value,new Text(text),true);
        }else{
          groupTypeField.addRadioButton(value,new Text(text));
        }
      }

      frPane.add(groupTypeField);
      frPane.setWidth(200);
      propertyTable.add(frPane,1,1);
      frameTable.add(propertyTable,1,2);
    }else if (size == 1){
      frameTable.add(new HiddenInput(((Group)com.idega.user.data.GroupBMPBean.getStaticInstance((Class)groupType.get(0))).getGroupTypeValue()));
    }else{
      frameTable.add(new HiddenInput(groupTypeFieldParameterName,com.idega.user.data.GroupBMPBean.getStaticInstance().getGroupTypeValue()));
    }
    // Property end



    // buttonTable begin
    Table buttonTable = new Table(3,1);
    buttonTable.setCellpadding(0);
    buttonTable.setCellspacing(0);
    buttonTable.setHeight(1,rowHeight);
    buttonTable.setWidth(2,"5");

    buttonTable.add(okButton,1,1);
    buttonTable.add(cancelButton,3,1);
    // buttonTable end


    frameTable.add(nameTable,1,1);


    frameTable.add(buttonTable,1,3);
    frameTable.setAlignment(1,3,"right");

    myForm.add(frameTable);

  }



  public void commitCreation(IWContext iwc) throws Exception{

    String name = iwc.getParameter(this.groupNameFieldParameterName);
    String description = iwc.getParameter(this.descriptionFieldParameterName);
    String type = iwc.getParameter(this.groupTypeFieldParameterName);

    if(type == null){
      throw new Exception("no group_type selected");
    }
    this.getGroupBusiness(iwc).createGroup(name,description,type);
  }


  public void main(IWContext iwc) throws Exception {
    lineUpElements(iwc);
    String submit = iwc.getParameter("submit");
    if(submit != null){
      if(submit.equals("ok")){
        this.commitCreation(iwc);
        this.close();
        this.setParentToReload();
      }else if(submit.equals("cancel")){
        this.close();
      }
    }
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

//  public UserGroupBusiness getUserGroupBusiness(IWApplicationContext iwc){
//    UserGroupBusiness business = null;
//    if(business == null){
//      try{
//        business = (UserGroupBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,UserGroupBusiness.class);
//      }
//      catch(java.rmi.RemoteException rme){
//        throw new RuntimeException(rme.getMessage());
//      }
//    }
//    return business;
//  }


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
