package com.idega.user.business.search;

import java.util.Collection;

import org.jdom.Document;

import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOServiceBean;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Text;
import com.idega.user.helpers.UserHelper;
import com.idega.util.CoreUtil;

public class UserSearchEngineBean extends IBOServiceBean implements UserSearchEngine {

	private static final long serialVersionUID = -4520130366076513478L;
	
	private UserHelper helper = new UserHelper();
	
	public Collection getSearchResults(String searchKey) {
		try {
			return helper.getUserEntities(searchKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getUserBrowser(String searchKey) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}

		IWResourceBundle iwrb = getBundle().getResourceBundle(iwc);
		Collection entities = getSearchResults(searchKey);
		Layer container = new Layer();
		String message = iwrb.getLocalizedString("uc_no_results_were_found", "Sorry, no results were found");
		if (entities == null) {
			container.add(new Text(message));
			return BuilderLogic.getInstance().getRenderedPresentationObject(iwc, container, false);
		}
		else {
			if (entities.size() > 0) {
				StringBuffer resultText = new StringBuffer(iwrb.getLocalizedString("uc_results_for", "Results for"));
				resultText.append(": ").append(searchKey);
				message = resultText.toString();
			}
		}
		container.add(new Text(message));
		container.add(new Break());
		
		container.add(helper.getUserBrowser(entities, searchKey, iwc, 8));
		
		return BuilderLogic.getInstance().getRenderedPresentationObject(iwc, container, false);
	}

}
