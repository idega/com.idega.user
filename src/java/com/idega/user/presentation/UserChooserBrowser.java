/*
 * Title:        
 * Copyright:    Idega Software Copyright (c) 2003
 * Company:      Idega Software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Mar 31, 2003
 * 
 */
package com.idega.user.presentation;

import com.idega.builder.business.BuilderConstants;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractChooser;


public class UserChooserBrowser extends AbstractChooser {
	
	private String imgName = "open.gif";
	private boolean isUserBundle = false;
	
  public UserChooserBrowser(String chooserName) {
    addForm(false);
    setChooserParameter(chooserName);
  }

  public UserChooserBrowser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
    empty();
    IWBundle iwb = null;
    if(isUserBundle) {
    		iwb = iwc.getIWMainApplication().getBundle("com.idega.user");
    }
    else{
    		iwb = iwc.getIWMainApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
    }
    setChooseButtonImage(iwb.getImage(imgName,"Choose"));
  }

  public Class getChooserWindowClass() {
    return UserChooserBrowserWindow.class;
  }

	public void setSelectedUser(String userId, String userName) {
		super.setChooserValue(userName,userId);
	}
	public void setImageName(String imgName) {
		this.imgName = imgName;
	}
	public void setUserBundle(boolean isUserBundle) {
		this.isUserBundle = isUserBundle;
	}


 /* public PresentationObject getPresentationObject(IWContext iwc) {
    Link link = new Link("tryrtt");
    
    
    //setDisabled(disabled);

    if (_style != null) {
      link.setAttribute("style",_style);
    }

    if(_stringDisplay != null){
      link.setName(_stringDisplay);
    }

    return link;
  }*/

  
}
