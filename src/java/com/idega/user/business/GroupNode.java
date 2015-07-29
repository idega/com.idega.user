package com.idega.user.business;

import java.util.List;

public class GroupNode {

	private String id;
	private String name = null;
	private String uniqueId = null;
	private String image = null;

	private boolean hasChildren = false;

	private List<GroupNode> children = null;

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

	public List<GroupNode> getChildren() {
		return this.children;
	}

	public void setChildren(List<GroupNode> children) {
		this.children = children;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
