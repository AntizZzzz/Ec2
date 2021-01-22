package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 检测分机的注册状态
 *
 * @author jinht
 *
 * @date 2015-9-7 下午6:07:29 
 *
 */
public class CheckExtenRegistryStatus extends BaseAgiScript {

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		String exten = request.getParameter("exten");
		ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
		/**
		 * 判断 exten 这个号码是否为分机号, 如果不是分机号的话, 则将注册信息设置为 true.
		 * 因为在转接的时候会把座席呼通的手机号转接到 90000 队列, 如果这里不进行过滤, 转接的时候就会提示分机未注册. 
		 */
		// if(exten != null && exten.length() == 6 && exten.startsWith("8")) {
		// TODO 武睿定制, 分机号改为 6 开头五位数
		if(exten != null && exten.length() == 5 && exten.startsWith("6")) {
			if(extenStatus != null && extenStatus.getIp() != null && extenStatus.getRegisterStatus().startsWith("OK")) {
				this.setVariable("extenRegStatus", "true");
			} else {
				this.setVariable("extenRegStatus", "false");
			}
		} else {
			this.setVariable("extenRegStatus", "true");
		}
	}
	
}
