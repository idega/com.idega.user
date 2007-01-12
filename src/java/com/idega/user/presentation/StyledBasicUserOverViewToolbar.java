package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.app.Toolbar;
import com.idega.user.app.ToolbarElement;
import com.idega.user.business.UserGroupPlugInBusiness;
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
    private Group aliasGroup;
    private boolean hasCreatePermissionForRealGroup = false;
  //  private boolean hasDeletePermissionForRealGroup = false;
    private boolean hasEditPermissionForRealGroup = false;
    private boolean hasOwnerPermissionForRealGroup = false;
    private boolean hasPermitPermissionForRealGroup = false;
    //private boolean hasViewPermissionForRealGroup = false;
    private boolean isCurrentUserSuperAdmin = false;
    private boolean isRoleMaster = false;
    private ICDomain parentDomain;
    private Group parentGroup;
    private Group selectedGroup;
    private String styleButton = "overviewButton";
    private String styledLinkClass = "styledLink";
    //private String styleSelectedBox = "selectedBox";
    
    /**
     * Constructor for StyledBasicUserOverViewToolbar.
     */
    public StyledBasicUserOverViewToolbar() {
    	// default
    }
    
    public StyledBasicUserOverViewToolbar(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
    
    /**
     * Returns the domain.
     * @return IBDomain
     */
    public ICDomain getDomain() {
        return this.parentDomain;
    }
    
    /**
     * Returns the parentGroup.
     * @return Group
     */
    public Group getParentGroup() {
        return this.parentGroup;
    }
    
    /**
     * Returns the selectedGroup.
     * @return Group
     */
    public Group getSelectedGroup() {
        return this.selectedGroup;
    }
    
    public void main(final IWContext iwc) throws Exception {
        this.empty();
        this.iwb = getBundle(iwc);
        this.iwrb = getResourceBundle(iwc);
        boolean showISStuff = iwc.getApplicationSettings().getProperty("temp_show_is_related_stuff") != null;
        
        Table toolbar1 = new Table();
        toolbar1.setCellpadding(0);
        toolbar1.setCellspacing(0);
        
        if (this.title != null) {
            Text text = new Text(this.title);
            text.setFontFace(Text.FONT_FACE_VERDANA);
            text.setFontSize(Text.FONT_SIZE_7_HTML_1);
            text.setBold();
            
            toolbar1.add(this.title, 10, 1);
        }
        
        if (this.selectedGroup != null) {
            
            if(this.selectedGroup.isAlias()){
                this.aliasGroup = this.selectedGroup.getAlias();
            }
            
            setAccessPermissions(iwc);
            
            //TODO EIki check alias stuff
            //can a user create a group under an alias?
            
            if (this.hasCreatePermissionForRealGroup) {
                
                Table button = new Table(1, 1);
                button.setStyleClass(this.styleButton);
                button.setAlignment(1,1,"center");
                button.setCellpadding(1);
                Text text = new Text(this.iwrb.getLocalizedString("new.member", "New member"));
                Link tLink11 = new Link(text);
                tLink11.setStyleClass(this.styledLinkClass);
                tLink11.setWindowToOpen(CreateUser.class);
                if (this.aliasGroup!=null){
                    tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, this.aliasGroup.getPrimaryKey().toString());
                }
                else{
                    tLink11.setParameter(CreateUser.PARAMETERSTRING_GROUP_ID, this.selectedGroup.getPrimaryKey().toString());
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
            editGroup.setStyleClass(this.styleButton);
            editGroup.setAlignment(1,1,"center");
            editGroup.setCellpadding(1);
            Text editText = new Text(this.iwrb.getLocalizedString("edit.group", "Edit group"));
            Link editLink = new Link(editText);
            editLink.setStyleClass(this.styledLinkClass);
            if (this.selectedGroup != null) {
				editLink.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, ((Integer) this.selectedGroup.getPrimaryKey()).toString());
			}
            if (this.parentGroup != null) {
				editLink.setParameter(GroupPropertyWindow.PARENT_GROUP_ID_KEY, ((Integer) this.parentGroup.getPrimaryKey()).toString());
			}
            
            editLink.setWindowToOpen(GroupPropertyWindow.class);
            editGroup.add(editLink, 1, 1);
            toolbar1.add(editGroup, 3, 1);
            toolbar1.setAlignment(3,1,"center");

            //permission	
            //TODO Eiki open up seperate windows for the alias group and the permissions
            if( this.isRoleMaster ){
                Table button4 = new Table(1, 1);
                button4.setStyleClass(this.styleButton);
                button4.setAlignment(1,1,"center");
                button4.setCellpadding(1);
                Text text3 = new Text(this.iwrb.getLocalizedString("roles", "Roles"));
                Link tLink12 = new Link(text3);
                tLink12.setStyleClass(this.styledLinkClass);
                SelectGroupEvent selectGroup = new SelectGroupEvent();
                selectGroup.setGroupToSelect(this.selectedGroup.getNodeID());
                
                button4.add(tLink12, 1, 1);
                selectGroup.setSource(this);
                tLink12.addEventModel(selectGroup);
                
                tLink12.setWindowToOpen(GroupRolesWindow.class);
                
                toolbar1.add(button4, 4, 1);
                toolbar1.setAlignment(4,1,"center");
            }
            
            
            if ( this.hasOwnerPermissionForRealGroup || this.hasPermitPermissionForRealGroup) {
                Table button4 = new Table(1, 1);
                button4.setStyleClass(this.styleButton);
                button4.setAlignment(1,1,"center");
                button4.setCellpadding(1);
                Text text3 = new Text(this.iwrb.getLocalizedString("permissions", "Permissions"));
                Link tLink12 = new Link(text3);
                tLink12.setStyleClass(this.styledLinkClass);
                SelectGroupEvent selectGroup = new SelectGroupEvent();
                selectGroup.setGroupToSelect(this.selectedGroup.getNodeID());
                
                button4.add(tLink12, 1, 1);
                selectGroup.setSource(this);
                tLink12.addEventModel(selectGroup);
                
                tLink12.setWindowToOpen(GroupPermissionWindow.class);
                
                toolbar1.add(button4, 5, 1);
                toolbar1.setAlignment(5,1,"center");
            }
            
            if( this.hasOwnerPermissionForRealGroup) {
                // delete button
                
                Table button5 = new Table(1, 1);
                button5.setStyleClass(this.styleButton);
                button5.setAlignment(1,1,"center");
                button5.setCellpadding(1);
                Text text5 = new Text(this.iwrb.getLocalizedString("Delete.group", "Delete group"));
                Link tLink5 = new Link(text5);
                tLink5.setStyleClass(this.styledLinkClass);
                tLink5.setWindowToOpen(DeleteGroupConfirmWindow.class);
                if (this.selectedGroup != null) {
					tLink5.addParameter(DeleteGroupConfirmWindow.GROUP_ID_KEY, ((Integer) this.selectedGroup.getPrimaryKey()).toString());
				}
                if (this.parentGroup != null) {
					tLink5.addParameter(DeleteGroupConfirmWindow.PARENT_GROUP_ID_KEY, ((Integer) this.parentGroup.getPrimaryKey()).toString());
				}
                if (this.parentDomain != null) {
					tLink5.addParameter(DeleteGroupConfirmWindow.PARENT_DOMAIN_ID_KEY, ((Integer) this.parentDomain.getPrimaryKey()).toString());
				}
                button5.add(tLink5, 1, 1);
                toolbar1.add(button5, 6, 1);
                toolbar1.setAlignment(6,1,"center");
            }            
       
            if (this.hasEditPermissionForRealGroup && this.selectedGroup != null) {
                //mass registering button
                if (showISStuff) {
                    Table button3 = new Table(1, 1);
                    button3.setStyleClass(this.styleButton);
                    button3.setAlignment(1,1,"center");
                    button3.setCellpadding(1);
                    Text text3 = new Text(this.iwrb.getLocalizedString("massregistering", "Bulk registering"));
                    Link tLink14 = new Link(text3);
                    tLink14.setStyleClass(this.styledLinkClass);
                    if(this.aliasGroup==null){
                        tLink14.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, this.selectedGroup.getPrimaryKey().toString());
                    }
                    else{
                        tLink14.setParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, this.aliasGroup.getPrimaryKey().toString());
                    }
                    
                    tLink14.setWindowToOpen(MassRegisteringWindow.class);
                    
                    button3.add(tLink14, 1, 1);
                    toolbar1.add(button3, 7, 1);
                    toolbar1.setAlignment(7,1,"center");
                }
        		// adding all plugins that implement the interface ToolbarElement
        		//get plugins
                ///
                // Assertion: selectedGroup is not null
                ///

        		List  toolbarElements = new ArrayList();
        		Group realGroup = (this.aliasGroup == null) ? this.selectedGroup : this.aliasGroup;
            	String selectedGroupID = realGroup.getPrimaryKey().toString();
        		Collection plugins = getGroupBusiness(iwc).getUserGroupPluginsForGroup(realGroup);
        		Iterator iter = plugins.iterator();
        		while (iter.hasNext()) {
        			UserGroupPlugInBusiness pluginBiz = (UserGroupPlugInBusiness) iter.next();
        			List list = pluginBiz.getGroupToolbarElements(realGroup);
        			if (list != null) {
        				toolbarElements.addAll(list);
        			}
        		}
        		// adding some toolbar elements that belong to this bundle
//        		toolbarElements.add(new MassMovingWindowPlugin());
        		// all toolbar elements found, start sorting
        		int column = 8;
        		Comparator priorityComparator = new Comparator() {
        			
        			public int compare(Object toolbarElementA, Object toolbarElementB) {
        				int priorityA = ((ToolbarElement) toolbarElementA).getPriority(iwc);
        				int priorityB = ((ToolbarElement) toolbarElementB).getPriority(iwc);
        				if (priorityA == -1  && priorityB == -1) {
        					return 0;
        				}
        				else if (priorityA == -1) {
        					return 1;
        				}
        				else if (priorityB ==  -1) {
        					return -1;
        				}
        				return priorityA - priorityB;
        			}
        		};
        		Collections.sort(toolbarElements, priorityComparator);
        		// sorting finished
        		Iterator toolbarElementsIterator = toolbarElements.iterator();
        		while (toolbarElementsIterator.hasNext()) {
        			ToolbarElement toolbarElement = (ToolbarElement) toolbarElementsIterator.next();
        			if (toolbarElement.isValid(iwc)) {
        				Class toolPresentationClass = toolbarElement.getPresentationObjectClass(iwc);
        				Map parameterMap = toolbarElement.getParameterMap(iwc);    
        				if (parameterMap == null) {
        					parameterMap = new HashMap(1);
        				}
        				// note: not all plugins are using that parameter
        				parameterMap.put(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID,selectedGroupID );        				
                        Table toolButton = new Table(1, 1);
                        toolButton.setStyleClass(this.styleButton);
                        toolButton.setAlignment(1,1,"center");
                        toolButton.setCellpadding(1);
                        String toolName = toolbarElement.getName(iwc);
                        Text toolText = new Text(toolName);
                        Link toolLink = new Link(toolText);
                        toolLink.setStyleClass(this.styledLinkClass);
                        toolLink.setParameter(parameterMap);
                        toolLink.setWindowToOpen(toolPresentationClass);
                        toolButton.add(toolLink, 1,1);
                        toolbar1.add(toolButton, column, 1);
                        toolbar1.setAlignment(column++, 1, Table.HORIZONTAL_ALIGN_CENTER);
        			}
            	}
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
        this.isCurrentUserSuperAdmin = iwc.isSuperAdmin();
        
      //  hasViewPermissionForRealGroup = isCurrentUserSuperAdmin;
        this.hasEditPermissionForRealGroup = this.isCurrentUserSuperAdmin;
        //hasDeletePermissionForRealGroup = isCurrentUserSuperAdmin;
        this.hasOwnerPermissionForRealGroup = this.isCurrentUserSuperAdmin;
        this.hasCreatePermissionForRealGroup = this.isCurrentUserSuperAdmin;
        this.hasPermitPermissionForRealGroup = this.isCurrentUserSuperAdmin;
        
        this.isRoleMaster = this.isCurrentUserSuperAdmin;
        
        if (!this.isCurrentUserSuperAdmin){
            
            this.isRoleMaster = accessController.isRoleMaster(iwc);
            
            if(this.aliasGroup!=null){//thats the real group
                this.hasOwnerPermissionForRealGroup = accessController.isOwner(this.aliasGroup, iwc); 
                if(!this.hasOwnerPermissionForRealGroup) {
          //          hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(aliasGroup, iwc);
                    this.hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(this.aliasGroup, iwc);
            //        hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(aliasGroup, iwc);
                    this.hasCreatePermissionForRealGroup = accessController.hasCreatePermissionFor(this.aliasGroup, iwc);
                    this.hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(this.aliasGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
              //      hasViewPermissionForRealGroup = true;
                    this.hasEditPermissionForRealGroup = true;
                //    hasDeletePermissionForRealGroup = true;
                    this.hasCreatePermissionForRealGroup = true;
                    this.hasPermitPermissionForRealGroup = true;
                }
            }
            else if(this.selectedGroup!=null){//the third case: selectedGroup == null happens when doing a search for example
                this.hasOwnerPermissionForRealGroup = accessController.isOwner(this.selectedGroup, iwc); 
                if(!this.hasOwnerPermissionForRealGroup) {
                  //  hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(selectedGroup, iwc);
                    this.hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(this.selectedGroup, iwc);
                   // hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(selectedGroup, iwc);
                    this.hasCreatePermissionForRealGroup = accessController.hasCreatePermissionFor(this.selectedGroup, iwc);
                    this.hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(this.selectedGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
                  //  hasViewPermissionForRealGroup = true;
                    this.hasEditPermissionForRealGroup = true;
                  //  hasDeletePermissionForRealGroup = true;
                    this.hasCreatePermissionForRealGroup = true;
                    this.hasPermitPermissionForRealGroup = true;
                }
            }
            
        }
    }
    
    
    
}
