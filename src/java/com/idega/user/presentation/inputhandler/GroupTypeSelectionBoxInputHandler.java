/*
 * Created on Dec 10, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.idega.user.presentation.inputhandler;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.business.InputHandler;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.SelectionBox;
import com.idega.user.data.GroupType;
import com.idega.user.data.GroupTypeHome;

/**
 * @author jonas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class GroupTypeSelectionBoxInputHandler extends SelectionBox implements InputHandler {

	public GroupTypeSelectionBoxInputHandler() {
		super();
	}
	
	public GroupTypeSelectionBoxInputHandler(String name) {
		super(name);
	}
	
	
	public void main(IWContext iwc) {
		try {
			GroupTypeHome groupTypeHome = (GroupTypeHome) IDOLookup.getHome(GroupType.class);

			Collection groupTypes = groupTypeHome.findAllGroupTypes();

			if (groupTypes != null) {
				Iterator iter = groupTypes.iterator();
				while (iter.hasNext()) {
					GroupType groupType = groupTypeHome.findByPrimaryKey(iter.next());
					String name = groupType.getDefaultGroupName();
					
					addMenuElement(groupType.getPrimaryKey().toString(), name);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.idega.business.InputHandler#getHandlerObject(java.lang.String, java.lang.String, com.idega.presentation.IWContext)
	 */
	public PresentationObject getHandlerObject(String name, String stringValue, IWContext iwc) {
		this.setName(name);
		if (stringValue != null) {
			this.setContent(stringValue);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see com.idega.business.InputHandler#getResultingObject(java.lang.String[], com.idega.presentation.IWContext)
	 */
	public Object getResultingObject(String[] values, IWContext iwc) throws Exception {
		Collection groupTypes = null;
		int count = values.length;
		if (values != null && count > 0) {
			groupTypes = new ArrayList(count);
			GroupTypeHome groupTypeHome = (GroupTypeHome) IDOLookup.getHome(GroupType.class);
			for(int i=0; i<values.length; i++) {
				GroupType gType = groupTypeHome.findByPrimaryKey(values[i]);
				if(gType!=null) {
					groupTypes.add(gType);
				}
			}
		}

		return groupTypes;
	}

	/* (non-Javadoc)
	 * @see com.idega.business.InputHandler#getDisplayNameOfValue(java.lang.Object, com.idega.presentation.IWContext)
	 */
	public String getDisplayNameOfValue(Object value, IWContext iwc) {
		String result = null;
		if(value!=null && value instanceof Collection) {
			Collection groupTypes = (Collection) value;
			StringBuffer buf = new StringBuffer();
			Iterator gtIter = groupTypes.iterator();
			boolean isFirst = true;
			while(gtIter.hasNext()) {
				if(isFirst) {
					isFirst=false;
				} else {
					buf.append(", ");
				}
				GroupType gType = (GroupType) gtIter.next();
				String name = gType.getDefaultGroupName();
				buf.append(name);
			}
			result = buf.toString();
		}
		return result;
	}

}









