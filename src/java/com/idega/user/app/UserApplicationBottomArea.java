/*
 * Created on 14.8.2004
 */
package com.idega.user.app;

import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;


/**
 * @author laddi
 */
public class UserApplicationBottomArea extends Page implements IWBrowserView {
	
	private String styleScript = "DefaultStyle.css";

	public void main(IWContext iwc) throws Exception {
		IWBundle iwb = getBundle(iwc);
		//IWResourceBundle iwrb = getResourceBundle(iwc);
		Page parentPage = this.getParentPage();
		String styleSrc = iwb.getVirtualPathWithFileNameString(styleScript);
		parentPage.addStyleSheetURL(styleSrc);
		
		this.setAllMargins(0);

		Table table = new Table(1, 1);
		table.setCellpaddingAndCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(35);
		table.setAlignment(1, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setCellpaddingRight(1, 1, 7);
		
		add(table);
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