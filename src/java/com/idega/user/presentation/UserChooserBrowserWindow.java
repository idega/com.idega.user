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
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.IWColor;


public class UserChooserBrowserWindow extends StyledAbstractChooserWindow {
  
  private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  private static int NUMBER_OF_ROWS = 4;
  
  public static final String SEARCH_KEY = "search_key";
  public static final String SEARCH_SUBMIT_KEY = "search_submit_key";
  
  private static final String SHOW_LIST_ACTION = "show_list_action";
  private static final String DO_NOT_SHOW_LIST_ACTION = "do_not_show_list_action";
  
  private String searchString = "";
  
  
  public UserChooserBrowserWindow() {
    setWidth(600);
    setHeight(300);
    setScrollbar(true);
    setResizable(true);
  }
  
	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.AbstractChooserWindow#displaySelection(com.idega.presentation.IWContext)
	 */
	public void displaySelection(IWContext iwc) {
    IWResourceBundle resourceBundle = getResourceBundle(iwc);
    // set title
    String title = resourceBundle.getLocalizedString("uc_find_user", "Find user");
    setTitle(title);
    addTitle(title, IWConstants.BUILDER_FONT_STYLE_TITLE);
    // parse request and get search string
    String action = parseRequest(iwc);
    
    Table mainTable = new Table(1,4);
    mainTable.setCellpadding(5);

    Table searchTable = getSearchInputField(resourceBundle);
    mainTable.add(searchTable ,1 ,1);
    String message; 
    if (SHOW_LIST_ACTION.equals(action))  {
      Collection entities = getEntities();
      if (entities.isEmpty()) {
        message = resourceBundle.getLocalizedString("uc_no_results_were_found", "Sorry, no results were found");
      }
      else {
        message = resourceBundle.getLocalizedString("uc_results_for", "Results for") + ": " + searchString;
        EntityBrowser browser = getBrowser(entities, iwc);
        mainTable.add(browser,1,3);
      }
    }
    else {
      message = resourceBundle.getLocalizedString("uc_fill_in_search_field", "Please fill in the search field");
    }
    // show message for user
    Text messageText = new Text(message);
    messageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
    mainTable.add(messageText ,1 ,2);
      
    Link okayLink = getOkayButton(resourceBundle);
    mainTable.add(okayLink,1,4);
    Form form = new Form();
    form.maintainAllParameters();
    form.add(mainTable);
    add(form,iwc);
	}
  
  private String parseRequest(IWContext iwc)  {
    if (iwc.isParameterSet(SEARCH_SUBMIT_KEY))  {
      // reset browser
      EntityBrowser.releaseBrowser(iwc);
    }
    searchString = iwc.isParameterSet(SEARCH_KEY) ? iwc.getParameter(SEARCH_KEY) : "";
    if (searchString.length() == 0) {
      return DO_NOT_SHOW_LIST_ACTION;
    }
    else  {
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

  private EntityBrowser getBrowser(Collection entities, IWContext iwc)  {
    // define checkbox button converter class
    EntityToPresentationObjectConverter converterToChooseButton = new EntityToPresentationObjectConverter() {

      public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
        return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);  
      } 

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
    String nameKey = User.class.getName()+".FIRST_NAME:" + User.class.getName()+".MIDDLE_NAME:"+User.class.getName()+".LAST_NAME";
    String pinKey = User.class.getName()+".PERSONAL_ID";
    EntityBrowser browser = EntityBrowser.getInstanceUsingExternalForm();
    browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
    browser.setDefaultNumberOfRows(NUMBER_OF_ROWS);
    browser.setEntities("chooser_window_" + searchString, entities);

    browser.setWidth(Table.HUNDRED_PERCENT);
      
    //fonts
    Text column = new Text();
    column.setBold();
    browser.setColumnTextProxy(column);
      
    //    set color of rows
    browser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
    browser.setColorForOddRows("#FFFFFF");
      
    browser.setDefaultColumn(1, nameKey);
    browser.setDefaultColumn(2, pinKey);
    browser.setMandatoryColumn(1, "Choose");
    // set special converters
    browser.setEntityToPresentationConverter("Choose", converterToChooseButton);
    // set mandatory parameters
    browser.addMandatoryParameters(getHiddenParameters(iwc));
    browser.addMandatoryParameter(SEARCH_KEY, searchString);
    return browser;
  }
    
  private Collection getEntities()  {
    if (searchString == null)
      return new ArrayList();
    try {
      UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
      String modifiedSearch = getModifiedSearchString(searchString);
      Collection entities = userHome.findUsersBySearchCondition(modifiedSearch, false);
      return entities;
    }
    // Remote and FinderException
    catch (Exception ex)  {
      throw new RuntimeException(ex.getMessage());
    }
  }
  
  private String getModifiedSearchString(String originalSearchString)  {
    StringBuffer buffer = new StringBuffer("%");
    buffer.append(originalSearchString).append("%");
    return buffer.toString();
  }
    
  private Table getSearchInputField(IWResourceBundle iwrb) {
    Table table = new Table(2,1);
    SubmitButton searchButton = 
      new SubmitButton(iwrb.getLocalizedImageButton("search", "Search"), SEARCH_SUBMIT_KEY, SEARCH_SUBMIT_KEY);
    TextInput searchInput = new TextInput(SEARCH_KEY);
    searchInput.setContent(searchString);
    table.add(searchInput,1,1);
    table.add(searchButton,2,1);
    return table;
  }
    
  
  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
}