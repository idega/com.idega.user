package com.idega.user.presentation;

import java.rmi.RemoteException;

import com.idega.block.media.presentation.ImageInserter;
import com.idega.business.IBOLookup;
import com.idega.user.presentation.UserTab;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;


/**
 * Title:        A simple tab to add images to users
 * Copyright:    Idega Software Copyright (c) 2001
 * Company:      Idega Software
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */

public class UserImageTab extends UserTab{

/*	this is the order methods are executed
    initializeFieldNames();
    initializeFields();
    initializeTexts();
    initializeFieldValues();
    lineUpFields();
 */   
    
  private ImageInserter imageField;
  private String imageFieldName;
  private Text imageText;
  private UserBusiness biz;
  
  private User user = null;
  private int systemImageId = -1;

  public UserImageTab() {
    super();
    setName("Image");
  }

  public UserImageTab(int userId){
    this();
    setUserID(userId);
  }

  public void initializeFieldNames(){
    imageFieldName = "userSystemImageId";
  }

  public void initializeFields(){
    imageField = new ImageInserter(imageFieldName);
    imageField.setHasUseBox(false);
  }
  
  public void initializeTexts(){
    imageText = getTextObject();
    imageText.setText("Image"+":");
  }
  
  public void initializeFieldValues(){
    fieldValues.put(this.imageFieldName,"");

    this.updateFieldsDisplayStatus();
  }

  public void lineUpFields(){
    this.resize(1,1);

    Table imageTable = new Table(1,2);
    imageTable.setWidth("100%");
    imageTable.setCellpadding(0);
    imageTable.setCellspacing(0);

    imageTable.add(imageText,1,1);
    imageTable.add(this.imageField,1,2);
    this.add(imageTable,1,1);
  }
  
  public void updateFieldsDisplayStatus(){

    if ( systemImageId != -1 ){
      imageField.setImageId(systemImageId);       
    }
  }

  public boolean collect(IWContext iwc){
    if(iwc != null){

      String image = iwc.getParameter(this.imageFieldName);

      if(image != null){
        fieldValues.put(this.imageFieldName,image);
      }

      return true;
    }
    return false;
  }

  public boolean store(IWContext iwc){
    try{
      if(getUserId() > -1){
        
        String image = (String)fieldValues.get(imageFieldName);
        
        if( (image!=null) && (!image.equals("-1")) && (!image.equals("")) && (!image.equals("0")) ){
      		if( user == null ) user = getUserBusiness().getUser(this.getUserId());
	  			int id = Integer.parseInt(image);
	  			user.setSystemImageID(id);
        	user.store();
        
        	updateFieldsDisplayStatus();
        
        
        }
        
        
                
      }
    }
    catch(Exception e){
      e.printStackTrace(System.err);
      throw new RuntimeException("update user exception");
    }
    return true;
  }


  public void initFieldContents(){

    try{
      if( user == null ) user = getUserBusiness().getUser(this.getUserId());
			
			systemImageId = getSelectedImageId(user);
			
			System.out.println("IMAGE ID = "+systemImageId);
			
			if( systemImageId!=-1 ){
      	fieldValues.put(this.imageFieldName,Integer.toString(systemImageId));
			}
      
      this.updateFieldsDisplayStatus();
    }
    catch(Exception e){
      System.err.println("UserImageTab error initFieldContents, userId : " + getUserId());
    }


  }

	private UserBusiness getUserBusiness() throws RemoteException {
		if(biz==null) biz = (UserBusiness) IBOLookup.getServiceInstance(this.getIWApplicationContext(), UserBusiness.class);	
		return biz;
	}
	
	private void setSelectedImageId(){
		try {
			String image = (String)fieldValues.get(this.imageFieldName);
			if( (image!=null) && (!image.equals("-1")) && (!image.equals("")) && (!image.equals("0")) ){
      	systemImageId = Integer.parseInt(image);
			}
    } 
    catch (Exception ex) {
    	ex.printStackTrace(System.err);
    }
    
    System.out.println("IMAGE ID = "+systemImageId);
	}
	
	private int getSelectedImageId(User user){
		try {
			int tempImageId = user.getSystemImageID();
 			if( (systemImageId==-1) && (tempImageId!=-1) ) systemImageId=tempImageId;
    }
    catch (Exception ex) {
    	ex.printStackTrace(System.err);
    }
    
    return systemImageId;
	}

	

} 
