package com.jiangyifen.ec2.ui.mgr.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
/**
 * 按资源搜索
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class ResourceSearch extends VerticalLayout implements Button.ClickListener{
	/**
	 * 基本条件组件
	 */
	private TextField nameField;//姓名
	private OptionGroup optionGroup;//性别
	private PopupDateField birthdayFrom;	//生日开始
	private PopupDateField birthdayTo;	//生日结束
	private TextField phoneField;
	private ListSelect companySelect;
	private ListSelect addressSelect;

	/**
	 * 扩展字段组件
	 */
	private GridLayout extendGridLayout;
	private Button statuAdd;
	//记录扩展组件的按钮，由按钮记录组件一行的状态
	private List<Button> deleteLineButtonList;
	
	/**
	 * 搜索和复原按钮
	 */
	private Button search;
	private Button discard;
	
	/**
	 * 持有父组件的引用
	 */
	private SearchTabSheet tabSheet;
	
	/**
	 * 构造器
	 */
	public ResourceSearch(SearchTabSheet tabSheet) {
		this.setSizeFull();
		this.tabSheet=tabSheet;
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);
		
		//约束基本条件和扩展条件不要占满整行
		HorizontalLayout constrantConditionLayout = new HorizontalLayout();
		constrantConditionLayout.setSpacing(true);
		constrantLayout.addComponent(constrantConditionLayout);
		
		//添加基本字段组件
		constrantConditionLayout.addComponent(buildBasicCondition());
		
		//添加扩展字段组件
		constrantConditionLayout.addComponent(buildExtendCondition());
		
		//添加搜索和复原按钮
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		search=new Button("搜索");
		search.addListener(this);
		buttonsLayout.addComponent(search);
		
		discard=new Button("复原");
		discard.addListener(this);
		buttonsLayout.addComponent(discard);
		constrantConditionLayout.addComponent(buttonsLayout);
		constrantConditionLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);
	}
	
	/**
	 * 创建基本条件输出组件
	 * @return
	 */
	private Panel buildBasicCondition() {
		Panel basicConditionPanel=new Panel("基本条件");
		basicConditionPanel.setWidth("-1px");
		basicConditionPanel.addStyleName("light");
		basicConditionPanel.addStyleName("notopborder");
		
		//Grid布局管理器
		GridLayout gridLayout=new GridLayout(4,4);//4列4行
		gridLayout.setSpacing(true);
		basicConditionPanel.addComponent(gridLayout);
		
		//姓名
		gridLayout.addComponent(new Label("姓名"),0,0);
		nameField=new TextField();
		nameField.setWidth("150px");
		gridLayout.addComponent(nameField,1,0);
		
		//性别
		gridLayout.addComponent(new Label("性别"),2,0);
		optionGroup=new OptionGroup();
		List<String> options = Arrays.asList("男","女");
		final Container c = new IndexedContainer();
        if (options != null) {
            for (final Iterator<?> i = options.iterator(); i.hasNext();) {
                c.addItem(i.next());
            }
        }
        optionGroup.setWidth("100px");
        optionGroup.setContainerDataSource(c);
		optionGroup.setStyleName("twocolchb");
		optionGroup.setEnabled(false);//默认为不可用状态
		//标识是不是可以用用户性别组件，因为性别是单选，选中即无法取消选中
		final CheckBox enableGender=new CheckBox("可用");
		enableGender.setImmediate(true);
		enableGender.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//如果是选中状态，则设置OptionGroup为可用
				if((Boolean)(enableGender.getValue())){
					optionGroup.setEnabled(true);
				}else{
					optionGroup.setEnabled(false);
					optionGroup.setValue(null);
				}
			}
		});
		HorizontalLayout genderLayout=new HorizontalLayout();
		genderLayout.addComponent(optionGroup);
		genderLayout.addComponent(enableGender);
		gridLayout.addComponent(genderLayout,3,0);
		
		//生日开始
		gridLayout.addComponent(new Label("生日1"),0,1);
		birthdayFrom = new PopupDateField();
		birthdayFrom.setWidth("160px");
		birthdayFrom.setDateFormat("yyyy-MM-dd");
		birthdayFrom.setResolution(PopupDateField.RESOLUTION_DAY);
		birthdayFrom.setImmediate(true);
		birthdayFrom.setWidth("150px");
		gridLayout.addComponent(birthdayFrom,1,1);

		//生日结束
		gridLayout.addComponent(new Label("生日2"),2,1);
		birthdayTo = new PopupDateField();
		birthdayTo.setWidth("160px");
		birthdayTo.setDateFormat("yyyy-MM-dd");
		birthdayTo.setResolution(PopupDateField.RESOLUTION_DAY);
		birthdayTo.setImmediate(true);
		birthdayTo.setWidth("150px");
		gridLayout.addComponent(birthdayTo,3,1);
		
		//电话
		gridLayout.addComponent(new Label("电话"),0,2);
		phoneField=new TextField();
		phoneField.setWidth("150px");
		gridLayout.addComponent(phoneField,1,2);
		
		//公司
		gridLayout.addComponent(new Label("公司"),0,3);
		companySelect=new ListSelect();
		companySelect.setRows(3);
		companySelect.setWidth("150px");
		gridLayout.addComponent(companySelect,1,3);

		//地址
		gridLayout.addComponent(new Label("地址"),2,3);
		addressSelect=new ListSelect();
		addressSelect.setRows(3);
		addressSelect.setWidth("150px");
		gridLayout.addComponent(addressSelect,3,3);
		
		return basicConditionPanel;
	}

	
	/**
	 * 创建扩展字段组件
	 * @return
	 */
	private Panel buildExtendCondition() {
		Panel extendConditionPanel=new Panel("扩展条件");
		extendConditionPanel.setHeight("100%");
		extendConditionPanel.addStyleName("light");
		extendConditionPanel.addStyleName("notopborder");
		
		//Grid布局管理器
		extendGridLayout=new GridLayout(3,11);
		extendGridLayout.setHeight("100%");
		extendGridLayout.setSpacing(true);
		//添加布局管理器的第一行组件
		Label descKey=new Label("描述字段");
		descKey.setWidth("155px");
		Label descValue=new Label("描述结果");
		descValue.setWidth("155px");
		extendGridLayout.addComponent(descKey,0,0);
		extendGridLayout.addComponent(descValue,1,0);
		statuAdd=new Button();
		statuAdd.setStyleName(BaseTheme.BUTTON_LINK);
		statuAdd.addListener(this);
		statuAdd.setIcon(ResourceDataMgr.status_add);
		extendGridLayout.addComponent(statuAdd,2,0);
		//初始化存储每一行的List
		deleteLineButtonList=new ArrayList<Button>();
		extendConditionPanel.addComponent(extendGridLayout);
		
		return extendConditionPanel;
	}
	

	/**
	 * 移除按钮对应的自己的这一行组件
	 * @param button
	 */
	private void removeSelfLine(Button deleteButton) {
		//从List集合中移除Button
		deleteLineButtonList.remove(deleteButton);
		
		//重新布局所有组件的输出
		reArrangeExtendLayout();
	}
	/**
	 * 添加一行扩展组件
	 */
	private void addOneExtendCondition() {
		if(deleteLineButtonList.size()>=10){
			NotificationUtil.showWarningNotification(this, "目前支持的扩展组件个数为10个，不能再添加！");
			return;
		}
		//添加前面的两个TextField
		List<TextField> keyValueList=new ArrayList<TextField>();
		TextField key=new TextField();
		keyValueList.add(key);
		TextField value=new TextField();
		keyValueList.add(value);
		//创建按钮组件
		Button deleteLine=new Button();
		deleteLine.addListener(this);
		deleteLine.setStyleName(BaseTheme.BUTTON_LINK);
		deleteLine.setIcon(ResourceDataMgr.status_delete);
		deleteLine.setData(keyValueList);
		//将按钮添加到全局的List中
		deleteLineButtonList.add(deleteLine);
		//重新布局所有组件的输出
		reArrangeExtendLayout();
	}
	
	/**
	 * 重新布局扩展组件
	 */
	@SuppressWarnings("unchecked")
	private void reArrangeExtendLayout(){
		//保留第一行，其它行移除
		for(int row=1;row<extendGridLayout.getRows();row++){
			extendGridLayout.removeComponent(0, row);
			extendGridLayout.removeComponent(1, row);
			extendGridLayout.removeComponent(2, row);
		}
		//从第二行开始添加，重新布局
		for(int i=1;i<deleteLineButtonList.size()+1;i++){
			Button deleteButton=deleteLineButtonList.get(i-1);
			List<TextField> fields=(List<TextField>)deleteButton.getData();
			extendGridLayout.addComponent(fields.get(0),0,i);
			extendGridLayout.addComponent(fields.get(1),1,i);
			extendGridLayout.addComponent(deleteButton,2,i);
			
		}
	}
	/**
	 * 清除所有更改
	 */
	private void executeDiscard() {
		nameField.setValue("");
		birthdayFrom.setValue(null);
		birthdayTo.setValue(null);
		phoneField.setValue("");
		companySelect.setValue(null);
		addressSelect.setValue(null);
		optionGroup.setValue(null);//如果不可用，自然是null，如果可用，可以供用户选择，临时是null
		deleteLineButtonList.clear();
		//重新布局所有组件的输出
		reArrangeExtendLayout();
	}
	
	/**
	 * 执行搜索
	 * @param 
	 */
	@SuppressWarnings("unchecked")
	private void executeSearch(){
		//手动控制sqlField和allSqlPart同步
		List<String> sqlBasicField=new ArrayList<String>();
		List<String> sqlBasicValue=new ArrayList<String>();
		
		//搜索名字部分的处理
		String nameStr="";
		if(nameField.getValue()!=null){
			nameStr=nameField.getValue().toString().trim();
		}
		if(!nameStr.equals("")){
			sqlBasicField.add("name");
			sqlBasicValue.add(" name like '%"+nameStr+"%' ");
		}
		
		//生日的处理 TODO chb 资源全局搜索
		//电话的处理
		//性别的处理
		//公司的处理
		//地址的处理
		
		//扩展字段
		List<String> sqlExtendKey=new ArrayList<String>();
		List<String> sqlExtendValue=new ArrayList<String>();
		
		//扩展条件的处理
		for(Button button:deleteLineButtonList){
			List<TextField> twoField=(List<TextField>)button.getData();
			TextField keyField=twoField.get(0);
			TextField valueField=twoField.get(1);
			//如果一行中有一个组件为null，则继续循环
			if(keyField.getValue()==null||valueField.getValue()==null) continue;
			//取出一行中组件的Str值
			String keyFieldStr=keyField.getValue().toString().trim();
			String valueFieldStr=valueField.getValue().toString().trim();
			//如果一行中有一个组件为空字符串，则继续循环
			if(keyFieldStr.equals("")||valueFieldStr.equals("")) continue;
			sqlExtendKey.add(keyFieldStr);
			sqlExtendValue.add(valueFieldStr);
		}
				
		//SQL语句格式  select * from ec2_customer_resource,ec2_customer_resource_description where  (key in(*)) and (value in(*))
		//Sql 基本字段的前半句部分
		String asterBasicField="";
		Iterator<String> basicFieldIter = sqlBasicField.iterator();
		while(basicFieldIter.hasNext()){
			asterBasicField+=basicFieldIter.next();
			if(basicFieldIter.hasNext()) asterBasicField+=",";
		}
		//Sql 基本字段的后半句部分
		String asterBasicValue="";
		Iterator<String> basicValueIter = sqlBasicValue.iterator();
		while(basicValueIter.hasNext()){
			asterBasicValue+=basicValueIter.next();
			if(basicValueIter.hasNext()) asterBasicValue+=" and ";
		}
		//Sql 的后半句的Key部分
		String asterKey="(";
		Iterator<String> keyIter = sqlExtendKey.iterator();
		while(keyIter.hasNext()){
			asterKey+="'"+keyIter.next()+"'";
			if(keyIter.hasNext()) asterKey+=",";
		}
		asterKey+=")";
		//Sql 的后半句的Value部分
		String asterValue="(";
		Iterator<String> valueIter = sqlExtendValue.iterator();
		while(valueIter.hasNext()){
			asterValue+="'"+valueIter.next()+"'";
			if(valueIter.hasNext()) asterValue+=",";
		}
		asterValue+=")";
		
		//正式组装Sql语句
		String selectSql="";
		String countSql="";
		//如果没有设置搜索条件，则只显示姓名字段，并且并不搜描述表
		if(sqlBasicField.isEmpty()&&sqlExtendKey.isEmpty()){
			selectSql="select name,birthday from ec2_customer_resource";
			countSql="select count(*) from ec2_customer_resource";
		}else if(sqlBasicField.isEmpty()&&!sqlExtendKey.isEmpty()){
			selectSql="select name,key,value from ec2_customer_resource where key in"+asterKey+" and value in"+asterValue;
			countSql="select count(*) from ec2_customer_resource where key in"+asterKey+" and value in"+asterValue;
		}else if(!sqlBasicField.isEmpty()&&sqlExtendKey.isEmpty()){
			selectSql="select "+asterBasicField+",birthday from ec2_customer_resource,ec2_customer_resource_description where "+asterBasicValue+" ";
			countSql="select count(*) from ec2_customer_resource,ec2_customer_resource_description where "+asterBasicValue+" ";
		}else if(!sqlBasicField.isEmpty()&&!sqlExtendKey.isEmpty()){
			selectSql="select "+asterBasicField+",key,value from ec2_customer_resource,ec2_customer_resource_description " +
					"where "+asterBasicValue+" and key in"+asterKey+" and value in"+asterValue;
			countSql="select count(*) from ec2_customer_resource,ec2_customer_resource_description " +
					"where "+asterBasicValue+" and key in"+asterKey+" and value in"+asterValue;
		}
		

//System.out.println("selectSql:"+selectSql);
//System.out.println("countSql:"+countSql);
		
		//TODO chb  资源全局搜索
		tabSheet.executeSearch(selectSql, countSql);
	}
	/**
	 * 监听按钮的的点击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==statuAdd){
			addOneExtendCondition();
			//如果按钮中记录的东西是一个集合，说明是一个删除自身的按钮
			//这样做有些牵强，但是按钮比较少，暂时先这样做
		}else if(event.getButton()==search){
			executeSearch();
		}else if(event.getButton()==discard){
			executeDiscard();
		}else if(event.getButton().getData() instanceof List){
			removeSelfLine(event.getButton());
		}
	}
}
