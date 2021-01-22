package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.NoticeItemEao;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;


public class NoticeItemEaoImpl extends BaseEaoImpl implements NoticeItemEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<NoticeItem> getNoticeItemsByUser(User user) {
		return getEntityManager().createQuery("select ni from NoticeItem as ni where ni.user.id = " + user.getId() + " order by ni.notice.sendDate desc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public NoticeItem getByUserAndNotice(User user, Notice notice) {
		List<NoticeItem> noticeItems = getEntityManager().createQuery("select ni from NoticeItem as ni where ni.user.id = " + user.getId() + " and ni.notice.id = " + notice.getId()).getResultList();
		if(noticeItems.size() > 0) {
			return noticeItems.get(0);
		}
		return null;
	}

	@Override
	public void deleteByNoticeId(Object primaryKey) {
		getEntityManager().createQuery("delete from NoticeItem ni where ni.notice.id = " +primaryKey).executeUpdate();
	}

}
