package com.idega.user.presentation;

import java.rmi.RemoteException;

import com.idega.builder.presentation.IBPageChooser;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.IFrame;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.util.ICUserConstants;
import com.idega.util.Disposable;

/**
 * Title:        User
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega.is
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */
public class GeneralGroupInfoTab extends UserGroupTab implements Disposable {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "gen_tab_name";
	private static final String DEFAULT_TAB_NAME = "General";
	
	private static final String HELP_TEXT_KEY = "general_group_info_tab";

	private TextInput nameField;
	private TextArea descriptionField;
	private IBPageChooser homepageField;
//	private DropdownMenu grouptypeField;
	private Text grouptypeField;
	private TextInput shortNameField;
	private TextInput abbrField;
	
	private Text nameText;
	private Text descriptionText;
	private Text homepageText;
	private Text grouptypeText;
	private Text shortNameText;
	private Text abbrText;

	private String nameFieldName;
	private String descriptionFieldName;
	private String homepageFieldName;
	private String grouptypeFieldName;
	private String shortNameFieldName;
	private String abbrFieldName;
	
	private IWResourceBundle _iwrb = null;

	private Link addLink;
	private IFrame memberofFrame;
	public static final String PARAMETER_GROUP_ID = "ic_group_id";
  public static final String PARENT_GROUP_ID = "parent_group_id";
	public static final String SESSIONADDRESS_GROUPS_DIRECTLY_RELATED = UserGroupList.SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED;//"ic_group_ic_group_direct_GGIT";
	public static final String SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED = UserGroupList.SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED;//"ic_group_ic_group_not_direct_GGIT";

	protected Text memberof;
	
	private String underTableStyle = "main";
	private String linkStyle = "styledLinkGeneral";

	public GeneralGroupInfoTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));		
