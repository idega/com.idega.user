package com.idega.user.presentation;

import com.idega.user.data.Group;
import com.idega.user.data.UserGroupPlugIn;

/**
 * Title:        idegaWeb User Subsystem
 * Description:  idegaWeb User Subsystem is the base system for Users and Group management
 * Copyright:    Copyright (c) 2002
 * Company:      idega
 * @author <a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
 * @version 1.0
 */

public interface UserGroupPlugInPresentable{

  //public void initialize(IWApplicationContext iwac);
  public void initialize(Group group);
  public UserGroupPlugIn getPlugIn();

}