package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;

import com.idega.builder.business.PageTreeNode;
import com.idega.builder.presentation.IBPageChooser;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.IFrame;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.GroupType;
import com.idega.user.data.GroupTypeHome;
import com.idega.util.Disposable;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */
public class GeneralGroupInfoTab extends UserGroupTab implements Disposable {
	private TextInput nameField;
	private TextArea descriptionField;
	private IBPageChooser homepageField;
	private DropdownMenu grouptypeField;

	private Text nameText;
	private Text descriptionText;
	private Text homepageText;
	private Text grouptypeText;

	private String nameFieldName;
	private String descriptionFieldName;
	private String homepageFieldName;
	private String grouptypeFieldName; 
	
	private IWResourceBundle _iwrb = null;

	private Link addLink;
	private IFrame memberofFrame;
	public static final String PARAMETER_GROUP_ID = "ic_group_id";
	public static final String SESSIONADDRESS_GROUPS_DIRECTLY_RELATED = "ic_group_ic_group_direct_GGIT";
	public static final String SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED = "ic_group_ic_group_not_direct_GGIT";

	protected Text memberof;

	public GeneralGroupInfoTab() {
		super();
		setName("General");
	}

	public void initFieldContents() {
		addLink.setWindowToOpen(GroupGroupSetter.class);
		addLink.addParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID, getGroupId());

