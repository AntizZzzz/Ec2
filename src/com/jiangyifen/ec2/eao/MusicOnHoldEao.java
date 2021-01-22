package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;



public interface MusicOnHoldEao extends BaseEao {
	
	
	/**
	 * jrh
	 * 重写 BaseEao 中的保存实体方法
	 * @param entity	 需要保存的实体
	 */
	@Transactional
	public void save(MusicOnHold musicOnHold);
	
	/**
	 * jrh
	 * 重写 BaseEao 中的更新实体方法
	 * @param entity	 需要更新的实体
	 */
	@Transactional
	public MusicOnHold update(MusicOnHold musicOnHold);

	/**
	 * jrh
	 * 在指定的域范围中，检查是否已经存在文件夹名称为 directory 的MusicOnHold 对象，如果存在返回true，否则返回false
	 * @param directory MusicOnHold 的文件夹名称
	 * @param domain 指定的域范围
	 * @return
	 */
	public boolean existByDirectory(String directory, Domain domain);

	/**
	 * jrh
	 * 获取指定域中的所有MusicOnHold 对象
	 * @param domain 指定的域
	 * @return
	 */
	public List<MusicOnHold> getAllByDomain(Domain domain);
	

	/**
	 * jrh
	 *	 获取系统中配置名称(name)不是'default'的语音文件夹中，当前最大的语音文件夹名称，并将String 转化成Long 类型返回
	 */
	public Long getMaxMusicOnHoldName();
	
}
