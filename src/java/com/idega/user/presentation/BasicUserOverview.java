package com.idega.user.presentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.swing.event.ChangeListener;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converters.DateConverter;
import com.idega.block.entity.presentation.converters.MessageConverter;
import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.data.Address;
import com.idega.core.data.Country;
import com.idega.core.data.Email;
import com.idega.core.data.Phone;
import com.idega.core.data.PhoneType;
import com.idega.core.data.PostalCode;
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
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWColor;
/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson</a>
 * @version 1.0
 */
public class BasicUserOverview extends Page implements IWBrowserView, StatefullPresentation {

	public static final String SELECTED_USERS_KEY = "selected_users";
  public static final String SELECTED_TARGET_GROUP_KEY = "selected_target_group";
  public static final String SELECTED_GROUP_KEY = "selected_group_key";
	public static final String DELETE_USERS_KEY = "delete_selected_users";
  public static final String MOVE_USERS_KEY = "move_users";

  
	private String _controlTarget = null;
	private IWPresentationEvent _controlEvent = null;
	protected IWResourceBundle iwrb = null;
	private IWBundle iwb = null;
	protected BasicUserOverviewPS _presentationState = null;
	private BasicUserOverViewToolbar toolbar = null;
	private com.idega.core.user.data.User administratorUser = null;//TODO convert to new user system
	private boolean isCurrentUserSuperAdmin = false;

	protected boolean canEditUser;
	
	protected BasicUserOverviewPS ps;
	Group selectedGroup;
	protected IBDomain selectedDomain;
	protected Group aliasGroup;
	protected AccessController accessController;
	
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
	
