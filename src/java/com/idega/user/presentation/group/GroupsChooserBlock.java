package com.idega.user.presentation.group;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.bean.PropertiesBean;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.builder.business.ICBuilderConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.FieldSet;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Legend;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.RadioGroup;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.AbstractChooserBlock;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreUtil;

public class GroupsChooserBlock extends AbstractChooserBlock {
	
	private static final String SERVER_NAME_FIELD_ID  = "serverNameFieldId";
	private static final String LOGIN_FIELD_ID = "loginFieldId";
	private static final String PASSWORD_FIELD_ID = "passwordFieldId";
	
	private static final String GROUPS_TREE_CONTAINER_ID = "groups_tree_container_id";
	
	private static final String RADIO_BUTTON_STYLE = "groupInfoChooserRadioStyle";
	
	private static final String PARAMETERS_SEPARATOR = "', '";
	
	private boolean addExtraJavaScript = true;
	private boolean executeScriptOnLoad = true;
	
	private String nodeOnClickAction = null;
	private String server = null;
	private String login = null;
	private String password = null;
	
	private List<String> uniqueIds = null;
	
	private boolean isRemoteMode = false;
	
	private String idsParameterForFunction = null;
	
	public void main(IWContext iwc) {
		idsParameterForFunction = getIdsString(uniqueIds);
		
		Layer main = new Layer();
		
		//	JavaScript
		addJavaScript(iwc);
		
		//	Groups tree
		addGroupsTreeContainer(iwc, main);
		
		//	Connection chooser
		addConnectionTypeField(iwc, main);
		
		add(main);
	}
	
	private void addGroupsTreeContainer(IWContext iwc, Layer main) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer groupsTreeContainer = new Layer();
		FieldSet groupsContainer = new FieldSet(new Legend(iwrb.getLocalizedString("groups_tree", "Groups tree")));
		Text explanation = new Text(iwrb.getLocalizedString("how_select_group", "Select (deselect) group by clicking it's name:"));
		groupsContainer.add(explanation);
		
		GroupTreeViewer groupsTree = new GroupTreeViewer(executeScriptOnLoad);
		groupsTree.setGroupsTreeContainerId(GROUPS_TREE_CONTAINER_ID);
		if (isRemoteMode) {
			StringBuffer function = new StringBuffer("getGroupsWithValues('");
			function.append(iwrb.getLocalizedString("loading", "Loading...")).append(PARAMETERS_SEPARATOR);
			function.append(server).append(PARAMETERS_SEPARATOR).append(login).append(PARAMETERS_SEPARATOR);
			function.append(CoreUtil.getEncodedValue(password)).append(PARAMETERS_SEPARATOR);
			function.append(GROUPS_TREE_CONTAINER_ID).append(PARAMETERS_SEPARATOR);
			function.append(iwrb.getLocalizedString("cannot_connect", "Sorry, unable to connect to:")).append(PARAMETERS_SEPARATOR);
			function.append(iwrb.getLocalizedString("failed_login", "Sorry, unable to log in to:")).append(PARAMETERS_SEPARATOR);
			function.append(iwrb.getLocalizedString("no_groups_found", "Sorry, no groups found on selected server.")).append("', true");
			function.append(", ").append(idsParameterForFunction).append(");");
			groupsTree.setLoadRemoteGroupsFunction(function.toString());
		}
		groupsTree.setSelectedGroupsParameter(idsParameterForFunction);
		groupsContainer.add(groupsTree);
		
