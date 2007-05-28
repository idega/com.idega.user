package com.idega.user.presentation.group;

import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.bean.GroupPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.user.business.UserConstants;
import com.idega.webface.WFUtil;

public class GroupInfoViewer extends Block {
	
	private static final String GROUP_INFO_CONTAINER_ID = "selected_group_info_container";
	
	private String server = null;
	private String user = null;
	private String password = null;
	
	private List<String> uniqueIds = null;
	
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
	
	private String additionalInfo = null;
	
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
		System.out.println("Additional info");
		
		String instanceId = BuilderLogic.getInstance().getInstanceId(this);
		if (instanceId == null) {
			throw new NullPointerException("Instance of presentation object 'GroupInfoViewer' is null");
		}
		
		//	JavaScript
		addJavaScript(iwc, instanceId);
		
		Layer main = new Layer();
		
		//	Group info container
		Layer groupContainer = new Layer();
		groupContainer.setId(GROUP_INFO_CONTAINER_ID);
		main.add(groupContainer);
		
		add(main);
		
		setPropertiesBean(instanceId);
	}
	
	private void setPropertiesBean(String instanceId) {
		GroupPropertiesBean properties = new GroupPropertiesBean();
		
		properties.setServer(server);
		properties.setLogin(user);
		properties.setPassword(password);
		
		properties.setUniqueIds(uniqueIds);
		
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
		
		Object[] parameters = new Object[2];
		parameters[0] = instanceId;
		parameters[1] = properties;
		
		Class[] classes = new Class[2];
		classes[0] = String.class;
		classes[1] = GroupPropertiesBean.class;
		
		//	Setting parameters to bean, these parameters will be taken by DWR and sent to selected server to get required info
		WFUtil.invoke(UserConstants.GROUPS_MANAGER_BEAN_ID, "addGroupProperties", parameters, classes);
	}
	
	private void addJavaScript(IWContext iwc, String instanceId) {
		Page parent = getParentPage();
		if (parent == null) {
			return;
		}
		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		AddResource resourceAdder = AddResourceFactory.getInstance(iwc);
		
		//	"Helper"
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwb.getVirtualPathWithFileNameString("javascript/GroupInfoViewerHelper.js"));
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		
		//	DWR
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");

		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {getSelectedGroups('");
		action.append(instanceId).append("', '").append(GROUP_INFO_CONTAINER_ID).append("', '");
		action.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');});");
		parent.addJavaScriptAfterJavaScriptURLs("get_groups_action", action.toString());
	}
	
	public void setGroups(String server, String user, String password, List<String> uniqueIds) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.uniqueIds = uniqueIds;
	}
	
	public void setDisplayOptions(boolean showName, boolean showHomePage, boolean showDescription, boolean showExtraInfo,
			boolean showShortName, boolean showPhone, boolean showFax, boolean showEmails, boolean showAddress, boolean showEmptyFields) {
		this.showName = showName;
		this.showHomePage = showHomePage;
		this.showDescription = showDescription;
		this.showExtraInfo = showExtraInfo;
		this.showShortName = showShortName;
		this.showPhone = showPhone;
		this.showFax = showFax;
		this.showEmails = showEmails;
		this.showAddress = showAddress;
		this.showEmptyFields = showEmptyFields;
	}

	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

}
