/*
 * Created on Jan 25, 2005
 */
package com.idega.user.presentation.inputhandler;

import java.rmi.RemoteException;
import java.util.Collection;

import com.idega.business.IBOLookup;
import com.idega.business.InputHandler;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.presentation.GroupChooser;

/**
 * @author Sigtryggur
 */
public class GroopChooserInputHandler extends GroupChooser implements InputHandler {

    private boolean isInitialized = false;
    protected GroupBusiness groupBiz = null;
    protected UserBusiness userBiz = null;


    public GroopChooserInputHandler() {
		super();
	}
	
	public GroopChooserInputHandler(String name) {
		super(name);
	}
    public Object convertSingleResultingObjectToType(Object value, String className) {
		return value;
    }

    public String getDisplayForResultingObject(Object value, IWContext iwc) {
        String groupID = (String)value;
        Group group = null;
        if (groupID != null && !groupID.equals("")) {
	        groupID = groupID.substring(groupID.lastIndexOf("_")+1);    
        }
        try {
            group = getGroupBusiness().getGroupByGroupID(Integer.parseInt((groupID)));
            return group.getName();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return groupID;
        
    }

	public PresentationObject getHandlerObject(String name, Collection values, IWContext iwc) {
        return null;
    }

    public PresentationObject getHandlerObject(String name,	String value, IWContext iwc) {
        this.setName(name);
		if (value != null) {
			this.setValue(value);
		}
		Collection groups = null;
		try {
		    groups = this.getUserBusiness().getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
		} catch (RemoteException e) {
		    e.printStackTrace();
		}
		if (groups != null && !groups.isEmpty()) {
		    //System.out.println(groups.iterator().next().getClass());
		    Group group = (Group)groups.iterator().next();
		    if (group != null) {
		        this.setSelectedGroup(group.getPrimaryKey().toString(),group.getName());
		    }
		}
		return this;
	}

	public Object getResultingObject(String[] value, IWContext iwc)	throws Exception {
	    String groupID = null;
		if (value != null && value.length == 1)
			groupID = value[0];
		return groupID;
	}
	
	private GroupBusiness getGroupBusiness() throws RemoteException {
		if (groupBiz == null) {
			groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(this.getIWApplicationContext(), GroupBusiness.class);
		}	
		return groupBiz;
	}

	private UserBusiness getUserBusiness() throws RemoteException {
		if (userBiz == null) {
			userBiz = (UserBusiness) IBOLookup.getServiceInstance(this.getIWApplicationContext(), UserBusiness.class);
		}	
		return userBiz;
	}
}