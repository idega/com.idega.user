package com.idega.user.presentation;

import java.rmi.RemoteException;

import javax.swing.event.ChangeListener;

import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWControlFramePresentationState;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.Frame;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.app.UserApplication;
import com.idega.user.data.Group;
import com.idega.user.event.DeleteGroupEvent;

/**
 *@author     <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 *@version    1.0
 */
public class DeleteGroupConfirmWindow extends IWAdminWindow implements StatefullPresentation{
  
  private IWPresentationState presentationState = null;
  
  public static final String GROUP_ID_KEY = "group_id_key";
  public static final String PARENT_GROUP_ID_KEY = "parent_group_id";
  public static final String PARENT_DOMAIN_ID_KEY = "parent_domain_id";
  
  public DeleteGroupConfirmWindow() {
    setWidth(240);
    setHeight(140);
    setScrollbar(false);
  }
  
  public void main(IWContext iwc) {
    DeleteGroupConfirmWindowPS state = (DeleteGroupConfirmWindowPS) this.getPresentationState(iwc);
    // get groupid
    Integer groupId = new Integer(-1);
    if (iwc.isParameterSet(GROUP_ID_KEY)) {
      String groupIdString = iwc.getParameter(GROUP_ID_KEY);
      groupId = new Integer(groupIdString);
    }
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
    // create delete event
    DeleteGroupEvent deleteEvent = new DeleteGroupEvent();
    // set group id at event
    deleteEvent.setGroupId(groupId);
    deleteEvent.setParentGroupId(parentGroupId);
    deleteEvent.setParentDomainId(parentDomainId);
    
    // check if the group can be deleted
    // use the event method
    boolean confirmation = false;
    try {
      Group group = getGroup(groupId);
      confirmation = (group.getGroupType().equals("alias") || group.getChildCount() <= 0);
    }
    catch (Exception ex)  {
      System.err.println("[DeleteGroupConfirmWindow] RemoteException or NullpointerException"+ ex.getMessage());
      ex.printStackTrace(System.err);
      confirmation = false;
    }  
    //_createEvent.setSource(this.getLocation());
    deleteEvent.setSource(this);
    // set controller (added by Thomas)
    String id = IWMainApplication.getEncryptedClassName(UserApplication.Top.class);
    id = PresentationObject.COMPOUNDID_COMPONENT_DELIMITER + id;
    deleteEvent.setController(id);
    Table table = new Table(1,2);
    Form form = new Form();
    // add event model
    form.addEventModel(deleteEvent, iwc);
    // get resource bundle
    IWResourceBundle resourceBundle = getResourceBundle(iwc);
    String textString = (confirmation) ? 
      resourceBundle.getLocalizedString("Do you really want to remove the selected group?", "Do you really want to remove the selected group?"):
      resourceBundle.getLocalizedString("Selected group has children and can not be removed.", "Selected group has children and ca not be removed.");
    Text text = new Text(textString);
    SubmitButton close = new SubmitButton(resourceBundle.getLocalizedImageButton("Close", "Close"), DeleteGroupEvent.CANCEL_KEY);
    SubmitButton ok = new SubmitButton(resourceBundle.getLocalizedImageButton("yes", "Yes"), DeleteGroupEvent.OKAY_KEY);
    SubmitButton cancel = new SubmitButton(resourceBundle.getLocalizedImageButton("cancel", "Cancel"), DeleteGroupEvent.CANCEL_KEY);
    close.setOnClick("window.close()");
    cancel.setOnClick("window.close()");
    ok.setOnClick("window.close()");
    table.add(textString, 1,1);
    if (confirmation) {
      table.add(ok,1,2);
      table.add(cancel, 1,2);
    }
    else  { 
      table.add(close, 1,2);
    }
    form.add(table);
    add(form);
  }

  public void initializeInMain(IWContext iwc) {
    StringBuffer id = new StringBuffer(PresentationObject.COMPOUNDID_COMPONENT_DELIMITER);
    id.append(IWMainApplication.getEncryptedClassName(UserApplication.class));
    id.append(Frame.COMPOUND_ID_FRAME_NAME_KEY);
    id.append("iwb_main_left");
    setArtificialCompoundId(id.toString(), iwc);
    IWPresentationState state = this.getPresentationState(iwc);
    // add action listener
    addActionListener((IWActionListener) state);
    // get and set change listener
    id = new StringBuffer(PresentationObject.COMPOUNDID_COMPONENT_DELIMITER);
    id.append(IWMainApplication.getEncryptedClassName(UserApplication.Top.class));
    IWStateMachine stateMachine;
    IWPresentationState changeListenerState = null;
    try {
      stateMachine = (IWStateMachine) IBOLookup.getSessionInstance(iwc, IWStateMachine.class);
      changeListenerState = (IWControlFramePresentationState) stateMachine.getStateFor(id.toString(), IWControlFramePresentationState.class);
    }
    catch (RemoteException e) {
    }
    state.addChangeListener((ChangeListener) changeListenerState);
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
        ex.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }   
  
}

