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
import com.idega.util.CoreConstants;


public class UserChooserBrowser extends AbstractChooser {
	
	private String imgName = "magnifyingglass.gif";
	private boolean isUserBundle = false;
	
	public UserChooserBrowser(String chooserName) {
		this(chooserName, true);
	}
	
  public UserChooserBrowser(String chooserName, boolean useOldLogic) {
	  super(useOldLogic);
    addForm(false);
    setChooserParameter(chooserName);
  }

  public UserChooserBrowser(String chooserName, String style, boolean useOldLogic) {
    this(chooserName, useOldLogic);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
    empty();
    IWBundle iwb = null;
    if (this.isUserBundle) {
    	iwb = iwc.getIWMainApplication().getBundle(CoreConstants.IW_USER_BUNDLE_IDENTIFIER);
    }
    else {
    	iwb = iwc.getIWMainApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
    }
    setChooseButtonImage(iwb.getImage(this.imgName,getResourceBundle(iwc).getLocalizedString("user_chooser.choose","Choose")));
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
