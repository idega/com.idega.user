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
  private IWPresentationEvent _controlEvent = null;

  private SearchForm searchForm = new SearchForm();
  private Table toolbarTable = new Table(1,3);
  private Toolbar toolbar = new Toolbar();
  private static String IW_BUNDLE_IDENTIFIER = "com.idega.user";



  public UserApplicationMenuArea() {
    _stateHandler = new StatefullPresentationImplHandler();
    _stateHandler.setPresentationStateClass(UserApplicationMenuAreaPS.class);
	this.setAllMargins(0);
  }

  public void setControlEventModel(IWPresentationEvent model){
//    System.out.print("UserApplicationMenuArea: setControlEventModel(IWPresentationEvent model)");
    _controlEvent = model;

    searchForm.setControlEventModel(model);
	toolbar.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
//    System.out.print("UserApplicationMenuArea: setControlTarget(String controlTarget)");
    _controlTarget = controlTarget;
    searchForm.setControlTarget(controlTarget);
	toolbar.setControlTarget(controlTarget);
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
    return this.IW_BUNDLE_IDENTIFIER;
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

    //IWColor color = new IWColor(212,208,200);
    IWColor color = new IWColor(207,208,210);//jonni color

    toolbarTable.setColor(color);
    toolbarTable.setColor(1,1,color.brighter());
    toolbarTable.setColor(1,3,color.darker());


    toolbarTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_RIGHT);

    super.add(toolbarTable);


	Table table = new Table(2,1);
	table.setCellpadding(0);
    table.setCellspacing(0);
    table.setWidth("100%");

	table.setAlignment(1,1,Table.HORIZONTAL_ALIGN_LEFT);
	table.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);

	table.add(toolbar,1,1);
	table.add(searchForm,2,1);
	toolbarTable.add(table,1,2);

//	this.add(toolbar);
//
//    this.add(searchForm);

//    this.setIWUserContext(iwc);

//	CreateGroupWindow createGroup = new CreateGroupWindow();
//
//    toolbar.add((ToolbarElement)createGroup);

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

//  public void add(PresentationObject obj){
//    toolbarTable.add(obj,1,2);
//  }

//  public void main(IWContext iwc) throws Exception {
//
    //GroupBusiness gBusiness = (GroupBusiness)IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
//
//    this.empty();
//
//
//  }



}