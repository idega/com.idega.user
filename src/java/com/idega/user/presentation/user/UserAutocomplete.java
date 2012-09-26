package com.idega.user.presentation.user;

import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.IWUIBase;
import com.idega.user.business.UserConstants;

public class UserAutocomplete extends IWUIBase{

	IWResourceBundle iwrb = null;
	@Override
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = getIwc(context);
		setTag("input");
		setMarkupAttribute("value", "Not implemented yet");
	}
	
	
	protected IWResourceBundle getIwrb() {
		if(iwrb == null){
			iwrb = getIwc().getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(getIwc());
		}
		return iwrb;
	}

	protected void setIwrb(IWResourceBundle iwrb) {
		this.iwrb = iwrb;
	}
	
	@Override
	public List<String> getScripts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getStyleSheets() {
		// TODO Auto-generated method stub
		return null;
	}

}
