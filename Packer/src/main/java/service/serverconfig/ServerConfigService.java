package service.serverconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import common.config.Config;
import common.logger.Logger;
import common.logger.LoggerManger;
import common.user.User;
import common.user.UserService;
import common.utils.JsonRespUtils;
import common.utils.JsonUtils;
import common.utils.StringUtils;

@Controller
@RequestMapping("server-config")
public class ServerConfigService {
	private static Logger log=LoggerManger.getLogger();
	public static Map<Integer,GameServer> gameServerContentById=new ConcurrentHashMap<Integer,GameServer>();
	public static Map<Integer,UploadServer> uploadServerContentById=new ConcurrentHashMap<Integer,UploadServer>();
	public static AtomicInteger gsIndex=new AtomicInteger();
	public static AtomicInteger usIndex=new AtomicInteger();
	/**
	 * 初始化服务器配置数据
	 */
	public static void initServerContent(){
		try {
			log.info("Star init serverContent json data.");
			String filePath=Config.CONFIG_DIR + File.separator + "server.json";
			Map map=JsonUtils.readFromFile(filePath, Map.class);
			
			Map<Integer,GameServer> tempGameServerContentById=new ConcurrentHashMap<Integer,GameServer>();
			Map<Integer,UploadServer> tempUploadServerContentById=new ConcurrentHashMap<Integer,UploadServer>();
			
			gsIndex.set((int)map.get("gs-index"));
			usIndex.set((int)map.get("us-index"));
			List<Map> gss=(List<Map>)map.get("gameserver");
			if(gss!=null){
				for(Map gs:gss){
					GameServer game=new GameServer();
					game.setGameDIR(StringUtils.removeEnd((String)gs.get("gameDIR"), "/")+"/");
					game.setHost((String)gs.get("host"));
					game.setId((Integer)gs.get("id"));
					game.setName((String)gs.get("name"));
					game.setPassword((String)gs.get("password"));
					game.setPort((String)gs.get("port"));
					game.setRunFile((String)gs.get("runFile"));
					game.setUser((String)gs.get("user"));
					String keywords=(String)gs.get("keywords");
					if(keywords.contains(StringUtils.SPACE)){
						throw new IllegalArgumentException("keywords can not contains SPACE,gameid=:"+game.getId());
					}
					game.setKeywords((String)gs.get("keywords"));
					List<Integer> users=(List<Integer>)gs.get("users");
					if(users==null){
						users=new ArrayList<Integer>();
						List<User> userList=UserService.getUserList();
						for(User u:userList){
							users.add(u.getId());
						}
					}
					game.setUsers(users);
					tempGameServerContentById.put(game.getId(), game);
				}
			}
			
			List<Map> uss=(List<Map>)map.get("uploadserver");
			if(uss!=null){
				for(Map us:uss){
					UploadServer game=new UploadServer();
					game.setUploadDIR(StringUtils.removeEnd((String)us.get("uploadDIR"), "/")+"/");
					game.setHost((String)us.get("host"));
					game.setId((Integer)us.get("id"));
					game.setName((String)us.get("name"));
					game.setPassword((String)us.get("password"));
					game.setPort((String)us.get("port"));
					game.setUser((String)us.get("user"));
					List<Integer> users=(List<Integer>)us.get("users");
					if(users==null){
						users=new ArrayList<Integer>();
						List<User> userList=UserService.getUserList();
						for(User u:userList){
							users.add(u.getId());
						}
					}
					game.setUsers(users);
					
					tempUploadServerContentById.put(game.getId(),game);
				}
			}
			gameServerContentById=tempGameServerContentById;
			uploadServerContentById=tempUploadServerContentById;
			log.info("Init serverContent json data complete.");
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * 回写服务器配置数据
	 */
	public static void flushServerContent(){
		log.info("Star flush serverContent json data.");
		try {
			String filePath=Config.CONFIG_DIR + File.separator + "server.json";
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("gameserver", new ArrayList(gameServerContentById.values()));
			map.put("uploadserver", new ArrayList(uploadServerContentById.values()));
			map.put("gs-index", gsIndex.get());
			map.put("us-index", usIndex.get());
			JsonUtils.writeToFile(filePath, map);
		} catch (Exception e) {
			log.error(e.toString());
			e.printStackTrace();
		}
		log.info("Flush serverContent json data completed.");
	}
	
	@RequestMapping(value ="create-gameserver",method = RequestMethod.POST)
	@ResponseBody
	public String createGameserver(
			@RequestParam(value = "name") String name,
			@RequestParam(value = "host") String host,
			@RequestParam(value = "port") String port,
			@RequestParam(value = "user") String user,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "gameDIR") String gameDIR,
			@RequestParam(value = "runFile") String runFile,
			@RequestParam(value = "keywords") String keywords,
			@RequestParam(value = "userids") String userids) {
		if(StringUtils.isBlack(name,host,port,user,password,gameDIR,runFile,keywords)){
			return JsonRespUtils.fail("必要数据不能为空");
		}
		if(gameServerNameExist(name)){
			return JsonRespUtils.fail("服务器名已存在");
		}
		if(keywords.contains(StringUtils.SPACE)){
			return JsonRespUtils.fail("keywords不能包含空格");
		}
		GameServer gameserver=new GameServer();
		gameserver.setId(gsIndex.incrementAndGet());
		gameserver.setName(name.trim());
		gameserver.setPassword(password.trim());
		gameserver.setHost(host.trim());
		gameserver.setPort(port.trim());
		gameserver.setUser(user.trim());
		gameDIR=StringUtils.removeEnd(gameDIR.trim(), "/")+"/";
		gameserver.setGameDIR(gameDIR);
		gameserver.setRunFile(runFile.trim());
		gameserver.setKeywords(keywords.trim());
		List<Integer> useridList=new ArrayList<Integer>();
		if(StringUtils.isBlack(userids)){
			List<User> users=UserService.getUserList();
			for(User u:users){
				useridList.add(u.getId());
			}
		}else{
			String[] uidStr=userids.split(",");
			for(String id:uidStr){
				useridList.add(Integer.parseInt(id));
			}
		}
		gameserver.setUsers(useridList);
		gameServerContentById.put(gameserver.getId(), gameserver);
		flushServerContent();
		return JsonRespUtils.success("创建成功");
	}
	
	@RequestMapping(value ="update-gameserver",method = RequestMethod.POST)
	@ResponseBody
	public String updateGameserver(
			@RequestParam(value = "id") String id,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "host") String host,
			@RequestParam(value = "port") String port,
			@RequestParam(value = "user") String user,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "gameDIR") String gameDIR,
			@RequestParam(value = "runFile") String runFile,
			@RequestParam(value = "keywords") String keywords,
			@RequestParam(value = "userids") String userids) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		GameServer gameserver=gameServerContentById.get(Integer.valueOf(id));
		if(gameserver==null){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		if(StringUtils.isBlack(name,host,port,user,password,gameDIR,runFile,keywords)){
			return JsonRespUtils.fail("必要数据不能为空");
		}
		if(!name.equals(gameserver.getName())&&gameServerNameExist(name)){
			return JsonRespUtils.fail("服务器名已存在");
		}
		if(keywords.contains(StringUtils.SPACE)){
			return JsonRespUtils.fail("keywords不能包含空格");
		}
		gameserver.setName(name.trim());
		gameserver.setPassword(password.trim());
		gameserver.setHost(host.trim());
		gameserver.setPort(port.trim());
		gameserver.setUser(user.trim());
		gameDIR=StringUtils.removeEnd(gameDIR.trim(), "/")+"/";
		gameserver.setGameDIR(gameDIR);
		gameserver.setRunFile(runFile.trim());
		gameserver.setKeywords(keywords.trim());
		List<Integer> useridList=new ArrayList<Integer>();
		if(StringUtils.isNotBlank(userids)){
			String[] uidStr=userids.split(",");
			for(String uid:uidStr){
				useridList.add(Integer.parseInt(uid));
			}
		}
		gameserver.setUsers(useridList);
		gameServerContentById.put(gameserver.getId(), gameserver);
		flushServerContent();
		return JsonRespUtils.success("修改成功");
	}
	
	@RequestMapping(value ="update-uploadserver",method = RequestMethod.POST)
	@ResponseBody
	public String updateUploadserver(
			@RequestParam(value = "id") String id,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "host") String host,
			@RequestParam(value = "port") String port,
			@RequestParam(value = "user") String user,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "uploadDIR") String uploadDIR,
			@RequestParam(value = "userids") String userids) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		UploadServer uploadserver=uploadServerContentById.get(Integer.valueOf(id));
		if(uploadserver==null){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		if(StringUtils.isBlack(name,host,port,user,password,uploadDIR)){
			return JsonRespUtils.fail("必要数据不能为空");
		}
		if(!name.equals(uploadserver.getName())&&uploadServerNameExist(name)){
			return JsonRespUtils.fail("服务器名已存在");
		}
		uploadserver.setName(name.trim());
		uploadserver.setPassword(password.trim());
		uploadserver.setHost(host.trim());
		uploadserver.setPort(port.trim());
		uploadserver.setUser(user.trim());
		uploadDIR=StringUtils.removeEnd(uploadDIR.trim(), "/")+"/";
		uploadserver.setUploadDIR(uploadDIR);
		List<Integer> useridList=new ArrayList<Integer>();
		if(StringUtils.isNotBlank(userids)){
			String[] uidStr=userids.split(",");
			for(String uid:uidStr){
				useridList.add(Integer.parseInt(uid));
			}
		}
		uploadserver.setUsers(useridList);
		
		uploadServerContentById.put(uploadserver.getId(), uploadserver);
		flushServerContent();
		return JsonRespUtils.success("修改成功");
	}
	
	@RequestMapping(value ="get-gameserver",method = RequestMethod.POST)
	@ResponseBody
	public String getGameserver(
			@RequestParam(value = "id") String id) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		try {
			Integer i=Integer.valueOf(id);
			GameServer g=gameServerContentById.get(i);
			if(g==null){
				return JsonRespUtils.fail("数据错误");
			}else{
				return JsonRespUtils.success(g);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("数据错误");
		}
	}
	
	@RequestMapping(value ="get-uploadserver",method = RequestMethod.POST)
	@ResponseBody
	public String getUploadserver(
			@RequestParam(value = "id") String id) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		try {
			Integer i=Integer.valueOf(id);
			UploadServer g=uploadServerContentById.get(i);
			if(g==null){
				return JsonRespUtils.fail("数据错误");
			}else{
				return JsonRespUtils.success(g);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("数据错误");
		}
	}
	
	@RequestMapping(value ="delete-gameserver",method = RequestMethod.POST)
	@ResponseBody
	public String deleteGameserver(
			@RequestParam(value = "id") String id) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		try {
			Integer i=Integer.valueOf(id);
			gameServerContentById.remove(i);
			flushServerContent();
			return JsonRespUtils.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("数据错误");
		}
	}
	
	@RequestMapping(value ="delete-uploadserver",method = RequestMethod.POST)
	@ResponseBody
	public String deleteUploaderver(
			@RequestParam(value = "id") String id) {
		if(StringUtils.isBlack(id)){
			return JsonRespUtils.fail("数据错误，刷新页面再尝试");
		}
		try {
			Integer i=Integer.valueOf(id);
			uploadServerContentById.remove(i);
			flushServerContent();
			return JsonRespUtils.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("数据错误");
		}
	}
	
	@RequestMapping(value ="create-uploadserver",method = RequestMethod.POST)
	@ResponseBody
	public String createUploadserver(
			@RequestParam(value = "name") String name,
			@RequestParam(value = "host") String host,
			@RequestParam(value = "port") String port,
			@RequestParam(value = "user") String user,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "uploadDIR") String uploadDIR,
			@RequestParam(value = "userids") String userids) {
		if(StringUtils.isBlack(name,host,port,user,password,uploadDIR)){
			return JsonRespUtils.fail("必要数据不能为空");
		}
		if(uploadServerNameExist(name)){
			return JsonRespUtils.fail("服务器名已存在");
		}
		UploadServer uploadserver=new UploadServer();
		uploadserver.setId(usIndex.incrementAndGet());
		uploadserver.setName(name.trim());
		uploadserver.setPassword(password.trim());
		uploadserver.setHost(host.trim());
		uploadserver.setPort(port.trim());
		uploadserver.setUser(user.trim());
		uploadDIR=StringUtils.removeEnd(uploadDIR.trim(), "/")+"/";
		uploadserver.setUploadDIR(uploadDIR);
		List<Integer> useridList=new ArrayList<Integer>();
		if(StringUtils.isBlack(userids)){
			List<User> users=UserService.getUserList();
			for(User u:users){
				useridList.add(u.getId());
			}
		}else{
			String[] uidStr=userids.split(",");
			for(String id:uidStr){
				useridList.add(Integer.parseInt(id));
			}
		}
		uploadserver.setUsers(useridList);
		
		uploadServerContentById.put(uploadserver.getId(), uploadserver);
		flushServerContent();
		return JsonRespUtils.success("创建成功");
	}
	
	public static List<GameServer> getGameServers(){
		List<GameServer> list=new ArrayList<GameServer>(gameServerContentById.values());
		Collections.sort(list, gameServerSorter);
		return list;
	}
	
	public static List<GameServer> getGameServers(User user) {
		List<GameServer> result = new ArrayList<GameServer>();
		if (user == null) {
			return result;
		}
		List<GameServer> list = getGameServers();
		for (GameServer gs : list) {
			if (gs.getUsers().contains(user.getId())) {
				result.add(gs);
			}
		}
		return result;
	}
	
	public static List<UploadServer> getUploadServers(){
		List<UploadServer> list=new ArrayList<UploadServer>(uploadServerContentById.values());
		Collections.sort(list, uploadServerSorter);
		return list;
	}
	
	public static List<UploadServer> getUploadServers(User user) {
		List<UploadServer> result = new ArrayList<UploadServer>();
		if (user == null) {
			return result;
		}
		List<UploadServer> list = getUploadServers();
		for (UploadServer us : list) {
			if (us.getUsers().contains(user.getId())) {
				result.add(us);
			}
		}
		return result;
	}
	
	private static Comparator<GameServer> gameServerSorter=new Comparator<GameServer>() {
		@Override
		public int compare(GameServer o1, GameServer o2) {
			return o1.getId()-o2.getId();
		}
	};
	private static Comparator<UploadServer> uploadServerSorter=new Comparator<UploadServer>() {
		@Override
		public int compare(UploadServer o1, UploadServer o2) {
			return o1.getId()-o2.getId();
		}
	};
	
	public static boolean gameServerNameExist(String name){
		if(StringUtils.isBlank(name)){
			return true;
		}
		name.trim();
		Map<Integer,GameServer> gmap=ServerConfigService.gameServerContentById;
		for(GameServer g:gmap.values()){
			if(name.equals(g.getName())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean uploadServerNameExist(String name){
		if(StringUtils.isBlank(name)){
			return true;
		}
		name.trim();
		Map<Integer,UploadServer> umap=ServerConfigService.uploadServerContentById;
		for(UploadServer u:umap.values()){
			if(name.equals(u.getName())){
				return true;
			}
		}
		return false;
	}
}
