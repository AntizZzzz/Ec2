package com.jiangyifen.ec2.ui.csr.statusbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 转接界面
 * @author jrh
 *  2013-7-10
 */
@SuppressWarnings("serial")
public class CsrChannelRedirectWindow extends Window implements CloseListener,
		ValueChangeListener, Button.ClickListener {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"empNo", "realName"};

	private final String[] COL_HEADERS = new String[] {"工号", "姓名"};

	private Panel statusPanel; 				// 分机状态显示总面板
	private ComboBox queueSelector;	 		// 队列选择框
	private TextField searchKeyword; 		// 关键字搜索区
	private Table extenTable; 				// 分机状态信息显示表格
	private Notification notification;		// 提示信息

	private ComboBox transferQueues;		// 可转接的队列
	private Button directTransferToQueue;	// 直接转接到队列	
	private Button callFirstForQueue;		// 先与对队列中的接通人沟通情况
	private Button transferExtenForQueue;	// 将呼叫等待的客户所拥有的通道传入下一个话务员的ShareData 中
	
	private TextField transferTelephone;		// 待转接的手机号
	private Button directTransferToTelephone;	// 直转手机
	private Button callFirstForTelephone;		// 先与手机沟通
	private Button transferExtenForTelephone;	// 把客户对象转给手机用户
	
	private String holdOnChannel = "";	// 被置为呼叫等待的客户拥有的通道

	private String exten; 				// 当前用户所使用的分机号
	private Domain domain;				// 当前登陆者所属域
	private User loginUser; 			// 当前登录用户
	private Queue allQueues; 			// 全队列 作为队列选择框的默认值

	private QueueService queueService; 	// 队列服务类
	private StaticQueueMemberService staticQueueMemberService; 	// 队列成员服务类
	private ChannelRedirectService channelRedirectService; 		// 电话转接服务类
	private UserQueueService userQueueService;
	private UserService userService;
	private DialService dialService;
	private HangupService hangupService;
	
	private BeanItemContainer<Queue> queueContainer;
	private BeanItemContainer<User> csrStatusContainer;			// 用户状态集合(用于描述其使用的分机状态 忙/闲)
	private Or searchQueueFilter; 		// sipInfoContainer 的队列过滤器
	private Or searchKeywordFilter; 	// sipInfoContainer 的关键字过滤器

	private volatile boolean isGotoRun = true;	// 是否允许刷新表格中的内容
	private Window redirectShowLinesWindow;

	public CsrChannelRedirectWindow() {
		// 初始化该类需要的一些字段
		initializeParameters();
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		VerticalLayout sipConfigsVLayout = new VerticalLayout();
		sipConfigsVLayout.setSpacing(true);
		sipConfigsVLayout.setMargin(true);
		
		statusPanel = new Panel("电话转接");
		statusPanel.setWidth("-1px");
		statusPanel.setContent(sipConfigsVLayout);
		this.addComponent(statusPanel);

		// 创建图标描述层组件
		createIconDescription(sipConfigsVLayout);

		// 创建过滤层组件
		createFilterComponent(sipConfigsVLayout);

		// 创建分机状态显示表格
		createExtenTable(sipConfigsVLayout);
		
		createTransferToQueue(sipConfigsVLayout);
		createTransferToTelephone(sipConfigsVLayout);
		
		// 创建一个Window，目的是不重复创建
		redirectShowLinesWindow = new Window("转接窗口");
		redirectShowLinesWindow.center();
	}

	private void initializeParameters() {
		this.center();
		this.setWidth("518px");
		this.setHeight("-1px");
		this.setResizable(false);
		this.setStyleName("opaque");
		this.addListener((CloseListener) this);
		
		exten = SpringContextHolder.getExten();
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		queueService = SpringContextHolder.getBean("queueService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		channelRedirectService = SpringContextHolder.getBean("channelRedirectService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		userService = SpringContextHolder.getBean("userService");
		dialService = SpringContextHolder.getBean("dialService");
		hangupService = SpringContextHolder.getBean("hangupService");

		csrStatusContainer = new BeanItemContainer<User>(User.class);
		queueContainer = new BeanItemContainer<Queue>(Queue.class);
		
		allQueues = new Queue();
		allQueues.setDescription("全部");
		allQueues.setName("");
		queueContainer.addBean(allQueues);
		queueContainer.addAll(queueService.getAllByDomain(loginUser.getDomain()));
	}

	/**
	 * 创建用于描述分机目前使用情况的Icon 说明组件
	 * 
	 * @param sipConfigsVLayout
	 */
	private void createIconDescription(VerticalLayout sipConfigsVLayout) {
		HorizontalLayout iconHLayout = new HorizontalLayout();
		iconHLayout.setSpacing(true);
		sipConfigsVLayout.addComponent(iconHLayout);

		iconHLayout.addComponent(new Embedded(null,
				ResourceDataCsr.green_14_sidebar_ico));
		iconHLayout.addComponent(new Label("空闲; "));
		iconHLayout.addComponent(new Embedded(null,
				ResourceDataCsr.red_14_sidebar_ico));
		iconHLayout.addComponent(new Label("工作中; "));
		iconHLayout.addComponent(new Embedded(null,
				ResourceDataCsr.grey_14_sidebar_ico));
		iconHLayout.addComponent(new Label("离线。"));
	}

	/**
	 * 创建过滤组件
	 * 
	 * @param sipConfigsVLayout
	 */
	private void createFilterComponent(VerticalLayout sipConfigsVLayout) {
		HorizontalLayout filterHLayout = new HorizontalLayout();
		filterHLayout.setSpacing(true);
		filterHLayout.setWidth("100%");
		sipConfigsVLayout.addComponent(filterHLayout);

		filterHLayout.addComponent(new Label("队列"));
		queueSelector = new ComboBox();
		queueSelector.addListener(this);
		queueSelector.setNullSelectionAllowed(false);
		queueSelector.setContainerDataSource(queueContainer);
		queueSelector.setItemCaptionPropertyId("descriptionAndName");
		queueSelector.setValue(allQueues);
		filterHLayout.addComponent(queueSelector);

		searchKeyword = new TextField();
		searchKeyword.setInputPrompt("请输入工号或姓名！");
		searchKeyword.setStyleName("search");
		searchKeyword.setImmediate(true);
		searchKeyword.addListener(this);
		searchKeyword.setWidth("190px");
		filterHLayout.addComponent(searchKeyword);
	}

	/**
	 * 创建分机状态显示表格
	 * 
	 * @param sipConfigsVLayout
	 */
	private void createExtenTable(VerticalLayout sipConfigsVLayout) {
		extenTable = new Table();
		extenTable.setWidth("450px");
		extenTable.setPageLength(20);
		extenTable.setHeight("-1px");
		extenTable.setImmediate(true);
		extenTable.setSelectable(false);
		extenTable.setStyleName("striped");
		extenTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		sipConfigsVLayout.addComponent(extenTable);

		extenTable.setContainerDataSource(csrStatusContainer);
		extenTable.setVisibleColumns(VISIBLE_PROPERTIES);
		extenTable.setColumnHeaders(COL_HEADERS);
		extenTable.addGeneratedColumn("分机", new ExtenColumn());
		extenTable.setColumnAlignment("分机", Table.ALIGN_CENTER);
		extenTable.addGeneratedColumn("状态", new CsrStatusColumn());
		extenTable.setColumnAlignment("状态", Table.ALIGN_CENTER);
		extenTable.addGeneratedColumn("直接转接", new ChannelRedirectColumn());
		extenTable.setColumnAlignment("直接转接", Table.ALIGN_CENTER);
		extenTable.addGeneratedColumn("沟通后转接", new DialAndRedirectColumn());
		extenTable.setColumnAlignment("沟通后转接", Table.ALIGN_CENTER);
	}
	
	/**
	 * 创建转接到队列的相关组件
	 * @param sipConfigsVLayout
	 */
	private void createTransferToQueue(VerticalLayout sipConfigsVLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		sipConfigsVLayout.addComponent(layout);
		
		Label label = new Label("<B>转接到队列：</B>", Label.CONTENT_XHTML);
		label.setWidth("-1px");
		layout.addComponent(label);
		

		BeanItemContainer<Queue> container = new BeanItemContainer<Queue>(Queue.class);
		container.addAll(queueService.getAllByDomain(loginUser.getDomain()));
		
		transferQueues = new ComboBox();
		transferQueues.setWidth("170px");
		transferQueues.setNullSelectionAllowed(false);
		transferQueues.setContainerDataSource(container);
		transferQueues.setItemCaptionPropertyId("descriptionAndName");
		layout.addComponent(transferQueues);
		
		directTransferToQueue = new Button("直转");
		directTransferToQueue.addStyleName("borderless");
		directTransferToQueue.addStyleName(BaseTheme.BUTTON_LINK);
		directTransferToQueue.setIcon(ResourceDataCsr.transfer_14_ico);
		directTransferToQueue.addListener((ClickListener)this);
		layout.addComponent(directTransferToQueue);
		
		callFirstForQueue = new Button("沟通");
		callFirstForQueue.addStyleName("borderless");
		callFirstForQueue.addStyleName(BaseTheme.BUTTON_LINK);
		callFirstForQueue.setIcon(ResourceDataCsr.dial_12_ico);
		callFirstForQueue.addListener((ClickListener)this);
		layout.addComponent(callFirstForQueue);
		
		transferExtenForQueue = new Button("传递");
		transferExtenForQueue.addStyleName("borderless");
		transferExtenForQueue.addStyleName(BaseTheme.BUTTON_LINK);
		transferExtenForQueue.setIcon(ResourceDataCsr.queue_transfer_moh_14_ico);
		transferExtenForQueue.addListener((ClickListener)this);
		layout.addComponent(transferExtenForQueue);
	}

	/**
	 * 用于转接到手机的相关组件
	 * @param sipConfigsVLayout
	 */
	private void createTransferToTelephone(VerticalLayout sipConfigsVLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		sipConfigsVLayout.addComponent(layout);
		
		Label label = new Label("<B>转接到手机：</B>", Label.CONTENT_XHTML);
		label.setWidth("-1px");
		layout.addComponent(label);
		
		transferTelephone = new TextField();
		transferTelephone.setWidth("170px");
		transferTelephone.setInputPrompt("请输入手机号");
		transferTelephone.setDescription("<B>转接长途电话请加 '0'！</B>");
		transferTelephone.addValidator(new RegexpValidator("\\*?\\d+", "电话号格式错误，请重试！"));
		transferTelephone.setValidationVisible(false);
		layout.addComponent(transferTelephone);
		
		directTransferToTelephone = new Button("直转");
		directTransferToTelephone.addStyleName("borderless");
		directTransferToTelephone.addStyleName(BaseTheme.BUTTON_LINK);
		directTransferToTelephone.setIcon(ResourceDataCsr.transfer_14_ico);
		directTransferToTelephone.addListener((ClickListener)this);
		layout.addComponent(directTransferToTelephone);
		
		callFirstForTelephone = new Button("沟通");
		callFirstForTelephone.addStyleName("borderless");
		callFirstForTelephone.addStyleName(BaseTheme.BUTTON_LINK);
		callFirstForTelephone.setIcon(ResourceDataCsr.dial_12_ico);
		callFirstForTelephone.addListener((ClickListener)this);
		layout.addComponent(callFirstForTelephone);
		
		transferExtenForTelephone = new Button("转接");
		transferExtenForTelephone.addStyleName("borderless");
		transferExtenForTelephone.addStyleName(BaseTheme.BUTTON_LINK);
		transferExtenForTelephone.setIcon(ResourceDataCsr.transfer_14_ico);
		transferExtenForTelephone.addListener((ClickListener)this);
		layout.addComponent(transferExtenForTelephone);
	}

	/**
	 * 取得所有处于Bridged状态的Channel
	 * 
	 * @return
	 */
	private Set<String> getAllBridgedChannel(String exten) {
		Set<String> allBridgedChannel = new HashSet<String>();
		Set<String> channels = ShareData.peernameAndChannels.get(exten);
		if(channels != null) {
			for(String channel : channels) {
				ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
				if (channelSession != null) {
					String brigdedChannel = channelSession.getBridgedChannel();
					if (brigdedChannel != null && !"".equals(brigdedChannel)) {
						allBridgedChannel.add(brigdedChannel);
					}
				}
			}
		}
		
		return allBridgedChannel;
	}

	/**
	 * 取得所有处于Bridged状态的Channel、以及分机自身建立的channel(即：处于呼叫状态，但对方未接起)
	 * 
	 * @return
	 */
	private Set<String> getAllChannels(String exten) {
		Set<String> allChannels = new HashSet<String>();
		Set<String> allBridgedChannel = new HashSet<String>();
		Set<String> selfChannels = ShareData.peernameAndChannels.get(exten);
		for (String channel : selfChannels) {
			ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
			if (channelSession != null) {
				String bridgedChannel = channelSession.getBridgedChannel();
				if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
					allBridgedChannel.add(bridgedChannel);
				}
			}
		}
		allChannels.addAll(selfChannels);
		allChannels.addAll(allBridgedChannel);
		return allChannels;
	}
	
	/**
	 * 用户使用的分机显示列自动生成器
	 */
	private class ExtenColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			// 查询用户在使用的分机拥有的通道数，如果不存在则通道数为 0
			User csr = (User) itemId;
			String exten = ShareData.userToExten.get(csr.getId());
			if(exten == null) {
				return "";
			}
			return exten;
		}
	}

	/**
	 * 用户状态图标显示列自动生成器
	 */
	private class CsrStatusColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			// 查询用户在使用的分机拥有的通道数，如果不存在则通道数为 0
			User csr = (User) itemId;
			String peername = ShareData.userToExten.get(csr.getId());
			int channelCount = 0;
			synchronized (peername) {
				if(ShareData.peernameAndChannels.get(peername) != null) {
					channelCount = ShareData.peernameAndChannels.get(peername).size();
				}
			}

			// 根据通道数创建组件
			Embedded embedded = new Embedded(null, ResourceDataCsr.green_14_sidebar_ico);
			if (channelCount == 0) {
				embedded.setSource(ResourceDataCsr.green_14_sidebar_ico);
			} else if (channelCount == 1) {
				embedded.setSource(ResourceDataCsr.red_14_sidebar_ico);
			} else if (channelCount == 2) {
				embedded.setSource(ResourceDataCsr.grey_14_sidebar_ico);
			}
			
			return embedded;
		}
	}

	/**
	 * 电话转接按钮列自动生成器
	 */
	private class ChannelRedirectColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			User csr = (User) itemId;
			Button redirect = new Button("直转");
			redirect.addStyleName("borderless");
			redirect.addStyleName(BaseTheme.BUTTON_LINK);
			redirect.setIcon(ResourceDataCsr.transfer_14_ico);
			redirect.addListener((Button.ClickListener) CsrChannelRedirectWindow.this);
			
			// 为按钮附加一个工号，以在点击按钮的事件中使用（因为转接是根据用户id号找分机的）
			redirect.setData(csr.getId());
			
			return redirect;
		}
	}
	
	/**
	 * 先与话务员沟通后再转接
	 */
	private class DialAndRedirectColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final User csr = (User) itemId;
			Button dial = new Button("沟通");
			dial.addStyleName("borderless");
			dial.addStyleName(BaseTheme.BUTTON_LINK);
			dial.setIcon(ResourceDataCsr.dial_12_ico);
			dial.setImmediate(true);
			dial.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					// 检查当前话务员是否有通话
					List<String> allSelfBridgedChannels = new ArrayList<String>(getAllBridgedChannel(exten));
					if(allSelfBridgedChannels.size() == 0) {
						notification.setCaption("<font color='red'><B>您当前没有通话！</B></font>");
						statusPanel.getApplication().getMainWindow().showNotification(notification);
						return;
					}

					// 检查需要沟通话务员【被叫】是否有通话
					String destinationExten = ShareData.userToExten.get(csr.getId());
					List<String> allDestBridgedChannels = new ArrayList<String>(getAllBridgedChannel(destinationExten));
					if(allDestBridgedChannels.size() > 0) {
						String empNo = csr.getEmpNo();
						notification.setCaption("<font color='red'>工号:"+empNo+"当前正忙,请与其他坐席联系，或稍后再试！<B！</B></font>");
						statusPanel.getApplication().getMainWindow().showNotification(notification);
						return;
					}
					
					// 先将当前话务员的通话转接到一个队列中，然后再给发起呼叫
					String selfBridgedChannel = allSelfBridgedChannels.get(0);
					channelRedirectService.redirectQueue(selfBridgedChannel, "900000");
