package com.jiangyifen.ec2.ui.admin.tab;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.asteriskjava.manager.event.QueueEntryEvent;
import org.asteriskjava.manager.event.StatusEvent;

import com.google.gson.Gson;
import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.BridgetimeInfo;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.MeettingRoomFirstJoinMemberInfo;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.MemoryMethod;
import com.jiangyifen.ec2.entity.QueueRequestDetail;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MemoryMethodService;
import com.jiangyifen.ec2.ui.admin.tab.memory.SelectMemoryMethodWidnow;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.MemoryParameter;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * 获取内存中的方法所存储的内容,并显示
 * 
 * @author JHT
 * 
 * @date 2014-10-16 上午9:29:18
 *
 */
public class MemoryMethodDataVisiableManagement extends VerticalLayout implements Button.ClickListener, Property.ValueChangeListener{
	
	private static final long serialVersionUID = 1081616226069706774L;
	private Gson gson =  new Gson();

	// 主要组件
	private Button btnMethodName;						// 选择方法按钮
	private TextField txtMethodName;					// 存放内存方法名字
	private ComboBox cmbDataType;						// 存放数据的显示方式
	private TextField txtKeyword;						// 存放方法参数的查询
	private Button btnSearch;							// 查询按钮
	
	private Label lbMsg;								// 用来显示点击数据的提示信息
	private TextArea txtAreaDataMemoryLeft;				// 显示查询出来的内存数据左侧
	private TextArea txtAreaDataMemoryRight;			// 显示查询出来的内存数据右侧
	
	// 接收选择查询条件的变量
	private MemoryMethod memoryMethod;					// 接收选中方法信息
	
	private MemoryParameter memoryParameter;			// 用来存放输入的选择条件后的信息 
	private MemoryMethodService memoryMethodService;	// 用来查询参数信息的Service
	
	// 构造方法
	public MemoryMethodDataVisiableManagement() {
		this.setSizeFull();
		this.setMargin(true,false,false,false);
		
		memoryMethodService = SpringContextHolder.getBean("memoryMethodService");	// 初始化Service
		memoryParameter = new MemoryParameter();
		this.addComponent(buildSearchLayout());										// 添加页面显示组件
		this.addComponent(buildShowPromptLayout());
		this.addComponent(buildComponentLayout());
	}
	
	// 创建搜索输出
	private HorizontalLayout buildSearchLayout(){
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		
		// 使label和选择框组合在一起
		HorizontalLayout constrantLayout1 = new HorizontalLayout();
		constrantLayout1.setSpacing(true);
		/*constrantLayout1.addComponent(new Label("内存方法："));*/
		btnMethodName = new Button("选择", this);
		txtMethodName = new TextField();
		txtMethodName.setWidth("250px");
		txtMethodName.setEnabled(false);
		txtMethodName.setInputPrompt("点击选择按钮选择方法名称");
		//constrantLayout1.addComponent(new Label("",Label.CONTENT_XHTML));
		constrantLayout1.addComponent(btnMethodName);
		constrantLayout1.addComponent(txtMethodName);
		
		searchLayout.addComponent(constrantLayout1);

		HorizontalLayout constrantLayout2 = new HorizontalLayout();
		constrantLayout2.addComponent(new Label("方式："));
		cmbDataType = new ComboBox();
		cmbDataType.setTextInputAllowed(false);
		cmbDataType.setNullSelectionAllowed(false);
		cmbDataType.setImmediate(true);
		cmbDataType.addContainerProperty("pm", String.class, null);
		cmbDataType.setItemCaptionPropertyId("pm");
		Item item1 = cmbDataType.addItem("all");
		item1.getItemProperty("pm").setValue("全部");
		Item item2  = cmbDataType.addItem("single");
		item2.getItemProperty("pm").setValue("单个");
		cmbDataType.setValue("all");
		cmbDataType.addListener(this);
		constrantLayout2.addComponent(cmbDataType);
		searchLayout.addComponent(constrantLayout2);
		
		HorizontalLayout constrantLayout3 = new HorizontalLayout();
		constrantLayout3.addComponent(new Label("参数："));
		txtKeyword = new TextField();
		txtKeyword.setInputPrompt("请输入参数");
		constrantLayout3.addComponent(txtKeyword);
		searchLayout.addComponent(constrantLayout3);
		
		btnSearch = new Button("查询", this);
		btnSearch.setStyleName("chb");
		searchLayout.addComponent(btnSearch);
		
		return searchLayout;
	}
	
	// 创建提示信息输出
	private HorizontalLayout buildShowPromptLayout(){
		HorizontalLayout promptLayout = new HorizontalLayout();
		promptLayout.setWidth("100%");
		lbMsg = new Label("&nbsp;", Label.CONTENT_XHTML);
		lbMsg.setSizeFull();
		promptLayout.addComponent(lbMsg);
		return promptLayout;
	}
	
