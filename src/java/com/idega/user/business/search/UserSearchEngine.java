package com.idega.user.business.search;

import java.util.Collection;

import org.jdom2.Document;

import com.idega.business.IBOService;
import com.idega.user.data.User;

public interface UserSearchEngine extends IBOService {

	public Collection<User> getSearchResults(String searchKey);

	public Document getUserBrowser(String searchKey);

}