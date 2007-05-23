package com.idega.user.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GroupsManagerBean implements Serializable {

	private static final long serialVersionUID = 3054659649289011497L;
	
	private Map<String, GroupPropertiesBean> properties = null;
	
	public GroupsManagerBean() {
		properties = new HashMap<String, GroupPropertiesBean>();
	}
	
	public boolean addProperties(String instanceId, GroupPropertiesBean propertiesBean) {
		if (instanceId == null || propertiesBean == null) {
			return false;
		}
		
		properties.put(instanceId, propertiesBean);
		
		return true;
	}
	
	public GroupPropertiesBean getProperties(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		
		return properties.get(instanceId);
	}
	
	public boolean removeProperties(String instanceId) {
		if (instanceId == null) {
			return false;
		}
		
		properties.remove(instanceId);
		
		return true;
	}

}
