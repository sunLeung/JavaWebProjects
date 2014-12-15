package service.projectmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import service.serverconfig.GameServer;
import service.serverconfig.UploadServer;
import common.config.Config;
import common.logger.Logger;
import common.logger.LoggerManger;
import common.user.User;
import common.user.UserService;
import common.utils.JsonRespUtils;
import common.utils.JsonUtils;
import common.utils.StringUtils;

@Controller
@RequestMapping("project-config")
public class ProjectService {
	private static Logger log=LoggerManger.getLogger();
	public static String projectDIR;
	public static String buildDIR;
	public static String packer;
	public static Map<Integer,Project> projectContent=new ConcurrentHashMap<Integer,Project>();
	
	/**
	 * 初始化服务器配置数据
	 */
	public static void initProjectContent(){
		try {
			log.info("Star init projectContent json data.");
			String filePath=Config.CONFIG_DIR + File.separator + "project.json";
			Map map=JsonUtils.readFromFile(filePath, Map.class);
			
			projectDIR=(String)map.get("projectDIR");
			projectDIR=StringUtils.removeEnd(projectDIR, File.separator)+File.separator;
			buildDIR=(String)map.get("buildDIR");
			buildDIR=StringUtils.removeEnd(buildDIR, File.separator)+File.separator;
			packer=(String)map.get("packer");
			
			Map<Integer,Project> tempProjectConfig=new ConcurrentHashMap<Integer,Project>();
			
			List<Map> projects=(List<Map>)map.get("projects");
			if(projects!=null){
				for(Map p:projects){
					Project pro=new Project();
					pro.setId((Integer)p.get("id"));
					pro.setName((String)p.get("name"));
					pro.setSrcDIR((String)p.get("srcDIR"));
					pro.setBuildFolder((String)p.get("buildFolder"));
					List<Integer> users=(List<Integer>)p.get("users");
					if(users==null){
						users=new ArrayList<Integer>();
						List<User> userList=UserService.getUserList();
						for(User u:userList){
							users.add(u.getId());
						}
					}
					pro.setUsers(users);
					tempProjectConfig.put(pro.getId(), pro);
				}
			}
			projectContent=tempProjectConfig;
			
			File projectDIRFile=new File(projectDIR);
			if(!projectDIRFile.exists()){
				projectDIRFile.mkdirs();
			}
			File buildDIRFile=new File(buildDIR);
			if(!buildDIRFile.exists()){
				buildDIRFile.mkdirs();
			}
			log.info("Init projectContent json data complete.");
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * 回写服务器配置数据
	 */
	public static void flushProjectContent(){
		log.info("Star flush projectContent json data.");
		try {
			String filePath=Config.CONFIG_DIR + File.separator + "project.json";
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("projectDIR", projectDIR);
			map.put("buildDIR", buildDIR);
			map.put("packer", packer);
			map.put("projects", new ArrayList<Project>(projectContent.values()));
			JsonUtils.writeToFile(filePath, map);
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
		}
		log.info("Flush serverContent json data completed.");
	}
	
	private static Comparator<Project> projectSorter=new Comparator<Project>() {
		@Override
		public int compare(Project o1, Project o2) {
			return o1.getId()-o2.getId();
		}
	};
	
	public static List<Project> getProjectList(){
		List<Project> list=new ArrayList<Project>(projectContent.values());
		Collections.sort(list, projectSorter);
		return list;
	}
	
	public static List<Project> getProjectList(User user){
		List<Project> result = new ArrayList<Project>();
		if (user == null) {
			return result;
		}
		List<Project> list = getProjectList();
		for (Project p : list) {
			if (p.getUsers().contains(user.getId())) {
				result.add(p);
			}
		}
		return result;
	}
	
	@RequestMapping(value ="get-project",method = RequestMethod.POST)
	@ResponseBody
	public String getProject(
			@RequestParam(value = "id") String id) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		try {
			Integer i=Integer.valueOf(id);
			Project p=projectContent.get(i);
			if(p==null){
				return JsonRespUtils.fail("数据错误");
			}else{
				return JsonRespUtils.success(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("数据错误");
		}
	}
	
	@RequestMapping(value ="update-project",method = RequestMethod.POST)
	@ResponseBody
	public String updateProject(
			@RequestParam(value = "id") String id,
			@RequestParam(value = "userids") String userids) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		Project p=projectContent.get(Integer.valueOf(id));
		if(p==null){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		List<Integer> useridList=new ArrayList<Integer>();
		if(StringUtils.isNotBlank(userids)){
			String[] uidStr=userids.split(",");
			for(String uid:uidStr){
				useridList.add(Integer.parseInt(uid));
			}
		}
		p.setUsers(useridList);
		projectContent.put(p.getId(), p);
		flushProjectContent();
		return JsonRespUtils.success("修改成功");
	}
}
