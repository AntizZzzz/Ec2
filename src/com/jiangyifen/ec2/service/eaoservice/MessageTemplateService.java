package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MessageTemplateService extends FlipSupportService<MessageTemplate> {
	
	@Transactional
	public void save(MessageTemplate message);
	
	@Transactional
	public void delete(MessageTemplate message);
	
	@Transactional
	public MessageTemplate update(MessageTemplate message);
	
	@Transactional
	public List<MessageTemplate> getMessagesByType(MessageTemplateType type);
	
	@Transactional
	public List<MessageTemplate> getMessagesByCreator(User user);
	
	@Transactional
	public List<MessageTemplate> getAllByDomain(Domain domain);
	
	/**
	 * jrh
	 * 	更加JPQL 语句获取所有信息模板
	 * @param searchSql
	 * @return
	 */
	@Transactional
	public List<MessageTemplate> getAllByJpql(String searchSql);

	/**
	 * jrh 获取指定域下的所有指定 模板类型的短信模板集合
	 * 
	 * @param templateType	模板类型
	 * @param domainId		所属域的编号
	 * @return List<MessageTemplate>
	 */
	@Transactional
	public List<MessageTemplate> getAllByType(MessageTemplateType templateType, Long domainId);
	
}
