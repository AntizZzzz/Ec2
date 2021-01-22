package com.jiangyifen.ec2.entity.enumtype;


public enum SmsPhoneNumberType {
	CONTACTS("联系人", 0), HISTORY("历史联系人" ,1);
	
	private String name;
	private int index;
	
	private SmsPhoneNumberType(String name, int index){
		this.name = name;
		this.index = index;
	}
	
	// 普通方法
		public static String getName(int index) {
			for (SmsPhoneNumberType type : SmsPhoneNumberType.values()) {
				if (type.getIndex() == index) {
					return type.name;
				}
			}
			return null;
		}

	//======================  Setter and Getter ==============================//
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
