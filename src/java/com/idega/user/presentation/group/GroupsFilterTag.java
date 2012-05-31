package com.idega.user.presentation.group;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;

import com.idega.util.ListUtil;

public class GroupsFilterTag extends UIComponentELTag{
	private static final String COMPONENT_TYPE = "groupsFilter";
	
	private String selectedGroupParameterName = null;
	
	private List<String> selectedGroups = null;
	
	private Integer levelsToOpen = null;
	
	private Boolean displayAllLevels = null;
	private Boolean useRadioBox = null;
	
	private String onClickAction = null;
	
	
	@Override
	public void release() {
		super.release();
		selectedGroupParameterName = null;
		selectedGroups = null;
		levelsToOpen = null;
		displayAllLevels = null;
		useRadioBox = null;
	}
	
	public String getSelectedGroupParameterName() {
		return selectedGroupParameterName;
	}

	public void setSelectedGroupParameterName(String selectedGroupParameterName) {
		this.selectedGroupParameterName = selectedGroupParameterName;
	}

	public List<String> getSelectedGroups() {
		return selectedGroups;
	}

	public void setSelectedGroups(List<String> selectedGroups) {
		this.selectedGroups = selectedGroups;
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

	public boolean isUseRadioBox() {
		return useRadioBox;
	}

	public void setUseRadioBox(boolean useRadioBox) {
		this.useRadioBox = useRadioBox;
	}

	public String getOnClickAction() {
		return onClickAction;
	}

	public void setOnClickAction(String onClickAction) {
		this.onClickAction = onClickAction;
	}

	@Override
	public String getComponentType() {
		return COMPONENT_TYPE;
	}

	@Override
	public String getRendererType() {
		// null means the component renders itself
		return null;
	}
	
	@Override
	protected void setProperties(UIComponent component) { 
		// the super class method should be called 
		super.setProperties(component);
		
		Logger.getAnonymousLogger().log(Level.INFO, "something logged");
		
		GroupsFilter groupsFilter = (GroupsFilter)component;
		
		if(selectedGroupParameterName != null){
			groupsFilter.setSelectedGroupParameterName(selectedGroupParameterName);
		}
		
		if(!ListUtil.isEmpty(selectedGroups)){
			groupsFilter.setSelectedGroups(selectedGroups);
		}
		
		if(levelsToOpen != null){
			groupsFilter.setLevelsToOpen(levelsToOpen);
		}
		
		if(displayAllLevels){
			groupsFilter.setDisplayAllLevels(displayAllLevels);
		}
		
		if(useRadioBox != null){
			groupsFilter.setUseRadioBox(useRadioBox);
		}
		
		if(onClickAction != null){
			groupsFilter.setOnClickAction(onClickAction);
		}
	}

}
