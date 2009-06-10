package com.idega.user.business;

import java.util.List;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.business.BuilderLogic;
import com.idega.core.component.bean.RenderedComponent;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.user.presentation.user.UsersFilterList;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(UsersFilterHelper.SPRING_BEAN_NAME)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
		@Param(name="beanName", value=UsersFilterHelper.SPRING_BEAN_NAME),
		@Param(name="javascript", value=UsersFilterHelper.DWR_OBJECT)
	}, name=UsersFilterHelper.DWR_OBJECT)
public class UsersFilterHelper implements DWRAnnotationPersistance {

	static final String SPRING_BEAN_NAME = "usersFilterHelperBean";
	public static final String DWR_OBJECT = "UsersFilter";
	
	@RemoteMethod
	public RenderedComponent getUsersInGroup(String groupId, List<String> selectedUsers, String selectedInputName) {
		UsersFilterList component = new UsersFilterList();
		component.setSelectedUsers(selectedUsers);
		component.setGroupId(groupId);
		component.setSelectedUserInputName(selectedInputName);
		return BuilderLogic.getInstance().getRenderedComponent(component, null);
	}
}
