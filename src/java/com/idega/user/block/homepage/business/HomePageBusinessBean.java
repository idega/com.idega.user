package com.idega.user.block.homepage.business;


import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.idega.builder.dynamicpagetrigger.business.DPTTriggerBusinessBean;
import com.idega.builder.dynamicpagetrigger.data.PageLink;
import com.idega.builder.dynamicpagetrigger.data.PageTriggerInfo;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.accesscontrol.data.ICPermission;
import com.idega.core.builder.business.BuilderConstants;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.data.GenericGroup;
import com.idega.core.data.ICTreeNode;
import com.idega.data.IDOLookupException;
import com.idega.event.EventLogic;
import com.idega.presentation.IWContext;
import com.idega.user.data.Group;

/**
 * Title: HomePageBusinessBean 
 * Description: 
 * Copyright: 
 * Copyright (c) 2004
 * Company: idega Software
 * @author 2004 - idega team -<br><a href="mailto:gummi@idega.is">Gudmundur Agust Saemundsson </a> <br>
 * @version 1.0
 */

public class HomePageBusinessBean extends DPTTriggerBusinessBean implements HomePageBusiness {

	private final static Map pagePropertieIds = new HashMap();
	public static int tmpHardcodedPageTriggerInfoId = 1;

	public HomePageBusinessBean() {
	}

	public List getGroupDPTPageLinks(int[] catTypes) throws SQLException {
//		//TODO re-implement - replace project with Group
//		PageLink staticPLink = (PageLink) PageLinkBMPBean.getStaticInstance(PageLink.class);
//
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("select e.* from ");
//		buffer.append(tableToSelectFrom);
//		buffer.append(" middle, ");
//		buffer.append(staticPLink.getEntityName());
//		buffer.append(" e, ");
//		buffer.append(staticIPProject.getEntityName());
//		buffer.append(" p");
//		buffer.append(" where ");
//		buffer.append("middle.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" = p.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" and middle.");
//		buffer.append(staticPLink.getIDColumnName());
//		buffer.append(" = e.");
//		buffer.append(staticPLink.getIDColumnName());
//		buffer.append(" and middle.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" in (");
//		// get projectIDs
//		buffer.append(" select p.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" from ");
//		buffer.append(staticIPProject.getEntityName());
//		buffer.append(" p ");
//		buffer.append("where p.");
//		buffer.append(IPProjectBMPBean._COLUMN_DELETED);
//		buffer.append(" != 'Y' or p.");
//		buffer.append(IPProjectBMPBean._COLUMN_DELETED);
//		buffer.append(" is null");
//		if (catTypes != null) {
//			for (int i = 0; i < catTypes.length; i++) {
//				buffer.append(" and p.");
//				buffer.append(staticIPProject.getIDColumnName());
//				buffer.append(" in ( ");
//				buffer.append("select ");
//				buffer.append(staticIPProject.getIDColumnName());
//				buffer.append(" from ");
//				buffer.append(projectCategoryMiddleTable);
//				buffer.append(" where ");
//				buffer.append(staticIPCategory.getIDColumnName());
//				buffer.append(" = ");
//				buffer.append(catTypes[i]);
//				buffer.append(")");
//			}
//		}
//		// get projectIDs ends
//		buffer.append(")");
//
//		String SQLString = buffer.toString();
//
//		/*
//		 * System.err.print("getProjectDPTPageLinks( ");
//		 * System.err.print(SQLString); System.err.println(" )");
//		 */
//		return EntityFinder.findAll(staticPLink, SQLString);
		return null; //TMP
	}
	
