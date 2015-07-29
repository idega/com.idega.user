package com.idega.user.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeListener;

import com.idega.block.login.presentation.WelcomeMessage;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICDomain;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationState;
import com.idega.event.IWStateMachine;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.presentation.Script;
import com.idega.presentation.StatefullPresentation;
import com.idega.presentation.StatefullPresentationImplHandler;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.TopNodeGroup;
import com.idega.user.presentation.BasicUserOverview;
import com.idega.user.presentation.BasicUserOverviewPS;
import com.idega.user.presentation.GroupTreeView;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;

/**
 * <p>
 * Title: idegaWeb
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: idega Software
 * </p>
 *
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson </a>
 * @version 1.0
 */

public class UserApplicationControlArea extends Page implements IWBrowserView, StatefullPresentation {

	//this has been changed to match the styles in UserApplicationStyle.css:
	//private final static String LINK_STYLE =
	// "font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#000000;text-decoration:none;";
	private final static String LINK_STYLE = "font-family: verdana,helvetica,arial,sans-serif;font-size:9px;text-decoration:none;";

	//private final static String LINK_HOVER_STYLE =
	// "font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#FF8008;text-decoration:none;";
	private final static String LINK_HOVER_STYLE = "font-family: verdana,helvetica,arial,sans-serif;font-size:9px;text-decoration:none;";

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private IWBundle iwb;

	private IWResourceBundle iwrb;

	private StatefullPresentationImplHandler _stateHandler = null;

	private String styleScript = "DefaultStyle.css";

	private GroupTreeView groupTree = new GroupTreeView();

	private UserBusiness userBiz = null;


	public UserApplicationControlArea() {
		this.setAllMargins(0);
		this._stateHandler = new StatefullPresentationImplHandler();
		this._stateHandler.setPresentationStateClass(UserApplicationControlAreaPS.class);
	}

	@Override
	public void setControlEventModel(IWPresentationEvent model) {
		this.groupTree.setControlEventModel(model);
	}

	@Override
	public void setControlTarget(String controlTarget) {
		this.groupTree.setControlTarget(controlTarget);
	}

	@Override
	public Class<?> getPresentationStateClass() {
		return this._stateHandler.getPresentationStateClass();
	}

	@Override
	public IWPresentationState getPresentationState(IWUserContext iwuc) {
		return this._stateHandler.getPresentationState(this, iwuc);
	}

