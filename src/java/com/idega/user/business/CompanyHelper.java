package com.idega.user.business;

import com.idega.repository.data.Singleton;
import com.idega.user.bean.UserDataBean;

public interface CompanyHelper extends Singleton {
	
	public static final String SPRING_BEAN_IDENTIFIER = "companyHelperInUserBundle";
	
	public UserDataBean getCompanyInfo(String companyPersonalId);

}