	public void createHomePage(IWContext iwc, Group group, PageTriggerInfo info) throws RemoteException, Exception {

		PageLink pageLink = createPageLink(iwc, info, group.getPrimaryKey().toString(), group.getName(), null, null, null, null);

		if (pageLink != null) {
			group.setHomePageID(pageLink.getPageId());
			group.store();

			// temp - only necessary to add newly created pages into this map
			pagePropertieIds.clear();

			// replicate permissions

			List participantGroups = this.getDPTPermissionGroups(info);

			if (participantGroups != null && participantGroups.size() > 0) {
				BuilderService bservice = BuilderServiceFactory.getBuilderService(iwc);

				ICTreeNode rootPage = bservice.getPageTree(pageLink.getPageId(), iwc.getCurrentUserId());
				Vector v = new Vector();
				//System.out.println("collecting subpages");
				this.collectSubpages(v, rootPage);

				Set s = new HashSet();
				Set pages = new HashSet();
				Iterator setIter = v.iterator();
				while (setIter.hasNext()) {
					ICTreeNode item = (ICTreeNode) setIter.next();
					pages.add(Integer.toString(item.getNodeID()));
					//System.out.println("----------------------------------");
					//System.out.println("getInstanceIdsOnPage("+item.getNodeID()+")");
					//BuilderLogic.getInstance().getIBXMLPage(item.getNodeID())
					Set set = EventLogic.getInstanceIdsOnPage(item.getNodeID());

					if (set != null) {
						s.addAll(set);
					}
				}

				Iterator iter = participantGroups.iterator();
				while (iter.hasNext()) {
					GenericGroup oldGroup = (GenericGroup) iter.next();
					GenericGroup newGroup = this.getReplicatedParticipantGroup(oldGroup, "System group");

					//Pages
					List pagePermissions = AccessControl.getGroupsPermissionsForPages(oldGroup, pages);
					//System.err.println("pagePermissions: "+pagePermissions);
					if (pagePermissions != null) {
						Iterator permissionIter = pagePermissions.iterator();
						while (permissionIter.hasNext()) {
							ICPermission item = (ICPermission) permissionIter.next();
							AccessControl.replicatePermissionForNewGroup(item, newGroup);
						}
					}

					//Instances
					List permissions = AccessControl.getGroupsPermissionsForInstances(oldGroup, s);
					if (permissions != null) {
						Iterator permissionIter = permissions.iterator();
						while (permissionIter.hasNext()) {
							ICPermission item = (ICPermission) permissionIter.next();
							AccessControl.replicatePermissionForNewGroup(item, newGroup);
						}
					}

				}

			}

			// replicate permissions ends
		} else {
			// throw Exception;
		}
	}

//	public void createPageLink(IWContext iwc, int projectId, String name) throws Exception {
//
//		IPProject project = ((IPProjectHome) com.idega.data.IDOLookup.getHomeLegacy(IPProject.class)).findByPrimaryKeyLegacy(projectId);
//
//		List l = EntityFinder.findAll(com.idega.builder.dynamicpagetrigger.data.PageTriggerInfoBMPBean.getStaticInstance(PageTriggerInfo.class));
//
//		PageTriggerInfo info = (PageTriggerInfo) l.get(0);
//
//		PageLink pageLink = createPageLink(iwc, info, Integer.toString(projectId), name, null, null, null, null);
//
//		if (pageLink != null) {
//			project.addTo(PageLink.class, pageLink.getID());
//
//			// temp - only necessary to add newly created pages into this map
//			iwc.removeApplicationAttribute(_APPADDRESS_HOMEPAGES);
//
//			// replicate permissions
//
//			List participantGroups = this.getDPTPermissionGroups(info);
//
//			if (participantGroups != null && participantGroups.size() > 0) {
//				BuilderService bservice = BuilderServiceFactory.getBuilderService(iwc);
//
//				ICTreeNode rootPage = bservice.getPageTree(pageLink.getPageId(), iwc.getCurrentUserId());
//				Vector v = new Vector();
//				//System.out.println("collecting subpages");
//				this.collectSubpages(v, rootPage);
//
//				Set s = new HashSet();
//				Set pages = new HashSet();
//				Iterator setIter = v.iterator();
//				while (setIter.hasNext()) {
//					ICTreeNode item = (ICTreeNode) setIter.next();
//					pages.add(Integer.toString(item.getNodeID()));
//					//System.out.println("----------------------------------");
//					//System.out.println("getInstanceIdsOnPage("+item.getNodeID()+")");
//					//BuilderLogic.getInstance().getIBXMLPage(item.getNodeID())
//					Set set = EventLogic.getInstanceIdsOnPage(item.getNodeID());
//
//					if (set != null) {
//						s.addAll(set);
//					}
//				}
//
//				Iterator iter = participantGroups.iterator();
//				while (iter.hasNext()) {
//					GenericGroup oldGroup = (GenericGroup) iter.next();
//					GenericGroup newGroup = this.getReplicatedParticipantGroup(oldGroup, project, "System group");
//
//					//Pages
//					List pagePermissions = AccessControl.getGroupsPermissionsForPages(oldGroup, pages);
//					//System.err.println("pagePermissions: "+pagePermissions);
//					if (pagePermissions != null) {
//						Iterator permissionIter = pagePermissions.iterator();
//						while (permissionIter.hasNext()) {
//							ICPermission item = (ICPermission) permissionIter.next();
//							AccessControl.replicatePermissionForNewGroup(item, newGroup);
//						}
//					}
//
//					//Instances
//					List permissions = AccessControl.getGroupsPermissionsForInstances(oldGroup, s);
//					if (permissions != null) {
//						Iterator permissionIter = permissions.iterator();
//						while (permissionIter.hasNext()) {
//							ICPermission item = (ICPermission) permissionIter.next();
//							AccessControl.replicatePermissionForNewGroup(item, newGroup);
//						}
//					}
//
//				}
//
//			}
//
//			// replicate permissions ends
//		} else {
//			// throw Exception;
//		}
//	}


