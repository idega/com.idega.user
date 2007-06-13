package com.idega.user.presentation;

import com.idega.idegaweb.IWConstants;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.user.helpers.UserHelper;
import com.idega.util.CoreConstants;

/**
 * <p>Title: GroupChooserWindow</p>
 * <p>Description: In this window you named a group, select its parent group and group type</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gudmundur Agust Saemundsson</a>,<a href="eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.1
 */

public class GroupChooserWindow extends StyledAbstractChooserWindow {
	
	protected static final String SUBMIT_PARENT_FORM_AFTER_CHANGE = "submit_p_form";
	private static final int _width = 280;
	private static final int _height = 400;
	private static final String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";
	private String mainStyleClass = "main";
	/**
	 *
	 */
	public GroupChooserWindow() {
		setTitle("Group chooser");
		setWidth(_width);
		setHeight(_height);
		//   setCellpadding(5);
		setScrollbar(true);
		this.getLocation().setApplicationClass(this.getClass());
		this.getLocation().isInPopUpWindow(true);
	}
	
	
	
	/**
	 *
	 */
	public void displaySelection(IWContext iwc) {
		IWResourceBundle iwrb = this.getResourceBundle(iwc);
		addTitle(iwrb.getLocalizedString("select_group","Select group"), TITLE_STYLECLASS);
		setTitle(iwrb.getLocalizedString("select_group","Select group"));
		setName(iwrb.getLocalizedString("select_group","Select group"));
		setStyles();
		Table table = new Table(1,2);
		table.setStyleClass(this.mainStyleClass);
		table.setCellpaddingAndCellspacing(0);
		
		Text text = new Text(iwrb.getLocalizedString("select_group","Select group")+":");
		text.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
		table.add(text,1,1);
		
		UserHelper helper = new UserHelper();
		GroupTreeView viewer = helper.getGroupTree(iwc);
		viewer.setLocation((IWLocation)this.getLocation().clone());
		viewer.getLocation().setSubID(1);
		
		table.add(viewer,1,2);
		
		add(table,iwc);
		viewer.setToMaintainParameter(SCRIPT_PREFIX_PARAMETER,iwc);
		viewer.setToMaintainParameter(SCRIPT_SUFFIX_PARAMETER,iwc);
		viewer.setToMaintainParameter(DISPLAYSTRING_PARAMETER_NAME,iwc);
		viewer.setToMaintainParameter(VALUE_PARAMETER_NAME,iwc);
		viewer.setDefaultOpenLevel(1);
		
		Link link = new Link();
		link.setNoTextObject(true);
		viewer.setLinkPrototype(link);
		viewer.setTreeStyle(_linkStyle);
		viewer.setToUseOnClick();
		//sets the hidden input and textinput of the choosing page

		viewer.setOnClick(SELECT_FUNCTION_NAME+"("+GroupTreeView.ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME+","+GroupTreeView.ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME+");");
		//
		//    viewer.setNodeActionParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
		//
		viewer.setLinkPrototype(link);
	}
	
	protected String getPerformAfterSelectScriptString(IWContext iwc){
		boolean submitParentFormAfterChange = iwc.isParameterSet(SUBMIT_PARENT_FORM_AFTER_CHANGE);
		String script = null;
		
		if(submitParentFormAfterChange){
			script = iwc.getParameter(SCRIPT_PREFIX_PARAMETER);
			if(script==null){
				script = iwc.getParameter(SCRIPT_PREFIX_IN_A_FRAME);
			}
			
			if(script!=null){
				script = script+"submit();"+super.getPerformAfterSelectScriptString(iwc);
				
			}
			
		}
		else {
			script = super.getPerformAfterSelectScriptString(iwc);
		}
		
		return script;
	}
	
	private void setStyles() {
		String _linkStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#000000;text-decoration:none;";
		String _linkHoverStyle = "font-family:Arial,Helvetica,sans-serif;font-size:8pt;color:#FF8008;text-decoration:none;";
		if ( getParentPage() != null ) {
			getParentPage().setStyleDefinition("A",_linkStyle);
			//getParentPage().setStyleDefinition("A."+STYLE_NAME+":visited",_linkStyle);
			//getParentPage().setStyleDefinition("A."+STYLE_NAME+":active",_linkStyle);
			getParentPage().setStyleDefinition("A:hover",_linkHoverStyle);
		}
	}
	
	public String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}
}
