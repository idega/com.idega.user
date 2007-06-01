package com.idega.user.presentation.group;

import com.idega.bean.PropertiesBean;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.user.business.UserConstants;

/**
 * 
 * @author <a href="valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/06/01 15:30:08 $ by $Author: valdas $
 *
 */
public class GroupsChooser extends AbstractChooser {
	
	private PropertiesBean bean = null;
	
	public GroupsChooser(String instanceId, String method) {
		super(false);
		addForm(false);
		setInstanceId(instanceId);
		setMethod(method);
	}
	
	public PresentationObject getChooser(IWContext iwc, IWBundle bundle) {
		Layer chooser = new Layer();
		
		GroupsChooserBlock groupsChooser = new GroupsChooserBlock();
		groupsChooser.setAddExtraJavaScript(false);
		groupsChooser.setExecuteScriptOnLoad(false);
		groupsChooser.setPropertiesBean(bean);
		
		chooser.add(groupsChooser);
		
		return chooser;
	}
	
	public Class getChooserWindowClass() {
		return GroupsChooserBlock.class;
	}
	
	public String getBundleIdentifier()	{
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public void setBean(PropertiesBean bean) {
		this.bean = bean;
	}
	
}
