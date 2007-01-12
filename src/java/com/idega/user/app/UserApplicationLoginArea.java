/*
 * Created on Nov 10, 2003
 */
package com.idega.user.app;

import com.idega.block.login.presentation.Login;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.Table;

/**
 * Description: <br>
 * Copyright: Idega Software 2003 <br>
 * Company: Idega Software <br>
 * @author <a href="mailto:birna@idega.is">Birna Iris Jonsdottir</a>
 */
public class UserApplicationLoginArea extends Page  implements IWBrowserView{
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private Login login = null;
	private IWPresentationEvent _contolEvent = null;
	private String _controlTarget = null;
	
	public UserApplicationLoginArea() {
		this.setAllMargins(0);
	}
	
	public void main(IWContext iwc) throws Exception {
		this.empty();	
		
		Table loginTable = displayTable(iwc);
		this.add(loginTable);	
	}
	
	public Table displayTable(IWContext iwc) {
		IWBundle iwb = getBundle(iwc);
		Image logoutImage = iwb.getImage("logout.gif");
		Image lockImage = iwb.getImage("las.gif");
		
		this.login = new Login();
		this.login.setLogoutButton(logoutImage);
  	
		Table loginTable = new Table(2,1);
		loginTable.setCellspacing(0);
		loginTable.setCellpadding(0);
		loginTable.setWidth(Table.HUNDRED_PERCENT);
		loginTable.setHeight(50);
		loginTable.setAlignment(1,1,"center");
		loginTable.setAlignment(2,1,"center");
		loginTable.setVerticalAlignment(1,1,"middle");
		loginTable.setVerticalAlignment(2,1,"middle");
		if(iwc.isLoggedOn()) {
			loginTable.add(lockImage,1,1);
		}
		loginTable.add(this.login,2,1);
  	
  	
		return loginTable;
	}


	
	public String getBundleIdentifier(){
		return IW_BUNDLE_IDENTIFIER;
	}
	public void setControlEventModel(IWPresentationEvent model){
//		System.out.print("UserApplicationControlArea: setControlEventModel(IWPresentationEvent model)");
		this._contolEvent = model;
	}
	public void setControlTarget(String controlTarget){
//		System.out.print("UserApplicationControlArea: setControlTarget(String controlTarget)");
		this._controlTarget = controlTarget;
	}

}
