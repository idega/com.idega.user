/*
 * $Id: UserContactSearch.java,v 1.9 2005/04/09 21:46:41 eiki Exp $ Created on
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
 * Last modified: $Date: 2005/04/09 21:46:41 $ by $Author: eiki $ This class
 * implements the Searchplugin interface and can therefore be used in a Search
 * block (com.idega.core.search). <br>
 * It searches lots of user related info like name, personalid,email etc. and
 * returns contact information for users. <br>
 * To use it simply register this class as a iw.searchplugin component in a
 * bundle.
 * 
 * @author <a href="mailto:eiki@idega.com">Eirikur S. Hrafnsson </a>
 * @version $Revision: 1.9 $
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
		List alreadyAddedContacts = new ArrayList();
		
		BasicSearch searcher = new BasicSearch();
		searcher.setSearchName(getSearchName());
		searcher.setSearchType(SEARCH_TYPE);
		searcher.setSearchQuery(searchQuery);
		Collection users = getUsers(searchQuery);
		
		if (users != null && !users.isEmpty()) {
			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				if(alreadyAddedContacts.contains(user.getPrimaryKey())){
					//don't add twice
					continue;
				}
				else{
					alreadyAddedContacts.add(user.getPrimaryKey());
					
					BasicSearchResult result = new BasicSearchResult();
					fillSearchResultType(result, user);
					fillSearchResultName(result,user);
					fillSearchResultURI(result,user);
					fillSearchResultAbstract(result,user);
					fillSearchResultExtraInformation(result,user);
					fillSearchResultAttributesMap(result,user);

					results.add(result);
				}
			}
		}
		searcher.setSearchResults(results);
		return searcher;
	}

	protected void fillSearchResultURI(BasicSearchResult result, User user) {
		Collection emails = user.getEmails();
		if (emails != null && !emails.isEmpty()) {
			String email = ((Email) emails.iterator().next()).getEmailAddress();
			result.setSearchResultURI("mailto:" + email);
		}
		else {
			result.setSearchResultURI("#");
		}
		
	}

	protected void fillSearchResultExtraInformation(BasicSearchResult result, User user) {
		Collection emails = user.getEmails();
		if (emails != null && !emails.isEmpty()) {
			String email = ((Email) emails.iterator().next()).getEmailAddress();
			result.setSearchResultExtraInformation(email);
		}
	}

	protected void fillSearchResultName(BasicSearchResult result, User user) {
		StringBuffer name = new StringBuffer();
		name.append(user.getName());
		result.setSearchResultName(name.toString());	
	}

	protected void fillSearchResultAbstract(BasicSearchResult result, User user) {
		boolean someThingAdded = false;
		StringBuffer abstractText = new StringBuffer();
		Collection phones = user.getPhones();
		Collection addresses = user.getAddresses();
		
		if (addresses != null && !addresses.isEmpty()) {
			abstractText.append(((Address) addresses.iterator().next()).getStreetAddress());
			someThingAdded = true;
		}
		
		if (phones != null && !phones.isEmpty()) {
			Iterator numbers = phones.iterator();
			
			while (numbers.hasNext()) {
				Phone phone = (Phone) numbers.next();
				String number = phone.getNumber();
				if (number!=null && !"".equals(number) && !"null".equals(number)) {
					if (someThingAdded) {
						abstractText.append(" - ");
					}
					abstractText.append(number);
					someThingAdded = true;
				}
				
			}
			
		}
		
		result.setSearchResultAbstract(abstractText.toString());
	}

	/**
	 * @param result
	 */
	protected void fillSearchResultType(BasicSearchResult result, User user) {
		result.setSearchResultType(SEARCH_TYPE);
	}

	/**
	 * Extend this method to add extra attributes to the search result. The method does nothing by default
	 * @param result
	 * @param user
	 */
	protected void fillSearchResultAttributesMap(BasicSearchResult result, User user) {}

	protected Collection getUsers(SearchQuery searchQuery) {
		try {
			SearchEngine userSearch = (SearchEngine) IBOLookup.getServiceInstance(iwma.getIWApplicationContext(),
					SearchEngine.class);
			String query = ((SimpleSearchQuery) searchQuery).getSimpleSearchQuery();
			if(query!=null){
				query = query.replace('*', '%');
				return userSearch.getSimpleSearchResults(query);
			}
			else{
				return ListUtil.getEmptyList();
			}
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