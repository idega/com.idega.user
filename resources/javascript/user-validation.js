/**
 * jQuery validate extend for validating user
 * requires:
 * 		jQuery
 * 		jQuery validate
 * 		UserApplicationEngine.js (dwr)
 * Method init should called at first.
 */
var userValidationHelper ={
		options : {
			userId : function(){
				return null;
			},
			locale : {
				LOGIN_NAME_ALREADY_EXISTS : "User login already exists"
			},
			passwordInput : function(){
				return jQuery();
			},
			loginInput : function(){
				return jQuery();
			}
		},
		isBlank : function(str) {
		    return (!str || /^\s*$/.test(str));
		},
		init : function(opts){
			userValidationHelper.options = jQuery.extend(true,{},userValidationHelper.options,opts);
			jQuery.validator.addMethod("validate-login-existance", function(value, element,arg) {
				var valid = false;
				UserApplicationEngine.getUserIdByLogin(value,{
					callback:function(reply) {
						if(!reply || (reply < 0)){
							valid = true;
							return;
						}
						var vUserId = userValidationHelper.options.userId();
						if(!vUserId || (vUserId < 0)){
							valid = false;
							return;
						}
						if(reply == vUserId){
							valid = true;
							return;
						}
						valid = false;
						return;
					},
					async:false
				});
				return valid;
			},userValidationHelper.options.locale.LOGIN_NAME_ALREADY_EXISTS);
			var password = userValidationHelper.options.passwordInput();
			password.keyup(function(){
				jQuery(this).data('uv-input-changed',true);
			});
			userValidationHelper.options.loginInput().keyup(function(){
				jQuery(this).data('uv-input-changed',true);
			});
		},
		isValidateUserPassword : function(input){
			var userId = userValidationHelper.options.userId();
			if(!userId || (userId < 0)){
				return true;
			}
			var password = jQuery(input);
			if(userValidationHelper.isInputChanged(password)){
				return true;
			}
			return false;
		},
		isInputChanged : function(input){
			return input.data('uv-input-changed') == true;
		}
};
