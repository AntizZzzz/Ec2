package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.TableInfoVo;

/**
 * Service接口：数据表信息
 * @author JHT
 */
public interface TableInfoVoService {
	/**
	 * 获取所有的数据表的大小, 可根据表的名称进行查询
	 * @param tableName 表的名称
	 * @return
	 */
	@Transactional
	public List<TableInfoVo> getTableInfoVoList(String tableName);
	
	/**
	 * 获取所有表的总数据的大小 MB
	 * @return
	 */
	@Transactional
	public String getSumTableInfoSize();
	
	/**
	 * 获取表的总数
	 * @return
	 */
	@Transactional
	public int getTableInfoCount();
	
	/**
	 * 根据表的名称，来获取表的总条数
	 * @param name
	 * @return
	 */
	@Transactional
	public long getTableColumnCountByTableName(String name);

}
