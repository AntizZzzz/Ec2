package com.jiangyifen.ec2.service.csr;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.User;

public interface PopupIncomingWindowService {
	
	/**
	 * 根据呼入者的信息，和被叫客服对象，进行弹屏操作
	 * 	如果呼入客户在数据库中已经存在，则从数据库中取出其对应资料，并在弹屏中显示出来
	 *  如果呼入客户在数据库中不存在，则创建一个新的资源（并没有直接保存到数据库），然后显示信息到弹屏中
	 * @param csr
	 * @param ringingExtenChannelSession 被叫分机振铃时的通道信息
	 */
	@Transactional
	public void popupIncomingWindow(User csr, ChannelSession ringingExtenChannelSession);

}
