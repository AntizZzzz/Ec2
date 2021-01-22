package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.MusicOnHoldService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.mohmanage.AddMusicOnHold;
import com.jiangyifen.ec2.ui.mgr.mohmanage.EditMusicOnHold;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.ConvertVoiceFormat;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * 保持音乐管理 组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MusicOnHoldManagement extends VerticalLayout implements
		ClickListener, ValueChangeListener, Action.Handler {

	// 表格中语音文件夹的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] { "name", "mode",
			"description" };

	private final String[] COL_HEADERS = new String[] { "配置名称", "模式", "描述信息" };

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// 主界面布局管理器
	private GridLayout gridLayout;

	private Notification notification;

	// 左侧搜索组件
	private TextField leftKeyword;
	private Button leftSearch;

	// 语音文件夹表格组件
	private Table leftTable;
	private String searchSql;
	private String countSql;
	private FlipOverTableComponent<MusicOnHold> flip;

	// 语音文件夹表格按钮组件
	private Button leftAdd;
	private Button leftEdit;
	private Button leftDelete;
	// private Button applyToAsterisk; // 将更改后的内容应用到 asterisk 的配置文件中去

	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");

	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private AddMusicOnHold addMohWindow;
	private EditMusicOnHold editMohWindow;

	// 当要删除左侧表格中的语音文件夹对象时，弹出确认窗口，因为删除文件的同时也需要将文件夹对应的语音文件一起删除
	private Window leftDeleteConfirmWindow;
	private Button submit;
	private Button abolish;

	// 右侧语音文件查看组件
	private TextField rightKeyword;
	private Button rightSearch;
	private Table rightTable;
	private BeanItemContainer<VoiceFile> rightTableContainer;

	// 右侧语音文件表格按钮组件
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	private Upload rightUpload;
	private Button cancelUpload;
	private Button rightDelete;
	private String uploadFileFullPath; // 文件上传的全路径
	private String convertFileFullPath; // 文件转码后存放的路径（asterisk 真实使用的路径）
	private Boolean isFileExists = false;
	private Boolean isFileTypeError = false;

	/**
	 * 其他变量
	 */
	private Domain domain;
	private ArrayList<MusicOnHold> needDeleteMusicOnHolds; // 需要被删除的MusicOnHold
															// 对象，这些对象都是左侧表格中CheckBox
															// 被选中的对象
	private ArrayList<MusicOnHold> canDeleteMusicOnHolds; // 可以被删除的MusicOnHold
															// 对象，这些对象从needDeleteMudicOnHolds
															// 和leftTable
															// 被选中的值中选出来的，都是可以被删除的对象
	private ArrayList<VoiceFile> needDeleteVoiceFiles;
	private MusicOnHoldService musicOnHoldService;
	private QueueService queueService;
	private ReloadAsteriskService reloadAsteriskService;

	public MusicOnHoldManagement() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("100%");

		needDeleteMusicOnHolds = new ArrayList<MusicOnHold>();
		canDeleteMusicOnHolds = new ArrayList<MusicOnHold>();
		needDeleteVoiceFiles = new ArrayList<VoiceFile>();

		domain = SpringContextHolder.getDomain();
		musicOnHoldService = SpringContextHolder.getBean("musicOnHoldService");
		queueService = SpringContextHolder.getBean("queueService");
		reloadAsteriskService = SpringContextHolder
				.getBean("reloadAsteriskService");

		gridLayout = new GridLayout(2, 3);
		gridLayout.setWidth("100%");
		gridLayout.setSpacing(true);
		gridLayout.setColumnExpandRatio(0, 0.6f);
		gridLayout.setColumnExpandRatio(1, 0.4f);
		this.addComponent(gridLayout);

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		// 创建队列表格上方的组件（包括搜索组件）
		gridLayout.addComponent(createLeftTopComponents(), 0, 0);
		gridLayout.addComponent(createRightTopComponents(), 1, 0);

		// 创建队列表格显示区组件
		leftTable = createLeftTable();
		gridLayout.addComponent(leftTable, 0, 1);

		rightTable = createRightTable();
		gridLayout.addComponent(rightTable, 1, 1);

		// 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
		gridLayout.addComponent(createLeftTableFooterComponents(), 0, 2);
		gridLayout.addComponent(createrightTableFooterComponents(), 1, 2);

		// 创建删除语音文件夹的确认子窗口
		createDeleteConfirmWindow();
	}

	/**
	 * 创建删除语音文件夹的确认子窗口
	 */
	private void createDeleteConfirmWindow() {
		leftDeleteConfirmWindow = new Window("确认操作");
		leftDeleteConfirmWindow.center();
		leftDeleteConfirmWindow.setWidth("300px");
		leftDeleteConfirmWindow.setModal(true);
		leftDeleteConfirmWindow.setResizable(false);

		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeFull();
		windowContent.setMargin(true);
		windowContent.setSpacing(true);
		leftDeleteConfirmWindow.setContent(windowContent);

		Label noticeInConfirmWindow = new Label(
				"<font color='red'>删除语音文件夹将会<B>把该文件夹对应的所有语音文件一起删除</B>，"
						+ "你<B>确定</B>要删除与之关联的语音文件吗？</font>",
				Label.CONTENT_XHTML);
		windowContent.addComponent(noticeInConfirmWindow);

		HorizontalLayout buttonsHLayout = new HorizontalLayout();
		buttonsHLayout.setSpacing(true);
		windowContent.addComponent(buttonsHLayout);

		submit = new Button("确 定", this);
		abolish = new Button("取 消", this);
		abolish.setStyleName("default");
		buttonsHLayout.addComponent(submit);
		buttonsHLayout.addComponent(abolish);
	}

	/**
	 * 创建搜索需要的响应组件
	 */
	private HorizontalLayout createLeftTopComponents() {
		// 创建搜索分机的相应组件
		HorizontalLayout leftSearchHLayout = new HorizontalLayout();
		leftSearchHLayout.setSpacing(true);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		leftSearchHLayout.addComponent(caption);

		leftKeyword = new TextField();
		leftKeyword.setImmediate(true);
		leftKeyword.setInputPrompt("请输入搜索关键字");
		leftKeyword.setDescription("可按'配置名称'进行搜索");
		leftKeyword.setStyleName("search");
		leftKeyword.addListener(this);
		leftSearchHLayout.addComponent(leftKeyword);

		leftSearch = new Button("搜索", this);
		leftSearch.setImmediate(true);
		leftSearchHLayout.addComponent(leftSearch);

		return leftSearchHLayout;
	}

	/**
	 * 创建队列表格显示区组件
	 */
	private Table createLeftTable() {
		Table table = createFormatColumnTable();
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.addActionHandler(this);
		table.setPageLength(20);
		return table;
	}

	private HorizontalLayout createRightTopComponents() {
		// 创建搜索分机的相应组件
		HorizontalLayout rightSearchHLayout = new HorizontalLayout();
		rightSearchHLayout.setSpacing(true);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		rightSearchHLayout.addComponent(caption);

		rightKeyword = new TextField();
		rightKeyword.setImmediate(true);
		rightKeyword.setInputPrompt("请输入搜索关键字");
		rightKeyword.setDescription("可按'文件名称'进行搜索");
		rightKeyword.setStyleName("search");
		rightKeyword.addListener(this);
		rightSearchHLayout.addComponent(rightKeyword);

		rightSearch = new Button("搜索", this);
		rightSearch.setImmediate(true);
		rightSearchHLayout.addComponent(rightSearch);

		return rightSearchHLayout;
	}

	/**
	 * 创建格式化 了 name 显示内容的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (colId.equals("name")) {
					String name = (String) property.getValue();
					return name;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	private Table createRightTable() {
		Table table = new Table();
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.setPageLength(20);

		rightTableContainer = new BeanItemContainer<VoiceFile>(VoiceFile.class);
		table.setContainerDataSource(rightTableContainer);
		table.setVisibleColumns(new Object[] { "name" });
		table.setColumnHeaders(new String[] { "语音文件名称" });
		table.addGeneratedColumn("delete", new DeleteColumnRightGenerator());
		table.setColumnHeader("delete", "删除语音文件");
		return table;
	}

	/**
	 * 创建右侧表格下方的各组件，包括上传进度显示、删除、上传按钮等
	 * 
	 * @return
	 */
	private HorizontalLayout createrightTableFooterComponents() {
		HorizontalLayout rightFooterHLayout = new HorizontalLayout();
		rightFooterHLayout.setSpacing(true);
		rightFooterHLayout.setWidth("100%");

		// 创建进度组件
		progressLayout = new HorizontalLayout();
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		rightFooterHLayout.addComponent(progressLayout);

		Label piLabel = new Label("上传进度：");
		piLabel.setWidth("-1px");
		progressLayout.addComponent(piLabel);

		pi = new ProgressIndicator();
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

		cancelUpload = new Button("取 消", this);
		progressLayout.addComponent(cancelUpload);

		// 创建上传组件及删除组件
		HorizontalLayout rightFooter = new HorizontalLayout();
		rightFooter.setSpacing(true);
		rightFooterHLayout.addComponent(rightFooter);
		rightFooterHLayout.setComponentAlignment(rightFooter,
				Alignment.MIDDLE_RIGHT);

		rightUpload = new Upload();
		rightUpload.setButtonCaption("上传语音");
		rightUpload.setEnabled(false);
		rightUpload.setImmediate(true);
		rightFooter.addComponent(rightUpload);

		// 为上传按钮添加接收器
		addReceiverToUpLoad(rightUpload);

		// 为上传按钮添加监听器
		addListenersToUpload(rightUpload);

		rightDelete = new Button("删 除", this);
		rightDelete.setImmediate(true);
		rightDelete.setEnabled(false);
		rightFooter.addComponent(rightDelete);

		return rightFooterHLayout;
	}

	/**
	 * 为上传按钮添加接收器
	 * 
	 * @param upload
	 */
	// TODO 该方法存在处理方式问题，在上传文件格式不正确，及文件已经存在的情况下，应该返回null值，并且处理上传导致的异常
	private void addReceiverToUpLoad(final Upload upload) {
		upload.setReceiver(new Receiver() {
			private OutputStream os = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					rightUpload.interruptUpload();
				}
			};

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				isFileExists = false;
				isFileTypeError = false;
				if (!filename.matches("\\w+\\.\\w+")) {
					isFileExists = true;
					notification.setCaption("文件名只能由数字或字母组成！");
					upload.getApplication().getMainWindow()
							.showNotification(notification);
					return os;
				}

				if (!(filename.endsWith(".wav") || filename.endsWith(".mp3"))) {
					isFileExists = true;
					notification.setCaption("上传文件格式错误，(只能上传 wav,mp3 格式的语音文件)！");
					upload.getApplication().getMainWindow()
							.showNotification(notification);
					return os;
				}
				//
				// String type = mimeType.substring(0, mimeType.indexOf("/"));
				// if(!"audio".equals(type)) {
				// isFileExists = true;
				// upload.getApplication().getMainWindow().showNotification(filename+
				// " 不是语音文件，无法上传！");
				// return os;
				// }

				FileOutputStream fos = null;
				BufferedOutputStream bw = null;
				MusicOnHold moh = (MusicOnHold) leftTable.getValue();
				String newFilename = filename.substring(0,
						filename.indexOf("."))
						+ ".sln";
				uploadFileFullPath = Config.props
						.getProperty(Config.CUSTOMER_MOH_PATH)
						+ domain.getName()
						+ "/"
						+ moh.getDirectory()
						+ "/uncorverted/" + filename;
				convertFileFullPath = Config.props
						.getProperty(Config.CUSTOMER_MOH_PATH)
						+ domain.getName()
						+ "/"
						+ moh.getDirectory()
						+ "/"
						+ newFilename;
				File oldFile = new File(convertFileFullPath);
				File voiceFile = new File(uploadFileFullPath);
				if (!oldFile.exists()) {
					if (!voiceFile.getParentFile().exists()) {
						voiceFile.getParentFile().mkdirs();
					}
					try {
						voiceFile.createNewFile();
					} catch (IOException e) {
						logger.error("创建文件失败:" + newFilename, e);
						notification.setCaption("创建文件 " + newFilename + " 失败！");
						upload.getApplication().getMainWindow()
								.showNotification(notification);
						return os;
					}
				} else {
					isFileExists = true;
					notification.setCaption("文件 " + newFilename
							+ " 已经存在，请删除老文件后再重新上传！");
					upload.getApplication().getMainWindow()
							.showNotification(notification);
					return os;
				}
				try {
					fos = new FileOutputStream(voiceFile);
					bw = new BufferedOutputStream(fos);
				} catch (FileNotFoundException e) {
					e.printStackTrace(); // 应该不会出现
				}

				return bw;
			}
		});
	}

	/**
	 * 为文件上传按钮添加监听器
	 * 
	 * @param upload
	 */
	private void addListenersToUpload(final Upload upload) {
		upload.addListener(new StartedListener() {
			@Override
			public void uploadStarted(StartedEvent event) {
				upload.setVisible(false);
				progressLayout.setVisible(true);
				pi.setValue(0f);
				pi.setPollingInterval(1000);
				pi.setDescription("正在上传语音文件：" + event.getFilename());
			}
		});

		upload.addListener(new ProgressListener() {
			@Override
			public void updateProgress(long readBytes, long contentLength) {
				pi.setValue(new Float(readBytes / (float) contentLength));
			}
		});

		upload.addListener(new SucceededListener() {
			@Override
			public void uploadSucceeded(SucceededEvent event) {
				if (!isFileTypeError && !isFileExists) {
					try {
						// TODO lc 格式转换
						ConvertVoiceFormat.startConvert(uploadFileFullPath,
								convertFileFullPath);
					} catch (Exception e) {

						File file = new File(uploadFileFullPath);
						if (file.exists()) {
							file.delete();
						}
						e.printStackTrace();
						logger.error(e.getMessage() + "上传语音文件转码时出现异常！", e);
						notification.setCaption("语音文件转换失败，请重试！");
						upload.getApplication().getMainWindow()
								.showNotification(notification);
						return;
					} 

					// 删除刚上传的未转码文件
					File file = new File(uploadFileFullPath);
					if (file.exists()) {
						file.delete();
					}
					// 更新右侧表格的显示内容
					MusicOnHold moh = (MusicOnHold) leftTable.getValue();
					leftTable.setValue(null);
					leftTable.setValue(moh);
					String newFilename = event.getFilename().substring(0,
							event.getFilename().indexOf("."))
							+ ".sln";
					notification.setCaption("成功上传语音文件：" + newFilename);
					upload.getApplication().getMainWindow()
							.showNotification(notification);

					// reload asterisk
					reloadAsteriskService.reloadAsterisk();
				}
			}
		});

		upload.addListener(new FailedListener() {
			@Override
			public void uploadFailed(FailedEvent event) {
				if (!isFileTypeError && !isFileExists) {
					notification.setCaption("上传语音文件：" + event.getFilename()
							+ " 失败！");
					upload.getApplication().getMainWindow()
							.showNotification(notification);
				}
			}
		});

		// This method gets called always when the upload finished, either
		// succeeding or failing
		upload.addListener(new FinishedListener() {
			@Override
			public void uploadFinished(FinishedEvent event) {
				progressLayout.setVisible(false);
				upload.setVisible(true);
			}
		});
	}

	/**
	 * 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
	 */
	private HorizontalLayout createLeftTableFooterComponents() {
		HorizontalLayout leftFooterHLayout = new HorizontalLayout();
		leftFooterHLayout.setSpacing(true);
		leftFooterHLayout.setWidth("100%");
		this.addComponent(leftFooterHLayout);

		HorizontalLayout leftFooter = new HorizontalLayout();
		leftFooter.setSpacing(true);
		leftFooterHLayout.addComponent(leftFooter);

		leftAdd = new Button("添 加", this);
		leftAdd.setImmediate(true);
		leftFooter.addComponent(leftAdd);

		leftEdit = new Button("编 辑", this);
		leftEdit.setImmediate(true);
		leftEdit.setEnabled(false);
		leftFooter.addComponent(leftEdit);

		leftDelete = new Button("删 除", this);
		leftDelete.setImmediate(true);
		leftDelete.setEnabled(false);
		leftFooter.addComponent(leftDelete);

		// 初始化查询语句
		initializeSql();

		flip = new FlipOverTableComponent<MusicOnHold>(MusicOnHold.class,
				musicOnHoldService, leftTable, searchSql, countSql, null);
		flip.setPageLength(20, false);
		leftTable.setVisibleColumns(VISIBLE_PROPERTIES);
		leftTable.setColumnHeaders(COL_HEADERS);
		leftTable.addGeneratedColumn("delete", new DeleteColumnLeftGenerator());
		leftTable.setColumnHeader("delete", "删除");
		leftFooterHLayout.addComponent(flip);
		leftFooterHLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);

		return leftFooterHLayout;
	}

	/**
	 * 根据不同的需求，初始化查询语句
	 */
	private void initializeSql() {
		String keywordValue = (String) leftKeyword.getValue();
		countSql = "select count(moh) from MusicOnHold as moh where moh.name like '%"
				+ keywordValue + "%' and moh.domain.id = " + domain.getId();
		searchSql = countSql.replaceFirst("count\\(moh\\)", "moh")
				+ " order by moh.name asc";
	}

	/**
	 * 用于自动生成“删除”列，用于标记 是否将对应行的队列删除
	 */
	private class DeleteColumnLeftGenerator implements Table.ColumnGenerator {

		@Override
		public Object generateCell(Table source, final Object itemId,
				Object columnId) {
			final MusicOnHold musicOnhold = (MusicOnHold) itemId;

			if ("default".equals(musicOnhold.getName())) { // 如果是默认的语音文件夹，将不允许删除
				return null;
			}

			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if (needDeleteMusicOnHolds.contains(musicOnhold)) {
				check.setValue(true);
			}

			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if (value == false) {
						needDeleteMusicOnHolds.remove(musicOnhold);
					} else {
						needDeleteMusicOnHolds.add(musicOnhold);
					}

					// 修改删除按钮
					if (needDeleteMusicOnHolds.size() > 0) {
						leftDelete.setCaption("删除勾选项");
						leftDelete.setEnabled(true);
					} else {
						leftDelete.setCaption("删 除");
						leftDelete.setEnabled(leftTable.getValue() != null);
					}
				}
			});

			return check;
		}
	}

	/**
	 * 用于自动生成“删除”列，用于标记 是否将对应行的队列删除
	 */
	private class DeleteColumnRightGenerator implements Table.ColumnGenerator {

		@Override
		public Object generateCell(Table source, final Object itemId,
				Object columnId) {
			final VoiceFile voiceFile = (VoiceFile) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if (needDeleteVoiceFiles.contains(voiceFile)) {
				check.setValue(true);
			}

			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if (value == false) {
						needDeleteVoiceFiles.remove(voiceFile);
					} else {
						needDeleteVoiceFiles.add(voiceFile);
					}

					// 修改删除按钮
					if (needDeleteVoiceFiles.size() > 0) {
						rightDelete.setCaption("删除勾选项");
						rightDelete.setEnabled(true);
					} else {
						rightDelete.setCaption("删 除");
						rightDelete.setEnabled(rightTable.getValue() != null);
					}
				}
			});

			return check;
		}
	}

	/**
	 * Action.Handler 实现方法，供左侧表格使用
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		MusicOnHold moh = (MusicOnHold) target;
		if (moh == null || "default".equals(moh.getName())) {
			return new Action[] { ADD };
		}
		return new Action[] { ADD, EDIT, DELETE };
	}

	/**
	 * 处理表格的邮件单机事件，供左侧表格使用
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		leftTable.setValue(null);
		leftTable.select(target);
		if (ADD == action) {
			leftAdd.click();
		} else if (EDIT == action) {
			leftEdit.click();
		} else if (DELETE == action) {
			leftDelete.click();
		}
	}

	/**
	 * 当用户点击的添加队列按钮时，buttonClick调用显示 添加 队列窗口
	 */
	private void showAddWindow() {
		if (addMohWindow == null) {
			addMohWindow = new AddMusicOnHold(this);
		}
		this.getWindow().addWindow(addMohWindow);
	}

	/**
	 * 当用户点击的编辑队列按钮时，buttonClick调用显示 编辑 队列窗口
	 */
	private void showEditWindow() {
		if (editMohWindow == null) {
			editMohWindow = new EditMusicOnHold(this);
		}
		this.getWindow().addWindow(editMohWindow);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if (source == leftTable) {
			MusicOnHold target = (MusicOnHold) leftTable.getValue();
			// 将右侧文件显示表格中的内容清空
			rightTableContainer.removeAllItems();

			leftEdit.setEnabled(target != null);
			if (target == null) {
				leftDelete.setEnabled(needDeleteMusicOnHolds.size() > 0);
				rightUpload.setEnabled(false);
				rightDelete.setEnabled(false);
			} else {
				// 改变右侧文件显示表格中的内容
				rightKeyword.setValue("");
				List<String> filenames = musicOnHoldService.getVoiceFileNames(
						target.getDirectory(), domain);
				List<VoiceFile> files = new ArrayList<VoiceFile>();
				for (String filename : filenames) {
					VoiceFile vf = new VoiceFile();
					vf.setName(filename);
					files.add(vf);
				}
				rightTableContainer.addAll(files);

				if ("default".equals(target.getName())) {
					rightUpload.setEnabled(false);
					rightTable.setVisibleColumns(new Object[] { "name" });
				} else {
					rightUpload.setEnabled(true);
					rightTable.setVisibleColumns(new Object[] { "name",
							"delete" });
					rightTable
							.setColumnHeaders(new String[] { "语音文件名称", "删除语音" });
				}

				leftDelete.setEnabled(true);
				rightDelete.setEnabled(false);
			}
		} else if (source == leftKeyword) {
			leftSearch.click();
		} else if (source == rightTable) {
			VoiceFile target = (VoiceFile) rightTable.getValue();
			if (target == null && needDeleteVoiceFiles.size() == 0) {
				rightDelete.setEnabled(false);
			} else {
				rightDelete.setEnabled(true);
			}
		} else if (source == rightKeyword) {
			rightSearch.click();
		}
	}

	/**
	 * 处理各种操作按钮的单击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == leftAdd) {
			showAddWindow();
		} else if (source == leftEdit) {
			MusicOnHold moh = (MusicOnHold) leftTable.getValue();
			if ("default".equals(moh.getName())) {
				notification.setCaption("不能编辑系统默认的语音文件夹！");
				this.getApplication().getMainWindow()
						.showNotification(notification);
			} else {
				showEditWindow();
			}
		} else if (source == leftDelete) {
			confirmDeleteLeftTableValue();
		} else if (source == leftSearch) {
			initializeSql();
			flip.setCountSql(countSql);
			flip.setSearchSql(searchSql);
			flip.refreshToFirstPage();
			leftTable.setValue(null);
		} else if (source == rightSearch) {
			executeRightSearch();
		} else if (source == rightDelete) {
			excuteRightDelete();
		} else if (source == submit) {
			this.getApplication().getMainWindow()
					.removeWindow(leftDeleteConfirmWindow);
			excuteLeftDelete();
		} else if (source == abolish) {
			this.getApplication().getMainWindow()
					.removeWindow(leftDeleteConfirmWindow);
		} else if (source == cancelUpload) {
			rightUpload.interruptUpload();
		}
	}

	/**
	 * 判断左侧表格中被勾选项或者直接选中的Item 是否可删，如果可以删除，则将其存放到canDeleteMusicOnHolds 集合中
	 */
	private void confirmDeleteLeftTableValue() {
		// 清空可删除MusicOnHold的集合
		canDeleteMusicOnHolds.clear();
		int size = needDeleteMusicOnHolds.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				MusicOnHold moh = needDeleteMusicOnHolds.get(i);
				if (checkMusicOnHoldDeleteAble(moh) == false) {
					notification.setCaption("由于语音文件夹 " + moh.getName()
							+ "已经有相关队列与相关联，所以此次操作将不会删除它！");
					this.getApplication().getMainWindow()
							.showNotification(notification);
				} else {
					canDeleteMusicOnHolds.add(moh);
				}
			}
			if (canDeleteMusicOnHolds.size() > 0) {
				this.getApplication().getMainWindow()
						.addWindow(leftDeleteConfirmWindow);
			}
		} else {
			MusicOnHold moh = (MusicOnHold) leftTable.getValue();
			if ("default".equals(moh.getName())) {
				notification.setCaption("不能删除系统默认的语音文件夹！");
				this.getApplication().getMainWindow()
						.showNotification(notification);
			} else if (checkMusicOnHoldDeleteAble(moh) == false) {
				notification.setCaption("语音文件夹 " + moh.getName()
						+ "已经有相关队列与之关联，暂不可删除！");
				this.getApplication().getMainWindow()
						.showNotification(notification);
			} else {
				canDeleteMusicOnHolds.add(moh);
				this.getApplication().getMainWindow()
						.addWindow(leftDeleteConfirmWindow);
			}
		}
	}

	/**
	 * 检验选中的语音文件夹是否已经被队列使用，如果已经被关联，则不允许删除
	 * 
	 * @param moh
	 * @return
	 */
	private boolean checkMusicOnHoldDeleteAble(MusicOnHold moh) {
		List<Queue> queues = queueService.getAllByMusicOnHold(moh, domain);
		if (queues.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 由buttonClick调用,左侧组件删除
	 */
	private void excuteLeftDelete() {
		for (int i = 0; i < canDeleteMusicOnHolds.size(); i++) {
			MusicOnHold moh = canDeleteMusicOnHolds.get(i);
			// 删除数据库中的语音文件夹描述实体
			musicOnHoldService.deleteById(moh.getId());
			// 删除Asterisk 中当前语音文件夹中的所有语音文件
			musicOnHoldService.deleteVoiceFolder(moh.getDirectory(), domain);
		}
		// 清空可删除的集合 和 存放需要删除的语音文件夹的List集合
		canDeleteMusicOnHolds.clear();
		needDeleteMusicOnHolds.clear();

		leftTable.setValue(null);
		leftDelete.setCaption("删 除");
		leftDelete.setEnabled(false);

		updateTable(false);
	}

	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if (rightTableContainer == null)
			return;

		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();

		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = (String) rightKeyword.getValue();

		Like compareAll = new Like("name", "%" + rightKeywordStr + "%", false);
		rightTableContainer.addContainerFilter(compareAll);
	}

	/**
	 * 由buttonClick调用,右侧组件删除
	 */
	private void excuteRightDelete() {
		int size = needDeleteVoiceFiles.size();
		MusicOnHold moh = (MusicOnHold) leftTable.getValue();
		if ("default".equals(moh.getName())) {
			notification.setCaption("不能删除系统默认的语音文件夹中的语音文件！");
			this.getApplication().getMainWindow()
					.showNotification(notification);
		} else {
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					VoiceFile vf = needDeleteVoiceFiles.get(0);
					boolean success = musicOnHoldService.deleteVoiceFile(
							moh.getDirectory(), vf.getName(), domain);
					if (success == true) {
						needDeleteVoiceFiles.remove(0);
						rightTableContainer.removeItem(vf);
						notification.setCaption("成功 删除语音文件 " + vf.getName()
								+ " !");
						this.getApplication().getMainWindow()
								.showNotification(notification);
					} else {
						notification.setCaption("删除语音文件 " + vf.getName()
								+ " 失败!");
						this.getApplication().getMainWindow()
								.showNotification(notification);
					}
				}
			} else {
				VoiceFile vf = (VoiceFile) rightTable.getValue();
				boolean success = musicOnHoldService.deleteVoiceFile(
						moh.getDirectory(), vf.getName(), domain);
				if (success == true) {
					rightTableContainer.removeItem(vf);
					notification.setCaption("成功 删除语音文件 " + vf.getName() + " !");
					this.getApplication().getMainWindow()
							.showNotification(notification);
				} else {
					notification.setCaption("删除语音文件 " + vf.getName() + " 失败!");
					this.getApplication().getMainWindow()
							.showNotification(notification);
				}
			}

			rightDelete.setCaption("删 除");
			rightDelete.setEnabled(false);
			// reload asterisk
			reloadAsteriskService.reloadAsterisk();
		}
	}

	/**
	 * 刷新显示语音文件夹的表格
	 * 
	 * @param isToFirst
	 *            刷新后是否跳转至第一页
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(searchSql);
			flip.setCountSql(countSql);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	public Table getleftTable() {
		return leftTable;
	}

	/**
	 * 语音文件实体类，用来获取一个文件夹下的所有文件
	 */
	public class VoiceFile {

		private String name;

		public VoiceFile() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
