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

  public Image getButtonImage(IWContext iwc);
  public String getName(IWContext iwc);
  public Class getPresentationObjectClass(IWContext iwc);
  public Map getParameterMap(IWContext iwc);
  public boolean isValid(IWContext iwc);
  public int getPriority(IWContext iwc);

}