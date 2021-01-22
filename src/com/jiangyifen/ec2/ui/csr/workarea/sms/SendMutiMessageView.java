package com.jiangyifen.ec2.ui.csr.workarea.sms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.sms.SmsUtil;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
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
import com.vaadin.ui.themes.BaseTheme;

/**
 * 话务员发送短信界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class SendMutiMessageView extends VerticalLayout implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss_SSS");
	private final DecimalFormat DF = new DecimalFormat("0");	// 格式化 number String 字符  

	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息

	// 收信人相关组件
	private ListSelect receivers_ls;								// 收信人-电话成员显示区
	private TextField phoneNo_tf;									// 添加电话输入区
	private Button add_button;										// 添加按钮
	private Button delete_button;									// 删除按钮
	
	// 短信内容及发送相关组件
	private ComboBox template_cb;									// 短信模板选择框
	private TextArea content_ta;									// 内容输入框
	private Button send_button;										// 发送按钮

	private HorizontalLayout progressLayout;						// 上传进度条显示布局
	private ProgressIndicator pi;									// 进度条组件
	private Upload uploadFile_ul;									// 上传文件按钮
	private Button cancelUpload_bt;									// 取消上传按钮
	private File phoneFile;											// 电话号码源文件
	private String failedReason;									// 文件上传失败原因

	private Embedded downloader; 									// 存放数据的组件
	private Button downloadTxt_bt;									// 下载Txt 模板文件
	private Button downloadExcel2003_bt;							// 下载Excel2003 模板文件
	private Button downloadExcel2007_bt;							// 下载Excel2007 模板文件
	
	private BeanItemContainer<String> phoneNosContainer;			// 存放电话号码的容器
	private BeanItemContainer<MessageTemplate> templateContainer;	// 存放短信模板的容器
	
	private User loginUser;											// 当前登陆用户
	private RoleType roleType;										// 调用该模块的用户使用的角色类型
	private String exten;											// 当前登陆用户使用的分机
	private Domain domain;											// 当前登陆用户所属域
	private MessageTemplateService messageTemplateService;			// 模板短信服务类
	
	public SendMutiMessageView() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		
		roleType = SpringContextHolder.getRoleType();
		loginUser = SpringContextHolder.getLoginUser();
		exten = SpringContextHolder.getExten();
		domain = SpringContextHolder.getDomain();
		
		messageTemplateService = SpringContextHolder.getBean("messageTemplateService");

		phoneNosContainer = new BeanItemContainer<String>(String.class);
		templateContainer = new BeanItemContainer<MessageTemplate>(MessageTemplate.class);
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		this.addComponent(mainLayout);
		
		// 创建管理收信人电话号的组件
		mainLayout.addComponent(createPhoneNosComponents());
		
		// 创建短信内容填写组件
		VerticalLayout contentComponents = createContentComponents();
		mainLayout.addComponent(contentComponents);
		mainLayout.setExpandRatio(contentComponents, 1.0f);
		
		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setSpacing(true);
		bottomLayout.setWidth("100%");
		this.addComponent(bottomLayout);
		
		this.createUploadComponents(bottomLayout);
		
		send_button = new Button("发 送", this);
		send_button.setStyleName("default");
		send_button.setIcon(ResourceDataCsr.phone_message_send_16_ico);
		bottomLayout.addComponent(send_button);
		bottomLayout.setComponentAlignment(send_button, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 创建管理收信人电话号的组件
	 * @return VerticalLayout
	 */
	private VerticalLayout createPhoneNosComponents() {
		VerticalLayout phoneNos_l = new VerticalLayout();
		phoneNos_l.setWidth("-1px");
		phoneNos_l.setSpacing(true);
		phoneNos_l.setMargin(false, true, false, false);
		
		// 按钮组件
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setHeight("28px");
		operator_l.setSpacing(true);
		phoneNos_l.addComponent(operator_l);

		// 添加可以输入的组件
		phoneNo_tf = new TextField();
		phoneNo_tf.setInputPrompt("请输入正确的手机号码");
		phoneNo_tf.setImmediate(true);
		phoneNo_tf.setMaxLength(11);
		phoneNo_tf.setWidth("150px");
		phoneNo_tf.setDescription("<B>号码不能为空，并且只能是数字，长度为5-11位</B>");
		phoneNo_tf.addValidator(new RegexpValidator("\\d{5,11}", "号码不能为空，并且只能由5-11位的数字组成"));
		phoneNo_tf.setValidationVisible(false);
		operator_l.addComponent(phoneNo_tf);

		add_button = new Button("添加", this);
		operator_l.addComponent(add_button);

		delete_button = new Button("删除", this);
		operator_l.addComponent(delete_button);

		// 收信人电话号
		receivers_ls = new ListSelect("收信人列表：");
		receivers_ls.setContainerDataSource(phoneNosContainer);
		receivers_ls.setNullSelectionAllowed(false);
		receivers_ls.setMultiSelect(true);
		receivers_ls.setWidth("300px");
		receivers_ls.setHeight("300px");
		receivers_ls.setDescription("<B>可以按住'Ctrl'或'Shift'键，一次性 选择多个成员，进行删除！</B>");
		phoneNos_l.addComponent(receivers_ls);
		
		return phoneNos_l;
	}

	/**
	 * 创建短信内容填写组件
	 * @return
	 */
	private VerticalLayout createContentComponents() {
		VerticalLayout content_l = new VerticalLayout();
		content_l.setWidth("100%");
		content_l.setSpacing(true);
	
		// 按钮组件
		HorizontalLayout template_l = new HorizontalLayout();
		template_l.setSpacing(true);
		template_l.setHeight("28px");
		content_l.addComponent(template_l);
		
		Label templateCaption = new Label("使用模板：");
		templateCaption.setWidth("-1px");
		template_l.addComponent(templateCaption);
		
		template_cb = new ComboBox();
		template_cb.setWidth("200px");
		template_cb.setImmediate(true);
		template_cb.addListener(this);
		template_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		template_cb.setInputPrompt("请选择指定短信模板");
		template_cb.setDescription("<B>如果不使用模板，可以手动输入短信内容<B>");
		template_cb.setItemCaptionPropertyId("title");
		template_cb.setContainerDataSource(templateContainer);
		template_l.addComponent(template_cb);
		
		content_ta = new TextArea();
		content_ta.setHeight("300px");
		content_ta.setCaption("短信内容：");
		content_ta.setDescription("<B>短信内容不能为空!</B>");
		content_ta.setNullRepresentation("");
		content_ta.setWidth("100%");
		content_l.addComponent(content_ta);
		
		return content_l;
	}

	/**
	 * 创建上传文件组件
	 * @param bottomLayout
	 */
	private void createUploadComponents(HorizontalLayout bottomLayout) {
		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		bottomLeftLayout.setSpacing(true);
		bottomLayout.addComponent(bottomLeftLayout);

		uploadFile_ul = new Upload();
		uploadFile_ul.setImmediate(true);
		uploadFile_ul.setButtonCaption("从文件中提取号码");
		bottomLeftLayout.addComponent(uploadFile_ul);

		// 为上传按钮添加接收器
		this.addReceiverToUpLoad(uploadFile_ul);
		
		// 为上传按钮添加监听器
		this.addListenersToUpload(uploadFile_ul);
		
		// 创建进度组件
		progressLayout = new HorizontalLayout();
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		bottomLeftLayout.addComponent(progressLayout);
		
		cancelUpload_bt = new Button("取消上传", this);
		progressLayout.addComponent(cancelUpload_bt);

		Label piLabel = new Label("上传进度：");
		piLabel.setWidth("-1px");
		progressLayout.addComponent(piLabel);

		pi = new ProgressIndicator();
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);
		
		// 创建下载模板文件按钮
		HorizontalLayout downloadModel_hl = createPhoneModelFile();
		bottomLeftLayout.addComponent(downloadModel_hl);
		bottomLeftLayout.setComponentAlignment(downloadModel_hl, Alignment.MIDDLE_LEFT);
	}

	/**
	 * 创建下载模板文件按钮
	 * @return
	 */
	private HorizontalLayout createPhoneModelFile() {
		HorizontalLayout downloadLayout = new HorizontalLayout();
		downloadLayout.setSpacing(true);
		
		Label downloadFile_lb = new Label("下载模板文件：");
		downloadFile_lb.setWidth("-1px");
		downloadLayout.addComponent(downloadFile_lb);
		downloadLayout.setComponentAlignment(downloadFile_lb, Alignment.MIDDLE_LEFT);
		
		downloadTxt_bt = new Button("Txt 文件",this);
		downloadTxt_bt.setStyleName(BaseTheme.BUTTON_LINK);
		downloadLayout.addComponent(downloadTxt_bt);
		
		downloadExcel2003_bt = new Button("Excel 2003 文件",this);
		downloadExcel2003_bt.setStyleName(BaseTheme.BUTTON_LINK);
		downloadLayout.addComponent(downloadExcel2003_bt);
		
		downloadExcel2007_bt = new Button("Excel 2007 文件",this);
		downloadExcel2007_bt.setStyleName(BaseTheme.BUTTON_LINK);
		downloadLayout.addComponent(downloadExcel2007_bt);

		Label description_lb = new Label("[说明：txt 一行一个号码、excel 一个单元格一个号码]");
		description_lb.setWidth("-1px");
		downloadLayout.addComponent(description_lb);
		downloadLayout.setComponentAlignment(description_lb, Alignment.MIDDLE_LEFT);

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
		
		return downloadLayout;
	}
	
	/**
	 * 为上传按钮添加接收器
	 * 
	 * @param upload
	 */
	private void addReceiverToUpLoad(final Upload upload) {
		upload.setReceiver(new Receiver() {
			private OutputStream os = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					uploadFile_ul.interruptUpload();
				}
			};
			
			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				if (!(filename.endsWith(".txt") || filename.endsWith(".xls") || filename.endsWith(".xlsx"))) {
					failedReason = "导入文件只能是文本或表格文件(Txt、xls、xlsx)!!";
					return os;
				}
				
				FileOutputStream fos = null;
				String userName = loginUser.getUsername();
				String dateStr = SDF_SEC.format(new Date());

				filename = ConfigProperty.PATH + "/" + dateStr + "_" + userName+ "_" + filename;
				phoneFile = new File(filename);
				
				if (!phoneFile.exists()) {
					if (!phoneFile.getParentFile().exists()) {
						phoneFile.getParentFile().mkdirs();
					}
					try {
						phoneFile.createNewFile();
					} catch (IOException e) {
						logger.error("创建文件失败:"+filename,e);
						throw new RuntimeException("无法再指定位置创建新文件！");
					}
				} else {
					throw new RuntimeException("文件已经存在，请重新创建！");
				}
				try {
					fos = new FileOutputStream(phoneFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();//应该不会出现
					logger.error("jrh 文件创建失败！----》"+e.getMessage(), e);
				}
				return fos;
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
				failedReason = "";
				pi.setValue(0f);
				pi.setPollingInterval(1000);
				pi.setDescription("正在上传文件：" + event.getFilename());
			}
		});

		upload.addListener(new ProgressListener() {
			@Override
			public void updateProgress(long readBytes, long contentLength) {
				if(contentLength > 1024*500) {
					uploadFile_ul.interruptUpload();
					if(!"".equals(failedReason)) {
						failedReason = failedReason + "，以及，上传的文件不能大于500K!!";
					} else {
						failedReason = "上传的文件不能大于500K!!";
					}
				}
				pi.setValue(new Float(readBytes / (float) contentLength));
			}
		});

		upload.addListener(new SucceededListener() {
			@Override
			public void uploadSucceeded(SucceededEvent event) {
				if(phoneFile != null) {
					long count =  0;
					String fileName = phoneFile.getName();
					try {
						if(fileName.endsWith(".txt")) {			// 处理文本文件
							count = analyzeTxtFile();
						} else if(fileName.endsWith(".xls")) {	// 处理表格文件
							count = analyzeExcel2003File();
						} else if(fileName.endsWith(".xlsx")) {	// 处理表格文件
							count = analyzeExcel2007File();
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("jrh 读取表格中的电话号码失败！----》"+e.getMessage(), e);
					}
					
					phoneFile.delete();	// 取完号码后删除
					upload.getApplication().getMainWindow().showNotification("成功从文件中提取 " +count+ " 个号码!");
				}
			}
		});

		upload.addListener(new FailedListener() {
			@Override
			public void uploadFailed(FailedEvent event) {
				upload.getApplication().getMainWindow().showNotification("文件上传失败! 失败原因：" + failedReason, Notification.TYPE_WARNING_MESSAGE);
			}
		});

		// This method gets called always when the upload finished, either succeeding or failing
		upload.addListener(new FinishedListener() {
			@Override
			public void uploadFinished(FinishedEvent event) {
				progressLayout.setVisible(false);
				upload.setVisible(true);
			}
		});
	}
	
	/**
	 * 解析Txt 文本文件  取出电话号码
	 * @param count	成功取得的号码数量
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private long analyzeTxtFile() throws FileNotFoundException, IOException {
		long count = 0;
		
		FileReader fr = new FileReader(phoneFile);
		BufferedReader br = new BufferedReader(fr);
		
		String s = null ; 
		while( (s=br.readLine()) != null) { 
			String phoneNo = pickOutPhoneNo(s);
			if(!"".equals(phoneNo)) {
				count++;
				phoneNosContainer.addBean(phoneNo);
			}
		} 
		br.close(); 
		fr.close();
		return count;
	}
	
	/**
	 * 解析Excel2003 文件  取出电话号码
	 * @param count	成功取得的号码数量
	 * @return
	 * @throws IOException
	 * @throws BiffException
	 */
	private long analyzeExcel2003File() throws IOException, BiffException {
		long count =  0;
		jxl.Workbook book = jxl.Workbook.getWorkbook(phoneFile);
		jxl.Sheet[] sheets = book.getSheets();

		for (int i = 0; i < sheets.length; i++) {
			jxl.Sheet sheet = sheets[i];
			for(int row = 0; row < sheet.getRows(); row++) {
				for(int col = 0; col < sheet.getColumns(); col++) {
					String content = StringUtils.trimToEmpty(sheet.getCell(col, row).getContents());
					String phoneNo = pickOutPhoneNo(content);
					if(!"".equals(phoneNo)) {
						count++;
						phoneNosContainer.addBean(phoneNo);
					}
				}
			}
		}
		return count;
	}
	
	/**
	 * 解析Excel2007 文件  取出电话号码
	 * @param count	成功取得的号码数量
	 * @return
	 * @throws FileNotFoundException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	private long analyzeExcel2007File() throws FileNotFoundException, InvalidFormatException,
			IOException {
		long count =  0;
		
		InputStream is = new FileInputStream(phoneFile);
		OPCPackage opc = OPCPackage.open(is);
		XSSFWorkbook xwb = new XSSFWorkbook(opc);
		
		int sheetCount = xwb.getNumberOfSheets();
		for (int i = 0; i < sheetCount; i++) {
			XSSFSheet sheet = (XSSFSheet) xwb.getSheetAt(i);
			for (int r = sheet.getFirstRowNum(); r < sheet
					.getPhysicalNumberOfRows(); r++) {
				XSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				for (int c = row.getFirstCellNum(); c <= row.getLastCellNum(); c++) {
					XSSFCell cell = row.getCell(c);
					if (cell == null) {
						continue;
					}
					Object value = null;
					switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case XSSFCell.CELL_TYPE_NUMERIC:
							value = DF.format(cell.getNumericCellValue());
							break;
						case XSSFCell.CELL_TYPE_BOOLEAN:
							value = cell.getBooleanCellValue();
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = "";
							break;
						default:
							value = cell.toString();
					}
					String content = StringUtils.trimToEmpty(value.toString());
					String phoneNo = pickOutPhoneNo(content);
					if(!"".equals(phoneNo)) {
						count++;
						phoneNosContainer.addBean(phoneNo);
					}
				}
			}
		}
		
		return count;
	}
	
    /**
     * 取出文件中的电话号码，如原始数据位 ‘ 135&%￥1676jrh0398  ’, 执行后返回 13516760398
     * @param originalData     原始数据
     * @return
     */
    public String pickOutPhoneNo(String originalData) {
          String phoneNoStr = "";
          originalData = StringUtils.trimToEmpty(originalData);
          for(int i = 0; i < originalData.length(); i++) {
               char c = originalData.charAt(i);
               int assiiCode = (int) c;
               if(assiiCode >= 48 && assiiCode <= 57) {
                    phoneNoStr = phoneNoStr + c;
               }
          }
          while(phoneNoStr.startsWith("0")) {
        	  phoneNoStr = phoneNoStr.substring(1);
          }
          if(!phoneNoStr.startsWith("1") || !phoneNoStr.matches("\\d{5,11}")) {
        	  phoneNoStr = ""; 
          }
          return phoneNoStr;
    }

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == template_cb) {
			MessageTemplate template = (MessageTemplate) template_cb.getValue();
			if(template == null) {
				content_ta.setValue("");
			} else {
				content_ta.setValue(template.getContent());
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == add_button) {
			executeAdd();
		} else if(source == delete_button) {
			executeDelete();
		} else if(source == send_button) {
			try {
				executeSend();
			} catch (Exception e) {
				logger.error("jrh 执行发送短信，出现异常 -----> "+e.getMessage(), e);
				warning_notification.setCaption("短信发送失败！！！原因："+e.getMessage());
				this.getApplication().getMainWindow().showNotification(warning_notification);
			}
		} else if (source == cancelUpload_bt) {
			uploadFile_ul.interruptUpload();
		} else if(source == downloadTxt_bt) {
			executeDownloadTxt();
		} else if(source == downloadExcel2003_bt) {
			executeDownloadExcel2003();
		} else if(source == downloadExcel2007_bt) {
			executeDownloadExcel2007();
		}
	}

	/**
	 * 执行添加操作
	 */
	private void executeAdd() {
		//检查输入是否合法
		String phoneNumber = StringUtils.trimToEmpty((String)phoneNo_tf.getValue());
		if("".equals(phoneNumber) || !phoneNo_tf.isValid()) {
			warning_notification.setCaption("号码不能为空，并且只能是数字，长度为5-11位");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		phoneNosContainer.addBean(phoneNumber);
		phoneNo_tf.setValue("");
	}
	
	/**
	 * 执行删除手机成员操作
	 */
	@SuppressWarnings("unchecked")
	private void executeDelete() {
		Set<String> phoneNos = (Set<String>) receivers_ls.getValue();
		if(phoneNos.size() == 0) {
			warning_notification.setCaption("请先选中一个手机成员后再删");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		for(String phoneNo : phoneNos) {
			phoneNosContainer.removeItem(phoneNo);
		}
	}

	/**
	 *  进行发送短息的操作
	 */
	private void executeSend() {
		// 验证信息
		final List<String> reciverPhoneNos = new ArrayList<String>(phoneNosContainer.getItemIds());
		if(reciverPhoneNos.size() == 0) {
			warning_notification.setCaption("收信人列表不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}

		final String content = StringUtils.trimToEmpty((String) content_ta.getValue()).trim();
		if("".equals(content)) {
			warning_notification.setCaption("短信模板内容不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		final Window mainWindow = this.getApplication().getMainWindow();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 发送短信
				String result = SmsUtil.sendSMS(loginUser, exten, domain.getId(), reciverPhoneNos, content);
				if(!result.contains("成功")) {
					warning_notification.setCaption("短信发送失败！！！原因："+result);
					mainWindow.showNotification(warning_notification);
					return;
				}
				
				// 清空内容
				phoneNosContainer.removeAllItems();
				template_cb.setValue(null);
				success_notification.setCaption(result);
				mainWindow.showNotification(success_notification);
			}
		}).start();
	}

	
	/**
	 * 下载 txt 2007 号码模板文件
	 */
	private void executeDownloadTxt() {
		File file = null;
		try {
			file = new File(new String(("号码模板txt文件.txt").getBytes("GBK"), "ISO-8859-1"));
			FileWriter fw = new FileWriter(file); 
			PrintWriter out = new PrintWriter(fw);
			
            out.append("13802458171");
            out.append("\r\n");
            out.append("13802458172");
            out.append("\r\n");
            out.append("13802458178");
            out.append("\r\n");
            out.append("15002459570");
            out.append("\r\n");
            
            out.close(); 
            fw.close();
            
            // 文本文件没法直接下载，需要压缩后处理
            file = zipFile(file);
            
			this.downloadFile(file);	// 文件创建好后，下载文件
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("创建样例Txt文件失败，请重试！");
		} 

	}

	/**
	 * 下载excel 2003 号码模板文件
	 */
	private void executeDownloadExcel2003() {
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			file = new File(new String(("号码模板Excel2003文件.xls").getBytes("GBK"), "ISO-8859-1"));
			writableWorkbook = jxl.Workbook.createWorkbook(file);
			WritableSheet sheet = writableWorkbook.createSheet("Sheet0", 0);
			
			for(int r = 0; r < 10; r++) {
				for(int c = 0; c < 2; c++) {
					sheet.addCell(new jxl.write.Label(c, r, randomPhoneNumber()));
				}
			}
			// JXL 只能写一次,不然出问题
			writableWorkbook.write();

			this.downloadFile(file);	// 文件创建好后，下载文件
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建样例Excel文件失败，请重试！");
		} finally {
			if (writableWorkbook != null) {
				try {
					writableWorkbook.close();
				} catch (Exception e1) {
					logger.info("jrh 下载Excel2003文件出错,关闭要下载的Excel文件失败！");
				}
			}
		}
	}

	/**
	 * 下载excel 2007 号码模板文件
	 */
	private void executeDownloadExcel2007() {
		File excel07 = null;
		FileOutputStream fileOut = null;
		XSSFWorkbook wb = null;
		try {
			String fileName = new String(("号码模板Excel2007文件.xlsx").getBytes("GBK"), "ISO-8859-1"); 
			excel07 = new File(fileName); 
			
			wb = new XSSFWorkbook();	//创建工作簿
			XSSFSheet sheet = wb.createSheet(new String(("号码模板Excel2007文件.xlsx").getBytes("GBK"), "ISO-8859-1"));//创建sheet 

			for(int r = 0; r < 10; r++) {
				Row row = sheet.createRow(r);  
				for(int c = 0; c < 2; c++) {
					Cell cell = row.createCell(c); 
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);//设置单元格 格式为 字符串  
					cell.setCellValue(randomPhoneNumber());
				}
			}
			
			fileOut = new FileOutputStream(excel07);
			wb.write(fileOut);
			this.downloadFile(excel07);	// 文件创建好后，下载文件
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建样例Excel文件失败，请重试！");
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (Exception e1) {
					logger.info("jrh 下载Excel2007文件出错,关闭要下载的Excel文件失败！");
				}
			}
		}
	}

	/**
	 * 随机创建一个电话号码
	 * @return
	 */
	private String randomPhoneNumber() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("138");
		Random random = new Random();
		for(int i = 0; i < 8; i++) {
			buffer.append(random.nextInt(10));
		}
		return buffer.toString();
	}

	/**
	 * 压缩文件
	 * 
	 * @param sourceFile
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private File zipFile(File sourceFile) throws Exception {
		String filename = new String(sourceFile.getName().getBytes("ISO-8859-1"), "GBK");
		FileOutputStream fos = new FileOutputStream(sourceFile.getName()+ ".zip");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);// 压缩包
		ZipEntry ze = new ZipEntry(filename);// 这是压缩包名里的文件名
		zos.putNextEntry(ze);// 写入新的 ZIP 文件条目并将流定位到条目数据的开始处

		FileInputStream fis = new FileInputStream(sourceFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = bis.read(buf)) != -1) {
			zos.write(buf, 0, len);
			zos.flush();
		}
		bis.close();
		zos.close();
		return new File(sourceFile.getAbsolutePath() + ".zip");
	}
	
	/**
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
	 * 刷新模板容器中的对象
	 */
	public void refreshTemplates() {
		templateContainer.removeAllItems();
		List<MessageTemplate> templates = new ArrayList<MessageTemplate>();
		// 话务员只能使用自己建的模板，管理员可以是用所有模板
		if(roleType.equals(RoleType.csr)) {
			templates = messageTemplateService.getMessagesByCreator(loginUser);
			templates.addAll(messageTemplateService.getAllByType(MessageTemplateType.system, domain.getId()));
		} else {
			templates = messageTemplateService.getAllByDomain(domain);
		}
		templateContainer.addAll(templates);
	}
}
