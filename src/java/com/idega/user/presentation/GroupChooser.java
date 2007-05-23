package com.idega.user.presentation;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.presentation.ui.Form;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserConstants;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class GroupChooser extends AbstractChooser {
	
  private Image chooserButtonImage = null;
  private boolean submitForm;
  
  public GroupChooser() {
  	this(false);
  }
  
  public GroupChooser(boolean useOldLogic) {
	  super(useOldLogic);
	  addForm(false);
  }
  
  public GroupChooser(String chooserName) {
	  this(chooserName, true);
  }
  
  public GroupChooser(String chooserName, boolean useOldLogic) {
    this(useOldLogic);
    setChooserParameter(chooserName);
  }

  public GroupChooser(String chooserName, String style) {
    this(chooserName, true);
    setInputStyle(style);
  }
  public GroupChooser(String chooserName, String style, Image chooserButtonImage) {
  	this(chooserName, true);
  	setInputStyle(style);
  	setChooseButtonImage(chooserButtonImage);
  }

  public void main(IWContext iwc){
    empty();
    if (this.chooserButtonImage == null) {
		IWBundle iwb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		setChooseButtonImage(iwb.getImage("magnifyingglass.gif","Choose"));
    }
    	
  }

  public Class getChooserWindowClass() {
	  if (isUseOldLogic()) {
		  return GroupChooserWindow.class;
	  }
	  return GroupChooserBlock.class;
  }

  public void setSelectedNode(GroupTreeNode groupNode) {
      super.setChooserValue(groupNode.getNodeName(),groupNode.getNodeType()+"_"+groupNode.getNodeID());
  }
  
  public void setSelectedGroup(String userId, String userName) {
	  super.setChooserValue(userName,userId);
  }
  
  public String getBundleIdentifier(){
    return UserConstants.IW_BUNDLE_IDENTIFIER;
  }

	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooser#addParametersToForm(com.idega.presentation.ui.Form)
	 */
	protected void addParametersToForm(Form form) {
		if(this.submitForm){
			form.addParameter(GroupChooserWindow.SUBMIT_PARENT_FORM_AFTER_CHANGE, "true");
		}
	}
	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooser#addParametersToLink(com.idega.presentation.text.Link)
	 */
	protected void addParametersToLink(Link link) {
		if(this.submitForm){
			link.addParameter(GroupChooserWindow.SUBMIT_PARENT_FORM_AFTER_CHANGE, "true");
		}
	}
	
	public void setToSubmitParentFormOnChange(){
		this.submitForm = true;
	}
}
