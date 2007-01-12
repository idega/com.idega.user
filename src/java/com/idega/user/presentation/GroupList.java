package com.idega.user.presentation;
import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupComparator;
import com.idega.user.data.CachedGroup;
import com.idega.user.data.Group;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

/**
 * Former innerClass of UserGroupList
 */

public class GroupList extends Page {

	private Collection groups = null;
	private GroupBusiness groupBiz = null;
	private GroupComparator groupComparator = null;

	public GroupList() {
		super();
	}

	public Table getGroupTable(IWContext iwc) {

		Collection direct =
			(Collection) iwc.getSessionAttribute(
				UserGroupList.SESSIONADDRESS_USERGROUPS_DIRECTLY_RELATED);
		Collection notDirect =
			(Collection) iwc.getSessionAttribute(
				UserGroupList.SESSIONADDRESS_USERGROUPS_NOT_DIRECTLY_RELATED);

		ArrayList allGroups = new ArrayList();
		if (direct != null) {
		    allGroups.addAll(direct);
		}
		if (notDirect != null) {
			allGroups.addAll(notDirect);
		}
		this.groupComparator = new GroupComparator(iwc);
		this.groupComparator.setGroupBusiness(this.getGroupBusiness(iwc));		
		this.groupComparator.setSortByParents(true);
		Collections.sort(allGroups, this.groupComparator); 
		Table table = null;
		try {
			Iterator iter = null;
			int row = 1;
			if (allGroups != null) {
				table = new Table(3, allGroups.size());

				iter = allGroups.iterator();
				while (iter.hasNext()) {
					Object item = iter.next();
					if (item != null) {
					    CachedGroup cachedGroup = null;
						Group group = null;
					    Integer groupID = (Integer)((Group) item).getPrimaryKey();
					    String key = groupID.toString();
					    if (this.groupComparator.getApplicationCachedGroups()!=null) {
							if (this.groupComparator.getApplicationCachedGroups().containsKey(key)) {
								cachedGroup = (CachedGroup)this.groupComparator.getApplicationCachedGroups().get(key);
							}
							else
							{	
							    group = getGroupBusiness(iwc).getGroupByGroupID(groupID.intValue());
							    cachedGroup = new CachedGroup(group);
							    this.groupComparator.getApplicationCachedGroups().put(key, cachedGroup);
							}
						}
						else {
						    group = getGroupBusiness(iwc).getGroupByGroupID(groupID.intValue());
						    cachedGroup = new CachedGroup(group);
						}
						
						String name = this.groupComparator.getIndentedGroupName(cachedGroup);
						Text text = new Text(name);
						if (direct.contains(item)) {
						    text.setBold();
						}
						table.add(text, 2, row++);
					} else {
						System.err.println("ITEM IS NULL in grouplist for D");
					}
				}

			} 
		} catch (Exception e) {
			add("Error: " + e.getMessage());
			e.printStackTrace();
		}

		if (table != null) {
			table.setWidth("100%");
			table.setWidth(1, "1");
			table.setWidth(3, "10");
		}

		return table;
	}

	public void main(IWContext iwc) throws Exception {
		this.getParentPage().setAllMargins(0);
		Table tb = getGroupTable(iwc);
		if (tb != null) {
			this.add(tb);
		}
	}
	
	public GroupBusiness getGroupBusiness(IWContext iwc) {
		if (this.groupBiz == null) {
			try {
				this.groupBiz = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return this.groupBiz;
	}

}