package com.jiangyifen.ec2.globaldata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.servlet.http.common.pojo.BridgeVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.HangupVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.PopupIncomingVo;

/**
 * @Description 描述: 武睿专用, 内存维护信息
 * 
 * @author jht
 * @date 2015年10月26日 10:51:53
 */
public class WuRuiShareData {

	/**
	 * 座席用户名与来电信息封装类的对应关系 Map<用户名, PopupIncomingVo>
	 */
	public static Map<String, PopupIncomingVo> csrToPopupIncomingVoMap = new ConcurrentHashMap<String, PopupIncomingVo>();
	
	/**
	 * 座席接起信息存储 Map<用户名, BridgeVo>
	 */
	public static Map<String, BridgeVo> csrToBridgeVoMap = new ConcurrentHashMap<String, BridgeVo>();
	
	/**
	 * 座席挂短信息存储 Map<用户名, HangupVo>
	 */
	public static Map<String, HangupVo> csrToHangupVoMap = new ConcurrentHashMap<String, HangupVo>();
	
}
