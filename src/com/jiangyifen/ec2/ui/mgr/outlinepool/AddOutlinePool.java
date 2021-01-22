package com.jiangyifen.ec2.ui.mgr.outlinepool;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserOutlinePoolManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 添加号码池
 * 
 * @author chb
 */
@SuppressWarnings("serial")
public class AddOutlinePool extends Window implements Button.ClickListener {
	private static final Object[] POOL_COL_ORDER = new Object[] { "name", "description" };
	private static final String[] POOL_COL_NAME = new String[] { "号码池名称", "描述信息" };

	/**
	 * 主要组件输出
	 */
	// Form输出
	private Form form;

	// 保存按钮和取消按钮
	private Button save;
	private Button cancel;

	/**
	 * 其他组件
	 */
	// 持有Department控制对象的引用
	private UserOutlinePoolManagement userOutlinePoolManagement;
	private OutlinePool outlinePool;
	private OutlinePoolService outlinePoolService;

	public AddOutlinePool(UserOutlinePoolManagement userOutlinePoolManagement) {
		this.center();
		this.setModal(true);
		this.userOutlinePoolManagement = userOutlinePoolManagement;

		// Service初始化
		outlinePoolService = SpringContextHolder.getBean("outlinePoolService");

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// From输出
		form = new Form();
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.addStyleName("chb");
		// 创建Form时提前取出所有的角色，以用于回显做准备
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(buildButtonsLayout());
		windowContent.addComponent(form);
	}

	/**
	 * 按钮输出区域
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}

	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo() {
		// 则自己新建部门
		this.setCaption("新建号码池");
		outlinePool = new OutlinePool();

		// 设置Form的数据源
		form.setItemDataSource(new BeanItem<OutlinePool>(outlinePool), Arrays.asList(POOL_COL_ORDER));
		for (int i = 0; i < POOL_COL_ORDER.length; i++) {
			form.getField(POOL_COL_ORDER[i]).setCaption(POOL_COL_NAME[i]);
		}
	}

	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private boolean executeSave() {
		try {
			outlinePool.setDomain(SpringContextHolder.getDomain());
			outlinePool.setCreateTime(new Date());
			String poolNum = new SimpleDateFormat("yyyyMMddHHmmssss").format(new Date()) + new Double(Math.random() * 1000).intValue();
			outlinePool.setPoolNum(poolNum);
			boolean existed = outlinePoolService.existByName(outlinePool.getName(), outlinePool.getDomain());
			if (existed) {
				this.getApplication().getMainWindow().showNotification("该号码池名称已存在，请重新输入!",
						Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			outlinePoolService.update(outlinePool);
			// 刷新deptManagement的Table到首页
			userOutlinePoolManagement.updateTable(true);
			userOutlinePoolManagement.getPoolTable().setValue(null);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存号码池失败，可能是信息填写有误！",
					Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	private class MyFieldFactory extends DefaultFieldFactory {

		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if ("name".equals(propertyId)) {
				TextField empNoField = new TextField();
				empNoField.setNullRepresentation("");
				empNoField.setRequired(true);
				empNoField.setRequiredError("号码池名称不能为空");
				empNoField.setWidth("270px");
				empNoField.addValidator(
						new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,64}$", "号码池名称 只能由长度不大于64位的字符组成(可以为汉字)"));
				return empNoField;
			} else if ("description".equals(propertyId)) {
				TextArea noteArea = new TextArea();
				noteArea.setColumns(20);
				noteArea.setRows(5);
				noteArea.setWordwrap(true);
				noteArea.setNullRepresentation("");
				noteArea.setInputPrompt("请输入与号码池相关的描述信息！");
				noteArea.addValidator(
						new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,200}$", "号码池描述 只能由长度不大于2位的字符组成(可以为汉字)"));
				return noteArea;
			} else {
				return null;
			}
		}
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		outlinePool = null;
		updateFormInfo();
	}

	/**
	 * 点击保存、取消按钮的事件
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == save) {
			try {
				form.commit();
			} catch (Exception e) {
				this.showNotification(e.getMessage());
				return;
			}
			boolean saveSuccess = executeSave();
			if (saveSuccess) {
				this.getParent().removeWindow(this);
			}
		} else if (source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}
}
