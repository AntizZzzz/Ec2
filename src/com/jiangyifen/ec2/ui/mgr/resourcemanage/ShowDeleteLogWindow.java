package com.jiangyifen.ec2.ui.mgr.resourcemanage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowDeleteLogWindow extends Window implements Window.CloseListener,ClickListener{
	
	private static final long serialVersionUID = -7764799543176919415L;
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final String[] COL_HEADERS = new String[] { "操作时间","操作"};
	private final Object[] VISIBLE_PROPERTIES = new Object[] { "operateDate","description"};
	
	
	private Table table;
 	
	private Button btnReload;		//保存关闭
	private Button btnCcl;		//取消按钮
	
	private Domain domain;

	private BeanItemContainer<OperationLog> beanItemContainer = new BeanItemContainer<OperationLog>(OperationLog.class);
	private CommonService commonService;
	
	
	public ShowDeleteLogWindow(String width,String height){
		
		this.center();
		this.setModal(true);
		this.setWidth(width);
		this.setHeight(height);
		this.setResizable(false);
		this.setCaption("删除日志");
		
		initBusinessAndSpring();
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		table = new Table() {
			private static final long serialVersionUID = 6984501501698797941L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getType() == Date.class) {
					return sdf.format((Date) property.getValue());
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};

		table.setStyleName("striped");
		table.setWidth("100%");
		table.setHeight("210px");
		table.setSelectable(true);
		table.setImmediate(true);
		
		reloadTable();
		
		
		mainLayout.addComponent(table);
		
		
		HorizontalLayout foolter = new HorizontalLayout();
		foolter.setSpacing(true);
		foolter.setSizeFull();
		btnReload = new Button("刷新",this);
		btnReload.setStyleName("default");
		foolter.addComponent(btnReload);
		
		btnCcl = new Button("关闭",this);
		foolter.addComponent(btnCcl);
		foolter.setComponentAlignment(btnCcl, Alignment.BOTTOM_RIGHT);
		
		mainLayout.addComponent(foolter);
		this.addComponent(mainLayout);
	}



	private void initBusinessAndSpring() {
		domain = SpringContextHolder.getDomain();
		commonService = SpringContextHolder.getBean("commonService");
	}

	private void reloadTable() {
		String jpql = "select s from OperationLog as s where s.ip='DELETE_RESOURCE' and s.domain.id ="+domain.getId()+" order by s.id desc ";
		List<OperationLog> ls = commonService.getEntitiesByJpql(jpql);
		beanItemContainer.removeAllItems();
		if(ls != null){
			beanItemContainer.addAll(ls);
		}
		table.setContainerDataSource(beanItemContainer);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		closeWindow();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnReload){
			reloadTable();
		}else if(source == btnCcl){
			closeWindow();
		}
	}
	
	public void closeWindow(){
		this.getApplication().getMainWindow().removeWindow(this);
	}

}
