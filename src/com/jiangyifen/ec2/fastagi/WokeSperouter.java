package com.jiangyifen.ec2.fastagi;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.servlet.woke.other.WokeRouter;
/**
 * chb 20140117 沃客装修网专员路由
 * 在incoming中的 getResourceManagerExten.agi 下添加
 * exten => _x.,n,Agi(agi://${AGISERVERADDR}/wokerouter.agi) ;woke specific router
 * @author chb
 *
 */
public class WokeSperouter extends BaseAgiScript {

	@Override
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		//获取外线号码
		String outlinenum = channel.getVariable("EXTEN");
		//手机或电话号码
		String phonenum = channel.getVariable("CALLERID(num)");
		
		//路由
		String routerReturn=WokeRouter.route(phonenum,outlinenum);
		if(StringUtils.isEmpty(routerReturn)){
			//do nothing
		}else{
			//set channel variable
			channel.setVariable("woke", routerReturn);
		}
	}

}
