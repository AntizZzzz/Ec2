package com.jiangyifen.ec2.test;

public class Initialize_DataBase_Jiang {

	/**
	 * 下面的表需要从 192.168.1.242:5432/asterisk 迁移
	 * @param args
	 */
	public static void main(String[] args) {
		// 注释部分放入了 Chen_Migration_DB.java
//		// 导入权限控制
//		Jiang_Import_BussinessControl_test.importAllBusinessControl();
//		
//		// 将权限与角色进行关联
//		Jiang_Import_Role_Business_Link_test.buildLink();
//		
//		// 导入置忙原因
//		Jiang_Import_PauseReason_test.createPauseReasonsToDB();
//		
//		// 导入呼叫记录的结果状态
//		Jiang_Import_RecordStatus_test.importRecordStatus();
		
		// 迁移队列
		Jiang_Migration_Queue_test.migrationQueue();
		
//		// 迁移话机
		Jiang_Migration_SipConf_test.migrationSip();
//		
		// 迁移用户与队列关系
		Jiang_Migration_UserQueue_test.migrationUserQueue();

	}
	
}
