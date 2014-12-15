package service.projectmanager;

import java.util.List;

public class Project {
	private int id;
	private String name;
	private String srcDIR;
	private String buildFolder;
	/**用户ID*/
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
	public String getSrcDIR() {
		return srcDIR;
	}
	public void setSrcDIR(String srcDIR) {
		this.srcDIR = srcDIR;
	}
	public String getBuildFolder() {
		return buildFolder;
	}
	public void setBuildFolder(String buildFolder) {
		this.buildFolder = buildFolder;
	}
	public List<Integer> getUsers() {
		return users;
	}
	public void setUsers(List<Integer> users) {
		this.users = users;
	}
}
