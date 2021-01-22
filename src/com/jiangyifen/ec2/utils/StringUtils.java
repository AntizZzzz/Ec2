package com.jiangyifen.ec2.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
	/**
	 * 字符串摘要
	 * 
	 * @param srcString
	 *            输入字符串
	 * @param algorithm
	 *            算法类型，MD5,SHA-1,SHA-256,默认使用SHA-256
	 * @return
	 */
	public static String hash(String srcString, String algorithm) {
		MessageDigest md = null;
		String dstString = null;

		byte[] bt = srcString.getBytes();
		try {
			md = MessageDigest.getInstance(algorithm);
			md.update(bt);
			dstString = bytes2Hex(md.digest()); // to HexString
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return dstString;
	}

	private static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	public static void main(String args[]) {
		String s;
		s = StringUtils.hash("jiangyifen", "SHA-256");
		System.out.println(s);
		s = StringUtils.hash("jiangyifen", "md5");
		System.out.println(s);
		s = StringUtils.hash("jiangyifen", "sha-1");
		System.out.println(s);

	}
}