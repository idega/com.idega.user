/*
 * $Id: UserContactSearch.java,v 1.2 2005/01/19 01:50:17 eiki Exp $
 * Created on Jan 17, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
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


/**
 * 
 * Last modified: $Date: 2005/01/19 01:50:17 $ by $Author: eiki $
 * This class implements the Searchplugin interface and can therefore be used in a Search block for searching for user contact info.
 * To use it simply register this class as a iw.searchable component in a bundle.
 * 
 * @author <a href="mailto:eiki@idega.com">Eirikur S. Hrafnsson</a>
 * @version $Revision: 1.2 $
 */
public class UserContactSearch implements SearchPlugin {

	public static final String SEARCH_NAME_LOCALIZABLE_KEY = "user_contact_search.name";
	public static final String SEARCH_DESCRIPTION_LOCALIZABLE_KEY = "user_contact_search.description";
	public static final String SEARCH_TYPE = "user";
	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	private IWMainApplication iwma;
	
	
	/**
	 * 
	 */
	public UserContactSearch() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#getAdvancedSearchSupportedParameters()
	 */
	public List getAdvancedSearchSupportedParameters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsSimpleSearch()
	 */
	public boolean getSupportsSimpleSearch() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#getSupportsAdvancedSearch()
	 */
	public boolean getSupportsAdvancedSearch() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#initialize(com.idega.idegaweb.IWMainApplication)
	 */
	public boolean initialize(IWMainApplication iwma) {
		this.iwma = iwma;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#destroy(com.idega.idegaweb.IWMainApplication)
	 */
	public void destroy(IWMainApplication iwma) {
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#createSearch(com.idega.core.search.business.SearchQuery)
	 */
	public Search createSearch(SearchQuery searchQuery) {
		List results = new ArrayList();
		BasicSearch searcher = new BasicSearch();
		searcher.setSearchName(getSearchName());
		searcher.setSearchType(SEARCH_TYPE);
		searcher.setSearchQuery(searchQuery);
		
		try {
			SearchEngine userSearch = (SearchEngine) IBOLookup.getServiceInstance(iwma.getIWApplicationContext(),SearchEngine.class);
			Collection users = userSearch.getSimpleSearchResults( ((SimpleSearchQuery)searchQuery).getSimpleSearchQuery().replace('*','%'));
			
			if(users!=null && !users.isEmpty()){
				Iterator iter = users.iterator();
				while (iter.hasNext()) {
					User user = (User) iter.next();
					String name = user.getName();
					Collection emails = user.getEmails();
					
					BasicSearchResult result = new BasicSearchResult();
					result.setSearchResultType(SEARCH_TYPE);
					result.setSearchResultName(name);
					result.setSearchResultAbstract(user.getDescription());
					
					if(emails!=null && !emails.isEmpty()){
						result.setSearchResultURI("mailto:"+((Email)emails.iterator().next()).getEmailAddress());
					}

					results.add(result);
				}
			}
			
			searcher.setSearchResults(results);
//			add("Group name search:");
//			addBreak();
//			try {
//				GroupBusiness groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
	//
//				Collection groups = groupBusiness.getGroupsByGroupName(queryString.replace('*','%'));
//				List users = new ArrayList();
//				
//				if(groups!=null){
//					Iterator iterator = groups.iterator();
//					while (iterator.hasNext()) {
//						Group group = (Group) iterator.next();
//						try {
//							users.addAll(groupBusiness.getUsers(group));
//						}
//						catch (FinderException e1) {
//							e1.printStackTrace();
//						}
//					}
//				}
//				
//				if(!users.isEmpty()){
//					Iterator iterator = users.iterator();
//					while (iterator.hasNext()) {
//						User user = (User) iterator.next();
//						
//						add(user.getName());
//						add(" , email: ");
//						add(user.getEmails());
//						addBreak();
//					}
//				}
//				
//			}
//			catch (IBOLookupException e) {
//				e.printStackTrace();
//			}
//			catch (RemoteException e) {
//				e.printStackTrace();
//			}
//			
	//
//			addBreak();
//			add("Group type search:");
//			addBreak();
//			try {
//				GroupBusiness groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
////	find a better finder
//				Collection groups = groupBusiness.getGroupsByGroupTypeAndFirstPartOfName(queryString.replace('*','%').toLowerCase(),"");
//				List users = new ArrayList();
//				
//				if(groups!=null){
//					Iterator iterator = groups.iterator();
//					while (iterator.hasNext()) {
//						Group group = (Group) iterator.next();
//						try {
//							users.addAll(groupBusiness.getUsers(group));
//						}
//						catch (FinderException e1) {
//							e1.printStackTrace();
//						}
//					}
//				}
//				
//				if(!users.isEmpty()){
//					Iterator iterator = users.iterator();
//					while (iterator.hasNext()) {
//						User user = (User) iterator.next();
//						
//						add(user.getName());
//						add(" , email: ");
//						add(user.getEmails());
//						addBreak();
//					}
//				}
//				
//			}
//			catch (IBOLookupException e) {
//				e.printStackTrace();
//			}
//			catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return searcher;
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#getSearchName()
	 */
	public String getSearchName() {
		IWBundle bundle = iwma.getBundle(IW_BUNDLE_IDENTIFIER);
		return bundle.getResourceBundle(IWContext.getInstance()).getLocalizedString(SEARCH_NAME_LOCALIZABLE_KEY,"Contacts");
	}

	/* (non-Javadoc)
	 * @see com.idega.core.search.business.SearchPlugin#getSearchDescription()
	 */
	public String getSearchDescription() {
		IWBundle bundle = iwma.getBundle(IW_BUNDLE_IDENTIFIER);
		return bundle.getResourceBundle(IWContext.getInstance()).getLocalizedString(SEARCH_DESCRIPTION_LOCALIZABLE_KEY,"Searches for user contact information");
	}
	
}
