package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.eao.AutoDialoutTaskEao;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;

public class AutoDialoutTaskServiceImpl implements AutoDialoutTaskService,FlipSupportService<AutoDialoutTask> {
	
	private AutoDialoutTaskEao autoDialoutTaskEao;
	
	// enhanced method

	/**
	 * chb
	 * 在构造完时调用，由Spring调用，将自动外呼中正在进行的任务置为暂停
	 */
	@Override
	public void initSetAllTaskToPause(){
		autoDialoutTaskEao.initSetAllTaskToPause();
	}
	
	// common method
	
	@Override
	public AutoDialoutTask get(Object primaryKey) {
		return autoDialoutTaskEao.get(AutoDialoutTask.class, primaryKey);
	}

	@Override
	public void save(AutoDialoutTask autoDialoutTask) {
		autoDialoutTaskEao.save(autoDialoutTask);
	}

	@Override
	public AutoDialoutTask update(AutoDialoutTask autoDialoutTask) {
		return (AutoDialoutTask)autoDialoutTaskEao.update(autoDialoutTask);
	}

	@Override
	public void delete(AutoDialoutTask autoDialoutTask) {
		autoDialoutTaskEao.delete(autoDialoutTask);
	}

	@Override
	public void deleteById(Object primaryKey) {
		autoDialoutTaskEao.delete(AutoDialoutTask.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AutoDialoutTask> loadPageEntities(int start, int length,
			String sql) {
		return autoDialoutTaskEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return autoDialoutTaskEao.getEntityCount(sql);
	}

	//getter and setter

	public AutoDialoutTaskEao getAutoDialoutTaskEao() {
		return autoDialoutTaskEao;
	}

	public void setAutoDialoutTaskEao(AutoDialoutTaskEao autoDialoutTaskEao) {
		this.autoDialoutTaskEao = autoDialoutTaskEao;
	}
	
	/**
	 * chb
	 *  取出所有已经分配到指定自动外呼任务任务中的Csr集合
	 * @param autoDialoutTask
	 * @param domain
	 * @return 指派是只能指派CSR，故查处来的结果应该不会有管理员
	 */
//	TODO 这里为什么需要这样去CSR 集合 直接autoDialoutTask.getUsers(); 不行吗？！
	@Override
	public List<User> getCsrsByAutoDialoutTask(AutoDialoutTask autoDialoutTask,
			Domain domain) {
		Set<User> csrs= autoDialoutTaskEao.get(AutoDialoutTask.class, autoDialoutTask.getId()).getUsers();
		return new ArrayList<User>(csrs);
	}
	
	/**
	 * chb
	 * 取出所有的自动外呼任务、还有语音外呼任务
	 */
	@Override
	public List<AutoDialoutTask> getAll(Domain domain) {
		return autoDialoutTaskEao.getAll(domain);
	}
	
	/** jrh 获取指定域下的某一类型的所有自动外呼任务（目前类型有两种：自动外呼、语音群发） */
	@Override
	public List<AutoDialoutTask> getAllByDialoutType(Domain domain, String dialoutType) {
		return autoDialoutTaskEao.getAllByDialoutType(domain, dialoutType);
	}

	@Override
	public List<AutoDialoutTask> getAllByDialoutType(Long domainId, String dialoutType, AutoDialoutTaskStatus autoDialoutTaskStatus) {
		return autoDialoutTaskEao.getAllByDialoutType(domainId, dialoutType, autoDialoutTaskStatus);
	}

}
