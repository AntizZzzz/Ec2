package com.jiangyifen.ec2.ui.mgr.ordermanange;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.DiliverStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.QualityStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.OrderManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 导出订单
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class ExportOrderWindow extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String EXPORT_DETIAL="导出订单及明细";
	
	/**
	 * 按钮
	 */
	private HorizontalLayout buttonsLayout;
	private OptionGroup optionGroup;
	private Button exportButton;
	private Button cancel;
	
	
	/**
	 * 其他组件
	 */
	private CommonService commonService;
	
	
	//进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	private OrderManagement orderManagement;
	private int processCount;

	/**
	 * 构造器
	 * @param resourceImport
	 */
	public ExportOrderWindow(OrderManagement orderManagement) {
		this.initService();
		this.center();
		this.setModal(true);
		this.orderManagement = orderManagement;
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("选择导出类型");

		
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		// 添加Window内最大的Layout
		windowContent.setSizeUndefined();// .setSizeFull();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		VerticalLayout exportTypeSelectLayout = buildExportTypeSelectLayout();
		windowContent.addComponent(exportTypeSelectLayout);
		
		// 按钮输出
		HorizontalLayout buttonsLayout = buildButtonsLayout();
		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout,
				Alignment.BOTTOM_RIGHT);
		
		//创建进度条组件
		progressOuterLayout=orderManagement.getProgressLayout();
		progressLayout=new HorizontalLayout();
		progressOuterLayout.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
	}
	
	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		commonService=SpringContextHolder.getBean("commonService");
	}
	
	/**
	 * 创建让用户选择表格头的组件
	 * @return
	 */
	private VerticalLayout buildExportTypeSelectLayout() {
		VerticalLayout exportTypeSelectLayout=new VerticalLayout();
		exportTypeSelectLayout.setSizeUndefined();
		
		optionGroup=new OptionGroup("导出类型选择", Arrays.asList(EXPORT_DETIAL,"导出订单"));
		exportTypeSelectLayout.addComponent(optionGroup);
		
		return exportTypeSelectLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);

		//按钮
		exportButton = new Button("导出");
		exportButton.addListener(this);
		buttonsLayout.addComponent(exportButton);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}


	@Override
	public void attach() {
		super.attach();
		optionGroup.setValue(EXPORT_DETIAL);
	}


	/**
	 * 按钮操作
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == exportButton){
			new Thread(){
				@Override
				public void run() {
					//初始化进度条
					pi.setEnabled(true);
					pi.setValue(0f);
					progressLayout.addComponent(new Label("导出进度:"));
					progressLayout.addComponent(pi);
					
					Long startTime=System.currentTimeMillis();
					executeExport();
					Long endTime=System.currentTimeMillis();
					
					//导入完成后进度条的处理进度条
					pi.setEnabled(false);
					progressLayout.removeAllComponents();
					
					String info="导出数据耗时:"+(endTime-startTime)/1000+"秒";
					NotificationUtil.showWarningNotification(ExportOrderWindow.this, info);
					logger.info(info);
				}
			}.start();
			//Excel 上传完毕移除窗口
			ExportOrderWindow.this.getParent().removeWindow(ExportOrderWindow.this);
		}else if(event.getButton() == cancel){
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * 导出数据的Excel表格
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void executeExport() {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			file = new File(new String(("订单信息-"+System.currentTimeMillis()+".xls").getBytes("GBK"),
					"ISO-8859-1"));
			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = writableWorkbook.createSheet("Sheet0", 0);
			
			//NOTE ： 可能超过6w条数据
			
//			查资源1000条 loop{
//				查询描述字段，拼装到Excel中
//			}
			
			String sqlSelect=orderManagement.getSqlSelect();
			String sqlCount=orderManagement.getSqlCount();
			
			Long totalLong=(Long)commonService.excuteSql(sqlCount, ExecuteType.SINGLE_RESULT);
			
			//记录总数和已经完成的任务条数，从而计算完成的百分比
			float taskNum=0;
			float totalNum=totalLong.floatValue();
			
			//记录处理的条数，以便分页
			processCount=0;
			
			//记录sheet的数量，以便对sheet标号
			int sheetCount=0;
			
			// ===========以步长1000取 Order==================//
			Long recordId = Long.MAX_VALUE;
			int step = 1000;
			// 每次加载step步长条数据
			List<Order> orders = null;
			for (;;) {
				//判断是不是创建新的TabSheet页,因为有详单，所以默认每页10000就下一sheet页
				if ((processCount % 10000) == 0) {
					sheet = writableWorkbook.createSheet("Sheet" + sheetCount,sheetCount);
					generateHeader(sheet);//生成Sheet的表格
					processCount=0;
					sheetCount++;
				}
				
				// batchId 隐含 domain 概念
//				
				String newSqlSelect=sqlSelect.replace("order", "and e.id<"+recordId+" order");

				Long startTime=System.currentTimeMillis();
				orders = (List<Order>) commonService.getEntityManager().createQuery(newSqlSelect).setFirstResult(0).setMaxResults(step).getResultList();
				
				Long endTime=System.currentTimeMillis();
				logger.info("加载"+orders.size()+"数据耗时:"+(endTime-startTime)/1000+"秒");
				
				//在SQL中不适用distinct对取最小值没有影响
				if (orders.size() > 0) {
					// 取得最小的Id值
					recordId = ((Order)orders.get(orders.size() - 1)).getId();
				} else {
					break;	// 如果资源加载完，则跳出for循环
				}

				Long startTime1=System.currentTimeMillis();
				//将资源存入Excel中
				for(Order order:orders){
					taskNum++;
					generateSheetOneRow(order,sheet,++processCount);
					float percentage = taskNum/totalNum;  //显示比例
					pi.setValue(percentage);
				}
				Long endTime1=System.currentTimeMillis();
				logger.info("写入Excel中"+orders.size()+"耗时:"+(endTime1-startTime1)/1000+"秒");
				
			}
			// =============================================================//
			
			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建Excel文件出现异常！");
		} finally {
			if (writableWorkbook != null) {
				try {
					writableWorkbook.close();
				} catch (Exception e1) {
					logger.info("下载Excel文件出错,关闭要下载的Excel文件失败！");
				}
			}
		}

		// 弹出下载文件窗口
		Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		Resource resource = new FileResource(file, orderManagement.getApplication());
		downloader.setSource(resource);
		orderManagement.addComponent(downloader);
		
	}

	/**
	 * 生成sheet页面的表格头部
	 * @param sheet
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 */
	private void generateHeader(WritableSheet sheet) throws Exception {
		
		WritableFont defaultKeywordFont = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.GRAY_50);
		WritableCellFormat defaultFormat = new WritableCellFormat(defaultKeywordFont);
		jxl.write.Label label0=new jxl.write.Label(0, 0, "ID",defaultFormat);
		jxl.write.Label label1=new jxl.write.Label(1, 0, "订货人姓名",defaultFormat);
		jxl.write.Label label2=new jxl.write.Label(2, 0, "订货人电话",defaultFormat);
		jxl.write.Label label3=new jxl.write.Label(3, 0, "订单总价",defaultFormat);
		jxl.write.Label label4=new jxl.write.Label(4, 0, "发货状态",defaultFormat);
		jxl.write.Label label5=new jxl.write.Label(5, 0, "订单状态",defaultFormat);
		jxl.write.Label label6=new jxl.write.Label(6, 0, "下单日期",defaultFormat);
		jxl.write.Label label7=new jxl.write.Label(7, 0, "下单项目",defaultFormat);
		jxl.write.Label label8=new jxl.write.Label(8, 0, "下单坐席",defaultFormat);
		jxl.write.Label label9=new jxl.write.Label(9, 0, "质检管理员",defaultFormat);
		jxl.write.Label label10=new jxl.write.Label(10, 0, "收货地址",defaultFormat);
		jxl.write.Label label11=new jxl.write.Label(11, 0, "备注",defaultFormat);
		sheet.addCell(label0);
		sheet.addCell(label1);
		sheet.addCell(label2);
		sheet.addCell(label3);
		sheet.addCell(label4);
		sheet.addCell(label5);
		sheet.addCell(label6);
		sheet.addCell(label7);
		sheet.addCell(label8);
		sheet.addCell(label9);
		sheet.addCell(label10);
		sheet.addCell(label11);
	}

	/**
	 * 生成一个表单页中的一行
	 * @param order 
	 * @param sheet //表格的Sheet
	 * @param sheetRowNum //记录写在Sheet的哪一行
	 * @throws Exception 
	 */
	private void generateSheetOneRow(Order order, WritableSheet sheet,int sheetRowNum) throws Exception {
		//========================================================//
		//==========================导出订单信息======================//
		//========================================================//
		if(order==null) return;
		
		sheet.addCell(new jxl.write.Label(0, sheetRowNum, order.getId()+""));
		sheet.addCell(new jxl.write.Label(1, sheetRowNum, order.getCustomerName()));
		sheet.addCell(new jxl.write.Label(2, sheetRowNum, order.getCustomerPhoneNumber()));
		sheet.addCell(new jxl.write.Label(3, sheetRowNum, order.getTotalPrice()+""));
		DiliverStatus diliverStatus = order.getDiliverStatus();
		if(diliverStatus!=null){
			sheet.addCell(new jxl.write.Label(4, sheetRowNum, diliverStatus.getName()));
		}
		QualityStatus qualityStatus=order.getQualityStatus();
		if(qualityStatus!=null){
			sheet.addCell(new jxl.write.Label(5, sheetRowNum, qualityStatus.getName()));
		}
		
		String generateDate="";
		if(order.getGenerateDate()!=null){
			generateDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getGenerateDate());
		}
		sheet.addCell(new jxl.write.Label(6, sheetRowNum,generateDate));
		sheet.addCell(new jxl.write.Label(7, sheetRowNum, order.getProjectName()));
		Long csrUserId=order.getCsrUserId();
		if(csrUserId!=null){
			User user=(User)commonService.excuteSql("select u from User u where u.id="+csrUserId, ExecuteType.SINGLE_RESULT);
			if(user!=null){
				sheet.addCell(new jxl.write.Label(8, sheetRowNum,user.getUsername()+"("+user.getEmpNo()+")" ));
			}
		}
		Long mgrUserId=order.getMgrUserId();
		if(mgrUserId!=null){
			User user=(User)commonService.excuteSql("select u from User u where u.id="+csrUserId, ExecuteType.SINGLE_RESULT);
			if(user!=null){
				sheet.addCell(new jxl.write.Label(9, sheetRowNum,user.getUsername()));
			}
		}
		
		String address="";
		String street="";
		if(order.getStreet()!=null){
			street=order.getStreet();
		}
		if(order.getProvince()!=null&&order.getCity()!=null&&order.getCounty()!=null){
			address=order.getProvince()+"(省)"+order.getCity()+"(市)"+order.getCounty()+"(区、县)"+street;
		}
		sheet.addCell(new jxl.write.Label(10, sheetRowNum, address));
		sheet.addCell(new jxl.write.Label(11, sheetRowNum, order.getNote()));
		
		if(optionGroup.getValue().equals(EXPORT_DETIAL)){
//			"导出订单及明细","导出订单"
			//导出订单详情
			Set<Orderdetails> orderDetails = order.getOrderdetails();
			for(Orderdetails tmpOrderDetail:orderDetails){
				++processCount;
				//样式
				WritableFont detailFormat = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.BLUE_GREY);
				WritableCellFormat detailfCellFormat = new WritableCellFormat(detailFormat);
				detailfCellFormat.setBackground(jxl.format.Colour.GREY_25_PERCENT);
				
				sheet.addCell(new jxl.write.Label(1, processCount,"详单编号" ,detailfCellFormat));
				sheet.addCell(new jxl.write.Label(2, processCount,tmpOrderDetail.getId().toString(),detailfCellFormat));
				sheet.addCell(new jxl.write.Label(3, processCount,"商品名称",detailfCellFormat));
				String commodityName = tmpOrderDetail.getCommodityName();
				if(commodityName!=null){
					sheet.addCell(new jxl.write.Label(4, processCount,commodityName,detailfCellFormat));
				}
				Commodity commodity = tmpOrderDetail.getCommodity();
				Double commodityPrice=0d;
				String description="";
				if(commodity!=null){
					commodityPrice = commodity.getCommodityPrice();
					description=commodity.getDescription();
		 		}
				sheet.addCell(new jxl.write.Label(5, processCount,"商品单价",detailfCellFormat));
				sheet.addCell(new jxl.write.Label(6, processCount, commodityPrice.toString(),detailfCellFormat));
				 
				String salePriceStr = "";
				if(tmpOrderDetail.getSalePrice() != null) {
					salePriceStr = tmpOrderDetail.getSalePrice().toString();
				}
				sheet.addCell(new jxl.write.Label(7, processCount,"售出单价",detailfCellFormat));
				sheet.addCell(new jxl.write.Label(8, processCount, salePriceStr,detailfCellFormat));
				
				String subTotalPriceStr = "";
				if(tmpOrderDetail.getSubTotalPrice() != null) {
					subTotalPriceStr = tmpOrderDetail.getSubTotalPrice().toString();
				}
				sheet.addCell(new jxl.write.Label(9, processCount,"小计",detailfCellFormat));
				sheet.addCell(new jxl.write.Label(10, processCount, subTotalPriceStr,detailfCellFormat));
				
				sheet.addCell(new jxl.write.Label(11, processCount,"订购数量",detailfCellFormat));
				Integer orderNum = tmpOrderDetail.getOrderNum();
				if(orderNum!=null){
					sheet.addCell(new jxl.write.Label(12, processCount, orderNum.toString(),detailfCellFormat));
				}
				sheet.addCell(new jxl.write.Label(13, processCount,"商品描述",detailfCellFormat));
				sheet.addCell(new jxl.write.Label(14, processCount,description,detailfCellFormat));
			}
		}else{
			//do nothing
		}
		
	}
	

	
}
