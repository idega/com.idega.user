package com.idega.user.presentation;

import com.idega.block.cal.presentation.AttendantChooser;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.ui.AbstractChooser;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class GroupChooser extends AbstractChooser implements AttendantChooser {
  private String style;
  private Image chooserButtonImage = null;
  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
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

  

}
