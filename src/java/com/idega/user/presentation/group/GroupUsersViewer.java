package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.List;

import com.idega.bean.UserPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserConstants;
import com.idega.webface.WFUtil;

public class GroupUsersViewer extends GroupViewer {
	
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
	private boolean showLabels = false;
	
	private String imageWidth = "100";
	private String imageHeight = "150";
	
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
		String instanceId = BuilderLogic.getInstance().getInstanceId(this);
		/*if (instanceId == null) {
			throw new NullPointerException("Instance of presentation object 'GroupUsersViewer' is null");
		}*/
		
		Layer main = new Layer();
		
		//	Users info container
		Layer usersContainer = new Layer();
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
		properties.setShowEmails(showEmails);
		properties.setShowEducation(showEducation);
		properties.setShowSchool(showSchool);
		properties.setShowArea(showArea);
		properties.setShowBeganWork(showBeganWork);
		properties.setShowImage(showImage);
		properties.setShowLabels(showLabels);
		
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
		if (isAddJavaScriptForGroupsTree()) {
			files.add(iwb.getVirtualPathWithFileNameString("javascript/groupTree.js"));
		}
		//	DWR
		files.add(UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		files.add("/dwr/engine.js");
		addScriptFiles(iwc, files, instanceId == null);
		
		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {getSelectedUsers('");
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

	public void setShowEmails(boolean showEmails) {
		this.showEmails = showEmails;
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
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}
	
}
