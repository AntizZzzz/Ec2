package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.MobileAreacode;
import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.UserOutline;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserOutlineService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.outlinemanage.AddOutline;
import com.jiangyifen.ec2.ui.mgr.outlinemanage.ConfMultiOutlineWindow;
import com.jiangyifen.ec2.ui.mgr.outlinemanage.EditOutline;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr.otil.OutlineToIvrLinkManageView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 外线管理 组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class OutlineManagement extends VerticalLayout implements Property.ValueChangeListener, Action.Handler, ClickListener {
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "name", "username", "host", "secret", "context", 
			"type", "qualify", "canreinvite", "disallow", "allow", "isDefaultOutline", "ispopupWin", "nat", "call_limit"};
	private final String[] VISIBLE_HEADERS = new String[] {"ID", "外线名称","注册名称","SIP服务器","密码", "呼出路由", 
			"注册类型", "心跳确认", "二次拨号", "不允许编码", "允许编码", "是否默认","是否弹屏","内网穿透","并发限制" };
	
	//搜索组件
	private TextField keyword;
	private Button search;
	
	//外线表格组件
	private Table table;
	private String searchSql;
	private String countSql;
	private SipConfigService sipConfigService;
	private ReloadAsteriskService reloadAsteriskService;
	private FlipOverTableComponent<SipConfig> flip;
	
	//外线表格按钮组件
	private ComboBox selectType_cb;							// 选择类型（全选当前页、反选当前页等）
	private Button add;
	private Button edit;
	private Button delete;
	private Button confMulti;
	
	/**
	 * 右键组件
	 */
	private Action ADD= new Action("添加");
	private Action EDIT= new Action("编辑");
	private Action DELETE= new Action("删除");
	private Action CONF_MULTI = new Action("批量配置勾选项");

	/**
	 * 弹出窗口
	 */
	//弹出窗口 只创建一次
	private AddOutline addOutlineWindow;
	private EditOutline editOutlineWindow;
	private ConfMultiOutlineWindow confMultiOutlineWindow;
	private OutlineToIvrLinkManageView outlineToIvrLinkManageView;
	private Window ivr_to_outline_wd;

	/**
	 * 其他变量
	 */
	private Domain domain;
	private LinkedHashMap<Long,SipConfig> needDeleteSips;
	private MarketingProjectService marketingProjectService;
	private UserOutlineService userOutlineService;
	
	/**
	 * 构造器	
	 */
	public OutlineManagement() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("100%");
		
		needDeleteSips = new LinkedHashMap<Long,SipConfig>();
		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		userOutlineService = SpringContextHolder.getBean("userOutlineService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		// 创建队列表格上方的组件（包括搜索组件及多项删除组件）
		createTableTopComponents();

		// 创建外线表格显示区组件
		createSipConfigTable();
		
		// 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
		createTableFooterComponents();
		
	}

	/**
	 *  创建搜索需要的响应组件
	 */
	private void createTableTopComponents() {
		// 创建搜索外线的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		this.addComponent(searchHLayout);
		
		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		
		keyword = new TextField();
		keyword.setImmediate(true);
		keyword.setInputPrompt("请输入搜索关键字");
		keyword.setDescription("可按'外线名称'进行搜索");
		keyword.setStyleName("search");
		keyword.addListener(this);
		searchHLayout.addComponent(keyword);
		
		search = new Button("搜索", this);
		search.setImmediate(true);
		searchHLayout.addComponent(search);
	}
	
	/**
	 *  创建外线表格显示区组件
	 */
	private void createSipConfigTable() {
		table = createFormatColumnTable();
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.addActionHandler(this);
		table.setPageLength(20);
		this.addComponent(table);
	}

	/**
	 *  创建格式化 了 显示内容的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object v = property.getValue();
				
				//判断yes and no
				if(v!=null&&v instanceof String) {
					String value=(String)v;
					if("yes".equals(value)){
						return "是";
					}else if("no".equals(value)){
						return "否";
					}
					//continue
				}
				
				if(property.getValue() == null) {
					return "";
				} else if(colId.equals("isDefaultOutline")) {
					boolean value = (Boolean) property.getValue();
					if(value == true) {
						return "是";
					} 
					return "";
				} else if(colId.equals("ispopupWin")) {
					boolean value = (Boolean) property.getValue();
					return value ? "呼叫-弹屏" : "呼叫-不弹屏";
				}else if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				} else if("context".equals(colId)) {
					if("incoming".equals(v)){
						return "呼出";
					}else if("outgoing".equals(v)){
						return "呼入";
					}
				} else if("type".equals(colId)) {
					if("friend".equals(v)){
						return "任意IP";
					}else{
						//fail to default
					}
				} else if("disallow".equals(colId)) {
					if("all".equals(v)){
						return "所有";
					}else{
						//fail to default
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		
//		// 创建表格
//				table = new Table() {
//					@Override
//					protected String formatPropertyValue(Object rowId, Object colId,
//							Property property) {
//						Object v = property.getValue();
//						if (v instanceof Date) {
//							// 缺点是每创建一行就创建一次SimpleDateFormat对象
//							return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
//									.format(v);
//						} else if("context".equals(colId)) {
//							if("incoming".equals(v)){
//								return "呼出";
//							}else if("outgoing".equals(v)){
//								return "呼入";
//							}
//						} else if("type".equals(colId)) {
//							if("friend".equals(v)){
//								return "任意IP";
//							}else{
//								//fail to default
//							}
//						} else if("disallow".equals(colId)) {
//							if("all".equals(v)){
//								return "所有";
//							}else{
//								//fail to default
//							}
//						}
//						return super.formatPropertyValue(rowId, colId, property);
//					}
//				};
	}

	
	/**
	 *  创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
	 */
	private void createTableFooterComponents() {
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		footerHLayout.setWidth("100%");
		this.addComponent(footerHLayout);
		
		HorizontalLayout leftFooter = new HorizontalLayout();
		leftFooter.setSpacing(true);
		footerHLayout.addComponent(leftFooter);

		Label selectType_lb = new Label("<b>全选/反选：</b>", Label.CONTENT_XHTML);
		selectType_lb.setWidth("-1px");
		leftFooter.addComponent(selectType_lb);
		leftFooter.setComponentAlignment(selectType_lb, Alignment.MIDDLE_LEFT);
		
		selectType_cb = new ComboBox();
		selectType_cb.setWidth("100px");
		selectType_cb.addListener(this);
		selectType_cb.setImmediate(true);
		selectType_cb.setNullSelectionAllowed(true);
		selectType_cb.addItem("SelectCurrentPage");
		selectType_cb.addItem("InvertCurrentPage");
		selectType_cb.addItem("RemoveCurrentPage");
		selectType_cb.addItem("SelectAllPages");
		selectType_cb.addItem("InvertAllPages");
		selectType_cb.addItem("RemoveAllPages");
		selectType_cb.setItemCaption("SelectCurrentPage", "全选当前页");
		selectType_cb.setItemCaption("InvertCurrentPage", "反选当前页");
		selectType_cb.setItemCaption("RemoveCurrentPage", "不选当前页");
		selectType_cb.setItemCaption("SelectAllPages", "全选所有页");
		selectType_cb.setItemCaption("InvertAllPages", "反选所有页");
		selectType_cb.setItemCaption("RemoveAllPages", "不选所有页");
		leftFooter.addComponent(selectType_cb);
		leftFooter.setComponentAlignment(selectType_cb, Alignment.MIDDLE_LEFT);
		
		add = new Button("添 加", this);
		add.setImmediate(true);
		leftFooter.addComponent(add);
		
		edit = new Button("编 辑", this);
		edit.setImmediate(true);
		edit.setEnabled(false);
		leftFooter.addComponent(edit);
		
		delete = new Button("删 除", this);
		delete.setImmediate(true);
		delete.setEnabled(false);
		leftFooter.addComponent(delete);
		
		confMulti = new Button("批量配置勾选项", this);
		confMulti.setImmediate(true);
		confMulti.setEnabled(false);
		leftFooter.addComponent(confMulti);
		
		// 初始化搜索语句
		initializeSql();
		
		flip = new FlipOverTableComponent<SipConfig>(SipConfig.class, sipConfigService, table, searchSql, countSql, null);
		flip.setPageLength(20, false);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(VISIBLE_HEADERS);
		table.addGeneratedColumn("belong", new BelongColumnGenerator());
		table.setColumnHeader("belong", "归属地");
		table.addGeneratedColumn("delete", new DeleteColumnGenerator());
		table.setColumnHeader("delete", "勾选项");
		table.addGeneratedColumn("operators", new OperatorColumnGenerator());
		table.setColumnAlignment("operators", Table.ALIGN_CENTER);
		table.setColumnHeader("operators", "操作");
		footerHLayout.addComponent(flip);
		footerHLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据不同的需求，初始化查询语句
	 */
	private void initializeSql() {
		String keywordValue = StringUtils.trimToEmpty((String) keyword.getValue());
		String keyWordSql = "";
		if(!"".equals(keywordValue)) {
			keyWordSql = " and s.name like '%" + keywordValue + "%'";
		}
		String sipTypeSql = "(" +getSipTypeSql(SipConfigType.sip_outline) +"," +getSipTypeSql(SipConfigType.gateway_outline) +")";
		
		countSql = "Select count(s) from SipConfig as s where s.domain.id = " +domain.getId()+ " and s.sipType in " +sipTypeSql +keyWordSql;
		searchSql = countSql.replaceFirst("count\\(s\\)", "s") + " order by s.name asc";
	}

	/**
	 * jrh 根据SipConfig对象，得到用于查询sip 的类型创建收索语句语句
	 * @param 	sipConfigType 	sip类型
	 * @return 	String
	 */
	private String getSipTypeSql(SipConfigType sipConfigType) {
		String statuStr = sipConfigType.getClass().getName() + ".";
		if (sipConfigType.getIndex() == 0) {
			statuStr += "exten";
		} else if (sipConfigType.getIndex() == 1) {
			statuStr += "sip_outline";
		} else if (sipConfigType.getIndex() == 2) {
			statuStr += "gateway_outline";
		}
		return statuStr;
	}
	
	/**
	 * 归属生成
	 */
	private class BelongColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final SipConfig sip = (SipConfig) itemId;
			final Label belong= new Label();
			String areaCode=sip.getBelong();
			if(areaCode != null) {
				MobileAreacode mobileAreacode = ShareData.areacodeMap.get(areaCode);
				if(mobileAreacode!=null){
					belong.setValue(mobileAreacode.toString());
				}else{
					belong.setValue("无区号");
				}
			} else {
				belong.setValue("无区号");
			}
			return belong;
		}
	}
	
	/**
	 * 用于自动生成“删除”列，用于标记 是否将对应行的外线删除
	 */
	private class DeleteColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final SipConfig sip = (SipConfig) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if(needDeleteSips.containsKey(sip.getId())) {
				check.setValue(true);
			}
			
			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if(value == false) {
						needDeleteSips.remove(sip.getId());
					} else {
						needDeleteSips.put(sip.getId(), sip);
					}
					
					// 修改删除按钮
					updateButtonStatus();
				}
			});
			
        	return check;
		}
	}

	/**
	 * 用于自动生成各种操作按钮显示组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(final Table source, final Object itemId, Object columnId) {
			final SipConfig outline = (SipConfig) itemId;
			
			Button ivr_link_detail_bt = new Button("IVR关联管理");
			ivr_link_detail_bt.setStyleName(BaseTheme.BUTTON_LINK);

			ivr_link_detail_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					showOutlineToIvrLinkWindow(source, itemId, outline);
				}
			});
			
//			PopupView ivr_link_detail = new PopupView(new Content() {
//				@Override
//				public Component getPopupComponent() {
//					if(outlineToIvrLinkManageView == null) {
//						outlineToIvrLinkManageView = new OutlineToIvrLinkManageView();
//					}
//					return outlineToIvrLinkManageView;
//				}
//				
//				@Override
//				public String getMinimizedValueAsHTML() {
//					return "IVR关联管理";
//				}
//			});
//			
//			ivr_link_detail.setHideOnMouseOut(false);
//			ivr_link_detail.addListener(new ComponentAttachListener() {
//				@Override
//				public void componentAttachedToContainer(ComponentAttachEvent event) {
//					outlineToIvrLinkManageView.updateUiDataSource(outline);
//					source.select(itemId);
//				}
//			});

			HorizontalLayout opeartorLayout = new HorizontalLayout();
			opeartorLayout.setSpacing(true);
			opeartorLayout.addComponent(ivr_link_detail_bt);
//			opeartorLayout.addComponent(ivr_link_detail);
			return opeartorLayout;
		}
	}

	/**
	 * @Description 描述：显示外线与IVR的关联窗口
	 *
	 * @author  JRH
	 * @date    2014年10月21日 下午5:36:41
	 * @param source
	 * @param itemId
	 * @param outline void
	 */
	private void showOutlineToIvrLinkWindow(final Table source, final Object itemId, final SipConfig outline) {
		if(outlineToIvrLinkManageView == null) {
			outlineToIvrLinkManageView = new OutlineToIvrLinkManageView();
		}
		
		outlineToIvrLinkManageView.updateUiDataSource(outline);
		source.select(itemId);
		
		if(ivr_to_outline_wd == null) {
			ivr_to_outline_wd = new Window();
		}
		ivr_to_outline_wd.setResizable(false);
		ivr_to_outline_wd.setSizeUndefined();
		ivr_to_outline_wd.center();
		ivr_to_outline_wd.setContent(outlineToIvrLinkManageView);

		table.getApplication().getMainWindow().removeWindow(ivr_to_outline_wd);
		table.getApplication().getMainWindow().addWindow(ivr_to_outline_wd);
	}
	
	/**
	 *  修改按钮状态，如删除按钮、批量编辑按钮等
	 */
	private void updateButtonStatus() {
		if(needDeleteSips.size() > 0) {
			delete.setCaption("删除勾选项");
			delete.setEnabled(true);
			confMulti.setEnabled(true);
		} else {
			delete.setCaption("删 除");
			confMulti.setEnabled(false);
			delete.setEnabled(table.getValue() != null);
		}
	}
	
	/**
	 * 当用户点击的添加外线按钮时，buttonClick调用显示 添加 外线窗口
	 */
	private void showAddWindow() {
		if(addOutlineWindow==null){
			addOutlineWindow=new AddOutline(this);
		}
		this.getWindow().addWindow(addOutlineWindow);
	}
	
	/**
	 * 当用户点击的编辑外线按钮时，buttonClick调用显示 编辑 外线窗口
	 */
	private void showEditWindow() {
		if(editOutlineWindow==null){
			editOutlineWindow=new EditOutline(this);
		}
		this.getWindow().addWindow(editOutlineWindow);
	}

	/**
	 * 当用户点击的批量编辑外线按钮时，buttonClick调用显示 编辑 外线窗口
	 */
	private void showConfMultiExtenWindow() {
		if(confMultiOutlineWindow == null){
			confMultiOutlineWindow = new ConfMultiOutlineWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(confMultiOutlineWindow);
		this.getApplication().getMainWindow().addWindow(confMultiOutlineWindow);
	}
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			SipConfig target = (SipConfig) table.getValue();
			edit.setEnabled(target != null);
			if(target == null && needDeleteSips.size() == 0) {
				delete.setEnabled(false);
			} else {
				delete.setEnabled(true);
			}
		} else if(source == keyword) {
			search.click();
		} else if(source == selectType_cb) {
			String value = (String) selectType_cb.getValue();
			if(value == null) {
				return;
			}
			
			if("SelectCurrentPage".equals(value)) {
				for(Object outline : table.getItemIds()) {
					Long id = ((SipConfig) outline).getId();
					needDeleteSips.put(id, (SipConfig)outline);
				}
			} else if("InvertCurrentPage".equals(value)) {
				// 保存之前在当前页选中的Id 号
				Set<Long> originalIds = new HashSet<Long>();
				for(Object outline : table.getItemIds()) {
					Long id = ((SipConfig) outline).getId();
					if(needDeleteSips.containsKey(id)) {	// 如果之前选中了，则加入set集合
						originalIds.add(id);
					}
					needDeleteSips.put(id, (SipConfig)outline);
				}
				// 再将在当前页原来选中的去除
				for(Long oId : originalIds) {
					needDeleteSips.remove(oId);
				}
			} else if("SelectAllPages".equals(value)) {
				String searchSql = flip.getSearchSql();
				List<SipConfig> sips = sipConfigService.getAllByJpql(searchSql);
				for(SipConfig outline : sips) {
					needDeleteSips.put(outline.getId(), outline);
				}
			} else if("InvertAllPages".equals(value)) {
				// 保存之前所有选中的Id 号
				Set<Long> originalIds = new HashSet<Long>(needDeleteSips.keySet());
				// 先将所有的客户放入集合
				String searchSql = flip.getSearchSql();
				List<SipConfig> sips = sipConfigService.getAllByJpql(searchSql);
				for(SipConfig outline : sips) {
					needDeleteSips.put(outline.getId(), outline);
				}
				// 再将原来选中的去除
				for(Long oId : originalIds) {
					needDeleteSips.remove(oId);
				}
			} else if("RemoveCurrentPage".equals(value)) {
				for(Object outline : table.getItemIds()) {
					Long id = ((SipConfig) outline).getId();
					needDeleteSips.remove(id);
				}
			} else if("RemoveAllPages".equals(value)) {
				needDeleteSips.clear();
			} 
			flip.refreshInCurrentPage();
			
			// 修改删除按钮
			updateButtonStatus();
			selectType_cb.setValue(null);
		}
	}
	
	/**
	 * 处理各种操作按钮的单击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search) {
			initializeSql();
			flip.setCountSql(countSql);
			flip.setSearchSql(searchSql);
			flip.refreshToFirstPage();
			table.setValue(null);
		} else if(source == add) {
			showAddWindow();
		} else if(source == edit) {
			showEditWindow();
		} else if(source == confMulti) {
			showConfMultiExtenWindow();
		} else if(source == delete) {
			executeDelete();
		} 
	}

	/**
	 * 执行删除
	 */
	private void executeDelete() {
		int size = needDeleteSips.size();
		if(size > 0) {
			ArrayList<Long> sipIds = new ArrayList<Long>(needDeleteSips.keySet());
			for(int i = 0; i < size; i++) {
				Long oId = sipIds.get(i);
				SipConfig sip = needDeleteSips.get(oId);
				if(confirmSipDeleteAble(sip) == true) {
					// 先删除该外线与用户的静态关联
					List<UserOutline> userOutlines = userOutlineService.getAllByOutline(sip, domain);
					for(UserOutline userOutline : userOutlines) {
						userOutlineService.deleteById(userOutline.getId());
					}
					// 删除外线
					sipConfigService.deleteById(sip.getId());
					
					/********************** 更新内存 *********************/
					// 删除域中对应的当前外线
					List<String> outlineList = ShareData.domainToOutlines.get(domain.getId());
					outlineList.remove(sip.getUsername());
					// 删除外线对应的呼入呼出黑名单
					ShareData.outlineToIncomingBlacklist.remove(sip.getId());
					ShareData.outlineToOutgoingBlacklist.remove(sip.getId());
					// 从内存中移除外线与IVR 的关联
					ShareData.outlineIdToIvrLinkMap.remove(sip.getId());
				}
				// 不管能不能删，都应该将其从集合中移除
				needDeleteSips.remove(oId);
			}
		} else {
			SipConfig sip = (SipConfig) table.getValue();
			if(sip != null && confirmSipDeleteAble(sip) == true) {
				// 先删除该外线与用户的静态关联
				List<UserOutline> userOutlines = userOutlineService.getAllByOutline(sip, domain);
				for(UserOutline userOutline : userOutlines) {
					userOutlineService.deleteById(userOutline.getId());
				}
				
				// 删除外线
				sipConfigService.deleteById(sip.getId());

				// 删除域中对应的当前外线
				List<String> outlineList = ShareData.domainToOutlines.get(domain.getId());
				outlineList.remove(sip.getUsername());
				// 删除外线对应的呼入呼出黑名单
				ShareData.outlineToIncomingBlacklist.remove(sip.getId());
				ShareData.outlineToOutgoingBlacklist.remove(sip.getId());
				// 从内存中移除外线与IVR 的关联
				ShareData.outlineIdToIvrLinkMap.remove(sip.getId());
			}
		}

		// 更新队列在asterisk 中的配置文件  sip_regirster_domainname.conf
		sipConfigService.updateAsteriskRegisterSipConfigFile(domain);
		// 更新队列在asterisk 中的配置文件  sip_outline_domainname.conf
		sipConfigService.updateAsteriskOutlineSipConfigFile(domain);
		// 修改配置文件成功， 则重新加载asterisk 的配置文件
		reloadAsteriskService.reloadSip();
		
		table.setValue(null);
		delete.setCaption("删 除");	
		delete.setEnabled(false);
		
		updateTable(false);
	}

	/**
	 * 检验外线是否已经有用户在使用，如果是则返回false 表示该外线不可删除
	 * 并且检查该外线是否已经于项目关联，如果关联了，则不可删
	 * @param sip 外线
	 * @return
	 */
	private boolean confirmSipDeleteAble(SipConfig sip) {
		// 检查当前外线是否为默认外线，如果是，则不让删除
		if(sip.getIsDefaultOutline() == true) {
			this.getApplication().getMainWindow().showNotification("外线 " +sip.getUsername()+ " 是当前域的默认外线，请修改默认外线后再试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		// 检查是否有正在执行的项目在使用当前外线，如果有则不让删
		if(ShareData.extenToDynamicOutline.containsValue(sip.getUsername())) {
			this.getApplication().getMainWindow().showNotification("外线 " +sip.getUsername()+ " 正在被使用，暂不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		// 检查是否有用户正在使用当前外线，如果有则不让删
		if(ShareData.extenToStaticOutline.containsValue(sip.getUsername())) {
			this.getApplication().getMainWindow().showNotification("外线 " +sip.getUsername()+ " 被配置了外线成员，请删除后外线成员后再试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		// 检查是否有项目与当前外线关联，如果有则不让删
		if(marketingProjectService.getAllBySip(sip).size() > 0) {
			this.getApplication().getMainWindow().showNotification("外线 " +sip.getUsername()+ " 已经有项目与之关联，暂不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		// jrh 2014-02-21	检查是否与之关联的IVRAction 对象，如果有，则表示不能删除
		boolean isDeleteAble = sipConfigService.checkDeleteAbleByIvrAction(false, sip.getName(), domain.getId()); 
		if(!isDeleteAble) {
			this.getApplication().getMainWindow().showNotification("分机 " +sip.getName()+ " 正在与IVR(呼入语音导航关联)，暂不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[]{ADD, CONF_MULTI};
		} 
		return new Action[]{ADD, EDIT, DELETE, CONF_MULTI};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if(ADD==action){
			add.click();
		}else if(EDIT==action){
			edit.click();
		}else if(DELETE==action){
			delete.click();
		}else if(CONF_MULTI==action){
			if(needDeleteSips.size() > 0) {
				confMulti.click();
			} else {
				this.getApplication().getMainWindow().showNotification("请您先勾选要修改的外线！", Notification.TYPE_WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * 刷新显示外线的表格
	 * @param isToFirst 刷新后是否跳转至第一页
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(searchSql);
			flip.setCountSql(countSql);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	public Table getTable() {
		return table;
	}

	public LinkedHashMap<Long, SipConfig> getNeedDeleteSips() {
		return needDeleteSips;
	}

	public void clearNeedDeleteSips() {
		needDeleteSips.clear();
		updateButtonStatus();
	}

}
