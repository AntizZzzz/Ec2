package com.jiangyifen.ec2.ui.csr.workarea.callrecord;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.asteriskjava.fastagi.AgiChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomDialPopupWindow;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @Description 描述：呼叫记录显示界面
 *
 * @author  JRH
 * @date    2012年9月9日 下午4:17:07
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class CdrTabView extends VerticalLayout {
	
	// 号码加密权限
	private final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	private final String CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD = "call_record_management&download_call_record";
	private final String CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD = "call_record_management&listen_call_record";
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final DecimalFormat DF_ONE_PRECISION = new DecimalFormat("0.0");

	// 表格的显示属性列
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"startTimeDate", "duration", "billableSeconds", "ec2_billableSeconds", "ringDuration", "cdrDirection", "isBridged",  
			"recordFileName", "url", "resourceId", "src", "destination", "srcDeptName", "srcEmpNo", "srcRealName", "srcUsername", 
			"destDeptName", "destEmpNo", "destRealName", "destUsername", "projectName", "isAutoDial", "userField" };
	
	// 表格的列标题
	private final String[] COL_HEADERS = new String[] {"联系时间", "呼叫时长", "接通时长", "通话时长", "振铃时长", "呼叫方向", "接通情况", 
			"录音文件名称", "试听/下载录音", "客户编号", "主叫", "被叫", "主叫部门", "主叫工号", "主叫姓名", "主叫用户名", 
			"被叫部门", "被叫工号", "被叫姓名", "被叫用户名", "项目名称", "呼叫类型", "客户按键" };
	
	// 不能看到url列【试听、下载】，表格的显示属性列
	private final Object[] VISIBLE_NOURL_PROPERTIES = new Object[] {"startTimeDate", "duration", "billableSeconds", "ec2_billableSeconds", "ringDuration", "cdrDirection", "isBridged",  
			"recordFileName", "resourceId", "src", "destination", "srcDeptName", "srcEmpNo", "srcRealName", "srcUsername", 
			"destDeptName", "destEmpNo", "destRealName", "destUsername", "projectName", "isAutoDial", "userField" };
	
	// 不能看到url列【试听、下载】，表格的列标题
	private final String[] COL_NOURL_HEADERS = new String[] {"联系时间", "呼叫时长", "接通时长", "通话时长", "振铃时长", "呼叫方向", "接通情况", 
			"录音文件名称", "客户编号", "主叫", "被叫", "主叫部门", "主叫工号", "主叫姓名", "主叫用户名", 
			"被叫部门", "被叫工号", "被叫姓名", "被叫用户名", "项目名称", "呼叫类型", "客户按键" };
	
	private Integer[] screenResolution;					// 屏幕分辨率
	private Table cdrTable;								// 显示呼叫记录的表格
	private CdrFilter cdrFilter;						// 呼叫记录的查询工具
	private FlipOverTableComponent<Cdr> cdrTableFlipOver;	// 为Table添加的翻页组件
	private CustomDialPopupWindow customDialPopupWindow;	// 自定义呼叫弹屏窗口
	
	private String searchSql;							// 用于搜索对象的查询语句
	private String countSql;							// 用于统计记录总数的查询语句
	
	private User loginUser;								// 当前登录的用户
	private String exten;								// 当前用户所使用的分机号
	private ArrayList<String> ownBusinessModels;		// 当前用户拥有的权限
	private boolean isEncryptMobile = true;				// 默认使用加密的电话号码和手机号
	private CdrService cdrService;						// 呼叫记录服务类
	private DialService dialService;					// 拨打电话的服务类
	private TelephoneService telephoneService;			// 电话号码服务类
	
	private Label player;								// 播放器显示标签
	private HorizontalLayout playerLayout;				// 存放播放器的布局管理器
	
	private Label statisticInfo_lb;		                // 坐席统计信息
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	

	//录音基础路径
	private String baseUrl=BaseUrlUtils.getBaseUrl();
	
	public CdrTabView() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();		
		exten = SpringContextHolder.getExten();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		cdrService = SpringContextHolder.getBean("cdrService");
		dialService = SpringContextHolder.getBean("dialService");
		telephoneService = SpringContextHolder.getBean("telephoneService");

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		// 创建Table下面的收索组件
		cdrFilter = new CdrFilter();
		cdrFilter.setCdrTabView(this);
		this.addComponent(cdrFilter);
		
		// 存放cdr显示Table 和 翻页组件
		VerticalLayout cdrVLayout = new VerticalLayout();
		cdrVLayout.setWidth("100%");
		cdrVLayout.setSpacing(true);
		this.addComponent(cdrVLayout);
		this.setExpandRatio(cdrVLayout, 1);
		
		// 创建呼叫记录的显示表格
		createCdrTable(cdrVLayout);
		
		HorizontalLayout tableFooterLayout = new HorizontalLayout();
		tableFooterLayout.setWidth("100%");
		tableFooterLayout.setSpacing(true);
		cdrVLayout.addComponent(tableFooterLayout);
		
		playerLayout = new HorizontalLayout();
		playerLayout.setSpacing(true);
		if(ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {	// 如果持有试听权限，则添加试听组件，否则不添加
			tableFooterLayout.addComponent(playerLayout);
		}
		
		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 " 
								+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
		
		// 创建翻页组件
		createTableFlipOver(tableFooterLayout);
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
	}
	
	/**
	 * 创建呼叫记录的显示表格
	 * @param cdrVLayout
	 */
	private void createCdrTable(VerticalLayout cdrVLayout) {
		cdrTable = createFormatColumnTable();
		cdrTable.setStyleName("striped");
		cdrTable.setImmediate(true);
		cdrTable.setSelectable(true);
		cdrTable.setSizeFull();
		cdrTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		cdrTable.setColumnAlignment("calldate", Table.ALIGN_CENTER);
		cdrTable.addGeneratedColumn("src", new DialColumnGenerator());
		cdrTable.addGeneratedColumn("destination", new DialColumnGenerator());
		cdrTable.setColumnWidth("src", 97);
		cdrTable.setColumnWidth("destination", 97);
//		TODO
		// 只有当前用户包含试听或者下载录音两个权限中的任何一个，就创建 url【试听、下载录音】列
		if(ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD) 
				|| ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {
			cdrTable.addGeneratedColumn("url", new ListenTypeColumnGenerator());
		}
		cdrVLayout.addComponent(cdrTable);
	}

	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null && !"isAutoDial".equals(colId)) {
					return "";
				} else if(property.getType() == Date.class) {
					if(property.getValue() != null) {
						return dateFormat.format((Date)property.getValue());
					}
					return "";
				} else if("isAutoDial".equals(colId)) {
					Boolean value = (Boolean) property.getValue();
					if(value == null) {
						return "手动呼叫";
					}
					return value ? "自动群呼" : "语音群发";
				} else if("isBridged".equals(colId)) {
					boolean value = (Boolean) property.getValue();
					return value ? "已接通" : "";
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 创建表格的翻页组件
	 */
	private void createTableFlipOver(HorizontalLayout tableFooterLayout) {
		VerticalLayout bottomRight_vl = new VerticalLayout();
		tableFooterLayout.addComponent(bottomRight_vl);
		bottomRight_vl.setWidth("100%");
		tableFooterLayout.setComponentAlignment(bottomRight_vl, Alignment.TOP_RIGHT);
		tableFooterLayout.setExpandRatio(bottomRight_vl, 1.0f);
		
		// 设置初始查询语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		countSql = "select count(c) from Cdr as c where (c.srcUsername = '"+loginUser.getUsername()+"' or c.destUsername = '"
					+ loginUser.getUsername()+"') and c.startTimeDate >= '" +dateStrs[0]+ "' and c.startTimeDate < '" +dateStrs[1]+"'";
		searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.startTimeDate desc";
		
		// 创建翻页组件
		cdrTableFlipOver = new FlipOverTableComponent<Cdr>(Cdr.class, cdrService, cdrTable, searchSql , countSql, null);
		cdrFilter.setTableFlipOver(cdrTableFlipOver);
		
		// 只有当前用户包含试听或者下载录音两个权限中的任何一个，就创建 url【试听、下载录音】列
		if(ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD) 
				|| ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {
			cdrTable.setVisibleColumns(VISIBLE_PROPERTIES);
			cdrTable.setColumnHeaders(COL_HEADERS);
			if (ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD) && ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {
				cdrTable.setColumnHeader("url", "试听/下载录音");
			} else if (ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD)) {
				cdrTable.setColumnHeader("url", "下载录音");
			} else if (ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {
				cdrTable.setColumnHeader("url", "试听录音");
			} 
		} else {
			cdrTable.setVisibleColumns(VISIBLE_NOURL_PROPERTIES);
			cdrTable.setColumnHeaders(COL_NOURL_HEADERS);
		}
		
		bottomRight_vl.addComponent(cdrTableFlipOver);
		bottomRight_vl.setComponentAlignment(cdrTableFlipOver, Alignment.TOP_RIGHT);
		
		statisticInfo_lb = new Label("", Label.CONTENT_XHTML);
		statisticInfo_lb.setWidth("-1px");
		bottomRight_vl.addComponent(statisticInfo_lb);
		bottomRight_vl.setComponentAlignment(statisticInfo_lb, Alignment.TOP_RIGHT);
		
		// 初始化当个坐席的话务统计信息
		Date[] timeScope = ParseDateSearchScope.parseToDate("今天");
		initializeStatisticInfo(timeScope[0], timeScope[1]);
		
		//设置表格的显示行数
		setTablePageLength();
	}

	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			cdrTable.setPageLength(28);
			cdrTableFlipOver.setPageLength(28, false);
		} else if(screenResolution[1] >= 1050) {
			cdrTable.setPageLength(25);
			cdrTableFlipOver.setPageLength(25, false);
		} else if(screenResolution[1] >= 900) {
			cdrTable.setPageLength(18);
			cdrTableFlipOver.setPageLength(18, false);
		} else if(screenResolution[1] >= 768) {
			cdrTable.setPageLength(11);
			cdrTableFlipOver.setPageLength(11, false);
		} else {
			cdrTable.setPageLength(8);
			cdrTableFlipOver.setPageLength(8, false);
		}
	}
	
	/**
	 * 创建呼叫客户时需要显示个各种信息的组件
	 */
	private void createOutgoingDialWindow() {
		customDialPopupWindow = new CustomDialPopupWindow();
		if(screenResolution[0] >= 1366) {
			customDialPopupWindow.setWidth("1190px");
		} else {
			customDialPopupWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			customDialPopupWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			customDialPopupWindow.setHeight("560px");
		} else {
			customDialPopupWindow.setHeight("510px");
		}

		customDialPopupWindow.setResizable(false);
		customDialPopupWindow.setStyleName("opaque");
	}
	
	/**
	 * 用于自动生成“试听录音”列
	 */
	private class ListenTypeColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Cdr cdr = (Cdr) itemId;
			String tmpUrl = null;
			if(isEncryptMobile) {
				tmpUrl = cdr.getRealUrl(baseUrl);
			} else {
				tmpUrl = cdr.getUrl(baseUrl);
			}
			final String url = tmpUrl;
			
			if(url != null) {	// 如果录音存在则返回其他组件
				Button listen = new Button("试听");
				listen.setStyleName(BaseTheme.BUTTON_LINK);
				listen.setIcon(ResourceDataCsr.listen_type_ico);
				listen.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if(player != null) {
							player.setValue(null);
							playerLayout.removeAllComponents();
						}

						// 判断当前是否对电话进行了加密，如果加密了，则播放器上的录音访问路径也不能显示电话号码，只能显示不带电话的录音名称
						String path = "<EMBED src='" +url+ "' hidden='false' autostart='true' width=360 height=63 " 
								+ "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
						player = new Label(path, Label.CONTENT_XHTML);
						playerLayout.addComponent(player);
					}
				});
				
				Link typelink = new Link("下载", new ExternalResource(url));
				typelink.setIcon(ResourceDataCsr.down_type_ico);
				typelink.setTargetName("_blank");
				
				//TODO The button with a caption is created by lc,as follows. 
