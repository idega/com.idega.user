package com.idega.user.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.idega.block.importer.presentation.Importer;
import com.idega.event.IWPresentationEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectOption;
import com.idega.repository.data.ImplementorRepository;
import com.idega.user.block.search.presentation.SearchForm;
import com.idega.user.block.search.presentation.SearchWindow;
import com.idega.user.event.ChangeClassEvent;
import com.idega.user.handler.UserNationalRegisterFileImportHandler;
import com.idega.user.handler.UserNationalRegisterImportFile;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.CreateUser;
import com.idega.user.presentation.MassMovingWindow;
import com.idega.user.presentation.RoleMastersWindow;
import com.idega.user.presentation.UserClubMemberExchangeWindow;
import com.idega.user.presentation.UserWorkReportWindow;

/**
 * <p>
 * Title: idegaWeb
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: idega Software
 * </p>
 * 
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson </a>
 * @version 1.0
 */

public class Toolbar extends Page implements IWBrowserView {

	protected String title;

	protected IWBundle iwb;

	protected IWResourceBundle iwrb;

	protected String _controlTarget = null;

	protected IWPresentationEvent _controlEvent = null;

	protected Vector _toolbarElements = new Vector();

	private SearchForm searchForm = new SearchForm();

	private String selectedGroupProviderStateId = null;

	private String userApplicationMainAreaStateId = null;

	private String menuTableStyle = "menu";

	private String styledLink = "styledLink";

	public Toolbar() {
		// default constructor
	}

	public void add(ToolbarElement element) {
		_toolbarElements.add(element);
	}

	public void setSelectedGroupProviderStateId(String selectedGroupProviderStateId) {
		this.selectedGroupProviderStateId = selectedGroupProviderStateId;
	}

	public void add(Toolbar toolbar) {
		_toolbarElements.add(toolbar);
		//    addSeperator();
		//    List l = getToolbarElements();
		//    if (l != null) {
		//      Iterator iter = l.iterator();
		//      while (iter.hasNext()) {
		//        Object item = iter.next();
		//        this.add((ToolbarElement)item);
		//      }
		//    }
	}

	protected List getToolbarElements() {
		return _toolbarElements;
	}

	public String getBundleIdentifier() {
		return "com.idega.user";
	}

	public void setControlEventModel(IWPresentationEvent model) {
		_controlEvent = model;
		searchForm.setControlEventModel(model);
	}

	public void setControlTarget(String controlTarget) {
		_controlTarget = controlTarget;
		searchForm.setControlTarget(controlTarget);

	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff") != null;
		boolean showReportGenerator = iwc.getApplicationSettings().getProperty("temp_show_report_generator") != null;
		boolean showCalendar = iwc.getApplicationSettings().getProperty("temp_show_calendar") != null;

		//	added for stylesheet writout:
		//			parentPage = this.getParentPage();
		//			styleSrc = iwb.getVirtualPathWithFileNameString(styleScript);
		//			parentPage.addStyleSheetURL(styleSrc);

		Table toolbarTable = new Table(3, 3);
		boolean useDropdown = iwb.getBooleanProperty("use_dropdown_in_toolbar", false);

		//	added for isi style
		//		toolbarTable.setStyleClass(menuTableStyle); --- changed

		toolbarTable.setCellpadding(0);
		toolbarTable.setCellspacing(0);
		toolbarTable.setStyleClass(menuTableStyle);
		toolbarTable.setWidth(Table.HUNDRED_PERCENT);
		toolbarTable.setHeight(2, Table.HUNDRED_PERCENT);
		//    toolbarTable.setHeight(1,1);
		//    toolbarTable.setHeight(3,1);

		//commented out 7/10/03 - for isi styles - birna
		//    IWColor color = new IWColor(207,208,210);//jonni color
		//    this.setBackgroundColor(color);
		//    this.getParentPage().setBackgroundColor(color);
		//    
		//    toolbarTable.setColor(1,2,color);
		//    toolbarTable.setColor(1,1,color.brighter());
		//    toolbarTable.setColor(2,1,color.brighter());
		//    toolbarTable.setColor(1,3,color.darker());
		//    toolbarTable.setColor(2,3,color.darker());

		toolbarTable.setAlignment(2, 2, Table.HORIZONTAL_ALIGN_RIGHT);

		add(toolbarTable);

		Table toolbar1 = new Table(11, 1);

		//  toolbar1.setStyleClass(menuTableStyle);
		//		Image menuTile = iwb.getImage("menu_tile.gif");
		//		toolbar1.setBackgroundImage(menuTile);
		//  toolbar1.setColor(color);

		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);
		//toolbar1.setWidth(Table.HUNDRED_PERCENT);
		//toolbar1.setHeight(Table.HUNDRED_PERCENT);
		//int iconDimentions = 20;

