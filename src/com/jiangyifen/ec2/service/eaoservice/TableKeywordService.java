package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeyword;

public interface TableKeywordService {
	// common method 
	@Transactional
	public TableKeyword getTableKeyword(Object primaryKey);
	
	@Transactional
	public void saveTableKeyword(TableKeyword tableKeyword);
	
	@Transactional
	public void updateTableKeyword(TableKeyword tableKeyword);
	
	@Transactional
	public void deleteTableKeyword(TableKeyword tableKeyword);
	
	@Transactional
	public void deleteTableKeywordById(Object primaryKey);
	
	@Transactional
	public List<TableKeyword> loadPageEntitys(int start,int length,String sql);
	
	@Transactional
	public int getEntityCount(String sql);
	
	/**
	 * 取得域内所有Table可以使用的关键字
	 * @param domain
	 * @return
	 */
	public List<String> getAllStrByDomain(Domain domain);
	/**
	 * 取得域内所有Table可以使用的关键字对象
	 * @param domain
	 * @return
	 */
	public List<TableKeyword> getAllByDomain(Domain domain);
}
