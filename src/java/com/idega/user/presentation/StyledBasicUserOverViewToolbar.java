package com.idega.user.presentation;

import com.idega.block.importer.data.ColumnSeparatedImportFile;
import com.idega.block.importer.presentation.Importer;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.repository.data.ImplementorRepository;
import com.idega.user.app.Toolbar;
import com.idega.user.data.Group;
import com.idega.user.event.SelectGroupEvent;
import com.idega.user.handler.UserPinLookupToGroupImportHandler;

/**
 * 
 * Description: <br>
 * Copyright: Idega Software 2003 <br>
 * Company: Idega Software <br>
 * @author <a href="mailto:birna@idega.is">Birna Iris Jonsdottir</a>
 */
public class StyledBasicUserOverViewToolbar extends Toolbar {
    public static final String PARAMETERSTRING_GROUP_ID = "ic_group_id";
    private Group aliasGroup;
    private boolean hasCreatePermissionForRealGroup = false;
    private boolean hasDeletePermissionForRealGroup = false;
    private boolean hasEditPermissionForRealGroup = false;
    private boolean hasOwnerPermissionForRealGroup = false;
    private boolean hasPermitPermissionForRealGroup = false;
    private boolean hasViewPermissionForRealGroup = false;
    private boolean isCurrentUserSuperAdmin = false;
    private boolean isRoleMaster = false;
    private ICDomain parentDomain;
    private Group parentGroup;
    private Group selectedGroup;
    private String styleButton = "overviewButton";
    private String styledLinkClass = "styledLink";
    private String styleSelectedBox = "selectedBox";
    
    /**
     * Constructor for StyledBasicUserOverViewToolbar.
     */
    public StyledBasicUserOverViewToolbar() {
    }
    
