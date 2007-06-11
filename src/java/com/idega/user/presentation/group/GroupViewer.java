package com.idega.user.presentation.group;

import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.bean.PropertiesBean;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;

public class GroupViewer extends Block {
	
	private String server = null;
	private String user = null;
	private String password = null;
	
	private List<String> uniqueIds = null;
	
	private boolean remoteMode = false;
	private boolean addJavaScriptForGroupsTree = true;
	
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
	
}
