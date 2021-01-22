package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.BusHandle;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于将用户使用的分机置忙，并根据需要创建置忙记录
 * 
 * 	坐席必须同时具备以下条件，方可进行置忙：
 * 
 * 		1、坐席存在（即要求用户名参数要正确）
 * 		2、坐席已经与分机建立了绑定关系（即相当于登录了EC2系统）
 * 		3、存在与绑定分机关联的队列
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/pauseUser?accessId=xxx&accessKey=xxx&username=1001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/pauseUser?accessId=xxx&accessKey=xxx&username=1001
 * 
 * @author  JRH
 * @date    2014年8月11日 上午11:30:01
 */
@SuppressWarnings("serial")
public class PauseUserServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private QueuePauseService queuePauseService = SpringContextHolder.getBean("queuePauseService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("置忙成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));

			if("".equals(username)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名不能为空！");
				logger.warn("JRH - IFACE 置忙失败，原因：请求参数中用户名username 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}

			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 置忙失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
					
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚未绑定分机！");
				logger.warn("JRH - IFACE 置忙失败，原因：用户"+username+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			// 这里将置忙原因全部都改为了忙碌状态
			String pauseReason = GlobalVariable.DEFAULT_IFACE_PAUSE_EXTEN_REASON;
			BusHandle busHandle = queuePauseService.pauseAllAndCreateLog(loginUser, exten, pauseReason, true, true);
			if(!busHandle.isSuccess()) {	// 如果置忙失败，则设置失败原因
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，"+busHandle.getNotice());
				logger.warn("JRH - IFACE 置忙失败，原因：可能是调用queuePauseService.pauseAllAndCreateLog() 时出现异常了！");
			}

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 置忙, 出现异常！"+e.getMessage(), e);
		}
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
			response.setContentType("text/plain");
			out.println("callback(" + GsonUtil.toJson(commonRespBo) + ");");
		}
		out.close();
	}

}
