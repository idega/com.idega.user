package com.idega.user.handler;

import java.util.List;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.chooser.helper.GroupsChooserHelper;
import com.idega.core.builder.presentation.ICPropertyHandler;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.business.UserConstants;
import com.idega.user.presentation.group.GroupInfoViewer;
import com.idega.user.presentation.group.GroupUsersViewer;
import com.idega.user.presentation.group.GroupsChooser;
import com.idega.util.CoreConstants;

public class GroupsHandler implements ICPropertyHandler {

	public List getDefaultHandlerTypes() {
		return null;
	}

	public PresentationObject getHandlerObject(String name, String stringValue, IWContext iwc, boolean oldGenerationHandler, String instanceId, String method) {
		
		String action = null;
		
		BuilderLogic builder = BuilderLogic.getInstance();
		String className = builder.getModuleClassName(builder.getCurrentIBPage(iwc), instanceId);
		if (className != null) {
			String message = "Loading...";
			try{
				IWResourceBundle iwrb =  iwc.getIWMainApplication().getBundle(CoreConstants.IW_USER_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
				message = iwrb.getLocalizedString("loading", message);
			} catch (Exception e) {}
			
			StringBuffer temp = new StringBuffer();
			if (className.equals(GroupInfoViewer.class.getName())) {
				temp.append("reloadGroupProperties('");
			}
			else if (className.equals(GroupUsersViewer.class.getName())) {
				temp.append("reloadGroupMemberProperties('");
			}
			temp.append(instanceId).append("', '").append(instanceId).append(UserConstants.GROUP_VIEWER_CONTAINER_ID_ENDING).append("', '");
			temp.append(message).append("');");
			action = temp.toString();
		}

		GroupsChooser chooser = new GroupsChooser(instanceId, method, action);
		GroupsChooserHelper helper = new GroupsChooserHelper();
		chooser.setBean(helper.getExtractedPropertiesFromString(stringValue));
		return chooser;
	}

	public void onUpdate(String[] values, IWContext iwc) {
	}

}
