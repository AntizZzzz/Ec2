package com.jiangyifen.ec2.email.service.impl;

import com.jiangyifen.ec2.email.eao.MailConfigEao;
import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.service.MailConfigService;
import com.jiangyifen.ec2.entity.User;

public class MailConfigServiceImpl implements MailConfigService {

	private MailConfigEao mailConfigEao;
	
	@Override
	public MailConfig get(Object primaryKey) {
		return mailConfigEao.get(MailConfig.class, primaryKey);
	}

	@Override
	public void save(MailConfig mailConfig) {
		mailConfigEao.save(mailConfig);
	}

	@Override
	public void update(MailConfig mailConfig) {
		mailConfigEao.update(mailConfig);
	}

	@Override
	public void delete(MailConfig mailConfig) {
		mailConfigEao.delete(mailConfig);
	}

	@Override
	public void deleteById(Object primaryKey) {
		mailConfigEao.delete(MailConfig.class, primaryKey);
	}

	@Override
	public MailConfig getDefaultMailConfig(Long domainId) {
		return mailConfigEao.getDefaultMailConfig(domainId);
	}

	@Override
	public MailConfig getMailConfigByUser(User user) {
		return mailConfigEao.getMailConfigByUser(user);
	}

	public MailConfigEao getMailConfigEao() {
		return mailConfigEao;
	}

	public void setMailConfigEao(MailConfigEao mailConfigEao) {
		this.mailConfigEao = mailConfigEao;
	}
	
}
