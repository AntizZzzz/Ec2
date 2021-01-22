package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;

public interface NoticeItemEao extends BaseEao {

	/**
	 * 获取指定用户的所有通知项
	 * @param user 用户对象
	 * @return List 存放Item的集合
	 */
	public List<NoticeItem> getNoticeItemsByUser(User user);
	
	/**
	 * 获取指定用户的某一通知对应的Item项
	 * @param user	通知的拥有者
	 * @param notice	通知
	 * @return NoticeItem 具体通知项
	 */
	public NoticeItem getByUserAndNotice(User user, Notice notice);
	
	/**
	 * chb
	 * 通过Notice的Id删除所有引用此Notice的NoticeItem
	 * @param primaryKey
	 */
	public void deleteByNoticeId(Object primaryKey);

}
