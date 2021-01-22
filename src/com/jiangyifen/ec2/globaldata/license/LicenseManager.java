package com.jiangyifen.ec2.globaldata.license;

import java.io.InputStream;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemLicence;

/**
 * 基于 mac+salt 的系统 License 认证工具
 * 与 license.properties 文件配置实用
 * @author chb
 *
 */
public class LicenseManager {
	private static final Logger logger = LoggerFactory.getLogger(LicenseManager.class);
	
	public static final String  LICENSE_FILE="/license.properties";

	//是否通过验证
	public static final String  LICENSE_VALID="valid";
	public static final String  LICENSE_INVALID="invalid";
	public static String  LICENSE_VALIDATE_RESULT="license_validate_result"; //LICENSE_VALID 有效， LICENSE_INVALID 无效  --> 字符串

	//license 相关
	
	public static String  LICENSE_DATE="license_date";
	public static String  LICENSE_COUNT="license_count";
	public static String  LICENSE_LOCALMD5="license_localmd5";
	
	//从文件中读取的信息
	private static Map<String,String> licenseMap=new ConcurrentHashMap<String, String>();
	
	//其它
	public static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static{
		loadLicenseFile(LicenseManager.LICENSE_FILE.substring(1));
	}
	
	/**
	 * 解析认证License
	 */
	public static Map<String, String> licenseValidate(Map<String, String> localLicenseMap) {
		try {
			
			//根据配置信息和系统的多块网卡比对校验license有效性（是否被恶意篡改）
			Boolean isValide = false;
			List<String> macAddressList = getMacAddressList();
			for (String macAddress : macAddressList) {
				Boolean tmpIsValide = licenseValidate(localLicenseMap.get(LicenseManager.LICENSE_LOCALMD5), macAddress,
						localLicenseMap.get(LicenseManager.LICENSE_DATE),localLicenseMap.get(LicenseManager.LICENSE_COUNT));
				if (tmpIsValide == true) {
					isValide = true;
					break;
				}
			}
			
			//如果是有效license，给系统返回相应的授权信息，系统可以根据授权信息进行控制决策
			if(isValide){
				localLicenseMap.put(LicenseManager.LICENSE_VALIDATE_RESULT, LICENSE_VALID);
			}else{
				localLicenseMap.put(LicenseManager.LICENSE_VALIDATE_RESULT, LICENSE_INVALID);
			}
			return localLicenseMap;
		} catch (Exception e) {
			e.printStackTrace();
//			localLicenseMap=new HashMap<String, String>();
			return localLicenseMap;
		}
	}
	
	/**
	 * 解析认证License
	 */
	public static Map<String, String> licenseValidate() {
		try {
			
			//根据配置信息和系统的多块网卡比对校验license有效性（是否被恶意篡改）
			Boolean isValide = false;
			List<String> macAddressList = getMacAddressList();
			for (String macAddress : macAddressList) {
				Boolean tmpIsValide = licenseValidate(licenseMap.get(LicenseManager.LICENSE_LOCALMD5), macAddress,
						licenseMap.get(LicenseManager.LICENSE_DATE),licenseMap.get(LicenseManager.LICENSE_COUNT));
				if (tmpIsValide == true) {
					isValide = true;
					break;
				}
			}
			
			//如果是有效license，给系统返回相应的授权信息，系统可以根据授权信息进行控制决策
			if(isValide){
				licenseMap.put(LicenseManager.LICENSE_VALIDATE_RESULT, LICENSE_VALID);
			}else{
				licenseMap.put(LicenseManager.LICENSE_VALIDATE_RESULT, LICENSE_INVALID);
			}
			return licenseMap;
		} catch (Exception e) {
			e.printStackTrace();
//			licenseMap=new HashMap<String, String>();
			return licenseMap;
		}
	}
	
	/**
	 * 生成 License
	 * @param mac 传入mac
	 * @param date 出入授权到期日
	 * @param count 传入授权数量（可能是坐席，可能是分机）
	 * @return
	 */
	public static String generateLicense(String mac,String date,String count) {
		// mac、date、count格式认证
		Boolean isWellformat=regexMatchCheck(mac,date,count);
		if(isWellformat){
			//continue
		}else{
			return "Input params format error";
		}
		
		// 系统计算的32位md5 结果
		String saltedLicense = mac.toLowerCase() + LicenseManager.LICENSE_SALT + date + count;
		String bit32_md5_result = bit32_md5(saltedLicense);
		
		//返回完整的License信息
		StringBuilder stringBuilder=new StringBuilder();
		stringBuilder.append("license_date="+date);
		stringBuilder.append("\r\n");
		stringBuilder.append("license_count="+count);
		stringBuilder.append("\r\n");
		stringBuilder.append("license_localmd5="+bit32_md5_result);
		
		return stringBuilder.toString();
	}
	
