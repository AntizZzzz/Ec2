package com.jiangyifen.ec2.email.eao;

import com.jiangyifen.ec2.eao.BaseEao;
import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.entity.User;

public interface MailConfigEao extends BaseEao {
	
	public MailConfig getDefaultMailConfig(Long domainId);
	
	public MailConfig getMailConfigByUser(User user);
	
}
