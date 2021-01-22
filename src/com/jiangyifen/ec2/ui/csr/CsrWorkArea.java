package com.jiangyifen.ec2.ui.csr;

import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CsrWorkArea extends VerticalLayout {
	private Integer[] screenResolution;	// 屏幕分辨率
	
	private CsrWorkAreaSideBar csrSideBar;
	private CsrWorkAreaRightView csrRightView;
	private HorizontalSplitPanel csrWorkAreaSplitPanel;
	
	public CsrWorkArea() {
		this.setSizeFull();
		screenResolution = SpringContextHolder.getScreenResolution();
		
		csrWorkAreaSplitPanel = new HorizontalSplitPanel();
		this.addComponent(csrWorkAreaSplitPanel);
		
		if(screenResolution[0] < 1400) {	// 根据横向分辨率设置水平分割点
			csrWorkAreaSplitPanel.setSplitPosition(190, Panel.UNITS_PIXELS);
		} else if(screenResolution[0] >= 1400) {
			csrWorkAreaSplitPanel.setSplitPosition(210, Panel.UNITS_PIXELS);
		} else if(screenResolution[0] >= 1600) {
			csrWorkAreaSplitPanel.setSplitPosition(250, Panel.UNITS_PIXELS);
		}
		
		csrRightView = new CsrWorkAreaRightView();
		csrWorkAreaSplitPanel.setSecondComponent(csrRightView);
		
		csrSideBar = new CsrWorkAreaSideBar(csrRightView);
		csrWorkAreaSplitPanel.setFirstComponent(csrSideBar);
	}

	public CsrWorkAreaSideBar getCsrSideBar() {
		return csrSideBar;
	}

	public CsrWorkAreaRightView getCsrRightView() {
		return csrRightView;
	}
	
}
