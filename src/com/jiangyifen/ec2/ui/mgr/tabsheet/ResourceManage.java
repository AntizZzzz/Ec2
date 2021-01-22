package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.ResourceManageService;
import com.jiangyifen.ec2.ui.FlipOverTableComponentNoGo;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.myresource.AddCustomerResourceWindow;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.ResourceDeleteUtil;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.ShowConfigDeleteWindow;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.ShowDeleteLogWindow;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.ShowResourceInfoWindow;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.Task;
import com.jiangyifen.ec2.ui.mgr.resourcemanage.TaskShareData;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 管理员-资源管理-新
 * @author LXY
 * 
 */
public class ResourceManage extends VerticalLayout implements ClickListener {

	private static final long serialVersionUID = -2121013786349694035L;
	private Logger logger = LoggerFactory.getLogger(ResourceManage.class);
 	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";	//号码加密权限
	private static final int PAGE_LENGTH = 10;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	//第一行
	private Label lbBatch;									//批次选择
	private ComboBox cbxBatch;
	private List<CustomerResourceBatch> customerResourceBatchList;
	private BeanItemContainer<CustomerResourceBatch> customerResourceBatchContainer = new BeanItemContainer<CustomerResourceBatch>(CustomerResourceBatch.class);

	private Label lbCustomerLevel;							//客户等级选择
	private ComboBox cbxCustomerLevel;
	private List<CustomerLevel> customerLevelList;
	private BeanItemContainer<CustomerLevel> customerLevelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
	
	private Label lbCallNum;
	private TextField tfFromCallNum;
	private Label lbCallNumH;
	private TextField tfToCallNum;
	
	//第二行
	private ComboBox cmbResourceImport;
	private PopupDateField pdfImportDateStart;
	private PopupDateField pdfImportDateEnd;
	
	//第三行
	private Label lbCustomerId; 
	private TextField tfCustomerId;
	private PopupDateField pdfLastDateStart;
	private PopupDateField pdfLastDateEnd;
	
	//第四行
	private Label lbName;
	private TextField tfName;
	private Label lbTelphone;
	private TextField tfTelphone;
	
	//表格/底部按钮
	private VerticalLayout tableVLayout;	
	private HorizontalLayout buttonLayout;
	
	private Button btnSearch;
  	private Button btnDeleteTask;
	private Button createBathButton;
	private Button addCustomerResource;
	private Button btnShowIllustration;
	private Button btnShowDeleteLog;
	
	private boolean isEncryptMobile = true; // 默认使用加密的电话号码和手机号
	private Table table;
	private FlipOverTableComponentNoGo<CustomerResource> flipOverTableComponent;

