package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;
import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.event.EntityBrowserEvent;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.block.entity.presentation.converters.CheckBoxConverter;
import com.idega.business.IBOLookup;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.app.ToolbarElement;
import com.idega.user.app.UserApplicationMainArea;
import com.idega.user.app.UserApplicationMenuAreaPS;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
//import com.idega.user.data.GroupBMPBean;
import com.idega.user.data.User;

import com.idega.util.IWColor;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 * Created on Apr 14, 2003
 */
public class MassMovingWindow extends IWAdminWindow implements ToolbarElement {
    
  private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";  
  
  public static final String EVENT_NAME = "mass_moving";
  
  public static final String SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY = "selected_group_mm_id_key";
  public static final String MOVE_SELECTED_GROUPS = "move_selected_groups";
  public static final String SELECTED_CHECKED_GROUPS_KEY = "selected_checked_groups_key";
  public static final String SELECTED_TARGET_GROUP_KEY = "selected_target_group_key";
  
  private static final String SHOW_CHILDREN_OF_GROUP_ACTION = "show_children_of_group_action";
  private static final String SHOW_ERROR_MESSAGE_ACTION = "error_message";
  
  public static final String GROUP_TYPE_CLUB = "iwme_club";
  
  // display settings
  private final int NUMBER_OF_ROWS = 15;
  
  private Group group;
  private UserApplicationMenuAreaPS groupProviderState;
  private BasicUserOverviewPS actionListener;
    

  /* (non-Javadoc)
   * @see com.idega.user.app.ToolbarElement#getButtonImage(com.idega.presentation.IWContext)
   */
  public Image getButtonImage(IWContext iwc) {
    IWBundle bundle = this.getBundle(iwc);
    return bundle.getImage("create_group.gif", "Create group");
  }

  /* (non-Javadoc)
   * @see com.idega.user.app.ToolbarElement#getName(com.idega.presentation.IWContext)
   */
  public String getName(IWContext iwc) {
    IWResourceBundle rBundle = this.getBundle(iwc).getResourceBundle(iwc);
    return rBundle.getLocalizedString("massMovingWindow.name", "Move...");
  }


  /* (non-Javadoc)
   * @see com.idega.user.app.ToolbarElement#getPresentationObject(com.idega.presentation.IWContext)
   */
  public PresentationObject getPresentationObject(IWContext iwc) {
    return this;
  }

  public String getBundleIdentifier() {
    return IW_BUNDLE_IDENTIFIER;
  }

  public void main(IWContext iwc) throws Exception {
    IWResourceBundle iwrb = getResourceBundle(iwc);
    setTitle(iwrb.getLocalizedString("searchwindow.title", "Moving"));
    addTitle(iwrb.getLocalizedString("searchwindow.title", "Moving"), IWConstants.BUILDER_FONT_STYLE_TITLE);
    
    String action = parseRequest(iwc);
    if (SHOW_CHILDREN_OF_GROUP_ACTION.equals(action)) {
      showListOfChildren(iwrb, iwc);
    }
  }

  private String parseRequest(IWContext iwc) {
    /*String actionListenerStateId = "";
    if (iwc.isParameterSet(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY)) {
      actionListenerStateId = iwc.getParameter(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY);
    }
    else {
      return SHOW_ERROR_MESSAGE_ACTION;
    }*/
		if (!iwc.isParameterSet(UserApplicationMainArea.USER_APPLICATION_MAIN_AREA_PS_KEY)) {
			return SHOW_ERROR_MESSAGE_ACTION;
		}
    // try to get the group 
    if (iwc.isParameterSet(SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY))  {
      String selectedGroupProviderStateId = iwc.getParameter(SELECTED_GROUP_PROVIDER_PRESENTATION_STATE_ID_KEY);
      GroupBusiness groupBusiness = getGroupBusiness(iwc);
      try {
        // try to get the selected group  
        IWStateMachine stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
        groupProviderState = (UserApplicationMenuAreaPS) stateMachine.getStateFor(selectedGroupProviderStateId, UserApplicationMenuAreaPS.class);
        Integer selectedGroupId = (Integer) groupProviderState.getSelectedGroupId();
        if (selectedGroupId == null)  {
          return SHOW_ERROR_MESSAGE_ACTION;
        }
        group = groupBusiness.getGroupByGroupID(selectedGroupId.intValue());
        // try to get the action listener
        //TODO thomas change this in the way that actually the userApplicationMainAreaPs is used
        //actionListener = (UserApplicationMainAreaPS) stateMachine.getStateFor(actionListenerStateId, UserApplicationMainAreaPS.class);
        actionListener = (BasicUserOverviewPS) stateMachine.getStateFor(":6893", BasicUserOverviewPS.class);
      }
      catch (RemoteException ex)  {
        throw new RuntimeException(ex.getMessage());
      }
      catch (FinderException ex)  {
        throw new RuntimeException(ex.getMessage());
      }
      // type of group correct?
      String groupType = group.getGroupType();
      if (GROUP_TYPE_CLUB.equals(groupType))  {
        return SHOW_CHILDREN_OF_GROUP_ACTION;
      }
    }
    return SHOW_ERROR_MESSAGE_ACTION;
  }
    
