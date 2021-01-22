package com.jiangyifen.ec2.servlet.http.common.utils;

import java.text.DateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson工具
 * @author chb
 *
 */
public class GsonUtil { //AD
	
	private static Gson gson;
	
	static{
		JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
				return src == null ? null : new JsonPrimitive(src.getTime());
			}
		};
		
		gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).registerTypeAdapter(Date.class, ser).create();
	}
	
	/**
	 * 转换成单引号的json字符串
	 * @return
	 */
	public static String toJson(Object object){
		String result=gson.toJson(object);
		return result;
	}
	
	/**
	 * 取得gson对象
	 * @return
	 */
	public static Gson getGson() {
		return gson;
	}
	
}
