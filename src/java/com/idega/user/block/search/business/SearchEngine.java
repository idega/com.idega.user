/*
 * $Id: SearchEngine.java,v 1.5 2005/01/17 00:33:34 eiki Exp $
 * Created on Jan 17, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.user.block.search.business;

import java.rmi.RemoteException;
import java.util.Collection;
import com.idega.business.IBOService;
import com.idega.user.block.search.event.UserSearchEvent;


/**
 * 
 *  Last modified: $Date: 2005/01/17 00:33:34 $ by $Author: eiki $
 * 
 * @author <a href="mailto:gummi@idega.com">Gummi</a>
 * @version $Revision: 1.5 $
 */
public interface SearchEngine extends IBOService {

	/**
	 * @see com.idega.user.block.search.business.SearchEngineBean#getResult
	 */
	public Collection getResult(UserSearchEvent e) throws RemoteException;

	/**
	 * @see com.idega.user.block.search.business.SearchEngineBean#getResultType
	 */
	public Class getResultType(UserSearchEvent e) throws java.rmi.RemoteException;

	/**
	 * @see com.idega.user.block.search.business.SearchEngineBean#getSimpleSearchResults
	 */
	public Collection getSimpleSearchResults(String searchString) throws RemoteException;
}
