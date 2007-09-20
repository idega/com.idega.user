package com.idega.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
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
import com.idega.repository.data.RefactorClassRegistry;
import com.idega.user.business.UserAppDWREventListener;

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
		userNode.setJspUri(starterBundle.getJSPURI("userapp.jsp"));
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
			dwr = (DWREventService) IBOLookup.getServiceInstance(iwac, DWREventService.class);
			dwr.registerListener(new UserAppDWREventListener());
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void registerFramedUserApp(IWMainApplication iwma){
		try {
			ViewManager viewManager = ViewManager.getInstance(iwma);
			Class applicationClass = RefactorClassRegistry.forName("com.idega.user.app.UserApplication");
			FramedWindowClassViewNode userNode = new FramedWindowClassViewNode("user",viewManager.getWorkspaceRoot());
			userNode.setKeyboardShortcut(new KeyboardShortcut("1"));
			
			Collection<String> roles = new ArrayList<String>();
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

	public void stop(IWBundle starterBundle) {
	}

}
