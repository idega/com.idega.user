package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.user.bean.PropertiesBean;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class UserGroups extends GroupsChooserBlock {

	@Autowired
	private JQuery jQuery;
	
	@Autowired
	private Web2Business web2;
	
	@Override
	public void main(IWContext iwc) {
		ELUtil.getInstance().autowire(this);
		
		IWBundle iwb = getBundle(iwc);
		IWResourceBundle iwrb = iwb.getResourceBundle(iwc);
		
		List<String> css = new ArrayList<String>();
		css.add(iwb.getVirtualPathWithFileNameString("style/user.css"));
		css.add(iwb.getVirtualPathWithFileNameString("style/user_groups.css"));
		css.add(web2.getBundleURIToFancyBoxStyleFile());
		PresentationUtil.addStyleSheetsToHeader(iwc, css);
		
		List<String> js = new ArrayList<String>();
		js.addAll(Arrays.asList(
				CoreConstants.DWR_ENGINE_SCRIPT,
				CoreConstants.DWR_UTIL_SCRIPT,
				"/dwr/interface/GroupService.js",
				jQuery.getBundleURIToJQueryLib(),
				jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.SCROLL_TO),
				iwb.getVirtualPathWithFileNameString("javascript/UserGroups.js")
		));
		js.addAll(web2.getBundleURIsToFancyBoxScriptFiles());
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, js);
		
		Layer main = new Layer();
		add(main);
		initializeGroupsContainerId(main);
		main.add(new Heading3(iwrb.getLocalizedString("my_groups_list", "The list of my groups:")));
		Layer usersContainer = new Layer();
		usersContainer.setStyleClass("userGroupMembers");
		
		PropertiesBean bean = new PropertiesBean();
		bean.setRemoteMode(true);
		
		//	TODO
		bean.setServer("http://felix.is");
		bean.setLogin("martha");
		bean.setPassword("060455");
		
		setPropertiesBean(bean);
		
		setNodeOnClickAction("function() {UserGroups.getRenderedGroup('" + iwrb.getLocalizedString("loading", "Loading...") + "', this.id, '" + usersContainer.getId() +
				"', jQuery(this).text());}");
		
		main.add(getGroupsTree(iwrb, iwc));
		
		main.add(usersContainer);
		
		addJavaScript(iwc);
	}
	
}