package common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import service.serverconfig.GameServer;
import service.serverconfig.UploadServer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import common.logger.Logger;
import common.logger.LoggerManger;

public class SSHUtils {
	
	public static String runLinuxCMD(String... cmd){
		String result="";
		try {
			List<String> cmds = new ArrayList<String>(); 
			cmds.add("/bin/sh"); 
			cmds.add("-c"); 
			cmds.addAll(Arrays.asList(cmd));
			ProcessBuilder pb=new ProcessBuilder(cmds); 
			Process p = pb.start();
			result=IOUtils.toString(p.getInputStream());
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void startGameServer(String cmd,String interrupt){
		try {
			List<String> cmds = new ArrayList<String>(); 
			cmds.add("/bin/sh"); 
			cmds.add("-c"); 
			cmds.add(cmd);
			ProcessBuilder pb=new ProcessBuilder(cmds); 
			Process p = pb.start();
			
			String temp="";
			BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((temp=br.readLine())!=null){
				if(StringUtils.containsIgnoreCase(temp, interrupt)){
					break;
				}
			}
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String runWindowsCMD(String cmd){
		String result=null;
		try {
			List<String> cmds = new ArrayList<String>(); 
			cmds.add("cmd.exe"); 
			cmds.add("-c"); 
			cmds.add(cmd); 
			ProcessBuilder pb=new ProcessBuilder(cmds); 
			Process p = pb.start();
			result=IOUtils.toString(p.getInputStream());
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean uploadFile(UploadServer server,String[] files,HttpSession httpSession){
		try {
			JSch.setConfig("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(server.getUser(), server.getHost(), Integer.valueOf(server.getPort()));
			session.setPassword(server.getPassword());
			session.connect(30000);
			
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp c = (ChannelSftp) channel;
			
			int mode=ChannelSftp.OVERWRITE;
			for(String file:files){
				c.put(file,server.getUploadDIR(),new MyProgressMonitor(httpSession,server), mode); 
			}
			c.disconnect();
			session.disconnect();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
//	public static void main(String[] args) {
//		try {
//			JSch.setConfig("StrictHostKeyChecking", "no");
//			JSch jsch = new JSch();
//			Session session = jsch.getSession("root", "175.126.103.102", 22);
//			session.setPassword("qef65l5%");
//			session.connect(30000);
//			
//			Channel channel = session.openChannel("sftp");
//			channel.connect();
//			ChannelSftp c = (ChannelSftp) channel;
//			
//			int mode=ChannelSftp.OVERWRITE;
//			c.put("D:\\packet\\kr_test\\res.zip","/root/upload",new MyProgressMonitor(), mode); 
//			c.disconnect();
//			session.disconnect();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static class MyProgressMonitor implements SftpProgressMonitor {
		private long count = 0;
		private long max = 0;
		private long percent = -1;
		private String src;
		private String dest;
		private HttpSession httpSession;
		private UploadServer uploaderServer;
		private String fileName;
		public MyProgressMonitor(HttpSession httpSession,UploadServer uploaderServer){
			this.httpSession=httpSession;
			this.uploaderServer=uploaderServer;
		}
		public void init(int op, String src, String dest, long max) {
			this.max = max;
			this.src=src;
			this.dest=dest;
			String[] path=src.split(File.separator);
			this.fileName=path[path.length-1];
			count = 0;
			percent = -1;
			System.out.println(((op == SftpProgressMonitor.PUT) ? "put" : "get") + ": "+ src);
			System.out.println("dest："+src);
			System.out.println("dest："+this.dest);
		}

		public boolean count(long count) {
			this.count += count;

			if (percent >= this.count * 100 / max) {
				return true;
			}
			percent = this.count * 100 / max;

			System.out.println(("Completed " + src +"  "+ this.count + "(" + percent + "%) out of " + max + "."));
			JsonRespUtils.responseWsResUpload(httpSession, "packer:uploadfile:"+uploaderServer.getId(), uploaderServer.getId(), fileName, percent+"%");
			return true;
		}
		public void end() {
			System.out.println("upload end");
		}
	}
	
	public static String runRemoteCMD(String cmd, Session session, String endStr,HttpSession httpSession,int gameserverid) throws JSchException, IOException {
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
		String temp = "";
		channel.connect();
		Logger log=LoggerManger.getLogger("RemoteServer","%msg");
		log.info("\n\n###############################BEGIN########################################");
		log.info("CMD:"+cmd);
		log.info("Time:"+ new Date().toString());
		StringBuilder sb=new StringBuilder();
		while ((temp = br.readLine()) != null) {
			log.info(temp);
			if(!cmd.contains("grep"))
				JsonRespUtils.responseWsGameManager(httpSession,"gameManager:gameserverid:"+gameserverid, gameserverid, temp);
			//sb.append(temp).append("\n");
			if (temp.contains(endStr)) {
				break;
			}
		}
		channel.disconnect();
		if(br!=null){
			br.close();
		}
		log.info("################################END#########################################");
		return sb.toString();
	}
	
	public static String runRemoteCMD(String cmd, Session session,HttpSession httpSession,int gameserverid) throws JSchException, IOException {
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
		String temp = "";
		channel.connect();
		Logger log=LoggerManger.getLogger("RemoteServer","%msg");
		log.info("\n\n###############################BEGIN########################################");
		log.info("CMD:"+cmd);
		log.info("Time:"+ new Date().toString());
		StringBuilder sb=new StringBuilder();
		while ((temp = br.readLine()) != null) {
			log.info(temp);
			if(!cmd.contains("grep"))
				JsonRespUtils.responseWsGameManager(httpSession,"gameManager:gameserverid:"+gameserverid, gameserverid, temp);
			sb.append(temp).append("\n");
		}
		channel.disconnect();
		if(br!=null){
			br.close();
		}
		log.info("################################END#########################################");
		return sb.toString();
	}
	
	public static Session getSSHSession(HttpSession hsession,GameServer gs) throws NumberFormatException, JSchException{
		Map<Integer,Session> sshContent=(Map<Integer,Session>)hsession.getAttribute("sshContent");
		if(sshContent==null){
			sshContent=new HashMap<Integer, Session>();
			hsession.setAttribute("sshContent", sshContent);
		}
		Session con=sshContent.get(gs.getId());
		if(con==null){
			JSch.setConfig("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			con = jsch.getSession(gs.getUser(), gs.getHost(), Integer.valueOf(gs.getPort()));
			con.setPassword(gs.getPassword());
			con.connect(30000);
			sshContent.put(gs.getId(), con);
		}
		if(!con.isConnected()){
			JSch.setConfig("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			con = jsch.getSession(gs.getUser(), gs.getHost(), Integer.valueOf(gs.getPort()));
			con.setPassword(gs.getPassword());
			con.connect(30000);
			sshContent.put(gs.getId(), con);
		}
		return con;
	}
}
