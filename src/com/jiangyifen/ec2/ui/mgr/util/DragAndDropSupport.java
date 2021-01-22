package com.jiangyifen.ec2.ui.mgr.util;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;

public class DragAndDropSupport {
	
	public static void addDragAndDropSupport(Table firstTable,Table secondTable){
		initializeTable(new SourceIs(secondTable),firstTable);
		initializeTable(new SourceIs(firstTable),secondTable);
	}
	
	@SuppressWarnings("serial")
	private static void initializeTable(final ClientSideCriterion acceptCriterion,final Table table) {
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			public void drop(DragAndDropEvent dropEvent) {
				//验证传递过来的是否是可传递的对象
				DataBoundTransferable transferable= (DataBoundTransferable) dropEvent.getTransferable();
				//不是BeanItemContainer则不响应，直接返回
				if (!(transferable.getSourceContainer() instanceof BeanItemContainer)) {
					return;
				}
				
				//获取源sourceItemId
				Object sourceItemId = transferable.getItemId();
				
//				//选中要Drop到的targetItemId
//				AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent
//						.getTargetDetails());
//				Object targetItemId = dropData.getItemIdOver();
				table.getContainerDataSource().addItem(sourceItemId);
				transferable.getSourceContainer().removeItem(sourceItemId);
			}
			public AcceptCriterion getAcceptCriterion() {
				return new com.vaadin.event.dd.acceptcriteria.And(acceptCriterion, AcceptItem.ALL);
			}
		});
	}
	
}
