package com.idega.user.presentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.swing.event.ChangeListener;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.data.Address;
import com.idega.core.data.Email;
import com.idega.core.data.Phone;
import com.idega.data.GenericEntity;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
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

	public static final String PARAMETER_DELETE_USERS = "delete_users";
	public static final String DELETE_USERS_KEY = "delete_selected_users";
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
					
					SubmitButton deleteButton =
						new SubmitButton(
							resourceBundle.getLocalizedImageButton("Delete selection", "Delete selection"),
							BasicUserOverview.DELETE_USERS_KEY,
							BasicUserOverview.DELETE_USERS_KEY);
					deleteButton.setSubmitConfirm("Delete selected users?");
					form.add(deleteButton);
				}
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
		if( toolbar == null){
			toolbar = new BasicUserOverViewToolbar();
		}

		if( selectedGroup != null ){
			toolbar.setSelectedGroup(selectedGroup);
			toolbar.setDomain(ps.getParentDomainOfSelection());
			toolbar.setParentGroup(ps.getParentGroupOfSelection());
		}
		
		return toolbar;
				
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
					address = BasicUserOverview.getUserBusiness(iwc).getUsersMainAddress(user);
				}
				catch (RemoteException ex) {
					System.err.println("[BasicUserOverview]: Address could not be retrieved.Message was :" + ex.getMessage());
					ex.printStackTrace(System.err);
				}
				StringBuffer displayValues = new StringBuffer();
				values = path.getValues((GenericEntity) address);
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
					
				if(!canEditUser && !isCurrentUserSuperAdmin){
					return text; 
				}
				else{		
					Link aLink = new Link(text);
					if (!user.equals(administratorUser)) {
						aLink.setWindowToOpen(UserPropertyWindow.class);
						aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
						
						if(ps.getSelectedGroup()!=null){
							aLink.addParameter("laufey_pjasa", ps.getSelectedGroup().getPrimaryKey().toString());
						}
						
					}
					else if (isCurrentUserSuperAdmin) {
						aLink.setWindowToOpen(AdministratorPropertyWindow.class);
						aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					}
					return aLink;
				}
			}
		};
		// define checkbox button converter class
		EntityToPresentationObjectConverter converterToDeleteButton = new EntityToPresentationObjectConverter() {
    
      public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
        CheckBox checkAllCheckBox = new CheckBox("checkAll");
        checkAllCheckBox.setToCheckOnClick(BasicUserOverview.PARAMETER_DELETE_USERS, "this.checked");
        return checkAllCheckBox;
      } 
      
      public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
				User user = (User) entity;

				if (!user.equals(administratorUser)) {
					CheckBox checkBox = new CheckBox(BasicUserOverview.PARAMETER_DELETE_USERS, Integer.toString(user.getID()));
					return checkBox;
				}
				else
					return new Text("");
			}
		};
		// set default columns
		String nameKey = "com.idega.user.data.User.FIRST_NAME:" + "com.idega.user.data.User.MIDDLE_NAME:"+"com.idega.user.data.User.LAST_NAME";
		String completeAddressKey =
			"com.idega.core.data.Address.STREET_NAME:"
				+ "com.idega.core.data.Address.STREET_NUMBER:"
				+ "com.idega.core.data.Address.P_O_BOX:"
				+ "com.idega.core.data.PostalCode.POSTAL_CODE_ID|POSTAL_CODE:"
				+ "com.idega.core.data.Address.CITY:"
				+ "com.idega.core.data.Country.IC_COUNTRY_ID|COUNTRY_NAME";
		String emailKey = "com.idega.core.data.Email.ADDRESS";
		String phoneKey = "com.idega.core.data.PhoneType.IC_PHONE_TYPE_ID|TYPE_DISPLAY_NAME:" + "com.idega.core.data.Phone.PHONE_NUMBER";
		String pinKey = "com.idega.user.data.User.PERSONAL_ID";
			
		String identifier = (selectedGroup==null)? "" : selectedGroup.getName();
		identifier += (ps.getSelectedDomain() != null) ? ps.getSelectedDomain().getPrimaryKey().toString() : "";
			
		entityBrowser.setEntities(identifier, users);
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
		// set converter for all columns of this class
		entityBrowser.setEntityToPresentationConverter("com.idega.core.data.Address", converterAddress);
		entityBrowser.setEntityToPresentationConverter("com.idega.core.data.Email", converterEmail);
		entityBrowser.setEntityToPresentationConverter("com.idega.core.data.Phone", converterPhone);
		// set foreign entities
		entityBrowser.addEntity("com.idega.core.data.Address");
		entityBrowser.addEntity("com.idega.core.data.Email");
		entityBrowser.addEntity("com.idega.core.data.Phone");
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
}