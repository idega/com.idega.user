package com.idega.user.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

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
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.GenericUserComparator;

public class GroupHelperBusinessBean implements GroupHelper {
	
	private UserBusiness userBusiness = null;
	private GroupBusiness groupBusiness = null;
	
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
	
	public List<GroupNode> getTopGroupsAndDirectChildren(User user, IWContext iwc) {
		List<GroupNode> fake = new ArrayList<GroupNode>();
		
		if (user == null || iwc == null) {
			return fake;
		}
		
		userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return fake;
		}
		
		try {
			return convertGroupsToGroupNodes(userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc), iwc, true, getGroupImageBaseUri(iwc));
		} catch (RemoteException e) {
			e.printStackTrace();
			return fake;
		}
	}
	
	public synchronized UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBusiness == null) {
			try {
				userBusiness = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userBusiness;
	}
	
	@SuppressWarnings("unchecked")
	public List<GroupNode> convertGroupsToGroupNodes(Collection groups, IWContext iwc, boolean isFirstLevel, String imageBaseUri) {
		List <GroupNode> list = new ArrayList<GroupNode>();
		if (groups == null || iwc == null) {
			return list;
		}
		
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		
		Object o = null;
		Group group = null;
		GroupNode groupNode = null;
		for (Iterator it = groups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				groupNode = createGroupNodeFromGroup(group, imageBaseUri, false);
				if (groupNode != null) {
					if (isFirstLevel) {
						if (groupBusiness != null) {
							try {
								groupNode.setChildren(convertGroupsToGroupNodes(groupBusiness.getChildGroups(group), iwc, false, imageBaseUri));
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}
					
					list.add(groupNode);
				}
			}
		}

		return list;
	}
	
	private GroupNode createGroupNodeFromGroup(Group group, String imageBaseUri, boolean nodeIsOpened) {
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
		node.setHasChildren(group.getChildCount() > 0);
		
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
		node.setImage(imageUri.toString());
		
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public GroupNode addChildGroupsToNode(GroupNode parentNode, Collection groups, String image) {
		if (groups == null) {
			parentNode.setHasChildren(false);
			return parentNode;
		}
		if (groups.size() == 0) {
			return parentNode;
		}
		
		List<GroupNode> children = new ArrayList<GroupNode>();
		Object o = null;
		Group group = null;
		GroupNode childNode = null;
		for (Iterator it = groups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				childNode = createGroupNodeFromGroup(group, image, false);
				
				children.add(childNode);
			}
		}
		
		if (children.size() > 0) {
			parentNode.setHasChildren(true);
			parentNode.setChildren(children);
		}
		
		return parentNode;
	}
	
	@SuppressWarnings("unchecked")
	private List<Group> getChilrenfOfGroups(Collection groups) {
		List<Group> children = new ArrayList<Group>();
		if (groups == null) {
			return null;
		}
		Object o = null;
		Group group = null;
		for (Iterator it = groups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				children.addAll(group.getChildGroups());
			}
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
	public Collection<Group> getFilteredGroups(Collection<Group> groups, List<String> types, boolean useChildrenAsTopNodes) {
		Collection<Group> filtered = new ArrayList<Group>();
		if (groups == null) {
			return filtered;
		}
		if (groups.size() == 0) {
			return filtered;
		}
		
		if (types == null) {
			if (useChildrenAsTopNodes) {
				return getChilrenfOfGroups(groups);
			}
 			return groups;
		}
		if (types.size() == 0) {
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
	
	@SuppressWarnings("unchecked")
	private Collection<Group> getFilteredGroups(Collection groups, List<String> types) {
		Collection<Group> filtered = new ArrayList<Group>();
		
		Object o = null;
		Group group = null;
		for (Iterator it = groups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				if (group.getGroupType() != null) {
					if (types.contains(group.getGroupType())) {
						filtered.add(group);
					}
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
	public Collection<Group> getFilteredGroups(Collection<Group> groups, String typesValue, String splitter, boolean useChildrenAsTopNodes) {
		return getFilteredGroups(groups, getExtractedTypesList(typesValue, splitter), useChildrenAsTopNodes);
	}
	
	private synchronized GroupBusiness getGroupBusiness(IWApplicationContext iwac) {
		if (groupBusiness == null) {
			try {
				groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groupBusiness;
	}
	
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
	
	@SuppressWarnings("unchecked")
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
		List<Group> checkedFiltered = new ArrayList<Group>();
		for (int i = 0; i < filtered.size(); i++) {
			group = filtered.get(i);
				
			if (group.getUniqueId() == null) {
				checkedFiltered.add(group);
			}
			else {
				if (!parent.getUniqueId().equals(group.getUniqueId())) {
					checkedFiltered.add(group);
				}
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
		for (int i = 0; i < roles.size(); i++) {
			roleKey = roles.get(i).toString();
			for (int j = 0; j < checkedFiltered.size(); j++) {
				group = checkedFiltered.get(j);
				if (controler.hasRole(roleKey, group, iwc)) {
					filteredByRole.add(group);
				}
			}
			
			if (checkedFiltered.size() > 0) {
				//	Removing groups (from basic groups list) that were filtered and added to list
				for (int j = 0; j < filteredByRole.size(); j++) {
					group = (Group) filteredByRole.get(j);
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
	
	@SuppressWarnings("unchecked")
	public List<User> getSortedUsers(IWContext iwc, SimpleUserPropertiesBean bean) {
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
		if (users == null) {
			return null;
		}
		
		List<User> sortedUsers = new ArrayList<User>(users);
		if (bean.getOrderBy() == SimpleUserApp.USER_ORDER_BY_ID) {
			Collections.sort(sortedUsers, new GenericUserComparator(iwc.getCurrentLocale(), GenericUserComparator.PERSONALID));
		}
		else if (bean.getOrderBy() == SimpleUserApp.USER_ORDER_BY_NAME) {
			Collections.sort(sortedUsers, new GenericUserComparator(iwc.getCurrentLocale(), GenericUserComparator.FIRSTLASTMIDDLE));
		}
		
		return sortedUsers;
	}
	
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
	
	public Group getGroup(IWContext iwc, String id) {
		return getGroup(iwc, getParsedValue(id));
	}
	
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
	
	public User getUser(IWContext iwc, String id) {
		return getUser(iwc, getParsedValue(id));
	}
	
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
	
	@SuppressWarnings("unchecked")
	public List<String> getUserGroupsIds(IWContext iwc, User user) {
		List<String> ids = new ArrayList<String>();
		if (user == null) {
			return ids;
		}
		
		UserBusiness userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return ids;
		}
		
		Collection userGroups = null;
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
		
		Object o = null;
		for (Iterator it = userGroups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				ids.add(((Group) o).getId());
			}
		}
		
		return ids;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Group> getTopGroupsFromDomain(IWContext iwc) {
		ICDomain domain = iwc.getDomain();
		if (domain == null) {
			return null;
		}
		
		try {
			return domain.getTopLevelGroupsUnderDomain();
		} catch (IDORelationshipException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Group> getTopGroups(IWContext iwc, User user) {
		if (iwc == null || user == null) {
			return new ArrayList();
		}
		userBusiness = getUserBusiness(iwc);
		if (userBusiness == null) {
			return new ArrayList();
		}
		
		try {
			return userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
			return new ArrayList();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Group> getTopAndParentGroups(Collection topGroups) {
		if (topGroups == null) {
			return null;
		}
		
		Object o = null;
		Group group = null;
		Collection<Group> topAndParentGroups = new ArrayList<Group>(topGroups);
		List<Group> parentGroups = null;
		for (Iterator it = topGroups.iterator(); it.hasNext();) {
			o = it.next();
			if (o instanceof Group) {
				group = (Group) o;
				parentGroups = group.getParentGroups();
				if (parentGroups != null) {
					topAndParentGroups.addAll(parentGroups);
				}
			}
		}
		
		return topAndParentGroups;
	}
	
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
}
