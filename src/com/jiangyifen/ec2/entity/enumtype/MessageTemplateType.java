package com.jiangyifen.ec2.entity.enumtype;

public enum MessageTemplateType {
	
	system("系统",0),  csr("座席",1);
	
	//成员变量
	private String typeName;
	private int index;
	
	//构造方法
	private MessageTemplateType(String typeName,int index){
		this.typeName = typeName;
		this.index = index;
	}

	
	//setter和getter方法
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}


	@Override
	public String toString() {
		
		return typeName;
	}
	
	
	
	

}
