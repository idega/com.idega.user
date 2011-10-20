package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.core.idgenerator.business.IdGeneratorFactory;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.group.GroupsComparator;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class UserGroupMembership extends Block {

	private IWBundle bundle;
	private IWResourceBundle iwrb;
	
	@Autowired
	private JQuery jQuery;
	
	@Autowired
	private Web2Business web2;
	
	@Override
	public String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}
	
	@Override
	public void main(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		bundle = getBundle(iwc);
		iwrb = bundle.getResourceBundle(iwc);
		
		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
				bundle.getVirtualPathWithFileNameString("style/user.css"),
				web2.getBundleURIToFancyBoxStyleFile()
		));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				jQuery.getBundleURIToJQueryLib(),
				bundle.getVirtualPathWithFileNameString("javascript/UserInfoViewerHelper.js")
		));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, web2.getBundleURIsToFancyBoxScriptFiles());
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("userGroupMembership");
		
		if (!iwc.isLoggedOn()) {
			container.add(new Heading3(iwrb.getLocalizedString("user_not_logged_in", "User is not logged in")));
			return;
		}
		
		User user = iwc.getCurrentUser();
		UserBusiness userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		@SuppressWarnings("unchecked")
		Collection<Group> groups = userBusiness.getUserGroups(user);
		if (ListUtil.isEmpty(groups)) {
			container.add(new Heading3(iwrb.getLocalizedString("user_is_not_member_of_any_group", "User is not a member of any group")));
			return;
		}
		
		List<Group> userGroups = new ArrayList<Group>(groups);
		Locale locale = iwc.getCurrentLocale();
		Collections.sort(userGroups, new GroupsComparator(locale));
		
		BuilderLogic builder = BuilderLogic.getInstance();
		
		Lists list = new Lists();
		container.add(list);
		list.setStyleClass("myGroupsList");
		for (Iterator<Group> groupsIter = userGroups.iterator(); groupsIter.hasNext();) {
			Group group = groupsIter.next();
			String uniqueId = group.getUniqueId();
			if (StringUtil.isEmpty(uniqueId)) {
				uniqueId = IdGeneratorFactory.getUUIDGenerator().generateId();
				group.setUniqueId(uniqueId);
				group.store();
			}
			
			ListItem item = new ListItem();
			Link link = new Link(group.getNodeName(locale));
			link.setURL(builder.getUriToObject(GroupMembers.class, Arrays.asList(new AdvancedProperty(GroupMembers.GROUP_UNIQUE_ID_PARAM, uniqueId))));
			link.setStyleClass("userGroupMembershipSelectedGroup");
			item.add(link);
			list.add(item);
		}
		
		String action = "UserInfoViewerHelper.initialize();";
		if (!CoreUtil.isSingleComponentRenderingProcess(iwc)) {
			action = "jQuery(window).load(function() {"+ action +"});";
		}
		PresentationUtil.addJavaScriptActionToBody(iwc, action);
	}
}