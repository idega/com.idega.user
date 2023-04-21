package com.idega.user.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.entity.business.EntityToPresentationObjectConverter;
import com.idega.block.entity.data.EntityPath;
import com.idega.block.entity.presentation.EntityBrowser;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginInfo;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.contact.data.PhoneTypeBMPBean;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.PostalCode;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.AbstractChooser;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.StyledAbstractChooserWindow;
import com.idega.user.bean.UserDataBean;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.user.presentation.CreateGroupWindow;
import com.idega.user.presentation.GroupTreeView;
import com.idega.user.presentation.UserChooserBrowserWindow;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWColor;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Service(UserHelper.USER_HELPER_BEAN)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class UserHelper extends DefaultSpringBean {

	public static final String USER_HELPER_BEAN = "userHelperBean";

	private UserBusiness userBusiness = null;

	public GroupTreeView getGroupTree(IWContext iwc) {
		GroupTreeView viewer = new GroupTreeView();
		try {
			if (iwc.isSuperAdmin()) {
				GroupTreeNode node = new GroupTreeNode(iwc.getDomain(),iwc.getApplicationContext());
				viewer.setRootNode(node);
			}
			else{
				UserBusiness biz = getUserBusiness(iwc);
				Collection<Group> allGroups = biz.getUsersTopGroupNodesByViewAndOwnerPermissions(iwc.getCurrentUser(), iwc);

				//	Filter groups
				List<String> allowedGroupTypes = null;
				if (iwc.isParameterSet(AbstractChooser.FILTER_PARAMETER))  {
					String filter = iwc.getParameter(AbstractChooser.FILTER_PARAMETER);
					if (filter.length() > 0)  {
						allowedGroupTypes = getGroupTypes(filter, iwc);
					}
				}

				Collection<Group> groups = new ArrayList<>();
				if (allowedGroupTypes == null)  {
					groups = allGroups;
				}
				else {
					for (Group group: allGroups) {
						if (checkGroupType(group, allowedGroupTypes))  {
							groups.add(group);
						}
					}
				}
				Collection<GroupTreeNode> groupNodes = convertGroupCollectionToGroupNodeCollection(groups, iwc.getApplicationContext());
				viewer.setFirstLevelNodes(groupNodes.iterator());
			}
		}
		catch(Exception e) {
			 getLogger().log(Level.WARNING, "Error getting GroupTreeView", e);
		}

		return viewer;
	}

	private UserBusiness getUserBusiness(IWApplicationContext iwc) {
		if (this.userBusiness == null) {
			try {
				this.userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
			}
			catch (RemoteException rme) {
				return null;
			}
		}
		return this.userBusiness;
	}

	private List<String> getGroupTypes(String selectedGroup, IWContext iwc)  {
		Group group = null;
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness = IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
			if (!(CreateGroupWindow.NO_GROUP_SELECTED.equals(selectedGroup)))  {
				group = groupBusiness.getGroupByGroupID(Integer.valueOf(selectedGroup));
			}
		}
		catch (Exception e)  {
			getLogger().log(Level.WARNING, "Error getting group by: " + selectedGroup, e);
			return null;
		}
		if (group == null) {
			return null;
		}

		Collection<GroupType> groupsTypes = null;
		try {
			groupsTypes = groupBusiness.getAllAllowedGroupTypesForChildren(group, iwc);
		}
		catch (Exception e) {

		}
		if (ListUtil.isEmpty(groupsTypes)) {
			return null;
		}

		List<String> groupTypes = new ArrayList<>();
		for (GroupType groupType: groupsTypes)  {
			groupTypes.add(groupType.getType());
		}

		return groupTypes;
	}

	private boolean checkGroupType(Group group, Collection<String> allowedGroupTypes) {
		if (group == null || ListUtil.isEmpty(allowedGroupTypes)) {
			return false;
		}

		String groupType = group.getGroupTypeValue();
		for (String type: allowedGroupTypes)  {
			if (type.equals(groupType)) {
				return true;
			}
		}
		return false;
	}

	private Collection<GroupTreeNode> convertGroupCollectionToGroupNodeCollection(Collection<Group> groups, IWApplicationContext iwac){
		List<GroupTreeNode> list = new ArrayList<>();
		for (Group group: groups) {
			GroupTreeNode node = new GroupTreeNode(group, iwac);
			list.add(node);
		}
		return list;
	}

	public EntityBrowser getUserBrowser(Collection<User> entities, String searchKey, IWContext iwc, int rows)  {
	    // define checkbox button converter class
	    EntityToPresentationObjectConverter converterToChooseButton = new EntityToPresentationObjectConverter() {

	      @Override
		public PresentationObject getHeaderPresentationObject(EntityPath entityPath, EntityBrowser browser, IWContext iwc) {
	        return browser.getDefaultConverter().getHeaderPresentationObject(entityPath, browser, iwc);
	      }

	      @Override
		public PresentationObject getPresentationObject(Object entity, EntityPath path, EntityBrowser browser, IWContext iwc) {
	        User user = (User) entity;
	        RadioButton radioButton = new RadioButton();
	        // define displaystring and value of the textinput of the parent window
	        radioButton.setOnClick(StyledAbstractChooserWindow.SELECT_FUNCTION_NAME+"('"
	          + user.getName() +
	          "','"
	          + ((Integer) user.getPrimaryKey()).toString() +
	          "')");
	        return radioButton;
	      }
	    };
	    // set default columns
	    String nameKey = User.class.getName()+".FIRST_NAME:" + User.class.getName()+".MIDDLE_NAME:"+User.class.getName()+".LAST_NAME";
	    String pinKey = User.class.getName()+".PERSONAL_ID";
	    EntityBrowser browser = EntityBrowser.getInstanceUsingExternalForm();
//	    browser.setLeadingEntity(User.class);
	    browser.setEntities("chooser_window_" + searchKey, entities);
	    browser.setAcceptUserSettingsShowUserSettingsButton(false, false);
	    browser.setDefaultNumberOfRows(rows);

	    browser.setWidth(Table.HUNDRED_PERCENT);

	    //fonts
	    Text column = new Text();
	    column.setBold();
	    browser.setColumnTextProxy(column);

	    //    set color of rows
	    browser.setColorForEvenRows(IWColor.getHexColorString(246, 246, 247));
	    browser.setColorForOddRows("#FFFFFF");

	    browser.setDefaultColumn(1, nameKey);
	    browser.setDefaultColumn(2, pinKey);
	    browser.setMandatoryColumn(1, "Choose");
	    // set special converters
	    browser.setEntityToPresentationConverter("Choose", converterToChooseButton);
	    // set mandatory parameters
	    browser.addMandatoryParameters(StyledAbstractChooserWindow.getHiddenParameters(iwc));
	    browser.addMandatoryParameter(UserChooserBrowserWindow.SEARCH_KEY, searchKey);
	    return browser;
	}

	public Collection<User> getUserEntities(String searchKey)  {
	    if (searchKey == null) {
			return new ArrayList<>();
		}
	    try {
	    	UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
	    	String modifiedSearch = getModifiedSearchString(searchKey);
	    	return userHome.findUsersBySearchCondition(modifiedSearch, false);
	    }
	    // Remote and FinderException
	    catch (Exception ex)  {
	    	throw new RuntimeException(ex.getMessage());
	    }
	}

	private String getModifiedSearchString(String originalSearchString)  {
	    StringBuffer buffer = new StringBuffer("%");
	    buffer.append(originalSearchString).append("%");
	    return buffer.toString();
	}

	public UserDataBean getUserInfo(User user) {
		IWContext iwc = CoreUtil.getIWContext();
		IWApplicationContext iwac = iwc == null ? getIWApplicationContext() : iwc;

		UserBusiness userBusiness = getUserBusiness(iwac);
		if (userBusiness == null) {
			return null;
		}

		IWBundle bundle = getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		UserDataBean bean = new UserDataBean();
		if (user == null) {
			IWResourceBundle iwrb = bundle.getResourceBundle(iwc == null ? getCurrentLocale() : iwc.getCurrentLocale());
			String errorMessage = "Unable to find user by provided personal ID!";
			errorMessage = iwrb.getLocalizedString("unable_to_find_user_by_personal_id", errorMessage);
			bean.setErrorMessage(errorMessage);
		} else {
			//	ID
			try {
				bean.setUserId(Integer.valueOf(user.getId()));
			} catch(Exception e) {
				e.printStackTrace();
			}

			//	Name
			bean.setName(user.getName());

			//	Personal ID
			String personalID = user.getPersonalID();
			bean.setPersonalId(personalID == null ? CoreConstants.EMPTY : personalID);

			//	Picture
			String pictureUri = null;
			Image image = userBusiness.getUserImage(user);
			if (image == null) {
				//	Default image
				boolean male = true;
				try {
					male = userBusiness.isMale(user.getGenderID());
				} catch (Exception e) {}
				pictureUri = new StringBuilder(bundle.getVirtualPathWithFileNameString("images/")).append(male ? "user_male" : "user_female").append(".png")
					.toString();
			} else {
				pictureUri = image.getMediaURL(iwc);
				bean.setImageSet(true);
			}
			bean.setPictureUri(pictureUri);

			//	Login
//			String login = userBusiness.getUserLogin(user);
//			bean.setLogin(login == null ? CoreConstants.EMPTY : login);

			//	Password
//			String password = userBusiness.getUserPassword(user);
//			bean.setPassword(password == null ? CoreConstants.EMPTY : password);

			//	Disabled account?
			LoginInfo loginInfo = null;
			try {
				loginInfo = LoginDBHandler.getLoginInfo(LoginDBHandler.getUserLogin(user));
			} catch(Exception e) {}
			if (loginInfo == null) {
				bean.setAccountEnabled(Boolean.TRUE);
			} else {
				bean.setAccountExists(true);
				bean.setAccountEnabled(loginInfo.getAccountEnabled());
				bean.setChangePasswordNextTime(loginInfo.getChangeNextTime());
			}

			//	Phone
			Phone phone = null;
			Phone mobilePhone = null;
			Phone workphoPhone = null;
			try {
				int userId = Integer.valueOf(user.getId());
				phone = userBusiness.getUserPhone(userId, PhoneTypeBMPBean.HOME_PHONE_ID);
				mobilePhone = userBusiness.getUserPhone(userId, PhoneTypeBMPBean.MOBILE_PHONE_ID);
				workphoPhone = userBusiness.getUserPhone(userId, PhoneTypeBMPBean.WORK_PHONE_ID);
			} catch (Exception e) {}

			//	Email
			Email email = null;
			try {
				email = userBusiness.getUserMail(user);
			} catch (Exception e) {}

			//	Address
			Address address = null;
			try {
				address = userBusiness.getUsersMainAddress(user);
			} catch (RemoteException e) {}
			fillUserInfo(bean, phone, mobilePhone, workphoPhone, email, address);
		}

		return bean;
	}

	public void setAddress(UserDataBean info, Address address) {
		if (address == null) {
			return;
		}

		info = info == null ? new UserDataBean() : info;

		info.setAddressId(address.getPrimaryKey().toString());

		String streetNameAndNumber = address.getStreetAddress();
		if (StringUtil.isEmpty(streetNameAndNumber)) {
			streetNameAndNumber = address.getStreetNameOriginal();
			String number = address.getStreetNumber();
			streetNameAndNumber = StringUtil.isEmpty(streetNameAndNumber) ?
					streetNameAndNumber :
					StringUtil.isEmpty(number) ? streetNameAndNumber : streetNameAndNumber.concat(CoreConstants.SPACE).concat(number);
		}
		info.setStreetNameAndNumber(streetNameAndNumber == null ? CoreConstants.EMPTY : streetNameAndNumber);

		String postalCodeValue = null;
		PostalCode postalCode = address.getPostalCode();
		if (postalCode != null) {
			postalCodeValue = postalCode.getPostalCode();
		}
		info.setPostalCodeId(postalCodeValue == null ? CoreConstants.EMPTY : postalCodeValue);

		String countryName = CoreConstants.EMPTY;
		Country country = address.getCountry();
		if (country != null) {
			countryName = country.getName();
		}
		info.setCountryName(countryName == null ? CoreConstants.EMPTY : countryName);

		String city = address.getCity();
		if (StringUtil.isEmpty(city) && postalCode != null) {
			Commune commune = postalCode.getCommune();
			commune = commune == null ? address.getCommune() : commune;
			if (commune != null) {
				city = commune.getCommuneName();
			}

			if (StringUtil.isEmpty(city)) {
				city = postalCode.getName();
			}
		}
		info.setCity(city == null ? CoreConstants.EMPTY : city);

		String province = address.getProvince();
		info.setProvince(province == null ? CoreConstants.EMPTY : province);

		String postalBox = address.getPOBox();
		if (StringUtil.isEmpty(postalBox) && postalCode != null) {
			postalBox = postalCode.getPostalCode();
		}
		info.setPostalBox(postalBox == null ? CoreConstants.EMPTY : postalBox);

		Commune commune = address.getCommune();
		if (commune != null) {
			String communeName = commune.getCommuneName();
			info.setCommune(communeName == null ? CoreConstants.EMPTY : communeName);
		}
	}

	public void fillUserInfo(UserDataBean info, Phone phone, Phone mobilePhone, Phone workPhone, Email email, Address address) {
		if (phone != null) {
			info.setPhone(phone.getNumber());
		}
		if (mobilePhone != null) {
			info.setMobilePhone(mobilePhone.getNumber());
		}

		if (workPhone != null) {
			info.setWorkPhone(workPhone.getNumber());
		}

		if (email != null) {
			info.setEmail(email.getEmailAddress());
		}

		setAddress(info, address);
	}

}