package com.idega.user.presentation;

import com.idega.user.presentation.GroupTreeNode;
import com.idega.user.presentation.GroupTreeView;
import com.idega.idegaweb.*;
import com.idega.presentation.text.*;
import com.idega.presentation.Table;
import com.idega.presentation.ui.AbstractChooserWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.TreeViewer;
import com.idega.builder.data.IBDomain;
import com.idega.builder.business.PageTreeNode;
import com.idega.builder.business.BuilderLogic;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GroupChooserWindow extends AbstractChooserWindow {

  private static final int _width = 280;
  private static final int _height = 400;
  private static final String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";

  /**
   *
   */
  public GroupChooserWindow() {
    setTitle("Group chooser");
    setWidth(_width);
    setHeight(_height);
    setCellpadding(5);
    setScrollbar(true);
    this.getLocation().setApplicationClass(this.getClass());
    this.getLocation().isInPopUpWindow(true);
  }



  /**
   *
   */
  public void displaySelection(IWContext iwc) {
    IWResourceBundle iwrb = iwc.getApplication().getBundle(BuilderLogic.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
    addTitle(iwrb.getLocalizedString("select_group","Select group"),IWConstants.BUILDER_FONT_STYLE_TITLE);
    setStyles();

    Text text = new Text(iwrb.getLocalizedString("select_group","Select group")+":");
      text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
    add(text);

    try {
//      TreeViewer viewer = com.idega.builder.business.IBPageHelper.getInstance().getPageTreeViewer(iwc);

      GroupTreeView viewer = new GroupTreeView();

      GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());

      viewer.setRootNode(node);

      viewer.setLocation((IWLocation)this.getLocation().clone());
      viewer.getLocation().setSubID(1);

      add(viewer);

      viewer.setToMaintainParameter(SCRIPT_PREFIX_PARAMETER,iwc);
      viewer.setToMaintainParameter(SCRIPT_SUFFIX_PARAMETER,iwc);
      viewer.setToMaintainParameter(DISPLAYSTRING_PARAMETER_NAME,iwc);
      viewer.setToMaintainParameter(VALUE_PARAMETER_NAME,iwc);
      viewer.setDefaultOpenLevel(10);

      Link link = new Link();
	link.setNoTextObject(true);
      viewer.setLinkPrototype(link);
      viewer.setTreeStyle(_linkStyle);
      viewer.setToUseOnClick();
      //sets the hidden input and textinput of the choosing page
      viewer.setOnClick(SELECT_FUNCTION_NAME+"("+viewer.ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME+","+viewer.ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME+")");


//
//    viewer.setNodeActionParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
//
    viewer.setLinkPrototype(link);


    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private void setStyles() {
    String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";
    String _linkHoverStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#FF8008;text-decoration:none;";
    if ( getParentPage() != null ) {
      getParentPage().setStyleDefinition("A",_linkStyle);
      //getParentPage().setStyleDefinition("A."+STYLE_NAME+":visited",_linkStyle);
      //getParentPage().setStyleDefinition("A."+STYLE_NAME+":active",_linkStyle);
      getParentPage().setStyleDefinition("A:hover",_linkHoverStyle);
    }
  }

}
