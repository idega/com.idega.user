package com.idega.user.block.search.presentation;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.block.search.event.UserSearchEvent;
/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
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
	private IWPresentationEvent _controlEvent = null;
	private String textValue = null;
	
	
	public SearchForm() {
	
	}
	public String getBundleIdentifier() {
		return "com.idega.user";
	}
	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		
		searchInput = new TextInput(UserSearchEvent.SEARCH_FIELD_SIMPLE_SEARCH_STRING);
		setStyle(searchInput);
		searchButton = new SubmitButton(iwrb.getLocalizedString("searchform","Search"));
		//setStyle(searchButton);
		
		HiddenInput type = new HiddenInput(UserSearchEvent.SEARCH_FIELD_SEARCH_TYPE, Integer.toString(UserSearchEvent.SEARCHTYPE_SIMPLE));
		
		
		/*searchTypeDropDown = new DropdownMenu(SimpleSearchEvent.FIELDNAME_SEARCHTYPE);
		searchTypeDropDown.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_USER), "User");
		searchTypeDropDown.addMenuElement(Integer.toString(SimpleSearchEvent.SEARCHTYPE_GROUP), "Group");
		setStyle(searchTypeDropDown);*/
		
		Form form = new Form();
		form.add(type);
		UserSearchEvent event = new UserSearchEvent();
		Table table = new Table(3, 1);
		table.setHorizontalAlignment(Table.HORIZONTAL_ALIGN_RIGHT);
		table.setCellpadding(0);
		table.setCellspacing(1);
		
		table.add(searchInput, 2, 1);
		table.add(searchButton, 3, 1);
		//table.add(searchTypeDropDown, 1, 1);
		
		 // get the source from the controlEvent (this is a hack that this works with newer core versions)
		String sourceParameterValue = (_controlEvent == null) ? "" : _controlEvent.getSourceParameterValue();
		event.setSource(sourceParameterValue);
		form.addEventModel(event, iwc);
		form.add(table);
		searchButton.setButtonImage(iwb.getImage("search.gif"));
		
		if(textValue != null) {
			searchInput.setContent(textValue);
			searchInput.setOnFocus("if(this.value==\'" + iwrb.getLocalizedString("insert_search_string","Insert a search string") + "\')this.value=\'\' ");
		}
// do not set the controlEvent (this is a hack that this works with newer core versions)
//		if (_controlTarget != null) {
//			form.setTarget(_controlTarget);
//		}
//		if (_controlEvent != null) {
//			form.addEventModel(_controlEvent, iwc);
//		}
		this.add(form);
	}
	public void setControlEventModel(IWPresentationEvent model) {
		_controlEvent = model;
	}
	public void setControlTarget(String controlTarget) {
		_controlTarget = controlTarget;
	}
	public void setStyle(PresentationObject obj) {
		if (obj instanceof Text) {
			this.setStyle((Text) obj);
		}
		else {
			obj.setMarkupAttribute("style", STYLE);
		}
	}
	public void setStyle(Text obj) {
		obj.setMarkupAttribute("style", STYLE_2);
	}
	public void setStyle(PresentationObject obj, String style) {
		obj.setMarkupAttribute("style", style);
	}
	public void setTextInputValue(String textValue) {
		this.textValue = textValue;

	}
}