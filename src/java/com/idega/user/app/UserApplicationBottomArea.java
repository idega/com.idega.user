/*
 * Created on 14.8.2004
 */
package com.idega.user.app;

import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;


/**
 * @author laddi
 */
public class UserApplicationBottomArea extends Page implements IWBrowserView {
	
	private String styleScript = "DefaultStyle.css";
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	public void main(IWContext iwc) throws Exception {
		IWBundle iwb = getBundle(iwc);
		IWResourceBundle iwrb = getResourceBundle(iwc);
		Page parentPage = this.getParentPage();
		String styleSrc = iwb.getVirtualPathWithFileNameString(styleScript);
		parentPage.addStyleSheetURL(styleSrc);
		setAllMargins(0);

		Table table = new Table(1, 1);
		table.setCellpaddingAndCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setAlignment(1, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setCellpaddingRight(1, 1, 16);
		
		Text text = new Text(iwrb.getLocalizedString("bottom_message", "© 2000-2003 | idega software |ÊEngjavegi 6 | 104 Reykjavik | Iceland |ÊTel. +354 554 7557 |ÊFax +354 554 7749 |Êidega@idega.is"));
		text.setStyleClass("bottomStyleText");
		table.add(text, 1, 1);
		
		add(table);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.browser.presentation.IWBrowserView#setControlTarget(java.lang.String)
	 */
	public void setControlTarget(String target) {
	}

	/* (non-Javadoc)
	 * @see com.idega.idegaweb.browser.presentation.IWBrowserView#setControlEventModel(com.idega.event.IWPresentationEvent)
	 */
	public void setControlEventModel(IWPresentationEvent model) {
	}
}