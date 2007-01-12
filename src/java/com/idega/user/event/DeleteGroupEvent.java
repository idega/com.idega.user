package com.idega.user.event;

import com.idega.core.builder.data.ICDomain;
import com.idega.data.IDOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class DeleteGroupEvent extends IWPresentationEvent {

  public static final String OKAY_KEY = "okay_key";
  public static final String CANCEL_KEY = "cancel_key";
  
  private Integer groupId = null;
  private Integer parentGroupId = null;
  private Integer parentDomainId = null;
  
  public static final String GROUP_ID = "group_id";
  public static final String PARENT_GROUP_ID = "parent_group_id";
  public static final String PARENT_DOMAIN_ID = "parent_domain_id";
  private boolean okay = false;

  public DeleteGroupEvent() {
  }


  /**
   * @see com.idega.event.IWPresentationEvent#initializeEvent(com.idega.presentation.IWContext)
   */
  public boolean initializeEvent(IWContext iwc) {
    this.okay = iwc.isParameterSet(OKAY_KEY);
    
    if (iwc.isParameterSet(GROUP_ID)) {
		this.groupId = new Integer(iwc.getParameter(GROUP_ID));
	}
      
    if (iwc.isParameterSet(PARENT_GROUP_ID)) {
		this.parentGroupId = new Integer(iwc.getParameter(PARENT_GROUP_ID));
	}
      
    if (iwc.isParameterSet(PARENT_DOMAIN_ID)) {
		this.parentDomainId = new Integer(iwc.getParameter(PARENT_DOMAIN_ID));
	}  
    return true;
  }


  public boolean isDeletingConfirmed()  {
    return this.okay;
  }

  public Group getGroup(){
    if(this.groupId != null && (! new Integer(-1).equals(this.groupId))) {
      try {
        return (Group)IDOLookup.findByPrimaryKey(Group.class, this.groupId);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }  
  

  public Group getParentGroup(){
    if(this.parentGroupId != null && (! new Integer(-1).equals(this.parentGroupId))) {
      try {
        return (Group)IDOLookup.findByPrimaryKey(Group.class, this.parentGroupId);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }    

  public ICDomain getParentDomain(){
    if(this.parentDomainId != null && (! new Integer(-1).equals(this.parentDomainId))) {
      try {
        return (ICDomain)IDOLookup.findByPrimaryKey(ICDomain.class, this.parentDomainId);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }    
  
  
	/**
	 * Sets the groupId.
	 * @param groupId The groupId to set
	 */
	public void setGroupId(Integer primaryKey) {
    this.addParameter(GROUP_ID, primaryKey.toString());;
	}

	/**
	 * Sets the parentDomainId.
	 * @param parentDomainId The parentDomainId to set
	 */
	public void setParentDomainId(Integer primaryKey) {
    this.addParameter(PARENT_DOMAIN_ID, primaryKey.toString());
	}

	/**
	 * Sets the parentGroupId.
	 * @param parentGroupId The parentGroupId to set
	 */
	public void setParentGroupId(Integer primaryKey) {
    this.addParameter(PARENT_GROUP_ID, primaryKey.toString());
	}

}
