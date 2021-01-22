package com.jiangyifen.ec2.servlet.http.common.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.JointLicense;
import com.jiangyifen.ec2.service.eaoservice.JointLicenseService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：路径访问的过滤器
 * 
 * @author jrh
 * @date 2014年3月3日 下午1:47:56
 * @version v1.0.0
 */
public class HttpCommonFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private JointLicenseService jointLicenseService;

	@Override
	public void destroy() {	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			String requestURI = request.getRequestURI(); 			// 取得根目录所对应的绝对路径:
			
//			System.out.println(">JRH--->>>HttpCommonFilter.class-----requestURI-------"+requestURI);
//			System.out.println(!requestURI.startsWith("/ec2/http/common/"));

			if (!requestURI.startsWith("/ec2/http/common/")) {			// 检查范围路径，看是否需要过滤。如果是公用接口链接访问的uri 将以这个开头
				chain.doFilter(new XssHttpServletRequestWrapper(request), response); // 特殊字符创过滤
			} else {
				CommonRespBo commonRespBo = new CommonRespBo();			// 响应信息
				commonRespBo.setCode("0");
				
				String remoteIp = request.getRemoteAddr();

				if(AnalyzeIfaceJointUtil.IP_FILTERABLE_VALUE) {			// 检查是否需要进行 IP 过滤
					HashSet<String> ipSet = AnalyzeIfaceJointUtil.AUTHORIZE_IPS_SET;
					if(ipSet == null || !ipSet.contains(remoteIp)) { 	// IP 过滤，检查当前访问者的IP是否为允许调用EC2 接口的IP
						commonRespBo.setCode("-1");
						commonRespBo.setMessage("没有调用EC2该接口的权限！");
						logger.warn("JRH - IFACE 调用EC2接口失败，原因：没有调用EC2该接口的权限！");
						operateResponse(response, commonRespBo);
						return;
					}
				}
				
				if(AnalyzeIfaceJointUtil.JOINT_LICENSE_NECESSARY_VALUE) {	// 检查第三方系统调用EC2的接口是否需要License 验证（即：accessId 跟 accessKey 验证）
					if (jointLicenseService == null) {
						jointLicenseService = SpringContextHolder.getBean("jointLicenseService");
					}

					String accessId = StringUtils.trimToEmpty(request.getParameter("accessId"));
					String accessKey = StringUtils.trimToEmpty(request.getParameter("accessKey"));
					if("".equals(accessId) || "".equals(accessKey)) { 	// 检查请求参数是否合法
						commonRespBo.setCode("-1");
						commonRespBo.setMessage("访问编号(accessId) 与访问秘钥(accessKey) 均不能为空！");
						logger.warn("JRH - IFACE 调用EC2接口失败，原因：访问编号(accessId) 与访问秘钥(accessKey) 均不能为空！！");
						operateResponse(response, commonRespBo);
						return;
					}
					
					JointLicense jointLicense = jointLicenseService.getByIdKey(accessId, accessKey);
					if(jointLicense == null) { 	// 检查对接License是否存在
						commonRespBo.setCode("-1");
						commonRespBo.setMessage("访问编号(accessId) 或 访问秘钥(accessKey) 有误！");
						logger.warn("JRH - IFACE 调用EC2接口失败，原因：访问编号(accessId) 或 访问秘钥(accessKey) 有误！");
						operateResponse(response, commonRespBo);
						return;
					}

					request.setAttribute("domainId", jointLicense.getDomainId());		 // 设置租户编号
				} else {
					request.setAttribute("domainId", 1L);		 // 设置租户编号
				}

				chain.doFilter(new XssHttpServletRequestWrapper(request), response); 	// 特殊字符创过滤, 并放行
			}
		} catch (Exception e) {
			logger.error("JRH - IFACE 调用EC2接口出现异常！"+e.getMessage(), e);
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
		response.setContentType("text/plain");
		out.println(""+GsonUtil.toJson(commonRespBo));
		out.close();
	}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		logger.info("HttpCommonFilter 初始化");
	}

}
