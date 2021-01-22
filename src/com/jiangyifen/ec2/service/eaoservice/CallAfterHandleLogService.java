package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CallAfterHandleLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * @Description 描述：话后处理服务类 Service
 *
 * @author  JRH
 * @date    2014年6月24日 上午10:59:58
 * @version v1.0.0
 */
public interface CallAfterHandleLogService extends FlipSupportService<CallAfterHandleLog> {

	
	//========= common method ========/
	
	@Transactional
	public CallAfterHandleLog get(Object primaryKey);
	
	@Transactional
	public void save(CallAfterHandleLog callAfterHandleLog);

	@Transactional
	public void update(CallAfterHandleLog callAfterHandleLog);

	@Transactional
	public void delete(CallAfterHandleLog callAfterHandleLog);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	//========= enhanced method ========/
	
	/**
	 * @Description 描述：获取指定用户最近一次话后处理记录
	 *
	 * @author  jrh
	 * @date    2014年4月2日 下午4:01:03
	 * @param userId
	 * @return CallAfterHandleLog
	 */
	@Transactional
	public CallAfterHandleLog getLastLogByUid(Long userId);
	
	/**
	 * @Description 描述： 创建一条新的话后处理记录
	 *
	 * @author  jrh
	 * @date    2014年4月2日 下午4:00:13
	 * @param user		用户对象
	 * @param exten		分机
	 * @param direction	话后处理的方向
	 * @return boolean	执行成功与否
	 */
	@Transactional
	public boolean createNewLog(User user, String exten, String direction);

	/**
	 * @Description 描述： 终止指定坐席最近的话后处理记录，将finishDate 写入
	 *
	 * @author  jrh
	 * @date    2014年4月2日 下午4:00:13
	 * @param userId	用户编号
	 * @param direction	话后处理的方向
	 * @return boolean	执行成功与否
	 */
	@Transactional
	public boolean finishLastLogByUid(Long userId, String direction);
	
}
