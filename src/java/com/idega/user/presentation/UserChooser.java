package com.idega.user.presentation;

import java.util.Collection;

import com.idega.builder.business.BuilderConstants;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.user.data.User;

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
    IWBundle iwb = iwc.getIWMainApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
    setChooseButtonImage(iwb.getImage("open.gif",iwb.getResourceBundle(iwc).getLocalizedString("choose", "Choose")));
  }


 public void setSelected(String userId){
    super.setChooserValue(userId,userId);
    super.setParameterValue("user_id",userId);
  }
 
 public void setSelected(User user) {
 		super.setChooserValue(user.getName(), user.getPrimaryKey().toString());
 		super.setParameterValue("user_id",user.getPrimaryKey().toString());
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
