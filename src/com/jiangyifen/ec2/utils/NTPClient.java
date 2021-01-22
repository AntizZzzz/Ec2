package com.jiangyifen.ec2.utils;

import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public final class NTPClient {

	public static Date getNtpDate() {
		
		String timeAddress = "0.pool.ntp.org";
		Date date = null;
		NTPUDPClient client = null;
		try {
			client = new NTPUDPClient();
			client.setDefaultTimeout(10000);
			client.open();
			InetAddress hostAddr = InetAddress.getByName(timeAddress);
			TimeInfo info = client.getTime(hostAddr);
			
// 这里计算出来的结果还是当前的系统时间，并不是真正的从网络中获取到的时间				
//			long destTime = info.getReturnTime();
//			TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
//			date = destNtpTime.getDate();

			date = info.getMessage().getTransmitTimeStamp().getDate();
			
		} catch (Exception e) {
			/*e.printStackTrace();*/
			System.out.println("logger info ------>> Time synchronization failure![时间同步失败 - "+timeAddress+"]");
		} finally {
			client.close();
		}

		return date;
	}

}
