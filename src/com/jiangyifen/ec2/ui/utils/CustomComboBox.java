package com.jiangyifen.ec2.ui.utils;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;

/**
 * 自定义ComboBox 组件，该组件可以通过传入的 map 对象,将map 的values 作为ComboBox 的数据源，
 * 	而为ComboBox 设置默认值的时候传递的是 key 的值，然后根据key 来找到对应的 value ，
 *  其 getValue 的方法是 通过Map 的 value 值找到对应的 key 然后便于存入数据库中 (数据库中存储的信息是 key 的值)
 *  
 *  作用：针对数据库是存的是一种语言格式 或数据类型，而在Form 等组件中需要显示的确实翻译过来的另一种语言或类型，那么就可以使用这个自定义组件
 *  	DataBase 内存     		UI 界面显示内容
 *  	    英文(中文)				中文(英文)
 *  	 Boolean(String)		String(Boolean)		如 true 在界面显示 'yes' 或者 '是'
 *  
 *  	其他的类型应用都可以实现
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomComboBox extends CustomField {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ComboBox comboBox;
	private Map<?, ?> newDataSource;
	
	public CustomComboBox() {
		// 创建主要的组件
		comboBox = new ComboBox();
		comboBox.setImmediate(true);
		this.setCompositionRoot(comboBox);
	}
	
	@Override
	public Class<?> getType() {
		return Object.class;
	}
	
	/**
	 * 获取的是HashMap 的 key
	 */
	@Override
	public Object getValue() {
		Object value = (Object) comboBox.getValue();
		for(Object key : newDataSource.keySet()) {
			if(newDataSource.get(key).equals(value)) {
				return key;
			}
		}
		return null;
	}
	
	/**
	 * 为自定义的ComboBox 设置，其实质是为 vaddin 的ComboBox 设置值
	 * @param object  传入的是 HashMap 的 key 值
	 */
	@Override
	public void setValue(Object object) {
		if(object == null) {
			comboBox.setValue(null);
		} else {
			comboBox.setValue(newDataSource.get(object));
		}
	}
	
	/**
	 * 设置自定义的ComboBox 是否允许空选项
	 * @param nullSelectionAllowed
	 */
	public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
		comboBox.setNullSelectionAllowed(nullSelectionAllowed);
	}
	
	/**
	 * 设置自定义的ComboBox 的数据源
	 * @param newDataSource
	 */
	public void setDataSource(Map<?, ?> newDataSource) {
		this.newDataSource = newDataSource;
		BeanItemContainer<Object> container = new BeanItemContainer<Object>(Object.class);
		container.addAll(newDataSource.values());
		comboBox.setContainerDataSource(container);
	}
	
	@Override
	public void setDescription(String description) {
		comboBox.setDescription(description);
	}
	
	/**
	 * 设置自定义的ComboBox 的宽度
	 * @param width
	 */
	@Override
	public void setWidth(String width) {
		comboBox.setWidth(width);
	}
	
	/**
	 * 设置自定义的ComboBox 的只读属性
	 * 无论设置传入的是什么值，组件都是只读的
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		comboBox.setReadOnly(readOnly);
	}

	/**
	 * 给ComboBox 添加监听器，并且使用反射来回调，这样就相当于CustomComboBox 自身发生了ValueChange 
	 * @param obj
	 */
	public void addValueChangeListener(final Object obj) {
		comboBox.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				try {
					Method method = obj.getClass().getMethod("reflectForSelectType");
					method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage()+" 使用反射回显信息出现异常!", e);
				}
			}
		});
	}
}
