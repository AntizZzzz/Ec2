package com.jiangyifen.ec2.fastagi;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DomainService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.workarea.callrecord.CdrTabView;
import com.jiangyifen.ec2.ui.csr.workarea.historyservicerecord.MyHistoryServiceRecordTabView;
import com.jiangyifen.ec2.ui.csr.workarea.marketingtask.MyMarketingTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.mycustomer.ProprietaryCustomersTabView;
import com.jiangyifen.ec2.ui.csr.workarea.myresource.MyResourcesTabView;
import com.jiangyifen.ec2.ui.csr.workarea.order.MyHistoryOrderTabView;
import com.jiangyifen.ec2.ui.csr.workarea.questionnairetask.QuestionnaireTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.servicerecord.MyServiceRecordTabView;
import com.jiangyifen.ec2.utils.ExternalInterface;
import com.jiangyifen.ec2.utils.HttpIfaceUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.VerticalLayout;

/**
 * CSR 呼叫外线时进行弹屏操作
 * @author jrh
 */
public class PopupOutgoingWindow extends BaseAgiScript {
	
	// 弹屏后是否需要将分机置忙
	private final static String PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW = "pasue_exten_after_csr_popup_calling_window";
	private final static String CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW = "create_afterCall_log_after_csr_popup_calling_window";
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		// 取得被叫号码
		String destNum = request.getDnid();
		if(destNum == null) {
			destNum = request.getExtension();
		}
		
		// 获取CSR 的id号
		String name = channel.getName();
		String exten = name.substring(name.indexOf("/")+1, name.indexOf("-"));
		Long domainId = ShareData.extenToDomain.get(exten);
//		TODO 由于tomcat 重启时，免重新登录的代码中并没与把分机跟域的对应关系加入，所以extenToDomain可能得不到域的id
		if(domainId == null) {
			for(Long key : ShareData.domainToExts.keySet()) {
				if(ShareData.domainToExts.get(key).contains(exten)) {
					domainId = key;
					break;
				}
			}
		}
		// 如果没找到域对应的id 值，则直接退出
		if(domainId == null) {
			return;
		}
		
		Long userId = ShareData.extenToUser.get(exten);
		CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(userId);
		
		// 获取用户的信息
		UserService userService = SpringContextHolder.getBean("userService");
		User loginUser = userService.get(userId);
		
		// 按拨打号码进行查询资源
		CustomerResourceService customerResourceService = SpringContextHolder.getBean("customerResourceService");
		CustomerResource customerResource = customerResourceService.getCustomerResourceByPhoneNumber(destNum, domainId);

		// 如果是拨打的不是分机号码，则查找该电话号码有没有与之对应的客户，如果没有，并且当前坐席已经用电脑登陆了，则创建新的CustomerResource 与 Telephone  对象
		if(customerResource == null) {
			DomainService domainService = SpringContextHolder.getBean("domainService");
			Domain domain = domainService.getDomain(domainId);
			
			Telephone telephone = new Telephone();
			telephone.setNumber(destNum);
			telephone.setDomain(domain);

			Set<Telephone> telephones = new HashSet<Telephone>();
			telephones.add(telephone);

			customerResource = new CustomerResource();
			customerResource.setDomain(domain);
			customerResource.setImportDate(new Date());
			customerResource.setTelephones(telephones);
			Calendar cal = Calendar.getInstance();
			cal.set(1970, 0, 1, 0, 0, 0);
			customerResource.setCount(1);
			customerResource.setLastDialDate(new Date());
			customerResource.setExpireDate(cal.getTime());
			telephone.setCustomerResource(customerResource); 						// JRH 如果系统不存在，则自动存储 20141118
			customerResource = customerResourceService.update(customerResource);	// JRH 如果系统不存在，则自动存储 20141118
		} else if(customerResource != null) {
			// 更新客户信息
			int dialCount = customerResource.getCount() + 1;
			customerResource.setCount(dialCount);
			customerResource.setLastDialDate(new Date());
			customerResource = customerResourceService.update(customerResource);
		}
		
