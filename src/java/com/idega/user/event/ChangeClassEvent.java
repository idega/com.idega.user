package com.idega.user.event;

import com.idega.presentation.IWContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.event.*;


/**
 * <p>Title: com.idega.user.event.ChangeClassEvent</p>
 * <p>Description: Event that contains a class to instanciate similar to ObjectInstanciator</p>
 * <p>Copyright: (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="eiki@idega.is">EirikurS. Hrafnsson</a>
 * @version 1.0
 */

public class ChangeClassEvent extends IWPresentationEvent {
	
  private final String ENCRYPTED_CLASS_PARAMETER = IWMainApplication.classToInstanciateParameter;
  private String _className;

  public ChangeClassEvent(Class theClass){
	setChangeClass(theClass);
  }
  
  public void setChangeClass(Class theClass){
    addParameter(ENCRYPTED_CLASS_PARAMETER,IWMainApplication.getEncryptedClassName(theClass));
  }
  
  public String getChangeClassName(){
    return _className;
  }
  
  private void setChangeClassName(String theClass){
    _className = theClass;
  }


  public boolean initializeEvent(IWContext iwc) {
    //get the encrypted class name
    String className = iwc.getParameter(ENCRYPTED_CLASS_PARAMETER);
   	if( className != null ){
   		setChangeClassName(IWMainApplication.decryptClassName(className));
 		return true;
   	}
    
    return false;


  }

}