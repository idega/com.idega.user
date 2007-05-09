var treeUlCounter = 0;

function setLocal(tableId) {
	var table = document.getElementById(tableId);
	table.style.display = 'none';
}

function setRemote(tableId) {
//console.log(tableId);	
	var table = document.getElementById(tableId);
	table.style.display = 'inline';
}

function sendConnectionData(serverId, loginId, passwordId) {
	var serverName = document.getElementById(serverId);
	var login = document.getElementById(loginId);
	var password = document.getElementById(passwordId);
	CalService.setConnectionData(serverName.value, login.value, password.value, empty());
}

function empty(result){}

//	behaviour

function setBehaviour(){
	
	var myrules = {
/*		
		'b.someclass' : function(element){
			element.onclick = function(){
				alert(this.innerHTML);
			}
		},
*/ 
		'#radioBtnLocal input' : function(element){
			element.onclick = function(){
				setLocal('connectionData');
			}
		}
		,
		'#radioBtnRemote input' : function(element){
			element.onclick = function(){
				setRemote('connectionData');
			}
		}
		
	};
	
	Behaviour.register(myrules);	
}
