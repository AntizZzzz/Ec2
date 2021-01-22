package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface NoticeItemService extends FlipSupportService<NoticeItem> {
	
	// enhanced method
	
	/**
	 * 获取指定用户的所有通知项
	 * @param user 用户对象
	 * @return List 存放Item的集合
	 */
	public List<NoticeItem> getByUser(User user);
	
	/**
	 * 获取指定用户的某一通知对应的Item项
	 * @param user	通知的拥有者
	 * @param notice	通知
	 * @return NoticeItem 具体通知项
	 */
	public NoticeItem getByUserAndNotice(User user, Notice notice);

	// common method 
	
	@Transactional
	public NoticeItem get(Object primaryKey);
	
	@Transactional
	public void save(NoticeItem noticeItem);

	@Transactional
	public void update(NoticeItem noticeItem);

	@Transactional
	public void delete(NoticeItem noticeItem);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
//	@Transactional
//	public List<NoticeItem> loadPageEntities(int start,int length,String sql);
//
//	@Transactional
//	public int getEntityCount(String sql);
}
