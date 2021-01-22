package com.jiangyifen.ec2.ui.mgr.system.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.system.realtime.pojo.form.SystemStatistic;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;

/**
 * 系统非业务数据统计
 * 
 * @author lxy
 * 
 */
public class RealTimeSystemStatisticPanel extends Panel implements ClickListener {

	private static final long serialVersionUID = 5025890708361430529L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志工具

	// 持有UI对象
	// private QuestionnaireManagement questionnaireManagement;
	// 本控件持有对象

	// 页面布局组件
	private HorizontalLayout hl_cpu;
	private HorizontalLayout hl_memory_ram;
	private HorizontalLayout hl_memory_swap;
	private HorizontalLayout hl_disk;
	private HorizontalLayout hl_network_up;
	private HorizontalLayout hl_network_down;

	// 页面控件
	private Label lb_msg;

	private Label lb_move;
	
	private Label lb_cpu;
	private Link lk_cpu;

	private Label lb_memory_ram;
	private Link lk_memory_ram;

	private Label lb_memory_swap;
	private Link lk_memory_swap;

	private Label lb_disk_root;
	private Link lk_disk_root;

	private Label lb_network_up;
	private Link lk_network_up;

	private Label lb_network_down;
	private Link lk_network_down;

	// private Button bt_excute;

	// 业务必须对象
 	
	// 业务本页对象
	 
	// 业务注入对象

