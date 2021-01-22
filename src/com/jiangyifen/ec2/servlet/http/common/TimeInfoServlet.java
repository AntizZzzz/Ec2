package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.WuRuiShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.BridgeVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.HangupVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.PopupIncomingVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.TimeInfoVo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 实时获取弹屏信息
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/timeInfo?username=1001
 * 
 * @author LXY
 * @date    2014年8月14日 上午11:30:01
 */
public class TimeInfoServlet extends HttpServlet {

	private static final long serialVersionUID = 8943119815600338045L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private UserService userService = SpringContextHolder.getBean("userService");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("----------进入------实时弾屏轮询请求----------");
		try {
			String username = request.getParameter("username");
			List<User> usersList = userService.getUsersByUsername(username);
			String exten = null;
			if(usersList != null && usersList.size() > 0) {
				User user = usersList.get(0);
				exten = ShareData.userToExten.get(user.getId());
			}
			
			// 弾屏请求
			/*if(WuRuiShareData.csrToPopupIncomingVoMap.size() > 0)
				System.out.println("弾屏请求 -- "+WuRuiShareData.csrToPopupIncomingVoMap);
			if(WuRuiShareData.csrToBridgeVoMap.size() > 0)
				System.out.println("接起请求 -- "+WuRuiShareData.csrToBridgeVoMap);
			if(WuRuiShareData.csrToHangupVoMap.size() > 0)
				System.out.println("挂断请求 -- "+WuRuiShareData.csrToHangupVoMap);*/

			TimeInfoVo timeInfoVo = new TimeInfoVo();		// 响应信息
			timeInfoVo.setCode("-1");
			timeInfoVo.setMessage("无弾屏信息");
			if(WuRuiShareData.csrToPopupIncomingVoMap.containsKey(username) && null != WuRuiShareData.csrToPopupIncomingVoMap.get(username)){
				PopupIncomingVo phoneInfo = WuRuiShareData.csrToPopupIncomingVoMap.get(username);
				timeInfoVo.setCode("0");
				timeInfoVo.setMessage("");
				timeInfoVo.setResultsPhoneIn(phoneInfo);
				WuRuiShareData.csrToPopupIncomingVoMap.remove(username);
			}
			
			if(exten != null && !"".equals(exten)) {
				if(WuRuiShareData.csrToBridgeVoMap.containsKey(exten) && null != WuRuiShareData.csrToBridgeVoMap.get(exten)) {
					BridgeVo bridgeVo = WuRuiShareData.csrToBridgeVoMap.get(exten);
					timeInfoVo.setCode("0");
					timeInfoVo.setMessage("");
					timeInfoVo.setResultsBridge(bridgeVo);
					WuRuiShareData.csrToBridgeVoMap.remove(exten);
				}
			}
			
			if(WuRuiShareData.csrToHangupVoMap.containsKey(username) && null != WuRuiShareData.csrToHangupVoMap.get(username)) {
				HangupVo hangupVo = WuRuiShareData.csrToHangupVoMap.get(username);
				timeInfoVo.setCode("0");
				timeInfoVo.setMessage("");
				timeInfoVo.setResultsHangUp(hangupVo);
				WuRuiShareData.csrToHangupVoMap.remove(username);
			}
			
			operateResponse(response, timeInfoVo);
			
		} catch (Exception e) {
			logger.error("LXY - IFACE 试试获取弹屏电话信息异常",e);
			TimeInfoVo timeInfoVo = new TimeInfoVo();		// 响应信息					
			timeInfoVo.setCode("-1");
			timeInfoVo.setMessage("获取屏电话信息错误");
			
			operateResponse(response, timeInfoVo);
		}
			
