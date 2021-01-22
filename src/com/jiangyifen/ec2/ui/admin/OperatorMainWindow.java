package com.jiangyifen.ec2.ui.admin;

import com.jiangyifen.ec2.ui.admin.tab.OperatorTabSheet;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.StateVisiableVaraible;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Window;

/**
 * 系统信息查看主界面
 * 
 * @author JHT
 */
@SuppressWarnings("serial")
public class OperatorMainWindow extends Window{
	/**
	 * 主要组件
	 */
	private OperatorTabSheet operatorTabSheet;					// TabSheet显示组件
	private MenuBar topMenu;									// 头部显示的菜单
	
	public OperatorMainWindow(){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setScrollable(false);
		this.setImmediate(true);
		this.setWidth("1000px");
		this.setHeight("700px");
		this.setCaption("系统信息");
		
		this.addComponent(buildShowTabSheetTopButtons());
		
		operatorTabSheet = new OperatorTabSheet();
		operatorTabSheet.setSizeFull();
		this.addComponent(operatorTabSheet);
	}
	
	// 添加在TabSheet上面的水平布局
	private HorizontalLayout buildShowTabSheetTopButtons() {
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setWidth("100%");
		topMenu = new MenuBar();
		topMenu.setWidth("100%");
		topLayout.addComponent(topMenu);
		
		/*MenuBar.MenuItem itemOperator = topMenu.addItem("文件", null);
		MenuBar.MenuItem itemTool = topMenu.addItem("工具", null);*/
		MenuBar.MenuItem itemSystem = topMenu.addItem("系统", null);
		
		/*itemOperator.addItem("查询", menuCommand);
		itemOperator.addItem("退出", menuCommand);
		itemTool.addItem("查询", menuCommand);
		itemTool.addItem("退出", menuCommand);*/
		/*itemSystem.addItem("查询", menuCommand);*/
		itemSystem.addItem("退出", menuCommand);
		return topLayout;
	}

	private Command menuCommand = new Command() {
		@Override
		public void menuSelected(MenuItem selectedItem) {
			if(selectedItem.getText() == "退出"){
				closeWindow();
			}
		}
	};
	
	// 关闭窗体操作
	private void closeWindow(){
		this.close();
	}

	// 当执行了窗体的关闭操作时, 操作员操作状态设置为False
	@Override
	protected void close() {	
		super.close();
		StateVisiableVaraible.OPERATOR_SHOW_STATE = false;
	}
	
	
}
