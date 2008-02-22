package com.idega.user.presentation.group;

import java.rmi.RemoteException;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.SpringBeanLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.CoreConstants;

public class GroupTreeViewer extends Block {
	
	private String groupsTreeViewerId = null;
	private String selectedGroupsParameter = "null";
	private String loadRemoteGroupsFunction = null;
	private String styleClass = "groupsTreeListElement";	// Set your style if you want to define actions for tree node
	
	private String customFunction = null;
	
	private boolean executeScriptOnLoad = true;
	private boolean addExtraJavaScript = true;
	
	public GroupTreeViewer() {
	}
	
	public GroupTreeViewer(boolean executeScriptOnLoad) {
		this.executeScriptOnLoad = executeScriptOnLoad;
	}
	
	public void main(IWContext iwc) {
		Layer main = new Layer();
		
		Layer treeContainer = new Layer();
		if (groupsTreeViewerId != null) {
			treeContainer.setId(groupsTreeViewerId);
		}
		main.add(treeContainer);
		
		addJavaScript(iwc, treeContainer.getId());
		
		add(main);
	}
	
	private void addJavaScript(IWContext iwc, String id) {
		if (addExtraJavaScript) {
			IWBundle iwb = getBundle(iwc);
			
			AddResource resource = AddResourceFactory.getInstance(iwc);
			
			//	"Helpers"
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/groupTree.js"));
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,iwb.getVirtualPathWithFileNameString("javascript/GroupHelper.js"));
			
			//	DWR
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
			resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, CoreConstants.DWR_ENGINE_SCRIPT);
			
			//	Mootools
			Web2Business web2 = SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
			
			if (web2 != null) {
				try {
					resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getBundleURIToMootoolsLib());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		//	Actions to be performed on page loaded event
		StringBuffer function = new StringBuffer();
		if (loadRemoteGroupsFunction == null) {	//	Then loading local groups
			function.append("loadLocalTree('").append(id).append("', '");
			function.append(getResourceBundle(iwc).getLocalizedString("no_groups_found", "Sorry, no groups found on selected server."));
			function.append("', ").append(selectedGroupsParameter).append(", ");
			if (styleClass == null) {
				function.append("null");
			}
			else {
				function.append("'").append(styleClass).append("'");
			}	
			function.append(");");
		}
		else {
			function.append(loadRemoteGroupsFunction);
		}
		
		if (customFunction != null) {
			function.append(" ").append(customFunction);
		}
		
		StringBuffer action = new StringBuffer();
		if (executeScriptOnLoad) {
			action.append("window.addEvent('domready', function() {").append(function).append("});");
		}
		else {
			action = function;
		}
		
		StringBuffer scriptString = new StringBuffer();
		scriptString.append("<script type=\"text/javascript\" > \n")
		.append("\t").append(action).append(" \n")
		.append("</script> \n");
		 
		add(scriptString.toString());
	}

	public String getBundleIdentifier()	{
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
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

	public void setGroupsTreeViewerId(String groupsTreeViewerId) {
		this.groupsTreeViewerId = groupsTreeViewerId;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public String getCustomFunction() {
		return customFunction;
	}

	public void setCustomFunction(String customFunction) {
		this.customFunction = customFunction;
	}
	
}
