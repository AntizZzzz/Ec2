package com.jiangyifen.ec2.backgroundthread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.utils.Config;

public class DiskSpaceMonitor extends Thread {
	private static final int HI_LINE = 90;
	private static final int LO_LINE = 70;
	private static final int period = 3600000;

	private final Log logger = LogFactory.getLog(getClass());
	private String recLocalDiskPath;

	public DiskSpaceMonitor() {
		recLocalDiskPath = Config.props.getProperty("rec_local_disk_path");
		this.setDaemon(true);
		this.setName("DiskSpaceMonitorThread");
		this.start();
	}

	private int getFreeDiskSpace() {
		int freeSpace = 0;
		try {
			Process p = Runtime.getRuntime().exec("df");
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			br.readLine();
			br.readLine();
			String line = br.readLine();
			while (line.indexOf("  ") != -1)
				line = line.replace("  ", " ");
			String s = line.split(" ")[4];
			freeSpace = Integer.valueOf(s.substring(0, s.length() - 1)).intValue();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return freeSpace;
	}

	private File[] getFiles(String dir) {
		File d = new File(dir);
		return d.listFiles();
	}

	public void run() {
		try {
			while (true) {

				// 根据内部配置管理里面进行判断磁盘空间不足是否自动删除录音, 该配置功能只针对单租户, 多租户的话, 没有办法配置, 默认是删除录音的
				if(ShareData.domainToConfigs.get(1L).get("setting_diskspace_delete_soundfile") != null && ShareData.domainToConfigs.get(1L).get("setting_diskspace_delete_soundfile")) {
					try {
						Thread.sleep(300000);
						continue;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
				
				Date current = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("HH");
				String h = sdf.format(current);
				Integer hour = Integer.valueOf(h);

				if (hour >= 3 && hour <= 6) {

					int usedSpace = getFreeDiskSpace();
					if (usedSpace >= HI_LINE) {
						logger.warn("Disk space not enough: " + usedSpace + "% used.");
						File[] recDirs = getFiles(recLocalDiskPath);
						Arrays.sort(recDirs);
						String cmd;
						for (int i = 0; i < recDirs.length; i++) {
							cmd = "rm " + recDirs[i].toString() + " -rf";
							logger.info(cmd);
							try {
								Runtime.getRuntime().exec(cmd);
								Thread.sleep(300000);
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
							usedSpace = getFreeDiskSpace();
							logger.info("Disk used " + usedSpace + "%");
							if (usedSpace <= LO_LINE) {
								logger.info("Disk used " + usedSpace + "%, < " + LO_LINE + "%.Stop deleteing record files");
								break;
							}
						}
					}

				}

				Thread.sleep(period);

			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
