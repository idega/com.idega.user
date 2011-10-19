package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Phone;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableBodyRowGroup;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableHeaderRowGroup;
import com.idega.presentation.TableRow;
import com.idega.presentation.text.Heading3;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.user.util.UserComparator;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class GroupMembers extends Block {

	public static final String GROUP_UNIQUE_ID_PARAM = "groupUniqueId";

	private String uniqueId;
	
	private IWBundle bundle;
	private IWResourceBundle iwrb;

	@Autowired
	private UserApplicationEngine userApp;
	
	@Override
	public String getBundleIdentifier() {
		return CoreConstants.IW_USER_BUNDLE_IDENTIFIER;
	}
	
	@Override
	public void main(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		
		bundle = getBundle(iwc);
		iwrb = bundle.getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("groupMembers");
		
		if (iwc.isParameterSet(GROUP_UNIQUE_ID_PARAM))
			uniqueId = iwc.getParameter(GROUP_UNIQUE_ID_PARAM);
		if (StringUtil.isEmpty(uniqueId)) {
			container.add(new Heading3(iwrb.getLocalizedString("group_is_not_provided", "Group is not provided")));
			return;
		}
		
		GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
		Group group = groupHome.findGroupByUniqueId(uniqueId);
		UserBusiness userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		Collection<User> users = null;
		try {
			users = userBusiness.getUsersInGroup(group);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		Locale locale = iwc.getCurrentLocale();
		
		if (ListUtil.isEmpty(users)) {
			container.add(new Heading3(iwrb.getLocalizedString("there_are_no_members_in_group", "There are no members in group") + " " + group.getNodeName(locale)));
			return;
		}
		
		List<User> groupMembers = new ArrayList<User>(users);
		Collections.sort(groupMembers, new UserComparator(locale));
		
		container.add(new Heading3(iwrb.getLocalizedString("group_members_of_group", "The members of a group") + CoreConstants.SPACE + group.getNodeName(locale) +
				CoreConstants.COLON));
		
		Table2 table = new Table2();
		container.add(table);
		TableHeaderRowGroup header = table.createHeaderRowGroup();
		TableRow row = header.createRow();
		row.createCell().add(new Text(iwrb.getLocalizedString("nr", "Nr")));
		row.createCell();
		row.createCell().add(new Text(iwrb.getLocalizedString("name", "Name")));
		row.createCell().add(new Text(iwrb.getLocalizedString("personal_id", "Personal ID")));
		row.createCell().add(new Text(iwrb.getLocalizedString("phone", "Phone")));
		row.createCell().add(new Text(iwrb.getLocalizedString("email", "E-mail")));
		TableBodyRowGroup body = table.createBodyRowGroup();
		for (int i = 0; i < groupMembers.size(); i++) {
			row = body.createRow();
			
			User user = groupMembers.get(i); 
			UserDataBean data = userApp.getUserInfo(user);
			
			//	Number
			TableCell2 cell = row.createCell();
			cell.add(new Text(String.valueOf(i + 1)));
			
			//	Image
			Image image = new Image();
			image.setURL(data.getPictureUri());
			cell = row.createCell();
			cell.add(image);
			
			//	Name
			cell = row.createCell();
			cell.add(new Text(user.getName()));
			
			//	Personal ID
			cell = row.createCell();
			String personalId = user.getPersonalID();
			cell.add(new Text(StringUtil.isEmpty(personalId) ? CoreConstants.MINUS : personalId));
			
			//	Phones
			cell = row.createCell();
			cell.add(getPhones(user.getPhones()));
			
			//	Email
			String email = data.getEmail();
			cell = row.createCell();
			if (StringUtil.isEmpty(email)) {
				cell.add(new Text(CoreConstants.MINUS));
			} else {
				Link mail = new Link(email);
				mail.setURL("mailto:" + email);
				cell.add(mail);
			}
		}
	}
	
	private Text getPhones(Collection<?> phones) {
		if (ListUtil.isEmpty(phones))
			return new Text(CoreConstants.MINUS);
		
		StringBuilder phonesString = new StringBuilder();
		for (Iterator<?> phonesIter = phones.iterator(); phonesIter.hasNext();) {
			phonesString.append(((Phone) phonesIter.next()).getNumber());
			if (phonesIter.hasNext()) {
				phonesString.append("; ");
			}
		}
		return new Text(phonesString.toString());
	}
	
}