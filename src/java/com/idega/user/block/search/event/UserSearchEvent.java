package com.idega.user.block.search.event;

import com.idega.presentation.IWContext;
import com.idega.event.*;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class UserSearchEvent extends IWPresentationEvent
{


	public static final String SEARCH_FIELD_SEARCH_TYPE = "usr_search_type";
	
	public static final String SEARCH_FIELD_SIMPLE_SEARCH_STRING = "usr_search";
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

  public UserSearchEvent(){
  }

	public String getSearchString(){
	    return searchString;
	}

	public int getSearchType(){
	    return searchType;
	}


    public boolean initializeEvent(IWContext iwc){
	
			String type = iwc.getParameter(SEARCH_FIELD_SEARCH_TYPE);
			if(type == null){
			    type = iwc.getParameter(SEARCH_FIELD_SEARCH_TYPE+".x");
			}
	
			try
			{
				searchType = Integer.parseInt(type);
				
				if(searchType == this.SEARCHTYPE_SIMPLE){// simple search
					searchString = iwc.getParameter(SEARCH_FIELD_SIMPLE_SEARCH_STRING);
					return iwc.isParameterSet(SEARCH_FIELD_SIMPLE_SEARCH_STRING);
					
				}
				else if( searchType == this.SEARCHTYPE_ADVANCED ){// advanced search
					searchString = iwc.getParameter(SEARCH_FIELD_SIMPLE_SEARCH_STRING);
					groups = iwc.getParameterValues(this.SEARCH_FIELD_GROUPS);
					
					if( iwc.isParameterSet(SEARCH_FIELD_STATUS_ID)){
						statusId = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_STATUS_ID));
					}
					
					if( iwc.isParameterSet(SEARCH_FIELD_GENDER_ID)){
						genderId = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_GENDER_ID));
					}

					if( iwc.isParameterSet(SEARCH_FIELD_AGE_FLOOR)){
						ageFloor = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_AGE_FLOOR));
					}
					
					if( iwc.isParameterSet(SEARCH_FIELD_AGE_CEILING)){
						ageCeil = Integer.parseInt(iwc.getParameter(SEARCH_FIELD_AGE_CEILING));
					}
					
					if( iwc.isParameterSet(SEARCH_FIELD_PERSONAL_ID)){
						personalId = iwc.getParameter(SEARCH_FIELD_PERSONAL_ID);
					}
					
					if( iwc.isParameterSet(SEARCH_FIELD_ADDRESS)){
						address = iwc.getParameter(SEARCH_FIELD_ADDRESS);
					}		
					
					
				}
				else return false;//NO TYPE
				
				
				
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
		return ageCeil;
	}

	/**
	 * @return
	 */
	public int getAgeFloor() {
		return ageFloor;
	}

	/**
	 * @return
	 */
	public int getGenderId() {
		return genderId;
	}

	/**
	 * @return
	 */
	public String[] getGroups() {
		return groups;
	}

	/**
	 * @return
	 */
	public int getStatusId() {
		return statusId;
	}

	/**
	 * @param i
	 */
	public void setAgeCeil(int i) {
		ageCeil = i;
	}

	/**
	 * @param i
	 */
	public void setAgeFloor(int i) {
		ageFloor = i;
	}

	/**
	 * @param i
	 */
	public void setGenderId(int i) {
		genderId = i;
	}

	/**
	 * @param strings
	 */
	public void setGroups(String[] strings) {
		groups = strings;
	}

	/**
	 * @param string
	 */
	public void setSearchString(String string) {
		searchString = string;
	}

	/**
	 * @param i
	 */
	public void setSearchType(int i) {
		searchType = i;
	}

	/**
	 * @param i
	 */
	public void setStatusId(int i) {
		statusId = i;
	}

	/**
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return
	 */
	public String getPersonalId() {
		return personalId;
	}

	/**
	 * @param string
	 */
	public void setAddress(String string) {
		address = string;
	}

	/**
	 * @param string
	 */
	public void setPersonalId(String string) {
		personalId = string;
	}

}