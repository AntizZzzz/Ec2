package com.jiangyifen.ec2.email.eao;

import java.util.List;

import com.jiangyifen.ec2.eao.BaseEao;
import com.jiangyifen.ec2.email.entity.MailContact;

public interface MailContactEao extends BaseEao {
	
	public List<MailContact> getMailContactsByUserId(Long userId);
}
