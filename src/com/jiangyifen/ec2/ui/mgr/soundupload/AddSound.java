package com.jiangyifen.ec2.ui.mgr.soundupload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.SoundUpload;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ConvertVoiceFormat;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 创建上传新资源（Upload）
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AddSound extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件区域
	 */
	// 上传新批次组件
	private TextField name;
	private Label infoLable;
	private Upload upload;
	private Button cancel;
	private Button save;

	/**
	 * 其他组件
	 */
	private String labelName;
	private String storeName;

	private Domain domain;
	private User user;
	// 上传的文件
	private File uploadFile;
	// 上传文件的路径
	private String path;
	// 父组件的引用
	private SoundUpload soundUpload;
	private SoundFileService soundFileService;

	/**
	 * 构造器
	 * 
	 * @param soundUpload
	 *            资源上传视图的引用
	 */
	public AddSound(SoundUpload soundUpload) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("上传语音");
		this.soundUpload = soundUpload;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		// 批次名称
		windowContent.addComponent(buildSoundNameLayout());

		// 上传文件信息Label
		infoLable = new Label("");
		windowContent.addComponent(infoLable);

		// 上传输出
		HorizontalLayout uploadLayout = buildUploadLayout();

		windowContent.addComponent(uploadLayout);
		windowContent.setComponentAlignment(uploadLayout,
				Alignment.BOTTOM_RIGHT);
	}

	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		user = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		soundFileService = SpringContextHolder.getBean("soundFileService");
	}

	/**
	 * 上传批次的名称
	 * 
	 * @return
	 */
	private HorizontalLayout buildSoundNameLayout() {
		HorizontalLayout soundNameLayout = new HorizontalLayout();
		this.addComponent(soundNameLayout);

		soundNameLayout.addComponent(new Label("语音名称"));
		name = new TextField();
		name.setImmediate(true);
		name.setWriteThrough(true);
		name.setInputPrompt("请输入名称");
		name.setWidth("7em");
		soundNameLayout.addComponent(name);
		return soundNameLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildUploadLayout() {
		HorizontalLayout uploadLayout = new HorizontalLayout();
		uploadLayout.setSpacing(true);

		// 选择上传文件按钮
		upload = new Upload();
		upload.setImmediate(true);
		upload.setButtonCaption("上传");
		this.setUploadListener();
		this.assignReceiverForUpload(upload);
		uploadLayout.addComponent(upload);

		// 取消按钮
		save = new Button("保存");
		save.addListener(this);
		uploadLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		uploadLayout.addComponent(cancel);
		return uploadLayout;
	}

	/**
	 * 由 buildPathLayout 调用，指定上传的Excel文件的存储名称和位置
	 */
	private void assignReceiverForUpload(final Upload upload) {

		upload.setReceiver(new Upload.Receiver() {
			private OutputStream fos = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// jrh 中断上传
					upload.interruptUpload();
				}
			};

			public OutputStream receiveUpload(String filename, String mimeType) {

				// lc 文件格式检测

				if (!filename.matches("\\w+\\.\\w+")) {

					upload.getApplication().getMainWindow()
							.showNotification("文件名只能有数字或字母组成！");
					return fos;
				}

				if (!(filename.endsWith(".wav") || filename.endsWith(".mp3"))) {

					upload.getApplication()
							.getMainWindow()
							.showNotification("上传文件格式错误，(只能上传 wav,mp3 格式的语音文件)");
					return fos;
				}

				// Output stream to write to
				// FileOutputStream fos = null;
				labelName = filename;
				storeName = System.currentTimeMillis() + "";
				String type = filename.substring(filename.indexOf("."));
				path = ConfigProperty.ASTERISK_SOUNDFILE_PATH + "/"
						+ domain.getId() + "/" + storeName + type;

				uploadFile = new File(path);

				if (!uploadFile.exists()) {
					if (!uploadFile.getParentFile().exists()) {
						uploadFile.getParentFile().mkdirs();
					}
					try {
						uploadFile.createNewFile();
					} catch (IOException e) {
						logger.error("创建文件失败:" + path, e);
						throw new RuntimeException("无法再指定位置创建新文件！");
					}
				} else {
					try {
						uploadFile.delete();
					} catch (Exception e) {
						logger.error("文件被替换:" + path, e);
					}
				}
				try {
					fos = new FileOutputStream(uploadFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();// 应该不会出现
				}

				return fos;
			}

		});

	}

	/**
	 * 为Upload组件设置多个监听器
	 */
	private void setUploadListener() {
		upload.addListener(new Upload.SucceededListener() {
			public void uploadSucceeded(SucceededEvent event) {
				infoLable.setValue(labelName);

				// TODO lc 格式转换
				try {
					if (path != null) {

						ConvertVoiceFormat.startConvert(path, path);

						File file = new File(path);
						if (file.exists()) {
							file.delete();
						}

					}

				} catch (Exception e) {
					File file = new File(path);
					if (file.exists()) {
						file.delete();
					}
					logger.error(e.getMessage(), e);
					upload.getApplication().getMainWindow()
							.showNotification("语音格式转换失败，请重试");
				} 
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		name.setValue("");
		infoLable.setValue("");
	}

	/**
	 * 点击上传按钮后的操作
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == cancel) {
			this.getParent().removeWindow(this);
		} else if (event.getButton() == save) {
			try {
				executeSave();
			} catch (Exception e) {
				logger.error("chb： 保存上传的语音文件失败！", e);
			}
		}
	}

	/**
	 * 执行保存
	 */
	private void executeSave() {
		SoundFile soundFile = new SoundFile();
		String nameStr = "";
		if (name.getValue() != null) {
			nameStr = ((String) name.getValue()).trim();
		}
		if (nameStr.equals("")) {
			NotificationUtil.showWarningNotification(this, "文件名不能为空");
			return;
		}

		if (infoLable.getValue() == null
				|| ((String) infoLable.getValue()).equals("")) {
			NotificationUtil.showWarningNotification(this, "请先上传文件");
			return;
		}
		// 新建并保存声音文件实体
		soundFile.setDescName(nameStr);
		soundFile.setImportDate(new Date());
		soundFile.setStoreName(storeName);
		soundFile.setUser(user);
		soundFile.setDomain(domain);
		soundFileService.update(soundFile);

		// 移除窗口，更新表格
		this.getParent().removeWindow(this);
		soundUpload.updateTable(true);
	}

}
