package com.idega.user.presentation;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.IWColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


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
		private IWResourceBundle iwrb = null;
		private static final String IW_BUNDLE_IDENTIFIER  = "com.idega.user";
		private int width = 500;
		private int height = 300;
		
    public UserGroupSetter(){
      super("add user to groups");
      this.setAllMargins(0);
			this.setWidth(width);
			this.setHeight(height);
      this.setBackgroundColor(new IWColor(207,208,210));
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
				frameTable.setVerticalAlignment(Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(1,1,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(1,2,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(1,3,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(2,1,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(2,2,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(2,3,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(3,1,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(3,2,Table.VERTICAL_ALIGN_TOP);
				frameTable.setVerticalAlignment(3,3,Table.VERTICAL_ALIGN_TOP);
        //frameTable.setBorder(1);


        SelectionDoubleBox sdb = new SelectionDoubleBox(FIELDNAME_SELECTION_DOUBLE_BOX,iwrb.getLocalizedString("usergroupsetter.in","Not in"),iwrb.getLocalizedString("usergroupsetter.not_in","In"));

        SelectionBox left = sdb.getLeftBox();
        left.setHeight(15);
        left.setWidth("200");
        left.selectAllOnSubmit();


        SelectionBox right = sdb.getRightBox();
        right.setHeight(15);
				right.setWidth("200");
        right.selectAllOnSubmit();



        String stringUserId = iwc.getParameter(UserGroupList.PARAMETER_USER_ID);
        int userId = Integer.parseInt(stringUserId);
        form.addParameter(UserGroupList.PARAMETER_USER_ID,stringUserId);
        
        GroupBusiness groupBusiness = getGroupBusiness(iwc);

        Collection directGroups = userBusiness.getUserGroupsDirectlyRelated(userId);

        Iterator iter = null;
        if(directGroups!=null && !directGroups.isEmpty()){
          iter = directGroups.iterator();
          while (iter.hasNext()) {
            Group item = (Group) iter.next();
            if( item != null){
            	right.addElement(item.getPrimaryKey().toString(),groupBusiness.getNameOfGroupWithParentName(item));
            }
          }
        }
        // former:Collection notDirectGroups = userBusiness.getNonParentGroups(userId);
 

        User user = iwc.getCurrentUser();
        Collection notDirectGroups  = userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc);

				if(notDirectGroups!=null && !notDirectGroups.isEmpty()){
	        Iterator topGroupsIterator = notDirectGroups.iterator();
	        List allGroups = new ArrayList();
	        while (topGroupsIterator.hasNext())  {
	          Group parentGroup = (Group) topGroupsIterator.next();
	          allGroups.add(parentGroup);
	          Collection coll = groupBusiness.getChildGroupsRecursive(parentGroup);
	          if (coll != null)
	            allGroups.addAll(coll);
	        }        
	                
	        if(allGroups != null){
	          iter = allGroups.iterator();
	          while (iter.hasNext()) {
	            Group item = (Group) iter.next();
	            left.addElement(item.getPrimaryKey().toString(),groupBusiness.getNameOfGroupWithParentName(item));
	          }
	        }
				}

        frameTable.setAlignment(2,2,"center");
        System.out.println("UserId: "+userId);
        frameTable.add(sdb,2,2);
				SubmitButton save = new SubmitButton(iwrb.getLocalizedString("usergroupsetter.save","save"),"save","true");
        save.setAsImageButton(true);
				CloseButton close = new CloseButton(iwrb.getLocalizedString("usergroupsetter.close","close"));
				close.setAsImageButton(true);
				frameTable.add(close,2,3);
        frameTable.add(save,2,3);
        frameTable.setAlignment(2,3,"right");
        form.add(frameTable);
      }
      catch(Exception e){
        add(new ExceptionWrapper(e,this));
        e.printStackTrace();
      }
      this.add(form);
    }

    public void main(IWContext iwc) throws Exception{    	
			iwrb = getResourceBundle(iwc);
			
			setTitle(iwrb.getLocalizedString("usergroupsetter.title","Add a user to a group"));
			setName(iwrb.getLocalizedString("usergroupsetter.title","Add a user to a group"));
			
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
              ((Group)item).removeUser(user, iwc.getCurrentUser());
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
              ((Group)item).removeUser(user, iwc.getCurrentUser());
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

    private GroupBusiness getGroupBusiness(IWApplicationContext iwc){
      GroupBusiness business;
      try {
        business = (GroupBusiness) IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
      return business;
    }


		public String getBundleIdentifier() {
			return IW_BUNDLE_IDENTIFIER;
		}

  }