	public RealTimeSystemStatisticPanel() {
		 
		this.addComponent(new Label("CPU"));
		hl_cpu = new HorizontalLayout();
		hl_cpu.setSpacing(true);
		hl_cpu.setWidth("100%");
		int cpu_use_val = 0;
		String cpu_use_str = findDDB("#CCCCCC", "margin:0", "#00AA00",cpu_use_val + "%", "&nbsp");
		lb_cpu = new Label(cpu_use_str, Label.CONTENT_XHTML);
		lb_cpu.setWidth("100%");
		hl_cpu.addComponent(lb_cpu);
		hl_cpu.setExpandRatio(lb_cpu, 0.7f);
		lk_cpu = new Link();
		lk_cpu.setCaption("空");
		lk_cpu.setWidth("50px");
		hl_cpu.addComponent(lk_cpu);
		this.addComponent(hl_cpu);

		this.addComponent(new Label("内存"));
		hl_memory_ram = new HorizontalLayout();
		hl_memory_ram.setSpacing(true);
		hl_memory_ram.setWidth("100%");
		int memory_app_val = 0;
		String memory_app_str = findDDB("#CCCCCC", "margin:0", "#00AA00",memory_app_val + "%", "&nbsp");
		lb_memory_ram = new Label(memory_app_str, Label.CONTENT_XHTML);
		lb_memory_ram.setWidth("100%");
		hl_memory_ram.addComponent(lb_memory_ram);
		hl_memory_ram.setExpandRatio(lb_memory_ram, 0.7f);
		lk_memory_ram = new Link();
		lk_memory_ram.setCaption("空");
		lk_memory_ram.setWidth("50px");
		hl_memory_ram.addComponent(lk_memory_ram);
		this.addComponent(hl_memory_ram);

		this.addComponent(new Label("交换"));
		hl_memory_swap = new HorizontalLayout();
		hl_memory_swap.setSpacing(true);
		hl_memory_swap.setWidth("100%");
		int memory_swap_val= 0;
 		String memory_swap_str = findDDB("#CCCCCC", "margin:5px 0 0 0","#CCCCCC", memory_swap_val + "%", "&nbsp");
 		lb_memory_swap = new Label(memory_swap_str, Label.CONTENT_XHTML);
		lb_memory_swap.setWidth("100%");
		hl_memory_swap.addComponent(lb_memory_swap);
		hl_memory_swap.setExpandRatio(lb_memory_swap, 0.7f);
		lk_memory_swap = new Link();
		lk_memory_swap.setCaption("空");
		lk_memory_swap.setWidth("50px");
		hl_memory_swap.addComponent(lk_memory_swap);
		this.addComponent(hl_memory_swap);

		this.addComponent(new Label("硬盘"));

		hl_disk = new HorizontalLayout();
		hl_disk.setSpacing(true);
		hl_disk.setWidth("100%");
		int disk_root_val = 0;
		String disk_root_str = findDDB("#CCCCCC", "margin:0", "#00AA00",disk_root_val + "%", "/");//FF0000
		lb_disk_root = new Label(disk_root_str, Label.CONTENT_XHTML);
		lb_disk_root.setWidth("100%");
		hl_disk.addComponent(lb_disk_root);
		hl_disk.setExpandRatio(lb_disk_root, 0.7f);
		lk_disk_root = new Link();
		lk_disk_root.setCaption("空");
		lk_disk_root.setWidth("50px");
		hl_disk.addComponent(lk_disk_root);
		this.addComponent(hl_disk);

		//this.addComponent(new Label("网络"));
		hl_network_up = new HorizontalLayout();
		hl_network_up.setSpacing(true);
		hl_network_up.setWidth("100%");
		//String network_up_val = "13.34KB/s";
 		String network_up_str = findDDB("#CCCCCC", "margin:5px 0 0 0","#CCCCCC", "100%", "上行");
		lb_network_up = new Label(network_up_str, Label.CONTENT_XHTML);
		lb_network_up.setWidth("100%");
		hl_network_up.addComponent(lb_network_up);
		hl_network_up.setExpandRatio(lb_network_up, 0.7f);
		lk_network_up = new Link();
		lk_network_up.setCaption("空");
		lk_network_up.setWidth("90px");
		hl_network_up.addComponent(lk_network_up);
		//this.addComponent(hl_network_up);

		hl_network_down = new HorizontalLayout();
		hl_network_down.setSpacing(true);
		hl_network_down.setWidth("100%");
		//String network_down_val = "13.34KB/s";
 		// "<dd style='margin:5px 0 0 0 ;height:1.5em;display:block;background:#CCCCCC;width:100%;'><b style='float:left;display:block; background:#CCCCCC; width:100%;'>下行</b></dd>";
		String network_down_str = findDDB("#CCCCCC", "margin:5px 0 0 0","#CCCCCC", "100%", "下行");
		lb_network_down = new Label(network_down_str, Label.CONTENT_XHTML);
		lb_network_down.setWidth("100%");
		hl_network_down.addComponent(lb_network_down);
		hl_network_down.setExpandRatio(lb_network_down, 0.7f);
		lk_network_down = new Link();
		lk_network_down.setCaption("空");
		lk_network_down.setWidth("90px");
		hl_network_down.addComponent(lk_network_down);
		//this.addComponent(hl_network_down);
		
		HorizontalLayout hl_lb_msg = new HorizontalLayout();
		hl_lb_msg.setMargin(true,false,false,false);
		hl_lb_msg.setSpacing(true);
		lb_msg = new Label("刷新频率：2秒", Label.CONTENT_XHTML);
		lb_move = new Label("[]", Label.CONTENT_XHTML);
		hl_lb_msg.addComponent(lb_msg);
		hl_lb_msg.addComponent(new Label("运行时间", Label.CONTENT_XHTML));
		hl_lb_msg.addComponent(lb_move);
		this.addComponent(hl_lb_msg);
	}

