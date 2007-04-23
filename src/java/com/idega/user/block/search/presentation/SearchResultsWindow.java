/**
 * Title:        A window class that displays the results of a search
 * Copyright:    Idega Software Copyright (c) 2003
 * Company:      Idega Software
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
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;
import com.idega.user.block.search.business.SearchEngine;
import com.idega.user.presentation.BasicUserOverview;
import com.idega.user.presentation.StyledBasicUserOverViewToolbar;


public class SearchResultsWindow extends BasicUserOverview {
  
  private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

  public SearchResultsWindow() {
  }
  
	 
	/* (non-Javadoc)
	 * @see com.idega.user.presentation.BasicUserOverview#getEntries(com.idega.presentation.IWContext)
	 */
	protected Collection getEntries(IWContext iwc) {
		SearchResultsWindowPS sPs = (SearchResultsWindowPS)this.ps;
		 try {
		 	SearchEngine engine = getSearchEngine(iwc);
			return engine.getResult( sPs.getLastUserSearchEvent() );
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String getEntityBrowserIdentifier(){
		String identifier = "search-";
		SearchResultsWindowPS sPs = (SearchResultsWindowPS)this.ps;
		if(sPs!=null){
			identifier+= sPs.getSearchString();
		}	
		
		return identifier;
	}
	
	protected PresentationObject getEmptyListPresentationObject() {
		Text text = new Text(this.iwrb.getLocalizedString("searchresultswindow.search_had_no_match", "The search did not return any results"));
		
		return text;
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
	
	
	/* (non-Javadoc)
	 * @see com.idega.user.presentation.BasicUserOverview#getToolbar()
	 */
	protected StyledBasicUserOverViewToolbar getToolbar() {
		StyledBasicUserOverViewToolbar toolbar = super.getToolbar();
		SearchResultsWindowPS sPs = (SearchResultsWindowPS)this.ps;
		if(sPs!=null && this.iwrb!=null){
			String search = sPs.getSearchString();
			if(search!=null ) {
				toolbar.setTitle(this.iwrb.getLocalizedString("searchresultswindow.search:","Search : ")+search+ Text.getNonBrakingSpace(2));
			}
			else{
				toolbar.setTitle(this.iwrb.getLocalizedString("searchresultswindow.advanced_search:","Advanced search")+Text.getNonBrakingSpace(2));	
			}
		}
		
		return toolbar;
	}

}