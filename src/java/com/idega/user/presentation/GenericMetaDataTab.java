package com.idega.user.presentation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.idega.data.IDOEntity;
import com.idega.data.MetaDataCapable;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.TextInput;

/**
 * A generic tab for displaying and editing metadata for any entity bean.<br>
 * Use the constructor to initilize the tab with the desired entity interface.<br>
 * The entity must implement MetaDataCapable (GenericEntity does).<br>
 * It will also have access to the current user/group because it extends UserTab.<br>
 * TODO allow the user to ADD and remove metadata
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 1.0
 */

public class GenericMetaDataTab extends UserTab {
	protected static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	protected static final String TAB_NAME = "generic_metadata_tab";
	protected static final String DEFAULT_TAB_NAME = "Metadata";
	protected static final String HELP_TEXT_KEY = "generic_metadata_tab";
	protected IDOEntity entity;
	protected Map metaDataInputsMap = new HashMap();
	protected Map metaDataInputTitlesMap = new HashMap();
	
	public GenericMetaDataTab(IDOEntity entity) {
		super();
		this.entity = entity;
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
		
		initializeFields();
		initializeTexts();
		lineUpFields();
	}

	/**
	 * Overridden because we want to contruct the tab with an entity
	 */
	public void init() {}

	public void initializeFieldNames() {}
	public void initializeFieldValues() {}

	public void updateFieldsDisplayStatus() {
//		get the metadata values and create all the inputs
		Map metaDataMap = getMetaDataMap();
		
		if(!metaDataMap.isEmpty()){
			Iterator iter = metaDataMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				//get the type and add different inputs, see productcatalogue
				String value = getMetaData(key);
				InterfaceObject input = (InterfaceObject)metaDataInputsMap.get(key);
				if(value!=null){
					input.setContent(value);
				}
			}	
		}
	}

	public void initializeFields() {
		//get the metadata values and create all the inputs
		Map metaDataMap = getMetaDataMap();
		
		if(!metaDataMap.isEmpty()){
			Iterator iter = metaDataMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String value = getMetaData(key);
				InterfaceObject input;
				//get the type and add different inputs, see productcatalogue
				if(value!=null || !"".equals(value)){
					input = new TextInput(key,value);
				}else{
					input = new TextInput(key);
				}
				metaDataInputsMap.put(key,input);
			}	
		}
	}

	protected Map getMetaDataMap() {
		Map metaDataMap = ((MetaDataCapable)entity).getMetaDataAttributes();
		return metaDataMap;
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);
		Map metaDataMap = getMetaDataMap();
		
		if(!metaDataMap.isEmpty()){
			Iterator iter = metaDataMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				//bold text
				Text title = new Text(iwrb.getLocalizedString(key,key),true,false,false);
				//title.setFontStyle("font-size:8px");
				metaDataInputTitlesMap.put(key,title);
			}	
		}
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

	public void lineUpFields() {
		this.resize(1, 1);
		//TODO Eiki change tab behaviour so it does not go many times into main
//		ScrollTable table = new ScrollTable();
//		table.setNumberOfHeaderRows(0);
	//	table.setScrollLayerHeaderRowThickness(50);  // prior 47
		
		Table table = new Table();
		table.setWidth(450);
		table.setHeight("100%");
		table.setColumns(2);
		table.setCellpadding(5);
		table.setCellspacing(0);
		table.setBorder(0);
		
		table.add(new Text("key"),1,1);
		table.add(new Text("value"),2,1);
		table.setRowVerticalAlignment(1,Table.VERTICAL_ALIGN_TOP);
		
		
		
		this.add(table, 1, 1);
		
		if(!metaDataInputsMap.isEmpty()){
			int row = 2;
			Iterator iter = metaDataInputsMap.keySet().iterator();
			while(iter.hasNext()){
				String key = (String) iter.next();
				table.add((PresentationObject)metaDataInputTitlesMap.get(key), 1, row);
				table.add((PresentationObject)metaDataInputsMap.get(key), 2, row);
				table.setRowVerticalAlignment(row,Table.VERTICAL_ALIGN_TOP);
				row++;
			}
		}


	}

	public void main(IWContext iwc) {
		getPanel().addHelpButton(getHelpButton());
	}

	public boolean collect(IWContext iwc) {
		if (iwc != null) {
//			get the metadata values and create all the inputs
			Map metaDataMap = getMetaDataMap();
			
			if(!metaDataMap.isEmpty()){
				Iterator iter = metaDataMap.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					//what about getParameterValues() ?
					String value = iwc.getParameter(key);
					setMetaData(key, value);
				}	
			}

			this.updateFieldsDisplayStatus();

			return true;
		}
		return false;
	}

	protected void setMetaData(String key, String value) {
		((MetaDataCapable)entity).setMetaData(key, value);
	}

	protected String getMetaData(String key) {
		return ((MetaDataCapable)entity).getMetaData(key);
	}
	
	public boolean store(IWContext iwc) {
		
		entity.store();
		
		return true;
	}

	public void initFieldContents() {

		try {
			
			this.updateFieldsDisplayStatus();

		} catch (Exception e) {
			System.err.println("UserPhoneTab error initFieldContents, userId : " + getUserId());
		}
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

} // Class UserPhoneTab