	/**
	 * 更新系统监控的信息,不报异常，自己处理异常
	 * @param statistic
	 */
	public void updateStatistics(SystemStatistic statistic){
		try {
			if(null != statistic){
				if(1 == statistic.getFindCode()){
					
					lb_move.setValue(statistic.getRunTime());	//系统运行时间
					
					long cpuUse = statistic.getCupUse();		//CPU使用率
					long cpuUseVl = 100-cpuUse;
					if(cpuUseVl <= 80){
						lb_cpu.setValue(findDDB("#CCCCCC", "margin:0", "#00AA00", cpuUseVl+ "%", "&nbsp"));
					}else{
						lb_cpu.setValue(findDDB("#CCCCCC", "margin:0", "#FF0000", cpuUseVl+ "%", "&nbsp"));
					}
					lk_cpu.setCaption(cpuUseVl + "%");
					
					long diskRootVl = statistic.getDiskRootUse();//硬盘使用率
					if(diskRootVl <= 80){
						lb_disk_root.setValue(findDDB("#CCCCCC", "margin:0", "#00AA00", diskRootVl+ "%", "/"));
					}else{
						lb_disk_root.setValue(findDDB("#CCCCCC", "margin:0", "#FF0000", diskRootVl+ "%", "/"));
					}
					lk_disk_root.setCaption(diskRootVl + "%");
					
					double  memoryRamTotal = statistic.getMemoryRamTotal();//内存使用率
					double  memoryRamFree = statistic.getMemoryRamFree();
			  		double memoryRamAvgVl = ((memoryRamTotal - memoryRamFree)/memoryRamTotal) * 100;
			 		NumberFormat formatter = new DecimalFormat("0.00");
			 		String memoryRamAvgVlStr = formatter.format(memoryRamAvgVl);
					if(memoryRamAvgVl <= 80){
						lb_memory_ram.setValue(findDDB("#CCCCCC", "margin:0", "#00AA00",memoryRamAvgVlStr + "%", "&nbsp"));
					}else{
						lb_memory_ram.setValue(findDDB("#CCCCCC", "margin:0", "#FF0000",memoryRamAvgVlStr + "%", "&nbsp"));

					}
			 		lk_memory_ram.setCaption(memoryRamAvgVlStr + "%");
					
					double memorySwapTotal = statistic.getMemorySwapTotal();//交换
					double memorySwapFree = statistic.getMemorySwapFree();
					double memorySwapAvgVl = ((memorySwapTotal - memorySwapFree)/memorySwapTotal) * 100;
					String memorySwapAvgVlStr = formatter.format(memorySwapAvgVl);
					if(memorySwapAvgVl <= 80){
						lb_memory_swap.setValue(findDDB("#CCCCCC", "margin:0", "#00AA00",memorySwapAvgVlStr + "%", "&nbsp"));
					}else{
						lb_memory_swap.setValue(findDDB("#CCCCCC", "margin:0", "#FF0000",memorySwapAvgVlStr + "%", "&nbsp"));
					}
					lk_memory_swap.setCaption(memorySwapAvgVlStr + "%");
					lb_msg.setValue("刷新频率：2秒");
				}else{
					resetStatistics();
					lb_msg.setValue(WorkUIUtils.fontColorHtmlString(statistic.getFindMsg()));
				}
			}else{
				//没有到数据
				resetStatistics();
				lb_msg.setValue(WorkUIUtils.fontColorHtmlString("服务器SMP连接失败"));
			}
		} catch (Exception e) {
 			e.printStackTrace();
 			 logger.error("系统监控信息展示_updateStatistics_LLXXYY", e);
		}
	}
	
	public void resetStatistics(){
		lk_cpu.setCaption("空");
		lk_memory_ram.setCaption("空");
		lk_memory_swap.setCaption("空");
		lk_disk_root.setCaption("空");
		lk_network_up.setCaption("空");
		lk_network_down.setCaption("空");
	}

	private String findDDB(String ddcolor, String ddmargin, String bcolor,String bwidth, String text) {
		StringBuffer ddbBuffer = new StringBuffer();
		ddbBuffer.append("<dd style='");
		ddbBuffer.append(ddmargin);
		ddbBuffer.append(";height:1.5em;display:block;background:");
		ddbBuffer.append(ddcolor);
		ddbBuffer.append(";width:100%;'><b style='float:left;display:block; background:");
		ddbBuffer.append(bcolor);
		ddbBuffer.append("; width:");
		ddbBuffer.append(bwidth);
		ddbBuffer.append(";'>");
		ddbBuffer.append(text);
		ddbBuffer.append("</b></dd>");
		return ddbBuffer.toString();
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		
	}
	
}
