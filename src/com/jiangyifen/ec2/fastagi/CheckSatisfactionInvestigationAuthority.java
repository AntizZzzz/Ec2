package com.jiangyifen.ec2.fastagi;

import java.util.Map;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 使用分机拨“#888#”做满意度调查时
 * 需要判断该域是否开启了呼入或呼出的满意度调查配置项
 * 如果没开启，返回一个空字符串的变量，然后asterisk 播放提示音“您还没开启满意度调查”
 * @author jrh
 *
 */
public class CheckSatisfactionInvestigationAuthority extends BaseAgiScript {

	@Override
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		Long domainId = 0L;
		
		// 设置话务员信息
		ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel.getName());
		if(channelSession != null) {
			Long bridgedUserId = channelSession.getBridgedUserId();
			if(bridgedUserId != null) {
				domainId = ShareData.userToDomain.get(bridgedUserId);
			}
		}
		
		String callDirection = channel.getVariable("direction");
		String hasAuthority = "";

		// 全局配置项
		Map<String, Boolean> configs = ShareData.domainToConfigs.get(domainId);
		// 判断全局配置是否开启了外呼或呼入客户满意度调查
		if("incoming".equals(callDirection)) {
			Boolean isOutgoingSati = configs.get("incoming_sati_config");
			if(isOutgoingSati != null && isOutgoingSati == true) {
				hasAuthority = "true";
			}
		} else if("outgoing".equals(callDirection)) {
			Boolean isOutgoingSati = configs.get("outgoing_sati_config");
			if(isOutgoingSati != null && isOutgoingSati == true) {
				hasAuthority = "true";
			}
		}
		channel.setVariable("hasAuthority", hasAuthority);
	}

}
