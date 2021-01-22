package com.jiangyifen.ec2.globaldata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.service.eaoservice.SmsInfoService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 用来存储所有域的SmsInfo信息
 * @author chb
 */
public class SmsShareData {
	public static Map<Long, SmsInfo> domainToSmsInfo = new ConcurrentHashMap<Long, SmsInfo>();
	static{
		// 加载所有domain的 SmsInfo信息到内存
		SmsInfoService smsInfoService = SpringContextHolder.getBean("smsInfoService");
		List<SmsInfo> allSmsInfoList = smsInfoService.getAll();
		for (SmsInfo smsInfo : allSmsInfoList) {
			domainToSmsInfo.put(smsInfo.getDomain().getId(), smsInfo);
		}
	}
}
