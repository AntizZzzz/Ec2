package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.QueuePauseRecordEao;
import com.jiangyifen.ec2.entity.QueuePauseRecord;

public class QueuePauseRecordEaoImpl extends BaseEaoImpl implements
		QueuePauseRecordEao {

	// ========= enhanced method ========/
	
	@SuppressWarnings("unchecked")
	@Override
	public QueuePauseRecord getLastPauseRecord(String username, String exten, String queue) {
		
//		long l1 = System.currentTimeMillis();
//		String sql2 = "select p from QueuePauseRecord as p "
//								+ "where p.pauseDate is not null and p.unpauseDate is null " 
//								+ "and p.username = '" + username + "' " 
//								+ "and p.sipname = 'SIP/"+ exten + "' " 
//								+ "and p.queue = '" + queue + "' " 
//								+ "and p.pauseDate = (select max(p1.pauseDate) from QueuePauseRecord as p1 "
//									+ "where p1.pauseDate is not null and p1.unpauseDate is null " 
//									+ "and p1.username = '" + username + "' " 
//									+ "and p1.sipname = 'SIP/"+ exten + "' " 
//									+ "and p1.queue = '" + queue + "') "
//								+ "and NOT EXISTS(select p2 from QueuePauseRecord as p2 "
//									+ "where p2.pauseDate is not null and p2.unpauseDate is not null " 
//									+ "and p2.username = '" + username + "' " 
//									+ "and p2.sipname = 'SIP/"+ exten + "' " 
//									+ "and p2.queue = '" + queue + "' " 
//									+ "and p2.pauseDate >= p.pauseDate) ";
//
//		List<QueuePauseRecord> pauseList2 = getEntityManager().createQuery(sql2).getResultList();
//		
//		long l2 = System.currentTimeMillis();
//		System.out.println("--------> "+(l2-l1));
		
		String sql = "select p from QueuePauseRecord as p "
				+ "where p.sipname = 'SIP/"+ exten + "' " 
				+ "and p.username = '" + username + "' " 
				+ "and p.queue = '" + queue + "' " 
				+ "and p.pauseDate is not null and p.unpauseDate is null "
				
				+ "and p.pauseDate = (select max(p1.pauseDate) from QueuePauseRecord as p1 "
				+ "where p1.sipname = 'SIP/"+ exten + "' "
				+ "and p1.username = '" + username + "' " 
				+ "and p1.queue = '" + queue + "' "
				+ "and p1.pauseDate is not null and p1.unpauseDate is null "
				+ ") "
				
				+ "and NOT EXISTS(select p2 from QueuePauseRecord as p2 "
				+ "where p2.pauseDate >= p.pauseDate "
				+ "and p2.queue = '" + queue + "' " 
				+ "and p2.sipname = 'SIP/"+ exten + "' " 
				+ "and p2.username = '" + username + "' " 
				+ "and p2.pauseDate is not null and p2.unpauseDate is not null"
				+ ") ";
		
		List<QueuePauseRecord> pauseList = getEntityManager().createQuery(sql).getResultList();

//		long l3 = System.currentTimeMillis();
//		System.out.println("==============--------> "+(l3-l2));
//		System.out.println();
//		System.out.println(sql);
		
		if(pauseList.size() > 0) {
			return pauseList.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QueuePauseRecord> getAllQueuePauseRecords(String sql) {//XKP
		
		return getEntityManager().createQuery(sql).getResultList();
	}
	
	/**
	 * @param sql
	 * @return 根据原生sql查询出所有的置忙记录
	 * @author xkp
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getRecordsByNativeSql(String sql) {

		return getEntityManager().createNativeQuery(sql).getResultList();
	}
}
