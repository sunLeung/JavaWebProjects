package service.serverconfig;

/**
 * 游戏服务器
 * @author liangyx
 * @date 2014-10-10
 */
public class GameServer extends Server{
	private String gameDIR;
	private String runFile;
	private String keywords;
	public String getGameDIR() {
		return gameDIR;
	}
	public void setGameDIR(String gameDIR) {
		this.gameDIR = gameDIR;
	}
	public String getRunFile() {
		return runFile;
	}
	public void setRunFile(String runFile) {
		this.runFile = runFile;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
}
