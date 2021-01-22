package com.jiangyifen.ec2.ui.mgr.sysdisk;

public class SoundRecording {

	private Long id;
	private String name;
	private String absolutePath;
	private String fileTotalSize;
	
	public SoundRecording(String absolutePath,String name,String fileTotalSize){
		this.absolutePath = absolutePath;
		this.name = name;
		this.fileTotalSize = fileTotalSize;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getFileTotalSize() {
		return fileTotalSize;
	}

	public void setFileTotalSize(String fileTotalSize) {
		this.fileTotalSize = fileTotalSize;
	}

	@Override
	public String toString() {
		return "SoundRecording [name=" + name + ", absolutePath=" + absolutePath + ", fileTotalSize=" + fileTotalSize + "]";
	}
	
	
}
