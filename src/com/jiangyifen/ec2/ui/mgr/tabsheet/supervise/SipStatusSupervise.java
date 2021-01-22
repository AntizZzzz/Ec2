package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.SipStatusEntity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 *	分机状态实时监控界面 
 * @author jrh
 */

@SuppressWarnings("serial")
public class SipStatusSupervise extends VerticalLayout {

	// 表格中分机状态的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"interfaze", "usingOutline","outlineSource","registerStatus", "callStatus", "registedIp", "port", "loginStatus"};

	private final String[] COL_HEADERS = new String[] {SipStatusEntity.INTERFAZE_HEADER,SipStatusEntity.USINGOUTLINE_HEADER,SipStatusEntity.OUTLINESOURCE_HEADER,SipStatusEntity.REGISTERSTATUS_HEADER, SipStatusEntity.CALLSTATUS_HEADER, 
			SipStatusEntity.REGISTEDIP_HEADER, SipStatusEntity.PORT_HEADER, SipStatusEntity.LOGINSTATUS_HEADER};
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Table extenStatusTable; 			// 职工状态信息显示表格
	
	private Domain domain;						// 当前用户所属域
	private BeanItemContainer<SipStatusEntity> sipStatusContainer;  
	
	private volatile boolean isGotoRun = true;			// 是否允许刷新表格中的内容
	private volatile boolean hasCreatedThread = false;	// 否创建一个新的线程
	private SipConfigService sipConfigService;				// 分机服务类
	
	public SipStatusSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		
		sipStatusContainer = new BeanItemContainer<SipStatusEntity>(SipStatusEntity.class);
		
