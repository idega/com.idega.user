package com.idega.user.presentation;

import com.idega.user.app.Toolbar;
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
 * @author eiki
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BasicUserOverViewToolbar extends Toolbar {

  /**
   * Constructor for BasicUserOverViewToolbar.
   */
  public BasicUserOverViewToolbar() {
  }

  public void main(IWContext iwc) throws Exception{
    iwb = getBundle(iwc);

    Table toolbarTable = new Table(1,1);
    toolbarTable.setCellpadding(0);
    toolbarTable.setCellspacing(0);
    toolbarTable.setWidth("100%");
    toolbarTable.setHeight("100%");
    toolbarTable.setHeight(1,1);
    toolbarTable.setHeight(3,1);

    IWColor color = new IWColor(230,230,230);//jonni color

    toolbarTable.setColor(color);
    toolbarTable.setColor(1,1,color.brighter());
    toolbarTable.setColor(1,3,color.darker());

    add(toolbarTable);

    Table toolbar1 = new Table();
    toolbar1.setCellpadding(0);
    toolbar1.setCellspacing(0);

    Table button = new Table(2,1);
 	button.setCellpadding(0);
    Image iconCrUser = iwb.getImage("new_user.gif");
    button.add(iconCrUser,1,1);
   	Text text = new Text("Nýr félagi");
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
	Text text2 = new Text("Nýr hópur");
 	text2.setFontFace(Text.FONT_FACE_VERDANA);
 	text2.setFontSize(Text.FONT_SIZE_7_HTML_1);
    Link tLink12 = new Link(text2);
    tLink12.setWindowToOpen(CreateGroupWindow.class);
    button2.add(tLink12,2,1);
    toolbar1.add(button2,3,1);


   //hitt
   //group
   //user
   //calendar
   toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Kladdar", iwb.getImage("calendar.gif"), com.idega.block.news.presentation.News.class),4,1);
   //history
   toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Saga", iwb.getImage("history.gif"), com.idega.block.news.presentation.News.class),5,1);
   //import
   toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Import", iwb.getImage("import.gif"), com.idega.block.news.presentation.News.class),6,1);
   //export
   toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Export", iwb.getImage("export.gif"), com.idega.block.news.presentation.News.class),7,1);
     //bread crumbs
//VANTAR



	toolbarTable.add(toolbar1,1,2);

  }


}
