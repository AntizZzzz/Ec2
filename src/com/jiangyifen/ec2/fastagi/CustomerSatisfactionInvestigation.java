package com.jiangyifen.ec2.fastagi;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.CustomerSatisfactionInvestigationLog;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerSatisfactionInvestigationLogService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 客户挂机后对CSR进行满意度调查的Agi
 * 
 * @author jrh
 */
public class CustomerSatisfactionInvestigation extends BaseAgiScript {
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		String uniqueid = channel.getUniqueId();
		String calleridNum = channel.getVariable("CALLERID(num)");
		String grade = channel.getVariable("grade");
		String direction = channel.getVariable("direction");
		
		if(direction == null || "".equals(direction)) {	// 一般不会出现为空的情况
			direction = "incoming";
		}

		// 设置基础信息
		CustomerSatisfactionInvestigationLog log = new CustomerSatisfactionInvestigationLog();
		log.setDate(new Date());
		log.setCustomerPhoneNum(calleridNum);
		log.setUniqueid(uniqueid);
		log.setGrade(pickOutNumbers(grade));
		log.setDirection(direction);

		// 设置话务员信息
		ChannelSession channelSession = ShareData.channelAndChannelSession
				.get(channel.getName());
		if (channelSession != null) {
			Long bridgedUserId = channelSession.getBridgedUserId();
			log.setCsrId(bridgedUserId);
			String exten = ShareData.userToExten.get(bridgedUserId);
			log.setExten(exten);
			Long domainId = ShareData.userToDomain.get(bridgedUserId);
			log.setDomainId(domainId);
		}

		CustomerSatisfactionInvestigationLogService service = SpringContextHolder.getBean("customerSatisfactionInvestigationLogService");
		service.save(log);
	}

    /**
     * 取出字符串中的数字，如原始数据位 ‘ 135&%￥1676jrh0398  ’, 执行后返回 13516760398
     * @param originalData     原始数据
     * @return
     */
    private String pickOutNumbers(String originalData) {
          String numbers = "";
          originalData = StringUtils.trimToEmpty(originalData);
          for(int i = 0; i < originalData.length(); i++) {
               char c = originalData.charAt(i);
               int assiiCode = (int) c;
               if(assiiCode >= 48 && assiiCode <= 57) {
                    numbers = numbers + c;
               }
          }
          if("".equals(numbers)) {
        	  numbers = "5";
          }
          return numbers;
    }
}