		//User
		Table button = new Table(2, 1);
		button.setCellpadding(0);
		Image iconCrUser = iwb.getImage("new_user.gif");
		button.setCellpaddingLeft(1, 1, 7);
		button.add(iconCrUser, 1, 1);
		Text text = new Text(iwrb.getLocalizedString("new.member", "New member"));
		//    text.setFontFace(Text.FONT_FACE_VERDANA);
		//    text.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link tLink11 = new Link(text);
		tLink11.setStyleClass(styledLink);
		tLink11.setWindowToOpen(CreateUser.class);
		//tLink11.setClassToInstanciate(CreateUser.class);
		//tLink11.setTarget("iwb_main");
		button.setWidth(2, 10);
		button.setCellpaddingTop(2, 1, 2);
		button.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button.add(tLink11, 2, 1);
		toolbar1.add(button, 2, 1);

		//Group
		Table button2 = new Table(2, 1);
		button2.setCellpadding(0);
		Image iconCrGroup = iwb.getImage("new_group.gif");
		button2.add(iconCrGroup, 1, 1);
		Text text2 = new Text(iwrb.getLocalizedString("new.group", "New group"));
		//    text2.setFontFace(Text.FONT_FACE_VERDANA);
		//    text2.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link tLink12 = new Link(text2);
		tLink12.setStyleClass(styledLink);
		tLink12.setWindowToOpen(CreateGroupWindow.class);
		if (selectedGroupProviderStateId != null)
			tLink12.addParameter(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId);
		button2.setWidth(2, 10);
		button2.setCellpaddingTop(2, 1, 2);
		button2.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button2.add(tLink12, 2, 1);
		toolbar1.add(button2, 3, 1);

		if (iwc.isSuperAdmin()) {
			Table button4 = new Table(2, 1);
			button4.setCellpadding(0);
			Image iconRoleMasters = iwb.getImage("other_choises.gif");
			button4.add(iconRoleMasters, 1, 1);
			Text text4 = new Text(iwrb.getLocalizedString("button.role_masters", "Role Masters"));
			Link tLink14 = new Link(text4);
			tLink14.setStyleClass(styledLink);
			tLink14.setWindowToOpen(RoleMastersWindow.class);
			button4.setWidth(2, 10);
			button4.setCellpaddingTop(2, 1, 2);
			button4.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
			button4.add(tLink14, 2, 1);
			toolbar1.add(button4, 4, 1);
		}

