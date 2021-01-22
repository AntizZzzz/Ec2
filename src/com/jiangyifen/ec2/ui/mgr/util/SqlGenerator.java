package com.jiangyifen.ec2.ui.mgr.util;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 生成Sql语句的条件生成器
 * @author chb
 * <p>Like 自动加 '% %' ,不需再添加</p>
 * <p>可以自动加上域的概念</p>
 */
public class SqlGenerator {
	//升序和降序排列
	public static String ASC="asc";
	public static String DESC="desc";
	
	//要对哪个属性排列
	private String orderByAttribute;
	//升序排列还是降序排列
	private String order;
	
	//AndCondition条件集合
	private ArrayList<Condition> andConditions;
	//OrCondition条件集合
	private ArrayList<Condition> orConditions;
	
	//要查找的实体名字
	private String entityName;
	
	public SqlGenerator(String entityName) {
		this.entityName=entityName;
		andConditions=new ArrayList<SqlGenerator.Condition>();
		orConditions=new ArrayList<SqlGenerator.Condition>();
	}
	/**
	 * 添加与条件
	 * @param condition
	 * @return
	 */
	public SqlGenerator addAndCondition(Condition condition){
		andConditions.add(condition);
		return this;
	}
	/**
	 * 移除与条件
	 * @param condition
	 * @return
	 */
	public SqlGenerator removeAndCondition(Condition condition){
		andConditions.remove(condition);
		return this;
	}
	/**
	 * 添加或条件
	 * @param condition
	 * @return
	 */
	public SqlGenerator addOrCondition(Condition condition){
		orConditions.add(condition);
		return this;
	}
	/**
	 * 移除或条件
	 * @param condition
	 * @return
	 */
	public SqlGenerator removeOrCondition(Condition condition){
		orConditions.remove(condition);
		return this;
	}
	
	/**
	 * 对某一属性排序
	 * @param orderByAttribute
	 * @param order
	 * @return
	 */
	public SqlGenerator setOrderBy(String orderByAttribute,String order){
		this.orderByAttribute=orderByAttribute;
		this.order=order;
		return this;
	}
	
	/**
	 * 生成查询语句
	 * @return
	 */
	public String generateSelectSql(){
		String selectSql="select e from "+entityName+" e";
		if(andConditions.size()>0||orConditions.size()>0){
			selectSql+=" where";
			selectSql=addOrConditions(selectSql);
			selectSql=addAndConditions(selectSql);
		}
		
		//加上域的概念
		Long id=new Long(2);
		try {
			id=SpringContextHolder.getDomain().getId();
		} catch (Exception e) {
			Logger logger=LoggerFactory.getLogger(this.getClass());
			logger.warn("取不到Session,或Session里的User,或域");
		}
		
		//如果以Where结尾，进行相应操作
		if(selectSql.endsWith(" where")){
			selectSql+=" e.domain.id="+id;
		}else{
			selectSql+=" and e.domain.id="+id;
		}
		
		//orderByAttribute有效，并且order有效
		if(orderByAttribute!=null&&!orderByAttribute.equals("")){
			if(order!=null&&(order.equals("desc")||order.equals("asc"))){
				selectSql+=" order by e."+orderByAttribute+" "+order;
			}
		}
		Logger logger  =  LoggerFactory.getLogger(this.getClass());
		logger.info("SelectSql: >>>>"+selectSql+"<<<<");
		return selectSql;
	}
	
	/**
	 * 生成计数语句
	 * @return
	 */
	public String generateCountSql(){
		String countSql="select count(e) from "+entityName+" e";
		if(andConditions.size()>0||orConditions.size()>0){
			countSql+=" where";
			countSql=addOrConditions(countSql);
			countSql=addAndConditions(countSql);
		}
		//加上域的概念,默认域为2,以后应去掉
		Long id=new Long(2);
		try {
			id=SpringContextHolder.getDomain().getId();
		} catch (Exception e) {
			Logger logger=LoggerFactory.getLogger(this.getClass());
			logger.warn("取不到Session,或Session里的User,或域");
		}
		
		//如果以Where结尾，进行相应操作
		if(countSql.endsWith(" where")){
			countSql+=" e.domain.id="+id;
		}else{
			countSql+=" and e.domain.id="+id;
		}
				
		Logger logger  =  LoggerFactory.getLogger(this.getClass());
		logger.debug("CountSql: >>"+countSql+"<<");
		return countSql;
	}
	