//					channelRedirectService.redirectCommonExtension(selfBridgedChannel, "holdon");
					holdOnChannel = selfBridgedChannel;
					
					boolean success = dialService.dial(exten, destinationExten);
					if(success) {
						notification.setCaption("成功发出沟通请求！");
					} else {
						notification.setCaption("<font color='red'><B>请求失败，请重试！</B></font>");
					}
					statusPanel.getApplication().getMainWindow().showNotification(notification);
				}
			});
			
			Button redirect = new Button("传递");
			redirect.addStyleName("borderless");
			redirect.addStyleName(BaseTheme.BUTTON_LINK);
			redirect.setIcon(ResourceDataCsr.queue_transfer_moh_14_ico);
			redirect.setImmediate(true);
			redirect.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					List<String> allBridgedChannels = new ArrayList<String>(getAllBridgedChannel(exten));
					String bridgedChannel = "";
					if(allBridgedChannels.size() == 1) {
						bridgedChannel = allBridgedChannels.get(0);
					} else {
						notification.setCaption("<font color='red'><B>您当前没有通话！</B></font>");
						statusPanel.getApplication().getMainWindow().showNotification(notification);
						return;
					}
					
					String currentBridgedExten = "000000";
					int startIndex = bridgedChannel.indexOf("/")+1;
					int endIndex = bridgedChannel.indexOf("-");
					try {
						currentBridgedExten = bridgedChannel.substring(startIndex, endIndex);
						if(!ShareData.domainToExts.get(domain.getId()).contains(currentBridgedExten)) {
							notification.setCaption("<font color='red'><B>您当前没有通话对象不是坐席，请先跟坐席沟通后重试！</B></font>");
							statusPanel.getApplication().getMainWindow().showNotification(notification);
							return;
						}
					} catch (Exception e) {
						logger.error("jrh 话务员转接时出现异常", e);
					}
					
					if(!"000000".equals(currentBridgedExten)) {
						// 将呼入的客户通道存放到即将处理该业务的话务员对应的内存中去，以便其获取
						List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(currentBridgedExten);
						if(holdOnCallerChannels == null) {
							holdOnCallerChannels = new ArrayList<String>();
							ShareData.userExtenToHoldOnCallerChannels.put(currentBridgedExten, holdOnCallerChannels);
						}
						if(!holdOnCallerChannels.contains(holdOnChannel)) {
							holdOnCallerChannels.add(holdOnChannel);
						}
						
						// TODO
						Long bridgedCsrId = ShareData.extenToUser.get(currentBridgedExten);
						CsrStatusBar bridgedCsrStatusbar = ShareData.csrToStatusBar.get(bridgedCsrId); 
						if(bridgedCsrStatusbar != null) {
							bridgedCsrStatusbar.updateDialHoldOnCallerBt();
						}
						
						// 转给某个队列时，必须有CSR1 来通过点击 transferExtenForQueue 来进行挂断，不然将转接失败
						Set<String> cs = getAllChannels(exten);
						for(String c : cs) {
							hangupService.hangup(c);
						}
						notification.setCaption("传递客户通道成功！");
					} else {
						notification.setCaption("<font color='red'><B>传递客户通道失败，请重试！</B></font>");
					}
					statusPanel.getApplication().getMainWindow().showNotification(notification);
				}
			});
			
			HorizontalLayout layout = new HorizontalLayout();
			layout.setSpacing(true);
			layout.addComponent(dial);
			layout.addComponent(redirect);
			return layout;
		}
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if (source == searchKeyword) {
			synchronized (csrStatusContainer) {
				if(searchKeywordFilter != null) {
					csrStatusContainer.removeContainerFilter(searchKeywordFilter);
				}
			}

			String searchValue = (String) searchKeyword.getValue();
			if(!"".equals(searchValue.trim())) {
				searchKeywordFilter = new Or(
						new Like("empNo", "%" + searchValue + "%"),
						new Like("realName", "%" + searchValue + "%"));

				synchronized (csrStatusContainer) {
					csrStatusContainer.addContainerFilter(searchKeywordFilter);
				}
			}
		} else if (source == queueSelector) {
			synchronized (csrStatusContainer) {
				if(searchQueueFilter != null) { 
					csrStatusContainer.removeContainerFilter(searchQueueFilter);
				}
			}
			
			Queue selectedQueue = (Queue) queueSelector.getValue();
			String queueName = selectedQueue.getName();
			
			if (selectedQueue.getId() != null) {
				List<UserQueue> dynQueueMembers = userQueueService.getAllByQueueName(queueName, loginUser.getDomain());
				List<StaticQueueMember> staticQueueMembers = staticQueueMemberService.getAllByQueueName(loginUser.getDomain(), queueName);

				List<String> empNoList = new ArrayList<String>();
				// 根据动态队列添加工号
				for(UserQueue uq : dynQueueMembers) {
					for(Long userId : ShareData.extenToUser.values()) {
						User csr = userService.get(userId);
						if(!csr.getUsername().equals(loginUser.getUsername()) 
								&& csr.getUsername().equals(uq.getUsername())) {
							empNoList.add(csr.getEmpNo());
							break;
						}
					}
				}
				
				// 根据静态队列添加工号
				for (StaticQueueMember sqm : staticQueueMembers) {
					//根据分机取得用户
					Long userId = ShareData.extenToUser.get(sqm.getSipname());
					if(userId != null && !userId.equals(loginUser.getId())) {
						User csr = userService.get(userId);
						empNoList.add(csr.getEmpNo());
					}
				}
				
				Like[] likes = new Like[empNoList.size()];
				// 根据工号添加过滤器
				for(int i = 0; i < empNoList.size(); i++) {
					likes[i] = new Like("empNo", empNoList.get(i));
				}
				
				searchQueueFilter = new Or(likes);
				synchronized (csrStatusContainer) {
					csrStatusContainer.addContainerFilter(searchQueueFilter);
				}
			}
		}
	}

	/**
	 * 转接按钮点击的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		List<String> allBridgedChannels = new ArrayList<String>(getAllBridgedChannel(exten));
		try {
			if (allBridgedChannels.size() == 0) {
				notification.setCaption("<font color='red'><B>您当前没有通话！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return;
			} else if (allBridgedChannels.size() == 1) {
				boolean isSuccess = false;
				String bridgedChannel = allBridgedChannels.get(0);
				if(source == directTransferToQueue) {
					holdOnChannel = "";
					Queue queue = (Queue) transferQueues.getValue();
					if(queue == null) {					
						notification.setCaption("<font color='red'><B>队列不能为空，请重试！</B></font>");
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					}
					String queueName = queue.getName();
					isSuccess = channelRedirectService.redirectQueue(bridgedChannel, queueName);
				} else if(source == callFirstForQueue) {
					Queue queue = (Queue) transferQueues.getValue();
					if(queue == null) {
						notification.setCaption("<font color='red'><B>队列不能为空，请重试！</B></font>");
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					}
					channelRedirectService.redirectQueue(bridgedChannel, "900000");
					holdOnChannel = bridgedChannel;
					boolean success = dialService.dial(exten, queue.getName());
					if(success) {
						notification.setCaption("成功发出沟通请求！");
					} else {
						notification.setCaption("<font color='red'><B>请求失败，请重试！</B></font>");
					}
					this.getApplication().getMainWindow().showNotification(notification);
					return;
				} else if(source == transferExtenForQueue) {
					String currentBridgedExten = "000000";
					int startIndex = bridgedChannel.indexOf("/")+1;
					int endIndex = bridgedChannel.indexOf("-");
					try {
						currentBridgedExten = bridgedChannel.substring(startIndex, endIndex);
					} catch (Exception e) {
						logger.error("jrh 话务员转接时出现异常", e);
					}
					
					if(!"000000".equals(currentBridgedExten)) {
						// 将呼入的客户通道存放到即将处理该业务的话务员对应的内存中去，以便其获取
						List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(currentBridgedExten);
						if(holdOnCallerChannels == null) {
							holdOnCallerChannels = new ArrayList<String>();
							ShareData.userExtenToHoldOnCallerChannels.put(currentBridgedExten, holdOnCallerChannels);
						}
						if(!holdOnCallerChannels.contains(holdOnChannel)) {
							holdOnCallerChannels.add(holdOnChannel);
						}
						
						// 转给某个队列时，必须有CSR1 来通过点击 transferExtenForQueue 来进行挂断，不然将转接失败
						Set<String> cs = getAllChannels(exten);
						for(String c : cs) {
							hangupService.hangup(c);
						}
						notification.setCaption("传递客户通道成功！");
					} else {
						notification.setCaption("<font color='red'><B>传递客户通道失败，请重试！</B></font>");
					}
					this.getApplication().getMainWindow().showNotification(notification);
					return;
				} else if(source == directTransferToTelephone) {
					holdOnChannel = "";
					String phone = StringUtils.trimToEmpty((String) transferTelephone.getValue());
					if(transferTelephone.isValid() && !"".equals(phone)) {
						isSuccess = channelRedirectService.redirectTelephone(bridgedChannel, phone);
					} else {
						notification.setCaption("<font color='red'><B>电话号输入错误，请重试！</B></font>");
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					}
				} else if(source == callFirstForTelephone) {
					String phone = StringUtils.trimToEmpty((String) transferTelephone.getValue());
					if(transferTelephone.isValid() && !"".equals(phone)) {
						channelRedirectService.redirectQueue(bridgedChannel, "900000");
						holdOnChannel = bridgedChannel;
						boolean success = dialService.dial(exten, phone);
						if(success) {
							notification.setCaption("成功发出沟通请求！");
						} else {
							notification.setCaption("<font color='red'><B>请求失败，请重试！</B></font>");
						}
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					} else {
						notification.setCaption("<font color='red'><B>电话号输入错误，请重试！</B></font>");
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					}
				} else if(source == transferExtenForTelephone) {
					String phone = StringUtils.trimToEmpty((String) transferTelephone.getValue());
					if(transferTelephone.isValid() && !"".equals(phone)) {
						// TODO 由于asterisk 有bug，所以暂时这样实现
						Set<String> cs = getAllChannels(exten);
						for(String c : cs) {
							hangupService.hangup(c);
						}
						isSuccess = channelRedirectService.redirectExten(holdOnChannel, phone);
//						TODO 以后asterisk 支持bridge action 后使用下面的方式
//						isSuccess = doBridgeChannelsService.doBridgeChannels(holdOnChannel, bridgedChannel, true);
					} else {
						notification.setCaption("<font color='red'><B>电话号输入错误，请重试！</B></font>");
						this.getApplication().getMainWindow().showNotification(notification);
						return;
					}
				} else {
					String sipname = ShareData.userToExten.get(source.getData());
					isSuccess = channelRedirectService.redirectExten(bridgedChannel, sipname);
				}
				
				if(isSuccess) {
					notification.setCaption("转接成功！");
					this.getApplication().getMainWindow().showNotification(notification);
				} else {
					notification.setCaption("<font color='red'><B>转接失败，请重试！</B></font>");
					this.getApplication().getMainWindow().showNotification(notification);
				}
				return;
			} else {
				final String sipname = ShareData.userToExten.get(source.getData());
				// 移除窗口
				redirectShowLinesWindow.removeAllComponents();
				if (redirectShowLinesWindow.getParent() != null) {
					redirectShowLinesWindow.getParent().removeWindow(redirectShowLinesWindow);
				}

				// 构建窗口
				for (final String channel : allBridgedChannels) {
					final Button redirect = new Button("转接"+ ShareData.channelAndChannelSession.get(channel).getCallerIdNum());
					redirect.setStyleName("borderless");
					redirect.setIcon(ResourceDataCsr.transfer_14_ico);
					redirect.addListener(new Button.ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							boolean isSuccess = channelRedirectService.redirectExten(
									ShareData.channelAndChannelSession.get(channel).getBridgedChannel(), sipname);
							if(isSuccess) {
								notification.setCaption("转接成功！");
								redirect.getApplication().getMainWindow().showNotification(notification);
							} else {
								notification.setCaption("<font color='red'><B>转接失败，请重试！</B></font>");
								redirect.getApplication().getMainWindow().showNotification(notification);
							}
							event.getButton().getWindow().getParent().removeWindow(event.getButton().getWindow());
						}
					});
					redirectShowLinesWindow.addComponent(redirect);
				}

				// 添加窗口
				this.getApplication().getMainWindow().addWindow(redirectShowLinesWindow);
			}
		} catch (Exception e) {
			notification.setCaption("<font color='red'><B>转接失败，请重试！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			logger.error("jrh 话务员转接时出现异常"+e.getMessage(), e);
		}
	}

	@Override
	public void windowClose(CloseEvent e) {
		queueSelector.setValue("default");
		searchKeyword.setValue("");
		// 使得更新Table表格组件线程跳出循环刷新
		isGotoRun = false;
	}
	
	/**
	 * 更新表格的数据源，供 CsrStatusBar.java 的转接按钮的事件调用
	 */
	public void updateTableSource() {
		// 每次调用刷新方法时，先将设置可刷新状态
		isGotoRun = true;
		
		new Thread("话务员电话转接窗口实时线程-->"+System.currentTimeMillis()) {
			@Override
			public void run() {
				while(isGotoRun) {
					try {
						// 删除所有的用户，这样做的原因是，如果Table 中的数据源没有发生变化，那么表格中用来描述分机状态的图标也不会发生变化(即使分机状态已经变化)
						synchronized (csrStatusContainer) {
							csrStatusContainer.removeAllItems();
						}
						
						for(Long csrId : ShareData.extenToUser.values()) {
							User csr = userService.get(csrId);
							if(csr.getDomain().getId() != null && csr.getDomain().getId().equals(domain.getId()) && !csr.getId().equals(loginUser.getId())) {
								// 由于过滤器按对象各个字段过滤对象时，对象的字段值不能为空，不然会报异常，所以，如果用户名为空，则置为空字符串
								if(csr.getRealName() == null) {
									csr.setRealName("");
								}
								synchronized (csrStatusContainer) {
									csrStatusContainer.addItem(csr);
								}
							}
						}
						synchronized (csrStatusContainer) {
							csrStatusContainer.sort(new Object[] {"username"}, new boolean[] {true});
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e.getMessage() +"更新转接电话界面时，线程更新表格信息出现异常！", e);
						throw new RuntimeException("更新转接电话界面时，线程更新表格信息出现异常！");
					}
					try {
						Thread.sleep(5000); // 更新成功还是失败，都让线程睡眠5秒
					} catch (InterruptedException e) {
						e.printStackTrace();
						logger.error(e.getMessage() +"更新转接电话界面时，线程休眠出现异常！", e);
						throw new RuntimeException("更新转接电话界面时，线程休眠出现异常！");
					}
				}
			}
		}.start();
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