  private void  showListOfChildren(IWResourceBundle iwrb, IWContext iwc) {
    // set event
    EntityBrowserEvent event = new EntityBrowserEvent();
    event.setEventName(EVENT_NAME);
    event.setSource(this);
    // set form
    Form form = new Form();
    form.addParameter(MOVE_SELECTED_GROUPS,"w");
    form.setName("mass_form");
    form.addEventModel(event, iwc);
    // get entities
    Collection coll = getChildrenOfGroup(iwc);
    // define browser
    EntityBrowser browser = getBrowser(coll);
    // get target goup list
    DropdownMenu targetGroupMenu = getGroupList(iwc);
    // define button
    SubmitButton move = new SubmitButton(iwrb.getLocalizedImageButton("move", "Move to"));
    SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("close", "Close"));
    close.setOnClick("window.close(); return false;");
    move.setOnClick("mass_form.submit(); window.close();");
    // assemble table
    Table table = new Table(1,2);
    Table buttons = new Table(3,1);
    buttons.add(close, 1, 1);
    buttons.add(move, 2, 1);
    buttons.add(targetGroupMenu,3,1);
    table.add(browser,1,1);
    table.add(buttons,1,2);    
    form.add(table);
    add(form);
    // add action listener
    addActionListener(actionListener);
  } 
    

  private Collection getChildrenOfGroup(IWContext iwc) {
    try {
      return getGroupBusiness(iwc).getChildGroups(group);
    }
    catch (Exception ex)  {
      throw new RuntimeException(ex.getMessage());
    }
  }
        
    
  // service method  
  private GroupBusiness getGroupBusiness(IWContext iwc) {
    try {
      return (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
    }
    catch (RemoteException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  } 
    

  private EntityBrowser getBrowser(Collection entities)  {
    // define checkbox button converter class
    EntityToPresentationObjectConverter checkBoxConverter = new CheckBoxConverter(SELECTED_CHECKED_GROUPS_KEY); 
    // set default columns
    //String columnName = GroupBMPBean.getNameColumnName();
    String nameKey = "com.idega.user.data.Group.NAME"; //+ GroupBMPBean.getNameColumnName();
    EntityBrowser browser = new EntityBrowser();
    // keep things simple
    browser.setUseEventSystem(false);
    browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
    // set number of rows
    browser.setDefaultNumberOfRows(NUMBER_OF_ROWS);
    browser.setEntities("mass_moving", entities);
    browser.setWidth(Table.HUNDRED_PERCENT);
    // fonts
    Text column = new Text();
    column.setBold();
    browser.setColumnTextProxy(column);
    // set color of rows
    browser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
    browser.setColorForOddRows("#FFFFFF");
    // set columns 
    browser.setDefaultColumn(1, nameKey);
    browser.setMandatoryColumn(1, "Choose");
    // set special converters
    browser.setEntityToPresentationConverter("Choose", checkBoxConverter);
    browser.setUseExternalForm(true);
    return browser;
  }    
    

  private DropdownMenu getGroupList(IWContext iwc) {
    DropdownMenu groupList = new DropdownMenu(SELECTED_TARGET_GROUP_KEY);
    GroupBusiness groupBusiness = BasicUserOverview.getGroupBusiness(iwc);
    UserBusiness business = BasicUserOverview.getUserBusiness(iwc);
    User user = iwc.getCurrentUser();
    Collection coll = business.getAllGroupsWithEditPermission(user, iwc);
    Iterator iterator = coll.iterator();
    while (iterator.hasNext())  {
      Group group = (Group) iterator.next();
      String id = ((Integer) group.getPrimaryKey()).toString();
      String name = groupBusiness.getNameOfGroupWithParentName(group);
      groupList.addMenuElement(id,name);
    }
    return groupList;
  }    
    
    
    
    
    
    
    
    
    
    
    
    

  

}
