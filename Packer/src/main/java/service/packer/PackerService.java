package service.packer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import service.projectmanager.Project;
import service.projectmanager.ProjectService;
import service.serverconfig.ServerConfigService;
import service.serverconfig.UploadServer;
import common.user.User;
import common.utils.CalcUtils;
import common.utils.FileUtils;
import common.utils.JsonRespUtils;
import common.utils.SSHUtils;
import common.utils.StringUtils;
import common.utils.TimerManagerUtils;

@Controller
@RequestMapping("packer")
public class PackerService {
	
	@RequestMapping(value ="pack",method = RequestMethod.POST)
	@ResponseBody
	public String packProject(
			@RequestParam(value = "projectid") String projectid,HttpSession session) {
		try {
			if(StringUtils.isBlack(projectid)){
				return JsonRespUtils.fail("请选择打包项目");
			}
			User user=(User)session.getAttribute("user");
			if(user==null){
				return JsonRespUtils.fail("请重新登录");
			}
			Project project=ProjectService.projectContent.get(Integer.valueOf(projectid));
			if(project==null){
				return JsonRespUtils.fail("获取项目失败");
			}
			//检查用户目录
			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
			String userBuildDIR=ProjectService.buildDIR+user.getName()+File.separator+project.getBuildFolder()+File.separator+sdf.format(new Date());
			System.out.println("userBuildDIR:"+userBuildDIR);
			File userBuildFolder=new File(userBuildDIR);
			if(!userBuildFolder.exists()){
				userBuildFolder.mkdirs();
			}
			String projectDIR=StringUtils.removeEnd(project.getSrcDIR(), File.separator);
			projectDIR+=File.separator;
			String runBuild=projectDIR+ProjectService.packer+" "+userBuildDIR;
			System.out.println("runBuild: "+runBuild);
			String result=SSHUtils.runLinuxCMD(runBuild);
			IOUtils.write(result, new FileWriter(new File(userBuildDIR+File.separator+"packer.log")));
			System.out.println(result);
			CalFilesMD5(userBuildDIR);
			return JsonRespUtils.success("打包完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JsonRespUtils.success("打包失败");
	}

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public String uploadResource(
			@RequestParam(value = "projectid") String projectid,
			@RequestParam(value = "resourceid") String resourceid,
			@RequestParam(value = "uploadserverid") String uploadserverid,
			HttpSession session) {
		try {
			if (StringUtils.isBlack(resourceid, uploadserverid)) {
				return JsonRespUtils.fail("上传失败");
			}
			User user = (User) session.getAttribute("user");
			if (user == null) {
				return JsonRespUtils.fail("请重新登录");
			}
			UploadServer uploadserver = ServerConfigService.uploadServerContentById
					.get(Integer.valueOf(uploadserverid));
			if (uploadserver == null) {
				return JsonRespUtils.fail("资源服不存在");
			}
			Project project = ProjectService.projectContent.get(Integer
					.valueOf(projectid));
			if (project == null) {
				return JsonRespUtils.fail("获取项目失败");
			}

			String userBuildDIR = ProjectService.buildDIR + user.getName()
					+ File.separator + project.getBuildFolder()
					+ File.separator + resourceid;
			String codeFile = userBuildDIR + File.separator + "game.jar";
			String resFile = userBuildDIR + File.separator + "res.zip";
			String[] uploadfiles = new String[] { codeFile, resFile };
			boolean result = SSHUtils.uploadFile(uploadserver, uploadfiles,session);
			if (result) {
				return JsonRespUtils.success("上传完成");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.success("上传失败");
		}
		return JsonRespUtils.success("上传失败");
	}
	
	public static void CalFilesMD5(String path){
		try {
			if(path!=null&&path.length()>0){
				File dir=new File(path);
				if(dir.isDirectory()){
					File[] files=dir.listFiles();
					StringBuilder sbresult=new StringBuilder();
					for(File f:files){
						String fName=f.getName();
						if(fName.endsWith(".jar")||fName.endsWith(".zip")){
							sbresult.append(fName).append(": ");
							String md5=FileUtils.getFileMD5String(f);
							sbresult.append(md5).append("\n");
						}
					}
					FileOutputStream fos=new FileOutputStream(new File(dir.getPath()+File.separator+"readme.txt"));
					fos.write(sbresult.toString().getBytes());
					fos.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<Map<String,String>> getUserPackageList(String projectid,HttpSession session){
		if(StringUtils.isBlack(projectid)||"-1".equals(projectid)){
			return null;
		}
		User user=(User)session.getAttribute("user");
		if(user==null){
			return null;
		}
		Project project=ProjectService.projectContent.get(Integer.valueOf(projectid));
		if(project==null){
			return null;
		}
		String userBuildDIR=ProjectService.buildDIR+user.getName()+File.separator+project.getBuildFolder();
		System.out.println("userBuildDIR:"+userBuildDIR);
		File dir=new File(userBuildDIR);
		if(!dir.exists()){
			return null;
		}
		File[] subDirList=dir.listFiles();
		if(subDirList==null||subDirList.length<=0){
			return null;
		}
		List<Map<String,String>> result=new ArrayList<Map<String,String>>();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(File f:subDirList){
			if((System.currentTimeMillis()-f.lastModified())>(long)30*24*60*60*1000){
				System.out.println("del:"+f.lastModified());
				FileUtils.deletefile(f);
				continue;
			}
			
			Map<String,String> map=new HashMap<String, String>();
			map.put("id", f.getName());
			map.put("time", sdf.format(new Date(f.lastModified())));
			long gameSize=0;
			long resSize=0;
			File[] flist=f.listFiles();
			for(File fl:flist){
				if("game.jar".equals(fl.getName())){
					gameSize=fl.length();
				}
				if("res.zip".equals(fl.getName())){
					resSize=fl.length();
				}
			}
			map.put("gameSize", gameSize+"");
			map.put("resSize", resSize+"");
			result.add(map);
		}
		Collections.sort(result, packerSorter);
		return result;
	}
	
	private static Comparator<Map<String,String>> packerSorter=new Comparator<Map<String,String>>() {
		@Override
		public int compare(Map<String,String> o1, Map<String,String> o2) {
			return o2.get("id").compareTo(o1.get("id"));
		}
	};
	
	/**
	 * 凌晨2点自动打Nightly包
	 */
	public static void initTimingPacker(){
		TimerManagerUtils.scheduleMany(new Runnable() {
			
			@Override
			public void run() {
				autoPacker();
			}
		}, CalcUtils.getDelayHour(2), 24, TimeUnit.HOURS);
	}
	
	
	public static void autoPacker(){
		try {
			String dir = "autobuild";
			List<Project> projects = ProjectService.getProjectList();
			for (Project project : projects) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String userBuildDIR = ProjectService.buildDIR + dir
						+ File.separator + project.getBuildFolder()
						+ File.separator + sdf.format(new Date());
				System.out.println("userBuildDIR:" + userBuildDIR);
				File userBuildFolder = new File(userBuildDIR);
				if (!userBuildFolder.exists()) {
					userBuildFolder.mkdirs();
				}
				String projectDIR = StringUtils.removeEnd(project.getSrcDIR(),
						File.separator);
				projectDIR += File.separator;
				String runBuild = projectDIR + ProjectService.packer + " "
						+ userBuildDIR;
				System.out.println("runBuild: " + runBuild);
				String result = SSHUtils.runLinuxCMD(runBuild);
				IOUtils.write(result, new FileWriter(new File(userBuildDIR
						+ File.separator + "packer.log")));
				System.out.println(result);
				CalFilesMD5(userBuildDIR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
