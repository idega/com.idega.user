package com.idega.user.event;

import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
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
    return selectedPartition;
  }

  public int getPartitionSize(){
    return partitionSize;
  }

  public int getFirstPartitionIndex(){
    return firstPartition;
  }


  public boolean initializeEvent(IWContext iwc) {
    try {
      partitionSize = Integer.parseInt(iwc.getParameter(PARTITION_SIZE));
      System.out.println("PartionSelectionEvent: partsize = "+partitionSize);
    }
    catch (NumberFormatException ex) {
      System.err.println(ex.getMessage());
      return false;
    }

    try {
      selectedPartition = Integer.parseInt(iwc.getParameter(SELECTED_PARTITION));
    }
    catch (NumberFormatException ex) {
      System.err.println(ex.getMessage());
      return false;
    }

    try {
      firstPartition = Integer.parseInt(iwc.getParameter(FIRST_PARTITION));
    }
    catch (NumberFormatException ex) {
//      System.err.println(ex.getMessage());
      firstPartition = 0;
    }

    return true;


  }

}