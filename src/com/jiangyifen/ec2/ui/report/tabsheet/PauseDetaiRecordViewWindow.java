package com.jiangyifen.ec2.ui.report.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.report.pojo.PauseDetailPo;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponentUseNativeSql;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 置忙统计-详情页面
 * @author XKP
 * @version 创建时间：2014-9-1 下午2:07:30 类说明
 */
public class PauseDetaiRecordViewWindow extends Window implements Button.ClickListener {

	private static final long serialVersionUID = -3990190043212740149L;
	
	private static Object[] visibleColumns = new Object[] { "deptName", "username", "queue", "reason", "pauseDate", "unpauseDate" };
	private static String[] columnHeaders = new String[] { "部门名称", "用户名", "队列", "置忙原因", "置忙时间", "置闲时间" };
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 关键字和搜索按钮
	private Button btnSearch;
	private Table table;               	
	private ComboBox cbPauseReason;       //置忙原因ComboBox
	private PauseDetailPo pauseDetailPo;  //PauseDetailPo对象
	private FlipOverTableComponentUseNativeSql<QueuePauseRecord> flip;
	private PopupDateField pdfPause;

	private PauseDetailReport pauseDetailReport;//置忙报表详情
	private StringBuffer sqlSelectBuffer;
	private StringBuffer sqlCountBuffer;
	private List<PauseReason> reasons;    //置忙原因集合
	private String queryDate;             //查询时间
	private String lastDay;               //查询时间

	private QueuePauseRecordService queuePauseRecordService;
	
	public PauseDetaiRecordViewWindow(PauseDetailReport pauseDetailReport, String queryDate,String lastDay, List<PauseReason> reasons) {
		
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("置忙详细信息");
		
		this.pauseDetailReport = pauseDetailReport;
		this.queryDate = queryDate;
		this.reasons = reasons;
		this.lastDay = lastDay;
		
		pdfPause=pauseDetailReport.getPopupDate();
		queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
		
		VerticalLayout windowContent = new VerticalLayout();  // 创建一个windowContent的组件
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		windowContent.setSizeUndefined();
		windowContent.setSpacing(true);
		windowContent.setMargin(false, true, true, true);
		windowContent.addComponent(buildSearchLayout());        // 搜索组件
		
		executeSearch();	//默认执行查询
		windowContent.addComponent(buildTabelAndButtonsLayout());// 表格和按钮
		
		this.setContent(windowContent);
	}

	// 创建搜索输出（Search）
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSizeUndefined();
		searchLayout.setSpacing(true);

		HorizontalLayout constrantLayout = new HorizontalLayout();
		searchLayout.addComponent(constrantLayout);

		Label lb_1 = new Label("置忙原因：");              //置忙原因选择
		constrantLayout.addComponent(lb_1);

		BeanItemContainer<PauseReason> pauseReasonContainer = new BeanItemContainer<PauseReason>(PauseReason.class);
		PauseReason showAllReason = new PauseReason(); // 设置ComboBox数据源
		showAllReason.setReason("全部");
		pauseReasonContainer.removeAllItems();
		pauseReasonContainer.addBean(showAllReason);
		pauseReasonContainer.addAll(reasons);

		cbPauseReason = new ComboBox();                 // 创建ComboBox 存放置忙的全部原因
		cbPauseReason.setContainerDataSource(pauseReasonContainer);
		cbPauseReason.setValue(showAllReason);
		cbPauseReason.setWidth("100px");
		cbPauseReason.setNullSelectionAllowed(false);
		cbPauseReason.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		constrantLayout.addComponent(cbPauseReason);
		
		btnSearch = new Button(" 搜 索 ");                  // 搜索按钮
		btnSearch.setStyleName("small");
		btnSearch.addListener((Button.ClickListener) this);
		searchLayout.addComponent(btnSearch);
		
