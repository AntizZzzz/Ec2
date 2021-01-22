package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.enumtype.ExecuteType;

public interface CommonEao extends BaseEao {
	/**
	 * 执行sql语句
	 * @param sql
	 * @return
	 */
	public Object excuteSql(String sql,ExecuteType executeType);

	public Object excuteNativeSql(String nativeSql,ExecuteType executeType);
	public List<?> loadStepRows(String nativeSql,int stepSize);
}
