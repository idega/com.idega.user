package com.idega.user.presentation;

import com.idega.data.IDOLookup;
import com.idega.user.data.*;
import com.idega.user.event.CreateGroupEvent;
import java.util.*;
import com.idega.builder.data.IBDomainHome;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import com.idega.data.IDORelationshipException;
import javax.ejb.FinderException;
import com.idega.builder.data.IBDomain;
import com.idega.core.ICTreeNode;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
 * @version 1.0
 */

public class GroupTreeNode implements ICTreeNode {

	private ICTreeNode _parent = null;

	private IBDomain _domain = null;
	private Group _group = null;
	private int _nodeType;
	public static final int TYPE_DOMAIN = CreateGroupEvent.TYPE_DOMAIN;
	public static final int TYPE_GROUP = CreateGroupEvent.TYPE_GROUP;

	public GroupTreeNode(IBDomain domain) {
		_domain = domain;
		_nodeType = TYPE_DOMAIN;
	}

	public GroupTreeNode(Group group) {
		_group = group;
		_nodeType = TYPE_GROUP;
	}

	private GroupTypeHome getGroupTypeHome() throws RemoteException {
		return ((GroupTypeHome) IDOLookup.getHome(GroupType.class));
	}
	private GroupHome getGroupHome() throws RemoteException {
		return ((GroupHome) IDOLookup.getHome(Group.class));
	}

	public int getNodeType() {
		return _nodeType;
	}

	public void setParent(ICTreeNode parent) {
		_parent = parent;
	}

	public Iterator getChildren() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				/**
				 * @todo optimize the tree. store the tree nodes in session?
				 */
				try {
					List l = new Vector();
					//          Iterator iter = _domain.getTopLevelGroupsUnderDomain().iterator();
					Collection groupTypes = this.getGroupTypeHome().findVisibleGroupTypes();
					Iterator iter = this.getGroupHome().findTopNodeGroupsContained(_domain, groupTypes, true).iterator();
					GroupTreeNode node = null;
					while (iter.hasNext()) {
						Group item = (Group) iter.next();
						node = new GroupTreeNode(item);
						node.setParent(this);
						l.add(node);
					}

					return l.iterator();

				}
				catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			case TYPE_GROUP :
				List l = new Vector();

				Group g = null;
				if (isAlias()) {
					g = getAlias();
				}
				else {
					g = _group;
				}

				Iterator iter = g.getChildren();
				GroupTreeNode node = null;
				while (iter.hasNext()) {
					Group item = (Group) iter.next();
					node = new GroupTreeNode(item);
					node.setParent(this);
					l.add(node);
				}

				return l.iterator();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}

	}

	private boolean isAlias() {
		if (_group == null)
			return false;

		try {
			if (_group.getGroupType().equals("alias")) {
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private Group getAlias() {
		if (_group == null)
			return null;

		Group alias = null;

		try {
			alias = _group.getAlias();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return alias;
	}

	public boolean getAllowsChildren() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				return true;
			case TYPE_GROUP :
				if (isAlias())
					return getAlias().getAllowsChildren();
				else
					return _group.getAllowsChildren();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public ICTreeNode getChildAtIndex(int childIndex) {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				try {
					GroupTreeNode node = new GroupTreeNode(((IBDomainHome) _domain.getEJBLocalHome()).findByPrimaryKey(new Integer(childIndex)));
					node.setParent(this);
					return node;
				}
				catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			case TYPE_GROUP :
				try {
					Group g = null;
					if (isAlias())
						g = getAlias();
					else
						g = _group;
					GroupTreeNode node = new GroupTreeNode((Group) g.getChildAtIndex(childIndex));
					node.setParent(this);
					return node;
				}
				catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public int getChildCount() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				try {
					/**
					 * @todo optimize
					 */
					Collection groupTypes = this.getGroupTypeHome().findVisibleGroupTypes();
					return this.getGroupHome().getNumberOfTopNodeGroupsContained(_domain, groupTypes, true);
				}
				catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			case TYPE_GROUP :
				if (isAlias())
					return getAlias().getChildCount();
				else
					return _group.getChildCount();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public int getIndex(ICTreeNode node) {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				return node.getNodeID();
			case TYPE_GROUP :
				if (isAlias())
					return getAlias().getIndex(node);
				else
					return _group.getIndex(node);
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public ICTreeNode getParentNode() {
		return _parent;
	}

	public boolean isLeaf() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				return false;
			case TYPE_GROUP :
				return false;
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public String getNodeName() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				return _domain.getName();
			case TYPE_GROUP :
				return _group.getNodeName();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public int getNodeID() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				try {
					return ((Integer) _domain.getPrimaryKey()).intValue();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex.getMessage());
				}
			case TYPE_GROUP :
				return _group.getNodeID();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}

	public int getSiblingCount() {
		switch (_nodeType) {
			case TYPE_DOMAIN :
				if (_parent != null) {
					return _parent.getChildCount();
				}
				else {
					return 0;
				}
			case TYPE_GROUP :
				return _group.getSiblingCount();
			default :
				throw new UnsupportedOperationException("Operation not supported for type:" + getNodeType());
		}
	}
}