/**
 * 
 */
var UserAutocomplete = {};

UserAutocomplete.createAutocomplete = function(inputId,max,name){
	if(max == undefined){
		max = 5;
	}
	var input = jQuery("#"+inputId);
	input.tagedit({
		autocompleteURL: function(request, response) {
			UserAutocompleteBean.getAutocompletedItems(request.term,max,name, {
				callback: function(userDataCollection) {
					response(userDataCollection);
				}
			});
		},
		allowEdit: true,
		allowAdd: true,
		delay: 100,
		autocompleteOptions: {
			minLength : 1, //Change to 3 to start from 3 letters if you want
			html: true,
			select: function( event, ui ) {
				jQuery("#"+inputId).val(ui.item.value).trigger('transformToTag', [ui.item.id, ui.item.label]);
				return false;
			}
		},
		transform : function(event, id, label) {
			var obj = jQuery("#"+inputId).data("tag-options-data");
			var oldValue = (typeof id != 'undefined' && id.length > 0);

			if(label == undefined){
				var request  = jQuery("#"+inputId).val();
				UserAutocompleteBean.getAutocompletedItems(request, 2, {
					callback: function(userDataCollection) {
						if(userDataCollection.length == 1){
							var theOneGroup = userDataCollection[0];
							jQuery("#"+inputId).trigger('transformToTag', [id,theOneGroup.label ]);
						}
						else{
//							humanMsg.displayMsg(UserAutocomplete.nontrivialUserDefiningPhraseErrorMsg);
							jQuery("#"+inputId).focus();
						}
					}
				});
				return false;
			}
			var checkAutocomplete = oldValue == true? false : true;
			// check if the Value ist new
			var isNewResult = obj.isNew(label, checkAutocomplete);
			if(isNewResult[0] === true || (isNewResult[0] === false && typeof isNewResult[1] == 'string')) {

				if(oldValue == false && typeof isNewResult[1] == 'string') {
					oldValue = true;
					id = isNewResult[1];
				}

				if(obj.options.allowAdd == true || oldValue) {
					// Make a new tag in front the input
					html = '<li class="tagedit-listelement tagedit-listelement-old">';
					html += '<span dir="'+obj.options.direction+'">' + label + '</span>';
					html += "<input type='hidden' name = 'tag[]' disabled='disabled'" + " value=\""+ jQuery(label).find("input:hidden").val()+"\" />";
					html += '<a class="tagedit-close" title="'+obj.options.texts.removeLinkTitle+'">x</a>';
					html += '</li>';

					jQuery(this).parent().before(html);
				}
			}
			jQuery(this).val('');

			// close autocomplete
			if(obj.options.autocompleteOptions.source) {
				jQuery(this).autocomplete( "close" );
			}

		}
	});
}