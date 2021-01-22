package com.jiangyifen.ec2.servlet.http.common.queue;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述信息: 获取队列成员状态信息 
 *
 * 该接口用来获取队列成员的置忙状态, 满足以下条件:
 *  1. username 用户名, 首先会判断接口中传递的用户名是否为空, 如果不为空, 则进行判断系统中和该用户绑定的分机是否在线等信息, 
 * 如果满足判断条件, 则进行判断 queue 队列名称是否为空, 如果为空则返回该用户在所有队列中的置忙状态; 如果不为空, 则返回该用户在指定队列下的置忙状态.
 * 
 *  2. exten 分机名, 这里和 1 中的业务流程基本一致, 只是少了用户名判断.
 * 
 *  3. queue 队列名, 判断队列名称是否为空, 如果为空, 则查询所有队列下的队列成员的置忙状态; 如果不为空, 则查询某个队列下的所有队列成员的置忙状态.
 * 
 * http://192.168.1.160:8081/ec2/http/common/queue/acquireQueueMemberStatus?exten=800001&accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0
 * 获取单个分机所在所有队列下置忙状态的示例: callback({"code":0,"message":"成功","results":{"sipName":"800001","port":0,"pauseQueues2Reason":{},"unpauseQueues2Reason":{"900010":"空闲","900009":"空闲","900001":"空闲"}}})
 * 
 * http://192.168.1.160:8081/ec2/http/common/queue/acquireQueueMemberStatus?queue=900001&exten=800001&accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0
 * 获取单个分机所在单个队列下的置忙状态示例: callback({"code":0,"message":"成功","results":{"id":4545,"username":"1001","queue":"900001","sipname":"SIP/800001","reason":"空闲","pauseDate":1453108371998,"deptId":1,"deptName":"银来总部","domainId":1}})
 * 
 * http://192.168.1.160:8081/ec2/http/common/queue/acquireQueueMemberStatus?queue=900001&accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0
 * 获取单个队列返回结果的示例: callback({"code":0,"message":"成功","results":[{"id":4545,"username":"1001","queue":"900001","sipname":"SIP/800001","reason":"空闲","pauseDate":1453108371998,"deptId":1,"deptName":"银来总部","domainId":1},{"id":4542,"username":"1002","queue":"900001","sipname":"SIP/800002","reason":"空闲","pauseDate":1453108371667,"deptId":61,"deptName":"中金分部","domainId":1}]})
 * 
 * http://192.168.1.160:8081/ec2/http/common/queue/acquireQueueMemberStatus?accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0
 * 获取所有队列成员信息的返回结果示例: callback({"code":0,"message":"成功","results":{"900010":[{"id":4547,"username":"1001","queue":"900010","sipname":"SIP/800001","reason":"空闲","pauseDate":1453108372007,"deptId":1,"deptName":"银来总部","domainId":1},{"id":4544,"username":"1002","queue":"900010","sipname":"SIP/800002","reason":"空闲","pauseDate":1453108371677,"deptId":61,"deptName":"中金分部","domainId":1}],"900009":[{"id":4546,"username":"1001","queue":"900009","sipname":"SIP/800001","reason":"空闲","pauseDate":1453108372003,"deptId":1,"deptName":"财务部","domainId":1},{"id":4543,"username":"1002","queue":"900009","sipname":"SIP/800002","reason":"空闲","pauseDate":1453108371672,"deptId":61,"deptName":"中金分部","domainId":1}],"900001":[{"id":4545,"username":"1001","queue":"900001","sipname":"SIP/800001","reason":"空闲","pauseDate":1453108371998,"deptId":1,"deptName":"客服部","domainId":1},{"id":4542,"username":"1002","queue":"900001","sipname":"SIP/800002","reason":"空闲","pauseDate":1453108371667,"deptId":61,"deptName":"销售部","domainId":1}]}}) 
 * 
 * @auther jinht
 *
 * @date 2016-1-15 下午5:53:41
 */
@SuppressWarnings("serial")
public class AcquireQueueMemberStatusServlet extends HttpServlet {
	
	private StaticQueueMemberService staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");	// 静态队列成员业务类
	private UserQueueService userQueueService = SpringContextHolder.getBean("userQueueService");
	private QueueService queueService = SpringContextHolder.getBean("queueService");
	private UserService userService = SpringContextHolder.getBean("userService");
	private QueuePauseRecordService queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();
		commonRespBo.setCode("0");
		commonRespBo.setMessage("获取分机状态信息成功！");
		
