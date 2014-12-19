<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="common.user.User"%>
<%@page import="service.serverconfig.ServerConfigService"%>
<%@page import="service.packer.PackerService"%>
<%@page import="service.serverconfig.UploadServer"%>
<%@page import="service.projectmanager.ProjectService"%>
<%@page import="service.projectmanager.Project"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%
User user=(User)session.getAttribute("user");
List<Project> projectList=ProjectService.getProjectList(user);
String projectid=request.getParameter("id");
List<Map<String,String>> plist=PackerService.getUserPackageList(projectid, session);
List<UploadServer> ulist=ServerConfigService.getUploadServers(user);
%>
<div class="page-header">
  <h6 class="title-font">打包 <small style="title-font">管理</small></h6>
</div>

<div>
	<select id="select-project" class="select">
		<option value="-1">选择项目</option>
	<%if(projectList!=null&&projectList.size()>0){for(Project p:projectList){if(projectid!=null&&projectid.equals(p.getId()+"")){ %>
		<option value="<%=p.getId() %>" selected="selected"><%=p.getName() %></option>
	<%}else{%>
		<option value="<%=p.getId() %>"><%=p.getName() %></option>
	<%}}}else{%>
		<option>无项目配置</option>
	<%} %>
	</select>
	<button id="btn-pack" data-loading-text="打包中..." class="btn btn-primary">打包</button>
</div>

<div class="content-box">
    <strong>打包机状态:</strong>
    <p>打包队列0个</p>
</div>

<hr>
<select id="select-project-package" class="select">
	<option value="-1">选择项目</option>
<%if(projectList!=null&&projectList.size()>0){for(Project p:projectList){if(projectid!=null&&projectid.equals(p.getId()+"")){ %>
	<option value="<%=p.getId() %>" selected="selected"><%=p.getName() %></option>
<%}else{%>
	<option value="<%=p.getId() %>"><%=p.getName() %></option>
<%}}}else{%>
	<option>无项目配置</option>
<%} %>
</select>
<button id="btn-show-pack" class="btn btn-primary">查看</button>
<table id="uploaded-files" style="margin-top:5px;" class="table table-striped table-bordered table-hover">
    <tr>
        <th>ID</th>
        <th>时间</th>
        <th>大小</th>
        <th>选择服务器</th>
        <th>操作</th>
    </tr>
    <%if(plist!=null&&plist.size()>0){ for(Map<String,String> map:plist){%>
    	<tr>
    		<td><%=map.get("id") %></td>
    		<td><%=map.get("time") %></td>
    		<td>game: <%=map.get("gameSize") %><br>res: <%=map.get("resSize") %></td>
    		<td>
    			<select id="select-uploadserver-<%=map.get("id") %>" class="select">
				<%if(ulist!=null&&ulist.size()>0){for(UploadServer u:ulist){%>
					<option value="<%=u.getId() %>"><%=u.getName() %></option>
				<%}}else{%>
					<option>无资源服</option>
				<%} %>
				</select>
    		</td>
    		<td><button name="btn-upload" data-loading-text="上传中..." resourceid="<%=map.get("id") %>" projectid="<%=projectid %>" class="btn btn-success">上传</button><span></span></td>
    	</tr>
    <%} }else{%>
    	<tr>
    		<td colspan="5">暂无数据</td>
    	</tr>
    <% } %>
</table>

<script src="lib/bootstrap-3.2.0-dist/js/bootstrap.min.js"></script>
<script src="lib/Flat-UI-master/js/application.js"></script>
<script>
$(document).ready(function(){

	$("#btn-pack").on("click",function(){
		var project=$("#select-project");
		var projectid=project.val();
		//var projectName=project.text();
		console.log("id:"+projectid);
		//console.log("name:"+projectName);
		$.myconfirm("确定打包？",function(){
		    var $btn = $("#btn-pack").button('loading');
			$.post("packer/pack.do",{projectid:projectid},function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				$btn.button("reset");
				$.myload($(".main"),"view/packer-manager.jsp?id="+projectid);
			},"json");
		});
	});

	$("#btn-show-pack").on("click",function(){
		var project=$("#select-project-package");
		var projectid=project.val();
		if(projectid!="-1")
			$.myload($(".main"),"view/packer-manager.jsp?id="+projectid);
	});
	
	$("button[name='btn-upload']").on("click",function(){
		var $btn=$(this);
		var resourceid=$btn.attr("resourceid");
		var projectid=$btn.attr("projectid");
		var uploadserverid=$("#select-uploadserver-"+resourceid).val();
		$.myconfirm("确定上传？",function(){
		    $btn.button('loading');
		    createWS('packer:uploadfile:'+uploadserverid,function(evt){
		    	var data=jQuery.parseJSON(evt.data);
				console.log(data);
				var file=data.file;
				var percent=data.percent;
				$btn.next().text(file+":"+percent);
		    });
			$.post("packer/upload.do",{projectid:projectid,resourceid:resourceid,uploadserverid:uploadserverid},function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				$btn.next().text(json.data);
				$btn.button("reset");
			},"json");
		});
	});
});
</script>