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
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateUser;
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
  private IWPresentationEvent _contolEvent = null;

  private Vector _toolbarElements = new Vector();

  public Toolbar(){
    if(this.isChildOfOtherPage()){
      Page parent = this.getParentPage();
      parent.setAllMargins(0);
      parent.setBackgroundColor(IWColor.getHexColorString(212,208,200));
    } else {
      setAllMargins(0);
      setBackgroundColor(IWColor.getHexColorString(212,208,200));
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
    _contolEvent = model;
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
  }


  public void main(IWContext iwc) throws Exception{
    iwb = getBundle(iwc);

    Table toolbarTable = new Table(1,3);
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

    add(toolbarTable);

    Table toolbar1 = new Table();
    toolbar1.setCellpadding(0);
    toolbar1.setCellspacing(0);


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

    int iconDimentions = 20;

    Image iconCrUser = iwb.getImage("group.gif");
    iconCrUser.setHeight(iconDimentions);
    iconCrUser.setWidth(iconDimentions);
    Link tLink12 = new Link(iconCrUser);
    tLink12.setWindowToOpen(CreateGroupWindow.class);
    toolbar1.add(tLink12,2,1);

    Image iconCrGroup = iwb.getImage("user.gif");
    iconCrGroup.setHeight(iconDimentions);
    iconCrGroup.setWidth(iconDimentions);
    Link tLink11 = new Link(iconCrGroup);
    tLink11.setWindowToOpen(CreateUser.class);
    toolbar1.add(tLink11,3,1);

    toolbarTable.add(toolbar1,1,2);

    toolbar1.setWidth(2,"26");
    toolbar1.setWidth(3,"26");


    Link resetLink = new Link("reset");
    resetLink.addEventModel(new ResetPresentationEvent());
    if(_contolEvent != null){
      resetLink.addEventModel(_contolEvent);
    }
    if(_controlTarget != null){
      resetLink.setTarget(_controlTarget);
    }

    toolbar1.add(resetLink,4,1);

  }


}