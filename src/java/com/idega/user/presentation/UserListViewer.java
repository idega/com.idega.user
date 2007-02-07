/**
 * 
 */
package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Email;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.MailToLink;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Label;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.user.util.UserComparator;
import com.idega.util.IWTimestamp;
import com.idega.util.text.Name;


/**
 * <p>
 * TODO laddi Describe Type UserListViewer
 * </p>
 *  Last modified: $Date: 2007/02/07 20:46:30 $ by $Author: laddi $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision: 1.1 $
 */
public class UserListViewer extends Block {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	
	private Object iGroupPK = null;
	private String id = null;
	private String iMetadata = null;

	private IWBundle iwb;
	private IWResourceBundle iwrb;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.Block#getBundleIdentifier()
	 */
	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	@Override
	public void main(IWContext iwc) throws Exception {
		iwb = getBundle(iwc);
		iwrb = getResourceBundle(iwc);
		
		if (iGroupPK != null) {
			Group group = getUserBusiness(iwc).getGroupHome().findByPrimaryKey(new Integer(iGroupPK.toString()));
			
			add(getUserGroupList(iwc, group));
		}
		else {
			add(new Text(iwrb.getLocalizedString("no_group_selected", "No group selected")));
		}
	}
	
	private Layer getUserGroupList(IWContext iwc, Group group) throws RemoteException {
		Layer layer = new Layer();
		layer.setStyleClass("userGroupList");
		if (id != null) {
			layer.setID(id);
		}
		
		List users = new ArrayList(getUserBusiness(iwc).getUsersInGroup(group));
		if (users != null && !users.isEmpty()) {
			Collections.sort(users, new UserComparator(iwc.getCurrentLocale()));
			
			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				Email email = null;
				try {
					email = getUserBusiness(iwc).getUsersMainEmail(user);
				}
				catch (NoEmailFoundException e1) {
					//No email registered
				}
				
				Layer entry = new Layer();
				entry.setStyleClass("userEntry");
				layer.add(entry);
				
				if (user.getSystemImageID() > -1) {
					try {
						Image image = new Image(user.getSystemImageID());
						image.setStyleClass("image");
						entry.add(image);
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				}
				else {
					Image image = iwb.getImage("user_image.gif");
					image.setStyleClass("image");
					entry.add(image);
				}
				
				Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
				Layer userObject = new Layer();
				userObject.setStyleClass("userObject");
				userObject.setStyleClass("name");
				Label label = new Label();
				label.setLabel(iwrb.getLocalizedString("user_list_viewer.name", "Name"));
				userObject.add(label);
				Span span = new Span();
				span.add(new Text(name.getName(iwc.getCurrentLocale())));
				userObject.add(span);
				entry.add(userObject);
				
				if (getUserBusiness(iwc).getUserJob(user) != null) {
					userObject = new Layer();
					userObject.setStyleClass("userObject");
					userObject.setStyleClass("job");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("user_list_viewer.job", "Job"));
					userObject.add(label);
					span = new Span();
					span.add(new Text(getUserBusiness(iwc).getUserJob(user)));
					userObject.add(span);
					entry.add(userObject);
				}

				if (user.getDescription() != null && user.getDescription().length() > 0) {
					userObject = new Layer();
					userObject.setStyleClass("userObject");
					userObject.setStyleClass("description");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("user_list_viewer.description", "Description"));
					userObject.add(label);
					span = new Span();
					span.add(new Text(user.getDescription()));
					userObject.add(span);
					entry.add(userObject);
				}

				if (user.getDateOfBirth() != null) {
					IWTimestamp stamp = new IWTimestamp(user.getDateOfBirth());

					userObject = new Layer();
					userObject.setStyleClass("userObject");
					userObject.setStyleClass("dateOfBirth");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("user_list_viewer.date_of_birth", "Date of birth"));
					userObject.add(label);
					span = new Span();
					span.add(new Text(stamp.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.MEDIUM)));
					userObject.add(span);
					entry.add(userObject);
				}

				if (iMetadata != null) {
					StringTokenizer tokens = new StringTokenizer(iMetadata, ",");
					while (tokens.hasMoreTokens()) {
						String key = tokens.nextToken();
						String value = user.getMetaData(key);

						if (value != null) {
							if (value.length() > 0) {
								userObject = new Layer();
								userObject.setStyleClass("userObject");
								userObject.setStyleClass(key);
								label = new Label();
								label.setLabel(iwrb.getLocalizedString("user_list_viewer." + key, key));
								userObject.add(label);
								span = new Span();
								span.add(new Text(value));
								userObject.add(span);
								entry.add(userObject);
							}
						}
						else {
							user.setMetaData(key, "");
						}
					}
				}
				
				if (email != null && email.getEmailAddress() != null) {
					MailToLink link = new MailToLink(email.getEmailAddress(), email.getEmailAddress());
					
					userObject = new Layer();
					userObject.setStyleClass("userObject");
					userObject.setStyleClass("email");
					label = new Label();
					label.setLabel(iwrb.getLocalizedString("user_list_viewer.email", "E-mail"));
					userObject.add(label);
					span = new Span();
					span.add(link);
					userObject.add(span);
					entry.add(userObject);
				}
			}
		}
		
		return layer;
	}
	
	private UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException e) {
			throw new IBORuntimeException(e);
		}
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setMetadata(String metadata) {
		this.iMetadata = metadata;
	}
	
	public void setGroupPK(Object groupPK) {
		this.iGroupPK = groupPK;
	}
}