	private String[] columnHeaders = new String[] { "客户编号", "姓名", "电话", "性别", "生日", "导入时间", "导入者",  "最后拨打时间","拨打次数", "客户经理", "资源等级",  "批次" };
	private Object[] visibleColumns = new Object[] { "id", "name", "telephones", "sex", "birthday","importDate", "owner",  "lastDialDate","count",  "accountManager", "customerLevel", "customerResourceBatches" };

	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);					// 右键单击 action
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private TabSheet customerInfoTabSheet;						 	// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow; 			// 客户所有相关信息查看窗口
	private AddCustomerResourceWindow addCustomerResourceWindow;	// 资源添加窗口
	private ShowResourceInfoWindow showResourceInfoWindow;			// 显示资源说明
	private ShowDeleteLogWindow showDeleteLogWindow;				// 显示删除日志
	private ShowConfigDeleteWindow showConfigDeleteWindow;			// 显示是否执行删除
	
	private Label lbInfo;
	private Button btnReloadTaskNum;
	private Label lbTaskStartTime;
	private Label lbTaskPlanNum;
	private Label lbTaskSurplusNum;
	private Label lbTaskStatus;
	private TextArea taJpql;
	
	private StringBuilder selectListJpql = new StringBuilder("");
	private StringBuilder selectCountJpql = new StringBuilder("");
	private StringBuilder whereJpql = new StringBuilder("");
	private String listSql;
	private String countSql;
	private Long taskId = -100L;
	private int permitExecuteDeleteHour = 18;
	private int deleteTaskMaxNum = 1000000;
	
	private StringBuilder jpqlParams = new StringBuilder("查询条件:");
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	private BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
	
	private User loginUser;
	private Domain domain;
	
	private CommonService commonService;
	private ResourceManageService resourceManageService;
 	
	/** 资源管理 */
	public ResourceManage() {
		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);
		
		initExecuteInfo();	//初始化参数
		initBusinessAndSpring();
		createCustomerInfoWindow();// 客户所有相关信息查看窗口
		buildSearchGridLayout();
	}
	
	//初始化参数
	private void initExecuteInfo() {
		try {
			permitExecuteDeleteHour = Integer.valueOf(Config.props.get(Config.PERMIT_EXECUTE_DELETE_HOUR).toString());
			deleteTaskMaxNum = Integer.valueOf(Config.props.get(Config.DELETE_TASK_MAX_NUM).toString());
		} catch (Exception e) {
			permitExecuteDeleteHour = 18;
			deleteTaskMaxNum = 1000000;
			logger.error("LLXXYY_"+e.getMessage(), e);
		}
		
	}

	//业务属性
	private void initBusinessAndSpring(){// 初始化业务与Spring组件
		loginUser  = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);// 判断是否需要加密
		
		commonService = SpringContextHolder.getBean("commonService");
		resourceManageService = SpringContextHolder.getBean("resourceManageService");
		
	}
	
	//创建查询表格
	private void buildSearchGridLayout(){
		
		GridLayout gridLayout = new GridLayout(5, 4);
		gridLayout.setSpacing(true);
		this.addComponent(gridLayout);

		
		//-----------------第一行查询布局-----------------------------------
		buildFirstLayout(gridLayout);
		
		//-----------------第二行查询布局-----------------------------------
		buildSecondLayout(gridLayout);
		
		//-----------------第三行查询布局-----------------------------------
		bulidThirdLayout(gridLayout);
		
		//-----------------第四行查询布局-----------------------------------
		buildFourLayout(gridLayout);
		
		//-----------------第五行表格布局-----------------------------------
		HorizontalLayout buttonHLayout = bulidTableInfoLayout();
		
		//-----------------第五行底部按钮-----------------------------------
		bulidToolLayout(buttonHLayout);
		
		//-----------------第六行底部任务信息布局-----------------------------------
		bulidTaskInfoLayout();
		
	}

	/** 第一行查询布局 */
	private void buildFirstLayout(GridLayout gridLayout) {
		/**
		 * 批次名称
		 */
		lbBatch = new Label("批次名称：");
		lbBatch.setWidth("-1px");
		
		cbxBatch = new ComboBox();
		cbxBatch.setWidth("150px");
		cbxBatch.setImmediate(true);
		cbxBatch.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		cbxBatch.setNullSelectionAllowed(true);
		
		HorizontalLayout batchBoxLayout = new HorizontalLayout();
		batchBoxLayout.setSpacing(true);
		batchBoxLayout.addComponent(lbBatch);
		batchBoxLayout.addComponent(cbxBatch);
		
		String crblJpql = "select S from CustomerResourceBatch as S where S.domain.id = " +  domain.getId() + "";
		customerResourceBatchList = resourceManageService.getEntity(CustomerResourceBatch.class, crblJpql);
		customerResourceBatchContainer.removeAllItems();
		customerResourceBatchContainer.addAll(customerResourceBatchList);
		cbxBatch.setContainerDataSource(customerResourceBatchContainer);
		cbxBatch.setItemCaptionPropertyId("batchName");
		gridLayout.addComponent(batchBoxLayout, 0, 0);
		
		/**
		 * 客户等级
		 */
		lbCustomerLevel = new Label("　　客户等级：", Label.CONTENT_XHTML);
		lbCustomerLevel.setWidth("-1px");
		
		cbxCustomerLevel = new ComboBox();
		cbxCustomerLevel.setImmediate(true);
		cbxCustomerLevel.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		cbxCustomerLevel.setWidth("153px");
		cbxCustomerLevel.setNullSelectionAllowed(true);
		
		HorizontalLayout  customerLevelBoxLayout = new HorizontalLayout();
		customerLevelBoxLayout.setSpacing(true);
		customerLevelBoxLayout.addComponent(lbCustomerLevel);
		customerLevelBoxLayout.addComponent(cbxCustomerLevel);
		
		String clJpql = "select S from CustomerLevel as S where S.domain.id = "+  domain.getId() + "";
		customerLevelList = resourceManageService.getEntity(CustomerLevel.class, clJpql);
		customerLevelContainer.removeAllItems();
		customerLevelContainer.addAll(customerLevelList);
		cbxCustomerLevel.setContainerDataSource(customerLevelContainer);
		cbxCustomerLevel.setItemCaptionPropertyId("levelName");
		gridLayout.addComponent(customerLevelBoxLayout, 1, 0);
		
		/**
		 * 呼叫次数
		 */
		lbCallNum = new Label("呼叫次数：", Label.CONTENT_XHTML);
		lbCallNum.setWidth("-1px");
		
		tfFromCallNum = new TextField();
		tfFromCallNum.setImmediate(true);
		tfFromCallNum.setMaxLength(3);
		tfFromCallNum.addValidator(new RegexpValidator("\\d+", "只能是数字"));
		tfFromCallNum.setWidth("66px");
		
		lbCallNumH = new Label("-");
		lbCallNumH.setWidth("-1px");
		
		tfToCallNum = new TextField();
		tfToCallNum.setImmediate(true);
		tfToCallNum.setMaxLength(3);
		tfToCallNum.addValidator(new RegexpValidator("\\d+", "只能是数字"));
		tfToCallNum.setWidth("67px");
		
		HorizontalLayout callNumLayout = new HorizontalLayout();
		callNumLayout.setSpacing(true);
		callNumLayout.addComponent(lbCallNum);
		callNumLayout.addComponent(tfFromCallNum);
		callNumLayout.addComponent(lbCallNumH);
		callNumLayout.addComponent(tfToCallNum);
		gridLayout.addComponent(callNumLayout, 2, 0);
		
		btnSearch = new Button("查    询");
		btnSearch.addListener((ClickListener) this);
		gridLayout.addComponent(btnSearch, 3, 0);
	}
	
	/** 第二行查询布局 */
	private void buildSecondLayout(GridLayout gridLayout) {
		/**
		 * 资源导入者
		 */
		Label lblResourceImport = new Label("　导入者：");
		lblResourceImport.setWidth("-1px");
		
		cmbResourceImport = new ComboBox();
		cmbResourceImport.setWidth("150px");
		cmbResourceImport.setImmediate(true);
		cmbResourceImport.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		cmbResourceImport.setNullSelectionAllowed(true);
		
		HorizontalLayout resourceImportLayout = new HorizontalLayout();
		resourceImportLayout.setSpacing(true);
		resourceImportLayout.addComponent(lblResourceImport);
		resourceImportLayout.addComponent(cmbResourceImport);
		gridLayout.addComponent(resourceImportLayout, 0, 1);
		
		/**
		 * 导入时间
		 */
		Label lblImportDate = new Label("　　导入时间：");
		lblImportDate.setWidth("-1px");

		pdfImportDateStart = new PopupDateField();
		pdfImportDateStart.setWidth("150px");
		pdfImportDateStart.setImmediate(true);
		pdfImportDateStart.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfImportDateStart.setResolution(PopupDateField.RESOLUTION_SEC);
		
		HorizontalLayout hlImport1 = new HorizontalLayout();
		hlImport1.setSpacing(true);
		hlImport1.addComponent(lblImportDate);
		hlImport1.addComponent(pdfImportDateStart);
		gridLayout.addComponent(hlImport1, 1, 1);
		
		Label lblJoinImport = new Label("　　到　　");
		lblJoinImport.setWidth("-1px");

		pdfImportDateEnd = new PopupDateField();
		pdfImportDateEnd.setWidth("150px");
		pdfImportDateEnd.setImmediate(true);
		pdfImportDateEnd.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfImportDateEnd.setResolution(PopupDateField.RESOLUTION_SEC);
		
		HorizontalLayout hlImport2 = new HorizontalLayout();
		hlImport2.setSpacing(true);
		hlImport2.addComponent(lblJoinImport);
		hlImport2.addComponent(pdfImportDateEnd);
		gridLayout.addComponent(hlImport2, 2, 1);
	}

	/** 第三行查询布局 */
	private void bulidThirdLayout(GridLayout gridLayout) {
		/**
		 * 客户编号
		 */
		lbCustomerId = new Label("客户编号：");
		lbCustomerId.setWidth("-1px");
		
		tfCustomerId = new TextField();
		tfCustomerId.setImmediate(true);
		tfCustomerId.setWidth("150px");
		tfCustomerId.setMaxLength(20);
		tfCustomerId.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		tfCustomerId.setValidationVisible(false);
		
		HorizontalLayout customerLayout = new HorizontalLayout();
		customerLayout.setSpacing(true);
		customerLayout.addComponent(lbCustomerId);
		customerLayout.addComponent(tfCustomerId);
		gridLayout.addComponent(customerLayout, 0, 2);
		
		/**
		 * 最后拨打时间
		 */
		Label lblLastDate = new Label("最后拨打时间：");
		lblLastDate.setWidth("-1px");
		
		pdfLastDateStart = new PopupDateField();
		pdfLastDateStart.setWidth("150px");
		pdfLastDateStart.setImmediate(true);
		pdfLastDateStart.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfLastDateStart.setResolution(PopupDateField.RESOLUTION_SEC);
		
		HorizontalLayout hlLastDate1 = new HorizontalLayout();
		hlLastDate1.setSpacing(true);
		hlLastDate1.addComponent(lblLastDate);
		hlLastDate1.addComponent(pdfLastDateStart);
		gridLayout.addComponent(hlLastDate1, 1, 2);
		
		Label lblJoinLast = new Label("　　到　　");
		lblJoinLast.setWidth("-1px");
		
		pdfLastDateEnd = new PopupDateField();
		pdfLastDateEnd.setWidth("150px");
		pdfLastDateEnd.setImmediate(true);
		pdfLastDateEnd.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfLastDateEnd.setResolution(PopupDateField.RESOLUTION_SEC);
		
		HorizontalLayout hlLastDate2 = new HorizontalLayout();
		hlLastDate2.setSpacing(true);
		hlLastDate2.addComponent(lblJoinLast);
		hlLastDate2.addComponent(pdfLastDateEnd);
		gridLayout.addComponent(hlLastDate2, 2, 2);
	}
	
	/** 第四行查询布局 */
	private void buildFourLayout(GridLayout gridLayout) {
		/**
		 * 姓名
		 */
		lbName = new Label("姓　　名：");
		lbName.setWidth("-1px");
		
		tfName = new TextField();
		tfName.setImmediate(true);
		tfName.setWidth("150px");
		tfName.setMaxLength(20);
		
		HorizontalLayout nameLayout = new HorizontalLayout();
		nameLayout.setSpacing(true);
		nameLayout.addComponent(lbName);
		nameLayout.addComponent(tfName);
		gridLayout.addComponent(nameLayout, 0, 3);
		
		/**
		 * 电话号码
		 */
		lbTelphone = new Label("　　电话号码：");
		lbTelphone.setWidth("-1px");
		
		tfTelphone = new TextField();
		tfTelphone.setImmediate(true);
		tfTelphone.setWidth("150px");
		tfTelphone.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成！"));
		tfTelphone.setMaxLength(20);
		
		HorizontalLayout telphoneLayout = new HorizontalLayout();
		telphoneLayout.setSpacing(true);
		telphoneLayout.addComponent(lbTelphone);
		telphoneLayout.addComponent(tfTelphone);
		gridLayout.addComponent(telphoneLayout, 1, 3);
	}
	
	private HorizontalLayout bulidTableInfoLayout() {
		tableVLayout = new VerticalLayout();
		tableVLayout.setSpacing(true);
		this.addComponent(tableVLayout);
		this.setExpandRatio(tableVLayout, 1);

		table = createFormatColumnTable();
 		table.setImmediate(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.addGeneratedColumn("telephones", new TelephonesColumnGenerator());
		tableVLayout.addComponent(table);
		tableVLayout.setExpandRatio(table, 1);

		addActionToTable(table);
		HorizontalLayout buttonHLayout = new HorizontalLayout();
		buttonHLayout.setWidth("100%");
		tableVLayout.addComponent(buttonHLayout);
		return buttonHLayout;
	}
	
	private void bulidToolLayout(HorizontalLayout buttonHLayout) {
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonHLayout.addComponent(buttonLayout);
		buttonHLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_LEFT);

		btnDeleteTask = new Button("执行删除",this);
		buttonLayout.addComponent(btnDeleteTask);
		buttonLayout.setComponentAlignment(btnDeleteTask, Alignment.BOTTOM_LEFT);
		
		
		/*createBathButton = new Button("创建批次",this);
		buttonLayout.addComponent(createBathButton);
		buttonLayout.setComponentAlignment(createBathButton, Alignment.BOTTOM_LEFT);*/

		addCustomerResource = new Button("添加资源",this);
		buttonLayout.addComponent(addCustomerResource);
		buttonLayout.setComponentAlignment(addCustomerResource, Alignment.BOTTOM_LEFT);

		btnShowIllustration = new Button("删除说明",this);
		buttonLayout.addComponent(btnShowIllustration);
		buttonLayout.setComponentAlignment(btnShowIllustration, Alignment.BOTTOM_LEFT);
		
		btnShowDeleteLog = new Button("删除日志",this);
		buttonLayout.addComponent(btnShowDeleteLog);
		buttonLayout.setComponentAlignment(btnShowDeleteLog, Alignment.BOTTOM_LEFT);
		
		lbInfo = new Label("",Label.CONTENT_XHTML);
		buttonLayout.addComponent(lbInfo);
		buttonLayout.setComponentAlignment(lbInfo, Alignment.BOTTOM_LEFT);
		
		addFlipOverTableComponent(buttonHLayout);
	}
	
	private void bulidTaskInfoLayout(){
		HorizontalLayout taskLayout = new HorizontalLayout();
 		taskLayout.setWidth("100%");
		tableVLayout.addComponent(taskLayout);
		
		VerticalLayout taskInfoLayout = new VerticalLayout();
		taskLayout.addComponent(taskInfoLayout);
		taskInfoLayout.setSpacing(true);
		taskInfoLayout.setWidth("100%");
		taskInfoLayout.setSizeFull();
		
		GridLayout taskInfoGrid = new GridLayout(9, 2);
		taskInfoGrid.setSizeFull();
		taskInfoLayout.addComponent(taskInfoGrid);
		
		btnReloadTaskNum = new Button("刷新任务信息",this);
		btnReloadTaskNum.setStyleName("link");
		taskInfoGrid.addComponent(btnReloadTaskNum, 0, 0);
		
		taskInfoGrid.addComponent(new Label("开始时间:"), 1, 0);
		lbTaskStartTime = new Label("--",Label.CONTENT_XHTML);
		taskInfoGrid.addComponent(lbTaskStartTime, 2, 0);
		
		taskInfoGrid.addComponent(new Label("计划删除数量:"), 3, 0);
		lbTaskPlanNum = new Label("--",Label.CONTENT_XHTML);
		taskInfoGrid.addComponent(lbTaskPlanNum, 4, 0);
		
		taskInfoGrid.addComponent(new Label("剩余数量:"), 5, 0);
		lbTaskSurplusNum = new Label("--",Label.CONTENT_XHTML);
		taskInfoGrid.addComponent(lbTaskSurplusNum, 6, 0);
		
		taskInfoGrid.addComponent(new Label("删除任务状态:"), 7, 0);
		lbTaskStatus = new Label("--",Label.CONTENT_XHTML);
		taskInfoGrid.addComponent(lbTaskStatus, 8, 0);
		
		HorizontalLayout jpqlLayout = new HorizontalLayout();
		jpqlLayout.setWidth("100%");
		taskInfoLayout.addComponent(jpqlLayout);
		
		taJpql = new TextArea();
		taJpql.setWidth("100%");
		taJpql.setHeight("40px");
		taJpql.setValue("查询条件:");
		jpqlLayout.addComponent(taJpql);
	}

	private void reloadTaskInfo(){//刷新删除任务信息
		lbTaskStatus.setValue(ResourceDeleteUtil.deleteStatus.getName());
		if(ResourceDeleteUtil.getInstance().getDomainId() == domain.getId()){//当前任务是该域下人员创建 可以获取全部任务信息
			this.lbInfo.setValue("");
			Task task = TaskShareData.taskMap.get(domain.getId());
			if(task != null){
				lbTaskStartTime.setValue(sdf.format(task.getCreateDate()));
				lbTaskPlanNum.setValue(task.getCountNum());
				lbTaskPlanNum.setValue(task.getCountNum());
				int countNum = resourceManageService.getEntityCount(task.getCountSql());
				lbTaskSurplusNum.setValue(countNum);
				taJpql.setValue(task.getJpqlParams());
			}else{
				lbTaskStartTime.setValue("");
				lbTaskPlanNum.setValue("");
				lbTaskSurplusNum.setValue("");
				taJpql.setValue("");
			}
		}
	}
	
	//用于自动生成可以拨打电话的列 如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫-----------格式化电话号码字段
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		private static final long serialVersionUID = 8082817023250260447L;
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (columnId.equals("telephones")) {
				CustomerResource resource = (CustomerResource) itemId;
				Set<Telephone> telephones = resource.getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			}
			return null;
		}
	}
	 
	//为指定任务表格 添加右键单击事件
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			private static final long serialVersionUID = -4008685565586884971L;
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.setValue(null);
				table.select(target);
				CustomerResource customerResource = null;
				table.getApplication().getMainWindow().removeWindow(customerAllInfoWindow);
				table.getApplication().getMainWindow().addWindow(customerAllInfoWindow);
				@SuppressWarnings("unchecked")
				Set<CustomerResource> resources = (Set<CustomerResource>) table.getValue();//TODO 当前表格选中的数据，有可能很多需要控制一下
				Iterator<CustomerResource> iterator = resources.iterator();
				while (iterator.hasNext()) {
					customerResource = iterator.next();
					break;
				}
				customerAllInfoWindow.initCustomerResource(customerResource);
				if (action == BASEINFO) {
					customerAllInfoWindow.echoCustomerBaseInfo(customerResource);
					customerInfoTabSheet.setSelectedTab(0);
				} else if (action == DESCRIPTIONINFO) {
					customerAllInfoWindow.echoCustomerDescription(customerResource);
					customerInfoTabSheet.setSelectedTab(1);
				} else if (action == ADDRESSINFO) {
					customerAllInfoWindow.echoCustomerAddress(customerResource);
					customerInfoTabSheet.setSelectedTab(2);
				} else if (action == HISTORYRECORD) {
					customerAllInfoWindow.echoHistoryRecord(customerResource);
					customerInfoTabSheet.setSelectedTab(3);
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				if (target != null) {
					return new Action[] { BASEINFO, DESCRIPTIONINFO, ADDRESSINFO, HISTORYRECORD };
				}
				return null;
			}
		});
	}

	//创建客户信息查看窗口及相关组件
	private void createCustomerInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}

	//格式化表格字段
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = -5577620177615804634L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (property.getValue() == null) {
					return "";
				} else if ((property.getType() == Date.class)) {
					return sdf.format((Date) property.getValue());
				} else if ("customerResourceBatches".equals(colId)) {
					@SuppressWarnings("unchecked")
					Set<CustomerResourceBatch> batches = (Set<CustomerResourceBatch>) property.getValue();
					StringBuffer sb = new StringBuffer();
					for (CustomerResourceBatch batch : batches) {	//TODO 这里需要注意数据量
						sb.append(batch.getBatchName());
						sb.append(",");
					}
					if (!sb.toString().equals("")) {
						String batchNames = sb.toString().substring(0, sb.toString().length() - 1);
						return batchNames;
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	@Override
	public void buttonClick(ClickEvent event) { 	//按钮事件2014-08-12 00:00:00 "853442"
		Button source = event.getButton();
		if (source == btnSearch) {
			executeSearch();
		} else if (source == createBathButton) {
			if (table.getContainerDataSource().size() == 0) {
				table.getApplication().getMainWindow().showNotification("当前没有查询到的资源，不能创建批次！", Notification.TYPE_WARNING_MESSAGE);
			} else {
				//createBath();						// 创建批次
			}
		} else if (source == addCustomerResource) {
			showAddCustomerResourceForm();					// 添加资源
		} else if(source == btnDeleteTask){
			showConfigDelete();
		} else if(source == btnReloadTaskNum){
			reloadTaskInfo();
		} else if(source == btnShowIllustration){
			showIllustration();
		} else if(source == btnShowDeleteLog){
			showDeleteLog();
		} 
	}
	
	@Override
	public void attach() {
		String deptIds = achieveBasicUtil.getDeptIds();
		if(deptIds != null && deptIds.length() >= 1) {
			@SuppressWarnings("unchecked")
			List<User> userList = (List<User>) commonService.excuteSql("select u from User u where u.domain.id = " + domain.getId() + " and u.department.id in("+deptIds+") order by u.empNo asc", ExecuteType.RESULT_LIST);
			if(userList != null && userList.size() >= 1) {
				userContainer.removeAllItems();
				userContainer.addAll(userList);
				cmbResourceImport.setContainerDataSource(userContainer);
			}
		}
	}
	
	//显示是否选择删除
	private void showConfigDelete(){
		if(showConfigDeleteWindow == null){
			showConfigDeleteWindow = new ShowConfigDeleteWindow(this,"<font color='red'>1.删除资源关联删除，资源、电话号码、资源与任务关联、资源与批次关联、客服记录。<br>2.【刷新任务信息】查看删除进度。<br>3.确定要删除吗?</font>", "400px", "180px");
		}
		this.getApplication().getMainWindow().removeWindow(showConfigDeleteWindow);
		this.getApplication().getMainWindow().addWindow(showConfigDeleteWindow);
		
	}
	
	/**
	 *	执行删除
	 *	删除提示 是否要删除  之后执行删除方法 
	 */
	public void doDeleteTask() {
		try {
			if(StringUtils.isNotEmpty(countSql)){
				final int countNum = resourceManageService.getEntityCount(countSql);
				if(countNum <= 0){			//删除是数据为0条
					this.lbInfo.setValue("<font color='red'> 0 条记录无需删除!<font>");
				}else if(countNum <= 10000){//删除是数据为大于0条小于10000
					new Thread(new Runnable() {//启动线程异步执行
						@Override
						public void run() {
							Long T1 = System.currentTimeMillis();
							List<CustomerResource> ls = resourceManageService.getEntity(CustomerResource.class, listSql);
							if(ls != null && ls.size() > 0){
								saveResourceOperationLog(domain,loginUser,"","直接删除资源小于10000条",listSql,commonService);
								resourceManageService.deleteCustomerResourceByListId(ls);
							}
							Long T2 = System.currentTimeMillis();
							logger.info("执行删除,"+countNum+"条共用时："+(T2-T1));
							flipOverTableComponent.getApplication().getMainWindow().showNotification("执行删除,"+countNum+"条共用时："+(T2-T1), Notification.TYPE_HUMANIZED_MESSAGE);
							flipOverTableComponent.refreshToFirstPage();
						}
					}).start();
				}else if(countNum < deleteTaskMaxNum){//删除是数据为大于10000条小于2000000 判断条数
					Calendar calendar = Calendar.getInstance();//判断时间
					if(calendar.get(Calendar.HOUR_OF_DAY) >= permitExecuteDeleteHour){	//当前时间大于允许执行时间
						if(StringUtils.isNotEmpty(listSql) && StringUtils.isNotEmpty(countSql)){//Task参数正常
							if(ResourceDeleteUtil.canExecuteDelete()){//判断是否执行删除
								taskId = domain.getId();
								Task task = new Task();
								task.setId(taskId);
								task.setCreateDate(new Date());
								task.setListSql(listSql);
								task.setCountSql(countSql);
								task.setCreateUserId(loginUser.getId());
								task.setDomain(domain);
								task.setStatus("");
								task.setType("DELETE_CUSTOMER_RESOURCE");
								task.setCountNum(Long.valueOf(countNum));
								task.setJpqlParams(jpqlParams.toString());
								TaskShareData.taskMap.put(taskId, task);
								
								new Thread(new Runnable() {	//启动线程异步执行
									@Override
									public void run() {
										Long T1 = System.currentTimeMillis();
										saveResourceOperationLog(domain,loginUser,"","直接删除资源大于10000条",listSql,commonService);
										ResourceDeleteUtil.getInstance().startExecuteDelete(taskId,domain.getId());
										Long T2 = System.currentTimeMillis();
										logger.info("异步执行删除,"+countNum+"条共用时："+(T2-T1));
										flipOverTableComponent.getApplication().getMainWindow().showNotification("异步执行删除,"+countNum+"条共用时："+(T2-T1), Notification.TYPE_HUMANIZED_MESSAGE);
										flipOverTableComponent.refreshToFirstPage();
									}
								}).start();
							}else{
								this.lbInfo.setValue("<font color='red'>已经有删除任务在执行，不能再执行删除任务，请等候!<font>");
							}
						}else{
							this.lbInfo.setValue("<font color='red'>Task参数异常Sql语句不能为空<font>");
						}
					}else{
						this.lbInfo.setValue("<font color='red'>您的执行不被允许，允许执行删除的时间晚于"+permitExecuteDeleteHour+"点<font>");
					}
				}else{
					this.lbInfo.setValue("<font color='red'>数据量大于"+deleteTaskMaxNum+"不能执行<font>");
				}
			}else{
				this.getApplication().getMainWindow().showNotification("先查询再删除", Notification.TYPE_WARNING_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//执行查询
	private void executeSearch() {
		try {
			this.lbInfo.setValue("");//清空提示信息

			if (!tfFromCallNum.isValid()) {
				tfFromCallNum.getApplication().getMainWindow().showNotification("只能是数字", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			if (!tfToCallNum.isValid()) {
				tfToCallNum.getApplication().getMainWindow().showNotification("只能是数字", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if (!tfTelphone.isValid()) {
				tfTelphone.getApplication().getMainWindow().showNotification("客户号码只能由数字组成", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if (!tfCustomerId.isValid()) {
				tfCustomerId.getApplication().getMainWindow().showNotification("客户编号只能由数字组成", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			selectListJpql = new StringBuilder(" select cr from CustomerResource as cr where 1=1 ");
			selectCountJpql = new StringBuilder(" select count(cr) from CustomerResource as cr where 1=1 ");
			
			whereJpql = new StringBuilder(" ");
			jpqlParams = new StringBuilder("查询条件    ");
			
			// 选择批次
			if (cbxBatch.getValue() != null) {										
				CustomerResourceBatch batch = (CustomerResourceBatch)cbxBatch.getValue();
	 			if(batch != null){
					whereJpql.append(" and cr.id in (select cr.id from CustomerResource as cr,cr.customerResourceBatches as crb where crb.id = "+ batch.getId() + ")  ");
					jpqlParams.append("批次:"+batch.getBatchName()+" ");
				}
			}
			
			// 客户等级
			if (cbxCustomerLevel.getValue() != null){
				CustomerLevel cl = (CustomerLevel)cbxCustomerLevel.getValue();
	 			if(cl != null){
					whereJpql.append(" and cr.customerLevel.id = "+ cl.getId() + " ");
					jpqlParams.append("客户等级:"+cl.getLevelName()+" ");
				}
			}
			
			// 呼叫次数
			if (tfFromCallNum.getValue() != null){
				Integer fromNum = -1;
				try {
					fromNum = Integer.valueOf(tfFromCallNum.getValue().toString().trim());
				} catch (Exception e) {}
				if(fromNum > -1 ){
					whereJpql.append(" and cr.count >= " + "" + fromNum + " ");
					jpqlParams.append("呼叫次数大于:"+fromNum+" ");
				}
			}
			if (tfToCallNum.getValue() != null){
				Integer toNum = -1;
				try {
					toNum = Integer.valueOf(tfToCallNum.getValue().toString().trim());
				} catch (Exception e) {}
				if(toNum > -1 ){
					whereJpql.append(" and cr.count <= " + "" + toNum + " ");
					jpqlParams.append("呼叫次数小于:"+toNum+" ");
				}
			}
			
			// 资源导入者用
			if(cmbResourceImport.getValue() != null) {
				User user = (User) cmbResourceImport.getValue();
				if(user != null && user.getId() != null) {
					whereJpql.append(" and cr.owner.id = " + user.getId());
					jpqlParams.append("资源导入者: ");
					jpqlParams.append(user.getId());
					jpqlParams.append(" ");
				}
			}
			
			// 导入时间
			if (pdfImportDateStart.getValue() != null) {
				whereJpql.append(" and cr.importDate >= '" + sdf.format(pdfImportDateStart.getValue()) + "' ");
				jpqlParams.append("导入时间>=:"+sdf.format(pdfImportDateStart.getValue())+" ");
			}
			if (pdfImportDateEnd.getValue() != null) {
				whereJpql.append(" and cr.importDate <= '" + sdf.format(pdfImportDateEnd.getValue()) + "' ");
				jpqlParams.append("导入时间<=:"+sdf.format(pdfImportDateEnd.getValue())+" ");
			}
			
			// 最后拨打时间
			if (pdfLastDateStart.getValue() != null) {
				whereJpql.append(" and cr.lastDialDate >= '" + sdf.format(pdfLastDateStart.getValue()) + "' ");
				jpqlParams.append("最后拨打时间>=:"+sdf.format(pdfLastDateStart.getValue())+" ");
			}
			if (pdfLastDateEnd.getValue() != null) {
				whereJpql.append(" and cr.lastDialDate <= '" + sdf.format(pdfLastDateEnd.getValue()) + "' ");
				jpqlParams.append("最后拨打时间<=:"+sdf.format(pdfLastDateEnd.getValue())+" ");
			}
			
			// 域
			whereJpql.append(" and cr.domain.id = "+domain.getId());
			
			// 电话号码
			String telephoneNum = StringUtils.trimToEmpty((String) tfTelphone.getValue());
			if (!"".equals(telephoneNum)) {
				whereJpql.append(" and cr.id in (select telephone.customerResource.id from Telephone as telephone where telephone.number = '" + tfTelphone.getValue().toString() + "') ");
			}
			
			// 客户姓名
			if(tfName.getValue() != null && StringUtils.isNotEmpty(tfName.getValue().toString().trim())){
				whereJpql.append(" and cr.name = '" + tfName.getValue().toString().trim() + "' ");
			}
			
			// 客户编号
			if(tfCustomerId.getValue() != null && StringUtils.isNotEmpty(tfCustomerId.getValue().toString().trim())){
				whereJpql.append(" and cr.id = " + tfCustomerId.getValue().toString().trim() + " ");
			}
			
			// 执行查询语句
			executeSelect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//执行查询语句
	private void executeSelect() {
		listSql = selectListJpql.toString() + whereJpql.toString() +" order by cr.id ";
		countSql = selectCountJpql.toString() + whereJpql.toString() ;
		
		flipOverTableComponent.setSearchSql(listSql);
		flipOverTableComponent.setCountSql(countSql);
		flipOverTableComponent.refreshToFirstPage();
	}
	
	//显示删除说明
	private void showIllustration(){
		if(showResourceInfoWindow == null){
			showResourceInfoWindow = new ShowResourceInfoWindow("<font color='red'>1.同一时间只能一个人执行删除任务。<br>2.小于10000条记录可以直接删除。<br>3.大于10000条小于"+deleteTaskMaxNum+"的需要执行删除任务删除。<br>4.删除任务只能在晚于"+permitExecuteDeleteHour+"点。</font>", "400px", "180px");
		}
		this.getApplication().getMainWindow().removeWindow(showResourceInfoWindow);
		this.getApplication().getMainWindow().addWindow(showResourceInfoWindow);
	}
	
	//显示删除日志
	private void showDeleteLog(){
		if(showDeleteLogWindow == null){
			showDeleteLogWindow = new ShowDeleteLogWindow("800px", "300px");
		}
		this.getApplication().getMainWindow().removeWindow(showDeleteLogWindow);
		this.getApplication().getMainWindow().addWindow(showDeleteLogWindow);
	}
		
	//保存删除资源日志
	private void saveResourceOperationLog(Domain domain,User user,String operateIp,String description,String listJpql,CommonService commonService){
		try {
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			operationLog.setProgrammerSee(listJpql);
 			operationLog.setDescription(description);
 			operationLog.setIp("DELETE_RESOURCE");
			commonService.save(operationLog);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//当用户点击的添加分机按钮时，buttonClick调用显示 添加 资源窗口
	private void showAddCustomerResourceForm() {
		if (addCustomerResourceWindow == null) {
			addCustomerResourceWindow = new AddCustomerResourceWindow(this);
		}
		this.getApplication().getMainWindow().addWindow(addCustomerResourceWindow);
	}

	//刷新界面信息
	public void refreshTableInfoByReflect(boolean refreshToFirstPage) {
		if (refreshToFirstPage) {
			flipOverTableComponent.refreshToFirstPage();
		} else {
			flipOverTableComponent.refreshInCurrentPage();
		}
		table.setValue(null);
	}


	//
	private void addFlipOverTableComponent(HorizontalLayout horizontalLayout) {
		String searchSql = "select customerResource from CustomerResource as customerResource where 1=2 ";
		String countSql = "select count(customerResource) from CustomerResource as customerResource where 1=2 ";
		
		flipOverTableComponent = new FlipOverTableComponentNoGo<CustomerResource>(CustomerResource.class, resourceManageService, table, searchSql, countSql, null);
		horizontalLayout.addComponent(flipOverTableComponent);
		horizontalLayout.setComponentAlignment(flipOverTableComponent, Alignment.BOTTOM_RIGHT);
		
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		table.setPageLength(PAGE_LENGTH);
		table.setHeight("210px");
		
		flipOverTableComponent.setPageLength(PAGE_LENGTH, true);
	}
	
}
	//创建资源
	/*private void createBath() {

		if (bathWindow == null) {
			bathWindow = new Window("创建批次");
			bathWindow.center();
			bathWindow.setWidth("300");
			bathWindow.setHeight("250");
		}

		this.getApplication().getMainWindow().addWindow(bathWindow);

		if (bathVerticalLayout == null) {
			bathVerticalLayout = new VerticalLayout();
			bathVerticalLayout.setSizeFull();
			bathVerticalLayout.setSpacing(true);
		}

		bathWindow.addComponent(bathVerticalLayout);

		if (layout1 == null) {
			layout1 = new HorizontalLayout();
			layout1.setSpacing(true);
		}

		bathVerticalLayout.addComponent(layout1);

		if (bathName == null) {
			bathName = new Label("批次名称");
		}
		if (bathField == null) {
			bathField = new TextField();
		}

		layout1.addComponent(bathName);
		layout1.addComponent(bathField);

		if (layout2 == null) {
			layout2 = new HorizontalLayout();
			layout2.setSpacing(true);
		}

		bathVerticalLayout.addComponent(layout2);

		if (remark == null) {
			remark = new Label("备注信息");
		}
		if (textArea == null) {
			textArea = new TextArea();
			textArea.setInputPrompt("请输入与批次相关的备注信息！");
		}

		layout2.addComponent(remark);
		layout2.addComponent(textArea);

		if (layout3 == null) {
			layout3 = new HorizontalLayout();
			layout3.setSpacing(true);
		}
		bathVerticalLayout.addComponent(layout3);

		if (saveBath == null) {
			saveBath = new Button("保存");
			saveBath.addListener(new ClickListener() {
				private static final long serialVersionUID = 3397963229345521910L;
				@Override
				public void buttonClick(ClickEvent event) {

					try {
						event.getComponent().getApplication().getMainWindow().removeWindow(bathWindow);
						bathWindow.removeAllComponents();

						String batchName = StringUtils.trimToEmpty((String) bathField.getValue());
						String note = StringUtils.trimToEmpty((String) textArea.getValue());

						if ("".equals(batchName)) { // jrh 添加
							table.getApplication().getMainWindow().showNotification("批次名称不能为空！", Notification.TYPE_WARNING_MESSAGE);
							return;
						}
						// 批次
						CustomerResourceBatch customerResourceBatch = new CustomerResourceBatch();
						customerResourceBatch.setBatchName(batchName);
						customerResourceBatch.setCreateDate(new Date());
						customerResourceBatch.setDomain(domain);
						customerResourceBatch.setUser(loginUser);
						customerResourceBatch.setNote(note);

						// 持久化批次
						CustomerResourceBatch batch = resourceBatchService.update(customerResourceBatch);

						OperationLogUtil.simpleLog(loginUser, "资源管理-创建批次：" + batch.getBatchName());

						// 获被持久化后批次的id
						Long batchId = batch.getId();

						// 资源
						List<CustomerResource> customerResources = resourceManageService.getEntity(CustomerResource.class, listSql);

						// 向维护批资与资源关联关系的中间表插入数据
						for (CustomerResource customerResource : customerResources) {
							resourceManageService.executeUpdate("insert into ec2_customer_resource_ec2_customer_resource_batch(customerresources_id,customerresourcebatches_id) values ("+ customerResource.getId() + "," + batchId + ")");
						}

						table.getApplication().getMainWindow().showNotification("批次创建成功！");
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("LXY 在资源管理模块，创建批次出现异常：-->" + e.getMessage(), e);
						table.getApplication().getMainWindow().showNotification("批次创建失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
					}

				}
			});
		}
		layout3.addComponent(saveBath);
		layout3.setComponentAlignment(saveBath, Alignment.BOTTOM_LEFT);

		if (cancelBath == null) {
			cancelBath = new Button("取消");
			cancelBath.addListener(new ClickListener() {
				private static final long serialVersionUID = -2498150484045470684L;
				@Override
				public void buttonClick(ClickEvent event) {
					event.getComponent().getApplication().getMainWindow().removeWindow(bathWindow);
					bathWindow.removeAllComponents();
				}
			});
		}
		layout3.addComponent(cancelBath);
		layout3.setComponentAlignment(cancelBath, Alignment.BOTTOM_RIGHT);

	}*/