	//添加所有Or条件
	private String addOrConditions(String selectSql) {
		int size=selectSql.length();
		if(orConditions.size()>0) selectSql+=" (";
		for(Condition condition:orConditions){
			selectSql=addOneCondition(condition,"or",selectSql);
		}
		if(orConditions.size()>0) selectSql+=") ";
		//如果仅仅添加了（），则将空括号移除
		if(orConditions.size()>0&&(selectSql.length()==size+4)){
			selectSql=selectSql.substring(0, size);
		}
		return selectSql;
	}
	//添加所有And条件
	private String addAndConditions(String selectSql) {
		int size=selectSql.length();
//		if(andConditions.size()>0) selectSql+=" (";
		for(Condition condition:andConditions){
			selectSql=addOneCondition(condition,"and",selectSql);
		}
//		if(andConditions.size()>0) selectSql+=") ";
		//如果仅仅添加了（），则将空括号移除
		if(andConditions.size()>0&&(selectSql.length()==size+4)){
			selectSql=selectSql.substring(0, size);
		}
		return selectSql;
	}
	//添加一个Condition条件到Sql
	private String addOneCondition(Condition condition,String type,String selectSql) {
		selectSql=selectSql.trim();
		
		if(selectSql.endsWith("where (")||selectSql.endsWith("where")){
			type="";
		}
		//如果属性为null或“” 则不添加条件
		if(condition.getAttribute()==null||condition.getAttribute().equals("")){
			return selectSql;
		}
		
		//看Condition的类型，并相应处理
		if(condition instanceof Is){
			Is isCondition=(Is)condition;
			//如果是Is boolean值为空不添加条件
			if(isCondition.getBooleanValue()==null){
				return selectSql;
			}
			selectSql+=" "+type+" e."+isCondition.getAttribute()+"= "+isCondition.getBooleanValue();
			return selectSql;
		}else if(condition instanceof Between){
			Between betweenCondition=(Between)condition;
			if((betweenCondition.getFirstValue()==null||betweenCondition.getFirstValue().equals(""))&&(betweenCondition.getSecondValue()==null||betweenCondition.getSecondValue().equals(""))){
				return selectSql;
			}
			//如果是Between 则对值进行相应转换 处理，并添加相应条件
			if(betweenCondition.getIsString()){
				if((betweenCondition.getFirstValue()==null||betweenCondition.getFirstValue().equals(""))&&(betweenCondition.getSecondValue()!=null&&!betweenCondition.getSecondValue().equals(""))){
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+"<='"+betweenCondition.getSecondValue()+"'";
				}else if((betweenCondition.getFirstValue()!=null&&!betweenCondition.getFirstValue().equals(""))&&(betweenCondition.getSecondValue()==null||betweenCondition.getSecondValue().equals(""))){
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+">='"+betweenCondition.getFirstValue()+"'";
				}else{
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+">='"+betweenCondition.getFirstValue()+"' and e."+betweenCondition.getAttribute()+"<='"+betweenCondition.getSecondValue()+"'";
				}
			}else{
				if((betweenCondition.getFirstValue()==null||betweenCondition.getFirstValue().equals(""))&&(betweenCondition.getSecondValue()!=null&&!betweenCondition.getSecondValue().equals(""))){
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+"<="+betweenCondition.getSecondValue();
				}else if((betweenCondition.getFirstValue()!=null&&!betweenCondition.getFirstValue().equals(""))&&(betweenCondition.getSecondValue()==null||betweenCondition.getSecondValue().equals(""))){
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+">="+betweenCondition.getFirstValue();
				}else{
					selectSql+=" "+type+" e."+betweenCondition.getAttribute()+">="+betweenCondition.getFirstValue()+" and e."+betweenCondition.getAttribute()+"<="+betweenCondition.getSecondValue();
				}
			}
			return selectSql;
		}
		
		//如果值为空，直接返回
		if(condition.getValue()==null||condition.getValue().equals("")){
			return selectSql;
		}
		
		if(condition instanceof Equal){
			if(condition.getIsString()){
				selectSql+=" "+type+" e."+condition.getAttribute()+"='"+condition.getValue()+"'";
			}else{
				selectSql+=" "+type+" e."+condition.getAttribute()+"= "+condition.getValue();
			}
		}else if(condition instanceof Like){
			selectSql+=" "+type+" e."+condition.getAttribute()+" like '%"+condition.getValue()+"%'";
		}else if(condition instanceof GreaterOrEqual){
			if(condition.getIsString()){
				selectSql+=" "+type+" e."+condition.getAttribute()+">'"+condition.getValue()+"'";
			}else{
				selectSql+=" "+type+" e."+condition.getAttribute()+">"+condition.getValue();
			}
		}else if(condition instanceof LessOrEqual){
			if(condition.getIsString()){
				selectSql+=" "+type+" e."+condition.getAttribute()+"<'"+condition.getValue()+"'";
			}else{
				selectSql+=" "+type+" e."+condition.getAttribute()+"<"+condition.getValue();
			}
		}
		return selectSql.trim();
	}
	
