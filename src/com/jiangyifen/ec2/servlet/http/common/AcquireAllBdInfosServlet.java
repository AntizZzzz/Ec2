package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.pojo.UserBindingInfo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于获取所有在线用户与分机的绑定信息
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/aquireAllBdInfos?accessId=xxx&accessKey=xxx
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/acquireAllBdInfos?accessId=xxx&accessKey=xxx
 * 
 * @author  JRH
 * @date    2014年8月11日 上午10:49:01
 */
@SuppressWarnings("serial")
public class AcquireAllBdInfosServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("获取所有用户与分机的绑定信息成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得

			List<UserBindingInfo> bindingInfoLs = new ArrayList<UserBindingInfo>();
			Set<Long> userIdSet = ShareData.userToExten.keySet();
			if(userIdSet != null && userIdSet.size() > 0) {
				for(Long userId : userIdSet) {
					User user = userService.getByIdInDomain(userId, domainId);
					if(user != null) {
						String exten = ShareData.userToExten.get(user.getId());
						exten = StringUtils.trimToEmpty(exten);		 // 如果用户尚未绑定分机，则将exten = "" 空字符串
						
						UserBindingInfo bindingInfo = new UserBindingInfo();
						bindingInfo.setExten(exten);
						bindingInfo.setUsername(user.getUsername());
						bindingInfoLs.add(bindingInfo);
					}
				}
			}
			
			if(bindingInfoLs.size() == 0) {
				commonRespBo.setMessage("获取所有用户与分机的绑定信息成功, 暂无绑定关系！");
			} else {
				commonRespBo.setResults(bindingInfoLs);
			}
			
			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 获取所有用户与分机的绑定信息, 出现异常！"+e.getMessage(), e);
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
			response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
			out.println("callback(" + GsonUtil.toJson(commonRespBo) + ");");
		}
		out.close();
	}

}
