package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MusicOnHoldService extends FlipSupportService<MusicOnHold> {
	
	// enhanced method

	/**
	 * jrh
	 * 获取指定域中的所有MusicOnHold 对象
	 * @param domain 指定的域
	 * @return
	 */
	@Transactional
	public List<MusicOnHold> getAllByDomain(Domain domain);

	/**
	 * jrh
	 * 在指定的域范围中，检查是否已经存在文件夹名称为 directory 的MusicOnHold 对象，如果存在返回true，否则返回false
	 * @param directory MusicOnHold 的文件夹名称
	 * @param domain 指定的域范围
	 * @return
	 */
	@Transactional
	public boolean existByDirectory(String directory, Domain domain);
	/**
	 * jrh
	 * 删除指定域在Asterisk 配置文件中指定语音文件夹, 并删除文件夹下的所有文件
	 * @param folder	文件夹名称
	 * @param domain	文件所属域
	 */
	@Transactional
	public boolean deleteVoiceFolder(String folder, Domain domain);
	
	/**
	 * jrh
	 * 删除指定域在Asterisk 配置文件中指定语音文件夹下的语音文件名为filename 的语音
	 * @param folder	文件夹名称
	 * @param filename	语音文件名称
	 * @param domain	文件所属域
	 */
	@Transactional
	public boolean deleteVoiceFile(String folder, String filename, Domain domain);
	
	/**
	 * jrh
	 * 获取指定域在Asterisk 配置文件中指定语音文件夹下的所有文件
	 * @param folder	文件夹名称
	 * @param domain	文件所属域
	 * @return
	 */
	@Transactional
	public List<String> getVoiceFileNames(String folder, Domain domain);
	
	
	/**
	 * jrh
	 * 将数据库中的MusicOnHold 对象信息更新至Asterisk 的配置文件中去，
	 * 	并且查看MusicOnHold 对应的文件夹在指定目录下是否存在，如果不存在，则创建文件夹
	 * @param domain 指定的域
	 * @return
	 */
	@Transactional
	public boolean updateAsteriskMusicOnHoldFile(Domain domain);
	
	// common method 
	
	@Transactional
	public MusicOnHold get(Object primaryKey);
	
	/**
	 * 保存新建的语音文件夹，并自动为其设置文件夹名称
	 * @param queue
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public void save(MusicOnHold musicOnHold);

	/**
	 * 更新或保存新建的语音文件夹，如果是新建的，则自动为其设置文件夹名称
	 * @param MusicOnHold
	 * @return
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public MusicOnHold update(MusicOnHold musicOnHold);

	@Transactional
	public void delete(MusicOnHold musicOnHold);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
