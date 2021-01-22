package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.NoticeItemEao;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.NoticeItemService;

public class NoticeItemServiceImpl implements NoticeItemService {

	private NoticeItemEao noticeItemEao;
	
	// enhanced method
	
	@Override
	public List<NoticeItem> getByUser(User user) {
		return noticeItemEao.getNoticeItemsByUser(user);
	}

	@Override
	public NoticeItem getByUserAndNotice(User user, Notice notice) {
		return noticeItemEao.getByUserAndNotice(user, notice);
	}
	
	
	// common method 

	@Override
	public NoticeItem get(Object primaryKey) {
		return noticeItemEao.get(NoticeItem.class, primaryKey);
	}

	@Override
	public void save(NoticeItem noticeItem) {
		noticeItemEao.save(noticeItem);
	}

	@Override
	public void update(NoticeItem noticeItem) {
		noticeItemEao.update(noticeItem);
	}

	@Override
	public void delete(NoticeItem noticeItem) {
		noticeItemEao.delete(noticeItem);
	}

	@Override
	public void deleteById(Object primaryKey) {
		noticeItemEao.delete(NoticeItem.class, primaryKey);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<NoticeItem> loadPageEntities(int startIndex, int pageRecords,
			String selectSql) {
		return noticeItemEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}
	
	@Override
	public int getEntityCount(String countSql) {
		return noticeItemEao.getEntityCount(countSql);
	}

	// getter setter

	public NoticeItemEao getNoticeItemEao() {
		return noticeItemEao;
	}

	public void setNoticeItemEao(NoticeItemEao noticeItemEao) {
		this.noticeItemEao = noticeItemEao;
	}

}
