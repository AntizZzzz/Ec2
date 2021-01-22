package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Knowledge;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.KnowledgeStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.LucenceService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.knowledgebase.AddKnowledgeWindow;
import com.jiangyifen.ec2.ui.mgr.knowledgebase.EditKnowledgeWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 知识库 管理页面
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class KnowledgeManagement extends VerticalLayout implements
		Button.ClickListener,Property.ValueChangeListener {

	// 搜索组件
	private TextField keyWord;
	private Button search;

	// 表格组件
	private Table table;

	// 按钮组件
	private Button add;
	private Button delete;
	private Button edit;
	private Button publish;
	private Button unPublish;
	private Button publishAll;
	
	
	//Sql语句
	private String sqlSelect;
	private String sqlCount;

	// 当前选中的知识
	private Knowledge selectedKnowledge;
	// 弹出窗口
	private AddKnowledgeWindow addWindow;
	private EditKnowledgeWindow editWindow;
	private FlipOverTableComponent<Knowledge> flip;
//	private BeanItemContainer<Knowledge> container;

	private CommonService commonService;
	 private LucenceService lucenceService;
	private Domain domain;

	private ArrayList<Knowledge> needPublishKnowledges=new ArrayList<Knowledge>();
	
	public KnowledgeManagement() {
		commonService = SpringContextHolder.getBean("commonService");
		lucenceService=SpringContextHolder.getBean("lucenceService");
		this.domain = SpringContextHolder.getDomain();
		
		this.setSizeFull();
		this.setMargin(true);

		// 设置最大的layout
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSpacing(true);
		this.addComponent(contentLayout);
		// 添加搜索组件
		contentLayout.addComponent(buildSearchLayout());
		// 初始化container
		search.click();
		// 添加表格和按钮组件
		contentLayout.addComponent(buildTableAndButtonsLayout());

	}

	// 创建搜索组件
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		// searchLayout.setWidth("100%");


		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:"));// 关键字
		keyWord = new TextField();// 输入区域
		keyWord.setWidth("6em");
		keyWord.setStyleName("search");
		keyWord.setInputPrompt("标题名");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		//搜索按钮
		search = new Button("查询");
		search.addListener((ClickListener) this);
		searchLayout.addComponent(search);

		//搜索按钮
		publishAll = new Button("发布全部");
		publishAll.addListener((ClickListener) this);
//		searchLayout.addComponent(publishAll);
		return searchLayout;
	}

	// 创建表格和按钮组件
	private VerticalLayout buildTableAndButtonsLayout() {
		VerticalLayout tableAndButtonsLayout = new VerticalLayout();
		tableAndButtonsLayout.setSpacing(true);

		table = new Table();
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addListener((Property.ValueChangeListener) this);

		table.addGeneratedColumn("content", new Table.ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				Knowledge message = (Knowledge) itemId;
				String value = "";
				if (message.getContent().length() < 10) {
					value = message.getContent();
				} else {
					value = message.getContent().substring(0, 9) + "......";
				}
				Label content = new Label();
				content.setValue(value);
				content.setDescription(message.getContent());
				return content;
			}
		});
		

		tableAndButtonsLayout.addComponent(table);

		// 创建按钮
		tableAndButtonsLayout.addComponent(buildButtons());

		table.addGeneratedColumn("发布", new PublishColumnGenerator());

		return tableAndButtonsLayout;
	}

	// 创建按钮组件
	@SuppressWarnings("unchecked")
	private HorizontalLayout buildButtons() {

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("100%");
		buttonsLayout.setSpacing(true);

		
		//左侧组件
		HorizontalLayout leftButtonsLayout = new HorizontalLayout();
		add = new Button("新建");
		add.addListener((ClickListener) this);
		leftButtonsLayout.addComponent(add);
		edit = new Button("编辑");
		edit.addListener((ClickListener) this);
		edit.setEnabled(false);
		leftButtonsLayout.addComponent(edit);
		delete = new Button("删除");
		delete.addListener((ClickListener) this);
		delete.setEnabled(false);
		leftButtonsLayout.addComponent(delete);
		unPublish = new Button("取消发布");
		unPublish.setEnabled(false);
		unPublish.setStyleName("default");
		unPublish.addListener((ClickListener) this);
		leftButtonsLayout.addComponent(unPublish);
		publish = new Button("发布");
		publish .setEnabled(false);
		publish.setStyleName("default");
		publish.addListener((ClickListener) this);
		leftButtonsLayout.addComponent(publish);
//		publishAll = new Button("重新发布");
//		publishAll.setStyleName("default");
//		publishAll.addListener((ClickListener) this);
//		leftButtonsLayout.addComponent(publishAll);
		buttonsLayout.addComponent(leftButtonsLayout);
		buttonsLayout.addComponent(leftButtonsLayout);

		
		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<Knowledge>(
				Knowledge.class, commonService, table,
				sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);


		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "user", "title","knowledgeStatus","content"};
		String[] columnHeaders = new String[] { "创建者", "知识标题", "知识状态","知识内容"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		buttonsLayout.addComponent(flip);
		buttonsLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return buttonsLayout;
	}

	// 弹出添加窗口
	private void showAddWindow() {
		if (addWindow == null) {
			addWindow = new AddKnowledgeWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(addWindow);
		this.getApplication().getMainWindow().addWindow(addWindow);

	}

	// 弹出编辑窗口
	private void showEditWindow() {
		if (editWindow == null) {
			editWindow = new EditKnowledgeWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(editWindow);
		this.getApplication().getMainWindow().addWindow(editWindow);

	}

	/**
	 * 更新表格显示的内容
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

	// 执行删除
	private void executeDelete() {
		Label label = new Label("您确定要删除吗?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);

	}

	// 执行搜索
	private void executeSearch() {
		//读取搜索关键字
		String keyWordStr=StringUtils.trimToEmpty((String)keyWord.getValue());
		
		//拼装Sql语句
		sqlSelect="select k from Knowledge k where k.title like "+"'%"+keyWordStr+"%' order by k.id desc";
		sqlCount="select count(k) from Knowledge k where k.title like "+"'%"+keyWordStr+"%'";
		
		//更新表格
		updateTable(true);
	}

	// 执行发布操作            
	@SuppressWarnings("unchecked")
	private void executePublishAll() {
		//生成还不存在的文件
		String sql="select k from Knowledge k where k.domain.id="+domain.getId();
		List<Knowledge> knowledgeList=(List<Knowledge>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
		
		lucenceService.generateFile(domain.getId(), knowledgeList, false);
		for(Knowledge knowledge:knowledgeList){
			knowledge.setKnowledgeStatus(KnowledgeStatus.PUBLISHED);
			commonService.update(knowledge);
		}
		
		//创建索引
		 String knowledgebase=(String)Config.props.get("knowledgebase");
		 knowledgebase=knowledgebase+domain.getId()+"/";
		 String filePath=knowledgebase+"file/";
		 String toPath=knowledgebase+"index/";
		 
		 lucenceService.indexFile(filePath,toPath);
	}

	// 执行发布操作            
	private void executePublish() {
		if(publish.getCaption().equals("发布")){
			selectedKnowledge.setKnowledgeStatus(KnowledgeStatus.PUBLISHED);
			commonService.update(selectedKnowledge);
		}
		
		lucenceService.generateFile(domain.getId(), needPublishKnowledges, false);
		for(Knowledge knowledge:needPublishKnowledges){
			knowledge.setKnowledgeStatus(KnowledgeStatus.PUBLISHED);
			commonService.update(knowledge);
		}
		
		//创建索引
		String knowledgebase=(String)Config.props.get("knowledgebase");
		knowledgebase=knowledgebase+domain.getId()+"/";
		String filePath=knowledgebase+"file/";
		String toPath=knowledgebase+"index/";
		
		lucenceService.indexFile(filePath,toPath);
		needPublishKnowledges.clear();
		publish.setCaption("发布");
		publish.setEnabled(false);
	}

	// 执行发布操作            
	private void executeUnPublish() {
		//===================删除文件===================//
		String knowledgebase=(String)Config.props.get("knowledgebase");
		knowledgebase=knowledgebase+domain.getId()+"/";
		String filePath=knowledgebase+"file/";
		String toPath=knowledgebase+"index/";
		
		//删除文件
		String fileName=filePath+selectedKnowledge.getTitle()+".txt";
		File toDeleteFile=new File(fileName);
		if(toDeleteFile.exists()){
			toDeleteFile.delete();
		}
//		toDeleteFile.
		
		//重建索引
		lucenceService.indexFile(filePath,toPath);
		
		//更新实体
		selectedKnowledge.setKnowledgeStatus(KnowledgeStatus.UNPUBLISHED);
		commonService.update(selectedKnowledge);
		unPublish.setEnabled(false);
	}
	
	/**
	 * 用于自动生成“发布”列，用于标记 是否将对应行发布
	 */
	private class PublishColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final Knowledge knowledge = (Knowledge) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if(knowledge.getKnowledgeStatus()==KnowledgeStatus.PUBLISHED) {
				check.setValue(true);
				check.setEnabled(false);
			}else{
				check.setValue(false);
			}

			if(needPublishKnowledges.contains(knowledge)) {
				check.setValue(true);
			}
			
			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if(value == false) {
						needPublishKnowledges.remove(knowledge);
					} else {
						needPublishKnowledges.add(knowledge);
					}
					
					// 修改删除按钮
					if(needPublishKnowledges.size() > 0) {
						publish.setCaption("发布勾选项");
						publish.setEnabled(true);
					} else {
						publish.setCaption("发布");
						if(selectedKnowledge!=null&&selectedKnowledge.getKnowledgeStatus()==KnowledgeStatus.UNPUBLISHED){
							publish.setEnabled(true);
						}else{
							publish.setEnabled(false);
						}
						publish.setEnabled(table.getValue() != null);
					}
				}
			});
			
        	return check;
		}
	}

	
	// 确认删除窗口
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed) {
			//==============================生成并写入文件=========================//
			String knowledgebase=(String)Config.props.get("knowledgebase");
			knowledgebase=knowledgebase+domain.getId()+"/";
			
			// 把数据存到txt文件中
			File file = new File(knowledgebase+selectedKnowledge.getTitle()+".txt");
			if (file.exists()) {
				file.delete();
			}

			//===============删除实体
			commonService.delete(Knowledge.class, selectedKnowledge.getId());
			table.setValue(null);
			this.updateTable(true);
			this.getApplication().getMainWindow().showNotification("删除成功！");
		}
	}

	// 表格valueChange事件
	@Override
	public void valueChange(ValueChangeEvent event) {
		this.selectedKnowledge = (Knowledge) table.getValue();

		// 改变按钮
		if (selectedKnowledge != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			if(selectedKnowledge.getKnowledgeStatus()==KnowledgeStatus.PUBLISHED){
				unPublish.setEnabled(true);
			}else{
				if(publish.getCaption().equals("发布"))
					publish.setEnabled(true);
			}
			edit.setEnabled(true);
			delete.setEnabled(true);
		} else {
			if(needPublishKnowledges.size()==0){
				if(publish.getCaption().equals("发布"))
					publish.setEnabled(false);
			}
			// 应该是可以通过设置父组件来使之全部为false;
			unPublish.setEnabled(false);
			edit.setEnabled(false);
			delete.setEnabled(false);
		}

	}

	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == add) {
			showAddWindow();
		} else if (event.getButton() == edit) {
			showEditWindow();
		} else if (event.getButton() == delete) {
			executeDelete();
		} else if (event.getButton() == search) {
			executeSearch();
		} else if (event.getButton() == publish) {
			executePublish();
			table.setValue(null);
			this.updateTable(false);
			NotificationUtil.showWarningNotification(this,"知识发布成功");
		} else if (event.getButton() == publishAll) {
			executePublishAll();
			NotificationUtil.showWarningNotification(this,"所有知识发布成功");
		} else if (event.getButton() == unPublish) {
			executeUnPublish();
			//表格不选中
			table.setValue(null);
			this.updateTable(false);
			NotificationUtil.showWarningNotification(this,"取消发布成功");
		}

	}

	// *****************setter和getter*******************//
	

	public Table getTable() {
		return table;
	}

	public Knowledge getSelectedKnowledge() {
		return selectedKnowledge;
	}

	public void setSelectedKnowledge(Knowledge selectedKnowledge) {
		this.selectedKnowledge = selectedKnowledge;
	}

	public Button getSearch() {
		return search;
	}

	public void setSearch(Button search) {
		this.search = search;
	}

}
