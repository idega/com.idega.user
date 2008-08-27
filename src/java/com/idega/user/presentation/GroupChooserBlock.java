package com.idega.user.presentation;

import java.util.Arrays;
import java.util.List;

import com.idega.core.builder.business.ICBuilderConstants;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.util.AbstractChooserBlock;
import com.idega.user.presentation.group.GroupsFilter;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;

public class GroupChooserBlock extends AbstractChooserBlock {
	
	public GroupChooserBlock() {
		super();
	}
	
	public GroupChooserBlock(String idAttribute, String valueAttribute, String hiddenInputAttribute) {
		super(idAttribute, valueAttribute, hiddenInputAttribute);
	}
	
	public void main(IWContext iwc) {
		super.main(iwc);
		
		Layer container = getMainContaier();
		this.add(container);
		
		StringBuffer action = new StringBuffer();
		//	Action to remove old value
		action.append(getRemoveSelectedPropertyAction());
		//	Action to add new Value
		action.append(getChooserObjectAction(false));
		// Action to set view
		action.append(getChooserViewAction());
		
		GroupsFilter filter = new GroupsFilter();
		filter.setOnClickAction(action.toString());
		filter.setUseRadioBox(true);
		List<String> selectedGroups = null;
		String value = getValue();
		if (!StringUtil.isEmpty(value)) {
			String ids[] = value.split(CoreConstants.COMMA);
			selectedGroups = Arrays.asList(ids);
			filter.setSelectedGroups(selectedGroups);
		}
		container.add(filter);
	}
	
	public boolean getChooserAttributes() {
		//	Setting default values
		if (getIdAttribute() == null) {
			setIdAttribute(ICBuilderConstants.GROUP_ID_ATTRIBUTE);
		}
		if (getValueAttribute() == null) {
			setValueAttribute(ICBuilderConstants.GROUP_NAME_ATTRIBUTE);
		}
		
		return true;
	}

}