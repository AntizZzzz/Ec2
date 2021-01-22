package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CallAfterHandleLogEao;
import com.jiangyifen.ec2.entity.CallAfterHandleLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CallAfterHandleLogService;

/**
 * @Description 描述：话后处理服务类 Service 实现类
 *
 * @author  JRH
 * @date    2014年6月24日 上午11:00:23
 * @version v1.0.0
 */
public class CallAfterHandleLogServiceImpl implements CallAfterHandleLogService {

	private CallAfterHandleLogEao callAfterHandleLogEao;

	//========= common method ========/
	
	
	@Override
	public CallAfterHandleLog get(Object primaryKey) {
		return callAfterHandleLogEao.get(CallAfterHandleLog.class, primaryKey);
	}

	@Override
	public void save(CallAfterHandleLog callAfterHandleLog) {
		callAfterHandleLogEao.save(callAfterHandleLog);
	}

	@Override
	public void update(CallAfterHandleLog callAfterHandleLog) {
		callAfterHandleLogEao.update(callAfterHandleLog);
	}

	@Override
	public void delete(CallAfterHandleLog callAfterHandleLog) {
		callAfterHandleLogEao.delete(callAfterHandleLog);
	}

	@Override
	public void deleteById(Object primaryKey) {
		callAfterHandleLogEao.delete(CallAfterHandleLog.class, primaryKey);
	}
	
	
	//======== getter setter ========/
	
	public CallAfterHandleLogEao getCallAfterHandleLogEao() {
		return callAfterHandleLogEao;
	}
	
	
	public void setCallAfterHandleLogEao(CallAfterHandleLogEao callAfterHandleLogEao) {
		this.callAfterHandleLogEao = callAfterHandleLogEao;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CallAfterHandleLog> loadPageEntities(int start, int length,String sql) {
 		return callAfterHandleLogEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return callAfterHandleLogEao.getEntityCount(sql);
	}


	//========= enhanced method ========/

	@Override
	public CallAfterHandleLog getLastLogByUid(Long userId) {
		return callAfterHandleLogEao.getLastLogByUid(userId);
	}

	@Override
	public boolean createNewLog(User user, String exten, String direction) {
		return callAfterHandleLogEao.createNewLog(user, exten, direction);
	}

	@Override
	public boolean finishLastLogByUid(Long userId, String direction) {
		return callAfterHandleLogEao.finishLastLogByUid(userId, direction);
	}
	
}
