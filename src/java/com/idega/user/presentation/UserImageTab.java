package com.idega.user.presentation;

import java.rmi.RemoteException;

import com.idega.block.media.presentation.ImageInserter;
import com.idega.business.IBOLookup;
import com.idega.core.user.presentation.UserTab;
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

  private ImageInserter imageField;

  private String imageFieldName;

  private Text imageText;
  
  private User user = null;

  public UserImageTab() {
    super();
    this.setName("Image");
  }

  public UserImageTab(int userId){
    this();
    this.setUserID(userId);
  }

  public void initializeFieldNames(){
    imageFieldName = "STimage";
  }

  public void initializeFieldValues(){
    fieldValues.put(this.imageFieldName,"");

    this.updateFieldsDisplayStatus();
  }

  public void updateFieldsDisplayStatus(){
    int imageId = -1;
    try {
      imageId = Integer.parseInt((String)fieldValues.get(this.imageFieldName));
    }
    catch (NumberFormatException ex) {
      imageId = -1;
    }

    if ( imageId != -1 )
      imageField.setImageId(imageId);
  }

  public void initializeFields(){
    imageField = new ImageInserter(imageFieldName);
    imageField.setHasUseBox(false);
  }

  public void initializeTexts(){
    imageText = getTextObject();
    imageText.setText("Image"+":");
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
        
        String imageId = (String)fieldValues.get(this.imageFieldName);
        
        if( imageId!=null && !imageId.equals("-1") && !imageId.equals("") ){
	  			if( user == null ) user = getUserBusiness().getUser(this.getUserId());
	  			int id = Integer.parseInt(imageId);
	  			user.setSystemImageID(id);
        	user.store();
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

      fieldValues.put(this.imageFieldName,(user.getSystemImageID() != -1) ? Integer.toString(user.getSystemImageID()):"" );
      this.updateFieldsDisplayStatus();
    }
    catch(Exception e){
      System.err.println("UserImageTab error initFieldContents, userId : " + getUserId());
    }


  }

	private UserBusiness getUserBusiness() throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(this.getIWApplicationContext(), UserBusiness.class);	
	}

} // Class StaffInfoTab
