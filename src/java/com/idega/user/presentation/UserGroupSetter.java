package com.idega.user.presentation;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import java.util.Collection;
import java.util.Iterator;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

  /**
  * Former innerClass of UserGroupList
  */

  public class UserGroupSetter extends Window {

    private static final String FIELDNAME_SELECTION_DOUBLE_BOX = "related_groups";

    public UserGroupSetter(){
      super("add user to groups");
      this.setAllMargins(0);
      this.setWidth(400);
      this.setHeight(300);
      this.setBackgroundColor("#d4d0c8");
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


    private void LineUpElements(IWContext iwc){

      Form form = new Form();
        try{
        UserBusiness userBusiness = getUserBusiness(iwc);

        Table frameTable = new Table(3,3);
        frameTable.setWidth("100%");
        frameTable.setHeight("100%");
        //frameTable.setBorder(1);


        SelectionDoubleBox sdb = new SelectionDoubleBox(FIELDNAME_SELECTION_DOUBLE_BOX,"Not in","In");

        SelectionBox left = sdb.getLeftBox();
        left.setHeight(8);
        left.selectAllOnSubmit();


        SelectionBox right = sdb.getRightBox();
        right.setHeight(8);
        right.selectAllOnSubmit();



        String stringUserId = iwc.getParameter(UserGroupList.PARAMETER_USER_ID);
        int userId = Integer.parseInt(stringUserId);
        form.addParameter(UserGroupList.PARAMETER_USER_ID,stringUserId);

        Collection directGroups = userBusiness.getUserGroupsDirectlyRelated(userId);

        Iterator iter = null;
        if(directGroups != null){
          iter = directGroups.iterator();
          while (iter.hasNext()) {
            Object item = iter.next();
            right.addElement(((Group)item).getPrimaryKey().toString(),((Group)item).getName());
          }
        }
        Collection notDirectGroups = userBusiness.getNonParentGroups(userId);
        if(notDirectGroups != null){
          iter = notDirectGroups.iterator();
          while (iter.hasNext()) {
            Object item = iter.next();
            left.addElement(((Group)item).getPrimaryKey().toString(),((Group)item).getName());
          }
        }


        frameTable.setAlignment(2,2,"center");
        frameTable.add("UserId: "+userId,2,1);
        frameTable.add(sdb,2,2);
        frameTable.add(new SubmitButton("  Save  ","save","true"),2,3);
        frameTable.setAlignment(2,3,"right");
        form.add(frameTable);
      }
      catch(Exception e){
        add(new ExceptionWrapper(e,this));
        e.printStackTrace();
      }
      this.add(form);
    }

    public void main(IWContext iwc) throws Exception {

      UserBusiness userBusiness = getUserBusiness(iwc);
      String save = iwc.getParameter("save");
      if(save != null){
        String stringUserId = iwc.getParameter(UserGroupList.PARAMETER_USER_ID);
        int userId = Integer.parseInt(stringUserId);

        String[] related = iwc.getParameterValues(UserGroupSetter.FIELDNAME_SELECTION_DOUBLE_BOX);

        //User user = ((com.idega.user.data.UserHome)com.idega.data.IDOLookup.getHomeLegacy(User.class)).findByPrimaryKeyLegacy(userId);
        User user = userBusiness.getUser(userId);
        Collection currentRelationShip = userBusiness.getUserGroupsDirectlyRelated(user);


        if(related != null){

          if(currentRelationShip != null){
            for (int i = 0; i < related.length; i++) {
              int id = Integer.parseInt(related[i]);
              //Group gr = ((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(id);
              Group gr = userBusiness.getGroupHome().findByPrimaryKey(new Integer(id));
              if(!currentRelationShip.remove(gr)){
                //user.addTo(gr);
                gr.addGroup(user);
              }
            }

            Iterator iter = currentRelationShip.iterator();
            while (iter.hasNext()) {
              Object item = iter.next();
              //user.removeFrom((Group)item);
              ((Group)item).removeUser(user);
            }

          } else{
            for (int i = 0; i < related.length; i++) {
              //user.addTo(Group.class,Integer.parseInt(related[i]));
              //((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(Integer.parseInt(related[i])).addUser(user);
              //com.idega.user.data.GroupBMPBean.addUser(Integer.parseInt(related[i]),user);
              Group gr = userBusiness.getGroupHome().findByPrimaryKey(new Integer(related[i]));
              gr.addGroup(user);
            }
          }

        }else if (currentRelationShip != null){
            Iterator iter = currentRelationShip.iterator();
            while (iter.hasNext()) {
              Object item = iter.next();
              ((Group)item).removeUser(user);
            }
          }

        this.close();
        this.setParentToReload();
      } else {
        LineUpElements(iwc);
      }

  /*
      Enumeration enum = iwc.getParameterNames();
       System.err.println("--------------------------------------------------");
      if(enum != null){
        while (enum.hasMoreElements()) {
          Object item = enum.nextElement();
          if(item.equals("save")){
            this.close();
          }
          String val[] = iwc.getParameterValues((String)item);
          System.err.print(item+" = ");
          if(val != null){
            for (int i = 0; i < val.length; i++) {
              System.err.print(val[i]+", ");
            }
          }
          System.err.println();
        }
      }
  */
    }

  }