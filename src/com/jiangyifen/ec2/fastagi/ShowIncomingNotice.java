package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
/**
 * 更改用户密码的AGI
 * @author
 *
 */
public class ShowIncomingNotice extends BaseAgiScript {
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		//拨打但是没有打通的电话
		String calledExten=channel.getVariable("resourceManagerExten");
		String callerIdNumber=request.getCallerIdNumber();

		Long userId = null;
		if(calledExten != null && !"".equals(calledExten)) {
			userId=ShareData.extenToUser.get(calledExten);
		}

		if(userId!=null){
			CsrToolBar toolBar = (CsrToolBar)ShareData.csrToToolBar.get(userId);
			if(toolBar != null) {	// 如果接收消息的用户在线，则直接刷新用户的界面
				if(callerIdNumber.startsWith("0")) {
					callerIdNumber = callerIdNumber.substring(1);
				}
				toolBar.updateMissCallNotice(callerIdNumber);
			}
		}
	}
}
