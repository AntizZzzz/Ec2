package com.jiangyifen.ec2.service.csr.ami.impl;

import org.asteriskjava.manager.action.BridgeAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.service.csr.ami.DoBridgeChannelsService;

/**
 * 直接让两个通道建立通话
 * @author jrh
 *  2013-7-10
 */
public class DoBridgeChannelsServiceImpl implements DoBridgeChannelsService {

	@Override
	public boolean doBridgeChannels(String channel1, String channel2) {
		//如果为空直接返回
		if(channel1==null || channel1.equals("") || channel2==null || channel2==null){
			return false;
		}
		
		//创建转接bridgeAction
		BridgeAction bridgeAction = new BridgeAction(channel1, channel2);
		return AmiManagerThread.sendAction(bridgeAction);
	}

	@Override
	public boolean doBridgeChannels(String channel1, String channel2,
			Boolean tone) {
		//如果为空直接返回
		if(channel1==null || channel1.equals("") || channel2==null || channel2==null){
			return false;
		}
		
		//创建转接bridgeAction
		BridgeAction bridgeAction = new BridgeAction(channel1, channel2, tone);
		return AmiManagerThread.sendAction(bridgeAction);
	}

}
