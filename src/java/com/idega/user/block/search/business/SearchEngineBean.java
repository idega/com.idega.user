package com.idega.user.block.search.business;

//import javax.ejb.EJBException;
//import java.rmi.RemoteException;
import com.idega.util.ListUtil;
import java.util.*;
//import com.idega.user.data.UserHome;
//import com.idega.data.IDOLookup;
import com.idega.user.data.User;
import com.idega.user.block.search.event.SimpleSearchEvent;
import com.idega.business.IBOServiceBean;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class SearchEngineBean extends IBOServiceBean
{
//	private static final String AND = "AND";
//	private static final String OR = "OR";
//	private static final String QUOTE = "\"";
	private static final String WHITE_SPACE = " ";
//	private static final String PLUS = "+";


    public SearchEngineBean()
    {

    }



	public Collection getResult(SimpleSearchEvent e){
		String searchString = e.getSearchString();
		if(searchString != null){
			StringTokenizer tokenizer = new StringTokenizer(searchString);
			Vector words = new Vector();
			while (tokenizer.hasMoreTokens())
			{
				words.add(tokenizer.nextToken(WHITE_SPACE));
			}

			Collection toReturn = new Vector();

			switch (e.getSearchType())
			{
				case SimpleSearchEvent.SEARCHTYPE_USER:
					System.out.println("["+this.getClass()+"]: search for User");
					/*try
					{
						UserHome usrHome = (UserHome)IDOLookup.getHome(User.class);*/
						// firstname = word.get(0), lastname = word.get(word.size()), middlename = word.get(1 -> (size -1))

						// firstname = word.get(0->size)

						// lastname = word.get(0->size)

						// middlename = word.get(0->size)

						return toReturn;
					/*}
					catch (RemoteException ex)
					{
						throw new EJBException(ex);
					}*/


				case SimpleSearchEvent.SEARCHTYPE_GROUP:
					System.out.println("["+this.getClass()+"]: search for Group");
					return toReturn;
				default:
					throw new UnsupportedOperationException("SearchType not known");
			}

		} else {
	        return ListUtil.getEmptyList();
		}
	}

	public Class getResultType(SimpleSearchEvent e){
	    return User.class;
	}
}