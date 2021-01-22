package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：坐席登录时，用户与分机进行绑定的接口
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/loginBind?accessId=xxx&accessKey=xxx&username=1001&exten=800001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/loginBind?accessId=xxx&accessKey=xxx&username=1001&exten=800001
 * 
 * @author  JRH
 * @date    2014年8月7日 下午2:10:14
 */
@SuppressWarnings("serial")
public class LoginBindServlet extends HttpServlet {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserLoginService userLoginService = SpringContextHolder.getBean("userLoginService");
	private UserService userService = SpringContextHolder.getBean("userService");
	private CommonService commonService = SpringContextHolder.getBean("commonService");

	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("用户与分机绑定成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			String exten = StringUtils.trimToEmpty(request.getParameter("exten"));
			

			if("".equals(username) || "".equals(exten)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名与分机号均不能为空！");
				logger.warn("JRH - IFACE 用户与分机绑定失败，原因：请求参数中用户名username 或 分机号exten 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			User loginUser = null;
			List<User> userLs = userService.getUsersByUsername(username, domainId);

			if (userLs == null || userLs.size() == 0) {
				List<User> existedUserLs = userService.getUsersByUsername(username);
				if(existedUserLs.size() > 0) {
					commonRespBo.setCode("-1");
					commonRespBo.setMessage("失败，用户名已经被占用！");
					logger.warn("JRH - IFACE 用户与分机绑定失败，原因：请求参数中用户名username "+username+", 已经被租户编号不是"+domainId+"的其他租户占用了！");
				} else {	// 添加用户
					List<Role> roleList = commonService.loadPageEntities(0, 1, "select r from Role as r where r.type = com.jiangyifen.ec2.bean.RoleType.csr and r.domain.id = "+domainId+" order by id asc");
					if(roleList.size() > 0) {	// 存在坐席角色
						List<Department> deptList = commonService.loadPageEntities(0, 1, "select d from Department as d where d.domain.id = "+domainId+" order by d.id asc");
						if(deptList.size() > 0) {
							Date registedDate = new Date();
							int countByEmpNo = commonService.getEntityCountByNativeSql("select count(*) from ec2_user where empNo = '"+username+"' and domain_id = "+domainId);
							String empNo = (countByEmpNo == 0) ? username : registedDate.getTime()+"";
							HashSet<Role> roles = new HashSet<Role>(roleList);
							Department dept = deptList.get(0);
							loginUser = new User();
							loginUser.setDepartment(deptList.get(0));
							loginUser.setEmpNo(empNo);
							loginUser.setPassword("123456");
							loginUser.setRegistedDate(registedDate);
							loginUser.setUsername(username);
							loginUser.setRoles(roles);
							loginUser.setDomain(dept.getDomain());
							userService.save(loginUser);
						}
					}
				}
			} else {
				loginUser = userLs.get(0);
			}
			
			try {
				if(loginUser != null) {
					userLoginService.login(username, loginUser.getPassword(), exten, "0.0.0.0", RoleType.csr);
				}
			} catch (Exception e) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，"+e.getMessage());
			}

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 登录绑定, 出现异常！"+e.getMessage(), e);
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
