package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.TableInfoVoEao;
import com.jiangyifen.ec2.eao.impl.BaseEaoImpl;

/**
 * Eao实现类：数据表信息
 * @author JHT
 */
public class TableInfoVoEaoImpl  extends BaseEaoImpl implements TableInfoVoEao{

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTableNameListOrSeqNameList(String sql) {
		return this.getEntityManager().createNativeQuery(sql).getResultList();
	}

	@Override
	public Long getTableSizeOrMaxId(String sql) {
		return Long.valueOf(this.getEntityManager().createNativeQuery(sql).getSingleResult().toString());
	}

}
