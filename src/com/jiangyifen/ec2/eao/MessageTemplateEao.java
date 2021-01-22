package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;

public interface MessageTemplateEao extends BaseEao{
	
	public List<MessageTemplate> getMessagesByType(MessageTemplateType type);
	
	public List<MessageTemplate> getMessagesByCreator(User user);
	
	public List<MessageTemplate> getAllByDomain(Domain domain);

	/**
	 * jrh
	 * 	更加JPQL 语句获取所有信息模板
	 * @param searchSql
	 * @return
	 */
	public List<MessageTemplate> getAllByJpql(String searchSql);

	/**
	 * jrh 获取指定域下的所有指定 模板类型的短信模板集合
	 * 
	 * @param templateType	模板类型
	 * @param domainId		所属域的编号
	 * @return List<MessageTemplate>
	 */
	public List<MessageTemplate> getAllByType(MessageTemplateType templateType, Long domainId);
	
}
