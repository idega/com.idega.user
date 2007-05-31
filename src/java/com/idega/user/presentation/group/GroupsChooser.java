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
 * @author <a href="justinas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/05/31 13:52:14 $ by $Author: valdas $
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
	
	/*private List<String> getSelectedUniqueIds(IWContext iwc) {
		BuilderLogic builder = BuilderLogic.getInstance();
		
		String pageKey = builder.getCurrentIBPage(iwc);
		if (pageKey == null) {
			return null;
		}
		
		String value = builder.getProperty(pageKey, getInstanceId(), getMethod());
		if (value == null) {
			return null;
		}
		String[] values = value.split(",");
		if (values == null) {
			return null;
		}
		
		List<String> uniqueIds = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			uniqueIds.add(values[i]);
		}
		
		return uniqueIds;
	}*/
	
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
