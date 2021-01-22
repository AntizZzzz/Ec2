package com.jiangyifen.ec2.updateui;

import java.util.Map;

public interface Updateable {
	
	/**
	 * 根据传递的值更新相应的组件
	 * @param datamap	map<要修改的对象的引用名称，修改后的值>
	 */
	public void update(Map<String, Object> datamap);
	
}