	private void collectSubpages(List l, ICTreeNode node) {
		if (node != null) {
			l.add(node);
			Iterator tmp = node.getChildren();
			if (tmp != null) {
				while (tmp.hasNext()) {
					collectSubpages(l, (ICTreeNode) tmp.next());
				}
			}
		}
	}

	private GenericGroup getReplicatedParticipantGroup(GenericGroup group, String newGroupName) throws SQLException {
//		IPParticipantGroup newGroup = ((IPParticipantGroupHome) IDOLookup.getHomeLegacy(IPParticipantGroup.class)).createLegacy();
//
//		if (newGroupName != null) {
//			newGroup.setName(newGroupName);
//		} else {
//			newGroup.setName(group.getName());
//		}
//
//		String desc = group.getDescription();
//		if (desc != null) {
//			newGroup.setDescription(desc);
//		}
//
//		String extra = group.getExtraInfo();
//		if (extra != null) {
//			newGroup.setExtraInfo(extra);
//		}
//
//		newGroup.insert();
//
//		newGroup.addGroup(group);
//
//		return newGroup;
		return null; //tmp
	}
	
//	private GenericGroup getReplicatedParticipantGroup(GenericGroup group, IPProject project, String newGroupName) throws SQLException {
//		IPParticipantGroup newGroup = ((IPParticipantGroupHome) com.idega.data.IDOLookup.getHomeLegacy(IPParticipantGroup.class)).createLegacy();
//
//		if (newGroupName != null) {
//			newGroup.setName(newGroupName);
//		} else {
//			newGroup.setName(group.getName());
//		}
//
//		String desc = group.getDescription();
//		if (desc != null) {
//			newGroup.setDescription(desc);
//		}
//
//		String extra = group.getExtraInfo();
//		if (extra != null) {
//			newGroup.setExtraInfo(extra);
//		}
//
//		newGroup.insert();
//
//		newGroup.addGroup(group);
//		project.addTo(newGroup);
//
//		return newGroup;
//	}
//
//	public GenericGroup getProjectParticipantGroup(int dptPermissionGroupId, int projectId) throws SQLException {
//		GenericGroup staticGenericGroup = (GenericGroup) GenericGroupBMPBean.getStaticInstance(GenericGroup.class);
//		IPProject staticIPProject = (IPProject) IPProjectBMPBean.getStaticInstance(IPProject.class);
//
//		String ipProjectICGroup = staticIPProject.getNameOfMiddleTable(staticIPProject, staticGenericGroup);
//		String groupTreeTable = staticIPProject.getNameOfMiddleTable(staticGenericGroup, staticGenericGroup);
//
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("select g.* from ");
//		buffer.append(ipProjectICGroup);
//		buffer.append(" pg, ");
//		buffer.append(groupTreeTable);
//		buffer.append(" gt, ");
//		buffer.append(staticIPProject.getEntityName());
//		buffer.append(" p, ");
//		buffer.append(staticGenericGroup.getEntityName());
//		buffer.append(" g");
//		buffer.append(" where ");
//		buffer.append("pg.");
//		buffer.append(staticGenericGroup.getIDColumnName());
//		buffer.append(" = g.");
//		buffer.append(staticGenericGroup.getIDColumnName());
//		buffer.append(" and pg.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" = p.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" and gt.");
//		buffer.append(staticGenericGroup.getIDColumnName());
//		buffer.append(" = g.");
//		buffer.append(staticGenericGroup.getIDColumnName());
//		buffer.append(" and gt.child_");
//		buffer.append(staticGenericGroup.getIDColumnName());
//		buffer.append(" = ");
//		buffer.append(dptPermissionGroupId);
//		buffer.append(" and pg.");
//		buffer.append(staticIPProject.getIDColumnName());
//		buffer.append(" = ");
//		buffer.append(projectId);
//
//		/**
//		 * select g.* from ip_project_ic_group pg, ic_group_tree gt, ic_group g,
//		 * ip_project p where pg.ic_group_id = g.ic_group_id and
//		 * pg.ip_project_id = p.ip_project_id and gt.ic_group_id = g.ic_group_id
//		 * and gt.child_ic_group_id = 5 and pg.ip_project_id = 56
//		 */
//
//		String SQLString = buffer.toString();
//		/*
//		 * System.err.print("getProjectParticipantGroup( ");
//		 * System.err.print(SQLString); System.err.println(" )");
//		 */
//
//		List l = EntityFinder.findAll(staticGenericGroup, SQLString);
//
//		//  System.err.println(" = "+l);
//
//		if (l != null && l.size() > 0) {
//			return (GenericGroup) l.get(0);
//		} else {
//			return null;
//		}
//	}

