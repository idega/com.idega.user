package com.idega.user.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
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
      int groupId = Integer.parseInt(stringGroupId);
      form.addParameter(GeneralGroupInfoTab.PARAMETER_GROUP_ID,stringGroupId);

      Collection directGroups = getGroupBusiness(iwc).getParentGroups(groupId);

      Iterator iter = null;
      if(directGroups != null){
        iter = directGroups.iterator();
        while (iter.hasNext()) {
          Object item = iter.next();
          right.addElement(((Group)item).getPrimaryKey().toString(),((Group)item).getName());
        }
      }
      
      Collection notDirectGroups = getGroupBusiness(iwc).getNonParentGroups(groupId);
      if(notDirectGroups != null){
        iter = notDirectGroups.iterator();
        while (iter.hasNext()) {
          Object item = iter.next();
          left.addElement(((Group)item).getPrimaryKey().toString(),((Group)item).getName());
        }
      }

      //left.addSeparator();
      //right.addSeparator();

      frameTable.setAlignment(2,2,"center");
      //frameTable.add("GroupId: "+groupId,2,1);
			System.out.println("GroupId: "+groupId);
      frameTable.add(sdb,2,2);
      frameTable.add(new SubmitButton("  Save  ","save","true"),2,3);
      frameTable.setAlignment(2,3,"right");
      form.add(frameTable);
      this.add(form);
    }

    public void main(IWContext iwc) throws Exception {
//TODO only display allowed ggroups

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
              ((Group)item).removeGroup(group);
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
              ((Group)item).removeGroup(group);
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
  
    public GroupBusiness getGroupBusiness(IWApplicationContext iwc){
    GroupBusiness business = null;
    if(business == null){
      try{
        business = (GroupBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,GroupBusiness.class);
      }
      catch(java.rmi.RemoteException rme){
        throw new RuntimeException(rme.getMessage());
      }
    }
    return business;
  }
  
  } 