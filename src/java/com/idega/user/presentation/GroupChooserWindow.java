package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.AbstractChooserWindow;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;

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
	private UserBusiness userBiz = null;
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

			if(iwc.isSuperAdmin()){
				GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
				viewer.setRootNode(node);
			}
			else{
				UserBusiness biz = getUserBusiness(iwc);
				Collection groups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
				Collection groupNodes = convertGroupCollectionToGroupNodeCollection(groups,iwc.getApplicationContext());
				viewer.setFirstLevelNodes(groupNodes.iterator());

}

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

	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBiz == null) {
			try {
				userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return userBiz;
	}
	
	public Collection convertGroupCollectionToGroupNodeCollection(Collection col, IWApplicationContext iwac){
		List list = new Vector();
		
		Iterator iter = col.iterator();
		while (iter.hasNext()) {
			Group group = (Group) iter.next();
			GroupTreeNode node = new GroupTreeNode(group,iwac);
			list.add(node);
		}
	

		return list;
	}
	
}
