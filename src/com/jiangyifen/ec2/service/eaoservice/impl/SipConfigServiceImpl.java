package com.jiangyifen.ec2.service.eaoservice.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.SipConfigEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.Config;

public class SipConfigServiceImpl implements SipConfigService {

	// 日志工具
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private SipConfigEao sipConfigEao;
	private CommonEao commonEao;
	
	//===================== enhanced method ==================// 
	
	@Override
	public boolean existBySipname(String sipname) {
		return sipConfigEao.existBySipname(sipname);
	}
	
	@Override
	public boolean existBySipname(String sipname, Domain domain) {
		return sipConfigEao.existBySipname(sipname, domain);
	}

	@Override
	public List<SipConfig> getAllExtsByDomain(Domain domain) {
		return sipConfigEao.getAllExtsByDomain(domain);
	}

	@Override
	public List<SipConfig> getAllOutlinesByDomain(Domain domain) {
		return sipConfigEao.getAllOutlinesByDomain(domain);
	}

	@Override
	public SipConfig getDefaultOutlineByDomain(Domain domain) {
		return sipConfigEao.getDefaultOutlineByDomain(domain);
	}
	
	@Override
	public boolean updateAsteriskExtenSipConfigFile(Domain domain) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"sip_exten_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"sip_exten_"+ domain.getName();
		
		// 如果backup 备份文件夹不存在，则创建文件夹
		File file = new File(Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH));
		if(!file.exists()) {
			file.mkdir();
		}
		
		List<SipConfig> sips = sipConfigEao.getAllExtsByDomain(domain);
		boolean isSuccess = createExtenFileContent(srcFileName, dstFileName, sips);
		
		// 更新会议室配置文件
		updateMeetMeConfig(domain, sips);
		
		/**
		 * @changelog 2014-6-17 下午1:35:46 chenhb <p>description: 添加语音信箱支持 </p>
		 * 更新voicemail配置文件
		 * @changelog 2014-6-19 上午9:09:50 chenhb <p>description: 改为按照用户名或用户ID来建立语音信箱</p>
		 */
