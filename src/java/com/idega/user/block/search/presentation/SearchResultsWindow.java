/**
 * Title:        A window class that displays the results of a search
 * Copyright:    Idega Software Copyright (c) 2003
 * Company:      Idega Software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0

 * 
 */
package com.idega.user.block.search.presentation;

import java.rmi.RemoteException;
import java.util.Collection;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.user.block.search.business.SearchEngine;
import com.idega.user.presentation.BasicUserOverview;


public class SearchResultsWindow extends BasicUserOverview {
  
  private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  private String searchString = null;
  
  
  public SearchResultsWindow() {
  }
  
	 
	/* (non-Javadoc)
	 * @see com.idega.user.presentation.BasicUserOverview#getEntries(com.idega.presentation.IWContext)
	 */
	protected Collection getEntries(IWContext iwc) {
		SearchResultsWindowPS sPs = (SearchResultsWindowPS)ps;
		 try {
		 	SearchEngine engine = getSearchEngine(iwc);
			return engine.getResult( sPs.getSearchType() , sPs.getSearchString() );
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	public static SearchEngine getSearchEngine(IWApplicationContext iwc) {
		SearchEngine business = null;
			try {
				business = (SearchEngine) IBOLookup.getServiceInstance(iwc, SearchEngine.class);
			}
			catch (RemoteException rme) {
				rme.printStackTrace();
			}
			
		return business;
	}
	
	public Class getPresentationStateClass() {
		return SearchResultsWindowPS.class;
	}
	


}