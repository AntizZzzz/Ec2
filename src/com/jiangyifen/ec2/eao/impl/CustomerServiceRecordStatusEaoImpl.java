package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.CustomerServiceRecordStatusEao;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.ServiceRecordStatus2DepartmentLink;

public class CustomerServiceRecordStatusEaoImpl extends BaseEaoImpl implements CustomerServiceRecordStatusEao {
	
	/** jrh 获取指定域中指定呼叫方向的 所有客服记录状态 <br/> domain 域对象
	 * <br/> 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入] <br/> List 存放客服记录状态的集合 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatus> getAllByDirection(Domain domain, String direction) {
		if("inAndOut".equals(direction)) {
			List<CustomerServiceRecordStatus> unsortedOutgoingList = getEntityManager().createQuery("select status from CustomerServiceRecordStatus as status " +
					"where status.domain.id = " + domain.getId() + " and status.direction = 'outgoing' and status.enabled = true").getResultList();
			List<CustomerServiceRecordStatus> unsortedIncomingList = getEntityManager().createQuery("select status from CustomerServiceRecordStatus as status " +
					"where status.domain.id = " + domain.getId() + " and status.direction = 'incoming' and status.enabled = true").getResultList();
			return getInAndOutSortedServiceRecordStatus(unsortedOutgoingList, unsortedIncomingList);
		} else if("outgoing".equals(direction)) {
			List<CustomerServiceRecordStatus> unsortedOutgoingList = getEntityManager().createQuery("select status from CustomerServiceRecordStatus as status " +
					"where status.domain.id = " + domain.getId() + " and status.direction = 'outgoing' and status.enabled = true").getResultList();
			return getSortedOutgoingRecordStatus(unsortedOutgoingList);
		} else if("incoming".equals(direction)) {
			List<CustomerServiceRecordStatus> unsortedIncomingList = getEntityManager().createQuery("select status from CustomerServiceRecordStatus as status " +
					"where status.domain.id = " + domain.getId() + " and status.direction = 'incoming' and status.enabled = true").getResultList();
			return getSortedIncomingRecordStatus(unsortedIncomingList);
		}
		return null;
	}
	
	/**
	 * 获取指定域中的所有呼入和呼出的客服记录状态，并且将其存入list 集合中;
	 * @param domain	指定的域
	 */
	private List<CustomerServiceRecordStatus> getInAndOutSortedServiceRecordStatus(List<CustomerServiceRecordStatus> unsortedOutgoingList, 
			List<CustomerServiceRecordStatus> unsortedIncomingList) {
		List<CustomerServiceRecordStatus> allList = new ArrayList<CustomerServiceRecordStatus>();
		allList.addAll(getSortedOutgoingRecordStatus(unsortedOutgoingList));
		allList.addAll(getSortedIncomingRecordStatus(unsortedIncomingList));
		return allList;
	}

	/**
	 * 获取指定域中的所有的外呼记录的状态，并且将其存入list 中;
	 * @param unsortedOutgoingList	未排序前的呼叫记录状态集合【呼出】
	 */
	private List<CustomerServiceRecordStatus> getSortedOutgoingRecordStatus(List<CustomerServiceRecordStatus> unsortedOutgoingList) {
		List<CustomerServiceRecordStatus> answseredList = new ArrayList<CustomerServiceRecordStatus>();
		List<CustomerServiceRecordStatus> noAnsweredList = new ArrayList<CustomerServiceRecordStatus>();
		for(CustomerServiceRecordStatus status : unsortedOutgoingList) {
			if(status.getIsAnswered()) {
				answseredList.add(status);
			} else {
				noAnsweredList.add(status);
			}
		}
		
		List<CustomerServiceRecordStatus> noRefusedList = new ArrayList<CustomerServiceRecordStatus>();
		List<CustomerServiceRecordStatus> refusedList = new ArrayList<CustomerServiceRecordStatus>();
		List<CustomerServiceRecordStatus> sortedOutgoingList = new ArrayList<CustomerServiceRecordStatus>();
		
		for(CustomerServiceRecordStatus status : answseredList) {
			if(status.getStatusName().contains("拒绝")) {
				refusedList.add(status);	continue;
			}
			noRefusedList.add(status);
		}
		
		sortServiceRecordStatusList(noAnsweredList);
		sortServiceRecordStatusList(noRefusedList);
		sortServiceRecordStatusList(refusedList);
		sortedOutgoingList.addAll(noAnsweredList);
		sortedOutgoingList.addAll(noRefusedList);
		sortedOutgoingList.addAll(refusedList);
		return sortedOutgoingList;
	}