//		updateVoicemailConfig(domain, sips);
		
		return isSuccess;
	}
	
	/**
	 * 更新voicemail配置文件
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean updateVoicemailConfig(Domain domain, List<SipConfig> sips) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"voicemail_domain_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"voicemail_domain_"+ domain.getName();
		
		boolean isSuccess = createVoicemailContent(srcFileName, dstFileName, sips);
		return isSuccess ;
	}
	
	/**
	 * 更新meetme配置文件
	 * @return
	 */
	private boolean updateMeetMeConfig(Domain domain, List<SipConfig> sips) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"meetme_domain_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"meetme_domain_"+ domain.getName();
		
		boolean isSuccess = createMeetMeFileContent(srcFileName, dstFileName, sips);
		return isSuccess ;
	}

	@Override
	public boolean updateAsteriskOutlineSipConfigFile(Domain domain) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"sip_outline_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"sip_outline_"+  domain.getName();
		
		// 如果backup 备份文件夹不存在，则创建文件夹
		File file = new File(Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH));
		if(!file.exists()) {
			file.mkdir();
		}
		
		List<SipConfig> sips = sipConfigEao.getAllOutlinesByDomain(domain);
		boolean isSuccess = createOutlineFileContent(srcFileName, dstFileName, sips);
		return isSuccess;
	}
	
	@Override
	public boolean updateAsteriskRegisterSipConfigFile(Domain domain) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"sip_register_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"sip_register_"+  domain.getName();
		
		// 如果backup 备份文件夹不存在，则创建文件夹
		File file = new File(Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH));
		if(!file.exists()) {
			file.mkdir();
		}
		
		List<SipConfig> sips = sipConfigEao.getAllOutlinesByDomain(domain);
		boolean isSuccess = createRegisterFileContent(srcFileName, dstFileName, sips);
		return isSuccess;
	}

	/**
	 * 将源文件备份，并根据获取的分机或外线集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createRegisterFileContent(String srcFileName, String dstFileName, List<SipConfig> sips) {
		String ipRegex = "([1-9]||[1-9]\\d||1\\d{2}||2[0-4]\\d||25[0-5])(\\.(\\d||[1-9]\\d||1\\d{2}||2[0-4]\\d|25[0-5])){3}";	// ip 格式匹配
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
			sipcontent.append(";;;;;;;;;;;;;;; custom sip.conf  ---  register setting   ;;;;;;;;;;;;;;\r\n");
			sipcontent.append("\r\n\r\n\r\n");
			sipcontent.append("\r\n\r\n\r\n");
			
			// 如果是操作Sip 外线的配置文件，首先将注册信息写好(网关外线不需要注册)
			for(SipConfig sip : sips) {
				if(SipConfigType.sip_outline.equals(sip.getSipType()) && sip.getHost().matches(ipRegex)) {	// 是sip 外线，并且host 为正确的ip 格式， 如host 为 dynamic 的O口网关外线，sip_xmit.c 解析888@dynamic:5060 时出错，导致所有外线注册失败
					String port = sip.getPort();
					if(port == null || "".equals(port.toString().trim())) {
						port = "5060";
					} 
					String outlineRegistry = sip.getUsername() +":"+ sip.getSecret() +"@"+ sip.getHost() +":"+port+"/"+ sip.getUsername();
					sipcontent.append("register =>" + outlineRegistry);
					sipcontent.append("\r\n\r\n\r\n");
				}
			}
			
			String sipcontentStr = sipcontent.toString();
			bw.write(sipcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage()+"修改asterisk 外线注册信息配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"修改asterisk 外线注册信息配置文件，IO 流关闭时出现异常！", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，并根据获取的分机或外线集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createOutlineFileContent(String srcFileName, String dstFileName, List<SipConfig> sips) {
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
			sipcontent.append(";;;;;;;;;;;;;;; custom sip.conf  --- outline setting ;;;;;;;;;;;;;;\r\n");
			sipcontent.append("\r\n\r\n\r\n");
			
			// 写入基本配置信息
			for(SipConfig sip : sips) {
				sipcontent.append(analysisObject("name", sip.getName()));
				sipcontent.append(analysisObject("type", sip.getType()));
				sipcontent.append(analysisObject("context", sip.getContext()));
				sipcontent.append(analysisObject("qualify", sip.getQualify()));
				sipcontent.append(analysisObject("nat", sip.getNat()));
				sipcontent.append(analysisObject("canreinvite", sip.getCanreinvite()));
				sipcontent.append(analysisObject("disallow", sip.getDisallow()));
				sipcontent.append(analysisObject("allow", sip.getAllow()));
				sipcontent.append(analysisObject("call-limit", sip.getCall_limit()));
				sipcontent.append(analysisObject("host", sip.getHost()));
				sipcontent.append(analysisObject("secret", sip.getSecret()));
				sipcontent.append(analysisObject("port", sip.getPort()));
				
				// 如果是SIP外线，则需要配置username、fromuser、fromdomain
//				if(SipConfigType.sip_outline.equals(sip.getSipType())) {	// JRH 2015-01-06 主要是用 insecure 来解决网关外线的呼入问题，[insecure = port,invite] 这样就不用验证了，讯时网关也可以用了 
					sipcontent.append(analysisObject("username", sip.getUsername()));
					sipcontent.append(analysisObject("fromuser", sip.getFromuser()));
					sipcontent.append(analysisObject("fromdomain", sip.getFromdomain()));
					sipcontent.append(analysisObject("insecure", sip.getInsecure()));	// 如果是网关外线，不配置这个，将无法呼入
//				}
				sipcontent.append("\r\n\r\n\r\n");
			}
			String sipcontentStr = sipcontent.toString();
			bw.write(sipcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage()+"修改asterisk 外线基本信息配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"修改asterisk 外线基本信息配置文件，IO 流关闭时出现异常！", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，创建新的语音信箱配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createVoicemailContent(String srcFileName, String dstFileName, List<SipConfig> sips) {
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
			for(int i=0;i<sips.size();i++) {
				SipConfig sipConfig = sips.get(i);
				if(i==0){
					domainId=sipConfig.getDomain().getId();
					sipcontent.append("[callvoicemail"+domainId+"]"+"\r\n");
				}
				
				sipcontent.append(sipConfig.getName()+" => "+sipConfig.getSecret()+",Exten "+sipConfig.getName()+","+sipConfig.getName()+"@callvoicemail"+domainId);
				
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

	/**
	 * 将源文件备份，并根据获取的分机或外线集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param isWirteExt	是否为修改分机配置文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createExtenFileContent(String srcFileName, String dstFileName, List<SipConfig> sips) {
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
			sipcontent.append(";;;;;;;;;;;;;;; custom sip.conf  ---  exten setting ;;;;;;;;;;;;;;\r\n");
			sipcontent.append("\r\n\r\n\r\n");
			
			// 写入基本配置信息
			for(SipConfig sip : sips) {
				sipcontent.append(analysisObject("name", sip.getName()));
				sipcontent.append(analysisObject("type", sip.getType()));
				sipcontent.append(analysisObject("qualify", sip.getQualify()));
				sipcontent.append(analysisObject("context", sip.getContext()));
				sipcontent.append(analysisObject("host", sip.getHost()));
				sipcontent.append(analysisObject("canreinvite", sip.getCanreinvite()));
				sipcontent.append(analysisObject("disallow", sip.getDisallow()));
				sipcontent.append(analysisObject("allow", sip.getAllow()));
				sipcontent.append(analysisObject("nat", sip.getNat()));
				sipcontent.append(analysisObject("pickupgroup", sip.getPickupgroup()));
				sipcontent.append(analysisObject("callgroup", sip.getCallgroup()));
				sipcontent.append(analysisObject("secret", sip.getSecret()));
				sipcontent.append(analysisObject("call-limit", sip.getCall_limit()));
				sipcontent.append(analysisObject("port", sip.getPort()));

//				sipcontent.append(analysisObject("insecure", sip.getInsecure()));
//				sipcontent.append(analysisObject("username", sip.getUsername()));
//				sipcontent.append(analysisObject("fromuser", sip.getFromuser()));
//				sipcontent.append(analysisObject("fromdomain", sip.getFromdomain()));
//				sipcontent.append(analysisObject("cancallforward", sip.getCancallforward()));
//				sipcontent.append(analysisObject("lastms", sip.getLastms()));
//				sipcontent.append(analysisObject("regseconds", sip.getRegseconds()));
//				sipcontent.append(analysisObject("ipaddr", sip.getIpaddr()));
//				sipcontent.append(analysisObject("accountcode", sip.getAccountcode()));
//				sipcontent.append(analysisObject("amaflags", sip.getAmaflags()));
//				sipcontent.append(analysisObject("callerid", sip.getCallerid()));
//				sipcontent.append(analysisObject("defaultip", sip.getDefaultip()));
//				sipcontent.append(analysisObject("defaultuser", sip.getDefaultuser()));
//				sipcontent.append(analysisObject("deny", sip.getDeny()));
//				sipcontent.append(analysisObject("dtmfmode", sip.getDtmfmode()));
//				sipcontent.append(analysisObject("fullcontact", sip.getFullcontact()));
//				sipcontent.append(analysisObject("language", sip.getLanguage()));
//				sipcontent.append(analysisObject("mailbox", sip.getMailbox()));
//				sipcontent.append(analysisObject("mask", sip.getMask()));
//				sipcontent.append(analysisObject("md5secret", sip.getMd5secret()));
//				sipcontent.append(analysisObject("musiconhold", sip.getMusiconhold()));
//				sipcontent.append(analysisObject("permit", sip.getPermit()));
//				sipcontent.append(analysisObject("regexten", sip.getRegexten()));
//				sipcontent.append(analysisObject("regserver", sip.getRegserver()));
//				sipcontent.append(analysisObject("restrictcid", sip.getRestrictcid()));
//				sipcontent.append(analysisObject("rtpholdtimeout", sip.getRtpholdtimeout()));
//				sipcontent.append(analysisObject("rtptimeout", sip.getRtptimeout()));
//				sipcontent.append(analysisObject("setvar", sip.getSetvar()));
//				sipcontent.append(analysisObject("subscribecontext", sip.getSubscribecontext()));
				sipcontent.append("\r\n\r\n\r\n");
			}
			String sipcontentStr = sipcontent.toString();
			bw.write(sipcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage()+"修改asterisk 分机信息配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"修改asterisk 分机信息配置文件，IO 流关闭时出现异常！", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，并根据获取的分机或外线集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param isWirteExt	是否为修改分机配置文件
	 * @param sips			分机或外线集合
	 * @return boolean		创建是否成功
	 */
	private boolean createMeetMeFileContent(String srcFileName, String dstFileName, List<SipConfig> sips) {
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
			StringBuffer meetmeContent = new StringBuffer();
			
			meetmeContent.append("\r\n\r\n\r\n");
			meetmeContent.append(";;;;;;;;;;;;;;; custom meetme.conf setting ;;;;;;;;;;;;;;\r\n");
			meetmeContent.append("\r\n\r\n\r\n");
			
			// 写入基本配置信息
			for(SipConfig sip : sips) {
				// 如果是分机
				if(sip.getSipType()==SipConfigType.exten){
					meetmeContent.append("conf => "+sip.getName()+","+sip.getName()+"\r\n");
				}
			}
			String meetmeContentStr = meetmeContent.toString();
			bw.write(meetmeContentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage()+"chb:修改asterisk MeetMe 写文件  Exception！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"chb:修改asterisk MeetMe 写文件  Exception!", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}
	
	/**
	 * 解析分机的各个字段，然后组合成需要的值后返回
	 * @param key		字段名称
	 * @param value		字段值
	 * @return String	组合结果
	 */
	private String analysisObject(String key, Object value) {
		if(key.equals("name")) {
			return "["+value+"]"+"\r\n";
		} else if(value == null) {
			return "";
		} else if(value.getClass() == String.class && "".equals(((String)value).trim())) {
			return "";
		} else if(value.equals(true)) {
			return key+"=yes"+"\r\n";
		} else if(value.equals(false)) {
			return key+"=no"+"\r\n";
		} else {
			return key+"="+value+"\r\n";
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SipConfig> loadPageEntities(int start, int length, String sql) {
		return sipConfigEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return sipConfigEao.getEntityCount(sql);
	}
	
	/**
	 * chb 
	 * 根据外线取得domain
	 * @return
	 */
	@Override
	public Domain getDomainByOutLine(String outline) {
		return sipConfigEao.getDomainByOutLine(outline);
	}

	
	//===================== common method ==================// 
	
	@Override
	public SipConfig get(Object primaryKey) {
		return sipConfigEao.get(SipConfig.class, primaryKey);
	}

	@Override
	public synchronized void save(SipConfig sipConfig) {
		// 如果是分机，则自动为其设置分机名
		if(SipConfigType.exten.equals(sipConfig.getSipType())) {
			// Long maxSipnum = sipConfigEao.getMaxSipnameInExt() + 1L;
			Long maxSipnum = sipConfigEao.getMaxSipnameInExt6() + 1L;		// TODO 武睿定制, 分机改为 5 位数
			sipConfig.setName(maxSipnum.toString());
		}
		sipConfigEao.save(sipConfig);
	}

	@Override
	public synchronized SipConfig update(SipConfig sipConfig) {
		// 如果是新建的分机，则自动为其设置分机名
		if(SipConfigType.exten.equals(sipConfig.getSipType()) && sipConfig.getId() == null) {
			// Long maxSipnum = sipConfigEao.getMaxSipnameInExt() + 1L;		// TODO 武睿定制, 分机改为 5 位数
			Long maxSipnum = sipConfigEao.getMaxSipnameInExt6() + 1L;
			sipConfig.setName(maxSipnum.toString());
		}
		return sipConfigEao.update(sipConfig);
	}

	@Override
	public void delete(SipConfig sipConfig) {
		sipConfigEao.delete(sipConfig);
	}

	@Override
	public void deleteById(Object primaryKey) {
		sipConfigEao.deleteById(primaryKey);
	}
	
	// getter setter
	
	public SipConfigEao getSipConfigEao() {
		return sipConfigEao;
	}

	public void setSipConfigEao(SipConfigEao sipConfigEao) {
		this.sipConfigEao = sipConfigEao;
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

	/**
	 * chb 
	 * 根据domain 取得所有没有被其他项目使用的外线
	 * @return
	 */
	@Override
	public List<SipConfig> getAllUseableOutlinesByDomain(Domain domain) {
		return sipConfigEao.getAllUseableOutlinesByDomain(domain);
	}
	/**
	 * chb 
	 *根据外线的名字取出一条外线
	 * @return
	 */
	@Override
	public SipConfig getOutlineByOutlineName(String outlineName) {
		return sipConfigEao.getOutlineByOutlineName(outlineName);
	}

	@Override
	public List<SipConfig> getAllByJpql(String jpql) {
		return sipConfigEao.getEntitiesByJpql(jpql);
	}

	@Override
	public boolean checkDeleteAbleByIvrAction(boolean isExten, String sipName, Long domainId) {
		return sipConfigEao.checkDeleteAbleByIvrAction(isExten, sipName, domainId);
	}

	@Override
	public void checkIvrActionAndUpdate(boolean isExten, String sipName, Long domainId) {
		sipConfigEao.checkIvrActionAndUpdate(isExten, sipName, domainId);
	}
	
}
