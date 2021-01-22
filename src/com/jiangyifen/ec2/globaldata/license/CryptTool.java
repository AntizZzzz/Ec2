package com.jiangyifen.ec2.globaldata.license;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/** 
 * 加密解密 
 * @author chb
 */  
public class CryptTool {  
   
    /** 
     * 将二进制转化为16进制字符串 
     *  
     * @param b 
     *            二进制字节数组 
     * @return String 
     */  
    public String byte2hex(byte[] b) {  
        String hs = "";  
        String stmp = "";  
        for (int n = 0; n < b.length; n++) {  
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));  
            if (stmp.length() == 1) {  
                hs = hs + "0" + stmp;  
            } else {  
                hs = hs + stmp;  
            }  
        }  
        return hs.toUpperCase();  
    }  
    /** 
     * 十六进制字符串转化为2进制 
     *  
     * @param hex 
     * @return 
     */  
    public byte[] hex2byte(String hex) {  
        byte[] ret = new byte[8];  
        byte[] tmp = hex.getBytes();  
        for (int i = 0; i < 8; i++) {  
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);  
        }  
        return ret;  
    }  
    /** 
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF 
     *  
     * @param src0 
     *            byte 
     * @param src1 
     *            byte 
     * @return byte 
     */  
    public static byte uniteBytes(byte src0, byte src1) {  
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))  
                .byteValue();  
        _b0 = (byte) (_b0 << 4);  
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))  
                .byteValue();  
        byte ret = (byte) (_b0 ^ _b1);  
        return ret;  
    }  
    //=====================//

    
    //=====================//
    /** 
     * 创建密匙 
     *  
     * @param algorithm 
     *            加密算法,可用 DES,DESede,Blowfish 
     * @return SecretKey 秘密（对称）密钥 
     */  
    public SecretKey createSecretKey(String algorithm) {  
        // 声明KeyGenerator对象  
        KeyGenerator keygen;  
        // 声明 密钥对象  
        SecretKey deskey = null;  
        try {  
            // 返回生成指定算法的秘密密钥的 KeyGenerator 对象  
            keygen = KeyGenerator.getInstance(algorithm);  
            // 生成一个密钥  
            deskey = keygen.generateKey();  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
        // 返回密匙  
        return deskey;  
    } 
    
    
    
    /** 
     * 根据密匙进行DES加密 
     *  
     * @param key 
     *            密匙 
     * @param info 
     *            要加密的信息 
     * @return String 加密后的信息 
     */  
    public String encryptToDES(SecretKey key, String info) {  
        // 定义 加密算法,可用 DES,DESede,Blowfish  
        String Algorithm = "DES";  
        // 加密随机数生成器 (RNG),(可以不写)  
        SecureRandom sr = new SecureRandom();  
        // 定义要生成的密文  
        byte[] cipherByte = null;  
        try {  
            // 得到加密/解密器  
            Cipher c1 = Cipher.getInstance(Algorithm);  
            // 用指定的密钥和模式初始化Cipher对象  
            // 参数:(ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE,UNWRAP_MODE)  
            c1.init(Cipher.ENCRYPT_MODE, key, sr);  
            // 对要加密的内容进行编码处理,  
            cipherByte = c1.doFinal(info.getBytes());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // 返回密文的十六进制形式  
        return byte2hex(cipherByte);  
    }  
    /** 
     * 根据密匙进行DES解密 
     *  
     * @param key 
     *            密匙 
     * @param sInfo 
     *            要解密的密文 
     * @return String 返回解密后信息 
     */  
    public String decryptByDES(SecretKey key, String sInfo) {  
        // 定义 加密算法,  
        String Algorithm = "DES";  
        // 加密随机数生成器 (RNG)  
        SecureRandom sr = new SecureRandom();  
        byte[] cipherByte = null;  
        try {  
            // 得到加密/解密器  
            Cipher c1 = Cipher.getInstance(Algorithm);  
            // 用指定的密钥和模式初始化Cipher对象  
            c1.init(Cipher.DECRYPT_MODE, key, sr);  
            // 对要解密的内容进行编码处理  
            cipherByte = c1.doFinal(hex2byte(sInfo));  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        // return byte2hex(cipherByte);  
        return new String(cipherByte);  
    }  
    
    /**
     * 用我们的秘钥解密的过程
     * @param cryptTool
     * @param mac
     * @param outdate
     */
    private static void decryptionMacAndOutdate(CryptTool cryptTool,SecretKey secretKey,String mac,String outdate) {
    	System.out.println();
    	System.out.println();
    	System.out.println("==============解密查看===============");
    	String macCrypt =cryptTool.decryptByDES(secretKey, mac);  
    	System.out.println("mac密文："+mac);
    	System.out.println("mac解密："+macCrypt);
    	
    	System.out.println();
    	
    	String outdateCrypt = cryptTool.decryptByDES(secretKey, outdate);
    	System.out.println("outdate密文："+outdate);
    	System.out.println("outdate解密："+outdateCrypt);
    	System.out.println("=============================");
    	System.out.println();
    	System.out.println();
    } 
    
    /**
     * 用我们的秘钥加密的过程
     * @param cryptTool
     * @param mac
     * @param outdate
     */
	private static void generateMacAndOutdate(CryptTool cryptTool,SecretKey secretKey,String mac,String outdate) {
//		String hexMac=cryptTool.byte2hex(mac.getBytes());
//		String hexOutdate=cryptTool.byte2hex(outdate.getBytes());
		System.out.println("------------");
		System.out.println(mac);
		System.out.println(outdate);
		System.out.println("------------");
		
    	System.out.println();
    	System.out.println();
    	System.out.println("==============生成秘钥===============");
    	String macCrypt = cryptTool.encryptToDES(secretKey, mac);
    	System.out.println("mac原文："+mac);
    	System.out.println("mac加密："+macCrypt);
    	
    	System.out.println();
    	
    	String outdateCrypt = cryptTool.encryptToDES(secretKey, outdate);
    	System.out.println("outdate原文："+outdate);
    	System.out.println("outdate加密："+outdateCrypt);
    	System.out.println("=============================");
    	System.out.println();
    	System.out.println();
	} 
    
	  
    /** 
     * 测试 
     *  
     * @param args 
     * @throws Exception 
     * @throws InvalidKeyException 
     */  
    public static void main(String[] args) throws Exception {  
//    	A20E6BCD6126D9A1  hex byte
    	CryptTool cryptTool = new CryptTool();
    	byte[] keybytes=cryptTool.hex2byte("A20E6BCD6126D9A1");
    	SecretKey secretKey= new SecretKeySpec(keybytes, "DES");  
    	
    	
    	
    	String mac="00:0C:29:16:5C:CE";
    	String outdate="2013";
    	generateMacAndOutdate(cryptTool,secretKey,mac,outdate);
    	
    	String mac2="018CEC2460B32FD336256C768E97A4F52DDB1CCD662D04B4";
    	String outdate2="16B5A245EF17165D";
    	decryptionMacAndOutdate(cryptTool,secretKey,mac2,outdate2);
    }
}  
        