	// 中间区域布局
	private HorizontalLayout buildComponentLayout(){
		HorizontalLayout middleLayout = new HorizontalLayout();
		middleLayout.setWidth("100%");
		middleLayout.setHeight("540px");
		
		HorizontalSplitPanel panelSplit = new HorizontalSplitPanel();
		panelSplit.setSizeFull();
		panelSplit.setImmediate(true);
		panelSplit.addStyleName(Reindeer.SPLITPANEL_SMALL);
		
		txtAreaDataMemoryLeft = new TextArea();
		txtAreaDataMemoryLeft.setSizeFull();
		txtAreaDataMemoryRight = new TextArea();
		txtAreaDataMemoryRight.setWordwrap(false);						// 设置为不自动换行
		txtAreaDataMemoryRight.setSizeFull();
		
		panelSplit.setFirstComponent(txtAreaDataMemoryLeft);
		panelSplit.setSecondComponent(txtAreaDataMemoryRight);
		panelSplit.setSplitPosition(15);								// 设置分割百分比
		//panelSplit.setMaxSplitPosition(40, UNITS_PERCENTAGE);			// 设置分割拖动最大百分比
		panelSplit.setMinSplitPosition(15, UNITS_PERCENTAGE);			// 设置分割拖动最小百分比
		middleLayout.addComponent(panelSplit);
		return middleLayout;
	}
	
	/**
	 * 由外部调用，用来选中内存方法
	 * @param memoryParameter
	 */
	public void setMemoryMethodParameter(MemoryParameter memoryParameter){
		this.memoryParameter = memoryParameter;
		if(memoryParameter.getMemoryMethod() != null){
			memoryMethod = memoryParameter.getMemoryMethod();
			txtMethodName.setValue(memoryParameter.getMemoryMethod().getName());
			lbMsg.setValue("<font color=#0000ff>选择方法："+memoryParameter.getMemoryMethod().getDescription()+"</font>");
		}
	}
	
	// 按钮的点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnSearch){
			txtAreaDataMemoryLeft.setValue("");	// 每次点击查询按钮时，清空文本域里面的内容，以起到刷新的作用
			txtAreaDataMemoryRight.setValue("");
			String dataModeString = cmbDataType.getValue() == null ? "all" : cmbDataType.getValue().toString();
			String methodKeywordString = txtKeyword.getValue() == null ? "" : txtKeyword.getValue().toString();
			if(memoryMethod == null){	//如果内存方法选择框没有选中数据，则给予提示
				lbMsg.setValue("<font color=red>请选择一个内存方法</font>");
				btnMethodName.focus();
				return;
			}
			if(dataModeString.equals("single") && methodKeywordString.equals("")){	// 如果选择了单个显示方式，并且输入参数文本框为空，则给予提示
				if(memoryMethod.getName().equals("statusEvents")){
					lbMsg.setValue("<font color=red>特殊方法，不支持单个查询</font>");
					/*cmbDataType.setValue("all");*/
					return;
				} else{
					lbMsg.setValue("<font color=red>您选择的是单个显示方式，所以参数不能为空</font>");
					txtKeyword.focus();
					return;
				}
			}
			if(dataModeString.equals("all")){
				txtKeyword.setValue("");
			}
			memoryParameter.setShowType(dataModeString);
			memoryParameter.setParameter(methodKeywordString);
			
