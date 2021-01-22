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

public class AkkyTest3 {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 5, 01);

		// createSql(cal);

		statisticMobileLocal();
	}

	@SuppressWarnings("unused")
	private static void createSql(Calendar cal) {
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		System.out.println("-- ");
		System.out.println("-- 2014-06-01 09:00:00 - 2014-06-24 22:00:00");
		System.out.println("-- ");
		System.out.println("-- ===========================================================");
		System.out.println("-- ===========================================================");
		allIncomingByTimeScope(cal, "09", "22");

		// System.out.println();
		// System.out.println();
		// System.out.println();
		// cal.set(2014, 5, 01);
		//
		// System.out.println("--
		// ===========================================================");
		// System.out.println("--
		// ===========================================================");
		// System.out.println("-- ");
		// System.out.println("-- 2014-06-14 11:00:00 - 2014-06-21 12:00:00");
		// System.out.println("-- ");
		// System.out.println("--
		// ===========================================================");
		// System.out.println("--
		// ===========================================================");
		// allIncomingByTimeScope(cal, "11" , "12");
	}

	private static void allIncomingByTimeScope(Calendar cal, String startHour, String endHour) {

		String startTime = " " + startHour + ":00:00";
		String endTime = " " + endHour + ":00:00";

		String dt = sdf.format(cal.getTime());

		for (int i = 0; i < 24; i++) {
			String sql = "select src from cdr as c " + " where c.startTimeDate >= '" + dt + startTime + "' "
					+ " and c.startTimeDate <= '" + dt + endTime + "' "
					+ " and ( c.destDeptId in (244, 1, 105, 156, 190, 209, 302, 254, 255) "
					+ " or (c.srcDeptId is null and c.destDeptId is null) ) " + " and c.outlinename = '60650825' "
					+ " and c.cdrDirection = 2" + " and queuename = ''" + " and c.domainId = 1 " + " order by src asc;";
			System.out.println();
			System.out.println("-- ===========================================================");
			System.out.println("-- ");
			System.out.println("-- " + dt + startTime + " 至  " + dt + endTime);
			System.out.println("-- ");
			System.out.println();
			System.out.println("select timestamp  '" + dt + startTime + "'; ");
			System.out.println("select timestamp  '" + dt + endTime + "'; ");
			System.out.println();
			System.out.println();
			System.out.println(sql);

			cal.add(Calendar.DAY_OF_YEAR, +1);
			dt = sdf.format(cal.getTime());
		}

	}

	private static void statisticMobileLocal() {
		String[] locations = { "D:/workspace/ec2/WebContent/WEB-INF/applicationContextConfig.xml",
				"D:/workspace/ec2/WebContent/WEB-INF/applicationContextEao.xml",
				"D:/workspace/ec2/WebContent/WEB-INF/applicationContextEaoService.xml" };

		@SuppressWarnings("unused")
		ApplicationContext ac = new FileSystemXmlApplicationContext(locations);

		readAndWrite();
	}

	private static void readAndWrite() {
		try {
			String filename = "D://miss_log_phone.txt"; // 读取成功
			File file = new File(filename);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String startH = "09";
			String endH = "22";

			File file2 = new File("D://601_624_not_inqueue_and_localarea_" + startH + "_to_" + endH + "_per_day.txt");
			if (file2.exists()) {
				file2.delete();
			}
			FileWriter fw = new FileWriter(file2);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(fw);

			HashMap<String, Integer> statisticMap = new HashMap<String, Integer>();

			long stime = System.currentTimeMillis();

			Calendar cal = Calendar.getInstance();
			cal.set(2014, 5, 1);

			int i = 0;
			String phoneNum = null;
			while ((phoneNum = br.readLine()) != null) {
				i++;
				phoneNum = StringUtils.trimToEmpty(phoneNum);

				if (phoneNum != null && phoneNum.contains("-") && !phoneNum.equals("---------------------")) {
					System.out.println(phoneNum);
				}
				if ("src".equals(phoneNum)) {
					out.append("\r\n");
					out.append("\r\n");
					out.append("\r\n");
					for (String key : statisticMap.keySet()) {
						out.append(key + "\t" + statisticMap.get(key));
						out.append("\r\n");
					}
					statisticMap.clear();

					String startTime = " " + startH + ":00:00";
					String endTime = " " + endH + ":00:00";
					String dt = sdf.format(cal.getTime());

					out.append("\r\n");
					out.append(dt + startTime + " 至  " + dt + endTime);
					out.append("\r\n");
					out.append("\r\n");
					cal.add(Calendar.DAY_OF_YEAR, +1);
				}

				if ("".equals(phoneNum) || phoneNum.startsWith("(") || phoneNum.contains("-")
						|| phoneNum.startsWith("-") || phoneNum.startsWith("time") || phoneNum.startsWith("s")
						|| !phoneNum.matches("\\d+")) {
					continue;
				}

				String area = "未知";
				@SuppressWarnings("unused")
				String mobileLocStr = "号码归属地：" + area;
				MobileLoc mobileLoc = MobileLocUtil.getMobileAreaCode(phoneNum);
				if (mobileLoc != null) {
					area = mobileLoc.getMobileArea();
					String[] as = area.split(" ");
					if (as.length > 0) {
						area = as[0];
					}
					mobileLocStr = "号码归属地：" + area;
				}

				Integer count = statisticMap.get(area);
				if (count == null) {
					statisticMap.put(area, 1);
				} else {
					statisticMap.put(area, ++count);
				}

				// 去掉详单
				// out.append(phoneNum+"\t"+mobileLocStr+"\r\n");

				// System.out.println(phoneNum+"\t"+mobileLocStr+"\r\n");
			}

			out.append("\r\n");
			out.append("\r\n");
			out.append("\r\n");
			for (String key : statisticMap.keySet()) {
				out.append(key + "\t" + statisticMap.get(key));
				out.append("\r\n");
			}
			System.out.println(cal.getTime());
			System.out.println(phoneNum);

			statisticMap.clear();

			System.out.println("执行完成，共处理行数 ：" + i);

			long etime = System.currentTimeMillis();
			System.out.println("总耗时：" + ((etime - stime) / 1000) + " 秒");

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
