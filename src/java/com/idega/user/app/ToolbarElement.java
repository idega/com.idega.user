package com.idega.user.app;
import java.util.Map;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public interface ToolbarElement {

  Image getButtonImage(IWContext iwc);
  boolean isButton(IWContext iwc);
  String getName(IWContext iwc);
  Class getPresentationObjectClass(IWContext iwc);
  Map getParameterMap(IWContext iwc);
  boolean isValid(IWContext iwc);
  int getPriority(IWContext iwc);
  

}