		System.out.println("----------退出------实时弾屏轮询请求----------");
	}
	
	/**
	 * @Description 描述：返回操作的反馈信息
	 *
	 * @author  LXY
	 * @date    2014年8月14日 下午11:43:21
	 * @param response			HttpServletResponse
	 * @param commonRespBo		响应信息
	 * @throws IOException 
	 */
	private void operateResponse(HttpServletResponse response, TimeInfoVo timeInfoVo) throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
		out.println("callbackTimeInfo(" +GsonUtil.toJson(timeInfoVo) + ");");
				
		out.close();
	}

}

/*protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	System.out.println("----------进入------实时弾屏轮询请求----------");
	try {
		String username = request.getParameter("username");
		// 弾屏请求
		for(int i = 0; i < 120; i++) {
			if(WuRuiShareData.csrToPopupIncomingVoMap.size() > 0)
				System.out.println("弾屏请求 -- "+WuRuiShareData.csrToPopupIncomingVoMap);
			if(WuRuiShareData.csrToBridgeVoMap.size() > 0)
				System.out.println("接起请求 -- "+WuRuiShareData.csrToBridgeVoMap);
			if(WuRuiShareData.csrToHangupVoMap.size() > 0)
				System.out.println("挂断请求 -- "+WuRuiShareData.csrToHangupVoMap);
			
			if(WuRuiShareData.csrToPopupIncomingVoMap.containsKey(username) && null != WuRuiShareData.csrToPopupIncomingVoMap.get(username)){
				System.out.println(username + " -- " + WuRuiShareData.csrToPopupIncomingVoMap.get(username));					
				PopupIncomingVo vo = WuRuiShareData.csrToPopupIncomingVoMap.get(username);
				CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息					
				commonRespBo.setCode(0);
				commonRespBo.setMessage("");
				commonRespBo.setResults(vo);
				response.setCharacterEncoding("UTF-8");
				PrintWriter out = response.getWriter();
				response.setContentType("application/json");
				out.println("callbackPhoneIn(" + GsonUtil.toJson(commonRespBo) + ")");
				System.out.println("振铃转换结果: " + GsonUtil.toJson(commonRespBo));
				out.close();
				WuRuiShareData.csrToPopupIncomingVoMap.remove(username);
				break;
			}
			Thread.sleep(1000);
		}
		
		// 接起请求
		List<User> usersList = userService.getUsersByUsername(username);
		if(usersList != null && usersList.size() > 0) {
			User user = usersList.get(0);
			String exten = ShareData.userToExten.get(user.getId());
			if(WuRuiShareData.csrToBridgeVoMap.containsKey(exten) && null != WuRuiShareData.csrToBridgeVoMap.get(exten)) {
				BridgeVo bridgeVo = WuRuiShareData.csrToBridgeVoMap.get(exten);
				CommonRespBo commonRespBo = new CommonRespBo();
				commonRespBo.setCode(0);
				commonRespBo.setMessage("");
				commonRespBo.setResults(bridgeVo);
				operateResponse(response, commonRespBo);
				WuRuiShareData.csrToBridgeVoMap.remove(exten);
			}
		}
		
		// 挂断请求
		if(WuRuiShareData.csrToHangupVoMap.containsKey(username) && null != WuRuiShareData.csrToHangupVoMap.get(username)) {
			HangupVo hangupVo = WuRuiShareData.csrToHangupVoMap.get(username);
			CommonRespBo commonRespBo = new CommonRespBo();
			commonRespBo.setCode(0);
			commonRespBo.setMessage("");
			commonRespBo.setResults(hangupVo);
			operateResponse(response, commonRespBo);
			WuRuiShareData.csrToHangupVoMap.remove(username);
		}
		
	} catch (Exception e) {
		logger.error("LXY - IFACE 试试获取弹屏电话信息异常",e);
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息					
		commonRespBo.setCode(-1);
		commonRespBo.setMessage("获取屏电话信息错误");
		
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		out.println("callbackPhoneIn(" +GsonUtil.toJson(commonRespBo) + ")");
		System.err.println("振铃转换结果: " + GsonUtil.toJson(commonRespBo));
		
		out.close();
	}
		
	System.out.println("----------退出------实时弾屏轮询请求----------");
}*/