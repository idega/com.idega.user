package com.idega.user.business;

import java.util.List;

public class GroupNode {
	
	private String name = null;
	private String uniqueId = null;
	private boolean hasChildren = false;
	private List children = null;
	

	public boolean isHasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUniqueId() {
		return uniqueId;
	}
	
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public List getChildren() {
		return this.children;
	}
	
	public void setChildren(List children) {
		this.children = children;
	}
	
}
