/*
 * Created on Feb 2, 2005
 */
package com.idega.user.presentation;

import com.idega.presentation.text.Link;
import com.idega.repository.data.ImplementorPlaceholder;
import com.idega.user.data.Group;

/**
 * @author Sigtryggur
 */
public interface LinkToUserStats extends ImplementorPlaceholder {
    
    Link getLink();
    
    void setSelectedGroup(Group selectedGroup);
    
}
