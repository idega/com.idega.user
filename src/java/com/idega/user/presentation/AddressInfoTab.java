package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Hashtable;

import com.idega.business.IBOLookup;
import com.idega.core.location.business.CommuneBusiness;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.PostalCodeDropdownMenu;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * Title:        User
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @author 2002 <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */
public class AddressInfoTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "addr_info_tab_name";
	private static final String DEFAULT_TAB_NAME = "Address";
	
	private String USE_COMMUNES_BUNDLE_PROPERTY_NAME = "USE_COMMUNES";

	private TextInput streetField;
	private TextInput cityField;
	private TextInput provinceField;
	private PostalCodeDropdownMenu postalCodeField;
	private CountryDropdownMenu countryField;
	private TextInput poBoxField;
	private DropdownMenu communeField;

  private TextInput secondStreetField;
  private TextInput secondCityField;
  private TextInput secondProvinceField;
  private PostalCodeDropdownMenu secondPostalCodeField;
  private CountryDropdownMenu secondCountryField;
  private TextInput secondPoBoxField;
  private DropdownMenu secondCommuneField;

	private static final String streetFieldName = "UMstreet";
	private static final String cityFieldName = "UMcity";
	private static final String provinceFieldName = "UMprovince";
	private static final String postalCodeFieldName =
		PostalCodeDropdownMenu.IW_POSTAL_CODE_MENU_PARAM_NAME;
	private static final String countryFieldName = "UMcountry";
	private static final String communeFieldName = "UMcommune";
	private static final String poBoxFieldName = "UMpoBox";

  private static final String secondStreetFieldName = "UMsecondStreet";
  private static final String secondCityFieldName = "UMsecondCity";
  private static final String secondProvinceFieldName = "UMsecondProvince";
  private static final String secondPostalCodeFieldName = 
    "UMsecond" + PostalCodeDropdownMenu.IW_POSTAL_CODE_MENU_PARAM_NAME;
  private static final String secondCountryFieldName = "UMsecondCountry";
  private static final String secondCommuneFieldName = "UMsecondPoBox";
  private static final String secondPoBoxFieldName = "UMsecondCommune";
  
	private static final String HELP_TEXT_KEY = "address_info_tab";

	private Text streetText;
	private Text cityText;
	private Text provinceText;
	private Text postalCodeText;
	private Text countryText;
	private Text poBoxText;
	private Text communeText;
  
  private Text coAddressText;
  boolean useCommune = false;
  
	private User user = null; 
	private com.idega.core.user.data.User adminUser = null; 
  
  
	public AddressInfoTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
	}

	public void initializeFieldNames() {
	}

	public void initializeFieldValues() {
		if (fieldValues == null)
			fieldValues = new Hashtable();
	}

	public void updateFieldsDisplayStatus() {
		String street = (String)fieldValues.get(streetFieldName);
		String city = (String)fieldValues.get(cityFieldName);
		String province = (String)fieldValues.get(provinceFieldName);
		String postalId = (String)fieldValues.get(postalCodeFieldName);
		String countryId = (String)fieldValues.get(countryFieldName);
		String poBox = (String)fieldValues.get(poBoxFieldName);

		if (street != null)
			streetField.setContent(street);
		if (city != null)
			cityField.setContent(city);
		if (province != null)
			provinceField.setContent(province);
		if (postalId != null && !postalId.equals(""))
			postalCodeField.setSelectedElement(Integer.parseInt(postalId));
		if(countryId!=null && !countryId.equals("") ){
			Country country = null;
			try {
				country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    countryField.setSelectedCountry(country);
		    postalCodeField.setCountry(country);
		}
		if (poBox != null)
			poBoxField.setContent(poBox); 
		////////////////////     
    // second address
    street = (String)fieldValues.get(secondStreetFieldName);
    city = (String)fieldValues.get(secondCityFieldName);
    province = (String)fieldValues.get(secondProvinceFieldName);
    postalId = (String)fieldValues.get(secondPostalCodeFieldName);
    countryId = (String)fieldValues.get(secondCountryFieldName);
    poBox = (String)fieldValues.get(secondPoBoxFieldName);

    if (street != null)
      secondStreetField.setContent(street);
    if (city != null)
      secondCityField.setContent(city);
    if (province != null)
      secondProvinceField.setContent(province);
    if (postalId != null && !postalId.equals(""))
      secondPostalCodeField.setSelectedElement(Integer.parseInt(postalId));
    if(countryId!=null && !countryId.equals("") ){
        Country country = null;
		try {
			country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    secondCountryField.setSelectedCountry(country);
	    secondPostalCodeField.setCountry(country);
    }
    if (poBox != null)
      secondPoBoxField.setContent(poBox);
	}

	public void initializeFields() {
		IWContext iwc = IWContext.getInstance();
		streetField = new TextInput(streetFieldName);
    streetField.setDisabled(true);
		streetField.setLength(20);

		cityField = new TextInput(cityFieldName);
    cityField.setDisabled(true);
		cityField.setLength(20);

		provinceField = new TextInput(provinceFieldName);
    provinceField.setDisabled(true);
		provinceField.setLength(20);

		//only works for Iceland
		if (postalCodeField == null) {
			postalCodeField = new PostalCodeDropdownMenu();
      postalCodeField.setDisabled(true);
		}

		SelectorUtility su = new SelectorUtility();
		try {
			communeField = new DropdownMenu(communeFieldName);
			su.getSelectorFromIDOEntities(communeField, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
		}catch (RemoteException e) {}
		
		countryField = new CountryDropdownMenu(countryFieldName);
    countryField.setDisabled(true);

		poBoxField = new TextInput(poBoxFieldName);
    poBoxField.setDisabled(true);
		poBoxField.setLength(10);
    // second address
    secondStreetField = new TextInput(secondStreetFieldName);
    secondStreetField.setLength(20);

    secondCityField = new TextInput(secondCityFieldName);
    secondCityField.setLength(20);

    secondProvinceField = new TextInput(secondProvinceFieldName);
    secondProvinceField.setLength(20);

    //only works for Iceland
    if (secondPostalCodeField == null) {
      secondPostalCodeField = new PostalCodeDropdownMenu();
      secondPostalCodeField.setName(secondPostalCodeFieldName);
    }

    secondCountryField = new CountryDropdownMenu(secondCountryFieldName);

    secondPoBoxField = new TextInput(secondPoBoxFieldName);
    secondPoBoxField.setLength(10);

		try {
			secondCommuneField = new DropdownMenu(secondCommuneFieldName);
			su.getSelectorFromIDOEntities(secondCommuneField, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
		}catch (RemoteException e) {}

	}
	
	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle(UserConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
		
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		streetText = new Text(iwrb.getLocalizedString(streetFieldName,"Street"));
		streetText.setBold();

		cityText = new Text(iwrb.getLocalizedString(cityFieldName,"City"));
		cityText.setBold();

		provinceText = new Text(iwrb.getLocalizedString(provinceFieldName,"Province"));
		provinceText.setBold();

		postalCodeText = new Text(iwrb.getLocalizedString(postalCodeFieldName,"Postal"));
		postalCodeText.setBold();

		countryText = new Text(iwrb.getLocalizedString(countryFieldName,"Country"));
		countryText.setBold();

		poBoxText = new Text(iwrb.getLocalizedString(poBoxFieldName,"P.O.Box"));
		poBoxText.setBold();
    // the same texts are used for the second address
		
		communeText = new Text(iwrb.getLocalizedString(communeFieldName, "Commune"));
		communeText.setBold();
    
    coAddressText = new Text(iwrb.getLocalizedString("UM_coAddress","co address"));
    coAddressText.setBold();
	}
	
	public void lineUpFields() {
		this.resize(1, 1);
		int row = 1;

		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		user = iwc.getCurrentUser();
		try {
			adminUser = iwc.getAccessController().getAdministratorUser();
			
		}
		catch (Exception ex){
			System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
			ex.printStackTrace(System.err);
			adminUser = null;
		}
		
		Table table = new Table();
		table.setColumns(2);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setBorder(0);
    this.add(table);

		table.add(this.streetText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.streetField, 1, row);
		
		table.add(this.poBoxText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.poBoxField, 2, row++);

		table.add(this.cityText, 1, row);
		table.add(Text.getBreak(), 1, row);
		table.add(this.cityField, 1, row);

		table.add(this.provinceText, 2, row);
		table.add(Text.getBreak(), 2, row);
		table.add(this.provinceField, 2, row++);

    table.add(postalCodeText,1,row);
		table.add(Text.getBreak(), 1, row);
    table.add(postalCodeField, 1, row);

    int column = 2;
		if (useCommune) {
			table.add(this.communeText, column, row);
			table.add(Text.getBreak(), column, row);
			table.add(this.communeField, column, row);
			column = 1;
		}

		table.add(this.countryText, column, row);
		table.add(Text.getBreak(), column, row);
		table.add(this.countryField, column, row++);
		
		row++;
		table.add(coAddressText, 1, row++);

    //CO address
    table.add(this.streetText, 1, row);
		table.add(Text.getBreak(), 1, row);
    table.add(this.secondStreetField, 1, row);    

    table.add(this.poBoxText, 2, row);
		table.add(Text.getBreak(), 2, row);
    table.add(this.secondPoBoxField, 2, row++);

    table.add(this.cityText, 1, row);
		table.add(Text.getBreak(), 1, row);
    table.add(secondCityField, 1, row);

    table.add(this.provinceText, 2,row);
		table.add(Text.getBreak(), 2, row);
    table.add(secondProvinceField, 2, row++);
    
		table.add(postalCodeText,1,row);
		table.add(Text.getBreak(), 1, row);

		Table postalTable = new Table();
		postalTable.setCellpaddingAndCellspacing(0);
		postalTable.add(secondPostalCodeField,1,1);
		table.add(postalTable, 1, row);
		if(user.getPrimaryKey().equals(adminUser.getPrimaryKey())) {
			GenericButton addPostal = new GenericButton("add_postal", iwrb.getLocalizedString("AddressInfoTab.postalcodewindow.add","Add"));
			addPostal.setWindowToOpen(PostalCodeEditorWindow.class);
			StyledButton button = new StyledButton(addPostal);
			
			postalTable.setWidth(2, 3);
			postalTable.add(button, 3, 1);
		}
		
    column = 2;
    if (useCommune) {
	    	table.add(communeText, column, row);
	  		table.add(Text.getBreak(), column, row);
	    	table.add(secondCommuneField, column, row);
	    	column = 1;
    }
    
    table.add(this.countryText, column, row);
		table.add(Text.getBreak(), column, row);
    table.add(secondCountryField, column, row++);
  	}

	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());		
	}

	public boolean collect(IWContext iwc) {

		if (iwc != null) {
			String street = iwc.getParameter(this.streetFieldName);
			String city = iwc.getParameter(this.cityFieldName);
			String province = iwc.getParameter(this.provinceFieldName);
			String postal = iwc.getParameter(this.postalCodeFieldName);
			String country = iwc.getParameter(this.countryFieldName);
			String poBox = iwc.getParameter(this.poBoxFieldName);

			if (street != null) {
				fieldValues.put(this.streetFieldName, street);
			}
			if (city != null) {
				fieldValues.put(this.cityFieldName, city);
			}
			if (province != null) {
				fieldValues.put(this.provinceFieldName, province);
			}
			if (postal != null) {
				fieldValues.put(this.postalCodeFieldName, postal);
			}
			if (country != null) {
				fieldValues.put(this.countryFieldName, country);
			}
			if (poBox != null) {
				fieldValues.put(this.poBoxFieldName, poBox);
			}
      // second address
      street = iwc.getParameter(secondStreetFieldName);
      city = iwc.getParameter(secondCityFieldName);
      province = iwc.getParameter(secondProvinceFieldName);
      postal = iwc.getParameter(secondPostalCodeFieldName);
      country = iwc.getParameter(secondCountryFieldName);
      poBox = iwc.getParameter(secondPoBoxFieldName);

      if (street != null) {
        fieldValues.put(secondStreetFieldName, street);
      }
      if (city != null) {
        fieldValues.put(secondCityFieldName, city);
      }
      if (province != null) {
        fieldValues.put(secondProvinceFieldName, province);
      }
      if (postal != null) {
        fieldValues.put(secondPostalCodeFieldName, postal);
      }
      if (country != null) {
        fieldValues.put(secondCountryFieldName, country);
      }
      if (poBox != null) {
        fieldValues.put(secondPoBoxFieldName, poBox);
      }

			this.updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public boolean store(IWContext iwc) {

		Integer userId = new Integer(getUserId());
		String street = iwc.getParameter(streetFieldName);

		if (street != null) {
			try {
				Integer postalCodeId = null;
				String postal = iwc.getParameter(postalCodeFieldName);
				if (postal != null)
					postalCodeId = new Integer(postal);
				String countryId = iwc.getParameter(countryFieldName);
				Country country = null;
				try {
					country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
				} catch (Exception e) {
				    e.printStackTrace();
				}
				String city = iwc.getParameter(cityFieldName);
				String province = iwc.getParameter(provinceFieldName);
				String poBox = iwc.getParameter(poBoxFieldName);
				String commune = iwc.getParameter(communeFieldName);
				Integer communeID = null;
				try {
					communeID = Integer.valueOf(commune);
				} catch (NumberFormatException n) {}

				this.getUserBusiness(iwc).updateUsersMainAddressOrCreateIfDoesNotExist(
					userId,
					street,
					postalCodeId,
					country.getName(),
					city,
					province,
					poBox,
					communeID);

			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
    
    // second address
    street = iwc.getParameter(secondStreetFieldName);

    if (street != null) {
      try {
        Integer postalCodeId = null;
        String postal = iwc.getParameter(secondPostalCodeFieldName);
        if (postal != null)
          postalCodeId = new Integer(postal);
        String countryId = iwc.getParameter(secondCountryFieldName);
        Country country = null;
		try {
			country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
		} catch (Exception e) {
		    e.printStackTrace();
		}
        String city = iwc.getParameter(secondCityFieldName);
        String province = iwc.getParameter(secondProvinceFieldName);
        String poBox = iwc.getParameter(secondPoBoxFieldName);
				String commune = iwc.getParameter(secondCommuneFieldName);
				Integer communeID = null;
				try {
					communeID = Integer.valueOf(commune);
				} catch (NumberFormatException n) {}

        this.getUserBusiness(iwc).updateUsersCoAddressOrCreateIfDoesNotExist(
          userId,
          street,
          postalCodeId,
          country.getName(),
          city,
          province,
          poBox,
        	communeID);

      }
      catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }

		return true;

	}

	public void initFieldContents() {
		try {
			UserBusiness userBiz = getUserBusiness(this.getEventIWContext());
			Address addr = userBiz.getUsersMainAddress(getUser());

			boolean hasAddress = false;
			if (addr != null) {
				hasAddress = true;
			}

			if (hasAddress) {
				// TODO remove this fieldValues bullshit!**/
				String street = addr.getStreetAddress();
				int code = addr.getPostalCodeID();
				Country country = addr.getCountry();
				String countryId = null;
				if (country != null)
					countryId = country.getPrimaryKey().toString();
				String city = addr.getCity();
				String province = addr.getProvince();
				String poBox = addr.getPOBox();

				if (street != null)
					fieldValues.put(streetFieldName, street);
				if (city != null)
					fieldValues.put(cityFieldName, city);
				if (province != null)
					fieldValues.put(provinceFieldName, province);
				if (code != -1)
					fieldValues.put(postalCodeFieldName, String.valueOf(code));
				if (countryId != null)
					fieldValues.put(countryFieldName, countryId);
				if (poBox != null)
					fieldValues.put(poBoxFieldName, poBox);
			}
      // second address
      addr = userBiz.getUsersCoAddress(getUser());

      hasAddress = false;
      if (addr != null) {
        hasAddress = true;
      }

      if (hasAddress) {
        // TODO remove this fieldValues bullshit!**/
        String street = addr.getStreetAddress();
        int code = addr.getPostalCodeID();
        Country country = addr.getCountry();
        String countryId = null;
        if (country != null)
          countryId = country.getPrimaryKey().toString();
        String city = addr.getCity();
        String province = addr.getProvince();
        String poBox = addr.getPOBox();

        if (street != null)
          fieldValues.put(secondStreetFieldName, street);
        if (city != null)
          fieldValues.put(secondCityFieldName, city);
        if (province != null)
          fieldValues.put(secondProvinceFieldName, province);
        if (code != -1)
          fieldValues.put(secondPostalCodeFieldName, String.valueOf(code));
        if (countryId != null)
          fieldValues.put(secondCountryFieldName, countryId);
        if (poBox != null)
          fieldValues.put(secondPoBoxFieldName, poBox);
      }
      
			updateFieldsDisplayStatus();

		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println(
				"AddressInfoTab error initFieldContents, userId : " + getUserId());
		}
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	public CountryHome getCountryHome() throws RemoteException {
		return (CountryHome) IDOLookup.getHome(Country.class);
	}

	public CommuneBusiness getCommuneBusiness (IWApplicationContext iwac) throws RemoteException {
		return (CommuneBusiness) IBOLookup.getServiceInstance(iwac, CommuneBusiness.class);
	}	
} // Class AddressInfoTab
