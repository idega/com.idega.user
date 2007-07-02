package com.idega.user;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.view.FramedWindowClassViewNode;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWUserContext;
import com.idega.repository.data.RefactorClassRegistry;
import com.idega.user.business.UserBusiness;

/**
 * A starter for adding the user app workspace node and more
 * 
 * @author eiki
 * 
 */
public class IWBundleStarter implements IWBundleStartable {

	static Logger log = Logger.getLogger(IWBundleStarter.class.getName());

	public void start(IWBundle starterBundle) {
		addViewNodes(starterBundle);
	}

	private void addViewNodes(IWBundle starterBundle) {
		if (starterBundle != null) {
			IWMainApplication iwma = starterBundle.getApplication();
			ViewManager viewManager = ViewManager.getInstance(iwma);
			
			try {
				Class applicationClass = RefactorClassRegistry.forName("com.idega.user.app.UserApplication");
				
				//inline class to override the default hasUserAccess to check to top node view access
				FramedWindowClassViewNode userNode = new FramedWindowClassViewNode("user",viewManager.getWorkspaceRoot()){
				
					public boolean hasUserAccess(IWUserContext iwuc){
						try {
							return getUserBusiness().hasTopNodes(iwuc.getCurrentUser(), iwuc);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						return false;
					}
					
					public UserBusiness getUserBusiness() {
						try {
							return (UserBusiness) IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), UserBusiness.class);
						}
						catch (IBOLookupException ile) {
							throw new IBORuntimeException(ile);
						}
					}

				};
				
				
				userNode.setKeyboardShortcut(new KeyboardShortcut("1"));
				userNode.setName("#{localizedStrings['com.idega.user']['iwapplication_name.UserApplication']}");
				Collection roles = new ArrayList();
				roles.add(StandardRoles.ROLE_KEY_USERADMIN);
				userNode.setAuthorizedRoles(roles);
				userNode.setWindowClass(applicationClass);
				
				String jspPath = iwma.getBundle("com.idega.workspace").getJSPURI("workspace.jsp");
				userNode.setJspUri(jspPath);
				
				
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	public void stop(IWBundle starterBundle) {
	}

}