package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr.otil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.service.eaoservice.OutlineToIvrLinkService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.utils.ConfirmedWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @Description 描述：外线与IVR 的关联关系管理界面
 * 
 * @author  jrh
 * @date    2014年3月6日 上午9:25:23
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class OutlineToIvrLinkManageView extends VerticalLayout implements Action.Handler, ValueChangeListener, ClickListener {
	
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "ivrMenuId", "priority", "isUseable", "effectTimeScope"};
	
	private final String[] COL_HEADERS = new String[] {"编号", "关联的IVR", "优先级", "禁用状态", "生效时段"};
	
	private Label caption_lb;
	private Table otil_tb;													// 外线与IVR 对应关系的显示表格
	private FlipOverTableComponent<OutlineToIvrLink> otilTableFlip;			// 外线与IVR 对应关系Table的翻页组件

	private final Action ADD = new Action("添加");
	private final Action EDIT = new Action("编辑");
	private final Action DELETE = new Action("删除");
	private final Action FORBID = new Action("禁用");
	private final Action UNFORBID = new Action("启用");
	private final Action UPGRADE = new Action("升级");
	private final Action DOWNGRADE = new Action("降级");
	
	private Button add_bt;
	private Button edit_bt;
	private Button delete_bt;
	private Button forbid_bt;
	private Button unforbid_bt;
	private Button upgrade_bt;
	private Button downgrade_bt;

	private ConfirmedWindow confirmWindow;		// 确认执行的弹屏窗口
	private AddOtilWindow addOtilWindow;		// 添加外线与IVR的关联
	private EditOtilWindow editOtilWindow;		// 编辑外线与IVR的关联
	
	private String searchSql = "";				// 查询语句
	private String countSql = "";				// 统计语句
	
	private SipConfig outline; 
	private OutlineToIvrLink otil;
	public static Map<Long, IVRMenu> ivrMenusMap;
	
	private IvrMenuService ivrMenuService;			// IVRMenu  service
	private OutlineToIvrLinkService outlineToIvrLinkService;
	
	public OutlineToIvrLinkManageView() {
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		this.setSizeUndefined();

		ivrMenusMap = new ConcurrentHashMap<Long, IVRMenu>();

		ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		outlineToIvrLinkService = SpringContextHolder.getBean("outlineToIvrLinkService");

		caption_lb = new Label("", Label.CONTENT_XHTML);
		this.addComponent(caption_lb);
		
		// 创建历史记录表格
		otil_tb = createHistoryOutgoingRecordTable();
		this.addComponent(otil_tb);

		HorizontalLayout tableBottom_hlo = new HorizontalLayout();
		tableBottom_hlo.setSpacing(true);
		tableBottom_hlo.setWidth("100%");
		this.addComponent(tableBottom_hlo);
		
		// 创建操作组件
		HorizontalLayout operators_hlo = createOperatorBts();
		tableBottom_hlo.addComponent(operators_hlo);
		
		// 创建并添加翻页组件
		HorizontalLayout bottomRight_hlo = createTableFlipOver();
		tableBottom_hlo.addComponent(bottomRight_hlo);
		tableBottom_hlo.setComponentAlignment(bottomRight_hlo, Alignment.TOP_RIGHT);
	}

	/**
	 * 创建操作组件
	 * @author jrh
	 * @return
	 */
	private HorizontalLayout createOperatorBts() {
		add_bt = new Button("添 加", this);
		add_bt.setImmediate(true);
		
		edit_bt = new Button("", this);
		edit_bt.setImmediate(true);
		edit_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		edit_bt.addStyleName("borderless");
		
		forbid_bt = new Button("", this);
		forbid_bt.setImmediate(true);
		forbid_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		forbid_bt.addStyleName("borderless");
		
		unforbid_bt = new Button("", this);
		unforbid_bt.setImmediate(true);
		unforbid_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		unforbid_bt.addStyleName("borderless");
		
		delete_bt = new Button("", this);
		delete_bt.setImmediate(true);
		delete_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		delete_bt.addStyleName("borderless");
		
		upgrade_bt = new Button("", this);
		upgrade_bt.setImmediate(true);
		upgrade_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		upgrade_bt.addStyleName("borderless");
		
		downgrade_bt = new Button("", this);
		downgrade_bt.setImmediate(true);
		downgrade_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		downgrade_bt.addStyleName("borderless");

		HorizontalLayout operate_hlo = new HorizontalLayout();
		operate_hlo.setSpacing(true);
		operate_hlo.addComponent(add_bt);
		operate_hlo.addComponent(edit_bt);
		operate_hlo.addComponent(forbid_bt);
		operate_hlo.addComponent(delete_bt);
		operate_hlo.addComponent(upgrade_bt);
		operate_hlo.addComponent(downgrade_bt);
		
		return operate_hlo;
	}

	/**
	 * 创建历史记录表格
	 */
	private Table createHistoryOutgoingRecordTable() {
		Table table = createFormatColumnTable();
		table.setWidth("100%");
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		return table;
	}

	/**
	 * 创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date) property.getValue());
				} else if ("isUseable".equals(colId)) {
					boolean isUseable = (Boolean) property.getValue();
					if(!isUseable) {
						return "已禁用";
					}
					return "";
				} else if ("ivrMenuId".equals(colId)) {
					Long ivrMenuId = (Long) property.getValue();
					IVRMenu ivrMenu = ShareData.ivrMenusMap.get(ivrMenuId);
					if(ivrMenu == null) {	// 一般不会出现，如果出现标识内存维护出现了问题
						ivrMenu = ivrMenuService.getById(ivrMenuId);
					}
					ivrMenusMap.put(ivrMenuId, ivrMenu);
					return ivrMenu.getIvrMenuName()+" --IVR编号："+ivrMenuId;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 创建并添加翻页组件
	 */
	private HorizontalLayout createTableFlipOver() {
		otilTableFlip = new FlipOverTableComponent<OutlineToIvrLink>(OutlineToIvrLink.class, 
				outlineToIvrLinkService, otil_tb, searchSql, countSql, null);
		
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			otilTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		otil_tb.setVisibleColumns(VISIBLE_PROPERTIES);
		otil_tb.setColumnHeaders(COL_HEADERS);
		
		otil_tb.addGeneratedColumn("operators", new OperatorColumnGenerator());
		otil_tb.setColumnAlignment("operators", Table.ALIGN_CENTER);
		otil_tb.setColumnHeader("operators", "操作");
	
		otil_tb.setPageLength(5);
		otilTableFlip.setPageLength(5, false);
	
		HorizontalLayout tableFooterRightLayout = new HorizontalLayout();
		tableFooterRightLayout.setSpacing(true);
		tableFooterRightLayout.setWidth("100%");
		tableFooterRightLayout.addComponent(otilTableFlip);
		tableFooterRightLayout.setComponentAlignment(otilTableFlip, Alignment.TOP_RIGHT);
		
		return tableFooterRightLayout;
	}
	
	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[]{ADD};
		} 
		OutlineToIvrLink link = (OutlineToIvrLink) target;
		if(link.getIsUseable()) {
			return new Action[]{ADD, EDIT, DELETE, FORBID, UPGRADE, DOWNGRADE};
		} 
		return new Action[]{ADD, EDIT, DELETE, UNFORBID, UPGRADE, DOWNGRADE};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		otil_tb.setValue(null);
		otil_tb.select(target);
		if (ADD == action){
			add_bt.click();
		} else if (EDIT == action){
			edit_bt.click();
		} else if (DELETE == action){
			delete_bt.click();
		} else if (UPGRADE == action){
			upgrade_bt.click();
		} else if (DOWNGRADE == action){
			downgrade_bt.click();
		} else if (FORBID == action){
			forbid_bt.click();
		} else if (UNFORBID == action){
			unforbid_bt.click();
		}
	}
	
	/**
	 * 用于自动生成各种操作按钮显示组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		
		public Object generateCell(final Table source, final Object itemId, Object columnId) {

			OutlineToIvrLink link = (OutlineToIvrLink) itemId;
			
			Button inner_upgrade = new Button("升级");
			inner_upgrade.setIcon(ResourceDataMgr.upgrade_14);
			inner_upgrade.setImmediate(true);
			inner_upgrade.addStyleName(BaseTheme.BUTTON_LINK);
			inner_upgrade.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					source.select(itemId);
					upgrade_bt.click();
				}
			});
			
			Button inner_downgrade = new Button("降级");
			inner_downgrade.setIcon(ResourceDataMgr.downgrade_14);
			inner_downgrade.setImmediate(true);
			inner_downgrade.addStyleName(BaseTheme.BUTTON_LINK);
			inner_downgrade.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					source.select(itemId);
					downgrade_bt.click();
				}
			});
			
			Button inner_edit = new Button("编辑");
			inner_edit.setImmediate(true);
			inner_edit.addStyleName(BaseTheme.BUTTON_LINK);
			inner_edit.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					source.select(itemId);
					edit_bt.click();
				}
			});
			
			String fb_caption = link.getIsUseable() ? "禁用" : "启用";
			final Button inner_forbid = new Button(fb_caption);
			inner_forbid.setImmediate(true);
			inner_forbid.addStyleName(BaseTheme.BUTTON_LINK);
			inner_forbid.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					source.select(itemId);
					if("禁用".equals(inner_forbid.getCaption())) {
						forbid_bt.click();
					} else {
						unforbid_bt.click();
					}
				}
			});
			
			Button inner_delete = new Button("删除");
			inner_delete.setImmediate(true);
			inner_delete.addStyleName(BaseTheme.BUTTON_LINK);
			inner_delete.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					source.select(itemId);
					delete_bt.click();
				}
			});

			HorizontalLayout operate_hlo = new HorizontalLayout();
			operate_hlo.setSpacing(true);
			operate_hlo.addComponent(inner_upgrade);
			operate_hlo.addComponent(inner_downgrade);
			operate_hlo.addComponent(inner_edit);
			operate_hlo.addComponent(inner_forbid);
			operate_hlo.addComponent(inner_delete);
			
			return operate_hlo;
		}
		
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == otil_tb) {
			otil = (OutlineToIvrLink) otil_tb.getValue();
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == add_bt) {
			showAddOtilWindow();
		} else if(source == upgrade_bt) {
			executeUpdatePriority(true);
		} else if(source == downgrade_bt) {
			executeUpdatePriority(false);
		} else if(source == edit_bt) {
			showEditOtilWindow();
		} else if(source == forbid_bt) {
			executeUpdateUseableStatus(true);
		} else if(source == unforbid_bt) {
			executeUpdateUseableStatus(false);
		} else if(source == delete_bt) {
			try {
				IVRMenu menu = ivrMenusMap.get(otil.getIvrMenuId());
				String warningMsg = "<b><font color='red'>您确定要删除当前外线 ： <font color='blue'>["+outline.getName()
						+"]</font> 与IVR：<font color='blue'>["+menu.getIvrMenuName()+"]</font>的对应关系吗？？<br/></font></b>";
				showConfirmWindow(warningMsg, "400px", "executeDelete");
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("对不起，删除失败，请重试！！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("管理员删除外线与IVR 的对应关系 (OutlineToIvrLink)时, 出现异常--->"+e.getMessage(), e);
			}
		} 
	}

	/**
	 * 显示添加外线与IVR 关联的显示界面
	 * @author jrh
	 */
	private void showAddOtilWindow() {
		if(addOtilWindow == null) {
			addOtilWindow = new AddOtilWindow(this);
		}
		addOtilWindow.updateUiDataSouce(outline);
		
		this.getApplication().getMainWindow().removeWindow(addOtilWindow);
		this.getApplication().getMainWindow().addWindow(addOtilWindow);
	}
	
	/**
	 * 显示添加外线与IVR 关联的显示界面
	 * @author jrh
	 */
	private void showEditOtilWindow() {
		if(editOtilWindow == null) {
			editOtilWindow = new EditOtilWindow(this);
		}
		
		editOtilWindow.updateUiDataSouce(outline, otil);
		this.getApplication().getMainWindow().removeWindow(editOtilWindow);
		this.getApplication().getMainWindow().addWindow(editOtilWindow);
	}

	/**
	 * 显示确认窗口
	 * @author jrh
	 * @param warningMsg		确认窗口中显示的提示信息
	 * @param msgPxLen			界面显示宽度
	 * @param executeMethod		弹屏中点击确认按钮后，要通过反射，执行的方法
	 */
	private void showConfirmWindow(String warningMsg, String msgPxLen, String executeMethod) {
		confirmWindow = new ConfirmedWindow(warningMsg, msgPxLen, this, executeMethod);
		
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}

	/**
	 * 将当前选中的外线与IVR关联的优先级，进行升级或降级
	 * @author jrh
	 * @param isUpgrade	是否为升级
	 */
	private void executeUpdatePriority(boolean isUpgrade) {
		String grade_caption = isUpgrade ? "升级" : "降级";
		try {
			int priority = otil.getPriority();
			if(priority == 1 && isUpgrade) {	// 如果是升级，并且当前对象的优先级已经是最高的话，提示
				this.getApplication().getMainWindow().showNotification("对不起，当前设置已经是最 高 级别，不能再升级了！！", Notification.TYPE_WARNING_MESSAGE);
				return;
			} 

			ArrayList<OutlineToIvrLink> otilArrLs = ShareData.outlineIdToIvrLinkMap.get(outline.getId());
			if(!isUpgrade) {	// 如果是降级，则检查当前级别是不是已经是最低的了
				if(otilArrLs.size() == 1) {
					this.getApplication().getMainWindow().showNotification("对不起，当前设置已经是最 低 级别，不能再降级了！！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
				OutlineToIvrLink lowestLink = otilArrLs.get(otilArrLs.size() - 1);
				if(priority == lowestLink.getPriority()) {
					this.getApplication().getMainWindow().showNotification("对不起，当前设置已经是最 低 级别，不能再降级了！！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
			}
			
			outlineToIvrLinkService.editOtilPriority(otil, isUpgrade);	// 编辑外线与IVR的关联关系 的优先级别 【注意：一个Link 的升级，必然导致另一个Link 的降级；同理降级也一样】
			otilTableFlip.refreshInCurrentPage();

			// 更新内存中的数据信息
			int relatedIndex = 0;				// 需要更改优先级别的otil 在List中的序号
			int cursor = isUpgrade ? -1 : 1;	
			OutlineToIvrLink currentLink = null;
			for(OutlineToIvrLink link : otilArrLs) {
				if(link.getId().equals(otil.getId())) {
					relatedIndex = otilArrLs.indexOf(link) + cursor;
					link.setPriority(priority+cursor);
					currentLink = link;
					break;
				}
			}
			// 更新内存中牵涉到的另一个 Link，因为，一个升级，必然导致另一个降级
			int relatedCursor = isUpgrade ? 1 : -1;
			OutlineToIvrLink relatedLink = otilArrLs.get(relatedIndex);
			if(relatedLink != null) {
				relatedLink.setPriority(relatedLink.getPriority()+relatedCursor);
				otilArrLs.set(relatedIndex, currentLink);	// 修改后，原来对象在ArrayList 中的位置需要互换
				otilArrLs.set(relatedIndex+relatedCursor, relatedLink);
			}
			
			this.getApplication().getMainWindow().showNotification(grade_caption+"成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("对不起，"+grade_caption+"失败，请重试！！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("管理员"+grade_caption+"外线与IVR 的对应关系 (OutlineToIvrLink)时, 出现异常--->"+e.getMessage(), e);
		}
	}

	/**
	 * 启用或者禁用外线与IVR 的对应关系
	 * @author jrh
	 * @param isforbid	是否禁用
	 */
	private void executeUpdateUseableStatus(boolean isforbid) {
		String fb_caption = isforbid ? "禁用" : "启用";
		try {
			otil.setIsUseable(!isforbid);
			outlineToIvrLinkService.update(otil);
			otilTableFlip.refreshInCurrentPage();
		
			// 更新内存信息
			ArrayList<OutlineToIvrLink> otilArrLs = ShareData.outlineIdToIvrLinkMap.get(outline.getId());
			for(OutlineToIvrLink link : otilArrLs) {
				if(link.getId().equals(otil.getId())) {
					link.setIsUseable(!isforbid);
				}
			}
			
			this.getApplication().getMainWindow().showNotification(fb_caption+"成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("对不起，"+fb_caption+"失败，请重试！！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("管理员"+fb_caption+"外线与IVR 的对应关系 (OutlineToIvrLink)时, 出现异常--->"+e.getMessage(), e);
		}
	}

	/**
	 * 执行删除操作
	 * @author jrh
	 */
	public void executeDelete() {
		try {
			outlineToIvrLinkService.delete(otil);
			ArrayList<OutlineToIvrLink> otilArrLs = ShareData.outlineIdToIvrLinkMap.get(outline.getId());
			OutlineToIvrLink removeLink = null;
			for(OutlineToIvrLink link : otilArrLs) {
				int priority = link.getPriority();
				if(link.getId().equals(otil.getId())) {
					removeLink = link; 
				} else if(link.getPriority() > otil.getPriority()) {
					link.setPriority(priority-1);	// 这里之所以不需要更新数据库，是因为在执行deleteById(id)方法时已经处理了
				}
			}
			if(removeLink != null) {
				otilArrLs.remove(removeLink);
			}
			otilTableFlip.refreshInCurrentPage();
			this.getApplication().getMainWindow().showNotification("删除成功！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("对不起，删除失败，请重试！！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("管理员删除外线与IVR 的对应关系 (OutlineToIvrLink)时, 出现异常--->"+e.getMessage(), e);
		}
	}

	public void updateUiDataSource(SipConfig outline) {
		this.outline = outline;
		ivrMenusMap.clear();
		
		String caption = "当前外线编号：<font color='blue'>"+outline.getId()+"</font>，外线名称：<font color='blue'>"+outline.getName()+"</font>";
		caption_lb.setValue("<b>"+caption+"</b>");
		
		this.countSql = "select count(e) from OutlineToIvrLink as e where e.outlineId = "+outline.getId();
		this.searchSql = countSql.replaceFirst("count\\(e\\)", "e") + " order by e.priority asc";
		otilTableFlip.setSearchSql(searchSql);
		otilTableFlip.setCountSql(countSql);
		otilTableFlip.refreshToFirstPage();
	}

	/**
	 * 是否要刷新到首页
	 * 
	 * @param refreshToFirstPage
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if (refreshToFirstPage == true) {
			otilTableFlip.refreshToFirstPage();
		} else {
			otilTableFlip.refreshInCurrentPage();
		}
	}
	
}
