package com.jiangyifen.ec2.fastagi;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 记录录音文件语音名
 * @author
 */
public class StoreRecoreFilename extends BaseAgiScript {
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		//主叫电话号码
		String callerIdnum=channel.getVariable("CALLERID(num)");
		String uniqueId=channel.getUniqueId();
		String dateStr=sdf.format(new Date());
		String recoreFilename=new StringBuilder().append(dateStr).append("-").append(callerIdnum).append("-").append("UNKNOWN").append("-").append(uniqueId).append(".wav").toString();
		
		//存储uniqueid和录音文件名的对应关系
		ShareData.recordFileName.put(uniqueId, recoreFilename);
	}
}
