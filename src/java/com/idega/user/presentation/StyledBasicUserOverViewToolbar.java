package com.idega.user.presentation;

import com.idega.block.importer.data.ColumnSeparatedImportFile;
import com.idega.block.importer.presentation.Importer;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.app.Toolbar;
import com.idega.user.app.UserApplication;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;

/**
 * 
 * Description: <br>
 * Copyright: Idega Software 2003 <br>
 * Company: Idega Software <br>
 * @author <a href="mailto:birna@idega.is">Birna Iris Jonsdottir</a>
 */
public class StyledBasicUserOverViewToolbar extends Toolbar {
	private Group selectedGroup;
	private Group parentGroup;
	private ICDomain parentDomain;

	public static final String PARAMETERSTRING_GROUP_ID = "ic_group_id";
	
	private String styledLinkClass = "styledLink";
	private String styleSelectedBox = "selectedBox";
	private String styleButton = "overviewButton";

	/**
	 * Constructor for StyledBasicUserOverViewToolbar.
	 */
	public StyledBasicUserOverViewToolbar() {
	}

	public StyledBasicUserOverViewToolbar(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public void main(IWContext iwc) throws Exception {
		this.empty();
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff") != null;
		//boolean showCashierTab = iwc.getApplicationSettings().getProperty("temp_show_isi_cashier_tab") != null;
		AccessController access = iwc.getAccessController();

		Table toolbar1 = new Table();
		toolbar1.setCellpadding(0);
		toolbar1.setCellspacing(0);
		toolbar1.setAlignment("right");
		toolbar1.setVerticalAlignment("bottom");

		if (title != null) {
			Text text = new Text(title);
			text.setFontFace(Text.FONT_FACE_VERDANA);
			text.setFontSize(Text.FONT_SIZE_7_HTML_1);
			text.setBold();

			toolbar1.add(title, 10, 1);
		}
		
		Group aliasGroup = null;
		
		if(selectedGroup != null && selectedGroup.getGroupType().equals("alias")){
			aliasGroup = selectedGroup.getAlias();	
		}

		if (selectedGroup != null) {//TODO EIki check alias stuff
			
			
			//user
			boolean canCreateUserOrGroup = iwc.isSuperAdmin();
			
			if (!canCreateUserOrGroup){
				if(aliasGroup==null){
					canCreateUserOrGroup = access.hasCreatePermissionFor(selectedGroup, iwc);
				}
				else{
					canCreateUserOrGroup = access.hasCreatePermissionFor(aliasGroup, iwc);
				}
			}
			if (!canCreateUserOrGroup){
				if(aliasGroup==null){
					canCreateUserOrGroup = access.isOwner(selectedGroup, iwc);
				}
				else{
					canCreateUserOrGroup = access.isOwner(aliasGroup, iwc);
				}
			}

			if (canCreateUserOrGroup) {
	
				
				Table button = new Table(1, 1);
				button.setStyleClass(styleButton);
				button.setAlignment(1,1,"center");
				button.setCellpadding(0);
				Text text = new Text(iwrb.getLocalizedString("new.member", "New member"));
				Link tLink11 = new Link(text);
				tLink11.setStyleClass(styledLinkClass);
				tLink11.setWindowToOpen(CreateUser.class);
				if (aliasGroup!=null){
					tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, aliasGroup.getPrimaryKey().toString());
				}
				else{
					tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, selectedGroup.getPrimaryKey().toString());
				}
				
				button.add(tLink11, 1, 1);
				toolbar1.add(button, 2, 1);
				toolbar1.setAlignment(2,1,"center");
			}
			//group
			//TODO ADD ALIAS CHECK AND CHANGE THE LINK TO THE GROUP IF OWNER
			boolean canEditGroup = iwc.isSuperAdmin();
			if (!canEditGroup){
				canEditGroup = access.hasEditPermissionFor(selectedGroup, iwc);
			}
			if (!canEditGroup){
				canEditGroup = access.isOwner(selectedGroup, iwc);
				
				if(!canEditGroup && aliasGroup!=null){
					canEditGroup = access.isOwner(aliasGroup, iwc);
				}
			}
			
			
			if (canEditGroup) {
				//edit group
				Table button2 = new Table(1, 1);
				button2.setStyleClass(styleButton);
				button2.setAlignment(1,1,"center");
				button2.setCellpadding(0);
				Text text2 = new Text(iwrb.getLocalizedString("edit.group", "Edit group"));
				Link tLink12 = new Link(text2);
				tLink12.setStyleClass(styledLinkClass);
				if (selectedGroup != null)
					tLink12.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
				if (parentGroup != null)
					tLink12.setParameter(GroupPropertyWindow.PARENT_GROUP_ID_KEY, ((Integer) parentGroup.getPrimaryKey()).toString());
				// tLink12.setWindowToOpen(CreateGroupWindow.class);
				tLink12.setWindowToOpen(GroupPropertyWindow.class);
				button2.add(tLink12, 1, 1);
				toolbar1.add(button2, 3, 1);
				toolbar1.setAlignment(3,1,"center");

				//import button
				if (selectedGroup != null && showISStuff) {
					Table button3 = new Table(1, 1);
					button3.setStyleClass(styleButton);
					button3.setAlignment(1,1,"center");
					button3.setCellpadding(0);
					Text text3 = new Text(iwrb.getLocalizedString("import", "Import"));
					Link tLink14 = new Link(text3);
					tLink14.setStyleClass(styledLinkClass);

					tLink14.setParameter(Importer.PARAMETER_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
					tLink14.setParameter(Importer.PARAMETER_IMPORT_FILE, ColumnSeparatedImportFile.class.getName());
					//TODO: Eiki make plugin based
					tLink14.setParameter(Importer.PARAMETER_IMPORT_HANDLER, "is.idega.idegaweb.member.block.importer.business.PinLookupToGroupImportHandler");

					//setja import handler 
					//setja import file
					tLink14.setWindowToOpen(Importer.class);

					button3.add(tLink14, 1, 1);
					toolbar1.add(button3, 7, 1);
					toolbar1.setAlignment(7,1,"center");
				}

				//mass registering button
				if (selectedGroup != null && showISStuff) {
					Table button3 = new Table(1, 1);
					button3.setStyleClass(styleButton);
					button3.setAlignment(1,1,"center");
					button3.setCellpadding(0);
					Text text3 = new Text(iwrb.getLocalizedString("massregistering", "Bulk registering"));
					Link tLink14 = new Link(text3);
					tLink14.setStyleClass(styledLinkClass);
					if(aliasGroup==null){
						tLink14.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, selectedGroup.getPrimaryKey().toString());
					}
					else{
						tLink14.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, aliasGroup.getPrimaryKey().toString());
					}
					
					tLink14.setWindowToOpen(MassRegisteringWindow.class);

					button3.add(tLink14, 1, 1);
					toolbar1.add(button3, 8, 1);
					toolbar1.setAlignment(8,1,"center");
				}
				
				if (selectedGroup != null && showISStuff &&  ( selectedGroup.getGroupType().equals("iwme_league") || selectedGroup.getGroupType().equals("iwme_club_division_template") ) ) {
					Table button4 = new Table(1, 1);
					button4.setStyleClass(styleButton);
					button4.setAlignment(1,1,"center");
					button4.setCellpadding(0);
					Text text4 = new Text(iwrb.getLocalizedString("updatecdiv", "Update template"));
					Link tLink15 = new Link(text4);
					tLink15.setStyleClass(styledLinkClass);
					
					
					if(aliasGroup==null){
						tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, selectedGroup.getPrimaryKey().toString());
					}
					else{
						tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, aliasGroup.getPrimaryKey().toString());
					}
					
