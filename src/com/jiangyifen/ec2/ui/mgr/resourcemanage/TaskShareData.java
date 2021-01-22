package com.jiangyifen.ec2.ui.mgr.resourcemanage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TaskShareData {

	public static Map<Long, Task> taskMap = new ConcurrentHashMap<Long, Task>();
	
}
