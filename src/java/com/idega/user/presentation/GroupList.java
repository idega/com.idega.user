package com.idega.user.presentation;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.Table;
import com.idega.user.data.Group;

import java.util.Collection;
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

		Table table = null;
		try {
			Iterator iter = null;
			int row = 1;
			if (direct != null && notDirect != null) {
				table = new Table(5, direct.size() + notDirect.size());

				iter = direct.iterator();
				while (iter.hasNext()) {
					Object item = iter.next();
					if (item != null) {
						table.add("D", 1, row);
						table.add(((Group) item).getName(), 3, row++);
					} else {
						System.err.println("ITEM IS NULL in grouplist for D");
					}
				}

				iter = notDirect.iterator();
				while (iter.hasNext()) {
					Object item = iter.next();
					if (item != null) {
						table.add("E", 1, row);
						table.add(((Group) item).getName(), 3, row++);
					} else {
						System.err.println("ITEM IS NULL in grouplist for E");
					}

				}

			} else if (direct != null) {
				table = new Table(5, direct.size());
				iter = direct.iterator();
				while (iter.hasNext()) {
					Object item = iter.next();
					if (item != null) {
						table.add("D", 1, row);
						table.add(((Group) item).getName(), 3, row++);
					} else {
						System.err.println(
							"ITEM IS NULL in grouplist for D latter");
					}

				}
			}
		} catch (Exception e) {
			add("Error: " + e.getMessage());
			e.printStackTrace();
		}

		if (table != null) {
			table.setWidth("100%");
			table.setWidth(1, "10");
			table.setWidth(2, "3");
			table.setWidth(4, "10");
			table.setWidth(5, "10");
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

}