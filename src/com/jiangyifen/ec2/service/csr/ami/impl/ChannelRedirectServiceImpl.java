package com.jiangyifen.ec2.service.csr.ami.impl;


import org.asteriskjava.manager.action.RedirectAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;

/**
 * 转接操作Service
 * @author	jrh
 */
public class ChannelRedirectServiceImpl implements ChannelRedirectService {
	
	@Override
	public boolean redirectExten(String channel, String exten) {
		//如果为空直接返回
		if(channel==null||channel.equals("")){
			return false;
		}
		//创建转接redirectAction
		RedirectAction redirectAction = new RedirectAction(channel, "outgoing", exten, 1);
		return AmiManagerThread.sendAction(redirectAction);
	}

	@Override
	public boolean redirectQueue(String channel, String queueName) {
		//如果为空直接返回
		if(channel==null||channel.equals("")){
			return false;
		}

		RedirectAction redirectAction = new RedirectAction(channel, "outgoing", queueName, 1);
		return AmiManagerThread.sendAction(redirectAction);
	}

	@Override
	public boolean redirectTelephone(String channel, String telephone) {
		//如果为空直接返回
		if(channel==null||channel.equals("")){
			return false;
		}
				
		RedirectAction redirectAction = new RedirectAction(channel, "outgoing", telephone, 1);
		return AmiManagerThread.sendAction(redirectAction);
	}

	@Override
	public boolean redirectCommonExtension(String channel, String extension) {
		//如果为空直接返回
		if(channel==null||channel.equals("")){
			return false;
		}
				
		RedirectAction redirectAction = new RedirectAction(channel, "outgoing", extension, 1);
		return AmiManagerThread.sendAction(redirectAction);
	}

	@Override
	public boolean redirectDoubleChannels(String channel, String extraChannel, String extension) {
		//如果为空直接返回
		if(channel==null||channel.equals("")){
			return false;
		}

		RedirectAction redirectAction = new RedirectAction(channel, extraChannel, "outgoing", extension, 1);
		return AmiManagerThread.sendAction(redirectAction);
	}
	
}