    public StyledBasicUserOverViewToolbar(Group selectedGroup) {
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
     * Returns the selectedGroup.
     * @return Group
     */
    public Group getSelectedGroup() {
        return selectedGroup;
    }
    
    public void main(IWContext iwc) throws Exception {
        this.empty();
        iwb = getBundle(iwc);
        iwrb = getResourceBundle(iwc);
        boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff") != null;
        //boolean showCashierTab = iwc.getApplicationSettings().getProperty("temp_show_isi_cashier_tab") != null;
        
        Table toolbar1 = new Table();
        toolbar1.setCellpadding(0);
        toolbar1.setCellspacing(0);
        
        if (title != null) {
            Text text = new Text(title);
            text.setFontFace(Text.FONT_FACE_VERDANA);
            text.setFontSize(Text.FONT_SIZE_7_HTML_1);
            text.setBold();
            
            toolbar1.add(title, 10, 1);
        }
        
        if (selectedGroup != null) {
            
            if(selectedGroup.isAlias()){
                aliasGroup = selectedGroup.getAlias();
            }
            
            setAccessPermissions(iwc);
            
            //TODO EIki check alias stuff
            //can a user create a group under an alias?
            
            if (hasCreatePermissionForRealGroup) {
                
                Table button = new Table(1, 1);
                button.setStyleClass(styleButton);
                button.setAlignment(1,1,"center");
                button.setCellpadding(1);
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
            //OR ADD TWO LINKS ONE TO THE ALIAS THE OTHER TO THE REAL GROUP
            
            //edit or view group
            Table editGroup = new Table(1, 1);
            editGroup.setStyleClass(styleButton);
            editGroup.setAlignment(1,1,"center");
            editGroup.setCellpadding(1);
            Text editText = new Text(iwrb.getLocalizedString("edit.group", "Edit group"));
            Link editLink = new Link(editText);
            editLink.setStyleClass(styledLinkClass);
            if (selectedGroup != null)
                editLink.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
            if (parentGroup != null)
                editLink.setParameter(GroupPropertyWindow.PARENT_GROUP_ID_KEY, ((Integer) parentGroup.getPrimaryKey()).toString());
            
            editLink.setWindowToOpen(GroupPropertyWindow.class);
            editGroup.add(editLink, 1, 1);
            toolbar1.add(editGroup, 3, 1);
            toolbar1.setAlignment(3,1,"center");
            
            if (hasEditPermissionForRealGroup) {
                //import button
                if (selectedGroup != null && showISStuff) {
                    Table button3 = new Table(1, 1);
                    button3.setStyleClass(styleButton);
                    button3.setAlignment(1,1,"center");
                    button3.setCellpadding(1);
                    Text text3 = new Text(iwrb.getLocalizedString("import", "Import"));
                    Link tLink14 = new Link(text3);
                    tLink14.setStyleClass(styledLinkClass);
                    
                    tLink14.setParameter(Importer.PARAMETER_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
                    tLink14.setParameter(Importer.PARAMETER_IMPORT_FILE, ColumnSeparatedImportFile.class.getName());

                    Class pinLookupToGroupImportHandler = ImplementorRepository.getInstance().getAnyClassImpl(UserPinLookupToGroupImportHandler.class, this.getClass());
                    if (pinLookupToGroupImportHandler != null) {
        				logWarning("[StyledBasicUserOverviewToolbar] Implementation of UserPinLookupToGroupImportHandler could not be found. Implementing bundle was not loaded.");
        				tLink14.setParameter(Importer.PARAMETER_IMPORT_HANDLER, pinLookupToGroupImportHandler.getName());
                    
        				//setja import handler 
        				//setja import file
        				tLink14.setWindowToOpen(Importer.class);
                    
        				button3.add(tLink14, 1, 1);
        				
        				toolbar1.add(button3, 7, 1);
                    }
                    toolbar1.setAlignment(7,1,"center");
                }
                
                //mass registering button
                if (showISStuff) {
                    Table button3 = new Table(1, 1);
                    button3.setStyleClass(styleButton);
                    button3.setAlignment(1,1,"center");
                    button3.setCellpadding(1);
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
                
                if (showISStuff &&  ( selectedGroup.getGroupType().equals("iwme_league") || selectedGroup.getGroupType().equals("iwme_club_division_template") ) ) {
                    Table button4 = new Table(1, 1);
                    button4.setStyleClass(styleButton);
                    button4.setAlignment(1,1,"center");
                    button4.setCellpadding(1);
                    Text text4 = new Text(iwrb.getLocalizedString("updatecdiv", "Update template"));
                    Link tLink15 = new Link(text4);
                    tLink15.setStyleClass(styledLinkClass);
                    
                    
                    if(aliasGroup==null){
                        tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, selectedGroup.getPrimaryKey().toString());
                    }
                    else{
                        tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, aliasGroup.getPrimaryKey().toString());
                    }
                    Class updateClubDivisionTemplate = ImplementorRepository.getInstance().getAnyClassImpl(UserUpdateClubDivisionTemplate.class,this.getClass());
                    if (updateClubDivisionTemplate  != null) {
        				logWarning("[StyledBasicUserOverviewToolbar] Implementation of UserUpdateClubDivisionTemplate could not be found. Implementing bundle was not loaded.");
                
        				tLink15.setWindowToOpen(updateClubDivisionTemplate);
                    
        				button4.add(tLink15, 1, 1);
        				toolbar1.add(button4, 9, 1);
                    }
                    toolbar1.setAlignment(9,1,"center");
                }
                if(selectedGroup.getGroupType().equals("iwma_run")) {
                  Table t = new Table();
                  t.setStyleClass(styleButton);
                  t.setAlignment(1,1,Table.HORIZONTAL_ALIGN_CENTER);
                  t.setCellpadding(1);
                  Text text = new Text(iwrb.getLocalizedString("generate_year","Generate Year Group"));
                  Link l = new Link(text);
                  l.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
                  l.setWindowToOpen("is.idega.idegaweb.marathon.presentation.CreateYearWindow");
                  l.setStyleClass(styledLinkClass);
                  
                  t.add(l,1,1);
                  toolbar1.add(t,10,1);
                  toolbar1.setAlignment(10,1,Table.HORIZONTAL_ALIGN_CENTER);
                }
                
                if (showISStuff) {
                    Table button4 = new Table(1, 1);
                    button4.setStyleClass(styleButton);
                    button4.setAlignment(1,1,"center");
                    button4.setCellpadding(1);
                    Text text4 = new Text(iwrb.getLocalizedString("cashier", "Cashier"));
                    Link tLink15 = new Link(text4);
                    tLink15.setStyleClass(styledLinkClass);
                    tLink15.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) selectedGroup.getPrimaryKey()).toString());
                    Class cashierWindow = ImplementorRepository.getInstance().getAnyClassImpl(UserCashierWindow.class,this.getClass());
                    if (cashierWindow != null) {
        				logWarning("[StyledBasicUserOverviewToolbar] Implementation of UserCashierWindow could not be found. Implementing bundle was not loaded.");
        				tLink15.setWindowToOpen(cashierWindow);
        				button4.add(tLink15, 1, 1);
        				toolbar1.add(button4, 9, 1);
                    }
                    toolbar1.setAlignment(9,1,"center");
                }
            }
            
            
            
            //permission	
            //TODO Eiki open up seperate windows for the alias group and the permissions
            if( isRoleMaster ){
                Table button4 = new Table(1, 1);
                button4.setStyleClass(styleButton);
                button4.setAlignment(1,1,"center");
                button4.setCellpadding(1);
                Text text3 = new Text(iwrb.getLocalizedString("roles", "Roles"));
                Link tLink12 = new Link(text3);
                tLink12.setStyleClass(styledLinkClass);
                SelectGroupEvent selectGroup = new SelectGroupEvent();
                selectGroup.setGroupToSelect(selectedGroup.getNodeID());
                
                button4.add(tLink12, 1, 1);
                selectGroup.setSource(this);
                tLink12.addEventModel(selectGroup);
                
                tLink12.setWindowToOpen(GroupRolesWindow.class);
                
                toolbar1.add(button4, 4, 1);
                toolbar1.setAlignment(4,1,"center");
            }
            
            
            if ( hasOwnerPermissionForRealGroup || hasPermitPermissionForRealGroup) {
                Table button4 = new Table(1, 1);
                button4.setStyleClass(styleButton);
                button4.setAlignment(1,1,"center");
                button4.setCellpadding(1);
                Text text3 = new Text(iwrb.getLocalizedString("permissions", "Permissions"));
                Link tLink12 = new Link(text3);
                tLink12.setStyleClass(styledLinkClass);
                SelectGroupEvent selectGroup = new SelectGroupEvent();
                selectGroup.setGroupToSelect(selectedGroup.getNodeID());
                
                button4.add(tLink12, 1, 1);
                selectGroup.setSource(this);
                tLink12.addEventModel(selectGroup);
                
                tLink12.setWindowToOpen(GroupPermissionWindow.class);
                
                toolbar1.add(button4, 5, 1);
                toolbar1.setAlignment(5,1,"center");
            }
            
            if( hasOwnerPermissionForRealGroup) {
                // delete button
                
                Table button5 = new Table(1, 1);
                button5.setStyleClass(styleButton);
                button5.setAlignment(1,1,"center");
                button5.setCellpadding(1);
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
//      if (selectedGroup != null || this.title != null) {
//      toolbar1.add(new PrintButton(iwb.getImage("print.gif")), 9, 1);
//      }
        
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
    
    /**
     * Sets the selectedGroup.
     * @param selectedGroup The selectedGroup to set
     */
    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
    
    /**
     * sets the global access permission variables in this class for the current user
     * @param iwc
     * @throws Exception
     */
    private void setAccessPermissions(IWContext iwc) throws Exception {
        //access control stuff
        AccessController accessController = iwc.getAccessController();
        isCurrentUserSuperAdmin = iwc.isSuperAdmin();
        
        hasViewPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasEditPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasDeletePermissionForRealGroup = isCurrentUserSuperAdmin;
        hasOwnerPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasCreatePermissionForRealGroup = isCurrentUserSuperAdmin;
        hasPermitPermissionForRealGroup = isCurrentUserSuperAdmin;
        
        isRoleMaster = isCurrentUserSuperAdmin;
        
        if (!isCurrentUserSuperAdmin){
            
            isRoleMaster = accessController.isRoleMaster(iwc);
            
            if(aliasGroup!=null){//thats the real group
                hasOwnerPermissionForRealGroup = accessController.isOwner(aliasGroup, iwc); 
                if(!hasOwnerPermissionForRealGroup) {
                    hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(aliasGroup, iwc);
                    hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(aliasGroup, iwc);
                    hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(aliasGroup, iwc);
                    hasCreatePermissionForRealGroup = accessController.hasCreatePermissionFor(aliasGroup, iwc);
                    hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(aliasGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
                    hasViewPermissionForRealGroup = true;
                    hasEditPermissionForRealGroup = true;
                    hasDeletePermissionForRealGroup = true;
                    hasCreatePermissionForRealGroup = true;
                    hasPermitPermissionForRealGroup = true;
                }
            }
            else if(selectedGroup!=null){//the third case: selectedGroup == null happens when doing a search for example
                hasOwnerPermissionForRealGroup = accessController.isOwner(selectedGroup, iwc); 
                if(!hasOwnerPermissionForRealGroup) {
                    hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(selectedGroup, iwc);
                    hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(selectedGroup, iwc);
                    hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(selectedGroup, iwc);
                    hasCreatePermissionForRealGroup = accessController.hasCreatePermissionFor(selectedGroup, iwc);
                    hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(selectedGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
                    hasViewPermissionForRealGroup = true;
                    hasEditPermissionForRealGroup = true;
                    hasDeletePermissionForRealGroup = true;
                    hasCreatePermissionForRealGroup = true;
                    hasPermitPermissionForRealGroup = true;
                }
            }
            
        }
    }
    
    
    
}
