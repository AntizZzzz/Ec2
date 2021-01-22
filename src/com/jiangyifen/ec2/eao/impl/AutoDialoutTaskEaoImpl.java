package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.eao.AutoDialoutTaskEao;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;

public class AutoDialoutTaskEaoImpl extends BaseEaoImpl implements AutoDialoutTaskEao {
	
	/**
	 * chb
	 * 取出所有的自动外呼Task
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AutoDialoutTask> getAll(Domain domain) {
		String sql="select adt from AutoDialoutTask as adt where adt.domain.id="+domain.getId();
		return getEntityManager().createQuery(sql).getResultList();
	}
	
	/**
	 * chb
	 * 在构造完时调用，由Spring调用，将自动外呼中正在进行的任务置为暂停
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void initSetAllTaskToPause() {
		String sql="select adt from AutoDialoutTask as adt where adt.autoDialoutTaskStatus=?1";
		List<AutoDialoutTask> taskList=getEntityManager().createQuery(sql).setParameter(1,AutoDialoutTaskStatus.RUNNING).getResultList();
		for(AutoDialoutTask autoDialoutTask:taskList){
			autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.PAUSE);
			getEntityManager().merge(autoDialoutTask);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutoDialoutTask> getAllByDialoutType(Domain domain,
			String dialoutType) {
		String sql="select adt from AutoDialoutTask as adt where adt.domain.id="+domain.getId()+ " and adt.dialoutType = '"+dialoutType+"'";
		return getEntityManager().createQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutoDialoutTask> getAllByDialoutType(Long domainId, String dialoutType, AutoDialoutTaskStatus autoDialoutTaskStatus) {
		String sql = "select adt from AutoDialoutTask as adt where adt.domain.id = " + domainId + " and adt.dialoutType = '" + dialoutType + "' and adt.autoDialoutTaskStatus = :autoDialoutTaskStatus";
		return getEntityManager().createQuery(sql).setParameter("autoDialoutTaskStatus", autoDialoutTaskStatus).getResultList();
	}
	
}
