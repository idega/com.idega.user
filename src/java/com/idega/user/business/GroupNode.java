package com.idega.user.business;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "groupNode")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupNode implements Serializable {

	private static final long serialVersionUID = -885171588872322089L;

	private String id;
	private String name = null;
	private String uniqueId = null;
	private String image = null;
	private String type = null;


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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
