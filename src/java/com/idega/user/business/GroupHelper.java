package com.idega.user.business;

import java.util.Collection;
import java.util.List;

import com.idega.business.SpringBeanName;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;

@SpringBeanName("groupHelper")
public interface GroupHelper {

	public List<Group> getFilteredChildGroups(IWContext iwc, Group parent, String groupTypes, String groupRoles, String splitter);
	
	public List<Group> getFilteredChildGroups(IWContext iwc, int parentGroupId, String groupTypes, String groupRoles, String splitter);
	
	public Collection<Group> getFilteredGroups(Collection<Group> groups, List<String> types, boolean useChildrenAsTopNodes);
	
	public Collection<Group> getFilteredGroups(Collection<Group> groups, String typesValue, String splitter, boolean useChildrenAsTopNodes);
	
	public Group getGroup(IWContext iwc, int id);
	
	public Group getGroup(IWContext iwc, String id);
	
	public List<Group> getGroups(IWContext iwc, List<Integer> groupsIds);
	
	public List<User> getSortedUsers(IWContext iwc, SimpleUserPropertiesBean bean);
	
	public Collection<Group> getTopAndParentGroups(Collection topGroups);
	
	public Collection<Group> getTopGroups(IWContext iwc, User user);
	
	public List<GroupNode> getTopGroupsAndDirectChildren();
	
	public List<GroupNode> getTopGroupsAndDirectChildren(User user, IWContext iwc);
	
	public Collection<Group> getTopGroupsFromDomain(IWContext iwc);
	
	public User getUser(IWContext iwc, int id);
	
	public User getUser(IWContext iwc, String id);
	
	public UserBusiness getUserBusiness(IWApplicationContext iwc);
	
	public List<String> getUserGroupsIds(IWContext iwc, User user);
	
	public GroupNode addChildGroupsToNode(GroupNode parentNode, Collection groups, String image);
	
	public List<GroupNode> convertGroupsToGroupNodes(Collection groups, IWContext iwc, boolean isFirstLevel, String imageBaseUri);
	
	public String getGroupImageBaseUri(IWContext iwc);
	
	public String getActionForAddUserView(SimpleUserPropertiesBean bean, String userId);
	
	public String getJavaScriptParameter(String parameter);
	
}