package com.idega.user.app;

import com.idega.presentation.ui.Window;
import javax.swing.event.ChangeListener;
import com.idega.idegaweb.*;
import com.idega.event.*;
import com.idega.user.presentation.BasicUserOverview;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.*;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationMainArea extends Window implements IWBrowserView, StatefullPresentation {


  private IWBundle iwb;
  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;

  private BasicUserOverview _buo = new BasicUserOverview();


  public UserApplicationMainArea() {
    this.setAllMargins(0);
  _stateHandler = new StatefullPresentationImplHandler();
    getStateHandler().setPresentationStateClass(UserApplicationMainAreaPS.class);
  }

  public void setControlEventModel(IWPresentationEvent model){
    _contolEvent = model;
    _buo.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
    _buo.setControlTarget(controlTarget);
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

  public void main(IWContext iwc) throws Exception {
    this.empty();
    UserApplicationMainAreaPS ps = (UserApplicationMainAreaPS)this.getPresentationState(iwc);

    if( ps.getClassNameToShow()!= null ){
    	PresentationObject obj = (PresentationObject)Class.forName(ps.getClassNameToShow()).newInstance();
    	add(obj);
        ps.setClassNameToShow(null);
    }
    else{
      System.out.println("ps.getClassNameToShow() is NULL!");
      this.add(_buo);
    }

  }


  public void initializeInMain(IWContext iwc){

    System.out.println("in initializeInMain getClassToShow:"+((UserApplicationMainAreaPS)this.getPresentationState(iwc)).getClassNameToShow());


    iwb = getBundle(iwc);

    IWLocation location = (IWLocation)this.getLocation().clone();
    location.setSubID(1);//bara eitthva? id...herma eftir instance id
    _buo.setLocation(location,iwc);


    this.setIWUserContext(iwc);

    IWPresentationState buoState = _buo.getPresentationState(iwc);
    if(buoState instanceof IWActionListener){
      ((UserApplicationMainAreaPS)this.getPresentationState(iwc)).addIWActionListener((IWActionListener)buoState);
    }


    ChangeListener[] chListeners = this.getPresentationState(iwc).getChangeListener();
    if(chListeners != null){
      for (int i = 0; i < chListeners.length; i++) {
        buoState.addChangeListener(chListeners[i]);
      }
    }

//    this.getParentPage().setBackgroundColor(IWColor.getHexColorString(250,245,240));

  }


}