package common.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

public class JsonRespUtils {
	
	public static String response(int code,String msg){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("code", code);
		map.put("data", msg);
		String data=JsonUtils.encode2Str(map);
		return data;
	}
	
	public static void responseWsGameManager(HttpSession httpSession,String op,int gameserverid,String msg){
		try {
			Map<String,Session> sessions=(Map<String,Session>)httpSession.getAttribute("WSContext");
			if(sessions==null)
				return;
			Session session=sessions.get(op);
			if(session==null)
				return;
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("gameserverid", gameserverid);
			map.put("content", msg);
			String data=JsonUtils.encode2Str(map);
			session.getBasicRemote().sendText(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void responseWsResUpload(HttpSession httpSession,String op,int uploadserverid,String fileName,String percent){
		try {
			Map<String,Session> sessions=(Map<String,Session>)httpSession.getAttribute("WSContext");
			if(sessions==null)
				return;
			Session session=sessions.get(op);
			if(session==null)
				return;
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("gameserverid", uploadserverid);
			map.put("file", fileName);
			map.put("percent", percent);
			String data=JsonUtils.encode2Str(map);
			session.getBasicRemote().sendText(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String fail(String msg){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("code", Def.CODE_FAIL);
		map.put("data", msg);
		String data=JsonUtils.encode2Str(map);
		return data;
	}
	
	public static String success(Object obj){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("code", Def.CODE_SUCCESS);
		map.put("data", obj);
		String data=JsonUtils.encode2Str(map);
		return data;
	}
	
	public static String exception(Object obj){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("code", Def.CODE_EXCEPTION);
		map.put("data", obj);
		String data=JsonUtils.encode2Str(map);
		return data;
	}
}
