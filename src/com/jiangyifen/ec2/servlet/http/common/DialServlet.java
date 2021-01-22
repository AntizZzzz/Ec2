package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于坐席发起呼叫
 * 
 * 	坐席必须同时具备以下条件，方可发起呼叫：
 * 
 * 		1、坐席存在（即要求用户名参数要正确）
 * 		2、被叫号码格式要正确（即必须包含数字）
 * 		3、坐席已经与分机建立了绑定关系（即相当于登录了EC2系统）
 * 		4、坐席当前没有尚未挂断的电话
 *  
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/dial?accessId=xxx&accessKey=xxx&username=1001&destNum=13816760988
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/dial?accessId=xxx&accessKey=xxx&username=1001&destNum=13816760988
 * 
 * @author  JRH
 * @date    2014年8月11日 上午10:19:01
 */
@SuppressWarnings("serial")
public class DialServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private DialService dialService = SpringContextHolder.getBean("dialService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("呼叫成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			String destNum = StringUtils.trimToEmpty(request.getParameter("destNum"));
			
			String outlineNum = StringUtils.trimToEmpty(request.getParameter("outline"));
			String poolNum = StringUtils.trimToEmpty(request.getParameter("poolNum"));
			destNum = pickOutPhoneNo(destNum);								// 去掉被叫号码中的字码

			if("".equals(username) || "".equals(destNum)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名和被叫号码均不能为空，并且被叫号码必须包含数字！");
				logger.warn("JRH - IFACE 发起呼叫失败，原因：请求参数中用户名username或被叫号码destNum均不能为空，或被叫号码不包含数字！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 发起呼叫失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚未绑定分机！");
				logger.warn("JRH - IFACE 发起呼叫失败，原因：用户"+username+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}

			ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
			boolean isregisted = true;
			if(extenStatus == null) {	// 分机状态维护信息不存在，则表明无法呼叫该分机
				isregisted = false;
			} else if(extenStatus.getRegisterStatus() == null) {	// 分机没注册上，则表明无法呼叫该分机
				isregisted = false;
			} else if(!extenStatus.getRegisterStatus().contains("OK") && !extenStatus.getRegisterStatus().contains("ok")) {	// 分机虽然注册了，但状态不可用，则表明无法呼叫该分机
				isregisted = false;
			}
			
			if(!isregisted) {
				commonRespBo.setCode("-2");
				commonRespBo.setMessage("分机不可用");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> channels = ShareData.peernameAndChannels.get(exten);
			if(channels != null && channels.size() > 0){
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚有通话未挂断！");
				logger.warn("JRH - IFACE 发起呼叫失败，原因：用户"+username+"尚有通话未挂断！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			try {
				dialService.dial(exten, destNum, outlineNum, poolNum);
			} catch (Exception e) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，未知错误！");
				logger.error("JRH - IFACE 发起呼叫失败，原因：调用dialService.dial(...), 出现异常！"+e.getMessage(), e);
			}

			operateResponse(response, commonRespBo);
			
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 发起呼叫, 出现异常！"+e.getMessage(), e);
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

    /**
     * 取出文件中的电话号码，如原始数据位 ‘ 135&%￥1676jrh0398  ’, 执行后返回 13516760398
     * @param originalData     原始数据
     * @return
     */
	private String pickOutPhoneNo(String originalData) {
		StringBuffer phoneNoStrBf = new StringBuffer();
		for (int i = 0; i < originalData.length(); i++) {
			char c = originalData.charAt(i);
			int assiiCode = c;
			if (assiiCode >= 48 && assiiCode <= 57) {
				phoneNoStrBf.append(c);
			}
		}
		return phoneNoStrBf.toString();
	}

}
