package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.core.location.business.AddressBusiness;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.*;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;


/**
 * <p>Title: Postal code editor window</p>
 * <p>Description: A simple editor window to add postal codes for any local</p>
 * <p>Copyright: Idega Software Copyright (c) 2003</p>
 * <p>Company: Idega Software</p>
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0 
 */
public class PostalCodeEditorWindow extends StyledIWAdminWindow{

	private static final String PARAM_POSTAL_CODE = "postal_edwin_code";
	private static final String PARAM_COUNTRY_ID= "postal_edwin_country_id";
	private static final String PARAM_AREA = "postal_edwin_area";
	
	private static final String PARAM_SAVE = "postal_edwin_save";
	
	private AddressBusiness addressBiz;
	private IWResourceBundle iwrb = null;
	
	private String mainStyleClass = "main";
	

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  

	public PostalCodeEditorWindow() {
		setWidth(350);
		setHeight(200);
		setScrollbar(false);
		setResizable(true);
	}

	
	public void main(IWContext iwc) throws Exception {
		iwrb = getResourceBundle(iwc);
	
		if(iwc.isParameterSet(PARAM_SAVE)){
			String postalCode = iwc.getParameter(PARAM_POSTAL_CODE);
			String area = iwc.getParameter(PARAM_AREA);
			String countryID = iwc.getParameter(PARAM_COUNTRY_ID);
			
			
			addressBiz = getAddressBusiness(iwc);
			try {
				addressBiz.getPostalCodeAndCreateIfDoesNotExist(postalCode,area,addressBiz.getCountryHome().findByPrimaryKey(new Integer(countryID)));
				close();
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
				add(iwrb.getLocalizedString("postalcodeeditorwindow.error_message","The postal code was not saved because of an error, please notify your system administrator"));
			}
			catch (RemoteException e) {
				e.printStackTrace();
				add(iwrb.getLocalizedString("postalcodeeditorwindow.error_message","The postal code was not saved because of an error, please notify your system administrator"));
			}
			catch (CreateException e) {
				e.printStackTrace();
				add(iwrb.getLocalizedString("postalcodeeditorwindow.error_message","The postal code was not saved because of an error, please notify your system administrator"));
			}
			catch (FinderException e) {
				e.printStackTrace();
				add(iwrb.getLocalizedString("postalcodeeditorwindow.error_message","The postal code was not saved because of an error, please notify your system administrator"));
			}
			

		}
		else{
			Form form = new Form();
	
			setTitle(iwrb.getLocalizedString("postalcodeeditorwindow.title", "Postal codes"));
			addTitle(iwrb.getLocalizedString("postalcodeeditorwindow.title", "Postal codes"), TITLE_STYLECLASS);
			setName(iwrb.getLocalizedString("postalcodeeditorwindow.title", "Postal codes"));
	
			add(form,iwc);
			Table tab = new Table(2,5);
			form.add(tab);
			
			tab.setStyleClass(mainStyleClass);
			tab.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);
			tab.setColumnVerticalAlignment(2, Table.VERTICAL_ALIGN_TOP);
	
			tab.setCellspacing(0);
			//tab.setAlignment(2, 4, Table.HORIZONTAL_ALIGN_RIGHT);
			tab.setWidth(300);
			tab.setHeight(120);
			
			
			Text codeText = new Text();
			codeText.setText(iwrb.getLocalizedString("postalcodeeditorwindow.postal_code", "Code"));
			codeText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
					
			TextInput codeInput = new TextInput(PARAM_POSTAL_CODE);
			codeInput.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			codeInput.setAsNotEmpty(iwrb.getLocalizedString("postalcodeeditorwindow.confirm_message", "You must fill in all the fields first."));
			
			tab.add(codeText, 1, 1);
			tab.add(codeInput, 1, 2);
			
			Text countryText = new Text();
			countryText.setText(iwrb.getLocalizedString("postalcodeeditorwindow.country", "Country"));
			countryText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
				
			CountryDropdownMenu countryMenu = new CountryDropdownMenu(PARAM_COUNTRY_ID);
			countryMenu.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
	
			tab.add(countryText, 2, 1);
			tab.add(countryMenu, 2, 2);
			
			Text areaText = new Text();
			areaText.setText(iwrb.getLocalizedString("postalcodeeditorwindow.area", "Area"));
			areaText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
				
			TextInput areaInput = new TextInput(PARAM_AREA);
			areaInput.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
			areaInput.setAsNotEmpty(iwrb.getLocalizedString("postalcodeeditorwindow.confirm_message", "You must fill in all the fields first."));
	
			tab.add(areaText, 1, 3);
			tab.add(areaInput, 1, 4);
			
			//buttons
			SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("postalcodeeditorwindow.save","save"), PARAM_SAVE,"true");
	   	CloseButton close = new CloseButton(iwrb.getLocalizedImageButton("postalcodeeditorwindow.close", "close") );
	    
	    tab.setAlignment(2,5,"right");
			tab.add(close, 2, 5);
			tab.add(Text.getNonBrakingSpace(), 2, 5);
			tab.add(save, 2, 5);
		}
				
	}


	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public AddressBusiness getAddressBusiness(IWContext iwc) {
		if(addressBiz==null){	
			try {
				addressBiz = (AddressBusiness) IBOLookup.getServiceInstance(iwc,AddressBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}	
		}	
		return addressBiz;
	}
  
}