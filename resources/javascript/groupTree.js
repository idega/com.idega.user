	var JSTreeObj;
	var treeUlCounter = 0;
	var nodeId = 1;
	var thisTree;
	var divClass = '';
	var globalDivId = null;
	var saveOnDrop = false;
	var movingNode = false;
	var divId = '';
		

	function setNodes(nodes){
		var rootUl = document.createElement('ul');		
		rootUl = addTreeElements(nodes, rootUl);
		rootUl.setAttribute('class', 'tree_drag_drop');
		rootUl.setAttribute('id','tree');
		var divElement = document.getElementById(divId);
		divElement.appendChild(rootUl);
		
		treeObj = new GroupTree();
		treeObj.setTreeId('tree');
		treeObj.initTree();
		treeObj.expandAll();
	}
	
	function addTreeElements(nodes, rootUl){
		for(var i = 0; i < nodes.length; i++){
			var liElement = document.createElement('li');
			liElement.setAttribute('id',nodes[i].uniqueId);
			var link = document.createElement('a');
			link.setAttribute('href', '#');
			var text=document.createTextNode(nodes[i].name);
			link.appendChild(text);
			liElement.appendChild(link);
			if(nodes[i].hasChildren == true){
				var ulElement = document.createElement('ul');
				ulElement = addTreeElements(nodes[i].children, ulElement);
				ulElement.setAttribute('class', 'tree_drag_drop');
				liElement.appendChild(ulElement);
			}
			rootUl.appendChild(liElement);
		}
		return rootUl;
	}
	
	function setDivId(div){
		divId = div;
	}
	
	function loadTree(){

		GroupService.getTopGroupNodes(setNodes);		
	}

	function getPathToImageFolder(){
		ThemesEngine.getPathToImageFolder(setFolderPath);	
	}
	
	function setFolderPath(path){
		imageFolder = path;
		iconFolder = path + 'pageIcons/';
	}

	function GroupTree()
	{
		var thisTree = false;
		var idOfTree;
		
		var sourceTree;
		var actionOnMouseUp;
		
		var imageFolder;
		var folderImage;
		var plusImage;
		var minusImage;
		var maximumDepth;
		var dragNode_source;
		var dragNode_parent;
		var dragNode_sourceNextSib;
		var dragNode_noSiblings; 
		var deleteNodes;
		
		var dragNode_destination;
		var floatingContainer;
		var dragDropTimer;
		var dropTargetIndicator;
		var insertAsSub;
		var indicator_offsetX;
		var indicator_offsetX_sub;
		var indicator_offsetY;
		var newPageId;
		var treeStructure;
		var parentId;
		var firstTopPage;
		var previousParentId;
		var previousPlaceInLevel;
				
		this.firstTopPage = false;
		this.parentid = -1;
		this.newPageId = -1;
		this.deleteNodes = false;
		this.sourceTreee = true;
		this.actionOnMouseUp = 'empty';
		
//		this.imageFolder = '/idegaweb/bundles/com.idega.content.bundle/resources/images/';
//		this.iconFolder = '/idegaweb/bundles/com.idega.content.bundle/resources/images/pageIcons/';
		this.imageFolder = '/idegaweb/bundles/com.idega.user.bundle/resources/images/';
		this.iconFolder = '/idegaweb/bundles/com.idega.user.bundle/resources/images/';
//		this.imageFolder = '/idegaweb/bundles/com.idega.block.cal.bundle/resources/';
//		this.iconFolder = '/idegaweb/bundles/com.idega.block.cal.bundle/resources/';
//		this.folderImage = 'text.png';
		this.folderImage = 'general_node_closed.gif';
		this.plusImage = 'nav-plus.gif';
		this.minusImage = 'nav-minus.gif';
		this.maximumDepth = 6;
		var messageMaximumDepthReached;
				
		this.floatingContainer = document.createElement('UL');
		this.floatingContainer.style.position = 'absolute';
		this.floatingContainer.style.display='none';
		this.floatingContainer.id = 'floatingContainer';
		this.insertAsSub = false;
		document.body.appendChild(this.floatingContainer);
		this.dragDropTimer = -1;
		this.dragNode_noSiblings = false;
		
		if(document.all){
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 4;
			this.indicator_offsetY = 2;
		}else{
			this.indicator_offsetX = 1;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = 2;			
		}
		if(navigator.userAgent.indexOf('Opera')>=0){
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = -7;				
		}

		this.messageMaximumDepthReached = ''; // Use '' if you don't want to display a message 
	}
	
	/* GroupTree class */
	GroupTree.prototype = {
		
		Get_Cookie : function(name) { 
		   var start = document.cookie.indexOf(name+"="); 
		   var len = start+name.length+1; 
		   if ((!start) && (name != document.cookie.substring(0,name.length))) return null; 
		   if (start == -1) return null; 
		   var end = document.cookie.indexOf(";",len); 
		   if (end == -1) end = document.cookie.length; 
		   return unescape(document.cookie.substring(len,end)); 
		} 
		,
		// This function has been slightly modified
		Set_Cookie : function(name,value,expires,path,domain,secure) { 			
			expires = expires * 60*60*24*1000;
			var today = new Date();
			var expires_date = new Date( today.getTime() + (expires) );
		    var cookieString = name + "=" +escape(value) + 
		       ( (expires) ? ";expires=" + expires_date.toGMTString() : "") + 
		       ( (path) ? ";path=" + path : "") + 
		       ( (domain) ? ";domain=" + domain : "") + 
		       ( (secure) ? ";secure" : ""); 
		    document.cookie = cookieString; 
		} 
		,setMaximumDepth : function(maxDepth)
		{
			this.maximumDepth = maxDepth;	
		}
		,setMessageMaximumDepthReached : function(newMessage)
		{
			this.messageMaximumDepthReached = newMessage;
		}
		,	
		setImageFolder : function(path)
		{
			this.imageFolder = path;	
		}
		,
		setFolderImage : function(imagePath)
		{
			this.folderImage = imagePath;			
		}
		,
		setPlusImage : function(imagePath)
		{
			this.plusImage = imagePath;				
		}
		,
		setMinusImage : function(imagePath)
		{
			this.minusImage = imagePath;			
		}
		,		
		setTreeId : function(idOfTree)
		{
			this.idOfTree = idOfTree;		
		}	
		,
		expandAll : function()
		{
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');		
			for(var no=0;no<menuItems.length;no++){
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display!='block'){				
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}			
			}
		}	
		,
		collapseAll : function()
		{
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');
			for(var no=0;no<menuItems.length;no++){
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display=='block'){
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}			
			}		
		}	
		,
		showHideNode : function(e,inputId) {			
			if(inputId){
				if(!document.getElementById(inputId))
					return;
				thisNode = document.getElementById(inputId).getElementsByTagName('IMG')[0]; 
			}
			else {
				thisNode = this;
				if(this.tagName=='A')
					thisNode = this.parentNode.getElementsByTagName('IMG')[0];											
			}					
			if(thisNode.style.visibility=='hidden')
				return;		
			var parentNode = thisNode.parentNode;
			inputId = parentNode.id.replace(/[^0-9]/g,'');
			if(thisNode.src.indexOf(JSTreeObj.plusImage)>=0){
				thisNode.src = thisNode.src.replace(JSTreeObj.plusImage,JSTreeObj.minusImage);
				var ul = parentNode.getElementsByTagName('UL')[0];
				ul.style.display='block';
				if(!initExpandedNodes)initExpandedNodes = ',';
				if(initExpandedNodes.indexOf(',' + inputId + ',')<0) initExpandedNodes = initExpandedNodes + inputId + ',';
			}else{					
				thisNode.src = thisNode.src.replace(JSTreeObj.minusImage,JSTreeObj.plusImage);
				parentNode.getElementsByTagName('UL')[0].style.display='none';
				initExpandedNodes = initExpandedNodes.replace(',' + inputId,'');
			}	

			JSTreeObj.Set_Cookie('dhtmlgoodies_expandedNodes',initExpandedNodes,500);			
			return false;						
		}
		,

		folderPath : function (path){
			JSTreeObj.iconFolder = path;
		}
		,
		initClick : function(e)
		{
			if(saveOnDrop == true)
				movingNode = true;
			else
				movingNode = false;
			var liTag = document.getElementsByTagName('LI')[0];
			
			var subs = JSTreeObj.floatingContainer.getElementsByTagName('LI');
			if(subs.length>0){
				if(JSTreeObj.dragNode_sourceNextSib){
					
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
				}
				else{
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}					
			}
		}
		,
		copyDragableNode : function(e)
		{			
			if(saveOnDrop == true)
				movingNode = true;
			else
				movingNode = false;
			var sourceTree = false;			
			var liTag = document.getElementsByTagName('LI')[0];
			
			var subs = JSTreeObj.floatingContainer.getElementsByTagName('LI');

			if(subs.length>0){
				if(JSTreeObj.dragNode_sourceNextSib){
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
				}
				else{
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}
			}
			
			JSTreeObj.dragNode_source = this.parentNode;
			JSTreeObj.dragNode_parent = this.parentNode.parentNode;
			var parentDiv = JSTreeObj.dragNode_parent;
			while(true){
				if (parentDiv.getElementsByTagName('DIV')[0]){
					globalDivId = parentDiv.getElementsByTagName('DIV')[0].id;
					break;					
				}
				parentDiv = parentDiv.parentNode;
			}
			JSTreeObj.dragNode_sourceNextSib = false;
			
			if(JSTreeObj.dragNode_source.nextSibling)JSTreeObj.dragNode_sourceNextSib = JSTreeObj.dragNode_source.nextSibling;
			JSTreeObj.dragNode_destination = false;
			JSTreeObj.dragDropTimer = 0;
//			JSTreeObj.timerDragCopy();
			
			return false;
		}
		,		
		initTree : function()
		{
			JSTreeObj = this;
//			ThemesEngine.getPathToImageFolder(JSTreeObj.folderPath);
//			JSTreeObj.createDropIndicator();
			document.documentElement.onselectstart = JSTreeObj.cancelSelectionEvent;
			document.documentElement.ondragstart = JSTreeObj.cancelEvent;
			var nodeId = 0;
			
			var dhtmlgoodies_tree = document.getElementById(this.idOfTree);
			var menuItems = dhtmlgoodies_tree.getElementsByTagName('LI');	// Get an array of all menu items
			var item = menuItems[0];
			for(var no=0;no<menuItems.length;no++){
				// No children var set ?
				
				var noChildren = false;
				var tmpVar = menuItems[no].getAttribute('noChildren');
				if(!tmpVar)tmpVar = menuItems[no].noChildren;
				if(tmpVar=='true')noChildren=true;

				var sourceTree = false;
				var tmpVar = menuItems[no].getAttribute('sourceTree');
				if(!tmpVar)
					tmpVar = menuItems[no].sourceTree;
				if(tmpVar=='true')
					sourceTree=true;
				// No drag var set ?
				var noDrag = false;
				var tmpVar = menuItems[no].getAttribute('noDrag');
				if(!tmpVar)tmpVar = menuItems[no].noDrag;
				if(tmpVar=='true')noDrag=true;
				
				var iconfile = null;		 
				var tmpVar = menuItems[no].getAttribute('iconfile');
				if(!tmpVar)				
					tmpVar = menuItems[no].iconfile;
				if(tmpVar)
					iconfile = tmpVar;
				else {
					var pageType = menuItems[no].getAttribute('pagetype');
					if (pageType)
//						iconfile = this.imageFolder + menuItems[no].getAttribute('pagetype') +'.png';
						iconfile = JSTreeObj.iconFolder + menuItems[no].getAttribute('pagetype') +'.png';
					else
						iconfile = JSTreeObj.iconFolder + this.folderImage;
console.log('iconfile '+iconfile);				 						
				}

				var templatefile = null;		 
				var tmpVar = menuItems[no].getAttribute('templatefile');
				if(!tmpVar)				
					tmpVar = menuItems[no].templatefile;
				if(tmpVar)
					templatefile = tmpVar;
									
				nodeId++;
				var subItems = menuItems[no].getElementsByTagName('UL');
				var img = document.createElement('IMG');
				img.src = this.imageFolder + this.plusImage;
				img.onclick = JSTreeObj.showHideNode;
				
				if(subItems.length==0)
					img.style.visibility='hidden';
				else{
					subItems[0].id = 'tree_ul_' + treeUlCounter;
					treeUlCounter++;
				}
						
				var aTag = menuItems[no].getElementsByTagName('A')[0];
//console.log('a tag');				
			
				if(aTag.id)
					numericId = aTag.id.replace(/[^0-9]/g,'');
				else
					numericId = (no+1);			
	
				aTag.id = menuItems[no].id + 'a';
	
				var input = document.createElement('INPUT');
				input.style.width = '40%';
				input.style.display='none';
				
				menuItems[no].insertBefore(input,aTag);
	
				input.id = menuItems[no].id + 'input';
	
//				if(!noDrag)
//console.log(aTag.onmousedown);

				aTag.onclick = JSTreeObj.copyDragableNode;

				if(!noChildren)aTag.onmousemove = JSTreeObj.moveDragableNodes;
				if(sourceTree)aTag.onmousedown = JSTreeObj.copyDragableNode;
								
				menuItems[no].insertBefore(img,input);

				var folderImg = document.createElement('IMG');
				
				if(!noDrag){
					if(sourceTree)
						folderImg.onmousedown = JSTreeObj.copyDragableNode;
					else
						folderImg.onmousedown = JSTreeObj.initClick;
				}
				if(!noChildren)folderImg.onmousemove = JSTreeObj.moveDragableNodes;
				
				if(menuItems[no].className){
					folderImg.src = this.imageFolder + menuItems[no].className;
				}else{
					folderImg.src = this.imageFolder + this.folderImage;					
//					folderImg.src = this.imageFolder + this.iconFolder;					
				}
console.log('folderImg.src '+folderImg.src );				 
				if(iconfile)
					folderImg.src = iconfile;
				menuItems[no].insertBefore(folderImg,input);				
			}	
		
			initExpandedNodes = this.Get_Cookie('dhtmlgoodies_expandedNodes');
			if(initExpandedNodes){
				var nodes = initExpandedNodes.split(',');
			}			
			document.documentElement.onmousemove = JSTreeObj.moveDragableNodes;	
			
			document.documentElement.onmouseup = JSTreeObj.dropDragableNodesCopy;
			
			if(sourceTree){
				this.actionOnMouseUp = 'copy';
			}
			else{
				this.actionOnMouseUp = 'move';
			}
		}
	}
