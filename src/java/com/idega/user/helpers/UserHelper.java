package com.idega.user.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.GroupTreeView;
import com.idega.user.presentation.UserChooserBrowserWindow;
import com.idega.util.IWColor;

public class UserHelper {
	
	private UserBusiness userBusiness = null;
	
	@SuppressWarnings("unchecked")
	public GroupTreeView getGroupTree(IWContext iwc) {
		GroupTreeView viewer = new GroupTreeView();
		try {			
			if (iwc.isSuperAdmin()) {
				GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
				viewer.setRootNode(node);
			}
			else{
				UserBusiness biz = getUserBusiness(iwc);
				Collection allGroups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
				
				//	Filter groups
				List<String> allowedGroupTypes = null;
				if (iwc.isParameterSet(AbstractChooser.FILTER_PARAMETER))  {
					String filter = iwc.getParameter(AbstractChooser.FILTER_PARAMETER);
					if (filter.length() > 0)  {
						allowedGroupTypes = getGroupTypes(filter, iwc);
					}
				}
				
				Collection groups = new ArrayList();
				if (allowedGroupTypes == null)  {
					groups = allGroups;
				}
				else {
					for (Iterator it = allGroups.iterator(); it.hasNext(); )  {
						Group group = (Group) it.next();
						if (checkGroupType(group, allowedGroupTypes))  {
							groups.add(group);
						}
					}
				}
				Collection groupNodes = convertGroupCollectionToGroupNodeCollection(groups, iwc.getApplicationContext());
				viewer.setFirstLevelNodes(groupNodes.iterator());
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return viewer;
	}
	
	private UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (this.userBusiness == null) {
			try {
				this.userBusiness = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (RemoteException rme) {
				return null;
			}
		}
		return this.userBusiness;
	}
	
	private List<String> getGroupTypes(String selectedGroup, IWContext iwc)  {
		List<String> groupTypes = new ArrayList<String>();
		Group group = null;
		// get group types
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness =(GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			if (! CreateGroupWindow.NO_GROUP_SELECTED.equals(selectedGroup))  {
				group = groupBusiness.getGroupByGroupID((new Integer(selectedGroup)).intValue());
			}
		}
		// Remote and FinderException
		catch (Exception ex)  {
			throw new RuntimeException(ex.getMessage());
		}
		Iterator iterator = null;
		try {
			iterator = groupBusiness.getAllAllowedGroupTypesForChildren(group, iwc).iterator();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		for (Iterator it = iterator; it.hasNext();)  {
			GroupType item = (GroupType) it.next();
			String value = item.getType();
			groupTypes.add(value);
		}
		return groupTypes;
	}

	private boolean checkGroupType(Group group, Collection allowedGroupTypes) {
		String groupType = group.getGroupTypeValue();
		Iterator iterator = allowedGroupTypes.iterator();
		while (iterator.hasNext())  {
			String type = (String) iterator.next();
			if (type.equals(groupType)) {
				return true;
			}
		}
		return false;
	}
	
	private Collection<GroupTreeNode> convertGroupCollectionToGroupNodeCollection(Collection col, IWApplicationContext iwac){
		List<GroupTreeNode> list = new ArrayList<GroupTreeNode>();
		for (Iterator it = col.iterator(); it.hasNext();) {
			Group group = (Group) it.next();
			GroupTreeNode node = new GroupTreeNode(group, iwac);
			list.add(node);
		}		
		return list;
	}
	
	public EntityBrowser getUserBrowser(Collection entities, String searchKey, IWContext iwc, int rows)  {
	    // define checkbox button converter class
	    EntityToPresentationObjectConverter converterToChooseButton = new EntityToPresentationObjectConverter() {

	      public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
	        return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);  
	      } 

	      public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
	        User user = (User) entity;
	        RadioButton radioButton = new RadioButton();
	        // define displaystring and value of the textinput of the parent window
	        radioButton.setOnClick(StyledAbstractChooserWindow.SELECT_FUNCTION_NAME+"('"
	          + user.getName() +
	          "','"
	          + ((Integer) user.getPrimaryKey()).toString() + 
	          "')");
	        return radioButton;
	      }
	    };
	    // set default columns
	    String nameKey = User.class.getName()+".FIRST_NAME:" + User.class.getName()+".MIDDLE_NAME:"+User.class.getName()+".LAST_NAME";
	    String pinKey = User.class.getName()+".PERSONAL_ID";
	    EntityBrowser browser = EntityBrowser.getInstanceUsingExternalForm();
//	    browser.setLeadingEntity(User.class);
	    browser.setEntities("chooser_window_" + searchKey, entities);
	    browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
	    browser.setDefaultNumberOfRows(rows);

	    browser.setWidth(Table.HUNDRED_PERCENT);
	      
	    //fonts
	    Text column = new Text();
	    column.setBold();
	    browser.setColumnTextProxy(column);
	      
	    //    set color of rows
	    browser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
	    browser.setColorForOddRows("#FFFFFF");
	      
	    browser.setDefaultColumn(1, nameKey);
	    browser.setDefaultColumn(2, pinKey);
	    browser.setMandatoryColumn(1, "Choose");
	    // set special converters
	    browser.setEntityToPresentationConverter("Choose", converterToChooseButton);
	    // set mandatory parameters
	    browser.addMandatoryParameters(StyledAbstractChooserWindow.getHiddenParameters(iwc));
	    browser.addMandatoryParameter(UserChooserBrowserWindow.SEARCH_KEY, searchKey);
	    return browser;
	}
	
	public Collection getUserEntities(String searchKey)  {
	    if (searchKey == null) {
			return new ArrayList();
		}
	    try {
	    	UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
	    	String modifiedSearch = getModifiedSearchString(searchKey);
	    	Collection entities = userHome.findUsersBySearchCondition(modifiedSearch, false);
	    	return entities;
	    }
	    // Remote and FinderException
	    catch (Exception ex)  {
	    	throw new RuntimeException(ex.getMessage());
	    }
	}
	
	private String getModifiedSearchString(String originalSearchString)  {
	    StringBuffer buffer = new StringBuffer("%");
	    buffer.append(originalSearchString).append("%");
	    return buffer.toString();
	}
}
