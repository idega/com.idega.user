package com.idega.user.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.event.IWActionListener;
import com.idega.event.IWPresentationEvent;
import com.idega.event.IWPresentationStateImpl;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupBusiness;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupType;
import com.idega.user.data.User;
import com.idega.user.event.CreateGroupEvent;
import com.idega.user.util.ICUserConstants;

/**
 * Title: CreateGroupWindowPS<br>
 * Description: This class handles creating new groups.<br>
 * Copyright: Idega Software Copyright (c) 2002<br>
 * Company: Idega Software<br>
 * 
 * @author <a href="eiki@idega.is">Eirikur Hrafnsson </a>
 * @version 1.0
 */

public class CreateGroupWindowPS extends IWPresentationStateImpl implements IWActionListener {

    private boolean _close = false;
    private Integer groupId = null;
    private IWContext eventContext = null;
    private String _groupName = null;
    private String _groupDescription = null;
    private String _groupType = null;
    private IWResourceBundle iwrb;
    private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";
    private GroupBusiness groupBusiness;

    public CreateGroupWindowPS() {
    }

    public void reset() {
        _groupName = null;
        _groupDescription = null;
        _groupType = null;
        _close = false;
        groupId = null;
        eventContext = null;
    }

    public String getGroupName() {
        return _groupName;
    }

    public String getGroupDescription() {
        return _groupDescription;
    }

