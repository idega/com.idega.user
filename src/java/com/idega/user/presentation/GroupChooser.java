package com.idega.user.presentation;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
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

  public GroupChooser(String chooserName) {
    addForm(false);
    setChooserParameter(chooserName);
  }

  public GroupChooser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
    empty();
    IWBundle iwb = iwc.getApplication().getBundle(BuilderLogic.IW_BUNDLE_IDENTIFIER);
    setChooseButtonImage(iwb.getImage("open.gif","Choose"));
  }

  public Class getChooserWindowClass() {
    return GroupChooserWindow.class;
  }

  public void setSelectedNode(GroupTreeNode groupNode) {
      super.setChooserValue(groupNode.getNodeName(),groupNode.getNodeType()+"_"+groupNode.getNodeID());
  }
  
	public void setSelectedGroup(String userId, String userName) {
		super.setChooserValue(userName,userId);
	}
  

}
