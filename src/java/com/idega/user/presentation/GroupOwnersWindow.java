package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.user.event.SelectGroupEvent;
import com.idega.util.IWColor;

/**
 * Description: An editor window for the selected groups owner permissions.<br>
 * Company: Idega Software<br> 
 * Copyright: Idega Software 2003<br>
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * 
 */
public class GroupOwnersWindow extends GroupPermissionWindow {//implements StatefullPresentation{
	
	private static final String IW_BUNDLE_IDENTIFIER  = "com.idega.user";
	private static final String PARAM_SELECTED_GROUP_ID  = SelectGroupEvent.PRM_GROUP_ID; //todo remove when using event system
	private static final String PARAM_SAVING  = "gpw_save";

	
	private GroupBusiness groupBiz = null;
	
	private boolean saveChanges = false;

	protected int width = 640;
	protected int height = 480;
	
	private String selectedGroupId = null;
	
	private List permissionType;
	private IWResourceBundle iwrb = null;
	private UserBusiness userBiz = null;
	
	private final String permissionTypeOwner="owner";//HARD CODED TEMPORARY
	
	
	
	
	/**
	 * Constructor for GroupOwnersWindow.
	 */
	public GroupOwnersWindow() {
		super();
					
		setWidth(width);
		setHeight(height);
		setScrollbar(true);
		

	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * @param name
	 */
	public GroupOwnersWindow(String name) {
		super(name);
	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * @param width
	 * @param heigth
	 */
	public GroupOwnersWindow(int width, int heigth) {
		super(width, heigth);
	}
	/**
	 * Constructor for GroupOwnersWindow.
	 * @param name
	 * @param width
	 * @param height
	 */
	public GroupOwnersWindow(String name, int width, int height) {
		super(name, width, height);
	}

	
	public void main(IWContext iwc) throws Exception {
		iwrb = this.getResourceBundle(iwc);
		
		parseAction(iwc);		

		
		if(saveChanges){
			
			AccessController access = iwc.getAccessController();

			try {
			
				String[] values = iwc.getParameterValues(permissionTypeOwner);
				Map permissions = getPermissionMapFromSession(iwc,permissionTypeOwner,false);
				
				if(values!=null && values.length>0){
					
					for (int i = 0; i < values.length; i++) {//different from groupPermissionWindow selectedGroupId and vaules[i] are swapped
						access.setPermission(AccessController.CATEGORY_GROUP_ID,iwc,values[i],selectedGroupId,permissionTypeOwner,Boolean.TRUE);
					}
					
				}
				
				Iterator entries = permissions.values().iterator();
				while (entries.hasNext()) {
					ICPermission permission = (ICPermission) entries.next();
					permission.setPermissionValue(false);
					permission.store();
				}

					
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
				
		//get users, define converter to check is user is an owner	
	  Collection users = this.getUserBusiness(iwc).getUsersInGroup(Integer.parseInt(selectedGroupId));
	
	
		EntityBrowser browser = new EntityBrowser();
		browser.setEntities("gpow_"+selectedGroupId,users);
		browser.setDefaultNumberOfRows(users.size());
		browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
		browser.setWidth(browser.HUNDRED_PERCENT);
		browser.setUseExternalForm(true);
		
		
//	fonts
		Text columnText = new Text();
		columnText.setBold();
		browser.setColumnTextProxy(columnText);
				
		//		set color of rows
		browser.setColorForEvenRows("#FFFFFF");
		browser.setColorForOddRows(IWColor.getHexColorString(246, 246, 247));
		
		int column = 1;
		String nameKey = "com.idega.user.data.User.FIRST_NAME:" + "com.idega.user.data.User.LAST_NAME:" + "com.idega.user.data.User.MIDDLE_NAME";
			
		//browser.setLeadingEntity("com.idega.core.accesscontrol.data.ICPermission");
		//browser.setMandatoryColumn(column,"com.idega.core.accesscontrol.data.ICPermission.GROUP_ID");

		
//	define link converter class
			 EntityToPresentationObjectConverter converterLink = new EntityToPresentationObjectConverter() {
				 private com.idega.core.user.data.User administrator = null;
				 private boolean loggedInUserIsAdmin;
         
         public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
           return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);  
         } 
         
				 public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
					 User user = (User) entity;
					 if (administrator == null) {
						 try {
							 administrator = iwc.getAccessController().getAdministratorUser();
						 }
						 catch (Exception ex) {
							 System.err.println("[BasicUserOverview] access controller failed " + ex.getMessage());
							 ex.printStackTrace(System.err);
							 administrator = null;
						 }
						 loggedInUserIsAdmin = iwc.isSuperAdmin();
					 }
					 PresentationObject text = browser.getDefaultConverter().getPresentationObject(entity, path, browser, iwc);
					 Link aLink = new Link(text);
					 if (!user.equals(administrator)) {
						 aLink.setWindowToOpen(UserPropertyWindow.class);
						 aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					 }
					 else if (loggedInUserIsAdmin) {
						 aLink.setWindowToOpen(AdministratorPropertyWindow.class);
						 aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, user.getPrimaryKey().toString());
					 }
					 return aLink;
				 }
			 };
			 
			browser.setMandatoryColumn(column++,nameKey);
			browser.setEntityToPresentationConverter(nameKey,converterLink);
			//converter ends
			
		// define checkbox button converter class
			EntityToPresentationObjectConverter permissionTypeConverter =
				new EntityToPresentationObjectConverter() {
  
					private com.idega.core.user.data.User administrator = null;
					private boolean loggedInUserIsAdmin;

          
          public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
						Map permissionMap = getPermissionMapFromSession(iwc,permissionTypeOwner,true);
            return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);  
          } 
  
