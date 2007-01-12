package com.idega.user.event;

import com.idega.core.builder.data.ICDomain;
import com.idega.data.IDOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class SelectDomainEvent extends IWPresentationEvent {

  private Integer _selectedDomain = null;
  private static final String PRM_DOMAIN_ID = "ib_dm_id";

  public SelectDomainEvent() {
  }

  public void setDomainToSelect(Integer primaryKey){
    this.addParameter(PRM_DOMAIN_ID, primaryKey.toString());
  }

  public void setDomainToSelect(int nodeId){
    this.addParameter(PRM_DOMAIN_ID, nodeId);
  }


  public ICDomain getSelectedDomain(){
    if(this._selectedDomain != null){
      try {
        return (ICDomain)IDOLookup.findByPrimaryKey(ICDomain.class,this._selectedDomain);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  public boolean initializeEvent(IWContext iwc) {

    try {
      this._selectedDomain = new Integer(iwc.getParameter(PRM_DOMAIN_ID));
      return true;
    }
    catch (NullPointerException ex) {
      return false;
    } catch (NumberFormatException e){
      e.printStackTrace();
      return false;
    }

  }
}