package com.idega.user.presentation.group;

import com.idega.bean.PropertiesBean;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.util.CoreConstants;

/**
 * 
 * @author <a href="valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.5 $
 *
 * Last modified: $Date: 2007/06/15 13:19:27 $ by $Author: civilis $
 *
 */
public class GroupsChooser extends AbstractChooser {
		
	private PropertiesBean bean = null;
	
	public GroupsChooser(String instanceId, String method, String actionAfterPropertySaved) {
		super(false, false, actionAfterPropertySaved);
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
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}

	public void setBean(PropertiesBean bean) {
		this.bean = bean;
	}
	
	@Override
	protected String getChooserHelperVarName() {
		  return "groups_chooser_helper";
	}
}
