package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window; 
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;
import com.idega.util.IWColor;

 public class GroupGroupSetter extends Window {

    private static final String FIELDNAME_SELECTION_DOUBLE_BOX = "related_groups";

    public GroupGroupSetter(){
      super("add groups to groups");
      this.setAllMargins(0);
      this.setWidth(500);
      this.setHeight(400);
      this.setBackgroundColor(new IWColor(207,208,210));
    }


    private void LineUpElements(IWContext iwc)throws Exception{

      Form form = new Form();

      Table frameTable = new Table(3,3);
      frameTable.setWidth("100%");
      frameTable.setHeight("100%");
      //frameTable.setBorder(1);


      SelectionDoubleBox sdb = new SelectionDoubleBox(GroupGroupSetter.FIELDNAME_SELECTION_DOUBLE_BOX,"Not in","In");

      SelectionBox left = sdb.getLeftBox();
      left.setHeight(8);
      left.selectAllOnSubmit();


      SelectionBox right = sdb.getRightBox();
      right.setHeight(8);
      right.selectAllOnSubmit();



      String stringGroupId = iwc.getParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID);
      String stringParentGroupId = iwc.getParameter(GeneralGroupInfoTab.PARENT_GROUP_ID);
      int groupId = Integer.parseInt(stringGroupId);
      int parentGroupId = Integer.parseInt(stringParentGroupId);
      form.addParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID,stringGroupId);
      GroupBusiness groupBusiness = getGroupBusiness(iwc);
      Collection allowedGroupTypes = groupBusiness.getAllAllowedGroupTypesForChildren(parentGroupId, iwc); 
      Iterator iterator = allowedGroupTypes.iterator();
      HashMap map = new HashMap();
      while (iterator.hasNext())  {
        String value = ((GroupType) iterator.next()).getType();
        map.put(value, value);
      }        
      // usually the following group type should be already contained
      Group group = groupBusiness.getGroupByGroupID(groupId);
      String value = group.getGroupType();
      map.put(value, value);
        
      Collection directGroups = groupBusiness.getParentGroups(groupId);
      Iterator iter = null;
      if(directGroups != null){
        iter = directGroups.iterator();
        while (iter.hasNext()) {
          Group item = (Group) iter.next();
          right.addElement(item.getPrimaryKey().toString(),groupBusiness.getNameOfGroupWithParentName(item));
        }
      }
      
      // former: Collection notDirectGroups = getGroupBusiness(iwc).getNonParentGroups(groupId);
      User user = iwc.getCurrentUser();
      UserBusiness userBusiness = getUserBusiness(iwc);
      Collection notDirectGroups  = userBusiness.getUsersTopGroupNodesByViewAndOwnerPermissions(user, iwc);
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
          // filter
          if (map.containsKey(group.getGroupType()))
              // can not add a text
            left.addElement(item.getPrimaryKey().toString(), groupBusiness.getNameOfGroupWithParentName(item));
        }
      }

      //left.addSeparator();
      //right.addSeparator();

      frameTable.setAlignment(2,2,"center");
      //frameTable.add("GroupId: "+groupId,2,1);
			System.out.println("GroupId: "+groupId);
      frameTable.add(sdb,2,2);
      SubmitButton save = new SubmitButton("  Save  ","save","true");
      save.setAsImageButton(true);
      frameTable.add(save,2,3);
      frameTable.setAlignment(2,3,"right");
      form.add(frameTable);
      this.add(form);
    }

    public void main(IWContext iwc) throws Exception {

      String save = iwc.getParameter("save");
      if(save != null){
        String stringGroupId = iwc.getParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID);
        int groupId = Integer.parseInt(stringGroupId);

        String[] related = iwc.getParameterValues(GroupGroupSetter.FIELDNAME_SELECTION_DOUBLE_BOX);

        //Group group = ((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(groupId);
        Group group = getGroupBusiness(iwc).getGroupByGroupID(groupId);
        List currentRelationShip = group.getParentGroups();


        if(related != null){

          if(currentRelationShip != null){
            for (int i = 0; i < related.length; i++) {
              int id = Integer.parseInt(related[i]);
              //Group gr = ((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(id);
              Group gr = getGroupBusiness(iwc).getGroupByGroupID(id);
              if(!currentRelationShip.remove(gr)){
                gr.addGroup(group);
              }
            }

            Iterator iter = currentRelationShip.iterator();
            while (iter.hasNext()) {
              Object item = iter.next();
              ((Group)item).removeGroup(group, iwc.getCurrentUser());
            }

          } else{
            for (int i = 0; i < related.length; i++) {
              //((com.idega.user.data.GroupHome)com.idega.data.IDOLookup.getHomeLegacy(Group.class)).findByPrimaryKeyLegacy(Integer.parseInt(related[i])).addGroup(group);
              Group group2 = getGroupBusiness(iwc).getGroupByGroupID(Integer.parseInt(related[i]));
              group2.addGroup(group);
            }
          }

        }else if (currentRelationShip != null){
            Iterator iter = currentRelationShip.iterator();
            while (iter.hasNext()) {
              Object item = iter.next();
              ((Group)item).removeGroup(group, iwc.getCurrentUser());
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
  
  private UserBusiness getUserBusiness(IWApplicationContext iwc) {
    UserBusiness business;
    try {
      business = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
    }
    catch(java.rmi.RemoteException rme) {
      throw new RuntimeException(rme.getMessage());
    }
    return business;
  } 
}