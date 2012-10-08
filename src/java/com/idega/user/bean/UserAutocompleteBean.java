package com.idega.user.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.business.BuilderLogic;
import com.idega.core.business.DefaultSpringBean;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.business.UserApplicationEngine;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreUtil;
import com.idega.util.expression.ELUtil;


@Service(UserAutocompleteBean.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
	@Param(name="beanName", value=UserAutocompleteBean.BEAN_NAME),
	@Param(name="javascript", value="UserAutocompleteBean")
}, name="UserAutocompleteBean")
public class UserAutocompleteBean extends DefaultSpringBean implements DWRAnnotationPersistance {

	public static final String BEAN_NAME = "userAutocompleteBean";
	
	@RemoteMethod
	public List<LabeledItem> getAutocompletedItems(String term, int max,String name){
		IWContext iwc = CoreUtil.getIWContext();
		ArrayList<LabeledItem> items = new ArrayList<LabeledItem>();
		Collection <User> requestedUsers;
		try {
			UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
			requestedUsers = userHome.ejbAutocompleteRequest(term, -1, max, 0);
		} catch (IDOLookupException e) {
			getLogger().log(Level.WARNING, "failed getting user home", e);
			requestedUsers = Collections.emptyList();
		}
		UserApplicationEngine userApplicationEngine = ELUtil.getInstance().getBean(UserApplicationEngine.class);
		for(User user : requestedUsers){
			UserDataBean userDataBean = userApplicationEngine.getUserInfo(user);
			LabeledItem item = new LabeledItem();
			items.add(item);
			Layer main = new Layer();
			Image uimg = new Image(userDataBean.getPictureUri());
			uimg.setStyleAttribute("width:4em;height:4em;");
			main.add(uimg);
			main.add(userDataBean.getName());
			HiddenInput idInput = new HiddenInput(name, user.getId());
			main.add(idInput);
			String label = BuilderLogic.getInstance().getRenderedComponent(main, iwc, false);
			item.setLabel(label);
//			System.out.println(label);
			item.setValue(term);
		}
//		LabeledItem item = new LabeledItem();
//		items.add(item);
//		item.setLabel("<button name='"+name+"' value='"+2+"' >Administrator</button>");
//		item = new LabeledItem();
//		items.add(item);
//		item.setLabel("<button name='"+name+"' value='"+8+"' >soc</button>");
//		item = new LabeledItem();
//		items.add(item);
//		item.setLabel("<button name='"+name+"' value='"+9+"' >s2</button>");
		return items;
	}
	
}
