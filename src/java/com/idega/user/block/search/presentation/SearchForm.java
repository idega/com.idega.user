package com.idega.user.block.search.presentation;

import com.idega.presentation.ui.*;
import com.idega.presentation.*;
import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.browser.presentation.IWBrowserView;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class SearchForm extends PresentationObjectContainer implements IWBrowserView
{

	private TextInput _searchString;
	private SubmitButton _groupSearch;
	private SubmitButton _userSearch;

	private String _controlTarget = null;
	private IWPresentationEvent _contolEvent = null;


    public SearchForm()
    {
		_searchString = new TextInput(SimpleSearchEvent.FIELDNAME_TEXTINPUT);
		_userSearch = new SubmitButton("Search for User",SimpleSearchEvent.FIELDNAME_SEARCHTYPE,Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER));
		_groupSearch = new SubmitButton("Search for Group",SimpleSearchEvent.FIELDNAME_SEARCHTYPE,Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP));
    }

	public void main(IWContext iwc) throws Exception {
		Form form = new Form();
		SimpleSearchEvent event = new SimpleSearchEvent();
		Table table = new Table(3,1);
		table.add(_searchString,1,1);
		table.add(_userSearch,2,1);
		table.add(_groupSearch,3,1);
		form.addEventModel(event);
		form.add(table);

		if(_controlTarget != null){
		    form.setTarget(_controlTarget);
		}

		if(_contolEvent != null){
		    form.addEventModel(_contolEvent);
		}

		this.add(form);
	}


	public void setControlEventModel(IWPresentationEvent model){
		_contolEvent = model;
	}

	public void setControlTarget(String controlTarget){
	    _controlTarget = controlTarget;
	}
}