	/**
	 * 获取指定域中的所有的呼入记录的状态，并且将其存入list 中;
	 * @param unsortedOutgoingList	未排序前的呼叫记录状态集合【呼入】
	 */
	private List<CustomerServiceRecordStatus> getSortedIncomingRecordStatus(List<CustomerServiceRecordStatus> unsortedIncomingList) {
		List<CustomerServiceRecordStatus> noRefusedList = new ArrayList<CustomerServiceRecordStatus>();
		List<CustomerServiceRecordStatus> refusedList = new ArrayList<CustomerServiceRecordStatus>();
		List<CustomerServiceRecordStatus> sortedIncomingList = new ArrayList<CustomerServiceRecordStatus>();
		
		for(CustomerServiceRecordStatus status : unsortedIncomingList) {
			if(status.getStatusName().contains("拒绝")) {
				refusedList.add(status);	continue;
			}
			noRefusedList.add(status);
		}
		
		sortServiceRecordStatusList(noRefusedList);
		sortServiceRecordStatusList(refusedList);

		sortedIncomingList.addAll(noRefusedList);
		sortedIncomingList.addAll(refusedList);
		
		return sortedIncomingList;
	}

	/**
	 * 按状态名称的长度排序
	 * @param list 乱序的客服状态集合
	 */
	private void sortServiceRecordStatusList(List<CustomerServiceRecordStatus> list) {
		Collections.sort(list, new Comparator<CustomerServiceRecordStatus>() {
			@Override
			public int compare(CustomerServiceRecordStatus status1, CustomerServiceRecordStatus status2) {
				if(status1.getStatusName().length() > status2.getStatusName().length()) {
					return 1;
				} else if(status1.getStatusName().length() < status2.getStatusName().length()) {
					return -1;
				}
				return 0;
			}
		});
	}

	/** jrh 检查当前呼叫方向下的客服记录是否已经存在，如果存在返回true <br/> statusName 状态名称  <br/> direction 呼叫方向  <br/> domain 所属域 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkExistedByStatusName(String statusName, String direction, Long domainId) {
		String sql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.statusName = '" +statusName+ "' and csrs.direction = '" +direction+ "' and csrs.domain.id = " +domainId;
		List<CustomerServiceRecordStatus> statusList = getEntityManager().createQuery(sql).getResultList();
		if(statusList.size() > 0) {
			return true;
		}
		return false;
	}

	/** jrh  获取指定域下的所有指定状态(可用、停用状态)的客服记录状态值 <br/> enable  可用状态  <br/> domainId	指定域的id */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatus> getAllByEnable(boolean enabled, Long domainId) {
		String sql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.enabled = " +enabled+ " and csrs.domain.id = " +domainId+" order by csrs.id";
		List<CustomerServiceRecordStatus> statusList = getEntityManager().createQuery(sql).getResultList();
		return statusList;
	}

	/** jrh  获取指定域下、指定部门、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象 <br/> deptId 部门Id 
	 * <br/> direction 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]  <br/> enable  可用状态  <br/> domainId	指定域的id */
	@Override
	public List<CustomerServiceRecordStatus> getAllByDeptIdAndDirection(Long deptId, String direction, boolean enabled, Long domainId) {
		String recordStatusIdsSql = "select servicerecordstatusid from ec2_service_record_status_2_department_link where departmentid = "+deptId;
		return getAllByStatusIds(direction, enabled, domainId, recordStatusIdsSql);
	}
	
	/** jrh  获取指定域下、指定部门集合、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象 <br/> deptId 部门Id 
	 * <br/> direction 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]  <br/> enable  可用状态  <br/> domainId	指定域的id */
	@Override
	public List<CustomerServiceRecordStatus> getAllByDeptIdsAndDirection(List<Long> deptIds, String direction, boolean enabled, Long domainId) {
		String deptIdsSql = "0";
		if(deptIds != null) {
			for(Long deptId : deptIds) {
				deptIdsSql += deptId+ ",";
			}
			if(deptIdsSql.endsWith(",")) {
				deptIdsSql = deptIdsSql.substring(0, deptIdsSql.length()-1);
			}
		}
		
		String recordStatusIdsSql = "select servicerecordstatusid from ec2_service_record_status_2_department_link where departmentid in ("+deptIdsSql+")";
		return getAllByStatusIds(direction, enabled, domainId, recordStatusIdsSql);
	}

