package com.idega.user.presentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.swing.event.ChangeListener;
import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converter.DateConverter;
import com.idega.block.entity.presentation.converter.MessageConverter;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.builder.data.ICDomain;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneType;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.data.EntityRepresentation;
import com.idega.data.GenericEntity;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.IFrame;
import com.idega.presentation.ui.PrintButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWColor;
import com.idega.util.text.TextSoap;
/**
 * Title: User Description: Copyright: Copyright (c) 2001 Company: idega.is
 * 
 * @author 2000 - idega team -<a href="mailto:gummi@idega.is">Gudmundur Agust
 *         Saemundsson</a>
 * @version 1.0
 */
public class BasicUserOverview extends Page implements IWBrowserView, StatefullPresentation {
    
    public static final String SELECTED_USERS_KEY = "selected_users";
    public static final String SELECTED_TARGET_GROUP_KEY = "selected_target_group";
    public static final String SELECTED_GROUP_KEY = "selected_group_key";
    public static final String DELETE_USERS_KEY = "delete_selected_users";
    public static final String MOVE_USERS_KEY = "move_users";

    protected static final String PHONE_TYPE_PATH = PhoneType.class.getName() + ".IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME";
    protected static final String USER_APPLICATION_FRONT_PAGE_ID = "USER_APPLICATION_FRONT_PAGE_ID";
    
    private String _controlTarget = null;
    private IWPresentationEvent _controlEvent = null;
    protected IWResourceBundle iwrb = null;
    private IWBundle iwb = null;
    protected BasicUserOverviewPS _presentationState = null;
    private StyledBasicUserOverViewToolbar toolbar = null;
    private com.idega.core.user.data.User administratorUser = null; //TODO convert to new USER SYSTEM
    
    private boolean isCurrentUserSuperAdmin = false;
    protected BasicUserOverviewPS ps;
    Group selectedGroup;
    protected ICDomain selectedDomain;
    private Group aliasGroup;
    protected AccessController accessController;
    
    private Page parentPage;
    private String styleSrc = "";
    private String styledLink = "styledLinkGeneral";
    private String styledLinkUnderline = "styledLinkUnderline";
    private String styleTable = "borderAll";
    private String topTableStyle = "topTable";
    private String middleTableStyle = "middleTable";
    private boolean hasViewPermissionForRealGroup = false;
    private boolean hasEditPermissionForRealGroup = false;
    private boolean hasDeletePermissionForRealGroup = false;
    private boolean hasOwnerPermissionForRealGroup = false;
	private boolean hasPermitPermissionForRealGroup = false;
    
    public BasicUserOverview() {
        super();
    }
    
    public void setControlEventModel(IWPresentationEvent model) {
        _controlEvent = model;
        if (toolbar == null)
            toolbar = getToolbar();
        toolbar.setControlEventModel(model);
    }
    
    public void setControlTarget(String controlTarget) {
        _controlTarget = controlTarget;
        if (toolbar == null)
            toolbar = getToolbar();
        toolbar.setControlTarget(controlTarget);
    }
    
