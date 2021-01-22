package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.OutlinePoolEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.entity.Queue;

public class OutlinePoolEaoImpl extends BaseEaoImpl implements OutlinePoolEao {

	@Override
	public void save(OutlinePool outlinePool) {
		getEntityManager().persist(outlinePool);
	}

	@Override
	public OutlinePool update(OutlinePool outlinePool) {
		return getEntityManager().merge(outlinePool);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OutlinePool> getAllByDomain(Domain domain) {
		return getEntityManager()
				.createQuery(
						"select q from OutlinePool as q where q.domain.id = " + domain.getId() + " order by q.id desc")
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean existByName(String name, Domain domain) {
		List<Queue> list = getEntityManager().createQuery(
				"select q from OutlinePool as q where q.name = '" + name + "' and q.domain.id = " + domain.getId())
				.getResultList();
		if (list.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void deleteById(Object primaryKey) {
		String sql = "delete from ec2_outline_pool_outline_link where poolid = " + primaryKey;
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		this.delete(OutlinePool.class, primaryKey);

	}

}