	private static String  LICENSE_SALT=SystemLicence.serialNumber;
		
	/**
	 * 读取License配置文件，简单的读取功能，没有认证
	 * @return
	 */
	public static void loadLicenseFile(String licenseFilename){
		try {
			//初始化配置文件信息
			Properties props = new Properties();
			InputStream inputStream = LicenseManager.class.getClassLoader().getResourceAsStream(licenseFilename);
			props.load(inputStream);
			inputStream.close();
			
			//授权数量
			String license_count = (String)props.get(LicenseManager.LICENSE_COUNT);
			license_count=license_count==null?"":license_count;
			licenseMap.put(LicenseManager.LICENSE_COUNT, license_count);

			//授权日期
			String license_date = (String)props.get(LicenseManager.LICENSE_DATE);
			license_date=license_date==null?"":license_date;
			licenseMap.put(LicenseManager.LICENSE_DATE, license_date);
			
			//授权验证码
			String license_localmd5 = (String)props.get(LicenseManager.LICENSE_LOCALMD5);
			license_localmd5=license_localmd5==null?"":license_localmd5;
			licenseMap.put(LicenseManager.LICENSE_LOCALMD5, license_localmd5);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 按照"xx-xx-xx-xx-xx-xx"格式，获取本机MAC地址
	 * 
	 * @return
	 * @throws Exception
	 */
	private static List<String> getMacAddressList() {
		List<String> macAddressList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> ni = NetworkInterface
					.getNetworkInterfaces();

			while (ni.hasMoreElements()) {
				NetworkInterface netI = ni.nextElement();

				byte[] bytes = netI.getHardwareAddress();
				if (netI.isUp() && netI != null && bytes != null
						&& bytes.length == 6) {
					StringBuffer sb = new StringBuffer();
					for (byte b : bytes) {
						// 与11110000作按位与运算以便读取当前字节高4位
						sb.append(Integer.toHexString((b & 240) >> 4));
						// 与00001111作按位与运算以便读取当前字节低4位
						sb.append(Integer.toHexString(b & 15));
						sb.append("-");
					}
					sb.deleteCharAt(sb.length() - 1);
					macAddressList.add(sb.toString().toLowerCase());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return macAddressList;
	}

	/**
	 * license有效性判读
	 * 
	 * @param localmd5
	 * @param mac
	 * @param date
	 * @param count
	 * @return
	 */
	private static Boolean licenseValidate(String localmd5, String mac,
			String date, String count) {
		// 本地配置文件md5 认证码
		localmd5 = StringUtils.trimToEmpty(localmd5);

		// 读取元素
		mac = StringUtils.trimToEmpty(mac);
		date = StringUtils.trimToEmpty(date);
		count = StringUtils.trimToEmpty(count);

		// mac、date、count格式认证
		Boolean isWellformat=regexMatchCheck(mac,date,count);
		if(isWellformat){
			//continue
		}else{
			return false;
		}
		
		// 系统计算的32位md5 结果
		String saltedLicense = mac + LicenseManager.LICENSE_SALT + date + count;
		String bit32_md5_result = bit32_md5(saltedLicense);

		// 结果与本地存储的信息比较
		if (localmd5.equals(bit32_md5_result)) {
			//判断是否过期
//			LicenseManager.LICENSE_DATE
			try {
				Date expiredate=simpleDateFormat.parse(date);
				if(new Date().after(expiredate)){
					logger.warn("chb: License expires, license expiredate is "+date);
					return false;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				logger.warn("chb: License date parse exception");
				return false;
			}
			return true;
		} else {
			logger.warn("chb: License and bit32_md5_result not match , changed !!!");
			return false;
		}
	}

	/**
	 * 验证是不是格式正确
	 * @param mac
	 * @param date
	 * @param count
	 * @return
	 */
	public static Boolean regexMatchCheck(String mac, String date, String count) {
		if(StringUtils.isEmpty(mac)||StringUtils.isEmpty(date)||StringUtils.isEmpty(count)){
			return false;
		}
		String mac_regex="^[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}-[a-fA-F0-9]{2}$";
		String date_regex="^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$"; //不精确
		String count_regex="^\\d+$"; //不精确
		
		return mac.matches(mac_regex)&&date.matches(date_regex)&&count.matches(count_regex);
	}
	
	/**
	 * 32 为md5 加密
	 * 
	 * @param plainText
	 * @return
	 */
	private static String bit32_md5(String plainText) {
		if (StringUtils.isEmpty(plainText)) {
			return null;
		}

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
			// never happen
			return null;
		}
	}

}
