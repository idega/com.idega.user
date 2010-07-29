package com.idega.user.block.search.presentation;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.TextInput;
import com.idega.user.block.search.event.UserSearchEvent;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
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
	public final static String STYLE = "font-family:arial; font-size:7pt; color:#000000; text-align: justify; border: 1 solid #000000;";
	public final static String STYLE_2 = "font-family:arial; font-size:7pt; color:#000000; text-align: justify;";
	private IWPresentationEvent _controlEvent = null;
	private String textValue = null;
	
	@Autowired
	private JQuery jQuery;
	
	public SearchForm() {}
	
	@Override
	public String getBundleIdentifier() {
		return "com.idega.user";
	}
	
	@Override
	public void main(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		
		this.empty();
		this.iwb = getBundle(iwc);
		this.iwrb = getResourceBundle(iwc);
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				jQuery.getBundleURIToJQueryLib(),
				iwb.getVirtualPathWithFileNameString("javascript/UserApplication.js"),
				CoreConstants.DWR_UTIL_SCRIPT
		));
		
		this.searchInput = new TextInput(UserSearchEvent.SEARCH_FIELD_SIMPLE_SEARCH_STRING);
		setStyle(this.searchInput);
		
		HiddenInput type = new HiddenInput(UserSearchEvent.SEARCH_FIELD_SEARCH_TYPE, Integer.toString(UserSearchEvent.SEARCHTYPE_SIMPLE));
		
		Form form = new Form();
		form.add(type);
		Table table = new Table(3, 1);
		table.setHorizontalAlignment(Table.HORIZONTAL_ALIGN_RIGHT);
		table.setCellpadding(0);
		table.setCellspacing(1);
		
		table.add(this.searchInput, 2, 1);
		GenericButton button = new GenericButton(this.iwrb.getLocalizedString("searchform","Search"));
		table.add(button, 3, 1);
		
		UserSearchEvent event = new UserSearchEvent();
		 // get the source from the controlEvent (this is a hack that this works with newer core versions)
		String sourceParameterValue = (this._controlEvent == null) ? "" : this._controlEvent.getSourceParameterValue();
		event.setSource(sourceParameterValue);
		form.addEventModel(event, iwc);
		form.add(table);
		form.setOnSubmit("return false");
		
		Image image = this.iwb.getImage("search.gif");
		image.setId(button.getId()+"_image");
		button.setButtonImage(image);
		button.setInputType(GenericInput.INPUT_TYPE_IMAGE);
		button.setOnClick("UserApplication.search(event, '" + form.getId() + "');");
		
		if (this.textValue != null) {
			this.searchInput.setContent(this.textValue);
			this.searchInput.setOnFocus("if(this.value==\'" + this.iwrb.getLocalizedString("insert_search_string","Insert a search string") + "\')this.value=\'\' ");
		}

		this.add(form);
	}
	
	public void setControlEventModel(IWPresentationEvent model) {
		this._controlEvent = model;
	}
	
	public void setControlTarget(String controlTarget) {
	}
	
	public void setStyle(PresentationObject obj) {
		if (obj instanceof Text) {
			this.setStyle((Text) obj);
		} else {
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