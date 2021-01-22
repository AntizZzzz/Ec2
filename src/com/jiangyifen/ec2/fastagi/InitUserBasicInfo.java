package com.jiangyifen.ec2.fastagi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.license.LicenseManager;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.LoggerUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 检查用户状态（用户是否登录）Agi 
 * @author
 */
public class InitUserBasicInfo extends BaseAgiScript {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String USERID="userId";
	public static final String USERNAME="userName";
	public static final String EMPNO="empNo";
	public static final String DOMAINID="domainId";
	public static final String OUTLINE="outline";
	
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		Map<String, String> licenseMap = LicenseManager.licenseValidate();
		String outlineNum = request.getParameter("outlineNum");		// 被叫号码
		if(licenseMap.get(LicenseManager.LICENSE_VALIDATE_RESULT)==LicenseManager.LICENSE_VALID){
			//有效
		}else{
			LoggerUtil.logWarn(this, "系统过期 不能打电话！ System out date! ");
			channel.hangup();
			return;
		}
		
		this.setVariable("isLogin", "false");
		String channelName=channel.getName();
		String callerId = channelName.substring(channelName.indexOf("/") + 1, channelName.indexOf("-"));
//		TODO 这里如果是转接操作，就需要提前设置外线值，而转接的callerId 就是外线号 -->这里要求外线号与asterisk 中的 extension 名相同		
		for(List<String> outlines : ShareData.domainToOutlines.values()) {
			if(outlines.contains(callerId)) {
				channel.setVariable(OUTLINE, callerId);
				break;
			}
		}
		
		Long userId = ShareData.extenToUser.get(callerId);
		
		//用户没有登陆，直接返回
		if(userId ==null){
			//如果是外线呼入，则说明是主叫转接，将登陆状态设置为True
			for(Long domainId:ShareData.domainToOutlines.keySet()){
				List<String> outlines=ShareData.domainToOutlines.get(domainId);
				for(String outline:outlines){
					if(outline.equals(callerId)){
						this.setVariable(DOMAINID, domainId.toString());
						this.setVariable("isLogin", "true");
					}
				}
			}
			return;
		}
		
		UserService userService=SpringContextHolder.getBean("userService");
		User user=userService.get(userId);
		recoreCdrInfo(user,channel,callerId, outlineNum);
		storeBasicInfo(user,channel,callerId, outlineNum);
		channel.setVariable("isLogin", "true");
	}
	
	/**
	 * 存储一些用户的基本信息
	 * @param user
	 * @param channel
	 * @throws AgiException
	 */
	private void storeBasicInfo(User user,AgiChannel channel,String callerId, String outlineNum) throws AgiException {
		String userId=user.getId().toString();
		String userName=user.getUsername().toString();
		String empNo=user.getEmpNo();
		String domainId=user.getDomain().getId().toString();
		
		channel.setVariable(USERID, userId);
		channel.setVariable(USERNAME, userName);
		channel.setVariable(EMPNO, empNo);
		channel.setVariable(DOMAINID, domainId);
		channel.setVariable(OUTLINE, getOutLine(channel,Long.parseLong(domainId),callerId));
		if(StringUtils.isNotBlank(outlineNum)) {
			channel.setVariable(OUTLINE, outlineNum);
		}
	}
	
	public String getOutLine(AgiChannel channel,Long domainId,String callerId)
			throws AgiException {
		
		// 首先选择静态外线成员：
		String outline = ShareData.extenToStaticOutline.get(callerId);
		if(outline==null){
			// 其次查找项目外线
			outline = ShareData.extenToDynamicOutline.get(callerId);
			if(outline==null){
				// 最后选出指定域中的默认外线
				outline = ShareData.domainToDefaultOutline.get(domainId);
				if(outline==null){
					outline = "000000";
				}
			}
		}
		return outline;
	}

	/**
	 * 记录CDR要使用的普通呼叫发起方信息
	 * @param user
	 * @throws AgiException 
	 */
	private void recoreCdrInfo(User user,AgiChannel channel,String callerId, String outlineNum) throws AgiException {
		//用户信息   jrh 使用自定义的 cdr 统计字段 开始
		Long srcUserId=user.getId();
		String srcUserIdStr = (srcUserId == null) ? "" : srcUserId.toString();
		channel.setVariable("CDR(srcUserId)", srcUserIdStr);
		
		String srcEmpNo = user.getEmpNo();
		srcEmpNo = (srcEmpNo == null) ? "" : srcEmpNo;
		channel.setVariable("CDR(srcEmpNo)", srcEmpNo);
		
		String srcUsername = user.getUsername();
		srcUsername = (srcUsername == null) ? "" : srcUsername;
		channel.setVariable("CDR(srcUsername)", encodeString(srcUsername));

		String srcRealName = user.getRealName();
		srcRealName = (srcRealName == null) ? "" : srcRealName;
		channel.setVariable("CDR(srcRealName)", encodeString(srcRealName));
		
		//部门信息
		Long srcDeptId = user.getDepartment().getId();
		String srcDeptIdStr = (srcDeptId == null) ? "" : srcDeptId.toString();
		channel.setVariable("CDR(srcDeptId)", srcDeptIdStr);
		
		String srcDeptName = user.getDepartment().getName();
		srcDeptName = (srcDeptName == null) ? "" : srcDeptName;
		channel.setVariable("CDR(srcDeptName)", encodeString(srcDeptName));
		
		//项目信息
		String projectIdStr = "";
		String projectName = "";
		Long projectId = ShareData.extenToProject.get(callerId);
		if(projectId != null){
			MarketingProjectService marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
			MarketingProject project = marketingProjectService.get(projectId);
			projectIdStr = project.getId().toString();
			projectName = (project.getProjectName() == null) ? "" : project.getProjectName();
		}
		channel.setVariable("CDR(projectId)", projectIdStr);
		channel.setVariable("CDR(projectName)", encodeString(projectName));
		
		//域信息
		Long domainId=user.getDomain().getId();
		String domainIdStr = (domainId == null) ? "" : domainId.toString();
		channel.setVariable("CDR(domainId)", domainIdStr);

		// 呼出使用的外线
		String usedOutlineName = (domainId == null) ? "" : getOutLine(channel, domainId,callerId);
		List<String> exts = ShareData.domainToExts.get(domainId);
		if(exts != null && !exts.contains(usedOutlineName)) {
			channel.setVariable("CDR(usedOutlineName)", usedOutlineName);
		}
		if(StringUtils.isNotBlank(outlineNum)) {
			channel.setVariable("CDR(usedOutlineName)", outlineNum);
		}
		
		// jrh 自定义统计字段 结束
	}

	/**
	 * 将字符串[中文]进行编码，然后返回
	 * @param needEncodeStr	需要编码的信息
	 * @return
	 */
	private String encodeString(String needEncodeStr) {
		try {
			needEncodeStr = URLEncoder.encode(needEncodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace();
			logger.error("jrh 自动外呼 为asterisk 而 UrlEncode 中文信息时出现异常-->"+e.getMessage(), e);
		}
		return needEncodeStr;
	}
	
}
