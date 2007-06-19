package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.List;

import com.idega.bean.UserPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreConstants;
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
	private boolean showJob = false;
	private boolean showWorkplace = false;
	private boolean showStatus = true;
	private boolean addReflection = true;
	
	private String imageWidth = "70";
	private String imageHeight = "90";
	
	public GroupUsersViewer() {
//		setCacheable(getCacheKey());
	}

	/*public String getCacheKey() {
		return UserConstants.GROUP_USERS_VIEWER_CACHE_KEY;
	}
	
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		return cacheStatePrefix;
	}*/
	
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
		
		properties.setServer(getServer());
		properties.setLogin(getUser());
		properties.setPassword(getPassword());
		
		properties.setUniqueIds(getUniqueIds());
		
		properties.setShowGroupName(showGroupName);
		properties.setShowTitle(showTitle);
		properties.setShowAge(showAge);
		properties.setShowWorkPhone(showWorkPhone);
		properties.setShowHomePhone(showHomePhone);
		properties.setShowMobilePhone(showMobilePhone);
		properties.setShowEmails(isShowEmails());
		properties.setShowEducation(showEducation);
		properties.setShowSchool(showSchool);
		properties.setShowArea(showArea);
		properties.setShowBeganWork(showBeganWork);
		properties.setShowImage(showImage);
		properties.setShowLabels(isShowLabels());
		properties.setShowAddress(isShowAddress());
		properties.setShowExtraInfo(isShowExtraInfo());
		properties.setShowDescription(isShowDescription());
		properties.setShowDateOfBirth(showDateOfBirth);
		properties.setShowJob(showJob);
		properties.setShowCompanyAddress(showCompanyAddress);
		properties.setShowWorkplace(showWorkplace);
		properties.setShowStatus(showStatus);
		properties.setAddReflection(addReflection);
		
		properties.setRemoteMode(isRemoteMode());
		
		properties.setImageHeight(imageHeight);
		properties.setImageWidth(imageWidth);
		
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = properties;
		
		Class[] classes = new Class[2];
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
		
		List<String> files = new ArrayList<String>();
		//	"Helpers"
		files.add(iwb.getVirtualPathWithFileNameString("javascript/UserInfoViewerHelper.js"));
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		//	DWR
		files.add(CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		files.add("/dwr/engine.js");
		addScriptFiles(iwc, files, false);
		
		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("window.addEvent('domready', function() {getSelectedUsers('");
		action.append(instanceId).append("', '").append(id).append("', '");
		action.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');});");
		
		//	Adding script to page
		StringBuffer scriptString = new StringBuffer("<script type=\"text/javascript\" > \n").append("\t").append(action);
		scriptString.append(" \n").append("</script> \n");
		add(scriptString.toString());
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
	
	public String getBundleIdentifier()	{
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}

	public void setShowCompanyAddress(boolean showCompanyAddress) {
		this.showCompanyAddress = showCompanyAddress;
	}

	public void setShowDateOfBirth(boolean showDateOfBirth) {
		this.showDateOfBirth = showDateOfBirth;
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
	
}
