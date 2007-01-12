package com.idega.user.presentation;

import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.browser.presentation.IWTreeControl;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.user.business.GroupTreeNode;
import com.idega.user.event.SelectDomainEvent;
import com.idega.user.event.SelectGroupEvent;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class GroupTreeView extends IWTreeControl {

	private static final String TREEVIEW_PREFIX = "treeviewer/ui/";

	Image folderAndFileIcons[][] = null;
	String folderAndFileIconNames[] = { "treeviewer_node_closed.gif", "treeviewer_node_open.gif", "treeviewer_node_leaf.gif" };
	String classTypeIcons[] = { "domain/", "group/", "user/" };
	String fileIconNames[] = { "_node_open.gif", "_node_closed.gif", "_node_leaf.gif" };

	private static final int FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED = 0;
	private static final int FOLDERANDFILE_ICONINDEX_FOLDER_OPEN = 1;
	private static final int FOLDERANDFILE_ICONINDEX_FILE = 2;

	public static final String PRM_OPEN_TREENODES = "ic_opn_trnds";
	public static final String PRM_TREENODE_TO_CLOSE = "ic_cls_trnd";

	String nodeNameTarget = null;
	String nodeActionPrm = null;
	Link _linkPrototype = null;
	String _linkStyle = null;
	boolean _usesOnClick = false;
	private boolean _nowrap = true;
	private Layer _nowrapLayer = null;

	public static final String ONCLICK_FUNCTION_NAME = "treenodeselect";
	public static final String ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME = "iw_node_id";
	public static final String ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME = "iw_node_name";

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
  
  private int selectedGroupId;
  private int selectedDomainId;
	
	private IWBundle bundle;
	
	private boolean isModelSet = false;

	public GroupTreeView() {
		super();
		this.folderAndFileIcons = new Image[3][3];
		this.setColumns(2);
		this.setTreeColumnWidth(1, "16");
		setWrap(false);
	}

	public static GroupTreeView getGroupTreeInstance(ICTreeNode node, IWContext iwc) {
		GroupTreeView viewer = new GroupTreeView();
		viewer.setRootNode(node);
		return viewer;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	protected void updateIconDimensions() {
		super.updateIconDimensions();
		for (int j = 0; j < this.folderAndFileIcons.length; j++) {
			for (int i = 0; i < this.folderAndFileIcons[j].length; i++) {
				Image tmp = this.folderAndFileIcons[j][i];
				if (tmp != null) {
					//tmp.setWidth(iconWidth);
					tmp.setHeight(this.iconHeight);
					//tmp.setAlignment("top");
					this.folderAndFileIcons[j][i] = tmp;
				}
			}
		}
	}

	public void initIcons(IWContext iwc) {
		super.initIcons(iwc);

		this.bundle = getBundle(iwc);
		for (int j = 0; j < this.classTypeIcons.length; j++) {
			for (int i = 0; i < this.folderAndFileIcons.length; i++) {
				if (this.folderAndFileIcons[j][i] == null) {
					this.folderAndFileIcons[j][i] = this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[j] + this.folderAndFileIconNames[i]);
				}
			}
		}

		updateIconDimensions();
	}

	/*
	  public void addParameters(Link l, ICTreeNode node, IWContext iwc){
	
	  }
	*/

	public PresentationObject getObjectToAddToColumn(int colIndex, ICTreeNode node, IWContext iwc, boolean nodeIsOpen, boolean nodeHasChild, boolean isRootNode) {
		return getObjectToAddToColumn(colIndex, (GroupTreeNode) node, iwc, nodeIsOpen, nodeHasChild, isRootNode);
	}
	
	public PresentationObject getObjectToAddToColumn(int colIndex, GroupTreeNode node, IWContext iwc, boolean nodeIsOpen, boolean nodeHasChild, boolean isRootNode) {
		//System.out.println("adding into column "+ colIndex + " for node " + node);
		String image = "treeviewer";
		if (node.getGroupType() != null) {
			image = node.getGroupType();
		}
		
	    SelectDomainEvent dmSelect = new SelectDomainEvent();
        SelectGroupEvent grSelect = new SelectGroupEvent();
		
	    switch (node.getNodeType()) {
			case GroupTreeNode.TYPE_DOMAIN :
				dmSelect.setDomainToSelect(node.getNodeID());
				break;
			case GroupTreeNode.TYPE_GROUP :
				grSelect.setGroupToSelect(node.getNodeID());
				if (node.getParentNode() != null) {
					if (((GroupTreeNode) node.getParentNode()).getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
						grSelect.setParentDomainOfSelection(node.getParentNode().getNodeID());
					}
					else {
						grSelect.setParentGroupOfSelection(node.getParentNode().getNodeID());
					}
				}
				break;
	    }

		switch (colIndex) {
			case 1 :
				if(!this.isModelSet) {
					if(getRefreshLink() != null) {
						if(this.getControlEventModel() != null) {
							getRefreshLink().addEventModel(this.getControlEventModel());
							this.isModelSet = true;
						}
						if(this.getControlTarget() != null) {
							getRefreshLink().setTarget(this.getControlTarget());
						}
					}
				}
				

				if (!node.isLeaf()) {
					if (nodeIsOpen) {
						if (isRootNode && !showRootNodeTreeIcons()) {
							Link l = new Link();
							if (this.getControlEventModel() != null) {
								l.addEventModel(this.getControlEventModel());
							}
							if (this.getControlTarget() != null) {
								l.setTarget(this.getControlTarget());
							}
							if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
								l.setImage(this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN]);
							}
							else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
								l.setImage(this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image + this.fileIconNames[0]));
							}
							//              else if(node instanceof User){
							//                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN]);
							//              }

							if (!nodeIsOpen) { //   || allowRootNodeToClose ){
								this.setLinkToOpenOrCloseNode(l, node, nodeIsOpen);
							}
							return l;
						}
						else {
							if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
								return this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN];
							}
							else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
							    Link l = this.getLinkPrototypeClone(node.getNodeName());
							    l.setOnClick("setLinkToBold(findObj('group_id_"+node.getNodeID()+"'))");
							    l.addEventModel(grSelect);
							    l.setImage(this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image +  this.fileIconNames[0]));
							    return l;
							}
							//              else if(node instanceof User){
							//                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN];
							//              }

						}
					}
					else {
						if (isRootNode && !showRootNodeTreeIcons()) {
							Link l = new Link();
							if (this.getControlEventModel() != null) {
								l.addEventModel(this.getControlEventModel());
							}
							if (this.getControlTarget() != null) {
								l.setTarget(this.getControlTarget());
							}
							if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
								l.setImage(this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED]);
							}
							else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
								l.setImage(this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image +  this.fileIconNames[1]));
							}
							//              else if(node instanceof User){
							//                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED]);
							//              }
							this.setLinkToOpenOrCloseNode(l, node, nodeIsOpen);
							return l;
						}
						else {
							if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
								return this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED];
							}
							else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
							    Link l = this.getLinkPrototypeClone(node.getNodeName());
							    l.setOnClick("setLinkToBold(findObj('group_id_"+node.getNodeID()+"'))");
							    l.addEventModel(grSelect);
							    l.setImage(this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image +  this.fileIconNames[1]));
							    return l;
							}
							//              else if(node instanceof User){
							//                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED];
							//              }

						}
					}
				}
				else {
					if (isRootNode && !showRootNodeTreeIcons()) {
						Link l = new Link();
						if (this.getControlEventModel() != null) {
							l.addEventModel(this.getControlEventModel());
						}
						if (this.getControlTarget() != null) {
							l.setTarget(this.getControlTarget());
						}
						if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
							l.setImage(this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FILE]);
						}
						else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
							l.setImage(this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image +  this.fileIconNames[2]));
						}
						//              else if(node instanceof User){
						//                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FILE]);
						//              }
						this.setLinkToOpenOrCloseNode(l, node, nodeIsOpen);
						return l;
					}
					else {
						if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
							return this.folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FILE];
						}
						else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
							return this.bundle.getImage(TREEVIEW_PREFIX + getUI() + this.classTypeIcons[1] + image +  this.fileIconNames[2]);
						}
						//              else if(node instanceof User){
						//                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FILE];
						//              }
					}
				}
			case 2 :
				Link l = this.getLinkPrototypeClone(node.getNodeName());
				l.setOnClick("setLinkToBold(this)");
        l.setID("group_id_"+node.getNodeID());
        // set selection bold
        if (this.selectedDomainId > 0 || this.selectedGroupId > 0) {
          int nodeType = node.getNodeType();
          int nodeId = node.getNodeID();
          if (  ((nodeType == GroupTreeNode.TYPE_DOMAIN) && (nodeId == this.selectedDomainId)) ||
                ((nodeType == GroupTreeNode.TYPE_GROUP) && (nodeId == this.selectedGroupId)) ) {
            String script = "hugo=document.getElementById('"+node.getNodeID()+"'); setLinkToBold(hugo);";
            getParentPage().setOnLoad(script);
          }
        }

		if (node.getNodeType() == GroupTreeNode.TYPE_DOMAIN) {
		    l.addEventModel(dmSelect);
		} else if (node.getNodeType() == GroupTreeNode.TYPE_GROUP) {
		    l.addEventModel(grSelect);
		}

				if (this._usesOnClick) {
					String nodeName = node.getNodeName();
					l.setURL("#");
					l.setOnClick(ONCLICK_FUNCTION_NAME + "('" + nodeName + "','" + node.getNodeType() + "_" + node.getNodeID() + "')");
				}
				//        else if(nodeActionPrm != null){
				//          l.addParameter(nodeActionPrm,node.getNodeID());
				//        }
				this.setLinkToMaintainOpenAndClosedNodes(l);
				/*if(_nowrap){
				  return getNoWrapLayerClone(l);
				} else {*/
				return l;
				//}
		}
		return null;
	}

	public void setWrap() {
		setWrap(false);
	}

	public void setWrap(boolean value) {
		super.setNowrap(!value);
	}

	public void setNodeActionParameter(String prm) {
		this.nodeActionPrm = prm;
	}

	public void setTarget(String target) {
		this.nodeNameTarget = target;
	}

	public void setTreeStyle(String style) {
		this._linkStyle = style;
	}

	public void setLinkPrototype(Link link) {
		this._linkPrototype = link;
	}

	private Link getLinkPrototype() {
		if (this._linkPrototype == null) {
			this._linkPrototype = new Link();
		}

		if (this.nodeNameTarget != null) {
			this._linkPrototype.setTarget(this.nodeNameTarget);
		}

		if (this.getControlTarget() != null) {
			this._linkPrototype.setTarget(this.getControlTarget());
		}

		if (this._linkStyle != null) {
			this._linkPrototype.setFontStyle(this._linkStyle);
		}

		/*
		    if ( _linkStyle != null )
		      _linkPrototype.setFontStyle(_linkStyle);
		*/
		return this._linkPrototype;
	}

	public Layer getNoWrapLayer() {
		if (this._nowrapLayer == null) {
			this._nowrapLayer = new Layer();
			this._nowrapLayer.setNoWrap();
		}
		return this._nowrapLayer;
	}

	private Link getLinkPrototypeClone() {
		return (Link) getLinkPrototype().clone();
	}

	private Link getLinkPrototypeClone(String text) {
		Link l = (Link) getLinkPrototype().clone();
		l.setText(text);
		if (this.getControlEventModel() != null) {
			l.addEventModel(this.getControlEventModel());
		}
		//     else {
		//      System.out.println("GROUPTREEVIEW: eventmodel == null");
		//    }

		if (this.getControlTarget() != null) {
			l.setTarget(this.getControlTarget());
		}
		//     else {
		//      System.out.println("GROUPTREEVIEW: controlTarget == null");
		//    }
		return l;
	}

	private Layer getNoWrapLayerClone() {
		Layer l = (Layer) getNoWrapLayer().clone();
		return l;
	}

	private Layer getNoWrapLayerClone(PresentationObject obj) {
		Layer l = getNoWrapLayerClone();
		l.add(obj);
		return l;
	}

	private Link getLinkPrototypeClone(Image image) {
		Link l = (Link) getLinkPrototype().clone();
		l.setImage(image);
		if (this.getControlEventModel() != null) {
			l.addEventModel(this.getControlEventModel());
		}
		return l;
	}

	public void setToUseOnClick() {
		setToUseOnClick(ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME, ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME);
	}

	public void setToUseOnClick(String NodeNameParameterName, String NodeIDParameterName) {
		this._usesOnClick = true;
		getAssociatedScript().addFunction(ONCLICK_FUNCTION_NAME, "function " + ONCLICK_FUNCTION_NAME + "(" + NodeNameParameterName + "," + NodeIDParameterName + "){ }");

	}

	public void setOnClick(String action) {
		this.getAssociatedScript().addToFunction(ONCLICK_FUNCTION_NAME, action);
	}

	public void setControlTarget(String controlTarget) {
		super.setControlTarget(controlTarget);
		this.nodeNameTarget = null;
	}

  /**
   * @param i
   */
  public void setSelectedDomainId(int i) {
    this.selectedDomainId = i;
  }

  /**
   * @param i
   */
  public void setSelectedGroupId(int i) {
    this.selectedGroupId = i;
  }

}