					public PresentationObject getPresentationObject(Object userEntity, EntityPath path,EntityBrowser browser, IWContext iwc)  {

						User user = (User) userEntity;
						

						Collection col = getAllPermissionOwnedByUser(user,iwc);
    
						Iterator iterator = col.iterator();
					
						boolean isOwner = false;
						
						final String ownerType = permissionTypeOwner;
						String groupId = null;
						String permissionType = null;
						Map permissionMap = getPermissionMapFromSession(iwc,permissionTypeOwner,false);
						
						while (iterator.hasNext() && !isOwner) {
							ICPermission perm = (ICPermission) iterator.next();
							groupId = perm.getContextValue();
							permissionType = perm.getPermissionString();
							
							System.out.println("Context value: "+groupId+" permissionType "+permissionType);
							
							isOwner = (ownerType.equals(permissionType)) && groupId.equals(selectedGroupId) && perm.getPermissionValue() ;
							
							if( isOwner ){							
								permissionMap.put(groupId, perm);
							}
						}
					
						
						CheckBox returnObj = new CheckBox(ownerType,user.getPrimaryKey().toString());
						returnObj.setChecked(isOwner);
				
						return returnObj;
					
					}
					
					private Collection getAllPermissionOwnedByUser(User user, IWContext iwc){
						Collection allPermissions = null;
		
						try {
								allPermissions = AccessControl.getAllGroupPermissionsOwnedByGroup( user.getGroup() );
						}
						catch (Exception e) {
							e.printStackTrace();
						} 
						return allPermissions;
					}
		
				};

					browser.setMandatoryColumn(column++,permissionTypeOwner);
					browser.setEntityToPresentationConverter( permissionTypeOwner, permissionTypeConverter);	

		
				//converter ends
	
		
		Form form = getGroupPermissionForm(browser);
		form.add(new HiddenInput(PARAM_SELECTED_GROUP_ID,selectedGroupId));
		form.add(new HiddenInput(PARAM_SAVING,"TRUE"));
		add(form);

		
	}
	

	
	/**
	 * Method addGroupPermissionForm.
	 * @param iwc
	 */
	private Form getGroupPermissionForm(EntityBrowser browser) throws Exception{
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("save", "Save"));
		save.setSubmitConfirm(iwrb.getLocalizedString("change.selected.permissions?","Change selected permissions?"));
		
		SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"));
		close.setOnClick("window.close()");
				
		Table table = new Table(1,2);
		table.add(browser,1,1);
		table.add(close,1,2);
		table.add(save,1,2);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
		table.setAlignment(1,2,Table.HORIZONTAL_ALIGN_RIGHT);

		Form form = new Form();
		form.add(table);

		return form;
	}
	
	private void parseAction(IWContext iwc){
		selectedGroupId = iwc.getParameter(GroupOwnersWindow.PARAM_SELECTED_GROUP_ID);
		saveChanges = iwc.isParameterSet(PARAM_SAVING);
		
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	

	public String getName(IWContext iwc){
		IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
		return rBundle.getLocalizedString("group.owners","Group owners");
	}
	
	public GroupBusiness getGroupBusiness(IWContext iwc) {
		if(groupBiz==null){
			
			try {
				groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			
			
		}
		
		return groupBiz;
	}

	/**
	 * @see com.idega.presentation.PresentationObject#getName()
	 */
	public String getName() {
		return "Group owners";
	}
	
	
	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (userBiz == null) {
			try {
				userBiz = (UserBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return userBiz;
	}
	
	

}


