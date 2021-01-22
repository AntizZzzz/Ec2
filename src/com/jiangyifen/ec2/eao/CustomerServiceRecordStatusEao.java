package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.ServiceRecordStatus2DepartmentLink;

public interface CustomerServiceRecordStatusEao extends BaseEao {

	/** jrh
	 *  获取指定域中指定呼叫方向的 所有客服记录状态
	 * @param domain 域对象
	 * @param direction 呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]
	 * @return List 存放客服记录状态的集合
	 */
	public List<CustomerServiceRecordStatus> getAllByDirection(Domain domain, String direction);

	/** jrh
	 * 	检查当前呼叫方向下的客服记录是否已经存在，如果存在返回true
	 * @param statusName	状态名称
	 * @param direction		呼叫方向
	 * @param domain		所属域
	 * @return
	 */
	public boolean checkExistedByStatusName(String statusName, String direction, Long domainId);

	/**
	 * jrh 
	 * 	获取指定域下的所有指定状态(可用、停用状态)的客服记录状态值
	 * @param enable	可用状态
	 * @param domainId	指定域的Id
	 * @return
	 */
	public List<CustomerServiceRecordStatus> getAllByEnable(boolean enable, Long domainId);

	/**
	 * jrh
	 * 	获取指定域下、指定部门、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象
	 * @param deptId	部门id
	 * @param direction	呼叫方向[inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]
	 * @param enable	可用状态
	 * @param domainId	指定域的Id
	 * @return
	 */
	public List<CustomerServiceRecordStatus> getAllByDeptIdAndDirection(Long deptId, String direction, boolean enable, Long domainId);

	/**
	 * jrh
	 * 	获取指定域下、指定部门集合、指定方向下的所有指定状态(可用、停用状态)的客服记录状态对象
	 * @param deptIds	部门id集合
	 * @param direction	呼叫方向 [inAndOut 呼入和呼出；outgoing 呼出； incoming 呼入]
	 * @param enable	可用状态
	 * @param domainId	指定域的Id
	 * @return
	 */
	public List<CustomerServiceRecordStatus> getAllByDeptIdsAndDirection(List<Long> deptIds, String direction, boolean enable, Long domainId);

	/**
	 * jrh
	 * 	添加客服记录状态与部门的对应关系
	 * @param status2DeptLink
	 */
	public void saveStatus2DeptLink(ServiceRecordStatus2DepartmentLink status2DeptLink);
	
	/**
	 * jrh
	 * 	删除客服记录状态与部门的对应关系
	 * @param recordStatusId	客服记录状态id
	 * @param deptId			部门id
	 */
	public void deleteStatus2DeptLinkByIds(Long recordStatusId, Long deptId);
	
	/**
	 * chb 
	 * 取得所有的状态信息
	 * @param domain
	 * @return
	 */
	public List<CustomerServiceRecordStatus> getAll(Domain domain);
	
	/** jinht
	 * 根据状态名称查询状态信息
	 * @param domain
	 * @param statusName
	 * @return
	 */
	public CustomerServiceRecordStatus getByStatusName(Domain domain, String statusName);
	
	/**
	 * jinht
	 * 根据呼叫的方向进行查询客服记录状态信息
	 * @param domain
	 * @param direction
	 * @return List<CustomerServiceRecordStatus>
	 */
	public List<CustomerServiceRecordStatus> getByDirection(Domain domain, String direction);
}
