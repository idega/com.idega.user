package com.idega.user.presentation;

import com.idega.block.importer.data.ColumnSeparatedImportFile;
import com.idega.block.importer.presentation.Importer;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.PrintButton;
import com.idega.repository.data.ImplementorRepository;
import com.idega.user.app.Toolbar;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.user.handler.UserPinLookupToGroupImportHandler;
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
	private Group parentGroup;
	private ICDomain parentDomain;

	public static final String PARAMETERSTRING_GROUP_ID = "ic_group_id";
	
	private String styledLinkClass = "styledLink";

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
		boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff") != null;
		AccessController access = iwc.getAccessController();

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
			setTitle(selectedGroup.getName() + Text.NON_BREAKING_SPACE);
		}

		if (title != null) {
			Text text = new Text(title);
			text.setFontFace(Text.FONT_FACE_VERDANA);
			text.setFontSize(Text.FONT_SIZE_7_HTML_1);
			text.setBold();

			toolbarTable.add(title, 2, 2);
			
			IWColor color = new IWColor(230, 230, 230); //jonni color

			toolbarTable.setColor(color);
			toolbarTable.setColor(1, 1, color.brighter());
			toolbarTable.setColor(2, 1, color.brighter());
			toolbarTable.setColor(1, 3, color.darker());
			toolbarTable.setColor(2, 3, color.darker());
		}


		add(toolbarTable);

		Table toolbar1 = new Table();
		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);

		if (selectedGroup != null) {
			//user
			boolean canCreateUserOrGroup = access.hasCreatePermissionFor(selectedGroup, iwc);
			if (!canCreateUserOrGroup)
				canCreateUserOrGroup = access.isOwner(selectedGroup, iwc); //is this necessery (eiki)
			if (!canCreateUserOrGroup)
				canCreateUserOrGroup = iwc.isSuperAdmin();

			if (canCreateUserOrGroup) {

				Table button = new Table(2, 1);
				button.setCellpadding(0);
				Image iconCrUser = iwb.getImage("new_user.gif");
				button.add(iconCrUser, 1, 1);
				Text text = new Text(iwrb.getLocalizedString("new.member", "New member"));
				Link tLink11 = new Link(text);
				tLink11.setStyleClass(styledLinkClass);
				tLink11.setWindowToOpen(CreateUser.class);
				if (selectedGroup.getGroupType().equals("alias"))
					tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getAlias().getPrimaryKey()).toString());
				else
					tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());

				button.add(tLink11, 2, 1);
				toolbar1.add(button, 2, 1);
			}
			//group
			boolean canEditGroup = access.hasEditPermissionFor(selectedGroup, iwc);
			if (!canEditGroup)
				canEditGroup = access.isOwner(selectedGroup, iwc); //is this necessery (eiki)
			if (!canEditGroup)
				canEditGroup = iwc.isSuperAdmin();

			if (canEditGroup) {
				//edit group
				Table button2 = new Table(2, 1);
				button2.setCellpadding(0);
				Image iconCrGroup = iwb.getImage("new_group.gif");
				button2.add(iconCrGroup, 1, 1);
				Text text2 = new Text(iwrb.getLocalizedString("edit.group", "Edit group"));
				Link tLink12 = new Link(text2);
				tLink12.setStyleClass(styledLinkClass);
				if (selectedGroup != null)
					tLink12.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
				if (parentGroup != null)
					tLink12.setParameter(GroupPropertyWindow.PARENT_GROUP_ID_KEY, ((Integer) parentGroup.getPrimaryKey()).toString());
				// tLink12.setWindowToOpen(CreateGroupWindow.class);
				tLink12.setWindowToOpen(GroupPropertyWindow.class);
				button2.add(tLink12, 2, 1);
				toolbar1.add(button2, 3, 1);

				//import button
				if (selectedGroup != null && showISStuff) {
					Table button3 = new Table(2, 1);
					button3.setCellpadding(0);
					Image iconImport = iwb.getImage("import.gif");
					button3.add(iconImport, 1, 1);
					Text text3 = new Text(iwrb.getLocalizedString("import", "Import"));
					Link tLink14 = new Link(text3);
					tLink14.setStyleClass(styledLinkClass);

					tLink14.setParameter(Importer.PARAMETER_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
					tLink14.setParameter(Importer.PARAMETER_IMPORT_FILE, ColumnSeparatedImportFile.class.getName());

					Class pinLookupToGroupImportHandler = ImplementorRepository.getInstance().getAnyClassImpl(UserPinLookupToGroupImportHandler.class, this.getClass());
					if (pinLookupToGroupImportHandler == null) {
						logWarning("[BasicUserOverViewToolbar]  Implementation of UserPinLookupToGroupHandler could not be found. Implementing bundle was not loaded.");
						tLink14.setParameter(Importer.PARAMETER_IMPORT_HANDLER, pinLookupToGroupImportHandler.getName());

						//setja import handler 
						//setja import file
						tLink14.setWindowToOpen(Importer.class);

						button3.add(tLink14, 2, 1);
						toolbar1.add(button3, 6, 1);
					}
				}

				//mass registering button
				if (selectedGroup != null && showISStuff) {
					Table button3 = new Table(2, 1);
					button3.setCellpadding(0);
					Image iconRegister = iwb.getImage("export.gif");
					button3.add(iconRegister, 1, 1);
					Text text3 = new Text(iwrb.getLocalizedString("massregistering", "Bulk registering"));
					Link tLink14 = new Link(text3);
					tLink14.setStyleClass(styledLinkClass);
					tLink14.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
					tLink14.setWindowToOpen(MassRegisteringWindow.class);

					button3.add(tLink14, 2, 1);
					toolbar1.add(button3, 7, 1);
				}
				
				if (selectedGroup != null && showISStuff &&  ( selectedGroup.getGroupType().equals("iwme_league") || selectedGroup.getGroupType().equals("iwme_club_division_template") ) ) {
					Table button4 = new Table(2, 1);
					button4.setCellpadding(0);
					Image iconRegister = iwb.getImage("export.gif");
					button4.add(iconRegister, 1, 1);
					Text text4 = new Text(iwrb.getLocalizedString("updatecdiv", "Update template"));
					Link tLink15 = new Link(text4);
					tLink15.setStyleClass(styledLinkClass);
					tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
                    Class updateClubDivisionTemplate = ImplementorRepository.getInstance().getAnyClassImpl(UserUpdateClubDivisionTemplate.class,this.getClass());
                    if (updateClubDivisionTemplate == null) {
						logWarning("[BasicUserOverViewToolbar]  Implementation of UserUpdateClubDivisionTemplate  could not be found. Implementing bundle was not loaded.");
						tLink15.setWindowToOpen(updateClubDivisionTemplate);
						button4.add(tLink15, 2, 1);
						toolbar1.add(button4, 8, 1);
                    }
				}

			}

		}

		//permission	
		if (selectedGroup != null) {

			boolean isOwner = access.isOwner(selectedGroup, iwc);

			if (!isOwner)
				isOwner = iwc.isSuperAdmin();

			if (isOwner) {
				Table button4 = new Table(2, 1);
				button4.setCellpadding(0);
				Image iconCrGroup = iwb.getImage("lock.gif");
				button4.add(iconCrGroup, 1, 1);
				Text text3 = new Text(iwrb.getLocalizedString("permissions", "Permissions"));
				Link tLink12 = new Link(text3);
				tLink12.setStyleClass(styledLinkClass);
				SelectGroupEvent selectGroup = new SelectGroupEvent();
				selectGroup.setGroupToSelect(selectedGroup.getNodeID());

				button4.add(tLink12, 2, 1);
				selectGroup.setSource(this);
				tLink12.addEventModel(selectGroup);

				tLink12.setWindowToOpen(GroupPermissionWindow.class);

				toolbar1.add(button4, 4, 1);

				// delete button

				Table button5 = new Table(2, 1);
				button5.setCellpadding(0);
				Image iconDeleteGroup = iwb.getImage("toolbar_delete.gif");
				button5.add(iconDeleteGroup, 1, 1);
				Text text5 = new Text(iwrb.getLocalizedString("Delete.group", "Delete group"));
				Link tLink5 = new Link(text5);
				tLink5.setStyleClass(styledLinkClass);
				tLink5.setWindowToOpen(DeleteGroupConfirmWindow.class);
				if (selectedGroup != null)
					tLink5.addParameter(DeleteGroupConfirmWindow.GROUP_ID_KEY, ((Integer) selectedGroup.getPrimaryKey()).toString());
				if (parentGroup != null)
					tLink5.addParameter(DeleteGroupConfirmWindow.PARENT_GROUP_ID_KEY, ((Integer) parentGroup.getPrimaryKey()).toString());
				if (parentDomain != null)
					tLink5.addParameter(DeleteGroupConfirmWindow.PARENT_DOMAIN_ID_KEY, ((Integer) parentDomain.getPrimaryKey()).toString());
				button5.add(tLink5, 2, 1);
				toolbar1.add(button5, 5, 1);
			}
		}

		if (selectedGroup != null || this.title != null) {
			toolbar1.add(new PrintButton(iwb.getImage("print.gif")), 9, 1);
		}

		//calendar
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("calendar","Calendar"), iwb.getImage("calendar.gif"), com.idega.block.news.presentation.News.class),4,1);
		//history
		// toolbar1.add( this.getToolbarButtonWithChangeClassEvent(iwrb.getLocalizedString("history","History"), iwb.getImage("history.gif"), com.idega.block.news.presentation.News.class),5,1);
		//import

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

	/**
	 * Returns the domain.
	 * @return IBDomain
	 */
	public ICDomain getDomain() {
		return parentDomain;
	}

	/**
	 * Returns the parentGroup.
	 * @return Group
	 */
	public Group getParentGroup() {
		return parentGroup;
	}

	/**
	 * Sets the domain.
	 * @param domain The domain to set
	 */
	public void setDomain(ICDomain parentDomain) {
		this.parentDomain = parentDomain;
	}

	/**
	 * Sets the parentGroup.
	 * @param parentGroup The parentGroup to set
	 */
	public void setParentGroup(Group parentGroup) {
		this.parentGroup = parentGroup;
	}

}
