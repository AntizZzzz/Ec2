package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.TableKeywordDefault;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.resourceimport.AdvanceSearchBatch;
import com.jiangyifen.ec2.ui.mgr.resourceimport.AppendResource;
import com.jiangyifen.ec2.ui.mgr.resourceimport.DetailInfo;
import com.jiangyifen.ec2.ui.mgr.resourceimport.EditBatch;
import com.jiangyifen.ec2.ui.mgr.resourceimport.NewBatch;
import com.jiangyifen.ec2.ui.mgr.resourceimport.SelectHeaderWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 数据导入
 * <p>
 * 此页面应该只包含搜索组件、表格组件、表格下的按钮组件
 * </p>
 * 
 * @author chb
 */
@SuppressWarnings({ "serial", "deprecation" })
public class ResourceImport extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	private Random rand = new java.util.Random();
	
	/**
	 * 主要组件输出区，搜索组件、表格组件、表格的按钮组件
	 */
	private static String[] timeRange = { "全部批次", "最近一周", "最近一个月", "最近三个月","最近一年" };
	// 搜索组件、状态信息
	private TextField keyWord;
	private ComboBox nearDate;
	private Button search;
	private Button advanceSearch;
	private Label resourceNum;
	private Button help;
	private Button exportExample;

	// 批次表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<CustomerResourceBatch> flip;

	// 批次表格按钮组件
	private Button newBatch;
	private Button addResource;
	private Button detailInfo;
	private Button editBatch;
	private Button deleteBatch;
	private Button noShareBatch;
	private Button exportBatch;
	
	//外围组件
	private VerticalLayout progressOuterLayout;

	/**
	 * 右键组件
	 */
	private Action NEWBATCH = new Action("新建批次");
	private Action ADDBATCH = new Action("追加资源");
	private Action DETAILINFO = new Action("详细信息");
	private Action EDITBATCH = new Action("编辑批次");
	private Action DELETEBATCH = new Action("删除批次");
	private Action[] ACTIONS = new Action[] { NEWBATCH, ADDBATCH, DETAILINFO,
			EDITBATCH, DELETEBATCH };

	/**
	 * 模块组件输出区
	 */
	// 弹出窗口
	private NewBatch newBatchWindow;
	private AppendResource appendResourceWindow;
	private AdvanceSearchBatch advanceSearchWindow;
	private SelectHeaderWindow selectHeaderWindow;
	private DetailInfo detailInfoWindow;
	private EditBatch editBatchWindow;
