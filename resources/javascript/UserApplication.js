if (!UserApplication) var UserApplication = {};

UserApplication.doExecute = function(message, event, formId, action) {
	if (message != null) {
		if (!window.confirm(message)) {
			return false;
		}
	}
	
	var form = jQuery('#' + formId);
	var targetName = form.attr('target');
	var target = UserApplication.findTarget(window, targetName);
	target = target == null ? UserApplication.findTarget(window.opener, targetName) : target;
	if (target == null) {
		return false;
	}
	
	var bean = {index: 0, link: target.location.pathname};
	jQuery('input', form).each(function() {
		var input = jQuery(this);
		var include = true;
		if (input.attr('type') == 'radio' || input.attr('type') == 'checkbox') {
			include = input.attr('checked') == 'checked';
		}
		
		if (include) {
			var attrName = input.attr('name');
			var attrValue = input.attr('value');
			bean = UserApplication.addParameter(bean, attrName, attrValue);
		}
	});
	jQuery('select', form).each(function() {
		var select = jQuery(this);
		
		var attrName = select.attr('name');
		var attrValue = dwr.util.getValue(select.attr('id'));
		bean = UserApplication.addParameter(bean, attrName, attrValue);
	});
	
	bean = UserApplication.addParameter(bean, action, 'true');
	
	showLoadingMessage('');
	target.location.href = bean.link;
	
	if (event == null) {
		return false;
	} else {
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.cancelBubble = true;
	}
}

UserApplication.search = function(event, formId) {
	if (formId == null) {
		return false;
	}
	
	var form = jQuery('#' + formId);
	var targetName = form.attr('target');
	var target = UserApplication.findTarget(window, targetName);
	target = target == null ? UserApplication.findTarget(window.opener, targetName) : target;
	if (target == null) {
		return false;
	}
	
	var bean = {index: 0, link: target.location.pathname};
	jQuery('input', form).each(function() {
		var input = jQuery(this);
		
		var attrName = input.attr('name');
		var attrValue = input.attr('value');
		bean = UserApplication.addParameter(bean, attrName, attrValue);
	});
	jQuery('select', form).each(function() {
		var select = jQuery(this);
		
		var attrName = select.attr('name');
		var attrValue = dwr.util.getValue(select.attr('id'));
		bean = UserApplication.addParameter(bean, attrName, attrValue);
	});
	
	target.location.href = bean.link;
	
	if (event == null) {
		return false;
	} else {
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.cancelBubble = true;
	}
}

UserApplication.addParameter = function(bean, name, value) {
	var index = bean.index;
	var src = bean.link;
	if (name != null && name != '' && value != null && value != '') {
		if (index == 0) {
			src += '?';
		} else {
			src += '&';
		}
		src += name + '=' + value;
		
		index++;
	}
	return {index: index, link: src};
}

UserApplication.findTarget = function(win, targetName) {
	if (win == null || targetName == null) {
		return null;
	}

	var frame = win.frames[targetName];
	if (frame == null) {
		var parentWin = win.parent;
		return parentWin == win ? null : UserApplication.findTarget(parentWin, targetName);
	}
	
	return frame;
}