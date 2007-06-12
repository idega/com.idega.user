package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.List;

import com.idega.bean.GroupPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserConstants;
import com.idega.webface.WFUtil;

public class GroupInfoViewer extends GroupViewer {
	
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
	private boolean showLabels = true;
	
	public GroupInfoViewer() {
		//setCacheable(getCacheKey());
	}

	/*public String getCacheKey() {
		return UserConstants.GROUP_INFO_VIEWER_CACHE_KEY;
	}
	
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		return cacheStatePrefix;
	}*/
	
	public void main(IWContext iwc) {
		String instanceId = BuilderLogic.getInstance().getInstanceId(this);
		/*if (instanceId == null) {
			throw new NullPointerException("Instance of presentation object 'GroupInfoViewer' is null");
		}*/
		
		Layer main = new Layer();
		
		//	Group info container
		Layer groupContainer = new Layer();
		if (instanceId != null) {
			groupContainer.setId(new StringBuffer(instanceId).append(UserConstants.GROUP_VIEWER_CONTAINER_ID_ENDING).toString());
		}
		main.add(groupContainer);
		
		//	JavaScript
		addJavaScript(iwc, instanceId, groupContainer.getId());
		
		add(main);
		
		setPropertiesBean(instanceId);
	}
	
	private void setPropertiesBean(String instanceId) {
		GroupPropertiesBean properties = new GroupPropertiesBean();
		
		properties.setServer(getServer());
		properties.setLogin(getUser());
		properties.setPassword(getPassword());
		
		properties.setUniqueIds(getUniqueIds());
		
		properties.setShowName(showName);
		properties.setShowHomePage(showHomePage);
		properties.setShowDescription(showDescription);
		properties.setShowExtraInfo(showExtraInfo);
		properties.setShowShortName(showShortName);
		properties.setShowPhone(showPhone);
		properties.setShowFax(showFax);
		properties.setShowEmails(showEmails);
		properties.setShowAddress(showAddress);
		properties.setShowEmptyFields(showEmptyFields);
		properties.setShowLabels(showLabels);
		
		properties.setRemoteMode(isRemoteMode());
		
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = properties;
		
		Class[] classes = new Class[2];
		classes[0] = String.class;
		classes[1] = GroupPropertiesBean.class;
		
		//	Setting parameters to bean, these parameters will be taken by DWR and sent to selected server to get required info
		WFUtil.invoke(UserConstants.GROUPS_MANAGER_BEAN_ID, "addGroupProperties", parameters, classes);
	}
	
	private void addJavaScript(IWContext iwc, String instanceId, String id) {		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		List<String> files = new ArrayList<String>();
		//	"Helpers"
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupInfoViewerHelper.js"));
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		if (isAddJavaScriptForGroupsTree()) {
			files.add(iwb.getVirtualPathWithFileNameString("javascript/groupTree.js"));
		}
		//	DWR
		files.add(UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		files.add("/dwr/engine.js");
		addScriptFiles(iwc, files, true);	//	TODO

		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {getSelectedGroups('");
		action.append(instanceId).append("', '").append(id).append("', '");
		action.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');});");
		
		//	Adding script to page
		StringBuffer scriptString = new StringBuffer("<script type=\"text/javascript\" > \n").append("\t").append(action);
		scriptString.append(" \n").append("</script> \n");
		add(scriptString.toString());
	}

	public void setShowAddress(boolean showAddress) {
		this.showAddress = showAddress;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public void setShowEmails(boolean showEmails) {
		this.showEmails = showEmails;
	}

	public void setShowEmptyFields(boolean showEmptyFields) {
		this.showEmptyFields = showEmptyFields;
	}

	public void setShowExtraInfo(boolean showExtraInfo) {
		this.showExtraInfo = showExtraInfo;
	}

	public void setShowFax(boolean showFax) {
		this.showFax = showFax;
	}

	public void setShowHomePage(boolean showHomePage) {
		this.showHomePage = showHomePage;
	}

	public void setShowName(boolean showName) {
		this.showName = showName;
	}

	public void setShowPhone(boolean showPhone) {
		this.showPhone = showPhone;
	}

	public void setShowShortName(boolean showShortName) {
		this.showShortName = showShortName;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

}