    public String getGroupType() {
        return _groupType;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public IWContext getEventContext() {
        return eventContext;
    }

    public boolean doClose() {
        return _close;
    }

    public void doneClosing() {
        _close = false;
    }

    public void actionPerformed(IWPresentationEvent e) throws IWException {
       
        iwrb = e.getIWContext().getIWMainApplication().getBundle(
                IW_BUNDLE_IDENTIFIER).getResourceBundle(e.getIWContext());

        if (e instanceof CreateGroupEvent) {
            CreateGroupEvent event = (CreateGroupEvent) e;

            //create group, subgroups and give permissions
            if (event.doCommit()) {
                try {
                    IWContext iwc = e.getIWContext();
                    groupBusiness = getGroupBusiness(iwc);
                    Group group = null;
                    Group parentGroup = null;

                    //deside how to create the group
                    if (event.getParentType() == CreateGroupEvent.TYPE_DOMAIN) {
                        // create the group under the default Domain (it's a
                        // top node (as super user))
                        group = groupBusiness.createGroup(event.getName(), event
                                .getDescription(), event.getGroupType(), event
                                .getHomePageID(), event.getAliasID());
                    } else if (event.getParentType() == CreateGroupEvent.TYPE_GROUP) {
                        // create under the supplied parent group
                        int parentGroupId = event.getParentID();

                        parentGroup = groupBusiness.getGroupByGroupID(parentGroupId);
                        
                        if(iwc.getAccessController().hasEditPermissionFor(parentGroup,iwc)){
                        		group = groupBusiness.createGroupUnder(event.getName(),
                                event.getDescription(), event.getGroupType(),
                                event.getHomePageID(), event.getAliasID(),
                                parentGroup);
                        
                        
                        		copyGroupNumberFromParent(group, parentGroup);
                        }
                        else{
                        	//TODO throw an exception! you should not be able to create this group
                        	
                        }

                        /////////////

                    } else {
                       //UNKNOWN PARENT TYPE
                        System.err.println("[CreateGroupWindow]: parentGroupType "+ event.getParentType() + "not found. Use a proper parent type (0=domain, 1=group)");
                        
                    }
                    
                    
                    if(group!=null){
	                    // store group id and context, so change listners are able
	                    // to open windows (e.g. the group property window)
	                    groupId = (Integer) group.getPrimaryKey();
	                    eventContext = e.getIWContext();
	                    User currentUser = iwc.getCurrentUser();
	                    
	                    //Apply permission stuff
	                    groupBusiness.applyOwnerAndAllGroupPermissionsToNewlyCreatedGroupForUserAndHisPrimaryGroup(iwc, group, currentUser);
	
	                    // get groupType tree and iterate through it and create default sub groups.
	                    createDefaultSubGroupsFromGroupTypeTreeAndApplyPermissions(group, groupBusiness, e.getIWContext(), currentUser);
	
	                    //TODO fix this what is it doing? some caching stuff?
	                    e.getIWContext().getApplicationContext().removeApplicationAttribute("domain_group_tree");
	                    e.getIWContext().getApplicationContext().removeApplicationAttribute("group_tree");
                    }
                    
                } catch (CreateException ce) {
                    throw new EJBException(ce);
                } catch (RemoteException ex) {
                    throw new EJBException(ex);
                } catch (FinderException fe) {
                    throw new EJBException(fe);
                }

                this.fireStateChanged();
                // forget everything
                reset();

            } else if (event.doCancel()) {
                this.reset();
                _close = true;
                this.fireStateChanged();
            } else {
                _groupName = event.getName();
                _groupDescription = event.getDescription();
                _groupType = event.getGroupType();
            }

        }
    }

    /**
     * @param group
     * @param parentGroup
     */
    private void copyGroupNumberFromParent(Group group, Group parentGroup) {
        //todo refactor this to a plugin
        //used to see the path to groups in the permission window
        String groupNumber = parentGroup.getMetaData(ICUserConstants.META_DATA_GROUP_NUMBER);
        if(groupNumber!=null && !"".equals(groupNumber)) {
            if(!groupNumber.endsWith("-")) {
                groupNumber+="-";//add a - to thee number
            }
            
            group.setMetaData(ICUserConstants.META_DATA_GROUP_NUMBER,groupNumber);
            
            group.store();
            
        }
    }

    private GroupBusiness getGroupBusiness(IWContext iwc) {
        GroupBusiness business = null;
        try {
            business = (GroupBusiness) IBOLookup.getServiceInstance(iwc,
                    GroupBusiness.class);
        } catch (IBOLookupException e) {
            e.printStackTrace();
        }
        return business;
    }

    private UserBusiness getUserBusiness(IWContext iwc) {
        UserBusiness business = null;
        try {
            business = (UserBusiness) IBOLookup.getServiceInstance(iwc,
                    UserBusiness.class);
        } catch (IBOLookupException e) {
            e.printStackTrace();
        }
        return business;
    }

    

    /**
     * Creates the child groups specified in the groups grouptypetree definition.
     * 
     * @param group
     * @param business
     * @param iWContext
     */
    private void createDefaultSubGroupsFromGroupTypeTreeAndApplyPermissions(Group group, GroupBusiness business,
            IWContext iwc, User user) throws RemoteException {
        GroupType type;
        try {
            type = business.getGroupTypeHome().findByPrimaryKey(
                    group.getGroupType());
        } catch (FinderException e) {
            e.printStackTrace();
            return;
        }

        Iterator iterator = type.getChildrenIterator();

        while (iterator != null && iterator.hasNext()) {
            GroupType gType = (GroupType) iterator.next();
            String name = gType.getDefaultGroupName();

            if (gType.getAutoCreate()) {
                Integer numberOfInstances = gType
                        .getNumberOfInstancesToAutoCreate();
                int nrOfGroupsToCreate = 1;

                if ((numberOfInstances != null)
                        && (numberOfInstances.intValue() > 1)) {
                    nrOfGroupsToCreate = numberOfInstances.intValue();
                }

                for (int i = 1; i <= nrOfGroupsToCreate; i++) {
                    String typeString = gType.getType();
                    String typeLocalizingKey = "auto.create.name." + typeString;
                    String defaultValue = gType.getDescription();
                    if ((defaultValue == null) || ("".equals(defaultValue)))
                            defaultValue = typeString;

                    //to avoid circular reference with beginning type
                    //if( this.getGroupType().equals(typeString) ) continue;
                    // rather add all types to a map to check

                    if (name == null) {
                        if (nrOfGroupsToCreate > 1) {
                            typeLocalizingKey = typeLocalizingKey + " " + i;
                            defaultValue = defaultValue + " " + i;
                        }
                        name = iwrb.getLocalizedString(typeLocalizingKey,
                                defaultValue);
                    } else {
                        if (nrOfGroupsToCreate > 1) {
                            name = name + " " + i;
                        }

                    }

                    //create group then call recursive
                    try {
                        Group newGroup = business.createGroupUnder(name, "",typeString, group);
                        
                        copyGroupNumberFromParent(newGroup,group);

                        groupBusiness.applyOwnerAndAllGroupPermissionsToNewlyCreatedGroupForUserAndHisPrimaryGroup(iwc,newGroup, user);

                        if (!type.isLeaf()) {
                            createDefaultSubGroupsFromGroupTypeTreeAndApplyPermissions(newGroup, business, iwc, user);
                        }

                    } catch (CreateException e) {
                        e.printStackTrace();
                        return;
                    }

                }

            }
        }
    }

    

}