package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.RemoveException;

import com.idega.builder.data.IBDomain;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.user.data.Group;
import com.idega.user.event.DeleteGroupEvent;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class DeleteGroupConfirmWindowPS extends IWPresentationStateImpl implements IWActionListener {
  
  
	/**
	 * @see com.idega.event.IWActionListener#actionPerformed(com.idega.event.IWPresentationEvent)
	 */
	public void actionPerformed(IWPresentationEvent e) throws IWException {
    if (e instanceof DeleteGroupEvent)  {
      DeleteGroupEvent event = (DeleteGroupEvent) e;
      if (event.isDeletingConfirmed())  {
        Group group = event.getGroup();
        Group parentGroup = event.getParentGroup();
        IBDomain parentDomain = event.getParentDomain();
        try { 
          if (group.getGroupType().equals("alias") || group.getChildCount() <= 0)  {
            if (parentGroup != null)
              parentGroup.removeGroup(group);
            else if (parentDomain != null)  {
              int i = ((Integer)parentDomain.getPrimaryKey()).intValue();
              group.removeRelation( i, "TOP_NODE");
            }
          }
        }
         catch (RemoteException ex)  {
        }
        catch (RemoveException ex)  {
        }
        this.fireStateChanged();
      }
      
    }
	}
}
