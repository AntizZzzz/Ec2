package com.jiangyifen.ec2.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具
 * @author jrh
 *  2013-7-19
 */
public class MD5Encrypt {

	/**
	 * 将指定字符串按指定的编码格式加密
	 * @param string	待加密的字符串
	 * @param codeType	编码格式
	 * @return
	 */
	public static String getMD5Str(String string, String codeType) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(string.getBytes(codeType));
			byte[] byteArray = messageDigest.digest();  
			StringBuffer md5StrBuff = new StringBuffer();
			
			// 把密文转换成十六进制的字符串形式
			for (int i = 0; i < byteArray.length; i++) { 
			    if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
			        md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
			    else  
			        md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
			}
			return md5StrBuff.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}  
	}
	
}
