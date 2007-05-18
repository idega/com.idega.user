package com.idega.user.business.search;

import java.util.Collection;

import org.jdom.Document;

import com.idega.business.IBOService;

public interface UserSearchEngine extends IBOService {
	
	public Collection getSearchResults(String searchKey);
	
	public Document getUserBrowser(String searchKey);
	
}