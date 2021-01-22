package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 山西焦炭工作流配置文件
 * @author jrh
 *
 */
public class ShanXiJiaoTanWorkOrderConfig {
	
//	// check current customer is sanxijiaotan or not
//	public static final String PROJECT_FOR_SANXIJIAOTAN = "project_for_sanxijiaotan"; 	// 判断当前是不是山西焦炭的项目
	// 山西焦炭服务器的Mac 地址，用Mac 地址的好处只有是给山西焦炭做的时候才需要改Mac
	public static final String SANXIJIAOTAN_PC_MAC = "sanxijiaotan_pc_mac"; 			// 判断当前是不是山西焦炭的项目
	
	// the transfer conditions
	public static final String TRANSFER_TO_WORK_ORDER2 = "transfer_to_work_order2"; 	// 需要扭转到工作流程2的条件[成功土豆]
	public static final String TRANSFER_TO_WORK_ORDER3 = "transfer_to_work_order3"; 	// 需要扭转到工作流程3的条件[成功开户]

	// the first workflow
	public static final String WORK_ORDER1_PROJECT_ID = "work_order1_project_id"; 		// 第一个工作流对用的项目组id
	
	// the second workflow
	public static final String WORK_ORDER2_PROJECT_ID = "work_order2_project_id";  		// 第二个工作流对用的项目组id
	
	// the third workflow
	public static final String WORK_ORDER3_PROJECT_ID = "work_order3_project_id";  		// 第三个工作流对用的项目组id
	
	// the max count for csr who can pick new task from project per chance
	public static final String PICK_TASK_COUNT_PER_CHANCE_FOR_ORDER1 = "pick_task_count_per_chance_for_order1"; // 对于初级工作组，一次可获取的最大任务数（比如一销部门）
	public static final String MAX_TASK_COUNT_FOR_ORDER1 = "max_task_count_for_order1"; 						// 对于初级工作组，在获取部门前判断的最多拥有任务数（比如一销部门）
	public static final String PICK_TASK_COUNT_PER_CHANCE_FOR_ORDER2 = "pick_task_count_per_chance_for_order2"; // 对于中级工作组，一次可获取的最大任务数（比如二销部门）
	public static final String MAX_TASK_COUNT_FOR_ORDER2 = "max_task_count_for_order2"; 						// 对于中级工作组，在获取部门前判断的最多拥有任务数（比如二销部门）
	public static final String PICK_TASK_COUNT_PER_CHANCE_FOR_ORDER3 = "pick_task_count_per_chance_for_order3"; // 对于高级工作组，一次可获取的最大任务数（比如三销部门）
	public static final String MAX_TASK_COUNT_FOR_ORDER3 = "max_task_count_for_order3"; 						// 对于高级工作组，在获取部门前判断的最多拥有任务数（比如三销部门）

	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static{
		try {
			logger = LoggerFactory.getLogger(ShanXiJiaoTanWorkOrderConfig.class);
			props = new Properties();
			inputStream = ShanXiJiaoTanWorkOrderConfig.class.getClassLoader().getResourceAsStream("propertiefiles/shan_xi_jiao_tan_work_order.properties");
			props.load(inputStream);
		} catch (Exception e) {
			logger.error("san_xi_jiao_tan_work_order config file not found or cannot read!", e);
		}
	}
	
//	TODO
	public static void main(String[] args) {
		System.out.println(props.getProperty(SANXIJIAOTAN_PC_MAC));
		System.out.println(props.getProperty(TRANSFER_TO_WORK_ORDER2));
		System.out.println(props.getProperty(TRANSFER_TO_WORK_ORDER3));
		System.out.println();

		Long maxOncePickCount = Long.parseLong(props.getProperty(ShanXiJiaoTanWorkOrderConfig.PICK_TASK_COUNT_PER_CHANCE_FOR_ORDER1));
		System.out.println(maxOncePickCount);
		System.out.println();
		
//		ArrayList<String> transfer2Order3Names = new ArrayList<String>();
//		String transfer_to_work_order3 = (String) props.get(ShanXiJiaoTanWorkOrderConfig.TRANSFER_TO_WORK_ORDER3);
//		for(String singleCondition2 : transfer_to_work_order3.split(",")) {
//			System.err.println(singleCondition2);
//			transfer2Order3Names.add(singleCondition2);
//		}
//		System.err.println(transfer2Order3Names.contains("成功开户"));
	}
}
