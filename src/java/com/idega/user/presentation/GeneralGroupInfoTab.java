package com.idega.user.presentation;

import java.rmi.RemoteException;
import com.idega.builder.presentation.IBPageChooser;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.data.ICTreeNode;
import com.idega.core.ldap.util.IWLDAPConstants;
import com.idega.core.ldap.util.IWLDAPUtil;
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
import com.idega.user.data.GroupTypeBMPBean;
import com.idega.util.Disposable;

/**
 * Title: User Description: Copyright: Copyright (c) 2001 Company: idega.is
 * 
 * @author 2000 - idega team - <a href="mailto:gummi@idega.is">Gu�mundur
 *         �g�st S�mundsson </a>
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
	//universally unique id
	private TextInput uuidField;
	private Link linkToAliasedGroup;
	//generated distinguised name, ldap attribute (ou) that we find out by asking for recursively parents of this group
	private TextInput rdnField;
	
	private Text nameText;
	private Text descriptionText;
	private Text homepageText;
	private Text grouptypeText;
	private Text shortNameText;
	private Text abbrText;
	private Text uuidText;
	private Text linkToAliasedGroupText;
	private Text rdnText;
	
	private String nameFieldName;
	private String descriptionFieldName;
	private String homepageFieldName;
	private String grouptypeFieldName;
	private String shortNameFieldName;
	private String abbrFieldName;
	private String uuidFieldName;
	private String rdnFieldName;

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
		if (iwc.isSuperAdmin()) {
		    this.addLink.setWindowToOpen(GroupGroupSetter.class);
			this.addLink.setStyleClass(this.linkStyle);
			this.addLink.addParameter(PARAMETER_GROUP_ID, getGroupId());
			this.addLink.addParameter(PARENT_GROUP_ID, getSelectedParentGroupId());
		}
		try {
			Group group = (((GroupHome) com.idega.data.IDOLookup.getHome(Group.class)).findByPrimaryKey(new Integer(
					getGroupId())));
			this.fieldValues.put(this.nameFieldName, (group.getName() != null) ? group.getName() : "");
			this.fieldValues.put(this.descriptionFieldName, (group.getDescription() != null) ? group.getDescription() : "");
			this.fieldValues.put(this.homepageFieldName, new Integer(group.getHomePageID()));
			this.fieldValues.put(this.grouptypeFieldName, (group.getGroupType() != null) ? group.getGroupType() : "");
			this.fieldValues.put(this.shortNameFieldName, (group.getShortName() != null) ? group.getShortName() : "");
			this.fieldValues.put(this.abbrFieldName, (group.getAbbrevation() != null) ? group.getAbbrevation() : "");
			this.fieldValues.put(this.uuidFieldName, (group.getUniqueId() != null) ? group.getUniqueId() : "");
			if (group.getGroupType().equals(GroupTypeBMPBean.TYPE_ALIAS)) {
				Group alias = group.getAlias();
				this.linkToAliasedGroup.setText(alias.getName());
				//linkToAliasedGroup.setStyleClass(linkStyle);
				this.linkToAliasedGroup.setWindowToOpen(GroupPropertyWindow.class);
				this.linkToAliasedGroup.addParameter(GroupPropertyWindow.PARAMETERSTRING_GROUP_ID, alias.getPrimaryKey().toString());
				this.linkToAliasedGroupText.setText(iwrb.getLocalizedString("gen_openaliasedgroup", "Open aliased group")+": ");
				
			}
			
			String rdn = group.getMetaData(IWLDAPConstants.LDAP_META_DATA_KEY_DIRECTORY_STRING);
			if(rdn==null){
				rdn = IWLDAPUtil.getInstance().getGeneratedRDNFromGroup(group);
			}
			this.fieldValues.put(this.rdnFieldName, (rdn != null) ? rdn : "");
			
			updateFieldsDisplayStatus();
		}
		catch (Exception e) {
			System.err.println("GeneralGroupInfoTab error initFieldContents, GroupId : " + getGroupId());
		}
	}

	public void updateFieldsDisplayStatus() {
		this.nameField.setContent((String) this.fieldValues.get(this.nameFieldName));
		this.descriptionField.setContent((String) this.fieldValues.get(this.descriptionFieldName));
		Integer page = (Integer) this.fieldValues.get(this.homepageFieldName);
		if (page != null) {
			int pageId = page.intValue();
			IWApplicationContext iwc = getIWApplicationContext();
			//Map tree = PageTreeNode.getTree(iwc);
			//if (tree != null) {
			BuilderService bservice;
			try {
				bservice = getBuilderService(iwc);
				ICTreeNode node = bservice.getPageTree(pageId);
				if (node != null) {
					this.homepageField.setSelectedPage(node.getNodeID(), node.getNodeName());
				}
			}
			catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//}
		}
		this.shortNameField.setContent((String) this.fieldValues.get(this.shortNameFieldName));
		this.abbrField.setContent((String) this.fieldValues.get(this.abbrFieldName));
		
		this.uuidField.setContent((String) this.fieldValues.get(this.uuidFieldName));
		this.rdnField.setContent((String) this.fieldValues.get(this.rdnFieldName));
		//String type = (String) fieldValues.get(grouptypeFieldName);
		//grouptypeField.setSelectedElement(type);
		//grouptypeField.setText( type);
	}

	public void initializeFields() {
		this.nameField = new TextInput(this.nameFieldName);
		this.nameField.setLength(26);
		this.descriptionField = new TextArea(this.descriptionFieldName);
		this.descriptionField.setHeight(5);
		this.descriptionField.setWidth(Table.HUNDRED_PERCENT);
		this.descriptionField.setWrap(true);
		this.homepageField = new IBPageChooser(this.homepageFieldName);
		//grouptypeField = new DropdownMenu(grouptypeFieldName);
		this.grouptypeField = new Text();
		this.grouptypeField.setBold(false);
		this.memberofFrame = new IFrame("ic_user_memberof_ic_group", GroupList.class);
		this.memberofFrame.setHeight(150);
		this.memberofFrame.setWidth(Table.HUNDRED_PERCENT);
		this.memberofFrame.setScrolling(IFrame.SCROLLING_YES);
		this.memberofFrame.setStyleAttribute("border", "1px #b2b2b2 solid");
		//
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		if (iwc.isSuperAdmin()) {
			String addRemove = "  " + iwrb.getLocalizedString("gen_addremove", "Add/Remove") + "  ";
			this.addLink = new Link(addRemove);
		}
		this.shortNameField = new TextInput(this.shortNameFieldName);
		this.shortNameField.setLength(26);
		this.abbrField = new TextInput(this.abbrFieldName);
		this.abbrField.setLength(26);
		
		this.uuidField = new TextInput(this.uuidFieldName);
		this.uuidField.setLength(36);
		this.uuidField.setMaxlength(36);
		this.linkToAliasedGroup = new Link("");
		this.rdnField = new TextInput(this.rdnFieldName);
		this.rdnField.setLength(72);
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		this.nameText = new Text(iwrb.getLocalizedString("gen_name", "Name"));
		this.nameText.setBold();
		this.descriptionText = new Text(iwrb.getLocalizedString("gen_desc", "Description"));
		this.descriptionText.setBold();
		this.homepageText = new Text(iwrb.getLocalizedString("gen_home", "Home page"));
		this.homepageText.setBold();
		this.grouptypeText = new Text(iwrb.getLocalizedString("gen_type", "Group type"));
		this.grouptypeText.setBold();
		this.memberof = new Text(iwrb.getLocalizedString("gen_memberof", "Member of"));
		this.memberof.setBold();
		this.shortNameText = new Text(iwrb.getLocalizedString("gen_shortname", "Short name"));
		this.shortNameText.setBold();
		this.abbrText = new Text(iwrb.getLocalizedString("gen_abbr", "Abbrevation"));
		this.abbrText.setBold();
		this.uuidText = new Text(iwrb.getLocalizedString("gen_uuid", "Unique id"));
		this.uuidText.setBold();
		this.linkToAliasedGroupText = new Text("");
		this.linkToAliasedGroupText.setBold();
		this.rdnText = new Text(iwrb.getLocalizedString("gen_rdn", "RDN"));
		this.rdnText.setBold();
	}

	public boolean store(IWContext iwc) {
		try {
			if (getGroupId() > -1) {
				Group group = getGroupBusiness(iwc).getGroupByGroupID(getGroupId());
				group.setName((String) this.fieldValues.get(this.nameFieldName));
				group.setDescription((String) this.fieldValues.get(this.descriptionFieldName));
				Integer homePageId = (Integer) this.fieldValues.get(this.homepageFieldName);
				if (homePageId.intValue() > 0) {
					group.setHomePageID(homePageId);
				}
				group.setGroupType((String) this.fieldValues.get(this.grouptypeFieldName));
				group.setShortName((String) this.fieldValues.get(this.shortNameFieldName));
				group.setAbbrevation((String) this.fieldValues.get(this.abbrFieldName));
				
				if(iwc.isSuperAdmin()){
					group.setUniqueId((String) this.fieldValues.get(this.uuidFieldName));
					group.setMetaData(IWLDAPConstants.LDAP_META_DATA_KEY_DIRECTORY_STRING, (String) this.fieldValues.get(this.rdnFieldName));
				}
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
		resize(1, 1);
		setCellpadding(0);
		setCellspacing(0);
		Table table = new Table();
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.add(this.nameText, 1, 1);
		table.add(Text.getBreak(), 1, 1);
		table.add(this.nameField, 1, 1);
		table.add(this.shortNameText, 2, 1);
		table.add(Text.getBreak(), 2, 1);
		table.add(this.shortNameField, 2, 1);
		table.add(this.abbrText, 1, 2);
		table.add(Text.getBreak(), 1, 2);
		table.add(this.abbrField, 1, 2);
		table.add(this.homepageText, 2, 2);
		table.add(Text.getBreak(), 2, 2);
		table.add(this.homepageField, 2, 2);
		table.add(this.grouptypeText, 1, 3);
		table.add(Text.getBreak(), 1, 3);
		table.add(this.grouptypeField, 1, 3);
		table.add(this.linkToAliasedGroupText, 2, 3);
		table.add(Text.getBreak(), 2, 3);
		table.add(this.linkToAliasedGroup, 2, 3);
		
		
		if(IWContext.getInstance().isSuperAdmin()){
			table.mergeCells(1,4,2,4);
			table.add(this.uuidText, 1, 4);
			table.add(Text.getBreak(), 1, 4);
			table.add(this.uuidField, 1, 4);
			table.addBreak(1,4);
			table.addBreak(1,4);
			table.add(this.rdnText, 1, 4);
			table.add(Text.getBreak(), 1, 4);
			table.add(this.rdnField, 1, 4);
		}
		
		table.mergeCells(1, 5, 2, 5);
		table.add(this.descriptionText, 1, 5);
		table.add(Text.getBreak(), 1, 5);
		table.add(this.descriptionField, 1, 5);
		table.mergeCells(1, 6, 2, 6);
		table.add(this.memberof, 1, 6);
		table.add(Text.getBreak(), 1, 6);
		table.add(this.memberofFrame, 1, 6);
		table.add(Text.getBreak(), 1, 6);
		if (this.addLink != null) {
		    table.add(this.addLink, 1, 6);
		}
		add(table, 1, 1);
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {
			String gname = iwc.getParameter(this.nameFieldName);
			String desc = iwc.getParameter(this.descriptionFieldName);
			String homepage = iwc.getParameter(this.homepageFieldName);
			String grouptype = iwc.getParameter(this.grouptypeFieldName);
			String gshortname = iwc.getParameter(this.shortNameFieldName);
			String gabbr = iwc.getParameter(this.abbrFieldName);
			String uuid = iwc.getParameter(this.uuidFieldName);
			String rdn = iwc.getParameter(this.rdnFieldName);
			
			if (gname != null) {
				this.fieldValues.put(this.nameFieldName, gname);
			}
			if (desc != null) {
				this.fieldValues.put(this.descriptionFieldName, desc);
			}
			if (homepage != null && !homepage.equals("")) {
				Integer page = Integer.valueOf(homepage);
				this.fieldValues.put(this.homepageFieldName, page);
			}
			if (grouptype != null) {
				this.fieldValues.put(this.grouptypeFieldName, grouptype);
			}
			if (gshortname != null) {
				this.fieldValues.put(this.shortNameFieldName, gshortname);
			}
			if (gabbr != null) {
				this.fieldValues.put(this.abbrFieldName, gabbr);
			}
			if(uuid!=null && !"".equals(uuid)){
				this.fieldValues.put(this.uuidFieldName,uuid);
			}
			
			if(rdn!=null && !"".equals(rdn)){
				this.fieldValues.put(this.rdnFieldName,rdn);
			}
			
			updateFieldsDisplayStatus();
			return true;
		}
		return false;
	}

	public void initializeFieldNames() {
		this.descriptionFieldName = "UM_group_desc";
		this.nameFieldName = "UM_group_name";
		this.homepageFieldName = "UM_home_page";
		this.grouptypeFieldName = "UM_group_type";
		this.shortNameFieldName = "UM_group_short";
		this.abbrFieldName = "UM_group_abbr";
		this.uuidFieldName = "UM_group_uuid";
		this.rdnFieldName = "UM_group_rdn";
	}

	public void initializeFieldValues() {
		this.fieldValues.put(this.nameFieldName, "");
		this.fieldValues.put(this.descriptionFieldName, "");
		this.fieldValues.put(this.homepageFieldName, new Integer(0));
		this.fieldValues.put(this.grouptypeFieldName, "");
		this.fieldValues.put(this.shortNameFieldName, "");
		this.fieldValues.put(this.abbrFieldName, "");
		this.fieldValues.put(this.uuidFieldName, "");
		this.fieldValues.put(this.rdnFieldName, "");
		
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
		getPanel().addHelpButton(getHelpButton());
		//used by the GroupList class
		Object obj = iwc.getSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED);
		
		if (obj == null) {
			obj = getGroupBusiness(iwc).getParentGroups(getGroupId());
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_DIRECTLY_RELATED, obj);
		}
		
		Object ob = iwc.getSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
		
		if (ob == null) {
			ob = getGroupBusiness(iwc).getParentGroupsInDirect(getGroupId());
			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED, ob);
		}
		
//		String rdn = getGroupBusiness(iwc).getParentGroupsInDirect(getGroupId());
//		if (ob != null) {
//			iwc.setSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED, ob);
//		}
//		else {
//			iwc.removeSessionAttribute(GeneralGroupInfoTab.SESSIONADDRESS_GROUPS_NOT_DIRECTLY_RELATED);
//		}
		
		
		
		this._iwrb = getResourceBundle(iwc);
	}

	private void fillGroupTypeMenu(IWContext iwc, IWResourceBundle iwrb) {
		GroupBusiness groupBusiness;
		Group group;
		String groupTypeString;
		try {
			groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			group = groupBusiness.getGroupByGroupID(getGroupId());
			groupTypeString = group.getGroupType();
		}
		// Remote- and FinderException
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
		/*
		 * Collection groupTypes =
		 * groupBusiness.getAllAllowedGroupTypesForChildren(getSelectedParentGroupId(),
		 * iwc); boolean groupTypeOfCurrentGroupIsInList = false; Iterator
		 * iterator = groupTypes.iterator(); while (iterator.hasNext()) {
		 * GroupType item = (GroupType) iterator.next(); String value =
		 * item.getType(); // check if the current group of this tab is in the
		 * returned list (usually it should be contained) if
		 * (value.equals(groupTypeString)) groupTypeOfCurrentGroupIsInList =
		 * true; grouptypeField.addMenuElement(value,
		 * iwrb.getLocalizedString(value, value)); } if
		 * (!groupTypeOfCurrentGroupIsInList)
		 * grouptypeField.addMenuElementFirst(groupTypeString,
		 * iwrb.getLocalizedString(groupTypeString, groupTypeString));
		 */
		this.grouptypeField.setText(iwrb.getLocalizedString(groupTypeString, groupTypeString));
	}

	public Help getHelpButton() {
		IWContext iwc = IWContext.getInstance();
		IWBundle iwb = getBundle(iwc);
		Help help = new Help();
		Image helpImage = iwb.getImage("help.gif");
		help.setHelpTextBundle(UserConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(HELP_TEXT_KEY);
		help.setImage(helpImage);
		return help;
	}
}