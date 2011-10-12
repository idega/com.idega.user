package com.idega.user.presentation.group;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.user.bean.PropertiesBean;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class GroupViewer extends Block {
	
	private String server = null;
	private String user = null;
	private String password = null;
	private String callback;
	
	private List<String> uniqueIds = null;
	
	private boolean remoteMode = false;
	private boolean addJavaScriptForGroupsTree = true;
	
	private boolean showDescription = false;
	private boolean showExtraInfo = false;
	private boolean showEmails = true;
	private boolean showAddress = true;
	private boolean showLabels = false;
	
	private Integer cacheTime = 10;
	
	@Autowired
	private Web2Business web2;
	
	@Autowired
	private JQuery jQuery;
	
	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);
		
		List<String> styleFiles = Arrays.asList(
				web2.getBundleUriToHumanizedMessagesStyleSheet(),
				getBundle(iwc).getVirtualPathWithFileNameString("style/user.css")
		);
		
		List<String> scripts = Arrays.asList(
				jQuery.getBundleURIToJQueryLib(),
				web2.getBundleUriToHumanizedMessagesScript()
		);
		
		if (CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			add(PresentationUtil.getStyleSheetsSourceLines(styleFiles));
			add(PresentationUtil.getJavaScriptSourceLines(scripts));
		}
		else {
			PresentationUtil.addStyleSheetsToHeader(iwc, styleFiles);
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
		}
	}
	
	public boolean isAddJavaScriptForGroupsTree() {
		return addJavaScriptForGroupsTree;
	}
	
	public void setAddJavaScriptForGroupsTree(boolean addJavaScriptForGroupsTree) {
		this.addJavaScriptForGroupsTree = addJavaScriptForGroupsTree;
	}

	public boolean isRemoteMode() {
		return remoteMode;
	}
	
	public void setRemoteMode(boolean remoteMode) {
		this.remoteMode = remoteMode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public List<String> getUniqueIds() {
		return uniqueIds;
	}

	public void setUniqueIds(List<String> uniqueIds) {
		this.uniqueIds = uniqueIds;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setGroups(PropertiesBean bean) {
		if (bean == null) {
			server = null;
			user = null;
			password = null;
			uniqueIds = null;
			remoteMode = false;
			return;
		}
		server = bean.getServer();
		user = bean.getLogin();
		password = bean.getPassword();
		uniqueIds = bean.getUniqueIds();
		remoteMode = bean.isRemoteMode();
		
		if (server != null) {
			if (!server.startsWith("http://") && !server.startsWith("https://")) {
				server = new StringBuffer("http://").append(server).toString();
			}
			if (server.endsWith("/")) {
				server = server.substring(0, server.lastIndexOf("/"));
			}
		}
	}
	
	protected void addScriptFiles(IWContext iwc, List<String> files) {
		if (iwc == null || files == null) {
			return;
		}
		
		//	DWR
		files.add(0, CoreConstants.DWR_ENGINE_SCRIPT);
		files.add(0, CoreConstants.GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		
		if (!files.contains(CoreUtil.getCoreBundle().getVirtualPathWithFileNameString("javascript/ChooserHelper.js"))) {
			files.add(0, CoreUtil.getCoreBundle().getVirtualPathWithFileNameString("javascript/ChooserHelper.js"));
		}
		if (!files.contains("/dwr/interface/ChooserService.js")) {
			files.add("/dwr/interface/ChooserService.js");
		}
		
		IWBundle bundle = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		files.add(bundle.getVirtualPathWithFileNameString("javascript/groupTree.js"));
		String groupHelper = bundle.getVirtualPathWithFileNameString("javascript/GroupHelper.js");
		if (!(files.contains(groupHelper))) {
			files.add(groupHelper);
		}
		
		//	MooTools and reflection
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			try {
				files.add(web2.getBundleURIToMootoolsLib());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			files.add(web2.getReflectionForMootoolsScriptFilePath());
		}
		
		add(PresentationUtil.getJavaScriptSourceLines(files));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, files);
	}

	public boolean isShowAddress() {
		return showAddress;
	}

	public void setShowAddress(boolean showAddress) {
		this.showAddress = showAddress;
	}

	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public boolean isShowEmails() {
		return showEmails;
	}

	public void setShowEmails(boolean showEmails) {
		this.showEmails = showEmails;
	}

	public boolean isShowExtraInfo() {
		return showExtraInfo;
	}

	public void setShowExtraInfo(boolean showExtraInfo) {
		this.showExtraInfo = showExtraInfo;
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public Integer getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(Integer cacheTime) {
		this.cacheTime = cacheTime;
	}
	
	protected void setBasicProperties(PropertiesBean bean, String instanceId) {
		if (bean == null || instanceId == null) {
			return;
		}
		
		bean.setServer(getServer());
		bean.setLogin(getUser());
		bean.setPassword(getPassword());
		
		bean.setUniqueIds(getUniqueIds());
		
		bean.setShowDescription(isShowDescription());
		bean.setShowExtraInfo(isShowExtraInfo());
		bean.setShowEmails(isShowEmails());
		bean.setShowAddress(isShowAddress());
		bean.setShowLabels(isShowLabels());
		
		bean.setRemoteMode(isRemoteMode());
		
		bean.setCacheTime(getCacheTime());
		
		bean.setInstanceId(instanceId);
		
		bean.setCallback(getCallback());
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}
}