/*
 * Created on Apr 6, 2003
 *
 */
package com.idega.user.block.search.presentation;

import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWException;
import com.idega.user.block.search.event.UserSearchEvent;
import com.idega.user.presentation.BasicUserOverviewPS;

/**
 * @author <a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 */
public class SearchResultsWindowPS extends BasicUserOverviewPS {
	
	private int searchType;
	private String searchString;
	private UserSearchEvent lastUserSearchEvent = null;


	public SearchResultsWindowPS() {
		super();
	}
	
	
	public String getSearchString(){
			return searchString;
	}

	public int getSearchType(){
			return searchType;
	}
	
	/* 
	 * @see com.idega.event.IWActionListener#actionPerformed(com.idega.event.IWPresentationEvent)
	 */
	public void actionPerformed(IWPresentationEvent e) throws IWException {
		
		if( e instanceof UserSearchEvent){
			lastUserSearchEvent = (UserSearchEvent) e;
			
			searchString = ((UserSearchEvent)e).getSearchString();
			searchType = ((UserSearchEvent)e).getSearchType();
			this._selectedDomain = null;
			this._selectedGroup = null;
			
			this.fireStateChanged();	
			
			
		}
		
	}

	/* (non-Javadoc)
	 * @see com.idega.event.IWPresentationState#reset()
	 */
	public void reset() {
		super.reset();
		searchString =null;
		searchType=-1;	
	}

	/**
	 * @return the last event caught
	 */
	public UserSearchEvent getLastUserSearchEvent() {
		return lastUserSearchEvent;
	}

}
