package com.idega.user.block.search.business;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import com.idega.business.IBOServiceBean;
import com.idega.data.IDOLookup;
import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.ListUtil;
/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */
public class SearchEngineBean extends IBOServiceBean implements SearchEngine{
	public SearchEngineBean() {
	}
	
	public Collection getResult(SimpleSearchEvent e) throws RemoteException {
		return getResult(e.getSearchType(), e.getSearchString());
	}
	
	public Class getResultType(SimpleSearchEvent e) {
		return User.class;
	}
	
	public Collection getResult(int searchType, String searchString) throws RemoteException {
		if (searchString != null) {
			switch (searchType) {
				case SimpleSearchEvent.SEARCHTYPE_USER :
					return getEntities(searchString);
				case SimpleSearchEvent.SEARCHTYPE_GROUP :
					System.out.println("[" + this.getClass() + "]: search for Group");
					return new Vector();
				default :
					throw new UnsupportedOperationException("SearchType not known");
			}
		}
		else {
			//TODO implement for group
			return ListUtil.getEmptyList();
		}
	}
	
	private Collection getEntities(String searchString) {
		if (searchString == null)
			return new ArrayList();
		try {
			UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
			Collection entities = userHome.findUsersBySearchCondition(searchString);
			return entities;
		}
		// Remote and FinderException
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}