		try {
			Group group = (Group) (((GroupHome) com.idega.data.IDOLookup.getHome(Group.class)).findByPrimaryKey(new Integer(getGroupId())));

			fieldValues.put(nameFieldName, (group.getName() != null) ? group.getName() : "");
			fieldValues.put(descriptionFieldName, (group.getDescription() != null) ? group.getDescription() : "");
			fieldValues.put(homepageFieldName, new Integer(group.getHomePageID()));
			fieldValues.put(grouptypeFieldName, (group.getGroupType() != null) ? group.getGroupType() : "");
			updateFieldsDisplayStatus();
		}
		catch (Exception e) {
			System.err.println("GeneralGroupInfoTab error initFieldContents, GroupId : " + getGroupId());
		}
	}
	
	public void updateFieldsDisplayStatus() {
		nameField.setContent((String) fieldValues.get(nameFieldName));

		descriptionField.setContent((String) fieldValues.get(descriptionFieldName));

		Integer page = (Integer) fieldValues.get(homepageFieldName);
		if (page != null) {
			IWApplicationContext iwc = getIWApplicationContext();
			Map tree = PageTreeNode.getTree(iwc);
			if (tree != null) {
				PageTreeNode node = (PageTreeNode) tree.get(page);
				if (node != null)
					homepageField.setSelectedPage(node.getNodeID(), node.getNodeName());
			}
		}

		String type = (String) fieldValues.get(grouptypeFieldName);
		grouptypeField.setSelectedElement(type);
	}
	
	public void initializeFields() {
		nameField = new TextInput(nameFieldName);
		nameField.setLength(26);

		descriptionField = new TextArea(descriptionFieldName);
		descriptionField.setHeight(5);
		descriptionField.setWidth(43);
		descriptionField.setWrap(true);

		homepageField = new IBPageChooser(homepageFieldName);

		grouptypeField = new DropdownMenu(grouptypeFieldName);
		try {
//			IWResourceBundle iwrb = 
			GroupTypeHome gtHome = (GroupTypeHome) IDOLookup.getHome(GroupType.class);
			Collection types = gtHome.findVisibleGroupTypes();
			Iterator iter = types.iterator();
			while (iter.hasNext()) {
				GroupType item = (GroupType) iter.next();
				String value = item.getType();
				String name = item.getType(); //item.getName();
				if (_iwrb != null)
					grouptypeField.addMenuElement(value, _iwrb.getLocalizedString(name,name));
				else
					grouptypeField.addMenuElement(value,name);
			}
		}
		catch (Exception ex) {
			throw new EJBException(ex);
		}

		memberofFrame = new IFrame("ic_user_memberof_ic_group", GroupList.class);
		memberofFrame.setHeight(150);
		memberofFrame.setWidth(367);
		memberofFrame.setScrolling(IFrame.SCROLLING_YES);
//
		addLink = new Link("  Add/Remove  ");
	}
	
	public void initializeTexts() {
		nameText = getTextObject();
		nameText.setText("Name:");

		descriptionText = getTextObject();
		descriptionText.setText("Description:");

		homepageText = getTextObject();
		homepageText.setText("Home page:");

		grouptypeText = getTextObject();
		grouptypeText.setText("Group type:");

		memberof = getTextObject();
		memberof.setText("Member of:");
	}
	
	public boolean store(IWContext iwc) {
		try {
			if (getGroupId() > -1) {

				Group group = getGroupBusiness(iwc).getGroupByGroupID(getGroupId());
				group.setName((String) fieldValues.get(nameFieldName));
				group.setDescription((String) fieldValues.get(descriptionFieldName));
				group.setHomePageID((Integer) fieldValues.get(homepageFieldName));
				group.setGroupType((String) fieldValues.get(grouptypeFieldName));
			  group.store();
			}
		}
		catch (Exception e) {
			//return false;
			e.printStackTrace(System.err);
			throw new RuntimeException("update group exception");
		}
		return true;
	}
	
	public void lineUpFields() {
		resize(1, 7);
		setCellpadding(0);
		setCellspacing(0);

		Table nameTable = new Table(2, 1);
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setWidth(1, 1, "50");
		nameTable.add(nameText, 1, 1);
		nameTable.add(nameField, 2, 1);
		add(nameTable, 1, 1);

		Table homepageTable = new Table(2, 1);
		homepageTable.setCellpadding(0);
		homepageTable.setCellspacing(0);
		homepageTable.setWidth(1, "50");
		homepageTable.add(homepageText, 1, 1);
		homepageTable.add(homepageField, 2, 1);
		add(homepageTable, 1, 2);
		
		Table grouptypeTable = new Table(2,1);
		grouptypeTable.setCellpadding(0);
		grouptypeTable.setCellspacing(0);
		grouptypeTable.setWidth(1, "50");
		grouptypeTable.add(grouptypeText, 1, 1);
		grouptypeTable.add(grouptypeField, 2, 1);
		add(grouptypeTable,1,3);

		Table descriptionTable = new Table(1, 2);
		descriptionTable.setCellpadding(0);
		descriptionTable.setCellspacing(0);
		descriptionTable.setHeight(1, rowHeight);
		descriptionTable.add(descriptionText, 1, 1);
		descriptionTable.add(descriptionField, 1, 2);
		add(descriptionTable, 1, 4);

		add(memberof, 1, 5);
		add(memberofFrame, 1, 6);

//		setHeight(3, "30");
		setHeight(1, super.rowHeight);
		setHeight(2, super.rowHeight);
		setHeight(3, super.rowHeight);
		setHeight(4, super.rowHeight);
//		setHeight(6, super.rowHeight);

		add(addLink, 1, 7);
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

			String gname = iwc.getParameter(nameFieldName);
			String desc = iwc.getParameter(descriptionFieldName);
			String homepage = iwc.getParameter(homepageFieldName);
			String grouptype = iwc.getParameter(grouptypeFieldName);

			if (gname != null) {
				fieldValues.put(nameFieldName, gname);
			}

			if (desc != null) {
				fieldValues.put(descriptionFieldName, desc);
			}

			if (homepage != null && !homepage.equals("")) {
				Integer page = Integer.valueOf(homepage);
				fieldValues.put(homepageFieldName, page);
			}

			if (grouptype != null) {
				fieldValues.put(grouptypeFieldName, grouptype);
			}
			
			updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}
	
	public void initializeFieldNames() {
		descriptionFieldName = "UM_group_desc";
		nameFieldName = "UM_group_name";
		homepageFieldName = "UM_home_page";
		grouptypeFieldName = "UM_group_type";
	}

	public void initializeFieldValues() {
		fieldValues.put(nameFieldName, "");
		fieldValues.put(descriptionFieldName, "");
		fieldValues.put(homepageFieldName, new Integer(0));
		fieldValues.put(grouptypeFieldName, "");

		updateFieldsDisplayStatus();
	}

	public void dispose(IWContext iwc) {
		iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
		iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
	}

	public void main(IWContext iwc) throws Exception {
		Object obj = getGroupBusiness(iwc).getParentGroups(getGroupId());
		if (obj != null) {
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED, obj);
		}
		else {
			iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
		}

		Object ob = getGroupBusiness(iwc).getParentGroupsInDirect(getGroupId());
		if (ob != null) {
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED, ob);
		}
		else {
			iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
		}
		
		_iwrb = getResourceBundle(iwc);
	}
}