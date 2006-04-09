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
		return this._groupName;
	}

	public String getDescription() {
		return this._groupDescription;
	}

	public String getGroupType() {
		return this._groupType;
	}

	public int getParentID() {
		return this._groupParentID;
	}
	
	public int getAliasID() {
		if (this._groupAliasID != null && this._groupAliasID.length() > 0) {
			return Integer.parseInt(this._groupAliasID);
		}
		return -1;
	}
	
	public int getParentType() {
		return this._groupParentType;
	}

	public boolean doCommit() {
		return this._groupCommit != null;
	}

	public boolean doCancel() {
		return this._groupCancel != null;
	}

	public int getHomePageID() {
		if (this._groupHomePage != null && this._groupHomePage.length() > 0) {
			return Integer.parseInt(this._groupHomePage);
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
		this._groupName = iwc.getParameter(getIONameForName());
		this._groupDescription = iwc.getParameter(getIONameForDescription());
		this._groupType = iwc.getParameter(getIONameForGroupType());
		this._groupHomePage = iwc.getParameter(getIONameForHomePage());
		String groupParentTypeAndID = iwc.getParameter(getIONameForParentID());
		String aliasID = iwc.getParameter(getIONameForAliasID());
		if (aliasID != null && !aliasID.equals("")) {
			try {
				int index = aliasID.indexOf("_");
				this._groupAliasID = aliasID.substring(index+1);	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		if(groupParentTypeAndID != null && !groupParentTypeAndID.equals("")) {

			try {
				int indexOf = groupParentTypeAndID.indexOf("_");
				this._groupParentType = Integer.parseInt(groupParentTypeAndID.substring(0, indexOf));
				this._groupParentID = Integer.parseInt(groupParentTypeAndID.substring(indexOf + 1));
			}
			catch (Exception ex) {
				System.err.println("[" + this +"]: > ");
				System.err.println(ex + ": " + ex.getMessage());
				return false;
			}
		}
		else {
			this._groupCommit = null; 
			this._groupCancel = null; 
			return false;
		}


		this._groupCommit = iwc.getParameter(getIONameForCommit());
		this._groupCancel = iwc.getParameter(getIONameForCancel());

		if (this._groupCommit == null) {
			this._groupCommit = iwc.getParameter(getIONameForCommit() + ".x");
		}

		if (this._groupCancel == null) {
			this._groupCancel = iwc.getParameter(getIONameForCancel() + ".x");
		}

		return true;
	}
}