package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;

/**
 * <p>Title: GroupChooserWindow</p>
 * <p>Description: In this window you named a group, select its parent group and group type</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur Agust Saemundsson</a>,<a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.1
 */

public class GroupChooserWindow extends StyledAbstractChooserWindow {
	
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	protected static final String SUBMIT_PARENT_FORM_AFTER_CHANGE = "submit_p_form";
	private static final int _width = 280;
	private static final int _height = 400;
	private static final String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";
	private UserBusiness userBiz = null;
	private String mainStyleClass = "main";
	/**
	 *
	 */
	public GroupChooserWindow() {
		setTitle("Group chooser");
		setWidth(_width);
		setHeight(_height);
		//   setCellpadding(5);
		setScrollbar(true);
		this.getLocation().setApplicationClass(this.getClass());
		this.getLocation().isInPopUpWindow(true);
	}
	
	
	
	/**
	 *
	 */
	public void displaySelection(IWContext iwc) {
		IWResourceBundle iwrb = this.getResourceBundle(iwc);
		addTitle(iwrb.getLocalizedString("select_group","Select group"), TITLE_STYLECLASS);
		setTitle(iwrb.getLocalizedString("select_group","Select group"));
		setName(iwrb.getLocalizedString("select_group","Select group"));
		setStyles();
		Table table = new Table(1,2);
		table.setStyleClass(mainStyleClass);
		table.setCellpaddingAndCellspacing(0);
		
		
		Text text = new Text(iwrb.getLocalizedString("select_group","Select group")+":");
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		table.add(text,1,1);
		
		try {
			//      TreeViewer viewer = com.idega.builder.business.IBPageHelper.getInstance().getPageTreeViewer(iwc);
			
			GroupTreeView viewer = new GroupTreeView();
			
			if(iwc.isSuperAdmin()){
				GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
				viewer.setRootNode(node);
			}
			else{
				UserBusiness biz = getUserBusiness(iwc);
				Collection allGroups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
				// filter groups
				Collection allowedGroupTypes = null;
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
					Iterator iterator = allGroups.iterator();
					while (iterator.hasNext())  {
						Group group = (Group) iterator.next();
						if (checkGroupType(group, allowedGroupTypes))  {
							groups.add(group);
						}
					}
				}
				Collection groupNodes = convertGroupCollectionToGroupNodeCollection(groups,iwc.getApplicationContext());
				viewer.setFirstLevelNodes(groupNodes.iterator());
			} 
			
			viewer.setLocation((IWLocation)this.getLocation().clone());
			viewer.getLocation().setSubID(1);
			
			table.add(viewer,1,2);
			
			add(table,iwc);
			
			viewer.setToMaintainParameter(SCRIPT_PREFIX_PARAMETER,iwc);
			viewer.setToMaintainParameter(SCRIPT_SUFFIX_PARAMETER,iwc);
			viewer.setToMaintainParameter(DISPLAYSTRING_PARAMETER_NAME,iwc);
			viewer.setToMaintainParameter(VALUE_PARAMETER_NAME,iwc);
			viewer.setDefaultOpenLevel(1);
			
			Link link = new Link();
			link.setNoTextObject(true);
			viewer.setLinkPrototype(link);
			viewer.setTreeStyle(_linkStyle);
			viewer.setToUseOnClick();
			//sets the hidden input and textinput of the choosing page

			viewer.setOnClick(SELECT_FUNCTION_NAME+"("+viewer.ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME+","+viewer.ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME+");");
			//
			//    viewer.setNodeActionParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
			//
			viewer.setLinkPrototype(link);
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected String getPerformAfterSelectScriptString(IWContext iwc){
		boolean submitParentFormAfterChange = iwc.isParameterSet(SUBMIT_PARENT_FORM_AFTER_CHANGE);
		String script = null;
		
		if(submitParentFormAfterChange){
			script = iwc.getParameter(SCRIPT_PREFIX_PARAMETER);
			if(script==null){
				script = iwc.getParameter(SCRIPT_PREFIX_IN_A_FRAME);
			}
			
			if(script!=null){
				script = script+"submit();"+super.getPerformAfterSelectScriptString(iwc);
				
			}
			
		}
		else script = super.getPerformAfterSelectScriptString(iwc);
		
		return script;
	}
	
	private void setStyles() {
		String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";
		String _linkHoverStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#FF8008;text-decoration:none;";
		if ( getParentPage() != null ) {
			getParentPage().setStyleDefinition("A",_linkStyle);
			//getParentPage().setStyleDefinition("A."+STYLE_NAME+":visited",_linkStyle);
			//getParentPage().setStyleDefinition("A."+STYLE_NAME+":active",_linkStyle);
			getParentPage().setStyleDefinition("A:hover",_linkHoverStyle);
		}
	}
	
	
	
	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBiz == null) {
			try {
				userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return userBiz;
	}
	
	public Collection convertGroupCollectionToGroupNodeCollection(Collection col, IWApplicationContext iwac){
		List list = new Vector();
		
		Iterator iter = col.iterator();
		while (iter.hasNext()) {
			Group group = (Group) iter.next();
			GroupTreeNode node = new GroupTreeNode(group,iwac);
			list.add(node);
		}
		
		
		return list;
	}
	
	private Collection getGroupTypes(String selectedGroup, IWContext iwc)  {
		Collection groupTypes = new ArrayList();
		Group group = null;
		// get group types
		GroupBusiness groupBusiness;
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
		
		while (iterator!=null && iterator.hasNext())  {
			GroupType item = (GroupType) iterator.next();
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
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
}
