/*
 * $Id: SearchEngineHome.java,v 1.3 2005/01/17 00:33:34 eiki Exp $
 * Created on Jan 17, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.user.block.search.business;

import com.idega.business.IBOHome;


/**
 * 
 *  Last modified: $Date: 2005/01/17 00:33:34 $ by $Author: eiki $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.3 $
 */
public interface SearchEngineHome extends IBOHome {

	public SearchEngine create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