/**
 * Service
 */
	private User loginUser;
	private DepartmentService departmentService;
	private CustomerResourceBatchService customerResourceBatchService;
	private TableKeywordService tableKeywordService;
	private CommonService commonService;
	/**
	 * 构造器
	 */
	public ResourceImport() {
		this.initService();
		this.setSizeFull();
		this.setMargin(true);
		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());
		// Sql语句初始化
		search.click();
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());
		// 设置Button样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	
	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		departmentService=SpringContextHolder.getBean("departmentService");
		customerResourceBatchService = SpringContextHolder.getBean("customerResourceBatchService");
		tableKeywordService = SpringContextHolder.getBean("tableKeywordService");
		commonService = SpringContextHolder.getBean("commonService");
	}

	/**
	 * 创建搜索输出（Search）
	 */
	private HorizontalLayout buildSearchLayout() {
		// 最外围的组件，宽度100%
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setWidth("100%");
		
		//搜索组件左侧组件
		HorizontalLayout searchLayoutLeft = new HorizontalLayout();
		searchLayoutLeft.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setSpacing(true);
		Label lblKeyword = new Label("关键字：");
		lblKeyword.setWidth("-1px");
		constrantLayout.addComponent(lblKeyword); // 添加关键字Label
		keyWord = new TextField();
		keyWord.setStyleName("search");
		keyWord.setWidth("6em");
		keyWord.setInputPrompt("批次名");
		constrantLayout.addComponent(keyWord); // 添加关键字Field
		searchLayoutLeft.addComponent(constrantLayout);

		// comboBox 最近时间
		nearDate = new ComboBox();
		nearDate.addItem(timeRange[0]);
		nearDate.addItem(timeRange[1]);
		nearDate.addItem(timeRange[2]);
		nearDate.addItem(timeRange[3]);
		nearDate.addItem(timeRange[4]);
		nearDate.select(timeRange[0]); // 初始化选中all
		nearDate.setWidth("8em");
		nearDate.setNullSelectionAllowed(false); // 不允许有null选项
		searchLayoutLeft.addComponent(nearDate);

		// 搜索按钮
		search = new Button("搜索");
		search.setClickShortcut(KeyCode.ENTER);
		search.addListener((Button.ClickListener) this);
		searchLayoutLeft.addComponent(search);

		// 高级搜索按钮
		advanceSearch = new Button("高级搜索");
		advanceSearch.addListener((Button.ClickListener) this);
		searchLayoutLeft.addComponent(advanceSearch);

		// 记录条数信息
		resourceNum = new Label("资源条数--请选择批次");
		searchLayoutLeft.addComponent(resourceNum);
		searchLayout.addComponent(searchLayoutLeft);

		//右侧的Excel组件
		HorizontalLayout rightLayout=new HorizontalLayout();
		rightLayout.setSpacing(true);
		exportExample = new Button("下载Excel样例");
		exportExample.addListener((Button.ClickListener) this);
		rightLayout.addComponent(exportExample);
		
		help=new Button("导数据帮助");
		help.addListener((Button.ClickListener)this);
		rightLayout.addComponent(help);
		
		searchLayout.addComponent(rightLayout);
		searchLayout.setComponentAlignment(rightLayout,	Alignment.MIDDLE_RIGHT);
		return searchLayout;
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy年MM月dd日 HH时mm分ss秒");// 节省每次创建的资源
		// 表格组件
		table = new Table() { // 创建指定日期格式的表格组件
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					return sdf.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);

		// 表格下面的按钮组件，包括Flip
		tabelAndButtonsLayout.addComponent(buildButtonsAndFlipLayout());
		return tabelAndButtonsLayout;
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建Table下的按钮输出，包括Flip
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private HorizontalLayout buildButtonsAndFlipLayout() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setSpacing(true);
		tableButtons.setWidth("100%");

		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);
		
		newBatch = new Button("新建批次");
		newBatch.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(newBatch);

		addResource = new Button("追加资源");
		addResource.setEnabled(false); // 创建时为不可用
		addResource.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(addResource);

		detailInfo = new Button("详细信息");
		detailInfo.setEnabled(false); // 创建时为不可用
		detailInfo.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(detailInfo);

		editBatch = new Button("编辑批次");
		editBatch.setEnabled(false); // 创建时为不可用
		editBatch.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(editBatch);

		deleteBatch = new Button("删除批次");
		deleteBatch.setEnabled(false); // 创建时为不可用
		deleteBatch.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(deleteBatch);

		noShareBatch = new Button("资源独享");
		noShareBatch.setDescription("使该批次中的资源不出现在其它批次中");
		noShareBatch.setEnabled(false); // 创建时为不可用
		noShareBatch.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(noShareBatch);

		exportBatch = new Button("导出批次");
		exportBatch.setEnabled(false); // 创建时为不可用
		exportBatch.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(exportBatch);
		
		progressOuterLayout=new VerticalLayout();
		tableButtonsLeft.addComponent(progressOuterLayout);
		tableButtons.addComponent(tableButtonsLeft);

		// 右侧按钮（翻页组件）
		// table 已经创建，不为null，sql已经有search按钮的click事件初始化
		flip = new FlipOverTableComponent<CustomerResourceBatch>(
				CustomerResourceBatch.class, customerResourceBatchService,
				table, sqlSelect, sqlCount, null);
		table.setPageLength(10); // 为了方便修改，将设置Table的操作放在这里
		flip.setPageLength(10, false);
		flip.getEntityContainer().addNestedContainerProperty("user.username");

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "batchName", "createDate", "batchStatus","user.username" };
		String[] columnHeaders = new String[] { "批次编号", "批次","创建时间","批次状态", "创建者" };
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 设置生成的备注信息
		this.addColumn(table);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由 buildButtonsAndFlipLayout调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "note");
				String longNote = "";
				if (note != null && note.toString() != null) {
					longNote = note.toString();
				}
				String shortNote = longNote;
				if (shortNote.length() < 5) {
					shortNote += "...";
				} else {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				return label;
			}

		});
	}

	/**
	 * 由buildButtonsLayout 调用，设置表格行显示格式
	 * 
	 * @param table2
	 */
	private void setStyleGeneratorForTable(final Table table2) {
		// style generator
		table2.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				return null;
			}
		});
	}

	/**
	 * 由构造器调用， 设置界面按钮显示
	 */
	private void setButtonsStyle(String style) {
		search.setStyleName(style);
		advanceSearch.setStyleName(style);
		newBatch.setStyleName(style);
		detailInfo.setStyleName(style);
		addResource.setStyleName(style);
		editBatch.setStyleName(style);
		deleteBatch.setStyleName(style);
		noShareBatch.setStyleName(style);
		exportBatch.setStyleName(style);
		// 后改
		search.setStyleName("small");
		advanceSearch.setStyleName("small");
	}

	/**
	 * 由executeSearch调用， 将用户选择的组合框中的值解析为日期字符串
	 * 
	 * @return
	 */
	private String parseNearDate() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		String nearDateStr = "";
		if (nearDate.getValue() != null) {
			nearDateStr = nearDate.getValue().toString();
		}
		// 取得当前时间
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		if (nearDateStr.equals(timeRange[0])) {
			return "";
		} else if (nearDateStr.equals(timeRange[1])) {
			int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (week == 0) {
				cal.add(Calendar.WEEK_OF_YEAR, -1);
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			return df.format(cal.getTime());
		} else if (nearDateStr.equals(timeRange[2])) {
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			return df.format(cal.getTime());
		} else if (nearDateStr.equals(timeRange[3])) {
			cal.add(Calendar.MONTH, -2);
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			return df.format(cal.getTime());
		} else if (nearDateStr.equals(timeRange[4])) {
			cal.set(Calendar.DAY_OF_YEAR,
					cal.getActualMinimum(Calendar.DAY_OF_YEAR));
			return df.format(cal.getTime());
		}
		return null;
	}

	/**
	 * 由buttonClick调用显示高级搜索窗口，由于用户点击的高级搜索，此时翻页组件一定不为null
	 */
	private void showAdvanceSearchWindow() {
		// 清空搜索组件中的旧状态
		keyWord.setValue("");
		nearDate.setValue(timeRange[0]);
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (advanceSearchWindow == null) {
			advanceSearchWindow =new AdvanceSearchBatch(this);
		} 
		this.getWindow().addWindow(advanceSearchWindow);
	}

	/**
	 * 由buttonClick调用显示高级搜索窗口，由于用户点击的高级搜索，此时翻页组件一定不为null
	 */
	private void showSelectHeaderWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (selectHeaderWindow == null) {
			selectHeaderWindow =new SelectHeaderWindow(this);
		} 
		this.getWindow().addWindow(selectHeaderWindow);
	}

	/**
	 * 由buttonClick调用显示导入批次窗口
	 */
	private void showNewBatchWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (newBatchWindow == null) {
			//为了资源导入进度条正确显示，每次创建组件
			newBatchWindow = new NewBatch(this);
		}
		this.getWindow().addWindow(newBatchWindow);
	}

	/**
	 * 由buttonClick调用显示追加批次窗口
	 */
	private void showAppendResourceWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (appendResourceWindow == null) {
			appendResourceWindow = new AppendResource(this);
		}
		this.getWindow().addWindow(appendResourceWindow);
	}

	/**
	 * 由buttonClick调用显示批次资源的详细信息
	 */
	private void showDetailInfoWindow() {
		if (detailInfoWindow == null) {
			// 有可能在详细信息中删除项目,使纪录条数改变
			detailInfoWindow = new DetailInfo(this);
		}
		this.getWindow().addWindow(detailInfoWindow);
	}

	/**
	 * 由buttonClick调用，显示编辑批次的信息
	 */
	private void showEditBatchWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (editBatchWindow == null) {
			editBatchWindow = new EditBatch(this);
		}
		this.getWindow().addWindow(editBatchWindow);
	}
	/**
	 * 显示帮助窗口
	 */
	private void showHelpWindow() {
		//可以写入到配置文件
		String str="1、在左侧导航栏中选则“Excel配置”，对表格头部进行自定义设置。</br>" +
				"2、点击“新建批次”或“追加资源”，在弹出的窗口中点击导入按钮。</br>" +
				"3、在弹出的窗口中双击要导入的Excel文件,开始执行导入，如果文件较大可能要等待一段时间。</br>" +
				"4、文件导入成功，并显示导入的详情（成功导入、客户资源、更新数据、无效数据）！</br>" +
				"5、导入文件的规则：所选文件必须为Excel文件，格式必须符合管理员配置的格式，具体格式请点击" +
				"右上角的下载Excel样例，然后替换样例中的非表头数据，再执行导入，同一个条资源可以同时导入多个电话号码，" +
				"如果电话号码已经导入过，并且不是客户资源则更新客户资源的姓名、生日、公司等信息，如果是客户资源则跳过此条记录，" +
				"对于已经存在的非客户资源再次导入，则会使资源的描述信息增多，资源的地址增多，默认地址为最后一次导入的地址。";
		Window helpWindow=new Window("帮助信息");
		helpWindow.setModal(true);
		helpWindow.setResizable(false);
		HorizontalLayout infoLayout=new HorizontalLayout();
		infoLayout.setMargin(true);
		Label helpInfo=new Label(str,Label.CONTENT_XHTML);
		infoLayout.addComponent(helpInfo);
		helpInfo.setWidth("50em");
		helpWindow.setContent(infoLayout);
		this.getWindow().addWindow(helpWindow);
	}
	
	/**
	 * 进度组件
	 * @return
	 */
	public VerticalLayout getProgressLayout(){
		return progressOuterLayout;
	}
	
	/**
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("CustomerResourceBatch");
		// 关键字过滤
		String keyWordStr = "";
		if (keyWord.getValue() != null) {
			keyWordStr = keyWord.getValue().toString();
		}
		SqlGenerator.Like batchName = new SqlGenerator.Like("batchName",
				keyWordStr);
		sqlGenerator.addAndCondition(batchName);
		// 日期过滤
		String nearDateStr = parseNearDate();
		SqlGenerator.GreaterOrEqual greaterOrEqualDate = new SqlGenerator.GreaterOrEqual("createDate", nearDateStr, true);
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门创建的批次
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		for(Long deptId : allGovernedDeptIds) {
			SqlGenerator.Equal orEqual = new SqlGenerator.Equal("user.department.id", deptId.toString(), false);
			sqlGenerator.addOrCondition(orEqual);
		}
		
		sqlGenerator.addAndCondition(greaterOrEqualDate).setOrderBy("id",
				SqlGenerator.DESC);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();
		//更新表格到首页
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 根据管理员指定的字段去动态生成样例Excel表格
	 * 
	 * @return
	 */
	private void executeDownload() {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			file = new File(new String(("样例文件.xls").getBytes("GBK"),
					"ISO-8859-1"));
			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = writableWorkbook.createSheet("Sheet0", 0);
			
			//所有的表格头字段
			List<String> allKeywords = new ArrayList<String>();
			
			
			// 添加我们内部设置的字段的
			List<String> defaultKeywords = new ArrayList<String>();
			for(TableKeywordDefault keyword:TableKeywordDefault.values()){
				defaultKeywords.add(keyword.getName());
			}
			WritableFont defaultKeywordFont = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.GRAY_50);
			WritableCellFormat defaultKeywordFormat = new WritableCellFormat(defaultKeywordFont);
			for(int i=0;i<defaultKeywords.size();i++){
				jxl.write.Label label=new jxl.write.Label(i, 0, defaultKeywords.get(i));
				label.setCellFormat(defaultKeywordFormat);
				sheet.addCell(label);
			}
			allKeywords.addAll(defaultKeywords);//添加到所有字段
			
			// 添加数据库表管理员设置的字段
			List<String> tableKeywords = tableKeywordService
					.getAllStrByDomain(SpringContextHolder.getDomain());

			// 设置excel列名
			int defaultKeywordSize = defaultKeywords.size();
			for (int i =0 ; i < tableKeywords.size(); i++) {
				sheet.addCell(new jxl.write.Label(defaultKeywordSize+i, 0, tableKeywords.get(i)));
			}
			allKeywords.addAll(tableKeywords);//添加到所有字段
			
			//为生成的表格添加数据
			for(int i=1;i<11;i++){
				for(int j=0;j<allKeywords.size();j++){
					String title=allKeywords.get(j);
					String value="";
					if(title.equals(TableKeywordDefault.NAME.getName())){
						value=generateName();
					}else if(title.equals(TableKeywordDefault.SEX.getName())){
						value=generateSexValue();
					}else if(title.equals(TableKeywordDefault.BIRTHDAY.getName())){
						value=generateBirthday();
					}else if(title.equals(TableKeywordDefault.PHONE.getName())){
						value=generateMobilNumber();
					}else if(title.equals(TableKeywordDefault.COMPANY.getName())){
						value=generateCompany();
					}else if(title.equals(TableKeywordDefault.ADDRESS.getName())){
						value=generateAddress();
					}else{
						value=allKeywords.get(j)+i;
					}
					sheet.addCell(new jxl.write.Label(j, i, value));
				}
			}
			
			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建样例Excel文件出现异常！");
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
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		addComponent(downloader);
		
		downloader.addListener(new RepaintRequestListener() {
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});
	}
	
	/**
	 * 根据管理员指定的字段去动态生成样例Excel表格
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private void executeExport() {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			file = new File(new String(("批次文件.xls").getBytes("GBK"),
					"ISO-8859-1"));
			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = writableWorkbook.createSheet("Sheet0", 0);
			
			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建样例Excel文件出现异常！");
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
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		addComponent(downloader);
	}
	
	private String generateName() {
		String names[]={ "John", "Mary", "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc",
				"Robert", "Paula", "Lenny", "Kenny", "Nathan", "Nicole",
				"Laura", "Jos", "Josie", "Linus", "Torvalds", "Smith",
				"Adams", "Black", "Wilson", "Richards", "Thompson",
				"McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard",
				"Hill", "Fielding", "Einstein" };
		return names[rand.nextInt(names.length)];
	}
	
	private String generateSexValue(){
		String str[]={"男","女"};
		return str[Math.abs(rand.nextInt()%2)];
				
	}
	

	private String generateBirthday(){
		GregorianCalendar calendar=new GregorianCalendar();
		calendar.add(GregorianCalendar.DAY_OF_YEAR,-(rand.nextInt(40*365)+20*365));
		return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
	}
	
	static String mobilNo = "";
	private String generateMobilNumber() {
			String number = "1" + (rand.nextInt(7) + 3);
			for (int j = 0; j < 9; j++) {
				number += rand.nextInt(10);
			}
			mobilNo = number;
			return number;
	}
	
	private String generateCompany() {
		String companyA=getCharAndNumr(2);
		String companyB=getCharAndNumr(3);
		
		return "company_"+companyA+"_"+companyB;
	}
	
	private String generateAddress() {
		String addressA=getCharAndNumr(7);
		String addressB=getCharAndNumr(9);
		
		return "address_"+addressA+"_"+addressB;
	}
	
	private String getCharAndNumr(int length)     
	{     
	    String val = "";     
	    Random random = new Random();     
	    for(int i = 0; i < length; i++)     
	    {     
	        String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字     
	        if("char".equalsIgnoreCase(charOrNum)) // 字符串     
	        {     
	            int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母     
	            val += (char) (choice + random.nextInt(26));     
	        }     
	        else if("num".equalsIgnoreCase(charOrNum)) // 数字     
	        {     
	            val += String.valueOf(random.nextInt(10));     
	        }     
	    }     
	    return val;     
	}   

	/**
	 * 执行删除操作
	 */
	@SuppressWarnings("unchecked")
	private void executeDelete() {
		// 批次删除
		CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
		String nativeSql = "select distinct marketingproject_id from ec2_markering_project_ec2_customer_resource_batch where batches_id="+ batch.getId();
		List<Long> projectIdList= (List<Long>) commonService.excuteNativeSql(nativeSql,ExecuteType.RESULT_LIST);
		// 如果批次没有被项目引用，允许删除，否则弹出提示窗口
		if (projectIdList.size()==0) {
			Label label = new Label("删除批次将删除项目中与此批次相关的所有任务（已分配、未分配），您确定要删除批次<b>" + batch.getBatchName()
					+ "</b>?", Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
					"confirmDelete");
			this.getWindow().addWindow(confirmWindow);
		}else if (projectIdList.size()>0) {
			String note="批次与项目";
			List<String> projectNameList=new ArrayList<String>();
			for(Long projectId:projectIdList){
				MarketingProject marketingProject=commonService.get(MarketingProject.class, projectId);
				String projectName=marketingProject.getProjectName();
				projectNameList.add(projectName);
			}
			note+=StringUtils.join(projectNameList, "、");
			note+="关联,";
					
			Label label = new Label(note+"您确定要删除批次<b>" + batch.getBatchName()
					+ "</b>?", Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
					"confirmDelete");
			this.getWindow().addWindow(confirmWindow);
		}
		
	}
	
	/**
	 * 执行独享操作
	 */
	private void executeNoShare() {
		CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
		Label label = new Label("您确定要使批次资源独享<b>" + batch.getBatchName()
				+ "</b>?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmNoShare");
		this.getWindow().addWindow(confirmWindow);
	}
	
	/**
	 * 确认资源独享操作
	 */
	public void confirmNoShare(Boolean isConfirmed) {
		if(isConfirmed){
			CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
			OperationLogUtil.simpleLog(loginUser, "资源独享"+batch.getBatchName()+" Id:"+batch.getId());
			//隐含了域的概念
			String nativeSql="delete from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") and customerresourcebatches_id!="+batch.getId();
	logger.info("资源独享："+nativeSql);
			commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
			NotificationUtil.showWarningNotification(ResourceImport.this, "独享数据成功！");
		}
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlSelect
	 */
	public void setSqlSelect(String sqlSelect) {
		this.sqlSelect = sqlSelect;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlCount
	 */
	public void setSqlCount(String sqlCount) {
		this.sqlCount = sqlCount;
	}

	/**
	 * 由弹出窗口使用，获取table选中的值
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 */
	public void updateTable(Boolean isToFirst) {
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

	/**
	 * 由弹出窗口回调确认删除项目,此时marketingProjectService不应该为null
	 */
	public void confirmDelete(Boolean isConfirmed) {
		// 运行到此说明批次项目没有关联关系
		if (isConfirmed == true) {
			// 批次删除
			CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
//			CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
//			String nativeSql = "select distinct marketingproject_id from ec2_markering_project_ec2_customer_resource_batch where batches_id="+ batch.getId();
//			List<Long> projectIdList= (List<Long>) commonService.excuteNativeSql(nativeSql,ExecuteType.RESULT_LIST);
			// 删除批次项目关联
			OperationLogUtil.simpleLog(loginUser, "删除资源"+batch.getBatchName()+" Id:"+batch.getId());
			
			
			// 删除批次资源关联
			String nativeSql = "delete from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="
					+ batch.getId();
			// 删除此批次Id的Task
			String nativeSql3="delete from ec2_marketing_project_task where batchid="
					+ batch.getId();
			
			//删除批次与项目关联
			String nativeSql1="delete from ec2_markering_project_ec2_customer_resource_batch where batches_id="
					+ batch.getId();
			//删除批次
			String nativeSql2 = "delete from ec2_customer_resource_batch where id="
					+ batch.getId();
			commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
			commonService.excuteNativeSql(nativeSql3, ExecuteType.UPDATE);
			commonService.excuteNativeSql(nativeSql1, ExecuteType.UPDATE);
			commonService.excuteNativeSql(nativeSql2, ExecuteType.UPDATE);
			// 更新Table数据，并使Table处于未被选中状态
			this.updateTable(false);
			table.setValue(null);
		}
	}

	/**
	 * 由valueChange调用,更新资源条数信息,同时更新“详细信息”和“编辑批次” 按钮的状态
	 * <p>
	 * 查看详细信息也会调用，所有public
	 * </p>
	 */
	public void updateInfoAndStatus() {
		// 取得Table的选中状态信息,
		CustomerResourceBatch batch = (CustomerResourceBatch) table.getValue();
		// 更新消息
		if (batch == null) {
			resourceNum.setValue("资源条数--请选择批次");
		} else { // 此时至少含有一个批次
			// 计算选中批次的所有记录数量，并显示
			// 构造出查询使用的原生Sql语句
			String nativeSql = "select count(*) from ec2_customer_resource_ec2_customer_resource_batch e where e.customerresourcebatches_id="
					+ batch.getId();
			Long num = 0L;
			try {
				num = (Long) commonService.excuteNativeSql(nativeSql,
						ExecuteType.SINGLE_RESULT);
			} catch (Exception e) {
				e.printStackTrace();
			}
			resourceNum.setValue("资源条数--" + num + "条");
		}

		// 更新状态
		detailInfo.setEnabled(false);
		editBatch.setEnabled(false);
		deleteBatch.setEnabled(false);
		noShareBatch.setEnabled(false);
		exportBatch.setEnabled(false);
		addResource.setEnabled(false);
		if (batch != null) {
			deleteBatch.setEnabled(true);
			noShareBatch.setEnabled(true);
			exportBatch.setEnabled(true);
			editBatch.setEnabled(true);
			detailInfo.setEnabled(true);
			addResource.setEnabled(true);
		}
	}

	/**
	 * 表格的值改变时触发事件
	 * 
	 * @param event
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		updateInfoAndStatus();
	}

	/**
	 * 监听搜索，高级搜索，详细信息，编辑批次，批次整理的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == advanceSearch) {
			showAdvanceSearchWindow();
		} else if (event.getButton() == newBatch) {
			showNewBatchWindow();
		} else if (event.getButton() == addResource) {
			showAppendResourceWindow();
		} else if (event.getButton() == detailInfo) {
			showDetailInfoWindow();
		} else if (event.getButton() == editBatch) {
			showEditBatchWindow();
		} else if (event.getButton() == help) {
			showHelpWindow();
		} else if (event.getButton() == deleteBatch) {
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"删除出错！");
			}
		} else if (event.getButton() == noShareBatch) {
			try {
				executeNoShare();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"资源独享成功！");
			}
		}
		else if (event.getButton() == exportBatch) {
			showSelectHeaderWindow();
		}
		else if (event.getButton() == search) {
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"搜索出错！");
			}
		} else if (event.getButton() == exportExample) {
			try {
				executeDownload();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"下载样例出错！");
			}
		} else if (event.getButton() == help) {
			this.getWindow().addWindow(newBatchWindow);
		}
	}

	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {NEWBATCH};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (NEWBATCH == action) {
			newBatch.click();
		} else if (ADDBATCH == action) {
			addResource.click();
		} else if (DETAILINFO == action) {
			detailInfo.click();
		} else if (EDITBATCH == action) {
			editBatch.click();
		} else if (DELETEBATCH == action) {
			deleteBatch.click();
		}
	}
}
