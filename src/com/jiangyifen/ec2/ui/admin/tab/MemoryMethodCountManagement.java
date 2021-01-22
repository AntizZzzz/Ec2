package com.jiangyifen.ec2.ui.admin.tab;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.event.QueueEntryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.MemoryMethod;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MemoryMethodService;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.MemoryMethodCount;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * 查看内存数据的大小以及类型信息主界面
 * 
 * @author JHT
 * 
 * @date 2014-10-16 上午9:29:18
 * 
 */
public class MemoryMethodCountManagement extends VerticalLayout implements Button.ClickListener {

	private static final long serialVersionUID = -3602526932842983252L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());			// 日志工具
	private final Object[] VISIBLE_COLUMNS = new Object[] { "name", "type", "count", "sunCount" };
	private final String[] COLUMN_HEADERS = new String[] { "内存名称", "内存类型", "大小", "内存类型的Value是集合时大小" };

	// 主要组件
	private Table table; // 用来显示表格的信息
	private Button btnRefresh; // 刷新表格数据

	// 其他参数
	private MemoryMethodService memoryMethodService; // 用来查询内存信息的Service
	private BeanItemContainer<MemoryMethodCount> mmcContainer = new BeanItemContainer<MemoryMethodCount>(MemoryMethodCount.class);
	private List<MemoryMethod> memorymethodList; // 用来存放实体对象的集合
	private int i = 0;
	private volatile boolean isGoTo = true;

	// 构造方法
	public MemoryMethodCountManagement() {
		this.setMargin(false);
		this.setWidth("974px");
		this.setHeight("598px");

		i = 0;
		memoryMethodService = SpringContextHolder.getBean("memoryMethodService"); // 初始化Service

		VerticalLayout contextLayout = new VerticalLayout();
		contextLayout.setSizeFull();
		contextLayout.setMargin(true);
		this.addComponent(contextLayout);
		createTableLayout(contextLayout);

		memorymethodList = memoryMethodService.findAllMemoryMethods();
		startThread();
	}

	// 创建table输出组件
	private void createTableLayout(VerticalLayout contextLayout) {
		
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setWidth("100%");
//		topLayout.setMargin(false, false, true, false);
		contextLayout.addComponent(topLayout);
		btnRefresh = new Button("刷新", this);
		HorizontalLayout topRightLayout = new HorizontalLayout();
		topRightLayout.setSpacing(true);
		topRightLayout.addComponent(btnRefresh);
		topLayout.addComponent(topRightLayout);
		topLayout.setComponentAlignment(topRightLayout, Alignment.BOTTOM_LEFT);
		
		table = new Table();
		contextLayout.addComponent(table);
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setContainerDataSource(mmcContainer);
		table.setVisibleColumns(VISIBLE_COLUMNS);
		table.setColumnHeaders(COLUMN_HEADERS);
		table.setColumnExpandRatio("name", 0.2f);
		table.setColumnExpandRatio("type", 0.45f);
		table.setColumnExpandRatio("count", 0.1f);
		table.setColumnExpandRatio("sunCount", 0.25f);
		table.setPageLength(28); // 显示的条数
	}

	// 启动线程刷新数据
	private void startThread() {
		btnRefresh.setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				while (isGoTo) {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						isGoTo = false;
						logger.error("JHT -->> 内存数据大小信息模块线程错误！", e.getMessage());
					}
					try {
						createContextLayout();
					} catch (Exception e) {
						isGoTo = false;
						logger.error("JHT -->> 内存数据大小信息获取内存数据错误！", e.getMessage());
					}
				}
				btnRefresh.setEnabled(true);
			}
		}.start();
	}
	
	// 创建页面显示组件输出
	private void createContextLayout() {
		if (memorymethodList != null) {
			if (i == memorymethodList.size()) {
				isGoTo = false;
			} else {
				MemoryMethodCount mmc = getMemoryMethondCount(memorymethodList.get(i));
				mmcContainer.addBean(mmc);
				i++;
			}
		}
	}

	// 添加晓得模块
	private MemoryMethodCount getMemoryMethondCount(MemoryMethod mm) {
		MemoryMethodCount mmc = new MemoryMethodCount();
		mmc.setName(mm.getName());
		mmc.setType(mm.getType());
		int count = 0;
		int sunCount = 0;
		
		if (mmc.getName().equals("extenToUser")){
			count = ShareData.extenToUser.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userToExten")){
			count = ShareData.userToExten.size();
			sunCount = -1;
		} else if (mmc.getName().equals("extenToDomain")){
			count = ShareData.extenToDomain.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userToDomain")){
			count = ShareData.userToDomain.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userToDepartment")){
			count = ShareData.userToDepartment.size();
			sunCount = -1;
		} else if (mmc.getName().equals("extenToDynamicOutline")){
			count = ShareData.extenToDynamicOutline.size();
			sunCount = -1;
		} else if (mmc.getName().equals("extenToStaticOutline")){
			count = ShareData.extenToStaticOutline.size();
			sunCount = -1;
		} else if (mmc.getName().equals("projectToQueue")){
			count = ShareData.projectToQueue.size();
			sunCount = -1;
		} else if (mmc.getName().equals("projectToOutline")){
			count = ShareData.projectToOutline.size();
			sunCount = -1;
		} else if (mmc.getName().equals("outlineToProject")){
			count = ShareData.outlineToProject.size();
			sunCount = -1;
		} else if (mmc.getName().equals("extenToProject")){
			count = ShareData.extenToProject.size();
			sunCount = -1;
		} else if (mmc.getName().equals("domainToDefaultOutline")){
			count = ShareData.domainToDefaultOutline.size();
			sunCount = -1;
		} else if (mmc.getName().equals("domainToOutlines")){
			count = ShareData.domainToOutlines.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.domainToOutlines.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("domainToDefaultQueue")){
			count = ShareData.domainToDefaultQueue.size();
			sunCount = -1;
		} else if (mmc.getName().equals("domainToExts")){
			count = ShareData.domainToExts.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.domainToExts.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("peernameAndChannels")){
			count = ShareData.peernameAndChannels.size();
			if(count > 0){
				for(Entry<String, Set<String>> entry : ShareData.peernameAndChannels.entrySet()){
					Set<String> sunSet = entry.getValue();
					sunCount += sunSet.size();
				}
			}
		} else if (mmc.getName().equals("channelAndChannelSession")){
			count = ShareData.channelAndChannelSession.size();
			sunCount = -1;
		} else if (mmc.getName().equals("channelToChannelLifeCycle")){
			count = ShareData.channelToChannelLifeCycle.size();
			sunCount = -1;
		} else if (mmc.getName().equals("callerNumToChannelLifeCycle")){
			count = ShareData.callerNumToChannelLifeCycle.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userExtenToHoldOnCallerChannels")){
			count = ShareData.userExtenToHoldOnCallerChannels.size();
			if(count > 0){
				for(Entry<String, List<String>> entry : ShareData.userExtenToHoldOnCallerChannels.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("statusEvents")){
			count = ShareData.statusEvents.size();
			sunCount = -1;
		} else if (mmc.getName().equals("extenStatusMap")){
			count = ShareData.extenStatusMap.size();
			sunCount = -1;
		} else if (mmc.getName().equals("channelToBridgetime")){
			count = ShareData.channelToBridgetime.size();
			sunCount = -1;
		} else if (mmc.getName().equals("domainToConfigs")){
			count = ShareData.domainToConfigs.size();
			if(count > 0){
				for(Entry<Long, ConcurrentHashMap<String, Boolean>> entry : ShareData.domainToConfigs.entrySet()){
					ConcurrentHashMap<String, Boolean> sunMap = entry.getValue();
					sunCount += sunMap.size();
				}
			}
		} else if (mmc.getName().equals("queue2Members")){
			count = ShareData.queue2Members.size();
			if(count > 0){
				for(Entry<String, List<String>> entry : ShareData.queue2Members.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("queueRequestDetailMap")){
			count = ShareData.queueRequestDetailMap.size();
			sunCount = -1;
		} else if (mmc.getName().equals("queueToWaiters")){
			count = AutoDialHolder.queueToWaiters.size();
			if(count > 0){
				for(Entry<String, List<QueueEntryEvent>> entry : AutoDialHolder.queueToWaiters.entrySet()){
					List<QueueEntryEvent> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("queueToCallers")){
			count = AutoDialHolder.queueToCallers.size();
			sunCount = -1;
		} else if (mmc.getName().equals("queueToLoggedIn")){
			count = AutoDialHolder.queueToLoggedIn.size();
			sunCount = -1;
		} else if (mmc.getName().equals("queueToAvailable")){
			count = AutoDialHolder.queueToAvailable.size();
			sunCount = -1;
		} else if (mmc.getName().equals("nameToThread")){
			count = AutoDialHolder.nameToThread.size();
			sunCount = -1;
		} else if (mmc.getName().equals("domainToIncomingBlacklist")){
			count = ShareData.domainToIncomingBlacklist.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.domainToIncomingBlacklist.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("domainToOutgoingBlacklist")){
			count = ShareData.domainToOutgoingBlacklist.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.domainToOutgoingBlacklist.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("outlineToIncomingBlacklist")){
			count = ShareData.outlineToIncomingBlacklist.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.outlineToIncomingBlacklist.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("outlineToOutgoingBlacklist")){
			count = ShareData.outlineToOutgoingBlacklist.size();
			if(count > 0){
				for(Entry<Long, List<String>> entry : ShareData.outlineToOutgoingBlacklist.entrySet()){
					List<String> sunList = entry.getValue();
					sunCount += sunList.size();
				}
			}
		} else if (mmc.getName().equals("meettingToFirstJoinMemberMap")){
			count = ShareData.meettingToFirstJoinMemberMap.size();
			sunCount = -1;
		} else if (mmc.getName().equals("meetingToMemberRecords")){
			count = ShareData.meetingToMemberRecords.size();
			if(count > 0){
				for(Entry<String, ConcurrentHashMap<String, MeettingDetailRecord>> entry : ShareData.meetingToMemberRecords.entrySet()){
					ConcurrentHashMap<String, MeettingDetailRecord> sunMap = entry.getValue();
					sunCount += sunMap.size();
				}
			}
		} else if (mmc.getName().equals("meettingRoomExtenToMgrIdMap")){
			count = ShareData.meettingRoomExtenToMgrIdMap.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userToApp")){
			count = ShareData.userToApp.size();
			sunCount = -1;
		} else if (mmc.getName().equals("userToSession")){
			count = ShareData.userToSession.size();
			sunCount = -1;
		} else if (mmc.getName().equals("csrToToolBar")){
			count = ShareData.csrToToolBar.size();
			sunCount = -1;
		}
		
		
		
		mmc.setCount(count);
		if(sunCount == -1){
			mmc.setSunCount("无");
		} else{
			mmc.setSunCount(sunCount+"");
		}
		return mmc;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnRefresh == source){
			i = 0;
			isGoTo = true;
			mmcContainer.removeAllItems();
			startThread();
		}
	}

}
