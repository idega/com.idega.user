package com.idega.user.app;

import com.idega.user.presentation.*;
import com.idega.business.IBOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.app.IWBrowser;
import com.idega.idegaweb.browser.presentation.IWBrowseControl;
import com.idega.idegaweb.browser.presentation.IWBrowserCompliant;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.Frame;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.util.IWColor;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeListener;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */ 

public class UserApplication extends IWBrowser {

  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";

  public UserApplication() {

    initializePresentation();

  }

  public void initializePresentation(){

    this.setWidth(933);
    this.setHeight(700);

    this.addToTop(new Top());
    this.setSpanPixels(POS_TOP,33);
    this.setSpanPixels(POS_LEFTMAIN, 200);
    this.setSpanPixels(POS_MENU,28);
    this.setSpanPixels(POS_BOTTOM,28);


//    Toolbar toolbar = new Toolbar();
//
//    CreateGroupWindow createGroup = new CreateGroupWindow();
//
//    toolbar.add((ToolbarElement)createGroup);
//
//    this.addToMenu(toolbar);

	this.addToMenu(new UserApplicationMenuArea());

    this.showMenuFrame(true);
    this.showBottomFrame(true);



//    BasicGroupOverview bgo = new BasicGroupOverview();
    BasicUserOverview buo = new BasicUserOverview();

//    this.addToLeftMain(bgo);
    this.addToLeftMain(new UserApplicationControlArea());
    this.addToMain(buo);
//	this.addToMain(new UserApplicationMainArea());





    this.addToBottom(new Bottom());

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
    this.getMainFrame().setScrolling(true);
    this.getLeftMainFrame().setNoresize(false);
    this.getLeftMainFrame().setScrolling(true);



  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }



  public void main(IWContext iwc) throws Exception {

    Frame f = this.getFrame(this.getFrameName(POS_MAIN));

    PresentationObject buo = f.getPresentationObject();

    IWActionListener l = (IWActionListener)((StatefullPresentation)buo).getPresentationState(iwc);

    this.addIWActionListener(POS_LEFTMAIN,l);
    this.addIWActionListener(POS_MENU,l);
    this.addIWActionListener(POS_MAIN,l);


    Frame left = this.getFrame(this.getFrameName(POS_LEFTMAIN));

    PresentationObject bgo = left.getPresentationObject();

    IWActionListener listener = (IWActionListener)((StatefullPresentation)bgo).getPresentationState(iwc);

    this.addIWActionListener(POS_LEFTMAIN,listener);
    this.addIWActionListener(POS_MENU,listener);

  }



  public class Top extends Page implements IWBrowseControl, StatefullPresentation {
    private boolean initialized = false;

    private IWControlFramePresentationState _presentationState = null;

    public Top(){
      if(this.isChildOfOtherPage()){
        Page parent = this.getParentPage();
        parent.setAllMargins(0);
        parent.setBackgroundColor(IWAdminWindow.HEADER_COLOR);
      } else {
        setAllMargins(0);
        setBackgroundColor(IWAdminWindow.HEADER_COLOR);
      }
    }

    public ChangeListener getChangeControler(){
      return (ChangeListener)this.getPresentationState(this.getIWUserContext());
    }

    public IWPresentationState getPresentationState(IWUserContext iwuc){
      if(_presentationState == null){
        try {
          IWStateMachine stateMachine = (IWStateMachine)IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
          _presentationState = (IWControlFramePresentationState)stateMachine.getStateFor(this.getLocation(),this.getPresentationStateClass());
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
        Table headerTable = new Table(2,1);
        headerTable.setCellpadding(0);
        headerTable.setCellspacing(0);
        headerTable.setWidth("100%");
        headerTable.setHeight("100%");
        
        /** @todo setja inn mynd i header**/
        //headerTable.add(iwc.getApplication().getCoreBundle().getImage("/editorwindow/idegaweb.gif","idegaWeb"),1,1);
		Text adminTitle = new Text("Félagakerfi ÍSÍ & UMFÍ");
        adminTitle.setBold();
        adminTitle.setFontColor("#FFFFFF");
        adminTitle.setFontSize(Text.FONT_SIZE_12_HTML_3);
        adminTitle.setFontFace(Text.FONT_FACE_ARIAL);
		headerTable.add(adminTitle,1,1);
		
        /*Text adminTitle = new Text("Users & Groups &nbsp;&nbsp;");
          adminTitle.setBold();
          adminTitle.setFontColor("#FFFFFF");
          adminTitle.setFontSize("3");
          adminTitle.setFontFace(Text.FONT_FACE_ARIAL);

        headerTable.add(adminTitle,2,1);*/


        headerTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_LEFT);
        headerTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);


        add(headerTable);
        initialized = true;
      }
    }

  }


  public class Bottom extends Page implements IWBrowserCompliant {

    public Bottom(){
      if(this.isChildOfOtherPage()){
        Page parent = this.getParentPage();
        parent.setAllMargins(0);
        parent.setBackgroundColor(new IWColor(207,208,210).getHexColorString());
      } else {
        setAllMargins(0);
        setBackgroundColor(new IWColor(207,208,210).getHexColorString());
      }

    }

   public void main(IWContext iwc) throws Exception{



    Table footerTable = new Table(2,1);
    footerTable.setCellpadding(0);
    footerTable.setCellspacing(0);
    footerTable.setWidth("100%");
    footerTable.setHeight("100%");
    footerTable.setWidth(2,1,"20");

    CloseButton cb = new CloseButton();

    footerTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_RIGHT);

    footerTable.add(cb,1,1);

    add(footerTable);
   }


  }