		// 创建表格组件
		createExtenStatusTable();
	}
	
	/**
	 *  创建表格组件
	 */
	private void createExtenStatusTable() {
		extenStatusTable = new Table();
		extenStatusTable.setSizeFull();
		extenStatusTable.setImmediate(true);
		extenStatusTable.setSelectable(true);
		extenStatusTable.setStyleName("striped");
		extenStatusTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(extenStatusTable);
		this.setExpandRatio(extenStatusTable, 1.0f);
		
		extenStatusTable.setContainerDataSource(sipStatusContainer);
		extenStatusTable.setVisibleColumns(VISIBLE_PROPERTIES);
		extenStatusTable.setColumnHeaders(COL_HEADERS);
		extenStatusTable.setErrorHandler(new ComponentErrorHandler() {
			@Override
			public boolean handleComponentError(ComponentErrorEvent event) {
				return true;
			}
		});

	}

	/**
	 * 更新表格的数据源
	 */
	private void updateTableDataSource() {
		
		// 清空表格中的内容
		synchronized (sipStatusContainer) {
			sipStatusContainer.removeAllItems();
		}
		
		// 根据所有的分机对象，创建新的表格显示对象
		List<SipStatusEntity> sipStatusEntities = new ArrayList<SipStatusEntity>();
		for(SipConfig exten : sipConfigService.getAllExtsByDomain(domain)) {
			SipStatusEntity sipStatusEntity = new SipStatusEntity();
			sipStatusEntity.setInterfaze(exten.getName());
			sipStatusEntity.setUsingOutline(getOutLine(domain.getId(),exten.getName()).get(0));
			sipStatusEntity.setOutlineSource(getOutLine(domain.getId(),exten.getName()).get(1));
			sipStatusEntity.setPort(exten.getPort());
			sipStatusEntities.add(sipStatusEntity);
		}
		
		// 根据所有的外线对象，创建新的表格显示对象
		for(SipConfig outline : sipConfigService.getAllOutlinesByDomain(domain)) {
			SipStatusEntity sipStatusEntity = new SipStatusEntity();
			sipStatusEntity.setInterfaze(outline.getName());
			sipStatusEntity.setUsingOutline("外线");
			sipStatusEntity.setOutlineSource("无");
			sipStatusEntity.setPort(outline.getPort());
			sipStatusEntities.add(sipStatusEntity);
		}
		
		// 添加显示对象
		synchronized (sipStatusContainer) {
			sipStatusContainer.addAll(sipStatusEntities);
		}
		
		// 排序
		synchronized (sipStatusContainer) {
			sipStatusContainer.sort(new Object[]{"name"}, new boolean[]{true});
		}
	}

	//chb 取得外线
	private ArrayList<String> getOutLine(Long domainId,String callerId){
		ArrayList<String> outlineList=new ArrayList<String>();
		
		//选择外线
		String outline = ShareData.extenToStaticOutline.get(callerId);
		String outlineStatus="成员外线";
		if(outline==null){
			// 选出指定域中的默认外线
			outline = ShareData.extenToDynamicOutline.get(callerId);
			outlineStatus="项目外线";
			if(outline==null){
				outline = ShareData.domainToDefaultOutline.get(domainId);
				outlineStatus="默认外线";
				if(outline==null){
					outline = "000000";
					outlineStatus="无外线";
				}
			}
		}
		outlineList.add(outline);
		outlineList.add(outlineStatus);
		
		return outlineList;
	}
	
	/**
	 * 更新职员监工界面中的表格信息，已经重新获取部门选择项的内容
	 * 	当职工监控界面Tab页被选中时执行（即TabSheet selectedTabChange发生时调用）
	 */
	public void update() {
		// 更新部门选择框中的信息
		updateTableDataSource();
		
		if(!hasCreatedThread || !isGotoRun) {	// 第一次点击的时候是否创建
			// 每次调用刷新方法时，先将设置可刷新状态
			isGotoRun = true;
			
			// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
			new Thread("分机监控线程"+System.currentTimeMillis()) {
				@Override
				public void run() {
					
					while(isGotoRun) {
						try {
							List<SipStatusEntity> sipStatusEntities = new ArrayList<SipStatusEntity>();
							synchronized (sipStatusContainer) {
								sipStatusEntities.addAll(sipStatusContainer.getItemIds());
							}
							
							for(SipStatusEntity sipStatusEntity : sipStatusEntities) {
								String interfaze = sipStatusEntity.getInterfaze();
								
								//分机对应的外线
								// if(interfaze.length()==6&&interfaze.startsWith("8")){
								if(interfaze.length()==5&&interfaze.startsWith("6")){	// 武睿定制, 分机号为 6 开头五位数
									sipStatusEntity.setUsingOutline(getOutLine(domain.getId(),interfaze).get(0));
									sipStatusEntity.setOutlineSource(getOutLine(domain.getId(),interfaze).get(1));
								}
								
								Long userId = ShareData.extenToUser.get(interfaze);
								if(userId != null) {
									sipStatusEntity.setLoginStatus("被使用");
								} else {
									sipStatusEntity.setLoginStatus("");
								}
								
								ExtenStatus extenStatus = ShareData.extenStatusMap.get(interfaze);
								if(extenStatus != null) {
									sipStatusEntity.setRegisterStatus(extenStatus.getRegisterStatus());
									sipStatusEntity.setRegistedIp(extenStatus.getIp());
									sipStatusEntity.setPort(extenStatus.getPort()+"");
								} else {
									sipStatusEntity.setRegisterStatus("UNKNOWN");
									sipStatusEntity.setRegistedIp("Unspecified");
									sipStatusEntity.setPort("0");
								}
								
								Set<String> channels = ShareData.peernameAndChannels.get(interfaze);
								if(channels != null && channels.size() > 0) {
									sipStatusEntity.setCallStatus("通话中");
								} else {
									sipStatusEntity.setCallStatus("空闲");
								}
							}
							
							// 刷新表格的缓存资源
							synchronized (extenStatusTable) {
								extenStatusTable.refreshRowCache();
							}
						} catch (Exception e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage() +"更新职工状态监控界面时，线程更新表格信息出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新职工状态监控界面时，线程更新表格信息出现异常！");*/
						}
						try {
							Thread.sleep(60000); // 更新成功还是失败，都让线程睡眠3秒
						} catch (InterruptedException e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage()+"更新职工状态监控界面时，线程休眠出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新职工状态监控界面时，线程休眠出现异常！");*/
						}
					}
				}
			}.start();
		
			hasCreatedThread = true;
		}
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
