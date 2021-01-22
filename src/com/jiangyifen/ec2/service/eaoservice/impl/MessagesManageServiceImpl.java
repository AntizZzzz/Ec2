package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.eaoservice.MessagesManageService;
import com.jiangyifen.ec2.utils.LoggerUtil;

/**
 * MessagesManage的实现类
 * 
 * @author zhangm
 *
 */
public class MessagesManageServiceImpl implements MessagesManageService{
	@Autowired
	private CommonEao commonEao;
//====================  查找数据 ====================================//
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<Message> getMessages(User user, Date start, Date end) {
		String addStart = "";
		if(start != null){
			addStart  = " and m.time > {ts '"+start+ "'}";
		}
		String addEnd = "";
		if(end != null){
			addEnd = " and m.time < {ts '" + end +"'}";
		}
		String addUser = "";
		if(user != null){
			addUser = " and m.user.id="+user.getId();
		}
		/*StringBuffer sb = new StringBuffer("Select m from Message as m where 1=1 ");*/
		StringBuffer sb = new StringBuffer("Select m from Message as m ");
		sb.append(addUser);
		sb.append(addStart);
		sb.append(addEnd);
		sb.append(" order by m.time desc");
		String jpql = sb.toString().replaceFirst("and", "where");
		return (List<Message>) commonEao.excuteSql(jpql, ExecuteType.RESULT_LIST);
	}

	@Override
	@Transactional(readOnly=true)
	public Message getMessageById(Long messageId) {
		return (Message) commonEao.get(Message.class, messageId);
	}
	
//===================== 修改数据 ===================================//
	@Override
	@Transactional
	public void saveMessage(Message message) {
		LoggerUtil.logInfo(MessagesManageServiceImpl.class, "zhangm: saving Message: " + message.getId());
		commonEao.save(message);
	}

	@Override
	@Transactional
	public Message updateMessage(Message message) {
		return (Message)commonEao.update(message);
	}

	@Override
	@Transactional
	public boolean deleteMessage(Message message) {
		if(message == null){
			return false;
		}
		commonEao.delete(Message.class, message.getId());
		LoggerUtil.logInfo(MessagesManageServiceImpl.class, "zhangm: deleting Message: " + message.getId() );
		return true;
	}

	
	//=======================  翻页   ========================//
	@SuppressWarnings("unchecked")
	@Override
	public List<Message> loadPageEntities(int start, int length, String sql) {
		return commonEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return commonEao.getEntityCount(sql);
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

	
	
}
