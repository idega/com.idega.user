package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.ChangeListener;

import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.StyledIWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.event.DeleteGroupEvent;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class DeleteGroupConfirmWindow extends StyledIWAdminWindow implements StatefullPresentation{
  
  private IWPresentationState presentationState = null;
  
  public static final String GROUP_ID_KEY = "group_id_key";
  public static final String PARENT_GROUP_ID_KEY = "parent_group_id";
  public static final String PARENT_DOMAIN_ID_KEY = "parent_domain_id";
  public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  private static final String HELP_TEXT_KEY = "delete_group_confirm_window";
    
  public DeleteGroupConfirmWindow() {
    setWidth(300);
    setHeight(200);
    setScrollbar(false);
    setResizable(false);
  }
  
  public String getBundleIdentifier(){
  	return IW_BUNDLE_IDENTIFIER;
  }
  
  public void main(IWContext iwc) throws RemoteException {
    getPresentationState(iwc);
    // get groupid 
    Integer groupId = new Integer(-1);
    if (iwc.isParameterSet(GROUP_ID_KEY)) {
      String groupIdString = iwc.getParameter(GROUP_ID_KEY);
      groupId = new Integer(groupIdString);
    }
//    else {
//    	// no group is selected show nothing
//    	return;
//    }
    Integer parentGroupId = new Integer(-1);
    if (iwc.isParameterSet(PARENT_GROUP_ID_KEY)) {
      String groupIdString = iwc.getParameter(PARENT_GROUP_ID_KEY);
      parentGroupId = new Integer(groupIdString);
    }
    Integer parentDomainId = new Integer(-1);
    if (iwc.isParameterSet(PARENT_DOMAIN_ID_KEY)) {
      String groupIdString = iwc.getParameter(PARENT_DOMAIN_ID_KEY);
      parentDomainId = new Integer(groupIdString);
    }
    // get resource bundle 
    IWResourceBundle iwrb = getResourceBundle(iwc);
    setTitle(iwrb.getLocalizedString("delete_group", "Delete Group"));
    addTitle(iwrb.getLocalizedString("delete_group", "Delete Group"), TITLE_STYLECLASS);
    
    // create delete event
    DeleteGroupEvent deleteEvent = new DeleteGroupEvent();
    // set group id at event
    deleteEvent.setGroupId(groupId);
    deleteEvent.setParentGroupId(parentGroupId);
    deleteEvent.setParentDomainId(parentDomainId);
    deleteEvent.setSource(this);
    // form
    Form form = new Form();
    form.setName("delete_form");
    // add event model
    form.addEventModel(deleteEvent, iwc);
    form.addParameter(DeleteGroupEvent.OKAY_KEY,"w");
    // check if the group can be deleted
    Group group = getGroup(groupId);
    boolean askForConfirmation = getGroupBusiness(iwc).isGroupRemovable(group);   
		Table table = getContent(iwrb, group, askForConfirmation);
    form.add(table);
    add(form,iwc);
  }

	private Table getContent(IWResourceBundle iwrb, Group group, boolean askForConfirmation) {
    // get selected group
    String groupName;
    groupName = group.getName();
    StringBuffer buffer = new StringBuffer(iwrb.getLocalizedString("Group", "Group"))
      .append(": ")
      .append(groupName);
    Text selectedGroup = new Text(buffer.toString());
    selectedGroup.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		// get text
    Text question = null;
    Text explanation1 = null;
    Text explanation2 = null;
		if (askForConfirmation) {
      question =  
		    new Text(iwrb.getLocalizedString("Do you really want to remove the selected group?", "Do you really want to remove the selected group?"));
      question.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
    }
    else  {
      explanation1 = 
        new Text(iwrb.getLocalizedString("The selected group has children.", "The selected group has children."));
      explanation1.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
      explanation2 =
        new Text(iwrb.getLocalizedString("Please remove the children first.", "Please remove the children first."));
      explanation2.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);   
    }
		// get buttons
		Help help = getHelp(HELP_TEXT_KEY);
		SubmitButton close = new SubmitButton(iwrb.getLocalizedImageButton("Close", "Close"), DeleteGroupEvent.CANCEL_KEY);
		GenericButton ok = new GenericButton(iwrb.getLocalizedString("yes","Yes"));
    	ok.setAsImageButton(true);
		SubmitButton cancel = new SubmitButton(iwrb.getLocalizedImageButton("cancel", "Cancel"), DeleteGroupEvent.CANCEL_KEY);
		close.setOnClick("window.close(); return false;");
		cancel.setOnClick("window.close(); return false;");
		// first submit, then close window
		ok.setOnClick("document.delete_form.submit(); window.close();");
		
		Table mainTable = new Table();
		mainTable.setWidth(280);
		mainTable.setHeight(100);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		
    //  assemble table
    Table table = new Table(3,3);
    table.setWidth(Table.HUNDRED_PERCENT);
    table.setHeight(60);
    table.setStyleClass(MAIN_STYLECLASS);
    table.setAlignment(2,3,Table.HORIZONTAL_ALIGN_RIGHT);
    table.mergeCells(1,1,2,1);
		table.add(selectedGroup, 1,1);
		
		Table bottomTable = new Table();
		bottomTable.setCellpadding(0);
		bottomTable.setCellspacing(5);
		bottomTable.setWidth(Table.HUNDRED_PERCENT);
		bottomTable.setHeight(39);
		bottomTable.setStyleClass(MAIN_STYLECLASS);
		bottomTable.add(help,1,1);
		bottomTable.setAlignment(2,1,Table.HORIZONTAL_ALIGN_RIGHT);

		if (askForConfirmation) {
			table.add(question,1,2);
			bottomTable.add(ok,2,1);
			bottomTable.add(Text.NON_BREAKING_SPACE,2,1);
			bottomTable.add(cancel, 2,1);
		}
		else  { 
      table.add(explanation1,1,2);
      table.add(Text.getBreak(),1,2);
      table.add(explanation2,1,2);
      bottomTable.add(close, 2,1);
		}
		mainTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
		mainTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
		mainTable.add(table,1,1);
		mainTable.add(bottomTable,1,3);

		return mainTable;
	}

  public void initializeInMain(IWContext iwc) {
    IWPresentationState state = this.getPresentationState(iwc);
    // add action listener
    addActionListener((IWActionListener) state);
    IWStateMachine stateMachine;
    //IWPresentationState changeListenerState = null;
    // add all changelisteners
    Collection changeListeners;
    try {
      stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
      changeListeners = stateMachine.getAllChangeListeners();
    }
    catch (RemoteException e) {
      changeListeners = new ArrayList();
    }
    Iterator iterator = changeListeners.iterator();
    while (iterator.hasNext())  {
      state.addChangeListener((ChangeListener) iterator.next());
    }
  }




	/**
	 * @see com.idega.presentation.StatefullPresentation#getPresentationStateClass()
	 */
	public Class getPresentationStateClass() {
		return DeleteGroupConfirmWindowPS.class;
	}
 
  /**
   * @see com.idega.presentation.StatefullPresentation#getPresentationState(com.idega.idegaweb.IWUserContext)
   */   
  public IWPresentationState getPresentationState(IWUserContext iwuc){
    if(presentationState == null){
      try {
        IWStateMachine stateMachine = (IWStateMachine)IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
        presentationState = (DeleteGroupConfirmWindowPS)stateMachine.getStateFor(getCompoundId(),DeleteGroupConfirmWindowPS.class);
      }
      catch (RemoteException re) {
        throw new RuntimeException(re.getMessage());
      }
    }
    return presentationState;
  }
 
  private Group getGroup(Integer groupId){
    if(groupId != null){
      try {
        return (Group)IDOLookup.findByPrimaryKey(Group.class, groupId);
      }
      catch (Exception ex) {
        // FinderException and RemoteException
        throw new RuntimeException(ex.getMessage());
      }
    }
    return null;
  }   
  
  private GroupBusiness getGroupBusiness(IWApplicationContext iwac)   { 
    try {
      return (GroupBusiness) com.idega.business.IBOLookup.getServiceInstance(iwac, GroupBusiness.class);
    }
    catch (java.rmi.RemoteException rme) {
      throw new RuntimeException(rme.getMessage());
    }
  }
  
  
}

