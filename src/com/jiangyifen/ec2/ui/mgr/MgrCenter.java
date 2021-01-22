package com.jiangyifen.ec2.ui.mgr;

import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrTabSheet;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;

/**
 * Manager的主窗口的 中间部分（主要信息输出部分，包括 Accordin 和 主输出）
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrCenter extends HorizontalLayout{

	private Integer[] screenResolution;	// 屏幕分辨率
	
	// splitPanel分隔条
	private HorizontalSplitPanel splitPanel;
	//左侧Accordion
	private MgrAccordion mgrAccordion;
	//右侧TabSheet
	private MgrTabSheet mgrTabSheet;

	public MgrCenter() {
		this.setSizeFull();

		screenResolution = SpringContextHolder.getScreenResolution();
		
		// 水平分隔条组件
		splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
//		splitPanel.setSplitPosition(17,HorizontalSplitPanel.UNITS_PERCENTAGE);
//	jrh 修改左侧分割条宽度显示   start edit
		if(screenResolution[0] < 1400) {	// 根据横向分辨率设置水平分割点
			splitPanel.setSplitPosition(190, Panel.UNITS_PIXELS);
		} else if(screenResolution[0] >= 1400) {
			splitPanel.setSplitPosition(210, Panel.UNITS_PIXELS);
		} else if(screenResolution[0] >= 1600) {
			splitPanel.setSplitPosition(250, Panel.UNITS_PIXELS);
		}
//	jrh 修改左侧分割条宽度显示   end edit
		
		this.addComponent(splitPanel);

		// 分隔条左侧
		mgrAccordion=new MgrAccordion();
		mgrAccordion.setSizeFull();
		splitPanel.setFirstComponent(mgrAccordion);

		// 分隔条右侧
		mgrTabSheet=new MgrTabSheet();
		mgrTabSheet.setSizeFull();
		mgrAccordion.setMgrTabSheet(mgrTabSheet);
		splitPanel.setSecondComponent(mgrTabSheet);
	}

}
