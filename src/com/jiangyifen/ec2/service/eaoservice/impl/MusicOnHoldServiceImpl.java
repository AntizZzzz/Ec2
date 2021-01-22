package com.jiangyifen.ec2.service.eaoservice.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.MusicOnHoldEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.service.eaoservice.MusicOnHoldService;
import com.jiangyifen.ec2.utils.Config;

public class MusicOnHoldServiceImpl implements MusicOnHoldService {
	// 日志创建工具
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private MusicOnHoldEao musicOnHoldEao;

	// enhanced method
	
	@Override
	public List<MusicOnHold> getAllByDomain(Domain domain) {
		return musicOnHoldEao.getAllByDomain(domain);
	}
	
	@Override
	public boolean existByDirectory(String directory, Domain domain) {
		return musicOnHoldEao.existByDirectory(directory, domain);
	}
	
	@Override
	public boolean deleteVoiceFolder(String folder, Domain domain) {
		File folderFile = new File(Config.props.getProperty(Config.CUSTOMER_MOH_PATH) +domain.getName() +"/"+ folder);
		if(folderFile.exists()) {
			File[] files = folderFile.listFiles();
			for(File file : files) {
				file.delete();
			}
			folderFile.delete();
		}
		
		return !folderFile.exists();
	}

	@Override
	public boolean deleteVoiceFile(String folder, String filename, Domain domain) {
		String path = Config.props.getProperty(Config.CUSTOMER_MOH_PATH) +domain.getName() +"/"+ folder +"/"+ filename;
		File file = new File(path);
		if(file.exists()) {
			file.delete();
		}
		return !file.exists();
	}
	
	@Override
	public List<String> getVoiceFileNames(String folder, Domain domain) {
		String srcPath = Config.props.getProperty(Config.CUSTOMER_MOH_PATH) +domain.getName()+ "/" +folder;
		if("moh".equals(folder)) {
			srcPath = Config.props.getProperty(Config.DEFAULT_MOH_PATH);
		}
		
		File dir = new File(srcPath);
		File[] files = dir.listFiles();
		List<String> filenames = new ArrayList<String>();
		if(files != null) {
			for(File file : files) {
				if(!file.isDirectory() && !file.isHidden()) {
					filenames.add(file.getName());
				}
			}
		}
		Collections.sort(filenames);
		return filenames;
	}
	
	
	@Override
	public boolean updateAsteriskMusicOnHoldFile(Domain domain) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"musiconhold_domain_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"musiconhold_domain_"+  domain.getName();
		List<MusicOnHold> mohs = musicOnHoldEao.getAllByDomain(domain);
		boolean isSuccess = createFileContent(srcFileName, dstFileName, mohs);
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，并根据获取的MusicOnHold的集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param mohs			队列集合
	 * @return boolean		创建是否成功
	 */
	private boolean createFileContent(String srcFileName, String dstFileName,
			List<MusicOnHold> mohs) {
		File source = new File(srcFileName+ ".conf");
		File dst = new File(dstFileName+ ".conf.backup");
		
		// 删除原有的备份文件
		if(dst.exists()) {
			dst.delete();
		}
		
		// 创建新的备份文件
		source.renameTo(dst);
		
		// 创写新的sip.conf 文件
		boolean isSuccess = false;
		FileOutputStream fos = null;
		BufferedOutputStream bw = null;
		try {
			fos = new FileOutputStream(source);
			bw = new BufferedOutputStream(fos);
			StringBuffer mohcontent = new StringBuffer();

			mohcontent.append("\r\n\r\n\r\n");
			mohcontent.append(";;;;;;;;;;;;;;; custom musiconhold.conf ;;;;;;;;;;;;;;\r\n");
			mohcontent.append("\r\n\r\n\r\n");
			
			for(MusicOnHold moh : mohs) {
				if("default".equals(moh.getName())) continue;
				
				String directoryPath = Config.props.getProperty(Config.CUSTOMER_MOH_PATH)+moh.getDomain().getName() +"/"+ moh.getDirectory();
				mohcontent.append("[" +moh.getName()+ "]"+ "\r\n");
				mohcontent.append("mode=" +moh.getMode()+ "\r\n");
				mohcontent.append("directory=" +directoryPath+ "\r\n");
				mohcontent.append("\r\n\r\n");
				
				// 检查存放语音文件的文件夹是否已经存在，如果不存在，则创建它
				File voiceFolder = new File(directoryPath);
				if(!voiceFolder.exists()) {
					voiceFolder.mkdirs();
				}
			}
			
			String mohcontentStr = mohcontent.toString();
			bw.write(mohcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage() +"修改Asterisk 语音文件夹配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage()+"修改Asterisk 语音文件夹配置文件关闭流时，出现异常", e);
				e.printStackTrace();
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	
	// common method
	
	@Override
	public MusicOnHold get(Object primaryKey) {
		return musicOnHoldEao.get(MusicOnHold.class, primaryKey);
	}

	@Override
	public synchronized void save(MusicOnHold musicOnHold) {
		if(musicOnHold.getId() == null) {
			// 新建的分机需要自动设置语音文件夹名
			Long maxMohName = musicOnHoldEao.getMaxMusicOnHoldName() + 1L;
			musicOnHold.setName(maxMohName.toString());
			musicOnHold.setDirectory(maxMohName.toString());
		}
		musicOnHoldEao.save(musicOnHold);
	}

	@Override
	public synchronized MusicOnHold update(MusicOnHold musicOnHold) {
		if(musicOnHold.getId() == null) {
			// 新建的分机需要自动设置语音文件夹名,及文件夹路径的部分信息
			Long maxMohName = musicOnHoldEao.getMaxMusicOnHoldName() + 1L;
			musicOnHold.setName(maxMohName.toString());
			musicOnHold.setDirectory(maxMohName.toString());
		}
		return musicOnHoldEao.update(musicOnHold);
	}

	@Override
	public void delete(MusicOnHold musicOnHold) {
		musicOnHoldEao.delete(musicOnHold);
	}

	@Override
	public void deleteById(Object primaryKey) {
		musicOnHoldEao.delete(MusicOnHold.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MusicOnHold> loadPageEntities(int start, int length, String sql) {
		return musicOnHoldEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return musicOnHoldEao.getEntityCount(sql);
	}

	public MusicOnHoldEao getMusicOnHoldEao() {
		return musicOnHoldEao;
	}

	public void setMusicOnHoldEao(MusicOnHoldEao musicOnHoldEao) {
		this.musicOnHoldEao = musicOnHoldEao;
	}


}
