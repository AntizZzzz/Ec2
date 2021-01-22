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

/**
 * @Description 描述信息: 主被叫挂断的信息存储 servlet 
 *
 * @auther jinht
 *
 * @date 2015-11-25 下午2:05:53
 */
@SuppressWarnings("serial")
public class HangupInfoServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("----------进入------实时挂断轮询请求----------");
		
		try {
			/*String username = request.getParameter("username");
			for(int i = 0; i < 120; i++) {
				if(WuRuiShareData.csrToHangupVoMap.containsKey(username) && null != WuRuiShareData.csrToHangupVoMap.get(username)) {
					HangupVo hangupVo = WuRuiShareData.csrToHangupVoMap.get(username);
					CommonRespBo commonRespBo = new CommonRespBo();
					commonRespBo.setCode(0);
					commonRespBo.setMessage("");
					commonRespBo.setResults(hangupVo);
					operateResponse(response, commonRespBo);
					WuRuiShareData.csrToHangupVoMap.remove(username);
					break;
				}
				Thread.sleep(1000);
			}*/
			/*for(int i = 0; i < 120; i++) {
				if(WuRuiShareData.csrToHangupVoMap.containsKey(username) && null != WuRuiShareData.csrToHangupVoMap.get(username)) {
					HangupVo hangupVo = WuRuiShareData.csrToHangupVoMap.get(username);
					CommonRespBo commonRespBo = new CommonRespBo();
					commonRespBo.setCode(0);
					commonRespBo.setMessage("");
					commonRespBo.setResults(hangupVo);
					operateResponse(response, commonRespBo);
					WuRuiShareData.csrToHangupVoMap.remove(username);
					break;
				} else {
					Thread.sleep(1000);
				}
			}*/
			
		} catch (Exception e) {
			logger.error("jinht - IFACE 获取实时挂断信息请求时异常",e);
			CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息					
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("获取实时挂断信息请求时错误");
			operateResponse(response, commonRespBo);
		}
		
		System.out.println("----------退出------实时挂断轮询请求----------");
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
		out.println("callbackHangUp(" + GsonUtil.toJson(commonRespBo) + ");");
		out.close();
	}
	
}
