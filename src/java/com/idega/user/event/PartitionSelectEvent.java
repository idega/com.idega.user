package com.idega.user.event;

import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class PartitionSelectEvent extends IWPresentationEvent {

  private static String SELECTED_PARTITION = "sel_par";
  private static String PARTITION_SIZE = "par_size";
  private static String FIRST_PARTITION = "f_par";

  private int partitionSize = 20;
  private int selectedPartition = 0;
  private int firstPartition = 0;

  public void setSelectedPartition(int index){
    this.addParameter(SELECTED_PARTITION,index);
  }

  public void setPartitionSize(int size){
    this.addParameter(PARTITION_SIZE,size);
  }

  public void setFirstPartitionIndex(int index){
    this.addParameter(FIRST_PARTITION,index);
  }

  public int getSelectedPartition(){
    return this.selectedPartition;
  }

  public int getPartitionSize(){
    return this.partitionSize;
  }

  public int getFirstPartitionIndex(){
    return this.firstPartition;
  }


  public boolean initializeEvent(IWContext iwc) {
    try {
      this.partitionSize = Integer.parseInt(iwc.getParameter(PARTITION_SIZE));
      System.out.println("PartionSelectionEvent: partsize = "+this.partitionSize);
    }
    catch (NumberFormatException ex) {
      System.err.println(ex.getMessage());
      return false;
    }

    try {
      this.selectedPartition = Integer.parseInt(iwc.getParameter(SELECTED_PARTITION));
    }
    catch (NumberFormatException ex) {
      System.err.println(ex.getMessage());
      return false;
    }

    try {
      this.firstPartition = Integer.parseInt(iwc.getParameter(FIRST_PARTITION));
    }
    catch (NumberFormatException ex) {
//      System.err.println(ex.getMessage());
      this.firstPartition = 0;
    }

    return true;


  }

}