	/**
	 * 所有生产 Sql的条件语句类的基类
	 * @author chb
	 */
	private  static class Condition{
		private String attribute;
		private String value;
		private Boolean isString;//是字符串还是直接值
		public Condition(String attribute,String value,Boolean isString) {
			this.attribute=attribute.trim();
			if(value!=null){
				this.value=value.trim();
			}else{
				this.value=value;
			}
			//默认是字符串
			if(isString!=null){
				this.isString=isString;
			}else{
				this.isString=true;
			}
		}
		
		public String getAttribute() {
			return attribute;
		}
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}

		public Boolean getIsString() {
			return isString;
		}

		public void setIsString(Boolean isString) {
			this.isString = isString;
		}
	}
	
	/**
	 * 相等条件
	 * @author chb
	 */
	public static final class Equal extends Condition{
		public Equal(String attribute,String value,Boolean isString) {
			super(attribute,value,isString);
		}
	}
	
	/**
	 * 相似条件
	 * @author chb
	 *
	 */
	public static final class Like  extends Condition{
		public Like(String attribute,String value) {
			super(attribute, value,null);
		}
	}
	
	/**
	 * 是否条件
	 * @author chb
	 *
	 */
	public static final class Is  extends Condition{
		private Boolean booleanValue;
		public Is(String attribute,Boolean booleanValue) {
			super(attribute, "",null);
			this.booleanValue=booleanValue;
		}
		public Boolean getBooleanValue() {
			return booleanValue;
		}
		public void setBooleanValue(Boolean booleanValue) {
			this.booleanValue = booleanValue;
		}
	}
	
	/**
	 * 之间条件
	 * @author chb
	 *
	 */
	public static final class Between extends Condition{
		private String firstValue;
		private String secondValue;
		public Between(String attribute,String firstValue,String secondValue,Boolean isString) {
			super(attribute, "",isString);

			if(firstValue!=null){
				this.firstValue=firstValue.trim();
			}else{
				firstValue=null;
			}
			if(secondValue!=null){
				this.secondValue=secondValue.trim();
			}else{
				secondValue=null;
			}
		}
		public String getFirstValue() {
			return firstValue;
		}
		public void setFirstValue(String firstValue) {
			this.firstValue = firstValue;
		}
		public String getSecondValue() {
			return secondValue;
		}
		public void setSecondValue(String secondValue) {
			this.secondValue = secondValue;
		}
		
	}
	
	/**
	 * 大于等于条件
	 * @author chb
	 *
	 */
	public static final class GreaterOrEqual extends Condition{
		public GreaterOrEqual(String attribute,String value,Boolean isString) {
			super(attribute, value,isString);
		}
	}
	
	/**
	 * 小于等于条件
	 * @author chb
	 *
	 */
	public static final class LessOrEqual extends Condition{
		public LessOrEqual(String attribute,String value,Boolean isString) {
			super(attribute, value,isString);
		}
	}
	
}
