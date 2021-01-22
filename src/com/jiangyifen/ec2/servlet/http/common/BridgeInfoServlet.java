package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;

@SuppressWarnings("serial")
public class BridgeInfoServlet extends HttpServlet {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/*private UserService userService = SpringContextHolder.getBean("userService");*/

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("----------进入------实时接起轮询请求----------");
		
		try {
			/*String username = request.getParameter("username");
			List<User> usersList = userService.getUsersByUsername(username);
			if(usersList != null && usersList.size() > 0) {
				User user = usersList.get(0);
				String exten = ShareData.userToExten.get(user.getId());
				if(exten != null && !"".equals(exten)) {
					for(int i = 0; i < 120; i++) {
						if(WuRuiShareData.csrToBridgeVoMap.containsKey(exten) && null != WuRuiShareData.csrToBridgeVoMap.get(exten)) {
							BridgeVo bridgeVo = WuRuiShareData.csrToBridgeVoMap.get(exten);
							CommonRespBo commonRespBo = new CommonRespBo();
							commonRespBo.setCode(0);
							commonRespBo.setMessage("");
							commonRespBo.setResults(bridgeVo);
							operateResponse(response, commonRespBo);
							WuRuiShareData.csrToBridgeVoMap.remove(exten);
						}
						Thread.sleep(1000);
					}
					for(int i = 0; i < 120; i++) {
						if(WuRuiShareData.csrToBridgeVoMap.containsKey(exten) && null != WuRuiShareData.csrToBridgeVoMap.get(exten)) {
							BridgeVo bridgeVo = WuRuiShareData.csrToBridgeVoMap.get(exten);
							CommonRespBo commonRespBo = new CommonRespBo();
							commonRespBo.setCode(0);
							commonRespBo.setMessage("");
							commonRespBo.setResults(bridgeVo);
							operateResponse(response, commonRespBo);
							WuRuiShareData.csrToBridgeVoMap.remove(exten);
							break;
						} else {
							Thread.sleep(1000);
						}
					}
				}
			}*/
			
		} catch (Exception e) {
			logger.error("jinht - IFACE 获取实时接起信息请求时异常",e);
			CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息					
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("获取实时接起信息请求时错误");
			operateResponse(response, commonRespBo);
		}
		
		System.out.println("----------退出------实时接起轮询请求----------");
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
	private void operateResponse(HttpServletResponse response, CommonRespBo commonRespBo) throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
		out.println("callbackBridge(" +GsonUtil.toJson(commonRespBo) + ");");
		out.close();
	}
	
}
