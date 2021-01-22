package com.jiangyifen.ec2.ui.mgr.tabsheet.meetme;

import java.util.List;

import org.asteriskjava.manager.action.OriginateAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.globaldata.ShareData;

public class MeetMeInviteAction {

	private String context = "incoming";
	private Integer priority = 1;
	
	private String exten;
	private String confno="7777";
//	private String outline;

	public String execute(Long domainId, String number) {
//		outline = ShareData.domainToDefaultOutline.get(domainId);
		
		List<String> extens = ShareData.domainToExts.get(domainId);

		if (extens.contains(number)) {
			inviteInline(number);
		} else {
			inviteOutline(number);
		}
		return "success";

	}

	private void inviteOutline(String number) {
		// 呼叫
		// 呼叫Action
		exten = "998" + confno;
		OriginateAction originateAction = new OriginateAction();
//		originateAction.setChannel("SIP/"+number+"@"+outline);
		originateAction.setChannel("SIP/"+number+"@88860847043");
		originateAction.setCallerId(number);
		originateAction.setExten(exten);
		originateAction.setContext(context);
		originateAction.setPriority(1);
		originateAction.setAsync(true);
		AmiManagerThread.sendAction(originateAction);
	}

	private void inviteInline(String number) {
		// 呼叫Action
		exten = "998" + confno;
		OriginateAction originateAction = new OriginateAction();
		originateAction.setChannel("SIP/"+number);
		originateAction.setCallerId(number);
		originateAction.setExten(exten);
		originateAction.setContext(context);
		originateAction.setPriority(1);
		originateAction.setAsync(true);
		AmiManagerThread.sendAction(originateAction);
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setConfno(String confno) {
		this.confno = confno;
	}

	public String getConfno() {
		return confno;
	}

}
