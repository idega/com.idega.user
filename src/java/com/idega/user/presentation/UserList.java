package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;

import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.user.data.User;

  public class UserList extends Page {

    private Collection groups = null;

    public UserList(){
      super();
    }

    public Table getUserTable(IWContext iwc){

      Collection direct = (Collection)iwc.getSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_DIRECTLY_RELATED);
      Collection notDirect = (Collection)iwc.getSessionAttribute(GroupMembershipTab.SESSIONADDRESS_USERS_NOT_DIRECTLY_RELATED);

      Table table = null;
      Iterator iter = null;
      int row = 1;
        try{
        if(direct != null && notDirect != null){
          table = new Table(5,direct.size()+notDirect.size());

          iter = direct.iterator();
          while (iter.hasNext()) {
            Object item = iter.next();
            table.add("D",1,row);
            table.add(((User)item).getName(),3,row++);
          }

          iter = notDirect.iterator();
          while (iter.hasNext()) {
            Object item = iter.next();
            table.add("E",1,row);
            table.add(((User)item).getName(),3,row++);
          }

        } else if(direct != null){
          table = new Table(5,direct.size());
          iter = direct.iterator();
          while (iter.hasNext()) {
            Object item = iter.next();
            table.add("D",1,row);
            table.add(((User)item).getName(),3,row++);
          }
        }
      }
      catch(Exception e){
        add("Error fetching User: "+e.getMessage());
        e.printStackTrace();
      }

      if(table != null){
        table.setWidth("100%");
        table.setWidth(1,"10");
        table.setWidth(2,"3");
        table.setWidth(4,"10");
        table.setWidth(5,"10");
      }



      return table;
    }

    public void main(IWContext iwc) throws Exception {
      this.getParentPage().setAllMargins(0);
      Table tb = getUserTable(iwc);
      if(tb != null){
        this.add(tb);
      }
    }



  } 
