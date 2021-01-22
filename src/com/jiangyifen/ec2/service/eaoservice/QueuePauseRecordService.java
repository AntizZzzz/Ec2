package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.service.common.FlipSupportNativeSqlService;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface QueuePauseRecordService extends FlipSupportNativeSqlService<QueuePauseRecord>,FlipSupportService<QueuePauseRecord>{
	
	//========= enhanced method ========/
//	
//	/**
//	 * 根据指定用户名和分机号,获取其对应的最新的所有置忙记录
//	 * 	该记录的置忙时间不为空，但是置闲时间为空，并且该记录之后不存在指定用户和分机号做的置忙时间和置闲时间都不为空的记录
//	 * 	也就是说，获取的记录时指定用户用指定分机最后对不同队列（同一时间）做的记录
//	 * @param username	用户名
//	 * @param exten		分机号
//	 * @return List<QueuePauseRecord>	置忙记录集合
//	 */
//	@Transactional
//	public List<QueuePauseRecord> getLastPause(String username, String exten);
	
	/**
	 * 根据指定用户名和分机号,获取该用户在使用的分机对应的指定队列的最新置忙记录
	 *  假设当前用户名：lili、 分机：8500、队列名为：queue1
	 *  下面的所有操作都是满足用户名为 lili、 分为 8500、队列名为 queue1 的记录中进行的
	 *  	一、从所有置忙时间不为空，置闲时间为空的记录中找到置忙时间最大的记录 pauseRecord 
	 *  	二、pauseRecord 要满足条件：不存在比pauseRecord 的时间大，并且置忙时间和置闲时间都不为空的记录
	 *  	三、不存在一条以上置忙时间为空，并且置闲时间不为空，并且队列名不是 queue1 的，而且置忙时间比 pauseRecord 的置忙时间大的记录
	 * @param username	用户名
	 * @param exten		分机号
	 * @param queue		队列名
	 * @return QueuePauseRecord	置忙记录集合
	 */
	@Transactional
	public QueuePauseRecord getLastPauseRecord(String username, String exten, String queue);
	
	
	//========= common method ========/
	
	@Transactional
	public QueuePauseRecord get(Object primaryKey);
	
	@Transactional
	public void save(QueuePauseRecord queuePauseRecord);

	@Transactional
	public void update(QueuePauseRecord queuePauseRecord);

	@Transactional
	public void delete(QueuePauseRecord queuePauseRecord);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<QueuePauseRecord>  getAllQueuePauseRecords(String sql);//XKP
	
	/**
	 * @param sql
	 * @return 根据原生sql查询出所有的置忙记录
	 */
	@Transactional
	public List<Object[]> getRecordsByNativeSql(String sql);//XKP
}
