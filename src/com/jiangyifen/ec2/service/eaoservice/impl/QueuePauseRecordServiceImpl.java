package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.QueuePauseRecordEao;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;

public class QueuePauseRecordServiceImpl implements QueuePauseRecordService {

	private QueuePauseRecordEao queuePauseRecordEao;
	
	//========= enhanced method ========/

//	public List<QueuePauseRecord> getLastPause(String username, String exten) {
//		return queuePauseRecordEao.getLastPause(username, exten);
//	}
//	

	public QueuePauseRecord getLastPauseRecord(String username, String exten, String queue) {
		return queuePauseRecordEao.getLastPauseRecord(username, exten, queue);
	}
	
	//========= common method ========/
	
	
	@Override
	public QueuePauseRecord get(Object primaryKey) {
		return queuePauseRecordEao.get(QueuePauseRecord.class, primaryKey);
	}

	@Override
	public void save(QueuePauseRecord queuePauseRecord) {
		queuePauseRecordEao.save(queuePauseRecord);
	}

	@Override
	public void update(QueuePauseRecord queuePauseRecord) {
		queuePauseRecordEao.update(queuePauseRecord);
	}

	@Override
	public void delete(QueuePauseRecord queuePauseRecord) {
		queuePauseRecordEao.delete(queuePauseRecord);
	}

	@Override
	public void deleteById(Object primaryKey) {
		queuePauseRecordEao.delete(QueuePauseRecord.class, primaryKey);
	}
	
	@Override
	public List<QueuePauseRecord> getAllQueuePauseRecords(String sql) { //XKP
		return queuePauseRecordEao.getAllQueuePauseRecords(sql);
	}
	
	@Override
	public List<Object[]> getRecordsByNativeSql(String sql){//XKP
		return queuePauseRecordEao.getRecordsByNativeSql(sql);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<QueuePauseRecord> loadPageEntitiesByNativeSql(int start, int length, String nativeSql) {//XKP
		List<QueuePauseRecord> pauseDetailRecordList=new ArrayList<QueuePauseRecord>();
		List list=queuePauseRecordEao.loadPageEntitiesByNativeSql(start, length, nativeSql);
		for (int i = 0; i < list.size(); i++) {      // 将数组转化为QueuePauseRecord对象
			Object[] objs = (Object[]) list.get(i);
			String deptName = objs[0].toString();    // 部门名称
			String userName = (String) objs[1];      // 用户名
			String queue = (String) objs[2];          // 队列
			String reason = (String) objs[3];         // 置忙原因
			Date pauseDate = (Date) objs[4];          // 置忙时间
			Date unpauseDate = (Date) objs[5];        // 置闲时间
			QueuePauseRecord queuePauseRecord = new QueuePauseRecord();
			queuePauseRecord.setDeptName(deptName);
			queuePauseRecord.setUsername(userName);
			queuePauseRecord.setQueue(queue);
			queuePauseRecord.setReason(reason);
			queuePauseRecord.setPauseDate(pauseDate);
			queuePauseRecord.setUnpauseDate(unpauseDate);
			pauseDetailRecordList.add(queuePauseRecord);
		}
		return pauseDetailRecordList;
	}

	@Override
	public int getEntityCountByNativeSql(String nativeSql) {//XKP
		return queuePauseRecordEao.getEntityCountByNativeSql(nativeSql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QueuePauseRecord> loadPageEntities(int start, int length, String sql) {//XKP
		return queuePauseRecordEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) { //XKP
		return queuePauseRecordEao.getEntityCount(sql);
	}
	//======== getter setter ========/
	
	public QueuePauseRecordEao getQueuePauseRecordEao() {
		return queuePauseRecordEao;
	}
	
	
	public void setQueuePauseRecordEao(QueuePauseRecordEao queuePauseRecordEao) {
		this.queuePauseRecordEao = queuePauseRecordEao;
	}

	


}
