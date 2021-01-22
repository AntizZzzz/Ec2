package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;




public interface AutoDialoutTaskEao extends BaseEao {
	
	/**
	 * chb
	 * 取出所有的自动外呼Task，包括语音群发
	 */
	public List<AutoDialoutTask> getAll(Domain domain);

	/**
	 * chb
	 * 在构造完时调用，由Spring调用，将自动外呼中正在进行的任务置为暂停
	 */
	public void initSetAllTaskToPause();
	
	/**
	 * jrh
	 *  获取指定域下的某一类型的所有自动外呼任务
	 *  由于实体中的类型存储的就是汉字，所以这里的 dialoutType 值也必须传汉字（目前类型有两种：自动外呼、语音群发）
	 * @param domain 指定域
	 * @param dialoutType 任务类型
	 * @return
	 */
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
	public List<AutoDialoutTask> getAllByDialoutType(Long domainId, String dialoutType, AutoDialoutTaskStatus autoDialoutTaskStatus);

}
