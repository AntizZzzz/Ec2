package com.jiangyifen.ec2.test;

/**
 * 创建插入数据库的权限sql 语句，以及权限与角色的关联Sql 语句
 * 	 
 * 		使用方法：
 * 			
 * 			先执行 createBusinessModel 注意每个子模块的父模块的id 值  parent = i;
 * 			记下  <管理员角色>  对应权限的起始id 和终止id 值
 * 			还有  <话务员角色>  对应权限的起始id 和终止id 值
 * 			然后再运行createRelationShip 方法 
 * @author jrh
 *
 */
public class Jiang_BusinessModelSql_Creator {
	
	public static void main(String[] args) {
		// 这样做的好处，每次执行只需取消注释中的一行代码
		boolean run = true;
//		run = false;
		if(run) {
			createBusinessModel();
		} else {
			createRelationShip();
		}
	}

	/**
	 * 创建权限模块sql
	 * 注意parentId 值的变化
	 */
	private static void createBusinessModel() {
		int i = 0;
		int parentId = 0;
		
		System.out.println();
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- 管理员权限模块");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println();
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', 'manager', NULL, NULL, NULL);");
		System.out.println("-- 项目管理");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '项目管理', NULL, 'project_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '项目控制', 'project_control', 'project_management', "+parentId+");");
		System.out.println("-- 自动外呼管理");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '自动外呼管理', NULL, 'auto_dial_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '自动外呼', 'auto_dial', 'auto_dial_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '语音群发', 'voice_packet', 'auto_dial_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '语音上传', 'voice_upload', 'auto_dial_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '资源回收', 'recycle_resource', 'auto_dial_management', "+parentId+");");
		System.out.println("-- 数据管理");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '数据管理', NULL, 'data_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '资源导入', 'resource_import', 'data_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '全局搜索', 'global_search', 'data_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '资源管理', 'resource_manage', 'data_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '客服记录', 'service_record', 'data_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '呼叫记录', 'call_record', 'data_management', "+parentId+");");
		System.out.println("-- 消息管理");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '消息管理', NULL, 'message_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '消息发送', 'message_send', 'message_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '历史消息', 'history_message', 'message_management', "+parentId+");");
		System.out.println("-- 报表管理");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '报表管理', NULL, 'report_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '项目完成情况', 'project_finish_status', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '客服记录情况', 'service_record_status', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '话务考核', 'telephone_traffic_check', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '按通话时长考核话务', 'telephone_traffic_check_by_call_time', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '人员考核', 'staff_check', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '坐席占有率', 'seat_occupancy_rate', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '员工上下线详单', 'staff_time_sheet', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '队列详单', 'see_queue_detail', 'report_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '自动外呼详情', 'see_autodial_detail', 'report_management', "+parentId+");");
		System.out.println("-- 客户管理");                           
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '客户管理', NULL, 'customer_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '客户成员管理', 'customer_member_manage', 'customer_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '客户迁移日志', 'customer_migrate_log_manage', 'customer_management', "+parentId+");");
		System.out.println("-- 实时监控管理");                       
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '实时监控管理', NULL, 'supervise_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '职工状态监控', 'employee_status_supervise', 'supervise_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '队列状态监控', 'queue_status_supervise', 'supervise_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '分机状态监控', 'sip_status_supervise', 'supervise_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '通话状态监控', 'call_status_supervise', 'supervise_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '活动会议室监控', 'meeting_room_supervise', 'supervise_management', "+parentId+");");
		System.out.println("--  业务管理");                         
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '业务管理', NULL, 'business_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '商品管理', 'commodity_manage', 'business_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '订单管理', 'order_manage', 'business_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '问卷管理', 'questionnaire_manage', 'business_management', "+parentId+");");
		System.out.println("--  短信管理组件");                       
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '短信管理', NULL, 'phone_message_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '模板管理', 'template_management', 'phone_message_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '短信发送', 'send_management', 'phone_message_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '历史短信', 'history_management', 'phone_message_management', "+parentId+");");
		System.out.println("--  知识库管理组件");                     
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '知识库管理', NULL, 'knowledge_base_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '知识库管理', 'knowledge_manage', 'knowledge_base_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '知识库查看', 'knowledge_view', 'knowledge_base_management', "+parentId+");");
		System.out.println("--  系统管理");                         
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '系统管理', NULL, 'system_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '用户管理', 'user_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '角色管理', 'role_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '部门管理', 'department_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '分机管理', 'exten_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '外线管理', 'outline_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '队列管理', 'queue_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '外线成员管理', 'outline_member_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '保持音乐管理', 'moh_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '动态队列成员管理', 'dynamic_queue_member_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '静态队列成员管理', 'static_queue_member_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '内部配置管理', 'internal_setting_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '值班表管理', 'duty_report_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '外转外配置管理', 'mgr_phone2phone_setting_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '拨号方案详情', 'softphone_dialplan_manage', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '管理员会议室', 'mgr_meet_me', 'system_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '操作日志查看', 'operation_log_view', 'system_management', "+parentId+");");
		System.out.println("--  系统信息管理");                       
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '系统信息管理', NULL, 'system_info_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '系统信息', 'system_status', 'system_info_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', 'Licence信息', 'system_liscence', 'system_info_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'manager', '关于我们', 'about_us', 'system_info_management', "+parentId+");");
		System.out.println();
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("--  话务员权限 CSR");                    
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println();
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', 'csr', NULL, NULL, NULL);");
		System.out.println("-- 基础设置");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '基础设置', NULL, 'base_design_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '编辑账户信息', 'edit_csr_info', 'base_design_management', "+parentId+");");
		System.out.println("-- 我的任务");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的任务', NULL, 'task_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的外呼任务', 'outgoing_task', 'task_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的客服记录', 'service_record', 'task_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '编辑客户信息', 'edit_customer', 'task_management', "+parentId+");");
		System.out.println("-- 呼叫记录");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '呼叫记录', NULL, 'call_record_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '呼叫记录管理', 'call_record', 'call_record_management', "+parentId+");");
		System.out.println("-- 客户管理");                          
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '客户管理', NULL, 'my_customer_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的客户', 'proprietary_customers', 'my_customer_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的资源', 'my_resources', 'my_customer_management', "+parentId+");");
		System.out.println("-- 短信模块管理");                        
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '短信管理', NULL, 'csr_sms_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '短信模板', 'message_template_manage', 'csr_sms_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '发送短信', 'message_send_manage', 'csr_sms_management', "+parentId+");");
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '历史短信', 'message_history_manage', 'csr_sms_management', "+parentId+");");
		System.out.println("-- 其他功能模块管理");                     
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '其他功能管理', NULL, 'other_funcs_management', NULL);");
		parentId = i;
		System.out.println("INSERT INTO ec2_businessmodel VALUES ("+(++i)+", 'csr', '我的会议室', 'csr_meet_me', 'other_funcs_management', "+parentId+");");
	}

	/**
	 * 创建关联Sql
	 * 注意  min_manager、max_manager、min_csr、 max_csr四个的值须根据
	 *     createBusinessModel 方法的执行结果决定
	 */
	private static void createRelationShip() {
		int min_manager = 1;
		int max_manager = 68;
		System.out.println();
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- 管理员 权限关联");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println();
		for(int i = min_manager; i <= max_manager; i++) {
			System.out.println("INSERT INTO ec2_role_businessmodel_link VALUES (1, "+i+");");
			if((i%10)==0) {
				System.out.println();
			}
		}
		int min_csr = 69;
		int max_csr = 86;
		System.out.println();
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- CSR 权限关联");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println("-- ---------------------------------------------------------------------------");
		System.out.println();
		for(int i= min_csr; i <= max_csr; i++) {
			System.out.println("INSERT INTO ec2_role_businessmodel_link VALUES (2, "+i+");");
			if((i%10)==0) {
				System.out.println();
			}
		}
	}

}
