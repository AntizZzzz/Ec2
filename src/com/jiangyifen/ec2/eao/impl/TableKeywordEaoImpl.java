package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.TableKeywordEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeyword;

public class TableKeywordEaoImpl extends BaseEaoImpl implements TableKeywordEao {
	/**
	 * 取得域内所有Table可以使用的关键字
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllStrByDomain(Domain domain){
		List<String> keywords=new ArrayList<String>();								//TableKeyword
		List<TableKeyword> keywordList=getEntityManager().createQuery("select tk from TableKeyword as tk where tk.domain.id = " + domain.getId()).getResultList();
		for(TableKeyword tk:keywordList){
			keywords.add(tk.getColumnName());
		}
		return keywords;
	}
	/**
	 * 取得域内所有Table可以使用的关键字对象
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TableKeyword> getAllByDomain(Domain domain){
		return getEntityManager().createQuery("select tk from TableKeyword as tk where tk.domain.id = " + domain.getId()+" order by tk.id asc").getResultList();
	}

}
