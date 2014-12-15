<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="service.projectmanager.ProjectService"%>
<%@page import="service.projectmanager.Project"%>
<%@page import="common.user.User"%>
<%@page import="common.user.Group"%>
<%@page import="common.user.UserService"%>
<%@page import="common.user.AuthMap"%>
<%@page import="java.util.List"%>
<%
User user=(User)session.getAttribute("user");
List<Project> projects=ProjectService.getProjectList();
List<User> users=UserService.getUserList();
%>

<div class="page-header">
  <h6 class="title-font">项目 <small style="title-font">配置</small></h6>
</div>

<table class="table table-striped table-bordered table-hover">
	<tr> 
		<td>序号</td>
		<td>项目名</td>
		<td>操作</td>
	</tr>
<%if(projects!=null && projects.size()>0){%>
	<%for(Project p:projects){%>
		<tr> 
			<td><%=p.getId() %></td>
	        <td><%=p.getName() %></td>
	       	<td>
	    		<a href="#" name="update-project" projectid="<%=p.getId() %>">修改 </a>
	    	</td>
	      </tr>
	<%} %>
<%}else{%>
	<tr> 
		<td colspan="3">无数据</td>
	</tr>
<%} %>
</table>

<!-- 修改项目详细信息 -->
<div id="update-project-panel" class="modal fade" projectid="">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title">修改项目</h4>
      </div>
      <div class="modal-body">
         <table class="table table-bordered table-hover" style="margin-bottom: 5px;">
		  <tr>
		    <td>项目ID</td>
		    <td><span id="projectpanel_id"></span></td>
		  </tr>
		  <tr>
		    <td>项目名</td>
		    <td><span id="projectpanel_name"></span></td>
		  </tr>
		</table>
		<div class="well">
			<%for(User u:users){%>
				<label style="border: 1px dotted;margin: 5px;padding:0 5px;">
					<input type="checkbox" name="projectpanel_users" value="<%=u.getId() %>"/>
					<%=u.getName() %>
				</label>
			<%} %>
		</div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
        <button id="udpate-project-submit" type="button" class="btn btn-success">修改</button>
      </div>
    </div>
  </div>
</div>

<script src="lib/Flat-UI-master/js/application.js"></script>
<script>
$(document).ready(function(){
	$("a[name=update-project]").on("click",function(){
		var panel=$("#update-project-panel");
		var id=$(this).attr("projectid");
		var requestData={id:id};
		$.post("project-config/get-project.do",requestData,function(data, textStatus, jqXHR){
			var json=data;
			if(json.code==0){
				panel.attr("projectid",json.data.id);
				panel.find("#projectpanel_id").text(json.data.id);
				panel.find("#projectpanel_name").text(json.data.name);
				
				$("input[name='projectpanel_users']").removeAttr("checked");
				$.each(json.data.users, function (i, item) {
					$("input[name='projectpanel_users']").each(function (i, subitem) {
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
	
	$("#udpate-project-submit").on("click",function(){
		var panel=$("#update-project-panel");
		var id=panel.attr("projectid");
		var userids="";
		$("input[name='projectpanel_users']:checkbox:checked").each(function(){
			userids+=$(this).val()+',';
		});

		panel.off("hidden.bs.modal");
		panel.on("hidden.bs.modal",function (e) {
			$.myload($(".main"),"view/project-config.jsp");
			e.preventDefault();
		});
		
		$.myconfirm("确定修改?",function(){
			var requestData={id:id,userids:userids};
			$.post("project-config/update-project.do",requestData,function(data, textStatus, jqXHR){
				var json=data;
				$.myalert(json.data);
				console.log("adsfd");
				if(json.code==0){
					$.myload($(".main"),"view/project-config.jsp");
					panel.modal("hide");
					$(".modal-backdrop").hide();
				}
			},"json");
		});
	});
	
});
</script>