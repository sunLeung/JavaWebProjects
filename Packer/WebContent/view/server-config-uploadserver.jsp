<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="common.user.UserService"%>
<%@page import="service.serverconfig.UploadServer"%>
<%@page import="service.serverconfig.ServerConfigService"%>
<%@page import="common.user.User"%>
<%@page import="common.user.AuthMap"%>
<%@page import="java.util.List"%>
<%
List<UploadServer> uploadServerList=ServerConfigService.getUploadServers();
User user=(User)session.getAttribute("user");

List<User> users=UserService.getUserList();
%>

<div class="page-header">
  <h6 class="title-font">资源器 <small style="title-font">配置</small></h6>
</div>

<table class="table">
	<tr>
		<td><input id="name" class="form-control" type="text" placeholder="服务器名"/></td>
		<td><input id="user" class="form-control" type="text" placeholder="用户" value="root"/></td>
		<td><input id="password" class="form-control" type="text" placeholder="密码"/></td>
		<td><input id="host" class="form-control" type="text" placeholder="Host"/></td>
	</tr>
	<tr>
		<td><input id="port" class="form-control" type="text" placeholder="Port" value="22"/></td>
		<td><input id="uploadDIR" class="form-control" type="text" placeholder="上传目录" value="/root/GameLog/toser/game"/></td>
		<td colspan="2">
			<input id="select-user" type="button" class="btn btn-primary" value="选择用户"/>
			<input id="create-uploadserver" type="button" class="btn btn-success" value="创建服务器"/>
		</td>
	</tr>
</table>

<div id="user-content" class="well" style="display: none;">
	<%for(User u:users){%>
		<label style="border: 1px dotted;margin: 5px;padding:0 5px;">
			<input type="checkbox" name="power" value="<%=u.getId() %>"/>
			<%=u.getName() %>
		</label>
	<%} %>
</div>

<table class="table table-striped table-bordered table-hover">
	<tr> 
		<td>序号</td>
		<td>服务器</td>
		<td>Host</td>
		<td>操作</td>
	</tr>
<%if(uploadServerList!=null && uploadServerList.size()>0){%>
	<%for(UploadServer u:uploadServerList){%>
		<tr> 
			<td><%=u.getId() %></td>
	        <td><%=u.getName() %></td>
	        <td><%=u.getHost() %></td>
	       	<td>
	    		<a href="#" name="update-uploadserver" serverid="<%=u.getId() %>">修改 </a>
				|<a href="#" name="del-uploadserver" serverid="<%=u.getId() %>" servername="<%=u.getName() %>">删除 </a>
	    	</td>
	      </tr>
	<%} %>
<%}else{%>
	<tr> 
		<td colspan="4">无数据</td>
	</tr>
<%} %>
</table>

<!-- 修改用户详细信息 -->
<div id="update-server-panel" class="modal fade" serverid="">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title">修改服务器信息</h4>
      </div>
      <div class="modal-body">
         <table class="table table-bordered table-hover" style="margin-bottom: 5px;">
		  <tr>
		    <td>服务器名</td>
		    <td><input id="serverpanel_name" type="text" class="form-control"></td>
		  </tr>
		  <tr>
		    <td>用户</td>
		    <td><input id="serverpanel_user" type="text" class="form-control"></td>
		  </tr>
		  <tr>
		    <td>密码</td>
		    <td><input id="serverpanel_password" type="text" class="form-control"></td>
		  </tr>
		  <tr>
		    <td>Host</td>
		    <td><input id="serverpanel_host" type="text" class="form-control"></td>
		  </tr>
		  <tr>
		    <td>Port</td>
		    <td><input id="serverpanel_port" type="text" class="form-control"></td>
		  </tr>
		  <tr>
		    <td>上传目录</td>
		    <td><input id="serverpanel_uploadDIR" type="text" class="form-control"></td>
		   </tr>
		</table>
		
		<div class="well">
			<%for(User u:users){%>
				<label style="border: 1px dotted;margin: 5px;padding:0 5px;">
					<input type="checkbox" name="serverpanel_users" value="<%=u.getId() %>"/>
					<%=u.getName() %>
				</label>
			<%} %>
		</div>
		
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
        <button id="udpate-server-submit" type="button" class="btn btn-success">修改</button>
      </div>
    </div>
  </div>