//  public class Toolbar extends Page implements IWBrowserView {
//
//    private IWBundle iwb;
//    private String _controlTarget = null;
//    private IWPresentationEvent _contolEvent = null;
//
//    public Toolbar(){
//      if(this.isChildOfOtherPage()){
//        Page parent = this.getParentPage();
//        parent.setAllMargins(0);
//        parent.setBackgroundColor(IWColor.getHexColorString(212,208,200));
//      } else {
//        setAllMargins(0);
//        setBackgroundColor(IWColor.getHexColorString(212,208,200));
//      }
//
//    }
//
//    public String getBundleIdentifier(){
//      return "com.idega.user";
//    }
//
//    public void setControlEventModel(IWPresentationEvent model){
//      _contolEvent = model;
//    }
//
//    public void setControlTarget(String controlTarget){
//      _controlTarget = controlTarget;
//    }
//
//
//    public void main(IWContext iwc) throws Exception{
//      iwb = getBundle(iwc);
//
//      Table toolbarTable = new Table(1,3);
//      toolbarTable.setCellpadding(0);
//      toolbarTable.setCellspacing(0);
//      toolbarTable.setWidth("100%");
//      toolbarTable.setHeight("100%");
//      toolbarTable.setHeight(1,1);
//      toolbarTable.setHeight(3,1);
//      //footerTable.setWidth(2,1,"20");
//
//      IWColor color = new IWColor(212,208,200);
//      toolbarTable.setColor(color);
//      toolbarTable.setColor(1,1,color.brighter());
//      toolbarTable.setColor(1,3,color.darker());
//
//
//      toolbarTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_RIGHT);
//
//      add(toolbarTable);
//
//      Table toolbar1 = new Table();
//      toolbar1.setCellpadding(0);
//      toolbar1.setCellspacing(0);
//
//
//      Table table = new Table(4,3);
//      table.setCellpadding(0);
//      table.setCellspacing(0);
//      table.setWidth(1,"5");
//      table.setWidth(2,"1");
//      table.setWidth(3,"1");
//      table.setWidth(4,"4");
//      table.setHeight(1,"2");
//      table.setHeight(2,"20");
//      table.setHeight(3,"2");
//      table.setColor(2,2,color.brighter());
//      table.setColor(3,2,color.darker());
//      toolbar1.add(table,1,1);
//
//      int iconDimentions = 20;
//
//      Image iconCrUser = iwb.getImage("group.gif");
//      iconCrUser.setHeight(iconDimentions);
//      iconCrUser.setWidth(iconDimentions);
//      Link tLink12 = new Link(iconCrUser);
//      tLink12.setWindowToOpen(CreateGroupWindow.class);
//      toolbar1.add(tLink12,2,1);
//
//      Image iconCrGroup = iwb.getImage("user.gif");
//      iconCrGroup.setHeight(iconDimentions);
//      iconCrGroup.setWidth(iconDimentions);
//      Link tLink11 = new Link(iconCrGroup);
//      tLink11.setWindowToOpen(CreateUser.class);
//      toolbar1.add(tLink11,3,1);
//
//      toolbarTable.add(toolbar1,1,2);
//
//      toolbar1.setWidth(2,"26");
//      toolbar1.setWidth(3,"26");
//
//
//      Link resetLink = new Link("reset");
//      resetLink.addEventModel(new ResetPresentationEvent());
//      if(_contolEvent != null){
//        resetLink.addEventModel(_contolEvent);
//      }
//      if(_controlTarget != null){
//        resetLink.setTarget(_controlTarget);
//      }
//
//      toolbar1.add(resetLink,4,1);
//
//    }
//
//
//  }
}