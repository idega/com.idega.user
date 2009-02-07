package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.IntegerInput;
import com.idega.presentation.ui.Label;
import com.idega.user.app.SimpleUserApp;
import com.idega.user.app.SimpleUserAppHelper;
import com.idega.user.bean.SimpleUserPropertiesBean;
import com.idega.user.business.GroupHelper;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.User;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class GroupMembersListViewer extends Block {
	
	private Random idGenerator = new Random();
	private String idPrefix = "id";
	
	private SimpleUserPropertiesBean bean = null;
	private String image = null;
	private String containerId = null;
	private boolean checkIds = true;
	
	private Integer leftIndex;
	private Integer rightIndex;
	private Integer count;
	
	@Override
	public void main(IWContext iwc) {
		if (bean == null || image == null || StringUtil.isEmpty(containerId)) {
			return;
		}
		GroupHelper helper = ELUtil.getInstance().getBean(GroupHelper.class);
		if (helper == null) {
			return;
		}
		
		List<User> allUsers = helper.getUsersInGroup(iwc, bean, false);
		if (ListUtil.isEmpty(allUsers)) {
			return;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		List<User> users = getNeededPartOfUsers(helper.getSortedUsers(allUsers, iwc.getCurrentLocale(), bean));
		
		Layer container = new Layer();
		fixId(container);
		add(container);
		
		Layer pagingContainer = getPagingContainer(iwc, users, allUsers.size());
		if (pagingContainer != null) {
			container.add(pagingContainer);
		}
		
		User user = null;
		String userValuesLineContainerStyleClass = "userValuesLineContainerStyleClass";
		String nameContainerStyleClass = "userNameValueContainerStyleClass";
		String personalIdContainerStyleClass = "userPersonalIdValueContainerStyleClass";
		String emailContainerStyleClass = "userEmailValueContainerStyleClass";
		String phoneContainerStyleClass = "userPhoneValueContainerStyleClass";
		String changeUserContainerStyleClass = "changeUserImageContainerStyleClass";
		String removeUserContainerStyleClass = "removeUserCheckboxContainerStyleClass";
		String changeUserImageStyleClass = "changeUserImageStyleClass";
		StringBuffer checkBoxAction = null;
		
		String odd = "odd";
		String even = "even";
		String unknown = iwrb.getLocalizedString("unknown", "Unknown");
		String name = null;
		String personalId = null;
		String userId = null;
		String email = null;
		List<String> phoneNumbers = null;
		int groupId = -1;
		int from = getLeftIndex() + 1;
		for (int i = 0; i < users.size(); i++) {
			user = users.get(i);
			
			userId = user.getId();
			name = user.getName();
			if (CoreConstants.EMPTY.equals(name)) {
				name = null;
			}
			personalId = user.getPersonalID();
			if (CoreConstants.EMPTY.equals(personalId)) {
				personalId = null;
			}
			email = getUserEmail(iwc, user);
			phoneNumbers = getUserPhonesNumbers(iwc, user);
			
			groupId = bean.getGroupId();
			if (groupId < 0) {
				groupId = bean.getParentGroupId();
			}
			
			Layer lineContainer = new Layer();
			fixId(lineContainer);
			lineContainer.setStyleClass(userValuesLineContainerStyleClass);
			if (i % 2 == 0) {
				lineContainer.setStyleClass(even);
			}
			else {
				lineContainer.setStyleClass(odd);
			}
			container.add(lineContainer);
			
			Layer nameContainer = new Layer();
			fixId(nameContainer);
			nameContainer.setStyleClass(nameContainerStyleClass);
			Text nameText = new Text(name == null ? unknown : new StringBuilder().append(from + i).append(CoreConstants.DOT).append(CoreConstants.SPACE)
																				.append(name).toString());
			fixId(nameText);
			nameContainer.add(nameText);
			lineContainer.add(nameContainer);
			
			Layer personalIdContainer = new Layer();
			fixId(personalIdContainer);
			personalIdContainer.setStyleClass(personalIdContainerStyleClass);
			Text personalIdText = new Text(personalId == null ? unknown : personalId);
			fixId(personalIdText);
			personalIdContainer.add(personalIdText);
			lineContainer.add(personalIdContainer);
			
			Layer emailContainer = new Layer();
			fixId(emailContainer);
			emailContainer.setStyleClass(emailContainerStyleClass);
			Text emailText = StringUtil.isEmpty(email) ? new Text(CoreConstants.MINUS) : new Link(email);
			fixId(emailText);
			emailContainer.add(emailText);
			lineContainer.add(emailContainer);
			
			Layer phonesContainer = new Layer();
			fixId(phonesContainer);
			phonesContainer.setStyleClass(phoneContainerStyleClass);
			fillPhonesSection(phonesContainer, phoneNumbers);
			lineContainer.add(phonesContainer);
			
			Layer changeUserContainer = new Layer();
			fixId(changeUserContainer);
			changeUserContainer.setStyleClass(changeUserContainerStyleClass);
			Image changeUserImage = new Image(image);
			fixId(changeUserImage);
			changeUserImage.setStyleClass(changeUserImageStyleClass);
			changeUserImage.setTitle(iwrb.getLocalizedString("change_user", "Change user"));
			changeUserImage.setOnClick(helper.getActionForAddUserView(bean, userId));
			changeUserContainer.add(changeUserImage);
			lineContainer.add(changeUserContainer);
			
			Layer removeUserContainer = new Layer();
			fixId(removeUserContainer);
			removeUserContainer.setStyleClass(removeUserContainerStyleClass);
			CheckBox removeUserCheckbox = new CheckBox();
			removeUserCheckbox.setTitle(iwrb.getLocalizedString("remove_user", "Remove user"));
			fixId(removeUserCheckbox);
			checkBoxAction = new StringBuffer("removeUser('").append(lineContainer.getId());
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(userId);
			checkBoxAction.append(SimpleUserApp.PARAMS_SEPARATOR).append(groupId).append("', ");
			checkBoxAction.append(helper.getJavaScriptParameter(removeUserCheckbox.getId())).append(");");
			removeUserCheckbox.setOnClick(checkBoxAction.toString());
			removeUserContainer.add(removeUserCheckbox);
			lineContainer.add(removeUserContainer);
		}
		
		if (StringUtil.isEmpty(bean.getInstanceId())) {
			Logger.getLogger(GroupMembersListViewer.class.getSimpleName()).log(Level.WARNING, "Instance ID is unknown, can't set pager values");
		}
		else {
			List<Integer> properties = new ArrayList<Integer>(3);
			properties.add(getLeftIndex());
			properties.add(getRightIndex());
			properties.add(getCount());
			
			UserApplicationEngine userAppEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
			userAppEngine.setPagerProperties(userAppEngine.getIdForPagerProperties(bean), properties);
		}
	}
	
	private void fillPhonesSection(Layer container, List<String> numbers) {
		if (ListUtil.isEmpty(numbers)) {
			Text noNumbers = new Text(CoreConstants.MINUS);
			fixId(noNumbers);
			container.add(noNumbers);
			return;
		}
		
		for (Iterator<String> numbersIter = numbers.iterator(); numbersIter.hasNext();) {
			Text numberLine = new Text(numbersIter.next());
			fixId(numberLine);
			container.add(numberLine);
			
			if (numbersIter.hasNext()) {
				container.add(new Break());
			}
		}
	}
	
	private UserBusiness getUserBusiness(IWContext iwc) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getUserEmail(IWContext iwc, User user) {
		Email email = null;;
		try {
			email = getUserBusiness(iwc).getUsersMainEmail(user);
		} catch (Exception e) {}
		if (email == null) {
			return null;
		}
		
		return email.getEmailAddressMailtoFormatted();
	}
	
	private List<String> getUserPhonesNumbers(IWContext iwc, User user) {
		Phone[] phones = null;
		try {
			phones = getUserBusiness(iwc).getUserPhones(user);
		} catch(Exception e) {}
		if (ArrayUtil.isEmpty(phones)) {
			return null;
		}

		String number = null;
		List<String> numbers = new ArrayList<String>();
		List<Phone> filteredPhones = new ArrayList<Phone>();
		for (Phone phone: phones) {
			number = phone.getNumber();
			if (!StringUtil.isEmpty(number) && !numbers.contains(number)) {
				numbers.add(number);
				filteredPhones.add(phone);
			}
		}
		
		if (ListUtil.isEmpty(filteredPhones)) {
			return null;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		List<String> phoneNumbers = new ArrayList<String>(filteredPhones.size());
		for (Phone phone: filteredPhones) {
			switch(phone.getPhoneTypeId()) {
			case PhoneTypeBMPBean.HOME_PHONE_ID: {
				phoneNumbers.add(new StringBuilder(iwrb.getLocalizedString("home_phone_number", "Home")).append(CoreConstants.COLON)
						.append(CoreConstants.SPACE).append(phone.getNumber()).toString());
				break;
			}
			case PhoneTypeBMPBean.MOBILE_PHONE_ID: {
				phoneNumbers.add(new StringBuilder(iwrb.getLocalizedString("mobile_phone_number", "Mobile")).append(CoreConstants.COLON)
						.append(CoreConstants.SPACE).append(phone.getNumber()).toString());
				break;
			}
			case PhoneTypeBMPBean.WORK_PHONE_ID: {
				phoneNumbers.add(new StringBuilder(iwrb.getLocalizedString("work_phone_number", "Work")).append(CoreConstants.COLON)
						.append(CoreConstants.SPACE).append(phone.getNumber()).toString());
				break;
			}
			case PhoneTypeBMPBean.FAX_NUMBER_ID: {
				phoneNumbers.add(new StringBuilder(iwrb.getLocalizedString("fax_number", "Fax")).append(CoreConstants.COLON)
						.append(CoreConstants.SPACE).append(phone.getNumber()).toString());
				break;
			}
			}
		}
		
		return phoneNumbers;
	}
	
	private Layer getPagingContainer(IWContext iwc, List<User> users, int totalUsers) {
		if (users.size() < getCount()) {
			return null;
		}
		
		IWBundle bundle = getBundle(iwc);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		String ofLocalized = iwrb.getLocalizedString("of", "of");
		int pageSize = getCount();
		int previousUsers = getLeftIndex() / pageSize;
		int previousPages = previousUsers + ((getLeftIndex() % pageSize > 0 && previousUsers <= pageSize) ? 1 : 0);
		int nextUsers = totalUsers - getLeftIndex();
		int nextPages = nextUsers / pageSize;
		int currentPage = previousPages + 1;
		int totalPages = totalUsers / pageSize + (totalUsers % pageSize > 0 ? 1 : 0);
		IntegerInput pageSizeInput = new IntegerInput("usersListPageSize", pageSize);
		pageSizeInput.setStyleClass("simpleUserApplicationGroupMembersPagerPageSizeInputStyle");
		
		Layer pager = new Layer();
		pager.setStyleClass("simpleUserApplicationGroupMembersPagerStyle");

		String action = getPagerAction(iwrb, pageSizeInput.getId());
		
		Layer previousContainer = new Layer();
		pager.add(previousContainer);
		previousContainer.setStyleClass("simpleUserApplicationGroupMembersPreviousPagePagerStyle");
		if (previousPages > 0) {
			Image previous = new Image(bundle.getVirtualPathWithFileNameString("images/previous.png"));
			previousContainer.add(previous);
			previous.setStyleClass("simpleUserApplicationGroupMembersPreviousPageImageStyle");
			previous.setTitle(iwrb.getLocalizedString("go_to_previous_page", "Go to previous page"));
			previous.setOnClick(getFullPagerAction(action, getLeftIndex(), true));
			Text previousPage = new Text(iwrb.getLocalizedString("previous_page", "Previous page"));
			previousContainer.add(previousPage);
		}
		
		Layer pageSizeContainer = new Layer();
		pager.add(pageSizeContainer);
		pageSizeContainer.setStyleClass("simpleUserApplicationGroupMembersPageSizeStyle");
		Label pageSizeInfo = new Label(new StringBuilder(iwrb.getLocalizedString("showing_users", "Showing users")).append(CoreConstants.SPACE)
			.append(getLeftIndex() + 1).append(CoreConstants.MINUS).append(getRightIndex()).append(CoreConstants.SPACE).append(ofLocalized)
			.append(CoreConstants.SPACE).append(totalUsers).append(CoreConstants.DOT).append(CoreConstants.SPACE).append(iwrb.getLocalizedString("page", "Page"))
			.append(CoreConstants.SPACE).append(currentPage)
			.append(CoreConstants.SPACE).append(ofLocalized).append(CoreConstants.SPACE).append(totalPages).append(CoreConstants.DOT).append(CoreConstants.SPACE)
			.append(iwrb.getLocalizedString("page_size", "Page size")).append(":").toString(), pageSizeInput);
		pageSizeContainer.add(pageSizeInfo);
		pageSizeContainer.add(pageSizeInput);
		
		Layer nextContainer = new Layer();
		pager.add(nextContainer);
		nextContainer.setStyleClass("simpleUserApplicationGroupMembersNextPagePagerStyle");
		if (nextPages > 0) {
			Text nextPage = new Text(iwrb.getLocalizedString("next_page", "Next page"));
			nextContainer.add(nextPage);
			Image next = new Image(bundle.getVirtualPathWithFileNameString("images/next.png"));
			nextContainer.add(next);
			next.setStyleClass("simpleUserApplicationGroupMembersNextPageImageStyle");
			next.setTitle(iwrb.getLocalizedString("go_to_next_page", "Go to next page"));
			next.setOnClick(getFullPagerAction(action, getRightIndex(), false));
		}
		
		pager.add(new CSSSpacer());
		return pager;
	}
	
	private String getFullPagerAction(String action, int index, boolean moveToLeft) {
		return new StringBuilder(action).append(", ").append(index).append(", ").append(moveToLeft).append(");").toString();
	}
	
	private String getPagerAction(IWResourceBundle iwrb, String pageSizeInputId) {
		String message = iwrb.getLocalizedString("loading", "Loading...");
		SimpleUserAppHelper helper = new SimpleUserAppHelper();
		
		StringBuilder action = new StringBuilder("navigateInUsersList(['").append(containerId).append("', '").append(message).append("', '")
			.append(pageSizeInputId).append("', '")
			.append(iwrb.getLocalizedString("enter_valid_page_size_greater_than_zero", "Please, enter valid page size value (greater than zero)!"))
			.append("', '").append(bean.getParentGroupChooserId()).append("', '").append(bean.getGroupId()).append("'], ")
			.append(helper.getBeanAsParameters(bean, null, null, message)).append(", ").append(bean.getOrderBy());
		
		return action.toString();
	}
	
	@Override
	public String getBundleIdentifier() {
		return UserConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	private void fixId(PresentationObject component) {
		if (!checkIds) {
			return;
		}
		
		String id = component.getId();
		
		boolean changeId = false;
		while (findComponent(id) != null) {
			id = new StringBuilder(idPrefix).append(idGenerator.nextInt(Integer.MAX_VALUE)).toString();
			changeId = true;
		}
		
		if (changeId) {
			component.setId(id);
		}
	}

	public void setBean(SimpleUserPropertiesBean bean) {
		this.bean = bean;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setCheckIds(boolean checkIds) {
		this.checkIds = checkIds;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	private List<User> getNeededPartOfUsers(List<User> users) {
		if (ListUtil.isEmpty(users)) {
			return null;
		}
		
		int from = getLeftIndex();
		int to = getRightIndex();
		if (to > users.size()) {
			to = users.size();
			rightIndex = to;
		}
		
		if (users.size() >= to) {
			List<User> usersToDisplay = new ArrayList<User>();
			for (int i = from; (i < users.size() && i < to); i++) {
				usersToDisplay.add(users.get(i));
			}
			return usersToDisplay;
		}
		return users;
	}
	
	public int getLeftIndex() {
		if (leftIndex == null) {
			leftIndex = bean == null ? 0 : bean.getFrom() < 0 ? 0 : bean.getFrom();
		}
		return leftIndex;
	}
	
	public int getRightIndex() {
		if (rightIndex == null) {
			int from = getLeftIndex();
			int to = from + getCount();
			if (to < 0) {
				to = 1;
			}
			
			if (to < from) {
				int temp = from;
				leftIndex = to;
				to = temp;
			}
			rightIndex = to;
		}
		return rightIndex;
	}

	public void setLeftIndex(Integer leftIndex) {
		this.leftIndex = leftIndex;
	}

	public void setRightIndex(Integer rightIndex) {
		this.rightIndex = rightIndex;
	}
	
	public Integer getCount() {
		if (count == null) {
			count = bean == null ? 20 : bean.getCount() < 0 ? 1 : bean.getCount();
		}
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
}
