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

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur A. Saemundsson</a>
 * @modified <a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.1
 */

public class UserApplication extends IWBrowser {

  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  //userBusiness to access the stylesheet (which is a bundle property)
	private UserBusiness userBusiness = null;
  
	/**
		 * added 7/10/03 for stylesheet writeout
		 */
		private Page parentPage;
		private String styleSrc = "";
		private String bannerTableStyle = "banner";

  public UserApplication() {
    initializePresentation();
  }

  public void initializePresentation(){
    this.setWidth(900);
    this.setHeight(750);

    this.addToTop(new Top());
    this.setSpanPixels(POS_TOP,50); 
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
    

    //this.addToBottom(new Bottom());
   // System.out.println("SERVLET URL : "+getIWApplicationContext().getApplication().getTranslatedURIWithContext(IWPresentationEvent.IW_EVENT_HANDLER_URL));
    this.setEventURL(getIWApplicationContext().getIWMainApplication().getTranslatedURIWithContext(IWPresentationEvent.IW_EVENT_HANDLER_URL));
    
    
//    this.setBorder(20);
//    this.getMiddleFrameset().setBorder(10);
//    this.getLeftMainFrame().setBorder(10);
//    this.getMainFrame().setBorder(10);
    this.getTopFrame().setNoresize(true);
    this.getTopFrame().setScrolling(false);
    this.getMenuFrame().setScrolling(false);
    this.getMenuFrame().setNoresize(true);
    this.getBottomFrame().setScrolling(false);
    this.getBottomFrame().setNoresize(true);
    this.getEventFrame().setScrolling(false);
    this.getEventFrame().setNoresize(true);
    this.getMainFrame().setScrolling(true);
    this.getLeftMainFrame().setNoresize(false);
    this.getLeftMainFrame().setScrollingAuto();
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

  public void main(IWContext iwc) throws Exception {
  		setTitle(getResourceBundle(iwc).getLocalizedString("user_application.title", "User Application"));

    Frame f = this.getFrame(this.getFrameName(POS_MAIN));

    PresentationObject buo = f.getPresentationObject();
    IWActionListener l = (IWActionListener)((StatefullPresentation)buo).getPresentationState(iwc);

    this.addActionListener(POS_LEFTMAIN,l);
    this.addActionListener(POS_MENU,l);
    this.addActionListener(POS_MAIN,l); 


    Frame left = this.getFrame(this.getFrameName(POS_LEFTMAIN));

    PresentationObject bgo = left.getPresentationObject();
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
      } else {
        setAllMargins(0);
        setBackgroundColor("#ffffff"); //changed from #386cb7
      }
    }

    public String getBundleIdentifier(){
      return "com.idega.user";
    }

    public ChangeListener getChangeControler(){
      return (ChangeListener)this.getPresentationState(this.getIWUserContext());
    }

    public IWPresentationState getPresentationState(IWUserContext iwuc){
      if(_presentationState == null){
        try {
          IWStateMachine stateMachine = (IWStateMachine)IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
          _presentationState = (IWControlFramePresentationState)stateMachine.getStateFor(getCompoundId(),this.getPresentationStateClass());
        }
        catch (RemoteException re) {
          throw new RuntimeException(re.getMessage());
        }
      }
      return _presentationState;
    }

    public Class getPresentationStateClass(){
      return IWControlFramePresentationState.class;
    }



    public void main(IWContext iwc) throws Exception{
    	//IWBundle iwb = getBundle(iwc);
		
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

      if(!initialized){
        Table headerTable = new Table(1,1);
        //added for isi styles 7/10/03 - birna
				headerTable.setStyleClass(bannerTableStyle);
        headerTable.setCellpadding(0);
        headerTable.setCellspacing(0);
        headerTable.setWidth("100%");
        headerTable.setHeight("100%");
 
		//setting the styleSheet 
		userBusiness = getUserBusiness(iwc); 
		styleSrc = userBusiness.getUserApplicationStyleSheetURL();
		parentPage = getParentPage();
		parentPage.addStyleSheetURL(styleSrc);

        /** @todo setja inn mynd i header**/
//        headerTable.add(this.getBundle(iwc).getImage("/top.gif","idegaWeb Member"),1,1);
//				headerTable.add(this.getBundle(iwc).getImage("top.gif","idegaWeb Member"),1,1);

        /*Text adminTitle = new Text("F�lagakerfi �S� & UMF�");
        adminTitle.setBold();
        adminTitle.setFontColor("#FFFFFF");
        adminTitle.setFontSize(Text.FONT_SIZE_12_HTML_3);
        adminTitle.setFontFace(Text.FONT_FACE_ARIAL);
		headerTable.add(adminTitle,1,1);

        Text adminTitle = new Text("Users & Groups &nbsp;&nbsp;");
          adminTitle.setBold();
          adminTitle.setFontColor("#FFFFFF");
          adminTitle.setFontSize("3");
          adminTitle.setFontFace(Text.FONT_FACE_ARIAL);

        headerTable.add(adminTitle,2,1);*/


        headerTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_LEFT);
        headerTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);

        add(headerTable);
        initialized = true;
      }
    }

  }


//  public class Bottom extends Page implements IWBrowserCompliant {
//
//    public Bottom(){
//      if(this.isChildOfOtherPage()){
//        Page parent = this.getParentPage();
//        parent.setAllMargins(0);
//        parent.setBackgroundColor("#ffffff");//new IWColor(207,208,210).getHexColorString());
//      } else {
//        setAllMargins(0);
//        setBackgroundColor("#ffffff");//new IWColor(207,208,210).getHexColorString());
//      }
//
//    }
//
//   public void main(IWContext iwc) throws Exception{
//    Table toolbarTable = new Table(1,1);
//    
//    toolbarTable.setCellpadding(0);
//    toolbarTable.setCellspacing(0);
//    toolbarTable.setWidth("100%");
//    toolbarTable.setHeight("100%");
////    toolbarTable.setHeight(1,1);
////    toolbarTable.setHeight(3,1);
//
////    IWColor color = new IWColor(0,0,0);//new IWColor(207,208,210);//jonni color
////
////    toolbarTable.setColor(color);
////    toolbarTable.setColor(1,1,color.brighter());
////    toolbarTable.setColor(1,3,color.darker());
//
//    add(toolbarTable);
//   }
//
//
//  }
	protected UserBusiness getUserBusiness(IWApplicationContext iwc) {
			if (userBusiness == null) {
				try {
					userBusiness = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
				}
				catch (java.rmi.RemoteException rme) {
					throw new RuntimeException(rme.getMessage());
				}
			}
			return userBusiness;
		}
}