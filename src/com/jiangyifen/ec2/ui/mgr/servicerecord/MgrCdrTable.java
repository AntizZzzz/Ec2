package com.jiangyifen.ec2.ui.mgr.servicerecord;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
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

@SuppressWarnings("serial")
public class MgrCdrTable extends VerticalLayout{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件
	 */
	// 项目表格组件
	private Table table;
	private BeanItemContainer<Cdr> container;
	private CdrService cdrService; // 呼叫记录服务类
	
	//录音基础路径
	private String baseUrl=BaseUrlUtils.getBaseUrl();
	
	// jrh 2013-7-2
	private Label player; 					// 播放器显示标签
	private HorizontalLayout playerLayout; 	// 存放播放器的布局管理器
	private boolean isEncryptMobile = true;	// 判断电话是否需要加密

	private TelephoneService telephoneService;			// 电话号码服务类
	
	/**
	 * 构造器
	 */
	/**
	 * @param createSecretMobile
	 */
	public MgrCdrTable(final boolean createSecretMobile) {
		this.setSpacing(true);
		this.isEncryptMobile = createSecretMobile;
		telephoneService=SpringContextHolder.getBean("telephoneService");
		cdrService = SpringContextHolder.getBean("cdrService");
		
		
		//设置Table
		table = new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if(property.getValue() == null) {
					return "";
				}
				
				if(createSecretMobile == true) {
					if("src".equals(colId)) {
						String src = (String) property.getValue();
						return telephoneService.encryptMobileNo(src);
					} else if("destination".equals(colId)) {
						String destination = (String) property.getValue();
						return telephoneService.encryptMobileNo(destination);
					} 
				}
				
				if(property.getType() == Date.class) {
						return dateFormat.format((Date)property.getValue());
				} else if("isAutoDial".equals(colId)) {
					boolean value = (Boolean) property.getValue();
					return value ? "自动群呼" : "手动呼叫";
				} else if("isBridged".equals(colId)) {
					boolean value = (Boolean) property.getValue();
					return value ? "已接通" : "";
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.setImmediate(true);
		table.setSizeFull();
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setColumnAlignment("calldate", Table.ALIGN_CENTER);
		
		//为Table设置Container数据源
		container=new BeanItemContainer<Cdr>(Cdr.class);
		table.setContainerDataSource(container);
		
		//设置表格显示
		Object[] visibleColumns= new Object[] {"startTimeDate", "duration", "ec2_billableSeconds", "cdrDirection", 
				"isBridged", "recordFileName", "url", "src", "destination", "srcDeptName", "srcEmpNo", "srcRealName", "srcUsername", 
				"destDeptName", "destEmpNo", "destRealName", "destUsername", "projectName", "isAutoDial", "uniqueId"};
		String[] columnHeaders = new String[] {"开始时间", "呼叫时长", "计费时长", "呼叫方向", "接通情况", "录音文件名称", "试听/下载录音", 
				"主叫号码", "被叫号码", "主叫部门", "主叫分机", "主叫姓名", "主叫用户名", "被叫部门", "被叫分机", "被叫姓名", "被叫用户名", 
				"项目名称", "呼叫类型", "唯一标识"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		
		//添加试听录音组件
		table.addGeneratedColumn("url", new Table.ColumnGenerator() {
			public Object generateCell(Table source, Object itemId, Object columnId) {
				final Cdr cdr = (Cdr) itemId;
				String tmpUrl = null;
				if(isEncryptMobile) {
					tmpUrl = cdr.getRealUrl(baseUrl);
				} else {
					tmpUrl = cdr.getUrl(baseUrl);
				}
				final String url = tmpUrl;
				
				if (url != null) {
					// jrh
					Button listen = new Button("试听");
					listen.setStyleName(BaseTheme.BUTTON_LINK);
					listen.setIcon(ResourceDataCsr.listen_type_ico);
					listen.addListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							if (player != null) {
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

					HorizontalLayout layout = new HorizontalLayout();
					layout.setSpacing(true);
					layout.setWidth("100%");
					layout.addComponent(listen);
					layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);
					
					if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_SERVICE_RECORD)) {
						Link typelink = new Link("下载", new ExternalResource(url));
						typelink.setIcon(ResourceDataCsr.down_type_ico);
						typelink.setTargetName("_blank");
						
//						//TODO The button with a caption is created by lc,as follows. 
//						final Button download = new Button("下载");
//						download.setIcon(ResourceDataCsr.down_type_ico);
//						download.setStyleName(BaseTheme.BUTTON_LINK);
//						if(cdr.getIsDownloaded()){
//							download.addStyleName("red");
//						}
//						download.addListener(new Button.ClickListener() {
//							
//							@Override
//							public void buttonClick(ClickEvent event) {
//								 
//								download.addStyleName("red");
//							 
//								downloadRecord(url);
//								
//								updateCdr(cdr);
//							}
//						});
						
						layout.addComponent(typelink);
						layout.setComponentAlignment(typelink, Alignment.MIDDLE_RIGHT);
						source.setColumnHeader(columnId, "试听/下载录音");
					} else {
						source.setColumnHeader(columnId, "试听录音");
					}

					return layout;
				} else {
					return new Label("无录音文件");
				}
			}
		});
		
		table.setColumnWidth("url", 100);
		table.setColumnAlignment("url", Table.ALIGN_CENTER);
		this.addComponent(table);

		HorizontalLayout tableFooterLayout = new HorizontalLayout();
		tableFooterLayout.setWidth("100%");
		tableFooterLayout.setSpacing(true);
		this.addComponent(tableFooterLayout);

		// 创建播放录音组件
		createPlayerLayout(tableFooterLayout);
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
			operationLog.setDescription("mgr 客服记录");
			operationLog.setProgrammerSee(sql);
			CommonService commonService = SpringContextHolder
					.getBean("commonService");
			commonService.save(operationLog);
		}

	/**
	 * 创建播放录音组件
	 * 
	 * @param tableFooterLayout
	 */
	private void createPlayerLayout(HorizontalLayout tableFooterLayout) {
		playerLayout = new HorizontalLayout();
		playerLayout.setWidth("100%");
		playerLayout.setSpacing(true);
		tableFooterLayout.addComponent(playerLayout);

		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 "
				+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		player.setWidth("100%");
		playerLayout.addComponent(player);
	}
	
	//设置Table的新数据源
	public void setTableItems(List<Cdr> items){
		container.removeAllItems();
		container.addAll(items);
	}
}
