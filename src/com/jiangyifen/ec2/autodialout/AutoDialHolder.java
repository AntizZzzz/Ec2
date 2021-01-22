package com.jiangyifen.ec2.autodialout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.event.QueueEntryEvent;

public class AutoDialHolder {
	//******************************队列线程前缀***********************************//
	//生产线程名称前缀
	public static String AUTODIAL_PRODUCER_PRE="autodial_producer"; //名字： autodial_producer(project_id)
	//消费线程名称前缀
	public static String AUTODIAL_CONSUMER_PRE="autodial_consumer"; //名字： autodial_consumer(autoDialoutTask_id)
	
	
	//******************************队列外呼线程***********************************//
	/**
	 * 线程的名字找到线程的引用，所以线程的名字不能重复
	 */
	public static Map<String, Thread> nameToThread=new ConcurrentHashMap<String, Thread>();


	//******************************队列中 排队数百分比 ***********************************//
	/**
	 * 根据队列对队列中登陆的成员数数量和队列排队等待的数量进行计数
	 */
	public static Map<String,Integer> queueToAvailable=new ConcurrentHashMap<String, Integer>();
	public static Map<String,Integer> queueToLoggedIn=new ConcurrentHashMap<String, Integer>();		// jrh 注释： 'ok' 状态下的队列成员总数（该成员可能是内部分机，也可能是手机）
	public static Map<String,Integer> queueToCallers=new ConcurrentHashMap<String, Integer>();
	
	/**
	 * 队列中的排队人信息（QueueEntryEvent 包含了排队人的信息）
	 */
	public static Map<String,List<QueueEntryEvent>> queueToWaiters=new ConcurrentHashMap<String, List<QueueEntryEvent>>();

}
