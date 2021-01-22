package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.MessagesManageService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 短信息显示布局
 * 
 * @author zhangm
 * 
 */
@SuppressWarnings("serial")
public final class MessageShow extends VerticalLayout implements ClickListener,
		ValueChangeListener {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	public final String PHONE_MESSAGE_MANAGEMENT_DOWNLOAD_HISTORY_MANAGEMENT = "phone_message_management&download_history_management";

	// 表格中语音文件夹的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] { "time", "id", "phoneNumber", "content", "messageType", "user"};

	private final String[] COL_HEADERS = new String[] { "时间", "记录编号", "电话", "内容", "状态", "发送人"};
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// ============================= 持有的service =============================//
	private ApplicationContext context = SpringContextHolder.getApplicationContext();
	private MessagesManageService messagesManageService = (MessagesManageService) context
			.getBean("messagesManageService");

	private UserService userService = (UserService) context
			.getBean("userService");

	private CommonService commonService;

	// ============================= 搜索布局 =============================//
	private Button searchB; // 搜索按钮
	private Button clearUpB; // 清空按钮
	private ComboBox userC; // 选择用户下拉表单
	private PopupDateField startP; // 起始时间
	private PopupDateField endP; // 截止时间

	// ============================= 显示布局 ============================//
	private Table showTable;

	// ============================= 按钮和翻页布局 ============================//
	private HorizontalLayout buttonLayout; // 布局
	private Button deleteB; // 删除按钮
	
	// 翻页组件
	private FlipOverTableComponent<Message> flip; // 翻页组件
	private String sqlSelect;
	private String sqlCount;
	private Message selectedMsg; // 选中的消息

	// jrh
	private Embedded downloader; 				// 存放数据的组件
	private Button exportLogExcel;				// 导出日志Excel
	private ProgressIndicator pi; 				// 进度条
	private HorizontalLayout progressLayout; 	// 进度条组件存放布局管理器
	
	private User loginUser;						// 当前登录用户
	private boolean isEncryptMobile = true; 							// 默认使用加密的电话号码和手机号
	private String exportPath;											// 导出路径
	private ArrayList<String> ownBusinessModels;						// 当前用户所有用的所有管理角色的权限

	public MessageShow() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		this.addComponent(mainLayout);
		mainLayout.setSizeFull();
		
		this.loginUser = SpringContextHolder.getLoginUser();
		// 记录导出日志
		this.exportPath = ConfigProperty.PATH_EXPORT;

		this.ownBusinessModels = SpringContextHolder.getBusinessModel();
		// 判断是否需要加密
		this.isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		commonService = SpringContextHolder.getBean("commonService");
		
		// 添加搜索部分
		mainLayout.addComponent(buildSearchLayout());
		// 添加显示部分
		initTable();
		mainLayout.addComponent(showTable);
		// 添加按钮及翻页部分
		mainLayout.addComponent(buildButtonLayout());

		showTable.addGeneratedColumn("phoneNumber", new TelephonesColumnGenerator());
		showTable.setVisibleColumns(VISIBLE_PROPERTIES);
		showTable.setColumnHeaders(COL_HEADERS);
	}

	/*
	 * Table组件初始设定
	 */
	private void initTable() {
		this.showTable = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if(v == null) {
					return "";
				} else if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(v);
				} else if("user".equals(colId)) {
					User sender = (User) property.getValue();
					String info = sender.getEmpNo();
					if(sender.getRealName() != null) {
						info = info + " - " + sender.getRealName();
					}
					return info;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		showTable.setWidth("100%");
		showTable.setSelectable(true);
		showTable.setImmediate(true);

		showTable.addListener((ValueChangeListener) this);
	}

	/*
	 * 搜索组件初始设定
	 */
	private Component buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setMargin(true);

		HorizontalLayout constrantLayout = new HorizontalLayout();
		Label userL = new Label("用户");
		Label startDL = new Label("开始时间：");
		Label endDL = new Label("结束时间：");
		userC = new ComboBox();
		startP = new PopupDateField();
		startP.setWidth("160px");
		startP.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startP.setResolution(PopupDateField.RESOLUTION_SEC);
		startP.setImmediate(true);
		endP = new PopupDateField();
		endP.setWidth("160px");
		endP.setDateFormat("yyyy-MM-dd HH:mm:ss");
		endP.setResolution(PopupDateField.RESOLUTION_SEC);
		endP.setImmediate(true);
		constrantLayout.addComponent(userL);
		constrantLayout.addComponent(userC);
		constrantLayout.addComponent(startDL);
		constrantLayout.addComponent(startP);
		constrantLayout.addComponent(endDL);
		constrantLayout.addComponent(endP);

		List<User> users = userService.getAllByDomain(SpringContextHolder
				.getDomain());
		for (int i = 0; i < users.size(); i++) {
			userC.addItem(users.get(i));
		}
		searchLayout.addComponent(constrantLayout);
		searchB = new Button("搜索");
		clearUpB = new Button("清空");
		searchLayout.addComponent(searchB);
		searchLayout.addComponent(clearUpB);
		searchB.addListener((Button.ClickListener) this);
		clearUpB.addListener((Button.ClickListener) this);
		return searchLayout;
	}

	/*
	 * 按钮与翻页组件初始设定
	 */
	private Component buildButtonLayout() {
		buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		deleteB = new Button("删除");
		deleteB.addListener((Button.ClickListener) this);
//		layout.addComponent(deleteB);
		buttonLayout.addComponent(layout);

		HorizontalLayout flp_lo = new HorizontalLayout();
		flp_lo.setSpacing(true);
		buttonLayout.addComponent(flp_lo);
		buttonLayout.setComponentAlignment(flp_lo, Alignment.MIDDLE_RIGHT);

		// 创建下载组件
		createExportComponents(flp_lo);
		
		setSearchTerm();
		flip = new FlipOverTableComponent<Message>(Message.class,
				messagesManageService, showTable, sqlSelect, sqlCount, null);

		flp_lo.addComponent(flip);
		flp_lo.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);

		return buttonLayout;
	}

	/**
	 * jrh
	 *	 创建导出历史记录的工具
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooterRightLayout) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooterRightLayout.addComponent(exportHLayout);
		tableFooterRightLayout.setComponentAlignment(exportHLayout, Alignment.MIDDLE_LEFT);
		
		// 创建进度组件
		progressLayout = new HorizontalLayout();
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		exportHLayout.addComponent(progressLayout);

		Label piLabel = new Label("下载进度：");
		piLabel.setWidth("-1px");
		progressLayout.addComponent(piLabel);

		pi = new ProgressIndicator();
		pi.setPollingInterval(100);
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);

		// 如果拥有下载权限才创建相关下载组件
		if(ownBusinessModels.contains(PHONE_MESSAGE_MANAGEMENT_DOWNLOAD_HISTORY_MANAGEMENT)) {
			exportLogExcel = new Button("导出历史短信", this);
			exportLogExcel.setStyleName("default");
			exportLogExcel.setImmediate(true);
			tableFooterRightLayout.addComponent(exportLogExcel);
		}
	}
	
	/*
	 * 设定搜索语句
	 */
	private void setSearchTerm() {
		StringBuffer sql = new StringBuffer(
				"select m from Message as m where 1=1 ");
		if (userC.getValue() != null) {
			User user=(User)userC.getValue();
			sql.append(" and m.user.id = " + user.getId());
		}
		if (startP.getValue() != null) {
			sql.append(" and m.time > {ts '" + startP.getValue() + "'}");
		}
		if (endP.getValue() != null) {
			sql.append(" and m.time < {ts '" + endP.getValue() + "'}");
		}
		sql.append(" order by m.id desc");
		this.sqlSelect = sql.toString();
		
		
		//计数的Sql
		StringBuffer sqlCount = new StringBuffer(
				"select count(m) from Message as m where 1=1 ");
		if (userC.getValue() != null) {
			User user=(User)userC.getValue();
			sqlCount.append(" and m.user.id = " + user.getId());
		}
		if (startP.getValue() != null) {
			sqlCount.append(" and m.time > {ts '" + startP.getValue() + "'}");
		}
		if (endP.getValue() != null) {
			sqlCount.append(" and m.time < {ts '" + endP.getValue() + "'}");
		}
		this.sqlCount = sqlCount.toString();
	}

	/*
	 * 刷新关键字
	 */
	private void refresh() {
		// 更新用户
		userC.removeAllItems();
		List<User> users = userService.getAllByDomain(SpringContextHolder
				.getDomain());
		for (int i = 0; i < users.size(); i++) {
			userC.addItem(users.get(i));
		}
		updateTable(false);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		selectedMsg = (Message) showTable.getValue();
		// 改变按钮
		if (showTable.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			deleteB.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			deleteB.setEnabled(false);
		}

	}

	/*
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst 是否更新到第一页，default 是 false
	 */
	public void updateTable(Boolean isToFirst) {
		setSearchTerm();
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	/*
	 * 删除确认
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed) {
			Message message = selectedMsg;
			messagesManageService.deleteMessage(message);
			showTable.setValue(null);
			updateTable(false);
			this.getApplication().getMainWindow().showNotification("删除成功！");
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == deleteB) {
			execDelete();
		} else if (source == searchB) {
			setSearchTerm();
			updateTable(true);
		} else if (source == clearUpB) {
			refresh();
			startP.setValue(null);
			endP.setValue(null);
		} else if (source == exportLogExcel) {
			final String downloadCountSql = flip.getCountSql();
			final Domain domain = loginUser.getDomain();
			// 导出记录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportLogExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportExcelThreadRun(downloadCountSql, domain, loginUser);
					exportLogExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}

	/*
	 * 执行删除操作
	 */
	private void execDelete() {
		if (selectedMsg != null) {
			Label label = new Label("您确定要删除短信<b>" + selectedMsg.getContent()
					+ "</b>?", Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
					"confirmDelete");
			this.getApplication().getMainWindow().removeWindow(confirmWindow);
			this.getApplication().getMainWindow().addWindow(confirmWindow);
		} else {
			this.getApplication().getMainWindow().showNotification("未选中短信");
		}
	}

	/**
	 * 制作表格
	 */
	private boolean exportExcelThreadRun(String downloadCountSql,
			Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {

			//======================chb 历史短信======================//
			file = new File(new String((exportPath + "/历史短信_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),
					"ISO-8859-1"));
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法再指定位置创建新Excel文件！");
				}
			} else {
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			// 记录导出日志
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			operationLog.setIp(ip);
			operationLog.setDescription("导出历史短信");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(c\\)", "c"));
			commonService.save(operationLog);
			//======================chb 历史短信======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);

			// 记录查出来的历史短信Id值
			Message firstRecord = (Message) showTable.firstItemId();
			if (firstRecord == null) {
				this.getApplication().getMainWindow()
						.showNotification("选出的结果为空，不能导出历史短信！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			// 计数
			Long recordCount = commonService.getEntityCount(downloadCountSql) + 0L;
//			TODO
			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(m\\)", "max\\(m.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(m\\)", "m");

			// 每次加载500条
			List<Message> records = null;
			Long processCount = 0L;
			int pageStep = 500;
			int pageIndex = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++pageIndex;

					sheet = writableWorkbook
							.createSheet("Sheet" + pageIndex, pageIndex);
					// 设置excel列名
					for (int i = 0; i < COL_HEADERS.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 1, COL_HEADERS[i]));
					}

					// 设置excel标题
					sheet.mergeCells(0, 0, COL_HEADERS.length - 1, 0);

					// 应该有开始和结束时间的显示
					sheet.addCell(new jxl.write.Label(0, 0, "历史短信", cellFormat));
				}

				String useSql = downLoadSearchTmpSql + " and m.id <= " + recordId + " order by m.id desc";
				records = messagesManageService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < records.size(); i++) {
					Message record = records.get(i);
					
					String sendtime = "";
					if(record.getTime() != null) {
						sendtime = SDF.format(record.getTime());
					}
					
					String id = record.getId()+"";

					String phoneNo = "";
					if(record.getPhoneNumber() != null) {
						phoneNo = record.getPhoneNumber();
					}

					String content = "";
					if(record.getContent() != null) {
						content = record.getContent();
					}
					
					String status = "";
					if(record.getMessageType() != null) {
						status = record.getMessageType().getName();
					}
					
					String senderInfo = "";
					if(record.getUser() != null) {
						User sender = record.getUser();
						senderInfo = sender.getEmpNo();
						if(sender.getRealName() != null) {
							senderInfo = senderInfo + " - " + sender.getRealName();
						}
					}

					int index = 0;
					sheet.addCell(new jxl.write.Label(index, cursor + i, sendtime));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, id));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, phoneNo));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, content));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, status));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, senderInfo));
				}
			} while (records.size() > 0);

			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
			writableWorkbook.close();

			// 文件创建好后，下载文件
			downloadFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			// 导出完成之后是导出按钮可用
			exportLogExcel.setEnabled(true);
			progressLayout.setVisible(false);
			logger.error("jrh 管理员导出历史短信出现异常!" + e.getMessage(), e);
			this.getApplication().getMainWindow().showNotification("历史短信导出失败，请重试!", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	/**
	 * jrh
	 * 文件创建好之后，创建文件
	 * 
	 * @param file
	 */
	private void downloadFile(File file) {
		// 下载报表
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if (downloader.getParent() != null) {
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("phoneNumber")) {
				Message message = (Message) itemId;
				String telephone = message.getPhoneNumber();
				return new TelephoneNumberInTableLabelStyle(telephone, isEncryptMobile);
			} 
			return "";
		}
	}
	
	// ========================== Setter and Getter =====================//

	public Message getSelectedMsg() {
		return selectedMsg;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
