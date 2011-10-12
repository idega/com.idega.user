package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.List;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.bean.UserPropertiesBean;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.webface.WFUtil;

public class GroupUsersViewer extends GroupViewer {
	
	private boolean showGroupName = false;
	private boolean showTitle = false;
	private boolean showAge = false;
	private boolean showWorkPhone = true;
	private boolean showHomePhone = true;
	private boolean showMobilePhone = true;
	private boolean showEducation = false;
	private boolean showSchool = false;
	private boolean showArea = false;
	private boolean showBeganWork = false;
	private boolean showImage = true;	
	private boolean showCompanyAddress = false;
	private boolean showDateOfBirth = false;
	private boolean showYearOfBirth = false;
	private boolean showJob = false;
	private boolean showWorkplace = false;
	private boolean showStatus = true;
	private boolean addReflection = false;
	private boolean showUserInfoOne = false;
	private boolean showUserInfoTwo = false;
	private boolean showUserInfoThree = false;
	
	private String imageWidth = "70";
	private String imageHeight = "90";
	
	@Override
	public void main(IWContext iwc) {
		super.main(iwc);
		
		String instanceId = BuilderLogic.getInstance().getInstanceId(this);
		
		Layer main = new Layer();
		
		//	Users info container
		Layer usersContainer = new Layer();
		if (instanceId != null) {
			usersContainer.setId(new StringBuffer(instanceId).append(UserConstants.GROUP_VIEWER_CONTAINER_ID_ENDING).toString());
		}
		main.add(usersContainer);
		
		// JavaScript
		addJavaScript(iwc, instanceId, usersContainer.getId());
		
		add(main);
		
		setPropertiesBean(instanceId);
	}
	
	private void setPropertiesBean(String instanceId) {
		UserPropertiesBean properties = new UserPropertiesBean();
		setBasicProperties(properties, instanceId);

		properties.setShowGroupName(showGroupName);
		properties.setShowTitle(showTitle);
		properties.setShowAge(showAge);
		properties.setShowWorkPhone(showWorkPhone);
		properties.setShowHomePhone(showHomePhone);
		properties.setShowMobilePhone(showMobilePhone);
		properties.setShowEducation(showEducation);
		properties.setShowSchool(showSchool);
		properties.setShowArea(showArea);
		properties.setShowBeganWork(showBeganWork);
		properties.setShowImage(showImage);
		properties.setShowDateOfBirth(showDateOfBirth);
		properties.setShowYearOfBirth(showYearOfBirth);
		properties.setShowJob(showJob);
		properties.setShowCompanyAddress(showCompanyAddress);
		properties.setShowWorkplace(showWorkplace);
		properties.setShowStatus(showStatus);
		properties.setAddReflection(addReflection);
		properties.setShowUserInfoOne(showUserInfoOne);
		properties.setShowUserInfoTwo(showUserInfoTwo);
		properties.setShowUserInfoThree(showUserInfoThree);
		
		properties.setImageHeight(imageHeight);
		properties.setImageWidth(imageWidth);
		
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = properties;
		
		Class<?>[] classes = new Class[2];
		classes[0] = String.class;
		classes[1] = UserPropertiesBean.class;
		
		//	Setting parameters to bean, these parameters will be taken by DWR and sent to selected server to get required info
		WFUtil.invoke(UserConstants.GROUPS_MANAGER_BEAN_ID, "addUserProperties", parameters, classes);
	}
	
	private void addJavaScript(IWContext iwc, String instanceId, String id) {		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		boolean singleRenderingProcess = CoreUtil.isSingleComponentRenderingProcess(iwc);
		
		List<String> files = new ArrayList<String>();
		//	"Helpers"		
		files.add(iwb.getVirtualPathWithFileNameString("javascript/UserInfoViewerHelper.js"));
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		addScriptFiles(iwc, files);
		
		//	Actions to be performed on page loaded event
		StringBuffer singleAction = new StringBuffer("getSelectedUsers('").append(instanceId).append("', '").append(id).append("', '");
		singleAction.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');");
		StringBuffer action = null;
		if (singleRenderingProcess) {
			action = singleAction;
		} else {
			action = new StringBuffer("window.addEvent('domready', function() {").append(singleAction.toString()).append("});");
		}
		
		//	Adding script to page
		PresentationUtil.addJavaScriptActionToBody(iwc, action.toString());
	}
	
	public void setShowAge(boolean showAge) {
		this.showAge = showAge;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public void setShowBeganWork(boolean showBeganWork) {
		this.showBeganWork = showBeganWork;
	}

	public void setShowEducation(boolean showEducation) {
		this.showEducation = showEducation;
	}

	public void setShowGroupName(boolean showGroupName) {
		this.showGroupName = showGroupName;
	}

	public void setShowHomePhone(boolean showHomePhone) {
		this.showHomePhone = showHomePhone;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public void setShowMobilePhone(boolean showMobilePhone) {
		this.showMobilePhone = showMobilePhone;
	}

	public void setShowSchool(boolean showSchool) {
		this.showSchool = showSchool;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public void setShowWorkPhone(boolean showWorkPhone) {
		this.showWorkPhone = showWorkPhone;
	}

	public void setImageHeight(String imageHeight) {
		this.imageHeight = imageHeight;
	}

	public void setImageWidth(String imageWidth) {
		this.imageWidth = imageWidth;
	}
	
	@Override
	public String getBundleIdentifier()	{
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}

	public void setShowCompanyAddress(boolean showCompanyAddress) {
		this.showCompanyAddress = showCompanyAddress;
	}

	public void setShowDateOfBirth(boolean showDateOfBirth) {
		this.showDateOfBirth = showDateOfBirth;
	}

	public void setShowYearOfBirth(boolean showYearOfBirth) {
		this.showYearOfBirth = showYearOfBirth;
	}

	public void setShowJob(boolean showJob) {
		this.showJob = showJob;
	}

	public void setShowStatus(boolean showStatus) {
		this.showStatus = showStatus;
	}

	public void setShowWorkplace(boolean showWorkplace) {
		this.showWorkplace = showWorkplace;
	}

	public void setAddReflection(boolean addReflection) {
		this.addReflection = addReflection;
	}
	
	public void setDescriptions(boolean showUserInfoOne, boolean showUserInfoTwo, boolean showUserInfoThree) {
		this.showUserInfoOne = showUserInfoOne;
		this.showUserInfoTwo = showUserInfoTwo;
		this.showUserInfoThree = showUserInfoThree;
	}
}
