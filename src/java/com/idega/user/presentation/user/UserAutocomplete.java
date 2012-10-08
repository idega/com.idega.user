package com.idega.user.presentation.user;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.block.web2.business.Web2BusinessBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.IWUIBase;
import com.idega.user.business.UserConstants;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;
import com.idega.webface.WFUtil;

public class UserAutocomplete extends IWUIBase{

	IWResourceBundle iwrb = null;
	private Integer maxToShow;
	
	@Override
	protected void initializeComponent(FacesContext context) {
		getIwc(context);
		setTag("input");
		String name = getMarkupAttribute("name");
		if(StringUtil.isEmpty(name)){
			name = "autocompleted" + getId();
		}
		Integer maxToShow = getMaxToShow();
		if(maxToShow == null){
			maxToShow = 5;
		}
		setMarkupAttribute("name", "tag[]");
		getScriptOnLoad().append("UserAutocomplete.createAutocomplete('").append(getId())
				.append("',").append(maxToShow).append(",'").append(name).append(CoreConstants.JS_STR_PARAM_END);
	}
	
	
	protected IWResourceBundle getIwrb() {
		if(iwrb == null){
			iwrb = getIwc().getIWMainApplication().getBundle(UserConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(getIwc());
		}
		return iwrb;
	}

	protected void setIwrb(IWResourceBundle iwrb) {
		this.iwrb = iwrb;
	}
	
	@Override
	public List<String> getScripts() {
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		IWContext iwc = getIwc();
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.17","js/jquery-ui-1.8.17.custom.min.js"));
			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.17","ui.autocomplete.html.js"));


			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

			try{
				StringBuilder path = new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
				.append("/jquery-tagedit-remake.js");
				scripts.add(web2.getBundleURIWithinScriptsFolder(path.toString()));
				scripts.add(web2.getBundleURIWithinScriptsFolder(new StringBuilder(Web2BusinessBean.JQUERY_PLUGINS_FOLDER_NAME_PREFIX)
						.append(CoreConstants.SLASH)
						.append(Web2BusinessBean.TAGEDIT_SCRIPT_FILE_AUTOGROW).toString()));
			}catch(RemoteException e){
				getLogger().log(Level.WARNING,CoreConstants.EMPTY,e);
			}

		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/user-autocomplete.js"));
		scripts.add("/dwr/interface/UserAutocompleteBean.js");

		return scripts;
	}

	@Override
	public List<String> getStyleSheets() {
		List<String> styles = new ArrayList<String>();

		IWContext iwc = getIwc();
		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();

			styles.addAll(web2.getBundleURIsToTageditStyleFiles());
			styles.add(web2.getBundleURIToFancyBoxStyleFile());

			styles.add(jQuery.getBundleURIToJQueryUILib("1.8.17","themes/smoothness/ui-1.8.17.custom.css"));

			styles.add(web2.getBundleUriToHumanizedMessagesStyleSheet());



		}else{
			getLogger().log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}
		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		styles.add(iwb.getVirtualPathWithFileNameString("style/user-autocomplete.css"));
		return styles;
	}


	public Integer getMaxToShow() {
		return maxToShow;
	}


	public void setMaxToShow(Integer maxToShow) {
		this.maxToShow = maxToShow;
	}

}
