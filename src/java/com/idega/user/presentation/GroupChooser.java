package com.idega.user.presentation;

import com.idega.core.builder.business.BuilderConstants;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.ui.AbstractChooser;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GroupChooser extends AbstractChooser {
  private String style;
  private Image chooserButtonImage = null;

  public GroupChooser(String chooserName) {
    addForm(false);
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
			IWBundle iwb = iwc.getApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER);
			setChooseButtonImage(iwb.getImage("open.gif","Choose"));
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
  

}
