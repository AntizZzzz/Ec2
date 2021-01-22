package com.jiangyifen.ec2.service.csr.ami.impl;

import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.action.SetVarAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.SatisfactionInvestigationService;

/**
 * 
 * @Description 描述：满意度调查
 * 
 * @author  jrh
 * @date    2013年12月17日 下午5:22:42
 * @version v1.0.0
 */
public class SatisfactionInvestigationServiceImpl implements SatisfactionInvestigationService {

	/**
	 * jrh 
	 * 	客户满意度调查,邀请与当前话务员通话的客户做满意度调查
	 * 	当前只能支持一个分机单路通话的情况
	 * @param csrId  		   话务员id
	 * @param callDirection  调查方向
	 * @return	
	 * 			"success" ： 调查成功 
	 * 			"fail" ： 调查失败 
	 * 			"unbridge" ： 当前坐席没有建立通话
	 */
	@Override
	public String investigation(Long csrId, String callDirection){
		String bridgedChannel = "";
		String exten = ShareData.userToExten.get(csrId);
		for (String ch : ShareData.peernameAndChannels.get(exten)) {
			ChannelSession channelSession = ShareData.channelAndChannelSession.get(ch);
			if (channelSession != null) {
				String bc = channelSession.getBridgedChannel();
				if (bc != null && !"".equals(bc)) {
					bridgedChannel = bc;
					break;
				}
			}
		}
		
		if("".equals(bridgedChannel)) {
			return "unbridge";
		}
		
		RedirectAction redirectAction = new RedirectAction(bridgedChannel, "investigation", "dafen", 1);
		SetVarAction setvarAction = new SetVarAction(bridgedChannel, "direction", callDirection);
		AmiManagerThread.sendAction(setvarAction);
		boolean issuccess = AmiManagerThread.sendAction(redirectAction);
		
		return issuccess ? "success" : "fail";
	}

}
