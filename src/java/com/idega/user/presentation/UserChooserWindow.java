package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.builder.business.BuilderConstants;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;

/**
 * @author gimmi
 */
public class UserChooserWindow extends StyledAbstractChooserWindow {

	private String PARAMETER_SEARCH = "ucw_ss";
	private String PARAMETER_VIEW_ALL = "ucw_va";
	private String PARAMETER_CURRENT_PAGE = "ucw_pc";
	private String PARAMETER_USER_ID = "ucw_uid";
	private String TABLE_WIDTH = "75%";
	private int USERS_PER_PAGE = 25;

	private boolean showAll = false;
	private IWResourceBundle iwrb;
	private String searchString = "";
	private Collection users;
	private boolean usingUserPks = false;
	public static String AVAILABLE_USER_PKS_SESSION_PARAMETER = "us_ch_av_us_sp";
	public static String USING_AVAILABLE_USER_PKS_SESSION_PARAMETER = "ucw_upsp";
	private int currentPage = 0;

	private String mainTableStyle = "main";

	public UserChooserWindow() {
		setTitle("User chooser");
		setWidth(500);
		setHeight(500);
		//    setCellpadding(5);
		setScrollbar(true);
		this.getLocation().setApplicationClass(this.getClass());
		this.getLocation().isInPopUpWindow(true);
	}

