package com.idega.user.app;

import com.idega.user.presentation.*;
import com.idega.presentation.*;
import com.idega.user.block.search.presentation.SearchForm;
import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.user.business.GroupBusiness;
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

public class UserApplicationMenuArea extends Page implements IWBrowserView, StatefullPresentation {

  private IWBundle iwb;
  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;

  private SearchForm searchForm = new SearchForm();
  private Table toolbarTable = new Table(1,3);



  public UserApplicationMenuArea() {
    _stateHandler = new StatefullPresentationImplHandler();
    _stateHandler.setPresentationStateClass(UserApplicationMenuAreaPS.class);
	this.setAllMargins(0);
  }

  public void setControlEventModel(IWPresentationEvent model){
//    System.out.print("UserApplicationMenuArea: setControlEventModel(IWPresentationEvent model)");
    _contolEvent = model;

    searchForm.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
//    System.out.print("UserApplicationMenuArea: setControlTarget(String controlTarget)");
    _controlTarget = controlTarget;
    searchForm.setControlTarget(controlTarget);
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


  public void empty(){
    toolbarTable.empty();
  }


  public void initializeInMain(IWContext iwc){

    iwb = getBundle(iwc);

    IWLocation location = (IWLocation)this.getLocation().clone();
    location.setSubID(1);
    searchForm.setLocation(location,iwc);

	searchForm.setHorizontalAlignment("right");


	toolbarTable.setCellpadding(0);
    toolbarTable.setCellspacing(0);
    toolbarTable.setWidth("100%");
    toolbarTable.setHeight("100%");
    toolbarTable.setHeight(1,1);
    toolbarTable.setHeight(3,1);

    IWColor color = new IWColor(212,208,200);
    toolbarTable.setColor(color);
    toolbarTable.setColor(1,1,color.brighter());
    toolbarTable.setColor(1,3,color.darker());


    toolbarTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_RIGHT);

    super.add(toolbarTable);

    this.setIWUserContext(iwc);

//    IWPresentationState sfState = searchForm.getPresentationState(iwc);
//    if(sfState instanceof IWActionListener){
//      ((UserApplicationMenuAreaPS)this.getPresentationState(iwc)).addIWActionListener((IWActionListener)sfState);
//    }

//    ChangeListener[] chListeners = this.getPresentationState(iwc).getChangeListener();
//    if(chListeners != null){
//      for (int i = 0; i < chListeners.length; i++) {
//        searchForm.addChangeListener(chListeners[i]);
//      }
//    }

//    this.debugEventListanerList(iwc);
//    groupTree.debugEventListanerList(iwc);

    this.getParentPage().setBackgroundColor(IWColor.getHexColorString(212,208,200));

  }

  public void add(PresentationObject obj){
    toolbarTable.add(obj,1,2);
  }

  public void main(IWContext iwc) throws Exception {

    GroupBusiness gBusiness = (GroupBusiness)IBOLookup.getServiceInstance(iwc,GroupBusiness.class);

    this.empty();

	Toolbar toolbar = new Toolbar();

    CreateGroupWindow createGroup = new CreateGroupWindow();

    toolbar.add((ToolbarElement)createGroup);

    this.add(toolbar);

//    this.add(searchForm);

  }



}