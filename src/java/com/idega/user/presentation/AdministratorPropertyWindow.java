/*
 * $Id: AdministratorPropertyWindow.java,v 1.8.2.2 2007/05/29 08:11:10 laddi Exp $
 *
 * Copyright (C) 2001-2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package com.idega.user.presentation;

import com.idega.presentation.IWContext;
import com.idega.presentation.TabbedPropertyPanel;

/**
 * <p>
 * This is a special implementation of the Usser property window that is only opened for the Super Administrator user.
 * </p>
 * Last modified: $Date: 2007/05/29 08:11:10 $ by $Author: laddi $
 *
 * @author <a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson</a>,<a href="mailto:tryggvil@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.8.2.2 $
 */
public class AdministratorPropertyWindow extends UserPropertyWindow {

	public final String SESSION_ADDRESS = "ic_admin_property_window", IW_SESSION_ADDRESS1 = SESSION_ADDRESS;

	public AdministratorPropertyWindow() {
		super();
	}

	@Override
	public String getSessionAddressString() {
		return this.IW_SESSION_ADDRESS1;
	}

	/**
	 * <p>
	 * Overriding the method to only display login tab.
	 * </p>
	 */
	@Override
	public void initializePanel(IWContext iwc, TabbedPropertyPanel panel) {

		initializeUserAndGroup(iwc);

		UserLoginTab ult = new UserLoginTab();
		ult.setPanel(panel);
		ult.setUserID(getUserId());
		ult.setGroupID(getGroupId());

		panel.addTab(ult, 0, iwc);
	}
}