package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.idegaweb.presentation.*;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;

 public class GroupGroupSetter extends StyledIWAdminWindow { //changed from Window
 	
 		private int width = 510;
 		private int height = 400;

    private static final String FIELDNAME_SELECTION_DOUBLE_BOX = "related_groups";
    private IWResourceBundle iwrb = null;
		private static final String IW_BUNDLE_IDENTIFIER  = "com.idega.user";
		
		private static final String HELP_TEXT_KEY = "group_group_setter";
		
		private String mainStyleClass = "main";
	
    public GroupGroupSetter(){
      super("add groups to groups");
      this.setAllMargins(0);
      this.setWidth(width);
      this.setHeight(height);
      this.setResizable(true);
//      this.setBackgroundColor(new IWColor(207,208,210));
    }


    private void LineUpElements(IWContext iwc)throws Exception{

      Form form = new Form();

      Table frameTable = new Table(3,3);
      frameTable.setWidth(490);
      frameTable.setHeight(320);
      frameTable.setStyleClass(mainStyleClass);
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


      SelectionDoubleBox sdb = new SelectionDoubleBox(GroupGroupSetter.FIELDNAME_SELECTION_DOUBLE_BOX,iwrb.getLocalizedString("groupgroupsetter.not_in","Not in"),iwrb.getLocalizedString("groupgroupsetter.in","In"));

      SelectionBox left = sdb.getLeftBox();
      left.setHeight(15);
			left.setWidth("200");
      left.selectAllOnSubmit();


      SelectionBox right = sdb.getRightBox();
      right.setHeight(15);
			right.setWidth("200");
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
			if(directGroups!=null && !directGroups.isEmpty()){
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
	          // filter
	          if (map.containsKey(group.getGroupType()))
	              // can not add a text
	            left.addElement(item.getPrimaryKey().toString(), groupBusiness.getNameOfGroupWithParentName(item));
	        }
	      }
			}

      //left.addSeparator();
      //right.addSeparator();
      
      Help help = getHelp(HELP_TEXT_KEY);

      frameTable.setAlignment(2,2,"center");
      //frameTable.add("GroupId: "+groupId,2,1);
			System.out.println("GroupId: "+groupId);
      frameTable.add(sdb,2,2);
      SubmitButton save = new SubmitButton(iwrb.getLocalizedString("groupgroupsetter.save","save"),"save","true");
      save.setAsImageButton(true);
			CloseButton close = new CloseButton(iwrb.getLocalizedString("groupgroupsetter.close","close"));
			close.setAsImageButton(true);
			frameTable.add(help,1,3);
			frameTable.add(save,2,3);
			frameTable.add(Text.NON_BREAKING_SPACE,2,3);
      frameTable.add(close,2,3);
      frameTable.setAlignment(2,3,"right");
      form.add(frameTable);
      this.add(form,iwc);
    }

    public void main(IWContext iwc) throws Exception {
    	
    	iwrb = getResourceBundle(iwc);
    	
    	setTitle(iwrb.getLocalizedString("groupgroupsetter.title","Add a group to a group"));
			setName(iwrb.getLocalizedString("groupgroupsetter.title","Add a group to a group"));

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
  
//  private UserBusiness getUserBusiness(IWApplicationContext iwc) {
//    UserBusiness business;
//    try {
//      business = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
//    }
//    catch(java.rmi.RemoteException rme) {
//      throw new RuntimeException(rme.getMessage());
//    }
//    return business;
//  } 
 
 

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
}