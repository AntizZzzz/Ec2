package com.jiangyifen.ec2.service.csr.ami.impl;

import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.HangupAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
/**
 * 挂断操作Service
 * @author
 */
public class HangupServiceImpl implements HangupService {
	
	@Override
	public void hangup(String channel) {
		//如果callerIdNum没有任何Channel，直接返回
		if(channel != null && channel.equals("")){
			return;
		}
		
		//挂断Action
		HangupAction hangupAction = new HangupAction(channel);
		AmiManagerThread.sendAction(hangupAction);
	}

	@Override
	public void hangupByCommand(String channel) {
		//如果callerIdNum没有任何Channel，直接返回
		if(channel != null && channel.equals("")) {
			return;
		}
		
		CommandAction action = new CommandAction();
		action.setCommand("hangup request "+channel);
		AmiManagerThread.sendAction(action);
	}
	
}
