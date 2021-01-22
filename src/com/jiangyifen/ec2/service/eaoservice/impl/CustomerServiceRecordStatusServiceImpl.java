package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerServiceRecordStatusEao;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.ServiceRecordStatus2DepartmentLink;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;

public class CustomerServiceRecordStatusServiceImpl implements CustomerServiceRecordStatusService {
	
	private CustomerServiceRecordStatusEao customerServiceRecordStatusEao;
	
	// enhanced method
		
	/** jrh 获取指定域中指定呼叫方向的 所有客服记录状态 <br/> domain 域对象
	 * <br/> 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入] <br/> List 存放客服记录状态的集合 */
	@Override
	public List<CustomerServiceRecordStatus> getAllByDirection(Domain domain, String direction) {
		return customerServiceRecordStatusEao.getAllByDirection(domain, direction);
	}

	/** jrh 检查当前呼叫方向下的客服记录是否已经存在，如果存在返回true <br/> statusName 状态名称  <br/> direction 呼叫方向  <br/> domain 所属域 */
	@Override
	public boolean checkExistedByStatusName(String statusName, String direction, Long domainId) {
		return customerServiceRecordStatusEao.checkExistedByStatusName(statusName, direction, domainId);
	}

	/** jrh  获取指定域下的所有指定状态(可用、停用状态)的客服记录状态值 <br/> enable  可用状态  <br/> domainId	指定域的id */
	@Override
	public List<CustomerServiceRecordStatus> getAllByEnable(boolean enable, Long domainId) {
		return customerServiceRecordStatusEao.getAllByEnable(enable, domainId);
	}

	/** jrh  获取指定域下、指定部门、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象 <br/> deptId 部门Id 
	 * <br/> direction 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]  <br/> enable  可用状态  <br/> domainId	指定域的id */
	@Override
	public List<CustomerServiceRecordStatus> getAllByDeptIdAndDirection(Long deptId, String direction, boolean enable, Long domainId) {
		return customerServiceRecordStatusEao.getAllByDeptIdAndDirection(deptId, direction, enable, domainId);
	}
	
	/** jrh  获取指定域下、指定部门、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象 <br/> deptId 部门Id 
	 * <br/> direction 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]  <br/> enable  可用状态  <br/> domainId	指定域的id */
	@Override
	public List<CustomerServiceRecordStatus> getAllByDeptIdsAndDirection(List<Long> deptIds, String direction, boolean enable, Long domainId) {
//		List<CustomerServiceRecordStatus> allStatusByDepts = new ArrayList<CustomerServiceRecordStatus>();
//		Map<Long, CustomerServiceRecordStatus>
//		for(Long deptId : deptIds) {
//			for(CustomerServiceRecordStatus status : customerServiceRecordStatusEao.getAllByDeptIdAndDirection(deptId, direction, enable, domainId)) {
//				
//			}
//		}
		return customerServiceRecordStatusEao.getAllByDeptIdsAndDirection(deptIds, direction, enable, domainId);
	}

	/** jrh 	添加客服记录状态与部门的对应关系  */
	@Override
	public void saveStatus2DeptLink(ServiceRecordStatus2DepartmentLink status2DeptLink) {
		customerServiceRecordStatusEao.saveStatus2DeptLink(status2DeptLink);
	}
	
	/** jrh 	删除客服记录状态与部门的对应关系 <br/> @param recordStatusId	客服记录状态id <br/> @param deptId	部门id */
	@Override
	public void deleteStatus2DeptLinkByIds(Long recordStatusId, Long deptId) {
		customerServiceRecordStatusEao.deleteStatus2DeptLinkByIds(recordStatusId, deptId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatus> loadPageEntities(int start,
			int length, String sql) {
		return customerServiceRecordStatusEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return customerServiceRecordStatusEao.getEntityCount(sql);
	}

	// common method
	@Override
	public CustomerServiceRecordStatus get(Object primaryKey) {
		return customerServiceRecordStatusEao.get(CustomerServiceRecordStatus.class, primaryKey);
	}

	@Override
	public void save(CustomerServiceRecordStatus serviceRecordStatus) {
		customerServiceRecordStatusEao.save(serviceRecordStatus);
	}

	@Override
	public void update(CustomerServiceRecordStatus serviceRecordStatus) {
		customerServiceRecordStatusEao.update(serviceRecordStatus);
	}

	@Override
	public void delete(CustomerServiceRecordStatus serviceRecordStatus) {
		customerServiceRecordStatusEao.delete(serviceRecordStatus);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerServiceRecordStatusEao.delete(CustomerServiceRecordStatus.class, primaryKey);
	}

	// setter getter
	
	public CustomerServiceRecordStatusEao getCustomerServiceRecordStatusEao() {
		return customerServiceRecordStatusEao;
	}

	public void setCustomerServiceRecordStatusEao(CustomerServiceRecordStatusEao customerServiceRecordStatusEao) {
		this.customerServiceRecordStatusEao = customerServiceRecordStatusEao;
	}
	/**
	 * chb 
	 * 取得所有的状态信息
	 * @param domain
	 * @return
	 */
	@Override
	public List<CustomerServiceRecordStatus> getAll(Domain domain) {
		return customerServiceRecordStatusEao.getAll(domain);
	}

	/** jinht
	 * 根据状态名称查询状态信息
	 * @param domain
	 * @param statusName
	 * @return
	 */
	@Override
	public CustomerServiceRecordStatus getByStatusName(Domain domain, String statusName) {
		return customerServiceRecordStatusEao.getByStatusName(domain, statusName);
	}
	
	/**
	 * jinht
	 * 根据呼叫的方向进行查询客服记录状态信息
	 * @param domain
	 * @param direction
	 * @return List<CustomerServiceRecordStatus>
	 */
	@Override
	public List<CustomerServiceRecordStatus> getByDirection(Domain domain, String direction){
		return customerServiceRecordStatusEao.getByDirection(domain, direction);
	}
	
}
