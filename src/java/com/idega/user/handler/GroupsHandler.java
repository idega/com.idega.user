package com.idega.user.handler;

import java.util.List;

import com.idega.business.chooser.helper.GroupsChooserHelper;
import com.idega.core.builder.presentation.ICPropertyHandler;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.user.presentation.group.GroupsChooser;

public class GroupsHandler implements ICPropertyHandler {

	public List getDefaultHandlerTypes() {
		return null;
	}

	public PresentationObject getHandlerObject(String name, String stringValue, IWContext iwc, boolean oldGenerationHandler, String instanceId,
			String method) {
		GroupsChooser chooser = new GroupsChooser(instanceId, method);
		GroupsChooserHelper helper = new GroupsChooserHelper();
		chooser.setBean(helper.getExtractedPropertiesFromString(stringValue));
		return chooser;
	}

	public void onUpdate(String[] values, IWContext iwc) {
	}

}
