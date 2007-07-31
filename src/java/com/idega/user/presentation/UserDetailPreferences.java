package com.idega.user.presentation;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.file.data.ICFile;
import com.idega.core.localisation.presentation.LocalePresentationUtil;
import com.idega.core.location.business.AddressBusiness;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.AddressHome;
import com.idega.core.location.data.AddressType;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.UploadFile;
import com.idega.presentation.Block;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CountryDropdownMenu;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.EmailValidator;
import com.idega.util.FileUtil;

public class UserDetailPreferences extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_FORM_SUBMIT = 2;

	private final static String PARAMETER_FORM_SUBMIT = "cap_sbmt";
	private final static String PARAMETER_EMAIL = "cap_email";
	private final static String PARAMETER_PHONE_HOME = "cap_phn_h";
	private final static String PARAMETER_PHONE_WORK = "cap_phn_w";
	private final static String PARAMETER_PHONE_MOBILE = "cap_phn_m";
	private final static String PARAMETER_CO_STREET_ADDRESS = "cap_co_sa";
	private final static String PARAMETER_CO_POSTAL_CODE = "cap_co_pc";
	private final static String PARAMETER_CO_CITY = "cap_co_ct";
	private final static String PARAMETER_CO_COUNTRY = "cap_co_country";
	private final static String PARAMETER_REMOVE_IMAGE = "cap_remove_image";
	private final static String PARAMETER_PREFERRED_LOCALE = "cap_pref_locale";

	private final static String KEY_PREFIX = "userDetailPreferences.";
	private final static String KEY_EMAIL = KEY_PREFIX + "email";
	private final static String KEY_UPDATE = KEY_PREFIX + "update";
	private final static String KEY_PHONE_HOME = KEY_PREFIX + "phone_home";
	private final static String KEY_PHONE_MOBILE = KEY_PREFIX + "phone_mobile";
	private final static String KEY_PHONE_WORK = KEY_PREFIX + "phone_work";
	private final static String KEY_CO_STREET_ADDRESS = KEY_PREFIX + "co_street_address";
	private final static String KEY_CO_POSTAL_CODE = KEY_PREFIX + "co_postal_code";
	private final static String KEY_CO_CITY = KEY_PREFIX + "co_city";
	private final static String KEY_CO_COUNTRY = KEY_PREFIX + "co_country";
	private final static String KEY_EMAIL_INVALID = KEY_PREFIX + "email_invalid";
	private final static String KEY_CO_STREET_ADDRESS_MISSING = KEY_PREFIX + "co_street_address_missing";
	private final static String KEY_CO_POSTAL_CODE_MISSING = KEY_PREFIX + "co_postal_code_missing";
	private final static String KEY_CO_CITY_MISSING = KEY_PREFIX + "co_city_missing";
	private final static String KEY_PREFERENCES_SAVED = KEY_PREFIX + "preferenced_saved";
	private final static String KEY_PREFERENCES_SAVED_TEXT = KEY_PREFIX + "preferenced_saved_text";
	private final static String PREFERRED_LANGUAGE = "preferred_language";

	private final static String DEFAULT_EMAIL = "E-mail";
	private final static String DEFAULT_UPDATE = "Update";
	private final static String DEFAULT_PHONE_HOME = "Phone (home)";
	private final static String DEFAULT_PHONE_MOBILE = "Phone (mobile)";
	private final static String DEFAULT_PHONE_WORK = "Phone (work)";
	private final static String DEFAULT_CO_STREET_ADDRESS = "Street address";
	private final static String DEFAULT_CO_POSTAL_CODE = "Postal code";
	private final static String DEFAULT_CO_CITY = "City";
	private final static String DEFAULT_CO_COUNTRY = "Country";
	private final static String DEFAULT_EMAIL_INVALID = "Email address invalid.";
	private final static String DEFAULT_CO_STREET_ADDRESS_MISSING = "Street address must be entered.";
	private final static String DEFAULT_CO_POSTAL_CODE_MISSING = "Postal code must be entered.";
	private final static String DEFAULT_CO_CITY_MISSING = "City must be entered.";
	private final static String DEFAULT_PREFERENCES_SAVED = "Preferences saved";
	private final static String DEFAULT_PREFERENCES_SAVED_TEXT = "Your preferences have been saved.";

	private User user = null;

	private IWResourceBundle iwrb;

	private boolean showPreferredLocaleChooser = false;

	public void main(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
//			return;
		}
		this.iwrb = getResourceBundle(iwc);
		this.user = iwc.getCurrentUser();
		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_VIEW_FORM:
					viewForm(iwc);
					break;
				case ACTION_FORM_SUBMIT:
					updatePreferences(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}

	private int parseAction(final IWContext iwc) {
		int action = ACTION_VIEW_FORM;
		if (iwc.isParameterSet(PARAMETER_FORM_SUBMIT)) {
			action = ACTION_FORM_SUBMIT;
		}
		return action;
	}

	private void viewForm(IWContext iwc) throws java.rmi.RemoteException {
		Form form = new Form();
		form.setMultiPart();
		form.addParameter(PARAMETER_FORM_SUBMIT, Boolean.TRUE.toString());
		form.setID("userDetailPreferences");
		form.setStyleClass("userDetailForm");

		Layer header = new Layer(Layer.DIV);
		header.setStyleClass("header");
		form.add(header);

		Heading1 heading = new Heading1(this.iwrb.getLocalizedString(KEY_PREFIX + "user_detail_preferences", "User detail preferences"));
		header.add(heading);

		Layer contents = new Layer(Layer.DIV);
		contents.setStyleClass("formContents");
		form.add(contents);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		contents.add(section);

		UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);

		Image image = null;
		if (this.user.getSystemImageID() > 0) {
			try {
				image = new Image(this.user.getSystemImageID());
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Email mail = ub.getUserMail(this.user);
		Phone homePhone = null;
		try {
			homePhone = ub.getUsersHomePhone(this.user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Phone mobilePhone = null;
		try {
			mobilePhone = ub.getUsersMobilePhone(this.user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Phone workPhone = null;
		try {
			workPhone = ub.getUsersWorkPhone(this.user);
		}
		catch (NoPhoneFoundException e) {
			e.printStackTrace();
		}
		Address coAddress = getCOAddress(iwc);
		PostalCode postal = null;
		if (coAddress != null) {
			postal = coAddress.getPostalCode();
		}

		FileInput file = new FileInput();

		TextInput tiEmail = new TextInput(PARAMETER_EMAIL);
		if (mail != null && mail.getEmailAddress() != null) {
			tiEmail.setContent(mail.getEmailAddress());
		}

		TextInput tiPhoneHome = new TextInput(PARAMETER_PHONE_HOME);
		if (homePhone != null && homePhone.getNumber() != null) {
			tiPhoneHome.setContent(homePhone.getNumber());
		}

		TextInput tiPhoneMobile = new TextInput(PARAMETER_PHONE_MOBILE);
		if (mobilePhone != null && mobilePhone.getNumber() != null) {
			tiPhoneMobile.setContent(mobilePhone.getNumber());
		}

		TextInput tiPhoneWork = new TextInput(PARAMETER_PHONE_WORK);
		if (workPhone != null && workPhone.getNumber() != null) {
			tiPhoneWork.setContent(workPhone.getNumber());
		}

		TextInput tiCOStreetAddress = new TextInput(PARAMETER_CO_STREET_ADDRESS);
		if (coAddress != null && coAddress.getStreetAddress() != null) {
			tiCOStreetAddress.setContent(coAddress.getStreetAddress());
		}

		TextInput tiCOPostalCode = new TextInput(PARAMETER_CO_POSTAL_CODE);
		if (postal != null && postal.getPostalCode() != null) {
			tiCOPostalCode.setValue(postal.getPostalCode());
		}

		TextInput tiCOCity = new TextInput(PARAMETER_CO_CITY);
		if (coAddress != null && coAddress.getCity() != null) {
			tiCOCity.setValue(coAddress.getCity());
		}

		CountryDropdownMenu tiCOCountry = new CountryDropdownMenu(PARAMETER_CO_COUNTRY);
		if (postal != null && postal.getCountryID() > -1) {
			tiCOCountry.setSelectedCountry(postal.getCountry());
		}

		CheckBox removeImage = new CheckBox(PARAMETER_REMOVE_IMAGE, "true");
		removeImage.setStyleClass("checkbox");
		removeImage.keepStatusOnAction(true);

		Layer formItem;
		Label label;

		Layer layer = new Layer(Layer.DIV);
		layer.setID("userDetailImage");
		section.add(layer);

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(KEY_PREFIX + "image_help", "To remove the displayed image check the checkbox and save.")));
		layer.add(helpLayer);

		if (image != null) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.add(new Text(this.iwrb.getLocalizedString(KEY_PREFIX + "image", "Image")));
			formItem.add(label);
			formItem.add(image);
			layer.add(formItem);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("indentedCheckbox");
			formItem.setID("removeImage");
			label = new Label(this.iwrb.getLocalizedString(KEY_PREFIX + "remove_image", "Remove image"), removeImage);
			formItem.add(removeImage);
			formItem.add(label);
			layer.add(formItem);
		}

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		layer.add(clearLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("imageUpload");
		label = new Label(this.iwrb.getLocalizedString(KEY_PREFIX + "image_upload", "Image upload"), file);
		formItem.add(label);
		formItem.add(file);
		layer.add(formItem);

		section.add(clearLayer);

		layer = new Layer(Layer.DIV);
		layer.setID("userDetailEmail");
		section.add(layer);

		helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(KEY_PREFIX + "email_help", "Here you can change your current email address or choose not to be notified by email. You will still receive messages under your account even if you do.")));
		layer.add(helpLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_EMAIL, DEFAULT_EMAIL), tiEmail);
		formItem.add(label);
		formItem.add(tiEmail);
		layer.add(formItem);

		section.add(clearLayer);

		layer = new Layer(Layer.DIV);
		layer.setID("userDetailPhones");
		section.add(layer);

		helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(KEY_PREFIX + "phones_help", "Here you can modify your phone information or delete the numbers by leaving the fields empty.")));
		layer.add(helpLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_PHONE_HOME, DEFAULT_PHONE_HOME), tiPhoneHome);
		formItem.add(label);
		formItem.add(tiPhoneHome);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_PHONE_WORK, DEFAULT_PHONE_WORK), tiPhoneWork);
		formItem.add(label);
		formItem.add(tiPhoneWork);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setID("mobilePhone");
		label = new Label(this.iwrb.getLocalizedString(KEY_PHONE_MOBILE, DEFAULT_PHONE_MOBILE), tiPhoneMobile);
		formItem.add(label);
		formItem.add(tiPhoneMobile);
		layer.add(formItem);

		section.add(clearLayer);

		layer = new Layer(Layer.DIV);
		layer.setID("userDetailResidence");
		section.add(layer);

		helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(this.iwrb.getLocalizedString(KEY_PREFIX + "residence_help", "If you would like to receive letters via your C/O address, rather than your registered one, you can check the checkbox below and fill in your C/O address.")));
		layer.add(helpLayer);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_CO_STREET_ADDRESS, DEFAULT_CO_STREET_ADDRESS), tiCOStreetAddress);
		formItem.add(label);
		formItem.add(tiCOStreetAddress);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_CO_POSTAL_CODE, DEFAULT_CO_POSTAL_CODE), tiCOPostalCode);
		formItem.add(label);
		formItem.add(tiCOPostalCode);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_CO_CITY, DEFAULT_CO_CITY), tiCOCity);
		formItem.add(label);
		formItem.add(tiCOCity);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(this.iwrb.getLocalizedString(KEY_CO_COUNTRY, DEFAULT_CO_COUNTRY), tiCOCountry);
		formItem.add(label);
		formItem.add(tiCOCountry);
		layer.add(formItem);

		DropdownMenu localesDrop = LocalePresentationUtil.getAvailableLocalesDropdown(iwc.getIWMainApplication(), PARAMETER_PREFERRED_LOCALE);
		if (localesDrop.getChildCount() > 1 && isSetToShowPreferredLocaleChooser()) {
			section.add(clearLayer);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setID("preferredLang");

			if (user.getPreferredLocale() != null) {
				localesDrop.setSelectedElement(user.getPreferredLocale());
			}
			else {
				localesDrop.setSelectedElement(iwc.getCurrentLocale().toString());
			}

			label = new Label(this.iwrb.getLocalizedString(PREFERRED_LANGUAGE, "Preferred language"), localesDrop);
			formItem.add(label);
			formItem.add(localesDrop);
			layer.add(formItem);
		}

		section.add(clearLayer);

		Layer buttonLayer = new Layer(Layer.DIV);
		buttonLayer.setStyleClass("buttonLayer");
		contents.add(buttonLayer);

		Layer span = new Layer(Layer.SPAN);
		span.add(new Text(this.iwrb.getLocalizedString(KEY_UPDATE, DEFAULT_UPDATE)));
		Link send = new Link(span);
		send.setToFormSubmit(form);
		buttonLayer.add(send);

		add(form);
	}

	private void updatePreferences(IWContext iwc) throws Exception {
		boolean hasErrors = false;
		Collection errors = new ArrayList();

		int fileID = -1;
		UploadFile uploadFile = iwc.getUploadedFile();
		if (uploadFile != null && uploadFile.getName() != null && uploadFile.getName().length() > 0) {
			try {
				FileInputStream input = new FileInputStream(uploadFile.getRealPath());

				ICFile file = ((com.idega.core.file.data.ICFileHome) com.idega.data.IDOLookup.getHome(ICFile.class)).create();
				file.setName(uploadFile.getName());
				file.setMimeType(uploadFile.getMimeType());
				file.setFileValue(input);
				file.setFileSize((int) uploadFile.getSize());
				file.store();

				fileID = ((Integer) file.getPrimaryKey()).intValue();
				uploadFile.setId(fileID);
				try {
					FileUtil.delete(uploadFile);
				}
				catch (Exception ex) {
					System.err.println("MediaBusiness: deleting the temporary file at " + uploadFile.getRealPath() + " failed.");
				}
			}
			catch (RemoteException e) {
				e.printStackTrace(System.err);
				uploadFile.setId(-1);
			}
		}

		String sEmail = iwc.isParameterSet(PARAMETER_EMAIL) ? iwc.getParameter(PARAMETER_EMAIL) : null;
		String phoneHome = iwc.getParameter(PARAMETER_PHONE_HOME);
		String phoneMobile = iwc.getParameter(PARAMETER_PHONE_MOBILE);
		String phoneWork = iwc.getParameter(PARAMETER_PHONE_WORK);
		String coStreetAddress = iwc.getParameter(PARAMETER_CO_STREET_ADDRESS);
		String coPostalCode = iwc.getParameter(PARAMETER_CO_POSTAL_CODE);
		String coCity = iwc.getParameter(PARAMETER_CO_CITY);
		String coCountry = iwc.getParameter(PARAMETER_CO_COUNTRY);
		String preferredLocale = iwc.getParameter(PARAMETER_PREFERRED_LOCALE);
		boolean removeImage = iwc.isParameterSet(PARAMETER_REMOVE_IMAGE);

		boolean updateEmail = false;

		if (sEmail != null) {
			updateEmail = EmailValidator.getInstance().validateEmail(sEmail);
			if (!updateEmail) {
				errors.add(this.iwrb.getLocalizedString(KEY_EMAIL_INVALID, DEFAULT_EMAIL_INVALID));
				hasErrors = true;
			}
		}

		if (coStreetAddress.equals("")) {
			errors.add(this.iwrb.getLocalizedString(KEY_CO_STREET_ADDRESS_MISSING, DEFAULT_CO_STREET_ADDRESS_MISSING));
			hasErrors = true;
		}
		if (coPostalCode.equals("")) {
			errors.add(this.iwrb.getLocalizedString(KEY_CO_POSTAL_CODE_MISSING, DEFAULT_CO_POSTAL_CODE_MISSING));
			hasErrors = true;
		}
		if (coCity.equals("")) {
			errors.add(this.iwrb.getLocalizedString(KEY_CO_CITY_MISSING, DEFAULT_CO_CITY_MISSING));
			hasErrors = true;
		}

		if (!hasErrors) {
			UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			if (updateEmail) {
				ub.storeUserEmail(this.user, sEmail, true);
			}
			ub.updateUserHomePhone(this.user, phoneHome);
			ub.updateUserWorkPhone(this.user, phoneWork);
			ub.updateUserMobilePhone(this.user, phoneMobile);

			if (preferredLocale != null) {
				ub.setUsersPreferredLocale(user, preferredLocale, true);
			}

			Address coAddress = getCOAddress(iwc);
			coAddress.setStreetName(coStreetAddress);

			AddressBusiness addressBusiness = (AddressBusiness) IBOLookup.getServiceInstance(iwc, AddressBusiness.class);
			Country country = addressBusiness.getCountryHome().findByPrimaryKey(new Integer(coCountry));
			PostalCode pc = addressBusiness.getPostalCodeAndCreateIfDoesNotExist(coPostalCode, coCity, country);

			coAddress.setPostalCode(pc);
			coAddress.setCity(coCity);
			coAddress.store();


			if (removeImage) {
				this.user.setSystemImageID(null);
				this.user.store();
			}
			if (fileID != -1) {
				this.user.setSystemImageID(fileID);
				this.user.store();
			}

			Layer header = new Layer(Layer.DIV);
			header.setStyleClass("header");
			add(header);

			Heading1 heading = new Heading1(this.iwrb.getLocalizedString(KEY_PREFIX + "user_detail_preferences", "User detail preferences"));
			header.add(heading);

			Layer layer = new Layer(Layer.DIV);
			layer.setStyleClass("receipt");

			Layer image = new Layer(Layer.DIV);
			image.setStyleClass("receiptImage");
			layer.add(image);

			heading = new Heading1(this.iwrb.getLocalizedString(KEY_PREFERENCES_SAVED, DEFAULT_PREFERENCES_SAVED));
			layer.add(heading);

			Paragraph paragraph = new Paragraph();
			paragraph.add(new Text(this.iwrb.getLocalizedString(KEY_PREFERENCES_SAVED_TEXT, DEFAULT_PREFERENCES_SAVED_TEXT)));
			layer.add(paragraph);

			try {
				ICPage page = ub.getHomePageForUser(this.user);
				paragraph.add(new Break(2));

				Layer span = new Layer(Layer.SPAN);
				span.add(new Text(this.iwrb.getLocalizedString("my_page", "My page")));
				Link link = new Link(span);
				link.setStyleClass("homeLink");
				link.setPage(page);
				paragraph.add(link);
			}
			catch (FinderException fe) {
				// No homepage found...
			}

			add(layer);
		}
		else {
			showErrors(iwc, errors);
			viewForm(iwc);
		}
	}

	private Address getCOAddress(IWContext iwc) {
		Address coAddress = null;
		try {
			UserBusiness ub = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			AddressHome ah = ub.getAddressHome();
			AddressType coType = ah.getAddressType2();

			Address address = ub.getUserAddressByAddressType(iwc.getCurrentUserId(), coType);
			if (address != null) {
				return address;
			}
			coAddress = ah.create();
			coAddress.setAddressType(coType);
			coAddress.store();
			this.user.addAddress(coAddress);
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
		return coAddress;
	}

	/**
	 * @return Returns the showPreferredLocaleChooser.
	 */
	public boolean isSetToShowPreferredLocaleChooser() {
		return showPreferredLocaleChooser;
	}

	/**
	 * @param showPreferredLocaleChooser
	 *          The showPreferredLocaleChooser to set.
	 */
	public void setToShowPreferredLocaleChooser(boolean showPreferredLocaleChooser) {
		this.showPreferredLocaleChooser = showPreferredLocaleChooser;
	}

	/**
	 * Adds the errors encountered
	 * @param iwc
	 * @param errors
	 */
	protected void showErrors(IWContext iwc, Collection errors) {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("errorLayer");
		
		Layer image = new Layer(Layer.DIV);
		image.setStyleClass("errorImage");
		layer.add(image);
		
		Heading1 heading = new Heading1(getResourceBundle(iwc).getLocalizedString("application_errors_occured", "There was a problem with the following items"));
		layer.add(heading);
		
		Lists list = new Lists();
		layer.add(list);
		
		Iterator iter = errors.iterator();
		while (iter.hasNext()) {
			String element = (String) iter.next();
			ListItem item = new ListItem();
			item.add(new Text(element));
			
			list.add(item);
		}
		
		add(layer);
	}
}
