package com.jiangyifen.ec2.fastagi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 判断是否是白名单坐席
 * @author chb
 *
 */
public class WhiteList extends BaseAgiScript{  //AD
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		String calleridnum = request.getCallerIdNumber();
		if(StringUtils.isNumeric(calleridnum)){
			//获取去0的手机号码
			if(calleridnum.startsWith("0")&&calleridnum.length()>5){
				calleridnum=calleridnum.substring(1);
			}
			
			//查找User表，看用户是否是白名单
			CommonService commonService=SpringContextHolder.getBean("commonService");
			//目前只是给天相使用，没有进行域判断 TODO
			String sql="select u from User u where u.phoneNumber ='"+calleridnum+"'";
			List<User> userList=(List<User>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
			if(userList!=null&&userList.size()>0){
				channel.setVariable("ISWHITELIST","IS");
			}
		}else{
			logger.warn("chb: calleridnum should be number");
		}
	}
	
}
