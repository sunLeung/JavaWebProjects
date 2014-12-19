部署：
1.tomcat8
2.删除tomcat/lib中的websocket相关的包，因为项目已经引入了标准的websocket包
3.修改webapp/scripts/ws.js wsConfig.baseURL填写正确的部署服务器路径

项目简要说明：
1.使用了servlet3.0
2.使用编程式即唔web.xml servlet配置
3.使用了springMVC
4.使用了tomcat websocket
4.前端使用了bootstrap、flat-ui

项目部署打包：
1.ant配置需要把当前文件夹的
	jdtCompilerAdapter.jar
	org.eclipse.jdt.compiler.tool_1.0.300.v20140311-1758.jar
	org.eclipse.jdt.core_3.10.0.v20140604-1726.jar
	org.eclipse.jdt.debug.ui_3.6.300.v20140512-1926.jar
	以上文件copy到ant安装目录的lib
2.svn检出项目，把自动打包shell脚本（当前目录下的manualpacker.sh）copyd到项目跟目录中，并修改脚本中的相应项目参数
3.把当前目录下的（build.xml）copy到项目游戏目录覆盖相应的打包文件
4.把当前目录下的（atuo.sh run.sh stop.sh） copy到相应的服务器中
	