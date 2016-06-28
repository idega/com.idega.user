package com.idega.user;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.view.DefaultViewNode;
import com.idega.core.view.FramedWindowClassViewNode;
import com.idega.core.view.KeyboardShortcut;
import com.idega.core.view.ViewManager;
import com.idega.dwr.event.DWREventService;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWUserContext;
import com.idega.user.app.UserApplication;
import com.idega.user.business.UserAppDWREventListener;
import com.idega.user.business.UserBusiness;
import com.idega.util.CoreConstants;

/**
 * A starter for adding the user app workspace node and more
 *
 * @author eiki
 *
 */
public class IWBundleStarter implements IWBundleStartable {

	static Logger log = Logger.getLogger(IWBundleStarter.class.getName());

	public static String IW_BUNDLE_IDENTIFIER = CoreConstants.IW_USER_BUNDLE_IDENTIFIER;

	@Override
	public void start(IWBundle starterBundle) {
		addViewNodes(starterBundle);
	}

	protected void addViewNodes(IWBundle starterBundle) {
		if (starterBundle != null) {
			IWMainApplication iwma = starterBundle.getApplication();
			//TODO finish layered userapp
			//registerLayeredUserApp(starterBundle, iwma);
			registerFramedUserApp(iwma);


			registerDWRListeners(starterBundle.getApplication().getIWApplicationContext());

		}
	}

	protected void registerLayeredUserApp(IWBundle starterBundle, IWMainApplication iwma) {
		ViewManager viewManager = ViewManager.getInstance(iwma);

		DefaultViewNode userNode = new DefaultViewNode("user", viewManager.getWorkspaceRoot());
		userNode.setFaceletUri(starterBundle.getFaceletURI("userapp.xhtml"));
		userNode.setName("#{localizedStrings['com.idega.user']['user']}");
		userNode.setKeyboardShortcut(new KeyboardShortcut("1"));

		// TODO implement a special viewnode so we can have a more advanced
		// permission check
		Collection<String> roles = new ArrayList<String>();
		roles.add(StandardRoles.ROLE_KEY_USERADMIN);
		userNode.setAuthorizedRoles(roles);
	}

	protected void registerDWRListeners(IWApplicationContext iwac) {
		DWREventService dwr;
		try {
			dwr = IBOLookup.getServiceInstance(iwac, DWREventService.class);
			dwr.registerListener(new UserAppDWREventListener());
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}

	}

	protected void registerFramedUserApp(IWMainApplication iwma){
		try {
			ViewManager viewManager = ViewManager.getInstance(iwma);

			Class<UserApplication> applicationClass = UserApplication.class;

			//inline class to override the default hasUserAccess to check to top node view access
			FramedWindowClassViewNode userNode = new FramedWindowClassViewNode("user",viewManager.getWorkspaceRoot()){

				@Override
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
						return IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), UserBusiness.class);
					}
					catch (IBOLookupException ile) {
						throw new IBORuntimeException(ile);
					}
				}

			};


			userNode.setKeyboardShortcut(new KeyboardShortcut("1"));
			userNode.setName("#{localizedStrings['com.idega.user']['iwapplication_name.UserApplication']}");
			Collection<String> roles = new ArrayList<String>();
			roles.add(StandardRoles.ROLE_KEY_USERADMIN);
			userNode.setAuthorizedRoles(roles);
			userNode.setWindowClass(applicationClass);

			String faceletPath = iwma.getBundle(CoreConstants.WORKSPACE_BUNDLE_IDENTIFIER).getFaceletURI("workspace.xhtml");
			userNode.setFaceletUri(faceletPath);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(IWBundle starterBundle) {
	}

}