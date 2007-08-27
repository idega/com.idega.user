package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.List;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.bean.GroupPropertiesBean;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreConstants;
import com.idega.webface.WFUtil;

public class GroupInfoViewer extends GroupViewer {
	
	private boolean showName = true;
	private boolean showHomePage = true;
	private boolean showShortName = false;
	private boolean showPhone = true;
	private boolean showFax = true;
	private boolean showEmptyFields = false;
	
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
		super.main(iwc);
		
		String instanceId = BuilderLogic.getInstance().getInstanceId(this);
		
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
	
	@SuppressWarnings("unchecked")
	private void setPropertiesBean(String instanceId) {
		GroupPropertiesBean properties = new GroupPropertiesBean();
		setBasicProperties(properties, instanceId);
		
		properties.setShowName(showName);
		properties.setShowHomePage(showHomePage);
		properties.setShowShortName(showShortName);
		properties.setShowPhone(showPhone);
		properties.setShowFax(showFax);
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
	
	private void addJavaScript(IWContext iwc, String instanceId, String id) {		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		List<String> files = new ArrayList<String>();
		//	"Helpers"
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupInfoViewerHelper.js"));
		files.add(iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		//	DWR
		files.add(CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		files.add("/dwr/engine.js");
		addScriptFiles(iwc, files, false);

		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("window.addEvent('domready', function() {getSelectedGroups('");
		action.append(instanceId).append("', '").append(id).append("', '");
		action.append(iwb.getResourceBundle(iwc).getLocalizedString("loading", "Loading...")).append("');});");
		
		//	Adding script to page
		StringBuffer scriptString = new StringBuffer("<script type=\"text/javascript\" > \n").append("\t").append(action);
		scriptString.append(" \n").append("</script> \n");
		add(scriptString.toString());
	}

	public void setShowEmptyFields(boolean showEmptyFields) {
		this.showEmptyFields = showEmptyFields;
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

	public String getBundleIdentifier()	{
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}

}
