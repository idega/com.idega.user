/**
 * Title:        
 * Copyright:    Idega Software Copyright (c) 2003
 * Company:      Idega Software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Mar 31, 2003
 * 
 */
package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Collection;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.AbstractChooserWindow;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.IWColor;


public class UserChooserBrowserWindow extends AbstractChooserWindow {
  
  private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  public static final String SEARCH_KEY = "search_key";
  public static final String SEARCH_SUBMIT_KEY = "search_submit_key";
  
  private static final String SHOW_LIST_ACTION = "show_list_action";
  private static final String DO_NOT_SHOW_LIST_ACTION = "do_not_show_list_action";
  private static final String SELECTED_USER = "selectedUser";
  
  private String searchString = null;
  
  
  public UserChooserBrowserWindow() {
    setTitle("User chooser");
    setWidth(400);
    setHeight(400);
    setCellpadding(5);
    setScrollbar(true);
    setResizable(true);
  }
  
	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooserWindow#displaySelection(com.idega.presentation.IWContext)
	 */
	public void displaySelection(IWContext iwc) {
    IWResourceBundle resourceBundle = getResourceBundle(iwc);
    Table mainTable = new Table(1,3);
    Table searchTable = getSearchInputField(resourceBundle);
    mainTable.add(searchTable,1,1);
    // parse request
    String action = parseAction(iwc); 
    if (SHOW_LIST_ACTION.equals(action))  {
      Collection entities = getEntities();
      if (entities.isEmpty()) {
        Text nothingFound = new Text(resourceBundle.getLocalizedString("noResultsWereFound", "Sorry, no results were found"));
        mainTable.add(nothingFound,1,2);
      }
      else {
        EntityBrowser browser = getBrowser(entities);
        mainTable.add(browser,1,2);
      }
    }
    Link okayLink = getOkayButton(resourceBundle);
    mainTable.add(okayLink,1,3);
    Form form = new Form();
    form.maintainAllParameters();
    form.add(mainTable);
    add(form);
	}
  
  private String parseAction(IWContext iwc)  {
    if ( iwc.isParameterSet(SEARCH_SUBMIT_KEY) && 
         iwc.isParameterSet(SEARCH_KEY) &&  
         (searchString = iwc.getParameter(SEARCH_KEY)).length() == 0)  {
      searchString = null;
      return DO_NOT_SHOW_LIST_ACTION;
    }
    else  {
      searchString = "%"+searchString+"%";
      return SHOW_LIST_ACTION;
    }
  }
    
    
  
  
  private Link getOkayButton(IWResourceBundle resourceBundle) {
    String okayString = resourceBundle.getLocalizedString("Close", "close");
    Link okayLink = new Link(okayString);
    okayLink.setAsImageButton(true);
    okayLink.setOnClick("window.close(); return false;");
    return okayLink;
  }

  private EntityBrowser getBrowser(Collection entities)  {
    // define checkbox button converter class
    EntityToPresentationObjectConverter converterToChooseButton = new EntityToPresentationObjectConverter() {

      public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
        User user = (User) entity;
        RadioButton radioButton = new RadioButton();
        // define displaystring and value of the textinput of the parent window
        radioButton.setOnClick(SELECT_FUNCTION_NAME+"('"
          + user.getName() +
          "','"
          + ((Integer) user.getPrimaryKey()).toString() + 
          "')");
        return radioButton;
      }
    };
    // set default columns
    String nameKey = "com.idega.user.data.User.FIRST_NAME:" + "com.idega.user.data.User.MIDDLE_NAME:"+"com.idega.user.data.User.LAST_NAME";
    String completeAddressKey =
      "com.idega.core.data.Address.STREET_NAME:"
        + "com.idega.core.data.Address.STREET_NUMBER:"
        + "com.idega.core.data.Address.P_O_BOX:"
        + "com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE:"
        + "com.idega.core.data.Address.CITY:"
        + "com.idega.core.data.Country.IC_COUNTRY_ID|COUNTRY_NAME";
    String emailKey = "com.idega.core.data.Email.ADDRESS";
    String phoneKey = "com.idega.core.data.PhoneType.IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME:" + "com.idega.core.data.Phone.PHONE_NUMBER";
    String pinKey = "com.idega.user.data.User.PERSONAL_ID";
    EntityBrowser browser = new EntityBrowser();
    // keep things simple
    browser.setUseEventSystem(false);
    browser.setEntities("chooserWindow", entities);
    browser.setDefaultNumberOfRows(Math.min(entities.size(), 30));

    browser.setWidth(Table.HUNDRED_PERCENT);
      
    //fonts
    Text column = new Text();
    column.setBold();
    browser.setColumnTextProxy(column);
      
    //    set color of rows
    browser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
    browser.setColorForOddRows("#FFFFFF");
      
    //entityBrowser.setVerticalZebraColored("#FFFFFF",IWColor.getHexColorString(246, 246, 247)); why does this not work!??
      
    browser.setDefaultColumn(1, nameKey);
    browser.setDefaultColumn(2, pinKey);
    browser.setDefaultColumn(3, emailKey);
    browser.setDefaultColumn(4, completeAddressKey);
    browser.setDefaultColumn(5, phoneKey);
    browser.setMandatoryColumn(1, "Choose");
    // set special converters
    browser.setEntityToPresentationConverter("Choose", converterToChooseButton);
    browser.setUseExternalForm(true);
    return browser;
  }
    
  private Collection getEntities()  {
    if (searchString == null)
      return new ArrayList();
    try {
      UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
      Collection entities = userHome.findUsersBySearchCondition(searchString);
      return entities;
    }
    // Remote and FinderException
    catch (Exception ex)  {
      throw new RuntimeException(ex.getMessage());
    }
  }
    
  private Table getSearchInputField(IWResourceBundle iwrb) {
    Table table = new Table(2,1);
    SubmitButton searchButton = 
      new SubmitButton(iwrb.getLocalizedImageButton("search", "Search"), SEARCH_SUBMIT_KEY, SEARCH_SUBMIT_KEY);
    TextInput searchInput = new TextInput(SEARCH_KEY);
    table.add(searchInput,1,1);
    table.add(searchButton,2,1);
    return table;
  }
    
  
  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
}