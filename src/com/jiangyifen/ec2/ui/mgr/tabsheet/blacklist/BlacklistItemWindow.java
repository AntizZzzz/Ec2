package com.jiangyifen.ec2.ui.mgr.tabsheet.blacklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.BlackListItemService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述： 黑名单添加或编辑窗口
 * 
 * @author  jrh
 * @date    2013年11月29日 上午10:48:02
 * @version v1.0.2
 */

@SuppressWarnings("serial")
public class BlacklistItemWindow extends Window implements ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String ALL_DIRECTION = "alldirection";			// 如果是新增黑名单，则默认类型选择“全部”

	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	private OptionGroup type_og;									// 黑名单类型（呼入、呼出、全部）
	private HorizontalLayout phoneNo_lo;							// 号码布局
	private TextField phoneNo_tf;									// 收信人电话
	private TextArea reason_ta;										// 内容
	private TwinColSelect outline_tcs;								// 需要指定的外线{当前黑面单是针对哪些外线的}
	// 上传文件组件
	private HorizontalLayout importBatchPathLayout;					// 上传Excel或Txt组件 
	private Upload upload;											// 上传Excel或Txt的组件
	private File file;												// 文件
	private Label lbl_filename;										// 用来文件名称的label
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	
	
	private Button save_button;										// 保存按钮
	private Button cancel_button;									// 取消按钮

	private BlacklistView blacklistView;							// 黑名单显示界面

	private Domain domain;											// 当前用户所属域
	private String oldType;											// 修改之前的类型
	private String oldPhoneNo;										// 修改之前的电话号码
	private ArrayList<Long> oldSelectedOutlineIds;					// 修改之前黑名单选择的外线对应的Id集合
	private boolean isAddNewBlacklist;								// 黑名单模板Tab 页的操作是否为添加新的黑名单操作
	private BlackListItem blacklistItem;							// 当前黑名单对象
	private BeanItemContainer<SipConfig> outlineContainer;
	private boolean isMutilAdd = false;							// 是否是批量添加
	private ArrayList<String> blackTelephoneList = new ArrayList<String>();	// 用来存放黑名单里的手机号码

	private SipConfigService sipConfigService; 
	private BlackListItemService blackListItemService;				// 黑名单管理服务类
	private User loginUser;
	
	public BlacklistItemWindow(BlacklistView blacklistView) {
		this.center();
		this.setResizable(false);
		this.blacklistView = blacklistView;

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		blackListItemService = SpringContextHolder.getBean("blackListItemService");
		
		outlineContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		// 创建表单中的主要组件
		windowContent.addComponent(createFieldComponents());
		
		// 创建保存、取消等按钮
		windowContent.addComponent(creatOperateComponents());
		
		progressLayout = new HorizontalLayout();
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
	}
	
	/**
	 *  创建表单中的主要组件
	 */
	private VerticalLayout createFieldComponents() {
		VerticalLayout field_vlo = new VerticalLayout();
		field_vlo.setSpacing(true);
		
		HorizontalLayout type_lo = new HorizontalLayout();
		type_lo.setSpacing(true);
		field_vlo.addComponent(type_lo);
		
		Label type_lb = new Label("类型：");
		type_lb.setWidth("-1px");
		type_lo.addComponent(type_lb);
		type_lo.setComponentAlignment(type_lb, Alignment.MIDDLE_LEFT);
		
		type_og = new OptionGroup();
		type_og.addItem(BlackListItem.TYPE_INCOMING);
		type_og.addItem(BlackListItem.TYPE_OUTGOING);
		type_og.addItem(ALL_DIRECTION);
		type_og.setItemCaption(BlackListItem.TYPE_INCOMING, "呼入");
		type_og.setItemCaption(BlackListItem.TYPE_OUTGOING, "呼出");
		type_og.setItemCaption(ALL_DIRECTION, "全部");
		type_og.setNullSelectionAllowed(false);
		type_og.addStyleName("threecol300");
		type_og.setWidth("350px");
		type_og.setRequired(true);
		type_og.setImmediate(true);
		type_lo.addComponent(type_og);
		
		// TODO 文件选择
		importBatchPathLayout = new HorizontalLayout();
		importBatchPathLayout.setSpacing(false);
		field_vlo.addComponent(importBatchPathLayout);
		Label upload_lb = new Label("文件：");
		upload_lb.setWidth("-1px");
		importBatchPathLayout.addComponent(upload_lb);
		importBatchPathLayout.setComponentAlignment(upload_lb, Alignment.MIDDLE_LEFT);
		upload_lb.setWidth("-1px");
		upload = new Upload();
		upload.setImmediate(true);
		upload.addStyleName("link");
		
		upload.setButtonCaption("选择文件(Excel\\Txt)");
		importBatchPathLayout.addComponent(upload);
		importBatchPathLayout.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
		upload.addListener(new Upload.SucceededListener() {
			@Override
			public void uploadSucceeded(SucceededEvent event) {
				new Thread(){
					@Override
					public void run(){
						Long startTime = System.currentTimeMillis();
						executeImport();
						Long endTime = System.currentTimeMillis();
						logger.info("追加数据耗时:"+(endTime-startTime)/1000+"秒");
					}
				}.start();
				progressLayout.setVisible(false);
				upload.setReadOnly(false);
			}
		});
		
		upload.setReceiver(new Upload.Receiver() {
			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				if(!filename.matches("\\w+\\.\\w+")){
					showNotificationMessage("文件名只能由数字或字母组成！", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("文件名只能由数字或字母组成！");
				}
				
				if(!(filename.endsWith(".txt") || filename.endsWith(".xls") || filename.endsWith(".xlsx"))){
					showNotificationMessage("上传格式错误，(只能上传txt,xls,xlsx格式的文件)！", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("上传格式错误，(只能上传txt,xls,xlsx格式的文件)！");
				}
				
				FileOutputStream fos = null;
				String userName = loginUser.getUsername();
				String dataStr = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
				filename = ConfigProperty.PATH+"/"+dataStr+"-"+userName+"_"+filename;
				file = new File(filename);
				if (!file.exists()) {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					} 
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						showNotificationMessage("无法再指定位置创建新Excel文件！", Notification.TYPE_WARNING_MESSAGE);
						throw new RuntimeException("无法再指定位置创建新Excel文件！");
					}
				} else {
					showNotificationMessage("Excel文件已经存在，请重新创建！", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("Excel文件已经存在，请重新创建！");
				}
				
				try {
					fos = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException("Excel文件无法读取或不存在！");
				}
				
				logger.info("成功上传Excel文件:" + filename);
				return fos;
			}
		});
		lbl_filename = new Label("", Label.CONTENT_XHTML);
		lbl_filename.setImmediate(true);
		lbl_filename.setWidth("-1px");
		importBatchPathLayout.addComponent(lbl_filename);
		
		
		// 电话号码
		phoneNo_lo = new HorizontalLayout();
		phoneNo_lo.setSpacing(true);
		field_vlo.addComponent(phoneNo_lo);
		
		Label phoneNo_lb = new Label("号码：");
		phoneNo_lb.setWidth("-1px");
		phoneNo_lo.addComponent(phoneNo_lb);
		phoneNo_lo.setComponentAlignment(phoneNo_lb, Alignment.MIDDLE_LEFT);
		
		phoneNo_tf = new TextField();
		phoneNo_tf.setRequired(true);
		phoneNo_tf.setMaxLength(12);
		phoneNo_tf.setDescription("<B>号码不能为空，并且只能由7-12位的数字组成</B>");
		phoneNo_tf.addValidator(new RegexpValidator("\\d{7,12}", "号码不能为空，并且只能由7-12位的数字组成"));
		phoneNo_tf.setNullRepresentation("");
		phoneNo_tf.setValidationVisible(false);
		phoneNo_tf.setWidth("350px");
		phoneNo_lo.addComponent(phoneNo_tf);

		HorizontalLayout reason_lo = new HorizontalLayout();
		reason_lo.setSpacing(true);
		field_vlo.addComponent(reason_lo);
		
		Label reason_lb = new Label("原因：");
		reason_lb.setWidth("-1px");
		reason_lo.addComponent(reason_lb);
		reason_lo.setComponentAlignment(reason_lb, Alignment.MIDDLE_LEFT);
		
		reason_ta = new TextArea();
		reason_ta.setRows(2);
		reason_ta.setMaxLength(512);
		reason_ta.setRequired(true);
		reason_ta.setDescription("<B>原因内容不能为空!</B>");
		reason_ta.setNullRepresentation("");
		reason_ta.setWidth("350px");
		reason_lo.addComponent(reason_ta);

		HorizontalLayout outline_lo = new HorizontalLayout();
		outline_lo.setSpacing(true);
		field_vlo.addComponent(outline_lo);
		
		Label outline_lb = new Label("外线：");
		outline_lb.setWidth("-1px");
		outline_lo.addComponent(outline_lb);
		outline_lo.setComponentAlignment(outline_lb, Alignment.MIDDLE_LEFT);
		
		outline_tcs = new TwinColSelect();
		outline_tcs.setNullSelectionAllowed(true);
		outline_tcs.setImmediate(true);
		outline_tcs.setWidth("350px");
		outline_tcs.setHeight("200px");
		outline_tcs.setMultiSelect(true);
		outline_tcs.setContainerDataSource(outlineContainer);
		outline_tcs.setItemCaptionPropertyId("name");
		outline_lo.addComponent(outline_tcs);
		
		return field_vlo;
	}
	
	/**
	 * TODO 执行导入数据操作
	 */
	public Boolean executeImport(){
		
		blackTelephoneList.clear();
		String filename = file.getName();
		importBatchPathLayout.setVisible(true);
		
		// 获取总行数
		Long totalRow = 0L;
		if(filename.lastIndexOf(".") != -1){	// 说明这个文件有类型
			String type = filename.substring(filename.lastIndexOf("."), filename.length());
			if(type.equals(".txt")){	// 文本格式
				try {
					InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"UTF-8");
					BufferedReader bufferedReader = new BufferedReader(reader);
					String lineTxt = null;
					while((lineTxt = bufferedReader.readLine()) != null){
						if(isMobile(lineTxt)){
							blackTelephoneList.add(lineTxt);
							totalRow ++;
						}
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
					showNotificationMessage("读取txt文件内容出错", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("读取txt文件内容出错");
				}
			} else if(type.equals(".xls")) {	// JXL只支持读取2003的Excel
				Workbook book = null;
				try {
					book = Workbook.getWorkbook(file);
				} catch (Exception e) {
					e.printStackTrace();
					showNotificationMessage("不能识别的文件格式，请选择Excel文件或Txt文件作为上传格式", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("不能识别的文件格式，请选择Excel文件或Txt文件作为上传格式");
				}
				// 获得多页工作表对象
				Sheet[] sheets = book.getSheets();
				for(int i = 0; i < sheets.length; i++){
					Sheet sheet = sheets[i];
					// 如果第一页没有数据，则继续下一页
					if(sheet.getColumns() == 0){
						continue;
					}
					// 从Sheet中解析出列的对应信息
					for(int j = 0; j < sheet.getRows(); j++){
						Cell cell = sheet.getCell(0,j);
						if(isMobile(cell.getContents())){
							blackTelephoneList.add(cell.getContents());
							totalRow ++;
						}
					}
				}
			} else {	// 用POI包读取2007版本的Excel文件

				try {
					// 构造XSSFWorkbook对象
					@SuppressWarnings("deprecation")
					XSSFWorkbook xwb = new XSSFWorkbook(file.getAbsolutePath());
					// 读取第一张表格内容
					XSSFSheet sheet = xwb.getSheetAt(0);
					for(int i = 0; i < sheet.getPhysicalNumberOfRows(); i++){
						XSSFRow row = sheet.getRow(i);
						if(isMobile(row.getCell(0).toString())){
							blackTelephoneList.add(row.getCell(0).toString());
							totalRow ++;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					showNotificationMessage("不能识别的文件格式，请选择Excel文件或Txt文件作为上传格式", Notification.TYPE_WARNING_MESSAGE);
					throw new RuntimeException("不能识别的文件格式，请选择Excel文件或Txt文件作为上传格式");
				}
				
			}
			if(totalRow == 0L){
				showNotificationMessage("没有有效的手机号码！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			showNotificationMessage("您成功上传"+totalRow+"条手机号码！", Notification.TYPE_HUMANIZED_MESSAGE);
		} else {
			showNotificationMessage("文件类型格式错误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		// 初始化进度条
		upload.setVisible(false);
		pi.setEnabled(true);
		pi.setValue(0f);
		Label pi_lb = new Label("上传进度：");
		progressLayout.removeComponent(pi_lb);
		progressLayout.addComponent(pi_lb);
		importBatchPathLayout.removeComponent(progressLayout);
		importBatchPathLayout.addComponent(progressLayout);
		progressLayout.removeComponent(pi);
		progressLayout.addComponent(pi);
		
		lbl_filename.setValue("<font color='blue'>"+filename+"</font>");
		
		return true;
	}
	

	/**
	 *  创建操作按钮
	 */
	private HorizontalLayout creatOperateComponents() {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		operator_l.setWidth("100%");

		save_button = new Button("保 存", this);
		save_button.setStyleName("default");
		operator_l.addComponent(save_button);
		
		cancel_button = new Button("取 消", this);
		operator_l.addComponent(cancel_button);
		
		return operator_l;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_button) {
			try {
				executeSave();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 更新或添加黑名单出现异常 ！---->"+e.getMessage(), e);
				warning_notification.setCaption("操作黑名单失败！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
			}
		} else if(source == cancel_button) {
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 执行保存
	 */
	@SuppressWarnings("unchecked")
	private void executeSave() {
		//检查输入是否合法
		String currentPhoneNo = null;
		if(!isMutilAdd){
			currentPhoneNo = StringUtils.trimToEmpty((String)phoneNo_tf.getValue());
			if("".equals(currentPhoneNo) || !phoneNo_tf.isValid()) {
				warning_notification.setCaption("号码不能为空，并且只能由7-12位的数字组成");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
		}
		
		String reason = StringUtils.trimToEmpty((String) reason_ta.getValue());
		if("".equals(reason)) {
			warning_notification.setCaption("加入黑名单的原因不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		// 检查当前黑名单是否已经保存过，如果已经存在，则直接返回
		String currentType = (String) type_og.getValue();
		boolean isTypeChanged = !currentType.equals(oldType);			// 类型发生改变
		boolean isPhoneNoChanged = true;
		if(!isMutilAdd){
			isPhoneNoChanged = !currentPhoneNo.equals(oldPhoneNo);	// 号码发生改变
		}
		
		try {
			Set<SipConfig> selectedOutlines = (Set<SipConfig>) outline_tcs.getValue();	// 当前选中的外线
			ArrayList<Long> selectedOutlineIds = new ArrayList<Long>();					// 用于存储当前选中的所有外线对应的编号
			ArrayList<Long> needAddOutlineIds = new ArrayList<Long>();					// 用于存储当前 新增加的外线编号
			
			for(SipConfig sp : selectedOutlines) {
				Long spId = sp.getId();
				selectedOutlineIds.add(spId);
				if(!oldSelectedOutlineIds.contains(spId)) {								// 如果之前没有选中该外线，则加入新增行列
					needAddOutlineIds.add(spId);
				}
			}
			
			if(isAddNewBlacklist) {												// 处理添加黑名单
				
				if(isMutilAdd){	// 如果是批量添加
					if(BlackListItem.TYPE_INCOMING.equals(currentType)){	// 呼入
						for(int i = 0; i < blackTelephoneList.size(); i++){
							addItemToIncoming(blackTelephoneList.get(i), reason, selectedOutlineIds);
						}
					} else if(BlackListItem.TYPE_OUTGOING.equals(currentType)){		// 呼出
						for(int i = 0; i < blackTelephoneList.size(); i++){
							addItemToOutgoing(blackTelephoneList.get(i), reason, selectedOutlineIds);
						}
					} else if(ALL_DIRECTION.equals(currentType)){	// 呼入和呼出
						for(int i = 0; i < blackTelephoneList.size(); i++){
							addItemToIncoming(blackTelephoneList.get(i), reason, selectedOutlineIds);
						}
						for(int i = 0; i < blackTelephoneList.size(); i++){
							addItemToOutgoing(blackTelephoneList.get(i), reason, selectedOutlineIds);
						}
					}
					
					showNotificationMessage("批量添加黑名单成功！", Notification.TYPE_HUMANIZED_MESSAGE);
				} else {
					String resultInfo = "添加黑名单："+currentPhoneNo;
					if(BlackListItem.TYPE_INCOMING.equals(currentType)) {			// 处理添加呼入方向的黑名单
						resultInfo += addItemToIncoming(currentPhoneNo, reason, selectedOutlineIds);
						if(resultInfo.contains("已经存在")) {
							warning_notification.setCaption(resultInfo);
							this.getApplication().getMainWindow().showNotification(warning_notification);
							return;
						}
					} else if(BlackListItem.TYPE_OUTGOING.equals(currentType)) {	// 处理添加呼出方向的黑名单
						resultInfo += addItemToOutgoing(currentPhoneNo, reason, selectedOutlineIds);
						if(resultInfo.contains("已经存在")) {
							warning_notification.setCaption(resultInfo);
							this.getApplication().getMainWindow().showNotification(warning_notification);
							return;
						}
					} else if(ALL_DIRECTION.equals(currentType)) { 					// 处理添加全部方向的黑名单
						resultInfo += addItemToIncoming(currentPhoneNo, reason, selectedOutlineIds);
						resultInfo += addItemToOutgoing(currentPhoneNo, reason, selectedOutlineIds);
					} 
					success_notification.setCaption(resultInfo);
					this.getApplication().getMainWindow().showNotification(success_notification);
				}
				
			} else {															// 处理编辑黑名单
				String resultInfo = "编辑黑名单："+currentPhoneNo;

				ArrayList<Long> needRemoveOutlineIds = new ArrayList<Long>();				// 用于存储当前 删除的外线编号
				for(Long oid : oldSelectedOutlineIds) {
					if(!selectedOutlineIds.contains(oid)) {									// 如果当前没有选中该外线，则加入异常行列
						needRemoveOutlineIds.add(oid);
					}
				}
				
				boolean outlineHasChanged = false;								// 用来标示黑名单选择的外线是否发生变化
				if(needAddOutlineIds.size() > 0 || needRemoveOutlineIds.size() > 0) {
					outlineHasChanged = true;
				}
				
				if(isTypeChanged || isPhoneNoChanged) {							// 检查号码和类型是否发生变化
					if(BlackListItem.TYPE_INCOMING.equals(currentType)) {			// 处理编辑呼入方向的黑名单
						boolean existed = blackListItemService.checkExistedByPhoneNo(currentPhoneNo, currentType, domain);
						if(existed) {
							resultInfo += ", <font color='red'>呼入 方向已经存在!</font>";
							success_notification.setCaption(resultInfo);
							this.getApplication().getMainWindow().showNotification(success_notification);
							return;
						}

						// 更新黑名单
						blacklistItem.setPhoneNumber(currentPhoneNo);
						blacklistItem.setReason(reason);
						blacklistItem.setType(BlackListItem.TYPE_INCOMING);
						blackListItemService.update(blacklistItem);
						// 更新内存中维护的黑名单信息
						removeBlacklistItemInCache();												// 先移除
						addBlacklistItemToCache(currentPhoneNo, currentType, selectedOutlineIds);	// 后添加
						
					} else if(BlackListItem.TYPE_OUTGOING.equals(currentType)) {	// 处理添加呼出方向的黑名单
						boolean existed = blackListItemService.checkExistedByPhoneNo(currentPhoneNo, currentType, domain);
						if(existed) {
							resultInfo += ", <font color='red'>呼出 方向已经存在!</font>";
							success_notification.setCaption(resultInfo);
							this.getApplication().getMainWindow().showNotification(success_notification);
							return;
						}

						// 更新黑名单
						blacklistItem.setPhoneNumber(currentPhoneNo);
						blacklistItem.setReason(reason);
						blacklistItem.setType(BlackListItem.TYPE_OUTGOING);
						blackListItemService.update(blacklistItem);
						// 更新内存中维护的黑名单信息
						removeBlacklistItemInCache();												// 先移除
						addBlacklistItemToCache(currentPhoneNo, currentType, selectedOutlineIds);	// 后添加
					} 
					
					// 更新黑名单与外线对应关系的中间表
					updateBlacklistItemToOutline(blacklistItem.getId(), needAddOutlineIds, needRemoveOutlineIds);

				} else if(outlineHasChanged) {
					// 更新内存中维护的黑名单信息
					removeBlacklistItemInCache();												// 先移除
					addBlacklistItemToCache(currentPhoneNo, currentType, selectedOutlineIds);	// 后添加
					
					// 更新黑名单与外线对应关系的中间表
					updateBlacklistItemToOutline(blacklistItem.getId(), needAddOutlineIds, needRemoveOutlineIds);
				}

				resultInfo += " 成功！";
				success_notification.setCaption(resultInfo);
				this.getApplication().getMainWindow().showNotification(success_notification);
			}
		} catch (Exception e) {
			logger.error("管理员保存黑名单出现异常---> "+e.getMessage(), e);
			warning_notification.setCaption("保存失败，请检查信息后重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		// 更新黑名单显示界面的信息
		blacklistView.refreshTable(isAddNewBlacklist);
		
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 处理新增黑名单，并且是添加类型为“呼入”方向的
	 * @param currentPhoneNo		黑名单号码
	 * @param reason				加入原因
	 * @param selectedOutlineIds	选择的外线
	 * @return String  				返回存储结果
	 */
	private String addItemToIncoming(String currentPhoneNo, String reason, ArrayList<Long> selectedOutlineIds) {
		String resultInfo = "";
		boolean existedIncoming = blackListItemService.checkExistedByPhoneNo(currentPhoneNo, BlackListItem.TYPE_INCOMING, domain);
		if(existedIncoming) {
			resultInfo += ", <font color='red'>呼入 方向已经存在!</font>";
		} else {
			BlackListItem blacklistItem = new BlackListItem();
			blacklistItem.setDomain(domain);
			blacklistItem.setCreateTime(new Date());
			blacklistItem.setPhoneNumber(currentPhoneNo);
			blacklistItem.setReason(reason);
			blacklistItem.setType(BlackListItem.TYPE_INCOMING);
			blackListItemService.save(blacklistItem);
			resultInfo += ", 呼入 方向保存成功";

			// 添加当前黑名单到内存中
			addBlacklistItemToCache(currentPhoneNo, BlackListItem.TYPE_INCOMING, selectedOutlineIds);

			// 添加黑名单跟外线的关联
			updateBlacklistItemToOutline(blacklistItem.getId(), selectedOutlineIds, new ArrayList<Long>());
		}
		return resultInfo;
	}
	
	/**
	 * 处理新增黑名单，并且是添加类型为“呼出”方向的
	 * @param currentPhoneNo		黑名单号码
	 * @param reason				加入原因
	 * @param selectedOutlineIds	选择的外线
	 * @return String  				返回存储结果
	 */
	private String addItemToOutgoing(String currentPhoneNo, String reason, ArrayList<Long> selectedOutlineIds) {
		String resultInfo = "";
		boolean existedOutgoing = blackListItemService.checkExistedByPhoneNo(currentPhoneNo, BlackListItem.TYPE_OUTGOING, domain);
		if(existedOutgoing) {
			resultInfo += ", <font color='red'>呼出 方向已经存在!</font>";
		} else {
			BlackListItem outItem = new BlackListItem();
			outItem.setDomain(domain);
			outItem.setCreateTime(new Date());
			outItem.setPhoneNumber(currentPhoneNo);
			outItem.setType(BlackListItem.TYPE_OUTGOING);
			outItem.setReason(reason);
			blackListItemService.save(outItem);
			resultInfo += ", 呼出 方向保存成功";
			
			// 添加当前黑名单到内存中
			addBlacklistItemToCache(currentPhoneNo, BlackListItem.TYPE_OUTGOING, selectedOutlineIds);

			// 添加黑名单跟外线的关联
			updateBlacklistItemToOutline(outItem.getId(), selectedOutlineIds, new ArrayList<Long>());
		}
		return resultInfo;
	}

	/**
	 * 在内存中移除与当前操作的黑名单相关的信息
	 */
	private void removeBlacklistItemInCache() {
		if(oldType.equals(BlackListItem.TYPE_INCOMING)) {	// 原来是呼入类型
			ShareData.domainToIncomingBlacklist.get(domain.getId()).remove(oldPhoneNo);	// 从域范围内移除

			for(Long outlineId1 : oldSelectedOutlineIds) {								// 从外线范围内移除
				List<String> incomingBlacklist = ShareData.outlineToIncomingBlacklist.get(outlineId1);
				if(incomingBlacklist != null) {
					incomingBlacklist.remove(oldPhoneNo);
				}
			}
		} else {											// 原来是呼出类型
			ShareData.domainToOutgoingBlacklist.get(domain.getId()).remove(oldPhoneNo);	// 从域范围内移除
			
			for(Long outlineId1 : oldSelectedOutlineIds) {								// 从外线范围内移除
				List<String> outgoingBlacklist = ShareData.outlineToOutgoingBlacklist.get(outlineId1);
				if(outgoingBlacklist != null) {
					outgoingBlacklist.remove(oldPhoneNo);
				}
			}
		}
	}
	
	/**
	 * 添加当前黑名单到内存中
	 * 
	 * @param currentPhoneNo 		黑名单当前的电话号码
	 * @param currentType			黑名单当前的类型
	 * @param selectedOutlineIds	黑名单当前选中的外线对应的编号
	 */
	private void addBlacklistItemToCache(String currentPhoneNo, String currentType, ArrayList<Long> selectedOutlineIds) {
		if( currentType.equals(BlackListItem.TYPE_INCOMING) ) {		// 现在是呼入 类型
			ShareData.domainToIncomingBlacklist.get(domain.getId()).add(currentPhoneNo);		// 添加到域范围内
			for(Long selectedOid : selectedOutlineIds) {										// 添加到外线范围内
				List<String> outlineToInBls = ShareData.outlineToIncomingBlacklist.get(selectedOid);
				if(outlineToInBls == null) {
					outlineToInBls = new ArrayList<String>();
					ShareData.outlineToIncomingBlacklist.put(selectedOid, outlineToInBls);
				}
				outlineToInBls.add(currentPhoneNo);
			}
		} else {													// 现在是呼出 类型
			ShareData.domainToOutgoingBlacklist.get(domain.getId()).add(currentPhoneNo);		// 添加到域范围内
			for(Long selectedOid : selectedOutlineIds) {										// 添加到外线范围内
				List<String> outlineToOutBls = ShareData.outlineToOutgoingBlacklist.get(selectedOid);
				if(outlineToOutBls == null) {
					outlineToOutBls = new ArrayList<String>();
					ShareData.outlineToOutgoingBlacklist.put(selectedOid, outlineToOutBls);
				}
				outlineToOutBls.add(currentPhoneNo);
			}
		}
	}

	/**
	 * 更新黑名单与外线对应关系的中间表
	 * 
	 * @param blacklistItemId			当前待操作的黑名单对应的编号
	 * @param needAddOutlineIds			需要将黑名单与之添加关联的外线编号
	 * @param needRemoveOutlineIds		需要将黑名单与外线关联移除的外线编号
	 */
	private void updateBlacklistItemToOutline(Long blacklistItemId, ArrayList<Long> needAddOutlineIds, ArrayList<Long> needRemoveOutlineIds) {
		for(Long addOid : needAddOutlineIds) {
			Outline2BlacklistItemLink outline2BlacklistItemLink = new Outline2BlacklistItemLink();
			outline2BlacklistItemLink.setBlacklistItemId(blacklistItemId);
			outline2BlacklistItemLink.setOutlineId(addOid);
			blackListItemService.saveOutline2BlacklistLink(outline2BlacklistItemLink);
		}
		for(Long deleteOid : needRemoveOutlineIds) {
			blackListItemService.deleteOutline2BlacklistLinkByIds(blacklistItemId, deleteOid);
		}
	}

	/**
	 * 更新Form 的数据源
	 * @param blacklistItem			黑名单对象
	 * @param isAddNewBlacklist		是否为添加新的黑名单操作
	 */
	public void updateDataSource(BlackListItem blacklistItem, boolean isAddNewBlacklist, boolean isMutilAdd) {
		this.blacklistItem = blacklistItem;
		this.isMutilAdd = isMutilAdd;
		this.isAddNewBlacklist = isAddNewBlacklist;
		this.oldPhoneNo = blacklistItem.getPhoneNumber();
		this.oldType = blacklistItem.getType();
		this.oldSelectedOutlineIds = new ArrayList<Long>();
	
		// 更新重新查询外线
		List<SipConfig> allOutlines = sipConfigService.getAllOutlinesByDomain(domain);
		outlineContainer.removeAllItems();
		outlineContainer.addAll(allOutlines);
		
		lbl_filename.setValue("");
		
		if(isMutilAdd){
			upload.setVisible(true);	// 设置上传文件按钮为可用状态
			importBatchPathLayout.setVisible(true);
			phoneNo_lo.setVisible(false);
		} else {
			importBatchPathLayout.setVisible(false);
			phoneNo_lo.setVisible(true);
		}
		
		if(isAddNewBlacklist) {	// 	如果是添加，则默认选择所有外线
			reason_ta.setValue(null);
			outline_tcs.setValue(null);						// 清空原来选中的值，一定的加上,不然会导致 outline_tcs.getValue() 获取的Set集合会越来越大
			type_og.addItem(ALL_DIRECTION);
			type_og.setValue(ALL_DIRECTION);
			for(SipConfig outline : allOutlines) {
				outline_tcs.select(outline);
			}
		} else {  				// 如果是编辑，则回显该黑名单对应的外线
			type_og.removeItem(ALL_DIRECTION);
			// 回显信息
			type_og.setValue(blacklistItem.getType());
			phoneNo_tf.setValue(blacklistItem.getPhoneNumber());
			reason_ta.setValue(blacklistItem.getReason());
			outline_tcs.setValue(null);						// 清空原来选中的值，一定的加上,不然会导致 outline_tcs.getValue() 获取的Set集合会越来越大
			List<Long> needSelectedOutlineIds = blackListItemService.getAllOutlineIdsByBlacklistItemId(blacklistItem.getId());
			for(Long outlineId : needSelectedOutlineIds) {
				for(SipConfig outline : allOutlines) {
					if(outlineId.equals(outline.getId())) {
						outline_tcs.select(outline);
						oldSelectedOutlineIds.add(outline.getId());
					}
				}
			}
		}
	}
	
	/**
	 * 手机号码验证
	 * @return boolean
	 */
	private boolean isMobile(String str){
        Pattern p = Pattern.compile("^\\d{3,14}$"); // 验证手机号  
        Matcher m = p.matcher(str);  
        return m.matches();   
	}
	
	// 提示信息
	private void showNotificationMessage(String msg, int type){
		this.getApplication().getMainWindow().showNotification(msg, type);
	}

}


/*
 * 电话号码验证
 * @return boolean
 *
private boolean isPhone(String str){
	Matcher m = null;  
	Pattern p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的  
	Pattern p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的  
    if(str.length() >9)  
    {   m = p1.matcher(str);  
        return m.matches();    
    }else{  
        m = p2.matcher(str);  
        return m.matches();   
    }    
}
*/