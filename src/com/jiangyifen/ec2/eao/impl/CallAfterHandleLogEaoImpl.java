package com.jiangyifen.ec2.eao.impl;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.CallAfterHandleLogEao;
import com.jiangyifen.ec2.entity.CallAfterHandleLog;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

/**
 * @Description 描述：话后处理日志Eao 接口实现类
 *
 * @author  JRH
 * @date    2014年6月24日 上午10:58:29
 * @version v1.0.0
 */
public class CallAfterHandleLogEaoImpl extends BaseEaoImpl implements CallAfterHandleLogEao {

	@SuppressWarnings("unchecked")
	@Override
	public CallAfterHandleLog getLastLogByUid(Long userId) {
		String jpql = "select e from CallAfterHandleLog as e where e.userId = "+userId+" and e.id = "
				+ "(select max(e1.id) from CallAfterHandleLog as e1 where e.userId= "+userId+")";
		List<CallAfterHandleLog> logs = this.getEntityManager().createQuery(jpql).getResultList();
		if(logs.size() > 0) {
			return logs.get(0);
		}
		return null;
	}

	@Override
	public boolean createNewLog(User user, String exten, String direction) {
		try {
			Department dept = user.getDepartment();
			Domain domain = user.getDomain();
			
			CallAfterHandleLog log = new CallAfterHandleLog();
			log.setDirection(direction);
			log.setExten(exten);
			log.setStartDate(new Date());
			log.setUserId(user.getId());
			log.setUsername(user.getUsername());
			log.setDeptId(dept.getId());
			log.setDeptName(dept.getName());
			log.setDomainId(domain.getId());
			this.getEntityManager().persist(log);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean finishLastLogByUid(Long userId, String direction) {
		try {
//			CallAfterHandleLog log = getLastLogByUid(userId);
//			log.setFinishDate(new Date());
//			this.getEntityManager().merge(log);
			
			String nativeSql = "update ec2_call_after_handle_log set finishDate = now() where userid = "+userId
					+" and finishdate is null and id = (select max(id) from ec2_call_after_handle_log where userid = "+userId+" and direction = '"+direction+"')";
			this.getEntityManager().createNativeQuery(nativeSql).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
