package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.eao.QueueEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;

public class QueueEaoImpl extends BaseEaoImpl implements QueueEao {
	
	@Override
	public void save(Queue queue) {
		getEntityManager().persist(queue);
	}
	
	@Override
	public Queue update(Queue queue) {
		return getEntityManager().merge(queue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Queue> getAllByDomain(Domain domain) {
		return getEntityManager().createQuery("select q from Queue as q where q.domain.id = " + domain.getId()+" order by q.name asc").getResultList();
	}
	
	/** jrh 获取指定域中的所有普通队列或自动外呼队列 ,chb 项目选择queue */
	@SuppressWarnings("unchecked")
	@Override
	public List<Queue> getAllByDomain(Long domainId, boolean isnotAutoDial) {
		return getEntityManager().createQuery("select q from Queue as q where q.domain.id = " + domainId + " and q.isModifyable = "+isnotAutoDial+" order by q.name asc").getResultList();
	}
	
//	@SuppressWarnings("unchecked")
//	public boolean existByName(String name) {
//		List<Queue> list = getEntityManager().createQuery("select q from Queue as q where q.name = '" + name +"'").getResultList();
//		if(list.size() > 0) {
//			return true;
//		}
//		return false;
//	}
	
	@SuppressWarnings("unchecked")
	public boolean existByName(String name, Domain domain) {
		List<Queue> list = getEntityManager().createQuery("select q from Queue as q where q.name = '" + name +"' and q.domain.id = " + domain.getId()).getResultList();
		if(list.size() > 0) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Queue> getAllByMusicOnHold(MusicOnHold moh, Domain domain) {
		List<Queue> list = getEntityManager().createQuery("select q from Queue as q where q.musiconhold.id = " +moh.getId()+ " and q.domain.id = " + domain.getId()+" order by q.name asc").getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Queue getDefaultQueueByDomain(Long domainId) {
		List<Queue> list = getEntityManager().createQuery("select q from Queue as q where q.isDefaultQueue = true and q.domain.id = " + domainId).getResultList();
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getMaxQueueName() {
		List<Queue> queues = getEntityManager().createQuery("select q from Queue as q where q.name = " 
				+ "(select max(q2.name) from Queue as q2)").getResultList();
		if(queues.size() > 0) {
			return Long.parseLong(queues.get(0).getName());
		}
		return 900000L;
	}
	
	/** jrh 获取指定域下，所有可用的队列（非自动外呼队列、没有被其他项目使用的队列） */
	@SuppressWarnings("unchecked")
	@Override
	public List<Queue> getAllUseableSimpleQueueByDomain(Domain domain) {
		String sql1 = "select queue_id from ec2_markering_project where queue_id is not null and domain_id = "+domain.getId();
		List<Long> unuseableQueueIds = getEntityManager().createNativeQuery(sql1).getResultList();
		String removeQueueIdSql = "";
		int size = unuseableQueueIds.size();
		if(size > 0) {
			removeQueueIdSql = " and q.id not in (";
			for(int i = 0; i < size; i++) {
				if( i == (size -1) ) {
					removeQueueIdSql += unuseableQueueIds.get(i) +") ";
				} else {
					removeQueueIdSql += unuseableQueueIds.get(i) +", ";
				}
			}
		}
		String sql2 = "select q from Queue as q where q.domain.id = " +domain.getId()+" and q.isModifyable = true"+removeQueueIdSql;
		return getEntityManager().createQuery(sql2).getResultList();
	}

	/**
	 * chb
	 * 通过队列名取得队列
	 * @param queueName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Queue getQueueByQueueName(String queueName) {
		List<Queue> queues=new ArrayList<Queue>();
		try {
			queues=getEntityManager().createQuery("select q from Queue as q where q.name='"+queueName+"'").getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(queues.size()>0){
			return queues.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkDeleteAbleByIvrAction(String queueName, Long domainId) {
		// 先查出所有模板IVR 的编号
		String menuJpql = "select id from ec2_ivr_menu where domain_id = "+domainId+" and ivrmenutype = "+IVRMenuType.template.getIndex()+" order by id desc";
		List<Long> tempMenuIds = this.getEntityManager().createNativeQuery(menuJpql).getResultList();
		tempMenuIds.add(0L);
		
		String nativeSql = "select count(*) from ec2_ivr_action where domain_id = "+domainId+" and queuename = '"+queueName
				+"' and ivrmenu_id not in ("+StringUtils.join(tempMenuIds, ",")+")";
		
		Long count = (Long) this.getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		if(count > 0) {
			return false;
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Queue getByName(String queueName, Long domainId) {
		String jpql = "select q from Queue as q where q.name='"+queueName+"' and q.domain.id = " + domainId;
		List<Queue> queues = getEntityManager().createQuery(jpql).getResultList();
		if(queues.size()>0){
			return queues.get(0);
		}
		
		return null;
	}
	
}
