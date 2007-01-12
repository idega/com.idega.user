package com.idega.user.presentation;

import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class BacicUserOverviewPresentationState extends IWPresentationStateImpl implements IWActionListener {


  String color1 = "00FF00";
  String color2 = "FF0000";
  String color = this.color1;

  public BacicUserOverviewPresentationState() {

  }


  public String getColor(){
    return this.color;
  }



  public void actionPerformed(IWPresentationEvent e)throws IWException{
//    System.out.println("!!!!�JHHAAAAAAAAA!!!!!!!!!!!!!!");

    if(this.color == this.color1){
      this.color = this.color2;
    } else {
      this.color = this.color1;
    }

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
    this.fireStateChanged();
//    if(remove){
//      this.removeChangeListener((ChangeListener)e.getPage());
//    }
  }


}