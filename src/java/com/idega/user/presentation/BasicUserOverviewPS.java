package com.idega.user.presentation;

import com.idega.builder.data.IBDomain;
import com.idega.user.event.SelectDomainEvent;
import com.idega.presentation.event.ResetPresentationEvent;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.presentation.event.TreeViewerEvent;
import com.idega.idegaweb.browser.event.IWBrowseEvent;
import java.util.Iterator;
import java.util.List;
import com.idega.presentation.Page;
import javax.swing.event.ChangeListener;
import com.idega.idegaweb.IWException;
import com.idega.event.*;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class BasicUserOverviewPS extends IWPresentationStateImpl implements IWActionListener {


//  String color1 = "00FF00";
//  String color2 = "FF0000";
//  String color = color1;

  Group _selectedGroup = null;
  IBDomain _selectedDomain = null;

  public BasicUserOverviewPS() {

  }

  public Group getSelectedGroup(){
    return _selectedGroup;
  }

  public IBDomain getSelectedDomain(){
    return _selectedDomain;
  }

  public void reset(){
    super.reset();
    _selectedGroup = null;
    _selectedDomain = null;
  }


//  public String getColor(){
//    return color;
//  }



  public void actionPerformed(IWPresentationEvent e)throws IWException{

    if(e instanceof ResetPresentationEvent){
      this.reset();
      this.fireStateChanged();
    }

//    System.out.println("!!!!ÍJHHAAAAAAAAA!!!!!!!!!!!!!!");

//    Page pg = e.getPage();
//     boolean remove = false;
//    if(e.getPage() instanceof ChangeListener ){
//      System.out.println("is ChangeListener: true");
//      this.addChangeListener((ChangeListener)e.getPage());
//      remove = true;
//    }

//    else {
//      System.out.println("is ChangeListener: false");
//      List l = pg.getAllContainingObjects();
//      if(l != null){
//        Iterator iter = l.iterator();
//        while (iter.hasNext()) {
//          Object item = iter.next();
//          if(item instanceof ChangeListener ){
//            this.addChangeListener((ChangeListener)item);
//          }
//        }
//      }
//    }
//    System.out.println("[BasicUserOverviewPS]: e = "+e);

    if(e instanceof SelectGroupEvent){
      _selectedGroup = ((SelectGroupEvent)e).getSelectedGroup();
      _selectedDomain = null;
      this.fireStateChanged();
    }

    if(e instanceof SelectDomainEvent){
      _selectedDomain = ((SelectDomainEvent)e).getSelectedDomain();
      _selectedGroup = null;
      this.fireStateChanged();
    }


//    if(remove){
//      this.removeChangeListener((ChangeListener)e.getPage());
//    }
  }


}