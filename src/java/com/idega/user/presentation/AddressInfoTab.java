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
  private static final String secondCommuneFieldName = "UMsecondCommune";
  private static final String secondPoBoxFieldName = "UMsecondPoBox";
  
	private static final String HELP_TEXT_KEY = "address_info_tab";

	private Text streetText;
	private Text cityText;
	private Text provinceText;
	private Text postalCodeText;
	private Text countryText;
	private Text poBoxText;
	private Text communeText;
  
  private Text coAddressText;
  
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
		if (this.fieldValues == null) {
			this.fieldValues = new Hashtable();
		}
	}

	public void updateFieldsDisplayStatus() {
		String street = (String)this.fieldValues.get(streetFieldName);
		String city = (String)this.fieldValues.get(cityFieldName);
		String province = (String)this.fieldValues.get(provinceFieldName);
		String postalId = (String)this.fieldValues.get(postalCodeFieldName);
		String countryId = (String)this.fieldValues.get(countryFieldName);
		String communeId = (String)this.fieldValues.get(communeFieldName);
		String poBox = (String)this.fieldValues.get(poBoxFieldName);

		if (street != null) {
			this.streetField.setContent(street);
		}
		if (city != null) {
			this.cityField.setContent(city);
		}
		if (province != null) {
			this.provinceField.setContent(province);
		}
		if (postalId != null && !postalId.equals("")) {
			this.postalCodeField.setSelectedElement(Integer.parseInt(postalId));
		}
		if(countryId!=null && !countryId.equals("") ){
			Country country = null;
			try {
				country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    this.countryField.setSelectedCountry(country);
		    this.postalCodeField.setCountry(country);
		}
		if (communeId != null && !communeId.equals("")) {
			this.communeField.setSelectedElement(Integer.parseInt(communeId));
		}
		if (poBox != null) {
			this.poBoxField.setContent(poBox);
		} 
		////////////////////     
    // second address
    street = (String)this.fieldValues.get(secondStreetFieldName);
    city = (String)this.fieldValues.get(secondCityFieldName);
    province = (String)this.fieldValues.get(secondProvinceFieldName);
    postalId = (String)this.fieldValues.get(secondPostalCodeFieldName);
    countryId = (String)this.fieldValues.get(secondCountryFieldName);
    communeId = (String)this.fieldValues.get(secondCommuneFieldName);
    poBox = (String)this.fieldValues.get(secondPoBoxFieldName);

    if (street != null) {
		this.secondStreetField.setContent(street);
	}
    if (city != null) {
		this.secondCityField.setContent(city);
	}
    if (province != null) {
		this.secondProvinceField.setContent(province);
	}
    if (postalId != null && !postalId.equals("")) {
		this.secondPostalCodeField.setSelectedElement(Integer.parseInt(postalId));
	}
    if(countryId!=null && !countryId.equals("") ){
        Country country = null;
		try {
			country = getCountryHome().findByPrimaryKey(Integer.valueOf(countryId));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    this.secondCountryField.setSelectedCountry(country);
	    this.secondPostalCodeField.setCountry(country);
    }
    if (communeId != null && !communeId.equals("")) {
		this.secondCommuneField.setSelectedElement(Integer.parseInt(communeId));
	}
    if (poBox != null) {
		this.secondPoBoxField.setContent(poBox);
	}
	}

	public void initializeFields() {
		IWContext iwc = IWContext.getInstance();
		this.streetField = new TextInput(streetFieldName);
    this.streetField.setDisabled(true);
		this.streetField.setLength(20);

		this.cityField = new TextInput(cityFieldName);
    this.cityField.setDisabled(true);
		this.cityField.setLength(20);

		this.provinceField = new TextInput(provinceFieldName);
    this.provinceField.setDisabled(true);
		this.provinceField.setLength(20);

		//only works for Iceland
		if (this.postalCodeField == null) {
			this.postalCodeField = new PostalCodeDropdownMenu();
      this.postalCodeField.setDisabled(true);
		}

		SelectorUtility su = new SelectorUtility();
		try {
			this.communeField = new DropdownMenu(communeFieldName);
			this.communeField.addMenuElement(-1,"");
			su.getSelectorFromIDOEntities(this.communeField, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
			this.communeField.setDisabled(true);
		}catch (RemoteException e) {}
		
		this.countryField = new CountryDropdownMenu(countryFieldName);
    this.countryField.setDisabled(true);

		this.poBoxField = new TextInput(poBoxFieldName);
    this.poBoxField.setDisabled(true);
		this.poBoxField.setLength(10);
    // second address
    this.secondStreetField = new TextInput(secondStreetFieldName);
    this.secondStreetField.setLength(20);

    this.secondCityField = new TextInput(secondCityFieldName);
    this.secondCityField.setLength(20);

    this.secondProvinceField = new TextInput(secondProvinceFieldName);
    this.secondProvinceField.setLength(20);

    //only works for Iceland
    if (this.secondPostalCodeField == null) {
      this.secondPostalCodeField = new PostalCodeDropdownMenu();
      this.secondPostalCodeField.setName(secondPostalCodeFieldName);
    }

    this.secondCountryField = new CountryDropdownMenu(secondCountryFieldName);

    this.secondPoBoxField = new TextInput(secondPoBoxFieldName);
    this.secondPoBoxField.setLength(10);

		try {
			this.secondCommuneField = new DropdownMenu(secondCommuneFieldName);
			this.secondCommuneField.addMenuElement(-1,"");
			su.getSelectorFromIDOEntities(this.secondCommuneField, getCommuneBusiness(iwc).getCommunes(), "getCommuneName");
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

		this.streetText = new Text(iwrb.getLocalizedString(streetFieldName,"Street"));
		this.streetText.setBold();

		this.cityText = new Text(iwrb.getLocalizedString(cityFieldName,"City"));
		this.cityText.setBold();

		this.provinceText = new Text(iwrb.getLocalizedString(provinceFieldName,"Province"));
		this.provinceText.setBold();

		this.postalCodeText = new Text(iwrb.getLocalizedString(postalCodeFieldName,"Postal"));
		this.postalCodeText.setBold();

		this.countryText = new Text(iwrb.getLocalizedString(countryFieldName,"Country"));
		this.countryText.setBold();

		this.poBoxText = new Text(iwrb.getLocalizedString(poBoxFieldName,"P.O.Box"));
		this.poBoxText.setBold();
    // the same texts are used for the second address
		
		this.communeText = new Text(iwrb.getLocalizedString(communeFieldName, "Commune"));
		this.communeText.setBold();
    
    this.coAddressText = new Text(iwrb.getLocalizedString("UM_coAddress","co address"));
    this.coAddressText.setBold();
	}
	
	public void lineUpFields() {
		this.resize(1, 1);
		int row = 1;

		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		this.user = iwc.getCurrentUser();
		try {
			this.adminUser = iwc.getAccessController().getAdministratorUser();
			
		}
		catch (Exception ex){
			System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
			ex.printStackTrace(System.err);
			this.adminUser = null;
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

    table.add(this.postalCodeText,1,row);
		table.add(Text.getBreak(), 1, row);
    table.add(this.postalCodeField, 1, row);

    int column = 2;
			table.add(this.communeText, column, row);
			table.add(Text.getBreak(), column, row);
			table.add(this.communeField, column, row++);
			column = 1;

		table.add(this.countryText, column, row);
		table.add(Text.getBreak(), column, row);
		table.add(this.countryField, column, row++);
		
		row++;
		table.add(this.coAddressText, 1, row++);

    //CO address
    table.add(this.streetText, 1, row);
		table.add(Text.getBreak(), 1, row);
    table.add(this.secondStreetField, 1, row);    

    table.add(this.poBoxText, 2, row);
		table.add(Text.getBreak(), 2, row);
    table.add(this.secondPoBoxField, 2, row++);

    table.add(this.cityText, 1, row);
		table.add(Text.getBreak(), 1, row);
    table.add(this.secondCityField, 1, row);

    table.add(this.provinceText, 2,row);
		table.add(Text.getBreak(), 2, row);
    table.add(this.secondProvinceField, 2, row++);
    
		table.add(this.postalCodeText,1,row);
		table.add(Text.getBreak(), 1, row);

		Table postalTable = new Table();
		postalTable.setCellpaddingAndCellspacing(0);
		postalTable.add(this.secondPostalCodeField,1,1);
		table.add(postalTable, 1, row);
		if(this.user.getPrimaryKey().equals(this.adminUser.getPrimaryKey())) {
			GenericButton addPostal = new GenericButton("add_postal", iwrb.getLocalizedString("AddressInfoTab.postalcodewindow.add","Add"));
			addPostal.setWindowToOpen(PostalCodeEditorWindow.class);
			StyledButton button = new StyledButton(addPostal);
			
			postalTable.setWidth(2, 3);
			postalTable.add(button, 3, 1);
		}
		
    column = 2;
	    	table.add(this.communeText, column, row);
	  		table.add(Text.getBreak(), column, row);
	    	table.add(this.secondCommuneField, column, row++);
	    	column = 1;
    
    table.add(this.countryText, column, row);
		table.add(Text.getBreak(), column, row);
    table.add(this.secondCountryField, column, row++);
  	}

	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());		
	}

	public boolean collect(IWContext iwc) {

		if (iwc != null) {
			String street = iwc.getParameter(AddressInfoTab.streetFieldName);
			String city = iwc.getParameter(AddressInfoTab.cityFieldName);
			String province = iwc.getParameter(AddressInfoTab.provinceFieldName);
			String postal = iwc.getParameter(AddressInfoTab.postalCodeFieldName);
			String country = iwc.getParameter(AddressInfoTab.countryFieldName);
			String commune = iwc.getParameter(AddressInfoTab.communeFieldName);
			String poBox = iwc.getParameter(AddressInfoTab.poBoxFieldName);

			if (street != null) {
				this.fieldValues.put(AddressInfoTab.streetFieldName, street);
			}
			if (city != null) {
				this.fieldValues.put(AddressInfoTab.cityFieldName, city);
			}
			if (province != null) {
				this.fieldValues.put(AddressInfoTab.provinceFieldName, province);
			}
			if (postal != null) {
				this.fieldValues.put(AddressInfoTab.postalCodeFieldName, postal);
			}
			if (country != null) {
				this.fieldValues.put(AddressInfoTab.countryFieldName, country);
			}
			if (commune != null) {
				this.fieldValues.put(AddressInfoTab.communeFieldName, commune);
			}
			if (poBox != null) {
				this.fieldValues.put(AddressInfoTab.poBoxFieldName, poBox);
			}
      // second address
      street = iwc.getParameter(secondStreetFieldName);
      city = iwc.getParameter(secondCityFieldName);
      province = iwc.getParameter(secondProvinceFieldName);
      postal = iwc.getParameter(secondPostalCodeFieldName);
      country = iwc.getParameter(secondCountryFieldName);
      commune = iwc.getParameter(secondCommuneFieldName);
      poBox = iwc.getParameter(secondPoBoxFieldName);

      if (street != null) {
        this.fieldValues.put(secondStreetFieldName, street);
      }
      if (city != null) {
        this.fieldValues.put(secondCityFieldName, city);
      }
      if (province != null) {
        this.fieldValues.put(secondProvinceFieldName, province);
      }
      if (postal != null) {
        this.fieldValues.put(secondPostalCodeFieldName, postal);
      }
      if (country != null) {
        this.fieldValues.put(secondCountryFieldName, country);
      }
      if (commune != null) {
          this.fieldValues.put(secondCommuneFieldName, commune);
        }
      if (poBox != null) {
        this.fieldValues.put(secondPoBoxFieldName, poBox);
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
				if (postal != null) {
					postalCodeId = new Integer(postal);
				}
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
        if (postal != null) {
			postalCodeId = new Integer(postal);
		}
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
				if (country != null) {
					countryId = country.getPrimaryKey().toString();
				}
				String city = addr.getCity();
				String province = addr.getProvince();
				int communeId = addr.getCommuneID();
				String poBox = addr.getPOBox();

				if (street != null) {
					this.fieldValues.put(streetFieldName, street);
				}
				if (city != null) {
					this.fieldValues.put(cityFieldName, city);
				}
				if (province != null) {
					this.fieldValues.put(provinceFieldName, province);
				}
				if (code != -1) {
					this.fieldValues.put(postalCodeFieldName, String.valueOf(code));
				}
				if (countryId != null) {
					this.fieldValues.put(countryFieldName, countryId);
				}
				if (communeId != -1) {
					this.fieldValues.put(communeFieldName, String.valueOf(communeId));
				}
				if (poBox != null) {
					this.fieldValues.put(poBoxFieldName, poBox);
				}
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
        if (country != null) {
			countryId = country.getPrimaryKey().toString();
		}
        String city = addr.getCity();
        String province = addr.getProvince();
		int communeId = addr.getCommuneID();
        String poBox = addr.getPOBox();

        if (street != null) {
			this.fieldValues.put(secondStreetFieldName, street);
		}
        if (city != null) {
			this.fieldValues.put(secondCityFieldName, city);
		}
        if (province != null) {
			this.fieldValues.put(secondProvinceFieldName, province);
		}
        if (code != -1) {
			this.fieldValues.put(secondPostalCodeFieldName, String.valueOf(code));
		}
        if (countryId != null) {
			this.fieldValues.put(secondCountryFieldName, countryId);
		}
        if (communeId != -1) {
			this.fieldValues.put(secondCommuneFieldName, String.valueOf(communeId));
		}
        if (poBox != null) {
			this.fieldValues.put(secondPoBoxFieldName, poBox);
		}
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
