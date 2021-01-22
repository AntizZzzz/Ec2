package com.jiangyifen.ec2.ui.csr.workarea.myresource;

import org.vaadin.addon.customfield.CustomField;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.service.eaoservice.CityService;
import com.jiangyifen.ec2.service.eaoservice.CountyService;
import com.jiangyifen.ec2.service.eaoservice.ProvinceService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * 自定义地址编辑组件，返回类型是Address.class
 * 	可以直接获取到地址对象
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddAddressField extends CustomField {

    private ComboBox provinceSelector;			// 地址 省份
    private ComboBox citySelector;				// 地址 城市
    private ComboBox countySelector;			// 地址 区县
    private TextArea streetArea;				// 地址 街道
    
    private Address address;
	private BeanItemContainer<Province> provinceContainer;
	private BeanItemContainer<City> cityContainer;
	private BeanItemContainer<County> countyContainer;
	
	private ProvinceService provinceService;	// 省份服务类
	private CityService cityService;			// 城市服务类
	private CountyService countyService;		// 县区服务类
    
    public AddAddressField() {
    	address = new Address();
    	
		provinceService = SpringContextHolder.getBean("provinceService");	
		cityService = SpringContextHolder.getBean("cityService");	
		countyService = SpringContextHolder.getBean("countyService");	

		provinceContainer = new BeanItemContainer<Province>(Province.class);
		provinceContainer.addAll(provinceService.getAll());
		cityContainer = new BeanItemContainer<City>(City.class);
		countyContainer = new BeanItemContainer<County>(County.class);
		
    	// 创建主要的组件
		VerticalLayout addressMainVLayout = new VerticalLayout();
		addressMainVLayout.setSpacing(true);
		setCompositionRoot(addressMainVLayout);
		
		// 创建省的选择框
		createProvinceSelector(addressMainVLayout);
      	
		// 创建 城市 选择框
		createCitySelectior(addressMainVLayout);
     	
		// 创建 县区 选择框
		createCountySelector(addressMainVLayout);
     	
		// 创建 街道 输入区域
		createStreetArea(addressMainVLayout);
    }

	/**
	 * 创建 省份 选择框
	 * @param addressMainVLayout
	 */
	private void createProvinceSelector(VerticalLayout addressMainVLayout) {
		// 创建 省份 选择框
		HorizontalLayout provinceLayout = new HorizontalLayout();
		provinceLayout.setSpacing(true);
		addressMainVLayout.addComponent(provinceLayout);
		
     	provinceSelector = new ComboBox();
     	provinceSelector.setImmediate(true);
     	provinceSelector.setInputPrompt("请选择省 ");
     	provinceSelector.setNullSelectionAllowed(false);
     	provinceSelector.setContainerDataSource(provinceContainer);
     	provinceLayout.addComponent(provinceSelector);
     	
     	Label provinceLabel = new Label("省");
     	provinceLabel.setWidth("-1px");
     	provinceLayout.addComponent(provinceLabel);
     	
     	// 为 省份 选择框添加监听器
 		provinceSelector.addListener(new ValueChangeListener() {
 			@Override
 			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
 				if(provinceSelector.getValue() == null) {
 					citySelector.setReadOnly(true);
 					countySelector.setReadOnly(true);
 				} else {
 					citySelector.setReadOnly(false);
 					citySelector.removeAllItems();
      				cityContainer.addAll(cityService.getAllByProvince((Province)provinceSelector.getValue()));
      				citySelector.setContainerDataSource(cityContainer);
 				}
 			}
 		});
	}
	
	/**
	 * 创建 城市 选择框
	 * @param addressMainVLayout
	 * @return
	 */
	private HorizontalLayout createCitySelectior(
			VerticalLayout addressMainVLayout) {
		// 创建 城市 选择框
		HorizontalLayout cityLayout = new HorizontalLayout();
		cityLayout.setSpacing(true);
		addressMainVLayout.addComponent(cityLayout);
		
     	citySelector = new ComboBox();
     	citySelector.setImmediate(true);
     	citySelector.setReadOnly(true);
     	citySelector.setInputPrompt("请选择城市 ");
     	citySelector.setNullSelectionAllowed(false);
     	citySelector.setContainerDataSource(cityContainer);
     	cityLayout.addComponent(citySelector);
     	
     	Label cityLabel = new Label("市");
     	cityLabel.setWidth("-1px");
     	cityLayout.addComponent(cityLabel);

     	// 为 城市 选择框添加监听器
     	citySelector.addListener(new ValueChangeListener() {
     		@Override
     		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
     			if(citySelector.getValue() == null) {
     				countySelector.setReadOnly(true);
     			} else {
     				countySelector.setReadOnly(false);
     				countySelector.removeAllItems();
     				countyContainer.addAll(countyService.getAllByCity((City)citySelector.getValue()));
     				countySelector.setContainerDataSource(countyContainer);
     			}
     		}
     	});
		return cityLayout;
	}
	
    /**
     *  创建 县区 选择框
     * @param addressMainVLayout
     */
	private void createCountySelector(VerticalLayout addressMainVLayout) {
		HorizontalLayout countyLayout = new HorizontalLayout();
		countyLayout.setSpacing(true);
		addressMainVLayout.addComponent(countyLayout);
		
     	countySelector = new ComboBox();
     	countySelector.setImmediate(true);
     	countySelector.setReadOnly(true);
     	countySelector.setInputPrompt("请选择区县 ");
     	countySelector.setNullSelectionAllowed(false);
     	countySelector.setContainerDataSource(countyContainer);
     	countyLayout.addComponent(countySelector);
     	
     	Label countyLabel = new Label("县/区");
     	countyLabel.setWidth("-1px");
     	countyLayout.addComponent(countyLabel);
	}
	
    /**
     * 创建 街道 输入区域
     * @param addressMainVLayout
     */
	private void createStreetArea(VerticalLayout addressMainVLayout) {
		HorizontalLayout streetLayout = new HorizontalLayout();
		streetLayout.setSpacing(true);
		addressMainVLayout.addComponent(streetLayout);
		
		streetArea = new TextArea();
		streetArea.setRows(2);
		streetArea.setInputPrompt("请输入详细街道");
		streetArea.setNullRepresentation("");
		streetLayout.addComponent(streetArea);
	}
	
	@Override
	public Address getValue() {
		Province province = (Province) provinceSelector.getValue();
		City city = (City) citySelector.getValue();
		County county = (County) countySelector.getValue();
		String street = streetArea.getValue().toString().trim();
		if(province == null && city == null 
				&& county == null && "".equals(street)) {
			return null;
		} else {
			address.setProvince(province);
			address.setCity(city);
			address.setCounty(county);
			address.setStreet(street);
			return address;
		}
	}
	
	/**
	 * 设置自定义的ComboBox 的宽度
	 * @param width
	 */
	@Override
	public void setWidth(String width) {
		provinceSelector.setWidth(width);
     	citySelector.setWidth(width);
     	countySelector.setWidth(width);
     	streetArea.setWidth(width);
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		provinceSelector.setReadOnly(readOnly);
	}

	@Override
	public Class<?> getType() {
		return Address.class;
	}
}
