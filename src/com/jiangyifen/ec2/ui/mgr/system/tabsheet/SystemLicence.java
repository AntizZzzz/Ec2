package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.SerialNumber;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.license.LicenseManager;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.admin.index.OperatorLoginWindow;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.StateVisiableVaraible;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SystemLicence extends VerticalLayout implements Button.ClickListener {

	private Map<String, String> cmdMap = new HashMap<String, String>();

	private final String VALUE_COL_COLOR = "blue"; // grid 布局的信息内容列 的颜色配置
	private final String DESCRIPTION_COL_COLOR = "#608525"; // grid 布局的描述信息列
															// 的颜色配置

	private GridLayout licence_glo;

	/**
	 * 序列号 (年月日-xxxx-xxxx)xxxx 表示随机四位数
	 */
	public static String serialNumber;

	private static CommonService commonService;

	private Label csrAmountValue_lb;
	private Label expiredDateValue_lb;
	private Button operatorBtn; // 操作员用的按钮

	private Button btnShowOperatePanel;
	private Panel operatePanel;
	
	private Button btnRestartOS;
	private Button btnPowerOffOS;
	private Button btnRestartTomcat;
	
	private AllowOperateWindow allowOperateWindow;
	private RestartOSWindow restartOSWindow;
	private PowerOffOSWindow powerOffOSWindow;
	private RestartTomcatWindow restartTomcatWindow;
	
	private User loginUser;
	private Domain domain;
	
	// lc 加上序列号
	static {
		
		commonService = SpringContextHolder.getBean("commonService");
		List<SerialNumber> list = commonService.getEntitiesByJpql("select s from SerialNumber as s");

		if (list.size() != 0) {
			serialNumber = list.get(0).getSerialNumber();
		} else {
			serialNumber = new SimpleDateFormat("yyyy-MMdd").format(new Date()) + "-" + (new Random().nextInt(8999) + 1000) + "-" + (new Random().nextInt(8999) + 1000);
			SerialNumber number = new SerialNumber();
			number.setSerialNumber(serialNumber);
			// 保存
			commonService.save(number);
		}
	}

	public SystemLicence() {
		this.setMargin(true);
		this.setSizeUndefined();

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		cmdMap.put("reboot", "");
		cmdMap.put("poweroff", "");
		cmdMap.put("restart-tomcat", "");

		licence_glo = new GridLayout(3, 5);
		licence_glo.setMargin(true);
		licence_glo.setSpacing(true);
		licence_glo.setSizeUndefined();
		this.addComponent(licence_glo);

		int row_index = 0;
		int column_index = 0;

		// 表头
		Label key_lb = new Label("<b>信息</b>", Label.CONTENT_XHTML);
		key_lb.setWidth("-1px");
		licence_glo.addComponent(key_lb, (column_index++), row_index);
		licence_glo.setComponentAlignment(key_lb, Alignment.MIDDLE_LEFT);

		Label value_lb = new Label("<font color='blue'>信息内容</font>", Label.CONTENT_XHTML);
		value_lb.setWidth("-1px");
		licence_glo.addComponent(value_lb, (column_index++), row_index);
		licence_glo.setComponentAlignment(value_lb, Alignment.MIDDLE_LEFT);

		Label description_lb = new Label("<font color='" + DESCRIPTION_COL_COLOR + "'>描述</font>", Label.CONTENT_XHTML);
		description_lb.setWidth("-1px");
		licence_glo.addComponent(description_lb, (column_index++), row_index);
		licence_glo.setComponentAlignment(description_lb, Alignment.MIDDLE_LEFT);

		// 修改grid 中组件需要添加到的行、列号
		row_index++;
		column_index = 0;

		// 最大坐席在线人数
		Label csrAmount_lb = new Label("<b>坐席总数：</b>", Label.CONTENT_XHTML);
		csrAmount_lb.setWidth("-1px");
		licence_glo.addComponent(csrAmount_lb, (column_index++), row_index);

		csrAmountValue_lb = new Label("<font color='" + VALUE_COL_COLOR + "'>" + 0 + "</font>", Label.CONTENT_XHTML);
		csrAmountValue_lb.setWidth("-1px");
		licence_glo.addComponent(csrAmountValue_lb, (column_index++), row_index);

		Label csrAmountDescription_lb = new Label("<font color='" + DESCRIPTION_COL_COLOR + "'>同时可以在线的最大坐席数!!</font>", Label.CONTENT_XHTML);
		csrAmountDescription_lb.setWidth("-1px");
		licence_glo.addComponent(csrAmountDescription_lb, (column_index++), row_index);

		// 修改grid 中组件需要添加到的行、列号
		row_index++;
		column_index = 0;

		// 过期时间
		Label expiredDate_lb = new Label("<b>过期时间：</b>", Label.CONTENT_XHTML);
		expiredDate_lb.setWidth("-1px");
		licence_glo.addComponent(expiredDate_lb, (column_index++), row_index);

		expiredDateValue_lb = new Label("<font color='" + VALUE_COL_COLOR + "'>invalid</font>", Label.CONTENT_XHTML);
		expiredDateValue_lb.setWidth("-1px");
		licence_glo.addComponent(expiredDateValue_lb, (column_index++), row_index);

		Label expiredDateDescription_lb = new Label("<font color='" + DESCRIPTION_COL_COLOR + "'>当过了当前时间后，用户将无法登陆系统!!</font>", Label.CONTENT_XHTML);
		expiredDateDescription_lb.setWidth("-1px");
		licence_glo.addComponent(expiredDateDescription_lb, (column_index++), row_index);

		// 修改grid 中组件需要添加到的行、列号
		row_index++;
		column_index = 0;

		// 序列号
		Label licence_lb = new Label("<b>序&nbsp; 列&nbsp; 号：</b>", Label.CONTENT_XHTML);
		licence_lb.setWidth("-1px");
		licence_glo.addComponent(licence_lb, (column_index++), row_index);

		Label licenceValue_lb = new Label("<font color='" + VALUE_COL_COLOR + "'>" + serialNumber + "</font>", Label.CONTENT_XHTML);
		// Label licenceValue_lb = new
		// Label("<font color='blue'>"+GlobalVariable.CONCURRENT_MAX_CSR+"</font>",
		// Label.CONTENT_XHTML);
		licenceValue_lb.setWidth("-1px");
		licence_glo.addComponent(licenceValue_lb, (column_index++), row_index);

		Label licenceDescription_lb = new Label("<font color='" + DESCRIPTION_COL_COLOR + "'>系统的唯一标识，找客服维护时请报序列号!!</font>", Label.CONTENT_XHTML);
		licenceDescription_lb.setWidth("-1px");
		licence_glo.addComponent(licenceDescription_lb, (column_index++), row_index);

		this.addComponent(updateLicenseComponent());
		// 更新坐席数和过期时间显示
		refreshLicenseInfo();
		
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE_SYSTEM_OPERATE)) {
			bulidOperatingSystem();
		}
	}

	/**
	 * added by chb 20140520
	 * 
	 * @return
	 */
	private VerticalLayout updateLicenseComponent() {
		VerticalLayout licenseUpdateLayout = new VerticalLayout();
		licenseUpdateLayout.setSpacing(true);
		final VerticalLayout textAreaPlaceHolder = new VerticalLayout();
		final HorizontalLayout buttonPlaceHolder = new HorizontalLayout();
		buttonPlaceHolder.setSpacing(true);

		// 组件
		final Button updateButton = new Button("更新License");
		updateButton.setData("show");

		// 组件
		final Button cancelButton = new Button("取消");

		// 组件
		final TextArea licenseTextArea = new TextArea();
		licenseTextArea.setColumns(30);
		licenseTextArea.setRows(5);
		licenseTextArea.setWordwrap(true);
		licenseTextArea.setInputPrompt("请输入有效License信息！");

		// 添加组件到Layout
		buttonPlaceHolder.addComponent(updateButton);

		operatorBtn = new Button("查看", this);
		operatorBtn.addStyleName("link");
		buttonPlaceHolder.addComponent(operatorBtn);
		if (StateVisiableVaraible.OPERATOR_SHOW_STATE) {
			operatorBtn.setVisible(true);
		} else {
			operatorBtn.setVisible(false);
		}

		licenseUpdateLayout.addComponent(textAreaPlaceHolder);
		licenseUpdateLayout.addComponent(buttonPlaceHolder);

		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE_SYSTEM_OPERATE)) {
			btnShowOperatePanel = new Button("显示重启命令功能",this);
			btnShowOperatePanel.setStyleName("btn-danger");
			licenseUpdateLayout.addComponent(btnShowOperatePanel);
		}
		
		// 逻辑
		cancelButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				textAreaPlaceHolder.removeAllComponents();
				buttonPlaceHolder.removeComponent(cancelButton);
				updateButton.setData("show");
				updateButton.setCaption("更新License");
				if (StateVisiableVaraible.OPERATOR_SHOW_STATE)
					operatorBtn.setVisible(true);
			}
		});

		updateButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (((String) event.getButton().getData()).equals("show")) {
					textAreaPlaceHolder.removeAllComponents();
					textAreaPlaceHolder.addComponent(licenseTextArea);
					buttonPlaceHolder.addComponent(cancelButton);
					event.getButton().setData("updateAndHide");
					event.getButton().setCaption("提交");
					operatorBtn.setVisible(false);
				} else if (((String) event.getButton().getData()).equals("updateAndHide")) {
					StringReader stringReader = new StringReader(StringUtils.trimToEmpty((String) licenseTextArea.getValue()));
					Properties props = new Properties();
					try {
						props.load(stringReader);
					} catch (IOException e) {
						e.printStackTrace();
					}
					stringReader.close();
					// Boolean
					// isValidvalidateLicense(licenseTextArea.getValue());
					String license_date = props.getProperty(LicenseManager.LICENSE_DATE);
					String license_count = props.getProperty(LicenseManager.LICENSE_COUNT);
					String license_localmd5 = props.getProperty(LicenseManager.LICENSE_LOCALMD5);
					// 检查
					Boolean isMatch = regexMatchCheck(license_date, license_count, license_localmd5);
					if (isMatch) {

						Map<String, String> licenseMap = new HashMap<String, String>();
						// 授权数量
						// String license_count =
						// (String)props.get(LicenseManager.LICENSE_COUNT);
						license_count = license_count == null ? "" : license_count;
						licenseMap.put(LicenseManager.LICENSE_COUNT, license_count);

						// 授权日期
						// String license_date =
						// (String)props.get(LicenseManager.LICENSE_DATE);
						license_date = license_date == null ? "" : license_date;
						licenseMap.put(LicenseManager.LICENSE_DATE, license_date);

						// 授权验证码
						// String license_localmd5 =
						// (String)props.get(LicenseManager.LICENSE_LOCALMD5);
						license_localmd5 = license_localmd5 == null ? "" : license_localmd5;
						licenseMap.put(LicenseManager.LICENSE_LOCALMD5, license_localmd5);

						// 验证License有效性
						Map<String, String> resultMap = LicenseManager.licenseValidate(licenseMap);

						String validateResult = resultMap.get(LicenseManager.LICENSE_VALIDATE_RESULT);
						if (LicenseManager.LICENSE_VALID.equals(validateResult)) {
							// continue
						} else {
							NotificationUtil.showWarningNotification(SystemLicence.this, "License 不是一个有效的License");
							return;
						}

						try {
							URL resourceurl = SystemLicence.class.getResource(LicenseManager.LICENSE_FILE);
							// System.err.println("chb: SystemLicense"+resourceurl.getPath());
							OutputStream fos = new FileOutputStream(resourceurl.getPath());
							props.store(fos, "license");
						} catch (Exception e) {
							e.printStackTrace();
							NotificationUtil.showWarningNotification(SystemLicence.this, "License 更新失败");
							return;
						}
						textAreaPlaceHolder.removeAllComponents();
						buttonPlaceHolder.removeComponent(cancelButton);
						updateButton.setData("show");
						updateButton.setCaption("更新License");
						if (StateVisiableVaraible.OPERATOR_SHOW_STATE)
							operatorBtn.setVisible(true);
						LicenseManager.loadLicenseFile(LicenseManager.LICENSE_FILE.substring(1));
						// LicenseManager.loadLicenseFile(licenseFilename)
						refreshLicenseInfo();
						NotificationUtil.showWarningNotification(SystemLicence.this, "License 更新成功,请退出系统重新登陆");
					} else {
						NotificationUtil.showWarningNotification(SystemLicence.this, "License 内容格式不正确");
					}
				}
			}

			/**
			 * 校验输入的license 格式
			 * 
			 * @param license_date
			 * @param license_count
			 * @param license_localmd5
			 * @return
			 */
			private Boolean regexMatchCheck(String license_date, String license_count, String license_localmd5) {
				if (StringUtils.isEmpty(license_date) || StringUtils.isEmpty(license_count) || StringUtils.isEmpty(license_localmd5)) {
					return false;
				}
				String date_regex = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$"; // 不精确
				String count_regex = "^\\d+$"; // 不精确
				String md5_32_regex = "^\\w{32}$"; // 不精确
				return license_localmd5.matches(md5_32_regex) && license_date.matches(date_regex) && license_count.matches(count_regex);
			}
		});

		return licenseUpdateLayout;
	}

	/**
	 * 更新组件显示的License信息
	 */
	private void refreshLicenseInfo() {
		// Label value_lb = new Label("<font color='blue'>信息内容</font>",
		// Label.CONTENT_XHTML);
		Map<String, String> licenseMap = LicenseManager.licenseValidate();
		String licenseCount = licenseMap.get(LicenseManager.LICENSE_COUNT);
		String licenseDate = licenseMap.get(LicenseManager.LICENSE_DATE);
		csrAmountValue_lb.setValue("<font color='" + VALUE_COL_COLOR + "'>" + licenseCount + "</font>");// ,
																										// Label.CONTENT_XHTML);
		expiredDateValue_lb.setValue("<font color='" + VALUE_COL_COLOR + "'>" + licenseDate + "</font>");// ,
																											// Label.CONTENT_XHTML);
		// Label licenceValue_lb = new
		// Label("<font color='"+VALUE_COL_COLOR+"'>"+serialNumber+"</font>",
		// Label.CONTENT_XHTML);
	}

	
	private void bulidOperatingSystem() {

		Label panelNull = new Label();
		panelNull.setWidth("100%");
		panelNull.setHeight("30px");
		this.addComponent(panelNull);

		operatePanel = new Panel();
		operatePanel.setSizeFull();
		operatePanel.setCaption("系统操作");
		operatePanel.setVisible(false);
		this.addComponent(operatePanel);

		HorizontalLayout btnToolLayout = new HorizontalLayout();
		btnToolLayout.setMargin(true);
		btnToolLayout.setSizeFull();

		operatePanel.addComponent(btnToolLayout);

		btnRestartOS = new Button("重启Linux操作系统", this);
		btnRestartOS.setStyleName("btn-danger");
		btnToolLayout.addComponent(btnRestartOS);

		btnPowerOffOS = new Button("关闭Linux服务器", this);
		btnPowerOffOS.setStyleName("btn-danger");
		btnToolLayout.addComponent(btnPowerOffOS);

		btnRestartTomcat = new Button("重启呼叫中心软件", this);
		btnRestartTomcat.setStyleName("btn-danger");
		btnToolLayout.addComponent(btnRestartTomcat);

	}

	/**
	 * 执行
	 * 
	 * @param cmd
	 */
	private void executeCommond(String cmd) {
		try {
			if (cmdMap != null && cmdMap.containsKey(cmd)) {
				saveDownloadOperationLog(domain,"",loginUser,"127.0.0.1","执行服务器命令"+cmd,"",commonService);//保存下载日式
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 保存下载日志
	 * @param domain	域名
	 * @param filePath	下载路径
	 * @param user		登录用户
	 * @param operateIp	操作者IP
	 * @param description	描述
	 * @param programmerSee	sql语句
	 * @param commonService	service
	 */
	private void saveDownloadOperationLog(Domain domain,String filePath,User user,String operateIp,String description,String programmerSee,CommonService commonService){
		try {
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			operationLog.setIp(ip);
			operationLog.setDescription(description);
			operationLog.setProgrammerSee(programmerSee);
			commonService.save(operationLog);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == operatorBtn) {
			if (StateVisiableVaraible.OPERATOR_SHOW_STATE) {
				this.getApplication().getMainWindow().addWindow(new OperatorLoginWindow());
			} else {
				operatorBtn.setVisible(false);
			}
		} else if (event.getButton() == btnRestartOS) {
			if(restartOSWindow == null){
				restartOSWindow = new RestartOSWindow();
			}
			this.getApplication().getMainWindow().removeWindow(restartOSWindow);
			this.getApplication().getMainWindow().addWindow(restartOSWindow);

		} else if (event.getButton() == btnPowerOffOS) {
			if(powerOffOSWindow == null){
				powerOffOSWindow = new PowerOffOSWindow();
			}
			this.getApplication().getMainWindow().removeWindow(powerOffOSWindow);
			this.getApplication().getMainWindow().addWindow(powerOffOSWindow);
			
		} else if (event.getButton() == btnRestartTomcat) {
			if(restartTomcatWindow == null){
				restartTomcatWindow = new RestartTomcatWindow();
			}
			this.getApplication().getMainWindow().removeWindow(restartTomcatWindow);
			this.getApplication().getMainWindow().addWindow(restartTomcatWindow);
		} else if(event.getButton() == btnShowOperatePanel){
			if(allowOperateWindow == null){
				allowOperateWindow = new AllowOperateWindow(this,loginUser);
			}
			this.getApplication().getMainWindow().removeWindow(allowOperateWindow);
			this.getApplication().getMainWindow().addWindow(allowOperateWindow);
			
		}
	}

	private class RestartOSWindow extends Window implements ClickListener {

		private Label lbText;
		private Button btnOk;
		private Button btnClose;

		public RestartOSWindow() {
			this.center();
			this.setModal(true);
			this.setResizable(false);
			this.setWidth("500px");
			this.setHeight("200px");
			this.setCaption("重启Linux操作系统");

			lbText = new Label("", Label.CONTENT_XHTML);
			lbText.setValue("<hr color='red'><table><tr><td width='80px'>描述：</td><td > <span style='color:red;font-weight:bold;'> 服务器将重新启动,所有通话将立即中断! <br>在此期间呼叫中心系统停止运行!!!</td></tr>   <tr><td>时间间隔：</td><td><span style='color:red;font-weight:bold;'>6分钟</td></tr>    <tr><td>危险级别：</td><td><span style='color:red;font-weight:bold;'>最高级!!!</td></tr></table>");

			this.addComponent(lbText);

			HorizontalLayout btnToolLayout = new HorizontalLayout();
			btnToolLayout.setMargin(true);
			btnToolLayout.setSpacing(true);
			this.addComponent(btnToolLayout);

			btnOk = new Button("确定执行", this);
			btnOk.setStyleName("btn-danger");
			btnToolLayout.addComponent(btnOk);

			btnClose = new Button("取 消", this);
			btnToolLayout.addComponent(btnClose);
		}

		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if (source == btnOk) {
				executeCommond("reboot");
			} else if (source == btnClose) {
				this.close();
			}
		}
	}

	private class PowerOffOSWindow extends Window implements ClickListener {

		private Label lbText;
		private Button btnOk;
		private Button btnClose;

		public PowerOffOSWindow() {
			this.center();
			this.setModal(true);
			this.setResizable(false);
			this.setWidth("500px");
			this.setHeight("200px");
			this.setCaption("关闭Linux服务器");

			lbText = new Label("", Label.CONTENT_XHTML);
			lbText.setValue("<hr color='red'><table><tr><td width='80px'>描述：</td><td > <span style='color:red;font-weight:bold;'>关闭Linux服务器,所有通话立即终止! <br>如需要开启服务器,请手动按开机键!!! <tr><td>时间间隔：</td><td><span style='color:red;font-weight:bold;'>2分钟</td></tr>    </td></tr> <tr><td>危险级别：</td><td><span style='color:red;font-weight:bold;'>最高级!!!</td></tr></table>");

			this.addComponent(lbText);

			HorizontalLayout btnToolLayout = new HorizontalLayout();
			btnToolLayout.setMargin(true);
			btnToolLayout.setSpacing(true);
			this.addComponent(btnToolLayout);

			btnOk = new Button("确定执行", this);
			btnOk.setStyleName("btn-danger");
			btnToolLayout.addComponent(btnOk);

			btnClose = new Button("取 消", this);
			btnToolLayout.addComponent(btnClose);
		}

		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if (source == btnOk) {
				executeCommond("poweroff");
			} else if (source == btnClose) {
				this.close();
			}
		}
	}

	private class RestartTomcatWindow extends Window implements ClickListener {

		private Label lbText;
		private Button btnOk;
		private Button btnClose;

		public RestartTomcatWindow() {
			this.center();
			this.setModal(true);
			this.setResizable(false);
			this.setWidth("500px");
			this.setHeight("200px");
			this.setCaption("重启呼叫中心软件");

			lbText = new Label("", Label.CONTENT_XHTML);
			lbText.setValue("<hr color='red'><table><tr><td width='80px'>描述：</td><td > <span style='color:red;font-weight:bold;'>呼叫中心软件将重新启动,当前通话不受影响。</td></tr>   <tr><td>时间间隔：</td><td><span style='color:red;font-weight:bold;'>5分钟</td></tr>   <tr><td>危险级别：</td><td><span style='color:red;font-weight:bold;'>最高级!!!</td></tr></table>");

			this.addComponent(lbText);

			HorizontalLayout btnToolLayout = new HorizontalLayout();
			btnToolLayout.setMargin(true);
			btnToolLayout.setSpacing(true);
			this.addComponent(btnToolLayout);

			btnOk = new Button("确定执行", this);
			btnOk.setStyleName("btn-danger");
			btnToolLayout.addComponent(btnOk);

			btnClose = new Button("取 消", this);
			btnToolLayout.addComponent(btnClose);
		}

		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if (source == btnOk) {
				executeCommond("restart-tomcat");
			} else if (source == btnClose) {
				this.close();
			}
		}
	}
	
	private class AllowOperateWindow extends Window implements ClickListener {

		private Label lbText;
		private PasswordField tfPw;
		private Label lbMsg;
		
		private Button btnOk;
		private Button btnClose;
		
		private SystemLicence systemLicence;
		
		public AllowOperateWindow(SystemLicence systemLicence,User loginUser) {
			this.center();
			this.setModal(true);
			this.setResizable(false);
			this.setWidth("400px");
			this.setHeight("120px");
			this.setCaption("获取允许");
			this.systemLicence = systemLicence;
			
			HorizontalLayout allowLayout = new HorizontalLayout();
			allowLayout.setMargin(false,false,false,false);
			allowLayout.setSpacing(true);
			this.addComponent(allowLayout);
			
			lbText = new Label("登录密码:", Label.CONTENT_XHTML);
			allowLayout.addComponent(lbText);
			
			tfPw = new PasswordField();
			allowLayout.addComponent(tfPw);
			
			lbMsg = new Label("<font color='red'>  *</font>", Label.CONTENT_XHTML);
			allowLayout.addComponent(lbMsg);
			
			HorizontalLayout btnToolLayout = new HorizontalLayout();
			btnToolLayout.setMargin(true,false,false,false);
			btnToolLayout.setSpacing(true);
			this.addComponent(btnToolLayout);

			btnOk = new Button("显示操作", this);
			btnOk.setStyleName("btn-danger");
			btnToolLayout.addComponent(btnOk);

			btnClose = new Button("取 消", this);
			btnToolLayout.addComponent(btnClose);
		}

		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if (source == btnOk) {
				if(loginUser.getPassword().equals(this.tfPw.getValue().toString().trim())){
					this.systemLicence.showOperatePanel();	
					this.close();
				}else{
					lbMsg.setValue("<font color='red'>密码错误</font>");
				}
			} else if (source == btnClose) {
				this.close();
			}
		}
	}

	public void showOperatePanel(){
		this.operatePanel.setVisible(true);
	}
}
