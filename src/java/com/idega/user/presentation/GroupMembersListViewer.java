package com.idega.user.presentation;

import java.util.List;
import java.util.Random;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserConstants;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.expression.ELUtil;

public class GroupMembersListViewer extends Block {
	
	private Random idGenerator = new Random();
	private String idPrefix = "id";
	
	private SimpleUserPropertiesBean bean = null;
	private String image = null;
	private boolean checkIds = true;
	
	@Override
	public void main(IWContext iwc) {
		if (bean == null || image == null) {
			return;
		}
		GroupHelper helper = ELUtil.getInstance().getBean(GroupHelper.class);
		if (helper == null) {
			return;
		}
		
		List<User> users = helper.getSortedUsers(iwc, bean);
		if (users == null) {
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer container = new Layer();
		fixId(container);
		add(container);
		
		User user = null;
		String userValuesLineContainerStyleClass = "userValuesLineContainerStyleClass";
		String nameContainerStyleClass = "userNameValueContainerStyleClass";
		String personalIdContainerStyleClass = "userPersonalIdValueContainerStyleClass";
		String changeUserContainerStyleClass = "changeUserImageContainerStyleClass";
		String removeUserContainerStyleClass = "removeUserCheckboxContainerStyleClass";
		String changeUserImageStyleClass = "changeUserImageStyleClass";
		StringBuffer checkBoxAction = null;
		
		String odd = "odd";
		String even = "even";
		String unknown = iwrb.getLocalizedString("unknown", "Unknown");
		String name = null;
		String personalId = null;
		String userId = null;
		int groupId = -1;
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
			groupId = bean.getGroupId();
			if (groupId < 0) {
				groupId = bean.getParentGroupId();
			}
			
			Layer lineContainer = new Layer();
			fixId(lineContainer);
			lineContainer.setStyleClass(userValuesLineContainerStyleClass);
			if (i % 2 == 0) {
				lineContainer.setStyleClass(even);
			}
			else {
				lineContainer.setStyleClass(odd);
			}
			container.add(lineContainer);
			
			Layer nameContainer = new Layer();
			fixId(nameContainer);
			nameContainer.setStyleClass(nameContainerStyleClass);
			Text nameText = new Text(name == null ? unknown : name);
			fixId(nameText);
			nameContainer.add(nameText);
			lineContainer.add(nameContainer);
			
			Layer personalIdContainer = new Layer();
			fixId(personalIdContainer);
			personalIdContainer.setStyleClass(personalIdContainerStyleClass);
			Text personalIdText = new Text(personalId == null ? unknown : personalId);
			fixId(personalIdText);
			personalIdContainer.add(personalIdText);
			lineContainer.add(personalIdContainer);
			
			Layer changeUserContainer = new Layer();
			fixId(changeUserContainer);
			changeUserContainer.setStyleClass(changeUserContainerStyleClass);
			Image changeUserImage = new Image(image);
			fixId(changeUserImage);
			changeUserImage.setStyleClass(changeUserImageStyleClass);
			changeUserImage.setToolTip(iwrb.getLocalizedString("change_user", "Change user"));
			changeUserImage.setOnClick(helper.getActionForAddUserView(bean, userId));
			changeUserContainer.add(changeUserImage);
			lineContainer.add(changeUserContainer);
			
			Layer removeUserContainer = new Layer();
			fixId(removeUserContainer);
			removeUserContainer.setStyleClass(removeUserContainerStyleClass);
			CheckBox removeUserCheckbox = new CheckBox();
			removeUserCheckbox.setToolTip(iwrb.getLocalizedString("remove_user", "Remove user"));
			fixId(removeUserCheckbox);
			checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(userId);
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(groupId).append("', ");
			checkBoxAction.append(helper.getJavaScriptParameter(removeUserCheckbox.getId())).append(");");
			removeUserCheckbox.setOnClick(checkBoxAction.toString());
			removeUserContainer.add(removeUserCheckbox);
			lineContainer.add(removeUserContainer);
		}
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	private void fixId(PresentationObject component) {
		if (!checkIds) {
			return;
		}
		
		String id = component.getId();
		
		boolean changeId = false;
		while (findComponent(id) != null) {
			id = new StringBuilder(idPrefix).append(idGenerator.nextInt(Integer.MAX_VALUE)).toString();
			changeId = true;
		}
		
		if (changeId) {
			component.setId(id);
		}
	}

	public void setBean(SimpleUserPropertiesBean bean) {
		this.bean = bean;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setCheckIds(boolean checkIds) {
		this.checkIds = checkIds;
	}

}
