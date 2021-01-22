package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 获取话务员最近的通话对象的电话号码
 * @author jrh
 */
public class GetNearestConnectedNumber extends BaseAgiScript {
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {

		String exten = this.getVariable("CALLERID(num)");
		
		//根据分机取得用户
		Long userId=ShareData.extenToUser.get(exten);
		Long domainId = 0L;
		for(Long key :ShareData.domainToExts.keySet()) {
			if(ShareData.domainToExts.get(key).contains(exten)) {
				domainId = key;
				break;
			}
		}
		
		CdrService cdrService = SpringContextHolder.getBean("cdrService");
		Cdr cdr = cdrService.getNearestCdrByUserId(userId, domainId);
		String nearestConnectedNumber = "";
		if(cdr != null) {
			String direction = cdr.getDestinationContext();
			if("incoming".endsWith(direction)) {
				nearestConnectedNumber = cdr.getSrc();
			} else if("outgoing".endsWith(direction)) {
				nearestConnectedNumber = cdr.getDestination();
			}
		}
		
		this.setVariable("NEARESTCONNECTEDNUMBER", nearestConnectedNumber);
	}
}