		// 获取外线配置
		SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");
		String vasOutline = StringUtils.trimToEmpty(channel.getVariable("outline"));
		SipConfig outlineObj = sipConfigService.getOutlineByOutlineName(vasOutline);
		if(outlineObj != null && !outlineObj.getIspopupWin()) {		// 如果外线存在(正常都会存在)，并且外线设置为不需要弹屏，则直接返回
			return;
		}
		
		// 如果当前被叫没有用电脑登录系统，则直接退出
		if(csrStatusBar == null) {
			return;
		}
		
		// 座席登录系统后呼出电话时, 弾屏推送信息到第三方系统
		if("true".equals(ExternalInterface.OUTGOING_ELASTIC_SCREEN_INTERFACE_IS_OPEN)) {
			// 被叫号码, 主叫分机, 主叫用户Id, 主叫用户名
			final String params = MessageFormat.format(ExternalInterface.OUTGOING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS, destNum, exten, userId, loginUser.getUsername());
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpIfaceUtil.doPostRequest(ExternalInterface.OUTGOING_ELASTIC_SCREEN_INTERFACE_URL, params);
				}
			}).start();
		}
		
		// 获取呼叫类型，呼出的呼叫类型有两种，用软电话直接呼叫(softphonecall)和在系统中点击按钮呼叫(systemcall)
		// 如果是系统呼叫，则不需要查数据库来回显弹屏信息，而软电话直接呼，需要查找数据库，找出资源后再回显弹屏信息
		String dialType = channel.getVariable("CALLTYPE");
		VerticalLayout currentTab = ShareData.csrToCurrentTab.get(userId);
		
		// 如果界面组件不为空表示登陆了系统，然后根据外呼事件的发生点的不同，弹出相对应的窗口
		if(currentTab == null) {	// 坐席登录了系统，但是没有选中的Tab页
			csrStatusBar.showSoftPhoneCallPopWindow(customerResource, channel);
		} else if(currentTab != null) {
			// 如果是用软电话直接呼叫的，则使用状态栏的弹窗, 	如果不是，则根据呼出事件发生点进行弹窗
			if(!"systemcall".equals(dialType)) {
				csrStatusBar.showSoftPhoneCallPopWindow(customerResource, channel);
			} else {
				if(currentTab.getClass() == MyMarketingTaskTabView.class) {					// 在我的营销任务Tab 页中点击的呼叫
					((MyMarketingTaskTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == QuestionnaireTaskTabView.class) {		// 在我的问卷任务Tab 页中点击的呼叫
					((QuestionnaireTaskTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == MyServiceRecordTabView.class) {			// 在我的客服记录Tab 页中点击的呼叫
					((MyServiceRecordTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == MyHistoryServiceRecordTabView.class) {	// 在我的历史客服记录Tab 页中点击的呼叫
					((MyHistoryServiceRecordTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == CdrTabView.class) {						// 在我的呼叫记录Tab 页中点击的呼叫
					((CdrTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == ProprietaryCustomersTabView.class) {		// 在我的专有客户中 中点击的呼叫
					((ProprietaryCustomersTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == MyResourcesTabView.class) {				// 在我的资源中 中点击的呼叫
					((MyResourcesTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else if(currentTab.getClass() == MyHistoryOrderTabView.class) {			// 在历史订单中 中点击的呼叫
					((MyHistoryOrderTabView) currentTab).showSystemCallPopWindow(customerResource, channel);
				} else {					// 如果不是以上几个特殊模块，则显示系统状态栏中的呼出弹窗
					csrStatusBar.showSoftPhoneCallPopWindow(customerResource, channel);
				} 
			}
		}
		
		// jrh  2013-12-17 弹屏就置忙队列成员      ---------------  开始
		ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domainId);
		if(domainConfigs != null) {
			Boolean ispauseExtenPopupWindow = domainConfigs.get(PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW);
			if(ispauseExtenPopupWindow != null && ispauseExtenPopupWindow) {
				Boolean iscreateCallAfterLog = domainConfigs.get(CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW);
				iscreateCallAfterLog = (iscreateCallAfterLog == null) ? true : iscreateCallAfterLog;
				
				csrStatusBar.executeOutAfterCallHandle(iscreateCallAfterLog);
			}
		}	//	------------- 结束
		
	}

}
