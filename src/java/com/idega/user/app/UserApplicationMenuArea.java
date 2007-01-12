package com.idega.user.app;

import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class UserApplicationMenuArea extends Page implements IWBrowserView, StatefullPresentation {

  private IWBundle iwb;
  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _controlEvent = null;
  private Toolbar toolbar = new Toolbar();
  private static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private String userApplicationMainAreaStateId = null;
	private String styleScript = "DefaultStyle.css";



  public UserApplicationMenuArea() {
    this._stateHandler = new StatefullPresentationImplHandler();
    this._stateHandler.setPresentationStateClass(UserApplicationMenuAreaPS.class);
	this.setAllMargins(0);
  }

  public void setControlEventModel(IWPresentationEvent model){
//    System.out.print("UserApplicationMenuArea: setControlEventModel(IWPresentationEvent model)");
    this._controlEvent = model;
    this.toolbar.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
//    System.out.print("UserApplicationMenuArea: setControlTarget(String controlTarget)");
    this._controlTarget = controlTarget;
    this.toolbar.setControlTarget(controlTarget);
  }

  public Class getPresentationStateClass(){
    return this._stateHandler.getPresentationStateClass();
  }

  public IWPresentationState getPresentationState(IWUserContext iwuc){
    return this._stateHandler.getPresentationState(this,iwuc);
  }

  public StatefullPresentationImplHandler getStateHandler(){
    return this._stateHandler;
  }

  public String getBundleIdentifier(){
    return UserApplicationMenuArea.IW_BUNDLE_IDENTIFIER;
  }


 /* public void empty(){
    toolbarTable.empty();
  }*/


  public void initializeInMain(IWContext iwc){
    this.empty();
    this.iwb = getBundle(iwc);
 //commented out 8/10/03 for isi styles - birna
  //  this.setBackgroundColor(IWColor.getHexColorString(212,208,200));
   // getParentPage().setBackgroundColor(IWColor.getHexColorString(212,208,200));
    String id = getPresentationState(iwc).getCompoundId();
    this.toolbar.setSelectedGroupProviderStateId(id);
    this.toolbar.setUserApplicationMainAreaStateId(this.userApplicationMainAreaStateId);
    super.add(this.toolbar);
  }
  
  

	/**
	 * @param string
	 */
	public void setUserApplicationMainAreaStateId(String string) {
		this.userApplicationMainAreaStateId = string;
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		IWBundle iwb = getBundle(iwc);
		Page parentPage = this.getParentPage();
		String styleSrc = iwb.getVirtualPathWithFileNameString(this.styleScript);
		parentPage.addStyleSheetURL(styleSrc);
	
	}

}