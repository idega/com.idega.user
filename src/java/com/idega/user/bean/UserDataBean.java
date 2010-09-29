package com.idega.user.bean;

public class UserDataBean {
	
	private String name = null;
	private String login = null;
	private String password = null;
	private String personalId = null;
	private String email = null;
	private String errorMessage = null;
	private String phone = null;
	private String pictureUri;

	//	Address
	private String streetNameAndNumber = null;
	private String postalCodeId = null;
	private String countryName = null;
	private String city = null;
	private String province = null;
	private String postalBox = null;
	private String addressId;
	
	private Integer userId = null;
	
	private boolean juridicalPerson = false;
	private Boolean changePasswordNextTime = Boolean.FALSE;
	private Boolean accountEnabled = Boolean.TRUE;
	private boolean accountExists = false;
	private boolean imageSet;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPersonalId() {
		return personalId;
	}
	public void setPersonalId(String personalId) {
		this.personalId = personalId;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getStreetNameAndNumber() {
		return streetNameAndNumber;
	}
	public void setStreetNameAndNumber(String streetNameAndNumber) {
		this.streetNameAndNumber = streetNameAndNumber;
	}
	public String getPostalCodeId() {
		return postalCodeId;
	}
	public void setPostalCodeId(String postalCodeId) {
		this.postalCodeId = postalCodeId;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getPostalBox() {
		return postalBox;
	}
	public void setPostalBox(String postalBox) {
		this.postalBox = postalBox;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public boolean isJuridicalPerson() {
		return juridicalPerson;
	}
	public void setJuridicalPerson(boolean juridicalPerson) {
		this.juridicalPerson = juridicalPerson;
	}
	public boolean isAccountExists() {
		return accountExists;
	}
	public void setAccountExists(boolean accountExists) {
		this.accountExists = accountExists;
	}
	public Boolean getChangePasswordNextTime() {
		return changePasswordNextTime;
	}
	public void setChangePasswordNextTime(Boolean changePasswordNextTime) {
		this.changePasswordNextTime = changePasswordNextTime;
	}
	public Boolean getAccountEnabled() {
		return accountEnabled;
	}
	public void setAccountEnabled(Boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}
	public String getPictureUri() {
		return pictureUri;
	}
	public void setPictureUri(String pictureUri) {
		this.pictureUri = pictureUri;
	}
	public boolean isImageSet() {
		return imageSet;
	}
	public void setImageSet(boolean imageSet) {
		this.imageSet = imageSet;
	}
	public String getAddressId() {
		return addressId;
	}
	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}
	
}