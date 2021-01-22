package com.jiangyifen.ec2.service.common;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FlipSupportNativeSqlService<T> {
	/**
	 * 分页加载
	 * 
	 * @param startIndex
	 *            第几个Entity开始
	 * @param pageSize
	 *            查询几个Entity
	 * @param selectSql
	 *            查询语句
	 * @return 查询出的实体的List集合
	 */
	public List<T> loadPageEntitiesByNativeSql(int start, int length,
			String nativeSql);

	/**
	 * 数据库记录条数 (如果查询语句为空字符串 "" ,则查询数据空中对应实体的总记录数；否则按照查询条件进行查询)
	 * 
	 * @param countSql
	 *            查询条数的语句
	 * @return 查询的记录条数
	 */
	public int getEntityCountByNativeSql(String nativeSql);

}
