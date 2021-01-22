package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.TableKeywordEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeyword;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;

public class TableKeywordServiceImpl implements TableKeywordService{
	
	private TableKeywordEao tableKeywordEao;
	
	// common method 
	
	@Override
	public TableKeyword getTableKeyword(Object primaryKey) {
		return tableKeywordEao.get(TableKeyword.class, primaryKey);
	}

	@Override
	public void saveTableKeyword(TableKeyword tableKeyword) {
		tableKeywordEao.save(tableKeyword);
	}

	@Override
	public void updateTableKeyword(TableKeyword tableKeyword) {
		tableKeywordEao.update(tableKeyword);
	}

	@Override
	public void deleteTableKeyword(TableKeyword tableKeyword) {
		tableKeywordEao.delete(tableKeyword);
	}

	@Override
	public void deleteTableKeywordById(Object primaryKey) {
		tableKeywordEao.delete(TableKeyword.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TableKeyword> loadPageEntitys(int start, int length, String sql) {
		return tableKeywordEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return tableKeywordEao.getEntityCount(sql);
	}

	//Getter and Setter
	public TableKeywordEao getTableKeywordEao() {
		return tableKeywordEao;
	}

	public void setTableKeywordEao(TableKeywordEao tableKeywordEao) {
		this.tableKeywordEao = tableKeywordEao;
	}
	
	/**
	 * 取得域内所有Table可以使用的关键字
	 * @param domain
	 * @return
	 */
	public List<String> getAllStrByDomain(Domain domain){
		return tableKeywordEao.getAllStrByDomain(domain);
	}
	/**
	 * 取得域内所有Table可以使用的关键字对象
	 * @param domain
	 * @return
	 */
	public List<TableKeyword> getAllByDomain(Domain domain){
		return tableKeywordEao.getAllByDomain(domain);
	}
}
