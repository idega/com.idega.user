package com.idega.user.presentation;


import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;

public class StyledIWAdminWindow extends Window {

public static final String MENU_COLOR = "#EFEFEF";
private final static String IW_BUNDLE_IDENTIFIER="com.idega.user"; //changed from com.idega.core
public final static String STYLE = "font-family:arial; font-size:8pt; color:#000000; text-align: justify; border: 1 solid #000000;";
public final static String STYLE_2 = "font-family:arial; font-size:8pt; color:#000000; text-align: justify;";

private IWBundle iwb;
public IWBundle iwbCore;
public IWBundle iwbUser;
private IWResourceBundle iwrb;
private Form adminForm;
private Table adminTable;
private Table headerTable;
private Table leftTable;
private Table rightTable;
private boolean merged = true;
private boolean displayEmpty = false;

private String rightWidth = "160";
private String method = "post";
private int _cellPadding = 0;

private Page parentPage;
private String styleScript = "UserApplicationStyle.css";
private String styleSrc = "";
private String inputTextStyle = "text";
private String backgroundTableStyle = "back";
private String mainTableStyle = "main";
private String bannerTableStyle = "banner";


public Image felix;  

  public StyledIWAdminWindow(){
    super();
  }

  public StyledIWAdminWindow(String name){
    super(name);
  }

  public StyledIWAdminWindow(int width, int heigth) {
    super(width,heigth);
  }

  public StyledIWAdminWindow(String name,int width,int height){
    super(name,width,height);
  }

  public StyledIWAdminWindow(String name,String url){
    super(name,url);
  }

  public StyledIWAdminWindow(String name, int width, int height, String url){
    super(name,width,height,url);
  }

  public StyledIWAdminWindow(String name,String classToInstanciate,String template){
    super(name,classToInstanciate,template);
  }

  public StyledIWAdminWindow(String name,Class classToInstanciate,Class template){
    super(name,classToInstanciate,template);
  }

  public StyledIWAdminWindow(String name,Class classToInstanciate){
    super(name,classToInstanciate);
  }

  public Form getUnderlyingForm(){
    return adminForm;
  }

  private void makeTables(Image bannerImage) {
    
    adminForm = new Form();
    adminForm.setMethod(method);

    adminTable = new Table(2,2);
    adminTable.mergeCells(1,1,2,1);
    adminTable.setCellpadding(0);
    adminTable.setCellspacing(0);
    adminTable.setWidth("100%");
    adminTable.setHeight("100%");
    adminTable.setHeight(2,"100%");
    adminTable.setColor(1,2,"#FFFFFF");
    if ( !merged ) {
			adminTable.setColor(2,2,MENU_COLOR);
			adminTable.setWidth(2,2,rightWidth);
    }
    else {
			adminTable.mergeCells(1,2,2,2);
    }
    adminTable.setRowVerticalAlignment(2,"top");
    adminForm.add(adminTable);

    headerTable = new Table();
    headerTable.setCellpadding(0);
    headerTable.setCellspacing(0);
    headerTable.setStyleClass(bannerTableStyle);
    headerTable.setWidth("100%");
    headerTable.setAlignment(2,1,"right");
    
		headerTable.add(bannerImage);
    adminTable.add(headerTable,1,1);

    leftTable = new Table();
    leftTable.setStyleClass(mainTableStyle);
      leftTable.setCellpadding(_cellPadding);
      leftTable.setAlignment("center");
      leftTable.setWidth("100%");
      if ( merged ) {
	leftTable.setHeight("100%");
	leftTable.setCellspacing(0);
	leftTable.setVerticalAlignment(1,1,"top");
      }
      adminTable.add(leftTable,1,2);

    rightTable = new Table();
    rightTable.setStyleClass(mainTableStyle);
      rightTable.setCellpadding(8);
      rightTable.setAlignment("center");
      rightTable.setWidth("100%");
      if ( !merged ) {
	adminTable.add(rightTable,2,2);
      }
  }
  public void add(PresentationObject obj, IWContext iwc) {
		iwb = getBundle(iwc);
		iwbCore = iwc.getApplication().getBundle(IW_BUNDLE_IDENTIFIER);
    felix = iwb.getImage("top.gif","idegaWeb Member");
    if( !displayEmpty ){
      if(adminTable==null){
				makeTables(felix);
				super.add(adminTable);
      }
      leftTable.add(obj,1,1);
    }
    else super.add(obj);
  }
  public void addTitle(String title) {
    Text adminTitle = new Text(title+"&nbsp;&nbsp;");
      adminTitle.setBold();
      adminTitle.setFontColor("#FFFFFF");
      adminTitle.setFontSize("3");
      adminTitle.setFontFace(Text.FONT_FACE_ARIAL);

    super.setTitle(title);

    headerTable.add(adminTitle,2,1);
  }

  public void addTitle(String title,String style) {
    Text adminTitle = new Text(title+"&nbsp;&nbsp;");
      adminTitle.setFontStyle(style);

    super.setTitle(title);

    headerTable.add(adminTitle,2,1);
  }


  public Text formatText(String s, boolean bold){
    Text T= new Text();
    if ( s != null ) {
      T= new Text(s);
      if ( bold )
	T.setBold();
      T.setFontColor("#000000");
      T.setFontSize(Text.FONT_SIZE_7_HTML_1);
      T.setFontFace(Text.FONT_FACE_VERDANA);
    }
    return T;
  }

  public void formatText(Text text, boolean bold){
    if ( bold )
      text.setBold();
    text.setFontColor("#000000");
    text.setFontSize(Text.FONT_SIZE_7_HTML_1);
    text.setFontFace(Text.FONT_FACE_VERDANA);
  }

  public Text formatText(String s) {
    Text T = formatText(s,true);
    return T;
  }

  public Text formatHeadline(String s) {
    Text T= new Text();
    if ( s != null ) {
      T= new Text(s);
      T.setBold();
      T.setFontColor("#000000");
      T.setFontSize(Text.FONT_SIZE_10_HTML_2);
      T.setFontFace(Text.FONT_FACE_VERDANA);
    }
    return T;
  }
	public void _main(IWContext iwc)throws Exception{
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		iwbCore = iwc.getApplication().getBundle(IW_BUNDLE_IDENTIFIER);
		felix = iwb.getImage("top.gif","idegaWeb Member");
	//  iwbUser = iwc.getApplication().getBundle(IW_BUNDLE_USER_ID);
		if( !displayEmpty ){
			makeTables(felix);
			setAllMargins(0);

			if ( merged ){
	super.add(adminTable);
			}
			else{
	super.add(adminForm);
			}
		}
		/**
		 * sets the reference to the isi-stylesheet
		 */
		parentPage = this.getParentPage();
		styleSrc = iwb.getVirtualPathWithFileNameString(styleScript);
		parentPage.addStyleSheetURL(styleSrc);

		super._main(iwc);
	}

	public void main(IWContext iwc)throws Exception{
	}


  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

}