		//Search temp
		Table button3 = new Table(3, 1);
		button3.setCellpadding(0);
		Image iconSearch = iwb.getImage("search.gif");
		button3.add(iconSearch, 1, 1);
		Text text3 = new Text(iwrb.getLocalizedString("button.search", "Search"));
		//		text3.setFontFace(Text.FONT_FACE_VERDANA);
		//		text3.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link tLink13 = new Link(text3);
		tLink13.setStyleClass(styledLink);
		if (userApplicationMainAreaStateId != null)
			tLink13.addParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY, userApplicationMainAreaStateId);
		tLink13.setWindowToOpen(SearchWindow.class);
		button3.setCellpaddingTop(2, 1, 2);
		button3.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
		button3.add(tLink13, 2, 1);
		button3.setWidth(2, 10);
		button3.setWidth(3, 20);
		button3.add(Text.NON_BREAKING_SPACE, 3, 1);
		Image dottedImage = iwb.getImage("dotted.gif");
		button3.setAlignment(3, 1, "right");
		button3.add(dottedImage, 3, 1);
		toolbar1.add(button3, 5, 1);

		//		DropdownMenu dropdownMenu = new DropdownMenu();
		//		dropdownMenu.setOnChange("window.open(this.options[this.selectedIndex].value)");
		//Member exchange window temp
		
		Form form = new Form();
		DropdownMenu menu = new DropdownMenu("other_choices");
		menu.addMenuElement("", "");
		form.add(menu);
		
		if (showISStuff) {

			Table button5 = new Table(2, 1);
			button5.setCellpadding(0);
			Image iconExchange = iwb.getImage("other_choises.gif");
			button5.add(iconExchange, 1, 1);
			Text text5 = null;
			if (useDropdown) {
				text5 = new Text(iwrb.getLocalizedString("button.other_choices", "Other choices"));
			}
			else {
				text5 = new Text(iwrb.getLocalizedString("button.club_member_exchange","Club exchange"));
			}
			//			Text text5 = new
			// Text(iwrb.getLocalizedString("button.club_member_exchange","Club
			// exchange"));
			//			text5.setFontFace(Text.FONT_FACE_VERDANA);
			//			text5.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink15 = new Link(text5);
			tLink15.setStyleClass(styledLink);
			button5.setWidth(2, 10);
			button5.setCellpaddingTop(2, 1, 2);
			button5.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_TOP);
			button5.add(tLink15, 2, 1);
			toolbar1.add(button5, 6, 1);

			Class clubMemberExchangeWindow = ImplementorRepository.getInstance().getAnyClassImpl(UserClubMemberExchangeWindow.class, this.getClass());
			if (clubMemberExchangeWindow != null) {
				logWarning("[Toolbar]  Implementation of UserClubMemberExchangeWindow could not be found. Implementing bundle was not loaded.");
				///tLink15.setWindowToOpen(clubMemberExchangeWindow);
				SelectOption exchange = new SelectOption(iwrb.getLocalizedString("button.club_member_exchange", "Member exhange"), "1");
				exchange.setWindowToOpenOnSelect(clubMemberExchangeWindow, null);
				menu.addOption(exchange);
			}
			if (useDropdown) {
				toolbar1.setCellpaddingLeft(7, 1, 10);
				toolbar1.add(form, 7, 1);
			}
			//			dropdownMenu.addMenuElement(tLink15.getURL(),tLink15.toString());

			//			Table button5 = new Table(2,1);
			//			button5.setCellpadding(0);
			//			Image iconExchange = iwb.getImage("isi_new_group.gif");
			//			button5.add(iconExchange,1,1);
			//			Text text5 = new
			// Text(iwrb.getLocalizedString("button.club_member_exchange","Club
			// exchange"));
			//			text5.setFontFace(Text.FONT_FACE_VERDANA);
			//			text5.setFontSize(Text.FONT_SIZE_7_HTML_1);
			//			Link tLink15 = new Link(text5);
			//			//TODO Eiki add somekind of plugin lookup for toolbar items
			//do it it in the same way like above

			//			tLink15.setWindowToOpen("is.idega.idegaweb.member.presentation.ClubMemberExchangeWindow");
			//			button5.add(tLink15,2,1);
			//			toolbar1.add(button5,6,1);
		}
		//mass moving
		if (showISStuff) {

			Table button4 = new Table(1, 1);
			button4.setCellpadding(0);
			Text text4 = new Text(iwrb.getLocalizedString("button.massMoving", "Move"));
			//					text4.setFontFace(Text.FONT_FACE_VERDANA);
			//					text4.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink14 = new Link(text4);
			tLink14.setStyleClass(styledLink);
			if (userApplicationMainAreaStateId != null) {
				tLink14.addParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY, userApplicationMainAreaStateId);
			}
			if (selectedGroupProviderStateId != null) {
				tLink14.addParameter(MassMovingWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId);
			}
			tLink14.setWindowToOpen(MassMovingWindow.class);
			Map parameters = new HashMap();
			if (userApplicationMainAreaStateId != null) {
				parameters.put(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY, userApplicationMainAreaStateId);
			}
			if (selectedGroupProviderStateId != null) {
				parameters.put(MassMovingWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY, selectedGroupProviderStateId);
			}
			SelectOption massOption = new SelectOption(iwrb.getLocalizedString("button.massMoving", "Move"), "1");
			massOption.setWindowToOpenOnSelect(MassMovingWindow.class, parameters);
			menu.addOption(massOption);
			button4.add(tLink14, 1, 1);
			if (!useDropdown) {
				toolbar1.add(button4, 7, 1);
			}

			//				dropdownMenu.addMenuElement(tLink14.getURL(),tLink14.toString());

			//				 Table button4 = new Table(2,1);
			//				 button4.setCellpadding(0);
			//				 Image iconMassMoving = iwb.getImage("isi_new_group.gif");
			//				 button4.add(iconMassMoving,1,1);
			//				 Text text4 = new
			// Text(iwrb.getLocalizedString("button.massMoving","Move"));
			//				 text4.setFontFace(Text.FONT_FACE_VERDANA);
			//				 text4.setFontSize(Text.FONT_SIZE_7_HTML_1);
			//				 Link tLink14 = new Link(text4);
			//				 if (userApplicationMainAreaStateId != null) {
			//					 tLink14.addParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY,
			// userApplicationMainAreaStateId);
			//				 }
			//				 if (selectedGroupProviderStateId != null) {
			//					 tLink14.addParameter(MassMovingWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY,
			// selectedGroupProviderStateId);
			//				 }
			//				 tLink14.setWindowToOpen(MassMovingWindow.class);
			//				 button4.add(tLink14,2,1);
			//				 toolbar1.add(button4,5,1);
		}

		if (showISStuff && iwc.isSuperAdmin()) {
			Table button6 = new Table(2, 1);
			button6.setCellpadding(0);
			//			Image iconImport = iwb.getImage("import.gif");
			//			button6.add(iconImport, 1, 1);
			Text text6 = new Text(iwrb.getLocalizedString("nationRegister", "National Register"));
			//			text6.setFontFace(Text.FONT_FACE_VERDANA);
			//			text6.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink16 = new Link(text6);
			tLink16.setStyleClass(styledLink);

			ImplementorRepository repository = ImplementorRepository.getInstance();
			Class nationalRegisterImportFile = repository.getAnyClassImpl(UserNationalRegisterImportFile.class, this.getClass());
			Class nationalRegisterFileImportHandler = repository.getAnyClassImpl(UserNationalRegisterFileImportHandler.class, this.getClass());
			if (nationalRegisterImportFile == null || nationalRegisterFileImportHandler == null) {
				logWarning("[Toolbar]  Implementation of UserNationalRegisterImportFile or UserNationalRegisterImportHandler could not be found. Implementing bundle(s) was not loaded.");
			}
			else {
				tLink16.setParameter(Importer.PARAMETER_IMPORT_FILE, nationalRegisterImportFile.getName());
				tLink16.setParameter(Importer.PARAMETER_IMPORT_HANDLER, nationalRegisterFileImportHandler.getName());
					
				Map map = new HashMap();
				map.put(Importer.PARAMETER_IMPORT_FILE, nationalRegisterImportFile.getName());
				map.put(Importer.PARAMETER_IMPORT_HANDLER, nationalRegisterFileImportHandler.getName());
				
				tLink16.setWindowToOpen(Importer.class);
				button6.setWidth(2, 15);
				button6.add(tLink16, 2, 1);
				if (!useDropdown) {
					toolbar1.add(button6, 8, 1);
				}
				SelectOption importer = new SelectOption(iwrb.getLocalizedString("nationRegister", "National Register"), "1");
				importer.setWindowToOpenOnSelect(Importer.class, map);
				menu.addOption(importer);
			}
			//DOES NOT WORK - and should be plugin based
//			ImplementorRepository repository =  ImplementorRepository.getInstance();
//			Class nationalRegisterImportFile = repository.getAnyClassImpl(UserNationalRegisterImportFile.class,this.getClass());
//			Class nationalRegisterFileImportHandler = repository.getAnyClassImpl(UserNationalRegisterFileImportHandler.class, this.getClass());
//            if (nationalRegisterImportFile == null || nationalRegisterFileImportHandler == null) {
//				logWarning("[Toolbar]  Implementation of UserNationalRegisterImportFile or UserNationalRegisterImportHandler could not be found. Implementing bundle(s) was not loaded.");
//				tLink16.setParameter(Importer.PARAMETER_IMPORT_FILE, nationalRegisterImportFile.getName());
//				tLink16.setParameter(Importer.PARAMETER_IMPORT_HANDLER, nationalRegisterFileImportHandler.getName());
//
//				tLink16.setWindowToOpen(Importer.class);
//				button6.setWidth(2,15);
//				button6.add(tLink16, 2, 1);
//				toolbar1.add(button6, 8, 1);
//           }
			
			//TODO: Eiki make plugin based
			tLink16.setParameter(Importer.PARAMETER_IMPORT_FILE, "is.idega.block.nationalregister.data.NationalRegisterImportFile");
			tLink16.setParameter(Importer.PARAMETER_IMPORT_HANDLER, "is.idega.block.nationalregister.business.NationalRegisterFileImportHandler");

			tLink16.setWindowToOpen(Importer.class);
			button6.setWidth(2,15);
			button6.add(tLink16, 2, 1);
			if (!useDropdown) {
				toolbar1.add(button6, 8, 1);
			}
			
			Map map = new HashMap();
			map.put(Importer.PARAMETER_IMPORT_FILE, "is.idega.block.nationalregister.data.NationalRegisterImportFile");
			map.put(Importer.PARAMETER_IMPORT_HANDLER, "is.idega.block.nationalregister.data.NationalRegisterImportFile");
			
			SelectOption importer = new SelectOption(iwrb.getLocalizedString("nationRegister", "National Register"), "1");
			importer.setWindowToOpenOnSelect(Importer.class, map);
			menu.addOption(importer);

			//			String tLink16String = tLink16.toString();
//			tLink16String = java.net.URLEncoder.encode(tLink16String);
//			
//			tLink16.setURL(iwc.getApplication().getWindowOpenerURI(Importer.class));
//			tLink16.setParameter(tLink16.toString(),iwc.getApplication().getWindowOpenerURI(Importer.class));
			
 
 

			//			String tLink16String = tLink16.toString();
			//			tLink16String = java.net.URLEncoder.encode(tLink16String);
			//			
			//			tLink16.setURL(iwc.getApplication().getWindowOpenerURI(Importer.class));
			//			tLink16.setParameter(tLink16.toString(),iwc.getApplication().getWindowOpenerURI(Importer.class));

			//			dropdownMenu.addMenuElement(tLink16.getURL(),tLink16.toString());

		}
		//		toolbar1.add(dropdownMenu,11,1);

		//    if(showReportGenerator){
		//      Table button7 = new Table(2,1);
		//      button7.setCellpadding(0);
		//  // Image iconExchange = iwb.getImage("isi_new_group.gif");
		//  // button7.add(iconExchange,1,1);
		//      Text text7 = new
		// Text(iwrb.getLocalizedString("button.report_query_builder","Query
		// builder"));
		//      text7.setFontFace(Text.FONT_FACE_VERDANA);
		//      text7.setFontSize(Text.FONT_SIZE_7_HTML_1);
		//      Link tLink17 = new Link(text7);
		//      tLink17.setWindowToOpen(com.idega.user.presentation.QueryBuilderWindow.class);
		//      button7.add(tLink17,2,1);
		//      toolbar1.add(button7,8,1);
		//    }

		if (showReportGenerator) {
			Table button8 = new Table(2, 1);
			button8.setCellpadding(0);
			//    Image iconExchange = iwb.getImage("isi_new_group.gif");
			//    button8.add(iconExchange,1,1);
			Text text8 = new Text(iwrb.getLocalizedString("button.report_report_builder", "Report builder"));
			//      text8.setFontFace(Text.FONT_FACE_VERDANA);
			//      text8.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink18 = new Link(text8);
			tLink18.setStyleClass(styledLink);
			tLink18.setWindowToOpen(com.idega.block.datareport.presentation.ReportOverviewWindow.class);
			button8.add(tLink18, 2, 1);
			if (!useDropdown) {
				toolbar1.add(button8, 9, 1);
			}

			SelectOption report = new SelectOption(iwrb.getLocalizedString("button.report_report_builder", "Report builder"), "1");
			report.setWindowToOpenOnSelect(com.idega.block.datareport.presentation.ReportOverviewWindow.class, null);
			menu.addOption(report);
		}

		//finance
		// toolbar1.add(
		// this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("finance","Finance"),
		// iwb.getImage("finance.gif"),
		// com.idega.block.finance.presentation.AccountViewer.class),4,1);

		//work reports window temp
		if (showISStuff) {
			Table button5 = new Table(2, 1);
			button5.setCellpadding(0);
			//		 Image iconExchange = iwb.getImage("isi_new_group.gif");
			//		 button5.add(iconExchange,1,1);
			Text text5 = new Text(iwrb.getLocalizedString("button.work_reports", "Work Reports"));
			//		 text5.setFontFace(Text.FONT_FACE_VERDANA);
			//		 text5.setFontSize(Text.FONT_SIZE_7_HTML_1);

			/*
			 * LinkContainer tLink15 = new LinkContainer();
			 * 
			 * tLink15.add(text5); tLink15.setURL("/index.jsp?ib_page=4");
			 * tLink15.setAsPopup(iwrb.getLocalizedString("button.work_reports","Work
			 * Reports"),"800","600");
			 */

			Link tLink15 = new Link(text5);
			tLink15.setStyleClass(styledLink);
			Class workReportWindow = ImplementorRepository.getInstance().getAnyClassImpl(UserWorkReportWindow.class, this.getClass());
			if (workReportWindow != null) {
				logWarning("[Toolbar]  Implementation of UserWorkReportWindow could not be found. Implementing bundle was not loaded.");
				tLink15.setWindowToOpen(workReportWindow);

				button5.add(tLink15, 2, 1);
				if (!useDropdown) {
					toolbar1.add(button5, 10, 1);
				}

				SelectOption workReport = new SelectOption(iwrb.getLocalizedString("button.work_reports", "Work Reports"), "1");
				workReport.setWindowToOpenOnSelect(workReportWindow, null);
				menu.addOption(workReport);
			}
		}
		if (showCalendar) {
			Table butt = new Table(2, 1);
			butt.setCellpadding(0);
			Text tex = new Text(iwrb.getLocalizedString("button.calendar", "Calendar Window"));
			Link tLin = new Link(tex);
			tLin.setStyleClass(styledLink);
			tLin.setWindowToOpen(com.idega.block.cal.presentation.CalendarWindow.class);
			butt.add(tLin, 2, 1);
			if (!useDropdown) {
				toolbar1.add(butt, 11, 1);
			}

			SelectOption calendar = new SelectOption(iwrb.getLocalizedString("button.calendar", "Calendar Window"), "1");
			calendar.setWindowToOpenOnSelect(com.idega.block.cal.presentation.CalendarWindow.class, null);
			menu.addOption(calendar);
		}

		// toolbar1.add(
		// this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("reports","Reports"),
		// iwb.getImage("reports.gif"),
		// com.idega.block.reports.presentation.Reporter.class),5,1);

		//To do - stickies
		//    toolbar1.add( this.getToolbarButtonWithChangeClassEvent("To do",
		// iwb.getImage("todo.gif"),
		// com.idega.block.news.presentation.News.class),7,1);

		//settings
		//  toolbar1.add(
		// this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("settings","Settings"),
		// iwb.getImage("settings.gif"),com.idega.block.news.presentation.News.class
		// ),4,1);

		//view
		//dropdownmenu
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent("Yfirlit",
		// iwb.getImage("views.gif"),
		// com.idega.block.news.presentation.News.class),7,1);

		//search
		Table button9 = new Table(2, 1);
		Text text9 = new Text(iwrb.getLocalizedString("fast_search", "Fast search"));
		//		text9.setFontFace(Text.FONT_FACE_VERDANA);
		//		text9.setFontSize(Text.FONT_SIZE_7_HTML_1);
		button9.add(text9, 1, 1);
		button9.setHorizontalAlignment("right");
		IWLocation location = (IWLocation) this.getLocation().clone();
		location.setSubID(1);
		searchForm.setLocation(location, iwc);
		searchForm.setArtificialCompoundId(getCompoundId(), iwc);
		searchForm.setHorizontalAlignment("right");
		searchForm.setTextInputValue(iwrb.getLocalizedString("insert_search_string", "Insert a search string"));
		//toolbar1.add(button9,10,1);
		toolbarTable.setAlignment(2, 2, "right");
		toolbarTable.setVerticalAlignment(2, 2, "top");
		//  toolbarTable.add(text9,2,2);
		toolbarTable.add(searchForm, 3, 2);

		/*
		 * Text text3 = new Text("&nbsp;Reset");
		 * text3.setFontFace(Text.FONT_FACE_VERDANA);
		 * text3.setFontSize(Text.FONT_SIZE_7_HTML_1); Link resetLink = new
		 * Link(text3); resetLink.addEventModel(new ResetPresentationEvent());
		 * if(_controlEvent != null){ resetLink.addEventModel(_controlEvent); }
		 * if(_controlTarget != null){ resetLink.setTarget(_controlTarget); }
		 * 
		 * toolbar1.add(resetLink,8,1);
		 */

		//    toolbarTable.add(toolbar1,1,2);
		//	this.add(toolbar1);
		toolbarTable.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_LEFT);
		toolbarTable.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		toolbarTable.add(toolbar1, 1, 2);

	}

	protected Table getToolbarButtonWithChangeClassEvent(String textOnButton, Image icon, Class changeClass) {
		Table button = new Table(2, 1);
		button.setCellpadding(0);
		Text text = new Text(textOnButton);
		text.setFontFace(Text.FONT_FACE_VERDANA);
		text.setFontSize(Text.FONT_SIZE_7_HTML_1);
		Link eventLink = new Link(text);
		button.add(icon, 1, 1);
		button.add(eventLink, 2, 1);
		eventLink.addEventModel(new ChangeClassEvent(changeClass));
		if (_controlEvent != null) {
			eventLink.addEventModel(_controlEvent);
		}
		if (_controlTarget != null) {
			eventLink.setTarget(_controlTarget);
		}

		return button;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param string
	 */
	public void setUserApplicationMainAreaStateId(String string) {
		userApplicationMainAreaStateId = string;
	}

}