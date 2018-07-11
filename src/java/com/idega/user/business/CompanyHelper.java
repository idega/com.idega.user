package com.idega.user.business;

import com.idega.core.location.data.Address;
import com.idega.repository.data.Singleton;
import com.idega.user.bean.UserDataBean;

public interface CompanyHelper extends Singleton {

	public static final String SPRING_BEAN_IDENTIFIER = "companyHelperInUserBundle";

	public UserDataBean getCompanyInfo(String companyPersonalId);

	public Address getCompanyAddress(String companyPersonalId);

}