package com.idega.user.app;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupTreeNode;
import com.idega.user.presentation.GroupTreeView;
import com.idega.util.IWColor;

import javax.swing.event.ChangeListener;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationControlArea extends Page implements IWBrowserView, StatefullPresentation {

  private IWBundle iwb;
  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;

  private GroupTreeView groupTree = new GroupTreeView();
	private UserBusiness userBiz = null;


  public UserApplicationControlArea() {
  	this.setAllMargins(0);
    _stateHandler = new StatefullPresentationImplHandler();
    _stateHandler.setPresentationStateClass(UserApplicationControlAreaPS.class);
  }

  public void setControlEventModel(IWPresentationEvent model){
//    System.out.print("UserApplicationControlArea: setControlEventModel(IWPresentationEvent model)");
    _contolEvent = model;

    groupTree.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
//    System.out.print("UserApplicationControlArea: setControlTarget(String controlTarget)");
    _controlTarget = controlTarget;
    groupTree.setControlTarget(controlTarget);
  }

  public Class getPresentationStateClass(){
    return _stateHandler.getPresentationStateClass();
  }

  public IWPresentationState getPresentationState(IWUserContext iwuc){
    return _stateHandler.getPresentationState(this,iwuc);
  }

  public StatefullPresentationImplHandler getStateHandler(){
    return _stateHandler;
  }

  public String getBundleIdentifier(){
    return "com.idega.user";
  }



  public void initializeInMain(IWContext iwc){

    iwb = getBundle(iwc);

    IWLocation location = (IWLocation)this.getLocation().clone();
    location.setSubID(1);
    groupTree.setLocation(location,iwc);
    groupTree.setArtificialCompoundId(getCompoundId(),iwc);


//    IWPresentationState gtState = groupTree.getPresentationState(iwc);
//    if(gtState instanceof IWActionListener){
//      groupTree.addIWActionListener((IWActionListener)gtState);
//    }
//
//
//
//    EventListenerList list = this.getEventListenerList(iwc);
//    IWActionListener[] listeners = (IWActionListener[])list.getListeners(IWActionListener.class);
//    if(listeners != null ){
//      for (int i = 0; i < listeners.length; i++) {
//        groupTree.addIWActionListener(listeners[i]);
//      }
//
//    }




//    UserApplicationControlAreaPS ps = (UserApplicationControlAreaPS)this.getPresentationState(iwc);
//    ps.addInnerListener(TreeViewerEvent.class, (IWActionListener)groupTree.getPresentationState(iwc));

//    groupTree.addIWActionListener((IWActionListener)ps);



/**
 * fix : EventListenerList list = this.getEventListenerList(iwc);
 * fix : this.setIWUserContext(iwc);
 */
//    EventListenerList list = this.getEventListenerList(iwc);

    this.setIWUserContext(iwc);

    IWPresentationState gtState = groupTree.getPresentationState(iwc);
    if(gtState instanceof IWActionListener){
      ((UserApplicationControlAreaPS)this.getPresentationState(iwc)).addIWActionListener((IWActionListener)gtState);
    }

    groupTree.setToShowSuperRootNode(true);
    groupTree.setDefaultOpenLevel(0);
    groupTree.setSuperRootNodeName("IW Member");
    Image icon = iwb.getImage("super_root_icon.gif");
    groupTree.setSuperRootNodeIcon(icon);

    ChangeListener[] chListeners = this.getPresentationState(iwc).getChangeListener();
    if(chListeners != null){
      for (int i = 0; i < chListeners.length; i++) {
        gtState.addChangeListener(chListeners[i]);
      }
    }
  }

  public void main(IWContext iwc) throws Exception {

    GroupBusiness gBusiness = (GroupBusiness)IBOLookup.getServiceInstance(iwc,GroupBusiness.class);

    this.empty();
    this.add(groupTree);
    
    if(iwc.isSuperAdmin()){
    	GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
			groupTree.setRootNode(node);
    }
    else{
    	UserBusiness biz = getUserBusiness(iwc);
    	Collection groups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser());
    	Collection groupNodes = convertGroupCollectionToGroupNodeCollection(groups,iwc.getApplicationContext());
			groupTree.setFirstLevelNodes(groupNodes.iterator());

    }
    
    
    

//    Collection topGroups = iwc.getDomain().getTopLevelGroupsUnderDomain();
//
//    if(topGroups != null){
//      String type = gBusiness.getGroupType(UserGroupRepresentative.class);
//      Iterator iter = topGroups.iterator();
//      while (iter.hasNext()) {
//        Group item = (Group)iter.next();
//        if(type.equals(item.getGroupType())){
//          iter.remove();
//        }
//      }

//      groupTree.setFirstLevelNodes(topGroups.iterator());
//    }

//    groupTree.setControlEventModel(_contolEvent);
//    groupTree.setControlTarget(_controlTarget);

//    this.getParentPage().setBackgroundColor("#d4d0c8");
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