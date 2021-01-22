package com.jiangyifen.ec2.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.BusinessModel;

public class Jiang_Import_BussinessControl_test {

	public static void main(String[] args) {
		importAllBusinessControl();
	}

	public static void importAllBusinessControl() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em2 = entityMangerFactory.createEntityManager();

		em2.getTransaction().begin();
//
//		importManagerAuthority(em2);
//		System.out.println("创建Manger 权限成功");
//		
//		importCsrAuthority(em2);
//		System.out.println("创建Csr 权限成功");

		importModel(em2);
		System.out.println("成功");

		em2.getTransaction().commit();
		em2.close();
		entityMangerFactory.close();
	}

	private static void importModel(EntityManager em) {
		BusinessModel b1 = (BusinessModel) em.createQuery("select b from BusinessModel as b where b.id = 36").getSingleResult();

		BusinessModel entity13_in_system_model = new BusinessModel();
		entity13_in_system_model.setApplication("manager");
		entity13_in_system_model.setModule("system_management");
		entity13_in_system_model.setFunction("phone2phone_manage");
		entity13_in_system_model.setDescription("外转外配置管理");
		entity13_in_system_model.setParent(b1);
		em.persist(entity13_in_system_model);
	}

	@SuppressWarnings("unused")
	private static void importManagerAuthority(EntityManager em) {
		BusinessModel entity = new BusinessModel();
		entity.setApplication("manager");
		entity.setDescription("manager");
		em.persist(entity);

		// ========================================================================//

		BusinessModel project_model = new BusinessModel();
		project_model.setApplication("manager");
		project_model.setModule("project_management");
		project_model.setDescription("项目管理");
		em.persist(project_model);

		BusinessModel entity1_in_project_model = new BusinessModel();
		entity1_in_project_model.setApplication("manager");
		entity1_in_project_model.setModule("project_management");
		entity1_in_project_model.setFunction("project_control");
		entity1_in_project_model.setDescription("项目控制");
		entity1_in_project_model.setParent(project_model);
		em.persist(entity1_in_project_model);

		// ========================================================================//

		BusinessModel auto_dial_model = new BusinessModel();
		auto_dial_model.setApplication("manager");
		auto_dial_model.setModule("auto_dial_management");
		auto_dial_model.setDescription("自动外呼管理");
		em.persist(auto_dial_model);

		BusinessModel entity1_in_auto_dial_model = new BusinessModel();
		entity1_in_auto_dial_model.setApplication("manager");
		entity1_in_auto_dial_model.setModule("auto_dial_management");
		entity1_in_auto_dial_model.setFunction("auto_dial");
		entity1_in_auto_dial_model.setDescription("自动外呼");
		entity1_in_auto_dial_model.setParent(auto_dial_model);
		em.persist(entity1_in_auto_dial_model);

		BusinessModel entity2_in_voice_packet_model = new BusinessModel();
		entity2_in_voice_packet_model.setApplication("manager");
		entity2_in_voice_packet_model.setModule("auto_dial_management");
		entity2_in_voice_packet_model.setFunction("voice_packet");
		entity2_in_voice_packet_model.setDescription("语音群发");
		entity2_in_voice_packet_model.setParent(auto_dial_model);
		em.persist(entity2_in_voice_packet_model);

		BusinessModel entity3_in_voice_upload_model = new BusinessModel();
		entity3_in_voice_upload_model.setApplication("manager");
		entity3_in_voice_upload_model.setModule("auto_dial_management");
		entity3_in_voice_upload_model.setFunction("voice_upload");
		entity3_in_voice_upload_model.setDescription("语音上传");
		entity3_in_voice_upload_model.setParent(auto_dial_model);
		em.persist(entity3_in_voice_upload_model);
		
		BusinessModel entity4_in_voice_upload_model = new BusinessModel();
		entity4_in_voice_upload_model.setApplication("manager");
		entity4_in_voice_upload_model.setModule("auto_dial_management");
		entity4_in_voice_upload_model.setFunction("recycle_resource");
		entity4_in_voice_upload_model.setDescription("资源回收");
		entity4_in_voice_upload_model.setParent(auto_dial_model);
		em.persist(entity4_in_voice_upload_model);

		// ========================================================================//

		BusinessModel data_model = new BusinessModel();
		data_model.setApplication("manager");
		data_model.setModule("data_management");
		data_model.setDescription("数据管理");
		em.persist(data_model);

		BusinessModel entity1_in_data_model = new BusinessModel();
		entity1_in_data_model.setApplication("manager");
		entity1_in_data_model.setModule("data_management");
		entity1_in_data_model.setFunction("resource_import");
		entity1_in_data_model.setDescription("资源导入");
		entity1_in_data_model.setParent(data_model);
		em.persist(entity1_in_data_model);

		BusinessModel entity2_in_data_model = new BusinessModel();
		entity2_in_data_model.setApplication("manager");
		entity2_in_data_model.setModule("data_management");
		entity2_in_data_model.setFunction("global_search");
		entity2_in_data_model.setDescription("全局搜索");
		entity2_in_data_model.setParent(data_model);
		em.persist(entity2_in_data_model);

		BusinessModel entity4_in_data_model = new BusinessModel();
		entity4_in_data_model.setApplication("manager");
		entity4_in_data_model.setModule("data_management");
		entity4_in_data_model.setFunction("service_record");
		entity4_in_data_model.setDescription("客服记录");
		entity4_in_data_model.setParent(data_model);
		em.persist(entity4_in_data_model);

		BusinessModel entity5_in_data_model = new BusinessModel();
		entity5_in_data_model.setApplication("manager");
		entity5_in_data_model.setModule("data_management");
		entity5_in_data_model.setFunction("call_record");
		entity5_in_data_model.setDescription("呼叫记录");
		entity5_in_data_model.setParent(data_model);
		em.persist(entity5_in_data_model);

		// ========================================================================//

		BusinessModel message_model = new BusinessModel();
		message_model.setApplication("manager");
		message_model.setModule("message_management");
		message_model.setDescription("消息管理");
		em.persist(message_model);

		BusinessModel entity1_in_message_model = new BusinessModel();
		entity1_in_message_model.setApplication("manager");
		entity1_in_message_model.setModule("message_management");
		entity1_in_message_model.setFunction("message_send");
		entity1_in_message_model.setDescription("消息发送");
		entity1_in_message_model.setParent(message_model);
		em.persist(entity1_in_message_model);

		BusinessModel entity2_in_message_model = new BusinessModel();
		entity2_in_message_model.setApplication("manager");
		entity2_in_message_model.setModule("message_management");
		entity2_in_message_model.setFunction("history_message");
		entity2_in_message_model.setDescription("历史消息");
		entity2_in_message_model.setParent(message_model);
		em.persist(entity2_in_message_model);

		// ========================================================================//

		BusinessModel report_model = new BusinessModel();
		report_model.setApplication("manager");
		report_model.setModule("report_management");
		report_model.setDescription("报表管理");
		em.persist(report_model);

		BusinessModel entity1_in_report_model = new BusinessModel();
		entity1_in_report_model.setApplication("manager");
		entity1_in_report_model.setModule("report_management");
		entity1_in_report_model.setFunction("project_finish_status");
		entity1_in_report_model.setDescription("项目完成情况");
		entity1_in_report_model.setParent(report_model);
		em.persist(entity1_in_report_model);

		BusinessModel entity2_in_report_model = new BusinessModel();
		entity2_in_report_model.setApplication("manager");
		entity2_in_report_model.setModule("report_management");
		entity2_in_report_model.setFunction("service_record_status");
		entity2_in_report_model.setDescription("客服记录情况");
		entity2_in_report_model.setParent(report_model);
		em.persist(entity2_in_report_model);

		BusinessModel entity3_in_report_model = new BusinessModel();
		entity3_in_report_model.setApplication("manager");
		entity3_in_report_model.setModule("report_management");
		entity3_in_report_model.setFunction("telephone_traffic_check");
		entity3_in_report_model.setDescription("话务考核");
		entity3_in_report_model.setParent(report_model);
		em.persist(entity3_in_report_model);

		BusinessModel entity4_in_report_model = new BusinessModel();
		entity4_in_report_model.setApplication("manager");
		entity4_in_report_model.setModule("report_management");
		entity4_in_report_model
				.setFunction("telephone_traffic_check_by_call_time");
		entity4_in_report_model.setDescription("按通话时长考核话务");
		entity4_in_report_model.setParent(report_model);
		em.persist(entity4_in_report_model);

		BusinessModel entity5_in_report_model = new BusinessModel();
		entity5_in_report_model.setApplication("manager");
		entity5_in_report_model.setModule("report_management");
		entity5_in_report_model.setFunction("staff_check");
		entity5_in_report_model.setDescription("人员考核");
		entity5_in_report_model.setParent(report_model);
		em.persist(entity5_in_report_model);

		BusinessModel entity6_in_report_model = new BusinessModel();
		entity6_in_report_model.setApplication("manager");
		entity6_in_report_model.setModule("report_management");
		entity6_in_report_model.setFunction("seat_occupancy_rate");
		entity6_in_report_model.setDescription("坐席占有率");
		entity6_in_report_model.setParent(report_model);
		em.persist(entity6_in_report_model);

		BusinessModel entity7_in_report_model = new BusinessModel();
		entity7_in_report_model.setApplication("manager");
		entity7_in_report_model.setModule("report_management");
		entity7_in_report_model.setFunction("staff_time_sheet");
		entity7_in_report_model.setDescription("员工上下线详单");
		entity7_in_report_model.setParent(report_model);
		em.persist(entity7_in_report_model);

		BusinessModel entity8_in_report_model = new BusinessModel();
		entity8_in_report_model.setApplication("manager");
		entity8_in_report_model.setModule("report_management");
		entity8_in_report_model.setFunction("see_queue_detail");
		entity8_in_report_model.setDescription("队列详单");
		entity8_in_report_model.setParent(report_model);
		em.persist(entity8_in_report_model);

		BusinessModel entity9_in_report_model = new BusinessModel();
		entity9_in_report_model.setApplication("manager");
		entity9_in_report_model.setModule("report_management");
		entity9_in_report_model.setFunction("see_autodial_detail");
		entity9_in_report_model.setDescription("自动外呼详情");
		entity9_in_report_model.setParent(report_model);
		em.persist(entity9_in_report_model);

		// ========================================================================//

		// 客户管理模块
		BusinessModel customer_model = new BusinessModel();
		customer_model.setApplication("manager");
		customer_model.setModule("customer_management");
		customer_model.setDescription("客户管理");
		em.persist(customer_model);

		BusinessModel entity1_in_customer_model = new BusinessModel();
		entity1_in_customer_model.setApplication("manager");
		entity1_in_customer_model.setModule("customer_management");
		entity1_in_customer_model.setFunction("customer_member_manage");
		entity1_in_customer_model.setDescription("客户成员管理");
		entity1_in_customer_model.setParent(customer_model);
		em.persist(entity1_in_customer_model);

		BusinessModel entity2_in_customer_model = new BusinessModel();
		entity2_in_customer_model.setApplication("manager");
		entity2_in_customer_model.setModule("customer_management");
		entity2_in_customer_model.setFunction("customer_migrate_log_manage");
		entity2_in_customer_model.setDescription("客户迁移日志");
		entity2_in_customer_model.setParent(customer_model);
		em.persist(entity2_in_customer_model);

		// ========================================================================//

		// 实时监控管理模块
		BusinessModel supervise_model = new BusinessModel();
		supervise_model.setApplication("manager");
		supervise_model.setModule("supervise_management");
		supervise_model.setDescription("实时监控管理");
		em.persist(supervise_model);

		BusinessModel entity1_in_supervise_model = new BusinessModel();
		entity1_in_supervise_model.setApplication("manager");
		entity1_in_supervise_model.setModule("supervise_management");
		entity1_in_supervise_model.setFunction("employee_status_supervise");
		entity1_in_supervise_model.setDescription("职工状态监控");
		entity1_in_supervise_model.setParent(supervise_model);
		em.persist(entity1_in_supervise_model);

		BusinessModel entity2_in_supervise_model = new BusinessModel();
		entity2_in_supervise_model.setApplication("manager");
		entity2_in_supervise_model.setModule("supervise_management");
		entity2_in_supervise_model.setFunction("sip_status_supervise");
		entity2_in_supervise_model.setDescription("分机状态监控");
		entity2_in_supervise_model.setParent(supervise_model);
		em.persist(entity2_in_supervise_model);

		BusinessModel entity3_in_supervise_model = new BusinessModel();
		entity3_in_supervise_model.setApplication("manager");
		entity3_in_supervise_model.setModule("supervise_management");
		entity3_in_supervise_model.setFunction("queue_status_supervise");
		entity3_in_supervise_model.setDescription("队列状态监控");
		entity3_in_supervise_model.setParent(supervise_model);
		em.persist(entity3_in_supervise_model);

		BusinessModel entity4_in_supervise_model = new BusinessModel();
		entity4_in_supervise_model.setApplication("manager");
		entity4_in_supervise_model.setModule("supervise_management");
		entity4_in_supervise_model.setFunction("call_status_supervise");
		entity4_in_supervise_model.setDescription("通话状态监控");
		entity4_in_supervise_model.setParent(supervise_model);
		em.persist(entity4_in_supervise_model);

		// ========================================================================//

		// 系统管理模块
		BusinessModel system_model = new BusinessModel();
		system_model.setApplication("manager");
		system_model.setModule("system_management");
		system_model.setDescription("系统管理");
		em.persist(system_model);

		BusinessModel entity1_in_system_model = new BusinessModel();
		entity1_in_system_model.setApplication("manager");
		entity1_in_system_model.setModule("system_management");
		entity1_in_system_model.setFunction("user_manage");
		entity1_in_system_model.setDescription("用户管理");
		entity1_in_system_model.setParent(system_model);
		em.persist(entity1_in_system_model);

		BusinessModel entity2_in_system_model = new BusinessModel();
		entity2_in_system_model.setApplication("manager");
		entity2_in_system_model.setModule("system_management");
		entity2_in_system_model.setFunction("role_manage");
		entity2_in_system_model.setDescription("角色管理");
		entity2_in_system_model.setParent(system_model);
		em.persist(entity2_in_system_model);

		BusinessModel entity3_in_system_model = new BusinessModel();
		entity3_in_system_model.setApplication("manager");
		entity3_in_system_model.setModule("system_management");
		entity3_in_system_model.setFunction("department_manage");
		entity3_in_system_model.setDescription("部门管理");
		entity3_in_system_model.setParent(system_model);
		em.persist(entity3_in_system_model);

		BusinessModel entity4_in_system_model = new BusinessModel();
		entity4_in_system_model.setApplication("manager");
		entity4_in_system_model.setModule("system_management");
		entity4_in_system_model.setFunction("exten_manage");
		entity4_in_system_model.setDescription("分机管理");
		entity4_in_system_model.setParent(system_model);
		em.persist(entity4_in_system_model);

		BusinessModel entity5_in_system_model = new BusinessModel();
		entity5_in_system_model.setApplication("manager");
		entity5_in_system_model.setModule("system_management");
		entity5_in_system_model.setFunction("outline_manage");
		entity5_in_system_model.setDescription("外线管理");
		entity5_in_system_model.setParent(system_model);
		em.persist(entity5_in_system_model);

		BusinessModel entity6_in_system_model = new BusinessModel();
		entity6_in_system_model.setApplication("manager");
		entity6_in_system_model.setModule("system_management");
		entity6_in_system_model.setFunction("queue_manage");
		entity6_in_system_model.setDescription("队列管理");
		entity6_in_system_model.setParent(system_model);
		em.persist(entity6_in_system_model);

		BusinessModel entity7_in_system_model = new BusinessModel();
		entity7_in_system_model.setApplication("manager");
		entity7_in_system_model.setModule("system_management");
		entity7_in_system_model.setFunction("outline_member_manage");
		entity7_in_system_model.setDescription("外线成员管理");
		entity7_in_system_model.setParent(system_model);
		em.persist(entity7_in_system_model);

		BusinessModel entity8_in_system_model = new BusinessModel();
		entity8_in_system_model.setApplication("manager");
		entity8_in_system_model.setModule("system_management");
		entity8_in_system_model.setFunction("moh_manage");
		entity8_in_system_model.setDescription("保持音乐管理");
		entity8_in_system_model.setParent(system_model);
		em.persist(entity8_in_system_model);

		BusinessModel entity9_in_system_model = new BusinessModel();
		entity9_in_system_model.setApplication("manager");
		entity9_in_system_model.setModule("system_management");
		entity9_in_system_model.setFunction("dynamic_queue_member_manage");
		entity9_in_system_model.setDescription("动态队列成员管理");
		entity9_in_system_model.setParent(system_model);
		em.persist(entity9_in_system_model);

		BusinessModel entity10_in_system_model = new BusinessModel();
		entity10_in_system_model.setApplication("manager");
		entity10_in_system_model.setModule("system_management");
		entity10_in_system_model.setFunction("static_queue_member_manage");
		entity10_in_system_model.setDescription("静态队列成员管理");
		entity10_in_system_model.setParent(system_model);
		em.persist(entity10_in_system_model);

		BusinessModel entity11_in_data_model = new BusinessModel();
		entity11_in_data_model.setApplication("manager");
		entity11_in_data_model.setModule("system_management");
		entity11_in_data_model.setFunction("internal_setting_manage");
		entity11_in_data_model.setDescription("内部配置管理");
		entity11_in_data_model.setParent(system_model);
		em.persist(entity11_in_data_model);

		BusinessModel entity12_in_system_model = new BusinessModel();
		entity12_in_system_model.setApplication("manager");
		entity12_in_system_model.setModule("system_management");
		entity12_in_system_model.setFunction("duty_report_manage");
		entity12_in_system_model.setDescription("值班表管理");
		entity12_in_system_model.setParent(system_model);
		em.persist(entity12_in_system_model);

		BusinessModel entity13_in_system_model = new BusinessModel();
		entity13_in_system_model.setApplication("manager");
		entity13_in_system_model.setModule("system_management");
		entity13_in_system_model.setFunction("mgr_phone2phone_setting_manage");
		entity13_in_system_model.setDescription("外转外配置管理");
		entity13_in_system_model.setParent(system_model);
		em.persist(entity13_in_system_model);
		
		BusinessModel entity15_in_system_model = new BusinessModel();
		entity15_in_system_model.setApplication("manager");
		entity15_in_system_model.setModule("system_management");
		entity15_in_system_model.setFunction("softphone_dialplan_manage");
		entity15_in_system_model.setDescription("拨号方案详情");
		entity15_in_system_model.setParent(system_model);
		em.persist(entity15_in_system_model);
		
	}

	@SuppressWarnings("unused")
	private static void importCsrAuthority(EntityManager em) {
		BusinessModel entity = new BusinessModel();
		entity.setApplication("csr");
		entity.setDescription("csr");
		em.persist(entity);

		// ========================================================================//

		BusinessModel base_model = new BusinessModel();
		base_model.setApplication("csr");
		base_model.setModule("base_design_management");
		base_model.setDescription("基础设置");
		em.persist(base_model);

		BusinessModel entity1_in_base_model = new BusinessModel();
		entity1_in_base_model.setApplication("csr");
		entity1_in_base_model.setModule("base_design_management");
		entity1_in_base_model.setFunction("edit_csr_info");
		entity1_in_base_model.setDescription("编辑账户信息");
		entity1_in_base_model.setParent(base_model);
		em.persist(entity1_in_base_model);

		// ========================================================================//

		BusinessModel task_model = new BusinessModel();
		task_model.setApplication("csr");
		task_model.setModule("task_management");
		task_model.setDescription("我的任务");
		em.persist(task_model);

		BusinessModel entity1_in_task_model = new BusinessModel();
		entity1_in_task_model.setApplication("csr");
		entity1_in_task_model.setModule("task_management");
		entity1_in_task_model.setFunction("outgoing_task");
		entity1_in_task_model.setDescription("我的外呼任务");
		entity1_in_task_model.setParent(task_model);
		em.persist(entity1_in_task_model);

		BusinessModel entity2_in_task_model = new BusinessModel();
		entity2_in_task_model.setApplication("csr");
		entity2_in_task_model.setModule("task_management");
		entity2_in_task_model.setFunction("service_record");
		entity2_in_task_model.setDescription("我的客服记录");
		entity2_in_task_model.setParent(task_model);
		em.persist(entity2_in_task_model);

		BusinessModel entity3_in_task_model = new BusinessModel();
		entity3_in_task_model.setApplication("csr");
		entity3_in_task_model.setModule("task_management");
		entity3_in_task_model.setFunction("edit_customer");
		entity3_in_task_model.setDescription("编辑客户信息");
		entity3_in_task_model.setParent(task_model);
		em.persist(entity3_in_task_model);

		// ========================================================================//

		BusinessModel call_record_model = new BusinessModel();
		call_record_model.setApplication("csr");
		call_record_model.setModule("call_record_management");
		call_record_model.setDescription("呼叫记录");
		em.persist(call_record_model);

		BusinessModel entity1_in_call_record_model = new BusinessModel();
		entity1_in_call_record_model.setApplication("csr");
		entity1_in_call_record_model.setModule("call_record_management");
		entity1_in_call_record_model.setFunction("call_record");
		entity1_in_call_record_model.setDescription("呼叫记录管理");
		entity1_in_call_record_model.setParent(call_record_model);
		em.persist(entity1_in_call_record_model);

		// ========================================================================//

		BusinessModel customer_model = new BusinessModel();
		customer_model.setApplication("csr");
		customer_model.setModule("my_customer_management");
		customer_model.setDescription("客户管理");
		em.persist(customer_model);

		BusinessModel entity1_in_customer_model = new BusinessModel();
		entity1_in_customer_model.setApplication("csr");
		entity1_in_customer_model.setModule("my_customer_management");
		entity1_in_customer_model.setFunction("proprietary_customers");
		entity1_in_customer_model.setDescription("我的客户");
		entity1_in_customer_model.setParent(customer_model);
		em.persist(entity1_in_customer_model);

		// BusinessModel entity2_in_customer_model = new BusinessModel();
		// entity2_in_customer_model.setApplication("csr");
		// entity2_in_customer_model.setModule("my_customer_management");
		// entity2_in_customer_model.setFunction("success_customers");
		// entity2_in_customer_model.setDescription("我的成功客户");
		// entity2_in_customer_model.setParent(customer_model);
		// em.persist(entity2_in_customer_model);

		BusinessModel entity3_in_customer_model = new BusinessModel();
		entity3_in_customer_model.setApplication("csr");
		entity3_in_customer_model.setModule("my_customer_management");
		entity3_in_customer_model.setFunction("my_resources");
		entity3_in_customer_model.setDescription("我的资源");
		entity3_in_customer_model.setParent(customer_model);
		em.persist(entity3_in_customer_model);

	}

}
