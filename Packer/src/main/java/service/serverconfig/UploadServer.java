package service.serverconfig;

/**
 * 上传资源服务器
 * @author liangyx
 * @date 2014-10-10
 */
public class UploadServer extends Server{
	private String uploadDIR;

	public String getUploadDIR() {
		return uploadDIR;
	}

	public void setUploadDIR(String uploadDIR) {
		this.uploadDIR = uploadDIR;
	}

}
