package com.jiangyifen.ec2.service.csr.ami.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.OriginateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 呼叫挂断操作Service
 * @author
 */
public class DialServiceImpl implements DialService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private SipConfigService sipConfigService;
	
	/**
	 * 从指定分机号拨打指定电话
	 * @param exten					主叫(分机号)
	 * @param connectedLineNum		被叫(要拨打的号码)
	 */
	@Override
	public Boolean dial(final String exten, final String connectedLineNum) {
		// 取出数字号码
		String toDialNumber = pickOutPhoneNo(connectedLineNum);
		
		return executeDial(exten, toDialNumber, null, null);
	}
	
	@Override
	public Boolean dial(String exten, String connectedLineNum, String outline, String poolNum) {
		// 取出数字号码
		String toDialNumber = pickOutPhoneNo(connectedLineNum);

		return executeDial(exten, toDialNumber, outline, poolNum);
	}

	@Override
	public Boolean dial(String exten, String connectedLineNum, boolean nonNumericAble) {
		// 取出数字号码
		String toDialNumber = connectedLineNum;
		if(nonNumericAble) {
			toDialNumber = pickOutPhoneNo(connectedLineNum);
		}
		return executeDial(exten, toDialNumber, null, null);
	}

	/**
	 * @Description 描述：执行呼叫
	 *
	 * @author  jrh
	 * @date    2014年4月14日 下午8:49:08
	 * @param exten					主叫(分机号)
	 * @param connectedLineNum		被叫(要拨打的号码)
	 * @return Boolean
	 */
	private Boolean executeDial(final String exten, String toDialNumber, String outlineNum, String poolNum) {
		//==============自动进行归属地识别 Start===================//
		Long domainId=ShareData.extenToDomain.get(exten);
		if(domainId == null) {	// 如果根据分机获取域的编号失败(如该分机没有用户登录，则该值为null),则通过域找分机来解决
			Set<Long> didSet = ShareData.domainToExts.keySet();
			if(didSet != null) {
				for(Long did : ShareData.domainToExts.keySet()) {
					List<String> extLs = ShareData.domainToExts.get(did);
					if(extLs != null && extLs.contains(exten)) {
						domainId = did;
					}
				}
			}
		}
		
		// 判断是否要使用自定处理归属地问题
		Boolean belong = null;
		
		// jrh 判断是否是特殊呼叫，比如监听：555开头；拨打分机：800001等；打入会议室：998开头；
		boolean isSpecialDial = false;

		// jrh 这里有可能为空
		if(domainId != null){
			if(ShareData.domainToConfigs.get(domainId) != null) {
				belong = (Boolean)ShareData.domainToConfigs.get(domainId).get("belong_config");
			}
			
			List<String> extens = ShareData.domainToExts.get(domainId);
			if(toDialNumber.startsWith("555") || toDialNumber.startsWith("998") 
					|| (extens != null && extens.contains(toDialNumber))) {
				isSpecialDial = true;
			}
		}

		String outline=getOutLine(domainId,exten);
		
		//外线号码--->外线号码池编号-->原有设定外线
		if(StringUtils.isNotBlank(poolNum) && StringUtils.isEmpty(outlineNum)) {
			// 获取随机外线
			Set<SipConfig> sips = ShareData.outlinePoolToOutline.get(poolNum);
			if(sips != null && sips.size() > 0) {
				List<SipConfig> sipsTem = new ArrayList<SipConfig>(sips);
				// 随机获取一条外线
				SipConfig sipconfig = sipsTem.get(getRandomNum(sipsTem.size()));
				outline = sipconfig.getName();
				LOGGER.info("-------从外线池随机获取外线号码为： " + outline);
			}
		}
		if(StringUtils.isNotBlank(outlineNum)) {
			outline = outlineNum;
		}

		// 判断是否启用了按归属地判断拨打方式、是否不是拨打特殊号码
		if(belong !=null && isSpecialDial == false){	
			//查找外线区号
			if(sipConfigService==null){
				sipConfigService=SpringContextHolder.getBean("sipConfigService");
			}
			SipConfig outlineSipConfig=sipConfigService.getOutlineByOutlineName(outline);
			String postCode="";
			postCode=outlineSipConfig.getBelong();
			if(!postCode.equals("")){
				if(belong==false){ //加0
					toDialNumber=MobileLocUtil.prefixProcessZero(toDialNumber,postCode);
				}else if(belong==true){ //加区号
					toDialNumber=MobileLocUtil.prefixProcessPostCode(toDialNumber,postCode);
				}
			}
		}
		
		// 呼叫Action
		OriginateAction originateAction = new OriginateAction();
		originateAction.setTimeout(10*60*1000L);
		originateAction.setChannel("SIP/"+exten);
		originateAction.setCallerId(exten);
		originateAction.setExten(toDialNumber);
		originateAction.setContext("outgoing");
		originateAction.setPriority(1);
		originateAction.setAsync(true);
		
		/**
		 *  设置呼叫类型，呼出的呼叫类型有两种，用软电话直接呼叫(softphonecall)和在系统中点击按钮呼叫(systemcall)
		 *  如果是系统呼叫，则不需要查数据库来回显弹屏信息，而软电话直接呼，需要查找数据库，找出资源后再回显弹屏信息
		 */
		originateAction.setVariable("CALLTYPE", "systemcall");
		// 设置被叫号码
		originateAction.setVariable("CALLERID(DNID)", toDialNumber);
		originateAction.setVariable("CDR(usedOutlineName)", outline);	// 设置使用的外线
		originateAction.setVariable("outlineNum", outline);
		
		//呼叫
		return AmiManagerThread.sendAction(originateAction);
	}
	
	private int getRandomNum(int max) {
		 Random rand = new Random();
		 return rand.nextInt(max);  
	}
	
    /**
     * 取出文件中的电话号码，如原始数据位 ‘ 135&%￥1676jrh0399  ’, 执行后返回 13516760399
     * @param originalData     原始数据
     * @return
     */
    private String pickOutPhoneNo(String originalData) {
          String phoneNoStr = "";
          originalData = StringUtils.trimToEmpty(originalData);
          for(int i = 0; i < originalData.length(); i++) {
               char c = originalData.charAt(i);
               int assiiCode = (int) c;
               if(assiiCode >= 48 && assiiCode <= 57) {
                    phoneNoStr = phoneNoStr + c;
               }
          }
          return phoneNoStr;
    }
	
	private String getOutLine(Long domainId,String callerId){
		//选择外线
		String outline = ShareData.extenToStaticOutline.get(callerId);
		if(outline==null){
			// 选出指定域中的默认外线
			outline = ShareData.extenToDynamicOutline.get(callerId);
			if(outline==null){
				outline = ShareData.domainToDefaultOutline.get(domainId);
				if(outline==null){
					outline = "000000";
				}
			}
		}
		return outline;
	}

	@Override
	public boolean channelSpy(String exten, String connectedLineNum, String spiedChannel) {
		// 取出数字号码
		String toDialNumber = pickOutPhoneNo(connectedLineNum);
		
		// 呼叫Action
		OriginateAction originateAction = new OriginateAction();
		originateAction.setTimeout(10*60*1000L);
		originateAction.setChannel("SIP/"+exten);
		originateAction.setCallerId(exten);
		originateAction.setExten(toDialNumber);
		originateAction.setVariable("spychannel", spiedChannel);
		originateAction.setContext("outgoing");
		originateAction.setPriority(1);
		originateAction.setAsync(true);
		
		/**
		 *  设置呼叫类型，呼出的呼叫类型有两种，用软电话直接呼叫(softphonecall)和在系统中点击按钮呼叫(systemcall)
		 *  如果是系统呼叫，则不需要查数据库来回显弹屏信息，而软电话直接呼，需要查找数据库，找出资源后再回显弹屏信息
		 */
		originateAction.setVariable("CALLTYPE", "systemcall");
		// 设置被叫号码
		originateAction.setVariable("CALLERID(DNID)", toDialNumber);
		
		//呼叫
		return AmiManagerThread.sendAction(originateAction);
	}
	
}
