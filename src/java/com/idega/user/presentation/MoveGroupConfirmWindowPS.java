package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import com.idega.core.builder.data.ICDomain;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.event.MoveGroupEvent;

public class MoveGroupConfirmWindowPS extends IWPresentationStateImpl implements
		IWActionListener {

	public void actionPerformed(IWPresentationEvent e) throws IWException {
		if (e instanceof MoveGroupEvent)  {
			MoveGroupEvent event = (MoveGroupEvent) e;
			if (event.isMoveConfirmed())  {
				Group group = event.getGroup();
				Group oldParentGroup = event.getOldParentGroup();
				Group newParentGroup = event.getNewParentGroup();
				IWApplicationContext iwac = e.getIWContext().getApplicationContext();
				GroupBusiness groupBusiness = getGroupBusiness(iwac);
				try {
						
						
					//TODO fix this
					e.getIWContext().getApplicationContext().removeApplicationAttribute("domain_group_tree");
					e.getIWContext().getApplicationContext().removeApplicationAttribute("group_tree");			
					this.fireStateChanged();
				}
				catch (EJBException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private GroupBusiness getGroupBusiness(IWApplicationContext iwac)    {
		try {
			return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		}
		catch (java.rmi.RemoteException rme) {
			throw new RuntimeException(rme.getMessage());
		}
	}      
}