package com.jiangyifen.ec2.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jiangyifen.ec2.entity.MobileLoc;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;

public class AkkyTest2 {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 5, 14);

//		createSql(cal);
		
		statisticMobileLocal();
	}

	@SuppressWarnings("unused")
	private static void createSql(Calendar cal) {
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		System.out.println("-- ");
		System.out.println("-- 2014-06-14 09:00:00 - 2014-06-21 22:00:00");
		System.out.println("-- ");
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		allIncomingByTimeScope(cal, "09" , "22");
		
		System.out.println();
		System.out.println();
		System.out.println();
		cal.set(2014, 5, 14);
		
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		System.out.println("-- ");
		System.out.println("-- 2014-06-14 11:00:00 - 2014-06-21 12:00:00");
		System.out.println("-- ");
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		allIncomingByTimeScope(cal, "11" , "12");
	}

	private static void allIncomingByTimeScope(Calendar cal, String startHour, String endHour) {
		
		String startTime = " "+startHour+":00:00";
		String endTime = " "+endHour+":00:00";

		String dt = sdf.format(cal.getTime());
		
		for (int i = 0; i < 8; i++) {
			String sql = "select src from cdr as c "
					+ " where c.startTimeDate >= '" + dt + startTime + "' "
					+ " and c.startTimeDate <= '" + dt + endTime + "' "
					+ " and ( c.destDeptId in (244, 1, 105, 156, 190, 209, 302, 254, 255) "
					+ " or (c.srcDeptId is null and c.destDeptId is null) ) "
					+ " and c.outlinename = '60650825' "
					+ " and c.cdrDirection = 2 and c.lastApplication != 'ChanSpy' and c.lastApplication != '' "
					+ " and c.destination != 's' and c.destination != 'check' "
					+ " and c.domainId = 1 "
					+ " order by src asc;";
			System.out.println();
			System.out.println("-- ===========================================================");
			System.out.println("-- ");
			System.out.println("-- "+dt + startTime +" 至  "+dt + endTime);
			System.out.println("-- ");
			System.out.println();
			System.out.println("select timestamp  '"+dt + startTime+"'; ");
			System.out.println("select timestamp  '"+dt + endTime+"'; ");
			System.out.println();
			System.out.println();
			System.out.println(sql);
			
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dt = sdf.format(cal.getTime());
		}
		
	}

	private static void statisticMobileLocal() {
		 String[] locations = {"D:/workspace/ec2/WebContent/WEB-INF/applicationContextConfig.xml",
	                "D:/workspace/ec2/WebContent/WEB-INF/applicationContextEao.xml",
	                "D:/workspace/ec2/WebContent/WEB-INF/applicationContextEaoService.xml"};

		@SuppressWarnings("unused")
		ApplicationContext ac = new FileSystemXmlApplicationContext(locations);
		
		readAndWrite();
	}
	
	private static void readAndWrite() {
		try {
			String filename = "D://miss_log_phone.txt";           // 读取成功
			File file = new File(filename); 
			FileReader fr = new FileReader(file); 
			BufferedReader br = new BufferedReader(fr); 
			
			
			String startH = "09";
			String endH = "22";
			
			File file2 = new File("D://incoming_call_and_localarea_"+startH+"_to_"+endH+".txt");
			FileWriter fw = new FileWriter(file2);
			BufferedWriter bw=new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(fw);

			
			HashMap<String, Integer> statisticMap = new HashMap<String, Integer>();
			
			long stime = System.currentTimeMillis();
			
			Calendar cal = Calendar.getInstance();
			cal.set(2014, 5, 7);
			
            int i = 0;
			String phoneNum  = null ; 
			while ( (phoneNum = br.readLine()) != null) {
				i++;
	            phoneNum = StringUtils.trimToEmpty(phoneNum);
	            
	            if(phoneNum.startsWith("=")) {
	    			startH = "11";
	    			endH = "12";
	    			cal.set(2014, 5, 7);
	    			out.append("\r\n");
	    			out.append("========================================================");
	    			out.append(" 11-12点");
	            	out.append("========================================================");
	            	out.append("\r\n");
	            	out.append("\r\n");
	            	continue;
	            }
	            
	            if("--------------".equals(phoneNum)) {
	            	out.append("\r\n");
	            	for(String key : statisticMap.keySet()) {
	            		out.append(key+"\t"+statisticMap.get(key));
	            		out.append("\r\n");
	            	}
	            	statisticMap.clear();

	        		String startTime = " "+startH+":00:00";
	        		String endTime = " "+endH+":00:00";
	        		String dt = sdf.format(cal.getTime());

	    			cal.add(Calendar.DAY_OF_YEAR, +1);
	    			out.append("\r\n");
	            	out.append(dt + startTime +" 至  "+dt + endTime);
	            	out.append("\r\n");
	            	out.append("\r\n");
	            }
	            
	            if("".equals(phoneNum) || phoneNum.startsWith("(")
	            		|| phoneNum.contains("-") || phoneNum.startsWith("-")
	            		|| phoneNum.startsWith("time") || phoneNum.startsWith("s")
	            		|| !phoneNum.matches("\\d+")) {
	            	continue;
	            }
	            
	            String area = "未知";
	            String mobileLocStr = "号码归属地："+area;
    			MobileLoc mobileLoc = MobileLocUtil.getMobileAreaCode(phoneNum);
    			if(mobileLoc != null) {
    				area = mobileLoc.getMobileArea();
    				String[] as = area.split(" ");
    				if(as.length > 0) {
    					area = as[0];
    				}
					mobileLocStr = "号码归属地："+area;
    			}
	    		
    			Integer count = statisticMap.get(area);
    			if(count == null) {
    				statisticMap.put(area, 1);
    			} else {
    				statisticMap.put(area, ++count);
    			}
    			
//    			去掉详单
//	    		out.append(phoneNum+"\t"+mobileLocStr+"\r\n");
	    		
	    		System.out.println(phoneNum+"\t"+mobileLocStr+"\r\n");
			} 
			
        	out.append("\r\n");
        	for(String key : statisticMap.keySet()) {
        		out.append(key+"\t"+statisticMap.get(key));
        		out.append("\r\n");
        	}
        	
        	statisticMap.clear();
        	
			System.out.println("执行完成，共处理行数 ："+i);

			long etime = System.currentTimeMillis();
			System.out.println("总耗时："+((etime - stime) / 1000)+" 秒" );
			
			br.close(); 
			fr.close();
			
			out.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
