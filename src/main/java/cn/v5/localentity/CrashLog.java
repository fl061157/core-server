package cn.v5.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
@Entity(table = "crashlog")
public class CrashLog {
	@Id
	private String fileId;
	@Column
	private String os;
	@Column
	private String device;
	@Column
	private String date;
	
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public CrashLog() {
		
	}
	public CrashLog(String fileId, String os, String device, String date) {
		super();
		this.fileId = fileId;
		this.os = os;
		this.device = device;
		this.date = date;
	}
	
}