		return searchLayout;
	}
	
	//创建表格和按钮输出（Table）
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSizeUndefined();
		tabelAndButtonsLayout.setSpacing(true);
		
		table = createFormatColumnTable();                // 创建表格
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setWidth("673px");
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);  // 行号
		tabelAndButtonsLayout.addComponent(table);
		tabelAndButtonsLayout.addComponent(buildButtonsLayout());//创建分页按钮
		return tabelAndButtonsLayout;
	}

	// 创建格式化显示列的表格
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = -6483342728298303986L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (property.getValue() == null) {
					return "";
				} else if (colId.equals("pauseDate") || colId.equals("unpauseDate")) {
					Object st = property.getValue();
					if (st instanceof Date) {
						return sdf.format(st);
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	// 创建Table下的分页按钮
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout flipLayout = new HorizontalLayout();// 按钮输出
		flipLayout.setWidth("100%");
		flipLayout.setSpacing(true);
		
		flip = new FlipOverTableComponentUseNativeSql<QueuePauseRecord>(QueuePauseRecord.class, queuePauseRecordService, table, sqlSelectBuffer.toString(), sqlCountBuffer.toString(), null);
		flip.setPageLength(15);

		table.setPageLength(15);
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		table.setColumnWidth("unpauseDate", 150);
		table.setColumnWidth("pauseDate", 150);
		table.addGeneratedColumn("pauseTotalTime", new TotalTimeGenerator());
		table.setColumnHeader("pauseTotalTime", "置忙总时间");
		flipLayout.addComponent(flip);// 添加翻页组件
		flipLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return flipLayout;
	}
	
	// 总时间生成
	@SuppressWarnings("serial")
	private class TotalTimeGenerator implements Table.ColumnGenerator {

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			String totalTimeStr;
			QueuePauseRecord pauseRecord = (QueuePauseRecord) itemId;
			Date pauseTime = pauseRecord.getPauseDate();// 置忙时间
			Date unPausetime = pauseRecord.getUnpauseDate();// 置闲时间
			long totalTime = (unPausetime.getTime() - pauseTime.getTime()) / 1000;// 计算总时间
			totalTimeStr = DateUtil.getTime(totalTime); // 格式化总时间
			return totalTimeStr;
		}
	}

	// 执行查询，更新翻页组件到第一页
	private void executeSearch() {
		
		//初始化查询语句
		sqlSelectBuffer = new StringBuffer("select s.deptName,s.username,s.queue,s.reason,s.pauseDate,s.unpauseDate FROM  ec2_queue_member_pause_event_log as s where 1=1 ");
		sqlCountBuffer = new StringBuffer("select count(*) from ec2_queue_member_pause_event_log as s where 1=1 ");
		
		StringBuilder whereBuilder = new StringBuilder();//组装where条件
		
		pauseDetailPo = (PauseDetailPo) pauseDetailReport.getTable().getValue();
		whereBuilder.append(" and s.deptName='" + pauseDetailPo.getDept() + "' and s.username='" + pauseDetailPo.getUsername() + "' and s.queue='" + pauseDetailPo.getQueue() + "'");
		if(pdfPause.getResolution() == PopupDateField.RESOLUTION_DAY){//按天查询
			whereBuilder.append(" and s.pauseDate >= '" + queryDate + " 00:00:00' "+" and s.pauseDate <= '" + queryDate + " 23:59:59' ");
		}else{//月
			whereBuilder.append(" and s.pauseDate >= '" + queryDate + "-01 00:00:00' "+" and s.pauseDate <= '" + lastDay + " 23:59:59' ");
		}
		
		if (null != cbPauseReason.getValue()) {// 置忙原因
			PauseReason selectPauseReason = (PauseReason) cbPauseReason.getValue();
			if (StringUtils.isNotEmpty(selectPauseReason.getReason()) && !"全部".equals(selectPauseReason.getReason())) {
				whereBuilder.append(" and s.reason = '" + selectPauseReason.getReason() + "' ");
			}
		}
		
		if(reasons!=null){
			whereBuilder.append(" and s.reason in ('" + StringUtils.join(reasons, "','") + "') ");
		}
		sqlSelectBuffer.append(whereBuilder);//组装查询语句
		sqlSelectBuffer.append(" order by s.deptName ,s.username ,s.queue ");
		
		sqlCountBuffer.append(whereBuilder);
		
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}
 

	//更新表格数据
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelectBuffer.toString());
			flip.setCountSql(sqlCountBuffer.toString());
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}
	
	
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == btnSearch) {
			try {
				executeSearch();
			} catch (Exception e) {
				logger.error("查询置忙记录详情失败,可能输入的参数有误！-->" + e.getMessage(), e);
				System.out.println("查询置忙记录详情失败!");
			}
		}
	}
}
