package com.jiangyifen.ec2.service.common;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @Description 描述：分页加载数据，返回List<Object[]>
 * 
 * @author  jrh
 * @date    2013-11-6 下午4:29:29
 * @version
 */
@Transactional
public interface FlipOverNativeSqlService {
	
	/**
	 * 分页加载
	 * 
	 * @param startIndex
	 *           	 第几个Object[]开始
	 * @param pageSize
	 *           	 查询几个Object[]
	 * @param selectSql
	 *           	 查询语句
	 * @return List<Object[]> 
	 * 				查询出的实体的List集合
	 */
	public List<Object[]> loadPageInfosByNativeSql(int startIndex, int pageSize, String nativeSelectSql);

	/**
	 * 数据库记录条数 (如果查询语句为空字符串 "" ,则查询数据空中对应实体的总记录数；否则按照查询条件进行查询)
	 * 
	 * @param nativeCountSql
	 *            查询条数的语句
	 * @return 查询的记录条数
	 */
	public int getInfoCountByNativeSql(String nativeCountSql);

}
