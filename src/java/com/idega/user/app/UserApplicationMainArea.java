package com.idega.user.app;

import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.*;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserApplicationMainArea extends Block implements IWBrowserView, StatefullPresentation {

  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;


  public UserApplicationMainArea() {
    getStateHandler().setPresentationStateClass(UserApplicationMainAreaPS.class);
  }

  public void setControlEventModel(IWPresentationEvent model){
    _contolEvent = model;
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
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


}