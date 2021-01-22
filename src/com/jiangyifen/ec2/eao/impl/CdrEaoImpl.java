package com.jiangyifen.ec2.eao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.eao.CdrEao;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Telephone;

public class CdrEaoImpl extends BaseEaoImpl implements CdrEao {
	
	/**
	 * chb
	 * 根据serviceRecord(客服记录取得CDR的集合)
	 * @param serviceRecord
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Cdr> getRecordByServiceRecord(CustomerServiceRecord serviceRecord) {
		//对日期的解析
		Date createDate=serviceRecord.getCreateDate();
		Long domainId = serviceRecord.getDomain().getId();
		
		// 从录音开始到执行弹屏程序，保留10时间
		Date fromDate = new Date(createDate.getTime() - 10*60*1000);
		Date toDate = new Date(createDate.getTime() + 10*60*1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fromDateStr = sdf.format(fromDate);
		String toDateStr = sdf.format(toDate);
		
		//对电话的解析
		CustomerResource customerResource = serviceRecord.getCustomerResource();
		if(customerResource == null) {	// 正常情况下一般不存在，为防止后期手动操作数据库，导致的问题
			return new ArrayList<Cdr>();
		}
		Set<Telephone> phoneNumbers = customerResource.getTelephones();
		if(phoneNumbers.size() == 0) {	// 正常情况下一般不存在，为防止后期手动操作数据库，导致的问题
			return new ArrayList<Cdr>();
		}
		Iterator<Telephone> phoneNumberIter = phoneNumbers.iterator();
		
		StringBuilder sb=new StringBuilder();
		while(phoneNumberIter.hasNext()){
			String phoneNumberStr = phoneNumberIter.next().getNumber();
			sb.append(" cdr.src='"+phoneNumberStr+"' or cdr.src='0"+phoneNumberStr+"' or cdr.destination='"+phoneNumberStr+"' or cdr.destination='0"+phoneNumberStr+"'"); 
			if(phoneNumberIter.hasNext()){
				sb.append(" or");
			}
		}
		
		//根据电话和日期进行匹配的Sql
		String sql="select cdr from Cdr cdr where cdr.domainId = " +domainId+ " and ("+sb.toString()+") and cdr.endTimeDate>='"+fromDateStr+"' and cdr.endTimeDate<='"+toDateStr+"'";
		List<Cdr> cdrList=getEntityManager().createQuery(sql).getResultList();
		return cdrList;
	}
	
	/**
	 * jrh	获取话务员最近的通话记录
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Cdr getNearestCdrByUserId(Long userId, Long domainId) {
		List<Cdr> cdrs = getEntityManager().createQuery("select c from Cdr as c where (c.srcUserId = " +userId+ " or c.destUserId = " +userId+ ") and c.domainId = " +domainId+ 
				" and c.startTimeDate = ( select max(c2.startTimeDate) from Cdr as c2 where (c2.srcUserId = " +userId+ " or c2.destUserId = " +userId+ ") and c2.domainId = " +domainId+ ")" ).getResultList();
		if(cdrs.size() > 0) {
			return cdrs.get(0);
		}
		return null;
	}

	/**
	 * chb 通过订单取得CDR
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Cdr> getRecordByOrder(Order order) {
		//对日期的解析
				Date createDate=order.getGenerateDate();
				Long domainId = order.getDomain().getId();
				
				// 从录音开始到执行弹屏程序，保留40秒时间
				Date fromDate = new Date(createDate.getTime() - 10*60*1000);
				Date toDate = new Date(createDate.getTime() + 10*60*1000);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String fromDateStr = sdf.format(fromDate);
				String toDateStr = sdf.format(toDate);
				
//				String dateStr=new SimpleDateFormat("yyyy-MM-dd").format(createDate);
//				String fromDateStr=dateStr+" 00:00:00";
//				String toDateStr=dateStr+" 23:59:59";
//				
				//对电话的解析
				Set<Telephone> phoneNumbers = order.getCustomerResource().getTelephones();
				Iterator<Telephone> phoneNumberIter = phoneNumbers.iterator();
				
				StringBuilder sb=new StringBuilder();
				while(phoneNumberIter.hasNext()){
					String phoneNumberStr = phoneNumberIter.next().getNumber();
					sb.append(" cdr.src='"+phoneNumberStr+"' or cdr.src='0"+phoneNumberStr+"' or cdr.destination='"+phoneNumberStr+"' or cdr.destination='0"+phoneNumberStr+"'"); 
					if(phoneNumberIter.hasNext()){
						sb.append(" or");
					}
				}
				
				//根据电话和日期进行匹配的Sql
				String sql="select cdr from Cdr cdr where cdr.domainId = " +domainId+ " and ("+sb.toString()+") and cdr.endTimeDate>='"+fromDateStr+"' and cdr.endTimeDate<='"+toDateStr+"'";
				List<Cdr> cdrList=getEntityManager().createQuery(sql).getResultList();
				return cdrList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Cdr> getCdrByJpql(String jpql) {
		return this.getEntityManager().createQuery(jpql).getResultList();
	}

	/** <br/> jrh 根据原生Sql 获取 记录信息 <br/> @param nativeSql <br/> @return */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getInfosByNativeSql(String nativeSql) {
		return this.getEntityManager().createNativeQuery(nativeSql).getResultList();
	}
	
}
