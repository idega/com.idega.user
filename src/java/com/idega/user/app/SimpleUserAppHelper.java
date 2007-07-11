package com.idega.user.app;

import java.util.List;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelperBusinessBean;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class SimpleUserAppHelper {
	
	public Layer getMembersList(IWContext iwc, SimpleUserPropertiesBean bean, GroupHelperBusinessBean helper, String image) {
		Layer valuesContainer = new Layer();
		if (iwc == null || bean == null) {
			return valuesContainer;
		}
		if (helper == null) {
			helper = new GroupHelperBusinessBean();
		}
		
		List users = helper.getSortedUsers(iwc, bean);
		if (users == null) {
			return valuesContainer;
		}
		
		Object o = null;
		User user = null;
		String userValuesLineContainerStyleClass = "userValuesLineContainerStyleClass";
		String nameContainerStyleClass = "userNameValueContainerStyleClass";
		String personalIdContainerStyleClass = "userPersonalIdValueContainerStyleClass";
		String changeUserContainerStyleClass = "changeUserImageContainerStyleClass";
		String removeUserContainerStyleClass = "removeUserCheckboxContainerStyleClass";
		String changeUserImageStyleClass = "changeUserImageStyleClass";
		Layer lineContainer = null;
		Layer nameContainer = null;
		Layer personalIdContainer = null;
		Layer changeUserContainer = null;
		Layer removeUserContainer = null;
		Image changeUserImage = null;
		CheckBox removeUserCheckbox = null;
		StringBuffer checkBoxAction = null;
		StringBuffer changeUserAction = null;
		for (int i = 0; i < users.size(); i++) {
			o = users.get(i);
			if (o instanceof User) {
				user = (User) o;
				
				lineContainer = new Layer();
				lineContainer.setStyleClass(userValuesLineContainerStyleClass);
				valuesContainer.add(lineContainer);
				
				nameContainer = new Layer();
				nameContainer.setStyleClass(nameContainerStyleClass);
				nameContainer.add(new Text(user.getName() == null ? CoreConstants.EMPTY : user.getName()));
				lineContainer.add(nameContainer);
				
				personalIdContainer = new Layer();
				personalIdContainer.setStyleClass(personalIdContainerStyleClass);
				personalIdContainer.add(new Text(user.getPersonalID() == null ? CoreConstants.EMPTY : user.getPersonalID()));
				lineContainer.add(personalIdContainer);
				
				changeUserContainer = new Layer();
				changeUserContainer.setStyleClass(changeUserContainerStyleClass);
				changeUserImage = new Image(image);
				changeUserImage.setStyleClass(changeUserImageStyleClass);
				changeUserAction = new StringBuffer("addUserPresentationObject('").append(bean.getInstanceId());
				changeUserAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getContainerId());
				changeUserAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getParentGroupChooserId());
				changeUserAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupChooserId());
				changeUserAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getMessage()).append("', ");
				changeUserAction.append(getJavaScriptParameter(bean.getDefaultGroupId())).append(", '").append(user.getId());
				changeUserAction.append("', ").append(getJavaScriptParameter(bean.getGroupTypes()));
				changeUserAction.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getRoleTypes()));
				changeUserAction.append(");");
				changeUserImage.setOnClick(changeUserAction.toString());
				changeUserContainer.add(changeUserImage);
				lineContainer.add(changeUserContainer);
				
				removeUserContainer = new Layer();
				removeUserContainer.setStyleClass(removeUserContainerStyleClass);
				removeUserCheckbox = new CheckBox();
				checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
				checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(user.getId());
				checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupId()).append("', ");
				checkBoxAction.append(getJavaScriptParameter(removeUserCheckbox.getId())).append(");");
				removeUserCheckbox.setOnClick(checkBoxAction.toString());
				removeUserContainer.add(removeUserCheckbox);
				lineContainer.add(removeUserContainer);
			}
		}
		
		return valuesContainer;
	}
	
	public Layer getSelectedGroupsByIds(IWContext iwc, User user, GroupHelperBusinessBean helper, List groupsIds, List ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		if (groupsIds == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		
		List groups = helper.getGroups(iwc, groupsIds);
		return getSelectedGroups(iwc, user, helper, groups, ids, selectedGroupId);
	}
	
	public Layer getSelectedGroups(IWContext iwc, User user, GroupHelperBusinessBean helper, List groups, List ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		if (groups == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		if (groups.size() == 0) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		
		if (ids == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		
		List userGroups = helper.getUserGroupsIds(iwc, user);
		
		Object o = null;
		Group group = null;
		String groupId = null;
		for (int i = 0; i < groups.size(); i++) {
			o = groups.get(i);
			if (o instanceof Group) {
				group = (Group) o;
				
				//	Layer
				Layer selectedGroup = new Layer();
				selectedGroups.add(selectedGroup);
				
				//	Checkbox
				groupId = group.getId() == null ? CoreConstants.EMPTY : group.getId();
				CheckBox selectGroup = new CheckBox(group.getName(), groupId);
				if (groupId.equals(selectedGroupId) || userGroups.contains(groupId)) {
					selectGroup.setChecked(true, true);
				}
				ids.add(selectGroup.getId());
				selectedGroup.add(selectGroup);
				
				//	Label
				selectedGroup.add(new Text(group.getName() == null ? CoreConstants.EMPTY : group.getName()));
			}
		}
		
		return selectedGroups;
	}
	
	private void addLabelForNoGroups(IWContext iwc, Layer container) {
		IWResourceBundle iwrb = null;
		String text = "There are no groups available";
		try {
			iwrb = iwc.getApplicationContext().getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (iwrb == null) {
			container.add(new Text(text));
		}
		else {
			container.add(new Text(iwrb.getLocalizedString("no_groups_available", text)));
		}
	}
	
	protected String getJavaScriptParameter(String parameter) {
		if (parameter == null) {
			return "null";
		}
		return new StringBuffer("'").append(parameter).append("'").toString();
	}

}
