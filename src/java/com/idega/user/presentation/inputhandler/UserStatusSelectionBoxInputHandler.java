/*
 * Created on Jan 23, 2005
 */
package com.idega.user.presentation.inputhandler;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;

import com.idega.business.InputHandler;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.SelectionBox;
import com.idega.user.data.Status;
import com.idega.user.data.StatusHome;

/**
 * @author Sigtryggur
 */
public class UserStatusSelectionBoxInputHandler extends SelectionBox implements InputHandler {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private List _allUserStatuses = new ArrayList();
	
	public UserStatusSelectionBoxInputHandler() {
		super();
	}
	
	public UserStatusSelectionBoxInputHandler(String name) {
		super(name);
	}
	
	public void main(IWContext iwc) {
        initialize(iwc);
        super.main(iwc);
	}
	
	private void initialize(IWContext iwc) {
		try {
			final IWResourceBundle iwrb = this.getResourceBundle(iwc);
			List userStatuses = (List) ((StatusHome)IDOLookup.getHome(Status.class)).findAll();
			
			if (userStatuses != null && !userStatuses.isEmpty()) {
			    final Collator collator = Collator.getInstance(iwc.getLocale());
				Collections.sort(userStatuses,new Comparator() {
					public int compare(Object arg0, Object arg1) {
						return collator.compare(iwrb.getLocalizedString(((Status) arg0).getStatusKey(), ((Status) arg0).getStatusKey()), iwrb.getLocalizedString(((Status) arg1).getStatusKey(), ((Status) arg1).getStatusKey()));
					}				
				});
				Iterator iter = userStatuses.iterator();
				while (iter.hasNext()) {
				    Status status = (Status) iter.next();
					String name = status.getStatusKey();
					if(name!=null) {
						addMenuElement(name, iwrb.getLocalizedString(name,name));
						_allUserStatuses.add(name);
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
	}

	public PresentationObject getHandlerObject(String name, String value, IWContext iwc) {
		this.setName(name);
		if (value != null) {
			this.setSelectedElement(value);
		}
		return this;
	}

	public Object getResultingObject(String[] values, IWContext iwc) throws Exception {
		Collection userStatuses = null;
		if (values != null && values.length > 0) {
			userStatuses = new ArrayList();
			
			for(int i=0; i<values.length; i++) {
				userStatuses.add(values[i]);
			}
		} else {
			userStatuses = _allUserStatuses;
		}

		return userStatuses;
	}

	public String getDisplayForResultingObject(Object value, IWContext iwc) {
	    Collection userStatuses = (Collection) value;
		if (userStatuses == null || userStatuses.isEmpty()) {
			return this.getResourceBundle(iwc).getLocalizedString("UserStatusSelectionBox.none_selected","None Selected");
		}
		String result = null;
		IWResourceBundle iwrb = getResourceBundle(iwc);
		if(userStatuses != null) {
			StringBuffer buf = new StringBuffer();
			Iterator gtIter = userStatuses.iterator();
			boolean isFirst = true;
			while(gtIter.hasNext()) {
				if(isFirst) {
					isFirst=false;
				} else {
					buf.append(",");
				}
				String name = (String) gtIter.next();
				
				buf.append(iwrb.getLocalizedString(name,name));
			}
			result = buf.toString();
		}
		return result;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public PresentationObject getHandlerObject(String name, Collection values, IWContext iwc) {
		SelectionBox box = (SelectionBox) getHandlerObject(name, (String) null, iwc);
		if (values != null) {
			Iterator iterator = values.iterator();
			while (iterator.hasNext()) {
				String value = (String) iterator.next(); 
				box.setSelectedElement(value);
			}
		}
		return box;		
	}

	public Object convertSingleResultingObjectToType(Object value, String className) {
		return value;
	}
}