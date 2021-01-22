package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.NoticeEao;
import com.jiangyifen.ec2.eao.NoticeItemEao;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.service.eaoservice.NoticeService;

public class NoticeServiceImpl implements NoticeService {
	
	private NoticeEao noticeEao;
	private NoticeItemEao noticeItemEao;
	
	// enhanced method

	// common method
	
	@Override
	public Notice get(Object primaryKey) {
		return noticeEao.get(Notice.class, primaryKey);
	}

	@Override
	public void save(Notice notice) {
		noticeEao.save(notice);
	}

	@Override
	public Notice update(Notice notice) {
		return (Notice) noticeEao.update(notice);
	}

	@Override
	public void delete(Notice notice) {
		noticeEao.delete(notice);
	}

	@Override
	public void deleteById(Object primaryKey) {
		noticeItemEao.deleteByNoticeId(primaryKey);
		noticeEao.delete(Notice.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Notice> loadPageEntities(int start, int length, String sql) {
		return noticeEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return noticeEao.getEntityCount(sql);
	}

	public NoticeEao getNoticeEao() {
		return noticeEao;
	}

	public void setNoticeEao(NoticeEao noticeEao) {
		this.noticeEao = noticeEao;
	}

	public NoticeItemEao getNoticeItemEao() {
		return noticeItemEao;
	}

	public void setNoticeItemEao(NoticeItemEao noticeItemEao) {
		this.noticeItemEao = noticeItemEao;
	}

}
