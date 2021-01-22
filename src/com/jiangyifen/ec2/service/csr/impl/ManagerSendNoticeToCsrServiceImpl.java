package com.jiangyifen.ec2.service.csr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jiangyifen.ec2.eao.NoticeEao;
import com.jiangyifen.ec2.eao.NoticeItemEao;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ManagerSendNoticeToCsrService;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;

public class ManagerSendNoticeToCsrServiceImpl implements ManagerSendNoticeToCsrService {

	private NoticeEao noticeEao;
	
	private NoticeItemEao  noticeItemEao;
	
	@Override
	public void save(Notice notice, Collection<User> users) {
		noticeEao.save(notice);
		
		for(User user : users) {
			NoticeItem noticeItem = new NoticeItem();
			noticeItem.setNotice(notice);
			noticeItem.setUser(user);
			noticeItemEao.save(noticeItem);
		}
	}

	@Override
	public void refreshCsrUi(Collection<User> users) {
		for(User user: users) {
			CsrToolBar toolBar = (CsrToolBar)ShareData.csrToToolBar.get(user.getId());
			if(toolBar != null) {	// 如果接收消息的用户在线，则直接刷新用户的界面
				toolBar.updateNewNotice();
			}
		}
	}

	@Override
	public List<Notice> getNoticesByUser(User user) {
		List<Notice> notices = new ArrayList<Notice>();
		
		List<NoticeItem> noticeItemList = noticeItemEao.getNoticeItemsByUser(user);
		for(NoticeItem noticeItem : noticeItemList) {
			notices.add(noticeItem.getNotice());
		}
		return notices;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Notice> loadPageEntities(int startIndex, int pageRecords, String selectSql) {
		return noticeEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}

	@Override
	public int getEntityCount(String countSql) {
		return noticeItemEao.getEntityCount(countSql);
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
