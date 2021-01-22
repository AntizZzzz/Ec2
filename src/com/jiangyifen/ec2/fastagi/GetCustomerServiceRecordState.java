package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.Ec2ConfigurationService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 用来获取设置客服记录状态保存方式的Agi
 * @author JHT
 * @date 2014-11-14 上午10:06:20
 */
public class GetCustomerServiceRecordState extends BaseAgiScript {

	private Ec2ConfigurationService ec2ConfigurationService = SpringContextHolder.getBean("ec2ConfigurationService");
	private UserService userService = SpringContextHolder.getBean("userService");
	private Domain domain;
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		String extennumber = channel.getVariable("CALLERID(num)");	// 获取当前分机的号码/手机号
		Long userId = ShareData.extenToUser.get(extennumber);
		domain = userService.get(userId).getDomain();
		
		String name = "TELEPHONE_TO_CREATE_CUSTOMER_SERVICE_RECORD";	// 名字
		String value = "";

		Ec2Configuration configuration = ec2ConfigurationService.getByKey("create_customer_service_record_by_exten_type", domain.getId());
		if(configuration != null && configuration.getValue()){
			value = "fortel";
		}
		
		channel.setVariable(name, value);
		
	}

}
