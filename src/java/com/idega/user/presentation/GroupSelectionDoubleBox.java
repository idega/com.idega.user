package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

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
	
	private Collection selectedGroups = null;
	private Collection availableGroups = null;
	private String selectedGroupsParameter = null;
	private static final String selectedGroupsParameterDefaultValue = "iw_us_sel_group_id";
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
		selectedGroupsParameter = parameterName;
				
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfRightBox
	 * @param headerOfLeftBox
	 * @param headerOfRightBox
	 */
	public GroupSelectionDoubleBox(String parameterName,String headerOfLeftBox,String headerOfRightBox) {
		super(parameterName, headerOfLeftBox, headerOfRightBox);
		selectedGroupsParameter = parameterName;
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
		
		if( rootGroup!=null ){
			setAvailableGroups(getGroupBusiness(iwc).getGroupsContained(rootGroup));
		}
		
		if( (availableGroups != null) &&  !availableGroups.isEmpty() ){
			Iterator iter = availableGroups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				this.addToAvailableBox( ((Integer)group.getPrimaryKey()).toString() , group.getName() );
			}
		}
		
		if( (selectedGroups != null) &&  !selectedGroups.isEmpty() ){
			Iterator iter = selectedGroups.iterator();
			while (iter.hasNext()) {
				Group group = (Group) iter.next();
				this.addToSelectedBox( ((Integer)group.getPrimaryKey()).toString() , group.getName() );
			}
		}
		
	}

	/**
	 * Returns the availableGroups.
	 * @return Collection
	 */
	public Collection getAvailableGroups() {
		return availableGroups;
	}

	/**
	 * Returns the selectedGroups.
	 * @return Collection
	 */
	public Collection getSelectedGroups() {
		return selectedGroups;
	}

	/**
	 * Sets the availableGroups.
	 * @param availableGroups The availableGroups to set
	 */
	public void setAvailableGroups(Collection availableGroups) {
		this.availableGroups = availableGroups;
	}

	/**
	 * Sets the selectedGroups.
	 * @param selectedGroups The selectedGroups to set
	 */
	public void setSelectedGroups(Collection selectedGroups) {
		this.selectedGroups = selectedGroups;
	}
	
	

	/**
	 * Returns the selectedGroupsParameter.
	 * @return String
	 */
	public String getSelectedGroupsParameter() {
		return (selectedGroupsParameter==null) ? selectedGroupsParameterDefaultValue : selectedGroupsParameter;
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
		return rootGroup;
	}

	/**
	 * Sets the rootGroup.
	 * @param rootGroup The rootGroup to set
	 */
	public void setRootGroup(Group rootGroup) {
		this.rootGroup = rootGroup;
	}
	
	private GroupBusiness getGroupBusiness(IWContext iwc) throws RemoteException {
		return (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);	
	}

}
