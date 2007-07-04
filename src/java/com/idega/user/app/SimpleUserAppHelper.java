package com.idega.user.app;

import java.util.List;

import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelperBusinessBean;
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
		Layer lineContainer = null;
		Layer nameContainer = null;
		Layer personalIdContainer = null;
		Layer changeUserContainer = null;
		Layer removeUserContainer = null;
		Image changeUserImage = null;
		CheckBox removeUserCheckbox = null;
		StringBuffer checkBoxAction = null;
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
				changeUserContainer.add(changeUserImage);
				lineContainer.add(changeUserContainer);
				
				removeUserContainer = new Layer();
				removeUserContainer.setStyleClass(removeUserContainerStyleClass);
				removeUserCheckbox = new CheckBox();
				checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
				checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(user.getId());
				checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupId()).append("', this.checked);");
				removeUserCheckbox.setOnClick(checkBoxAction.toString());
				removeUserContainer.add(removeUserCheckbox);
				lineContainer.add(removeUserContainer);
			}
		}
		
		return valuesContainer;
	}

}
