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

import java.util.Collection;

import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.helpers.UserHelper;
import com.idega.util.CoreConstants;

public class UserChooserBrowserWindow extends StyledAbstractChooserWindow {
  
  private static int NUMBER_OF_ROWS = 8;
  
  public static final String SEARCH_KEY = "search_key";
  public static final String SEARCH_SUBMIT_KEY = "search_submit_key";
  
  private static final String SHOW_LIST_ACTION = "show_list_action";
  private static final String DO_NOT_SHOW_LIST_ACTION = "do_not_show_list_action";
  
  private String searchString = "";
  
  
  public UserChooserBrowserWindow() {
    setWidth(600);
    setHeight(400);
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
    addTitle(title, TITLE_STYLECLASS);
    // parse request and get search string
    String action = parseRequest(iwc);
    
    Table mainTable = new Table(1, 3);
    mainTable.setCellpadding(0);
    mainTable.setCellspacing(0);
    mainTable.setWidth(Table.HUNDRED_PERCENT);
    mainTable.setStyleClass(1, 3, "main");
    mainTable.setCellpadding(1, 3, 5);
    mainTable.setAlignment(1, 3, Table.HORIZONTAL_ALIGN_RIGHT);
    mainTable.setHeight(2, 5);
    
    Table inputTable = new Table();
    inputTable.setCellpadding(12);
    inputTable.setCellspacing(0);
    inputTable.setStyleClass("main");
    mainTable.add(inputTable, 1, 1);

    Table searchTable = getSearchInputField(resourceBundle);
    inputTable.add(searchTable ,1 ,1);
    inputTable.setWidth(Table.HUNDRED_PERCENT);
    String message; 
    
    UserHelper helper = new UserHelper();
    if (SHOW_LIST_ACTION.equals(action))  {
      Collection entities = helper.getUserEntities(this.searchString);
      if (entities.isEmpty()) {
        message = resourceBundle.getLocalizedString("uc_no_results_were_found", "Sorry, no results were found");
      }
      else {
        message = resourceBundle.getLocalizedString("uc_results_for", "Results for") + ": " + this.searchString;
        
        EntityBrowser browser = helper.getUserBrowser(entities, this.searchString, iwc, NUMBER_OF_ROWS);
        inputTable.add(browser,1,3);
        inputTable.setCellpaddingTop(1, 3, 0);
        inputTable.setCellpaddingBottom(1, 2, 2);
      }
    }
    else {
      message = resourceBundle.getLocalizedString("uc_fill_in_search_field", "Please fill in the search field");
    }
    // show message for user
    Text messageText = new Text(message);
    messageText.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
    inputTable.add(messageText ,1 ,2);
      
    mainTable.add(getOkayButton(resourceBundle), 1, 3);
    
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
    this.searchString = iwc.isParameterSet(SEARCH_KEY) ? iwc.getParameter(SEARCH_KEY).trim() : "";
    if (this.searchString.length() == 0) {
      return DO_NOT_SHOW_LIST_ACTION;
    }
    else  {
      return SHOW_LIST_ACTION;
    }
  }
  
  private StyledButton getOkayButton(IWResourceBundle resourceBundle) {
    String okayString = resourceBundle.getLocalizedString("Close", "close");
    CloseButton okayLink = new CloseButton(okayString);
    okayLink.setOnClick("window.close(); return false;");
    StyledButton close = new StyledButton(okayLink);
    close.setAlignment(Table.HORIZONTAL_ALIGN_RIGHT);
    return close;
  }

  private Table getSearchInputField(IWResourceBundle iwrb) {
    Table table = new Table(3, 1);
    table.setCellpadding(0);
    table.setCellspacing(0);
    table.setWidth(2, 5);
    
    StyledButton searchButton = new StyledButton(new SubmitButton(iwrb.getLocalizedString("search", "Search"), SEARCH_SUBMIT_KEY, SEARCH_SUBMIT_KEY));
    TextInput searchInput = new TextInput(SEARCH_KEY);
    searchInput.setContent(this.searchString);
    table.add(searchInput, 1, 1);
    table.add(searchButton, 3, 1);
    
    return table;
  }
  
  public String getBundleIdentifier(){
    return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
  }
}