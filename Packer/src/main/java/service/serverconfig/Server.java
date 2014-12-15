package service.serverconfig;

import java.util.List;

/**
 * linux 游戏服务器
 * @author liangyx
 * @date 2014-10-10
 */
public class Server {
	private int id;
	private String name;
	private String host;
	private String port;
	/**连接远程服务器的用户名*/
	private String user;
	private String password;
	/**可以操作的用户*/
	private List<Integer> users;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<Integer> getUsers() {
		return users;
	}
	public void setUsers(List<Integer> users) {
		this.users = users;
	}
}
