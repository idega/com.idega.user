package com.idega.user.bean;

public class SimpleUserPropertiesBean {
	
	private int parentGroupId = -1;
	private int groupId = -1;
	private int orderBy = -1;
	
	public SimpleUserPropertiesBean() {}
	
	public SimpleUserPropertiesBean(int parentGroupId, int groupId, int orderBy) {
		this();
		this.parentGroupId = parentGroupId;
		this.groupId = groupId;
		this.orderBy = orderBy;
	}
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}
	public int getParentGroupId() {
		return parentGroupId;
	}
	public void setParentGroupId(int parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

}
