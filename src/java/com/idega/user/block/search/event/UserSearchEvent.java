package com.idega.user.block.search.event;

import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: UserSearchEvent</p>
 * <p>Description: A search event for the user system</p>
 * <p>Copyright: Idega Software Copyright (c) 2003</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Eirikur Hrafnsson</a>
 * @version 1.1
 */

public class UserSearchEvent extends IWPresentationEvent
{


	public static final String SEARCH_FIELD_SEARCH_TYPE = "usr_search_type";
	
	public static final String SEARCH_FIELD_SIMPLE_SEARCH_STRING = "usr_search";
	public static final String SEARCH_FIELD_FIRST_NAME = "usr_search_f_name";
	public static final String SEARCH_FIELD_MIDDLE_NAME = "usr_search_m_name";
	public static final String SEARCH_FIELD_LAST_NAME = "usr_search_l_name";
	public static final String SEARCH_FIELD_GROUPS = "usr_search_groups";
	public static final String SEARCH_FIELD_STATUS_ID = "usr_search_status_id";
	public static final String SEARCH_FIELD_GENDER_ID = "usr_search_gender_id";
	public static final String SEARCH_FIELD_AGE_FLOOR= "usr_search_age_floor";
	public static final String SEARCH_FIELD_AGE_CEILING = "usr_search_age_ceil";
	public static final String SEARCH_FIELD_PERSONAL_ID = "usr_search_personal_id";
	public static final String SEARCH_FIELD_ADDRESS = "usr_search_address";
	
	public static final int SEARCHTYPE_SIMPLE = 0;
	public static final int SEARCHTYPE_ADVANCED = 1;
	
	

	private String searchString = null;
	private int searchType = 0;
	private String[] groups = null;
	private int statusId = 0;
	private int genderId = 0;
	private int ageFloor = 0;
	private int ageCeil = 0;
	private String personalId = null;
	private String address = null;
	private String firstName = null;
	private String middleName = null;
	private String lastName = null;

  public UserSearchEvent(){
  }

	public String getSearchString(){
	    return this.searchString;
	}

	public int getSearchType(){
	    return this.searchType;
	}


    public boolean initializeEvent(IWContext iwc){
	
			String type = iwc.getParameter(SEARCH_FIELD_SEARCH_TYPE);
			if(type == null){
			    type = iwc.getParameter(SEARCH_FIELD_SEARCH_TYPE+".x");
			}
	
			try
			{
				this.searchType = Integer.parseInt(type);
				
				if(this.searchType == UserSearchEvent.SEARCHTYPE_SIMPLE){// simple search
					this.searchString = iwc.getParameter(SEARCH_FIELD_SIMPLE_SEARCH_STRING);
					return iwc.isParameterSet(SEARCH_FIELD_SIMPLE_SEARCH_STRING);
					
				}
				else if( this.searchType == UserSearchEvent.SEARCHTYPE_ADVANCED ){// advanced search
					
					this.firstName = iwc.getParameter(SEARCH_FIELD_FIRST_NAME);
					this.middleName = iwc.getParameter(SEARCH_FIELD_MIDDLE_NAME);
					this.lastName = iwc.getParameter(SEARCH_FIELD_LAST_NAME);
					this.groups = iwc.getParameterValues(UserSearchEvent.SEARCH_FIELD_GROUPS);
					this.genderId = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_GENDER_ID));
					this.personalId = iwc.getParameter(SEARCH_FIELD_PERSONAL_ID);
					this.address = iwc.getParameter(SEARCH_FIELD_ADDRESS);
					
					if( iwc.isParameterSet(SEARCH_FIELD_AGE_FLOOR)){
						this.ageFloor = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_AGE_FLOOR));
					}
					if( iwc.isParameterSet(SEARCH_FIELD_AGE_CEILING)){
						this.ageCeil = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_AGE_CEILING));
					}	
					if( iwc.isParameterSet(SEARCH_FIELD_STATUS_ID)){
						this.statusId = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_STATUS_ID));
					}
					
				}
				else {
					return false;//NO TYPE
				}
				
				
				
			}
			catch (NumberFormatException ex)
			{
				System.err.println("["+this.getClass()+"] :No searchType or error in advances search");
				return false;
			}


			return true;
    }
	/**
	 * @return
	 */
	public int getAgeCeil() {
		return this.ageCeil;
	}

	/**
	 * @return
	 */
	public int getAgeFloor() {
		return this.ageFloor;
	}

	/**
	 * @return
	 */
	public int getGenderId() {
		return this.genderId;
	}

	/**
	 * @return
	 */
	public String[] getGroups() {
		return this.groups;
	}

	/**
	 * @return
	 */
	public int getStatusId() {
		return this.statusId;
	}

	/**
	 * @param i
	 */
	public void setAgeCeil(int i) {
		this.ageCeil = i;
	}

	/**
	 * @param i
	 */
	public void setAgeFloor(int i) {
		this.ageFloor = i;
	}

	/**
	 * @param i
	 */
	public void setGenderId(int i) {
		this.genderId = i;
	}

	/**
	 * @param strings
	 */
	public void setGroups(String[] strings) {
		this.groups = strings;
	}

	/**
	 * @param string
	 */
	public void setSearchString(String string) {
		this.searchString = string;
	}

	/**
	 * @param i
	 */
	public void setSearchType(int i) {
		this.searchType = i;
	}

	/**
	 * @param i
	 */
	public void setStatusId(int i) {
		this.statusId = i;
	}

	/**
	 * @return
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * @return
	 */
	public String getPersonalId() {
		return this.personalId;
	}

	/**
	 * @param string
	 */
	public void setAddress(String string) {
		this.address = string;
	}

	/**
	 * @param string
	 */
	public void setPersonalId(String string) {
		this.personalId = string;
	}

	/**
	 * @return
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * @return
	 */
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * @return
	 */
	public String getMiddleName() {
		return this.middleName;
	}

	/**
	 * @param string
	 */
	public void setFirstName(String string) {
		this.firstName = string;
	}

	/**
	 * @param string
	 */
	public void setLastName(String string) {
		this.lastName = string;
	}

	/**
	 * @param string
	 */
	public void setMiddleName(String string) {
		this.middleName = string;
	}

}