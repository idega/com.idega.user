package com.idega.user.presentation;

import com.idega.core.builder.business.ICBuilderConstants;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.helpers.UserHelper;

public class GroupChooserBlock extends Block {
	
	private static final String CONTAINER_ID = "chooser_presentation_object";
	
	public void main(IWContext iwc) {
		Layer container = new Layer();
		container.setId(CONTAINER_ID);
		this.add(container);
		
		container.add(getGroupTree(iwc));
	}
	
	private PresentationObject getGroupTree(IWContext iwc) {
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
		link.setURL(new StringBuffer("#").append(CONTAINER_ID).toString());
		link.setNoTextObject(true);
		//viewer.setLinkOpenClosePrototype(link);
		
		StringBuffer action = new StringBuffer();
		//	Action to remove old value
		action.append("removeAdvancedProperty('").append(ICBuilderConstants.GROUP_ID_ATTRIBUTE).append("');");
		
		//	Action to add new Value
		action.append("chooseObjectWithHidden(this, '").append(ICBuilderConstants.GROUP_ID_ATTRIBUTE);
		action.append("', '").append(ICBuilderConstants.GROUP_NAME_ATTRIBUTE).append("', '").append(CreateGroupWindow.SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY).append("');");
		
		// Action to set view
		action.append("setChooserView(this, '").append(ICBuilderConstants.GROUP_NAME_ATTRIBUTE).append("');");
		
		link.setOnClick(action.toString());
		viewer.setLinkPrototype(link);
		
		container.add(viewer);
		
		return container;
	}

}
