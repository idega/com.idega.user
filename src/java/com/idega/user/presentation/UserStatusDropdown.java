package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.user.data.Status;
import com.idega.user.data.StatusHome;


/**
 * @author gimmi
 */
public class UserStatusDropdown extends DropdownMenu{

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private Collection statuses;
	private IWResourceBundle iwrb;

	public UserStatusDropdown(String name) {
		super(name);
	}

	public void init(IWContext iwc) {
		try {
			iwrb = getResourceBundle(iwc);

			StatusHome sHome = (StatusHome) IDOLookup.getHome(Status.class);
			statuses = sHome.findAll();
		} catch (IDOLookupException e) {
			e.printStackTrace(System.err);
		} catch (FinderException e) {
			e.printStackTrace(System.err);
		}
	}

	public void main(IWContext iwc) throws Exception{
		init(iwc);
		
		if (statuses != null) {
			Status status;
			String key;
			Iterator iter = statuses.iterator();
			while (iter.hasNext()) {
				status = (Status) iter.next();
				key = status.getStatusKey();
				addMenuElement(status.getPrimaryKey().toString(), iwrb.getLocalizedString(key, key));	
			}
		}
		super.main(iwc);
	}
	
	public String getBundleIdentifier(){
	  return IW_BUNDLE_IDENTIFIER;
	}
}
