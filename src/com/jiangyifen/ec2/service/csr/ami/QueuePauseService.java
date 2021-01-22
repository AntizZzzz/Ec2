package com.jiangyifen.ec2.service.csr.ami;

import java.util.List;

import com.jiangyifen.ec2.bean.BusHandle;
import com.jiangyifen.ec2.entity.User;

public interface QueuePauseService {
	
	/**
	 * 将指定队列中的分机置忙 或 置闲
	 * @param queue		队列名	如果值为 "all" 则表示将所有队列都置忙或置闲
	 * @param exten		分机号
	 * @param paused	true 表示置忙	false 表示置闲
	 * @param reason	置忙置闲的原因
	 */
	public void pause(String queue, String exten, boolean paused, String reason);
	
	/**
	 * @Description 描述：不需要事务
	 * 						将指定用户，的所有队列执行置忙、置闲操作
	 * 
	 * 如果是置闲： pauseAsteriskReason = '管理员强制置闲' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON
	 * 如果是置忙： pauseAsteriskReason = '管理员强制忙碌' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_PAUSE_RECORD_REASON
	 *
	 * @author  jrh
	 * @date    2014年4月3日 上午10:08:51
	 * @param queueNames			待置忙的队列
	 * @param user					用户对象
	 * @param exten					分机号
	 * @param paused				置忙true、或置闲false 
	 * @param pauseAsteriskReason	传给asterisk的置忙原因【eg. 管理员强制忙碌】
	 * @param pauseRecordReason		用于记录数据的原因【eg. 忙碌】
	 * @return boolean				执行成功与否
	 */
	public boolean pauseAllByUser(List<String> queueNames, User user, String exten, boolean paused, String pauseAsteriskReason, String pauseRecordReason);

	/**
	 * @Description 描述：不需要事务
	 * 						将指定用户，的所有队列执行置忙、置闲操作，并且可以设置是否需要创建置忙记录
	 * 
	 * 如果是置闲： pauseAsteriskReason = '管理员强制置闲' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON
	 * 如果是置忙： pauseAsteriskReason = '管理员强制忙碌' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_PAUSE_RECORD_REASON
	 *
	 * @author  jrh
	 * @date    2014年4月3日 上午10:08:51
	 * @param queueNames			待置忙的队列
	 * @param user					用户对象
	 * @param exten					分机号
	 * @param paused				置忙true、或置闲false 
	 * @param pauseAsteriskReason	传给asterisk的置忙原因【eg. 管理员强制忙碌】
	 * @param pauseRecordReason		用于记录数据的原因【eg. 忙碌】
	 * @param iscreatePauseLog		是否需要创建置忙记录
	 * @return boolean				执行成功与否
	 */
	public boolean pauseAllByUser(List<String> queueNames, User user, String exten, boolean paused, String pauseAsteriskReason, String pauseRecordReason, boolean iscreatePauseLog);
	
	
	/**
	 * @Description 描述：不需要事务
	 * 						将指定用户的所有队列执行置忙、置闲操作
	 * 
	 * 如果是置闲： pauseAsteriskReason = '管理员强制置闲' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON
	 * 如果是置忙： pauseAsteriskReason = '管理员强制忙碌' 或者其他值 ,  pauseRecordReason = GlobalVariable.DEFAULT_PAUSE_RECORD_REASON
	 *
	 * @author  jrh
	 * @date    2014年4月3日 上午10:08:51
	 * @param userId				需要置忙的用户的编号
	 * @param paused				置忙true、或置闲false 
	 * @param pauseAsteriskReason	传给asterisk的置忙原因【eg. 管理员强制忙碌】
	 * @param pauseRecordReason		用于记录数据的原因【eg. 忙碌】
	 * @return BusHandle			返回业务操作结果信息
	 */
	public BusHandle pauseAllByUid(Long userId, boolean paused, String pauseAsteriskReason, String pauseRecordReason);

	/**
	 * @Description 描述：将对应队列下的分机进行置忙或置闲，并根据需要创建置忙置闲的记录 
	 *
	 * @author  JRH
	 * @date    2014年8月11日 下午2:11:21
	 * @param loginUser		待置忙置闲的坐席
	 * @param exten			分机
	 * @param reason		置忙置闲的原因
	 * @param isPaused		置忙或者置闲，true : 置忙, false : 置闲。 
	 * @param isCreateLog	是否需要创建置忙置闲记录
	 * @return BusHandle	返回操作结果
	 */
	public BusHandle pauseAllAndCreateLog(User loginUser, String exten, String reason, boolean isPaused, boolean isCreateLog);
	
}
