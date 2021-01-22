package com.jiangyifen.ec2.ui.csr.workarea.myresource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.asteriskjava.fastagi.AgiChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.jiangyifen.ec2.service.mgr.impl.ImportResourceServiceImpl;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.ui.csr.workarea.mycustomer.CustomerResourceSimpleFilter;
import com.jiangyifen.ec2.ui.csr.workarea.mycustomer.OutgoingDialForResourceWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 我的客户信息查看与管理界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MyResourcesTabView extends VerticalLayout implements ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "name", "company", "telephones", "sex", "birthday", "count", "customerLevel", "lastDialDate", /*"expireDate",*/ "importDate"};
	
	private final String[] COL_HEADERS = new String[] {"客户编号", "客户姓名", "公司名称", "电话号码", "性别", "生日", "联系次数", "客户级别", "最近联系时间", /*"过期时间",*/ "导入时间"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	
	private Button addCustomer;									// 添加单条资源按钮
	private Notification notification;							// 提示信息
	private Upload batchAddCustomer;							// 批量导入按钮
	private Table myResourceTable;								// 资源显示表格
	private CustomerResourceSimpleFilter customerResourceSimpleFilter;		// 资源的搜索组件
	private FlipOverTableComponent<CustomerResource> resourceTableFlip;		// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 
	private OutgoingDialForResourceWindow outgoingDialWindow;	// 客户信息弹屏窗口
	private AddCustomerResourceWindow addCustomerResourceWindow;// 资源添加界面
	
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限

	//进度条组件
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;

	private CustomerResourceService customerResourceService;
	private ImportResourceService importResourceService;
	private File excelFile;
	public MyResourcesTabView() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);

		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		importResourceService = SpringContextHolder.getBean("importResourceService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		customerResourceSimpleFilter = new CustomerResourceSimpleFilter();
		customerResourceSimpleFilter.setHeight("-1px");
		this.addComponent(customerResourceSimpleFilter);
		
		// 创建客户表格
		createCustomersTable();
		
		// 为表格添加右键单击事件
		addActionToTable(myResourceTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);
		
		// 创建表格下方左侧的组件（添加资源组件，批量导入组件）
		createTableFooterLeftComponents(tableFooter);
		
		// 创建翻页组件
		createTableFlipComponent(tableFooter);

		// 根据分辨率设置表格行数
		setTablePageLength();
		
		// 为我的资源Tab 页上方的搜索组件传递翻页组件对象
		customerResourceSimpleFilter.setResourceTableFlip(resourceTableFlip);
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
	}
	
	/**
	 * 创建客户显示表格
	 */
	private void createCustomersTable() {
		myResourceTable = createFormatColumnTable();
		myResourceTable.setWidth("100%");
		myResourceTable.setHeight("-1px");
		myResourceTable.setImmediate(true);
		myResourceTable.setSelectable(true);
		myResourceTable.setStyleName("striped");
		myResourceTable.setNullSelectionAllowed(false);
		myResourceTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(myResourceTable);
		
		myResourceTable.addGeneratedColumn("telephones", new DialColumnGenerator());
		myResourceTable.setColumnWidth("telephones", 192);
	}

	/**
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
            		return dateFormat.format((Date)property.getValue());
                } 
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class DialColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("telephones")) {
				Set<Telephone> telephones = ((CustomerResource) itemId).getTelephones();
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, MyResourcesTabView.this);
			} 
			return null;
		}
	}
	
	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(customerAllInfoWindow == null) {			// 只有单击右键时才创建客户信息查看窗口及相关组件
					createCustomerAllInfoWindow();
				}
				
				table.select(target);
				table.getApplication().getMainWindow().removeWindow(customerAllInfoWindow);
				table.getApplication().getMainWindow().addWindow(customerAllInfoWindow);
				CustomerResource customerResource = (CustomerResource) table.getValue();
				customerAllInfoWindow.initCustomerResource(customerResource);
				
				if(action == BASEINFO) {
					customerAllInfoWindow.echoCustomerBaseInfo(customerResource);
					customerInfoTabSheet.setSelectedTab(0);
				} else if(action == DESCRIPTIONINFO) {
					customerAllInfoWindow.echoCustomerDescription(customerResource);
					customerInfoTabSheet.setSelectedTab(1);
				} else if(action == ADDRESSINFO) {
					customerAllInfoWindow.echoCustomerAddress(customerResource);
					customerInfoTabSheet.setSelectedTab(2);
				} else if(action == HISTORYRECORD) {
					customerAllInfoWindow.echoHistoryRecord(customerResource);
					customerInfoTabSheet.setSelectedTab(3);
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {BASEINFO, DESCRIPTIONINFO, ADDRESSINFO, HISTORYRECORD};
				}
				return null;
			}
		});
	}

	/**
	 * 创建表格下方左侧的组件（添加资源组件，批量导入组件）
	 * @param tableFooter
	 */
	private void createTableFooterLeftComponents(HorizontalLayout tableFooter) {
		HorizontalLayout footerLeftLayout = new HorizontalLayout();
		footerLeftLayout.setSpacing(true);
		tableFooter.addComponent(footerLeftLayout);
		
		addCustomer = new Button("添加我的资源", this);
		addCustomer.setImmediate(true);
		addCustomer.setStyleName("default");
		footerLeftLayout.addComponent(addCustomer);
		
		// 选择上传文件按钮
		batchAddCustomer = new Upload();
		batchAddCustomer.setImmediate(true);
		batchAddCustomer.setButtonCaption("批量添加资源");
		batchAddCustomer.setStyleName("default");
		batchAddCustomer.addListener(new Upload.SucceededListener() {
			public void uploadSucceeded(SucceededEvent event) {
				new Thread(){
					@Override
					public void run() {
						Long startTime=System.currentTimeMillis();
						executeImport();
						Long endTime=System.currentTimeMillis();
						logger.info("导入数据耗时:"+(endTime-startTime)/1000+"秒");
					}
				}.start();
			}
		});
		this.assignReceiverForUpload(batchAddCustomer);
		footerLeftLayout.addComponent(batchAddCustomer);
		
		//创建进度条组件
		progressLayout=new HorizontalLayout();
		footerLeftLayout.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
	}

	/**
	 * 由 buildPathLayout 调用，指定上传的Excel文件的存储名称和位置
	 */
	private void assignReceiverForUpload(Upload upload) {
		upload.setReceiver(new Upload.Receiver() {

			public OutputStream receiveUpload(String filename, String mimeType) {
				// Output stream to write to
				FileOutputStream fos = null;
				String userName =loginUser.getUsername() ;
				String dateStr = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss")
						.format(new Date());

				filename = ConfigProperty.PATH + "/" + dateStr + "_" + userName
						+ "_" + filename;
				excelFile = new File(filename);
				if (!excelFile.exists()) {
					if (!excelFile.getParentFile().exists()) {
						excelFile.getParentFile().mkdirs();
					}
					try {
						excelFile.createNewFile();
					} catch (IOException e) {
						logger.error("创建文件失败:"+filename,e);
						throw new RuntimeException("无法再指定位置创建新Excel文件！");
					}
				} else {
					throw new RuntimeException("Excel文件已经存在，请重新创建！");
				}
				try {
					fos = new FileOutputStream(excelFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();//应该不会出现
				}
				return fos;
			}
		});
	}
	
	/**
	 * 执行导入数据库操作
	 * 导入成功返回true，导入失败返回false
	 */
	public Boolean executeImport() {
		//初始化进度条
		pi.setEnabled(true);
		pi.setValue(0f);
		progressLayout.addComponent(new Label("上传进度:"));
		progressLayout.addComponent(pi);
		batchAddCustomer.setEnabled(false);
		//调用导入数据的方法导入数据，更新表格信息
		String importMessage="";
		//chenhb 20140728 去重设置为true
		try {
			Map<String, Long> importDataResult = importResourceService.importData(excelFile, null,loginUser,false,pi,true);
			Long successNum=importDataResult.get(ImportResourceServiceImpl.IMPORT_SUCCESS);
			Long hasResourceUpdateNum=importDataResult.get(ImportResourceServiceImpl.HAS_RESOURCE_UPDATE);
			Long hasCustomerResourceNum=importDataResult.get(ImportResourceServiceImpl.HAS_CUSTOMER_IGNORE);
			Long invalidNum=importDataResult.get(ImportResourceServiceImpl.INVALID_NUMBER);
			Long elapsedTime = importDataResult.get(ImportResourceServiceImpl.ELAPSED_TIME);
			importMessage="成功导入"+successNum+"条,</br>";
			importMessage+="更新数据"+hasResourceUpdateNum+"条,</br>";
			importMessage+="客户资源"+hasCustomerResourceNum+"条,</br>";
			importMessage+="无效号码"+invalidNum+"条,</br>";
			importMessage+="总共耗时"+elapsedTime+"秒.";
			this.refreshTable(true);
			//导入完成后进度条的处理进度条
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
		} catch (Exception e) {
			//此处应该合理处理提示信息
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
			notification.setCaption(e.getMessage());
			this.getApplication().getMainWindow().showNotification(notification);
			batchAddCustomer.setEnabled(true);
			e.printStackTrace();
			return false;
		}
		NotificationUtil.showWarningNotification(this.getApplication(),importMessage);
		batchAddCustomer.setEnabled(true);
		return true;
	}

	/**
	 * 创建翻页组件
	 */
	private void createTableFlipComponent(HorizontalLayout tableFooter) {
		// 设置初始查询语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		String countSql = "select count(c) from CustomerResource as c where c.owner.id = " +loginUser.getId()+ " and c.lastDialDate >= '"+dateStrs[0]+"' and c.lastDialDate <= '"+dateStrs[1]+"'";
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.count asc, c.lastDialDate desc";
		
		resourceTableFlip = new FlipOverTableComponent<CustomerResource>(CustomerResource.class, 
				customerResourceService, myResourceTable, searchSql , countSql, null);
		resourceTableFlip.setSearchSql(searchSql);
		resourceTableFlip.setCountSql(countSql);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			resourceTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		myResourceTable.setVisibleColumns(VISIBLE_PROPERTIES);
		myResourceTable.setColumnHeaders(COL_HEADERS);
		
		tableFooter.addComponent(resourceTableFlip);
		tableFooter.setComponentAlignment(resourceTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			myResourceTable.setPageLength(29);
			resourceTableFlip.setPageLength(29, false);
		} else if(screenResolution[1] >= 1050) {
			myResourceTable.setPageLength(27);
			resourceTableFlip.setPageLength(27, false);
		} else if(screenResolution[1] >= 900) {
			myResourceTable.setPageLength(20);
			resourceTableFlip.setPageLength(20, false);
		} else if(screenResolution[1] >= 768) {
			myResourceTable.setPageLength(13);
			resourceTableFlip.setPageLength(13, false);
		} else {
			myResourceTable.setPageLength(10);
			resourceTableFlip.setPageLength(10, false);
		}
	}
	
	/**
	 *  创建客户信息查看窗口及相关组件
	 */
	private void createCustomerAllInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.csr);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);
		
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}
	
	/**
	 * 创建呼叫客户时需要显示个各种信息的组件
	 */
	private void createOutgoingDialWindow() {
		outgoingDialWindow = new OutgoingDialForResourceWindow();
		if(screenResolution[0] >= 1366) {
			outgoingDialWindow.setWidth("1190px");
		} else {
			outgoingDialWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			outgoingDialWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			outgoingDialWindow.setHeight("560px");
		} else {
			outgoingDialWindow.setHeight("510px");
		}

		outgoingDialWindow.setResizable(false);
		outgoingDialWindow.setStyleName("opaque");
		outgoingDialWindow.setEchoModifyByReflect(this);
		outgoingDialWindow.setProprietaryCustomersTableFlip(resourceTableFlip);
	}
	
	/**
	 * 供 CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		CustomerResource currentRecord = (CustomerResource) myResourceTable.getValue();
		resourceTableFlip.refreshInCurrentPage();
		for(int i = 0; i < myResourceTable.getItemIds().size(); i++) {
			CustomerResource resource = (CustomerResource) myResourceTable.getItemIds().toArray()[i];
			if(resource != null && currentRecord != null && resource.getId().equals(currentRecord.getId())) {
				myResourceTable.select(resource);	break;
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == addCustomer) {
			showAddCustomerResourceForm();
		}
	}

	/**
	 * 当用户点击的添加分机按钮时，buttonClick调用显示 添加 资源窗口
	 */
	private void showAddCustomerResourceForm() {
		if(addCustomerResourceWindow == null) {
			addCustomerResourceWindow = new AddCustomerResourceWindow(this);
		}
		this.getApplication().getMainWindow().addWindow(addCustomerResourceWindow);
	}

	/**
	 * 刷新我的专有客户显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			resourceTableFlip.refreshToFirstPage();
		} else {
			resourceTableFlip.refreshInCurrentPage();
		}
	}
	
	/**
	 * 通过反射的方式刷新界面信息
	 * @param refreshToFirstPage
	 */
	public void refreshTableInfoByReflect(boolean refreshToFirstPage) {
		this.refreshTable(refreshToFirstPage);
		myResourceTable.setValue(null);
	}

	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), MyResourcesTabView.this);
	}
	
	/**
	 * 当电话振铃时，弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(outgoingDialWindow);
		// 在弹屏前，回显弹屏中的信息
		outgoingDialWindow.echoInformations(customerResource);
		outgoingDialWindow.setAgiChannel(agiChannel);
		outgoingDialWindow.center();
		this.getApplication().getMainWindow().addWindow(outgoingDialWindow);
	}

}
