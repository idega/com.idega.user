package com.idega.user.presentation;

import com.idega.core.accesscontrol.business.AccessController;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.app.Toolbar;
import com.idega.user.app.UserApplication;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;

/**
 * @author eiki
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BasicUserOverViewToolbar extends Toolbar {
	private Group selectedGroup;
	public static final String PARAMETERSTRING_GROUP_ID = "ic_group_id";

	/**
	 * Constructor for BasicUserOverViewToolbar.
	 */
	public BasicUserOverViewToolbar() {
	}

	public BasicUserOverViewToolbar(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);

		Table toolbarTable = new Table(2, 3);
		toolbarTable.setCellpadding(0);
		toolbarTable.setCellspacing(0);
		toolbarTable.setWidth(Table.HUNDRED_PERCENT);
		toolbarTable.setHeight(Table.HUNDRED_PERCENT);
		toolbarTable.setHeight(1, 1);
		toolbarTable.setHeight(3, 1);
		toolbarTable.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_LEFT);
		toolbarTable.setAlignment(2, 2, Table.HORIZONTAL_ALIGN_RIGHT);

		if (selectedGroup != null) {
			Text groupName = new Text(selectedGroup.getName() + Text.NON_BREAKING_SPACE);
			groupName.setFontFace(Text.FONT_FACE_VERDANA);
			groupName.setFontSize(Text.FONT_SIZE_7_HTML_1);
			groupName.setBold();

			toolbarTable.add(groupName, 2, 2);

		}

		IWColor color = new IWColor(230, 230, 230); //jonni color

		toolbarTable.setColor(color);
		toolbarTable.setColor(1, 1, color.brighter());
		toolbarTable.setColor(2, 1, color.brighter());
		toolbarTable.setColor(1, 3, color.darker());
		toolbarTable.setColor(2, 3, color.darker());

		add(toolbarTable);

		Table toolbar1 = new Table();
		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);

		if (selectedGroup != null) {
			//user
			Table button = new Table(2, 1);
			button.setCellpadding(0);
			Image iconCrUser = iwb.getImage("new_user.gif");
			button.add(iconCrUser, 1, 1);
			Text text = new Text(iwrb.getLocalizedString("new.member", "New member"));
			text.setFontFace(Text.FONT_FACE_VERDANA);
			text.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink11 = new Link(text);
			tLink11.setWindowToOpen(CreateUser.class);
			if (selectedGroup.getGroupType().equals("alias"))
				tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getAlias().getPrimaryKey()).toString());
			else
				tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
				
			button.add(tLink11, 2, 1);
			toolbar1.add(button, 2, 1);
			//group

			Table button2 = new Table(2, 1);
			button2.setCellpadding(0);
			Image iconCrGroup = iwb.getImage("new_group.gif");
			button2.add(iconCrGroup, 1, 1);
			Text text2 = new Text(iwrb.getLocalizedString("edit.group", "Edit group"));
			text2.setFontFace(Text.FONT_FACE_VERDANA);
			text2.setFontSize(Text.FONT_SIZE_7_HTML_1);
			Link tLink12 = new Link(text2);
			tLink12.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
			// tLink12.setWindowToOpen(CreateGroupWindow.class);
			tLink12.setWindowToOpen(GroupPropertyWindow.class);
			button2.add(tLink12, 2, 1);
			toolbar1.add(button2, 3, 1);
		}

		//permission	
		if (selectedGroup != null) {
			AccessController access = iwc.getAccessController();
			boolean isOwner = access.isOwner(selectedGroup, iwc);

			if (isOwner) {
				Table button4 = new Table(2, 1);
				button4.setCellpadding(0);
				Image iconCrGroup = iwb.getImage("lock.gif");
				button4.add(iconCrGroup, 1, 1);
				Text text3 = new Text(iwrb.getLocalizedString("permissions", "Permissions"));
				text3.setFontFace(Text.FONT_FACE_VERDANA);
				text3.setFontSize(Text.FONT_SIZE_7_HTML_1);
				Link tLink12 = new Link(text3);
				SelectGroupEvent selectGroup = new SelectGroupEvent();
				selectGroup.setGroupToSelect(selectedGroup.getNodeID());

				// set controller (added by Thomas)
				String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
				id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
				selectGroup.setController(id);
				button4.add(tLink12, 2, 1);
				selectGroup.setSource(this);
				tLink12.addEventModel(selectGroup);

				tLink12.setWindowToOpen(GroupPermissionWindow.class);

				toolbar1.add(button4, 4, 1);
			}
		}

		//calendar
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("calendar","Calendar"), iwb.getImage("calendar.gif"), com.idega.block.news.presentation.News.class),4,1);
		//history
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("history","History"), iwb.getImage("history.gif"), com.idega.block.news.presentation.News.class),5,1);
		//import

		/* 
		 if(selectedGroup!=null){
			Table button3 = new Table(2,1);
			button3.setCellpadding(0);
				Image iconCrGroup = iwb.getImage("import.gif");
				button3.add(iconCrGroup,1,1);
			Text text3 = new Text(iwrb.getLocalizedString("import","Import"));
>>>>>>> 1.15
			text3.setFontFace(Text.FONT_FACE_VERDANA);
			text3.setFontSize(Text.FONT_SIZE_7_HTML_1);
				Link tLink12 = new Link(text3);
				tLink12.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID,((Integer)selectedGroup.getPrimaryKey()).toString() );
			 tLink12.setWindowToOpen(com.idega.block.importer.presentation.Importer.class);
		 
				button3.add(tLink12,2,1);
				toolbar1.add(button3,6,1);
		}
		*/

		//toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("import","Import"), iwb.getImage("import.gif"), com.idega.block.news.presentation.News.class),6,1);
		//export
		//toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("export","Export"), iwb.getImage("export.gif"), com.idega.block.news.presentation.News.class),7,1);
		//bread crumbs
		//VANTAR

		toolbarTable.add(toolbar1, 1, 2);

	}

	/**
	 * Returns the selectedGroup.
	 * @return Group
	 */
	public Group getSelectedGroup() {
		return selectedGroup;
	}

	/**
	 * Sets the selectedGroup.
	 * @param selectedGroup The selectedGroup to set
	 */
	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

}
