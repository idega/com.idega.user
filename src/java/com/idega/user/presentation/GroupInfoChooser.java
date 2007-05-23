package com.idega.user.presentation;

import java.rmi.RemoteException;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
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
import com.idega.user.business.UserConstants;

/**
 * 
 * @author <a href="justinas@idega.com">Justinas Rakita</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/05/23 08:04:31 $ by $Author: valdas $
 *
 */
public class GroupInfoChooser extends Block {
	
	private static final String SERVER_NAME_FIELD_ID  = "serverNameFieldId";
	private static final String LOGIN_FIELD_ID = "loginFieldId";
	private static final String PASSWORD_FIELD_ID = "passwordFieldId";
	
	private static final String GROUPS_TREE_CONTAINER_ID = "groups_tree_container_id";
	
	private static final String RADIO_BUTTON_STYLE = "groupInfoChooserRadioStyle";
	
	private static final String PARAMETERS_SEPARATOR = "' , '";
	
	public static final String GROUP_SERVICE_DWR_INTERFACE_SCRIPT = "/dwr/interface/GroupService.js";
	
	public void main(IWContext iwc) {
		Layer main = new Layer();
		
		Layer treeContainer = new Layer();
		treeContainer.setId(GROUPS_TREE_CONTAINER_ID);
		main.add(treeContainer);
		
		addJavaScript(iwc);
		addGroupInfoChooser(iwc, main);
		
		add(main);
	}
	
	private void addJavaScript(IWContext iwc) {
		Page parent = getParentPage();
		if (parent == null) {
			return;
		}
		
		IWBundle bundle = getBundle(iwc);
		if (bundle == null) {
			return;
		}
		
		//	"Helpers"
		String resourcesPath = bundle.getResourcesPath();
		parent.addJavascriptURL(new StringBuffer(resourcesPath).append("/javascript/groupTree.js").toString());
		parent.addJavascriptURL(new StringBuffer(resourcesPath).append("/javascript/group.js").toString());
		
		//	DWR
		parent.addJavascriptURL(GROUP_SERVICE_DWR_INTERFACE_SCRIPT);
		parent.addJavascriptURL("/dwr/engine.js");
		
		//	MooTools
		Web2Business web2Bean = null;
		try {
			web2Bean = (Web2Business) IBOLookup.getServiceInstance(iwc, Web2Business.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (web2Bean != null) {
			try {
				parent.addJavascriptURL(web2Bean.getBundleURIToMootoolsLib());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		//	Actions to be performed on page loaded event
		StringBuffer action = new StringBuffer("registerEvent(window, 'load', function() {loadLocalTree('");
		action.append(GROUPS_TREE_CONTAINER_ID).append("')});");
		parent.addJavaScriptAfterJavaScriptURLs("user_groups_tree", action.toString());
		parent.addJavaScriptAfterJavaScriptURLs("group_info_chooser_action","registerEvent(window,'load',registerGroupInfoChooserActions);");
	}
	
	public void addGroupInfoChooser(IWContext iwc, Layer main) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		//	Connection type
		Layer layer = new Layer();
		layer.setId("groupInfoConnectionChooser");
		
		RadioGroup radioGroup = new RadioGroup("server");
		
		Text txtLocal = new Text(iwrb.getLocalizedString("localServer", "Local server"));
		RadioButton btnLocal = new RadioButton("server");
		btnLocal.setValue(new StringBuffer("local").append("@").append(GROUPS_TREE_CONTAINER_ID).toString());
		btnLocal.setSelected();
		btnLocal.setStyleClass(RADIO_BUTTON_STYLE);

		Text txtRemote = new Text(iwrb.getLocalizedString("remoteServer", "Remote server"));
		RadioButton btnRemote = new RadioButton("server");
		btnRemote.setValue(new StringBuffer("remote").append("@").append(GROUPS_TREE_CONTAINER_ID).toString());
		btnRemote.setStyleClass(RADIO_BUTTON_STYLE);
		
		radioGroup.addRadioButton(btnLocal, txtLocal);
		radioGroup.addRadioButton(btnRemote, txtRemote);	
		
		//	Remote connection data
		Layer connData = new Layer();
		connData.setId("connectionData");
		connData.setStyleClass("hidden_table");
		connData.setStyleAttribute("display: none");
		
		FieldSet connectionContainer = new FieldSet(new Legend(iwrb.getLocalizedString("remote_connection_data", "Connection")));
		connData.add(connectionContainer);
		
		Table data = new Table(2, 3);
		connectionContainer.add(data);
		
		addRowToConnectionTable(data, "http://", iwrb.getLocalizedString("serverName", "Server name"), SERVER_NAME_FIELD_ID, false);
		addRowToConnectionTable(data, null, iwrb.getLocalizedString("login", "Login"), LOGIN_FIELD_ID, false);
		addRowToConnectionTable(data, null, iwrb.getLocalizedString("password", "password"), PASSWORD_FIELD_ID, true);
		
		Layer buttonLayer = new Layer();
		GenericButton button = new GenericButton(iwrb.getLocalizedString("refresh", "Refresh"));
		StringBuffer action = new StringBuffer("getRemoteGroups('").append(SERVER_NAME_FIELD_ID).append(PARAMETERS_SEPARATOR);
		action.append(LOGIN_FIELD_ID).append(PARAMETERS_SEPARATOR).append(PASSWORD_FIELD_ID).append(PARAMETERS_SEPARATOR);
		action.append(GROUPS_TREE_CONTAINER_ID).append("', ['").append(iwrb.getLocalizedString("enter_fields", "Please, fill all fields!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_server", "Please, enter server name!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_login", "Please, enter Your login!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("enter_password", "Please, enter Your password!"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("loading", "Loading..."));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("cannot_connect", "Sorry, unable to connect to:"));
		action.append(PARAMETERS_SEPARATOR).append(iwrb.getLocalizedString("failed_login", "Sorry, unable to log in to:"));
		action.append("']);");
		button.setOnClick(action.toString());
		button.setId("connDataBtn");
		buttonLayer.add(button);
		connectionContainer.add(buttonLayer);
		
		layer.add(radioGroup);
		layer.add(connData);	
		
		main.add(layer);
	}
	
	private void addRowToConnectionTable(Table table, String value, String content, String inputId, boolean isPassword) {
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
		if (value != null) {
			input.setValue(value);
		}
		inputCell.add(input);
	}
	
	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
}
