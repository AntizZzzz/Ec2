package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.eao.BaseEao;

/**
 * Eao接口：数据表信息
 * @author JHT
 */
public interface TableInfoVoEao extends BaseEao {

	/**
	 * 根据SQL语句进行查询数据库表的大小信息
	 * @param sql SQL语句
	 * @return 查询到的数据集合
	 */
	public List<String> getTableNameListOrSeqNameList(String sql);
	
	/**
	 * 根据SQL语句查询返回一条数据信息
	 */
	public Long getTableSizeOrMaxId(String sql);
	
}
