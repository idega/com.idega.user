/*
 * $Id: BasicUserOverviewEmailSenderWindow.java,v 1.1.2.3 2007/01/12 19:31:51 idegaweb Exp $
 * Created on Nov 28, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.user.presentation;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.MailToLink;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;

public class BasicUserOverviewEmailSenderWindow extends StyledIWAdminWindow {
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user"; 
	private static final String WINDOW_NAME = "Send emails";
	private static final String HELP_TEXT_KEY = "send_email_from_basic_user_overview";
	private static final String linkStyleUnderline = "styledLinkUnderline";
	
	protected static final String PARAM_MAIL_SERVER = "mail_server";
	protected static final String PARAM_FROM_ADDRESS = "from_address";
	protected static final String PARAM_TO_ADDRESS = "to_address";
	protected static final String PARAM_SUBJECT = "subject";
	protected static final String PARAM_BODY = "body";
	private static final String PARAM_SEND = "send_mail";
	
	
	//texts
	private Text fromAddressText;
	private Text toAddressText;
	private Text subjectText;
	private Text bodyText;
	private Text sendingResultsText;
	private Text sendingResultsMessageText;
	
	//fields
	private MailToLink mailToLink;
	private HiddenInput mailServerField;
	private TextInput fromAddressField;
	private TextInput toAddressField;
	private TextInput subjectField;
	private TextArea bodyField;
	
	//buttons
	private SubmitButton sendButton;
	private StyledButton styledSendButton;
	private CloseButton closeButton;
	private StyledButton styledCloseButton;
	
	private Table mainTable;
	private Form form;
	
	public BasicUserOverviewEmailSenderWindow() {
		setHeight(520);
		setWidth(500);
		setScrollbar(false);
	}
	
	public void main(IWContext iwc){
		IWResourceBundle iwrb = getResourceBundle(iwc);
		setTitle(iwrb.getLocalizedString(WINDOW_NAME, WINDOW_NAME));
		addTitle(iwrb.getLocalizedString(WINDOW_NAME, WINDOW_NAME), TITLE_STYLECLASS);
		this.form = new Form();
		initializeTexts(iwc);
		initializeFields(iwc);
		initializeContent(iwc);
		String send = iwc.getParameter(PARAM_SEND);
		if(send != null && !send.equals("")) {
			sendEmail(iwc);
		}
		lineUpFields(iwc);		
		add(this.form,iwc);
	}

	private void sendEmail(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		String mailServer = iwc.getParameter(PARAM_MAIL_SERVER);
		String fromAddress = iwc.getParameter(PARAM_FROM_ADDRESS);
		String toAddress = iwc.getParameter(PARAM_TO_ADDRESS);
		String subject = iwc.getParameter(PARAM_SUBJECT);
		String body = iwc.getParameter(PARAM_BODY);
		
		try {
			if (toAddress == null || toAddress.equals("")) {
				this.sendingResultsText = new Text(iwrb.getLocalizedString("error_sending_mail","Error sending mail, error message was:"));
				this.sendingResultsMessageText = new Text(iwrb.getLocalizedString("no_emails_defined_for_selected_recipients","No emails defined for selected recipients"));
				this.sendingResultsMessageText.setBold(false);
			}
			else {
				com.idega.util.SendMail.send(fromAddress,toAddress,"","",mailServer,subject,body);
				this.sendingResultsText = new Text(iwrb.getLocalizedString("successful_sending_mail","Sending mail was successful"));
			}
		} catch (Exception e) {
			this.sendingResultsText = new Text(iwrb.getLocalizedString("error_sending_mail","Error sending mail, error message was:"));
			this.sendingResultsMessageText = new Text(e.getMessage());
			this.sendingResultsMessageText.setBold(false);
			System.out.println("BasicUserOverviewEmailSenderWindow: Error sending mail: " +e.getClass()+": "+e.getMessage());
		}
	}
	
	protected void initializeTexts(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.fromAddressText = new Text(iwrb.getLocalizedString(PARAM_FROM_ADDRESS,"From Address"));
		this.toAddressText = new Text(iwrb.getLocalizedString(PARAM_TO_ADDRESS,"To Address"));
		this.subjectText = new Text(iwrb.getLocalizedString(PARAM_SUBJECT,"Subject"));
		this.bodyText = new Text(iwrb.getLocalizedString(PARAM_BODY,"Body"));
	}
	
	protected void initializeFields(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.mailServerField = new HiddenInput(PARAM_MAIL_SERVER);
		
		this.mailToLink = new MailToLink(iwrb.getLocalizedString("open_in_default_mail_program", "Open in default mail program"));
		this.mailToLink.setStyleClass(linkStyleUnderline);
		
		this.fromAddressField = new TextInput(PARAM_FROM_ADDRESS);
		this.fromAddressField.setSize(60);
		this.fromAddressField.setReadOnly(true);
		this.fromAddressField.setStyleAttribute("color: gray");
		
		this.toAddressField = new TextInput(PARAM_TO_ADDRESS);
		this.toAddressField.setSize(60);
		this.toAddressField.setReadOnly(true);
		this.toAddressField.setStyleAttribute("color: gray");
		
		this.subjectField = new TextInput(PARAM_SUBJECT);
		this.subjectField.setSize(60);
		
		this.bodyField = new TextArea(PARAM_BODY, 60, 20);
		
		this.sendButton = new SubmitButton(iwrb.getLocalizedString("send","Send"), PARAM_SEND, PARAM_SEND);
		this.styledSendButton = new StyledButton(this.sendButton);

		this.closeButton = new CloseButton(iwrb.getLocalizedString("close","Close"));
		this.styledCloseButton = new StyledButton(this.closeButton);
	}
	
	protected void initializeContent(IWContext iwc) {
		this.mailServerField.setContent((String)iwc.getSessionAttribute(PARAM_MAIL_SERVER));
		this.fromAddressField.setContent((String)iwc.getSessionAttribute(PARAM_FROM_ADDRESS));
		this.toAddressField.setContent((String)iwc.getSessionAttribute(PARAM_TO_ADDRESS));
		this.subjectField.setContent((String)iwc.getSessionAttribute(PARAM_SUBJECT));
		this.mailToLink.setRecipients((String)iwc.getSessionAttribute(PARAM_TO_ADDRESS));
		this.mailToLink.setSubject((String)iwc.getSessionAttribute(PARAM_SUBJECT));
	}
	
	public void lineUpFields(IWContext iwc) {	
		this.mainTable = new Table(1,3);
		this.mainTable.setCellspacing(0);
		this.mainTable.setCellpadding(0);
		this.mainTable.setWidth(Table.HUNDRED_PERCENT);
		this.mainTable.setHeight(2, 5);
	    
	    Table inputTable = new Table();
		inputTable.setWidth(Table.HUNDRED_PERCENT);
		inputTable.setCellspacing(5);
		inputTable.setCellpadding(0);
		inputTable.setStyleClass(MAIN_STYLECLASS);
		if (iwc.getParameter(PARAM_SEND) == null) {
			inputTable.add(this.mailServerField,2,1);
			inputTable.add(this.fromAddressText + ":",1,2);
			inputTable.add(this.fromAddressField,2,2);
			inputTable.add(this.toAddressText + ":",1,3);
			inputTable.add(this.toAddressField,2,3);
			inputTable.add(this.subjectText + ":",1,4);
			inputTable.add(this.subjectField,2,4);
			inputTable.add(this.bodyText + ":",1,5);
			inputTable.add(this.bodyField,2,5);
			inputTable.add(this.mailToLink,2,6);
		} else {
			inputTable.add(this.sendingResultsText,1,1);
			if (this.sendingResultsMessageText != null) {
				inputTable.add(this.sendingResultsMessageText,2,1);
			}
		}
		Table buttonTable = new Table();
		buttonTable.setCellspacing(0);
		buttonTable.setCellpadding(0);
		buttonTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
		if (iwc.getParameter(PARAM_SEND) == null) {
			buttonTable.add(this.styledSendButton,1,1);
		}
		buttonTable.setWidth(2, "5");
		buttonTable.add(this.styledCloseButton,3,1);
				
		Table helpTable = new Table();
		helpTable.setCellpadding(0);
		helpTable.setCellspacing(0);
		helpTable.add(getHelp(HELP_TEXT_KEY),1,1);
		
		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setStyleClass(MAIN_STYLECLASS);
		bottomTable.add(helpTable,1,1);
		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);
		bottomTable.add(buttonTable,2,1);
		
		this.mainTable.add(inputTable,1,1);
		this.mainTable.add(bottomTable,1,3);
		
		this.form.add(this.mainTable);
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}
