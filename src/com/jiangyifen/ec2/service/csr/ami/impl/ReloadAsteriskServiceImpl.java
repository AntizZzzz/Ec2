package com.jiangyifen.ec2.service.csr.ami.impl;

import org.asteriskjava.manager.action.CommandAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;

/**
 * 重新加载Asterisk 的配置操作Service
 * 
 * @author jrh
 */
public class ReloadAsteriskServiceImpl implements ReloadAsteriskService {

	@Override
	public void reloadAsterisk() {
		// 呼叫Action
		CommandAction commandAction = new CommandAction("reload");
		AmiManagerThread.sendAction(commandAction);
		// //新建连接并发送Action
		// String ip= Config.props.getProperty(Config.AMI_IP);
		// String username=Config.props.getProperty(Config.AMI_USERNAME);
		// String password=Config.props.getProperty(Config.AMI_PWD);
		// ManagerConnectionFactory factory = new ManagerConnectionFactory(ip,
		// username, password);
		// ManagerConnection managerConnection =
		// factory.createManagerConnection();
		//
		// try {
		// managerConnection.login();
		// managerConnection.sendAction(commandAction,
		// Config.AMI_DEFAULT_TIMEOUT);
		// }catch(Exception e){
		// logger.error(e.getMessage() +"Asterisk 配置文件重新加载失败！", e);
		// }finally{
		// try {
		// String state = managerConnection.getState().name();
		// if("CONNECTED".equals(state) || "RECONNECTING".equals(state)) {
		// managerConnection.logoff();
		// }
		// } catch (IllegalStateException e) {
		// logger.warn("Asterisk 配置文件重新加载出现异常！");
		// }
		// }
	}

	@Override
	public void reloadSip() {
		// 呼叫Action
		CommandAction commandAction = new CommandAction("sip reload");
		AmiManagerThread.sendAction(commandAction);
	}

	@Override
	public void reloadMoh() {
		// 呼叫Action
		CommandAction commandAction = new CommandAction("moh reload");
		AmiManagerThread.sendAction(commandAction);

	}

	@Override
	public void reloadQueue() {
		// 呼叫Action
		CommandAction commandAction = new CommandAction("queue reload all");
		AmiManagerThread.sendAction(commandAction);
	}
	
	@Override
	public void reloadVoicemail() {
		CommandAction commandAction = new CommandAction("voicemail reload");
		AmiManagerThread.sendAction(commandAction);
	}
	
}
