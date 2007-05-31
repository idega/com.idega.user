package com.idega.user.presentation.group;

import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.bean.UserPropertiesBean;
import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserConstants;
import com.idega.webface.WFUtil;

public class GroupUsersViewer extends Block {
	
	private static final String USERS_INFO_CONTAINER_ID = "selected_users_info_container";

	private String server = null;
	private String user = null;
	private String password = null;
	
	private List<String> uniqueIds = null;
	
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
		if (instanceId == null) {
			throw new NullPointerException("Instance of presentation object 'GroupUsersViewer' is null");
		}
		
		//	JavaScript
		addJavaScript(iwc, instanceId);
		
		Layer main = new Layer();
		
		//	Users info container
		Layer usersContainer = new Layer();
		usersContainer.setId(USERS_INFO_CONTAINER_ID);
		main.add(usersContainer);
		
		add(main);
		
		setPropertiesBean(instanceId);
	}
	
	private void setPropertiesBean(String instanceId) {
		UserPropertiesBean properties = new UserPropertiesBean();
		
		properties.setServer(server);
		properties.setLogin(user);
		properties.setPassword(password);
		
		properties.setUniqueIds(uniqueIds);
		
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
	
	private void addJavaScript(IWContext iwc, String instanceId) {		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		AddResource resourceAdder = AddResourceFactory.getInstance(iwc);
		
		//	"Helper"
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwb.getVirtualPathWithFileNameString("javascript/UserInfoViewerHelper.js"));
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		
		//	DWR
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");

		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {getSelectedUsers('");
		action.append(instanceId).append("', '").append(USERS_INFO_CONTAINER_ID).append("', '");
		action.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');});");
		
		//	Adding script to page
		StringBuffer scriptString = new StringBuffer("<script type=\"text/javascript\" > \n").append("\t").append(action);
		scriptString.append(" \n").append("</script> \n");
		add(scriptString.toString());
	}
	
	public void setGroups(String server, String user, String password, List<String> uniqueIds) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.uniqueIds = uniqueIds;
	}
	
	public void setDisplayOptions(boolean showGroupName, boolean showTitle, boolean showAge, boolean showWorkPhone, boolean showHomePhone,
			boolean showMobilePhone, boolean showEmails, boolean showEducation, boolean showSchool, boolean showArea, boolean showBeganWork,
			boolean showImage) {
		
		this.showGroupName = showGroupName;
		this.showTitle = showTitle;
		this.showAge = showAge;
		this.showWorkPhone = showWorkPhone;
		this.showHomePhone = showHomePhone;
		this.showMobilePhone = showMobilePhone;
		this.showEmails = showEmails;
		this.showEducation = showEducation;
		this.showSchool = showSchool;
		this.showArea = showArea;
		this.showBeganWork = showBeganWork;
		this.showImage = showImage;
	}
	
	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
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
	
}
