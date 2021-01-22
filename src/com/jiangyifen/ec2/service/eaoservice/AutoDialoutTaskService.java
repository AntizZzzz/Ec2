package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;


public interface AutoDialoutTaskService extends FlipSupportService<AutoDialoutTask>{
	
	// enhanced method 
	/**
	 * chb
	 * 在构造完时调用，由Spring调用，将自动外呼中正在进行的任务置为暂停
	 */
	public void initSetAllTaskToPause();
	
//	 extends CommonTable.TableService
	public AutoDialoutTask get(Object primaryKey);
	@Transactional
	public void save(AutoDialoutTask autoDialoutTask);
	@Transactional
	public AutoDialoutTask update(AutoDialoutTask autoDialoutTask);
	@Transactional
	public void delete(AutoDialoutTask autoDialoutTask);
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<AutoDialoutTask> loadPageEntities(int start,int length,String sql);
	
	@Transactional
	public int getEntityCount(String sql);
	
	/**
	 * chb
	 *  取出所有已经分配到指定自动外呼务中的Csr集合
	 * @param autoDialoutTask
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<User> getCsrsByAutoDialoutTask(AutoDialoutTask autoDialoutTask,
			Domain domain);
	/**
	 * chb
	 * 取出所有的自动外呼任务，包括语音群发
	 */
	@Transactional
	public List<AutoDialoutTask> getAll(Domain domain);
	
	/**
	 * jrh
	 *  获取指定域下的某一类型的所有自动外呼任务
	 *  由于实体中的类型存储的就是汉字，所以这里的 dialoutType 值也必须传汉字（目前类型有两种：自动外呼、语音群发）
	 * @param domain 指定域
	 * @param dialoutType 任务类型
	 * @return
	 */
	@Transactional
	public List<AutoDialoutTask> getAllByDialoutType(Domain domain, String dialoutType);
	
	/**
	 * jinht
	 * 获取指定域下的某一类型的所有某一自动外呼的状态的自动外呼任务
	 * 由于实体中的类型存储的就是汉字，所以这里的 dialoutType 值也必须传汉字（目前类型有两种：自动外呼、语音群发）
	 * @param domainId
	 * @param dialoutType
	 * @param autoDialoutTaskStatus
	 * @return
	 */
	@Transactional
	public List<AutoDialoutTask> getAllByDialoutType(Long domainId, String dialoutType, AutoDialoutTaskStatus autoDialoutTaskStatus);

}
