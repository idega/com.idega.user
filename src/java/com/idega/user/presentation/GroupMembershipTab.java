package com.idega.user.presentation;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.IFrame;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GroupMembershipTab extends UserGroupTab {


  //private Link addLink;
  private IFrame groupMembersFrame;
  private IFrame userMembersFrame;
//  public static final String PARAMETER_GROUP_ID = "ic_group_id";
  public static final String SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED = "ic_group_ic_group_direct_GMT";
  public static final String SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED = "ic_group_ic_group_not_direct_GMT";
  public static final String SESSIONADDRESS_USERS_DIRECTLY_RELATED = "ic_user_ic_group_direct_GMT";
  public static final String SESSIONADDRESS_USERS_NOT_DIRECTLY_RELATED = "ic_user_ic_group_not_direct_GMT";

  protected Text groupMembers;
  protected Text userMembers;

  public GroupMembershipTab() {
    super();
    this.setName("Members");

  }
  public void initFieldContents() {
    //addLink.setWindowToOpen(GroupMembershipTab.UserGroupSetter.class);
    //addLink.addParameter(GroupMembershipTab.PARAMETER_GROUP_ID,this.getUserId());
  }
  public void updateFieldsDisplayStatus() {
    /**@todo: implement this com.idega.user.presentation.UserTab abstract method*/
  }
  public void initializeFields() {
    groupMembersFrame = new IFrame("ic_group_group_members",GroupList.class);
    groupMembersFrame.setHeight(140);
    groupMembersFrame.setWidth(Table.HUNDRED_PERCENT);
    groupMembersFrame.setScrolling(IFrame.SCROLLING_YES);
    groupMembersFrame.setStyleAttribute("border", "1px #b2b2b2 solid");

    userMembersFrame = new IFrame("ic_user_group_members",UserList.class);
    userMembersFrame.setHeight(140);
    userMembersFrame.setWidth(Table.HUNDRED_PERCENT);
    userMembersFrame.setScrolling(IFrame.SCROLLING_YES);
    userMembersFrame.setStyleAttribute("border", "1px #b2b2b2 solid");

    //addLink = new Link("  Add  ");

  }
  public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		groupMembers = new Text(iwrb.getLocalizedString("group.groups", "Groups"));
		groupMembers.setBold();
		
    userMembers = new Text(iwrb.getLocalizedString("group.users", "Users"));
		userMembers.setBold();
  }
  public boolean store(IWContext iwc) {
    return true;
  }
  public void lineUpFields() {
    this.resize(1,2);
    setCellpadding(5);
    setCellspacing(0);

    this.add(groupMembers,1,1);
    this.add(Text.getBreak(), 1, 1);
    this.add(groupMembersFrame,1,1);

    this.add(userMembers,1,2);
    this.add(Text.getBreak(), 1, 2);
    this.add(userMembersFrame,1,2);
  }
  public boolean collect(IWContext iwc) {
    return true;
  }
  public void initializeFieldNames() {
    /**@todo: implement this com.idega.user.presentation.UserTab abstract method*/
  }
  public void initializeFieldValues() {
    /**@todo: implement this com.idega.user.presentation.UserTab abstract method*/
  }

  public void dispose(IWContext iwc){
    iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED);
    iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED);
  }

  public void main(IWContext iwc) throws Exception {
    Object obj = this.getGroupBusiness(iwc).getChildGroups(this.getGroupId());
    if(obj != null){
      iwc.setSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED,obj);
    }else{
      iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED);
    }

    Object ob = getGroupBusiness(iwc).getChildGroupsInDirect(this.getGroupId());
    if(ob != null){
      iwc.setSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED,ob);
    }else{
      iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED);
    }

    Object obju = getGroupBusiness(iwc).getUsersDirectlyRelated(this.getGroupId());
    if(obju != null){
      iwc.setSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_DIRECTLY_RELATED,obju);
    }else{
      iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_DIRECTLY_RELATED);
    }


    /**
     * @todo check
     */
    Object obu = getGroupBusiness(iwc).getUsersNotDirectlyRelated(this.getGroupId());
    if(obu != null){
      iwc.setSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_NOT_DIRECTLY_RELATED,obu);
    }else{
      iwc.removeSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_NOT_DIRECTLY_RELATED);
    }
  }
}