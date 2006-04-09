package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;

/**
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 *
 */
public class GroupSelectionDoubleBox extends SelectionDoubleBox {
	
	private List selectedGroups = null;
	private List availableGroups = null;
	private Map namedAvailableGroups = null;
	private String selectedGroupsParameter = null;
	public static final String selectedGroupsParameterDefaultValue = "iw_us_sel_group_id";
	private Group rootGroup = null;

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 */
	public GroupSelectionDoubleBox() {
		super(selectedGroupsParameterDefaultValue);
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfRightBox
	 */
	public GroupSelectionDoubleBox(String parameterName) {
		super(parameterName);
		this.selectedGroupsParameter = parameterName;
				
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfRightBox
	 * @param headerOfLeftBox
	 * @param headerOfRightBox
	 */
	public GroupSelectionDoubleBox(String parameterName,String headerOfLeftBox,String headerOfRightBox) {
		super(parameterName, headerOfLeftBox, headerOfRightBox);
		this.selectedGroupsParameter = parameterName;
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param headerOfLeftBox
	 * @param headerOfRightBox
	 */
	public GroupSelectionDoubleBox(String headerOfLeftBox,String headerOfRightBox) {
		super(selectedGroupsParameterDefaultValue, headerOfLeftBox, headerOfRightBox);
	}

	/**
	 * @see com.idega.presentation.PresentationObject#main(IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		getRightBox().selectAllOnSubmit();
		
		if( this.rootGroup!=null ){
			setAvailableGroups( com.idega.util.ListUtil.convertCollectionToList(getGroupBusiness(iwc).getChildGroupsRecursive(this.rootGroup)) );
		}
		else if( this.namedAvailableGroups!=null ){
			Iterator iter = this.namedAvailableGroups.keySet().iterator();
			
			while (iter.hasNext()) {
				String key = (String) iter.next();
				
				Group group = (Group)  this.namedAvailableGroups.get(key) ;
				this.addToAvailableBox( ((Integer)group.getPrimaryKey()).toString() , key );
			}
		
		}
		else if( (this.availableGroups != null) &&  !this.availableGroups.isEmpty() ){
			Iterator iter = this.availableGroups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				this.addToAvailableBox( ((Integer)group.getPrimaryKey()).toString() , group.getName() );
			}
		}
		
		if( (this.selectedGroups != null) &&  !this.selectedGroups.isEmpty() ){
			Iterator iter = this.selectedGroups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				this.addToSelectedBox( ((Integer)group.getPrimaryKey()).toString() , group.getName() );
			}
		}
		
	}

	/**
	 * Returns the availableGroups.
	 * @return List
	 */
	public List getAvailableGroups() {
		return this.availableGroups;
	}

	/**
	 * Returns the selectedGroups.
	 * @return List
	 */
	public List getSelectedGroups() {
		return this.selectedGroups;
	}

	/**
	 * Sets the availableGroups.
	 * @param availableGroups The availableGroups to set
	 */
	public void setAvailableGroups(List availableGroups) {
		this.availableGroups = availableGroups;
	}

	/**
	 * Sets the selectedGroups.
	 * @param selectedGroups The selectedGroups to set
	 */
	public void setSelectedGroups(List selectedGroups) {
		this.selectedGroups = selectedGroups;
	}
	
	

	/**
	 * Returns the selectedGroupsParameter.
	 * @return String
	 */
	public String getSelectedGroupsParameter() {
		return (this.selectedGroupsParameter==null) ? selectedGroupsParameterDefaultValue : this.selectedGroupsParameter;
	}

	/**
	 * Sets the selectedGroupsParameter.
	 * @param selectedGroupsParameter The selectedGroupsParameter to set
	 */
	public void setSelectedGroupsParameter(String selectedGroupsParameter) {
		this.selectedGroupsParameter = selectedGroupsParameter;
		this.getRightBox().setName(selectedGroupsParameter);
	}

	/**
	 * Returns the rootGroup.
	 * @return Group
	 */
	public Group getRootGroup() {
		return this.rootGroup;
	} 

	/**
	 * Sets the rootGroup.
	 * @param rootGroup The rootGroup to set
	 */
	public void setRootGroup(Group rootGroup) {
		this.rootGroup = rootGroup;
	}
	
	
	/**
	 * adds a group to the available selection
	 * @param group The group to add
	 */
	public void setAddToAvailableGroups(Group group) {
		if( this.availableGroups==null ) {
			this.availableGroups = new ArrayList();
		}
		this.availableGroups.add(group);
		
	}
	
	/**
	 * adds a group to the available selection
	 * @param group The group to add
	 * @param name display string
	 * 
	 */
	public void setAddToAvailableGroups(Group group, String name) {
		/**
		 * Had to add this method because the former one doesn't work as multivalued.
		 * FIX!!!
		 */
		if( this.namedAvailableGroups==null ) {
			this.namedAvailableGroups = new HashMap();
		}
		this.namedAvailableGroups.put(name, group);
	
		
	}
	
	private GroupBusiness getGroupBusiness(IWContext iwc) throws RemoteException {
		return (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);	
	}

}
