package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.idega.builder.data.IBDomain;
import com.idega.business.IBOLookup;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWColor;
import com.idega.util.ListUtil;

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
  private IWPresentationEvent _controlEvent = null;
  private IWResourceBundle iwrb = null ;

  private BasicUserOverviewPS _presentationState = null;
  private BasicUserOverViewToolbar toolbar = null;

  public BasicUserOverview(IWContext iwc) throws Exception {
    //this.empty();
    //this.add(this.getUsers(iwc));
  }
  public BasicUserOverview(){
    super();
  }


  public void setControlEventModel(IWPresentationEvent model){
    _controlEvent = model;
    if( toolbar == null ) toolbar = new BasicUserOverViewToolbar();
    toolbar.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
    _controlTarget = controlTarget;
    if( toolbar == null ) toolbar = new BasicUserOverViewToolbar();
    toolbar.setControlTarget(controlTarget);
  }


  public Table getUsers(IWContext iwc) throws Exception{
    this.empty();
    iwrb = this.getResourceBundle(iwc);
    //List users = EntityFinder.findAllOrdered(com.idega.user.data.UserBMPBean.getStaticInstance(),com.idega.user.data.UserBMPBean.getColumnNameFirstName());
//    Collection users = this.getUserBusiness(iwc).getAllUsersOrderedByFirstName();
    if( toolbar == null ) toolbar = new BasicUserOverViewToolbar();
    
    BasicUserOverviewPS ps = (BasicUserOverviewPS)this.getPresentationState(iwc);
    Group selectedGroup = ps.getSelectedGroup();
    IBDomain selectedDomain = ps.getSelectedDomain();

    Collection users = null;
    int userCount = 0;
    if(selectedGroup  != null){
      toolbar.setSelectedGroup(selectedGroup);
//      System.out.println("[BasicUserOverview]: selectedGroup = "+selectedGroup);
      users = this.getUserBusiness(iwc).getUsersInGroup(selectedGroup);
      if(users == null) {
      	userCount = 0;
      }
      else userCount = users.size();

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
    Table returnTable = new Table(1,2);
    returnTable.setCellpaddingAndCellspacing(0);
    returnTable.setWidth(Table.HUNDRED_PERCENT);
    returnTable.setHeight(Table.HUNDRED_PERCENT);
    returnTable.setHeight(1,22);

    returnTable.add(toolbar,1,1);


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
		int firstIndex = ps.getFirstPartitionIndex();
		//debug
System.out.println(" parSize = "+parSize);
System.out.println(" sel = "+sel);
System.out.println(" firstIndex = "+firstIndex);

		SubsetSelector selector = new SubsetSelector(parSize,userCount,6);
		selector.setControlEventModel(_controlEvent);
		selector.setControlTarget(_controlTarget);
		IWLocation location = (IWLocation)this.getLocation().clone();
		
		selector.setLocation(this.getLocation());
		selector.setSelectedSubset(sel);
		selector.setFirstSubset(firstIndex);

		this.add(selector);





//      System.out.println("BasicUserOverview: sel = "+sel+" & parSize = "+parSize);
      users = ListUtil.convertCollectionToList(users).subList( (sel*parSize), Math.min(users.size(),((sel+1)*parSize)) );
//      this.add(" ("+sel+")");

      userTable = new Table(3, ((users.size()>33)?users.size():33)+1  );
      returnTable.add(userTable,1,2);
      userTable.setCellpaddingAndCellspacing(0);
      userTable.setLineAfterColumn(1);
      userTable.setLineAfterColumn(2);
      userTable.setLineColor("#DBDCDF");

      userTable.setBackgroundImage(1,1,this.getBundle(iwc).getImage("glass_column_light.gif"));
      userTable.setBackgroundImage(2,1,this.getBundle(iwc).getImage("glass_column_light.gif"));
      userTable.setBackgroundImage(3,1,this.getBundle(iwc).getImage("glass_column_light.gif"));
      userTable.setHeight(1,16);

      userTable.setWidth(1,"200");
      userTable.setWidth(2,"200");

      Text name = new Text("&nbsp;"+iwrb.getLocalizedString("name","Name"));
 	  name.setFontFace(Text.FONT_FACE_VERDANA);
 	  name.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	  userTable.add(name,1,1);

 	  Text address = new Text("&nbsp;"+iwrb.getLocalizedString("address","Address"));
 	  address.setFontFace(Text.FONT_FACE_VERDANA);
 	  address.setFontSize(Text.FONT_SIZE_7_HTML_1);
 	  userTable.add(address,2,1);

 	  Text del = new Text("&nbsp;"+iwrb.getLocalizedString("delete.user","Delete user"));
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
            userTable.add("&nbsp;",1,line);
            userTable.add(aLink,1,line);
            delete = true;
            line++;
          }else if(userIsSuperAdmin && iwc.isSuperAdmin() ){
//            Text aText = new Text(tempUser.getName());
//            userTable.add(aText,2,i+1);
            Link aLink = new Link(new Text(tempUser.getName()));
            aLink.setWindowToOpen(AdministratorPropertyWindow.class);
            aLink.addParameter(AdministratorPropertyWindow.PARAMETERSTRING_USER_ID, tempUser.getPrimaryKey().toString());
            userTable.add("&nbsp;",1,line);
            userTable.add(aLink,1,line);
            delete = true;
            line++;
          }


          userTable.add(getUserBusiness(iwc).getUsersMainAddress(tempUser).getName(),2,line-1);

          if(delete && !adminUsers.contains(tempUser) && !userIsSuperAdmin && iwc.getAccessController().isAdmin(iwc)){
            Link delLink = new Link(new Text("Delete"));
            delLink.setWindowToOpen(ConfirmWindow.class);
            delLink.addParameter(BasicUserOverview.PARAMETER_DELETE_USER , tempUser.getPrimaryKey().toString());
            userTable.add("&nbsp;",3,line-1);
            userTable.add(delLink,3,line-1);
          }


        }
      }
    }

    return returnTable;
  }




  public void main(IWContext iwc) throws Exception {

    this.empty();
    this.add(getUsers(iwc));
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

  public String getBundleIdentifier(){
  	return "com.idega.user";
  }


} //Class end
