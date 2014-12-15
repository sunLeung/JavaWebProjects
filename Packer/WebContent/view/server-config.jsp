<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<ul id="subNav" class="nav nav-tabs" role="tablist">
  <li role="presentation" class="active"><a href="#" uri="view/server-config-gameserver.jsp">游戏服</a></li>
  <li role="presentation"><a href="#" uri="view/server-config-uploadserver.jsp">资源服</a></li>
</ul>
<div id="subMain"></div>

<script src="lib/Flat-UI-master/js/application.js"></script>
<script>
$(document).ready(function(){
	var uri=$("#subNav .active").find("a").attr("uri");
	$.myload($("#subMain"),uri);
	
	$("#subNav li").on("click",function(){
		$("#subNav li").removeClass("active");
		$(this).addClass("active");
		var uri=$(this).find("a").attr("uri");
		$.myload($("#subMain"),uri);
	});
});
</script>