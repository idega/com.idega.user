package com.idega.user.presentation;

import java.util.Collection;

import com.idega.core.builder.business.BuilderConstants;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractChooser;

/**
 * @author gimmi
 */
public class UserChooser extends AbstractChooser {

	private int _userId = -1;
	private Collection userPks;
	private boolean useUserPks = false;

  public UserChooser(String chooserName) {
    addForm(false);
    setChooserParameter(chooserName);
  }

  public UserChooser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
  	if (useUserPks) {
	  	iwc.setSessionAttribute(UserChooserWindow.AVAILABLE_USER_PKS_SESSION_PARAMETER, userPks);
	  	iwc.setSessionAttribute(UserChooserWindow.USING_AVAILABLE_USER_PKS_SESSION_PARAMETER, "true");
  	}else {
  		iwc.removeSessionAttribute(UserChooserWindow.AVAILABLE_USER_PKS_SESSION_PARAMETER);
  		iwc.removeSessionAttribute(UserChooserWindow.USING_AVAILABLE_USER_PKS_SESSION_PARAMETER);	
  	}
    IWBundle iwb = iwc.getApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
    setChooseButtonImage(iwb.getImage("open.gif","Choose"));
  }


 public void setSelected(String userId){
    super.setChooserValue(userId,userId);
    super.setParameterValue("user_id",userId);
  }
  
 	/**
	 * @see com.idega.presentation.ui.AbstractChooser#getChooserWindowClass()
	 */
	public Class getChooserWindowClass() {
		return UserChooserWindow.class;
	}
	
	public void setValidUserPks(Collection userPks) {
		this.userPks = userPks;
		this.useUserPks = true;	
	}

}