	/**
	 * temp wrong?
	 */
	private void cachPageToProjectRelationship(IWContext iwc, int pageId, int refDataID) {
		pagePropertieIds.put(Integer.toString(pageId), Integer.toString(refDataID));
	}

	public int getCurrentGroupID(IWContext iwc) throws Exception {

		String pageId = iwc.getParameter(BuilderConstants.IB_PAGE_PARAMETER);

		if (pageId != null) {
			String projectID = (String) pagePropertieIds.get(pageId);
			if (projectID != null) {
				return Integer.parseInt(projectID);
			} else {
				List links = null;
				try {
					links = getGroupDPTPageLinks(null);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (links != null) {
					BuilderService bservice = BuilderServiceFactory.getBuilderService(iwc);
					Iterator iter = links.iterator();
					while (iter.hasNext()) {
						PageLink item = (PageLink) iter.next();
						int pPageId = item.getPageId();
						String refDataID = item.getReferencedDataId();

						if ((pPageId != -1) && (refDataID != null) && (!refDataID.equals(""))) {
							if (pPageId > 0) {
								pagePropertieIds.put(Integer.toString(pPageId), refDataID);
							}
							Vector v = null;
							try {
								ICTreeNode rootPage = bservice.getPageTree(pPageId, iwc.getCurrentUserId());
								v = new Vector();
								collectSubpages(v, rootPage);
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							if (v != null) {
								Iterator iter2 = v.iterator();
								while (iter2.hasNext()) {
									ICTreeNode item2 = (ICTreeNode) iter2.next();
									int tmpPageID = item2.getNodeID();
									if (tmpPageID > 0) {
										pagePropertieIds.put(Integer.toString(tmpPageID), refDataID);
									}
								}
							}
						}
					}
				}
				String s = (String) pagePropertieIds.get(pageId);
				if (s != null) {
					try {
						return Integer.parseInt(s);
					} catch (NumberFormatException ex) {
						return -1;
					}
				} else {
					return -1;
				}
			}
		} else {
			return -1;
		}
	}

	public boolean invalidateGroup(IWContext iwc, Group group) throws IDOLookupException {

		//GroupHome grHome = ((GroupHome)IDOLookup.getHome(Group.class));
		
		group.setHomePage(null);
		
		group.store();
		
//		PageLink link = ((PageLinkHome)IDOLookup.getHome(PageLink.class)).find
		
//			List l = EntityFinder.findRelated(p,PageLinkBMPBean.getStaticInstance(PageLink.class));
//			if (l != null && l.size() > 0) {
//				boolean b = invalidatePageLink(iwc, (PageLink) l.get(0), User.get);
//				if (!b) {
//					return false;
//				}
//			}

			return true;
	}

} //