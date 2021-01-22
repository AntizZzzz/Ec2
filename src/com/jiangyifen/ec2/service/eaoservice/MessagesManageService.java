package com.jiangyifen.ec2.service.eaoservice;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * 短信查看
 */
public interface MessagesManageService extends FlipSupportService<Message>{
	
	/**
	 * 获取短信
	 * 
	 * @param user 用户名
	 * @param start 起始时间
	 * @param end 截止时间
	 * @return 消息集合
	 */
	public List<Message> getMessages(User user, Date start, Date end);
	
	/**
	 * 通过短信ID来获取短信
	 * 
	 * @param messageId 短信ID
	 * @return
	 */
	public Message getMessageById(Long messageId);
	
	/**
	 * 保存短信
	 * 
	 * @param message 短信对象
	 */
	public void saveMessage(Message message);
	
	/**
	 * 更新短信
	 * 
	 * @param message 短信对象
	 */
	public Message updateMessage(Message message);
	
	/**
	 * 删除短信
	 * 
	 * @param message 短信对象
	 */
	public boolean deleteMessage(Message message);
}
