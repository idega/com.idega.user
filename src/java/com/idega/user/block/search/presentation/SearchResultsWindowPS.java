/*
 * Created on Apr 6, 2003
 *
 */
package com.idega.user.block.search.presentation;

import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWException;
import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.user.presentation.BasicUserOverviewPS;

/**
 * @author <a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 */
public class SearchResultsWindowPS extends BasicUserOverviewPS {
	
	private int searchType;
	private String searchString;


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
		super.actionPerformed(e);
		
		if( e instanceof SimpleSearchEvent){
			searchString = ((SimpleSearchEvent)e).getSearchString();
			searchType = ((SimpleSearchEvent)e).getSearchType();
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

}