//		setName("General");
	}

	public void initFieldContents() {
    IWContext iwc = IWContext.getInstance();
    IWResourceBundle iwrb = getResourceBundle(iwc);
    fillGroupTypeMenu(iwc, iwrb);
		addLink.setWindowToOpen(GroupGroupSetter.class);
		addLink.setStyleClass(linkStyle);
		addLink.addParameter(PARAMETER_GROUP_ID, getGroupId());
    addLink.addParameter(PARENT_GROUP_ID, getSelectedParentGroupId());

		try {
			Group group = (Group) (((GroupHome) com.idega.data.IDOLookup.getHome(Group.class)).findByPrimaryKey(new Integer(getGroupId())));

			fieldValues.put(nameFieldName, (group.getName() != null) ? group.getName() : "");
			fieldValues.put(descriptionFieldName, (group.getDescription() != null) ? group.getDescription() : "");
			fieldValues.put(homepageFieldName, new Integer(group.getHomePageID()));
			fieldValues.put(grouptypeFieldName, (group.getGroupType() != null) ? group.getGroupType() : "");
			fieldValues.put(shortNameFieldName, (group.getShortName() != null) ? group.getShortName() : "");
			fieldValues.put(abbrFieldName, (group.getAbbrevation() != null) ? group.getAbbrevation() : "");
		updateFieldsDisplayStatus();
		}
		catch (Exception e) {
			System.err.println("GeneralGroupInfoTab error initFieldContents, GroupId : " + getGroupId());
		}
	}

	public void updateFieldsDisplayStatus() {
		nameField.setContent((String) fieldValues.get(nameFieldName));

		descriptionField.setContent((String) fieldValues.get(descriptionFieldName));

		Integer page = (Integer) fieldValues.get(homepageFieldName);
		if (page != null) {
			int pageId = page.intValue();
			IWApplicationContext iwc = getIWApplicationContext();
			//Map tree = PageTreeNode.getTree(iwc);
			//if (tree != null) {
				BuilderService bservice;
				try
				{
					bservice = getBuilderService(iwc);
					ICTreeNode node = (ICTreeNode) bservice.getPageTree(pageId);
					if (node != null)
						homepageField.setSelectedPage(node.getNodeID(), node.getNodeName());
				}
				catch (RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			//}
		}

		shortNameField.setContent((String) fieldValues.get(shortNameFieldName));
		abbrField.setContent((String) fieldValues.get(abbrFieldName));


		//String type = (String) fieldValues.get(grouptypeFieldName);
		//grouptypeField.setSelectedElement(type);
		//grouptypeField.setText( type);
	}

	public void initializeFields() {
		nameField = new TextInput(nameFieldName);
		nameField.setLength(26);

		descriptionField = new TextArea(descriptionFieldName);
		descriptionField.setHeight(5);
		descriptionField.setWidth(43);
		descriptionField.setWrap(true);

		homepageField = new IBPageChooser(homepageFieldName);

		//grouptypeField = new DropdownMenu(grouptypeFieldName);
		grouptypeField = getTextObject();
		grouptypeField.setBold(false);
		
		memberofFrame = new IFrame("ic_user_memberof_ic_group", GroupList.class);
		memberofFrame.setHeight(150);
		memberofFrame.setWidth(367);
		memberofFrame.setScrolling(IFrame.SCROLLING_YES);
		//
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		String addRemove = "  " + iwrb.getLocalizedString("gen_addremove","Add/Remove") + "  ";

		addLink = new Link(addRemove);

		
		shortNameField = new TextInput(shortNameFieldName);
		shortNameField.setLength(26);
		
		abbrField = new TextInput(abbrFieldName);
		abbrField.setLength(26);		
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		nameText = new Text();// getTextObject();
		nameText.setText(iwrb.getLocalizedString("gen_name","Name") + ":");

		descriptionText = new Text();// getTextObject();
		descriptionText.setText(iwrb.getLocalizedString("gen_desc","Description") + ":");

		homepageText = new Text();//getTextObject();
		homepageText.setText(iwrb.getLocalizedString("gen_home","Home page") + ":");

		grouptypeText = new Text();//getTextObject();
		grouptypeText.setText(iwrb.getLocalizedString("gen_type","Group type") + ":");

		memberof = new Text();//getTextObject();
		memberof.setText(iwrb.getLocalizedString("gen_memberof","Member of") + ":");
		
		shortNameText = new Text();//getTextObject();
		shortNameText.setText(iwrb.getLocalizedString("gen_shortname","Short name") + ":");
		
		abbrText = new Text();//getTextObject();
		abbrText.setText(iwrb.getLocalizedString("gen_abbr","Abbrevation") + ":");		
	}

	public boolean store(IWContext iwc) {
		try {
			if (getGroupId() > -1) {

				Group group = getGroupBusiness(iwc).getGroupByGroupID(getGroupId());
				group.setName((String) fieldValues.get(nameFieldName));
				group.setDescription((String) fieldValues.get(descriptionFieldName));
				Integer homePageId = (Integer) fieldValues.get(homepageFieldName);
				if(homePageId.intValue()>0){
					group.setHomePageID(homePageId);
				}
				group.setGroupType((String) fieldValues.get(grouptypeFieldName));
				group.setShortName((String) fieldValues.get(shortNameFieldName));
				group.setAbbrevation((String) fieldValues.get(abbrFieldName));
				group.store();
			}
		}
		catch (Exception e) {
			//return false;
			e.printStackTrace(System.err);
			throw new RuntimeException("update group exception");
		}
		return true;
	}

	public void lineUpFields() {
		resize(1, 8);
		setCellpadding(0);
		setCellspacing(0);
		
		Table nameTable = new Table(2, 3);
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		nameTable.setWidth(1, 1, "50");
		nameTable.setWidth(1, 2, "50");
		nameTable.setWidth(1, 3, "50");
		nameTable.add(nameText, 1, 1);
		nameTable.add(nameField, 2, 1);
		nameTable.add(shortNameText, 1, 2);
		nameTable.add(shortNameField, 2, 2);
		nameTable.add(abbrText, 1, 3);
		nameTable.add(abbrField, 2, 3);
	  add(nameTable, 1, 1);

		Table homepageTable = new Table(2, 1);
		homepageTable.setCellpadding(0);
		homepageTable.setCellspacing(0);
		homepageTable.setWidth(1, "50");
		homepageTable.add(homepageText, 1, 1);
		homepageTable.add(homepageField, 2, 1);
	  add(homepageTable, 1, 2);

		Table grouptypeTable = new Table(2, 1);
		grouptypeTable.setCellpadding(0);
		grouptypeTable.setCellspacing(0);
		grouptypeTable.setWidth(1, "50");
		grouptypeTable.add(grouptypeText, 1, 1);
		grouptypeTable.add(grouptypeField, 2, 1);
		add(grouptypeTable, 1, 3);

		Table descriptionTable = new Table(1, 2);
		descriptionTable.setCellpadding(0);
		descriptionTable.setCellspacing(0);
		descriptionTable.setHeight(1, rowHeight);
		descriptionTable.add(descriptionText, 1, 1);
		descriptionTable.add(descriptionField, 1, 2);
		add(descriptionTable, 1, 4);

		add(memberof, 1, 5);
		add(memberofFrame, 1, 6);

		//		setHeight(3, "30");
		setHeight(1, super.rowHeight);
		setHeight(2, super.rowHeight);
		setHeight(3, super.rowHeight);
		setHeight(4, super.rowHeight);
		//		setHeight(6, super.rowHeight);

		add(addLink, 1, 7);
		Help help = getHelpButton();
		add(help,1,8);
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {

			String gname = iwc.getParameter(nameFieldName);
			String desc = iwc.getParameter(descriptionFieldName);
			String homepage = iwc.getParameter(homepageFieldName);
			String grouptype = iwc.getParameter(grouptypeFieldName);
			String gshortname = iwc.getParameter(shortNameFieldName);
			String gabbr = iwc.getParameter(abbrFieldName);

			if (gname != null) {
				fieldValues.put(nameFieldName, gname);
			}

			if (desc != null) {
				fieldValues.put(descriptionFieldName, desc);
			}

			if (homepage != null && !homepage.equals("")) {
				Integer page = Integer.valueOf(homepage);
				fieldValues.put(homepageFieldName, page);
			}

			if (grouptype != null) {
				fieldValues.put(grouptypeFieldName, grouptype);
			}

			if (gshortname != null) {
				fieldValues.put(shortNameFieldName, gshortname);
			}

			if (gabbr != null) {
				fieldValues.put(abbrFieldName, gabbr);
			}

			updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	public void initializeFieldNames() {
		descriptionFieldName = "UM_group_desc";
		nameFieldName = "UM_group_name";
		homepageFieldName = "UM_home_page";
		grouptypeFieldName = "UM_group_type";
		shortNameFieldName = "UM_group_short";
		abbrFieldName = "UM_group_abbr";
	}

	public void initializeFieldValues() {
		fieldValues.put(nameFieldName, "");
		fieldValues.put(descriptionFieldName, "");
		fieldValues.put(homepageFieldName, new Integer(0));
		fieldValues.put(grouptypeFieldName, "");
		fieldValues.put(shortNameFieldName, "");
		fieldValues.put(abbrFieldName, "");

		updateFieldsDisplayStatus();
	}

	public void dispose(IWContext iwc) {
		iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
		iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public void main(IWContext iwc) throws Exception {
		Object obj = getGroupBusiness(iwc).getParentGroups(getGroupId());
		if (obj != null) {
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED, obj);
		}
		else {
			iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
		}

		Object ob = getGroupBusiness(iwc).getParentGroupsInDirect(getGroupId());
		if (ob != null) {
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED, ob);
		}
		else {
			iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
		}

		_iwrb = getResourceBundle(iwc);
	}
  
  private void fillGroupTypeMenu(IWContext iwc, IWResourceBundle iwrb)  {
    GroupBusiness groupBusiness;
    Group group;
    String groupTypeString;
    try {
      groupBusiness =(GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
      group = groupBusiness.getGroupByGroupID(getGroupId());
      groupTypeString = group.getGroupType();
    }
    // Remote- and FinderException
    catch (Exception ex)  {
      throw new RuntimeException(ex.getMessage());
    }
    /*
    Collection groupTypes = groupBusiness.getAllAllowedGroupTypesForChildren(getSelectedParentGroupId(), iwc);
    boolean groupTypeOfCurrentGroupIsInList = false;
    Iterator iterator = groupTypes.iterator();
    while (iterator.hasNext())  {
      GroupType item = (GroupType) iterator.next();
      String value = item.getType();
      // check if the current group of this tab is in the returned list (usually it should be contained)
      if (value.equals(groupTypeString))
        groupTypeOfCurrentGroupIsInList = true; 
      grouptypeField.addMenuElement(value, iwrb.getLocalizedString(value, value));
    }  
    if (!groupTypeOfCurrentGroupIsInList)
      grouptypeField.addMenuElementFirst(groupTypeString, iwrb.getLocalizedString(groupTypeString, groupTypeString));
      */
		grouptypeField.setText(iwrb.getLocalizedString(groupTypeString, groupTypeString));
      
      
  }
	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle( ICUserConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
		
	}
}