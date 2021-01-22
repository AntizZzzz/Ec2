package com.jiangyifen.ec2.service.csr;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface ManagerSendNoticeToCsrService extends FlipSupportService<Notice> {
	
	/**
	 * 创建一个通知，并将其保存到数据库中，同时在内部创建并保存NoticeItem对象
	 * 其中NoticeItem 用于保存一条通知Notice 与被通知对象 User 的对应关系
	 * 
	 * @param notice 通知
	 * @param notice 被通知的用户
	 */
	@Transactional
	public void save(Notice notice, Collection<User> users);
	
	/**
	 * 刷新值指定用户的通知界面
	 * @param users 接收到新信息的用户
	 */
	@Transactional
	public void refreshCsrUi(Collection<User> users);
	
	/**
	 * 获取指定用户的所有通知
	 * @param user	用户对象
	 * @return List 用户的所有通知
	 */
	@Transactional
	public List<Notice> getNoticesByUser(User user);

}
