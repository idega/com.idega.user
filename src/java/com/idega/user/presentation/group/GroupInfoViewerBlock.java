package com.idega.user.presentation.group;

import java.util.List;

import com.idega.bean.AddressData;
import com.idega.bean.GroupDataBean;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserConstants;

public class GroupInfoViewerBlock extends Block {
	
	private boolean showName = true;
	private boolean showHomePage = false;
	private boolean showDescription = false;
	private boolean showExtraInfo = false;
	private boolean showShortName = false;
	private boolean showPhone = false;
	private boolean showFax = false;
	private boolean showEmails = false;
	private boolean showAddress = false;
	private boolean showEmptyFields = true;
	
	private List<GroupDataBean> groupsData = null;
	
	private String id = null;
	private String styleClass = "groupsInfoList";
	
	public void main(IWContext iwc) {
		if (groupsData == null) {
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer container = new Layer();
		if (id != null) {
			container.setId(id);
		}
		if (styleClass != null) {
			container.setStyleClass(styleClass);
		}
		Lists groups = new Lists();
		
		GroupDataBean bean = null;
		ListItem group = null;
		
		for (int i = 0; i < groupsData.size(); i++) {
			bean = groupsData.get(i);
			
			System.out.println("Name: " + bean.getName());
			System.out.println("Short name: " + bean.getShortName());
			System.out.println("Address: " + bean.getAddress());
			System.out.println("Phone: " + bean.getPhoneNumber());
			System.out.println("Fax: " + bean.getFaxNumber());
			System.out.println("Home page: " + bean.getHomePageUrl());
			System.out.println("Emails: " + bean.getEmailAddresses());
			System.out.println("Description: " + bean.getDescription());
			System.out.println("Extra info: " + bean.getExtraInfo());
			
			group = new ListItem();
			
			//	Name
			if (showName) {
				Layer groupName = new Layer();
				groupName.add(new Text(iwrb.getLocalizedString("group_name", "Name: ")));
				groupName.add(new Text(bean.getName()));
				group.add(groupName);
			}
			
			//	Short name
			if (showShortName) {
				String shortName = getEmptyIfValueIsNull(bean.getShortName());
				if (showEmptyFields || shortName.length() > 0) {
					Layer shortNameContainer = new Layer();
					shortNameContainer.add(new Text(iwrb.getLocalizedString("short_name", "Short name: ")));
					shortNameContainer.add(new Text(shortName));
					group.add(shortNameContainer);
				}
			}

			//	Address
			if (showAddress) {
				Layer addressContainer = new Layer();
				Text groupAddress = new Text(iwrb.getLocalizedString("group_address", "Address: "));
				AddressData addressData = bean.getAddress();
				if (addressData == null) {
					if (showEmptyFields) {
						addressContainer.add(groupAddress);
					}
				}
				else {
					String address = new StringBuffer(addressData.getStreetAddress()).append(", ").append(addressData.getPostalCode())
						.append(" ").append(addressData.getCity()).toString();
					addressContainer.add(groupAddress);
					addressContainer.add(new Text(address));
				}
				group.add(addressContainer);
			}
			
			//	Phone
			if (showPhone) {
				String phoneNumber = getEmptyIfValueIsNull(bean.getPhoneNumber());
				if (showEmptyFields || phoneNumber.length() > 0) {
					Layer phoneContainer = new Layer();
					phoneContainer.add(new Text(iwrb.getLocalizedString("group_phone", "Phone: ")));
					phoneContainer.add(new Text(phoneNumber));
					group.add(phoneContainer);
				}
			}
			
			//	Fax
			if (showFax) {
				String faxNumber = getEmptyIfValueIsNull(bean.getFaxNumber());
				if (showEmptyFields || faxNumber.length() > 0) {
					Layer faxContainer = new Layer();
					faxContainer.add(new Text(iwrb.getLocalizedString("group_fax", "Fax: ")));
					faxContainer.add(new Text(faxNumber));
					group.add(faxContainer);
				}
			}
			
			//	Homepage
			if (showHomePage) {
				String homePageUrl = getEmptyIfValueIsNull(bean.getHomePageUrl());
				if (showEmptyFields || homePageUrl.length() > 0) {
					Layer homePageContainer = new Layer();
					homePageContainer.add(new Text(iwrb.getLocalizedString("group_homepage", "Homepage:")));
					homePageContainer.add(new Text("\u00A0"));
					Link link = new Link();
					link.setText(" " + homePageUrl);
					link.setTarget("newWindow");
					if (!homePageUrl.startsWith("http://")) {
						homePageUrl = new StringBuffer("http://").append(homePageUrl).toString();
					}
					link.setURL(homePageUrl);
					homePageContainer.add(link);
					group.add(homePageContainer);
				}
			}
				
			//	Emails
			if (showEmails) {
				Layer emails = getEmails(bean.getEmailAddresses());
				if (showEmptyFields || emails != null) {
					Layer emailContainer = new Layer();
					emailContainer.add(new Text(iwrb.getLocalizedString("group_email", "Email: ")));
					emailContainer.add(emails);
					group.add(emailContainer);
				}
			}
			
			//	Description
			if (showDescription) {
				String description = getEmptyIfValueIsNull(bean.getDescription());
				if (showEmptyFields || description.length() > 0) {
					Layer descriptionContainer = new Layer();
					descriptionContainer.add(new Text(iwrb.getLocalizedString("group_description", "Description: ")));
					descriptionContainer.add(new Text(description));
					group.add(descriptionContainer);
				}
			}
			
			//	Extra info
			if (showExtraInfo) {
				String extraInfo = getEmptyIfValueIsNull(bean.getExtraInfo());
				if (showEmptyFields || extraInfo.length() > 0) {
					Layer extraInfoContainer = new Layer();
					extraInfoContainer.add(new Text(iwrb.getLocalizedString("goup_extra_info", "Info: ")));
					extraInfoContainer.add(new Text(extraInfo));
					group.add(extraInfoContainer);
				}
			}
			
			groups.add(group);
		}
		
		container.add(groups);
		add(container);
	}
	
	private Layer getEmails(List<String> addresses) {
		if (addresses == null) {
			return null;
		}
		
		Layer emails = new Layer();
		
		Link email = null;
		for (int i = 0; i < addresses.size(); i++) {
			email = new Link(addresses.get(i));
			email.setURL(new StringBuffer("mailto:").append(addresses.get(i)).toString());
			email.setSessionId(false);
			emails.add(email);
			if (i + 1 > addresses.size()) {
				emails.add(new Text(", "));
			}
		}
		
		return emails;
	}
	
	public boolean isShowAddress() {
		return showAddress;
	}

	public void setShowAddress(boolean showAddress) {
		this.showAddress = showAddress;
	}

	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public boolean isShowEmails() {
		return showEmails;
	}

	public void setShowEmails(boolean showEmails) {
		this.showEmails = showEmails;
	}

	public boolean isShowEmptyFields() {
		return showEmptyFields;
	}

	public void setShowEmptyFields(boolean showEmptyFields) {
		this.showEmptyFields = showEmptyFields;
	}

	public boolean isShowExtraInfo() {
		return showExtraInfo;
	}

	public void setShowExtraInfo(boolean showExtraInfo) {
		this.showExtraInfo = showExtraInfo;
	}

	public boolean isShowFax() {
		return showFax;
	}

	public void setShowFax(boolean showFax) {
		this.showFax = showFax;
	}

	public boolean isShowHomePage() {
		return showHomePage;
	}

	public void setShowHomePage(boolean showHomePage) {
		this.showHomePage = showHomePage;
	}

	public boolean isShowName() {
		return showName;
	}

	public void setShowName(boolean showName) {
		this.showName = showName;
	}

	public boolean isShowPhone() {
		return showPhone;
	}

	public void setShowPhone(boolean showPhone) {
		this.showPhone = showPhone;
	}

	public boolean isShowShortName() {
		return showShortName;
	}

	public void setShowShortName(boolean showShortName) {
		this.showShortName = showShortName;
	}

	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public void setGroupsData(List<GroupDataBean> groupsData) {
		this.groupsData = groupsData;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
	private String getEmptyIfValueIsNull(String value) {
		return value == null ? "" : value;
	}
}
