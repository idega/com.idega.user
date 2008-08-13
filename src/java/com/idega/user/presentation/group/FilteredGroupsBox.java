package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Lists;
import com.idega.user.bean.group.GroupFilterResult;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.business.group.GroupsFilterEngine;
import com.idega.user.data.Group;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

public class FilteredGroupsBox extends Block {
	
	private int levelsToOpen = 1;
	
	private Collection<Group> groups;
	
	private List<GroupFilterResult> filteredGroups;
	private List<String> selectedGroups;
	
	private String selectedGroupParameterName = "selectedGroup";
	
	private boolean searchResult;
	private boolean displayAllLevels;
	
	private IWBundle bundle;
	private IWResourceBundle iwrb;
	
	@Override
	public void main(IWContext iwc) {
		bundle = getBundle(iwc);
		iwrb = bundle.getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		if (searchResult) {
			container.setMarkupAttribute("searchresult", Boolean.TRUE.toString());
		}
		container.setStyleClass("filteredGroupsBoxStyle");
		if (ListUtil.isEmpty(groups) && ListUtil.isEmpty(filteredGroups)) {
			container.add(new Heading3(iwrb.getLocalizedString("no_groups_available", "There are no groups available")));
			return;
		}
		
		if (ListUtil.isEmpty(groups)) {
			groups = new ArrayList<Group>();
			convertSearchResultsToGroups(iwc);
		}
		
		Lists groupsList = new Lists();
		container.add(groupsList);
		
		GroupsFilterEngine filterEngine = ELUtil.getInstance().getBean(GroupsFilterEngine.SPRING_BEAN_IDENTIFIER);
		if (!ListUtil.isEmpty(selectedGroups)) {
			selectedGroups = new ArrayList<String>(selectedGroups);
		}
		filterEngine.addGroups(iwc, groupsList, groups, selectedGroups, filteredGroups, iwc.getCurrentLocale(), container.getId(), selectedGroupParameterName, 0,
				levelsToOpen, displayAllLevels);
	}
	
	private void convertSearchResultsToGroups(IWApplicationContext iwac) {
		int level = 0;
		Group group = null;
		Group parentGroup = null;
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		for (GroupFilterResult result: filteredGroups) {
			level = result.getLevel();
			group = result.getGroup();
			
			if (level == 0) {
				if (!groups.contains(group)) {
					groups.add(group);
				}
			}
			else {
				parentGroup = getGroup(groupBusiness, group.getParentNode());
				while (parentGroup != null && level > 0) {
					level--;
					group = parentGroup;
					parentGroup = getGroup(groupBusiness, group.getParentNode());
				}
				if (!groups.contains(group)) {
					groups.add(group);
				}
			}
		}
	}
	
	private Group getGroup(GroupBusiness groupBusiness, ICTreeNode groupNode) {
		if (groupBusiness == null || groupNode == null) {
			return null;
		}
		
		try {
			return groupBusiness.getGroupByGroupID(Integer.valueOf(groupNode.getId()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	public Collection<Group> getGroups() {
		return groups;
	}

	public void setGroups(Collection<Group> groups) {
		this.groups = groups;
	}

	public String getSelectedGroupParameterName() {
		return selectedGroupParameterName;
	}

	public void setSelectedGroupParameterName(String selectedGroupParameterName) {
		this.selectedGroupParameterName = selectedGroupParameterName;
	}

	public boolean isSearchResult() {
		return searchResult;
	}

	public void setSearchResult(boolean searchResult) {
		this.searchResult = searchResult;
	}

	public List<String> getSelectedGroups() {
		return selectedGroups;
	}

	public void setSelectedGroups(List<String> selectedGroups) {
		this.selectedGroups = selectedGroups;
	}

	public List<GroupFilterResult> getFilteredGroups() {
		return filteredGroups;
	}

	public void setFilteredGroups(List<GroupFilterResult> filteredGroups) {
		this.filteredGroups = filteredGroups;
	}

	public int getLevelsToOpen() {
		return levelsToOpen;
	}

	public void setLevelsToOpen(int levelsToOpen) {
		this.levelsToOpen = levelsToOpen;
	}

	public boolean isDisplayAllLevels() {
		return displayAllLevels;
	}

	public void setDisplayAllLevels(boolean displayAllLevels) {
		this.displayAllLevels = displayAllLevels;
	}

}
