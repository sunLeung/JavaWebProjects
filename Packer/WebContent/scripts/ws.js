var wsConfig={
	baseURL:'ws://127.0.0.1:8080/Packer/ws/'
}

function createWS(op,onmessage) {
	console.log("createWS");
	var ws = null;
	
	if ('WebSocket' in window)
		ws = new WebSocket(wsConfig.baseURL+op);
	else if ('MozWebSocket' in window)
		ws = new MozWebSocket(wsConfig.baseURL+op);
	else
		alert("Do not use so cheap browser.Ok?Download Chrome or FireFox.");

	ws.onmessage = onmessage;

	ws.onclose = function(evt) {
		console.log("close");
	};

	ws.onopen = function(evt) {
		console.log("open");
	};
}

function appendConsole(data){
	var content=data.content;
	var gameserverid=data.gameserverid;
	$('#gameserver-panel[serverid='+gameserverid+']').find("#out-put-area").append("<p>"+content+"<p>");
	$('.content-box').scrollTop( $('.content-box')[0].scrollHeight );
}

function test(){
	$('#gameserver-panel[serverid='+1+']').find("#out-put-area").append("<p>"+'123456'+"<p>");
}

function fuck(data){
	console.log("hi fuck");
}