package com.idega.user.app;

import java.util.List;
import java.util.Vector;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateUser;
import com.idega.user.event.ChangeClassEvent;
import com.idega.util.IWColor;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class Toolbar extends Page implements IWBrowserView {

  private IWBundle iwb;
  private String _controlTarget = null;
  private IWPresentationEvent _controlEvent = null;

  private Vector _toolbarElements = new Vector();

  public Toolbar(){
    if(this.isChildOfOtherPage()){
      Page parent = this.getParentPage();
      parent.setAllMargins(0);
      parent.setBackgroundColor("#CFD0D2");
    } else {
      setAllMargins(0);
      setBackgroundColor("#CFD0D2");
      //"#E6E6E6" ljosari
    }

  }


//  public void add(PresentationObject obj){
//    if(!(obj instanceof Frame)){
//      Frame frame = new Frame();
//
//      IWLocation location = new IWPresentationLocation();
//      location.isInFrameSet(true);
//      location.setApplicationClass(this.getClass());
//
//      frame.setLocation(location);
//      frame.setPresentationObject(obj);
//      frame.setNameProperty(frameNameCounter++);
//      // super.add() but does not set Location = this.location;
//      try {
//        if (theObjects == null) {
//          this.theObjects = new Vector();
//        }
//        if (obj != null) {
//          obj.setParentObject(this);
//          //modObject.setLocation(this.getLocation());
//          this.theObjects.addElement(obj);
//        }
//      }
//      catch(Exception ex) {
//        ExceptionWrapper exep = new ExceptionWrapper(ex,this);
//      }
//    }else{
//      // super.add() but does not set Location = this.location;
//      try {
//        if (theObjects == null) {
//          this.theObjects = new Vector();
//        }
//        if (obj != null) {
//          obj.setParentObject(this);
//          //modObject.setLocation(this.getLocation());
//          this.theObjects.addElement(obj);
//        }
//      }
//      catch(Exception ex) {
//        ExceptionWrapper exep = new ExceptionWrapper(ex,this);
//      }
//    }
//
//  }


  public void add(ToolbarElement element){
    _toolbarElements.add(element);
  }

  public void add(Toolbar toolbar){
    _toolbarElements.add(toolbar);
//    addSeperator();
//    List l = getToolbarElements();
//    if (l != null) {
//      Iterator iter = l.iterator();
//      while (iter.hasNext()) {
//        Object item = iter.next();
//        this.add((ToolbarElement)item);
//      }
//    }
  }

  public void addSeperator(){

  }

  protected List getToolbarElements(){
    return _toolbarElements;
  }






  public String getBundleIdentifier(){
    return "com.idega.user";
  }

  public void setControlEventModel(IWPresentationEvent model){
    _controlEvent = model;
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
  }


  public void main(IWContext iwc) throws Exception{
    iwb = getBundle(iwc);

    Table toolbarTable = new Table(1,1);
    toolbarTable.setCellpadding(0);
    toolbarTable.setCellspacing(0);
//    toolbarTable.setWidth("100%");
//    toolbarTable.setHeight("100%");
//    toolbarTable.setHeight(1,1);
//    toolbarTable.setHeight(3,1);
//
//    IWColor color = new IWColor(212,208,200);
//    toolbarTable.setColor(color);
//    toolbarTable.setColor(1,1,color.brighter());
//    toolbarTable.setColor(1,3,color.darker());
//
//
//    toolbarTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_RIGHT);
//
    add(toolbarTable);

    Table toolbar1 = new Table();
    toolbar1.setCellpadding(0);
    toolbar1.setCellspacing(0);

/*
    Table table = new Table(4,3);
    table.setCellpadding(0);
    table.setCellspacing(0);
    table.setWidth(1,"5");
    table.setWidth(2,"1");
    table.setWidth(3,"1");
    table.setWidth(4,"4");
    table.setHeight(1,"2");
    table.setHeight(2,"20");
    table.setHeight(3,"2");
    table.setColor(2,2,color.brighter());
    table.setColor(3,2,color.darker());
    toolbar1.add(table,1,1);
*/

    //int iconDimentions = 20;



    Table button = new Table(2,1);
 	button.setCellpadding(0);
    Image iconCrUser = iwb.getImage("new_user.gif");
    button.add(iconCrUser,1,1);
   	Text text = new Text("New User");
 	text.setFontFace(Text.FONT_FACE_VERDANA);
 	text.setFontSize(Text.FONT_SIZE_7_HTML_1);
    Link tLink11 = new Link(text);
    tLink11.setWindowToOpen(CreateUser.class);
    button.add(tLink11,2,1);
    toolbar1.add(button,2,1);

 	Table button2 = new Table(2,1);
 	button2.setCellpadding(0);
    Image iconCrGroup = iwb.getImage("new_group.gif");
    button2.add(iconCrGroup,1,1);
	Text text2 = new Text("New Group");
 	text2.setFontFace(Text.FONT_FACE_VERDANA);
 	text2.setFontSize(Text.FONT_SIZE_7_HTML_1);
    Link tLink12 = new Link(text2);
    tLink12.setWindowToOpen(CreateGroupWindow.class);
    button2.add(tLink12,2,1);
    toolbar1.add(button2,3,1);


    //toolbar1.setWidth(2,"26");
   // toolbar1.setWidth(3,"26");



   //Group
   //user
   //finance
    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Finance", iwb.getImage("finance.gif"), com.idega.block.news.presentation.News.class),4,1);
   //reports
    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Reports", iwb.getImage("reports.gif"), com.idega.block.news.presentation.News.class),5,1);
   //To do - stickies
//    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("To do", iwb.getImage("todo.gif"), com.idega.block.news.presentation.News.class),7,1);
   //settings
    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Settings", iwb.getImage("settings.gif"), com.idega.block.news.presentation.News.class),6,1);

   //view
   	//dropdownmenu
    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Views", iwb.getImage("views.gif"), com.idega.block.news.presentation.News.class),7,1);
   //search
   	//text input
    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Search", iwb.getImage("search.gif"), com.idega.block.news.presentation.News.class),8,1);


   Text text3 = new Text("Reset");
   text3.setFontFace(Text.FONT_FACE_VERDANA);
   text3.setFontSize(Text.FONT_SIZE_7_HTML_1);
   Link resetLink = new Link(text3);
   resetLink.addEventModel(new ResetPresentationEvent());
   if(_controlEvent != null){
     resetLink.addEventModel(_controlEvent);
   }
   if(_controlTarget != null){
     resetLink.setTarget(_controlTarget);
   }

   toolbar1.add(resetLink,9,1);

   //    toolbarTable.add(toolbar1,1,2);
//	this.add(toolbar1);
	toolbarTable.add(toolbar1,1,1);

   //hitt
   //group
   //user
   //calendar
   //history
   //export
   //import
   //bread crumbs
   //name - address - pin



  }

  
 protected Table getToolbarButtonWithChangeClassEvent(String textOnButton, Image icon, Class changeClass){

 	Table button = new Table(2,1);
 	button.setCellpadding(0);
 	Text text = new Text(textOnButton);
 	text.setFontFace(Text.FONT_FACE_VERDANA);
 	text.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	Link eventLink = new Link(text);
 	button.add(icon,1,1);
 	button.add(eventLink,2,1);
 	eventLink.addEventModel(new ChangeClassEvent(changeClass));
    if(_controlEvent != null){
      eventLink.addEventModel(_controlEvent);
    }
    if(_controlTarget != null){
      eventLink.setTarget(_controlTarget);
    }

    return button;

 }


}