		try {
			// 通过 HttpCommonFilter 过滤器获取 domainId
			Long domainId = (Long) request.getAttribute("domainId");

			// 根据用户名查询
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			// 根据分机号查询
			String exten = StringUtils.trimToEmpty(request.getParameter("exten"));
			// 根据队列名称查询
			String queue = StringUtils.trimToEmpty(request.getParameter("queue"));
			
			if(!"".equals(username)) {
				List<User> users = userService.getUsersByUsername(username, domainId);
				if(users != null && users.size() > 0) {
					String userToExten = ShareData.userToExten.get(users.get(0).getId());
					Boolean isSuccess = obtainExtenStatus(userToExten, queue, commonRespBo);
					if(isSuccess) {
						operateResponse(response, commonRespBo);
						return;
					}
				}
				
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败, 用户名在系统中不存在!");
				return;
			}
			
			Boolean isSuccess = obtainExtenStatus(exten, queue, commonRespBo);
			if(isSuccess) {
				operateResponse(response, commonRespBo);
				return;
			}
			
			isSuccess = obtainQueueMembersStatus(queue, domainId, commonRespBo);
			if(isSuccess) {
				operateResponse(response, commonRespBo);
				return;
			}
			
			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败, 其他问题!");
			operateResponse(response, commonRespBo);
			
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取分机在各个队列下的置忙置闲状态
	 * @param exten
	 * @param commonRespBo
	 * @return
	 */
	private Boolean obtainExtenStatus(String exten, String queue, CommonRespBo commonRespBo) {
		if(!"".equals(exten)) {
			Long userId = ShareData.extenToUser.get(exten);
			if(userId == null) {
				commonRespBo.setCode("-2");
				commonRespBo.setMessage("失败, 分机不存在或分机没有和用户进行绑定!");
				return true;
			}
			
			// 如果分机和队列都不为空, 则获取分机在队列中的置忙状态
			if(!"".equals(queue)) {
				User user = userService.get(userId);
				if(user != null) {
					QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(user.getUsername(), exten, queue);
					commonRespBo.setCode("0");
					commonRespBo.setMessage("成功");
					commonRespBo.setResults(queuePauseRecord);
					return true;
				}
			}
			
			// 如果分机不为空, 队列为空, 则获取分机在所有队列中的置忙状态
			ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功");
			commonRespBo.setResults(extenStatus);
			return true;
		}
		
		commonRespBo.setCode("-2");
		commonRespBo.setMessage("失败, 分机不存在");
		return false;
	}
	
	/**
	 * 获取某个队列下所有在线队列成员的置忙状态
	 * @param queueName
	 * @param domainId
	 * @param commonRespBo
	 * @return
	 */
	private Boolean obtainQueueMembersStatus(String queueName, Long domainId, CommonRespBo commonRespBo) {
		if(domainId == null) {
			commonRespBo.setCode("-3");
			commonRespBo.setMessage("失败, 没有找到当前用户所在的域");
			return true;
		}
		
		// 如果队列不为空, 则获取单个队列下的所有成员的置忙状态
		if(!"".equals(queueName)) {
			Queue queue = queueService.getByName(queueName, domainId);
			if(queue != null) {
				List<QueuePauseRecord> queuePauseRecords = getQueuePauseRecords(queue, domainId);
				commonRespBo.setCode("0");
				commonRespBo.setMessage("成功");
				commonRespBo.setResults(queuePauseRecords);
				return true;
			}
			
			commonRespBo.setCode("-2");
			commonRespBo.setMessage("失败, 队列名称在系统中不存在!");
			return false;
		}
		
		// 如果队列为空, 则获取所有队列成员的状态
		List<Queue> queues = queueService.getAllByDomain(domainId, true);
		if(queues != null && queues.size() > 0) {
			Map<String, List<QueuePauseRecord>> queuePauseRecordsMap = new HashMap<String, List<QueuePauseRecord>>();
			for(Queue queue : queues) {
				List<QueuePauseRecord> queuePauseRecords = getQueuePauseRecords(queue, domainId);
				queuePauseRecordsMap.put(queue.getName(), queuePauseRecords);
			}
			
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功");
			commonRespBo.setResults(queuePauseRecordsMap);
			return true;
		}
		
		commonRespBo.setCode("-2");
		commonRespBo.setMessage("失败, 队列为不存在!");
		return false;
	}
	
	/**
	 * 获取队列下成员的置忙状态
	 */
	private List<QueuePauseRecord> getQueuePauseRecords(Queue queue, Long domainId) {
		List<User> users = new ArrayList<User>();
		
		if(queue.getDynamicmember() != null && queue.getDynamicmember()) {	// 动态队列
			List<UserQueue> dynQueueMembers = userQueueService.getAllByQueueName(queue.getName(), domainId);
			for(UserQueue dynQueueMember : dynQueueMembers) {
				String userName = dynQueueMember.getUsername();
				users.addAll(userService.getUsersByUsername(userName, domainId));
			}
			
		} else {	// 静态队列
			List<StaticQueueMember> staticQueueMembers = staticQueueMemberService.getAllByQueueName(domainId, queue.getName());
			for(StaticQueueMember staticQueueMember : staticQueueMembers) {
				String exten = staticQueueMember.getSipname();
				Long userId = ShareData.extenToUser.get(exten);
				if(userId != null) {
					users.add(userService.get(userId));
				}
			}
		}
		
		List<QueuePauseRecord> queuePauseRecords = new ArrayList<QueuePauseRecord>();	// 根据选有的用户对象，创建新的表格显示对象
		for(User user : users) {
			String exten = ShareData.userToExten.get(user.getId());
			if(StringUtils.isNotEmpty(exten)) {
				QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(user.getUsername(), exten, queue.getName());
				if(queuePauseRecord != null) 
					queuePauseRecords.add(queuePauseRecord);
			}
		}
		
		return queuePauseRecords;
	}
	
	/**
	 * @Description 描述：返回操作的反馈信息
	 *
	 * @author  JRH
	 * @date    2014年8月8日 下午12:43:21
	 * @param response			HttpServletResponse
	 * @param commonRespBo		响应信息
	 * @throws IOException 
	 */
	private void operateResponse(HttpServletResponse response, CommonRespBo commonRespBo) throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		if("false".equals(AnalyzeIfaceJointUtil.WHETHER_SUPPORT_CORS)) {
			response.setContentType("text/plain");
			out.println(""+GsonUtil.toJson(commonRespBo));
		} else {
			response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
			out.println("callback(" + GsonUtil.toJson(commonRespBo) + ");");
		}
		out.close();
	}
	
}
