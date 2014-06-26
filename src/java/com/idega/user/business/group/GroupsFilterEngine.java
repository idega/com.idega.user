package com.idega.user.business.group;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.jdom2.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.ICBuilderConstants;
import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.GenericInput;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.util.AbstractChooserBlock;
import com.idega.repository.data.Singleton;
import com.idega.user.bean.group.GroupFilterResult;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.presentation.group.FilteredGroupsBox;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 *
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version Revision: 1.00
 *
 * Last modified: 2008.07.31 09:30:25 by: valdas
 */

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(GroupsFilterEngine.SPRING_BEAN_IDENTIFIER)
public class GroupsFilterEngine implements Singleton {

	public static final String SPRING_BEAN_IDENTIFIER = "groupsFilterEngine";

	private BuilderLogicWrapper builder;

	private String groupsListStyle = "filteredGroupsListStyle";

	//	It's a Spring bean!
	private GroupsFilterEngine() {}

	public Document getFilteredGroups(String searchKey, String selectedGroupName, List<String> selectedGroups, String onClickAction, boolean useRadioBox) {
		if (StringUtil.isEmpty(searchKey)) {
			return null;
		}

		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {
			e.printStackTrace();
		}
		if (currentUser == null) {
			return null;
		}

		FilteredGroupsBox filteredGroups = new FilteredGroupsBox();
		filteredGroups.setFilteredGroups(getUserGroupsBySearchKey(iwc, currentUser, searchKey));
		filteredGroups.setSearchResult(Boolean.TRUE);
		filteredGroups.setSelectedGroups(selectedGroups);
		filteredGroups.setSelectedGroupParameterName(selectedGroupName);
		filteredGroups.setOnClickAction(onClickAction);
		filteredGroups.setUseRadioBox(useRadioBox);

		BuilderService builderService = getBuilderService(iwc);
		if (builderService == null) {
			return null;
		}
		return builderService.getRenderedComponent(iwc, filteredGroups, true);
	}

	private List<GroupFilterResult> getUserGroupsBySearchKey(IWContext iwc, User user, String searchKey) {
		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}

		Collection<Group> userGroupsByPhrase = groupBusiness.getUserGroupsByPhrase(iwc, searchKey);
		if (ListUtil.isEmpty(userGroupsByPhrase)) {
			return null;
		}

		List<GroupFilterResult> results = new ArrayList<GroupFilterResult>();
		formatGroupsTree(iwc, user, results, userGroupsByPhrase, groupBusiness);

