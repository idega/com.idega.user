package com.idega.user.block.search.presentation;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.text.Text;
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
public class SearchForm extends PresentationObjectContainer implements IWBrowserView {
	private IWBundle iwb;
	private TextInput _searchString;
	//	private SubmitButton _groupSearch;
	//	private SubmitButton _userSearch;
	private SubmitButton _search;
	private DropdownMenu _searchType;
	public final static String STYLE = "font-family:arial; font-size:7pt; color:#000000; text-align: justify; border: 1 solid #000000;";
	public final static String STYLE_2 = "font-family:arial; font-size:7pt; color:#000000; text-align: justify;";
	private String _controlTarget = null;
	private IWPresentationEvent _contolEvent = null;
	
	
	public SearchForm() {
		_searchString = new TextInput(SimpleSearchEvent.FIELDNAME_TEXTINPUT);
		this.setStyle(_searchString);
		//		_userSearch = new SubmitButton("Search for User",SimpleSearchEvent.FIELDNAME_SEARCHTYPE,Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER));
		//		this.setStyle(_userSearch);
		//		_groupSearch = new SubmitButton("Search for Group",SimpleSearchEvent.FIELDNAME_SEARCHTYPE,Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP));
		//        this.setStyle(_groupSearch);
		_search = new SubmitButton("  Search", "submit_", Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP));
		this.setStyle(_search);
		_searchType = new DropdownMenu(SimpleSearchEvent.FIELDNAME_SEARCHTYPE);
		_searchType.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER), "User");
		_searchType.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP), "Group");
		this.setStyle(_searchType);
	}
	public String getBundleIdentifier() {
		return "com.idega.user";
	}
	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		Form form = new Form();
		SimpleSearchEvent event = new SimpleSearchEvent();
		Table table = new Table(3, 1);
		table.add(_searchString, 2, 1);
		table.add(_search, 3, 1);
		table.add(_searchType, 1, 1);
		form.addEventModel(event, iwc);
		form.add(table);
		_search.setButtonImage(iwb.getImage("search.gif"));
		if (_controlTarget != null) {
			form.setTarget(_controlTarget);
		}
		if (_contolEvent != null) {
			form.addEventModel(_contolEvent, iwc);
		}
		this.add(form);
	}
	public void setControlEventModel(IWPresentationEvent model) {
		_contolEvent = model;
	}
	public void setControlTarget(String controlTarget) {
		_controlTarget = controlTarget;
	}
	public void setStyle(PresentationObject obj) {
		if (obj instanceof Text) {
			this.setStyle((Text) obj);
		}
		else {
			obj.setAttribute("style", STYLE);
		}
	}
	public void setStyle(Text obj) {
		obj.setAttribute("style", STYLE_2);
	}
	public void setStyle(PresentationObject obj, String style) {
		obj.setAttribute("style", style);
	}
}