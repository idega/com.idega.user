/*
 * $Id: UserContactSearch.java,v 1.5 2005/01/19 23:59:27 eiki Exp $ Created on
 * Jan 17, 2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.user.block.search.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.search.business.Search;
import com.idega.core.search.business.SearchPlugin;
import com.idega.core.search.business.SearchQuery;
import com.idega.core.search.data.BasicSearch;
import com.idega.core.search.data.BasicSearchResult;
import com.idega.core.search.data.SimpleSearchQuery;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.ListUtil;

/**
 * 
 * Last modified: $Date: 2005/01/19 23:59:27 $ by $Author: eiki $ This class
 * implements the Searchplugin interface and can therefore be used in a Search
 * block (com.idega.core.search). <br>
 * It searches lots of user related info like name, personalid,email etc. and
 * returns contact information for users. <br>
 * To use it simply register this class as a iw.searchplugin component in a
 * bundle.
 * 
 * @author <a href="mailto:eiki@idega.com">Eirikur S. Hrafnsson </a>
 * @version $Revision: 1.5 $
 */
public class UserContactSearch implements SearchPlugin {

	public static final String SEARCH_NAME_LOCALIZABLE_KEY = "user_contact_search.name";

	public static final String SEARCH_DESCRIPTION_LOCALIZABLE_KEY = "user_contact_search.description";

	public static final String SEARCH_TYPE = "user";

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	protected IWMainApplication iwma;

	/**
	 *  
	 */
	public UserContactSearch() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#getAdvancedSearchSupportedParameters()
	 */
	public List getAdvancedSearchSupportedParameters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsSimpleSearch()
	 */
	public boolean getSupportsSimpleSearch() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsAdvancedSearch()
	 */
	public boolean getSupportsAdvancedSearch() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#initialize(com.idega.idegaweb.IWMainApplication)
	 */
	public boolean initialize(IWMainApplication iwma) {
		this.iwma = iwma;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#destroy(com.idega.idegaweb.IWMainApplication)
	 */
	public void destroy(IWMainApplication iwma) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#createSearch(com.idega.core.search.business.SearchQuery)
	 */
	public Search createSearch(SearchQuery searchQuery) {
		List results = new ArrayList();
		BasicSearch searcher = new BasicSearch();
		searcher.setSearchName(getSearchName());
		searcher.setSearchType(SEARCH_TYPE);
		searcher.setSearchQuery(searchQuery);
		Collection users = getUsers(searchQuery);
		if (users != null && !users.isEmpty()) {
			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				StringBuffer name = new StringBuffer();
				name.append(user.getName());
				StringBuffer abstractText = new StringBuffer();
				Collection emails = user.getEmails();
				Collection phones = user.getPhones();
				Collection addresses = user.getAddresses();
				boolean someThingAdded = false;
				if (addresses != null && !addresses.isEmpty()) {
					abstractText.append(((Address) addresses.iterator().next()).getStreetAddress());
					someThingAdded = true;
				}
				if (phones != null && !phones.isEmpty()) {
					String number = ((Phone) phones.iterator().next()).getNumber();
					if (!"".equals(number) && !"null".equals(number)) {
						if (someThingAdded) {
							abstractText.append(" - ");
						}
						abstractText.append(number);
						someThingAdded = true;
					}
				}
				BasicSearchResult result = new BasicSearchResult();
				result.setSearchResultType(SEARCH_TYPE);
				if (emails != null && !emails.isEmpty()) {
					String email = ((Email) emails.iterator().next()).getEmailAddress();
					result.setSearchResultExtraInformation(email);
					result.setSearchResultURI("mailto:" + email);
				}
				else {
					result.setSearchResultURI("#");
				}
				result.setSearchResultName(name.toString());
				result.setSearchResultAbstract(abstractText.toString());
				results.add(result);
			}
		}
		searcher.setSearchResults(results);
		return searcher;
	}

	protected Collection getUsers(SearchQuery searchQuery) {
		try {
			SearchEngine userSearch = (SearchEngine) IBOLookup.getServiceInstance(iwma.getIWApplicationContext(),
					SearchEngine.class);
			Collection users = userSearch.getSimpleSearchResults(((SimpleSearchQuery) searchQuery).getSimpleSearchQuery().replace(
					'*', '%'));
			return users;
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return ListUtil.getEmptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#getSearchName()
	 */
	public String getSearchName() {
		IWBundle bundle = iwma.getBundle(IW_BUNDLE_IDENTIFIER);
		return bundle.getResourceBundle(IWContext.getInstance()).getLocalizedString(SEARCH_NAME_LOCALIZABLE_KEY,
				"Contacts");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.core.search.business.SearchPlugin#getSearchDescription()
	 */
	public String getSearchDescription() {
		IWBundle bundle = iwma.getBundle(IW_BUNDLE_IDENTIFIER);
		return bundle.getResourceBundle(IWContext.getInstance()).getLocalizedString(SEARCH_DESCRIPTION_LOCALIZABLE_KEY,
				"Searches for user contact information");
	}
}