/*
 * Created on 5.5.2004
 */
package com.idega.user.block.homepage.presentation;

import javax.ejb.FinderException;

import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.text.Link;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;


/**
 * Title: OwnerGroupInformationLink
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: idega Software
 * @author 2004 - idega team - <br><a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson</a><br>
 * @version 1.0
 */
public class OwnerGroupInformationLink extends Link {

	public final int SHOW_NAME = 0;
	public final int SHOW_SHROT_NAME = 1;
	public final int SHOW_ABBREVATION = 3;
	
	
	private String textBefore = "";
	private String textAfter = "";
	
	private int informationToShow = SHOW_NAME;
	
	private boolean setPageAsDPTRootpage = false;
	
	
	
	public void setInformationToShow(int showConstant) {
		informationToShow = showConstant;
	}
	
	public void main(IWContext iwc) throws Exception {
		
		Page page = this.getParentPage();
		if(page != null) {
			int rootPageID = page.getDPTRootPageID();
			if(rootPageID != -1) {
				try {
					Group gr = ((GroupHome)IDOLookup.getHome(Group.class)).findByHomePageID(rootPageID);
					
					switch (informationToShow) {
						case SHOW_NAME:
							this.setText(textBefore+" "+gr.getName()+" "+textAfter);
							break;
						case SHOW_SHROT_NAME:
							this.setText(textBefore+" "+gr.getShortName()+" "+textAfter);
							break;
						case SHOW_ABBREVATION:
							this.setText(textBefore+" "+gr.getAbbrevation()+" "+textAfter);
							break;
						default:
							this.setText(textBefore+" "+gr.getName()+" "+textAfter);
							break;
					}
					
				} catch (FinderException e) {
					// No Group found
					System.out.println("["+this.getClassName()+"]: no Group has this page("+rootPageID+") as homepage");
				}
				if(setPageAsDPTRootpage) {
					this.setPage(rootPageID);
				}
			} else {
				this.setText(textBefore+" <pre> <info> </pre> "+textAfter);
			}
		}
		
		
		
	}
	
	public void setPageAsDPTRootPage(boolean value) {
		setPageAsDPTRootpage = value;
	}

	/**
	 * @param textAfer The textAfer to set.
	 */
	public void setTextAfter(String textAfter) {
		this.textAfter = textAfter;
	}
	/**
	 * @param textBefore The textBefore to set.
	 */
	public void setTextBefore(String textBefore) {
		this.textBefore = textBefore;
	}
	
	public Object clone() {
		OwnerGroupInformationLink og = (OwnerGroupInformationLink)super.clone();
		og.textAfter=this.textAfter;
		og.textBefore=this.textBefore;
		og.informationToShow=this.informationToShow;
		og.setPageAsDPTRootpage=this.setPageAsDPTRootpage;
		return og;
	}


}
