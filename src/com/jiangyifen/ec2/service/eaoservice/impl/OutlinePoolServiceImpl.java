package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.OutlinePoolEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolService;

public class OutlinePoolServiceImpl implements OutlinePoolService {
	
	private OutlinePoolEao outlinePoolEao;
	
	private CommonEao commonEao;

	@SuppressWarnings("unchecked")
	@Override
	public List<OutlinePool> loadPageEntities(int start, int length, String sql) {
		return commonEao .loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return commonEao .getEntityCount(sql);
	}

	@Override
	public void save(OutlinePool outlinePool) {
		outlinePoolEao.save(outlinePool);
	}

	@Override
	public OutlinePool update(OutlinePool outlinePool) {
		// TODO Auto-generated method stub
		return outlinePoolEao.update(outlinePool);
	}

	@Override
	public List<OutlinePool> getAllByDomain(Domain domai) {
		// TODO Auto-generated method stub
		return outlinePoolEao.getAllByDomain(domai);
	}
	
	@Override
	public void deleteById(Object primaryKey) {
		outlinePoolEao.deleteById(primaryKey);
	}

	@Override
	public boolean existByName(String name, Domain domain) {
		// TODO Auto-generated method stub
		return outlinePoolEao.existByName(name, domain);
	}


	public OutlinePoolEao getOutlinePoolEao() {
		return outlinePoolEao;
	}

	public void setOutlinePoolEao(OutlinePoolEao outlinePoolEao) {
		this.outlinePoolEao = outlinePoolEao;
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}


}
