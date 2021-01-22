package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeyword;


public interface TableKeywordEao extends BaseEao {
	/**
	 * 取得域内所有Table可以使用的关键字
	 * @param domain
	 * @return
	 */
	public List<String> getAllStrByDomain(Domain domain);
	/**
	 * 取得域内所有Table可以使用的关键字对象
	 * @param domain
	 * @return
	 */
	public List<TableKeyword> getAllByDomain(Domain domain);
}
