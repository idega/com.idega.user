package com.idega.user.presentation.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserConstants;
import com.idega.user.business.group.GroupsFilterEngine;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class GroupsFilter extends IWBaseComponent {
	
	private String selectedGroupParameterName = null;//"selectedGroup";
	
	private Collection<String> selectedGroups = null;
	
	private Integer levelsToOpen = null;
	
	private Boolean displayAllLevels = false;
	private Boolean useRadioBox = false;
	
	private String onClickAction = "";
	
	@Autowired
	private JQuery jQuery;
	
	@Override
	protected void initializeComponent(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		ELUtil.getInstance().autowire(this);
		
		
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle bundle = iwma.getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		List<String> files = new ArrayList<String>(4);
		files.add(jQuery.getBundleURIToJQueryLib());
		files.add(CoreConstants.DWR_ENGINE_SCRIPT);
		files.add("/dwr/interface/GroupsFilterEngine.js");
		files.add(bundle.getVirtualPathWithFileNameString("javascript/GroupsFilter.js"));
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, files);
		PresentationUtil.addStyleSheetToHeader(iwc, bundle.getVirtualPathWithFileNameString("style/user.css"));
		
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("groupsChooserBoxStyle");
		
		Layer header = new Layer();
		header.setStyleClass("groupsChooserBoxHeaderStyle");
		container.add(header);
		
		Layer clearLayer = new Layer();
		clearLayer.setStyleClass("Clear");
		container.add(clearLayer);
		
		Layer body = new Layer();
		container.add(body);
		body.setStyleClass("groupsChooserBoxBodyStyle");
		
		TextInput filterInput = new TextInput();
		filterInput.setTitle(iwrb.getLocalizedString("enter_group_name", "Enter group's name"));
		
		Collection <String> selectedGroups = getSelectedGroups();
		boolean selectedAnything = !ListUtil.isEmpty(selectedGroups);
		StringBuilder selectedGroupsExpression = selectedAnything ? new StringBuilder("[") : new StringBuilder("null");
		if (selectedAnything) {
			for (Iterator<String> iter = selectedGroups.iterator();iter.hasNext();) {
				selectedGroupsExpression.append("'").append(iter.next()).append("'");
				
				if (iter.hasNext()) {
					selectedGroupsExpression.append(", ");
				}
			}
			selectedGroupsExpression.append("]");
		}
		String action = "GroupsFilter.setSelectedGroups(" + selectedGroupsExpression.toString() + ");";
		String jsAction = PresentationUtil.getJavaScriptAction(action);
		container.add(jsAction);
		
		String changedOnClickAction = null;
		if (!StringUtil.isEmpty(onClickAction)) {
			GroupsFilterEngine filterEngine = ELUtil.getInstance().getBean(GroupsFilterEngine.SPRING_BEAN_IDENTIFIER);
			changedOnClickAction = filterEngine.getActionAppliedToBeParameter(getOnClickAction());
		}
		String filterAction = new StringBuilder("GroupsFilter.filterGroupsByNewInfo(['").append(filterInput.getId()).append("', '")
												.append(iwrb.getLocalizedString("searching", "Searching...")).append("', '").append(body.getId())
												.append("', '").append(getSelectedGroupParameterName()).append("'], ").append("GroupsFilter.getSelectedGroups()")
												.append(", ").append(StringUtil.isEmpty(changedOnClickAction) ? "null" : new StringBuilder("'")
												.append(changedOnClickAction).append("'").toString()).append(", ").append(useRadioBox).append(");").toString();
		filterInput.setOnKeyPress(new StringBuilder("if (isEnterEvent(event)) {").append(filterAction).append(" return false;}").toString());
		Label filterInputLabel = new Label(iwrb.getLocalizedString("groups_filter", "Groups filter") + ":", filterInput);
		header.add(filterInputLabel);
		header.add(filterInput);
		
		GenericButton searchButton = new GenericButton(iwrb.getLocalizedString("search", "Search"));
		searchButton.setStyleClass("applicationButton");
		searchButton.setTitle(iwrb.getLocalizedString("search_for_groups", "Search for groups"));
		searchButton.setOnClick(filterAction);
		header.add(searchButton);
		GenericButton clearResults = new GenericButton(iwrb.getLocalizedString("clear", "Clear"));
		clearResults.setStyleClass("applicationButton");
		clearResults.setTitle(iwrb.getLocalizedString("clear_search_results", "Clear search results"));
		header.add(clearResults);
		clearResults.setOnClick(new StringBuilder("GroupsFilter.clearSearchResults(['").append(body.getId()).append("', '").append(filterInput.getId())
													.append("']);").toString());
		
		body.add(getFilteredGroupsBox(iwc));
	}
	
	private FilteredGroupsBox getFilteredGroupsBox(IWContext iwc) {
		FilteredGroupsBox filteredGroups = new FilteredGroupsBox();
		
		GroupsFilterEngine groupsFilter = ELUtil.getInstance().getBean(GroupsFilterEngine.SPRING_BEAN_IDENTIFIER);
		filteredGroups.setGroups(groupsFilter.getUserGroups(iwc, true));
		
		filteredGroups.setLevelsToOpen(getLevelsToOpen());
		filteredGroups.setDisplayAllLevels(isDisplayAllLevels());
		filteredGroups.setOnClickAction(getOnClickAction());
		filteredGroups.setUseRadioBox(isUseRadioBox());
		
		String[] selectedInForm = iwc.getParameterValues(getSelectedGroupParameterName());
		if (selectedInForm != null) {
			selectedGroups = Arrays.asList(selectedInForm);
		}
		
		Collection<String> selectedGroups = getSelectedGroups();
		List<String> selectedGroupsList = selectedGroups instanceof List ? (List<String>)selectedGroups : new ArrayList<String>(selectedGroups);
		filteredGroups.setSelectedGroups(selectedGroupsList);
		filteredGroups.setSelectedGroupParameterName(getSelectedGroupParameterName());
		return filteredGroups;
	}
	
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getSelectedGroups() {
		if(selectedGroups != null){
			return selectedGroups;
		}
		ValueExpression valueExpression = getValueExpression("selectedGroups");
		if(valueExpression == null){
			return Collections.emptyList();
		}
		selectedGroups = (Collection<String>)valueExpression.getValue(getFacesContext().getELContext());
		return selectedGroups;
	}

	public void setSelectedGroups(Collection<String> selectedGroups) {
		this.selectedGroups = selectedGroups;
	}

	public String getSelectedGroupParameterName() {
		if(selectedGroupParameterName != null){
			return selectedGroupParameterName;
		}
		Map <String, Object> map = getAttributes();
		selectedGroupParameterName = (String)map.get("selectedGroupsParameterName");
		return selectedGroupParameterName;
	}

	public void setSelectedGroupParameterName(String selectedGroupParameterName) {
		this.selectedGroupParameterName = selectedGroupParameterName;
	}

	public int getLevelsToOpen() {
		if(levelsToOpen != null){
			return levelsToOpen;
		}
		return 1;
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

	public String getOnClickAction() {
		if(onClickAction != null){
			return onClickAction;
		}
		onClickAction = (String)getAttributes().get("onClickAction");
		return onClickAction;
	}

	public void setOnClickAction(String onClickAction) {
		this.onClickAction = onClickAction;
	}

	public boolean isUseRadioBox() {
		return useRadioBox;
	}

	public void setUseRadioBox(boolean useRadioBox) {
		this.useRadioBox = useRadioBox;
	}
	
}