		groupsTreeContainer.add(groupsContainer);
		main.add(groupsTreeContainer);
	}
	
	private void addJavaScript(IWContext iwc) {		
		if (addExtraJavaScript) {
			//	MooTools
			Web2Business web2Bean = null;
			try {
				web2Bean = (Web2Business) IBOLookup.getServiceInstance(iwc, Web2Business.class);
			} catch (IBOLookupException e) {
				e.printStackTrace();
			}
			if (web2Bean != null) {
				try {
					AddResource resource = AddResourceFactory.getInstance(iwc);
					resource.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2Bean.getBundleURIToMootoolsLib());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		//	Actions to be performed on page loaded event
		StringBuffer function = new StringBuffer("registerGroupInfoChooserActions(");
		if (nodeOnClickAction == null) {
			function.append("null");
		}
		else {
//			function.append("'").append(nodeOnClickAction).append("'");
			function.append(nodeOnClickAction);
		}
		function.append(", '");
		function.append(getResourceBundle(iwc).getLocalizedString("no_groups_found", "Sorry, no groups found on selected server."));
		function.append("', ").append(idsParameterForFunction).append(");");
		
		StringBuffer scriptString = new StringBuffer("<script type=\"text/javascript\" > \n").append("\t");
		if (executeScriptOnLoad) {
			scriptString.append("registerEvent(window, 'load', ").append("function(){").append(function).append("});");
		}
		else {
			scriptString.append(function);
		}
		scriptString.append(" \n").append("</script> \n");
		add(scriptString.toString());
	}
	
	private void addConnectionTypeField(IWContext iwc, Layer main) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		//	Connection type
		Layer layer = new Layer();
		layer.setId("groupInfoConnectionChooser");
		
		FieldSet serverContainer = new FieldSet(new Legend(iwrb.getLocalizedString("server", "Server")));
		RadioGroup radioGroup = new RadioGroup("server");
		
		Text txtLocal = new Text(iwrb.getLocalizedString("localServer", "Local server"));
		RadioButton btnLocal = new RadioButton("server");
		btnLocal.setValue(new StringBuffer("local").append("@").append(GROUPS_TREE_CONTAINER_ID).toString());
		btnLocal.setStyleClass(RADIO_BUTTON_STYLE);

		Text txtRemote = new Text(iwrb.getLocalizedString("remoteServer", "Remote server"));
		RadioButton btnRemote = new RadioButton("server");
		btnRemote.setValue(new StringBuffer(ICBuilderConstants.GROUPS_CHOOSER_REMOTE_CONNECTION).append("@").append(GROUPS_TREE_CONTAINER_ID).toString());
		btnRemote.setStyleClass(RADIO_BUTTON_STYLE);
		
		if (isRemoteMode) {
			btnRemote.setSelected();
		}
		else {
			btnLocal.setSelected();
		}
		
		radioGroup.addRadioButton(btnLocal, txtLocal);
		radioGroup.addRadioButton(btnRemote, txtRemote);
		
		serverContainer.add(radioGroup);
		layer.add(serverContainer);
		
		//	Remote connection data
		Layer connData = new Layer();
		connData.setId("connectionData");
		connData.setStyleClass("hidden_table");
		if (!isRemoteMode) {
			connData.setStyleAttribute("display: none");
		}
		
		FieldSet connectionContainer = new FieldSet(new Legend(iwrb.getLocalizedString("remote_connection_data", "Connection")));
		connData.add(connectionContainer);
		
		Table data = new Table(2, 3);
		data.setStyleClass("groupsChooserConnectionDataTableStyle");
		connectionContainer.add(data);
		
		String server = getServer();
		if (server == null) {
			server = "http://";
		}
		addRowToConnectionTable(data, server, iwrb.getLocalizedString("serverName", "Server name"), SERVER_NAME_FIELD_ID, "server", false);
		addRowToConnectionTable(data, login, iwrb.getLocalizedString("login", "Login"), LOGIN_FIELD_ID, "login", false);
		addRowToConnectionTable(data, password, iwrb.getLocalizedString("password", "password"), PASSWORD_FIELD_ID, "password", true);
		
		Layer buttonLayer = new Layer();
		GenericButton button = new GenericButton(iwrb.getLocalizedString("refresh", "Refresh"));
		
		button.setOnClick(getGroupsTreeAction(iwrb));
		button.setId("connDataBtn");
		buttonLayer.add(button);
		connectionContainer.add(buttonLayer);
		
		layer.add(connData);	
		
		main.add(layer);
	}
	
	private String getGroupsTreeAction(IWResourceBundle iwrb) {
		StringBuffer action = new StringBuffer("getGroupsTree('").append(SERVER_NAME_FIELD_ID).append(PARAMETERS_SEPARATOR);
		action.append(LOGIN_FIELD_ID).append(PARAMETERS_SEPARATOR).append(PASSWORD_FIELD_ID).append(PARAMETERS_SEPARATOR);
		action.append(GROUPS_TREE_CONTAINER_ID).append("', ['").append(iwrb.getLocalizedString("enter_fields", "Please, fill all fields!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_server", "Please, enter server name!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_login", "Please, enter Your login!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_password", "Please, enter Your password!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("loading", "Loading..."));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("cannot_connect", "Sorry, unable to connect to:"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("failed_login", "Sorry, unable to log in to:"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("no_groups_found", "Sorry, no groups found on selected server."));
		action.append("'], ").append(idsParameterForFunction).append(");");
		
		return action.toString();
	}
	
	private String getIdsString(List<String> uniqueIds) {
		StringBuffer ids = new StringBuffer();
		
		if (uniqueIds == null) {
			return "null";
		}
		else {
			if (uniqueIds.size() == 0) {
				return "null";
			}
		}
		
		ids.append("['");
		for (int i = 0; i < uniqueIds.size(); i++) {
			ids.append(uniqueIds.get(i));
			if (i + 1 < uniqueIds.size()) {
				ids.append(PARAMETERS_SEPARATOR);
			}
		}
		ids.append("']");
		return ids.toString();
	}
	
	@SuppressWarnings("deprecation")
	private void addRowToConnectionTable(Table table, String value, String content, String inputId, String name, boolean isPassword) {
		TableRow tableRow = new TableRow();
		table.add(tableRow);
		
		TableCell2 textCell = tableRow.createCell();
		textCell.add(new Text(content));
		TableCell2 inputCell = tableRow.createCell();
		TextInput input = null;
		if (isPassword) {
			input = new PasswordInput();
		}
		else {
			input = new TextInput();
		}
		input.setId(inputId);
		input.setStyleClass("groupConnectionChooserInputStyle");
		input.setName(name);
		if (value != null) {
			input.setValue(value);
		}
		inputCell.add(input);
	}
	
	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public boolean getChooserAttributes() {
		return false;
	}

	public boolean isExecuteScriptOnLoad() {
		return executeScriptOnLoad;
	}

	public void setExecuteScriptOnLoad(boolean executeScriptOnLoad) {
		this.executeScriptOnLoad = executeScriptOnLoad;
	}

	public String getNodeOnClickAction() {
		return nodeOnClickAction;
	}

	public void setNodeOnClickAction(String nodeOnClickAction) {
		this.nodeOnClickAction = nodeOnClickAction;
	}

	public List<String> getUniqueIds() {
		return uniqueIds;
	}

	public void setUniqueIds(List<String> uniqueIds) {
		this.uniqueIds = uniqueIds;
	}

	public boolean isAddExtraJavaScript() {
		return addExtraJavaScript;
	}

	public void setAddExtraJavaScript(boolean addExtraJavaScript) {
		this.addExtraJavaScript = addExtraJavaScript;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
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
	
	public void setPropertiesBean(PropertiesBean bean) {
		if (bean == null) {
			return;
		}
		
		if (bean.isRemoteMode()) {
			this.server = bean.getServer();
			this.login = bean.getLogin();
			this.password = bean.getPassword();
		}
		else {
			server = null;
			login = null;
			password = null;
		}
		this.uniqueIds = bean.getUniqueIds();
		this.isRemoteMode = bean.isRemoteMode();
		
		if (server != null) {
			if (!server.startsWith("http://") && !server.startsWith("https://")) {
				server = new StringBuffer("http://").append(server).toString();
			}
			if (server.endsWith("/")) {
				server = server.substring(0, server.lastIndexOf("/"));
			}
		}
	}
	
}
