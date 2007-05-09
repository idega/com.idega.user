package com.idega.user.business;

import java.io.IOException;

import javax.faces.context.FacesContext;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObjectUtil;
import com.idega.presentation.Script;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.RadioGroup;
import com.idega.presentation.ui.TextInput;

/**
 * 
 * @author <a href="justinas@idega.com">Justinas Rakita</a>
 *
 */
public class GroupChooser extends Block {
	
//	private UserBusiness userBiz = null;
	private static IWBundle bundle = null;	
	
	public void main(IWContext iwc) {
		Page parentPage = PresentationObjectUtil.getParentPage(this);
		getParentPage(parentPage);
	}
	
	public Page getParentPage(Page parentPage){	
		
		if (parentPage == null){
			return null;
		}
		//TODO: get real resource path
		IWBundle bundle = getBundle();
		String path = bundle.getResourcesPath();
		parentPage.addJavascriptURL(path + "/javascript/groupTree.js");
		parentPage.addJavascriptURL(path + "/javascript/group.js");
		parentPage.addJavascriptURL("/dwr/interface/GroupService.js");
		parentPage.addJavascriptURL("/dwr/engine.js");
//		parentPage.addJavaScriptAfterJavaScriptURLs("tree", "getPathToImageFolder();");	
		parentPage.addJavaScriptAfterJavaScriptURLs("tree", "registerEvent(window, 'load', loadTree)");
		Layer layer = new Layer();
		layer.setId(GroupConstants.groupsDivId);
		
		RadioGroup radioGroup = new RadioGroup("server");
			
		Text txtLocal = new Text(bundle.getLocalizedString(GroupConstants.localServerParameterName,"Local server"));
		RadioButton btnLocal = new RadioButton("server");
		
		btnLocal.setId("radioBtnLocal");
		
		btnLocal.setOnClick("setLocal('"+GroupConstants.tableID+"');");

		Text txtRemote = new Text(bundle.getLocalizedString(GroupConstants.remoteServerParameterName,"Remote server"));
		RadioButton btnRemote = new RadioButton("server");
		btnRemote.setOnClick("setRemote('"+GroupConstants.tableID+"');");
		
		btnLocal.setId("radioBtnRemote");
		
		radioGroup.addRadioButton(btnLocal, txtLocal);
		radioGroup.addRadioButton(btnRemote, txtRemote);	
			
		Text txtServerName = new Text(bundle.getLocalizedString(GroupConstants.serverNameParameterName, "Server name"));
		Text txtLogin = new Text(bundle.getLocalizedString(GroupConstants.loginParameterName, "Login"));
		Text txtPass = new Text(bundle.getLocalizedString(GroupConstants.passwordParameterName, "Password"));
			
		TextInput inpServerName = new TextInput();
		inpServerName.setId(GroupConstants.serverNameFieldId);
		TextInput inpLogin = new TextInput();
		inpLogin.setId(GroupConstants.loginFieldId);
		PasswordInput pswInput = new PasswordInput();
		pswInput.setId(GroupConstants.passwordFieldid);
			
		GenericButton button = new GenericButton(bundle.getLocalizedString(GroupConstants.refreshParameterName, "Refresh"));
		button.setOnClick("sendConnectionData('"+GroupConstants.serverNameFieldId+"','"+GroupConstants.loginFieldId+"','"+GroupConstants.passwordFieldid+"')");
		button.setId("connDataBtn");
		
		Layer connData = new Layer();
		connData.setId("connectionData");
		Layer serverNameLayer = new Layer();
		serverNameLayer.add(txtServerName);
		serverNameLayer.add(inpServerName);
		serverNameLayer.setStyleClass("connection_table_row");
		
		Layer loginLayer = new Layer();
		loginLayer.add(txtLogin);
		loginLayer.add(inpLogin);
		loginLayer.setStyleClass("connection_table_row");
		
		Layer passLayer = new Layer();	
		passLayer.add(txtPass);
		passLayer.add(pswInput);		
		passLayer.setStyleClass("connection_table_row");
		
		Layer buttonLayer = new Layer();
		buttonLayer.setStyleClass("connection_table_button");
		buttonLayer.add(button);
		
		
		connData.add(serverNameLayer);
		connData.add(loginLayer);
		connData.add(passLayer);
		connData.add(buttonLayer);
		
		connData.setStyleClass("hidden_table");
		
		layer.add(radioGroup);
		layer.add(connData);
		
		Script script = new Script();
		script.addScriptLine("setDivId(\'"+GroupConstants.groupsDivId+"\');");
		layer.add(script);		
		
		parentPage.add(layer);
		
		return parentPage;
	}
	
	public void encodeBegin(FacesContext fc)throws IOException{
		super.encodeBegin(fc);
		
		Layer panels = (Layer)this.getFacet("PANELS");
		this.renderChild(fc,panels);
	}
	
//	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
//		if (this.userBiz == null) {
//			try {
//				this.userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
//			}
//			catch (java.rmi.RemoteException rme) {
//				throw new RuntimeException(rme.getMessage());
//			}
//		}
//		return this.userBiz;
//	}	
	
//	public Collection convertGroupCollectionToGroupNodeCollection(Collection col, IWApplicationContext iwac){
//		List<GroupTreeNode> list = new Vector<GroupTreeNode>();
//		
//		Iterator iter = col.iterator();
//		while (iter.hasNext()) {
//			Group group = (Group) iter.next();
//			GroupTreeNode node = new GroupTreeNode(group,iwac);
//			list.add(node);
//		}
//
//		return list;
//	}	
	
	public static IWBundle getBundle() {
		if (bundle == null) {
			setupBundle();
		}
		return bundle;
	}

	private static void setupBundle() {
		FacesContext context = FacesContext.getCurrentInstance();
		IWContext iwContext = IWContext.getIWContext(context);
		bundle = iwContext.getIWMainApplication().getBundle(GroupConstants.BUNDLE_IDENTIFIER);
	}
}
