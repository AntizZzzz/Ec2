package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PushNumStatus;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class SatisNumManager extends VerticalLayout {

	private Table table;
	private TextField textField;
	private List<TextField> textFields = new ArrayList<TextField>();
	private Button operation;
	private List<Button> buttons = new ArrayList<Button>();

	private String[] columnHeaders = new String[] { "按键", "状态名", "操作" };

	private Object[] visibleColumns = new Object[] { "number", "statusName",
			"operation" };

	private CommonService commonService = SpringContextHolder
			.getBean("commonService");

	// 获取登陆用户
	private User loginUser = (User) SpringContextHolder.getHttpSession()
			.getAttribute("loginUser");
	private Domain domain;
	private BeanItemContainer<PushNumStatus> beanItemContainer;
	private boolean isEdit = true;// 是否可编辑，默认是编辑状态
	private Map<Button, TextField> componentMap = new HashMap<Button, TextField>();

	// private PushNumStatus numStatus;

	public SatisNumManager() {

		domain = loginUser.getDomain();

		this.setMargin(true);
		this.setImmediate(true);

		table = new Table();
		table.setPageLength(11);

		table.addGeneratedColumn("statusName", new ColumnGenerator() {

			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				if (columnId.equals("statusName")) {

					textField = new TextField();
					textField.setValue(((PushNumStatus) itemId).getStatusName());
					textField.setReadOnly(true);

					// 存list
					textFields.add(textField);

					return textField;
				}

				return null;
			}
		});

		table.addGeneratedColumn("operation", new ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId,
					Object columnId) {

				if (columnId.equals("operation")) {

					operation = new Button();
					operation.setData(itemId);
					operation.setStyleName(BaseTheme.BUTTON_LINK);

					// 存list
					buttons.add(operation);

					operation.setCaption("编辑");

					operation.addListener(new Button.ClickListener() {

						@Override
						public void buttonClick(ClickEvent event) {

							// 存map，保持组件对应
							for (int i = 0; i < 10; i++) {

								componentMap.put(buttons.get(i),
										textFields.get(i));
							}

							operation = event.getButton();
							textField = componentMap.get(operation);

							if (isEdit) {

								textField.setReadOnly(false);
								operation.setCaption("保存");
								isEdit = false;

							} else {
								PushNumStatus pushNumStatus = (PushNumStatus) operation.getData();

								String statusName = null;
								if(textField.getValue() != null) {
									statusName = textField.getValue().toString();
								}
								
								// 状态名
								pushNumStatus.setStatusName(statusName);
								// 所在域
								pushNumStatus.setDomain(domain);

								// 更新
								commonService.update(pushNumStatus);

								textField.setReadOnly(true);
								operation.setCaption("编辑");
								isEdit = true;
							}

						}
					});

					return operation;
				}
				return null;
			}
		});

		List<PushNumStatus> list = commonService
				.getEntitiesByJpql("select p from PushNumStatus as p order by p.number");

		// set container
		beanItemContainer = new BeanItemContainer<PushNumStatus>(
				PushNumStatus.class);

		if (list.size() != 10) {

			// create entity from zero to nine
			PushNumStatus zeroStatus = new PushNumStatus();
			zeroStatus.setNumber("0");
			zeroStatus.setStatusName("");
			zeroStatus.setDomain(domain);
			PushNumStatus oneStatus = new PushNumStatus();
			oneStatus.setNumber("1");
			oneStatus.setStatusName("");
			oneStatus.setDomain(domain);
			PushNumStatus twoStatus = new PushNumStatus();
			twoStatus.setNumber("2");
			twoStatus.setStatusName("");
			twoStatus.setDomain(domain);
			PushNumStatus threeStatus = new PushNumStatus();
			threeStatus.setNumber("3");
			threeStatus.setStatusName("");
			threeStatus.setDomain(domain);
			PushNumStatus fourStatus = new PushNumStatus();
			fourStatus.setNumber("4");
			fourStatus.setStatusName("");
			fourStatus.setDomain(domain);
			PushNumStatus fiveStatus = new PushNumStatus();
			fiveStatus.setNumber("5");
			fiveStatus.setStatusName("");
			fiveStatus.setDomain(domain);
			PushNumStatus sixStatus = new PushNumStatus();
			sixStatus.setNumber("6");
			sixStatus.setStatusName("");
			sixStatus.setDomain(domain);
			PushNumStatus sevenStatus = new PushNumStatus();
			sevenStatus.setNumber("7");
			sevenStatus.setStatusName("");
			sevenStatus.setDomain(domain);
			PushNumStatus eightStatus = new PushNumStatus();
			eightStatus.setNumber("8");
			eightStatus.setStatusName("");
			eightStatus.setDomain(domain);
			PushNumStatus nineStatus = new PushNumStatus();
			nineStatus.setNumber("9");
			nineStatus.setStatusName("");
			nineStatus.setDomain(domain);

			// save to DB
			commonService.save(zeroStatus);
			commonService.save(oneStatus);
			commonService.save(twoStatus);
			commonService.save(threeStatus);
			commonService.save(fourStatus);
			commonService.save(fiveStatus);
			commonService.save(sixStatus);
			commonService.save(sevenStatus);
			commonService.save(eightStatus);
			commonService.save(nineStatus);

			// bind container
			beanItemContainer.addItem(zeroStatus);
			beanItemContainer.addItem(oneStatus);
			beanItemContainer.addItem(twoStatus);
			beanItemContainer.addItem(threeStatus);
			beanItemContainer.addItem(fourStatus);
			beanItemContainer.addItem(fiveStatus);
			beanItemContainer.addItem(sixStatus);
			beanItemContainer.addItem(sevenStatus);
			beanItemContainer.addItem(eightStatus);
			beanItemContainer.addItem(nineStatus);

		} else {

			for (PushNumStatus pushNumStatus : list) {

				beanItemContainer.addItem(pushNumStatus);

			}
		}

		table.setContainerDataSource(beanItemContainer);
		table.setWidth("50%");
		table.setStyleName("striped");
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		this.addComponent(table);

	}
}
