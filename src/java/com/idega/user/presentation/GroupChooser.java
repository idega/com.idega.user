package com.idega.user.presentation;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.presentation.ui.Form;
import com.idega.user.business.GroupTreeNode;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class GroupChooser extends AbstractChooser {
  private String style;
  private Image chooserButtonImage = null;
  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
private boolean submitForm;
  
  public GroupChooser() {
  	addForm(false);
  }

  public GroupChooser(String chooserName) {
    this();
    setChooserParameter(chooserName);
  }

  public GroupChooser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }
  public GroupChooser(String chooserName, String style, Image chooserButtonImage) {
  	this(chooserName);
  	setInputStyle(style);
  	setChooseButtonImage(chooserButtonImage);
  }

  public void main(IWContext iwc){
    empty();
    if(chooserButtonImage == null) {
			IWBundle iwb = iwc.getIWMainApplication().getBundle(IW_BUNDLE_IDENTIFIER); //BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
			setChooseButtonImage(iwb.getImage("magnifyingglass.gif","Choose"));
    }
    	
  }

  public Class getChooserWindowClass() {
    return GroupChooserWindow.class;
  }
//  public void setChooseButtonImage(Image image) {
//  	chooserButtonImage = image;
//  }

  public void setSelectedNode(GroupTreeNode groupNode) {
      super.setChooserValue(groupNode.getNodeName(),groupNode.getNodeType()+"_"+groupNode.getNodeID());
  }
  
	public void setSelectedGroup(String userId, String userName) {
		super.setChooserValue(userName,userId);
	}
  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
  

  

	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooser#addParametersToForm(com.idega.presentation.ui.Form)
	 */
	protected void addParametersToForm(Form form) {
		if(submitForm){
			form.addParameter(GroupChooserWindow.SUBMIT_PARENT_FORM_AFTER_CHANGE, "true");
		}
	}
	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooser#addParametersToLink(com.idega.presentation.text.Link)
	 */
	protected void addParametersToLink(Link link) {
		if(submitForm){
			link.addParameter(GroupChooserWindow.SUBMIT_PARENT_FORM_AFTER_CHANGE, "true");
		}
	}
	
	public void setToSubmitParentFormOnChange(){
		submitForm = true;
	}
}
