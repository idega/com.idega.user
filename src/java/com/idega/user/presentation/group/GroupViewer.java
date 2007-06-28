package com.idega.user.presentation.group;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.bean.PropertiesBean;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.SpringBeanLookup;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;

public class GroupViewer extends Block {
	
	private String server = null;
	private String user = null;
	private String password = null;
	
	private List<String> uniqueIds = null;
	
	private boolean remoteMode = false;
	private boolean addJavaScriptForGroupsTree = true;
	
	private boolean showDescription = false;
	private boolean showExtraInfo = false;
	private boolean showEmails = true;
	private boolean showAddress = true;
	private boolean showLabels = false;
	
	private Integer cacheTime = 10;
	
	public void main(IWContext iwc) {
		AddResource adder = AddResourceFactory.getInstance(iwc);
		
		adder.addStyleSheet(iwc, AddResource.HEADER_BEGIN, getBundle(iwc).getVirtualPathWithFileNameString("style/user.css"));
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
	
	protected void addScriptFiles(IWContext iwc, List<String> files, boolean addDirectly) {
		if (iwc == null || files == null) {
			return;
		}
		
		//	Mootools and reflection
		Web2Business web2 = (Web2Business) SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		
		if (web2 != null) {
			try {
				files.add(web2.getBundleURIToMootoolsLib());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			files.add(web2.getReflectionForMootoolsScriptFilePath());
		}
		
		if (addDirectly) {
			for (int i = 0; i < files.size(); i++) {
				add(new StringBuffer("<script type=\"text/javascript\" src=\"").append(files.get(i)).append("\"><!--//--></script>").toString());
			}
		}
		else {
			AddResource resource = AddResourceFactory.getInstance(iwc);
			for (int i = 0; i < files.size(); i++) {
				resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, files.get(i));
			}
		}
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
	}
}