			builderShowMemory(memoryParameter);
		} else if(source == btnMethodName){
			cmbDataType.setValue("all");
			txtKeyword.setValue("");
			this.getApplication().getMainWindow().addWindow(new SelectMemoryMethodWidnow(this));
		}
	}

	
	// 构造方法执行完之后执行此方法
	@Override
	public void attach() {
		/*txtKeyword.addListener(new FieldEvents.FocusListener() {	// 为输入参数文本框添加一个获取焦点事件
			private static final long serialVersionUID = 1858602172099955343L;
			@Override
			public void focus(FocusEvent event) {
				if(memoryMethod != null){
					if(lbMsg.getValue().toString().equals("<font color=red>您选择的是单个显示方式，所以参数不能为空</font>")){
						lbMsg.setValue("<font color=red>您选择的是单个显示方式，所以参数不能为空。例如："+memoryMethod.getSimpleDemo()+"</font>");
					} else{
						if(cmbDataType.getValue().toString().equals("single")){
							lbMsg.setValue("<font color=#0000ff>例如："+memoryMethod.getSimpleDemo()+"</font>");
						} else{
							lbMsg.setValue("<font color=green>您当前选择的显示方式为全部显示，请选择单个显示后在输入参数</font>");
						}
					}
				}
			}
		});*/
	}

	// 修改下拉框选项内容事件
	@Override
	public void valueChange(ValueChangeEvent event) {
		ComboBox cmbSource = (ComboBox) event.getProperty();
		if(cmbDataType == cmbSource){
			try {
				if(event.getProperty().getValue() != null && event.getProperty().getValue().toString().equals("single")){
					if(txtMethodName.getValue() != null){
						if(memoryMethod.getName().equals("statusEvents")){
							lbMsg.setValue("<font color=red>特殊方法，不支持单个查询</font>");
						} else{
							lbMsg.setValue("<font color=#0000ff>单选 ："+memoryMethod.getSimpleDemo()+"  类型："+memoryMethod.getParameter()+"</font>");
						}
					} else{
						//lbMsg.setValue("<font color=#0000ff>显示方式：显示单个的数据，请根据输入参数的提示输入内容");
						lbMsg.setValue("<font color=red>请选择一个要查询的内存方法，然后再选择单个显示方式</font>");
					}
				} else if(event.getProperty().getValue() != null && event.getProperty().getValue().toString().equals("all")){
					lbMsg.setValue("&nbsp;");
				}
			} catch (Exception e) {
				lbMsg.setValue("<font color=red>请选择一个要查询的内存方法，然后再选择单个显示方式</font>");
			}
		} /*else if(btnMethodName == cmbSource){
			memoryMethod = (MemoryMethod)btnMethodName.getValue();
			if(memoryMethod != null){
				lbMsg.setValue("<font color=#0000ff>选择方法："+memoryMethod.getDescription()+"</font>");
			}
		}*/
	}


	//=====================================================================//
	//------------------ 用来显示内存数据的信息  ----------------------------//
	//=====================================================================//
	
	// 显示内存数据的主方法
	private void builderShowMemory(MemoryParameter parameter) {			
		if(parameter != null && parameter.getMemoryMethod() != null){														// 如果这个对象不是空的,则进行执行下面的操作
			lbMsg.setValue("&nbsp;");
			/**********************************************基础信息类************************************************/
			if("extenToUser".equals(parameter.getMemoryMethod().getName())){											// 根据分机号进行查询分机对应的用户信息
				showExtenToUserView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("userToExten".equals(parameter.getMemoryMethod().getName())){									// 根据用户编号查询出用户所对应的分机
				showUserToExtenView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("extenToDomain".equals(parameter.getMemoryMethod().getName())){									// 根据分机号查询外线与域的对应关系
				showExtenToDomainView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("userToDomain".equals(parameter.getMemoryMethod().getName())){									// 根据用户编号查询用户对应的域
				showUserToDomainView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("userToDepartment".equals(parameter.getMemoryMethod().getName())){								// 根据用户编号进行查询用户与部门的对应关系
				showUserToDepartmentView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("extenToDynamicOutline".equals(parameter.getMemoryMethod().getName())){							// 根据分机号进行查询外线与动态队列成员的对应关系
				showExtenToDynamicOutlineView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("extenToStaticOutline".equals(parameter.getMemoryMethod().getName())){							// 根据分机号进行查询外线与静态队列成员的对应关系
				showExtenToStaticOutlineView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("projectToQueue".equals(parameter.getMemoryMethod().getName())){									// 根据项目编号进行查询项目与队列的对应关系
				showProjectToQueueView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("projectToOutline".equals(parameter.getMemoryMethod().getName())){								// 根据项目编号进行查询项目所对应的外线
				showProjectToOutlineView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("outlineToProject".equals(parameter.getMemoryMethod().getName())){								// 根据外线编号进行查询外线所对应的项目
				showOutlineToProjectView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("extenToProject".equals(parameter.getMemoryMethod().getName())){									// 根据分机号进行查询分机所对应的项目
				showExtenToProjectView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToDefaultOutline".equals(parameter.getMemoryMethod().getName())){							// 根据域查询域所对应的默认外线
				showDomainToDefaultOutlineView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToOutlines".equals(parameter.getMemoryMethod().getName())){								// 根据域查询域所对应的所有外线
				showDomainToOutlinesView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToDefaultQueue".equals(parameter.getMemoryMethod().getName())){							// 根据域查询域所对应的默认队列
				showDomainToDefaultQueueView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToExts".equals(parameter.getMemoryMethod().getName())){									// 根据域查询域所对应的所有分机
				showDomainToExtsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else 
			/**********************************************通话信息类************************************************/	
			if("peernameAndChannels".equals(parameter.getMemoryMethod().getName())){									// 分机或外线与其相关通道的对应关系
				showPeernameAndChannelsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("channelAndChannelSession".equals(parameter.getMemoryMethod().getName())){						// 通道与ChannelSession的对应关系
				showChannelAndChannelSessionView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("channelToChannelLifeCycle".equals(parameter.getMemoryMethod().getName())){						// 根据通道查询通道所对应的通道的周期
				showChannelToChannelLifeCycleView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("callerNumToChannelLifeCycle".equals(parameter.getMemoryMethod().getName())){					// 根据呼入者的电话查询呼入者的电话所对应的通道的周期
				showCallerNumToChannelLifeCycleView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("userExtenToHoldOnCallerChannels".equals(parameter.getMemoryMethod().getName())){				// 根据分机编号查询分机所对应的呼叫保持中的电话通道
				showUserExtenToHoldOnCallerChannelsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else
			/**********************************************实时监控类************************************************/
			if("statusEvents".equals(parameter.getMemoryMethod().getName())){											// Asterisk-Sippeer状态事件的信息
				showStatusEventsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("extenStatusMap".equals(parameter.getMemoryMethod().getName())){									// 分机与分机状态的对应关系
				showExtenStatusMapView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("channelToBridgetime".equals(parameter.getMemoryMethod().getName())){							// 通道与接通情况信息的对应关系
				showChannelToBridgetimeView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToConfigs".equals(parameter.getMemoryMethod().getName())){								// 域跟配置信息的对应关系
				showDomainToConfigsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("queue2Members".equals(parameter.getMemoryMethod().getName())){									// 队列与队列成员的对应关系
				showQueue2MembersView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("queueRequestDetailMap".equals(parameter.getMemoryMethod().getName())){							// 电话的UniqueId跟队列电话详单的对应关系
				showQueueRequestDetailMapView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else
			/********************************************自动外呼数据类**********************************************/
			if("queueToWaiters".equals(parameter.getMemoryMethod().getName())){										// 队列中的排队人信息
				showQueueToWaitersView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("queueToCallers".equals(parameter.getMemoryMethod().getName())){									// 队列中跟队列的通话数量的对应关系
				showQueueToCallersView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("queueToLoggedIn".equals(parameter.getMemoryMethod().getName())){								// 队列中跟在线分机成员总数的对应关系
				showQueueToLoggedInView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("queueToAvailable".equals(parameter.getMemoryMethod().getName())){								// 队列中跟可用成员总数的对应关系
				showQueueToAvailableView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("nameToThread".equals(parameter.getMemoryMethod().getName())){									// 线程的名字跟线程的对应关系
				showNameToThreadView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else
			/**********************************************黑名单类**************************************************/
			if("domainToIncomingBlacklist".equals(parameter.getMemoryMethod().getName())){								// 各域跟呼入方向的黑名单的对应关系
				showDomainToIncomingBlacklistView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("domainToOutgoingBlacklist".equals(parameter.getMemoryMethod().getName())){						// 各域跟呼出方向的黑名单的对应关系
				showDomainToOutgoingBlacklistView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("outlineToIncomingBlacklist".equals(parameter.getMemoryMethod().getName())){						// 外线与呼入方向的黑名单的对应关系
				showOutlineToIncomingBlacklistView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("outlineToOutgoingBlacklist".equals(parameter.getMemoryMethod().getName())){						// 外线与呼出方向的黑名单的对应关系
				showOutlineToOutgoingBlacklistView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else
			/*******************************************会议室相关信息类*********************************************/
			if("meettingToFirstJoinMemberMap".equals(parameter.getMemoryMethod().getName())){							// 会议室号跟第一个进入会议室的成员通道信息的对应关系
				showMeettingToFirstJoinMemberMapView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("meetingToMemberRecords".equals(parameter.getMemoryMethod().getName())){							// Map<会议室号，ConcurrentHashMap<会议室成员的通道, 成员详情记录信息>>
				showMeetingToMemberRecordsView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("meettingRoomExtenToMgrIdMap".equals(parameter.getMemoryMethod().getName())){					// 管理员发起的会议室Map<分机号，管理员编号>
				showMeettingRoomExtenToMgrIdMapView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else
			/**********************************************其他信息类************************************************/
			if("userToApp".equals(parameter.getMemoryMethod().getName())){												// 登录界面的用户与Application的对应关系
				showUserToAppView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("userToSession".equals(parameter.getMemoryMethod().getName())){									// 登录界面的用户与Session的对应关系
				showUserToSessionView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			} else if("csrToToolBar".equals(parameter.getMemoryMethod().getName())){									// 登录界面的用户与坐席界面工具栏的对应关系
				showCsrToToolBarView(parameter.getShowType(), parameter.getParameter(), parameter.getMemoryMethod());
			}
			
			if(!"statusEvents".equals(parameter.getMemoryMethod().getName())){
				resetSearchAfter(parameter);
			}
			
		}
	}
	
	//===================================基础信息类===================================//
	// 根据分机号进行查询分机对应的用户信息
	private void showExtenToUserView(String showType, String keyWord, MemoryMethod memory){				
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Long> entry : ShareData.extenToUser.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.extenToUser != null && ShareData.extenToUser.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenToUser.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord!= null){
				Long userLong = ShareData.extenToUser.get(keyWord);
				if(userLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+userLong);
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据用户编号查询出用户所对应的分机
	private void showUserToExtenView(String showType, String keyWord, MemoryMethod memory){
		try {
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, String> entry : ShareData.userToExten.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.userToExten != null && ShareData.userToExten.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userToExten.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord!= null){
				Long userId = memoryMethodService.findUserIdByEmpnoOrRealname(keyWord);
				String extenString = ShareData.userToExten.get(userId);
				if (extenString != null) {
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+extenString);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据分机号查询外线与域的对应关系
	private void showExtenToDomainView(String showType, String keyWord, MemoryMethod memory) {
		try {
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Long> entry : ShareData.extenToDomain.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.extenToDomain != null && ShareData.extenToDomain.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenToDomain.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord!= null){
				Long domainLong = ShareData.extenToDomain.get(keyWord);
				if(domainLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+domainLong);
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据用户编号查询用户对应的域
	private void showUserToDomainView(String showType, String keyWord, MemoryMethod memory) {
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, Long> entry : ShareData.userToDomain.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.userToDomain != null && ShareData.userToDomain.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userToDomain.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord!= null){
				Long userId = memoryMethodService.findUserIdByEmpnoOrRealname(keyWord);
				Long domainLong = ShareData.userToDomain.get(userId);
				if(domainLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+domainLong);
				}
			}
			
		} catch (Exception e) {		}
	}

	// 根据用户编号进行查询用户与部门的对应关系
	private void showUserToDepartmentView(String showType, String keyWord, MemoryMethod memory) {
		try {
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, Long> entry : ShareData.userToDepartment.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.userToDepartment != null && ShareData.userToDepartment.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userToDepartment.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Long userId = memoryMethodService.findUserIdByEmpnoOrRealname(keyWord);
				Long departmentLong = ShareData.userToDepartment.get(userId);
				if(departmentLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+departmentLong);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据分机号进行查询外线与动态队列成员的对应关系
	private void showExtenToDynamicOutlineView(String showType, String keyWord, MemoryMethod memory) {
		try {
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, String> entry : ShareData.extenToDynamicOutline.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.extenToDynamicOutline != null && ShareData.extenToDynamicOutline.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenToDynamicOutline.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String outlineString = ShareData.extenToDynamicOutline.get(keyWord);
				if(outlineString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+outlineString);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据分机号进行查询外线与静态队列成员的对应关系
	private void showExtenToStaticOutlineView(String showType, String keyWord, MemoryMethod memory) {
		try {
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, String> entry : ShareData.extenToStaticOutline.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.extenToStaticOutline != null && ShareData.extenToStaticOutline.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenToStaticOutline.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String outlineString = ShareData.extenToStaticOutline.get(keyWord);
				if(outlineString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+outlineString);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据项目编号进行查询项目与队列的对应关系
	private void showProjectToQueueView(String  showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, String> entry : ShareData.projectToQueue.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.projectToQueue != null && ShareData.projectToQueue.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.projectToQueue.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String queueString = ShareData.projectToQueue.get(keyWord);
				if(queueString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+queueString);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据项目编号进行查询项目所对应的外线
	private void showProjectToOutlineView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, String> entry : ShareData.projectToOutline.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());		
				if(ShareData.projectToOutline != null && ShareData.projectToOutline.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.projectToOutline.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String outlineString = ShareData.projectToOutline.get(keyWord);
				if(outlineString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+outlineString);	
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据外线编号进行查询外线所对应的项目
	private void showOutlineToProjectView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Long> entry : ShareData.outlineToProject.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.outlineToProject != null && ShareData.outlineToProject.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.outlineToProject.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Long projectLong = ShareData.outlineToProject.get(keyWord);
				if(projectLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+projectLong);	
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据分机号进行查询分机所对应的项目
	private void showExtenToProjectView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Long> entry : ShareData.extenToProject.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.extenToProject != null && ShareData.extenToProject.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenToProject.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Long projectLong = ShareData.extenToProject.get(keyWord);
				if(projectLong != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+projectLong);	
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据域查询域所对应的默认外线
	private void showDomainToDefaultOutlineView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, String> entry : ShareData.domainToDefaultOutline.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.domainToDefaultOutline != null && ShareData.domainToDefaultOutline.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToDefaultOutline.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String defaultOutlineString = ShareData.domainToDefaultOutline.get(Long.parseLong(keyWord));
				if(defaultOutlineString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+defaultOutlineString);	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 根据域查询域所对应的所有外线
	private void showDomainToOutlinesView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.domainToOutlines.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToOutlines != null && ShareData.domainToOutlines.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToOutlines.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> outlineList = ShareData.domainToOutlines.get(Long.parseLong(keyWord));
				if(outlineList != null && outlineList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+outlineList);	
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 根据域查询域所对应的默认队列
	private void showDomainToDefaultQueueView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, String> entry : ShareData.domainToDefaultQueue.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToDefaultQueue != null && ShareData.domainToDefaultQueue.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToDefaultQueue.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				String defaultQueueString = ShareData.domainToDefaultQueue.get(Long.parseLong(keyWord));
				if(defaultQueueString != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+defaultQueueString);	
				}
			}
			
		} catch (Exception e) {	}
	}
	
	// 根据域查询域所对应的所有分机
	private void showDomainToExtsView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.domainToExts.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToExts != null && ShareData.domainToExts.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToExts.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> outlineList = ShareData.domainToExts.get(Long.parseLong(keyWord));
				if(outlineList != null && outlineList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+outlineList);
				}
			}
			
		} catch (Exception e) {		}
	}
	
	//===================================通话信息类===================================//
	// 分机或外线与其相关通道的对应关系 gson
	private void showPeernameAndChannelsView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Set<String>> entry : ShareData.peernameAndChannels.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.peernameAndChannels != null && ShareData.peernameAndChannels.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.peernameAndChannels.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Set<String> outlineSet = ShareData.peernameAndChannels.get(Long.parseLong(keyWord));
				if(outlineSet != null && outlineSet.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(outlineSet.toString()));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 通道与ChannelSession的对应关系 gson
	private void showChannelAndChannelSessionView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, ChannelSession> entry : ShareData.channelAndChannelSession.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.channelAndChannelSession != null && ShareData.channelAndChannelSession.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.channelAndChannelSession.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ChannelSession channelSession = ShareData.channelAndChannelSession.get(Long.parseLong(keyWord));
				if(channelSession != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(channelSession));	
				}
			}
			
		} catch (Exception e) {		}
	}

	// 根据通道查询通道所对应的通道的周期 gson
	private void showChannelToChannelLifeCycleView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, ChannelLifeCycle> entry : ShareData.channelToChannelLifeCycle.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.channelToChannelLifeCycle != null && ShareData.channelToChannelLifeCycle.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.channelToChannelLifeCycle.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycle.get(Long.parseLong(keyWord));
				if(channelLifeCycle != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(channelLifeCycle));
				}
			}

		} catch (Exception e) {		}
	}

	// 根据呼入者的电话查询呼入者的电话所对应的通道的周期 gson
	private void showCallerNumToChannelLifeCycleView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, ChannelLifeCycle> entry : ShareData.callerNumToChannelLifeCycle.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.callerNumToChannelLifeCycle != null && ShareData.callerNumToChannelLifeCycle.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.callerNumToChannelLifeCycle.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ChannelLifeCycle channelLifeCycle = ShareData.callerNumToChannelLifeCycle.get(Long.parseLong(keyWord));
				if(channelLifeCycle != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(channelLifeCycle));
				}
			}

		} catch (Exception e) {		}
	}

	// 根据分机编号查询分机所对应的呼叫保持中的电话通道 gson
	private void showUserExtenToHoldOnCallerChannelsView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, List<String>> entry : ShareData.userExtenToHoldOnCallerChannels.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.userExtenToHoldOnCallerChannels != null && ShareData.userExtenToHoldOnCallerChannels.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userExtenToHoldOnCallerChannels.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> holdList = ShareData.userExtenToHoldOnCallerChannels.get(Long.parseLong(keyWord));
				if(holdList != null && holdList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(holdList));	
				}
			}

		} catch (Exception e) {		}
	}

	//===================================实时监控类===================================//
	// Asterisk-Sippeer状态事件的信息 gson
	private void showStatusEventsView(String showType, String keyWord, MemoryMethod memory){
		try{
			List<StatusEvent> statusList = ShareData.statusEvents;
			if(statusList != null && statusList.size() > 0){
				txtAreaDataMemoryLeft.setValue("");
				txtAreaDataMemoryRight.setValue(memory.getKeyPrompt()+"\n"+gson.toJson(statusList));
			} else{
				txtAreaDataMemoryLeft.setValue("");
				txtAreaDataMemoryRight.setValue("");
				lbMsg.setValue("<font color=red>查询结果 ：没有查询到相对应的数据！</font>");
			}
		} catch (Exception e) {		}
	}
	
	// 分机与分机状态的对应关系 gson
	private void showExtenStatusMapView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, ExtenStatus> entry : ShareData.extenStatusMap.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.extenStatusMap != null && ShareData.extenStatusMap.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.extenStatusMap.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ExtenStatus extenStatus = ShareData.extenStatusMap.get(keyWord);
				if(extenStatus != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(extenStatus));
				}
			}

		} catch (Exception e) {		}
	}
	
	// 通道与接通情况信息的对应关系  gson
	private void showChannelToBridgetimeView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, BridgetimeInfo> entry : ShareData.channelToBridgetime.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.channelToBridgetime != null && ShareData.channelToBridgetime.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.channelToBridgetime.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				BridgetimeInfo bridgetimeInfo = ShareData.channelToBridgetime.get(keyWord);
				if(bridgetimeInfo != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(bridgetimeInfo));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// TODO 域跟配置信息的对应关系   Map类型数据等待修改
	private void showDomainToConfigsView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, ConcurrentHashMap<String, Boolean>> entry : ShareData.domainToConfigs.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToConfigs != null && ShareData.domainToConfigs.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToConfigs.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ConcurrentHashMap<String, Boolean> configsMap = ShareData.domainToConfigs.get(keyWord);
				if(configsMap != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(configsMap));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 队列与队列成员的对应关系 gson
	private void showQueue2MembersView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, List<String>> entry : ShareData.queue2Members.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.queue2Members != null && ShareData.queue2Members.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.queue2Members.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> memberList = ShareData.queue2Members.get(keyWord);
				if(memberList != null && memberList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(memberList));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 电话的UniqueId跟队列电话详单的对应关系 gson
	private void showQueueRequestDetailMapView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, QueueRequestDetail> entry : ShareData.queueRequestDetailMap.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.queueRequestDetailMap != null && ShareData.queueRequestDetailMap.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.queueRequestDetailMap.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				QueueRequestDetail queueRequestDetail = ShareData.queueRequestDetailMap.get(keyWord);
				if(queueRequestDetail != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(queueRequestDetail));	
				}
			}

		} catch (Exception e) {		}
	}
	
	//===================================自动外呼数据类===================================//
	// 队列中的排队人信息 gson
	private void showQueueToWaitersView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, List<QueueEntryEvent>> entry : AutoDialHolder.queueToWaiters.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(AutoDialHolder.queueToWaiters != null && AutoDialHolder.queueToWaiters.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+AutoDialHolder.queueToWaiters.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<QueueEntryEvent> queueList = AutoDialHolder.queueToWaiters.get(keyWord);
				if(queueList != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(queueList));
				}
			}

		} catch (Exception e) {		}
	}
	
	// 队列中跟队列的通话数量的对应关系
	private void showQueueToCallersView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Integer> entry : AutoDialHolder.queueToCallers.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(AutoDialHolder.queueToCallers != null && AutoDialHolder.queueToCallers.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+AutoDialHolder.queueToCallers.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Integer callers = AutoDialHolder.queueToCallers.get(keyWord);
				if(callers != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(callers));
				}
			}

		} catch (Exception e) {		}
	}
	
	// 队列中跟在线分机成员总数的对应关系
	private void showQueueToLoggedInView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Integer> entry : AutoDialHolder.queueToLoggedIn.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(AutoDialHolder.queueToLoggedIn != null && AutoDialHolder.queueToLoggedIn.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+AutoDialHolder.queueToLoggedIn.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Integer logged = AutoDialHolder.queueToLoggedIn.get(keyWord);
				if(logged != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+logged);	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 队列中跟可用成员总数的对应关系
	private void showQueueToAvailableView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Integer> entry : AutoDialHolder.queueToAvailable.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(AutoDialHolder.queueToAvailable != null && AutoDialHolder.queueToAvailable.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+AutoDialHolder.queueToAvailable.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Integer available = AutoDialHolder.queueToAvailable.get(keyWord);
				if(available != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+available);
				}
			}

		} catch (Exception e) {		}
	}
	
	// 线程的名字跟线程的对应关系 gson
	private void showNameToThreadView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Thread> entry : AutoDialHolder.nameToThread.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(AutoDialHolder.nameToThread != null && AutoDialHolder.nameToThread.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+AutoDialHolder.nameToThread.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Thread thread = AutoDialHolder.nameToThread.get(keyWord);
				if(thread != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+thread);	
				}
			}

		} catch (Exception e) {		}
	}
	
	
	//===================================黑名单类===================================//
	// 各域跟呼入方向的黑名单的对应关系 gson
	private void showDomainToIncomingBlacklistView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.domainToIncomingBlacklist.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToIncomingBlacklist != null && ShareData.domainToIncomingBlacklist.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToIncomingBlacklist.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> blackList = ShareData.domainToIncomingBlacklist.get(Long.parseLong(keyWord));
				if(blackList != null && blackList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(blackList));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 各域跟呼出方向的黑名单的对应关系 gson
	private void showDomainToOutgoingBlacklistView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.domainToOutgoingBlacklist.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.domainToOutgoingBlacklist != null && ShareData.domainToOutgoingBlacklist.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.domainToOutgoingBlacklist.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> blackList = ShareData.domainToOutgoingBlacklist.get(Long.parseLong(keyWord));
				if(blackList != null && blackList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(blackList));
				}
			}

		} catch (Exception e) {		}
	}
	
	// 外线与呼入方向的黑名单的对应关系 gson
	private void showOutlineToIncomingBlacklistView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.outlineToIncomingBlacklist.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.outlineToIncomingBlacklist != null && ShareData.outlineToIncomingBlacklist.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.outlineToIncomingBlacklist.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> blackList = ShareData.outlineToIncomingBlacklist.get(Long.parseLong(keyWord));
				if(blackList != null && blackList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(blackList));
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 外线与呼出方向的黑名单的对应关系 gson
	private void showOutlineToOutgoingBlacklistView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, List<String>> entry : ShareData.outlineToOutgoingBlacklist.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.outlineToOutgoingBlacklist != null && ShareData.outlineToOutgoingBlacklist.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.outlineToOutgoingBlacklist.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				List<String> blackList = ShareData.outlineToOutgoingBlacklist.get(Long.parseLong(keyWord));
				if(blackList != null && blackList.size() > 0){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(blackList));	
				}
			}

		} catch (Exception e) {		}
	}
	
	
	//===================================会议室相关信息类===================================//
	// 会议室号跟第一个进入会议室的成员通道信息的对应关系 gson
	private void showMeettingToFirstJoinMemberMapView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, MeettingRoomFirstJoinMemberInfo> entry : ShareData.meettingToFirstJoinMemberMap.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());
				if(ShareData.meettingToFirstJoinMemberMap != null && ShareData.meettingToFirstJoinMemberMap.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.meettingToFirstJoinMemberMap.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				MeettingRoomFirstJoinMemberInfo meettingRoomFirstJoinMemberInfo = ShareData.meettingToFirstJoinMemberMap.get(keyWord);
				if(meettingRoomFirstJoinMemberInfo != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(meettingRoomFirstJoinMemberInfo));		
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// Map<会议室号，ConcurrentHashMap<会议室成员的通道, 成员详情记录信息>> gson
	private void showMeetingToMemberRecordsView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, ConcurrentHashMap<String, MeettingDetailRecord>> entry : ShareData.meetingToMemberRecords.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.meetingToMemberRecords != null && ShareData.meetingToMemberRecords.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.meetingToMemberRecords.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				ConcurrentHashMap<String, MeettingDetailRecord> memberMap = ShareData.meetingToMemberRecords.get(keyWord);
				if(memberMap != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(memberMap));	
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 管理员发起的会议室Map<分机号，管理员编号> 
	private void showMeettingRoomExtenToMgrIdMapView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<String, Long> entry : ShareData.meettingRoomExtenToMgrIdMap.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+entry.getValue());
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.meettingRoomExtenToMgrIdMap != null && ShareData.meettingRoomExtenToMgrIdMap.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.meettingRoomExtenToMgrIdMap.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Long room = ShareData.meettingRoomExtenToMgrIdMap.get(keyWord);
				if(room != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+room);
				}
			}
			
		} catch (Exception e) {		}
	}
	
	
	//===================================其他信息类===================================//
	// 登录界面的用户与Application的对应关系 gson
	private void showUserToAppView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, Application> entry : ShareData.userToApp.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());		
				if(ShareData.userToApp != null && ShareData.userToApp.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userToApp.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				Application app = ShareData.userToApp.get(Long.parseLong(keyWord));
				if(app != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(app));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 登录界面的用户与Session的对应关系 gson
	private void showUserToSessionView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, HttpSession> entry : ShareData.userToSession.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.userToSession != null && ShareData.userToSession.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.userToSession.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				HttpSession app = ShareData.userToSession.get(Long.parseLong(keyWord));
				if(app != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(app));
				}
			}
			
		} catch (Exception e) {		}
	}
	
	// 登录界面的用户与坐席界面工具栏的对应关系 gson
	private void showCsrToToolBarView(String showType, String keyWord, MemoryMethod memory){
		try{
			if(showType.equals("all")){
				StringBuffer allLeftBuffer = new StringBuffer();
				StringBuffer allRightBuffer = new StringBuffer();
				allLeftBuffer.append(memory.getKeyPrompt());
				allRightBuffer.append(memory.getValuePrompt());
				for(Map.Entry<Long, CsrToolBar> entry : ShareData.csrToToolBar.entrySet()){
					allLeftBuffer.append("\n"+entry.getKey());
					allRightBuffer.append("\n"+gson.toJson(entry.getValue()));
				}
				txtAreaDataMemoryLeft.setValue(allLeftBuffer.toString());
				txtAreaDataMemoryRight.setValue(allRightBuffer.toString());	
				if(ShareData.csrToToolBar != null && ShareData.csrToToolBar.size() > 0){
					lbMsg.setValue("<font color='#0066FF'>查询结果："+ShareData.csrToToolBar.size()+"  条数据</font>");
				}
			} else if(showType.equals("single") && keyWord != null){
				CsrToolBar csrToolBar = null;
				if(ShareData.csrToToolBar.size() > 0){
					csrToolBar = ShareData.csrToToolBar.get(Long.parseLong(keyWord));
				}
				if(csrToolBar != null){
					txtAreaDataMemoryLeft.setValue(memory.getKeyPrompt()+"\n"+keyWord);
					txtAreaDataMemoryRight.setValue(memory.getValuePrompt()+"\n"+gson.toJson(csrToolBar));	
				}
			}

		} catch (Exception e) {		}
	}
	
	// 当搜索之后进行显示的内容
	private void resetSearchAfter(MemoryParameter parameter){
		if(parameter != null && parameter.getMemoryMethod() != null){
			if(parameter.getShowType() != null && parameter.getShowType().equals("single")){
				if(parameter.getMemoryMethod().getParameter().equals("String")){
					if(txtKeyword.getValue() == null || txtKeyword.getValue().toString().equals("")){
						lbMsg.setValue("<font color=red>必须输入参数信息进行查询</font>");
						txtAreaDataMemoryLeft.setValue("");
						txtAreaDataMemoryRight.setValue("");
					} else if(txtAreaDataMemoryLeft.getValue().toString().length()<=parameter.getMemoryMethod().getKeyPrompt().length() || txtAreaDataMemoryRight.getValue().toString().length() <= parameter.getMemoryMethod().getValuePrompt().length()){
						lbMsg.setValue("<font color=red>查询结果：没有查询到相对应的数据！</font>");
					}
				} else if(parameter.getMemoryMethod().getParameter().equals("Long") || parameter.getMemoryMethod().getParameter().equals("Integer")){
					if(txtKeyword.getValue() == null || !txtKeyword.getValue().toString().matches("[0-9]*")){
						lbMsg.setValue("<font color=red>参数必须为数字</font>");
						txtAreaDataMemoryLeft.setValue("");
						txtAreaDataMemoryRight.setValue("");
					} else if(txtAreaDataMemoryLeft.getValue().toString().length()<=parameter.getMemoryMethod().getKeyPrompt().length() || txtAreaDataMemoryRight.getValue().toString().length() <= parameter.getMemoryMethod().getValuePrompt().length()){
						lbMsg.setValue("<font color=red>查询结果：没有查询到相对应的数据！</font>");
					}
				}
				
			} else if(txtAreaDataMemoryLeft.getValue().toString().length()<=parameter.getMemoryMethod().getKeyPrompt().length() || txtAreaDataMemoryRight.getValue().toString().length() <= parameter.getMemoryMethod().getValuePrompt().length()){
				lbMsg.setValue("<font color=red>查询结果：没有查询到相对应的数据！</font>");
				txtAreaDataMemoryLeft.setValue("");
				txtAreaDataMemoryRight.setValue("");
			} else{
				
			}
		}
		
	}
	
}
