package com.idega.user.event;

import org.omg.CORBA.PERSIST_STORE;

import com.idega.data.IDOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.user.data.User;

public class MoveGroupEvent extends IWPresentationEvent {
	private static final long serialVersionUID = 3041629810933217897L;

	public static final String OKAY_KEY = "okay_key";
	public static final String CANCEL_KEY = "cancel_key";

	private Integer groupId = null;
	private Integer oldParentGroupId = null;
	private Integer newParentGroupId = null;
	private Integer performer = null;

	public static final String GROUP_ID = "group_id";
	public static final String NEW_PARENT_GROUP_ID = "new_parent_group_id";
	public static final String OLD_PARENT_GROUP_ID = "old_parent_group_id";
	public static final String PERFORMER_ID = "performer_id";
	private boolean okay = false;

	public boolean initializeEvent(IWContext iwc) {
		this.okay = iwc.isParameterSet(OKAY_KEY);

		if (iwc.isParameterSet(GROUP_ID)) {
			this.groupId = new Integer(iwc.getParameter(GROUP_ID));
		}

		if (iwc.isParameterSet(OLD_PARENT_GROUP_ID)) {
			this.oldParentGroupId = new Integer(iwc
					.getParameter(OLD_PARENT_GROUP_ID));
		}

		if (iwc.isParameterSet(NEW_PARENT_GROUP_ID)) {
			String groupID = iwc.getParameter(NEW_PARENT_GROUP_ID);
			this.newParentGroupId = new Integer(groupID.substring(groupID.lastIndexOf("_") + 1));
		}
		
		if (iwc.isParameterSet(PERFORMER_ID)) {
			String performerID = iwc.getParameter(PERFORMER_ID);
			this.performer = new Integer(performerID);
		}
		
		return true;
	}

	public boolean isMoveConfirmed() {
		return this.okay;
	}

	public Group getGroup() {
		if (this.groupId != null && (!new Integer(-1).equals(this.groupId))) {
			try {
				return (Group) IDOLookup.findByPrimaryKey(Group.class,
						this.groupId);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public Group getOldParentGroup() {
		if (this.oldParentGroupId != null
				&& (!new Integer(-1).equals(this.oldParentGroupId))) {
			try {
				return (Group) IDOLookup.findByPrimaryKey(Group.class,
						this.oldParentGroupId);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public Group getNewParentGroup() {
		if (this.newParentGroupId != null
				&& (!new Integer(-1).equals(this.newParentGroupId))) {
			try {
				return (Group) IDOLookup.findByPrimaryKey(Group.class,
						this.newParentGroupId);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public User getPerformer() {
		if (this.performer != null
				&& (!new Integer(-1).equals(this.performer))) {
			try {
				return (User) IDOLookup.findByPrimaryKey(User.class,
						this.performer);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	
	public void setGroupId(Integer primaryKey) {
		this.addParameter(GROUP_ID, primaryKey.toString());
	}

	public void setNewParentGroupId(Integer primaryKey) {
		this.addParameter(NEW_PARENT_GROUP_ID, primaryKey.toString());
	}

	public void setOldParentGroupId(Integer primaryKey) {
		this.addParameter(OLD_PARENT_GROUP_ID, primaryKey.toString());
	}
	
	public void setPerformer(Integer primaryKey) {
		this.addParameter(PERFORMER_ID, primaryKey.toString());
	}

}