//				final Button download = new Button("下载");
//				download.setIcon(ResourceDataCsr.down_type_ico);
//				download.setStyleName(BaseTheme.BUTTON_LINK);
//				if(cdr.getIsDownloaded()){
//					download.addStyleName("red");
//				}
//				download.addListener(new Button.ClickListener() {
//					
//					@Override
//					public void buttonClick(ClickEvent event) {
//						 
//						download.addStyleName("red");
//					 
//						downloadRecord(url);
//						
//						updateCdr(cdr);
//					}
//				});

				HorizontalLayout layout = new HorizontalLayout();
				layout.setSpacing(true);
				layout.setWidth("100%");
				if (ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_LISTEN_CALL_RECORD)) {
					layout.addComponent(listen);
					layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);
				} 

				if (ownBusinessModels.contains(CALL_RECORD_MANAGEMENT_DOWNLOAD_CALL_RECORD)) {
					layout.addComponent(typelink);
					layout.setComponentAlignment(typelink, Alignment.MIDDLE_RIGHT);
				} 
				
				return layout;
			} else {
				return new Label("无录音文件");
			}
		}
	}
	
	//TODO The function is added by lc,as follows.
	public void downloadRecord(String url) {

		url = "/var/www/html/"+url.substring(url.indexOf("monitor"), url.length());
		
		File file = new File(url);

		 logger.info("file.exists(): "+file.exists());
		 logger.info("url: "+url);
		 
	 
		if (!file.exists()) {
			return;
		}

		Resource resource = new FileResource(file, this.getApplication());

		final Embedded downloader = new Embedded();

		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {

			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});
		
		recordOperatorLog(file,null);
	}
	
	//Update a cdr entity.
	public void updateCdr(Cdr cdr){
		
		cdr.setIsDownloaded(true);
		cdrService.update(cdr);
	}
	
	//Record the operator log.
	public void recordOperatorLog(File file,String sql){
		
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser()
				.getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser()
				.getRealName());
		WebApplicationContext context = (WebApplicationContext) this
				.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("csr 呼叫记录");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder
				.getBean("commonService");
		commonService.save(operationLog);
	}


	/**
	 * 用于自动生成可以拨打电话的列
	 */
	private class DialColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Property prop = source.getItem(itemId).getItemProperty(columnId);
			if(columnId.equals("src")) {
				return createGeneratorDialColumn(prop);
			} else if(columnId.equals("destination")) {
				return createGeneratorDialColumn(prop);
			} 
			return null;
		}
		
		/**
		 * 根据属性值，创建拨号按钮
		 * @param prop		属性对象
		 * @return
		 */
		private Object createGeneratorDialColumn(Property prop) {
			if(prop.getValue() == null) {
				return null;
			}
			
			final String connectedlinenum = prop.getValue().toString().trim();
			// 如果该字段的值为空，则不创建组件
			if("".equals(connectedlinenum)) {
				return null;
			}
			
			Button dialButton = new Button();
			dialButton.setStyleName("borderless");
			dialButton.setStyleName(BaseTheme.BUTTON_LINK);
			dialButton.setIcon(ResourceDataCsr.dial_12_ico);
			if(isEncryptMobile) {
				dialButton.setCaption(telephoneService.encryptMobileNo(connectedlinenum));
			} else {
				dialButton.setCaption(connectedlinenum);
			}
			dialButton.addListener(new ClickListener() {
				public void buttonClick(ClickEvent event) {
					// 检查当前聚焦tab 页，如果不是CdrTabView.this
					VerticalLayout currentTab = ShareData.csrToCurrentTab.get(loginUser.getId());
					if(currentTab.getClass() != CdrTabView.class) {
						ShareData.csrToCurrentTab.put(loginUser.getId(), CdrTabView.this);
					}
					dialService.dial(exten, connectedlinenum);
				}
			});
			return dialButton;
		}
	}

	/**
	 * 初始化单个坐席在线时长，话务统计等信息 2013-09-16
	 * @param startDate	开始查询时间
	 * @param endDate	截止查询时间
	 */
	public void initializeStatisticInfo(Date startDate, Date endDate) {
		// 统计话务信息
		String startDateSql = sdf.format(startDate);
		String endDateSql = sdf.format(endDate);
		String username = loginUser.getUsername();

		// 统计呼叫总数，总通时
		String callSql1 = "select count(*) as callAmount,sum(billableseconds) as totalCallTime from cdr where starttimedate >= '"+startDateSql +
				"' and starttimedate < '"+endDateSql+"' and (srcusername = '"+username+"' or destusername = '"+username+"')";
		
		List<Object[]> callInfos1 = cdrService.getInfosByNativeSql(callSql1);
		long callAmount = 0;
		long totalCallTime = 0;
		if(callInfos1.size() > 0) {
			callAmount = (Long) callInfos1.get(0)[0];
			totalCallTime = (callInfos1.get(0)[1] != null) ? ((Long) callInfos1.get(0)[1]) : 0;
		}
		
		// 统计接通总数，从发起电话到应答的总耗时
		String callSql2 = "select count(*) as bridgedAmount,sum(ringduration) as totalAnswerTime from cdr where isbridged = true and " +
				" starttimedate >= '"+startDateSql+"' and starttimedate < '"+endDateSql+"' and (srcusername = '"+username+"' or destusername = '"+username+"')";
		List<Object[]> callInfos2 = cdrService.getInfosByNativeSql(callSql2);
		
		long bridgedAmount = 0;
		long totalAnswerTime = 0;
		if(callInfos2.size() > 0) {
			bridgedAmount = (Long) callInfos2.get(0)[0];
			totalAnswerTime = (callInfos2.get(0)[1] != null) ? (Long) callInfos2.get(0)[1] : 0;
		}
		
		// 计算接通率，平均通时，平均应答速度
		double bridgedRate = (callAmount > 0) ? ((bridgedAmount + 0.0) / callAmount) : 0.0;
		double avgCallTime = (callAmount > 0) ? ((totalCallTime + 0.0) / callAmount) : 0.0;
		double avgAnswerTime = (bridgedAmount > 0) ? ((totalAnswerTime + 0.0) / bridgedAmount) : 0.0000;
		
		// 统计在线时长分两步走：第一步统计那些在当前查询时间范围内，登陆时间和退出时间都不为空的记录的时间差值
		String onlineSql1 = "select sum(logoutdate - logindate) as onlineTime from ec2_user_login_record where" +
				" logindate >= '"+startDateSql+"' and logindate < '"+endDateSql+"' " +
				" and logoutdate >= '"+startDateSql+"' and logoutdate < '"+endDateSql+"' and username = '"+username+"'";
		List<Object[]> onlineInfos1 = cdrService.getInfosByNativeSql(onlineSql1);

		ArrayList<Integer> onlineTimeInfos1 = new ArrayList<Integer>();
		if(onlineInfos1.size() > 0) {
			onlineTimeInfos1.addAll(formatTimeInteval(onlineInfos1.get(0)+""));
		} 
	
		// 第二步：如果当前查询的结束时间比当前时间点大，则需要把坐席当前的在线时间也算在内，因为坐席此时没有下线，所以会存在一条只有登陆时间，没有退出时间的记录
		ArrayList<Integer> onlineTimeInfos2 = new ArrayList<Integer>();
		Date now = new Date();
		if(startDate.before(now) && endDate.after(now)) {
			String onlineSql2 = "select (now() - max(logindate)) as onlineTime from ec2_user_login_record where logindate >= '"+startDateSql+"' and logindate < '"+endDateSql+"' and logoutdate is null and username = '"+username+"'";
		
			List<Object[]> onlineInfos2 = cdrService.getInfosByNativeSql(onlineSql2);
			if(onlineInfos2.size() > 0) {
				onlineTimeInfos2.addAll(formatTimeInteval(onlineInfos2.get(0)+""));
			} 
		}
		
		ArrayList<Integer> onlineTimeInfos = new ArrayList<Integer>();
		if(onlineTimeInfos1.size() > 0 && onlineTimeInfos2.size() > 0) {
			for(int i = 0; i < onlineTimeInfos1.size(); i++) {
				onlineTimeInfos.add(onlineTimeInfos1.get(i) + onlineTimeInfos2.get(i));
			}
		} else if(onlineTimeInfos1.size() > 0) {
			onlineTimeInfos.addAll(onlineTimeInfos1);
		} else if(onlineTimeInfos2.size() > 0) {
			onlineTimeInfos.addAll(onlineTimeInfos2);
		}

		String onlineTimeStr = "";
		if(onlineTimeInfos.size() == 0) {
			onlineTimeStr = "00:00:00";
		} else {
			int day = onlineTimeInfos.get(0);
			if(day > 0) {
				onlineTimeStr = day+"天 ";
			} 
			onlineTimeStr += onlineTimeInfos.get(1)+":"+onlineTimeInfos.get(2)+":"+onlineTimeInfos.get(3);
		}
		
		// 更新统计信息的标签信息	[呼叫总数：接通总数：接通率：总通时：平均通时：平均应答速度：在线时长：]
		String info = "<font color='blue'>统计 =>&nbsp</font>呼叫总数：<font color='blue'><B>"+callAmount+"</B></font>"+
				", 接通总数：<font color='blue'><B>"+bridgedAmount+"</B></font>"+
				", 接通率：<font color='blue'><B>"+DF_ONE_PRECISION.format((bridgedRate*100))+"%</B></font>"+
				", 总通时：<font color='blue'><B>"+totalCallTime+"</B></font>"+
				"秒, 平均通时：<font color='blue'><B>"+DF_ONE_PRECISION.format(avgCallTime)+"</B></font>"+
				"秒, 平均应答速度：<font color='blue'><B>"+DF_ONE_PRECISION.format(avgAnswerTime)+"</B></font>"+
				"秒, 在线时长：<font color='blue'><B>"+onlineTimeStr+"</B></font>";
		statisticInfo_lb.setValue(info);
		statisticInfo_lb.setWidth("-1px");
	}

	/**
	 * 由于postgresql 查出的统计时间差值形式一般为：754 days 561:04:31.449
	 * 根据查的在线时长，进行解析，统计出其在线的具体年、月、日、时、分、秒 
	 * @param durationInfoStr 查的的时长字符串
	 */
	private ArrayList<Integer> formatTimeInteval(String durationInfoStr) {
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;

		String[] dts = durationInfoStr.split(" ");
		if(dts != null && dts.length > 10) {
			dts[10] = dts[10].substring(0, dts[10].indexOf("."));
			day = Integer.parseInt(dts[4]);
			hour = Integer.parseInt(dts[6]);
			minute = Integer.parseInt(dts[8]);
			second = Integer.parseInt(dts[10]);
			int addHour = minute / 60;
			int addDay = (hour + addHour) / 24;
			day = day + addDay;
			hour = (hour + addHour) % 24;
			minute = minute % 60;
		}
		
		ArrayList<Integer> durationInfos = new ArrayList<Integer>();
		durationInfos.add(day);
		durationInfos.add(hour);
		durationInfos.add(minute);
		durationInfos.add(second);
		return durationInfos;
	}

	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), CdrTabView.this);
	}
	
	/**
	 * 刷新呼叫记录显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			cdrTableFlipOver.refreshToFirstPage();
		} else {
			cdrTableFlipOver.refreshInCurrentPage();
		}
	}
	
	/**
	 * 当电话振铃时，弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(customDialPopupWindow);
		// 回显弹窗中各组件的信息
		customDialPopupWindow.echoInformations(customerResource);
		customDialPopupWindow.setAgiChannel(agiChannel);
		customDialPopupWindow.center();
		this.getApplication().getMainWindow().addWindow(customDialPopupWindow);
	}
}
