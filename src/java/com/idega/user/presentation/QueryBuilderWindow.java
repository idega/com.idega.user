package com.idega.user.presentation;

import com.idega.block.dataquery.presentation.QueryBuilder;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Jun 11, 2003
 */
public class QueryBuilderWindow extends IWAdminWindow {
  
  public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  public QueryBuilderWindow() {
    setResizable(true);
    setWidth(800);
    setHeight(600);
  }

  public void main(IWContext iwc){  
    // get resource bundle 
    IWResourceBundle iwrb = getResourceBundle(iwc);
    addTitle(iwrb.getLocalizedString("user_report_report_builder", "ReportBuilder"), IWConstants.BUILDER_FONT_STYLE_TITLE);
    QueryBuilder queryBuilder = new QueryBuilder();
    add(queryBuilder);
  }
    
  public String getBundleIdentifier() {
    return IW_BUNDLE_IDENTIFIER;
  } 


    
}
