package com.idega.user.block.homepage.business;


import java.rmi.RemoteException;

import com.idega.builder.dynamicpagetrigger.business.DPTTriggerBusinessBean;
import com.idega.builder.dynamicpagetrigger.data.PageLink;
import com.idega.builder.dynamicpagetrigger.data.PageTriggerInfo;
import com.idega.data.IDOLookupException;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;
import com.idega.user.data.User;

/**
 * Title: HomePageBusinessBean 
 * Description: 
 * Copyright: 
 * Copyright (c) 2004
 * Company: idega Software
 * @author 2004 - idega team -<br><a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson </a> <br>
 * @version 1.0
 */

public class HomePageBusinessBean extends DPTTriggerBusinessBean implements HomePageBusiness {

	public static int tmpHardcodedPageTriggerInfoId = 1;

	public HomePageBusinessBean() {
	}
	
	public void createHomePage(IWContext iwc, Group group, PageTriggerInfo info) throws RemoteException, Exception {
		Group ownerGroup = null;
		if(!User.USER_GROUP_TYPE.equals(group.getGroupType())) {
			ownerGroup = group;
		}
		
		PageLink pageLink = createPageLink(iwc, info, group.getPrimaryKey().toString(), group.getName(),ownerGroup, null, null, null, null);

		if (pageLink != null) {
			group.setHomePageID(pageLink.getPageId());
			group.store();
			
		} else {
			// throw Exception;
		}
	}


	public boolean invalidateGroup(IWContext iwc, Group group) throws IDOLookupException {

		//GroupHome grHome = ((GroupHome)IDOLookup.getHome(Group.class));
		
		group.setHomePage(null);
		
		group.store();
		
//		PageLink link = ((PageLinkHome)IDOLookup.getHome(PageLink.class)).find
		
//			List l = EntityFinder.findRelated(p,PageLinkBMPBean.getStaticInstance(PageLink.class));
//			if (l != null && l.size() > 0) {
//				boolean b = invalidatePageLink(iwc, (PageLink) l.get(0), User.get);
//				if (!b) {
//					return false;
//				}
//			}

			return true;
	}

} //