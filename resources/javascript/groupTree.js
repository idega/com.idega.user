	function setGroupsNodes(nodes, id, styleClassName, selectedGroups) {
		closeAllLoadingMessages();

		var groupsList = new Element('ul');		
		groupsList.setProperty('start', true);
		addTreeElements(nodes, groupsList, styleClassName, selectedGroups);
		groupsList.addClass('tree_drag_drop');
		groupsList.setProperty('id', 'tree');
		
		var container = $(id);
		if (container != null) {
			container.empty();
			groupsList.injectInside(container);
		}
		
		initGroupsTree(selectedGroups);
		
		var firstLevelChildren = groupsList.getChildren();
		if (firstLevelChildren == null) {
			return false;
		}
		var firsLevelChild = null;
		for (var i = 0; i < firstLevelChildren.length; i++) {
			firsLevelChild = firstLevelChildren[i];
			if (firsLevelChild) {
				if (firsLevelChild.getTag() == 'li' && firsLevelChild.hasClass('groupsTreePartNode')) {
					openOrCloseGroupTreeListPart(firsLevelChild.getFirst().getFirst(), true);
				}
			}
		}
	}
	
	function addTreeElements(nodes, rootUl, styleClassName, selectedGroups) {
		if (nodes == null) {
			return false;
		}
		
		var node = null;
		for (var i = 0; i < nodes.length; i++) {
			node = nodes[i];
			var groupIsSelected = isGroupSelected(selectedGroups, node.uniqueId);
			
			var liElement = new Element('li');
			liElement.addClass('groupsTreePartNode');
			liElement.setProperty('id', node.uniqueId + 'li');
			
			var imageAndSpanContainer = new Element('div');
			imageAndSpanContainer.injectInside(liElement);
			
			var image = new Element('img');
			var imgSrc = '/idegaweb/bundles/com.idega.user.bundle/resources/images/nav-minus.gif';
			if (node.hasChildren) {
				imgSrc = '/idegaweb/bundles/com.idega.user.bundle/resources/images/nav-plus.gif';
			}
			else {
				image.setStyle('visibility', 'hidden');
			}
			image.setProperty('opened',  false);
			image.setProperty('src', imgSrc);
			image.setProperty('id', 'id' + new Date().getTime());
			image.addClass('groupsTreeExpanderCollapserImageStyle');
			image.injectInside(imageAndSpanContainer);
			
			var groupName = new Element('span');
			groupName.setProperty('id', node.uniqueId);
			if (styleClassName != null) {
				groupName.addClass(styleClassName);
			}
			var fontVariant = 'normal';
			if (groupIsSelected) {
				fontVariant = 'bold';
			}
			groupName.setStyle('font-weight', fontVariant);
			groupName.appendText(node.name);
			groupName.injectInside(imageAndSpanContainer);
			
			if (node.hasChildren) {
				var otherListContainer = new Element('div');
				otherListContainer.injectInside(liElement);
				
				var ulElement = new Element('ul');
				ulElement.setProperty('start', false);
				addTreeElements(node.children, ulElement, styleClassName, selectedGroups);
				ulElement.addClass('tree_drag_drop');
				ulElement.injectInside(otherListContainer);
				
				otherListContainer.setStyle('display', 'none');
			}
			
			liElement.injectInside(rootUl);
		}
		
		return true;
	}
	
	function isGroupSelected(selectedGroups, id) {
		if (selectedGroups == null || id == null) {
			return false;
		}
		for (var i = 0; i < selectedGroups.length; i++) {
			if (id == selectedGroups[i]) {
				return true;
			}
		}
		return false;
	}
	
	function initGroupsTree(selectedGroups) {
		$$('img.groupsTreeExpanderCollapserImageStyle').each(
			function(image) {
				image.addEvent('click', function() {
					openOrCloseGroupTreeListPart(image, false);
				});
			}
		);
		
		$$('img.groupsTreeExpanderCollapserImageStyle').each(
			function(image) {
				var spanElement = image.getNext();
				if (spanElement) {
					if (isGroupSelected(selectedGroups, spanElement.id)) {
						openOrCloseGroupTreeListPart(image, true);
						
						openAllParentGroupsTreeParts(image);
					}
				}
			}
		);
	}
	
	function openAllParentGroupsTreeParts(image) {
		var imageAndSpanDiv = image.getParent();
		if (imageAndSpanDiv == null) {
			return false;
		}
		if (imageAndSpanDiv.getTag() != 'div') {
			return false;
		}
		
		var liElement = imageAndSpanDiv.getParent();
		if (liElement == null) {
			return false;
		}
		if (liElement.getTag() != 'li') {
			return false;
		}
		
		var ulElement = liElement.getParent();
		if (ulElement == null) {
			return false;
		}
		if (ulElement.getTag() != 'ul') {
			return false;
		}
		if (!ulElement.hasClass('tree_drag_drop')) {
			return false;
		}
		if (ulElement.getProperty('start') == 'true') {
			return false;
		}
				
		var nextImage = null;
		try {
			nextImage = ulElement.getParent().getPrevious().getFirst();
		} catch(e) {}
		if (nextImage == null) {
			return false;
		}
		if (nextImage.getTag() != 'img') {
			return false;
		}
		if (nextImage.hasClass('groupsTreeExpanderCollapserImageStyle')) {
			openOrCloseGroupTreeListPart(nextImage, true);
			
			openAllParentGroupsTreeParts(nextImage);
		}
	}
	
	function openOrCloseGroupTreeListPart(image, forceOpen) {
		var lastElement = image.getParent().getNext();
		if (lastElement) {
			var needOpen = image.getProperty('opened') == 'false';
			if (forceOpen) {
				needOpen = true;
			}
		
			if (needOpen) {
				image.setProperty('opened',  true);
				image.setProperty('src', '/idegaweb/bundles/com.idega.user.bundle/resources/images/nav-minus.gif');
				lastElement.setStyle('display', 'inline');
			}
			else {
				image.setProperty('opened',  false);
				image.setProperty('src', '/idegaweb/bundles/com.idega.user.bundle/resources/images/nav-plus.gif');
				lastElement.setStyle('display', 'none');
			}
		}
	}