    protected Collection getEntries(IWContext iwc) {
        Collection users = null;
        try {
            if (selectedGroup != null) {
                if (aliasGroup != null) {
                    users = this.getUserBusiness(iwc).getUsersInGroup(aliasGroup);
                }
                else {
                    users = this.getUserBusiness(iwc).getUsersInGroup(selectedGroup);
                }
            }
            else
                if (selectedDomain != null) {
                    users = this.getUserBusiness(iwc).getAllUsersOrderedByFirstName();
                }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (FinderException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    protected Table getToolbarTable() {
        Table toolbarTable = new Table();
        return toolbarTable;
    }
    protected Table getList(IWContext iwc) {
        
    
        
        
//      create tables and toolbar
        toolbar = getToolbar();	
        Table topTable = topTable();
        Table middleTable = middleTable();
        Table returnTable = returnTable();
        returnTable.add(toolbar, 1, 1);
        returnTable.add(middleTable,1,2);
        returnTable.add(topTable,2,3);
        
        //get the data
        //this must be called here because classes that extend this class such as the searchwindow class
        //do not necesserely need a selectedGroup to work
        Collection users = getEntries(iwc);
        
        if(selectedGroup != null) {
//          adds the name of the group (or alias group)
            //todo add the alias name and an explanation to what real group it points to
            topTable.add(selectedGroup.getName() + Text.NON_BREAKING_SPACE,1,1);
        }
        
        //fill the returnTable
        if (users != null && !users.isEmpty()) {
            
            EntityBrowser entityBrowser = getEntityBrowser(users, iwc);
            // put print button to bottom
            entityBrowser.addPresentationObjectToBottom(new PrintButton(iwb.getImage("print.gif")));
            // put browser into a form
            Form form = new Form();
            form.add(entityBrowser);
            IWPresentationEvent event = (entityBrowser.getPresentationEvent());
            form.addEventModel(event, iwc);
            
//          add delete option
            addDeleteButton(entityBrowser);
            
            
            //add move to group option
            addMoveOrAddButton(entityBrowser);
            
            
            returnTable.add(form, 2, 4);
            
            
        }
        else {
            //this can give inhereting classes a way to add something when there are no users to display
            
            PresentationObject po = getEmptyListPresentationObject();
            
            if (po != null) {
                //why a print button!??
                returnTable.add(new PrintButton(iwb.getImage("print.gif")), 1, 4);
                returnTable.add(po, 2, 4);
            }
            
            
        }
        
        return returnTable;
        
    }
    
    
    private void addDeleteButton(EntityBrowser entityBrowser) {
        //add delete option
        if (hasDeletePermissionForRealGroup) {
            String confirmDeleting = iwrb.getLocalizedString("buo_delete_selected_users", "Delete selected users");
            confirmDeleting += " ?";
            SubmitButton deleteButton =
                new SubmitButton(
                        iwrb.getLocalizedImageButton("Delete selection", "Delete selection"),
                        BasicUserOverview.DELETE_USERS_KEY,
                        BasicUserOverview.DELETE_USERS_KEY);
            deleteButton.setSubmitConfirm(confirmDeleting);
            //form.add(deleteButton);
            //form.add(Text.getNonBrakingSpace());
            entityBrowser.addPresentationObjectToBottom(deleteButton);
            
        }
    }

    private void addMoveOrAddButton(EntityBrowser entityBrowser) {
        String confirmMoving;
        Image buttonMoving;
        boolean addMoveOrAddButton = true;
        if (selectedGroup == null) {
            confirmMoving = iwrb.getLocalizedString("buo_add_selected_users", "Add selected users");
            buttonMoving = iwrb.getLocalizedImageButton("Add to", "Add to");
        }
        else {
            addMoveOrAddButton = hasEditPermissionForRealGroup;
            confirmMoving = iwrb.getLocalizedString("buo_move_selected_users", "Move selected users");
            buttonMoving = iwrb.getLocalizedImageButton("Move to", "Move to");
        
        }
        confirmMoving += " ?";
        
        if(addMoveOrAddButton) {
            SubmitButton moveToButton = new SubmitButton(buttonMoving, BasicUserOverview.MOVE_USERS_KEY, BasicUserOverview.MOVE_USERS_KEY);
            moveToButton.setSubmitConfirm(confirmMoving);
            
       
            GroupChooser targetGroupChooser = new GroupChooser(SELECTED_TARGET_GROUP_KEY);
            targetGroupChooser.setInputStyle(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
            if (selectedGroup != null) {
                targetGroupChooser.setSelectedNode(new GroupTreeNode(selectedGroup));
            }
            
            entityBrowser.addPresentationObjectToBottom(moveToButton);
            entityBrowser.addPresentationObjectToBottom(targetGroupChooser);
        }
    }

    private Table returnTable() {
        Table returnTable = new Table(2, 4);
        returnTable.setCellpaddingAndCellspacing(0);
        returnTable.setWidth(Table.HUNDRED_PERCENT);
        returnTable.setHeight(Table.HUNDRED_PERCENT);
        returnTable.setHeight(4, Table.HUNDRED_PERCENT);
        returnTable.mergeCells(1,1,2,1);
        returnTable.mergeCells(1,2,2,2);
        returnTable.mergeCells(1,3,1,4);
        returnTable.setHeight(1, 42);
        returnTable.setHeight(2,6);
        returnTable.setWidth(1,3,6);
        returnTable.setColor(1,1,"#ffffff");
    		returnTable.setBackgroundImage(1, 1, iwb.getImage("bgtile.gif"));
        returnTable.setColor(1,3,"#f3f3f3");
        returnTable.setColor(2,4,"#f3f3f3");
        returnTable.setStyleClass("grayBorderBottom");
        
        returnTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_TOP);
        returnTable.setVerticalAlignment(2, 4, Table.VERTICAL_ALIGN_TOP);
        returnTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_BOTTOM);
        
        returnTable.setAlignment(1, 1, Table.HORIZONTAL_ALIGN_RIGHT);
        return returnTable;
    }
    
    /**
     * 
     * @return
     */
    public Table topTable() {
        Table topTable = new Table();
        topTable.setCellpadding(0);
        topTable.setCellspacing(3);
        topTable.setStyleClass(topTableStyle);
        topTable.setWidth("100%");
        topTable.setHeight(16);
        return topTable;
    }
    public Table middleTable() {
        Table middleTable = new Table();
        middleTable.setCellpadding(0);
        middleTable.setCellspacing(3);
        middleTable.setStyleClass(middleTableStyle);
        middleTable.setWidth("100%");
        middleTable.setHeight(6);
        return middleTable;
    }
    
    /**
     * This method is called everytime the getEntities method returns null or
     * empty list.
     * 
     * @return a presentation object
     */
    protected PresentationObject getEmptyListPresentationObject() {
        return null;
    }
    
    /**
     * @return StyledBasicUserOverViewToolbar
     */
    protected StyledBasicUserOverViewToolbar getToolbar() {
        IWContext iwc = IWContext.getInstance();
        ps = (BasicUserOverviewPS) this.getPresentationState(iwc);
        if (toolbar == null || selectedGroup == null) {
            toolbar = new StyledBasicUserOverViewToolbar();
        }
        
        if (selectedGroup != null) {
            toolbar.setSelectedGroup(selectedGroup);
            
        }
        toolbar.setDomain(ps.getParentDomainOfSelection());
        toolbar.setParentGroup(ps.getParentGroupOfSelection());
        
        return toolbar;
        
    }
    
    /**
     * @param users
     * @return
     */
    protected EntityBrowser getEntityBrowser(Collection users, IWContext iwc) {
        // define entity browser
        EntityBrowser entityBrowser = EntityBrowser.getInstanceUsingEventSystemAndExternalForm();
        PresentationObject parentObject = this.getParentObject();
        entityBrowser.setArtificialCompoundId(parentObject.getCompoundId(), iwc);
        IWPresentationState presentationStateParent = ((StatefullPresentation) parentObject).getPresentationState(iwc);
        IWPresentationState presentationStateChild = entityBrowser.getPresentationState(iwc);
        ChangeListener[] chListeners = presentationStateParent.getChangeListener();
        if (chListeners != null) {
            for (int i = 0; i < chListeners.length; i++) {
                presentationStateChild.addChangeListener(chListeners[i]);
            }
        }
        // add BasisUserOverviewPs as ActionListener to the entityBrowser
        entityBrowser.addActionListener((IWActionListener) presentationStateParent);
        
        //		define address converter class
        EntityToPresentationObjectConverter converterAddress = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Address address = null;
                try {
                    address = BasicUserOverview.getUserBusiness(iwc).getUsersMainAddress(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was : " + ex.getMessage());
                    
                    ex.printStackTrace(System.err);
                }
                // now the corresponding address was found, now just use the
                // default converter
                return (browser.getDefaultConverter().getPresentationObject((GenericEntity) address, path, browser, iwc));
            }
        };
        
        // define email converter class
        EntityToPresentationObjectConverter converterEmail = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Email email = null;
                try {
                    email = BasicUserOverview.getUserBusiness(iwc).getUserMail(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Email could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                // now the corresponding email was found, now just use the
                // default converter
                return browser.getDefaultConverter().getPresentationObject((GenericEntity) email, path, browser, iwc);
            }
        };
        
        // define phone converter class
        EntityToPresentationObjectConverter converterPhone = new EntityToPresentationObjectConverter() {
        	
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Phone[] phone = null;
                try {
                    phone = BasicUserOverview.getUserBusiness(iwc).getUserPhones(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Phone could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                // now the corresponding address was found, now just use the
                // default converter
                int i;
                Table table = new Table();
                for (i = 0; i < phone.length; i++) {
                    table.add(getPresentationObjectForPhone(phone[i], path, browser, iwc));
                }
                return table;
            }
           
            private PresentationObject getPresentationObjectForPhone(Object genericEntity, EntityPath path, EntityBrowser browser, IWContext iwc)  {
                StringBuffer displayValues = new StringBuffer();
                List list = path.getValues((EntityRepresentation) genericEntity);
                Iterator valueIterator = list.iterator();
                EntityPath currentPath = path;
                while (valueIterator.hasNext()) {
					Object object = valueIterator.next();
                	// if there is no entry the object is null
                	if (object == null) {
                		object = "";
                	}
                	else {
                    	// get localized string for phone type
                    	String shortKey = currentPath.getShortKeySection();
                    	currentPath = path.getNextEntityPath();
                    	String phoneType = object.toString();
                    	if (PHONE_TYPE_PATH.equals(shortKey)) {
                    		object = getBundle(iwc).getResourceBundle(iwc).getLocalizedString(phoneType, phoneType);
                    	}
                	}
                	displayValues.append(object.toString());
                	// append white space
                	displayValues.append(' ');  
                }
                Text text = new Text();
                text.setText(displayValues.toString());               
                return text;
              }
        };
        
        // define special converter class for complete address
        EntityToPresentationObjectConverter converterCompleteAddress = new EntityToPresentationObjectConverter() {
            private List values;
            
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object genericEntity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) genericEntity;
                Address address = null;
                try {
                    address = BasicUserOverview.getUserBusiness(iwc).getUsersCoAddress(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                StringBuffer displayValues = new StringBuffer();
                values = path.getValues((EntityRepresentation) address);
                // com.idega.core.data.Address.STREET_NUMBER plus
                // com.idega.core.data.Address.STREET_NUMBER
                displayValues.append(TextSoap.capitalize(getValue(0))).append(' ').append(getValue(1));
                // com.idega.core.data.Address.P_O_BOX
                String displayValue = getValue(2);
                if (displayValue.length() != 0)
                    displayValues.append(", P.O. Box ").append(displayValue).append(", ");
                // com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE
                // plus com.idega.core.data.Address.CITY
                displayValue = getValue(3);
                if (displayValue.length() != 0)
                    displayValues.append(", ").append(getValue(3)).append(' ').append(getValue(4));
                // com.idega.core.data.Country.IC_COUNTRY_ID|COUNTRY_NAME
                displayValue = getValue(5);
                if (displayValue.length() != 0)
                    displayValues.append(", ").append(displayValue);
                return new Text(displayValues.toString());
            }
            private String getValue(int i) {
                Object object = values.get(i);
                return ((object == null) ? "" : object.toString());
            }
        };
        // define user properties link converter class
        EntityToPresentationObjectConverter converterLink = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                User user = (User) entity;
                
                PresentationObject text = browser.getDefaultConverter().getPresentationObject(entity, path, browser, iwc);
                
                //if(!canEditUser && !isCurrentUserSuperAdmin){//TODO: Eiki
                // move to userpropertywindow instead
                //	return text;
                //}
                //else{
                Link aLink = new Link(text);
                //added to match new style links
                aLink.setStyleClass(styledLinkUnderline);
                boolean isUserSuperAdmin = user.getPrimaryKey().equals(getSuperAdmin(iwc).getPrimaryKey());
                
                if ( !isUserSuperAdmin) {
                    aLink.setWindowToOpen(UserPropertyWindow.class);
                    aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
                    
                    if (selectedGroup != null) {
                        aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID, selectedGroup.getPrimaryKey().toString());
                    }
                    
                }
                else
                    if (isUserSuperAdmin && isCurrentUserSuperAdmin) {
                        aLink.setWindowToOpen(AdministratorPropertyWindow.class);
                        aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
                    }
                return aLink;
                //}
            }
        };
        // define checkbox button converter class
        EntityToPresentationObjectConverter converterToDeleteButton = new EntityToPresentationObjectConverter() {
            
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                CheckBox checkAllCheckBox = new CheckBox("checkAll");
                checkAllCheckBox.setToCheckOnClick(BasicUserOverview.SELECTED_USERS_KEY, "this.checked");
                return checkAllCheckBox;
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                User user = (User) entity;
                
                if (!user.getPrimaryKey().equals(getSuperAdmin(iwc).getPrimaryKey())) {
                    CheckBox checkBox = new CheckBox(BasicUserOverview.SELECTED_USERS_KEY, Integer.toString(user.getID()));
                    return checkBox;
                }
                else
                    return new Text("");
            }
        };
        // set default columns
        String nameKey = User.class.getName() + ".FIRST_NAME:" + User.class.getName() + ".MIDDLE_NAME:" + User.class.getName() + ".LAST_NAME";
        String completeAddressKey =
            Address.class.getName()
            + ".STREET_NAME:"
            + Address.class.getName()
            + ".STREET_NUMBER:"
            + Address.class.getName()
            + ".P_O_BOX:"
            + PostalCode.class.getName()
            + ".POSTAL_CODE_ID|POSTAL_CODE:"
            + Address.class.getName()
            + ".CITY:"
            + Country.class.getName()
            + ".IC_COUNTRY_ID|COUNTRY_NAME";
        String emailKey = Email.class.getName() + ".ADDRESS";
        String phoneKey = PHONE_TYPE_PATH +":" + Phone.class.getName() + ".PHONE_NUMBER";
        String pinKey = User.class.getName() + ".PERSONAL_ID";
        
        String firstNameKey = User.class.getName() + ".FIRST_NAME";
        String middleNameKey = User.class.getName() + ".MIDDLE_NAME";
        String lastNameKey = User.class.getName() + ".LAST_NAME";
        String displayNameKey = User.class.getName() + ".DISPLAY_NAME";
        String descriptionKey = User.class.getName() + ".DESCRIPTION";
        
        String dateOfBirthKey = User.class.getName() + ".DATE_OF_BIRTH";
        
        entityBrowser.setEntities(getEntityBrowserIdentifier(ps), users);
        
        entityBrowser.setDefaultNumberOfRows(Math.min(users.size(), 30));
        //entityBrowser.setLineColor("#DBDCDF");
        entityBrowser.setWidth(Table.HUNDRED_PERCENT);
        //entityBrowser.setLinesBetween(true);
        
        //fonts
        Text column = new Text();
        column.setBold();
        entityBrowser.setColumnTextProxy(column);
        
        //		set color of rows
        entityBrowser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
        entityBrowser.setColorForOddRows("#FFFFFF");
        
        //set a style for the display table:
        entityBrowser.setStyleClass(styleTable);
        
        //entityBrowser.setVerticalZebraColored("#FFFFFF",IWColor.getHexColorString(246,
        // 246, 247)); why does this not work!??
        
        Table emptyTopTable = new Table(1,1);
        emptyTopTable.setCellpadding(0);
        emptyTopTable.setCellspacing(0);
        emptyTopTable.setHeight(16);
        emptyTopTable.emptyCell(1,1);
        
        entityBrowser.add(emptyTopTable);
        
        entityBrowser.setDefaultColumn(1, nameKey);
        entityBrowser.setDefaultColumn(2, pinKey);
        entityBrowser.setDefaultColumn(3, emailKey);
        entityBrowser.setDefaultColumn(4, completeAddressKey);
        entityBrowser.setDefaultColumn(5, phoneKey);
        entityBrowser.setMandatoryColumn(1, "Delete");
        // set special converters
        entityBrowser.setEntityToPresentationConverter("Delete", converterToDeleteButton);
        entityBrowser.setEntityToPresentationConverter(nameKey, converterLink);
        entityBrowser.setEntityToPresentationConverter(completeAddressKey, converterCompleteAddress);
        entityBrowser.setEntityToPresentationConverter(dateOfBirthKey, new DateConverter());
        // set converter for all columns of this class
        entityBrowser.setEntityToPresentationConverter(Address.class.getName(), converterAddress);
        entityBrowser.setEntityToPresentationConverter(Email.class.getName(), converterEmail);
        entityBrowser.setEntityToPresentationConverter(Phone.class.getName(), converterPhone);
        // set foreign entities
        entityBrowser.addEntity(Address.class.getName());
        entityBrowser.addEntity(Email.class.getName());
        entityBrowser.addEntity(Phone.class.getName());
        
        //set the option columns
        entityBrowser.setOptionColumn(0,firstNameKey);
        entityBrowser.setOptionColumn(1,middleNameKey);
        entityBrowser.setOptionColumn(2,lastNameKey);
        entityBrowser.setOptionColumn(3,displayNameKey);
        entityBrowser.setOptionColumn(4,descriptionKey);
        // change display
        entityBrowser.setCellspacing(2);
        
        return entityBrowser;
    }
    
    public void main(IWContext iwc) throws Exception {
        this.empty();
        iwb = this.getBundle(iwc);
        iwrb = this.getResourceBundle(iwc);
        
        this.getParentPage().setAllMargins(0);
        
        accessController = iwc.getAccessController();
        ps = (BasicUserOverviewPS) this.getPresentationState(iwc);
        selectedGroup = ps.getSelectedGroup();
        selectedDomain = ps.getSelectedDomain();
        
        //Sets the alias group as the real group the selectedgroup points to
        checkForAlias();
        
        //gets the super user 
        getSuperAdmin(iwc);
        
        //sets the access permission variables
        setAccessPermissions(iwc);
        
        //if we where moving stuff
        if (getPresentationStateOfBasicUserOverview(iwc).getResultOfMovingUsers() != null) {
            add(getResultList(iwc));
        }
        else if( ps.showSearchResult() ) {
            this.add(getList(iwc));
        }
        else if(selectedGroup!=null) {
            //add the list if we have the permissions to
            if (hasViewPermissionForRealGroup || hasPermitPermissionForRealGroup){
                this.add(getList(iwc));
            }
            else{
                add(iwrb.getLocalizedString("no.view.permission", "You are not allowed to view the data for this group."));
            }
        }
        else {
            //adds a front page if it exists
            addGreetingPage(iwc);	
        }
        
        
    }
    
    private void addGreetingPage(IWContext iwc) {
        //Birna did this
        				Table frameTable = new Table(1,2);
        				frameTable.setCellpaddingAndCellspacing(0);
        				frameTable.setHeight("100%");
        				frameTable.setWidth("100%");
        				frameTable.setHeight(1, 42);
        				frameTable.setBackgroundImage(1, 1, getBundle(iwc).getImage("bgtile.gif"));
        				frameTable.setStyleClass(1, 2, "main");
        				frameTable.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
        				frameTable.setLeftCellBorderWidth(1, 2, 0);
        				frameTable.setRightCellBorderWidth(1, 2, 0);
        
        				Layer layer = new Layer(Layer.DIV);
        				layer.setWidth("100%");
        				layer.setHeight("100%");
        				layer.setOverflow("auto");
                frameTable.add(layer, 1, 2);
        				
        String frontPageId = getBundle(iwc).getProperty(USER_APPLICATION_FRONT_PAGE_ID);
        if(frontPageId!=null && !"-1".equals(frontPageId)) {
            IFrame frontPage = new IFrame();
            frontPage.setHeight("98%");
            frontPage.setWidth("98%");
            frontPage.setIBPage(Integer.parseInt(frontPageId));
            frontPage.setScrolling(IFrame.SCROLLING_NO);
            frontPage.setBorder(0);
            layer.add(frontPage);
        }
        add(frameTable);
    }
    
    /**
     * sets the global access permission variables in this class for the current user
     * @param iwc
     * @throws Exception
     */
    private void setAccessPermissions(IWContext iwc) throws Exception {
        //access control stuff
        isCurrentUserSuperAdmin = iwc.isSuperAdmin();
        
        hasViewPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasEditPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasDeletePermissionForRealGroup = isCurrentUserSuperAdmin;
        hasOwnerPermissionForRealGroup = isCurrentUserSuperAdmin;
        hasPermitPermissionForRealGroup = isCurrentUserSuperAdmin;
        
        if (!isCurrentUserSuperAdmin){
            if(aliasGroup!=null){//thats the real group
                hasOwnerPermissionForRealGroup = accessController.isOwner(aliasGroup, iwc); 
                if(!hasOwnerPermissionForRealGroup) {
                    hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(aliasGroup, iwc);
                    hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(aliasGroup, iwc);
                    hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(aliasGroup, iwc);
                    hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(aliasGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
                    hasViewPermissionForRealGroup = true;
                    hasEditPermissionForRealGroup = true;
                    hasDeletePermissionForRealGroup = true;
                    hasPermitPermissionForRealGroup = true;
                }
            }
            else if(selectedGroup!=null){//the third case: selectedGroup == null happens when doing a search for example
                hasOwnerPermissionForRealGroup = accessController.isOwner(selectedGroup, iwc); 
                if(!hasOwnerPermissionForRealGroup) {
                    hasViewPermissionForRealGroup = accessController.hasViewPermissionFor(selectedGroup, iwc);
                    hasEditPermissionForRealGroup = accessController.hasEditPermissionFor(selectedGroup, iwc);
                    hasDeletePermissionForRealGroup = accessController.hasDeletePermissionFor(selectedGroup, iwc);
                    hasPermitPermissionForRealGroup = accessController.hasPermitPermissionFor(selectedGroup, iwc);
                }
                else {
                    //the user is the owner so he can do anything
                    hasViewPermissionForRealGroup = true;
                    hasEditPermissionForRealGroup = true;
                    hasDeletePermissionForRealGroup = true;
                    hasPermitPermissionForRealGroup = true;
                }
            }
            
        }
    }
    
    /**
     * @param iwc
     */
    private com.idega.core.user.data.User getSuperAdmin(IWContext iwc) {
        if (administratorUser == null) {
            try {
                administratorUser = iwc.getAccessController().getAdministratorUser();
            }
            catch (Exception ex) {
                System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }
        return administratorUser;
    }
    
    /**
     * 
     */
    private void checkForAlias() {
        if (selectedGroup != null) {
            //alias stuff
            if (selectedGroup.getGroupType().equals("alias")) {
                aliasGroup = selectedGroup.getAlias(); 
            }
            //TODO PALLI: Have to fix this once and for all. It looks like the class is cloned and not instanciated, so this variable doesn't become null.
            else {
                aliasGroup = null;
            }
        }
    }
    
    public static UserBusiness getUserBusiness(IWApplicationContext iwc) {
        UserBusiness business = null;
        if (business == null) {
            try {
                business = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
            }
            catch (java.rmi.RemoteException rme) {
                throw new RuntimeException(rme.getMessage());
            }
        }
        return business;
    }
    
    public static GroupBusiness getGroupBusiness(IWApplicationContext iwc) {
        GroupBusiness business = null;
        if (business == null) {
            try {
                business = (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
            }
            catch (java.rmi.RemoteException rme) {
                throw new RuntimeException(rme.getMessage());
            }
        }
        return business;
    }
    
    public static List removeUsers(Collection userIds, Group parentGroup, IWContext iwc) {
        UserBusiness userBusiness = getUserBusiness(iwc.getApplicationContext());
        ArrayList notRemovedUsers = new ArrayList();
        Iterator iterator = userIds.iterator();
        while (iterator.hasNext()) {
            String userId;
            if ((userId = (String) iterator.next()) != null) {
                try {
                    User currentUser = iwc.getCurrentUser();
                    userBusiness.removeUserFromGroup(Integer.parseInt(userId), parentGroup, currentUser);
                }
                catch (RemoveException e) {
                    System.err.println("[BasicUserOverview] user with id " + userId + " could not be removed" + e.getMessage());
                    e.printStackTrace(System.err);
                    notRemovedUsers.add(userId);
                }
            }
        }
        return notRemovedUsers;
    }
    
    public static Map moveUsers(Collection userIds, Group parentGroup, int targetGroupId, IWContext iwc) {
        UserBusiness userBusiness = getUserBusiness(iwc.getApplicationContext());
        //User currentUser = iwc.getCurrentUser();
        Map resultMap = new HashMap();
        Map map = userBusiness.moveUsers(iwc,userIds, parentGroup, targetGroupId);
        Integer groupId;
        if (parentGroup != null) {
            groupId = (Integer) parentGroup.getPrimaryKey();
        }
        else {
            groupId = new Integer(-1);
        }
        // map has user's ids as keys, messages as values
        // if the value is null the corresponding user was successfully moved
        resultMap.put(groupId, map);
        return resultMap;
    }
    
    public static synchronized Map moveContentOfGroups(Collection groups, Collection groupTypesToMoveAmong, IWContext iwc) {
        UserBusiness userBusiness = getUserBusiness(iwc.getApplicationContext());
        return userBusiness.moveUsers(iwc,groups, groupTypesToMoveAmong);
    }
    
    public IWPresentationState getPresentationState(IWUserContext iwuc) {
        if (_presentationState == null) {
            try {
                IWStateMachine stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwuc, IWStateMachine.class);
                _presentationState = (BasicUserOverviewPS) stateMachine.getStateFor(getCompoundId(), this.getPresentationStateClass());
            }
            catch (RemoteException re) {
                throw new RuntimeException(re.getMessage());
            }
        }
        return _presentationState;
    }
    public Class getPresentationStateClass() {
        return BasicUserOverviewPS.class;
    }
    public String getBundleIdentifier() {
        return "com.idega.user";
    }
    
    // necessary because of subclasses
    private BasicUserOverviewPS getPresentationStateOfBasicUserOverview(IWUserContext iwuc) {
        try {
            IWStateMachine stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwuc, IWStateMachine.class);
            String code = IWMainApplication.getEncryptedClassName(BasicUserOverview.class);
            code = ":" + code;
            return (BasicUserOverviewPS) stateMachine.getStateFor(code, BasicUserOverviewPS.class);
        }
        catch (RemoteException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    ////////////////////////////////////////////////////////// hack for friday
    // /////////////////////////////////////////////////////////////////////////////////////
    
    private Table getResultList(IWContext iwc) {
        BasicUserOverviewPS state = getPresentationStateOfBasicUserOverview(iwc);
        String movedUsersNumberMessage = getLocalizedString("number_of_sucessfully_moved_users", "Number of successfully moved users", iwc);
        String notMovedUsersNumberMessage = getLocalizedString("number_of_not_moved_users", "Number of not moved users", iwc);
        String notMovedUsersMessage = getLocalizedString("the_following_users_were_not moved", "Following users were not moved", iwc);
        String success = getLocalizedString("all_users_were_moved_to the_specified_group", "All users were successfully moved.", iwc);
        String target = getLocalizedString("Target", "Target", iwc);
        Map resultOfMovingUsers = state.getResultOfMovingUsers();
        UserBusiness userBusiness = BasicUserOverview.getUserBusiness(iwc);
        GroupBusiness groupBusiness = BasicUserOverview.getGroupBusiness(iwc);
        // map has ids of groups as key and groupMaps as values.
        // groupMaps has user's ids as key and messages as values.
        // if a message is null the corresponding user was successfully moved
        // collect all results, the piece of information about the source is
        // not used yet (perhaps in the future)
        int movedUsers = 0;
        int notMovedUsers = 0;
        Map completeResultOfMoving = new HashMap();
        Collection notMovedUsersColl = new ArrayList();
        Iterator iterator = resultOfMovingUsers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry groupMap = (Map.Entry) iterator.next();
            Map map = (Map) groupMap.getValue();
            Integer groupId = (Integer) groupMap.getKey();
            Group group;
            String groupName;
            if ((new Integer(-1)).equals(groupId)) {
                groupName = "";
            }
            else {
                try {
                    group = groupBusiness.getGroupByGroupID(groupId.intValue());
                }
                // Remote and FinderException
                catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                try {
					groupName = groupBusiness.getNameOfGroupWithParentName(group);
				}
				catch (RemoteException e) {
					  throw new RuntimeException(e.getMessage());
				}
            }
            Iterator entryIterator = map.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) entryIterator.next();
                String message = (String) entry.getValue();
                if (message != null) {
                    notMovedUsers++;
                    Integer userId = (Integer) entry.getKey();
                    StringBuffer buffer = new StringBuffer(groupName);
                    buffer.append(" ").append(message);
                    completeResultOfMoving.put(userId, buffer.toString());
                    try {
                        User notMovedUser = userBusiness.getUser(userId);
                        notMovedUsersColl.add(notMovedUser);
                    }
                    catch (RemoteException ex) {
                        throw new RuntimeException(ex.getMessage());
                    }
                }
                else {
                    movedUsers++;
                }
            }
        }
        int targetGroupId = state.getTargetGroupId();
        Group targetGroup;
        if (targetGroupId > 0) {
            GroupBusiness biz = getGroupBusiness(iwc);
            try {
                targetGroup = biz.getGroupByGroupID(targetGroupId);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
            try {
				String targetName = biz.getNameOfGroupWithParentName(targetGroup);
				movedUsersNumberMessage += ": " + movedUsers + "  " + target + ": " + targetName;
			}
			catch (RemoteException e) {
				  throw new RuntimeException(e.getMessage());
			}
        }
        else {
            movedUsersNumberMessage += ": " + movedUsers;
        }
        notMovedUsersNumberMessage += ": " + notMovedUsers;
        notMovedUsersMessage += ": ";
        
        Text movedUsersNumberMessageText = new Text(movedUsersNumberMessage);
        movedUsersNumberMessageText.setBold(); //setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
        
        Text notMovedUsersNumberMessageText = new Text(notMovedUsersNumberMessage);
        notMovedUsersNumberMessageText.setBold(); //setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
        
        Text notMovedUsersMessageText = new Text(notMovedUsersMessage);
        notMovedUsersMessageText.setBold(); //setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
        Text successText = new Text(success);
        successText.setBold();
        
        Table table = new Table(1, 4);
        table.add(movedUsersNumberMessageText, 1, 1);
        if (notMovedUsers > 0) {
            EntityBrowser browser = getEntityBrowserForResult(notMovedUsersColl, completeResultOfMoving, state, iwc);
            // put print button to bottom
            browser.addPresentationObjectToBottom(new PrintButton(iwb.getImage("print.gif")));
            table.add(notMovedUsersNumberMessageText, 1, 2);
            table.add(notMovedUsersMessageText, 1, 3);
            table.add(browser, 1, 4);
        }
        else
            if (movedUsers > 0) {
                table.add(successText, 1, 2);
            }
        return table;
    }
    
    private EntityBrowser getEntityBrowserForResult(Collection users, Map messageMap, BasicUserOverviewPS state, IWContext iwc) {
        // define entity browser
        EntityBrowser entityBrowser = EntityBrowser.getInstanceUsingEventSystem();
        PresentationObject parentObject = this.getParentObject();
        entityBrowser.setArtificialCompoundId(parentObject.getCompoundId(), iwc);
        IWPresentationState presentationStateParent = ((StatefullPresentation) parentObject).getPresentationState(iwc);
        IWPresentationState presentationStateChild = entityBrowser.getPresentationState(iwc);
        ChangeListener[] chListeners = presentationStateParent.getChangeListener();
        if (chListeners != null) {
            for (int i = 0; i < chListeners.length; i++) {
                presentationStateChild.addChangeListener(chListeners[i]);
            }
        }
        // add BasisUserOverviewPs as ActionListener to the entityBrowser
        entityBrowser.addActionListener((IWActionListener) presentationStateParent);
        
        // define error message converter class
        MessageConverter converterErrorMessage = new MessageConverter();
        // set error message map
        converterErrorMessage.setEntityMessageMap(messageMap);
        
        //    define address converter class
        EntityToPresentationObjectConverter converterAddress = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Address address = null;
                try {
                    address = BasicUserOverview.getUserBusiness(iwc).getUsersCoAddress(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was : " + ex.getMessage());
                    
                    ex.printStackTrace(System.err);
                }
                // now the corresponding address was found, now just use the
                // default converter
                return (browser.getDefaultConverter().getPresentationObject((GenericEntity) address, path, browser, iwc));
            }
        };
        
        // define email converter class
        EntityToPresentationObjectConverter converterEmail = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Email email = null;
                try {
                    email = BasicUserOverview.getUserBusiness(iwc).getUserMail(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Email could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                // now the corresponding email was found, now just use the
                // default converter
                return browser.getDefaultConverter().getPresentationObject((GenericEntity) email, path, browser, iwc);
            }
        };
        
        // define phone converter class
        EntityToPresentationObjectConverter converterPhone = new EntityToPresentationObjectConverter() {
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) entity;
                Phone[] phone = null;
                try {
                    phone = BasicUserOverview.getUserBusiness(iwc).getUserPhones(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Phone could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                // now the corresponding address was found, now just use the
                // default converter
                int i;
                Table table = new Table();
                for (i = 0; i < phone.length; i++) {
                    table.add(browser.getDefaultConverter().getPresentationObject((GenericEntity) phone[i], path, browser, iwc));
                }
                return table;
            }
        };
        // define special converter class for complete address
        EntityToPresentationObjectConverter converterCompleteAddress = new EntityToPresentationObjectConverter() {
            private List values;
            
            public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
                return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
            }
            
            public PresentationObject getPresentationObject(Object genericEntity, EntityPath path, EntityBrowser browser, IWContext iwc) {
                // entity is a user, try to get the corresponding address
                User user = (User) genericEntity;
                Address address = null;
                try {
                    address = BasicUserOverview.getUserBusiness(iwc).getUsersMainAddress(user);
                }
                catch (RemoteException ex) {
                    System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was :" + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
                StringBuffer displayValues = new StringBuffer();
                values = path.getValues((EntityRepresentation) address);
                // com.idega.core.data.Address.STREET_NUMBER plus
                // com.idega.core.data.Address.STREET_NUMBER
                displayValues.append(getValue(0)).append(' ').append(getValue(1));
                // com.idega.core.data.Address.P_O_BOX
                String displayValue = getValue(2);
                if (displayValue.length() != 0)
                    displayValues.append(", P.O. Box ").append(displayValue).append(", ");
                // com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE
                // plus com.idega.core.data.Address.CITY
                displayValue = getValue(3);
                if (displayValue.length() != 0)
                    displayValues.append(", ").append(getValue(3)).append(' ').append(getValue(4));
                // com.idega.core.data.Country.IC_COUNTRY_ID|COUNTRY_NAME
                displayValue = getValue(5);
                if (displayValue.length() != 0)
                    displayValues.append(", ").append(displayValue);
                return new Text(displayValues.toString());
            }
            private String getValue(int i) {
                Object object = values.get(i);
                return ((object == null) ? "" : object.toString());
            }
        };
        
        // set default columns
        String errorMessageKey = "errorMessageKey";
        String nameKey = User.class.getName() + ".FIRST_NAME:" + User.class.getName() + ".MIDDLE_NAME:" + User.class.getName() + ".LAST_NAME";
        String completeAddressKey =
            Address.class.getName()
            + ".STREET_NAME:"
            + Address.class.getName()
            + ".STREET_NUMBER:"
            + Address.class.getName()
            + ".P_O_BOX:"
            + PostalCode.class.getName()
            + ".POSTAL_CODE_ID|POSTAL_CODE:"
            + Address.class.getName()
            + ".CITY:"
            + Country.class.getName()
            + ".IC_COUNTRY_ID|COUNTRY_NAME";
        String emailKey = Email.class.getName() + ".ADDRESS";
        String phoneKey = PhoneType.class.getName() + ".IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME:" + Phone.class.getName() + ".PHONE_NUMBER";
        String pinKey = User.class.getName() + ".PERSONAL_ID";
        
        Iterator iterator = messageMap.keySet().iterator();
        String identifier = (iterator.hasNext()) ? iterator.next().toString() : "move";
        
        entityBrowser.setEntities(identifier, users);
        entityBrowser.setDefaultNumberOfRows(Math.min(users.size(), 22));
        //entityBrowser.setLineColor("#DBDCDF");
        entityBrowser.setWidth(Table.HUNDRED_PERCENT);
        //entityBrowser.setLinesBetween(true);
        
        //fonts
        Text column = new Text();
        column.setBold();
        entityBrowser.setColumnTextProxy(column);
        
        //    set color of rows
        entityBrowser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
        entityBrowser.setColorForOddRows("#FFFFFF");
        
        //entityBrowser.setVerticalZebraColored("#FFFFFF",IWColor.getHexColorString(246,
        // 246, 247)); why does this not work!??
        
        entityBrowser.setDefaultColumn(1, errorMessageKey);
        entityBrowser.setDefaultColumn(2, nameKey);
        entityBrowser.setDefaultColumn(3, pinKey);
        entityBrowser.setDefaultColumn(4, emailKey);
        entityBrowser.setDefaultColumn(5, completeAddressKey);
        entityBrowser.setDefaultColumn(6, phoneKey);
        
        // set special converters
        entityBrowser.setEntityToPresentationConverter(errorMessageKey, converterErrorMessage);
        entityBrowser.setEntityToPresentationConverter(completeAddressKey, converterCompleteAddress);
        // set converter for all columns of this class
        entityBrowser.setEntityToPresentationConverter(Address.class.getName(), converterAddress);
        entityBrowser.setEntityToPresentationConverter(Email.class.getName(), converterEmail);
        entityBrowser.setEntityToPresentationConverter(Phone.class.getName(), converterPhone);
        // set foreign entities
        entityBrowser.addEntity(Address.class.getName());
        entityBrowser.addEntity(Email.class.getName());
        entityBrowser.addEntity(Phone.class.getName());
        // change display
        entityBrowser.setCellspacing(2);
        entityBrowser.setAcceptUserSettingsShowUserSettingsButton(false, false);
        
        return entityBrowser;
    }
    
    protected String getEntityBrowserIdentifier(BasicUserOverviewPS state) {
        
        String identifier = (selectedGroup == null) ? "" : selectedGroup.getPrimaryKey().toString();
        identifier += "_";
        identifier += (state.getSelectedDomain() != null) ? state.getSelectedDomain().getPrimaryKey().toString() : "";
        
        return identifier;
    }
    
}
