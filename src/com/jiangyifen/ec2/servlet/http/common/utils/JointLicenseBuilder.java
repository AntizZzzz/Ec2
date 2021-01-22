package com.jiangyifen.ec2.servlet.http.common.utils;

import com.jiangyifen.ec2.utils.MD5Encrypt;

/**
 * @Description 描述：对接许可证生成器，根据指定信息生成许可证
 *
 * @author  JRH
 * @date    2014年8月8日 上午10:00:01
 */
public class JointLicenseBuilder {

	private static Long domainId = 1L;
	private static String systemSequnce = "2014-0403-4113-4844";
	
	public static void main(String[] args) {
		
		String accessId = systemSequnce+"HTDM"+domainId;
		accessId = accessId.replace("-", "");
		
		String accessKey = generateAccessKey(accessId, domainId, systemSequnce);

		String company = "益盟公司";
		
		StringBuffer strBf = new StringBuffer();
		strBf.append("\r\n");
		strBf.append("accessId = "+accessId);
		strBf.append("\r\n");
		strBf.append("accessKey = "+accessKey);
		strBf.append("\r\n");
		strBf.append("\r\n");
		strBf.append("\r\n");
		strBf.append("SELECT last_value FROM seq_ec2_joint_license_id;");
		strBf.append("\r\n");
		strBf.append("\r\n");
		strBf.append("INSERT INTO ec2_joint_license (\"id\", \"accessid\", \"accesskey\", \"createdate\", \"description\", \"domainid\") ");
		strBf.append(" VALUES (nextval('seq_ec2_joint_license_id'), '"+accessId+"', '"+accessKey+"', now(), '"+company+"调用EC2接口的许可证书', "+domainId+");");
		strBf.append("\r\n");
		
		System.out.println(strBf.toString());
		
	}

	/**
	 * @Description 描述：根据基础信息生成访问秘钥
	 *
	 * @author  JRH
	 * @date    2014年8月8日 上午10:42:06
	 * @param accessId			访问编号
	 * @param domainId			租户所属域的编号
	 * @param systemSequnce		系统序列号
	 * @return String			返回访问秘钥
	 */
	private static String generateAccessKey(String accessId, Long domainId, String systemSequnce) {

		String saltedLicense = accessId + "-HANTIAN-" + systemSequnce + "-DOMAINID-"+domainId;
		
		String key = MD5Encrypt.getMD5Str(saltedLicense, "UTF-8");
		key = MD5Encrypt.getMD5Str(key, "UTF-8");
		
		return key.toUpperCase();
	}
	
}
