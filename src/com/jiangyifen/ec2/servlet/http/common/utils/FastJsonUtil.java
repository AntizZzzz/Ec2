package com.jiangyifen.ec2.servlet.http.common.utils;


import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

/**
 * 
 * JSON 转换工具类
 *
 * @author jinht
 *
 * @date 2015-6-18 下午3:25:25 
 *
 */
public class FastJsonUtil {

	private static SerializeConfig mapping = new SerializeConfig();
	// 日期的格式
	private static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	
	public static String toJson(Object jsonText) {
		return JSON.toJSONString(jsonText, SerializerFeature.WriteDateUseDateFormat);
	}
	
	public static String toJson(String jsonText) {
		mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
		return JSON.toJSONString(jsonText, mapping);
	}
}
