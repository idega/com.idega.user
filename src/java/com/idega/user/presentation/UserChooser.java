package com.idega.user.presentation;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractChooser;

/**
 * @author gimmi
 */
public class UserChooser extends AbstractChooser {

	private int _userId = -1;

  public UserChooser(String chooserName) {
    addForm(false);
    setChooserParameter(chooserName);
  }

  public UserChooser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
    IWBundle iwb = iwc.getApplication().getBundle(BuilderLogic.IW_BUNDLE_IDENTIFIER);
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

}
