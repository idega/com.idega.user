package com.idega.user.block.search.presentation;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
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
	private IWResourceBundle iwrb;
	private IWBundle iwb;
	private TextInput searchInput;
	//	private SubmitButton _groupSearch;
	//	private SubmitButton _userSearch;
	private SubmitButton searchButton;
	private DropdownMenu searchTypeDropDown;
	public final static String STYLE = "font-family:arial; font-size:7pt; color:#000000; text-align: justify; border: 1 solid #000000;";
	public final static String STYLE_2 = "font-family:arial; font-size:7pt; color:#000000; text-align: justify;";
	private String _controlTarget = null;
	private IWPresentationEvent _contolEvent = null;
	
	
	public SearchForm() {
	
	}
	public String getBundleIdentifier() {
		return "com.idega.user";
	}
	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		
		searchInput = new TextInput(SimpleSearchEvent.FIELDNAME_TEXTINPUT);
		setStyle(searchInput);
		searchButton = new SubmitButton(iwrb.getLocalizedString("searchform","Search"));
		//setStyle(searchButton);
		
		HiddenInput type = new HiddenInput(SimpleSearchEvent.FIELDNAME_SEARCHTYPE, Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER));
		
		
		/*searchTypeDropDown = new DropdownMenu(SimpleSearchEvent.FIELDNAME_SEARCHTYPE);
		searchTypeDropDown.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER), "User");
		searchTypeDropDown.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP), "Group");
		setStyle(searchTypeDropDown);*/
		
		Form form = new Form();
		form.add(type);
		SimpleSearchEvent event = new SimpleSearchEvent();
		Table table = new Table(3, 1);
		table.setHorizontalAlignment(Table.HORIZONTAL_ALIGN_RIGHT);
		table.setCellpadding(0);
		table.setCellspacing(1);
		
		table.add(searchInput, 2, 1);
		table.add(searchButton, 3, 1);
		//table.add(searchTypeDropDown, 1, 1);
		
		form.addEventModel(event, iwc);
		form.add(table);
		searchButton.setButtonImage(iwb.getImage("search.gif"));
		
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