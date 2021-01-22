package com.jiangyifen.ec2.bean;
/**
 * 电话号码外线归属地
 * @author chb
 *
 */
public class MobileAreacode {
	private String mobileArea;
	private String areaCode;
	
	public MobileAreacode(String mobileArea,String areaCode) {
		this.mobileArea=mobileArea;
		this.areaCode=areaCode;
	}

	
	public String getMobileArea() {
		return mobileArea;
	}


	public void setMobileArea(String mobileArea) {
		this.mobileArea = mobileArea;
	}


	public String getAreaCode() {
		return areaCode;
	}


	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}


	@Override
	public String toString() {
		return mobileArea+"("+areaCode+")";
	}
	
}
