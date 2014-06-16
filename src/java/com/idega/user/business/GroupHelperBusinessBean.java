package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.data.IDORelationshipException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractTreeViewer;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.GenericUserComparator;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public class GroupHelperBusinessBean implements GroupHelper {

	private UserBusiness userBusiness = null;
	private GroupBusiness groupBusiness = null;

	@Override
	public List<GroupNode> getTopGroupsAndDirectChildren() {
		List<GroupNode> fake = new ArrayList<GroupNode>();

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return fake;
		}

		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(Exception e) {
			return fake;
		}

		return getTopGroupsAndDirectChildren(currentUser, iwc);
	}

	@Override
	public List<GroupNode> getTopGroupsAndDirectChildren(User user, IWContext iwc) {
		return getTopGroupsAndDirectChildren(user, iwc, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<GroupNode> getTopGroupsAndDirectChildren(User user, IWContext iwc, boolean basicInformationOnly) {
		List<GroupNode> fake = new ArrayList<GroupNode>();

		if (user == null || iwc == null) {
			return fake;
		}

		userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return fake;
		}

		try {
			return convertGroupsToGroupNodes(userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc), iwc, true, getGroupImageBaseUri(iwc),
					basicInformationOnly);
		} catch (RemoteException e) {
			e.printStackTrace();
			return fake;
		}
	}

	@Override
	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBusiness == null) {
			try {
				userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}

	@Override
	public List<GroupNode> convertGroupsToGroupNodes(Collection<Group> groups, IWContext iwc, boolean isFirstLevel, String imageBaseUri) {
		return convertGroupsToGroupNodes(groups, iwc, isFirstLevel, imageBaseUri, false);
	}

	private List<GroupNode> convertGroupsToGroupNodes(Collection<Group> groups, IWContext iwc, boolean isFirstLevel, String imageBaseUri,
			boolean basicInformationOnly) {
		List <GroupNode> list = new ArrayList<GroupNode>();
		if (groups == null || iwc == null) {
			return list;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);

		GroupNode groupNode = null;
		for (Group group: groups) {
			groupNode = createGroupNodeFromGroup(group, imageBaseUri, false, basicInformationOnly);
			if (groupNode != null) {
				if (isFirstLevel) {
					if (groupBusiness != null) {
						try {
							groupNode.setChildren(convertGroupsToGroupNodes(groupBusiness.getChildGroups(group), iwc, false, imageBaseUri, basicInformationOnly));
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}

				list.add(groupNode);
			}
		}

		return list;
	}

	private GroupNode createGroupNodeFromGroup(Group group, String imageBaseUri, boolean nodeIsOpened, boolean basicInformationOnly) {
		if (group == null) {
			return null;
		}

		String uniqueId = group.getUniqueId();
		if (uniqueId == null) {
			return null;
		}

		GroupNode node = new GroupNode();
		node.setUniqueId(uniqueId);
		node.setName(group.getName());
		node.setId(group.getId());
		node.setHasChildren(group.getChildCount() > 0);

		if (!basicInformationOnly) {
			node.setImage(getGroupIcon(group, imageBaseUri, nodeIsOpened));
		}

		return node;
	}

	@Override
	public String getGroupIcon(Group group, String imageBaseUri, boolean nodeIsOpened) {
		StringBuffer imageUri = new StringBuffer(imageBaseUri);
		if (!imageBaseUri.endsWith(CoreConstants.SLASH)) {
			imageUri.append(CoreConstants.SLASH);
		}
		imageUri.append(group.getGroupType());
		String imageEnd = "_node_closed.gif";
		if (nodeIsOpened) {
			imageEnd = "_node_open.gif";
		}
		imageUri.append(imageEnd);
		return imageUri.toString();
	}

	@Override
	public GroupNode addChildGroupsToNode(GroupNode parentNode, Collection<Group> groups, String image) {
		if (groups == null) {
			parentNode.setHasChildren(false);
			return parentNode;
		}
		if (groups.size() == 0) {
			return parentNode;
		}

		List<GroupNode> children = new ArrayList<GroupNode>();
		Group group = null;
		GroupNode childNode = null;
		for (Iterator<Group> it = groups.iterator(); it.hasNext();) {
			group = it.next();
			childNode = createGroupNodeFromGroup(group, image, false, false);
			children.add(childNode);
		}

		if (children.size() > 0) {
			parentNode.setHasChildren(true);
			parentNode.setChildren(children);
		}

		return parentNode;
	}

	private List<Group> getChilrenfOfGroups(Collection<Group> groups) {
		List<Group> children = new ArrayList<Group>();
		if (groups == null) {
			return null;
		}

		Group group = null;
		for (Iterator<Group> it = groups.iterator(); it.hasNext();) {
			group = it.next();
			children.addAll(group.getChildGroups());
		}

		return children;
	}

	/**
	 * Returns groups filtered by type(s)
	 * @param groups - list of Group objects
	 * @param types - list of String (group type value)
	 * @param useChildrenAsTopNodes - returns filtered children of provided groups
	 * @return
	 */
	@Override
	public Collection<Group> getFilteredGroups(Collection<Group> groups, List<String> types, boolean useChildrenAsTopNodes) {
		Collection<Group> filtered = new ArrayList<Group>();
		if (ListUtil.isEmpty(groups)) {
			return filtered;
		}

		if (ListUtil.isEmpty(types)) {
			if (useChildrenAsTopNodes) {
				return getChilrenfOfGroups(groups);
			}
 			return groups;
		}

		if (useChildrenAsTopNodes) {
			return getFilteredGroups(getChilrenfOfGroups(groups), types);
		}
		return getFilteredGroups(groups, types);
	}

	private Collection<Group> getFilteredGroups(Collection<Group> groups, List<String> types) {
		Collection<Group> filtered = new ArrayList<Group>();
		Group group = null;
		for (Iterator<Group> it = groups.iterator(); it.hasNext();) {
			group = it.next();
			if (group.getGroupType() != null) {
				if (types.contains(group.getGroupType())) {
					filtered.add(group);
				}
			}
		}

		return filtered;
	}

	/**
	 * Returns groups filtered by type(s)
	 * @param groups - list of Group objects
	 * @param typesValue - group type value(s)
	 * @param splitter - typesValue separator
	 * @return
	 */
	@Override
	public Collection<Group> getFilteredGroups(IWContext iwc, Collection<Group> groups, String typesValue, String splitter, boolean useChildrenAsTopNodes) {
		return getSortedGroups(getFilteredGroups(groups, getExtractedTypesList(typesValue, splitter), useChildrenAsTopNodes), iwc);
	}

	private GroupBusiness getGroupBusiness(IWApplicationContext iwac) {
		if (groupBusiness == null) {
			try {
				groupBusiness = IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}

	@Override
	public List<Group> getFilteredChildGroups(IWContext iwc, int parentGroupId, String groupTypes, String groupRoles, String splitter) {
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}
		Group parent = null;
		try {
			parent = groupBusiness.getGroupByGroupID(parentGroupId);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		return getFilteredChildGroups(iwc, parent, groupTypes, groupRoles, splitter);
	}

	@Override
	public List<Group> getFilteredChildGroups(IWContext iwc, Group parent, String groupTypes, String groupRoles, String splitter) {
		List<Group> filtered = new ArrayList<Group>();
		if (parent == null) {
			return filtered;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return filtered;
		}

		List<String> types = getExtractedTypesList(groupTypes, splitter);
		try {
			filtered.addAll(groupBusiness.getChildGroupsRecursiveResultFiltered(parent, types, true));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		Group group = null;
		String parentUniqueId = parent.getUniqueId();
		List<Group> checkedFiltered = new ArrayList<Group>();
		for (int i = 0; i < filtered.size(); i++) {
			group = filtered.get(i);
			if (group == null) {
				continue;
			}

			String uniqueId = group.getUniqueId();
			if (StringUtil.isEmpty(uniqueId)) {
				checkedFiltered.add(group);
			} else if (!StringUtil.isEmpty(parentUniqueId) && !parentUniqueId.equals(uniqueId)) {
				checkedFiltered.add(group);
			}
		}

		List<String> roles = getExtractedTypesList(groupRoles, splitter);
		if (roles == null) {
			return checkedFiltered;
		}

		AccessController controler = iwc.getAccessController();
		if (controler == null) {
			return checkedFiltered;
		}

		List<Group> filteredByRole = new ArrayList<Group>();
		String roleKey = null;
		group = null;
		Collection<?> allRoles = null;
		for (int i = 0; i < roles.size(); i++) {
			roleKey = roles.get(i).toString();
			for (int j = 0; j < checkedFiltered.size(); j++) {
				group = checkedFiltered.get(j);
				allRoles = controler.getAllRolesForGroup(group);

				//	Group has NO ROLES set or group has role
				if (ListUtil.isEmpty(allRoles) || controler.hasRole(roleKey, group, iwc)) {
					filteredByRole.add(group);
				}
			}

			if (checkedFiltered.size() > 0) {
				//	Removing groups (from basic groups list) that were filtered and added to list
				for (int j = 0; j < filteredByRole.size(); j++) {
					group = filteredByRole.get(j);
					if (checkedFiltered.contains(group)) {
						checkedFiltered.remove(group);
					}
				}
			}
		}

		return filteredByRole;
	}

	private List<String> getExtractedTypesList(String typesValue, String splitter) {
		if (typesValue == null || splitter == null) {
			return null;
		}

		String[] types = typesValue.split(splitter);
		if (types == null) {
			return null;
		}
		if (types.length == 0) {
			return null;
		}

		List<String> typesList = new ArrayList<String>();
		for (int i = 0; i < types.length; i++) {
			typesList.add(types[i].trim());
		}
		return typesList;
	}

	@Override
	public List<User> getUsersInGroup(IWContext iwc, SimpleUserPropertiesBean bean, boolean sort) {
		if (bean == null) {
			return null;
		}

		int groupId = bean.getGroupId();
		if (groupId < 0) {
			groupId = bean.getParentGroupId();
		}
		if (groupId < 0) {
			return null;
		}

		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}

		Collection<User> users = null;
		try {
			users = userBusiness.getUsersInGroup(groupId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(users)) {
			return null;
		}
		if (bean.isJuridicalPerson()) {
			List<User> juridicalUsers = new ArrayList<User>();
			for (User user: users) {
				if (user.isJuridicalPerson()) {
					juridicalUsers.add(user);
				}
			}
			users = juridicalUsers;
		}

		if (sort) {
			return getSortedUsers(users, iwc.getCurrentLocale(), bean);
		}

		return new ArrayList<User>(users);
	}

	@Override
	public List<User> getUsersByIds(IWContext iwc, List<String> usersIds, boolean sort) {
		if (ListUtil.isEmpty(usersIds)) {
			return null;
		}

		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}

		Collection<User> users = null;
		try {
			users = userBusiness.getUsers(ArrayUtil.convertListToArray(usersIds));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(users)) {
			return null;
		}

		if (sort) {
			return getSortedUsers(users, iwc.getCurrentLocale(), false);
		}

		return new ArrayList<User>(users);
	}

	@Override
	public List<User> getSortedUsers(Collection<User> users, Locale locale, SimpleUserPropertiesBean bean) {
		return getSortedUsers(users, locale, bean.getOrderBy() == SimpleUserApp.USER_ORDER_BY_ID);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<User> getSortedUsers(Collection<User> users, Locale locale, boolean sortByPersonalId) {
		if (ListUtil.isEmpty(users)) {
			return null;
		}

		List<User> sortedUsers = new ArrayList<User>(users);
		int comparatorId = sortByPersonalId ? GenericUserComparator.PERSONALID : GenericUserComparator.FIRSTLASTMIDDLE;
		Collections.sort(sortedUsers, new GenericUserComparator(locale, comparatorId));
		return sortedUsers;
	}

	@Override
	public Group getGroup(IWContext iwc, int id) {
		if (id < 0) {
			return null;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}

		try {
			return groupBusiness.getGroupByGroupID(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Group getGroup(IWContext iwc, String id) {
		return getGroup(iwc, getParsedValue(id));
	}

	@Override
	public List<Group> getGroups(IWContext iwc, List<Integer> groupsIds) {
		if (groupsIds == null) {
			return null;
		}

		List<Group> groups = new ArrayList<Group>();
		Group group = null;
		for (int i = 0; i < groupsIds.size(); i++) {
			group = getGroup(iwc, groupsIds.get(i));
			if (group != null) {
				groups.add(group);
			}
		}

		return groups;
	}

	@Override
	public User getUser(IWContext iwc, String id) {
		return getUser(iwc, getParsedValue(id));
	}

	@Override
	public User getUser(IWContext iwc, int id) {
		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return null;
		}

		try {
			return userBusiness.getUser(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	private int getParsedValue(String value) {
		if (value == null) {
			return -1;
		}
		try {
			return Integer.valueOf(value).intValue();
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public List<String> getUserGroupsIds(IWContext iwc, User user) {
		List<String> ids = new ArrayList<String>();
		if (user == null) {
			return ids;
		}

		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return ids;
		}

		Collection<Group> userGroups = null;
		try {
			userGroups = userBusiness.getUserGroups(user);
		} catch (EJBException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (userGroups == null) {
			return ids;
		}

		for (Iterator<Group> it = userGroups.iterator(); it.hasNext();) {
			ids.add(it.next().getId());
		}

		return ids;
	}

	@Override
	public Collection<Group> getTopGroupsFromDomain(IWContext iwc) {
		ICDomain domain = iwc.getDomain();
		if (domain == null) {
			return null;
		}

		Collection<Group> topLevelGroups = null;
		try {
			topLevelGroups = domain.getTopLevelGroupsUnderDomain();
		} catch (IDORelationshipException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}

		if (ListUtil.isEmpty(topLevelGroups)) {
			if (BuilderLogic.getInstance().reloadGroupsInCachedDomain(iwc, iwc.getServerName())) {
				try {
					topLevelGroups = domain.getTopLevelGroupsUnderDomain();
				} catch (IDORelationshipException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (FinderException e) {
					e.printStackTrace();
				}
			}
		}

		return getSortedGroups(topLevelGroups, iwc);
	}

	@SuppressWarnings("unchecked")
	private List<Group> getSortedGroups(Collection<Group> groups, IWContext iwc) {
		if (ListUtil.isEmpty(groups) || iwc == null) {
			return new ArrayList<Group>();
		}

		List<Group> groupsInList = new ArrayList<Group>(groups);
		GroupComparator comparator = new GroupComparator(iwc);
		comparator.setSortByParents(true);
		comparator.setGroupBusiness(getGroupBusiness(iwc));
		Collections.sort(groupsInList, comparator);
		return groupsInList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Group> getTopGroups(IWContext iwc, User user) {
		if (iwc == null || user == null) {
			return new ArrayList<Group>();
		}
		userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return new ArrayList<Group>();
		}

		try {
			return userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return new ArrayList<Group>();
		}
	}

	@Override
	public Collection<Group> getTopAndParentGroups(Collection<Group> topGroups) {
		if (ListUtil.isEmpty(topGroups)) {
			return null;
		}

		Group group = null;
		Collection<Group> topAndParentGroups = new ArrayList<Group>(topGroups);
		List<Group> parentGroups = null;
		for (Iterator<Group> it = topGroups.iterator(); it.hasNext();) {
			group = it.next();
			parentGroups = group.getParentGroups();
			if (parentGroups != null) {
				topAndParentGroups.addAll(parentGroups);
			}
		}

		return getSortedGroups(topAndParentGroups, CoreUtil.getIWContext());
	}

	@Override
	public String getGroupImageBaseUri(IWContext iwc) {
		if (iwc == null) {
			return null;
		}

		IWBundle iwb = null;
		try {
			iwb = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		StringBuffer uri = new StringBuffer(iwb.getResourcesURL()).append(CoreConstants.SLASH).append(AbstractTreeViewer.TREEVIEW_PREFIX);
		uri.append(AbstractTreeViewer._UI_IW).append("group/");

		return uri.toString();
	}

	@Override
	public String getActionForAddUserView(SimpleUserPropertiesBean bean, String userId) {
		StringBuffer action = new StringBuffer("addUserPresentationObject('").append(bean.getInstanceId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getContainerId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getParentGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getGroupChooserId());
		action.append(SimpleUserApp.PARAMS_SEPARATOR).append(bean.getMessage()).append("', ");
		action.append(getJavaScriptParameter(bean.getDefaultGroupId())).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(userId)).append(SimpleUserApp.COMMA_SEPARATOR);
		action.append(getJavaScriptParameter(bean.getGroupTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getRoleTypes()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isGetParentGroupsFromTopNodes());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(getJavaScriptParameter(bean.getGroupTypesForParentGroups()));
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isUseChildrenOfTopNodesAsParentGroups());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isAllFieldsEditable());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isJuridicalPerson());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isChangePasswordNextTime());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isSendMailToUser());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.isAllowEnableDisableAccount());
		action.append(SimpleUserApp.COMMA_SEPARATOR).append(bean.getParentGroupId() == -1 ? "null" :
																								getJavaScriptParameter(String.valueOf(bean.getParentGroupId())));
		action.append(");");
		return action.toString();
	}

	@Override
	public String getJavaScriptParameter(String parameter) {
		if (StringUtil.isEmpty(parameter) || parameter.equals("null")) {
			return "null";
		}
		return new StringBuffer("'").append(parameter).append("'").toString();
	}

	@Override
	public String getJavaScriptFunctionParameter(List<String> parameters) {
		if (ListUtil.isEmpty(parameters)) {
			return "null";
		}

		StringBuffer params = new StringBuffer("[");

		for (int i = 0; i < parameters.size(); i++) {
			params.append(getJavaScriptParameter(parameters.get(i)));
			if (i + 1 < parameters.size()) {
				params.append(SimpleUserApp.COMMA_SEPARATOR);
			}
		}

		params.append("]");
		return params.toString();
	}
}