	/**
	 * jrh
	 * 根据客服记录状态的id 集合进行查询
	 * @param direction				呼叫方向
	 * @param enabled				是否可用
	 * @param domainId				所属域
	 * @param recordStatusIdsSql	客服记录状态Id 集合的搜索语句
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CustomerServiceRecordStatus> getAllByStatusIds(String direction, boolean enabled, Long domainId, String recordStatusIdsSql) {
		List<Long> recordStatusIds = getEntityManager().createNativeQuery(recordStatusIdsSql).getResultList();
		String statusIndeptSql = "0,";
		for(Long statusId : recordStatusIds) {
			statusIndeptSql += statusId+ ",";
		}
		while(statusIndeptSql.endsWith(",")) {
			statusIndeptSql = statusIndeptSql.substring(0, statusIndeptSql.length()-1);
		}
		
		if("outgoing".equals(direction)) {
			String sql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.id in (" +statusIndeptSql+ ") and csrs.enabled = '" +enabled
					+ "' and csrs.direction = '" +direction+ "' and csrs.domain.id = " +domainId+" order by csrs.id";
			List<CustomerServiceRecordStatus> unsortedOutgoingList = getEntityManager().createQuery(sql).getResultList();
			return getSortedOutgoingRecordStatus(unsortedOutgoingList);
		} else if("incoming".equals(direction)) {
			String sql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.id in (" +statusIndeptSql+ ") and csrs.enabled = '" +enabled
					+ "' and csrs.direction = '" +direction+ "' and csrs.domain.id = " +domainId+" order by csrs.id";
			List<CustomerServiceRecordStatus> unsortedIncomingList = getEntityManager().createQuery(sql).getResultList();
			return getSortedIncomingRecordStatus(unsortedIncomingList);
		} else if("inAndOut".equals(direction)) {
			String outgoingSql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.id in (" +statusIndeptSql+ ") and csrs.enabled = '" +enabled
					+ "' and csrs.direction = 'outgoing' and csrs.domain.id = " +domainId+" order by csrs.id";
			List<CustomerServiceRecordStatus> unsortedOutgoingList = getEntityManager().createQuery(outgoingSql).getResultList();
			
			String incomingSql = "select csrs from CustomerServiceRecordStatus as csrs where csrs.id in (" +statusIndeptSql+ ") and csrs.enabled = '" +enabled
					+ "' and csrs.direction = 'incoming' and csrs.domain.id = " +domainId+" order by csrs.id";
			List<CustomerServiceRecordStatus> unsortedIncomingList = getEntityManager().createQuery(incomingSql).getResultList();
			return getInAndOutSortedServiceRecordStatus(unsortedOutgoingList, unsortedIncomingList);
		}
		
		return new ArrayList<CustomerServiceRecordStatus>();
	}

	/** jrh 	添加客服记录状态与部门的对应关系  */
	@Override
	public void saveStatus2DeptLink(ServiceRecordStatus2DepartmentLink status2DeptLink) {
		this.getEntityManager().persist(status2DeptLink);
	}
	
	/** jrh 	删除客服记录状态与部门的对应关系 <br/> @param recordStatusId	客服记录状态id <br/> @param deptId	部门id */
	@Override
	public void deleteStatus2DeptLinkByIds(Long recordStatusId, Long deptId) {
		String deleteLinkSql = "delete from ec2_service_record_status_2_department_link where servicerecordstatusid = "+ recordStatusId +" and departmentid = "+deptId;
		this.getEntityManager().createNativeQuery(deleteLinkSql).executeUpdate();
	}
	
	/**
	 * chb 
	 * 取得所有的状态信息
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatus> getAll(Domain domain) {
		List<CustomerServiceRecordStatus> statusList = getEntityManager().createQuery("select status from CustomerServiceRecordStatus as status " +
				"where status.domain.id = " + domain.getId() + " and status.enabled is not true").getResultList();
		return statusList;
	}
	
	/** jinht
	 * 根据状态名称查询状态信息
	 * @param domain
	 * @param statusName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CustomerServiceRecordStatus getByStatusName(Domain domain, String statusName) {
		List<CustomerServiceRecordStatus> customerServiceRecordStatusList =  getEntityManager().createQuery("select s from CustomerServiceRecordStatus as s " +
				"where s.domain.id = " + domain.getId() + " and s.statusName = '"+statusName+"' and s.enabled = true").getResultList();
		if(customerServiceRecordStatusList != null && customerServiceRecordStatusList.size() > 0){
			return customerServiceRecordStatusList.get(0);
		}
		return null;
	}
	
	/**
	 * jinht
	 * 根据呼叫的方向进行查询客服记录状态信息
	 * @param domain
	 * @param direction
	 * @return List<CustomerServiceRecordStatus>
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public List<CustomerServiceRecordStatus> getByDirection(Domain domain, String direction){
		return getEntityManager().createQuery("select s from CustomerServiceRecordStatus as s where s.domain.id="+domain.getId()+" " +
				" and s.direction='"+direction+"'").getResultList();
	}

}
