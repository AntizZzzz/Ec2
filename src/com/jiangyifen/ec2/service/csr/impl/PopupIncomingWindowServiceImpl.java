package com.jiangyifen.ec2.service.csr.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.PopupIncomingWindowService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.ui.csr.workarea.incoming.IncomingDialTabView;
import com.jiangyifen.ec2.ui.csr.workarea.incoming.IncomingDialWindow;

/**
 * 呼入弹屏服务类
 * 
 * @author jrh
 *
 */
public class PopupIncomingWindowServiceImpl implements PopupIncomingWindowService {
	
	private CustomerResourceService customerResourceService;
	
	@Override
	public void popupIncomingWindow(User csr, ChannelSession ringingExtenChannelSession) {
		// 主叫号码
		String connectedlinenum = ringingExtenChannelSession.getConnectedlinenum(); 

		// 检查该客服的弹屏窗口是否已经建立，如果没有，则新建
		IncomingDialWindow incomingDialWindow = ShareData.csrToIncomingDialWindow.get(csr.getId());
		if(incomingDialWindow == null) {
			incomingDialWindow = new IncomingDialWindow(csr);
		}
		
		// 如果当前connectedlinenum 已经有弹出的窗口，则直接选中并更对应Tab下的通道信息即可，不再重新弹屏
		IncomingDialTabView tabView = incomingDialWindow.getCallerNumToTabView().get(connectedlinenum);
		if(tabView != null) {
			// 更新呼入弹屏的Tab 页中的被叫分机振铃时的ChannelSession
			tabView.setRingingExtenChannelSession(ringingExtenChannelSession);
			incomingDialWindow.getTabSheet().setSelectedTab(tabView);
			return;
		} 
		
		// 按呼入的来电显示号码进行查询资源
		Long domainId = csr.getDomain().getId();
		CustomerResource customerResource = customerResourceService.getCustomerResourceByPhoneNumber(connectedlinenum, domainId);
		
		// 检查客户资源是否已经存在，不存在，则新建客户资源和电话号码对象
		if(customerResource == null) {
			Telephone telephone = new Telephone();
			telephone.setNumber(connectedlinenum);
			telephone.setDomain(csr.getDomain());

			Set<Telephone> telephones = new HashSet<Telephone>();
			telephones.add(telephone);

			customerResource = new CustomerResource();
			customerResource.setDomain(csr.getDomain());
			customerResource.setImportDate(new Date());
			customerResource.setTelephones(telephones);
			Calendar cal = Calendar.getInstance();
			cal.set(1970, 0, 1, 0, 0, 0);
			customerResource.setCount(1);
			customerResource.setLastDialDate(new Date());
			customerResource.setExpireDate(cal.getTime());
			telephone.setCustomerResource(customerResource); 						// JRH 如果系统不存在，则自动存储 20141118
			customerResource = customerResourceService.update(customerResource);	// JRH 如果系统不存在，则自动存储 20141118
		} else {
			// 更新客户信息
			int dialCount = customerResource.getCount() + 1;
			customerResource.setCount(dialCount);
			customerResource.setLastDialDate(new Date());
			customerResource = customerResourceService.update(customerResource);
		}
		
		// 调用组件的弹屏方法
		incomingDialWindow.showIncomingDialWindow(customerResource, ringingExtenChannelSession);
	}

	public CustomerResourceService getCustomerResourceService() {
		return customerResourceService;
	}

	public void setCustomerResourceService(CustomerResourceService customerResourceService) {
		this.customerResourceService = customerResourceService;
	}

}
