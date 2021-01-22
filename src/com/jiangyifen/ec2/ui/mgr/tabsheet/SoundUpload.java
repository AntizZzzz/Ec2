package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.soundupload.AddSound;
import com.jiangyifen.ec2.ui.mgr.soundupload.ConvertedUploadSound;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * <p>
 * 此页面应该只包含搜索组件、表格组件、表格下的按钮组件
 * </p>
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class SoundUpload extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private Button search;

	// 语音上传表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<SoundFile> flip;

	// 语音上传表格按钮组件
	private Button add;// 新建语音上传
	private Button convertedUpload;// 新建语音上传
	
	private Button delete;
	
	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加");
	private Action DELETE = new Action("删除");
	
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private AddSound addWindow;
	private ConvertedUploadSound convertedUploadSoundWindow;
	
	/**
	 * 其它
	 */
	private SoundFile soundFile;
	private SoundFileService soundFileService;
	private CommonService commonService;

	/**
	 * 构造器
	 */
	public SoundUpload() {
		this.initService();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());
		// 初始化Sql语句
		search.click();
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());
	}

	/**
	 * 将Service进行初始化
	 */
	private void initService() {
		soundFileService=SpringContextHolder.getBean("soundFileService");
		commonService=SpringContextHolder.getBean("commonService");
	}

	/**
	 * 创建搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:"));// 关键字
		keyWord = new TextField();// 输入区域
		keyWord.setWidth("6em");
		keyWord.setStyleName("search");
		keyWord.setInputPrompt("语音上传名称");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		// 搜索按钮
		search = new Button("搜索");
		search.setStyleName("small");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

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
		// 创建表格
		table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);
		// 创建按钮
		tabelAndButtonsLayout.addComponent(buildTableButtons());
		return tabelAndButtonsLayout;
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建按钮的输出，为Table设置数据源
	 * 
	 * @return
	 */
	private HorizontalLayout buildTableButtons() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setWidth("100%");
		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);
		tableButtons.addComponent(tableButtonsLeft);

		//新建语音上传
		add = new Button("上传并转码");
		add.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(add);

		//新建语音上传
		convertedUpload = new Button("转码后上传");
		convertedUpload.setDescription("16位、单声道、8000HZ的 wav语音文件");
		convertedUpload.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(convertedUpload);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		delete = new Button("删除");
		delete.setEnabled(false);
		delete.setStyleName(StyleConfig.BUTTON_STYLE);
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);

		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<SoundFile>(
				SoundFile.class, soundFileService, table,
				sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);
		flip.getEntityContainer().addNestedContainerProperty("user.username");

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "descName","storeName",
				"importDate","user.username"};
		String[] columnHeaders = new String[] { "ID", "语音名称","唯一标识", "上传时间","上传者"};
		
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("SoundFile");
		// 关键字过滤,语音上传名
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like autoDialoutTaskName = new SqlGenerator.Like("descName",
				keyWordStr);
		sqlGenerator.addAndCondition(autoDialoutTaskName);

		// 排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();
		// 更新Table，并使Table处于未选中
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 执行删除操作
	 */
	private void executeDelete() {
		String nativeSql="select count(*) from ec2_auto_dialout_task where soundfile_id="+soundFile.getId();
		Long count=(Long)commonService.excuteNativeSql(nativeSql, ExecuteType.SINGLE_RESULT);
		if(count>0){
			NotificationUtil.showWarningNotification(this,"语音文件"+soundFile.getDescName()+"被自动外呼使用，不能删除！");
			return;
		}

		// jrh 检查当前语音是否有与之相关联的IVRMenu、IVRAction 对象
		boolean isDeleteAble = soundFileService.checkDeleteAbleByIVR(soundFile.getId(), soundFile.getDomain().getId());
		if(!isDeleteAble) {
			NotificationUtil.showWarningNotification(this,"语音文件"+soundFile.getDescName()+"正被IVR(呼入语音导航)使用，不能删除！");
			return;
		}
		
		Label label = new Label("您确定要删除语音<b>"
		+ soundFile.getDescName() + "</b>?",
		Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
		"confirmDelete");
		this.getWindow().addWindow(confirmWindow);
	}
	
	/**
	 * 显示语音上传窗口
	 */
	private void showAddSoundFileWindow() {
//		if (addWindow == null) { chenhb : comment
		addWindow = new AddSound(this); 
//		}
		this.getWindow().addWindow(addWindow);
	}

	/**
	 * 显示语音上传窗口
	 */
	private void showConvertedUploadSoundFileWindow() {
//		if (convertedUploadSoundWindow== null) {
			convertedUploadSoundWindow = new ConvertedUploadSound(this);
//		}
		this.getWindow().addWindow(convertedUploadSoundWindow);
	}

	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
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
	 * 取得表格组件的引用
	 */
	public Table getTable(){
		return table;
	}

	/**
	 * 由弹出窗口回调确认删除语音上传
	 */
	public void confirmDelete(Boolean isConfirmed) {
			if (isConfirmed == true) {
			try {
				soundFileService.deleteById(soundFile.getId());
				this.updateTable(false);
				table.setValue(null);
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this,"删除异常！");
				e.printStackTrace();
			}
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

	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {ADD};
		}
		return new Action[] {ADD, DELETE};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (ADD == action) {
			add.click();
		} else if (DELETE == action) {
			delete.click();
		}
	}

	/**
	 * 按钮单击监听器
	 * <p>
	 * 搜索、高级搜索、新建语音上传、开始、停止、添加CSR/添加资源 、指派任务
	 * </p>
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			// 普通搜索
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "搜索出现错误");
			}
		}else if (event.getButton() == delete) {
			// 语音上传删除
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "删除出现异常！");
			}
		} else if (event.getButton() == add) {
			showAddSoundFileWindow();
		} else if (event.getButton() == convertedUpload) {
			showConvertedUploadSoundFileWindow();
		}
	}
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		soundFile=(SoundFile)table.getValue();
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			delete.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			delete.setEnabled(false);
		}
	}
}