	private void init(IWContext iwc) {

		searchString = iwc.getParameter(PARAMETER_SEARCH);
		iwrb = iwc.getIWMainApplication().getBundle(BuilderConstants.STANDARD_IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		showAll = iwc.isParameterSet(PARAMETER_VIEW_ALL);

		if (iwc.isParameterSet(PARAMETER_CURRENT_PAGE)) {
			currentPage = Integer.parseInt(iwc.getParameter(PARAMETER_CURRENT_PAGE));
		}
		//int start = currentPage * USERS_PER_PAGE;

		try {
			String useUserPks = (String) iwc.getSessionAttribute(USING_AVAILABLE_USER_PKS_SESSION_PARAMETER);
			if (useUserPks != null) {
				usingUserPks = true;
			}
			Collection availableUserPks = (Collection) iwc.getSessionAttribute(AVAILABLE_USER_PKS_SESSION_PARAMETER);
			String[] userIds = null;
			if (usingUserPks && availableUserPks != null) {
				userIds = new String[availableUserPks.size()];
				Iterator iter = availableUserPks.iterator();
				int counter = 0;
				while (iter.hasNext()) {
					Object i = iter.next();
					userIds[counter++] = i.toString();
				}
			}
			if (usingUserPks && searchString == null) {
				showAll = true;
			}

			UserHome uHome = (UserHome) IDOLookup.getHome(User.class);
			if (showAll) {
				if (usingUserPks && userIds != null) {
					users = uHome.findUsers(userIds);
				}
				else {
					users = uHome.findAllUsersOrderedByFirstName();
				}
			}
			else if (searchString != null) {
				users = uHome.findUsersBySearchCondition(searchString, userIds, false);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * @see com.idega.presentation.ui.AbstractChooserWindow#displaySelection(com.idega.presentation.IWContext)
	 */
	public void displaySelection(IWContext iwc) {
		String uId = iwc.getParameter(PARAMETER_USER_ID);
		if (uId != null) {
			try {
				User user = getUserHome().findByPrimaryKey(new Integer(uId));
				Page page = getParentPage();
				page.setOnLoad(SELECT_FUNCTION_NAME + "('" + user.getName() + "','" + uId + "')");
			}
			catch (RemoteException e) {
			}
			catch (FinderException e) {
			}
		}
		else {

			init(iwc);

			addTitle(iwrb.getLocalizedString("select_a_user", "Select a user"), IWConstants.BUILDER_FONT_STYLE_TITLE);

			Form form = new Form();
			form.maintainParameter(SCRIPT_PREFIX_PARAMETER);
			form.maintainParameter(SCRIPT_SUFFIX_PARAMETER);
			form.maintainParameter(DISPLAYSTRING_PARAMETER_NAME);
			form.maintainParameter(VALUE_PARAMETER_NAME);

			Table mainTable = new Table(1, 4);
			mainTable.setStyleClass(mainTableStyle);
			mainTable.setWidth(Table.HUNDRED_PERCENT);
			mainTable.setBorder(0);

			mainTable.add(getHeaderTable(iwc), 1, 1);
			mainTable.add(getNavigationTable(iwc), 1, 3);
			try {
				mainTable.add(getListTable(iwc), 1, 4);
			}
			catch (RemoteException r) {
				throw new RuntimeException(r.getMessage());
			}
			form.add(mainTable);
			add(form, iwc);
		}

	}

	public Table getListTable(IWContext iwc) throws RemoteException {
		Table table = new Table(2, USERS_PER_PAGE + 1);
		table.setCellspacing(0);
		table.setCellpadding(2);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHorizontalZebraColored(IWConstants.DEFAULT_INTERFACE_COLOR, IWConstants.DEFAULT_LIGHT_INTERFACE_COLOR);
		int row = 1;
		int rowHeight = 12;
		table.setHeight(table.getRows() * rowHeight);

		table.add(getTitleText(localize("user", "User")), 1, row);
		table.add(getTitleText(localize("personal_id", "Personal ID")), 2, row);
		table.setRowColor(row, IWConstants.DEFAULT_DARK_INTERFACE_COLOR);
		table.setHeight(row, rowHeight);

		if (users != null) {
			/** Calculating page....starts */
			int start = currentPage * USERS_PER_PAGE;
			Iterator iter = users.iterator();
			for (int i = 0; i < start; i++) {
				if (iter.hasNext()) {
					iter.next();
				}
			}
			/** Calculating page....ends */

			int counter = 0;
			User user;
			Link link;
			String pId;
			while (iter.hasNext() && counter < USERS_PER_PAGE) {
				++counter;
				++row;
				table.setHeight(row, rowHeight);

				user = (User) iter.next();
				pId = user.getPersonalID();
				if (pId == null) {
					pId = "-";
				}
				link = getLink(getText(user.getName()), iwc);
				link.addParameter(PARAMETER_USER_ID, user.getPrimaryKey().toString());
				table.add(link, 1, row);
				table.add(getText(pId), 2, row);
			}
		}

		return table;
	}

	public Table getNavigationTable(IWContext iwc) {

		int usersSize = 0;
		if (users != null) {
			usersSize = users.size();
		}

		int maxPage = (int) Math.ceil(usersSize / USERS_PER_PAGE);

		Table navigationTable = new Table(3, 1);
		navigationTable.setCellpadding(2);
		navigationTable.setCellspacing(0);
		navigationTable.setWidth(Table.HUNDRED_PERCENT);
		navigationTable.setBorder(0);
		navigationTable.setWidth(1, "33%");
		navigationTable.setWidth(2, "33%");
		navigationTable.setWidth(3, "33%");
		navigationTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_CENTER);
		navigationTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		Text prev = getSmallText(localize("previous", "Previous"));
		Text next = getSmallText(localize("next", "Next"));
		Text info = getSmallText(localize("page", "Page") + " " + (currentPage + 1) + " " + localize("of", "of") + " " + (maxPage + 1));
		if (currentPage > 0) {
			Link lPrev = getLink(getSmallText(localize("previous", "Previous")), iwc);
			lPrev.addParameter(PARAMETER_CURRENT_PAGE, Integer.toString(currentPage - 1));
			lPrev.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			if (showAll) {
				lPrev.addParameter(PARAMETER_VIEW_ALL, "true");
			}
			navigationTable.add(lPrev, 1, 1);
		}
		else {
			navigationTable.add(prev, 1, 1);
		}
		navigationTable.add(info, 2, 1);

		if (currentPage < maxPage) {
			Link lNext = getLink(getSmallText(localize("next", "Next")), iwc);
			lNext.addParameter(PARAMETER_CURRENT_PAGE, Integer.toString(currentPage + 1));
			lNext.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			if (showAll) {
				lNext.addParameter(PARAMETER_VIEW_ALL, "true");
			}
			navigationTable.add(lNext, 3, 1);
		}
		else {
			navigationTable.add(next, 3, 1);
		}
		return navigationTable;
	}

	public Table getHeaderTable(IWContext iwc) {
		Table headerTable = new Table();
		headerTable.setCellpaddingAndCellspacing(0);
		int column = 1;

		TextInput tiSearchString = new TextInput(PARAMETER_SEARCH);
		SubmitButton sSearch = new SubmitButton(iwrb.getLocalizedImageButton("search", "Search"));

		headerTable.add(getText(iwrb.getLocalizedString("search", "Search") + ":"), column++, 1);
		headerTable.setCellpaddingLeft(column, 1, 6);
		headerTable.add(tiSearchString, column++, 1);
		headerTable.setCellpaddingLeft(column, 1, 6);
		headerTable.add(sSearch, column++, 1);

		return headerTable;
	}

	private Text getText(String content) {
		Text text = new Text(content);
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		return text;
	}

	private Text getTitleText(String content) {
		Text text = new Text(content);
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_TITLE);
		return text;
	}

	private Text getSmallText(String content) {
		Text text = new Text(content);
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_SMALL);
		return text;
	}

	private Link getLink(Text text, IWContext iwc) {
		Link link = new Link(text);
		link.maintainParameter(SCRIPT_PREFIX_PARAMETER, iwc);
		link.maintainParameter(SCRIPT_SUFFIX_PARAMETER, iwc);
		link.maintainParameter(DISPLAYSTRING_PARAMETER_NAME, iwc);
		link.maintainParameter(VALUE_PARAMETER_NAME, iwc);
		return link;
	}

	private String localize(String key, String nullValue) {
		return iwrb.getLocalizedString(key, nullValue);
	}

	private UserHome getUserHome() throws RemoteException {
		return (UserHome) IDOLookup.getHome(User.class);
	}

}