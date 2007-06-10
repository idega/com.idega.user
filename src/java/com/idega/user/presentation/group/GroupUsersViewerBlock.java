package com.idega.user.presentation.group;

import java.util.List;

import com.idega.bean.GroupMemberDataBean;
import com.idega.bean.GroupMembersDataBean;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserConstants;

public class GroupUsersViewerBlock extends Block {

	private boolean showGroupName = true;
	private boolean showTitle = true;
	private boolean showAge = true;
	private boolean showWorkPhone = true;
	private boolean showHomePhone = true;
	private boolean showMobilePhone = true;
	private boolean showEmails = true;
	private boolean showEducation = true;
	private boolean showSchool = true;
	private boolean showArea = true;
	private boolean showBeganWork = true;
	private boolean showImage = true;
	
	private List<GroupMembersDataBean> membersData = null;
	
	private String styleClass = "groupsMembersInfoList";
	private boolean showLabels = false;
	
	private String imageWidth = "100";
	private String imageHeight = "150";
	
	private String server = null;
	
	public void main(IWContext iwc) {
		if (membersData == null) {
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer container = new Layer();
		if (styleClass != null) {
			container.setStyleClass(styleClass);
		}
		
		GroupMembersDataBean groupsData = null;
		GroupMemberDataBean userData = null;
		
		Lists groups = new Lists();
		
		List<GroupMemberDataBean> usersData = null;
		for (int i = 0; i < membersData.size(); i++) {
			groupsData = membersData.get(i);
			usersData = groupsData.getMembersInfo();
			
			ListItem groupUsers = new ListItem();
			
			if (showGroupName) {
				String groupName = getEmptyIfValueIsNull(groupsData.getGroupName());
				if (groupName.length() > 0){
					Layer groupNameContainer = new Layer();
					groupNameContainer.add(new Text(groupName));
					groupUsers.add(groupNameContainer);
				}
			}
			
			//	Rendering users info
			if (usersData != null) {
				Lists users = new Lists();
				for (int j = 0; j < usersData.size(); j++) {
					userData = usersData.get(j);
					
					ListItem user = new ListItem();
					
					//	Image
					if (showImage) {
						if (userData.getImageUrl() != null) {
							Layer imageContainer = new Layer();
							Image userImage = new Image(new StringBuffer(getServer()).append(userData.getImageUrl()).toString());
							if (imageWidth != null) {
								userImage.setWidth(imageWidth);
							}
							if (imageHeight != null) {
								userImage.setHeight(imageHeight);
							}
							imageContainer.add(userImage);
							user.add(imageContainer);
						}
					}
					
					//	Name
					Layer nameContainer = new Layer();
					if (showLabels) {
						nameContainer.add(new Text(iwrb.getLocalizedString("user_name", "Name: ")));
					}
					nameContainer.add(userData.getName());
					users.add(nameContainer);
					
					//	Title
					if (showTitle) {
						Layer titleContainer = new Layer();
						if (showLabels) {
							titleContainer.add(new Text(iwrb.getLocalizedString("user_title", "Title: ")));
						}
						titleContainer.add(new Text(getEmptyIfValueIsNull(userData.getTitle())));
						user.add(titleContainer);
					}
					
					//	Age
					if (showAge) {
						Layer ageContainer = new Layer();
						if (showLabels) {
							ageContainer.add(new Text(iwrb.getLocalizedString("user_age", "Age: ")));
						}
						ageContainer.add(new Text(getEmptyIfValueIsNull(userData.getAge())));
						user.add(ageContainer);
					}
					
					//	Work phone
					if (showWorkPhone) {
						Layer workPhoneContainer = new Layer();
						if (showLabels) {
							workPhoneContainer.add(new Text(iwrb.getLocalizedString("user_workphone", "Workphone: ")));
						}
						workPhoneContainer.add(new Text(getEmptyIfValueIsNull(userData.getWorkPhone())));
						user.add(workPhoneContainer);
					}
					
					//	Home phone
					if (showHomePhone) {
						Layer homePhoneContainer = new Layer();
						if (showLabels) {
							homePhoneContainer.add(new Text(iwrb.getLocalizedString("user_homephone", "Homephone: ")));
						}
						homePhoneContainer.add(new Text(getEmptyIfValueIsNull(userData.getHomePhone())));
						user.add(homePhoneContainer);
					}	
					
					//	Mobile phone
					if (showMobilePhone) {
						Layer mobilePhoneContainer = new Layer();
						if (showLabels) {
							mobilePhoneContainer.add(new Text(iwrb.getLocalizedString("user_mobilephone", "Mobilephone: ")));
						}
						mobilePhoneContainer.add(new Text(getEmptyIfValueIsNull(userData.getMobilePhone())));
						user.add(mobilePhoneContainer);
					}
					
					//	Emails
					if (showEmails) {
						Layer emailsContainer = new Layer();
						if (showLabels) {
							emailsContainer.add(new Text(iwrb.getLocalizedString("group_email", "Email: ")));
						}
						Layer emails = getEmails(userData.getEmailsAddresses());
						if (emails != null) {
							emailsContainer.add(emails);
						}
						user.add(emailsContainer);
					}
					
					//	Education
					if (showEducation) {
						Layer educationContainer = new Layer();
						if (showLabels) {
							educationContainer.add(new Text(iwrb.getLocalizedString("user_education", "Education: ")));
						}
						educationContainer.add(new Text(getEmptyIfValueIsNull(userData.getEducation())));
						user.add(educationContainer);
					}
					
					//	School
					if (showSchool) {
						Layer schoolContainer = new Layer();
						if (showLabels) {
							schoolContainer.add(new Text(iwrb.getLocalizedString("user_school", "School: ")));
						}
						schoolContainer.add(new Text(getEmptyIfValueIsNull(userData.getSchool())));
						user.add(schoolContainer);
					}
					
					//	Area
					if (showArea) {
						Layer areaContainer = new Layer();
						if (showLabels) {
							areaContainer.add(new Text(iwrb.getLocalizedString("user_area", "Area: ")));
						}
						areaContainer.add(new Text(getEmptyIfValueIsNull(userData.getArea())));
						user.add(areaContainer);
					}
					
					//	Began work
					if (showBeganWork) {
						Layer beganWorkContainer = new Layer();
						if (showLabels) {
							beganWorkContainer.add(new Text(iwrb.getLocalizedString("user_began_work", "Began work: ")));
						}
						if (userData.getBeganWork() != null) {
							beganWorkContainer.add(new Text(userData.getBeganWork()/*new IWTimestamp(userData.getBeganWork()).getDateString("dd-MM-yyyy")*/));
						}
						user.add(beganWorkContainer);
					}
					
					users.add(user);
				}
				groupUsers.add(users);
			}
			
			groups.add(groupUsers);
		}
		
		container.add(groups);
		add(container);
	}
	
	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public List<GroupMembersDataBean> getMembersData() {
		return membersData;
	}

	public void setMembersData(List<GroupMembersDataBean> membersData) {
		this.membersData = membersData;
	}

	public boolean isShowAge() {
		return showAge;
	}

	public void setShowAge(boolean showAge) {
		this.showAge = showAge;
	}

	public boolean isShowArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public boolean isShowBeganWork() {
		return showBeganWork;
	}

	public void setShowBeganWork(boolean showBeganWork) {
		this.showBeganWork = showBeganWork;
	}

	public boolean isShowEducation() {
		return showEducation;
	}

	public void setShowEducation(boolean showEducation) {
		this.showEducation = showEducation;
	}

	public boolean isShowEmails() {
		return showEmails;
	}

	public void setShowEmails(boolean showEmails) {
		this.showEmails = showEmails;
	}

	public boolean isShowGroupName() {
		return showGroupName;
	}

	public void setShowGroupName(boolean showGroupName) {
		this.showGroupName = showGroupName;
	}

	public boolean isShowHomePhone() {
		return showHomePhone;
	}

	public void setShowHomePhone(boolean showHomePhone) {
		this.showHomePhone = showHomePhone;
	}

	public boolean isShowMobilePhone() {
		return showMobilePhone;
	}

	public void setShowMobilePhone(boolean showMobilePhone) {
		this.showMobilePhone = showMobilePhone;
	}

	public boolean isShowSchool() {
		return showSchool;
	}

	public void setShowSchool(boolean showSchool) {
		this.showSchool = showSchool;
	}

	public boolean isShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public boolean isShowWorkPhone() {
		return showWorkPhone;
	}

	public void setShowWorkPhone(boolean showWorkPhone) {
		this.showWorkPhone = showWorkPhone;
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
	private String getEmptyIfValueIsNull(String value) {
		return value == null ? "" : value;
	}
	
	private Layer getEmails(List<String> addresses) {
		if (addresses == null) {
			return null;
		}
		
		Layer emails = new Layer();
		
		for (int i = 0; i < addresses.size(); i++) {
			Link email = new Link(addresses.get(i));
			email.setURL(new StringBuffer("mailto:").append(addresses.get(i)).toString());
			email.setSessionId(false);
			emails.add(email);
			if (i + 1 > addresses.size()) {
				emails.add(new Text(", "));
			}
		}
		
		return emails;
	}

	public boolean isShowImage() {
		return showImage;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public String getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(String imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(String imageWidth) {
		this.imageWidth = imageWidth;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		if (server == null) {
			this.server = null;
			return;
		}
		if (server.endsWith("/")) {
			server = server.substring(0, server.lastIndexOf("/"));
		}
		this.server = server;
	}
	
}
