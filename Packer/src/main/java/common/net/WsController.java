package common.net;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.JsonNode;

import common.utils.Def;
import common.utils.JsonUtils;

@ServerEndpoint(value="/ws/{op}",configurator=WsEndpointConfig.class)
public class WsController {
	
	@OnOpen
	public void onOpen(Session paramSession, EndpointConfig paramEndpointConfig,@PathParam("op") String op){
		HttpSession httpSession = (HttpSession) paramEndpointConfig.getUserProperties().get(HttpSession.class.getName());
		Map<String,Session> context=(Map<String,Session>)httpSession.getAttribute("WSContext");
		if(context==null){
			context=new ConcurrentHashMap<String, Session>();
			httpSession.setAttribute("WSContext", context);
		}
		context.put(op, paramSession);
		System.out.println("onOpen");
	}
	
	@OnMessage
	public void onMessage(Session session,String message) throws IOException{
		System.out.println("sessionid:"+session.getId()+"  onMessage:"+message);
		JsonNode node=JsonUtils.decode(message);
		int op=JsonUtils.getInt("op", node);
		if(op!=-1){
			switch (op) {
			case Def.OPEN_GAMESERVER:
				
				break;
			case Def.TEST:
				test(session);
			default:
				break;
			}
		}
	}
	
	@OnClose
	public void onClose(Session session, CloseReason closeReason){
		System.out.println("websocket close from session:"+session.getId());
	}
	
	@OnError
	public void OnError(Session session, Throwable throwable){
		System.out.println("websocket onError");
	}
	
	public void test(Session session) throws IOException{
		for(int i=0;i<10;i++){
		}
	}
}
