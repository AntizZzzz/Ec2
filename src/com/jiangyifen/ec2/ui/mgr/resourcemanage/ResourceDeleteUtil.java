package com.jiangyifen.ec2.ui.mgr.resourcemanage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.enumtype.ResourcDeleteStatus;
import com.jiangyifen.ec2.service.eaoservice.ResourceManageService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

public class ResourceDeleteUtil{

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static ResourcDeleteStatus deleteStatus = ResourcDeleteStatus.NO_NULL;
	
	private static ResourceDeleteUtil resourcDelete = null;
	
	private static int DELETE_STEP_LENGTH = 1000;
 
	private boolean isStopDo = false;
	
	private Long domainId;
	
	private ResourceDeleteUtil(){}
	
	public static synchronized ResourceDeleteUtil getInstance(){
		if(resourcDelete == null){
			resourcDelete = new ResourceDeleteUtil();
		}
		return resourcDelete;
	}
	
	//判断是否能执行删除任务
	public static synchronized boolean canExecuteDelete(){
		boolean tf = false;
		if(ResourceDeleteUtil.deleteStatus.getIndex()  == ResourcDeleteStatus.START_EXECUTE.getIndex()){//如果是正在执行状态不能执行删除
			tf = false;
		}else{
			tf = true;	
		}
		return tf;
	}
	
	/*
	 * 提供
	 * 删除1万条以下数据（立即执行）  执行任务删除小于200万
	 * 
	 1.立即执行小于1万条记录
	 2.任务删除小于100万条记录
	 3.每天20点以后自动执行，执行到第二天凌晨4点自动停止
	 4.一天执行的任务不能超过100万（一个配置数值）最大限制数 在后面测试获得
	 */
	public synchronized void startExecuteDelete(Long taskId,Long domainId){
		this.domainId = domainId;
		/*
		 1.取数据获取状态
		 2.更新状态
		 3.计算执行步数
		 4.执行步数
		 5.判断是否跳出
		 6.执行删除固定个数的资源数据
		 7.更新状态
		*/
		try {
			deleteStatus = ResourcDeleteStatus.START_EXECUTE;
			ResourceManageService resourceManageService = SpringContextHolder.getBean("resourceManageService");
			
			Task task = TaskShareData.taskMap.get(taskId);
			
			if(task != null){
				Date startDate = new Date();
				int countNum = resourceManageService.getEntityCount(task.getCountSql());
				if(countNum < 0){
					return ;
				}
				int forLength = (countNum + DELETE_STEP_LENGTH -1) / DELETE_STEP_LENGTH;
				int start = 0;
				int length = DELETE_STEP_LENGTH;
				List<CustomerResource> resourceList = null;
				
				System.out.println("------------------------------------需要遍历多少遍"+forLength+"------------------------------------");
				for (int i = 0; i < forLength; i++) {//遍历
					Long T1 = System.currentTimeMillis();
					
					if(isStopDo){//停止删除
						break;
					}
					resourceList = resourceManageService.loadPageEntities(start, length, task.getListSql());
					if(resourceList != null & resourceList.size() > 0){
						resourceManageService.deleteCustomerResourceByListId(resourceList);
					}
					Long T2 = System.currentTimeMillis();
					System.out.println("------------------start::"+sdf.format(startDate)+"----"+sdf.format(new Date())+"---------遍历步长第【"+(i+1)+"】次,结束-----用时：："+(T2-T1)+"-------------------------------");
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				deleteStatus = ResourcDeleteStatus.COMPLETE_SUCCESS_EXECITE;
			}else{
				deleteStatus = ResourcDeleteStatus.COMPLETE_ERROR_EXECITE;
			}
			
			if(TaskShareData.taskMap.containsKey(taskId)){
				TaskShareData.taskMap.remove(taskId);
			}
		} catch (Exception e) {
			deleteStatus = ResourcDeleteStatus.COMPLETE_ERROR_EXECITE;
		}
	}
	
	//
	public synchronized void stopExecuteDelete(){
		isStopDo = false;
	}

	public static ResourcDeleteStatus getDeleteStatus() {
		return deleteStatus;
	}

	public static void setDeleteStatus(ResourcDeleteStatus deleteStatus) {
		ResourceDeleteUtil.deleteStatus = deleteStatus;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
	
}
