/*
 * $Id: UserContactSearch.java,v 1.1 2005/01/17 19:15:25 eiki Exp $
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
import java.util.List;
import java.util.Map;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.search.business.Searchable;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.user.data.User;


/**
 * 
 * Last modified: $Date: 2005/01/17 19:15:25 $ by $Author: eiki $
 * This class implements the Searchable interface and can therefore be used in a Search block for searching for user contact info.
 * To use it simply register this class as a iw.searchable component in a bundle.
 * 
 * @author <a href="mailto:eiki@idega.com">Eirikur S. Hrafnsson</a>
 * @version $Revision: 1.1 $
 */
public class UserContactSearch implements Searchable {

	public static final String SEARCH_NAME_LOCALIZABLE_KEY = "user_contact_search.name";
	public static final String DEFAULT_LINK_STYLE_CLASS = "user_contact_search_link";
	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private String linkStyleClass = DEFAULT_LINK_STYLE_CLASS;
	
	
	/**
	 * 
	 */
	public UserContactSearch() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#initialize(com.idega.presentation.IWContext)
	 */
	public boolean initialize(IWContext iwc) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#destroy(com.idega.presentation.IWContext)
	 */
	public void destroy(IWContext iwc) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getSimpleSearchResults(java.lang.String, com.idega.presentation.IWContext)
	 */
	public Collection getSimpleSearchResults(String queryString, IWContext iwc) {
		try {
			SearchEngine userSearch = (SearchEngine) IBOLookup.getServiceInstance(iwc,SearchEngine.class);
			return userSearch.getSimpleSearchResults(queryString.replace('*','%'));
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getAdvancedSearchResults(java.util.Map, com.idega.presentation.IWContext)
	 */
	public Collection getAdvancedSearchResults(Map queryMap, IWContext iwc) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getAdvancedSearchSupportedParameters()
	 */
	public List getAdvancedSearchSupportedParameters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getSupportsSimpleSearch()
	 */
	public boolean getSupportsSimpleSearch() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getSupportsAdvancedSearch()
	 */
	public boolean getSupportsAdvancedSearch() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getSearchName(com.idega.presentation.IWContext)
	 */
	public String getSearchName(IWContext iwc) {
		IWBundle bundle = iwc.getIWMainApplication().getBundle(IW_BUNDLE_IDENTIFIER);
		return bundle.getResourceBundle(iwc).getLocalizedString(SEARCH_NAME_LOCALIZABLE_KEY,"Contacts");
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.Searchable#getResultPresentation(java.lang.Object, com.idega.presentation.IWContext)
	 */
	public PresentationObject getResultPresentation(Object resultObject, IWContext iwc) {
		User user = (User)resultObject;
		String name = user.getName();
		String emails = user.getEmails().toString();
		Link link = new Link(name+ ", "+emails,"mailto:"+emails);
		link.setStyleClass(getLinkStyleClass());
		return link;
	}
	
	/**
	 * @return Returns the linkStyleClass.
	 */
	public String getLinkStyleClass() {
		return linkStyleClass;
	}
	/**
	 * @param linkStyleClass The linkStyleClass to set.
	 */
	public void setLinkStyleClass(String linkStyleClass) {
		this.linkStyleClass = linkStyleClass;
	}
	
}
