<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="common.user.User"%>
<%@page import="service.serverconfig.ServerConfigService"%>
<%@page import="service.serverconfig.GameServer"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="service.gamemanager.GameManagerService"%>
<%
User user=(User)session.getAttribute("user");
List<GameServer> gsList=ServerConfigService.getGameServers(user);
%>

<div class="page-header">
  <h6 class="title-font">游戏服 <small style="title-font">管理</small></h6>
</div>

<div class="container-fluid">
  <div class="row">
  <%if(gsList!=null&&gsList.size()>0){for(GameServer g:gsList){%>
  	<div style="float: left;margin: 0 20px 20px 0;">
		<div class="tile" style="width: 186px;">
			<img class="tile-image big-illustration" alt="" src="img/servericon.jpg">
			<h3 class="tile-title"><%=g.getName() %></h3>
			<p></p>
			<button name="btn-open-server-pannel" gameserverid="<%=g.getId() %>" class="btn btn-primary btn-large btn-block">打开</button>
		</div>
	</div>
  <%}}else{%>
	<p>暂无配置游戏服务器</p>
  <%} %>
  </div>
</div>

<!-- 游戏控制面板 -->
<div id="gameserver-panel" class="modal fade" serverid="">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title">服务器控制面板</h4>
      </div>
      <div class="modal-body">
      	<div>
			<div class="btn-group">
				<button id="btn-start-server" data-loading-text="开服中..." class="btn btn-primary" disabled="disabled">开服</button>
				<button id="btn-stop-server" data-loading-text="关服中..." class="btn btn-primary" disabled="disabled">关服</button>
				<button id="btn-force-stop-server" data-loading-text="强制关服中..." class="btn btn-danger" disabled="disabled">强制关服</button>
			</div>
		</div>
		<div id="console-pannel" style="margin-top:20px;">
			<strong>console</strong>
			<div class="content-box" style="height: 300px;overflow-y: auto;">
				<strong>服务器状态：</strong><strong id="server-state"></strong>
				<div id="out-put-area"></div>
			</div>
		</div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
      </div>
    </div>
  </div>
</div>

 
<script src="lib/Flat-UI-master/js/application.js"></script>
<script>
$(document).ready(function(){
	
	$("button[name='btn-open-server-pannel']").on("click",function(){
		var gameserverid=$(this).attr("gameserverid");
		var panel=$("#gameserver-panel");
		panel.attr("serverid",gameserverid);
		panel.find("#out-put-area").empty();
		$("#loading").show();
		$.post("gamemanager/create-sshconnect.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
			var json=data;
			$.myalert(json.data);
			if(json.code==0){
				panel.modal({
					backdrop:"static",
					show:true
				});
				checkServerState(gameserverid);
				//创建WS
				createWS("gameManager:gameserverid:"+gameserverid,function(evt){
					var data=jQuery.parseJSON(evt.data);
					console.log(data);
					var content=data.content;
					var gameserverid=data.gameserverid;
					$('#gameserver-panel[serverid='+gameserverid+']').find("#out-put-area").append("<p>"+content+"<p>");
					$('.content-box').scrollTop( $('.content-box')[0].scrollHeight );
				});
			}
			$("#loading").hide();
		},"json");
	});
	
	function checkServerState(gameserverid){
		$.post("gamemanager/check-server-state.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
			var json=data;
			if(json.code==0){
				if(json.data.state==-1){//检测失败
					$("#btn-start-server").attr('disabled',"true");
					$("#btn-stop-server").attr('disabled',"true");
					$("#btn-force-stop-server").attr('disabled',"true");
				}
				if(json.data.state==0){//已停服
					$("#btn-start-server").removeAttr("disabled");
					$("#btn-stop-server").attr('disabled',"true");
					$("#btn-force-stop-server").attr('disabled',"true");
				}
				if(json.data.state==1){//已开服
					$("#btn-start-server").attr('disabled',"true");
					$("#btn-stop-server").removeAttr("disabled");
					$("#btn-force-stop-server").removeAttr("disabled");
				}
				$("#server-state").text(json.data.msg);
			}else{
				$.myalert(json.data);
				$("#out-put-area").append(json.data);
			}
		},"json");
	}
	
	$("#gameserver-panel").on('hide.bs.modal', function (e) {
		var gameserverid=$(this).attr("serverid");
		console.log("gameserverid:"+gameserverid);
		$.post("gamemanager/close-sshconnect.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
			var json=data;
			$.myalert(json.data);
		},"json");
	});
	
	$("#btn-start-server").on("click",function(){
		var gameserverid=$("#gameserver-panel").attr("serverid");
		$.myconfirm("确定开服？",function(){
		    var $btn = $("#btn-start-server").button('loading');
		    $("#out-put-area").append("<p>正在开服...</p>");
			$.post("gamemanager/start-server.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
				var json=data;
				//$("#out-put-area").append("<p>"+json.data+"<p>");
				$btn.button("reset");
				checkServerState(gameserverid);
			},"json");
		});
	});
	
	$("#btn-stop-server").on("click",function(){
		var gameserverid=$("#gameserver-panel").attr("serverid");
		$.myconfirm("确定关服？",function(){
		    var $btn = $("#btn-stop-server").button('loading');
		    $("#out-put-area").append("<p>正在关服...</p>");
		    $('.content-box').scrollTop( $('.content-box')[0].scrollHeight );
			$.post("gamemanager/stop-server.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
				var json=data;
				$("#out-put-area").append("<p>"+json.data+"<p>");
				$btn.button("reset");
				checkServerState(gameserverid);
			},"json");
		});
	});
	
	$("#btn-force-stop-server").on("click",function(){
		var gameserverid=$("#gameserver-panel").attr("serverid");
		$.myconfirm("确定强制关服？",function(){
		    var $btn = $("#btn-force-stop-server").button('loading');
		    $("#out-put-area").append("<p>正在强制关服...</p>");
		    $('.content-box').scrollTop( $('.content-box')[0].scrollHeight );
			$.post("gamemanager/forcestop-server.do",{gameserverid:gameserverid},function(data, textStatus, jqXHR){
				var json=data;
				$("#out-put-area").append("<p>"+json.data+"<p>");
				$btn.button("reset");
				checkServerState(gameserverid);
				$('.content-box').scrollTop( $('.content-box')[0].scrollHeight );
			},"json");
		});
	});

});
</script>