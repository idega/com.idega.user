/*
 * Created on Nov 13, 2006
 */
package com.idega.user.presentation;

import java.util.Collection;
import com.idega.presentation.IWContext;
import com.idega.repository.data.ImplementorPlaceholder;
import com.idega.user.data.User;

/**
 * @author Sigtryggur
 */
public interface LinkToFamilyLogic extends ImplementorPlaceholder {

	Collection getCustodiansFor(User user, IWContext iwc);
}
