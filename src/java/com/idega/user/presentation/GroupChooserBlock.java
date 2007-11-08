package com.idega.user.presentation;

import com.idega.core.builder.business.ICBuilderConstants;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.util.AbstractChooserBlock;
import com.idega.user.helpers.UserHelper;

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
		
		container.add(getGroupTree(iwc, container.getId()));
	}
	
	private PresentationObject getGroupTree(IWContext iwc, String id) {
		Layer container = new Layer();
		
		IWResourceBundle iwrb = this.getResourceBundle(iwc);
		setName(iwrb.getLocalizedString("select_group", "Select group"));		
		
		Text text = new Text(new StringBuffer(iwrb.getLocalizedString("select_group", "Select group")).append(":").toString());
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		
		UserHelper helper = new UserHelper();
		GroupTreeView viewer = helper.getGroupTree(iwc);
		
		viewer.setDefaultOpenLevel(Integer.MAX_VALUE);
		
		viewer.setAddGroupNameAttribute(true);
		viewer.setAddIdAttribute(true);
		
		Link link = new Link();
		link = new Link();
		link.setURL("javascript:void(0)");
		link.setNoTextObject(true);
		
		StringBuffer action = new StringBuffer();
		//	Action to remove old value
		action.append(getRemoveSelectedPropertyAction());
		
		//	Action to add new Value
		action.append(getChooserObjectAction(false));
		
		// Action to set view
		action.append(getChooserViewAction());
		
		link.setOnClick(action.toString());
		viewer.setLinkPrototype(link);
		
		container.add(viewer);
		
		return container;
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