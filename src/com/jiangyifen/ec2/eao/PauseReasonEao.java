package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PauseReason;

public interface PauseReasonEao extends BaseEao {
	
	/**
	 * 根据指定域获取指定域的置忙原因
	 * @param domain  域
	 * @param enabled  可用状态
	 * @return List<PauseReason>
	 */
	public List<PauseReason> getAllByEnabled(Domain domain, boolean enabled);
	
}
