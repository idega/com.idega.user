/*
 * Created on 4.5.2004
 */
package com.idega.user.block.homepage.presentation;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.builder.dynamicpagetrigger.data.PageTriggerInfo;
import com.idega.builder.dynamicpagetrigger.data.PageTriggerInfoHome;
import com.idega.builder.presentation.IBPageChooser;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.component.data.ICObject;
import com.idega.core.component.data.ICObjectHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.development.presentation.IWDeveloper;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.block.homepage.business.HomePageBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.GroupType;
import com.idega.user.data.GroupTypeHome;


/**
 * Title: HomePageTrigger
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: idega Software
 * @author 2004 - idega team - <br><a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson</a><br>
 * @version 1.0
 */
public class HomePageGenerator extends Block {
	
	private final static String _prmGroupType = "ic_gr_t";
	private final static String _prmCreateForGroup = "ic_cr_f_gr";
	private final static String _prmDetachPageAndGroup = "ic_dt_pg";
	private final static String _prmPageTriggerInfo = "ic_pti";
	
	
	private String _blockWidth = Table.HUNDRED_PERCENT;
	
	/**
	 * 
	 */
	public HomePageGenerator() {
		super();
	}
	
	
	
	public void main(IWContext iwc) throws Exception {
		add(IWDeveloper.getTitleTable(this.getClass()));
		if (!iwc.isIE())
			getParentPage().setBackgroundColor("#FFFFFF");
		
		
		String pDetachPageAndGroup = iwc.getParameter(_prmDetachPageAndGroup);
		if(pDetachPageAndGroup!= null && !"".equals(pDetachPageAndGroup)) {
			System.out.println("Detach page from Group: "+pDetachPageAndGroup);
			
			GroupHome grHome = ((GroupHome)IDOLookup.getHome(Group.class));
			Group myGroup = grHome.findByPrimaryKey(grHome.decode(pDetachPageAndGroup));
			
			myGroup.setHomePage(null);
			
			myGroup.store();
			
		}
		
		String pCreateForGroup = iwc.getParameter(_prmCreateForGroup);
		if(pCreateForGroup!= null && !"".equals(pCreateForGroup)) {
			String pPageTriggerInfo = iwc.getParameter(_prmPageTriggerInfo);
			System.out.println("Create pages for Group: "+pCreateForGroup);
			
			GroupHome grHome = ((GroupHome)IDOLookup.getHome(Group.class));
			Group myGroup = grHome.findByPrimaryKey(grHome.decode(pCreateForGroup));
			
			PageTriggerInfoHome ptHome = ((PageTriggerInfoHome)IDOLookup.getHome(PageTriggerInfo.class));
			PageTriggerInfo ptInfo = ptHome.findByPrimaryKey(ptHome.decode(pPageTriggerInfo));
			
			HomePageBusiness hpBusiness = (HomePageBusiness)IBOLookup.getServiceInstance(iwc,HomePageBusiness.class);
			
			hpBusiness.createHomePage(iwc,myGroup,ptInfo);
			
		}
		

		
		Form myForm = new Form();
		myForm.maintainParameter(IWDeveloper.actionParameter);
		myForm.maintainParameter(IWDeveloper.PARAMETER_CLASS_NAME);
		
		Table myTable = new Table();
		myTable.setWidth(_blockWidth);
		//myTable.setBorder(1);
		
		Table dropdownTable = new Table();
		//myTable.setWidth(_blockWidth);
		//myTable.setBorder(1);
		
		boolean someError = false;
		
		try {
			
			ICObject refObj = ((ICObjectHome)IDOLookup.getHome(ICObject.class)).findByClassName(Group.class.getName());
			
			
			Collection procedures = ((PageTriggerInfoHome)IDOLookup.getHome(PageTriggerInfo.class)).findAllByICObjectID(refObj);
			String pGroupType = iwc.getParameter(_prmGroupType);
			if(pGroupType != null) {
				Text procedureText = IWDeveloper.getText("Procedure:");
				dropdownTable.add(procedureText,1,2);
				dropdownTable.setNoWrap(1,2);
				
				DropdownMenu proceduresMenu = new DropdownMenu(procedures,_prmPageTriggerInfo);
				proceduresMenu.keepStatusOnAction();
				dropdownTable.add(proceduresMenu,2,2);
				
				
				Table groupListTable = new Table();
				groupListTable.setCellspacing(0);
				groupListTable.setCellpadding(0);
				//groupListTable.setBorder(1);
				groupListTable.setWidth(_blockWidth);
				groupListTable.setNoWrap();
				
				Collection groups = ((GroupHome)IDOLookup.getHome(Group.class)).findGroupsByType(pGroupType);
					
				groupListTable.add(IWDeveloper.getText("Group name"),1,1);
				groupListTable.add(IWDeveloper.getText("Homepage"),2,1);
				groupListTable.setColumnWidth(2,"200");
				groupListTable.setColumnAlignment(2,Table.HORIZONTAL_ALIGN_CENTER);
				groupListTable.add(IWDeveloper.getText("Create"),3,1);
				groupListTable.setColumnWidth(3,"100");
				groupListTable.setColumnAlignment(3,Table.HORIZONTAL_ALIGN_CENTER);
				
				groupListTable.setLineAfterRow(1);
				
				
				int tableRow = 2;
				for (Iterator iter = groups.iterator(); iter.hasNext(); tableRow++) {
					Group group = (Group) iter.next();
					groupListTable.add(group.getName(),1,tableRow);
					
					ICPage homePage = group.getHomePage();
					
					if(homePage!=null) {
						IBPageChooser page = new IBPageChooser("-");
						page.setSelectedPage(((Integer)homePage.getPrimaryKey()).intValue(),homePage.getName());
						page.setDisabled(true);
						groupListTable.add(page,2,tableRow);
						
						groupListTable.add(new SubmitButton("Detach",_prmDetachPageAndGroup,group.getPrimaryKey().toString()),3,tableRow);

					} else {
						groupListTable.add(new Text(" - "),2,tableRow);
						
						
						groupListTable.add(new SubmitButton("Create",_prmCreateForGroup,group.getPrimaryKey().toString()),3,tableRow);
						
						
						
					}
					
					groupListTable.setAlignment(2,tableRow,Table.HORIZONTAL_ALIGN_CENTER);
					groupListTable.setAlignment(3,tableRow,Table.HORIZONTAL_ALIGN_CENTER);

				}
				
				myTable.add(groupListTable,1,2);
				
			}
			
			Text grouptypeText = IWDeveloper.getText("GroupType:");
			dropdownTable.add(grouptypeText,1,1);
			dropdownTable.setNoWrap(1,1);
			
			Collection groupTypes = ((GroupTypeHome)IDOLookup.getHome(GroupType.class)).findVisibleGroupTypes();
			DropdownMenu groupTypeMenu = new DropdownMenu(groupTypes,_prmGroupType);
			groupTypeMenu.keepStatusOnAction();
			groupTypeMenu.setToSubmit();
			dropdownTable.add(groupTypeMenu,2,1);
			
			dropdownTable.add(new SubmitButton("Go"),3,1);
			
			
			
			
			
		} catch (IDOLookupException e) {
			someError=true;
			e.printStackTrace();
		} catch (FinderException e) {
			someError=true;
			e.printStackTrace();
		} finally {
			if(someError) {
				add("No procedure available for "+Group.class);
				add(Text.getBreak());
				add("Make sure that "+Group.class+" is registered and then create a procedure in "+PageTriggerInfo.class);
			}
		}
		
		
		
		myTable.add(dropdownTable,1,1);
		myForm.add(myTable);
		this.add(myForm);
	}
	
	
	

}
