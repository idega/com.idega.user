package com.idega.user.presentation.group;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.user.business.UserConstants;

public class GroupTreeViewer extends Block {
	
	public GroupTreeViewer() {
	}
	
	public GroupTreeViewer(boolean executeScriptOnLoad) {
		this.executeScriptOnLoad = executeScriptOnLoad;
	}
	
	private String groupsTreeContainerId = "local_groups_tree_container_id";
	private String selectedGroupsParameter = "null";
	private String loadRemoteGroupsFunction = null;
	
	private boolean executeScriptOnLoad = true;
	private boolean addExtraJavaScript = true;
	
	public void main(IWContext iwc) {
		Layer main = new Layer();
		
		Layer treeContainer = new Layer();
		treeContainer.setId(groupsTreeContainerId);
		main.add(treeContainer);
		
		addJavaScript(iwc);
		
		add(main);
	}
	
	private void addJavaScript(IWContext iwc) {
		if (addExtraJavaScript) {
			IWBundle iwb = getBundle(iwc);
			
			AddResource resource = AddResourceFactory.getInstance(iwc);
			
			//	"Helpers"
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/groupTree.js"));
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
			
			//	DWR
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, UserConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		}
		
		//	Actions to be performed on page loaded event
		StringBuffer loadTreeFunction = new StringBuffer();
		if (loadRemoteGroupsFunction == null) {	//	Then loading local groups
			loadTreeFunction.append("loadLocalTree('").append(groupsTreeContainerId).append("', '");
			loadTreeFunction.append(getResourceBundle(iwc).getLocalizedString("no_groups_found", "Sorry, no groups found on selected server."));
			loadTreeFunction.append("', ").append(selectedGroupsParameter).append(");");
		}
		else {
			loadTreeFunction.append(loadRemoteGroupsFunction);
		}
		
		StringBuffer action = new StringBuffer();
		if (executeScriptOnLoad) {
			action.append("registerEvent(window, 'load', function() {").append(loadTreeFunction).append("});");
		}
		else {
			action = loadTreeFunction;
		}
		
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

	public boolean isAddExtraJavaScript() {
		return addExtraJavaScript;
	}

	public void setAddExtraJavaScript(boolean addExtraJavaScript) {
		this.addExtraJavaScript = addExtraJavaScript;
	}

	public boolean isExecuteScriptOnLoad() {
		return executeScriptOnLoad;
	}

	public void setExecuteScriptOnLoad(boolean executeScriptOnLoad) {
		this.executeScriptOnLoad = executeScriptOnLoad;
	}

	public void setLoadRemoteGroupsFunction(String loadRemoteGroupsFunction) {
		this.loadRemoteGroupsFunction = loadRemoteGroupsFunction;
	}

	public void setSelectedGroupsParameter(String selectedGroupsParameter) {
		this.selectedGroupsParameter = selectedGroupsParameter;
	}

}
