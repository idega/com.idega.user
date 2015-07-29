/**
 * Title: A window class that displays the results of a search Copyright: Idega Software Copyright (c) 2003 Company: Idega Software
 *
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 *
 *
 */
package com.idega.user.block.search.presentation;

import java.rmi.RemoteException;
import java.util.Collection;

import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Email;
import com.idega.core.messaging.MessagingSettings;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.block.search.business.SearchEngine;
import com.idega.user.data.User;
import com.idega.user.presentation.BasicUserOverview;
import com.idega.user.presentation.BasicUserOverviewEmailSenderWindow;
import com.idega.user.presentation.StyledBasicUserOverViewToolbar;

public class SearchResultsWindow extends BasicUserOverview {

  public SearchResultsWindow() {}

	@Override
	protected Collection<User> getEntries(IWContext iwc) {
		SearchResultsWindowPS sPs = (SearchResultsWindowPS)this.ps;
		 try {
		 	SearchEngine engine = getSearchEngine(iwc);
			return engine.getResult(sPs.getLastUserSearchEvent() );
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String getEntityBrowserIdentifier() {
		String identifier = "search-";
		SearchResultsWindowPS sPs = (SearchResultsWindowPS) this.ps;
		if (sPs != null) {
			identifier += sPs.getSearchString();
		}

		return identifier;
	}

	@Override
	protected PresentationObject getEmptyListPresentationObject() {
		Text text = new Text(this.iwrb.getLocalizedString("searchresultswindow.search_had_no_match", "The search did not return any results"));
		return text;
	}

	public static SearchEngine getSearchEngine(IWApplicationContext iwc) {
		SearchEngine business = null;
		try {
			business = IBOLookup.getServiceInstance(iwc, SearchEngine.class);
		} catch (RemoteException rme) {
			rme.printStackTrace();
		}

		return business;
	}

	@Override
	public Class<SearchResultsWindowPS> getPresentationStateClass() {
		return SearchResultsWindowPS.class;
	}

	/* (non-Javadoc)
	 * @see com.idega.user.presentation.BasicUserOverview#getToolbar()
	 */
	@Override
	protected StyledBasicUserOverViewToolbar getToolbar() {
		StyledBasicUserOverViewToolbar toolbar = super.getToolbar();
		SearchResultsWindowPS sPs = (SearchResultsWindowPS) this.ps;
		if (sPs != null && this.iwrb != null) {
			String search = sPs.getSearchString();
			if (search != null) {
				toolbar.setTitle(this.iwrb.getLocalizedString("searchresultswindow.search:", "Search : ") + search + Text.getNonBrakingSpace(2));
			}
			else {
				toolbar.setTitle(this.iwrb.getLocalizedString("searchresultswindow.advanced_search:", "Advanced search") + Text.getNonBrakingSpace(2));
			}
		}

		return toolbar;
	}

	protected void addEmailButton(EntityBrowser entityBrowser, IWContext iwc) {
		//add emailing option
		//if (this.hasEditPermissionForRealGroup) {// || iwc.getAccessController().hasRole("email_search", iwc)) {
			SubmitButton emailButton = new SubmitButton(this.iwrb.getLocalizedString("Email selection", "Email selection"), BasicUserOverview.EMAIL_USERS_KEY, BasicUserOverview.EMAIL_USERS_KEY);
			StyledButton styledEmailButton = new StyledButton(emailButton);
			entityBrowser.addPresentationObjectToBottom(styledEmailButton);
			User currentUser = iwc.getCurrentUser();
			String fromAddress = null;
			Collection<Email> emails = currentUser.getEmails();
			if (emails != null && !emails.isEmpty()) {
				Email email = emails.iterator().next();
				if (email != null && email.getEmailAddress() != "") {
					fromAddress = email.getEmailAddress();
				}
			}
			if (fromAddress == null) {
				fromAddress = "no_from_address_set";
			}
			iwc.setSessionAttribute(BasicUserOverviewEmailSenderWindow.PARAM_MAIL_SERVER, iwc.getApplicationSettings().getProperty(MessagingSettings.PROP_SYSTEM_SMTP_MAILSERVER));
			iwc.setSessionAttribute(BasicUserOverviewEmailSenderWindow.PARAM_FROM_ADDRESS, fromAddress);
			iwc.setSessionAttribute(BasicUserOverviewEmailSenderWindow.PARAM_SUBJECT, this.iwrb.getLocalizedString("to_search_result", "To search result"));
		//}
	}

}