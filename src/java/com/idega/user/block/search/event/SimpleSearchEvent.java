package com.idega.user.block.search.event;

import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class SimpleSearchEvent extends IWPresentationEvent
{

	public static final String FIELDNAME_TEXTINPUT = "usr_search";
	public static final String FIELDNAME_SEARCHTYPE = "usr_search_type";
	public static final int SEARCHTYPE_USER = 0;
	public static final int SEARCHTYPE_GROUP = 1;

	private String _searchString = null;
	private int _searchType = 0;

    public SimpleSearchEvent(){
    }

	public String getSearchString(){
	    return _searchString;
	}

	public int getSearchType(){
	    return _searchType;
	}


    public boolean initializeEvent(IWContext iwc)
    {
		_searchString = iwc.getParameter(FIELDNAME_TEXTINPUT);

		String type = iwc.getParameter(FIELDNAME_SEARCHTYPE);
		if(type == null){
		    type = iwc.getParameter(FIELDNAME_SEARCHTYPE+".x");
		}

		try
		{
			_searchType = Integer.parseInt(type);
		}
		catch (NumberFormatException ex)
		{
			System.err.println("["+this.getClass()+"] :No searchType");
			return false;
		}


		return true;
    }
}