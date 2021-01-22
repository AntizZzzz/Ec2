package com.jiangyifen.ec2.ui.mgr.resourceimport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeywordDefault;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceImport;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 创建追加资源
 * 
 * @author chb
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
public class SelectHeaderWindow extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 选择表格头部的表头组件输出
	 */
	private VerticalLayout headersLayout;
	
	/**
	 * 按钮
	 */
	private HorizontalLayout buttonsLayout;
	private Button selectAllButton;
	private Button deselectAllButton;
	private Button exportButton;
	private Button cancel;
	
	private List<CheckBox> defaultCheckBox;
	private List<CheckBox> mgrCheckBox;
	
	/**
	 * 其他组件
	 */
	private ResourceImport resourceImport;
	private CustomerResourceBatch batch;
	private CommonService commonService;
	private Domain domain;
	
	
	//进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	
	
	/**
	 * 导出表格使用
	 */
	private List<String> allKeywords;
	private List<String> defaultKeywords;
	private List<String> mgrKeywords ;
	
	private User loginUser=SpringContextHolder.getLoginUser();

	/**
	 * 构造器
	 * @param resourceImport
	 */
	public SelectHeaderWindow(ResourceImport resourceImport) {
		this.initService();
		this.center();
		this.setModal(true);
		this.resourceImport = resourceImport;
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("选择要导出的表头字段");

		batch=(CustomerResourceBatch)resourceImport.getTable().getValue();
		
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		// 添加Window内最大的Layout
		windowContent.setSizeUndefined();// .setSizeFull();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		headersLayout =new VerticalLayout();
		headersLayout.setSizeUndefined();
		windowContent.addComponent(headersLayout);

		// 按钮输出
		HorizontalLayout buttonsLayout = buildButtonsLayout();
		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout,
				Alignment.BOTTOM_RIGHT);
		
		//创建进度条组件
		progressOuterLayout=resourceImport.getProgressLayout();
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
		domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");
	}
	
	/**
	 * 创建让用户选择表格头的组件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private VerticalLayout buildHeadersLayout() {
		VerticalLayout headersInnerLayout=new VerticalLayout();
		headersInnerLayout.setSizeUndefined();
		//默认固定字段的输出
		List<String> defaultKeywords = new ArrayList<String>();
		for(TableKeywordDefault keyword:TableKeywordDefault.values()){
			defaultKeywords.add(keyword.getName());
		}
		VerticalLayout defaultKeywordsLayout=new VerticalLayout();
		defaultKeywordsLayout.addComponent(new Label("<b>默认字段</b>",Label.CONTENT_XHTML));
		int defaultRows = (defaultKeywords.size()+5)/6;
		defaultCheckBox=new ArrayList<CheckBox>();
		for(int i=0;i<defaultRows;i++){
			HorizontalLayout rowLayout=new HorizontalLayout();
			defaultKeywordsLayout.addComponent(rowLayout);
			for(int j=0;j<6;j++){
				if(i*6+j<defaultKeywords.size()){
					String keyword=defaultKeywords.get(i*6+j);
					CheckBox checkBox=new CheckBox(keyword);
					checkBox.setData(keyword);
					checkBox.setValue(true);
					defaultCheckBox.add(checkBox);
					rowLayout.addComponent(checkBox);
				}
			}
		}
		headersInnerLayout.addComponent(defaultKeywordsLayout);
//		private List<CheckBox> defaultCheckBox;
//		private List<CheckBox> mgrCheckBox;
		
		//管理员字段的输出
		String nativeSql=" select distinct key from ec2_customer_resource_description where domain_id="+domain.getId();
		List<String> mgrKeywords=(List<String>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
		
		VerticalLayout mgrKeywordsLayout=new VerticalLayout();
		mgrKeywordsLayout.addComponent(new Label("<b>管理员扩展字段</b>",Label.CONTENT_XHTML));
		int mgrRows = (mgrKeywords.size()+5)/6;
		mgrCheckBox=new ArrayList<CheckBox>();
		for(int i=0;i<mgrRows;i++){
			HorizontalLayout rowLayout=new HorizontalLayout();
			mgrKeywordsLayout.addComponent(rowLayout);
			for(int j=0;j<6;j++){
				if(i*6+j<mgrKeywords.size()){
					String keyword=mgrKeywords.get(i*6+j);
					CheckBox checkBox=new CheckBox(keyword);
					checkBox.setData(keyword);
					mgrCheckBox.add(checkBox);
					rowLayout.addComponent(checkBox);
				}
			}
		}
		headersInnerLayout.addComponent(mgrKeywordsLayout);
		return headersInnerLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);

		//全选按钮
		selectAllButton = new Button("全选");
		selectAllButton.addListener(this);
		buttonsLayout.addComponent(selectAllButton);

		//全不选按钮
		deselectAllButton = new Button("全不选");
		deselectAllButton.addListener(this);
		buttonsLayout.addComponent(deselectAllButton);
		
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
		batch= (CustomerResourceBatch)resourceImport.getTable().getValue();
		headersLayout.removeAllComponents();
		headersLayout.addComponent(buildHeadersLayout());
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
					NotificationUtil.showWarningNotification(SelectHeaderWindow.this.getApplication(), info);
					logger.info(info);
				}
			}.start();
			//Excel 上传完毕移除窗口
			SelectHeaderWindow.this.getParent().removeWindow(SelectHeaderWindow.this);
		}else if(event.getButton() == cancel){
			this.getParent().removeWindow(this);
		}else if(event.getButton() == selectAllButton){
			for(CheckBox checkBox:mgrCheckBox){
				checkBox.setValue(true);
			}
		}else if(event.getButton() == deselectAllButton){
			for(CheckBox checkBox:mgrCheckBox){
				checkBox.setValue(false);
			}
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
		OperationLogUtil.simpleLog(loginUser, "导出批次"+batch.getBatchName()+" Id:"+batch.getId());
		try {
			file = new File(new String((batch.getBatchName()+System.currentTimeMillis()+".xls").getBytes("GBK"),
					"ISO-8859-1"));
			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = writableWorkbook.createSheet("Sheet0", 0);
			
			//NOTE ： 可能超过6w条数据
			
//			查资源1000条 loop{
//				查询描述字段，拼装到Excel中
//			}
			
			String countSql="SELECT count(*) FROM ec2_customer_resource_ec2_customer_resource_batch where  customerresourcebatches_id="+batch.getId();
			Long totalLong=(Long)commonService.excuteNativeSql(countSql, ExecuteType.SINGLE_RESULT);
			
			//记录总数和已经完成的任务条数，从而计算完成的百分比
			float taskNum=0;
			float totalNum=totalLong.floatValue();
			
			//记录处理的条数，以便分页
			int processCount=0;
			
			//记录sheet的数量，以便对sheet标号
			int sheetCount=0;
			
			// ===========以步长3000取 customerresources_id ==================//
			Long recordId = Long.MAX_VALUE;
			int step = 3000;
			// 每次加载step步长条数据
			List<Long> records = null;
			for (;;) {
				//判断是不是创建新的TabSheet页
				if ((processCount % 60000) == 0) {
					sheet = writableWorkbook.createSheet("Sheet" + sheetCount,sheetCount);
					generateHeader(sheet);//生成Sheet的表格
					processCount=0;
					sheetCount++;
				}
				
				// batchId 隐含 domain 概念
				String nativeSql = "SELECT customerresources_id FROM ec2_customer_resource_ec2_customer_resource_batch where customerresources_id<"+recordId+" and customerresourcebatches_id ="+batch.getId()+" order by customerresources_id desc";

				Long startTime=System.currentTimeMillis();
				records = (List<Long>) commonService.loadStepRows(nativeSql,step);
				Long endTime=System.currentTimeMillis();
				logger.info("加载"+records.size()+"数据耗时:"+(endTime-startTime)/1000+"秒");
				
				//在SQL中不适用distinct对取最小值没有影响
				if (records.size() > 0) {
					// 取得最小的Id值
					recordId = (Long)records.get(records.size() - 1);
				} else {
					break;	// 如果资源加载完，则跳出for循环
				}

				Long startTime1=System.currentTimeMillis();
				//将资源存入Excel中
				for(Long tempRecordId:records){
					taskNum++;
					generateSheetOneRow(tempRecordId,sheet,++processCount);
					float percentage = taskNum/totalNum;  //显示比例
					pi.setValue(percentage);
				}
				Long endTime1=System.currentTimeMillis();
				logger.info("写入Excel中"+records.size()+"耗时:"+(endTime1-startTime1)/1000+"秒");
				
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
		Resource resource = new FileResource(file, resourceImport.getApplication());
		downloader.setSource(resource);
		resourceImport.addComponent(downloader);
		
	}

	/**
	 * 生成sheet页面的表格头部
	 * @param sheet
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 */
	private void generateHeader(WritableSheet sheet) throws Exception {
		//用户选择导出表格的所有字段
		allKeywords = new ArrayList<String>();
		
		// 添加我们内部设置的字段
		defaultKeywords = new ArrayList<String>();
		ArrayList<String> phoneCells = new ArrayList<String>();
		for(CheckBox tempCheckBox:defaultCheckBox){
			if((Boolean)tempCheckBox.getValue()==true){
				String headerStr=(String)tempCheckBox.getData();
				if(headerStr.equals(TableKeywordDefault.PHONE.getName())){
					defaultKeywords.add(headerStr+"1");
					phoneCells.add(headerStr+"1");
					defaultKeywords.add(headerStr+"2");
					phoneCells.add(headerStr+"2");
					defaultKeywords.add(headerStr+"3");
					phoneCells.add(headerStr+"3");
					defaultKeywords.add(headerStr+"4");
					phoneCells.add(headerStr+"4");
				}else{
					defaultKeywords.add(headerStr);
				}
			}
		}
		allKeywords.addAll(defaultKeywords);
		WritableFont defaultKeywordFont = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.GRAY_50);
		WritableCellFormat defaultKeywordFormat = new WritableCellFormat(defaultKeywordFont);
		for(int i=0;i<defaultKeywords.size();i++){
			String keyword = defaultKeywords.get(i);
			if(phoneCells.contains(keyword)) {
				keyword = TableKeywordDefault.PHONE.getName();
			}
			jxl.write.Label label=new jxl.write.Label(i, 0, keyword);
			label.setCellFormat(defaultKeywordFormat);
			sheet.addCell(label);
		}
		
		// 设置管理员的excel列名
		int defaultKeywordSize = defaultKeywords.size();
		mgrKeywords = new ArrayList<String>();
		for(CheckBox tempCheckBox:mgrCheckBox){
			if((Boolean)tempCheckBox.getValue()==true){
				mgrKeywords.add((String)tempCheckBox.getData());
			}
		}
		allKeywords.addAll(mgrKeywords);
		for (int i =0 ; i < mgrKeywords.size(); i++) {
			sheet.addCell(new jxl.write.Label(defaultKeywordSize+i, 0, mgrKeywords.get(i)));
		}
	}

	/**
	 * 生成一个表单页中的一行
	 * @param tempRecordId // 资源的Id
	 * @param sheet //表格的Sheet
	 * @param sheetRowNum //记录写在Sheet的哪一行
	 * @throws Exception 
	 */
	private void generateSheetOneRow(Long tempRecordId, WritableSheet sheet,int sheetRowNum) throws Exception {
		//========================================================//
		//==========================导出基本信息======================//
		//========================================================//
		CustomerResource resource=(CustomerResource)commonService.get(CustomerResource.class, tempRecordId);
		if(resource==null) return;
		
		Set<Telephone> phones = resource.getTelephones();
		ArrayList<Telephone> phonesList=new ArrayList<Telephone>(phones); 
		
		for(String keyword:defaultKeywords){
			int index=defaultKeywords.indexOf(keyword);
			String content="";

			if(keyword.equals(TableKeywordDefault.ADDRESS.getName())){
				if(resource.getDefaultAddress()!=null&&!StringUtils.isEmpty(resource.getDefaultAddress().getStreet())){
					content=resource.getDefaultAddress().getStreet();//现在程序中只是用到了街道地址
				}
			}else if(keyword.equals(TableKeywordDefault.BIRTHDAY.getName())){
				if(!StringUtils.isEmpty(resource.getBirthdayStr())){
					content=resource.getBirthdayStr();
				}
			}else if(keyword.equals(TableKeywordDefault.COMPANY.getName())){
				Company company = resource.getCompany();
				if(company==null) continue;
					
				if(!StringUtils.isEmpty(company.getName())){
					content+="公司名："+company.getName();
				}
				
				if(!StringUtils.isEmpty(company.getAddress())){
					content+=" 公司地址："+company.getAddress();
				}

				if(!StringUtils.isEmpty(company.getTelephone())){
					content+=" 公司电话："+company.getTelephone();
				}
			}else if(keyword.equals(TableKeywordDefault.NAME.getName())){
				if(!StringUtils.isEmpty(resource.getName())){
					content=resource.getName();
				}
			}else if(keyword.equals(TableKeywordDefault.PHONE.getName()+1)){
				if(phonesList.size()>0){
					content=phonesList.get(0).getNumber();
				}
			}else if(keyword.equals(TableKeywordDefault.PHONE.getName()+2)){
				if(phonesList.size()>1){
					content=phonesList.get(1).getNumber();
				}
			}else if(keyword.equals(TableKeywordDefault.PHONE.getName()+3)){
				if(phonesList.size()>2){
					content=phonesList.get(2).getNumber();
				}
			}else if(keyword.equals(TableKeywordDefault.PHONE.getName()+4)){
				if(phonesList.size()>3){
					content=phonesList.get(3).getNumber();
				}
			}else if(keyword.equals(TableKeywordDefault.SEX.getName())){
				if(!StringUtils.isEmpty(resource.getSex())){
					content=resource.getSex();
				}
			}
			
			sheet.addCell(new jxl.write.Label(index, sheetRowNum, content));
		}
		
		
		//========================================================//
		//==========================导出扩展信息======================//
		//========================================================//
		String sql="select c from CustomerResourceDescription c where c.customerResource.id="+resource.getId();
		@SuppressWarnings("unchecked")
		List<CustomerResourceDescription> descriptions=(List<CustomerResourceDescription>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
		for(CustomerResourceDescription desc:descriptions){
			String key=desc.getKey();
			int index=allKeywords.indexOf(key);
			if(index!=-1){ // 说明想导出此字段
				String value=desc.getValue();
				String oldValue= sheet.getCell(index, sheetRowNum).getContents();
				if(!StringUtils.isEmpty(oldValue)){//如果旧值不为空，叠加，用于一个属性多列情况
					value+=";"+oldValue;
				}
				sheet.addCell(new jxl.write.Label(index, sheetRowNum, value));
			}
		}
	}
	

	
}
