package com.jiangyifen.ec2.ui.admin.tab;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.service.eaoservice.TableInfoVoService;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.TableInfoVo;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * 数据库表信息查看
 * 
 * @author JHT
 * 
 */
public class TableInfoLookManagement extends VerticalLayout implements Button.ClickListener, ValueChangeListener {
	private static final long serialVersionUID = 4123507380758841592L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());			// 日志工具
	private final Object[] VISIBLE_COLUMNS = new Object[]{"relName", "dataNum", "seqMax", "tableCount"};
	private final String[] COLUMN_HEADERS_KB = new String[]{"表名称", "表大小(k)", "序列当前值", "表的总数据"};
	private final String[] COLUMN_HEADERS_MB = new String[]{"表名称", "表大小(M)", "序列当前值", "表的总数据"};
	
	// 搜索组件
	private TextField keyWord;
	private Button btnSearch;
	private Button btnShow;															// 按M显示和按K显示表的数据大小
	private Button btnTableCount;													// 显示当前选中表的count大小
	
	// 表格组件
	private Table table;
	
	// 显示总信息的label组件
	private Label labelSum;	
	private Label labelResult;
	
	// 其他组件
	private  DecimalFormat df = new DecimalFormat("0.00");							// 只显示小数点后2位
	private TableInfoVoService tableInfoVoService;
	private List<TableInfoVo> atsList = new ArrayList<TableInfoVo>();
	private BeanItemContainer<TableInfoVo> asterContainer = new BeanItemContainer<TableInfoVo>(TableInfoVo.class);
	
	// 构造方法
	public TableInfoLookManagement(){
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		tableInfoVoService = SpringContextHolder.getBean("tableInfoVoService");		// 初始化业务类	
		
		VerticalLayout constrantLayout = new VerticalLayout();						// 约束组件, 使组件紧密排列
		this.addComponent(constrantLayout);
		constrantLayout.addComponent(buildSearchLayout());							// 搜索
		constrantLayout.addComponent(buildTableAndButtonsLayout());					// 表格和按钮
	}
	
	// 创建搜索输出(Search)
	private HorizontalLayout buildSearchLayout(){
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		
		// 使keyWord和keyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字："));
		keyWord = new TextField();
		keyWord.setStyleName("search");
		keyWord.setImmediate(true);
		keyWord.setInputPrompt("表名称关键字");
		keyWord.setDescription("可按'表名称'进行搜索");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);
		
		// 搜索按钮
		btnSearch = new Button("搜索", this);
		btnSearch.setImmediate(true);
		searchLayout.addComponent(btnSearch);
		btnShow = new Button("按M显示数据", this);
		searchLayout.addComponent(btnShow);
		
		// 显示表的Count大小按钮
		btnTableCount = new Button("显示表的Count值", this);
		btnTableCount.setImmediate(true);
		searchLayout.addComponent(btnTableCount);
		btnTableCount.setEnabled(false);
		
		return searchLayout;
	}
	
	// 创建表格和按钮输出
	private VerticalLayout buildTableAndButtonsLayout(){
		VerticalLayout tableAndButtonsLayout = new VerticalLayout();
		tableAndButtonsLayout.setSpacing(true);
		table = new Table(){
			private static final long serialVersionUID = -1171980769626617047L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object v = property.getValue();
				if(colId == "dataNum"){
					Double dataNum = Double.parseDouble(v.toString());
					if(btnShow.getCaption() == "按M显示数据"){
						return df.format(dataNum/1024);
					} else{
						return df.format(dataNum/1024/1024);
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		labelResult = new Label("", Label.CONTENT_XHTML);
		executeSearch();
		table.setStyleName("striped");
		table.setWidth("100%");
		table.addListener(this);
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setPageLength(27);		// 显示的条数
		
		tableAndButtonsLayout.addComponent(table);
		
		tableAndButtonsLayout.addComponent(buildTableAndLabelsLayout());
		return tableAndButtonsLayout;
	}
	
	// 添加Table的Column -- 表的总数据列
	private class TableCountColumnGenerator implements Table.ColumnGenerator{
		private static final long serialVersionUID = -128148646340197949L;
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			 if(columnId == "tableCount"){
				 TableInfoVo tVo = (TableInfoVo) itemId;
				if(tVo==null||-1 == tVo.getTableCount()){
					return "暂无显示";
				} else{
					Label lblTableCount = new Label("<font color='blue'>"+tVo.getTableCount()+"</font>", Label.CONTENT_XHTML);
					return lblTableCount;
				}
			}
			return null;
		}
	}
	
	// 由buildTableAndButtonsLayout调用, 创建table下的按钮输出
	private HorizontalLayout buildTableAndLabelsLayout() {
		HorizontalLayout tableLabels = new HorizontalLayout();						// label显示信息输出
		tableLabels.setSpacing(true);
		tableLabels.setWidth("100%");
		
		HorizontalLayout tableLabelLeft = new HorizontalLayout();					// 左侧label
		tableLabelLeft.setSpacing(true);
		tableLabelLeft.addComponent(labelResult);
		
		HorizontalLayout tableLabelRight = new HorizontalLayout();
		tableLabelRight.setSpacing(true);
		labelSum = new Label("总表数: <font style='font-weight:bold;'>"+tableInfoVoService.getTableInfoCount()+"</font>张表 &nbsp;&nbsp;总大小: <font style='font-weight:bold;'>"+tableInfoVoService.getSumTableInfoSize()+"</font> MB", Label.CONTENT_XHTML);
		tableLabelRight.addComponent(labelSum);
		
		tableLabels.addComponent(tableLabelLeft);
		tableLabels.addComponent(tableLabelRight);
		tableLabels.setComponentAlignment(tableLabelRight, Alignment.BOTTOM_RIGHT);
		
		return tableLabels;
	}
	
	// 由ButtonClick调用执行简单搜索
	private void executeSearch(){
		String keyWordStr = "";
		if(keyWord.getValue() != null){
			keyWordStr = keyWord.getValue().toString().toLowerCase();
		}
		try {
			atsList = tableInfoVoService.getTableInfoVoList(keyWordStr);
			setBeanItemContainer(atsList);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage() + " 执行查询操作出现异常!", e);
			this.getApplication().getMainWindow().showNotification("您的输入有误,查询失败,请重新输入!", Notification.TYPE_WARNING_MESSAGE);
		}
		keyWord.focus();
	}
	
	// 设置Table的数据源显示
	private void setBeanItemContainer(List<TableInfoVo> asterList){
		asterContainer.removeAllItems();
		asterContainer.addAll(asterList);
		
		labelResult.setValue("查询结果:<font style='font-weight:bold'> "+asterList.size()+"</font> 张表");
		table.setContainerDataSource(asterContainer);
		table.setVisibleColumns(VISIBLE_COLUMNS);									// 设置表格头部显示
		if(btnShow.getCaption() == "按M显示数据"){
			table.setColumnHeaders(COLUMN_HEADERS_KB);
		} else{
			table.setColumnHeaders(COLUMN_HEADERS_MB);
		}
		table.removeGeneratedColumn("tableCount");
		table.addGeneratedColumn("tableCount", new TableCountColumnGenerator());
		table.setColumnHeader("tableCount", "表的总数据");
		table.setColumnExpandRatio("relName", 0.4f);
		table.setColumnExpandRatio("dataNum", 0.3f);
		table.setColumnExpandRatio("seqMax", 0.3f);
		/*table.setSortAscending(false);	// 设置是否按升序排列// 设置默认表格的排序方式
		table.setSortContainerPropertyId("seqMax");*/
		
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnSearch == source){
			executeSearch();
		} else if(btnShow == source){
			if(btnShow.getCaption() == "按M显示数据"){
				btnShow.setCaption("按K显示数据");
			} else{
				btnShow.setCaption("按M显示数据");
			}
			executeSearch();
		} else if(btnTableCount == source){
			btnTableCount.setEnabled(false);
			if(table.getValue() != null){
				showContainer();
			} else{
				this.getApplication().getMainWindow().showNotification("您选中的数据为空，无法进行查看！", Notification.TYPE_WARNING_MESSAGE);
			}
			btnTableCount.setEnabled(true);
		}
	}
	
	// 获取选中数据在Container里拿出
	private void showContainer(){
		TableInfoVo tVo = (TableInfoVo) table.getValue();
		//table.getItemCaption(tVo);  // 获取这个对象的头部自动生成的ID(Gets the caption of an item. The caption is generated as specified by the item caption mode. See setItemCaptionMode() for more details. )
		int index = asterContainer.indexOfId(tVo);
		asterContainer.removeItem(tVo);
		long count = -1;
		try {
			count = tableInfoVoService.getTableColumnCountByTableName(tVo.getRelName());
		} catch (Exception e) {
			logger.error("JHT --->> 在执行显示选中行获取表的Count值时出现异常！",e.getMessage());
		}
		tVo.setTableCount(count);
		asterContainer.addItemAt(index,tVo);
	}

	/**
	 *  构造方法执行完之后, 将会自动执行这个方法
	 *  创建这个方法的原因:
	 *  	如果直接在构造方法里面添加这个 键盘事件, 会报空指针异常,
	 * 	  应该是在组件还没有加载完毕时就进行调用它的父窗体的问题.
	 */
	@Override
	public void attach() {
		keyWord.focus();
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(table == source){
			if(table.getValue() == null){
				btnTableCount.setEnabled(false);
			} else{
				btnTableCount.setEnabled(true);
			}
		}
	}

}
