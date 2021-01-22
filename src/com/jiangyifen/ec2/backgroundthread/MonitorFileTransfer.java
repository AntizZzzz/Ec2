package com.jiangyifen.ec2.backgroundthread;

import java.io.File;
import java.io.IOException;

import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

import com.jiangyifen.ec2.utils.Config;

public class MonitorFileTransfer extends Thread {

	private static final int sleepPeriod = 60 * 1000;
	private static final int modifyPeriod = 10 * 1000;

	private static final Log logger = LogFactory
			.getLog(MonitorFileTransfer.class);

	private static String recLocalDiskPath;
	private static String recLocalMemPath;

	public MonitorFileTransfer() {
		recLocalDiskPath = Config.props.getProperty("rec_local_disk_path");
		recLocalMemPath = Config.props.getProperty("rec_local_mem_path");
		this.setDaemon(true);
		this.setName("MonitorFileTransferThread");
//		this.start(); chenhb add , this thread not start and use
	}

	private static void move() {

		long now = System.currentTimeMillis();

		// 列出内存录音目录（即recLocalMemPath,/dev/shm）中所有的文件，并按最后修改时间排序
		File memPath = new File(recLocalMemPath);
		File[] files = memPath.listFiles();

		if (files.length != 0) {

			// Arrays.sort(files, new FileUtils.CompratorByLastModified());

			// 将已经写完的录音文件移动到硬盘上对应的目录中（即recLocalDiskPath,/var/www/html/monitor/xxxxxxxx）
			for (File file : files) {

				try {
					if ((now - file.lastModified() > modifyPeriod)) {

						String fileName = file.getName();
						String date = "";
						if(fileName.indexOf("-") > 0) {
							date = fileName.substring(0,fileName.indexOf("-"));
						}

						String diskPath = recLocalDiskPath + date + "/";

						String fromFile = recLocalMemPath + fileName;

						String toFile = fileName.replaceAll("WAV", "wav");
						toFile = diskPath + toFile;

						// 如果目录不存在，先建目录
						File diskPathFile = new File(diskPath);
						if (!diskPathFile.exists()) {
							diskPathFile.mkdirs();
						}

						try {
							String cmd = "mv " + fromFile + " " + toFile;
							logger.info(cmd);
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}

					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public void run() {
		try {
			while (true) {

				move();
				Thread.sleep(sleepPeriod);

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