</div>

<script src="lib/Flat-UI-master/js/application.js"></script>
<script>
$(document).ready(function(){
	
	$("#select-user").on("click",function(){
		$("#user-content").toggle();
	});
	
	$("#create-uploadserver").on("click",function(){
		var name=$("#name").val();
		var user=$("#user").val();
		var password=$("#password").val();
		var host=$("#host").val();
		var port=$("#port").val();
		var uploadDIR=$("#uploadDIR").val();
		var userids="";
		$("input[name='power']:checkbox:checked").each(function(){
			userids+=$(this).val()+',';
		});

		if(name==""||user==""||password==""||host==""||uploadDIR==""){
			$.myalert("所有填写项不能为空");
			return;
		}
		
		$.myconfirm("确定创建: ["+name+"] ?",function(){
			var requestData={name:name,user:user,password:password,host:host,port:port,uploadDIR:uploadDIR,userids:userids};
			$.post("server-config/create-uploadserver.do",requestData,function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				if(json.code==0){
					$.myload($("#subMain"),"view/server-config-uploadserver.jsp");
				}
			},"json");
		});
	});
	
	$("a[name=update-uploadserver]").on("click",function(){
		var panel=$("#update-server-panel");
		var id=$(this).attr("serverid");
		var requestData={id:id};
		$.post("server-config/get-uploadserver.do",requestData,function(data, textStatus, jqXHR){
			var json=data;
			if(json.code==0){
				panel.attr("serverid",json.data.id);
				panel.find("#serverpanel_name").val(json.data.name);
				panel.find("#serverpanel_user").val(json.data.user);
				panel.find("#serverpanel_password").val(json.data.password);
				panel.find("#serverpanel_host").val(json.data.host);
				panel.find("#serverpanel_port").val(json.data.port);
				panel.find("#serverpanel_uploadDIR").val(json.data.uploadDIR);
				
				$("input[name='serverpanel_users']").removeAttr("checked");
				$.each(json.data.users, function (i, item) {
					$("input[name='serverpanel_users']").each(function (i, subitem) {
						if($(subitem).val()==item){
							$(subitem).prop("checked",true);
						}
					});
		        });
				
				panel.modal("show");
			}else{
				$.myalert(json.data);
			}
		},"json");
	});
	
	$("#udpate-server-submit").on("click",function(){
		var panel=$("#update-server-panel");
		var id=panel.attr("serverid");
		var name=$("#serverpanel_name").val();
		var user=$("#serverpanel_user").val();
		var password=$("#serverpanel_password").val();
		var host=$("#serverpanel_host").val();
		var port=$("#serverpanel_port").val();
		var uploadDIR=$("#serverpanel_uploadDIR").val();
		var userids="";
		$("input[name='serverpanel_users']:checkbox:checked").each(function(){
			userids+=$(this).val()+',';
		});

		if(name==""||user==""||password==""||host==""||uploadDIR==""){
			$.myalert("所有填写项不能为空");
			return;
		}
		
		panel.off("hidden.bs.modal");
		panel.on("hidden.bs.modal",function (e) {
			$.myload($("#subMain"),"view/server-config-uploadserver.jsp");
			e.preventDefault();
		});
		
		$.myconfirm("确定修改: ["+name+"] ?",function(){
			var requestData={id:id,name:name,user:user,password:password,host:host,port:port,uploadDIR:uploadDIR,userids:userids};
			$.post("server-config/update-uploadserver.do",requestData,function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				if(json.code==0){
					$.myload($("#subMain"),"view/server-config-uploadserver.jsp");
					panel.modal("hide");
					$(".modal-backdrop").hide();
				}
			},"json");
		});
	});
	
	$("a[name=del-uploadserver]").on("click",function(){
		var id=$(this).attr("serverid");
		var name=$(this).attr("servername");
		$.myconfirm("确定删除？: ["+name+"] ?",function(){
			var requestData={id:id};
			$.post("server-config/delete-uploadserver.do",requestData,function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				if(json.code==0){
					$.myload($("#subMain"),"view/server-config-uploadserver.jsp");
				}
			},"json");
		});
	});

});
</script>