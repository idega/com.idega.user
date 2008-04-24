package com.idega.user.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class GroupsManagerBean implements Serializable {

	private static final long serialVersionUID = 3054659649289011497L;
	
	private Map<String, PropertiesBean> abstractProperties = null;
	private Map<String, GroupPropertiesBean> groupProperties = null;
	private Map<String, UserPropertiesBean> userProperties = null;
	
	public GroupsManagerBean() {
		abstractProperties = new HashMap<String, PropertiesBean>();
		groupProperties = new HashMap<String, GroupPropertiesBean>();
		userProperties = new HashMap<String, UserPropertiesBean>();
	}
	
	public boolean addAbstractProperties(String instanceId, PropertiesBean propertiesBean) {
		if (instanceId == null || propertiesBean == null) {
			return false;
		}
		abstractProperties.put(instanceId, propertiesBean);
		return true;
	}
	
	private PropertiesBean getAbstractProperties(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		return abstractProperties.get(instanceId);
	}
	
	public boolean addGroupProperties(String instanceId, GroupPropertiesBean propertiesBean) {
		if (instanceId == null || propertiesBean == null) {
			return false;
		}
		groupProperties.put(instanceId, propertiesBean);
		return true;
	}
	
	public boolean addUserProperties(String instanceId, UserPropertiesBean propertiesBean) {
		if (instanceId == null || propertiesBean == null) {
			return false;
		}
		userProperties.put(instanceId, propertiesBean);
		return true;
	}
	
	public GroupPropertiesBean getGroupProperties(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		PropertiesBean bean = getAbstractProperties(instanceId);
		if (bean == null) {
			return groupProperties.get(instanceId);
		}
		else {
			abstractProperties.remove(instanceId);
			
			if (bean instanceof GroupPropertiesBean) {
				return (GroupPropertiesBean) bean;
			}
			return new GroupPropertiesBean(bean, instanceId);
		}
		
	}
	
	public UserPropertiesBean getUserProperties(String instanceId) {
		if (instanceId == null) {
			return null;
		}
		PropertiesBean bean = getAbstractProperties(instanceId);
		if (bean == null) {
			return userProperties.get(instanceId);
		}
		else {
			abstractProperties.remove(instanceId);
			
			if (bean instanceof UserPropertiesBean) {
				return (UserPropertiesBean) bean;
			}
			return new UserPropertiesBean(bean, instanceId);
		}
	}
	
	public boolean removeGroupProperties(String instanceId) {
		return removeProperties(instanceId, groupProperties);
	}
	
	public boolean removeUserProperties(String instanceId) {
		return removeProperties(instanceId, userProperties);
	}
	
	private boolean removeProperties(String key, Map properties) {
		if (key == null) {
			return false;
		}
		
		properties.remove(key);
		
		return true;
	}

}