	public StatefullPresentationImplHandler getStateHandler() {
		return this._stateHandler;
	}

	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void initializeInMain(IWContext iwc) {

		this.iwb = getBundle(iwc);
		this.iwrb = getResourceBundle(iwc);

		IWLocation location = (IWLocation) this.getLocation().clone();
		location.setSubID(1);
		this.groupTree.setLocation(location, iwc);
		this.groupTree.setArtificialCompoundId(getCompoundId(), iwc);

		//    IWPresentationState gtState = groupTree.getPresentationState(iwc);
		//    if(gtState instanceof IWActionListener){
		//      groupTree.addIWActionListener((IWActionListener)gtState);
		//    }
		//
		//
		//
		//    EventListenerList list = this.getEventListenerList(iwc);
		//    IWActionListener[] listeners =
		// (IWActionListener[])list.getListeners(IWActionListener.class);
		//    if(listeners != null ){
		//      for (int i = 0; i < listeners.length; i++) {
		//        groupTree.addIWActionListener(listeners[i]);
		//      }
		//
		//    }

		//    UserApplicationControlAreaPS ps =
		// (UserApplicationControlAreaPS)this.getPresentationState(iwc);
		//    ps.addInnerListener(TreeViewerEvent.class,
		// (IWActionListener)groupTree.getPresentationState(iwc));

		//    groupTree.addIWActionListener((IWActionListener)ps);

		/**
		 * fix : EventListenerList list = this.getEventListenerList(iwc); fix :
		 * this.setIWUserContext(iwc);
		 */
		//    EventListenerList list = this.getEventListenerList(iwc);
		//this.setIWUserContext(iwc);

		IWPresentationState gtState = this.groupTree.getPresentationState(iwc);
		if (gtState instanceof IWActionListener) {
			((UserApplicationControlAreaPS) this.getPresentationState(iwc)).addIWActionListener((IWActionListener) gtState);
		}

		if (getParentPage() != null) {
			getParentPage().setStyleDefinition("A", LINK_STYLE);
			getParentPage().setStyleDefinition("A:hover", LINK_HOVER_STYLE);

		//TODO add bold stuff with a Behaviour and style class
			StringBuffer buffer = new StringBuffer();
			buffer.append("function setLinkToBold(input) {").append("\n\t");
			buffer.append("if (boldLink != null)").append("\n\t\t");
			buffer.append("boldLink.style.fontWeight='normal';").append("\n\t");
			buffer.append("if(input) input.style.fontWeight='bold';").append("\n\t");
			buffer.append("boldLink = input;").append("\n}");

			Script script = getParentPage().getAssociatedScript();
			script.addVariable("boldLink", "null");
			script.addFunction("setLinkToBold", buffer.toString());
		}

		BasicUserOverviewPS state = getPresentationStateOfBasicUserOverview(iwc);
		Group group = state.getSelectedGroup();
		ICDomain domain = state.getSelectedDomain();
		if (group != null) {
			int groupId = ((Integer) group.getPrimaryKey()).intValue();
			this.groupTree.setSelectedGroupId(groupId);
		}
		// use else if because both variables could be not null but only one
		// should be selected within the tree
		else if (domain != null) {
			int domainId = ((Integer) domain.getPrimaryKey()).intValue();
			this.groupTree.setSelectedGroupId(domainId);
		}
		this.groupTree.setToShowSuperRootNode(true);
		this.groupTree.setDefaultOpenLevel(iwc.getIWMainApplication().getSettings().getInt("groups_tree_levels_opened", 2));
		this.groupTree.setSuperRootNodeName(this.iwrb.getLocalizedString("tree.super.node.name", "My groups"));
		Image icon = this.iwb.getImage("super_root_icon.gif");
		this.groupTree.setSuperRootNodeIcon(icon);
		Collection<TopNodeGroup> topGroupNodes = null;
		try {
			topGroupNodes = getUserBusiness(iwc).getStoredTopGroupNodes(iwc.getCurrentUser());
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		if(topGroupNodes != null && topGroupNodes.size() > 0) {
			Image refreshIcon = this.iwb.getImage("refresh.gif");
			Link refreshLink = new Link(refreshIcon);
			this.groupTree.setRefreshLink(refreshLink);
		}

		ChangeListener[] chListeners = this.getPresentationState(iwc).getChangeListener();
		if (chListeners != null) {
			for (int i = 0; i < chListeners.length; i++) {
				gtState.addChangeListener(chListeners[i]);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public Table displayTable(IWContext iwc) {
		Table table = new Table(1, 2);
		table.setCellpadding(7);
		table.setCellpaddingTop(1, 2, 0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setHeight(1, Table.HUNDRED_PERCENT);
		table.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_CENTER);

		Table borderTable = new Table(1, 1);
		borderTable.setCellpadding(0);
		borderTable.setCellspacing(0);
		borderTable.setWidth(Table.HUNDRED_PERCENT);
		borderTable.setHeight(Table.HUNDRED_PERCENT);
		borderTable.setCellBorder(1, 1, 1, "#cccccc", "solid");
		borderTable.setColor(1, 1, "#FFFFFF");

		Layer layer = new Layer(Layer.DIV);
		layer.setStyleAttribute("width", "208px");
//		layer.setStyleAttribute("height", "100%");
		//layer.setStyleAttribute("border", "1px #cccccc solid");
		//layer.setStyleAttribute("background-color", "#ffffff");
		//layer.setStyleClass("main");
		//layer.setPadding(0);
		//layer.add(groupTree);
		layer.setStyleAttribute("overflow", "auto");
		borderTable.add(layer, 1, 1);

		Table treeTable = new Table(1, 1);
		treeTable.setCellpadding(4);
		treeTable.add(this.groupTree, 1, 1);
		treeTable.setWidth(Table.HUNDRED_PERCENT);
		treeTable.setHeight(Table.HUNDRED_PERCENT);
		treeTable.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		layer.add(treeTable);

		table.add(borderTable, 1, 1);

		Image image = getBundle(iwc).getImage("banner.gif");
		table.add(image, 1, 2);

		return table;
	}

	public Table welcomeMessageTable(IWContext iwc) {
		IWBundle iwb = getBundle(iwc);
		Image lockImage = iwb.getImage("las.gif");

		WelcomeMessage welcomeMessage = new WelcomeMessage();
		welcomeMessage.setBold();

		IWTimestamp s = IWTimestamp.RightNow();
		Text date = new Text(s.getDateString("EEEEEEEEEEEE dd.MM.yyyy", iwc.getCurrentLocale()));
		date.setFontClass("boldBlue");

		Table welcomeMessageTable = new Table(2, 1);
		welcomeMessageTable.setCellspacing(0);
		welcomeMessageTable.setCellpadding(0);
		welcomeMessageTable.setWidth(Table.HUNDRED_PERCENT);
		welcomeMessageTable.setHeight(42);
		welcomeMessageTable.setAlignment(1, 1, "center");
		welcomeMessageTable.setAlignment(2, 1, "left");
		welcomeMessageTable.setVerticalAlignment(1, 1, "middle");
		welcomeMessageTable.setVerticalAlignment(2, 1, "middle");
		if (iwc.isLoggedOn()) {
			welcomeMessageTable.add(lockImage, 1, 1);
		}
		welcomeMessageTable.add(welcomeMessage, 2, 1);
		welcomeMessageTable.add(Text.BREAK, 2, 1);
		welcomeMessageTable.add(date, 2, 1);

		return welcomeMessageTable;
	}

	public Layer displayLayer(IWContext iwc) {
		Layer layer = new Layer(Layer.DIV);
		layer.setID("treeLayer");
		layer.add(this.groupTree);

		return layer;
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		this.empty();

		IWBundle iwb = getBundle(iwc);
		String styleSrc = iwb.getVirtualPathWithFileNameString(this.styleScript);
		PresentationUtil.addStyleSheetToHeader(iwc, styleSrc);

		Table table = new Table(1, 2);
		table.setCellpaddingAndCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setHeight(1, 1, 42);
		table.setBackgroundImage(1, 1, iwb.getImage("bgtile.gif"));
		table.setHeight(1, 2, Table.HUNDRED_PERCENT);
		table.setVerticalAlignment(1, 2, Table.VERTICAL_ALIGN_TOP);
		table.setCellpaddingLeft(1, 1, 7);
		table.setStyleClass(1, 2, "back");

		table.add(welcomeMessageTable(iwc), 1, 1);
		table.add(displayTable(iwc), 1, 2);
		add(table);

		add(displayLayer(iwc));

		if (iwc.isSuperAdmin()) {
			GroupTreeNode node = new GroupTreeNode(iwc.getDomain(), iwc.getApplicationContext());
			this.groupTree.setRootNode(node);
		}
		else {
			UserBusiness biz = getUserBusiness(iwc);
			Collection<Group> groups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);
			Collection<GroupTreeNode> groupNodes = convertGroupCollectionToGroupNodeCollection(groups, iwc.getApplicationContext());

			this.groupTree.setFirstLevelNodes(groupNodes.iterator());

		}
	}

	public UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (this.userBiz == null) {
			try {
				this.userBiz = com.idega.business.IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (java.rmi.RemoteException rme) {
				throw new RuntimeException(rme.getMessage());
			}
		}
		return this.userBiz;
	}

	public Collection<GroupTreeNode> convertGroupCollectionToGroupNodeCollection(Collection<Group> col, IWApplicationContext iwac) {
		List<GroupTreeNode> list = new ArrayList<GroupTreeNode>();

		Iterator<Group> iter = col.iterator();
		while (iter.hasNext()) {
			Group group = iter.next();
			GroupTreeNode node = new GroupTreeNode(group, iwac);
			list.add(node);
		}

		return list;
	}

	private BasicUserOverviewPS getPresentationStateOfBasicUserOverview(IWUserContext iwuc) {
		try {
			IWStateMachine stateMachine = IBOLookup.getSessionInstance(iwuc, IWStateMachine.class);
			String code = IWMainApplication.getEncryptedClassName(BasicUserOverview.class);
			code = ":" + code;
			return (BasicUserOverviewPS) stateMachine.getStateFor(code, BasicUserOverviewPS.class);
		}
		catch (RemoteException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

}