package com.idega.user.event;

import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupTreeNode;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */
public class CreateGroupEvent extends IWPresentationEvent {
	public static final int TYPE_DOMAIN = GroupTreeNode.TYPE_DOMAIN;
	public static final int TYPE_GROUP = GroupTreeNode.TYPE_GROUP;

	private static String PRM_NAME = "group_name";
	private static String PRM_DESCRIPTION = "group_description";
	private static String PRM_TYPE = "group_type";
	private static String PRM_PARENT_ID = "group_parent_id";
	private static String PRM_HOME_PAGE = "group_home_page_id";
	private static String PRM_ALIAS_ID = "group_alias_id";

	private static String PRM_COMMIT = "group_commit";
	private static String PRM_CANCEL = "group_cancel";

	private String _groupName = null;
	private String _groupDescription = null;
	private String _groupType = null;
	private String _groupHomePage = null;
	private int _groupParentID = 0;
	private int _groupParentType = 0;
	private String _groupAliasID = null;

	private String _groupCommit = null;
	private String _groupCancel = null;

	public String getName() {
		return _groupName;
	}

	public String getDescription() {
		return _groupDescription;
	}

	public String getGroupType() {
		return _groupType;
	}

	public int getParentID() {
		return _groupParentID;
	}
	
	public int getAliasID() {
		if (_groupAliasID != null && _groupAliasID.length() > 0) {
			return Integer.parseInt(_groupAliasID);
		}
		return -1;
	}
	
	public int getParentType() {
		return _groupParentType;
	}

	public boolean doCommit() {
		return _groupCommit != null;
	}

	public boolean doCancel() {
		return _groupCancel != null;
	}

	public int getHomePageID() {
		if (_groupHomePage != null && _groupHomePage.length() > 0) {
			return Integer.parseInt(_groupHomePage);
		}
		return -1;
	}

	/**
	 * @return Name of the InterfaceObject handling the group's name
	 */
	public String getIONameForName() {
		return PRM_NAME;
	}

	/**
	 * @return Name of the InterfaceObject handling the group's description
	 */
	public String getIONameForDescription() {
		return PRM_DESCRIPTION;
	}
	
	public String getIONameForGroupType() {
		return PRM_TYPE;
	}
	
	public String getIONameForParentID() {
		return PRM_PARENT_ID;
	}
	
	public String getIONameForCommit() {
		return PRM_COMMIT;
	}
	
	public String getIONameForCancel() {
		return PRM_CANCEL;
	}
	
	public String getIONameForHomePage() {
		return PRM_HOME_PAGE;
	}
	
	public String getIONameForAliasID() {
		return PRM_ALIAS_ID;
	}

	public boolean initializeEvent(IWContext iwc) {
		_groupName = iwc.getParameter(getIONameForName());
		_groupDescription = iwc.getParameter(getIONameForDescription());
		_groupType = iwc.getParameter(getIONameForGroupType());
		_groupHomePage = iwc.getParameter(getIONameForHomePage());
		String groupParentTypeAndID = iwc.getParameter(getIONameForParentID());
		String aliasID = iwc.getParameter(getIONameForAliasID());
		if (aliasID != null && !aliasID.equals("")) {
			try {
				int index = aliasID.indexOf("_");
				_groupAliasID = aliasID.substring(index+1);	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		if(groupParentTypeAndID != null && !groupParentTypeAndID.equals("")) {

			try {
				int indexOf = groupParentTypeAndID.indexOf("_");
				_groupParentType = Integer.parseInt(groupParentTypeAndID.substring(0, indexOf));
				_groupParentID = Integer.parseInt(groupParentTypeAndID.substring(indexOf + 1));
			}
			catch (Exception ex) {
				System.err.println("[" + this +"]: > ");
				System.err.println(ex + ": " + ex.getMessage());
				return false;
			}
		}
		else {
			_groupCommit = null; 
			_groupCancel = null; 
			return false;
		}


		_groupCommit = iwc.getParameter(getIONameForCommit());
		_groupCancel = iwc.getParameter(getIONameForCancel());

		if (_groupCommit == null) {
			_groupCommit = iwc.getParameter(getIONameForCommit() + ".x");
		}

		if (_groupCancel == null) {
			_groupCancel = iwc.getParameter(getIONameForCancel() + ".x");
		}

		return true;
	}
}