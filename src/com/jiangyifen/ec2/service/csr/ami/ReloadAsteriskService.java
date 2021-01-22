package com.jiangyifen.ec2.service.csr.ami;

/**
 * 重新加载Asterisk 的配置
 * @author jrh
 */
public interface ReloadAsteriskService {
	
	/**
	 * 重新加载asterisk 的配置文件
	 */
	public void reloadAsterisk();

	/**
	 * 重新加载asterisk 的配置文件
	 */
	public void reloadSip();
	
	/**
	 * 重新加载asterisk 的配置文件
	 */
	public void reloadMoh();
	
	/**
	 * 重新加载asterisk 的配置文件
	 */
	public void reloadQueue();
	
	/**
	 * 重新加载 asterisk 的 voicemail 配置文件
	 */
	public void reloadVoicemail();
	
}
