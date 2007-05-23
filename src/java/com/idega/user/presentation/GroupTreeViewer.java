package com.idega.user.presentation;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.user.business.UserConstants;

public class GroupTreeViewer extends Block {
	
	private String groupsTreeContainerId = "local_groups_tree_container_id";
	
	public void main(IWContext iwc) {
		Layer main = new Layer();
		
		Layer treeContainer = new Layer();
		treeContainer.setId(groupsTreeContainerId);
		main.add(treeContainer);
		
		addJavaScript(iwc);
		
		add(main);
	}
	
	private void addJavaScript(IWContext iwc) {
		Page parent = getParentPage();
		if (parent == null) {
			return;
		}
		
		IWBundle iwb = getBundle(iwc);
		if (iwb == null) {
			return;
		}
		
		AddResource resourceAdder = AddResourceFactory.getInstance(iwc);
		
		//	"Helpers"
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/groupTree.js"));
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
		
		//	DWR
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");

		
		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {loadLocalTree('");action.append(groupsTreeContainerId).append("')});");
		
		StringBuffer scriptString = new StringBuffer();
		scriptString.append("<script type=\"text/javascript\" > \n")
		.append("\t").append(action).append(" \n")
		.append("</script> \n");
		add(scriptString.toString());
	}
	
	public String getGroupsTreeContainerId() {
		return groupsTreeContainerId;
	}

	public void setGroupsTreeContainerId(String groupsTreeContainerId) {
		this.groupsTreeContainerId = groupsTreeContainerId;
	}

	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

}
