package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ChangeListener;

import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.Script;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.presentation.BasicUserOverview;
import com.idega.user.presentation.BasicUserOverviewPS;
import com.idega.user.presentation.GroupTreeNode;
import com.idega.user.presentation.GroupTreeView;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationControlArea extends Page implements IWBrowserView, StatefullPresentation {

	private final static String LINK_STYLE = "font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#000000;text-decoration:none;";
	private final static String LINK_HOVER_STYLE = "font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#FF8008;text-decoration:none;";

  private IWBundle iwb;
	private IWResourceBundle iwrb;
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
    iwrb = getResourceBundle(iwc);

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
    
		if (getParentPage() != null) {
			getParentPage().setStyleDefinition("A", LINK_STYLE);
			getParentPage().setStyleDefinition("A:hover", LINK_HOVER_STYLE);
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("function setLinkToBold(input) {").append("\n\t");
			buffer.append("if (boldLink != null)").append("\n\t\t");
			buffer.append("boldLink.style.fontWeight='normal';").append("\n\t");
			buffer.append("input.style.fontWeight='bold';").append("\n\t");
			buffer.append("boldLink = input;").append("\n}");

			Script script = getParentPage().getAssociatedScript();
			script.addVariable("boldLink", "null");
			script.addFunction("setLinkToBold", buffer.toString());
		}
    
    BasicUserOverviewPS state = getPresentationStateOfBasicUserOverview(iwc);
    Group group = state.getSelectedGroup();
    IBDomain domain = state.getSelectedDomain();
    if (group != null) {
      int groupId = ((Integer) group.getPrimaryKey()).intValue();
      groupTree.setSelectedGroupId(groupId);
    }
    // use else if because both variables could be not null but only one should be selected within the tree
    else if (domain != null)  {
      int domainId = ((Integer) domain.getPrimaryKey()).intValue();
      groupTree.setSelectedGroupId(domainId);
    }
    groupTree.setToShowSuperRootNode(true);
    groupTree.setDefaultOpenLevel(0);
    groupTree.setSuperRootNodeName(iwrb.getLocalizedString("tree.super.node.name","My groups"));
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
    this.empty();
    this.add(groupTree);
    
    if(iwc.isSuperAdmin()){
    	GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
			groupTree.setRootNode(node);
    }
    else{
    	UserBusiness biz = getUserBusiness(iwc);
    	Collection groups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
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
  
  private BasicUserOverviewPS getPresentationStateOfBasicUserOverview(IWUserContext iwuc) {
    try {
      IWStateMachine stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwuc, IWStateMachine.class); 
      String code = IWMainApplication.getEncryptedClassName(BasicUserOverview.class);
      code = ":" + code;
      return (BasicUserOverviewPS) stateMachine.getStateFor( code , BasicUserOverviewPS.class);
    }
    catch (RemoteException ex)  {
      throw new RuntimeException(ex.getMessage());
    }
  }

}