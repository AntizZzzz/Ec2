package com.jiangyifen.ec2.ui.mgr.sysdisk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 磁盘使用信息
 * 必须先执行excuteSheel，再获取信息
 * @author lxy
 * 
 */
public class DiskUseInfoUtil {

	private static final Logger logger = LoggerFactory.getLogger(DiskUseInfoUtil.class);

	private static DecimalFormat df = new DecimalFormat("#.00");

	private long totalVal = -1L;
	private long useVal = -1L;
	private long freeVal = -1L;

	/**
	 * 执行回去脚本 获取最新数据，执行该方法，可以多次执行
	 */
	public void excuteSheel() {
		if(System.getProperty("os.name").equals("Linux")){//是linux下才执行
			try {
				boolean first = true;
	
				Process p = Runtime.getRuntime().exec("df -k");
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = br.readLine();
	
				List<Long> total = new ArrayList<Long>();
				List<Long> use = new ArrayList<Long>();
	
				while (line != null) {
					if (first) {
						first = false;
					} else {
						while (line.indexOf("  ") != -1) {
							line = line.replace("  ", " ");
						}
						if (!"tmpfs".equals(line.split(" ")[0]) && line.split(" ").length > 1) {
							total.add(getLongValue(line.split(" ")[1]));
							use.add(getLongValue(line.split(" ")[2]));
						}
					}
					line = br.readLine();
				}
	
				if (total.size() == use.size()) {
					for (int i = 0; i < total.size(); i++) {
						totalVal += total.get(i);
						useVal += use.get(i);
					}
					if (useVal < totalVal) {
						this.freeVal = totalVal - useVal;
					} else {
						this.freeVal = -1L;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取录音目录下的所有一级目录信息，包括大小
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SoundRecording> getSoundRecordings() {
		String recLocalDiskPath = Config.props.getProperty("rec_local_disk_path");
		File memPath = new File(recLocalDiskPath);
		File[] files = memPath.listFiles();
		List<SoundRecording> srls = new ArrayList<SoundRecording>();

		SoundRecording sr = null;
		Long fileSize = 0L;
		if(files != null){
			for (File file : files) {
				if (file.isDirectory()) {
					fileSize = getFileSize(file.getAbsolutePath());
				}
				sr = new SoundRecording(file.getAbsolutePath(), file.getName(), formetFileSizeBKMG(fileSize));
				srls.add(sr);
			}
	
			Collections.sort(srls, new SortByName());
		}
		return srls;
	}

	@SuppressWarnings("rawtypes")
	class SortByName implements Comparator {
		public int compare(Object o1, Object o2) {
			SoundRecording s1 = (SoundRecording) o1;
			SoundRecording s2 = (SoundRecording) o2;
			return s1.getName().compareTo(s2.getName());
		}
	}

	/**
	 * 获取long值方法 string->long
	 * 
	 * @param v
	 * @return
	 */
	private Long getLongValue(String v) {
		Long rv = 0L;
		try {
			if (v != null && v.trim() != null && v.trim().length() > 0) {
				rv = Long.valueOf(v.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

	/**
	 * 获取百分比字符串 两位小数点 10.00
	 * 
	 * @return
	 */
	public String getPercent() {
		if (totalVal > 0) {
			int r = (int) (getPercentPoint() * 100);
			return r + "%";
		} else {
			return "--";
		}
	}

	/**
	 * 获取百分比小数点值
	 * 
	 * @return
	 */
	public double getPercentPoint() {
		double rt = -1.0;
		if (totalVal > 0) {
			rt = useVal / Double.valueOf(totalVal);
		}
		return rt;
	}

	/**
	 * 获取总空间大小
	 * 
	 * @return
	 */
	public long getTotalVal() {
		return totalVal;
	}

	/**
	 * 获取使用空间大小
	 * 
	 * @return
	 */
	public long getUseVal() {
		return useVal;
	}

	/**
	 * 获取剩余空间大小
	 * 
	 * @return
	 */
	public long getFreeVal() {
		return freeVal;
	}

	/**
	 * 获取总空间大小
	 * 
	 * @return
	 */
	public String getTotalValToShow() {
		return formetFileSize(totalVal);
	}

	/**
	 * 获取使用空间大小
	 * 
	 * @return
	 */
	public String getUseValToShow() {
		return formetFileSize(useVal);
	}

	/**
	 * 获取剩余空间大小
	 * 
	 * @return
	 */
	public String getFreeValToShow() {
		return formetFileSize(freeVal);
	}

	/**
	 * 
	 * 转换前的值为k单位 转换文件的大小以K,M,G 计算以1000为一个进制单位
	 * 如果超过G，以G为单位
	 * @param fileSize
	 * @return
	 */
	public static String formetFileSize(long fileSize) {// 转换文件大小
		String fileSizeString = "";
		if (fileSize < 1024) {
			fileSizeString = df.format((double) fileSize) + "k";
		} else if (fileSize < 1048576) {
			fileSizeString = df.format((double) fileSize / 1024) + "M";
		} else if (fileSize < 1073741824) {
			fileSizeString = df.format((double) fileSize / 1048576) + "G";
		} else {
			fileSizeString = df.format((double) fileSize / 1048576) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取文件或者循环变量文件件次的文件的文件大小
	 * 
	 * @param strName
	 * @return
	 */
	public static Long getFileSize(String strName) {
		Long totalSize = 0L;
		File f = new File(strName);
		if (f.isFile())
			return f.length();
		else {
			if (f.isDirectory()) {
				File[] contents = f.listFiles();
				for (int i = 0; i < contents.length; i++) {
					if (contents[i].isFile())
						totalSize += contents[i].length();
					else {
						if (contents[i].isDirectory())
							totalSize += getFileSize(contents[i].getPath());
					}
				}
			}
		}
		return totalSize;
	}

	/**
	 * 转换前的值为b单位 转换文件的大小以K,M,G 计算以1024为一个进制单位
	 */
	public static String formetFileSizeBKMG(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 此方法危险，请勿轻易使用
	 * 
	 * @param soundRecording
	 */
	public void removeSoundRecording(SoundRecording soundRecording, Domain domain, User user) {
		String cmd = "rm -rf " + soundRecording.getAbsolutePath();
		try {
			if (user != null) {
				logger.info("LLXXYY removeSoundRecording commond " + cmd);
				saveLog(domain, cmd, user, soundRecording);
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			logger.error("LLXXYY removeSoundRecording ", e);
		}
	}

	private void saveLog(Domain domain, String cmd, User user, SoundRecording soundRecording) {
		try {
			CommonService commonService = SpringContextHolder.getBean("commonService");
			saveDownloadOperationLog(domain, cmd, user, "", cmd, soundRecording.toString(), commonService);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 保存下载日志
	 * 
	 * @param domain
	 *            域名
	 * @param filePath
	 *            下载路径
	 * @param user
	 *            登录用户
	 * @param operateIp
	 *            操作者IP
	 * @param description
	 *            描述
	 * @param programmerSee
	 *            sql语句
	 * @param commonService
	 *            service
	 */
	private void saveDownloadOperationLog(Domain domain, String filePath, User user, String operateIp, String description, String programmerSee, CommonService commonService) {
		try {
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			operationLog.setDescription(description);
			operationLog.setProgrammerSee(programmerSee);
			commonService.save(operationLog);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "DiskUseInfo [totalVal=" + totalVal + ", useVal=" + useVal + ", freeVal=" + freeVal + ", perent=" + getPercentPoint() + "]";
	}

}