	protected Collection getEntries(IWContext iwc){
		Collection users = null;
		try {
			if ( selectedGroup!= null ){	
				if (aliasGroup != null){
					users = this.getUserBusiness(iwc).getUsersInGroup(aliasGroup);
				}
				else{
					users = this.getUserBusiness(iwc).getUsersInGroup(selectedGroup);
				}
			}
			else if (selectedDomain != null) {
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
	
	
	protected Table getList(IWContext iwc) throws Exception {		
    
    if (getPresentationStateOfBasicUserOverview(iwc).getResultOfMovingUsers() != null)  {
      return getResultList(iwc);
    }
		
		toolbar = getToolbar();
		//	create the return table
		Table returnTable = new Table(1, 2);
		returnTable.setCellpaddingAndCellspacing(0);
		returnTable.setWidth(Table.HUNDRED_PERCENT);
		returnTable.setHeight(Table.HUNDRED_PERCENT);
		returnTable.setHeight(2, Table.HUNDRED_PERCENT);
		returnTable.setHeight(1, 22);
		returnTable.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		
		returnTable.add(toolbar, 1, 1);
			
		//for the link to open the user properties
		boolean canEditUserTemp = false;
		if( selectedGroup!=null ){
			//alias stuff
			if ( selectedGroup.getGroupType().equals("alias") ) {
				aliasGroup = selectedGroup.getAlias();//TODO should I check for permissions on this group?
			}
			
			canEditUserTemp = accessController.hasEditPermissionFor(selectedGroup,iwc);
			if(!canEditUserTemp) canEditUserTemp = accessController.isOwner(selectedGroup,iwc);//is this necessery (eiki)
			if(!canEditUserTemp) canEditUserTemp = isCurrentUserSuperAdmin;

		}
		canEditUser = canEditUserTemp;
		
		
		Collection users = getEntries(iwc);

//fill the returnTable
		if (users != null && !users.isEmpty()) {
			
			EntityBrowser entityBrowser = getEntityBrowser(users,iwc);
			// put browser into a form
			Form form = new Form();
			// switch off the inherent form of the entity browser
			entityBrowser.setUseExternalForm(true);
			form.add(entityBrowser);
			IWPresentationEvent event = (entityBrowser.getPresentationEvent());
			form.addEventModel(event, iwc);
			// add external delete button
			IWResourceBundle resourceBundle = getResourceBundle(iwc);
			
			if( (users.size()>0) && selectedGroup!=null){	
				boolean canDelete = accessController.hasDeletePermissionFor(selectedGroup,iwc);
				if(!canDelete) canDelete = accessController.isOwner(selectedGroup, iwc);
				if(!canDelete) canDelete = isCurrentUserSuperAdmin;
				
				if(canDelete){
          String confirmDeleting = resourceBundle.getLocalizedString("buo_delete_selected_users", "Delete selected users");
          confirmDeleting += " ?";
					SubmitButton deleteButton =
						new SubmitButton(
							resourceBundle.getLocalizedImageButton("Delete selection", "Delete selection"),
							BasicUserOverview.DELETE_USERS_KEY,
							BasicUserOverview.DELETE_USERS_KEY);
					deleteButton.setSubmitConfirm(confirmDeleting);
					//form.add(deleteButton);
          //form.add(Text.getNonBrakingSpace());
          entityBrowser.addPresentationObjectToBottom(deleteButton);
 
				}
			}
			
			// add move to group
			if (users.size()>0) {
        String confirmMoving;
        Image buttonMoving;
        if (selectedGroup == null) {
          confirmMoving = resourceBundle.getLocalizedString("buo_add_selected_users", "Add selected users");
          buttonMoving = resourceBundle.getLocalizedImageButton("Add to", "Add to");
        }
        else {
				  confirmMoving = resourceBundle.getLocalizedString("buo_move_selected_users", "Move selected users");
          buttonMoving = resourceBundle.getLocalizedImageButton("Move to", "Move to");
        }
				confirmMoving += " ?";
        
				SubmitButton moveToButton = 
					new SubmitButton(buttonMoving,
						BasicUserOverview.MOVE_USERS_KEY,
						BasicUserOverview.MOVE_USERS_KEY);
				moveToButton.setSubmitConfirm(confirmMoving);		
        
        // add group drop down list
        //DropdownMenu targetGroupMenu = getGroupList(iwc);
        GroupChooser targetGroupChooser = new GroupChooser(SELECTED_TARGET_GROUP_KEY);
				targetGroupChooser.setInputStyle(IWConstants.BUILDER_FONT_STYLE_INTERFACE);
				if (selectedGroup != null)  {
					targetGroupChooser.setSelectedNode(new GroupTreeNode(selectedGroup));
				} 
        
        //form.add(moveToButton);
        //form.add(Text.getNonBrakingSpace());
        //form.add(targetGroupMenu);
        entityBrowser.addPresentationObjectToBottom(moveToButton);
        entityBrowser.addPresentationObjectToBottom(targetGroupChooser);
			}
			
			
			returnTable.add(form, 1, 2);
			return returnTable;
	
		}
		else{
			PresentationObject po = getEmptyListPresentationObject();
			if( po != null ){
				returnTable.add(po,1,2); 
			}
			
			return returnTable;
		}
		
	}
	/**
	 * This method is called everytime the getEntities method returns null or empty list.
	 * @return a presentation object
	 */
	protected PresentationObject getEmptyListPresentationObject() {
		return null;
	}

	/**
	 * @return BasicUserOverViewToolbar
	 */
	protected BasicUserOverViewToolbar getToolbar() {
		if( toolbar == null || selectedGroup == null){
			toolbar = new BasicUserOverViewToolbar();
		}

		if( selectedGroup != null ){
			toolbar.setSelectedGroup(selectedGroup);
			toolbar.setDomain(ps.getParentDomainOfSelection());
			toolbar.setParentGroup(ps.getParentGroupOfSelection());
		}
		
		return toolbar;
				
	}
  
  private DropdownMenu getGroupList(IWContext iwc) {
    //Group selfGroup = selectedGroup;
    String selfGroupId = "";
    if (selectedGroup != null)  {
      selfGroupId = ((Integer) selectedGroup.getPrimaryKey()).toString();
    } 
    
    DropdownMenu groupList = new DropdownMenu(SELECTED_TARGET_GROUP_KEY);
    GroupBusiness groupBusiness = BasicUserOverview.getGroupBusiness(iwc);
    UserBusiness business = BasicUserOverview.getUserBusiness(iwc);
		User user = iwc.getCurrentUser();
		
//	NOT SUPER USER
    if(!iwc.isSuperAdmin()){
	    Collection coll = business.getAllGroupsWithEditPermission(user, iwc);
	    Iterator iterator = coll.iterator();
	    while (iterator.hasNext())  {
	      Group group = (Group) iterator.next();
	      String id = group.getPrimaryKey().toString();
	      if (! selfGroupId.equals(id)) {
	        String name = groupBusiness.getNameOfGroupWithParentName(group);
	        groupList.addMenuElement(id,name);
	      }
	    }
    }
    else{  //IS SUPER USER
			Collection tops = null;
			try {
				tops = business.getUsersTopGroupNodesByViewAndOwnerPermissions(user,iwc);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
      
			if(tops!=null && !tops.isEmpty()){
				Iterator topGroupsIterator = tops.iterator();
				List allGroups = new ArrayList();
		
				while (topGroupsIterator.hasNext())  {
					Group parentGroup = (Group) topGroupsIterator.next();
					allGroups.add(parentGroup);
					Collection coll = null;
					try {
						coll = groupBusiness.getChildGroupsRecursive(parentGroup);
					}
					catch (EJBException e1) {
						e1.printStackTrace();
					}
					catch (RemoteException e1) {
						e1.printStackTrace();
					}
					if (coll != null) allGroups.addAll(coll);
				}
				
				if(allGroups != null){
					Iterator iter = allGroups.iterator();
					while (iter.hasNext()) {
						Group item = (Group) iter.next();
						String id = item.getPrimaryKey().toString();
						if (!selfGroupId.equals(id)) {
							String name = groupBusiness.getNameOfGroupWithParentName(item);
							groupList.addMenuElement(id,name);
						}
					}
				}
			}
    	
    }
    
    return groupList;
  }

	/**
	 * @param users
	 * @return
	 */
	protected EntityBrowser getEntityBrowser(Collection users, IWContext iwc) {
		// define entity browser
		EntityBrowser entityBrowser = new EntityBrowser();
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
				// now the corresponding address was found, now just use the default converter 
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
				// now the corresponding email was found, now just use the default converter 
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
				// now the corresponding address was found, now just use the default converter 
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
					address = BasicUserOverview.getUserBusiness(iwc).getUsersCoAddress(user);
				}
				catch (RemoteException ex) {
					System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was :" + ex.getMessage());
					ex.printStackTrace(System.err);
				}
				StringBuffer displayValues = new StringBuffer();
				values = path.getValues((EntityRepresentation) address);
				// com.idega.core.data.Address.STREET_NUMBER plus com.idega.core.data.Address.STREET_NUMBER 
				displayValues.append(getValue(0)).append(' ').append(getValue(1));
				// com.idega.core.data.Address.P_O_BOX
				String displayValue = getValue(2);
				if (displayValue.length() != 0)
					displayValues.append(", P.O. Box ").append(displayValue).append(", ");						
					// com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE plus com.idega.core.data.Address.CITY 
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
					
				//if(!canEditUser && !isCurrentUserSuperAdmin){//TODO: Eiki move to userpropertywindow instead
				//	return text; 
				//}
				//else{		
					Link aLink = new Link(text);
					if (!user.equals(administratorUser)) {
						aLink.setWindowToOpen(UserPropertyWindow.class);
						aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					
						if(selectedGroup!=null){
							aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID, selectedGroup.getPrimaryKey().toString());			
						}
						
					}
					else if (user.equals(administratorUser) && isCurrentUserSuperAdmin) {
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

				if (!user.equals(administratorUser)) {
					CheckBox checkBox = new CheckBox(BasicUserOverview.SELECTED_USERS_KEY, Integer.toString(user.getID()));
					return checkBox;
				}
				else
					return new Text("");
			}
		};
		// set default columns
		String nameKey = User.class.getName()+".FIRST_NAME:" + User.class.getName()+".MIDDLE_NAME:"+User.class.getName()+".LAST_NAME";
		String completeAddressKey =
			Address.class.getName()+".STREET_NAME:"
				+ Address.class.getName()+".STREET_NUMBER:"
				+ Address.class.getName()+".P_O_BOX:"
				+ PostalCode.class.getName()+".POSTAL_CODE_ID|POSTAL_CODE:"
				+ Address.class.getName()+".CITY:"
				+ Country.class.getName()+".IC_COUNTRY_ID|COUNTRY_NAME";
		String emailKey = Email.class.getName()+".ADDRESS";
		String phoneKey = PhoneType.class.getName()+".IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME:" + Phone.class.getName()+".PHONE_NUMBER";
		String pinKey = User.class.getName()+".PERSONAL_ID";
    
    String dateOfBirthKey = User.class.getName()+".DATE_OF_BIRTH";
			
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
			
		//entityBrowser.setVerticalZebraColored("#FFFFFF",IWColor.getHexColorString(246, 246, 247)); why does this not work!??
			
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
		
		/*if(selectedGroup!=null){
			iwc.setSessionAttribute(UserPropertyWindow.PARAMETERSTRING_SELECTED_GROUP_ID,selectedGroup.getPrimaryKey().toString());
		}*/

		
		if (administratorUser == null) {
			try {
				administratorUser = iwc.getAccessController().getAdministratorUser();
			}
			catch (Exception ex) {
				System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
				ex.printStackTrace(System.err);
				administratorUser = null;
			}

		}
		
		isCurrentUserSuperAdmin = iwc.isSuperAdmin();
		
		if(selectedGroup!=null && !isCurrentUserSuperAdmin){
			if(accessController.hasViewPermissionFor(selectedGroup,iwc) || accessController.isOwner(selectedGroup,iwc)){
				this.add(getList(iwc));
			}
			else{
				add(iwrb.getLocalizedString("no.view.permission","You are not allowed to view the data for this group."));
			}
		}
		else{
			this.add(getList(iwc));
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
  
  public static Map moveUsers(Collection userIds, Group parentGroup, int targetGroupId, IWContext iwc)  {
    UserBusiness userBusiness = getUserBusiness(iwc.getApplicationContext());
    User currentUser = iwc.getCurrentUser();
    Map resultMap = new HashMap();
    Map map = userBusiness.moveUsers(userIds, parentGroup, targetGroupId, currentUser);
    Integer groupId; 
    if (parentGroup != null) {
      groupId = (Integer) parentGroup.getPrimaryKey();
    }
    else {
      groupId = new Integer(-1);
    }
    // map has user's ids as keys, messages as values
    // if the value is null the corresponding user was successfully moved
    resultMap.put(groupId , map);
    return resultMap;
  }
  
  public static Map moveContentOfGroups(Collection groupIds, String parentGroupType, IWContext iwc)  {
    UserBusiness userBusiness = getUserBusiness(iwc.getApplicationContext());
    User currentUser = iwc.getCurrentUser();
    return userBusiness.moveUsers(groupIds, parentGroupType, currentUser);
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
      return (BasicUserOverviewPS) stateMachine.getStateFor( code , BasicUserOverviewPS.class);
    }
    catch (RemoteException ex)  {
      throw new RuntimeException(ex.getMessage());
    }
  }
 
////////////////////////////////////////////////////////// hack for friday /////////////////////////////////////////////////////////////////////////////////////  
    
  private Table getResultList(IWContext iwc)  {
    BasicUserOverviewPS state = getPresentationStateOfBasicUserOverview(iwc);
    String movedUsersNumberMessage  = getLocalizedString("number_of_sucessfully_moved_users", "Number of successfully moved users", iwc );
    String notMovedUsersNumberMessage  = getLocalizedString("number_of_not_moved_users", "Number of not moved users", iwc );
    String notMovedUsersMessage = getLocalizedString("the_following_users_were_not moved", "Following users were not moved", iwc);
    String success = getLocalizedString("all_users_were_moved_to the_specified_group","All users were successfully moved.",iwc);
    String target = getLocalizedString("Target", "Target", iwc);
    Map resultOfMovingUsers = state.getResultOfMovingUsers();
    UserBusiness userBusiness = BasicUserOverview.getUserBusiness(iwc);
    GroupBusiness groupBusiness = BasicUserOverview.getGroupBusiness(iwc);
    // map has ids of groups as key and groupMaps as values.
    // groupMaps has user's ids as key and messages as values.
    // if a message is null the corresponding user was successfully moved
    // collect all results, the piece of information about the source is not used yet (perhaps in the future)
    int movedUsers = 0;
    int notMovedUsers = 0; 
    Map completeResultOfMoving = new HashMap();
    Collection notMovedUsersColl = new ArrayList();
    Iterator iterator = resultOfMovingUsers.entrySet().iterator();
    while (iterator.hasNext())  {
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
        catch (Exception ex)  {
          throw new RuntimeException(ex.getMessage());
        }
        groupName = groupBusiness.getNameOfGroupWithParentName(group);
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
          catch (RemoteException ex)  {
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
    if (targetGroupId > 0)  {
      GroupBusiness biz = getGroupBusiness(iwc);
      try {
        targetGroup = biz.getGroupByGroupID(targetGroupId);
      }
      catch (Exception ex)  {
        throw new RuntimeException(ex.getMessage());
      }
      String targetName = biz.getNameOfGroupWithParentName(targetGroup);
      movedUsersNumberMessage += ": " + movedUsers + "  " + target + ": " + targetName;
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
    
    Table table = new Table(1,4);
    table.add(movedUsersNumberMessageText,1,1);
    if (notMovedUsers > 0) {
      EntityBrowser browser = getEntityBrowserForResult(notMovedUsersColl, completeResultOfMoving, state, iwc);
      table.add(notMovedUsersNumberMessageText,1,2);
      table.add(notMovedUsersMessageText,1,3);
      table.add(browser,1,4);
    }
    else if (movedUsers > 0) {
      table.add(successText,1,2);
    }
    return table;
  }
  
  private EntityBrowser getEntityBrowserForResult(Collection users, Map messageMap, BasicUserOverviewPS state, IWContext iwc) {
    // define entity browser
    EntityBrowser entityBrowser = new EntityBrowser();
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
        // now the corresponding address was found, now just use the default converter 
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
        // now the corresponding email was found, now just use the default converter 
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
        // now the corresponding address was found, now just use the default converter 
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
        // com.idega.core.data.Address.STREET_NUMBER plus com.idega.core.data.Address.STREET_NUMBER 
        displayValues.append(getValue(0)).append(' ').append(getValue(1));
        // com.idega.core.data.Address.P_O_BOX
        String displayValue = getValue(2);
        if (displayValue.length() != 0)
          displayValues.append(", P.O. Box ").append(displayValue).append(", ");            
          // com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE plus com.idega.core.data.Address.CITY 
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
    String nameKey = User.class.getName()+".FIRST_NAME:" + User.class.getName()+".MIDDLE_NAME:"+User.class.getName()+".LAST_NAME";
    String completeAddressKey =
      Address.class.getName()+".STREET_NAME:"
        + Address.class.getName()+".STREET_NUMBER:"
        + Address.class.getName()+".P_O_BOX:"
        + PostalCode.class.getName()+".POSTAL_CODE_ID|POSTAL_CODE:"
        + Address.class.getName()+".CITY:"
        + Country.class.getName()+".IC_COUNTRY_ID|COUNTRY_NAME";
    String emailKey = Email.class.getName()+".ADDRESS";
    String phoneKey = PhoneType.class.getName()+".IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME:" + Phone.class.getName()+".PHONE_NUMBER";
    String pinKey = User.class.getName()+".PERSONAL_ID";
        
    Iterator iterator = messageMap.keySet().iterator();
    String identifier = (iterator.hasNext()) ? iterator.next().toString() : "move";
      
      
    entityBrowser.setEntities(identifier, users);
    entityBrowser.setDefaultNumberOfRows(Math.min(users.size(), 30));
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
      
    //entityBrowser.setVerticalZebraColored("#FFFFFF",IWColor.getHexColorString(246, 246, 247)); why does this not work!??
      
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
    entityBrowser.setAcceptUserSettingsShowUserSettingsButton(false,false);
    
    return entityBrowser;
  }
  
  protected String getEntityBrowserIdentifier(BasicUserOverviewPS state){
  	
		String identifier = (selectedGroup==null)? "" : selectedGroup.getPrimaryKey().toString();
		identifier +="_";
		identifier += (state.getSelectedDomain() != null) ? state.getSelectedDomain().getPrimaryKey().toString() : "";
		
		
		return identifier;
  }

    
}

