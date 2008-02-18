package com.idega.user.app;

import java.util.ArrayList;
import java.util.Collection;
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
		
		List<User> users = helper.getSortedUsers(iwc, bean);
		if (users == null) {
			return valuesContainer;
		}
		
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
		
		String unknown = getResourceBundle(iwc).getLocalizedString("unknown", "Unknown");
		String name = null;
		String personalId = null;
		String userId = null;
		for (int i = 0; i < users.size(); i++) {
			user = users.get(i);
			
			userId = user.getId();
			name = user.getName();
			if (CoreConstants.EMPTY.equals(name)) {
				name = null;
			}
			personalId = user.getPersonalID();
			if (CoreConstants.EMPTY.equals(personalId)) {
				personalId = null;
			}
			
			lineContainer = new Layer();
			lineContainer.setStyleClass(userValuesLineContainerStyleClass);
			valuesContainer.add(lineContainer);
			
			nameContainer = new Layer();
			nameContainer.setStyleClass(nameContainerStyleClass);
			nameContainer.add(new Text(name == null ? unknown : name));
			lineContainer.add(nameContainer);
			
			personalIdContainer = new Layer();
			personalIdContainer.setStyleClass(personalIdContainerStyleClass);
			personalIdContainer.add(new Text(personalId == null ? unknown : personalId));
			lineContainer.add(personalIdContainer);
			
			changeUserContainer = new Layer();
			changeUserContainer.setStyleClass(changeUserContainerStyleClass);
			changeUserImage = new Image(image);
			changeUserImage.setStyleClass(changeUserImageStyleClass);
			
			changeUserImage.setOnClick(getActionForAddUserView(bean, userId));
			changeUserContainer.add(changeUserImage);
			lineContainer.add(changeUserContainer);
			
			removeUserContainer = new Layer();
			removeUserContainer.setStyleClass(removeUserContainerStyleClass);
			removeUserCheckbox = new CheckBox();
			checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(userId);
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupId()).append("', ");
			checkBoxAction.append(getJavaScriptParameter(removeUserCheckbox.getId())).append(");");
			removeUserCheckbox.setOnClick(checkBoxAction.toString());
			removeUserContainer.add(removeUserCheckbox);
			lineContainer.add(removeUserContainer);
		}
		
		return valuesContainer;
	}
	
	public Layer getSelectedGroupsByIds(IWContext iwc, User user, GroupHelperBusinessBean helper, List<Integer> groupsIds, List<String> ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		List<Group> groups = null;
		if (groupsIds == null || groupsIds.size() == 0) {
			boolean changedToCurrentUser = false;
			if (user == null) {
				user = iwc.getCurrentUser();
				changedToCurrentUser = true;
			}
			Collection<Group> topGroups = helper.getTopGroups(iwc, user);
			if (changedToCurrentUser) {
				user = null;
			}
			
			if (topGroups == null || topGroups.size() == 0) {
				addLabelForNoGroups(iwc, selectedGroups);
				return selectedGroups;
			}
			
			groups = new ArrayList<Group>(topGroups);
		}
		else {
			groups = helper.getGroups(iwc, groupsIds);
		}
		
		return getSelectedGroups(iwc, user, helper, groups, ids, selectedGroupId);
	}
	
	public Layer getSelectedGroups(IWContext iwc, User user, GroupHelperBusinessBean helper, List<Group> groups, List<String> ids, String selectedGroupId) {
		Layer selectedGroups = new Layer();
		
		if (groups == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		if (groups.size() == 0) {
			boolean changedToCurrentUser = false;
			if (user == null) {
				user = iwc.getCurrentUser();
				changedToCurrentUser = true;
			}
			Collection<Group> topGroups = helper.getTopGroups(iwc, user);
			if (changedToCurrentUser) {
				user = null;
			}
			
			if (topGroups == null || topGroups.size() == 0) {
				addLabelForNoGroups(iwc, selectedGroups);
				return selectedGroups;
			}
			
			groups = new ArrayList<Group>(topGroups);
		}
		
		if (ids == null) {
			addLabelForNoGroups(iwc, selectedGroups);
			return selectedGroups;
		}
		
		List<String> userGroups = helper.getUserGroupsIds(iwc, user);
		
		Group group = null;
		String groupId = null;
		StringBuffer action = null;
		boolean checkGroup = false;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			
			checkGroup = false;
			//	Layer
			Layer selectedGroup = new Layer();
			selectedGroups.add(selectedGroup);
				
			//	Checkbox
			groupId = group.getId() == null ? CoreConstants.EMPTY : group.getId();
			CheckBox selectGroup = new CheckBox(group.getName(), groupId);
			if ("-1".equals(selectedGroupId) && userGroups.size() == 0 && i == 0) {
				checkGroup = true;
			}
			else {
				if (groupId.equals(selectedGroupId) || userGroups.contains(groupId)) {
					checkGroup = true;
				}
			}
			if (checkGroup) {
				selectGroup.setChecked(true, true);
			}
			action = new StringBuffer("deselectUserFromGroup(").append(getJavaScriptParameter(groupId)).append(");");
			selectGroup.setOnClick(action.toString());
			ids.add(selectGroup.getId());
			selectedGroup.add(selectGroup);
			
			//	Label
			selectedGroup.add(new Text(group.getName() == null ? CoreConstants.EMPTY : group.getName()));
		}
		
		return selectedGroups;
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		return iwc.getApplicationContext().getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
	}
	
	private void addLabelForNoGroups(IWContext iwc, Layer container) {
		IWResourceBundle iwrb = null;
		String text = "There are no groups available";
		try {
			iwrb = getResourceBundle(iwc);
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
	
	protected String getActionForAddUserView(SimpleUserPropertiesBean bean, String userId) {
		StringBuffer action = new StringBuffer("addUserPresentationObject('").append(bean.getInstanceId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getContainerId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getParentGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getMessage()).append("', ");
		action.append(getJavaScriptParameter(bean.getDefaultGroupId())).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(userId)).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(bean.getGroupTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getRoleTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isGetParentGroupsFromTopNodes());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getGroupTypesForParentGroups()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isUseChildrenOfTopNodesAsParentGroups());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isAllFieldsEditable());
		action.append(");");
		return action.toString();
	}
	
	protected String getJavaScriptFunctionParameter(List<String> parameters) {
		if (parameters == null || parameters.size() == 0) {
			return "null";
		}
		
		StringBuffer params = new StringBuffer("[");
	
		for (int i = 0; i < parameters.size(); i++) {
			params.append(getJavaScriptParameter(parameters.get(i)));
			if (i + 1 < parameters.size()) {
				params.append(SimpleUserApp.COMMA_SEPARATOR);
			}
		}
	
		params.append("]");
		return params.toString();
	}
	
}
