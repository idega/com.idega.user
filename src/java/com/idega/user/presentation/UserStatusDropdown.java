package com.idega.user.presentation;

import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ejb.FinderException;

import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.user.data.Status;
import com.idega.user.data.StatusHome;


/**
 * @author gimmi
 */
public class UserStatusDropdown extends DropdownMenu{

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  public static final String NO_STATUS_KEY = "default_key";
  public static final String DEFAULT_INPUT_NAME = "ic_user_status";
  
  // if you change this variable: default value is NOT localized yet
  public static final String NO_STATUS_VALUE = "";
  
	private Collection statuses;
	private IWResourceBundle iwrb;

	public UserStatusDropdown(){
		this(DEFAULT_INPUT_NAME);
	}
	
	public UserStatusDropdown(String name) {
		super(name);
	}

	public void init(IWContext iwc) {
		try {
			iwrb = getResourceBundle(iwc);

			StatusHome sHome = (StatusHome) IDOLookup.getHome(Status.class);
			statuses = sHome.findAll();
		} catch (IDOLookupException e) {
			e.printStackTrace(System.err);
		} catch (FinderException e) {
			e.printStackTrace(System.err);
		}
	}

	public void main(IWContext iwc) throws Exception{
		init(iwc);
		
		if (statuses != null) {
      // first add the default value
      addMenuElement(NO_STATUS_KEY, NO_STATUS_VALUE);
      SortedMap stringPrimaryKeyMap = getSortedStatuses(statuses, iwc);
			Iterator iter = stringPrimaryKeyMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				addMenuElement( (String) entry.getValue(), (String) entry.getKey());	
			}
		}
		super.main(iwc);
	}
	
	public String getBundleIdentifier(){
	  return IW_BUNDLE_IDENTIFIER;
	}
  
  private SortedMap getSortedStatuses(Collection statuses, IWContext iwc)  {
    // get collator
    Locale locale = iwc.getIWMainApplication().getSettings().getDefaultLocale();
    Collator collator = Collator.getInstance(locale);
    // create sorted map
    SortedMap stringPrimaryKeyMap = new TreeMap(collator);
    Status status;
    Iterator iterator = statuses.iterator();
    // fill the sorted map
    while (iterator.hasNext())  {
      status = (Status) iterator.next();
      String primaryKey = status.getPrimaryKey().toString();
      String key = status.getStatusKey();
      String string = iwrb.getLocalizedString(key, key);
      // use the localized string as key because the map is ordered by the keys not by the values
      stringPrimaryKeyMap.put(string, primaryKey);
    }
    return stringPrimaryKeyMap;
  }
}