					tLink15.setWindowToOpen("is.idega.idegaweb.member.presentation.UpdateClubDivisionTemplate");

					button4.add(tLink15, 1, 1);
					toolbar1.add(button4, 9, 1);
					toolbar1.setAlignment(9,1,"center");
				}
				else if (selectedGroup != null && showISStuff) {
					Table button4 = new Table(1, 1);
					button4.setStyleClass(styleButton);
					button4.setAlignment(1,1,"center");
					button4.setCellpadding(0);
					Text text4 = new Text(iwrb.getLocalizedString("cashier", "Cashier"));
					Link tLink15 = new Link(text4);
					tLink15.setStyleClass(styledLinkClass);
					tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
					tLink15.setWindowToOpen("is.idega.idegaweb.member.isi.block.accounting.presentation.CashierWindow");

					button4.add(tLink15, 1, 1);
					toolbar1.add(button4, 9, 1);
					toolbar1.setAlignment(9,1,"center");
				}
			}

		}

		//permission	
		
		
		
		
		//TODO Eiki open up seperate windows for the alias group and the permissions
		if (selectedGroup != null) {

			boolean isOwner = isOwner = iwc.isSuperAdmin();
			
			
			if (!isOwner){
				
				isOwner = access.isOwner(selectedGroup, iwc);

				if(!isOwner && aliasGroup!=null){
					isOwner = access.isOwner(aliasGroup, iwc);
				}
				
				
			}
			
			if( access.isRoleMaster(iwc) ){
				Table button4 = new Table(1, 1);
				button4.setStyleClass(styleButton);
				button4.setAlignment(1,1,"center");
				button4.setCellpadding(0);
				Text text3 = new Text(iwrb.getLocalizedString("roles", "Roles"));
				Link tLink12 = new Link(text3);
				tLink12.setStyleClass(styledLinkClass);
				SelectGroupEvent selectGroup = new SelectGroupEvent();
				selectGroup.setGroupToSelect(selectedGroup.getNodeID());

				// set controller (added by Thomas)
				String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
				id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
				selectGroup.setController(id);
				button4.add(tLink12, 1, 1);
				selectGroup.setSource(this);
				tLink12.addEventModel(selectGroup);

				tLink12.setWindowToOpen(GroupRolesWindow.class);

				toolbar1.add(button4, 4, 1);
				toolbar1.setAlignment(4,1,"center");
			}
				

			if (isOwner) {
				Table button4 = new Table(1, 1);
				button4.setStyleClass(styleButton);
				button4.setAlignment(1,1,"center");
				button4.setCellpadding(0);
				Text text3 = new Text(iwrb.getLocalizedString("permissions", "Permissions"));
				Link tLink12 = new Link(text3);
				tLink12.setStyleClass(styledLinkClass);
				SelectGroupEvent selectGroup = new SelectGroupEvent();
				selectGroup.setGroupToSelect(selectedGroup.getNodeID());

				// set controller (added by Thomas)
				String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
				id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
				selectGroup.setController(id);
				button4.add(tLink12, 1, 1);
				selectGroup.setSource(this);
				tLink12.addEventModel(selectGroup);

				tLink12.setWindowToOpen(GroupPermissionWindow.class);

				toolbar1.add(button4, 5, 1);
				toolbar1.setAlignment(5,1,"center");

				// delete button

				Table button5 = new Table(1, 1);
				button5.setStyleClass(styleButton);
				button5.setAlignment(1,1,"center");
				button5.setCellpadding(0);
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
				button5.add(tLink5, 1, 1);
				toolbar1.add(button5, 6, 1);
				toolbar1.setAlignment(6,1,"center");
			}
		}

//		if (selectedGroup != null || this.title != null) {
//			toolbar1.add(new PrintButton(iwb.getImage("print.gif")), 9, 1);
//		}

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

	//	toolbarTable.add(toolbar1, 1, 2);
		add(toolbar1);

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
