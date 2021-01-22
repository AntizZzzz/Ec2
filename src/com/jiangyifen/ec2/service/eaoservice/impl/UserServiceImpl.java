package com.jiangyifen.ec2.service.eaoservice.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.eao.UserEao;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.Config;

public class UserServiceImpl implements UserService {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String IMPORT_SUCCESS = "import_success";	// 成功添加用户数
	public static final String IMPORT_IGNORED = "import_ignored";	// 因用户已存在，而被忽略的用户数
	
	private UserEao userEao;
	
	// enhance method
	
	@Override
	public User identify(String username, String password,RoleType roleType) {
		return userEao.identify(username, password,roleType);
	}

	@Override
	public User identify(String username, RoleType roleType) {
		return userEao.identify(username, roleType);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> loadPageEntities(int start, int length, String sql) {
		return userEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return userEao.getEntityCount(sql);
	}
	
	@Override
	public List<User> getAllByDomain(Domain domain) {
		return userEao.getAllByDomain(domain);
	}
	
	@Override
	public List<User> getByDeptment(Department department, Domain domain) {
		return userEao.getByDeptment(department,domain);
	}
	
	@Override
	public List<User> getCsrsByDepartment(List<Long> deptIds, Long domainId) {
		return userEao.getCsrsByDepartment(deptIds, domainId);
	}
	
	@Override
	public Map<String, Integer> addMutiUsers(String amountStr,
			String startEmpnoStr, String startUsernameStr, String startSecretStr, 
			String pwType, Department dept, Set<Role> roles, Domain domain) {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		resultMap.put(IMPORT_SUCCESS, 0);
		resultMap.put(IMPORT_IGNORED, 0);
		boolean isSame2Username = "Same2Username".equals(pwType) ? true : false;
		
		if(!amountStr.matches("[1-9]\\d*") || Integer.parseInt(amountStr) > 1000) {
			throw new RuntimeException("添加数量只能是大于 0 小于 1000 的数值！");
		}
		if(!startEmpnoStr.matches("\\d{1,32}")) {
			throw new RuntimeException("工号 只能由长度不大于32位的数字组成");
		}
		// TODO 武睿定制, 去掉用户名验证
		/*if(!startUsernameStr.matches("\\w{1,64}")) {
			throw new RuntimeException("用户名称 只能由长度不大于64位的字目或数字组成");
		}*/
		if(!isSame2Username && !startSecretStr.matches("\\w{1,32}")) {
			throw new RuntimeException("起始密码 只能由长度不大于32位的字符组成!");
		}
		if(dept == null) {
			throw new RuntimeException("用户所属部门不能为空！");
		}
		if(roles.size() < 1) {
			throw new RuntimeException("用户拥有角色不能为空！");
		}
		
		String empNo = startEmpnoStr;
		String username = startUsernameStr;
		String password = startSecretStr;
		boolean nameIsNum = false;
		if(startUsernameStr.matches("[1-9]\\d*")) {
			nameIsNum = true;
		} 
		
		UserService userService = (UserService) AopContext.currentProxy();
		Integer ignoredCount = 0;
		Integer amount = Integer.parseInt(amountStr); 
		for(int i = 1; i <= amount; i++) {
			List<User> sameEmpNoCsrs = userService.getUsersByEmpNo(empNo, domain);
			List<User> sameUsernameCsrs = userService.getUsersByUsername(username);
			// 如果用户工号已经存在，则按下一个工号添加
			if(sameEmpNoCsrs.size() > 0){
				++ignoredCount;
				empNo = Long.parseLong(empNo) + 1L +"";
				username = nameIsNum ? Long.parseLong(username) + 1L +"" : startUsernameStr + i; 
				continue;
			}

			// 如果用户名已经存在，则按下一个用户名添加
			if(sameUsernameCsrs.size() > 0){
				++ignoredCount;
				empNo = Long.parseLong(empNo) + 1L +"";
				username = nameIsNum ? Long.parseLong(username) + 1L +"" : startUsernameStr + i; 
				continue;
			}
			
			User user = new User();
			user.setEmpNo(empNo);
			user.setDomain(domain);
			user.setDepartment(dept);
			user.setUsername(username);
			if(isSame2Username == true) {
				user.setPassword(username);
			} else {
				user.setPassword(password);
			}
			user.setRoles(roles);
			user.setRegistedDate(new Date());

			// 修改下一个将要添加的用户信息
			empNo = Long.parseLong(empNo) + 1L +"";
			username = nameIsNum ? Long.parseLong(username) + 1L +"" : startUsernameStr + i; 
			
			try {
				userService.update(user);
			} catch (Exception e) {
				++ignoredCount;
				e.printStackTrace();
				logger.error("批量添加用户时出现异常 --> " +e.getMessage(), e);
				continue;
			}
		}
		
		/**
		 * @changelog 2014-6-19 上午10:34:18 chenhb <p>description: 按照用户ID添加语音信箱</p>
		 * 获取域内所有用户，然后刷新到文件
		 */
		List<User> userList = userService.getAllByDomain(domain);
		updateVoicemailConfig(domain, userList);
		
		resultMap.put(IMPORT_SUCCESS, amount - ignoredCount);
		resultMap.put(IMPORT_IGNORED, ignoredCount);
		return resultMap;
	}

	/**
	 * 更新voicemail配置文件
	 * @return
	 */
	@Override
	public boolean updateVoicemailConfig(Domain domain, List<User> userList) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"voicemail_domain_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"voicemail_domain_"+ domain.getName();
		
		boolean isSuccess = createVoicemailContent(srcFileName, dstFileName, userList);
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，创建新的语音信箱配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createVoicemailContent(String srcFileName, String dstFileName, List<User> userList) {
		File source = new File(srcFileName+ ".conf");
		File dst = new File(dstFileName+ ".conf.backup");
		
		// 删除原有的备份文件
		if(dst.exists()) {
			dst.delete();
		}
		
		// 创建新的备份文件
		source.renameTo(dst);
		
		// 创写新的sip.conf 文件
		boolean isSuccess = false;
		FileOutputStream fos = null;
		BufferedOutputStream bw = null;
		try {
			fos = new FileOutputStream(source);
			bw = new BufferedOutputStream(fos);
			StringBuffer sipcontent = new StringBuffer();
			
			sipcontent.append("\r\n\r\n\r\n");
			sipcontent.append(";;;;;;;;;;;;;;; custom voicemail.conf  ---  voicemail setting ;;;;;;;;;;;;;;\r\n");
			sipcontent.append("\r\n\r\n\r\n");
			
			Long domainId=null;
			// 写入基本配置信息
			for(int i=0;i<userList.size();i++) {
				User user = userList.get(i);
				if(i==0){
					domainId=user.getDomain().getId();
					sipcontent.append("[callvoicemail"+domainId+"]"+"\r\n");
				}
				
				sipcontent.append(user.getId()+" => "+user.getPassword()+",Userid "+user.getId()+","+user.getId()+"@callvoicemail"+domainId);
				
				sipcontent.append("\r\n\r\n\r\n");
			}
			
			String sipcontentStr = sipcontent.toString();
			bw.write(sipcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage()+"修改asterisk voicemail信息配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"修改asterisk voicemail信息配置文件，IO 流关闭时出现异常！", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	
	
	// common method

	@Override
	public User get(Object primaryKey) {
		if(primaryKey == null) {
			return null;
		}
		return userEao.get(User.class, primaryKey);
	}

	@Override
	public void save(User user) {
		userEao.save(user);
	}

	@Override
	public User update(User user) {
		return (User) userEao.update(user);
	}

	@Override
	public void delete(User user) {
		userEao.delete(user);
	}

	@Override
	public void deleteById(Object primaryKey) {
		userEao.delete(User.class, primaryKey);
	}

	
	//getter and setter
	public UserEao getUserEao() {
		return userEao;
	}

	public void setUserEao(UserEao userEao) {
		this.userEao = userEao;
	}
	
	/**
	 * chb
	 * 取出所有Csr用户
	 * @return
	 */
	@Override
	public List<User> getCsrsByDomain(Domain domain) {
		return userEao.getCsrs(domain);
	}
	
	/**
	 * chb
	 * 根据工号取出域内User
	 * 判断同一个域内工号是否重复
	 */
	@Override
	public List<User> getUsersByEmpNo(String empNo,Domain domain){
		return userEao.getUsersByEmpNo(empNo,domain);
	}

	/**
	 * chb
	 * 根据用户名取出全局（所有域）User
	 * 判断用户名是否相同
	 */
	@Override
	public List<User> getUsersByUsername(String username){
		return userEao.getUsersByUsername(username);
	}

	@Override
	public List<User> getUsersByUsername(String username, Long domainId) {
		return userEao.getUsersByUsername(username, domainId);
	}

	@Override
	public User getByIdInDomain(Long userId, Long domainId) {
		return userEao.getByIdInDomain(userId, domainId);
	}

	@Override
	public List<User> getAllUsersByJpql(String sql) {
		return userEao.getAllUsersByJpql(sql);
	}

	@Override
	public List<User> getMgrByDomain(Domain domain) {
		return userEao.getMgrByDomain(domain);
	}
	
	@Override
	public User getMgrByDomainId(Long domainId) {
		if(domainId == null || domainId == 0L) {	// 如果域的 ID 为空或为 0L 则返回空值
			return null;
		}
		
		List<User> userList = userEao.getMgrByDomainId(domainId);
		if(userList != null && userList.size() != 0) {		// 如果用户集合不为空
			return userList.get(0);
		}
		
		return null;
	}
	
}
