package com.idega.user.presentation.group;

import com.idega.presentation.ui.AbstractChooser;

/**
 * 
 * @author <a href="justinas@idega.com">Justinas Rakita</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/05/28 09:39:27 $ by $Author: valdas $
 *
 */
public class GroupInfoChooser extends AbstractChooser {
	
	public GroupInfoChooser(String instanceId, String method) {
		super(false);
		setInstanceId(instanceId);
		setMethod(method);
	}
	
	public Class getChooserWindowClass() {
		return GroupInfoChooserBlock.class;
	}
	
}
