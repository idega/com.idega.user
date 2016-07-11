package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeListener;

import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.app.IWBrowser;
import com.idega.idegaweb.browser.presentation.IWBrowseControl;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.presentation.Frame;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.user.business.UserBusiness;
import com.idega.util.PresentationUtil;

/**
 * <p>
 * Title: idegaWeb
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: idega Software
 * </p>
 *
 * @author <a href="gummi@idega.is">Gudmundur A. Saemundsson</a>
 * @modified <a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.1
 */

public class UserApplication extends IWBrowser {

  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	UserBusiness userBusiness = null;

	/**
		 * added 7/10/03 for stylesheet writeout
		 */
		Page parentPage;
		String styleSrc = "";
		String bannerTableStyle = "banner";

  public UserApplication() {
    initializePresentation();
  }

  public void initializePresentation(){
    this.setWidth(900);
    this.setHeight(750);

    IWMainApplication iwma = IWMainApplication.getDefaultIWMainApplication();
    if(iwma.getProductInfo().isMajorPlatformVersionEqualOrHigherThan(3)){
    		//Not add the top for new versions
        this.addToTop(new Top());
        this.setSpanPixels(POS_TOP,0);
    } else {
        this.addToTop(new Top());
        this.setSpanPixels(POS_TOP,50);
    }

    this.setSpanPixels(POS_LEFTMAIN, 230);
    this.setSpanPixels(POS_MENU,29);
    this.setSpanPixels(POS_BOTTOM,35);
    this.setSpanPixels(POS_EVENT,1);

    UserApplicationMenuArea menuArea = new UserApplicationMenuArea();
	UserApplicationControlArea treeArea = new UserApplicationControlArea();
	UserApplicationMainArea mainArea = new UserApplicationMainArea();
	UserApplicationBottomArea bottomArea = new UserApplicationBottomArea();

	this.addToMenu(menuArea);
    this.showMenuFrame(true);
    this.showEventFrame(true); //MUST BE TRUE!
	this.addToLeftMain(treeArea);
    this.addToMain(mainArea);
    this.addToBottom(bottomArea);

	menuArea.setUserApplicationMainAreaStateId(mainArea.getCompoundId());

	this.setEventURL(IWPresentationEvent.getEventHandlerFrameURL(IWContext.getInstance()));

    this.getTopFrame().setNoresize(true);
    this.getTopFrame().setScrolling(false);
    this.getMenuFrame().setScrolling(false);
    this.getMenuFrame().setNoresize(true);
		this.getMenuFrame().setMarkupAttribute("marginheight", "0");
		this.getMenuFrame().setMarkupAttribute("marginwidth", "0");
    this.getBottomFrame().setScrolling(false);
    this.getBottomFrame().setNoresize(true);
    this.getEventFrame().setScrolling(false);
    this.getEventFrame().setNoresize(true);
    this.getMainFrame().setScrolling(true);
		this.getMainFrame().setMarkupAttribute("marginheight", "0");
		this.getMainFrame().setMarkupAttribute("marginwidth", "0");
    this.getLeftMainFrame().setNoresize(false);
		this.getLeftMainFrame().setScrollingAuto();
		this.getLeftMainFrame().setMarkupAttribute("marginheight", "0");
		this.getLeftMainFrame().setMarkupAttribute("marginwidth", "0");
  }

  @Override
public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

  @Override
public void main(IWContext iwc) throws Exception {
  	setTitle(getResourceBundle(iwc).getLocalizedString("user_application.title", "User Application"));
  		this.userBusiness = getUserBusiness(iwc);
		this.styleSrc = UserApplication.this.userBusiness.getUserApplicationStyleSheetURL();
		this.parentPage = getParentPage();
		this.parentPage.addStyleSheetURL(UserApplication.this.styleSrc);

    Frame mainFrame = this.getFrame(this.getFrameName(POS_MAIN));
    PresentationObject buo = mainFrame.getPresentationObject();
    IWActionListener l = (IWActionListener)((StatefullPresentation)buo).getPresentationState(iwc);

    this.addActionListener(POS_LEFTMAIN,l);
    this.addActionListener(POS_MENU,l);
    this.addActionListener(POS_MAIN,l);

    Frame leftFrame = this.getFrame(this.getFrameName(POS_LEFTMAIN));
    PresentationObject bgo = leftFrame.getPresentationObject();
    IWActionListener listener = (IWActionListener)((StatefullPresentation)bgo).getPresentationState(iwc);

    this.addActionListener(POS_LEFTMAIN,listener);
    this.addActionListener(POS_MENU,listener);
  }

 public class Top extends Page implements IWBrowseControl, StatefullPresentation {
    private boolean initialized = false;

    private IWControlFramePresentationState _presentationState = null;

    public Top(){
      if(this.isChildOfOtherPage()){
        Page parent = this.getParentPage();
        parent.setAllMargins(0);
        parent.setBackgroundColor("#ffffff"); //changed from #386cb7
		}
		else {
        setAllMargins(0);
        setBackgroundColor("#ffffff"); //changed from #386cb7
      }
    }

    @Override
	public String getBundleIdentifier(){
      return "com.idega.user";
    }

    @Override
	public ChangeListener getChangeControler(){
      return (ChangeListener)this.getPresentationState(this.getIWUserContext());
    }

    @Override
	public IWPresentationState getPresentationState(IWUserContext iwuc){
      if(this._presentationState == null){
        try {
          IWStateMachine stateMachine = IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
          this._presentationState = (IWControlFramePresentationState)stateMachine.getStateFor(getCompoundId(),this.getPresentationStateClass());
        }
        catch (RemoteException re) {
          throw new RuntimeException(re.getMessage());
        }
      }
      return this._presentationState;
    }

    @Override
	public Class<IWControlFramePresentationState> getPresentationStateClass(){
      return IWControlFramePresentationState.class;
    }

    @Override
	public void main(IWContext iwc) throws Exception{
      IWControlFramePresentationState state = (IWControlFramePresentationState)this.getPresentationState(iwc);
      if(state != null){
        Set onLoadSet = state.getOnLoadSet();
        Iterator iter = onLoadSet.iterator();
        while (iter.hasNext()) {
          Object item = iter.next();
          this.setOnLoad((String)item);
        }
        state.clearOnLoad();
      }

      if(!this.initialized){
        Table headerTable = new Table(1,1);
		headerTable.setStyleClass(UserApplication.this.bannerTableStyle);
        headerTable.setCellpadding(0);
        headerTable.setCellspacing(0);
        headerTable.setWidth("100%");
        headerTable.setHeight("100%");

		//setting the styleSheet
		UserApplication.this.userBusiness = getUserBusiness(iwc);
		UserApplication.this.styleSrc = UserApplication.this.userBusiness.getUserApplicationStyleSheetURL();
		UserApplication.this.parentPage = getParentPage();
		UserApplication.this.parentPage.addStyleSheetURL(UserApplication.this.styleSrc);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getBundle(iwc).getVirtualPathWithFileNameString("javascript/UserApplication.js"));

        headerTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_LEFT);
        headerTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);

        add(headerTable);
        this.initialized = true;
      }
    }

  }

	protected UserBusiness getUserBusiness(IWApplicationContext iwc) {
			if (this.userBusiness == null) {
				try {
					this.userBusiness = com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
				}
				catch (java.rmi.RemoteException rme) {
					throw new RuntimeException(rme.getMessage());
				}
			}
			return this.userBusiness;
		}
}