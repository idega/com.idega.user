package com.idega.user.app;

import java.util.Collection;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.business.GroupHelperBusinessBean;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;

public class SimpleUserApp extends Block {
	
	private GroupHelperBusinessBean groupsHelper = new GroupHelperBusinessBean();
	
	private Collection filteredTopGroups = null;
	
	private Group parentGroup = null;
	private String groupTypes = null;
	
	public void main(IWContext iwc) {
		addFiles(iwc);
		
		Layer container = new Layer();
		add(container);

		//	Upper part - dropdowns and description
		Layer choosersAndDescription = new Layer();
		choosersAndDescription.setStyleClass("choosersAndDescriptionStyleClass");
		container.add(choosersAndDescription);
		
		//	Dropdowns
		Layer choosersContainer = new Layer();
		choosersAndDescription.add(choosersContainer);
		choosersContainer.setStyleClass("userApplicationChoosersContainer");
		addChooserContainer(iwc, choosersContainer);
		
		//	Description
		Layer descriptionContainer = new Layer();
		choosersAndDescription.add(descriptionContainer);
		descriptionContainer.setStyleClass("userApplicationDescriptionContainerStyleClass");
		descriptionContainer.add(new Text(getResourceBundle(iwc).getLocalizedString("user_application_view_users_descripton", "To view users in the groups first select the parent group and then the desired sub group. You can remove a user from a group by checking the checkboxes here down below and by clicking the \"Remove\" button. To add new users to a group click the \"Add Users\" button.")));
		
		//	Spacer
		choosersAndDescription.add(getSpacer());
		
		//	Lower part
		Layer lowerPart = new Layer();
		lowerPart.setStyleClass("userAppLowerPartStyleClass");
		container.add(lowerPart);
		
		//	Members list
		Layer membersList = new Layer();
		lowerPart.add(membersList);
		addMembersList(iwc, membersList);
		
		lowerPart.add(getSpacer());
		
		//	Buttons
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("userApplicationButtonsContainerStyleClass");
		addButtons(iwc, buttons);
		
		container.add(getSpacer());
	}
	
	private void addButtons(IWContext iwc, Layer container) {
		BackButton back = new BackButton();
		container.add(back);
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		GenericButton removeFromGroup = new GenericButton(iwrb.getLocalizedString("remove_from_group", "Remove from group"));
		container.add(removeFromGroup);
	
		GenericButton addUsers = new GenericButton(iwrb.getLocalizedString("add_users", "Add users"));
		container.add(addUsers);
	}
	
	private void addMembersList(IWContext iwc, Layer container) {
		
	}
	
	private void addChooserContainer(IWContext iwc, Layer choosers) {
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		//	Parent group
		Layer parentGroupLabelContainer = new Layer();
		parentGroupLabelContainer.setStyleClass("parentGroupLabelContainerStyleClass");
		choosers.add(parentGroupLabelContainer);
		parentGroupLabelContainer.add(new Text(iwrb.getLocalizedString("select_parent_group", "Select parent group")));
		Layer parentGroupChooserContainer = new Layer();
		parentGroupChooserContainer.setStyleClass("parentGroupContainerStyleClass");
		choosers.add(parentGroupChooserContainer);
		fillParentGroupChooser(iwc, parentGroupChooserContainer);
		choosers.add(getSpacer());
		
		//	Child groups
		Layer childGroupsLabelContainer = new Layer();
		choosers.add(childGroupsLabelContainer);
		childGroupsLabelContainer.setStyleClass("childGroupsLabelContainerStyleClass");
		childGroupsLabelContainer.add(new Text(iwrb.getLocalizedString("select_sub_group", "Select sub group")));
		Layer childGroupChooserContainer = new Layer();
		childGroupChooserContainer.setStyleClass("childGroupChooserContainerSyleClass");
		choosers.add(childGroupChooserContainer);
		fillChildGroupsChooser(iwc, childGroupChooserContainer);
		choosers.add(getSpacer());
		
		//	Order
		Layer orderByLabelContainer = new Layer();
		choosers.add(orderByLabelContainer);
		orderByLabelContainer.setStyleClass("orderByLabelContainerStyleClass");
		orderByLabelContainer.add(new Text(iwrb.getLocalizedString("order_by", "Order by")));
		Layer orderByChooser = new Layer();
		choosers.add(orderByChooser);
		orderByChooser.setStyleClass("orderByChooserStyleClass");
		DropdownMenu orderByMenu = new DropdownMenu();
		SelectOption byName = new SelectOption(iwrb.getLocalizedString("name", "Name"), 0);
		orderByMenu.addOption(byName);
		SelectOption byId = new SelectOption(iwrb.getLocalizedString("personal_id", "Personlal ID"), 1);
		orderByMenu.addOption(byId);
		orderByChooser.add(orderByMenu);
		
	}
	
	private void fillChildGroupsChooser(IWContext iwc, Layer container) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Collection filteredChildGroups = groupsHelper.getFilteredChildGroups(iwc, filteredTopGroups, groupTypes, ",");
		if (filteredChildGroups.size() == 0) {
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return;
		}
		
		DropdownMenu childGroups = new DropdownMenu(filteredChildGroups);
		container.add(childGroups);
	}
	
	private void fillParentGroupChooser(IWContext iwc, Layer container) {
		filteredTopGroups = null;
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		if (parentGroup == null) {	//	Group is not set as property
			Collection topGroups = groupsHelper.getTopGroups(iwc, iwc.getCurrentUser());
			if (topGroups.size() > 0) {
				filteredTopGroups = groupsHelper.getFilteredGroups(topGroups, groupTypes, ",");
				if (filteredTopGroups.size() > 0) {
					DropdownMenu groupsDropdown = new DropdownMenu(filteredTopGroups);
					container.add(groupsDropdown);
					return;
				}
			}
			//	No groups found for current user
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return;
		}
		else {	//	Group is set as property
			String groupName = parentGroup.getName() == null ? 
					iwrb.getLocalizedString("unknown_group", "Unknown group") : parentGroup.getName();
			container.add(new Text(groupName));
		}
	}
	
	private void addFiles(IWContext iwc) {
		AddResource adder = AddResourceFactory.getInstance(iwc);
	
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		adder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/UserApplicationEngine.js");
		
		adder.addStyleSheet(iwc, AddResource.HEADER_BEGIN, getBundle(iwc).getVirtualPathWithFileNameString("style/user.css"));
	}
	
	public void setGroupTypes(String groupTypes) {
		this.groupTypes = groupTypes;
	}
	
	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}
	
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	private Layer getSpacer() {
		Layer spacer = new Layer();
		spacer.setStyleClass("spacer");
		return spacer;
	}

}
