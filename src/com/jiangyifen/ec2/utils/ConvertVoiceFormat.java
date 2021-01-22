package com.jiangyifen.ec2.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertVoiceFormat {

	private static Logger logger = LoggerFactory
			.getLogger(ConvertVoiceFormat.class);

	/**
	 * @param beforeConverPath
	 *            转换前文件路径（包含文件名）
	 * @param afterConverPath
	 *            转换后文件路径（包含文件名）
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void startConvert(String beforeConverPath,
			String afterConverPath) throws IOException, InterruptedException {

		logger.info("beforeConverPath ==> " + beforeConverPath);
		logger.info("afterConverPath ==> " + afterConverPath);

		boolean b = beforeConverPath.endsWith(".wav")
				|| beforeConverPath.endsWith(".mp3");

		if (!b) {
			throw new RuntimeException("要转换的语音文件必须以.wav或.mp3结尾");
		}

		if (beforeConverPath.endsWith(".mp3")) {

			// afterConverPath转换后的文件名（不包含后缀）
			String str = afterConverPath.substring(0,
					afterConverPath.indexOf("."));

			logger.info("/usr/local/bin/mpg123 -w " + str + ".wav" + " " + beforeConverPath);

			// mp3转成wav
			Process process = Runtime.getRuntime().exec(
					"/usr/local/bin/mpg123 -w " + str + ".wav" + " " + beforeConverPath);
			process.waitFor();

			logger.info("/usr/bin/sox " + str + ".wav"
					+ " -t raw -r 8k -c 1 -b 16 -e signed " + str + ".sln");
			// wav转成sln
			process = Runtime.getRuntime().exec(
					"sox " + str + ".wav"
							+ " -t raw -r 8k -c 1 -b 16 -e signed " + str
							+ ".sln");
			process.waitFor();

			logger.info("rm -rf " + str + ".wav");
			// 删除wav
			process = Runtime.getRuntime().exec("rm -rf " + str + ".wav");
			process.waitFor();

			if (!exists(str + ".sln")) {

				File file = new File(beforeConverPath);
				if (file.exists()) {
					file.delete();
				}
				logger.warn("语音文件格式转换失败");
				throw new RuntimeException("语音文件格式转换失败");
			}

		} else {

			// beforeConverPath转换后的文件名（不包含后缀）
			String bstr = beforeConverPath.substring(0,
					beforeConverPath.indexOf("."));

			// afterConverPath转换后的文件名（不包含后缀）
			String astr = afterConverPath.substring(0,
					afterConverPath.indexOf("."));

			logger.info("sox " + bstr + ".wav"
					+ " -t raw -r 8k -c 1 -b 16 -e signed " + astr + ".sln");
			// wav转成sln
			Process process = Runtime.getRuntime().exec(
					"sox " + bstr + ".wav"
							+ " -t raw -r 8k -c 1 -b 16 -e signed " + astr
							+ ".sln");
			process.waitFor();

			if (!exists(astr + ".sln")) {

				File file = new File(beforeConverPath);
				if (file.exists()) {
					file.delete();
				}
				logger.warn("语音文件格式转换失败");
				throw new RuntimeException("语音文件格式转换失败");
			}
		}

	}

	/**
	 * @param pathname
	 *            文件路径名
	 * @return 判断文件是否存在
	 */
	public static boolean exists(String pathname) {

		File file = new File(pathname);

		return file.exists();
	}
}
