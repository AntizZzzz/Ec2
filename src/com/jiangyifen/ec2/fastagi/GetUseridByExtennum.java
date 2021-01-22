package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 
 * <p>根据分机获取用户id</p>
 * 
 * <p>语音信箱使用</p>
 *
 * @version $Id: GetUseridByExtennum.java 2014-6-19 下午2:14:10 chenhb $
 *
 */
public class GetUseridByExtennum extends BaseAgiScript {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		String extennum = request.getParameter("extennum");
		Long userid = ShareData.extenToUser.get(extennum);
		if(userid==null){
			//do nothing
			logger.warn("chenhb: userid from extennum --> "+extennum+" is null");
		}else{
			channel.setVariable("voicemailUserid", userid.toString());
		}
	}

}