		return ListUtil.isEmpty(results) ? null : results;
	}

	private void formatGroupsTree(IWContext iwc, User user, List<GroupFilterResult> results, Collection<Group> groups, GroupBusiness groupBusiness) {
		int level = 0;
		Group parentGroup = null;
		ICTreeNode groupNode = null;
		for (Group group: groups) {
			level = 0;
			parentGroup = group;
			groupNode = parentGroup.getParentNode();

			try {
				while (parentGroup != null && groupNode != null) {
					try {
						parentGroup = groupBusiness.getGroupByGroupID(Integer.valueOf(groupNode.getId()));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (FinderException e) {
						e.printStackTrace();
					}

					if (parentGroup != null) {
						groupNode = parentGroup.getParentNode();
						level++;
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			results.add(new GroupFilterResult(level, group));
		}
	}

	public Document getChildGroups(Integer groupId, String selectedGroupParameterName, String onClickAction, boolean useRadioBox) {
		if (groupId == null) {
			return null;
		}
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return null;
		}
		Group group = null;
		try {
			group = groupBusiness.getGroupByGroupID(groupId);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FinderException e) {
			e.printStackTrace();
		}
		if (group == null) {
			return null;
		}

		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {
			e.printStackTrace();
		}
		if (currentUser == null) {
			return null;
		}

		Collection<Group> children = group.getChildren();
		if (ListUtil.isEmpty(children)) {
			return null;
		}

		Lists groupsList = new Lists();
		addGroups(iwc, groupsList, children, null, null, iwc.getCurrentLocale(), groupsList.getId(), selectedGroupParameterName, 0, 1, false,
				onClickAction, useRadioBox);

		BuilderService builderService = getBuilderService(iwc);
		if (builderService == null) {
			return null;
		}
		return builderService.getRenderedComponent(iwc, groupsList, true);
	}

	private GroupBusiness getGroupBusiness(IWApplicationContext iwac) {
		try {
			return IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addGroups(IWContext iwc, Lists container, Collection<Group> groups, List<String> selectedGroups, List<GroupFilterResult> filteredGroups,
							Locale locale, String mainContainerId, String selectedGroupParameterName, int level, int levelsToOpen, boolean displayAllLevels,
							String onClickAction, boolean useRadioBox) {
		IWBundle bundle = iwc.getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		GroupHelper groupHelper = ELUtil.getInstance().getBean(GroupHelper.class);

		container.setStyleClass(groupsListStyle);

		ListItem item = null;
		String groupId = null;
		String inputId = null;
		Image groupIcon = null;
		String groupName = null;
		Text groupNameText = null;
		String groupIconBase = null;
		Lists childrenGroups = null;
		StringBuilder action = null;
		Layer groupNodeContainer = null;
		boolean addOneMoreLevel = false;
		Collection<Group> children = null;
		GenericInput groupSelection = null;
		String changedOnClickAction = null;
		Layer childrenGroupsContainer = null;
		boolean onClickActionSet = !StringUtil.isEmpty(onClickAction);
		String checkBoxStyleClass = "checkBoxForFilteredGroupSelectionStyle";
		String loadingMessage = iwrb.getLocalizedString("loading", "Loading...");
		String imageOpenUri = bundle.getVirtualPathWithFileNameString("images/nav-plus.gif");
		String imageCloseUri = bundle.getVirtualPathWithFileNameString("images/nav-minus.gif");
		String openOrCloseGroupsTooltip = iwrb.getLocalizedString("open_or_close_group", "Open/close group");
		String selectOrDeselectGroupTooltip = iwrb.getLocalizedString("select_or_deselect_group", "Select/deselect group");
		for (Group group: groups) {
			groupId = group.getId();
			groupName = group.getNodeName(locale);
			addOneMoreLevel = addOneMoreLevel(iwc, group, selectedGroups, filteredGroups, level, levelsToOpen, displayAllLevels);

			item = new ListItem();
			container.add(item);
			item.setStyleClass("filteredGroupNodeStyle");

			groupNodeContainer = new Layer();
			item.add(groupNodeContainer);

			Image openOrCloseImage = new Image(addOneMoreLevel ? imageCloseUri : imageOpenUri);
			groupNodeContainer.add(openOrCloseImage);
			if (group.getChildCount() <= 0) {
				openOrCloseImage.setStyleClass("groupsFilterOpenOrCloseGroupsImageStyle");
			}
			else {
				openOrCloseImage.setTitle(openOrCloseGroupsTooltip);
				openOrCloseImage.setMarkupAttribute("groupid", groupId);
			}

			if (StringUtil.isEmpty(groupIconBase)) {
				groupIconBase = groupHelper.getGroupImageBaseUri(iwc);
			}
			groupIcon = new Image(groupHelper.getGroupIcon(group, groupIconBase, addOneMoreLevel));
			groupNodeContainer.add(groupIcon);
			groupIcon.setTitle(groupName);

			groupSelection = useRadioBox ? new RadioButton(selectedGroupParameterName, groupId) : new CheckBox(selectedGroupParameterName, groupId);
			groupSelection.setStyleClass(checkBoxStyleClass);
			groupSelection.setTitle(selectOrDeselectGroupTooltip);
			groupNodeContainer.add(groupSelection);
			if (!ListUtil.isEmpty(selectedGroups)) {
				if (selectedGroups.contains(groupId)) {
					selectedGroups.remove(groupId);
					if (useRadioBox) {
						((RadioButton) groupSelection).setSelected();
					}
					else {
						((CheckBox) groupSelection).setChecked(true, true);
					}
				}
			}
			inputId = groupSelection.getId();
			action = new StringBuilder("GroupsFilter.manageCheckedGroupsInOtherContainers(['").append(inputId).append("', '")
			.append(mainContainerId).append("', '").append(checkBoxStyleClass).append("']);");
			if (onClickActionSet) {
				action.append(AbstractChooserBlock.getNormalizedAction(onClickAction, inputId));
				changedOnClickAction = getActionAppliedToBeParameter(onClickAction);
			}
			groupSelection.setOnClick(action.toString());

			groupSelection.setMarkupAttribute(ICBuilderConstants.GROUP_ID_ATTRIBUTE, groupId);
			groupSelection.setMarkupAttribute(ICBuilderConstants.GROUP_NAME_ATTRIBUTE, groupName);

			groupNameText = new Text(groupName);
			groupNameText.setStyleClass(getStyleAttributeForGroupName(group, filteredGroups));
			groupNodeContainer.add(groupNameText);

			childrenGroupsContainer = new Layer();
			item.add(childrenGroupsContainer);
			openOrCloseImage.setOnClick(new StringBuilder("GroupsFilter.openOrCloseNodes(['").append(openOrCloseImage.getId()).append("', '")
							.append(childrenGroupsContainer.getId()).append("', '").append(imageOpenUri).append("', '").append(imageCloseUri)
							.append("', '").append(selectedGroupParameterName).append("', '").append(loadingMessage).append("'], ")
							.append(onClickActionSet ? new StringBuilder("'").append(changedOnClickAction).append("'").toString() : "null").append(", ")
							.append(useRadioBox).append(");").toString());
			if (addOneMoreLevel) {
				children = group.getChildren();
				if (!ListUtil.isEmpty(children)) {
					openOrCloseImage.setMarkupAttribute("opened", Boolean.TRUE.toString());
					if (addOneMoreLevel) {
						openOrCloseImage.setMarkupAttribute("dataloaded", Boolean.TRUE.toString());
					}

					childrenGroups = new Lists();
					childrenGroupsContainer.add(childrenGroups);
					addGroups(iwc, childrenGroups, children, selectedGroups, filteredGroups, locale, mainContainerId, selectedGroupParameterName, level + 1,
							levelsToOpen, displayAllLevels, onClickAction, useRadioBox);
				}
			}
			else {
				childrenGroupsContainer.setStyleAttribute("display", "none");
			}
		}
	}

	private boolean forceToOpenNode(IWContext iwc, Group group, List<String> selectedGroups, List<GroupFilterResult> filteredGroups) {
		if (ListUtil.isEmpty(selectedGroups)) {
			return false;
		}

		GroupBusiness groupBusiness = getGroupBusiness(iwc);
		if (groupBusiness == null) {
			return false;
		}

		Collection<Group> childGroups = null;
		try {
			childGroups = groupBusiness.getChildGroups(group);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(childGroups)) {
			return false;
		}

		for (String childGroupId: selectedGroups) {
			for (Group childGroup: childGroups) {
				if (childGroupId.equals(childGroup.getId())) {
					return true;
				}
			}
		}

		if (ListUtil.isEmpty(filteredGroups)) {
			return false;
		}
		for (GroupFilterResult filteredGroup: filteredGroups) {
			if (childGroups.contains(filteredGroup.getGroup())) {
				return true;
			}
		}

		return false;
	}

	private boolean addOneMoreLevel(IWContext iwc, Group group, List<String> selectedGroups, List<GroupFilterResult> filteredGroups, int level, int levelsToOpen,
			boolean displayAllLevels) {
		return displayAllLevels || (level + 1) < levelsToOpen || forceToOpenNode(iwc, group, selectedGroups, filteredGroups);
	}

	private String getStyleAttributeForGroupName(Group group, List<GroupFilterResult> filteredGroups) {
		StringBuilder styleClass = new StringBuilder("basicFilteredGroupNameElementStyle");
		if (ListUtil.isEmpty(filteredGroups)) {
			return styleClass.toString();
		}

		boolean directSearchResult = false;
		for (int i = 0; (i < filteredGroups.size() && !directSearchResult); i++) {
			if (group.equals(filteredGroups.get(i).getGroup())) {
				directSearchResult = true;
			}
		}

		if (directSearchResult) {
			return styleClass.append(" directSearchResultElementStyle").toString();
		}

		return styleClass.append(" inDirectSearchResultElementStyle").toString();
	}

	private BuilderService getBuilderService(IWApplicationContext iwac) {
		return builder.getBuilderService(iwac);
	}

	public BuilderLogicWrapper getBuilder() {
		return builder;
	}

	@Autowired
	public void setBuilder(BuilderLogicWrapper builder) {
		this.builder = builder;
	}

	public String getActionAppliedToBeParameter(String action) {
		if (StringUtil.isEmpty(action)) {
			return null;
		}
		action = StringHandler.replace(action, "'", "\\'");
		return action;
	}

	public Collection<Group> getUserGroups(IWContext iwc, boolean onlyTopGroups) {
		User currentUser = null;
		try {
			currentUser = iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {
			e.printStackTrace();
		}
		if (currentUser == null) {
			return null;
		}

		UserBusiness userBusiness = null;
		try {
			userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}

		Collection<Group> groupsByPermissions = null;
		try {
			groupsByPermissions = userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(currentUser, iwc);
		} catch (EJBException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		Collection<Group> directGroups = null;
		try {
			directGroups = userBusiness.getUserGroups(currentUser);
		} catch (EJBException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (ListUtil.isEmpty(groupsByPermissions) && ListUtil.isEmpty(directGroups)) {
			return null;
		}
		if (ListUtil.isEmpty(groupsByPermissions)) {
			return onlyTopGroups ? getOnlyTopGroups(iwc, directGroups) : directGroups;
		}
		if (ListUtil.isEmpty(directGroups)) {
			return onlyTopGroups ? getOnlyTopGroups(iwc, groupsByPermissions) : groupsByPermissions;
		}

		List<Group> userGroups = new ArrayList<Group>(directGroups);
		for (Group group: groupsByPermissions) {
			if (!userGroups.contains(group)) {
				userGroups.add(group);
			}
		}
		return onlyTopGroups ? getOnlyTopGroups(iwc, userGroups) : userGroups;
	}

	private Collection<Group> getOnlyTopGroups(IWContext iwc, Collection<Group> userGroups) {
		if (ListUtil.isEmpty(userGroups)) {
			return null;
		}

		GroupHelper helper = ELUtil.getInstance().getBean(GroupHelper.class);
		Collection<Group> topGroups = helper.getTopGroupsFromDomain(iwc);
		if (ListUtil.isEmpty(topGroups)) {
			return userGroups;
		}

		List<Group> onlyTopGroupsForUser = new ArrayList<Group>();
		for (Group group: userGroups) {
			if (topGroups.contains(group) && !onlyTopGroupsForUser.contains(group)) {
				onlyTopGroupsForUser.add(group);
			}
		}

		return ListUtil.isEmpty(onlyTopGroupsForUser) ? userGroups : onlyTopGroupsForUser;
	}

}
