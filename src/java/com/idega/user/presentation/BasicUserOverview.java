package com.idega.user.presentation;

import com.idega.idegaweb.*;
import com.idega.util.ListUtil;
import com.idega.user.data.*;
import com.idega.data.IDOLookup;
import com.idega.presentation.*;
import com.idega.user.event.PartitionSelectEvent;
import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.business.UserBusiness;
import com.idega.util.IWColor;

import java.awt.ActiveEvent;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class BasicUserOverview extends Page implements IWBrowserView, StatefullPresentation {

  private static final String PARAMETER_DELETE_USER =  "delete_ic_user";
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;

  private BasicUserOverviewPS _presentationState = null;

  public BasicUserOverview(IWContext iwc) throws Exception {
    //this.empty();
    //this.add(this.getUsers(iwc));
  }
  public BasicUserOverview(){
    super();
  }


  public void setControlEventModel(IWPresentationEvent model){
    _contolEvent = model;
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
  }


  public Table getUsers(IWContext iwc) throws Exception{
    //List users = EntityFinder.findAllOrdered(com.idega.user.data.UserBMPBean.getStaticInstance(),com.idega.user.data.UserBMPBean.getColumnNameFirstName());
//    Collection users = this.getUserBusiness(iwc).getAllUsersOrderedByFirstName();
    BasicUserOverviewPS ps = (BasicUserOverviewPS)this.getPresentationState(iwc);
    Group selectedGroup = ps.getSelectedGroup();
    IBDomain selectedDomain = ps.getSelectedDomain();
    Collection users = null;
    int userCount = 0;
    if(selectedGroup  != null){
//      System.out.println("[BasicUserOverview]: selectedGroup = "+selectedGroup);
      users = this.getUserBusiness(iwc).getUsersInGroup(selectedGroup);
      userCount = users.size();
    } else if(selectedDomain != null){
//      System.out.println("[BasicUserOverview]: selectedDomain = "+selectedDomain);
      users = this.getUserBusiness(iwc).getAllUsersOrderedByFirstName();
//      userCount = ((UserHome)IDOLookup.getHome(User.class)).getUserCount();
      userCount = users.size();
    } else {
//      System.out.println("[BasicUserOverview]: selectedGroup = All");

//      users = this.getUserBusiness(iwc).getAllUsersOrderedByFirstName();
    }

    Table userTable = null;
    /**
     * @todo important: change back to  List adminUsers = UserGroupBusiness.getUsersContainedDirectlyRelated(iwc.getAccessController().getPermissionGroupAdministrator());
     */
    Collection adminUsers = null; // UserGroupBusiness.getUsersContainedDirectlyRelated(iwc.getAccessController().getPermissionGroupAdministrator());


    if(users == null){
      users = new Vector();
    }
    if(users != null){
      if(adminUsers == null){
        adminUsers = new Vector(0);
      }

		int parSize = ps.getPartitionSize();
		int sel = ps.getSelectedPartition();

		SubsetSelector selector = new SubsetSelector(parSize,userCount,6);
		selector.setControlEventModel(_contolEvent);
		selector.setControlTarget(_controlTarget);
		IWLocation location = (IWLocation)this.getLocation().clone();
		selector.setLocation(this.getLocation());
		selector.setSelectedSubset(sel);
		selector.setFirstSubset(ps.getFirstPartitionIndex());

		this.add(selector);





//      System.out.println("BasicUserOverview: sel = "+sel+" & parSize = "+parSize);
      users = ListUtil.convertCollectionToList(users).subList( (sel*parSize), Math.min((users.size()),((sel+1)*parSize)) );
//      this.add(" ("+sel+")");

      userTable = new Table(3, ((users.size()>33)?users.size():33)+1  );
      userTable.setLineAfterColumn(1);
      userTable.setLineAfterColumn(2);
      userTable.setLineColor("#DBDCDF");
      
      userTable.setBackgroundImage(1,1,this.getBundle(iwc).getImage("glass_column_dark.gif"));
      userTable.setBackgroundImage(2,1,this.getBundle(iwc).getImage("glass_column_light.gif"));
      userTable.setBackgroundImage(3,1,this.getBundle(iwc).getImage("glass_column_light.gif"));      
      userTable.setHeight(1,16);
      Text name = new Text("Nafn");
 	  name.setFontFace(Text.FONT_FACE_VERDANA);
 	  name.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	  userTable.add(name,1,1);
 	  
 	  Text ssn = new Text("Kennitala");
 	  ssn.setFontFace(Text.FONT_FACE_VERDANA);
 	  ssn.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	  userTable.add(ssn,2,1);
 	 	  
 	  Text del = new Text("Eyða félaga");
 	  del.setFontFace(Text.FONT_FACE_VERDANA);
 	  del.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	  userTable.add(del,3,1);
 	  
 	

    
      userTable.setCellspacing(0);
      userTable.setHorizontalZebraColored("#FFFFFF",IWColor.getHexColorString(246,246,247));
      userTable.setWidth("100%");
      for (int i = 1; i <= userTable.getRows() ; i++) {
        userTable.setHeight(i,"20");
      }


      int line = 2;
      Iterator iter = users.iterator();
      while (iter.hasNext()) {
        User tempUser = (User)iter.next();
      //for (int i = 0; i < users.size(); i++) {
        //User tempUser = (User)users.get(i);
        if(tempUser != null){

          boolean userIsSuperAdmin = iwc.getAccessController().getAdministratorUser().equals(tempUser);
          boolean delete = false;

          if(!userIsSuperAdmin){
            Link aLink = new Link(new Text(tempUser.getName()));
            aLink.setWindowToOpen(UserPropertyWindow.class);
            aLink.addParameter(UserPropertyWindow.PARAMETERSTRING_USER_ID, tempUser.getPrimaryKey().toString());
            userTable.add(aLink,2,line);
            delete = true;
            line++;
          }else if(userIsSuperAdmin && iwc.isSuperAdmin() ){
//            Text aText = new Text(tempUser.getName());
//            userTable.add(aText,2,i+1);
            Link aLink = new Link(new Text(tempUser.getName()));
            aLink.setWindowToOpen(AdministratorPropertyWindow.class);
            aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, tempUser.getPrimaryKey().toString());
            userTable.add(aLink,2,line);
            delete = true;
            line++;
          }

          if(delete && !adminUsers.contains(tempUser) && !userIsSuperAdmin && iwc.getAccessController().isAdmin(iwc)){
            Link delLink = new Link(new Text("Delete"));
            delLink.setWindowToOpen(ConfirmWindow.class);
            delLink.addParameter(BasicUserOverview.PARAMETER_DELETE_USER , tempUser.getPrimaryKey().toString());
            userTable.add(delLink,3,line-1);
          }


        }
      }
    }

    return userTable;
  }




  public void main(IWContext iwc) throws Exception {

    this.empty();
    this.add(this.getUsers(iwc));
    this.getParentPage().setAllMargins(0);




//    this.getParentPage().setBackgroundColor("#d4d0c8");
     // this.getParentPage().setBackgroundColor(IWColor.getHexColorString(250,245,240));

//    this.getParentPage().setBackgroundColor(((BasicUserOverviewPS)this.getPresentationState(iwc)).getColor());
  }




  public static class ConfirmWindow extends Window{

    public Text question;
    public Form myForm;

    public SubmitButton confirm;
    public CloseButton close;
    public Table myTable = null;

    public static final String PARAMETER_CONFIRM = "confirm";

    public Vector parameters;

    public ConfirmWindow(){
      super("ConfirmWindow",300,130);
      super.setBackgroundColor("#d4d0c8");
      super.setScrollbar(false);
      super.setAllMargins(0);

      question = Text.getBreak();
      myForm = new Form();
      parameters = new Vector();
      confirm = new SubmitButton(ConfirmWindow.PARAMETER_CONFIRM,"   Yes   ");
      close = new CloseButton("   No    ");
      // close.setOnFocus();
      initialze();

    }


    public void lineUpElements(){
      myTable = new Table(2,2);
      myTable.setWidth("100%");
      myTable.setHeight("100%");
      myTable.setCellpadding(5);
      myTable.setCellspacing(5);
      //myTable.setBorder(1);


      myTable.mergeCells(1,1,2,1);

      myTable.add(question,1,1);

      myTable.add(confirm,1,2);

      myTable.add(close,2,2);

      myTable.setAlignment(1,1,"center");
//      myTable.setAlignment(2,1,"center");
      myTable.setAlignment(1,2,"right");
      myTable.setAlignment(2,2,"left");

      myTable.setVerticalAlignment(1,1,"middle");
      myTable.setVerticalAlignment(1,2,"middle");
      myTable.setVerticalAlignment(2,2,"middle");

      myTable.setHeight(2,"30%");

      myForm.add(myTable);

    }

    public void setQuestion(Text Question){
      question = Question;
    }


    /*abstract*/
    public void initialze(){
      this.setQuestion(new Text("Are you sure you want to delete this user?"));
      this.maintainParameter(BasicUserOverview.PARAMETER_DELETE_USER);
    }


    public void maintainParameter(String parameter){
      parameters.add(parameter);
    }

    /*abstract*/
    public void actionPerformed(IWContext iwc)throws Exception{
      String userDelId = iwc.getParameter(BasicUserOverview.PARAMETER_DELETE_USER);
      if(userDelId != null){
        getUserBusiness(iwc).deleteUser(Integer.parseInt(userDelId));
      }
    }


    public void _main(IWContext iwc) throws Exception {
      Iterator iter = parameters.iterator();
      while (iter.hasNext()) {
        String item = (String)iter.next();
        myForm.maintainParameter(item);
      }

      String confirmThis = iwc.getParameter(ConfirmWindow.PARAMETER_CONFIRM);

      if(confirmThis != null){
        this.actionPerformed(iwc);
        this.setParentToReload();
        this.close();
      } else{
        this.empty();
        if(myTable == null){
          lineUpElements();
        }
        this.add(myForm);
      }
      super._main(iwc);
    }

    public UserBusiness getUserBusiness(IWApplicationContext iwc){
      UserBusiness business = null;
      if(business == null){
        try{
          business = (UserBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,UserBusiness.class);
        }
        catch(java.rmi.RemoteException rme){
          throw new RuntimeException(rme.getMessage());
        }
      }
      return business;
    }


  }


  public UserBusiness getUserBusiness(IWApplicationContext iwc){
    UserBusiness business = null;
    if(business == null){
      try{
        business = (UserBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,UserBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
    }
    return business;
  }



  public IWPresentationState getPresentationState(IWUserContext iwuc){
    if(_presentationState == null){
      try {
        IWStateMachine stateMachine = (IWStateMachine)IBOLookup.getSessionInstance(iwuc,IWStateMachine.class);
        _presentationState = (BasicUserOverviewPS)stateMachine.getStateFor(this.getLocation(),this.getPresentationStateClass());
      }
      catch (RemoteException re) {
        throw new RuntimeException(re.getMessage());
      }
    }
    return _presentationState;
  }

  public Class getPresentationStateClass(){
    return BasicUserOverviewPS.class;
  }


} //Class end
