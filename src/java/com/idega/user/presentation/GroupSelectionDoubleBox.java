package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;

import com.idega.presentation.IWContext;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.user.data.Group;

/**
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 *
 */
public class GroupSelectionDoubleBox extends SelectionDoubleBox {
	
	private Collection selectedGroups = null;
	private Collection availableGroups = null;

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 */
	public GroupSelectionDoubleBox() {
		super();
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfRightBox
	 */
	public GroupSelectionDoubleBox(String nameOfRightBox) {
		super(nameOfRightBox);
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfRightBox
	 * @param headerOfLeftBox
	 * @param headerOfRightBox
	 */
	public GroupSelectionDoubleBox(
		String nameOfRightBox,
		String headerOfLeftBox,
		String headerOfRightBox) {
		super(nameOfRightBox, headerOfLeftBox, headerOfRightBox);
	}

	/**
	 * Constructor for GroupSelectionDoubleBox.
	 * @param nameOfLeftBox
	 * @param nameOfRightBox
	 */
	public GroupSelectionDoubleBox(
		String nameOfLeftBox,
		String nameOfRightBox) {
		super(nameOfLeftBox, nameOfRightBox);
	}

	/**
	 * @see com.idega.presentation.PresentationObject#main(IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		
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
				this.addToAvailableBox( ((Integer)group.getPrimaryKey()).toString() , group.getName() );
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

}
