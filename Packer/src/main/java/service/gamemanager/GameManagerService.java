package service.gamemanager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import service.serverconfig.GameServer;
import service.serverconfig.ServerConfigService;
import common.config.Config;
import common.user.User;
import common.utils.FileUtils;
import common.utils.JsonRespUtils;
import common.utils.SSHUtils;
import common.utils.StringUtils;

@Controller
@RequestMapping("gamemanager")
public class GameManagerService {

//	@RequestMapping(value = "upload", method = RequestMethod.POST)
//	@ResponseBody
//	public String handleFileUpload(
//			@RequestParam("file") CommonsMultipartFile file) {
//		String path = (String) Config.CONFIG_DATA.get("gameManagerUploadPath");
//		if (StringUtils.isBlack(path)) {
//			return JsonRespUtils
//					.fail("Config.json gameManagerUploadPath is null");
//		}
//		if (!file.isEmpty()) {
//			try {
//				file.transferTo(new File(path + File.separator
//						+ file.getOriginalFilename()));
//			} catch (IllegalStateException | IOException e) {
//				e.printStackTrace();
//				return JsonRespUtils.exception(e.toString());
//			}
//		}
//		return JsonRespUtils.fail("upload fail.");
//	}

	/**
	 * 上传资源文件
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public String upload(MultipartHttpServletRequest request,
			HttpServletResponse response) {
		try {
			Iterator<String> itr = request.getFileNames();
			MultipartFile mpf = null;
			List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
			String path = (String) Config.CONFIG_DATA.get("gameManagerUploadPath");
			if (StringUtils.isBlack(path)) {
				return JsonRespUtils.fail("Config.json gameManagerUploadPath is null");
			}
			File dir=new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}
			while (itr.hasNext()) {
				Map<String,Object> map=new HashMap<String, Object>();
				mpf = request.getFile(itr.next());
				map.put("fileName", mpf.getOriginalFilename());
				map.put("fileSize", mpf.getSize() / 1024 + " Kb");
				map.put("fileType", mpf.getContentType());
				File file=new File(path + File.separator+ mpf.getOriginalFilename());
				FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(file));
				map.put("fileMD5", FileUtils.getFileMD5String(file));
				result.add(map);
			}
			return JsonRespUtils.success(result);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.exception(e.toString());
		}
	}
	
	@RequestMapping(value = "create-sshconnect", method = RequestMethod.POST)
	@ResponseBody
	public String createSSHConnect(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs==null){
				return JsonRespUtils.fail("连接失败，没有该服务器");
			}
			User user = (User) session.getAttribute("user");
			if (user == null) {
				return JsonRespUtils.fail("请重新登录");
			}
			
			SSHUtils.getSSHSession(session,gs);
			
			return JsonRespUtils.success("连接成功");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("连接远程服务器失败，遇到错误");
		}
	}
	
	@RequestMapping(value = "close-sshconnect", method = RequestMethod.POST)
	@ResponseBody
	public String closeSSHConnect(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs!=null){
				Map<Integer,Session> sshContent=(Map<Integer,Session>)session.getAttribute("sshContent");
				if(sshContent!=null){
					Session con=sshContent.get(Integer.valueOf(gameserverid));
					if(con!=null){
						con.disconnect();
						sshContent.remove(Integer.valueOf(gameserverid));
						System.out.println("disconnect");
					}
				}
			}
			Map<String,Session> sessions=(Map<String,Session>)session.getAttribute("WSContext");
			if(sessions!=null){
				javax.websocket.Session se=(javax.websocket.Session)sessions.get("gameManager:gameserverid:"+gameserverid);
				if(se!=null){
					se.close();
				}
			}
			return JsonRespUtils.success("已和远程服务器断开连接");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("和远程服务器断开连接，遇到错误");
		}
	}
	
	@RequestMapping(value = "check-server-state", method = RequestMethod.POST)
	@ResponseBody
	public String checkServerState(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs==null){
				return JsonRespUtils.fail("获取服务器失败");
			}
			String ps="";
			Map<String,Object> result=new HashMap<String, Object>(); 
			ps=SSHUtils.runRemoteCMD("ps -ef | grep java", SSHUtils.getSSHSession(session, gs),session,gs.getId());
			if(StringUtils.isBlank(ps)){
				result.put("state", -1);
				result.put("msg", "检测失败");
			}else{
				if(ps.contains(gs.getKeywords())){
					result.put("state", 1);
					result.put("msg", "运行中");
				}else{
					result.put("state", 0);
					result.put("msg", "已停服");
				}
			}
			return JsonRespUtils.success(result);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("检查服务器状态，遇到错误");
		}
	}
	
	@RequestMapping(value = "stop-server", method = RequestMethod.POST)
	@ResponseBody
	public String stopServer(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs==null){
				return JsonRespUtils.fail("关服失败,获取服务器失败");
			}
			String cmd=gs.getGameDIR()+"stop.sh";
			String result=SSHUtils.runRemoteCMD(cmd, SSHUtils.getSSHSession(session, gs),session,gs.getId());
			return JsonRespUtils.success("关服成功");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("关服失败，遇到错误");
		}
	}
	
	@RequestMapping(value = "forcestop-server", method = RequestMethod.POST)
	@ResponseBody
	public String forcestopServer(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs==null){
				return JsonRespUtils.fail("强制关服失败,获取服务器失败");
			}
			String ps="";
			String cmd="ps x|grep java| grep "+gs.getKeywords()+" |awk '{print $1}'";
			ps=SSHUtils.runRemoteCMD(cmd, SSHUtils.getSSHSession(session, gs),session,gs.getId());
			if(StringUtils.isNotBlank(ps)){
				ps=ps.trim();
				cmd="kill -9 "+ps;
				SSHUtils.runRemoteCMD(cmd, SSHUtils.getSSHSession(session, gs),session,gs.getId());
				return JsonRespUtils.success("强制关服成功");
			}
			return JsonRespUtils.fail("强制关服失败");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("强制关服失败，遇到错误");
		}
	}
	
	@RequestMapping(value = "start-server", method = RequestMethod.POST)
	@ResponseBody
	public String startServer(@RequestParam(value = "gameserverid") String gameserverid,HttpSession session) {
		try {
			GameServer gs=ServerConfigService.gameServerContentById.get(Integer.valueOf(gameserverid));
			if(gs==null){
				return JsonRespUtils.fail("开服失败，没有找到服务器");
			}
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			String cmd=gs.getGameDIR()+"auto.sh";
			String result="";
			result=SSHUtils.runRemoteCMD(cmd,SSHUtils.getSSHSession(session, gs),"now desktop is",session,gs.getId());
			cmd="tail -f "+gs.getGameDIR()+"logs/"+sdf.format(new Date())+".log";
			result+="\n";
			result=SSHUtils.runRemoteCMD(cmd,SSHUtils.getSSHSession(session, gs),"listen on",session,gs.getId());
			return JsonRespUtils.success(result);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonRespUtils.fail("开服失败，遇到错误");
		}
	}
}
